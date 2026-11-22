package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.theme.*
import com.example.util.ImageSaver

// =========================================================================
// DATA MODELS FOR PRODUCT UPLOAD, SIZES, AGE & VARIANT MATRIX
// =========================================================================

data class VariantMatrixItem(
    val size: String,
    val color: String,
    var mrp: String,
    var sellingPrice: String,
    var stock: String,
    var sku: String,
    var barcode: String
)

data class ProductPhotoAngle(
    val id: String,
    val label: String,
    @DrawableRes val resId: Int
)

data class SizeChartMeasurement(
    val size: String,
    val bustChest: String,
    val waist: String,
    val hip: String,
    val length: String
)

data class EditableCatalogProduct(
    val id: String,
    var title: String,
    var sku: String,
    var category: String,
    var ageGroup: String,
    var ageYears: String,
    var mrp: String,
    var discountPrice: String,
    var stock: String,
    var selectedSizes: List<String>,
    var color: String,
    var fabric: String,
    @DrawableRes var drawableRes: Int
)

// =========================================================================
// FULL PRODUCT UPLOAD & SIZE MANAGEMENT COMPOSABLE
// Matches Screenshots 6:02 & 6:03 + Age, Years & Rich Editing Options
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullSellerProductUploadScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val context = LocalContext.current

    // Upload mode: 0 = Single Upload, 1 = Multiple Upload (4), 2 = Edit Products (8), 3 = Bulk Excel/CSV
    var uploadMode by remember { mutableIntStateOf(1) }
    var skuPrefix by remember { mutableStateOf("GYANVI-ETHNIC") }
    var selectedCategory by remember { mutableStateOf("Sarees & Kurtis") }

    // Dialog states
    var showAiStudioDialog by remember { mutableStateOf(false) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var showSizeChartDialog by remember { mutableStateOf(false) }
    var showVariantMatrixDialog by remember { mutableStateOf(false) }

    // Editing dialog states
    var editingBatchItemIndex by remember { mutableStateOf<Int?>(null) }
    var editingCatalogProduct by remember { mutableStateOf<EditableCatalogProduct?>(null) }

    // -------------------------------------------------------------------------
    // AGE / YEARS SIZE SYSTEMS
    // -------------------------------------------------------------------------
    val ageYearsList = listOf(
        "0-3 Months", "3-6 Months", "6-12 Months",
        "1-2 Years", "2-3 Years", "3-4 Years", "4-5 Years",
        "5-6 Years", "6-7 Years", "7-8 Years", "8-9 Years",
        "9-10 Years", "10-12 Years", "12-14 Years", "14-16 Years",
        "16-18 Years (Youth)", "Adults (18+)"
    )

    val standardSizesList = listOf("XS", "S", "M", "L", "XL", "XXL", "3XL", "4XL", "5XL", "6XL", "7XL")
    val numericSizesList = listOf("28", "30", "32", "34", "36", "38", "40", "42", "44", "46", "48", "50")
    val waistSizesList = listOf("24", "26", "28", "30", "32", "34", "36", "38", "40", "42", "44", "46", "48", "50")

    // Age Group Options
    val ageGroupsList = listOf("All Ages", "Infants (0-1 Yrs)", "Kids (2-7 Yrs)", "Teens (8-16 Yrs)", "Adults (18+ Yrs)")
    var selectedAgeGroup by remember { mutableStateOf("Adults (18+ Yrs)") }
    var selectedAgeYears by remember { mutableStateOf("Adults (18+)") }

    // -------------------------------------------------------------------------
    // SINGLE PRODUCT STATE (Matches Screenshot 6:03)
    // -------------------------------------------------------------------------
    var singleName by remember { mutableStateOf("Anarkali Embroidered Kurti Set") }
    var singleProductId by remember { mutableStateOf("PRD-8941") }
    var singleSku by remember { mutableStateOf("GYANVI-ETHNIC-101") }
    var singleBarcode by remember { mutableStateOf("8904123456789") }
    var singleHsn by remember { mutableStateOf("6204") }
    var singleGstRate by remember { mutableStateOf("5%") }

    var singleMrp by remember { mutableStateOf("1299") }
    var singleDiscount by remember { mutableStateOf("499") }
    var singleColor by remember { mutableStateOf("Royal Rani Pink") }
    var singleFabric by remember { mutableStateOf("Heavy Pure Georgette") }
    var singleStock by remember { mutableStateOf("120") }
    var singleDescription by remember {
        mutableStateOf("Beautiful designer kurti with fine sequin embroidery, matching bottom and dupatta.")
    }

    // Multi-photo gallery
    val photoAngles = remember {
        mutableStateListOf(
            ProductPhotoAngle("1", "Front View", R.drawable.dress_anarkali_1793417013639),
            ProductPhotoAngle("2", "Pallu/Detail", R.drawable.dress_saree_1793417052582),
            ProductPhotoAngle("3", "Flare/Lehenga", R.drawable.dress_lehenga_1793417067159),
            ProductPhotoAngle("4", "Close-up", R.drawable.dress_anarkali_1793417013639),
            ProductPhotoAngle("5", "Back Angle", R.drawable.dress_anarkali_1793417013639),
            ProductPhotoAngle("6", "Fabric Texture", R.drawable.dress_anarkali_1793417013639)
        )
    }
    var selectedPhotoIndex by remember { mutableIntStateOf(0) }

    // Size Modes: 0 = Standard, 1 = Age / Years, 2 = Numeric, 3 = Waist
    var selectedSizeType by remember { mutableIntStateOf(0) }
    val singleSelectedSizes = remember { mutableStateListOf("S", "M", "L", "XL", "XXL") }
    val singleSelectedAgeYears = remember { mutableStateListOf("Adults (18+)") }
    var singleAgeYearsText by remember { mutableStateOf("1-2 Years, 2-3 Years, 3-4 Years, 4-5 Years") }

    // Legal Compliance & Packaging Details (Meesho/Amazon/Govt Mandate)
    var singleManufacturerName by remember { mutableStateOf("Gyanvi Dresses Manufacturing Unit") }
    var singleManufacturerAddress by remember { mutableStateOf("Lal Bazar, Main Road, Bettiah, West Champaran, Bihar") }
    var singleManufacturerPincode by remember { mutableStateOf("845438") }
    var singlePackerName by remember { mutableStateOf("Gyanvi Dresses Packaging & Logistics Hub") }
    var singlePackerId by remember { mutableStateOf("PKR-GD-845438") }
    var singlePackerAddress by remember { mutableStateOf("Industrial Area, Factory Road, Bettiah, Bihar") }
    var singlePackerPincode by remember { mutableStateOf("845438") }

    // Color Swatches
    val colorSwatches = listOf(
        "Royal Rani Pink" to Color(0xFFD81B60),
        "Golden Yellow" to Color(0xFFFBC02D),
        "Navy Blue" to Color(0xFF1565C0),
        "Mint Green" to Color(0xFF26A69A),
        "Maroon" to Color(0xFF880E4F),
        "Crimson Red" to Color(0xFFC62828),
        "Jet Black" to Color(0xFF212121),
        "Off White" to Color(0xFFF5F5F5),
        "Bottle Green" to Color(0xFF1B5E20),
        "Purple Plum" to Color(0xFF6A1B9A)
    )

    // Variant Matrix
    val variantMatrixList = remember {
        mutableStateListOf(
            VariantMatrixItem("S", "Rani Pink", "1299", "499", "25", "GYANVI-S-PINK", "890412345001"),
            VariantMatrixItem("M", "Rani Pink", "1299", "499", "30", "GYANVI-M-PINK", "890412345002"),
            VariantMatrixItem("L", "Rani Pink", "1299", "499", "35", "GYANVI-L-PINK", "890412345003"),
            VariantMatrixItem("XL", "Rani Pink", "1299", "499", "20", "GYANVI-XL-PINK", "890412345004"),
            VariantMatrixItem("XXL", "Rani Pink", "1299", "499", "10", "GYANVI-XXL-PINK", "890412345005")
        )
    }

    // -------------------------------------------------------------------------
    // MULTIPLE PRODUCTS BATCH STATE (Matches Screenshot 6:02)
    // -------------------------------------------------------------------------
    val bulkItems = remember {
        mutableStateListOf(
            BulkUploadItem(
                id = "1",
                title = "Anarkali Embroidered Kurti (Front View)",
                sku = "$skuPrefix-101",
                mrp = "1199",
                discountPrice = "449",
                color = "Rani Pink",
                fabric = "Pure Georgette",
                selectedSizes = listOf("S", "M", "L", "XL", "XXL"),
                stockQuantity = "120",
                description = "Embroidered kurti set",
                drawableRes = R.drawable.dress_anarkali_1793417013639
            ),
            BulkUploadItem(
                id = "2",
                title = "Banarasi Silk Saree (Pallu Close-up)",
                sku = "$skuPrefix-102",
                mrp = "1999",
                discountPrice = "699",
                color = "Golden Yellow",
                fabric = "Banarasi Silk",
                selectedSizes = listOf("S", "M", "L", "XL", "XXL"),
                stockQuantity = "85",
                description = "Zari woven silk saree",
                drawableRes = R.drawable.dress_saree_1793417052582
            ),
            BulkUploadItem(
                id = "3",
                title = "Designer Georgette Lehenga (Full Shot)",
                sku = "$skuPrefix-103",
                mrp = "2499",
                discountPrice = "999",
                color = "Navy Blue",
                fabric = "Net & Velvet",
                selectedSizes = listOf("S", "M", "L", "XL", "XXL"),
                stockQuantity = "60",
                description = "Party wear lehenga",
                drawableRes = R.drawable.dress_lehenga_1793417067159
            ),
            BulkUploadItem(
                id = "4",
                title = "Chikankari Cotton Kurta (Fabric Texture)",
                sku = "$skuPrefix-104",
                mrp = "899",
                discountPrice = "379",
                color = "Mint Green",
                fabric = "100% Cotton",
                selectedSizes = listOf("S", "M", "L", "XL", "XXL"),
                stockQuantity = "150",
                description = "Daily wear cotton kurta",
                drawableRes = R.drawable.dress_anarkali_1793417013639
            )
        )
    }

    // -------------------------------------------------------------------------
    // EXISTING CATALOG PRODUCTS (For Editing Mode)
    // -------------------------------------------------------------------------
    val catalogProducts = remember {
        mutableStateListOf(
            EditableCatalogProduct("1", "Anarkali Embroidered Kurti Set", "GYANVI-ETHNIC-101", "Sarees & Kurtis", "Adults (18+ Yrs)", "Adults (18+)", "1299", "499", "120", listOf("S", "M", "L", "XL", "XXL"), "Royal Rani Pink", "Heavy Pure Georgette", R.drawable.dress_anarkali_1793417013639),
            EditableCatalogProduct("2", "Banarasi Silk Saree with Zari Border", "GYANVI-ETHNIC-102", "Sarees & Kurtis", "Adults (18+ Yrs)", "Free Size", "1999", "699", "85", listOf("Free Size"), "Golden Yellow", "Banarasi Silk", R.drawable.dress_saree_1793417052582),
            EditableCatalogProduct("3", "Designer Georgette Bridal Lehenga", "GYANVI-ETHNIC-103", "Lehengas", "Teens & Adults (14-25 Yrs)", "14-16 Years, Adults", "2499", "999", "60", listOf("M", "L", "XL"), "Navy Blue", "Net & Velvet", R.drawable.dress_lehenga_1793417067159),
            EditableCatalogProduct("4", "Chikankari Lucknowi Cotton Kurta", "GYANVI-ETHNIC-104", "Kurtas", "Adults (18+ Yrs)", "Adults (18+)", "899", "379", "150", listOf("S", "M", "L", "XL", "XXL"), "Mint Green", "100% Cotton", R.drawable.dress_anarkali_1793417013639),
            EditableCatalogProduct("5", "Kids Embroidered Festive Sharara Set", "GYANVI-KIDS-201", "Kids Wear", "Kids (2-7 Yrs)", "4-5 Years, 6-7 Years", "1199", "449", "90", listOf("4-5 Yrs", "6-7 Yrs"), "Rani Pink", "Georgette with Cotton Lining", R.drawable.dress_anarkali_1793417013639),
            EditableCatalogProduct("6", "Baby Girls Festive Georgette Frock", "GYANVI-KIDS-202", "Kids Wear", "Infants & Toddlers (0-3 Yrs)", "1-2 Years, 2-3 Years", "799", "299", "110", listOf("1-2 Yrs", "2-3 Yrs"), "Peacock Blue", "Soft Net & Satin", R.drawable.dress_lehenga_1793417067159),
            EditableCatalogProduct("7", "Teens Bandhani Flared Anarkali", "GYANVI-TEENS-301", "Teens Wear", "Teens (8-16 Yrs)", "10-12 Years, 12-14 Years, 14-16 Years", "1399", "549", "75", listOf("10-12 Yrs", "12-14 Yrs", "14-16 Yrs"), "Maroon", "Chanderi Cotton", R.drawable.dress_anarkali_1793417013639),
            EditableCatalogProduct("8", "Girls Traditional Pattu Pavadai Lehenga", "GYANVI-KIDS-203", "Kids Wear", "Kids (3-8 Yrs)", "3-4 Years, 5-6 Years, 7-8 Years", "1499", "599", "65", listOf("3-4 Yrs", "5-6 Yrs", "7-8 Yrs"), "Golden Pink", "South Silk", R.drawable.dress_saree_1793417052582)
        )
    }

    var catalogSearchQuery by remember { mutableStateOf("") }
    var catalogAgeFilter by remember { mutableStateOf("All Ages") }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("upload_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Text("Image Bulk Upload", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                    }

                    // "Defalt Option" chip in orange (Matches Screenshot 6:02 / 6:03)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF7ED),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFB923C)),
                        modifier = Modifier
                            .clickable {
                                skuPrefix = "GYANVI-ETHNIC"
                                selectedCategory = "Sarees & Kurtis"
                                singleName = "Anarkali Embroidered Kurti Set"
                                singleMrp = "1299"
                                singleDiscount = "499"
                                singleColor = "Royal Rani Pink"
                                singleFabric = "Heavy Pure Georgette"
                                singleStock = "120"
                                singleSelectedSizes.clear()
                                singleSelectedSizes.addAll(listOf("S", "M", "L", "XL", "XXL"))
                                singleSelectedAgeYears.clear()
                                singleSelectedAgeYears.add("Adults (18+)")
                                singleAgeYearsText = "1-2 Years, 2-3 Years, 3-4 Years, 4-5 Years"
                                singleManufacturerName = "Gyanvi Dresses Manufacturing Unit"
                                singleManufacturerAddress = "Lal Bazar, Main Road, Bettiah, West Champaran, Bihar"
                                singleManufacturerPincode = "845438"
                                singlePackerName = "Gyanvi Dresses Packaging & Logistics Hub"
                                singlePackerId = "PKR-GD-845438"
                                singlePackerAddress = "Industrial Area, Factory Road, Bettiah, Bihar"
                                singlePackerPincode = "845438"

                                bulkItems.clear()
                                bulkItems.addAll(
                                    listOf(
                                        BulkUploadItem("1", "Anarkali Embroidered Kurti (Front View)", "$skuPrefix-101", "1199", "449", "Rani Pink", "Pure Georgette", listOf("S", "M", "L", "XL", "XXL"), "120", "Embroidered kurti set", R.drawable.dress_anarkali_1793417013639),
                                        BulkUploadItem("2", "Banarasi Silk Saree (Pallu Close-up)", "$skuPrefix-102", "1999", "699", "Golden Yellow", "Banarasi Silk", listOf("S", "M", "L", "XL", "XXL"), "85", "Zari woven silk saree", R.drawable.dress_saree_1793417052582),
                                        BulkUploadItem("3", "Designer Georgette Lehenga (Full Shot)", "$skuPrefix-103", "2499", "999", "Navy Blue", "Net & Velvet", listOf("S", "M", "L", "XL", "XXL"), "60", "Party wear lehenga", R.drawable.dress_lehenga_1793417067159),
                                        BulkUploadItem("4", "Chikankari Cotton Kurta (Fabric Texture)", "$skuPrefix-104", "899", "379", "Mint Green", "100% Cotton", listOf("S", "M", "L", "XL", "XXL"), "150", "Daily wear cotton kurta", R.drawable.dress_anarkali_1793417013639)
                                    )
                                )
                                Toast.makeText(context, "✓ डिफ़ॉल्ट विकल्प (Default Option) लोड हो गया!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Defalt Option", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Mode Selector: Single Upload vs Multiple Upload vs Edit Existing Products vs Bulk CSV
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEEEEEE))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (uploadMode == 0) MeeshoMagenta else Color.Transparent,
                    modifier = Modifier.weight(1f).clickable { uploadMode = 0 }
                ) {
                    Text(
                        "Single",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = if (uploadMode == 0) Color.White else TextPrimary,
                        modifier = Modifier.padding(vertical = 7.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (uploadMode == 1) MeeshoMagenta else Color.Transparent,
                    modifier = Modifier.weight(1.3f).clickable { uploadMode = 1 }
                ) {
                    Text(
                        "Multiple (${bulkItems.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = if (uploadMode == 1) Color.White else TextPrimary,
                        modifier = Modifier.padding(vertical = 7.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (uploadMode == 2) MeeshoMagenta else Color.Transparent,
                    modifier = Modifier.weight(1.3f).clickable { uploadMode = 2 }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 7.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = if (uploadMode == 2) Color.White else MeeshoMagenta, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            "Edit (${catalogProducts.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (uploadMode == 2) Color.White else TextPrimary
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (uploadMode == 3) MeeshoMagenta else Color.Transparent,
                    modifier = Modifier.weight(1f).clickable { uploadMode = 3 }
                ) {
                    Text(
                        "Excel CSV",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = if (uploadMode == 3) Color.White else TextPrimary,
                        modifier = Modifier.padding(vertical = 7.dp)
                    )
                }
            }

            // =================================================================
            // TAB 0: SINGLE UPLOAD MODE (Matches Screenshot 6:03 + Age & Years)
            // =================================================================
            if (uploadMode == 0) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Single Product Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)

                        // 1. Photos & Media Section (Screenshot 6:03)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    painter = painterResource(photoAngles[selectedPhotoIndex].resId),
                                    contentDescription = "Product Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .clickable {
                                            ImageSaver.saveToGallery(context, photoAngles[selectedPhotoIndex].resId, singleName)
                                        }
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White, modifier = Modifier.padding(4.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                TextButton(
                                    onClick = {
                                        ImageSaver.saveToGallery(context, photoAngles[selectedPhotoIndex].resId, singleName)
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Download Image", fontSize = 12.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                                }
                                Text("High-Resolution 2048x2048", fontSize = 11.sp, color = TextSecondary)

                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFDF2F8),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBCFE8)),
                                    modifier = Modifier.clickable { showAiStudioDialog = true }
                                ) {
                                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Photo Editing Studio (Crop, BG, Text)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                                    }
                                }
                            }
                        }

                        // Photo Angles Strip (10-15 Photos support)
                        Text("Product Photos & Angles (${photoAngles.size}/15):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(photoAngles.size) { idx ->
                                val isSelected = idx == selectedPhotoIndex
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { selectedPhotoIndex = idx }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) MeeshoMagenta else Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                    ) {
                                        Image(
                                            painter = painterResource(photoAngles[idx].resId),
                                            contentDescription = photoAngles[idx].label,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Text(photoAngles[idx].label, fontSize = 9.sp, color = if (isSelected) MeeshoMagenta else TextSecondary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Target Age Group (उम्र वर्ग)
                        Text("Target Age Group (उम्र वर्ग):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(ageGroupsList) { ageGrp ->
                                val isSelected = selectedAgeGroup == ageGrp
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) Color(0xFFFDF2F8) else Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MeeshoMagenta else Color(0xFFCBD5E1)),
                                    modifier = Modifier.clickable { selectedAgeGroup = ageGrp }
                                ) {
                                    Text(
                                        ageGrp,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MeeshoMagenta else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        // 2. Product Name & AI Generator (Screenshot 6:03)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Product Name *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF5F3FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD6FE)),
                                modifier = Modifier.clickable {
                                    singleName = "Gyanvi Dresses Heavy Pure Georgette Anarkali Kurti Set with Dupatta"
                                    singleDescription = "Exclusive designer festive wear Anarkali Kurti set crafted with heavy pure georgette, fine zari sequence embroidery work, comfortable inner lining and matching dupatta."
                                    Toast.makeText(context, "✨ AI generated SEO-friendly product title & description!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("AI Title Suggest", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                }
                            }
                        }

                        OutlinedTextField(
                            value = singleName,
                            onValueChange = { singleName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Identifiers: SKU & Barcode & HSN
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = singleSku,
                                onValueChange = { singleSku = it },
                                label = { Text("SKU Number") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = singleBarcode,
                                onValueChange = { singleBarcode = it },
                                label = { Text("Barcode / EAN") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // 3. Pricing & Discounts (Screenshot 6:03)
                        val mrpVal = singleMrp.toIntOrNull() ?: 0
                        val discVal = singleDiscount.toIntOrNull() ?: 0
                        val percOff = if (mrpVal > discVal && mrpVal > 0) ((mrpVal - discVal) * 100 / mrpVal) else 0
                        val savings = if (mrpVal > discVal) (mrpVal - discVal) else 0

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = singleMrp,
                                onValueChange = { singleMrp = it },
                                label = { Text("Product Price / MRP (₹)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = singleDiscount,
                                onValueChange = { singleDiscount = it },
                                label = { Text("Discount Price (₹)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Discount & Customer Savings banner (Screenshot 6:03)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF0FDF4),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCFCE7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Discount: $percOff% Off", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF16A34A))
                                Text("Customer Saves: ₹$savings", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF16A34A))
                            }
                        }

                        // 4. Color & Fabric Details (Screenshot 6:03)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = singleColor,
                                onValueChange = { singleColor = it },
                                label = { Text("Product Color") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = singleFabric,
                                onValueChange = { singleFabric = it },
                                label = { Text("Fabric Details") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Color swatches row
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(colorSwatches) { (colorName, colorValue) ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(colorValue)
                                        .border(if (singleColor == colorName) 2.dp else 1.dp, if (singleColor == colorName) MeeshoMagenta else Color.Gray, CircleShape)
                                        .clickable { singleColor = colorName }
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // 5. Size & Age/Years Selection System & Matrix
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Sizes, Age & Variations (साइज और उम्र):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            TextButton(
                                onClick = { showSizeChartDialog = true },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Straighten, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Size Chart", fontSize = 11.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Size Categories Tabs: Standard, Age / Years, Numeric, Waist
                        TabRow(
                            selectedTabIndex = selectedSizeType,
                            containerColor = Color(0xFFF8FAFC),
                            contentColor = MeeshoMagenta,
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
                        ) {
                            Tab(selected = selectedSizeType == 0, onClick = { selectedSizeType = 0 }, text = { Text("Standard", fontSize = 11.sp) })
                            Tab(selected = selectedSizeType == 1, onClick = { selectedSizeType = 1 }, text = { Text("Age / Years", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                            Tab(selected = selectedSizeType == 2, onClick = { selectedSizeType = 2 }, text = { Text("Numeric", fontSize = 11.sp) })
                            Tab(selected = selectedSizeType == 3, onClick = { selectedSizeType = 3 }, text = { Text("Waist", fontSize = 11.sp) })
                        }

                        val activeSizeOptions = when (selectedSizeType) {
                            1 -> ageYearsList
                            2 -> numericSizesList
                            3 -> waistSizesList
                            else -> standardSizesList
                        }

                        // Size / Age Chips (Screenshot 6:03)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(activeSizeOptions) { sz ->
                                val isSelected = if (selectedSizeType == 1) singleSelectedAgeYears.contains(sz) else singleSelectedSizes.contains(sz)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) Color(0xFFF3E8FF) else Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF7C3AED) else Color(0xFFCBD5E1)),
                                    modifier = Modifier.clickable {
                                        if (selectedSizeType == 1) {
                                            if (isSelected) {
                                                if (singleSelectedAgeYears.size > 1) singleSelectedAgeYears.remove(sz)
                                            } else {
                                                singleSelectedAgeYears.add(sz)
                                            }
                                        } else {
                                            if (isSelected) {
                                                if (singleSelectedSizes.size > 1) singleSelectedSizes.remove(sz)
                                            } else {
                                                singleSelectedSizes.add(sz)
                                            }
                                        }
                                    }
                                ) {
                                    Text(
                                        sz,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF7C3AED) else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Stock Quantity (Screenshot 6:03)
                        OutlinedTextField(
                            value = singleStock,
                            onValueChange = { singleStock = it },
                            label = { Text("Stock Quantity (Units)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Variant Matrix Button
                        OutlinedButton(
                            onClick = { showVariantMatrixDialog = true },
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MeeshoMagenta)
                        ) {
                            Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Manage Individual Size/Age Pricing & Stock Matrix (${variantMatrixList.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // =========================================================
                        // AGE & YEARS (उम्र और साल) EDITING SECTION
                        // =========================================================
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Age & Years (उम्र और साल) *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Surface(
                                    color = Color(0xFFFDF2F8),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Govt Sizing Mandate", color = MeeshoMagenta, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            OutlinedTextField(
                                value = singleAgeYearsText,
                                onValueChange = { singleAgeYearsText = it },
                                label = { Text("Age & Years (e.g. 1-2 Years, 2-3 Years, 4-5 Years)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text("Quick Age/Years Chips (टैप करके जोड़ें):", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            val quickYearsChips = listOf("0-6 M", "6-12 M", "1-2 Years", "2-3 Years", "3-4 Years", "4-5 Years", "5-6 Years", "6-7 Years", "7-8 Years", "8-9 Years", "9-10 Years", "11-12 Years", "13-14 Years", "15-16 Years", "Adults (18+)")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                quickYearsChips.forEach { yr ->
                                    val isPresent = singleAgeYearsText.contains(yr)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isPresent) Color(0xFFF3E8FF) else Color(0xFFF8FAFC),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isPresent) Color(0xFF7C3AED) else Color(0xFFCBD5E1)),
                                        modifier = Modifier.clickable {
                                            if (!isPresent) {
                                                singleAgeYearsText = if (singleAgeYearsText.isBlank()) yr else "$singleAgeYearsText, $yr"
                                            }
                                        }
                                    ) {
                                        Text(
                                            yr,
                                            fontSize = 10.sp,
                                            fontWeight = if (isPresent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isPresent) Color(0xFF7C3AED) else TextSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // =========================================================
                        // PRODUCT MANUFACTURER DETAILS (उत्पाद निर्माता विवरण)
                        // =========================================================
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Product Manufacturer Details (निर्माता विवरण) *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Mandatory", color = Color(0xFF16A34A), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            OutlinedTextField(
                                value = singleManufacturerName,
                                onValueChange = { singleManufacturerName = it },
                                label = { Text("Product Manufacturer Name (निर्माता का नाम) *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = singleManufacturerAddress,
                                onValueChange = { singleManufacturerAddress = it },
                                label = { Text("Product Manufacturer Address (निर्माता का पता) *") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = singleManufacturerPincode,
                                onValueChange = { singleManufacturerPincode = it },
                                label = { Text("Product Manufacturer Pincode (निर्माता पिनकोड) *") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // =========================================================
                        // PACKER DETAILS (पैकर विवरण)
                        // =========================================================
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Packer Details (पैकर विवरण) *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFEDE7F6),
                                    modifier = Modifier.clickable {
                                        singlePackerName = singleManufacturerName
                                        singlePackerAddress = singleManufacturerAddress
                                        singlePackerPincode = singleManufacturerPincode
                                        Toast.makeText(context, "✓ Manufacturer details copied to Packer!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Same as Manufacturer", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = singlePackerName,
                                onValueChange = { singlePackerName = it },
                                label = { Text("Packer Name (पैकर का नाम) *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = singlePackerId,
                                onValueChange = { singlePackerId = it },
                                label = { Text("Packer ID (पैकर आईडी) *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = singlePackerAddress,
                                onValueChange = { singlePackerAddress = it },
                                label = { Text("Packer Address (पैकर का पता) *") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = singlePackerPincode,
                                onValueChange = { singlePackerPincode = it },
                                label = { Text("Packer Pincode (पैकर पिनकोड) *") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // 6. Product Description (Screenshot 6:03)
                        OutlinedTextField(
                            value = singleDescription,
                            onValueChange = { singleDescription = it },
                            label = { Text("Product Description") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Action Buttons: Preview & Publish (Screenshot 6:03)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showPreviewDialog = true },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Storefront Preview", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, "✓ '$singleName' सफलतापूर्वक पब्लिश हो गया!", Toast.LENGTH_LONG).show()
                                    onBack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                modifier = Modifier.weight(1.2f).height(48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Publish Product (पब्लिश करें)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 1: MULTIPLE UPLOAD MODE (Matches Screenshot 6:02 / 6:03 + Edit Options)
            // =================================================================
            if (uploadMode == 1) {
                // Batch Details Card (Screenshot 6:02)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Batch Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = skuPrefix,
                                onValueChange = { skuPrefix = it },
                                label = { Text("SKU Prefix") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = { selectedCategory = it },
                                label = { Text("Product Category") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Text("Multiple Products List (${bulkItems.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)

                // Render each product card in batch (Screenshot 6:02 & 6:03)
                bulkItems.forEachIndexed { idx, item ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFEF08A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(24.dp).clip(CircleShape).background(MeeshoMagenta),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${idx + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Product #${idx + 1}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Edit Product Button (NEW EDITING OPTION)
                                    IconButton(
                                        onClick = { editingBatchItemIndex = idx },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Product Details", tint = MeeshoMagenta, modifier = Modifier.size(18.dp))
                                    }

                                    // Download Image Button
                                    TextButton(
                                        onClick = {
                                            ImageSaver.saveToGallery(context, item.drawableRes, item.title)
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Download", fontSize = 10.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                                    }

                                    IconButton(
                                        onClick = { if (bulkItems.size > 1) bulkItems.removeAt(idx) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            // Product Name
                            OutlinedTextField(
                                value = item.title,
                                onValueChange = { newTitle ->
                                    val targetIdx = bulkItems.indexOfFirst { it.id == item.id }
                                    if (targetIdx != -1) bulkItems[targetIdx] = item.copy(title = newTitle)
                                },
                                label = { Text("Product Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Price, Discount & % Off Calculation
                            val bMrp = item.mrp.toIntOrNull() ?: 0
                            val bDisc = item.discountPrice.toIntOrNull() ?: 0
                            val bPerc = if (bMrp > bDisc && bMrp > 0) ((bMrp - bDisc) * 100 / bMrp) else 0

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = item.mrp,
                                    onValueChange = { newMrp ->
                                        val targetIdx = bulkItems.indexOfFirst { it.id == item.id }
                                        if (targetIdx != -1) bulkItems[targetIdx] = item.copy(mrp = newMrp)
                                    },
                                    label = { Text("MRP (₹)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = item.discountPrice,
                                    onValueChange = { newDisc ->
                                        val targetIdx = bulkItems.indexOfFirst { it.id == item.id }
                                        if (targetIdx != -1) bulkItems[targetIdx] = item.copy(discountPrice = newDisc)
                                    },
                                    label = { Text("Discount Price (₹)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("$bPerc% Off", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            // Color & Fabric Details (Screenshot 6:02 / 6:03)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = item.color,
                                    onValueChange = { newColor ->
                                        val targetIdx = bulkItems.indexOfFirst { it.id == item.id }
                                        if (targetIdx != -1) bulkItems[targetIdx] = item.copy(color = newColor)
                                    },
                                    label = { Text("Product Color") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = item.fabric,
                                    onValueChange = { newFabric ->
                                        val targetIdx = bulkItems.indexOfFirst { it.id == item.id }
                                        if (targetIdx != -1) bulkItems[targetIdx] = item.copy(fabric = newFabric)
                                    },
                                    label = { Text("Fabric Details") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Sizes & Stock Quantity (Screenshot 6:02 / 6:03)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("S", "M", "L", "XL", "XXL").forEach { sz ->
                                        val hasSz = item.selectedSizes.contains(sz)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (hasSz) Color(0xFFEDE7F6) else Color(0xFFEEEEEE),
                                            border = if (hasSz) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5E35B1)) else null,
                                            modifier = Modifier.clickable {
                                                val targetIdx = bulkItems.indexOfFirst { it.id == item.id }
                                                if (targetIdx != -1) {
                                                    val updatedSizes = item.selectedSizes.toMutableList()
                                                    if (hasSz) {
                                                        if (updatedSizes.size > 1) updatedSizes.remove(sz)
                                                    } else {
                                                        updatedSizes.add(sz)
                                                    }
                                                    bulkItems[targetIdx] = item.copy(selectedSizes = updatedSizes)
                                                }
                                            }
                                        ) {
                                            Text(sz, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (hasSz) Color(0xFF5E35B1) else TextMuted, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = item.stockQuantity,
                                    onValueChange = { newStock ->
                                        val targetIdx = bulkItems.indexOfFirst { it.id == item.id }
                                        if (targetIdx != -1) bulkItems[targetIdx] = item.copy(stockQuantity = newStock)
                                    },
                                    label = { Text("Stock Quantity") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(115.dp)
                                )
                            }
                        }
                    }
                }

                // Add Another Product Button (Screenshot 6:03)
                OutlinedButton(
                    onClick = {
                        val num = bulkItems.size + 1
                        bulkItems.add(
                            BulkUploadItem(
                                id = "$num",
                                title = "Gyanvi Festive Ethnic Collection #$num",
                                sku = "$skuPrefix-${100 + num}",
                                mrp = "1299",
                                discountPrice = "499",
                                color = "Multicolor",
                                fabric = "Pure Georgette",
                                selectedSizes = listOf("S", "M", "L", "XL", "XXL"),
                                stockQuantity = "100",
                                description = "Festive designer collection",
                                drawableRes = R.drawable.dress_anarkali_1793417013639
                            )
                        )
                        Toast.makeText(context, "+ नया प्रोडक्ट बैच में जुड़ गया!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Add Another Product to Batch", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Big Green Process & Publish All Button (Screenshot 6:03)
                Button(
                    onClick = {
                        Toast.makeText(context, "🎉 ${bulkItems.size} प्रोडक्ट्स सफलतापूर्वक पब्लिश हो गए!", Toast.LENGTH_LONG).show()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Process & Publish All (${bulkItems.size} Products)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // =================================================================
            // TAB 2: EDIT EXISTING CATALOG PRODUCTS (Brand New Mode!)
            // Allows editing Title, Age, Years, Sizes, Price, Stock & Photos
            // =================================================================
            if (uploadMode == 2) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Inventory, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Edit Catalog Products (${catalogProducts.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text("Modify pricing, age/years, stock and details in real-time", fontSize = 11.sp, color = TextSecondary)
                            }
                        }

                        // Search Bar
                        OutlinedTextField(
                            value = catalogSearchQuery,
                            onValueChange = { catalogSearchQuery = it },
                            placeholder = { Text("Search by name, SKU or fabric...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (catalogSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { catalogSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Age Filter Chips
                        Text("Filter by Target Age / Years:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf("All Ages", "Infants & Toddlers (0-3 Yrs)", "Kids (2-7 Yrs)", "Teens (8-16 Yrs)", "Adults (18+ Yrs)")) { filterItem ->
                                val isSelected = catalogAgeFilter == filterItem
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { catalogAgeFilter = filterItem },
                                    label = { Text(filterItem, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MeeshoMagenta,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        val filteredList = catalogProducts.filter { item ->
                            val matchesSearch = catalogSearchQuery.isEmpty() ||
                                    item.title.contains(catalogSearchQuery, ignoreCase = true) ||
                                    item.sku.contains(catalogSearchQuery, ignoreCase = true)
                            val matchesAge = catalogAgeFilter == "All Ages" || item.ageGroup.contains(catalogAgeFilter.take(4), ignoreCase = true)
                            matchesSearch && matchesAge
                        }

                        filteredList.forEach { prod ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(prod.drawableRes),
                                        contentDescription = prod.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(6.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("SKU: ${prod.sku} • Stock: ${prod.stock} Units", fontSize = 10.sp, color = TextSecondary)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("₹${prod.discountPrice}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MeeshoMagenta)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("₹${prod.mrp}", fontSize = 11.sp, textDecoration = TextDecoration.LineThrough, color = TextSecondary)
                                        }
                                        Surface(
                                            color = Color(0xFFF3E8FF),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text("Age: ${prod.ageYears}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }

                                    // Edit Action Button
                                    Button(
                                        onClick = { editingCatalogProduct = prod },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 3: BULK EXCEL / CSV IMPORT
            // =================================================================
            if (uploadMode == 3) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Bulk Catalog Import via Excel / CSV", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text("Upload 10, 50, 100 or 500 products with Age & Years in one click", fontSize = 11.sp, color = TextSecondary)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        Text("Step 1: Download Official Excel Template", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Pre-configured with columns: Product Title, Category, Age Group, Age Years (0M-18Y), Sizes (XS-7XL), Color, MRP, Selling Price, Stock.", fontSize = 11.sp, color = TextSecondary)

                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "✓ Gyanvi_Dresses_Bulk_Upload_Template.xlsx downloaded!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download .XLSX / .CSV Template", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text("Step 2: Upload Filled File & Verify", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Toast.makeText(context, "✓ File 'Gyanvi_Kurtis_Autumn2026_50SKU.xlsx' parsed successfully! 50 items ready.", Toast.LENGTH_LONG).show()
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Click to select Excel / CSV file from storage", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Supported formats: .XLSX, .XLS, .CSV (Max 15MB)", fontSize = 10.sp, color = TextSecondary)
                            }
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "🎉 50 प्रोडक्ट्स सफलतापूर्वक बल्क पब्लिश हो गए!", Toast.LENGTH_LONG).show()
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import & Publish 50 Catalog SKUs", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // =========================================================================
    // DIALOG: EDIT BATCH PRODUCT ITEM
    // =========================================================================
    editingBatchItemIndex?.let { batchIdx ->
        val item = bulkItems.getOrNull(batchIdx)
        if (item != null) {
            var tempTitle by remember { mutableStateOf(item.title) }
            var tempMrp by remember { mutableStateOf(item.mrp) }
            var tempDiscount by remember { mutableStateOf(item.discountPrice) }
            var tempColor by remember { mutableStateOf(item.color) }
            var tempFabric by remember { mutableStateOf(item.fabric) }
            var tempStock by remember { mutableStateOf(item.stockQuantity) }
            var tempDescription by remember { mutableStateOf(item.description) }
            var tempSelectedAgeYear by remember { mutableStateOf("Adults (18+)") }

            Dialog(onDismissRequest = { editingBatchItemIndex = null }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth().padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Edit Product #${batchIdx + 1}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            IconButton(onClick = { editingBatchItemIndex = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        OutlinedTextField(
                            value = tempTitle,
                            onValueChange = { tempTitle = it },
                            label = { Text("Product Title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Age & Years Selection for this product
                        Text("Age / Years (उम्र और साल):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(listOf("0-1 Yr", "2-3 Yrs", "4-5 Yrs", "6-7 Yrs", "8-9 Yrs", "10-12 Yrs", "12-14 Yrs", "14-16 Yrs", "Adults (18+)")) { yr ->
                                val isSel = tempSelectedAgeYear == yr
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSel) Color(0xFFF3E8FF) else Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color(0xFF7C3AED) else Color(0xFFCBD5E1)),
                                    modifier = Modifier.clickable { tempSelectedAgeYear = yr }
                                ) {
                                    Text(yr, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color(0xFF7C3AED) else TextSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = tempMrp,
                                onValueChange = { tempMrp = it },
                                label = { Text("MRP ₹") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = tempDiscount,
                                onValueChange = { tempDiscount = it },
                                label = { Text("Discount ₹") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = tempStock,
                                onValueChange = { tempStock = it },
                                label = { Text("Stock") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = tempColor,
                                onValueChange = { tempColor = it },
                                label = { Text("Color") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = tempFabric,
                                onValueChange = { tempFabric = it },
                                label = { Text("Fabric") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = tempDescription,
                            onValueChange = { tempDescription = it },
                            label = { Text("Description") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { editingBatchItemIndex = null }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    bulkItems[batchIdx] = item.copy(
                                        title = tempTitle,
                                        mrp = tempMrp,
                                        discountPrice = tempDiscount,
                                        color = tempColor,
                                        fabric = tempFabric,
                                        stockQuantity = tempStock,
                                        description = tempDescription
                                    )
                                    editingBatchItemIndex = null
                                    Toast.makeText(context, "✓ Product #${batchIdx + 1} updated!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                            ) {
                                Text("Save Edits")
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG: EDIT CATALOG PRODUCT (FULL EDITING OPTION)
    // =========================================================================
    editingCatalogProduct?.let { product ->
        var editTitle by remember { mutableStateOf(product.title) }
        var editMrp by remember { mutableStateOf(product.mrp) }
        var editDiscount by remember { mutableStateOf(product.discountPrice) }
        var editStock by remember { mutableStateOf(product.stock) }
        var editAgeGroup by remember { mutableStateOf(product.ageGroup) }
        var editAgeYears by remember { mutableStateOf(product.ageYears) }
        var editColor by remember { mutableStateOf(product.color) }
        var editFabric by remember { mutableStateOf(product.fabric) }

        Dialog(onDismissRequest = { editingCatalogProduct = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Edit Product Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        IconButton(onClick = { editingCatalogProduct = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Product Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Target Age Group & Years selector
                    Text("Target Age / Years (उम्र):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    OutlinedTextField(
                        value = editAgeYears,
                        onValueChange = { editAgeYears = it },
                        label = { Text("Age & Years (e.g. 4-5 Years, Teens 12-14, Adults)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editMrp,
                            onValueChange = { editMrp = it },
                            label = { Text("MRP ₹") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editDiscount,
                            onValueChange = { editDiscount = it },
                            label = { Text("Discount ₹") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editStock,
                            onValueChange = { editStock = it },
                            label = { Text("Stock") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editColor,
                            onValueChange = { editColor = it },
                            label = { Text("Color") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editFabric,
                            onValueChange = { editFabric = it },
                            label = { Text("Fabric") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { editingCatalogProduct = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val targetIdx = catalogProducts.indexOfFirst { it.id == product.id }
                                if (targetIdx != -1) {
                                    catalogProducts[targetIdx] = product.copy(
                                        title = editTitle,
                                        mrp = editMrp,
                                        discountPrice = editDiscount,
                                        stock = editStock,
                                        ageYears = editAgeYears,
                                        color = editColor,
                                        fabric = editFabric
                                    )
                                }
                                editingCatalogProduct = null
                                Toast.makeText(context, "✓ '${editTitle}' updated successfully in catalog!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG: AI IMAGE STUDIO (Background Remover, Crop, Enhance, Text Watermark)
    // =========================================================================
    if (showAiStudioDialog) {
        var watermarkText by remember { mutableStateOf("GYANVI DRESSES") }
        var showWatermark by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showAiStudioDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Photo Editing Studio", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text("Crop, remove background, enhance colors & add brand watermark", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            painter = painterResource(photoAngles[selectedPhotoIndex].resId),
                            contentDescription = "Editing image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (showWatermark) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                            ) {
                                Text(watermarkText, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { Toast.makeText(context, "✓ Background removed: Pure White Backdrop Applied!", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Remove BG", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { Toast.makeText(context, "✓ Auto-enhancement: Vibrant Colors & Contrast applied!", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Auto-Enhance", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { Toast.makeText(context, "✓ Aspect Ratio set to 3:4 Catalog Portrait", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Crop 3:4", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                showWatermark = !showWatermark
                                val msg = if (showWatermark) "✓ Brand watermark applied" else "Watermark removed"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(if (showWatermark) "Watermark ✓" else "+ Watermark", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = {
                                showAiStudioDialog = false
                                Toast.makeText(context, "✓ Edited photo saved to product angles!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                        ) {
                            Text("Apply & Done")
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG: VARIANT MATRIX PRICING & STOCK TABLE
    // =========================================================================
    if (showVariantMatrixDialog) {
        Dialog(onDismissRequest = { showVariantMatrixDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Variant Matrix Pricing", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        IconButton(onClick = { showVariantMatrixDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    Text("Independent Price, Stock, SKU & Barcode for each variation:", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    variantMatrixList.forEachIndexed { vIdx, vItem ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(color = Color(0xFFF3E8FF), shape = RoundedCornerShape(4.dp)) {
                                            Text("Size: ${vItem.size}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF7C3AED), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(vItem.color, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                    }
                                    Text("SKU: ${vItem.sku}", fontSize = 10.sp, color = TextSecondary)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = vItem.mrp,
                                        onValueChange = { newMrp ->
                                            variantMatrixList[vIdx] = vItem.copy(mrp = newMrp)
                                        },
                                        label = { Text("MRP ₹", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = vItem.sellingPrice,
                                        onValueChange = { newPrice ->
                                            variantMatrixList[vIdx] = vItem.copy(sellingPrice = newPrice)
                                        },
                                        label = { Text("Selling ₹", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = vItem.stock,
                                        onValueChange = { newStock ->
                                            variantMatrixList[vIdx] = vItem.copy(stock = newStock)
                                        },
                                        label = { Text("Stock", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            showVariantMatrixDialog = false
                            Toast.makeText(context, "✓ Variant pricing and inventory saved!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save All Variations")
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG: STOREFRONT PREVIEW MODAL
    // =========================================================================
    if (showPreviewDialog) {
        Dialog(onDismissRequest = { showPreviewDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Buyer Storefront Preview", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        IconButton(onClick = { showPreviewDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            painter = painterResource(photoAngles[0].resId),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("GYANVI DRESSES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                    Text(singleName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹$singleDiscount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("₹$singleMrp", fontSize = 13.sp, color = TextSecondary, textDecoration = TextDecoration.LineThrough)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(4.dp)) {
                            Text("61% Off", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Selected Age / Size: ${singleSelectedAgeYears.firstOrNull() ?: "Adults"}", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showPreviewDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Preview")
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG: CUSTOMIZABLE SIZE CHART (Inches & CM)
    // =========================================================================
    if (showSizeChartDialog) {
        Dialog(onDismissRequest = { showSizeChartDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Size & Age Chart (Inches & Years)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Text("Displayed to buyers for accurate fitting", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    val measurements = listOf(
                        SizeChartMeasurement("S (16-18 Y)", "36\"", "32\"", "38\"", "44\""),
                        SizeChartMeasurement("M (Adult)", "38\"", "34\"", "40\"", "44\""),
                        SizeChartMeasurement("L (Adult)", "40\"", "36\"", "42\"", "45\""),
                        SizeChartMeasurement("XL (Adult)", "42\"", "38\"", "44\"", "45\""),
                        SizeChartMeasurement("XXL (Adult)", "44\"", "40\"", "46\"", "46\"")
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFE2E8F0)).padding(6.dp)) {
                                Text("Size/Age", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(1.2f))
                                Text("Bust", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                Text("Waist", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                Text("Hip", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                Text("Length", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(1f))
                            }
                            measurements.forEach { m ->
                                Row(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
                                    Text(m.size, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MeeshoMagenta, modifier = Modifier.weight(1.2f))
                                    Text(m.bustChest, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                    Text(m.waist, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                    Text(m.hip, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                    Text(m.length, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showSizeChartDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}
