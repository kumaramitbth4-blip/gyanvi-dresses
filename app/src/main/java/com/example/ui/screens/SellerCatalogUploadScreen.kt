package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.util.ImageSaver

data class CatalogProductItem(
    val id: String,
    val title: String,
    val sku: String,
    val gyanviPrice: Int,
    val mrp: Int,
    val discountPercent: Int = if (mrp > 0) ((mrp - gyanviPrice) * 100 / mrp).coerceAtLeast(0) else 0,
    val couponCode: String = "FESTIVE15",
    val prepaidDiscount: Int = 20,
    val status: String, // "Draft", "QC in Progress", "QC Error", "QC Pass"
    val category: String,
    val dateAdded: String,
    val color: String = "Pink",
    val fabric: String = "Georgette",
    val sizes: List<String> = listOf("S", "M", "L", "XL", "XXL"),
    val totalStock: Int = 100,
    @DrawableRes val imageRes: Int = R.drawable.dress_anarkali_1793417013639
)

data class ProductVariantSize(
    val id: String = java.util.UUID.randomUUID().toString(),
    val size: String,
    val inventory: String,
    val skuId: String,
    val bustSize: String = "32 in",
    val lengthSize: String = "40 in"
)

data class MultipleUploadItem(
    val id: String,
    var title: String,
    var sku: String,
    var mrp: String,
    var sellerPrice: String,
    var couponCode: String = "FESTIVE15",
    var prepaidDiscount: String = "20",
    var color: String,
    var fabric: String,
    val sizes: MutableList<String> = mutableStateListOf("S", "M", "L", "XL", "XXL"),
    var stockUnits: String,
    var description: String,
    @DrawableRes var imageRes: Int
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SellerCatalogUploadScreen(
    onBack: () -> Unit,
    categories: List<com.example.model.CategoryItem> = com.example.data.SampleData.categoryList,
    subCategories: List<com.example.model.SubCategoryItem> = com.example.data.SampleData.defaultSubCategories,
    existingProducts: List<com.example.model.Product> = emptyList(),
    onPublishProductToCatalog: (
        name: String,
        category: String,
        subcategory: String,
        brandName: String,
        description: String,
        price: Int,
        mrp: Int,
        color: String,
        fabric: String,
        sizes: List<String>,
        numericSizes: List<String>,
        stock: Int,
        sku: String,
        weight: String,
        imageRes: Int,
        customImageUri: String?,
        extraImageUrls: List<String>
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current

    // Top Level Tabs: 0 = Single Upload, 1 = Multiple Upload, 2 = All Products
    var selectedTopTab by remember { mutableIntStateOf(0) }

    // Upload Wizard Steps: 1 = Add Product, 2 = Basic Details, 3 = Additional, 4 = Variants & Stock
    var currentStep by remember { mutableIntStateOf(1) }

    // =========================================================================
    // STEP 1 STATE: CATEGORY -> SUBCATEGORY -> PRODUCT NAME & IMAGES
    // =========================================================================
    var selectedMainCategory by remember { mutableStateOf("Girls Ethnic Wear") }
    var selectedSubCategory by remember { mutableStateOf("Gowns & Sharara") }
    var selectedCategory by remember { mutableStateOf("Girls Ethnic Wear > Gowns & Sharara") }
    var brandName by remember { mutableStateOf("GYANVI DRESSES") }
    var productWeight by remember { mutableStateOf("350g") }
    var selectedImageRes by remember { mutableIntStateOf(R.drawable.dress_anarkali_1793417013639) }
    var extraImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var pickerTargetBulk by remember { mutableStateOf(false) }

    // =========================================================================
    // STEP 2 STATE: BASIC & PRICING DETAILS (MRP, Seller Price, Discount, Size, Numeric Size, Color, Fabric)
    // =========================================================================
    var productName by remember { mutableStateOf("Girls Premium Net Embroidered Ethnic Gown & Sharara") }
    var meeshoPrice by remember { mutableStateOf("425") }
    var wdrpPrice by remember { mutableStateOf("405") }
    var mrpPrice by remember { mutableStateOf("999") }
    var couponCode by remember { mutableStateOf("FESTIVE15") }
    var prepaidDiscount by remember { mutableStateOf("20") }
    var productColor by remember { mutableStateOf("Rani Pink") }
    var productFabric by remember { mutableStateOf("Soft Net with Zari Embroidery") }
    var baseSku by remember { mutableStateOf("GYN-ETH-01") }
    val selectedStandardSizes = remember { mutableStateListOf("S", "M", "L", "XL", "XXL") }
    val selectedNumericSizes = remember { mutableStateListOf("28", "30", "32", "34", "36") }

    // =========================================================================
    // STEP 3 STATE: ADDITIONAL DETAILS (Age, Years, Manufacturer & Packer)
    // =========================================================================
    var productDescription by remember {
        mutableStateOf("High quality festive ethnic gown with matching dupatta and bottom. Breathable inner lining with rich border work, comfortable for kids and girls.")
    }
    var sleeveLength by remember { mutableStateOf("Sleeveless") }
    var neckType by remember { mutableStateOf("Round Neck") }
    var packageContents by remember { mutableStateOf("1 Gown, 1 Sharara, 1 Dupatta") }

    // Age & Years State
    var productAgeGroup by remember { mutableStateOf("Kids & Girls (2-8 Years)") }
    var productYears by remember { mutableStateOf("2-3 Years, 4-5 Years, 6-7 Years") }

    // Manufacturer Details
    var productManufacturerName by remember { mutableStateOf("Gyanvi Dresses Manufacturing Unit") }
    var productManufacturerAddress by remember { mutableStateOf("Lal Bazar, Main Road, Bettiah, West Champaran, Bihar") }
    var productManufacturerPincode by remember { mutableStateOf("845438") }

    // Packer Details
    var packerName by remember { mutableStateOf("Gyanvi Dresses Packaging & Logistics Hub") }
    var packerId by remember { mutableStateOf("PKR-GD-845438") }
    var packerAddress by remember { mutableStateOf("Industrial Area, Factory Road, Bettiah, Bihar") }
    var packerPincode by remember { mutableStateOf("845438") }

    // =========================================================================
    // STEP 4 STATE: VARIANTS & STOCK (Matching Screenshot)
    // =========================================================================
    val variantSizes = remember {
        mutableStateListOf(
            ProductVariantSize(
                id = "v1",
                size = "0-6 M",
                inventory = "15",
                skuId = "GWN-06M",
                bustSize = "32 in",
                lengthSize = "40 in"
            ),
            ProductVariantSize(
                id = "v2",
                size = "6-12 M",
                inventory = "20",
                skuId = "GWN-12M",
                bustSize = "32 in",
                lengthSize = "40 in"
            ),
            ProductVariantSize(
                id = "v3",
                size = "1-2 Y",
                inventory = "25",
                skuId = "GWN-12Y",
                bustSize = "32 in",
                lengthSize = "40 in"
            ),
            ProductVariantSize(
                id = "v4",
                size = "2-3 Y",
                inventory = "30",
                skuId = "GWN-23Y",
                bustSize = "34 in",
                lengthSize = "42 in"
            )
        )
    }

    var showAddSizeDialog by remember { mutableStateOf(false) }
    var newSizeName by remember { mutableStateOf("3-4 Y") }
    var newSizeInventory by remember { mutableStateOf("20") }
    var newSizeSku by remember { mutableStateOf("GWN-34Y") }
    var newSizeBust by remember { mutableStateOf("34 in") }
    var newSizeLength by remember { mutableStateOf("42 in") }

    // =========================================================================
    // MULTIPLE UPLOAD STATE (Bulk products batch)
    // =========================================================================
    var bulkSkuPrefix by remember { mutableStateOf("GYANVI-ETHNIC") }
    var bulkCategory by remember { mutableStateOf("Women") }
    var bulkSubCategory by remember { mutableStateOf("Saree") }
    val bulkProducts = remember {
        mutableStateListOf(
            MultipleUploadItem(
                id = "b1",
                title = "Anarkali Embroidered Kurti Set",
                sku = "GYANVI-ETHNIC-101",
                mrp = "1299",
                sellerPrice = "499",
                couponCode = "FESTIVE15",
                prepaidDiscount = "20",
                color = "Royal Rani Pink",
                fabric = "Heavy Pure Georgette",
                stockUnits = "120",
                description = "Beautiful designer kurti with fine sequin embroidery, matching bottom and dupatta.",
                imageRes = R.drawable.dress_anarkali_1793417013639
            ),
            MultipleUploadItem(
                id = "b2",
                title = "Banarasi Silk Saree (Pallu Close-up)",
                sku = "GYANVI-ETHNIC-102",
                mrp = "1999",
                sellerPrice = "699",
                couponCode = "GYANVI50",
                prepaidDiscount = "25",
                color = "Golden Yellow",
                fabric = "Pure Banarasi Silk",
                stockUnits = "85",
                description = "Traditional rich woven zari work silk saree with unstitched blouse piece.",
                imageRes = R.drawable.dress_saree_1793417052582
            ),
            MultipleUploadItem(
                id = "b3",
                title = "Designer Georgette Lehenga Choli",
                sku = "GYANVI-ETHNIC-103",
                mrp = "2499",
                sellerPrice = "999",
                couponCode = "GYANVI20",
                prepaidDiscount = "30",
                color = "Navy Blue",
                fabric = "Net & Velvet Embroidery",
                stockUnits = "60",
                description = "Bridal and festive wear flared lehenga with heavy border dupatta.",
                imageRes = R.drawable.dress_lehenga_1793417067159
            ),
            MultipleUploadItem(
                id = "b4",
                title = "Chikankari Cotton Kurta Set",
                sku = "GYANVI-ETHNIC-104",
                mrp = "899",
                sellerPrice = "379",
                couponCode = "SAVE10",
                prepaidDiscount = "15",
                color = "Mint Green",
                fabric = "100% Breathable Cotton",
                stockUnits = "150",
                description = "Handcrafted Lucknowi style casual kurti for summer comfort.",
                imageRes = R.drawable.dress_anarkali_1793417013639
            )
        )
    }

    // =========================================================================
    // ALL PRODUCTS REPOSITORY LIST
    // =========================================================================
    val allProductsList = remember {
        mutableStateListOf(
            CatalogProductItem("1", "Girls Premium Net Embroidered Ethnic Gown", "GWN-PNK-09", 425, 999, 57, "FESTIVE15", 20, "QC Pass", "Ethnic Gowns", "Today", "Pink", "Soft Net", listOf("0-6 M", "6-12 M", "1-2 Y"), 60, R.drawable.dress_anarkali_1793417013639),
            CatalogProductItem("2", "Peplum Kurti & Tiered Sharara Set", "SEA-GRN-SHR-02", 425, 899, 53, "GYANVI50", 15, "QC Pass", "Sharara Sets", "Yesterday", "Sea Green", "Georgette", listOf("S", "M", "L", "XL"), 70, R.drawable.dress_lehenga_1793417067159),
            CatalogProductItem("3", "Girls Premium Yellow Georgette Sharara Set", "Yellow-09", 425, 950, 55, "GYANVI20", 20, "QC Pass", "Kids Clothing", "2 Nov", "Yellow", "Georgette", listOf("S", "M", "L"), 45, R.drawable.dress_saree_1793417052582),
            CatalogProductItem("4", "Black Floral Kurta & Pant Set", "BLK-FLR-01", 380, 799, 52, "FESTIVE15", 20, "Draft", "Kurtis & Sets", "1 Nov", "Black", "Rayon", listOf("M", "L", "XL", "XXL"), 60, R.drawable.dress_anarkali_1793417013639),
            CatalogProductItem("5", "Pure Rayon Printed Anarkali Dress", "ANAR-RAY-44", 450, 999, 55, "SAVE10", 15, "QC in Progress", "Anarkalis", "30 Oct", "Blue", "Rayon", listOf("S", "M", "L", "XL", "XXL"), 110, R.drawable.dress_lehenga_1793417067159),
            CatalogProductItem("6", "Festival Silk Kids Lehanga Choli", "LEH-SLK-12", 599, 1299, 54, "GYANVI20", 25, "QC Pass", "Lehangas", "28 Oct", "Red", "Silk", listOf("S", "M", "L"), 40, R.drawable.dress_saree_1793417052582)
        )
    }

    var allProductsFilter by remember { mutableStateOf("All") }
    var allProductsSearch by remember { mutableStateOf("") }

    // Reusable text field colors that guarantee visibility in all themes
    val activeTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedContainerColor = CardWhite,
        unfocusedContainerColor = CardWhite,
        focusedBorderColor = MeeshoMagenta,
        unfocusedBorderColor = Color(0xFFCCCCCC),
        cursorColor = MeeshoMagenta,
        focusedLabelColor = MeeshoMagenta,
        unfocusedLabelColor = TextSecondary
    )

    // Hardware Back Navigation
    BackHandler {
        if (selectedTopTab != 0) {
            selectedTopTab = 0
        } else if (currentStep > 1) {
            currentStep -= 1
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (selectedTopTab != 0) {
                                        selectedTopTab = 0
                                    } else if (currentStep > 1) {
                                        currentStep -= 1
                                    } else {
                                        onBack()
                                    }
                                },
                                modifier = Modifier.testTag("catalog_back_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Text(
                                text = "Catalog Upload",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                        }

                        // Prominent Defalt Option / Fill Details Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFF3E0),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800)),
                            modifier = Modifier
                                .testTag("default_option_btn")
                                .clickable {
                                    productName = "Girls Premium Net Embroidered Ethnic Gown & Sharara"
                                    meeshoPrice = "425"
                                    wdrpPrice = "405"
                                    mrpPrice = "999"
                                    couponCode = "FESTIVE15"
                                    prepaidDiscount = "20"
                                    productColor = "Rani Pink"
                                    productFabric = "Soft Net with Zari Embroidery"
                                    productDescription = "High quality festive ethnic gown with matching dupatta and bottom. Breathable inner lining with rich border work, comfortable for kids and girls."
                                    sleeveLength = "Sleeveless"
                                    neckType = "Round Neck"
                                    packageContents = "1 Gown, 1 Sharara, 1 Dupatta"

                                    // Auto-fill Age, Years, Manufacturer & Packer
                                    productAgeGroup = "Kids & Girls (2-8 Years)"
                                    productYears = "2-3 Years, 4-5 Years, 6-7 Years"
                                    productManufacturerName = "Gyanvi Dresses Manufacturing Unit"
                                    productManufacturerAddress = "Lal Bazar, Main Road, Bettiah, West Champaran, Bihar"
                                    productManufacturerPincode = "845438"
                                    packerName = "Gyanvi Dresses Packaging & Logistics Hub"
                                    packerId = "PKR-GD-845438"
                                    packerAddress = "Industrial Area, Factory Road, Bettiah, Bihar"
                                    packerPincode = "845438"

                                    variantSizes.clear()
                                    variantSizes.addAll(
                                        listOf(
                                            ProductVariantSize(size = "0-6 M", inventory = "15", skuId = "GWN-06M", bustSize = "32 in", lengthSize = "40 in"),
                                            ProductVariantSize(size = "6-12 M", inventory = "20", skuId = "GWN-12M", bustSize = "32 in", lengthSize = "40 in"),
                                            ProductVariantSize(size = "1-2 Y", inventory = "25", skuId = "GWN-12Y", bustSize = "32 in", lengthSize = "40 in"),
                                            ProductVariantSize(size = "2-3 Y", inventory = "30", skuId = "GWN-23Y", bustSize = "34 in", lengthSize = "42 in")
                                        )
                                    )
                                    Toast.makeText(context, "✓ डिफ़ॉल्ट डेटा (Default Option) भर गया!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("डिफ़ॉल्ट विकल्प (Defalt)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            }
                        }
                    }

                    // Main Top Tabs: Single Upload vs Multiple Upload vs All Products
                    TabRow(
                        selectedTabIndex = selectedTopTab,
                        containerColor = CardWhite,
                        contentColor = MeeshoMagenta
                    ) {
                        Tab(
                            selected = selectedTopTab == 0,
                            onClick = { selectedTopTab = 0 },
                            text = {
                                Text(
                                    "Single Upload",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTopTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTopTab == 0) MeeshoMagenta else TextSecondary
                                )
                            }
                        )
                        Tab(
                            selected = selectedTopTab == 1,
                            onClick = { selectedTopTab = 1 },
                            text = {
                                Text(
                                    "Multiple Upload (${bulkProducts.size})",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTopTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTopTab == 1) MeeshoMagenta else TextSecondary
                                )
                            }
                        )
                        val mergedProductsCount = remember(allProductsList.size, existingProducts) {
                            (allProductsList.map { it.title.trim().lowercase() } + existingProducts.map { it.name.trim().lowercase() }).distinct().size
                        }
                        Tab(
                            selected = selectedTopTab == 2,
                            onClick = { selectedTopTab = 2 },
                            text = {
                                Text(
                                    "All Products ($mergedProductsCount)",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTopTab == 2) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTopTab == 2) MeeshoMagenta else TextSecondary
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        when (selectedTopTab) {
            0 -> {
                // =============================================================
                // TAB 0: SINGLE UPLOAD (4 STEPS WIZARD)
                // =============================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                ) {
                    // STEP PROGRESS BAR (Add Product, Basic Details, Additional, Variants)
                    Card(
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StepIndicatorItem(
                                stepNumber = 1,
                                title = "Add Product",
                                isCompleted = currentStep > 1,
                                isActive = currentStep == 1,
                                onClick = { currentStep = 1 }
                            )
                            StepDivider(isCompleted = currentStep > 1)
                            StepIndicatorItem(
                                stepNumber = 2,
                                title = "Basic Details",
                                isCompleted = currentStep > 2,
                                isActive = currentStep == 2,
                                onClick = { currentStep = 2 }
                            )
                            StepDivider(isCompleted = currentStep > 2)
                            StepIndicatorItem(
                                stepNumber = 3,
                                title = "Additional",
                                isCompleted = currentStep > 3,
                                isActive = currentStep == 3,
                                onClick = { currentStep = 3 }
                            )
                            StepDivider(isCompleted = currentStep > 3)
                            StepIndicatorItem(
                                stepNumber = 4,
                                title = "Variants",
                                isCompleted = false,
                                isActive = currentStep == 4,
                                onClick = { currentStep = 4 }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // =============================================================
                    // STEP 1: ADD PRODUCT & CATEGORY & PHOTO
                    // =============================================================
                    if (currentStep == 1) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Step 1: Category → Subcategory → Product & Photos", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)

                                // Meesho/Flipkart Style Guided Flow Pill
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFDF2F8),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MeeshoMagenta.copy(alpha = 0.28f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "$selectedMainCategory › $selectedSubCategory › ${productName.ifBlank { "Product" }} › $productColor › ₹$meeshoPrice",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MeeshoMagenta,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    )
                                }

                                // Searchable Category -> Subcategory -> Product Name Selector Button
                                Column {
                                    Text("Category & Subcategory (15+ Clothing & Footwear) *", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = "$selectedMainCategory > $selectedSubCategory",
                                        onValueChange = { selectedCategory = it },
                                        readOnly = true,
                                        label = { Text("Main Category > Subcategory") },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = {
                                                    pickerTargetBulk = false
                                                    showCategorySheet = true
                                                },
                                                modifier = Modifier.testTag("open_category_picker_btn")
                                            ) {
                                                Icon(Icons.Default.Search, contentDescription = "Search Categories", tint = MeeshoMagenta)
                                            }
                                        },
                                        singleLine = true,
                                        colors = activeTextFieldColors,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                pickerTargetBulk = false
                                                showCategorySheet = true
                                            }
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = {
                                            pickerTargetBulk = false
                                            showCategorySheet = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp).testTag("browse_all_categories_btn")
                                    ) {
                                        Icon(Icons.Default.Category, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Browse / Search All Clothing & Footwear Categories", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                                    }
                                }

                                // Quick Category Chips
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(
                                        listOf(
                                            Triple("Women", "Saree", "Banarasi Saree"),
                                            Triple("Women", "Kurti", "Anarkali Kurti"),
                                            Triple("Girls Ethnic Wear", "Gowns & Sharara", "Sharara Set"),
                                            Triple("Footwear", "Men Shoes", "Men's Formal Oxford Shoes"),
                                            Triple("Footwear", "Women Shoes", "Women's Embroidered Punjabi Jutti"),
                                            Triple("Men", "Shirts", "Formal Shirt"),
                                            Triple("Wedding & Festive Wear", "Bridal Lehenga", "Velvet Zari Bridal Lehenga")
                                        )
                                    ) { (mainCat, subCat, sampleProd) ->
                                        val isSel = selectedMainCategory.equals(mainCat, true) && selectedSubCategory.equals(subCat, true)
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isSel) Color(0xFFF3E5F5) else Color(0xFFEEEEEE),
                                            border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, MeeshoMagenta) else null,
                                            modifier = Modifier.clickable {
                                                selectedMainCategory = mainCat
                                                selectedSubCategory = subCat
                                                selectedCategory = "$mainCat > $subCat"
                                                productName = sampleProd
                                            }
                                        ) {
                                            Text("$mainCat • $subCat", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = TextPrimary)
                                        }
                                    }
                                }

                                // Product Name, Brand Name, SKU & Weight
                                OutlinedTextField(
                                    value = productName,
                                    onValueChange = { productName = it },
                                    label = { Text("Product Name *") },
                                    placeholder = { Text("e.g. Banarasi Saree / Men's Formal Oxford Shoes") },
                                    singleLine = true,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth().testTag("catalog_product_name_input")
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = brandName,
                                        onValueChange = { brandName = it },
                                        label = { Text("Brand Name *") },
                                        singleLine = true,
                                        colors = activeTextFieldColors,
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    OutlinedTextField(
                                        value = productWeight,
                                        onValueChange = { productWeight = it },
                                        label = { Text("Weight") },
                                        placeholder = { Text("350g") },
                                        singleLine = true,
                                        colors = activeTextFieldColors,
                                        modifier = Modifier.weight(0.8f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Main Image Preview & Photo Options with Download
                                Text("Product Image (Main Photo) *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF5F5F5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = selectedImageRes),
                                        contentDescription = "Selected Product",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Download Image Pill
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.Black.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(10.dp)
                                            .clickable {
                                                ImageSaver.saveToGallery(context, selectedImageRes, productName)
                                                Toast.makeText(context, "✓ फोटो गैलरी में सेव हो गई (Saved to Gallery)!", Toast.LENGTH_SHORT).show()
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Download Image", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Image Thumbnails Row to choose
                                Text("Choose Photo:", fontSize = 12.sp, color = TextSecondary)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    listOf(
                                        R.drawable.dress_anarkali_1793417013639 to "Anarkali Kurti",
                                        R.drawable.dress_lehenga_1793417067159 to "Lehenga Choli",
                                        R.drawable.dress_saree_1793417052582 to "Silk Saree"
                                    ).forEach { (resId, label) ->
                                        Card(
                                            shape = RoundedCornerShape(6.dp),
                                            border = if (selectedImageRes == resId) androidx.compose.foundation.BorderStroke(2.dp, MeeshoMagenta) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                            modifier = Modifier.size(68.dp).clickable { selectedImageRes = resId }
                                        ) {
                                            Image(
                                                painter = painterResource(id = resId),
                                                contentDescription = label,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }

                                // Multiple Product Images + Product Video Upload (Zero-Permission Android Photo Picker)

                                val multiImageLauncher = rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
                                ) { uris ->
                                    if (uris.isNotEmpty()) {
                                        extraImageUris = (extraImageUris + uris).distinct().take(10)
                                        Toast.makeText(context, "${uris.size} product image(s) added!", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                val videoPickerLauncher = rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.PickVisualMedia()
                                ) { uri ->
                                    if (uri != null) {
                                        selectedVideoUri = uri
                                        Toast.makeText(context, "Product showcase video attached!", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFF0F0F0))
                                Text("Multiple Product Images & Product Video Upload", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            multiImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).testTag("multi_image_upload_btn")
                                    ) {
                                        Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(16.dp), tint = MeeshoMagenta)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ Photos (${extraImageUris.size}/10)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).testTag("product_video_upload_btn")
                                    ) {
                                        Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF1D4ED8))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (selectedVideoUri != null) "Video Added ✓" else "+ Video (MP4)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                                    }
                                }

                                if (extraImageUris.isNotEmpty()) {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(extraImageUris) { uri ->
                                            Box(modifier = Modifier.size(64.dp)) {
                                                AsyncImage(
                                                    model = uri,
                                                    contentDescription = "Extra product image",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).border(1.dp, MeeshoMagenta, RoundedCornerShape(8.dp))
                                                )
                                                IconButton(
                                                    onClick = { extraImageUris = extraImageUris - uri },
                                                    modifier = Modifier.size(20.dp).align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = Color.White, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                selectedVideoUri?.let { vidUri ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFEFF6FF),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Video attached: ${vidUri.lastPathSegment ?: "product_demo.mp4"}", fontSize = 11.sp, color = Color(0xFF1E3A8A), maxLines = 1)
                                            }
                                            TextButton(onClick = { selectedVideoUri = null }) {
                                                Text("Remove", fontSize = 11.sp, color = Color(0xFFDC2626))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = { currentStep = 2 },
                                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("step1_next_btn")
                                ) {
                                    Text("Next: Pricing & Basic Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // =============================================================
                    // STEP 2: BASIC & PRICING DETAILS (MRP, Seller Price, Discount, Coupon, Prepaid)
                    // =============================================================
                    if (currentStep == 2) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Step 2: Pricing & Discounts", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                                // Product Title
                                OutlinedTextField(
                                    value = productName,
                                    onValueChange = { productName = it },
                                    label = { Text("Product Name *") },
                                    placeholder = { Text("e.g. Girls Embroidered Ethnic Gown") },
                                    singleLine = true,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Pricing Row: Meesho Price, WDRP Price, MRP
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = mrpPrice,
                                        onValueChange = { mrpPrice = it },
                                        label = { Text("MRP (₹) *") },
                                        singleLine = true,
                                        colors = activeTextFieldColors,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = meeshoPrice,
                                        onValueChange = { meeshoPrice = it },
                                        label = { Text("Seller Price (₹) *") },
                                        singleLine = true,
                                        colors = activeTextFieldColors,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = wdrpPrice,
                                        onValueChange = { wdrpPrice = it },
                                        label = { Text("WDRP Price (₹)") },
                                        singleLine = true,
                                        colors = activeTextFieldColors,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Savings & Discount Calculation Badge
                                val mPrice = meeshoPrice.toIntOrNull() ?: 0
                                val mrpVal = mrpPrice.toIntOrNull() ?: 0
                                val discPerc = if (mrpVal > mPrice && mrpVal > 0) ((mrpVal - mPrice) * 100 / mrpVal) else 0

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE8F5E9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Customer Discount: $discPerc% OFF", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), fontSize = 13.sp)
                                            Text("Customer Saves: ₹${(mrpVal - mPrice).coerceAtLeast(0)}", color = Color(0xFF2E7D32), fontSize = 11.sp)
                                        }
                                        Text("Best Price: ₹$mPrice", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF1B5E20))
                                    }
                                }

                                // Coupon Code & Prepaid Discount Section
                                Text("Coupons & RTO Reduction:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = couponCode,
                                        onValueChange = { couponCode = it.uppercase() },
                                        label = { Text("Coupon Code") },
                                        singleLine = true,
                                        colors = activeTextFieldColors,
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    OutlinedTextField(
                                        value = prepaidDiscount,
                                        onValueChange = { prepaidDiscount = it },
                                        label = { Text("Prepaid Off (₹)") },
                                        singleLine = true,
                                        colors = activeTextFieldColors,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Quick Coupon Chips
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(listOf("FESTIVE15" to "15% Off", "GYANVI20" to "20% Off", "GYANVI50" to "₹50 Off", "PREPAID20" to "Prepaid ₹20")) { (cCode, desc) ->
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (couponCode == cCode) Color(0xFFEDE7F6) else Color(0xFFF5F5F5),
                                            border = if (couponCode == cCode) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5E35B1)) else null,
                                            modifier = Modifier.clickable { couponCode = cCode }
                                        ) {
                                            Text("$cCode ($desc)", fontSize = 11.sp, color = TextPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }
                                }

                                // Color, Fabric & Main SKU
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = productColor,
                                        onValueChange = { productColor = it },
                                        label = { Text("Color *") },
                                        singleLine = true,
                                        colors = activeTextFieldColors,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = productFabric,
                                        onValueChange = { productFabric = it },
                                        label = { Text("Fabric / Material *") },
                                        singleLine = true,
                                        colors = activeTextFieldColors,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Color Swatch Chips
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(com.example.data.ClothingCatalogTaxonomy.colorOptions) { clr ->
                                        val isSel = productColor.equals(clr, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isSel) Color(0xFFFDF2F8) else Color(0xFFF1F5F9),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) MeeshoMagenta else Color(0xFFCBD5E1)),
                                            modifier = Modifier.clickable { productColor = clr }
                                        ) {
                                            Text(clr, fontSize = 10.5.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) MeeshoMagenta else TextPrimary, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
                                        }
                                    }
                                }

                                // Fabric Quick Chips
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(com.example.data.ClothingCatalogTaxonomy.fabricOptions + listOf("Genuine Leather", "Synthetic Leather", "Breathable Mesh")) { fab ->
                                        val isSel = productFabric.equals(fab, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isSel) Color(0xFFEDE7F6) else Color(0xFFF8FAFC),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color(0xFF5E35B1) else Color(0xFFE2E8F0)),
                                            modifier = Modifier.clickable { productFabric = fab }
                                        ) {
                                            Text(fab, fontSize = 10.5.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) Color(0xFF4527A0) else TextPrimary, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
                                        }
                                    }
                                }

                                // Clothing Sizes (S, M, L, XL, XXL, XXXL, 4XL, 5XL) & Numeric Sizes (16-42) & Footwear Sizes (UK 4-11)
                                Text("Standard Sizes (S – 5XL):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    com.example.data.ClothingCatalogTaxonomy.standardSizes.forEach { sz ->
                                        val active = sz in selectedStandardSizes
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (active) MeeshoMagenta else Color(0xFFF1F5F9),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (active) MeeshoMagenta else Color(0xFFCBD5E1)),
                                            modifier = Modifier.clickable {
                                                if (active && selectedStandardSizes.size > 1) selectedStandardSizes.remove(sz)
                                                else if (!active) selectedStandardSizes.add(sz)
                                            }
                                        ) {
                                            Text(sz, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (active) Color.White else TextPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                        }
                                    }
                                }

                                Text("Numeric Sizes (16 – 42) & Footwear UK Sizes:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    (com.example.data.ClothingCatalogTaxonomy.numericSizes + com.example.data.ClothingCatalogTaxonomy.footwearSizes).forEach { numSz ->
                                        val active = numSz in selectedNumericSizes
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (active) Color(0xFF1A237E) else Color(0xFFF8FAFC),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (active) Color(0xFF1A237E) else Color(0xFFE2E8F0)),
                                            modifier = Modifier.clickable {
                                                if (active) selectedNumericSizes.remove(numSz)
                                                else selectedNumericSizes.add(numSz)
                                            }
                                        ) {
                                            Text(numSz, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = if (active) Color.White else TextPrimary, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = baseSku,
                                    onValueChange = { baseSku = it.uppercase() },
                                    label = { Text("Base SKU Code *") },
                                    singleLine = true,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                // =========================================================
                                // PRODUCT SIZE, AGE & YEARS (साइज, उम्र और साल)
                                // =========================================================
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Product Size, Age & Years (साइज और उम्र) *", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFDF2F8)
                                    ) {
                                        Text("Gyanvi Dresses / Govt Mandate", color = MeeshoMagenta, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }

                                Text("Target Age Group (उम्र वर्ग):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val ageGroupsStep2 = listOf("Kids & Girls (2-8 Years)", "Infants (0-1 Yrs)", "Kids (2-7 Yrs)", "Teens (8-16 Yrs)", "Adults (18+ Yrs)", "All Ages")
                                    ageGroupsStep2.forEach { group ->
                                        val isSel = productAgeGroup == group
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSel) Color(0xFFFDF2F8) else Color(0xFFF1F5F9),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) MeeshoMagenta else Color(0xFFCBD5E1)),
                                            modifier = Modifier.clickable { productAgeGroup = group }
                                        ) {
                                            Text(
                                                group,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSel) MeeshoMagenta else TextSecondary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = productYears,
                                    onValueChange = { productYears = it },
                                    label = { Text("Product Size Age / Years (साल) *") },
                                    placeholder = { Text("e.g. 2-3 Years, 4-5 Years, 6-7 Years") },
                                    singleLine = true,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Quick selectable Years chips
                                Text("Quick Age/Years Chips (टैप करके जोड़ें / एडिट करें):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                val yearsQuickChipsStep2 = listOf("0-6 M", "6-12 M", "1-2 Years", "2-3 Years", "3-4 Years", "4-5 Years", "5-6 Years", "6-7 Years", "7-8 Years", "8-9 Years", "9-10 Years", "10-12 Years", "12-14 Years", "14-16 Years", "Adults (18+)")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    yearsQuickChipsStep2.forEach { yr ->
                                        val isPresent = productYears.contains(yr)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isPresent) Color(0xFFF3E8FF) else Color(0xFFF8FAFC),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isPresent) Color(0xFF7C3AED) else Color(0xFFE2E8F0)),
                                            modifier = Modifier.clickable {
                                                if (!isPresent) {
                                                    productYears = if (productYears.isBlank()) yr else "$productYears, $yr"
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

                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                // =========================================================
                                // PRODUCT MANUFACTURER DETAILS (उत्पाद निर्माता विवरण)
                                // =========================================================
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Product Manufacturer Details (निर्माता विवरण) *", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Surface(
                                        color = Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Mandatory", color = Color(0xFF16A34A), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }

                                OutlinedTextField(
                                    value = productManufacturerName,
                                    onValueChange = { productManufacturerName = it },
                                    label = { Text("Product Manufacturer Name (निर्माता का नाम) *") },
                                    singleLine = true,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = productManufacturerAddress,
                                    onValueChange = { productManufacturerAddress = it },
                                    label = { Text("Product Manufacturer Address (निर्माता का पता) *") },
                                    minLines = 2,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = productManufacturerPincode,
                                    onValueChange = { productManufacturerPincode = it },
                                    label = { Text("Product Manufacturer Pincode (निर्माता पिनकोड) *") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                // =========================================================
                                // PACKER DETAILS (पैकर विवरण)
                                // =========================================================
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Packer Details (पैकर विवरण) *", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFEDE7F6),
                                        modifier = Modifier.clickable {
                                            packerName = productManufacturerName
                                            packerAddress = productManufacturerAddress
                                            packerPincode = productManufacturerPincode
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
                                    value = packerName,
                                    onValueChange = { packerName = it },
                                    label = { Text("Packer Name (पैकर का नाम) *") },
                                    singleLine = true,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = packerId,
                                    onValueChange = { packerId = it },
                                    label = { Text("Packer ID (पैकर आईडी) *") },
                                    singleLine = true,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = packerAddress,
                                    onValueChange = { packerAddress = it },
                                    label = { Text("Packer Address (पैकर का पता) *") },
                                    minLines = 2,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = packerPincode,
                                    onValueChange = { packerPincode = it },
                                    label = { Text("Packer Pincode (पैकर पिनकोड) *") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedButton(
                                        onClick = { currentStep = 1 },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(46.dp)
                                    ) {
                                        Text("Back")
                                    }
                                    Button(
                                        onClick = { currentStep = 3 },
                                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(46.dp).testTag("step2_next_btn")
                                    ) {
                                        Text("Next: Additional", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // =============================================================
                    // STEP 3: ADDITIONAL DETAILS
                    // =============================================================
                    if (currentStep == 3) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Step 3: Additional Product Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                                OutlinedTextField(
                                    value = productDescription,
                                    onValueChange = { productDescription = it },
                                    label = { Text("Description *") },
                                    minLines = 3,
                                    maxLines = 5,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = sleeveLength,
                                        onValueChange = { sleeveLength = it },
                                        label = { Text("Sleeve Length") },
                                        singleLine = true,
                                        colors = activeTextFieldColors,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = neckType,
                                        onValueChange = { neckType = it },
                                        label = { Text("Neck Type") },
                                        singleLine = true,
                                        colors = activeTextFieldColors,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                OutlinedTextField(
                                    value = packageContents,
                                    onValueChange = { packageContents = it },
                                    label = { Text("Package Contents") },
                                    singleLine = true,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                // =========================================================
                                // AGE & YEARS SIZING (उम्र और साल)
                                // =========================================================
                                Text("Target Age & Years (लक्षित उम्र और साल) *", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)

                                // Age Group Chips
                                val ageGroupOptions = listOf("Infants (0-1 Yrs)", "Kids (2-7 Yrs)", "Teens (8-16 Yrs)", "Adults (18+ Yrs)")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ageGroupOptions.forEach { group ->
                                        val isSel = productAgeGroup == group
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSel) Color(0xFFFDF2F8) else Color(0xFFF1F5F9),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) MeeshoMagenta else Color(0xFFCBD5E1)),
                                            modifier = Modifier.clickable { productAgeGroup = group }
                                        ) {
                                            Text(
                                                group,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSel) MeeshoMagenta else TextSecondary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = productYears,
                                    onValueChange = { productYears = it },
                                    label = { Text("Age & Years (साल) * (e.g. 2-3 Years, 4-5 Years)") },
                                    singleLine = true,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Quick selectable Years chips
                                Text("Quick Age/Years Chips (टैप करके चुनें):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                val yearsQuickChips = listOf("0-6 M", "6-12 M", "1-2 Years", "2-3 Years", "3-4 Years", "4-5 Years", "5-6 Years", "6-7 Years", "7-8 Years", "8-9 Years", "9-10 Years", "10-12 Years", "12-14 Years", "14-16 Years", "Adults (18+)")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    yearsQuickChips.forEach { yr ->
                                        val isPresent = productYears.contains(yr)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isPresent) Color(0xFFF3E8FF) else Color(0xFFF8FAFC),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isPresent) Color(0xFF7C3AED) else Color(0xFFE2E8F0)),
                                            modifier = Modifier.clickable {
                                                if (!isPresent) {
                                                    productYears = if (productYears.isBlank()) yr else "$productYears, $yr"
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

                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                // =========================================================
                                // PRODUCT MANUFACTURER DETAILS (उत्पाद निर्माता विवरण)
                                // =========================================================
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Product Manufacturer Details (निर्माता) *", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Surface(
                                        color = Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Mandatory", color = Color(0xFF16A34A), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }

                                OutlinedTextField(
                                    value = productManufacturerName,
                                    onValueChange = { productManufacturerName = it },
                                    label = { Text("Product Manufacturer Name *") },
                                    singleLine = true,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = productManufacturerAddress,
                                    onValueChange = { productManufacturerAddress = it },
                                    label = { Text("Product Manufacturer Address *") },
                                    minLines = 2,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = productManufacturerPincode,
                                    onValueChange = { productManufacturerPincode = it },
                                    label = { Text("Product Manufacturer Pincode *") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                // =========================================================
                                // PACKER DETAILS (पैकर विवरण)
                                // =========================================================
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Packer Details (पैकर विवरण) *", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFEDE7F6),
                                        modifier = Modifier.clickable {
                                            packerName = productManufacturerName
                                            packerAddress = productManufacturerAddress
                                            packerPincode = productManufacturerPincode
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
                                    value = packerName,
                                    onValueChange = { packerName = it },
                                    label = { Text("Packer Name *") },
                                    singleLine = true,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = packerId,
                                    onValueChange = { packerId = it },
                                    label = { Text("Packer ID (पैकर आईडी) *") },
                                    singleLine = true,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = packerAddress,
                                    onValueChange = { packerAddress = it },
                                    label = { Text("Packer Address *") },
                                    minLines = 2,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = packerPincode,
                                    onValueChange = { packerPincode = it },
                                    label = { Text("Packer Pincode *") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedButton(
                                        onClick = { currentStep = 2 },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(46.dp)
                                    ) {
                                        Text("Back")
                                    }
                                    Button(
                                        onClick = { currentStep = 4 },
                                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(46.dp).testTag("step3_next_btn")
                                    ) {
                                        Text("Next: Variants", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // =============================================================
                    // STEP 4: VARIANTS & STOCK (EXACT MATCH TO USER SCREENSHOT)
                    // =============================================================
                    if (currentStep == 4) {
                        // Header Row: Step 4: Variants & Stock and + Add Size button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Step 4: Variants & Stock",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )

                            // + Add Size Button matching screenshot
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFFF3E0),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800)),
                                modifier = Modifier
                                    .testTag("add_size_btn")
                                    .clickable { showAddSizeDialog = true }
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Size", fontSize = 12.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // List of Variant Size Cards
                        variantSizes.forEachIndexed { index, variant ->
                            key(variant.id) {
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Row 1: Size Label & Delete Trash Icon
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Size: ${variant.size}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF3F51B5)
                                            )

                                            IconButton(
                                                onClick = {
                                                    if (variantSizes.size > 1) {
                                                        variantSizes.removeAt(index)
                                                        Toast.makeText(context, "Size ${variant.size} removed", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "At least one size is required", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp).testTag("delete_size_$index")
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Color(0xFFE53935),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        // Row 2: Inventory (Stock) and SKU ID TextFields
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Inventory with quick + / - buttons for easy interaction
                                            Column(modifier = Modifier.weight(1f)) {
                                                OutlinedTextField(
                                                    value = variant.inventory,
                                                    onValueChange = { newInv ->
                                                        val targetIndex = variantSizes.indexOfFirst { it.id == variant.id }
                                                        if (targetIndex != -1) {
                                                            variantSizes[targetIndex] = variant.copy(inventory = newInv)
                                                        }
                                                    },
                                                    label = { Text("Inventory (Stock)", fontSize = 11.sp) },
                                                    singleLine = true,
                                                    colors = activeTextFieldColors,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.fillMaxWidth().testTag("inventory_field_$index")
                                                )
                                                // Quick + / - adjustment
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFFEEEEEE),
                                                        modifier = Modifier.weight(1f).clickable {
                                                            val curr = variant.inventory.toIntOrNull() ?: 15
                                                            val next = (curr - 5).coerceAtLeast(0)
                                                            val targetIndex = variantSizes.indexOfFirst { it.id == variant.id }
                                                            if (targetIndex != -1) variantSizes[targetIndex] = variant.copy(inventory = next.toString())
                                                        }
                                                    ) {
                                                        Text("-5", textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 3.dp))
                                                    }
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFFE8F5E9),
                                                        modifier = Modifier.weight(1f).clickable {
                                                            val curr = variant.inventory.toIntOrNull() ?: 15
                                                            val next = curr + 5
                                                            val targetIndex = variantSizes.indexOfFirst { it.id == variant.id }
                                                            if (targetIndex != -1) variantSizes[targetIndex] = variant.copy(inventory = next.toString())
                                                        }
                                                    ) {
                                                        Text("+5", textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), modifier = Modifier.padding(vertical = 3.dp))
                                                    }
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFFEDE7F6),
                                                        modifier = Modifier.weight(1f).clickable {
                                                            val curr = variant.inventory.toIntOrNull() ?: 15
                                                            val next = curr + 20
                                                            val targetIndex = variantSizes.indexOfFirst { it.id == variant.id }
                                                            if (targetIndex != -1) variantSizes[targetIndex] = variant.copy(inventory = next.toString())
                                                        }
                                                    ) {
                                                        Text("+20", textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5E35B1), modifier = Modifier.padding(vertical = 3.dp))
                                                    }
                                                }
                                            }

                                            // SKU ID field
                                            Column(modifier = Modifier.weight(1f)) {
                                                OutlinedTextField(
                                                    value = variant.skuId,
                                                    onValueChange = { newSku ->
                                                        val targetIndex = variantSizes.indexOfFirst { it.id == variant.id }
                                                        if (targetIndex != -1) {
                                                            variantSizes[targetIndex] = variant.copy(skuId = newSku)
                                                        }
                                                    },
                                                    label = { Text("SKU ID", fontSize = 11.sp) },
                                                    singleLine = true,
                                                    colors = activeTextFieldColors,
                                                    modifier = Modifier.fillMaxWidth().testTag("sku_field_$index")
                                                )
                                                Text(
                                                    text = "Tap to edit or auto-prefix",
                                                    fontSize = 9.sp,
                                                    color = TextMuted,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                        }

                                        // Row 3: Bust & Length information
                                        Text(
                                            text = "Bust: ${variant.bustSize} • Length: ${variant.lengthSize}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stock Summary Card
                        val totalInventoryCount = variantSizes.sumOf { it.inventory.toIntOrNull() ?: 0 }
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Variant Inventory:", fontSize = 12.sp, color = Color(0xFF4527A0), fontWeight = FontWeight.Medium)
                                Text("$totalInventoryCount Units (${variantSizes.size} Sizes)", fontSize = 13.sp, color = Color(0xFF4527A0), fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Bottom Navigation Buttons: [ Back ] and [ Submit for QC Review ]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = 3 },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("Back", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    val firstSku = baseSku.ifBlank { variantSizes.firstOrNull()?.skuId ?: "GYN-01" }
                                    val mPrice = meeshoPrice.toIntOrNull()?.coerceAtLeast(1) ?: 425
                                    val mrpVal = mrpPrice.toIntOrNull()?.coerceAtLeast(mPrice) ?: 999
                                    val disc = if (mrpVal > mPrice && mrpVal > 0) ((mrpVal - mPrice) * 100 / mrpVal) else 0
                                    val pDisc = prepaidDiscount.toIntOrNull() ?: 20
                                    val resolvedSizes = (variantSizes.map { it.size } + selectedStandardSizes)
                                        .map { it.trim() }
                                        .filter { it.isNotBlank() }
                                        .distinct()
                                    val extraUris = extraImageUris.map { it.toString() }

                                    val newProduct = CatalogProductItem(
                                        id = System.currentTimeMillis().toString(),
                                        title = productName.ifBlank { "Girls Premium Net Embroidered Ethnic Gown" },
                                        sku = firstSku,
                                        gyanviPrice = mPrice,
                                        mrp = mrpVal,
                                        discountPercent = disc,
                                        couponCode = couponCode,
                                        prepaidDiscount = pDisc,
                                        status = "QC Pass",
                                        category = "$selectedMainCategory > $selectedSubCategory",
                                        dateAdded = "Just now",
                                        color = productColor,
                                        fabric = productFabric,
                                        sizes = resolvedSizes,
                                        totalStock = totalInventoryCount.coerceAtLeast(1),
                                        imageRes = selectedImageRes
                                    )
                                    allProductsList.add(0, newProduct)
                                    onPublishProductToCatalog(
                                        newProduct.title,
                                        selectedMainCategory.ifBlank { "Women" },
                                        selectedSubCategory.ifBlank { selectedMainCategory },
                                        brandName.ifBlank { "GYANVI DRESSES" },
                                        productDescription,
                                        mPrice,
                                        mrpVal,
                                        productColor,
                                        productFabric,
                                        resolvedSizes,
                                        selectedNumericSizes.toList(),
                                        totalInventoryCount.coerceAtLeast(1),
                                        firstSku,
                                        productWeight.ifBlank { "350g" },
                                        selectedImageRes,
                                        extraUris.firstOrNull(),
                                        extraUris
                                    )
                                    selectedTopTab = 2 // Switch to All Products tab
                                    Toast.makeText(
                                        context,
                                        "🎉 '${newProduct.title}' Room DB & Gyanvi Marketplace में सेव व पब्लिश हो गया!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(2f)
                                    .height(48.dp)
                                    .testTag("submit_qc_btn")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Publish to Catalog", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }

            1 -> {
                // =============================================================
                // TAB 1: MULTIPLE UPLOAD (BULK PRODUCTS BATCH)
                // =============================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Card: SKU Prefix & Category
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Batch Details (मल्टीपल प्रोडक्ट)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFE8F5E9),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("${bulkProducts.size} Products Ready", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = bulkSkuPrefix,
                                    onValueChange = { bulkSkuPrefix = it },
                                    label = { Text("SKU Prefix") },
                                    singleLine = true,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.weight(0.9f)
                                )
                                OutlinedTextField(
                                    value = "$bulkCategory > $bulkSubCategory",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category > Sub") },
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            pickerTargetBulk = true
                                            showCategorySheet = true
                                        }) {
                                            Icon(Icons.Default.Search, contentDescription = "Pick Bulk Category", tint = MeeshoMagenta)
                                        }
                                    },
                                    singleLine = true,
                                    colors = activeTextFieldColors,
                                    modifier = Modifier.weight(1.3f).clickable {
                                        pickerTargetBulk = true
                                        showCategorySheet = true
                                    }
                                )
                            }
                        }
                    }

                    // Bulk Products Items List
                    bulkProducts.forEachIndexed { idx, item ->
                        key(item.id) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    // Row 1: Index Badge, Title & Download Image button
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
                                                Text("${idx + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Product #${idx + 1}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Download Image Button
                                            Button(
                                                onClick = {
                                                    ImageSaver.saveToGallery(context, item.imageRes, item.title)
                                                    Toast.makeText(context, "✓ '${item.title}' फोटो गैलरी में सेव हो गई!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3E5F5)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Icon(Icons.Default.Download, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Download", fontSize = 10.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            IconButton(
                                                onClick = {
                                                    if (bulkProducts.size > 1) {
                                                        bulkProducts.removeAt(idx)
                                                    } else {
                                                        Toast.makeText(context, "कम से कम 1 प्रोडक्ट होना चाहिए", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    // Image Thumbnail & Change Image options
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(76.dp).clip(RoundedCornerShape(8.dp))) {
                                            Image(
                                                painter = painterResource(id = item.imageRes),
                                                contentDescription = item.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text("Photo Selection (High-Res):", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                listOf(
                                                    R.drawable.dress_anarkali_1793417013639 to "Kurti",
                                                    R.drawable.dress_saree_1793417052582 to "Saree",
                                                    R.drawable.dress_lehenga_1793417067159 to "Lehenga"
                                                ).forEach { (resId, label) ->
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        border = if (item.imageRes == resId) androidx.compose.foundation.BorderStroke(2.dp, MeeshoMagenta) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD)),
                                                        modifier = Modifier.size(36.dp).clickable {
                                                            val targetIdx = bulkProducts.indexOfFirst { it.id == item.id }
                                                            if (targetIdx != -1) bulkProducts[targetIdx] = item.copy(imageRes = resId)
                                                        }
                                                    ) {
                                                        Image(painter = painterResource(id = resId), contentDescription = label, contentScale = ContentScale.Crop)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Title
                                    OutlinedTextField(
                                        value = item.title,
                                        onValueChange = { newTitle ->
                                            val targetIdx = bulkProducts.indexOfFirst { it.id == item.id }
                                            if (targetIdx != -1) bulkProducts[targetIdx] = item.copy(title = newTitle)
                                        },
                                        label = { Text("Product Name *") },
                                        singleLine = true,
                                        colors = activeTextFieldColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Pricing: MRP, Seller Price & Live Discount
                                    val bMrp = item.mrp.toIntOrNull() ?: 0
                                    val bSeller = item.sellerPrice.toIntOrNull() ?: 0
                                    val bPerc = if (bMrp > bSeller && bMrp > 0) ((bMrp - bSeller) * 100 / bMrp) else 0

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = item.mrp,
                                            onValueChange = { newMrp ->
                                                val targetIdx = bulkProducts.indexOfFirst { it.id == item.id }
                                                if (targetIdx != -1) bulkProducts[targetIdx] = item.copy(mrp = newMrp)
                                            },
                                            label = { Text("MRP (₹) *") },
                                            singleLine = true,
                                            colors = activeTextFieldColors,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = item.sellerPrice,
                                            onValueChange = { newSeller ->
                                                val targetIdx = bulkProducts.indexOfFirst { it.id == item.id }
                                                if (targetIdx != -1) bulkProducts[targetIdx] = item.copy(sellerPrice = newSeller)
                                            },
                                            label = { Text("Seller Price (₹) *") },
                                            singleLine = true,
                                            colors = activeTextFieldColors,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    // Discount calculation strip
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFE8F5E9),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Discount: $bPerc% OFF", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Customer Saves: ₹${(bMrp - bSeller).coerceAtLeast(0)}", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                        }
                                    }

                                    // Coupon & Prepaid Discount
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = item.couponCode,
                                            onValueChange = { newCode ->
                                                val targetIdx = bulkProducts.indexOfFirst { it.id == item.id }
                                                if (targetIdx != -1) bulkProducts[targetIdx] = item.copy(couponCode = newCode.uppercase())
                                            },
                                            label = { Text("Coupon Code") },
                                            singleLine = true,
                                            colors = activeTextFieldColors,
                                            modifier = Modifier.weight(1.2f)
                                        )
                                        OutlinedTextField(
                                            value = item.prepaidDiscount,
                                            onValueChange = { newPrep ->
                                                val targetIdx = bulkProducts.indexOfFirst { it.id == item.id }
                                                if (targetIdx != -1) bulkProducts[targetIdx] = item.copy(prepaidDiscount = newPrep)
                                            },
                                            label = { Text("Prepaid Off (₹)") },
                                            singleLine = true,
                                            colors = activeTextFieldColors,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    // Color & Fabric
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = item.color,
                                            onValueChange = { newCol ->
                                                val targetIdx = bulkProducts.indexOfFirst { it.id == item.id }
                                                if (targetIdx != -1) bulkProducts[targetIdx] = item.copy(color = newCol)
                                            },
                                            label = { Text("Color") },
                                            singleLine = true,
                                            colors = activeTextFieldColors,
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = item.fabric,
                                            onValueChange = { newFab ->
                                                val targetIdx = bulkProducts.indexOfFirst { it.id == item.id }
                                                if (targetIdx != -1) bulkProducts[targetIdx] = item.copy(fabric = newFab)
                                            },
                                            label = { Text("Fabric") },
                                            singleLine = true,
                                            colors = activeTextFieldColors,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    // Stock Quantity
                                    OutlinedTextField(
                                        value = item.stockUnits,
                                        onValueChange = { newStk ->
                                            val targetIdx = bulkProducts.indexOfFirst { it.id == item.id }
                                            if (targetIdx != -1) bulkProducts[targetIdx] = item.copy(stockUnits = newStk)
                                        },
                                        label = { Text("Total Stock (Units)") },
                                        singleLine = true,
                                        colors = activeTextFieldColors,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    // Add Another Product Button
                    OutlinedButton(
                        onClick = {
                            val nextNum = bulkProducts.size + 1
                            bulkProducts.add(
                                MultipleUploadItem(
                                    id = System.currentTimeMillis().toString(),
                                    title = "Ethnic Festive Kurti Set #$nextNum",
                                    sku = "$bulkSkuPrefix-${100 + nextNum}",
                                    mrp = "1199",
                                    sellerPrice = "449",
                                    couponCode = "FESTIVE15",
                                    prepaidDiscount = "20",
                                    color = "Pastel Pink",
                                    fabric = "Pure Rayon & Cotton",
                                    stockUnits = "80",
                                    description = "Festive ethnic dress with floral print and gota patti work.",
                                    imageRes = R.drawable.dress_anarkali_1793417013639
                                )
                            )
                            Toast.makeText(context, "+ नया प्रोडक्ट जोड़ा गया (Added Product #${bulkProducts.size})", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MeeshoMagenta)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Add Another Product (नया प्रोडक्ट जोड़ें)", color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                    }

                    // Publish All Button
                    Button(
                        onClick = {
                            bulkProducts.forEach { bp ->
                                val mPrice = bp.sellerPrice.toIntOrNull()?.coerceAtLeast(1) ?: 450
                                val mrpVal = bp.mrp.toIntOrNull()?.coerceAtLeast(mPrice) ?: 999
                                val disc = if (mrpVal > mPrice && mrpVal > 0) ((mrpVal - mPrice) * 100 / mrpVal) else 0
                                val pDisc = bp.prepaidDiscount.toIntOrNull() ?: 20
                                val stockVal = bp.stockUnits.toIntOrNull()?.coerceAtLeast(1) ?: 100

                                allProductsList.add(
                                    0,
                                    CatalogProductItem(
                                        id = System.currentTimeMillis().toString() + bp.id,
                                        title = bp.title,
                                        sku = bp.sku,
                                        gyanviPrice = mPrice,
                                        mrp = mrpVal,
                                        discountPercent = disc,
                                        couponCode = bp.couponCode,
                                        prepaidDiscount = pDisc,
                                        status = "QC Pass",
                                        category = "$bulkCategory > $bulkSubCategory",
                                        dateAdded = "Just now",
                                        color = bp.color,
                                        fabric = bp.fabric,
                                        sizes = bp.sizes.toList(),
                                        totalStock = stockVal,
                                        imageRes = bp.imageRes
                                    )
                                )
                                onPublishProductToCatalog(
                                    bp.title,
                                    bulkCategory.ifBlank { "Women" },
                                    bulkSubCategory.ifBlank { bulkCategory },
                                    brandName.ifBlank { "GYANVI DRESSES" },
                                    bp.description,
                                    mPrice,
                                    mrpVal,
                                    bp.color,
                                    bp.fabric,
                                    bp.sizes.toList(),
                                    selectedNumericSizes.toList(),
                                    stockVal,
                                    bp.sku,
                                    productWeight.ifBlank { "350g" },
                                    bp.imageRes,
                                    null,
                                    emptyList()
                                )
                            }
                            selectedTopTab = 2 // Switch to All Products tab
                            Toast.makeText(context, "🎉 सभी ${bulkProducts.size} प्रोडक्ट Room DB व लाइव कैटलॉग में अपलोड हो गए!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Publish All Products (${bulkProducts.size}) - सबमिट करें", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }

            2 -> {
                // =============================================================
                // TAB 2: ALL PRODUCTS (REPOSITORY LIST)
                // =============================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Search and Filter Header
                    Card(
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = allProductsSearch,
                                onValueChange = { allProductsSearch = it },
                                placeholder = { Text("Search by product name, SKU or category...", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    if (allProductsSearch.isNotEmpty()) {
                                        IconButton(onClick = { allProductsSearch = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                colors = activeTextFieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Status Filter Chips
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(listOf("All", "QC Pass", "QC in Progress", "Draft", "QC Error")) { filter ->
                                    val isSelected = allProductsFilter == filter
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) MeeshoMagenta else Color(0xFFEEEEEE),
                                        modifier = Modifier.clickable { allProductsFilter = filter }
                                    ) {
                                        Text(
                                            filter,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextPrimary,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Combine Uploaded items + Live Room/Storefront Catalog Products
                    val combinedCatalogItems = remember(allProductsList.size, existingProducts) {
                        val dbMapped = existingProducts.map { prod ->
                            CatalogProductItem(
                                id = prod.id,
                                title = prod.name,
                                sku = prod.sku.ifBlank { "GYN-${prod.id.takeLast(5).uppercase()}" },
                                gyanviPrice = prod.price,
                                mrp = prod.originalPrice,
                                discountPercent = prod.discountPercent,
                                couponCode = "FESTIVE15",
                                prepaidDiscount = 20,
                                status = if (prod.inStock) "QC Pass" else "Draft",
                                category = "${prod.category} > ${prod.subcategory}",
                                dateAdded = "Live DB",
                                color = prod.color.ifBlank { "Multi" },
                                fabric = prod.fabric,
                                sizes = prod.sizes,
                                totalStock = if (prod.inStock) 50 else 0,
                                imageRes = prod.imageRes
                            )
                        }
                        (allProductsList + dbMapped).distinctBy { it.title.trim().lowercase() }
                    }

                    // Filtered Products List
                    val filteredProducts = combinedCatalogItems.filter { item ->
                        (allProductsFilter == "All" || item.status.equals(allProductsFilter, ignoreCase = true)) &&
                        (allProductsSearch.isBlank() || item.title.contains(allProductsSearch, ignoreCase = true) || item.sku.contains(allProductsSearch, ignoreCase = true) || item.category.contains(allProductsSearch, ignoreCase = true))
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredProducts) { prod ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.size(76.dp).clip(RoundedCornerShape(8.dp))) {
                                            Image(
                                                painter = painterResource(id = prod.imageRes),
                                                contentDescription = prod.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            IconButton(
                                                onClick = {
                                                    ImageSaver.saveToGallery(context, prod.imageRes, prod.title)
                                                    Toast.makeText(context, "✓ फोटो गैलरी में सेव हो गई!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .size(24.dp)
                                                    .background(Color.Black.copy(alpha = 0.65f))
                                            ) {
                                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Status Badge
                                                val badgeBg = when (prod.status) {
                                                    "QC Pass" -> Color(0xFFE8F5E9)
                                                    "QC in Progress" -> Color(0xFFE3F2FD)
                                                    "QC Error" -> Color(0xFFFFEBEE)
                                                    else -> Color(0xFFFFF3E0)
                                                }
                                                val badgeColor = when (prod.status) {
                                                    "QC Pass" -> Color(0xFF2E7D32)
                                                    "QC in Progress" -> Color(0xFF1976D2)
                                                    "QC Error" -> Color(0xFFD32F2F)
                                                    else -> Color(0xFFE65100)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(badgeBg)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(prod.status, fontSize = 10.sp, color = badgeColor, fontWeight = FontWeight.Bold)
                                                }
                                                Text(prod.dateAdded, fontSize = 10.sp, color = TextMuted)
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("SKU: ${prod.sku} • ${prod.category}", fontSize = 11.sp, color = TextSecondary)

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("₹${prod.gyanviPrice}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TextPrimary)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("₹${prod.mrp}", fontSize = 12.sp, color = TextMuted, textDecoration = TextDecoration.LineThrough)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("${prod.discountPercent}% OFF", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Coupon: ${prod.couponCode} • Prepaid: -₹${prod.prepaidDiscount}", fontSize = 11.sp, color = Color(0xFF3F51B5), fontWeight = FontWeight.Medium)
                                        TextButton(
                                            onClick = {
                                                selectedTopTab = 0
                                                currentStep = 2
                                                productName = prod.title
                                                meeshoPrice = prod.gyanviPrice.toString()
                                                mrpPrice = prod.mrp.toString()
                                                selectedImageRes = prod.imageRes
                                                couponCode = prod.couponCode
                                                prepaidDiscount = prod.prepaidDiscount.toString()
                                                Toast.makeText(context, "Editing ${prod.title}", Toast.LENGTH_SHORT).show()
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Edit Details", fontSize = 11.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG: ADD SIZE
    // =========================================================================
    if (showAddSizeDialog) {
        AlertDialog(
            onDismissRequest = { showAddSizeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MeeshoMagenta)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Size Variant", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Age & Years Quick Chips (साल / उम्र):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    val quickSizes = listOf("0-6 M", "6-12 M", "1-2 Y", "2-3 Y", "3-4 Y", "4-5 Y", "6-7 Y", "8-9 Y", "10-12 Y", "12-14 Y", "14-16 Y", "S", "M", "L", "XL", "XXL", "Free Size")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickSizes.forEach { sz ->
                            val isSel = newSizeName == sz
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSel) Color(0xFFF3E8FF) else Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color(0xFF7C3AED) else Color(0xFFCBD5E1)),
                                modifier = Modifier.clickable {
                                    newSizeName = sz
                                    newSizeSku = "GWN-${sz.replace(" ", "")}"
                                }
                            ) {
                                Text(
                                    sz,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color(0xFF7C3AED) else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newSizeName,
                        onValueChange = { newSizeName = it },
                        label = { Text("Size / Age / Years (e.g. 2-3 Y, 4-5 Y, XL) *") },
                        singleLine = true,
                        colors = activeTextFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSizeInventory,
                        onValueChange = { newSizeInventory = it },
                        label = { Text("Inventory (Stock) *") },
                        singleLine = true,
                        colors = activeTextFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSizeSku,
                        onValueChange = { newSizeSku = it },
                        label = { Text("SKU ID *") },
                        singleLine = true,
                        colors = activeTextFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newSizeBust,
                            onValueChange = { newSizeBust = it },
                            label = { Text("Bust Size") },
                            singleLine = true,
                            colors = activeTextFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newSizeLength,
                            onValueChange = { newSizeLength = it },
                            label = { Text("Length Size") },
                            singleLine = true,
                            colors = activeTextFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSizeName.isNotBlank()) {
                            variantSizes.add(
                                ProductVariantSize(
                                    size = newSizeName.trim(),
                                    inventory = newSizeInventory.ifBlank { "20" },
                                    skuId = newSizeSku.ifBlank { "GWN-${newSizeName.uppercase()}" },
                                    bustSize = newSizeBust.ifBlank { "34 in" },
                                    lengthSize = newSizeLength.ifBlank { "42 in" }
                                )
                            )
                            Toast.makeText(context, "✓ Size $newSizeName जोड़ा गया!", Toast.LENGTH_SHORT).show()
                            showAddSizeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Add Size")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSizeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // =========================================================================
    // MODAL: SEARCHABLE CLOTHING & FOOTWEAR CATEGORY PICKER POPUP
    // =========================================================================
    if (showCategorySheet) {
        com.example.ui.components.ClothingCategoryPickerDialog(
            initialCategory = if (pickerTargetBulk) bulkCategory else selectedMainCategory,
            initialSubcategory = if (pickerTargetBulk) bulkSubCategory else selectedSubCategory,
            categories = categories,
            subCategories = subCategories,
            onDismiss = { showCategorySheet = false },
            onSelect = { mainCat, subCat, suggestedName, imgRes ->
                if (pickerTargetBulk) {
                    bulkCategory = mainCat
                    bulkSubCategory = subCat
                } else {
                    selectedMainCategory = mainCat
                    selectedSubCategory = subCat
                    selectedCategory = "$mainCat > $subCat"
                    if (!suggestedName.isNullOrBlank()) {
                        productName = suggestedName
                    }
                    selectedImageRes = imgRes
                }
                showCategorySheet = false
            }
        )
    }
}

@Composable
private fun StepIndicatorItem(
    stepNumber: Int,
    title: String,
    isCompleted: Boolean,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> Color(0xFF4CAF50)
                        isActive -> Color(0xFF1976D2)
                        else -> Color(0xFFE0E0E0)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Text(
                    text = "$stepNumber",
                    color = if (isActive) Color.White else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) TextPrimary else TextSecondary
        )
    }
}

@Composable
private fun StepDivider(isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .width(26.dp)
            .height(2.dp)
            .background(if (isCompleted) Color(0xFF4CAF50) else Color(0xFFE0E0E0))
    )
}
