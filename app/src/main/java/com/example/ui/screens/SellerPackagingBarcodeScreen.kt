package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.QrAwbCameraScannerDialog
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.util.ImageSaver
import com.example.util.QrCodeGenerator

// =========================================================================
// DATA MODELS FOR PACKAGING & BARCODE SYSTEM
// =========================================================================
data class PackagingVendor(
    val id: String,
    val name: String,
    val location: String,
    val sizesCount: Int,
    val basePricePerPacket: Double,
    val rating: Double = 4.8,
    val deliveryDays: String = "2-3 Days",
    val materials: List<String> = listOf("Tamper-Proof Poly", "60 Micron Thick", "POD Sleeve")
)

data class PackagingOrder(
    val orderId: String,
    val vendorName: String,
    val size: String,
    val quantity: Int,
    val totalAmount: Double,
    val date: String,
    val status: String,
    val trackingAwb: String
)

data class BarcodeMapping(
    val barcodeId: String,
    val linkedOrderId: String,
    val sku: String,
    val sizeName: String,
    val status: String,
    val generatedDate: String
)

// =========================================================================
// MAIN FULL BUY BARCODED PACKETS SCREEN
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullBuyBarcodedPacketsScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    // Role Switch: 0 = Seller View, 1 = Admin Control Panel
    var activeRole by remember { mutableIntStateOf(0) }

    // 4 Main Tabs: 0 = Packaging Store, 1 = Barcode Management & Scan, 2 = Shipping Verification, 3 = Return & RTO Guard
    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog States
    var showBuyModal by remember { mutableStateOf(false) }
    var selectedVendorForBuy by remember { mutableStateOf<PackagingVendor?>(null) }
    var buyPacketSize by remember { mutableStateOf("10 x 12 inch (Kurti Standard)") }
    var buyQuantity by remember { mutableStateOf("500") }
    var deliveryPincode by remember { mutableStateOf("845438") }
    var paymentMethod by remember { mutableStateOf("UPI (Instant 2% Off)") }

    var showCustomOrderModal by remember { mutableStateOf(false) }
    var customBespokeSize by remember { mutableStateOf("12 x 14 inch") }
    var customLogoName by remember { mutableStateOf("GYANVI DRESSES") }
    var customQuantityInput by remember { mutableStateOf("1000") }

    var showVideoTutorialDialog by remember { mutableStateOf(false) }
    var showBarcodeScanModal by remember { mutableStateOf(false) }
    var scannedBarcodeResult by remember { mutableStateOf("") }
    var showPrintLabelModal by remember { mutableStateOf(false) }
    var selectedBarcodeForPrint by remember { mutableStateOf<BarcodeMapping?>(null) }

    // AI Packaging Recommender State
    var aiApparelType by remember { mutableStateOf("Banarasi Heavy Silk Saree") }
    var aiRecommendedSize by remember { mutableStateOf("12 x 14 inch (75 Micron Heavy Duty)") }
    var aiExplanation by remember { mutableStateOf("Recommended to prevent fabric snagging and ensure bubble-sealed moisture protection during monsoon transit.") }

    // Stock Level
    var currentInventoryStock by remember { mutableIntStateOf(42) } // Alert under 50

    // Vendors Data (Matching Screenshot 6:47)
    val vendors = remember {
        mutableStateListOf(
            PackagingVendor("V-1", "Parin Polymers", "Chandigarh", 9, 1.02),
            PackagingVendor("V-2", "Krishna Polymers", "Ajmer", 9, 1.06),
            PackagingVendor("V-3", "Pacfo", "Surat", 11, 1.07),
            PackagingVendor("V-4", "VS Products", "Delhi NCR", 16, 1.07),
            PackagingVendor("V-5", "PicknPack", "New Delhi", 12, 1.09)
        )
    }

    // Orders History
    val pastOrders = remember {
        mutableStateListOf(
            PackagingOrder("PKG-8491", "Parin Polymers", "10 x 12 inch", 500, 560.0, "Nov 04, 2026", "Delivered", "DEL-AWB-98412"),
            PackagingOrder("PKG-8120", "Pacfo", "12 x 14 inch (Saree)", 250, 310.0, "Oct 22, 2026", "Delivered", "DEL-AWB-77123")
        )
    }

    // Barcode mappings
    val barcodesList = remember {
        mutableStateListOf(
            BarcodeMapping("GYN-PKT-9812", "ODR-90421", "GWN-ANK-101", "10 x 12 in", "Dispatched (Linked)", "Nov 08, 2026"),
            BarcodeMapping("GYN-PKT-9813", "ODR-90422", "GWN-BNS-202", "12 x 14 in", "Packed (Awaiting Pickup)", "Nov 09, 2026"),
            BarcodeMapping("GYN-PKT-9814", "Unlinked", "Unassigned", "10 x 12 in", "Available in Stock", "Nov 09, 2026"),
            BarcodeMapping("GYN-PKT-9815", "Unlinked", "Unassigned", "8 x 10 in", "Available in Stock", "Nov 09, 2026")
        )
    }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 2.dp) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("buy_packets_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Buy Barcoded Packets", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                            Text("Mandatory packaging & barcode tracking for RTO protection", fontSize = 11.sp, color = TextSecondary)
                        }

                        // Role Switcher: Seller Hub vs Admin Mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (activeRole == 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                                .clickable {
                                    activeRole = if (activeRole == 0) 1 else 0
                                    Toast.makeText(
                                        context,
                                        if (activeRole == 1) "Switched to Packaging Admin Panel" else "Switched to Seller Hub View",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (activeRole == 0) Icons.Default.QrCode else Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = if (activeRole == 0) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (activeRole == 0) "Seller Hub" else "Admin Mode",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeRole == 0) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                )
                            }
                        }
                    }

                    // 4 Core Sub-Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        edgePadding = 12.dp,
                        containerColor = CardWhite,
                        contentColor = MeeshoMagenta,
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = MeeshoMagenta
                                )
                            }
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Packaging Store", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Barcode Management", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Shipping & Labels", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = { Text("RTO & Tamper Guard", fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) }
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: PACKAGING STORE (Exact match to Screenshot 6:47)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Stock Alert Card
                        if (currentInventoryStock < 50) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Low Packaging Stock Alert ($currentInventoryStock Packets Remaining)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFE65100))
                                        Text("Reorder now to avoid courier pickup cancellation. Delivery takes 2-3 days.", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // "Why use Barcoded Packets ?" Section (Matching screenshot 6:47)
                        Text("Why use Barcoded Packets ?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Using Barcoded Packet and linking them is mandatory for raising RTO claims.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Your order is secured from tampering and mismatch.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Video Card: "How to purchase and scan branded packets" (Matching screenshot 6:47)
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3DB5)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clickable { showVideoTutorialDialog = true }
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE53935)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play Video", tint = Color.White, modifier = Modifier.size(28.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "How to purchase and scan branded packets",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // AUTHORISED VENDORS Header with + Custom Order Button (Matching screenshot 6:47)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AUTHORISED VENDORS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)
                            OutlinedButton(
                                onClick = { showCustomOrderModal = true },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("custom_order_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Custom Order (Text Box)", fontSize = 11.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Vendors List (Parin Polymers, Krishna Polymers, Pacfo, VS Products, PicknPack)
                        vendors.forEach { vendor ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(vendor.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Location: ${vendor.location} • Sizes available: ${vendor.sizesCount}", fontSize = 12.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Price: Rs ${vendor.basePricePerPacket} per packet (6.5x8 inch)", fontSize = 12.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("★ ${vendor.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        Text(" • Fast Dispatch (${vendor.deliveryDays}) • 60 Micron Tamper-Proof", fontSize = 11.sp, color = TextMuted)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            selectedVendorForBuy = vendor
                                            showBuyModal = true
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp)
                                            .testTag("buy_now_${vendor.id}")
                                    ) {
                                        Text("Buy Now", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // AI Smart Packaging Recommendation Card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AI Packaging Size Assistant", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MeeshoMagenta)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Select your clothing item to get the ideal bag size & thickness:", fontSize = 11.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("Kurti", "Saree", "Sharara", "Kids Wear").forEach { type ->
                                        FilterChip(
                                            selected = aiApparelType.contains(type),
                                            onClick = {
                                                aiApparelType = type
                                                when (type) {
                                                    "Kurti" -> {
                                                        aiRecommendedSize = "10 x 12 inch (60 Micron POD Bag)"
                                                        aiExplanation = "Standard size for Kurtis, Tops & Tunics. Keeps parcel under 500g shipping slab."
                                                    }
                                                    "Saree" -> {
                                                        aiRecommendedSize = "12 x 14 inch (75 Micron Heavy Duty)"
                                                        aiExplanation = "Prevents zari fabric damage and accommodates matching petticoat/blouse."
                                                    }
                                                    "Sharara" -> {
                                                        aiRecommendedSize = "14 x 16 inch (Tamper-Proof Flap)"
                                                        aiExplanation = "High-volume flare packaging. Sealed adhesive stops transit moisture."
                                                    }
                                                    else -> {
                                                        aiRecommendedSize = "8 x 10 inch (Lightweight Poly)"
                                                        aiExplanation = "Best for frocks, baby suits and light kidswear. Lowest cost (₹0.85/pkt)."
                                                    }
                                                }
                                            },
                                            label = { Text(type, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White)
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text("AI Suggested Size: $aiRecommendedSize", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                        Text(aiExplanation, fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }

                1 -> {
                    // TAB 1: BARCODE MANAGEMENT & SCANNER
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("Barcode Management & Scanning", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Assign unique barcodes to orders to qualify for 100% RTO claims", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Actions
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { showBarcodeScanModal = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                modifier = Modifier.weight(1f).height(44.dp).testTag("scan_barcode_btn")
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan Barcode", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val newId = "GYN-PKT-${(1000..9999).random()}"
                                    barcodesList.add(0, BarcodeMapping(newId, "Unlinked", "Unassigned", "10 x 12 in", "Available in Stock", "Today"))
                                    Toast.makeText(context, "Generated Barcode $newId", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Generate Barcode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Active Barcode Stock & Mappings", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        barcodesList.forEach { barcode ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(barcode.barcodeId, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A237E))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (barcode.status.contains("Linked") || barcode.status.contains("Packed")) Color(0xFFE8F5E9) else Color(0xFFEDE7F6))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                barcode.status,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (barcode.status.contains("Linked") || barcode.status.contains("Packed")) Color(0xFF2E7D32) else MeeshoMagenta
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Linked Order: ${barcode.linkedOrderId} • SKU: ${barcode.sku}", fontSize = 12.sp, color = TextPrimary)
                                    Text("Packet Size: ${barcode.sizeName} • Created: ${barcode.generatedDate}", fontSize = 11.sp, color = TextSecondary)

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        OutlinedButton(
                                            onClick = {
                                                selectedBarcodeForPrint = barcode
                                                showPrintLabelModal = true
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Print Label", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }

                2 -> {
                    // TAB 2: SHIPPING VERIFICATION & LABELS
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("Shipping Label & Dispatch Manifest", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Ensure barcode is readable and tamper-tape is sealed before courier handover", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Pre-Dispatch Verification Checklist", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.height(10.dp))

                                listOf(
                                    "1. Item placed inside original transparent inner bag",
                                    "2. Barcode packet sealed with high-adhesion tamper flap",
                                    "3. Outer shipping label printed with high-contrast 300 DPI barcode",
                                    "4. Courier AWB barcode matched with seller portal packet ID"
                                ).forEach { step ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(step, fontSize = 12.sp, color = TextPrimary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Generated Shipping Manifest (PDF) for 4 parcels", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Download Courier Handover Manifest (PDF)")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                3 -> {
                    // TAB 3: RTO & TAMPER GUARD
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("RTO & Tamper Protection Center", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Verify returned packages and claim 100% compensation for wrong/damaged returns", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Zero-Loss RTO Protection Active", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                                    Text("Since your orders are dispatched in authorised barcoded packets, all courier transit damage and wrong return claims are approved automatically within 48 hours.", fontSize = 11.sp, color = TextPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                Toast.makeText(context, "Scan returned package barcode to initiate instant refund claim", Toast.LENGTH_LONG).show()
                                showBarcodeScanModal = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan Returned RTO Packet for Claim", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    // =========================================================================
    // MODALS & DIALOGS
    // =========================================================================

    // 1. BUY PACKET MODAL (Size, Quantity, Price, Payment & Pincode)
    if (showBuyModal && selectedVendorForBuy != null) {
        val vendor = selectedVendorForBuy!!
        val qtyInt = buyQuantity.toIntOrNull() ?: 500
        val baseUnitPrice = when {
            buyPacketSize.contains("6.5") -> vendor.basePricePerPacket
            buyPacketSize.contains("8 x 10") -> vendor.basePricePerPacket + 0.15
            buyPacketSize.contains("10 x 12") -> vendor.basePricePerPacket + 0.35
            buyPacketSize.contains("12 x 14") -> vendor.basePricePerPacket + 0.55
            else -> vendor.basePricePerPacket + 0.85
        }
        val subtotal = baseUnitPrice * qtyInt
        val gst = subtotal * 0.18
        val totalCost = subtotal + gst

        Dialog(onDismissRequest = { showBuyModal = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Order Barcoded Packets", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        IconButton(onClick = { showBuyModal = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }
                    Text("Vendor: ${vendor.name} (${vendor.location})", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Size Selection
                    Text("Select Packet Size", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    listOf(
                        "6.5 x 8 inch (Small Accessories / Jewelry)",
                        "8 x 10 inch (Kids Wear / Blouses)",
                        "10 x 12 inch (Kurti Standard - Most Popular)",
                        "12 x 14 inch (Saree & Salwar Suit Extra Strong)",
                        "14 x 16 inch (Heavy Lehenga / Sherwani)"
                    ).forEach { size ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { buyPacketSize = size }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = buyPacketSize == size, onClick = { buyPacketSize = size })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(size, fontSize = 12.sp, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quantity Selection
                    Text("Select Quantity (Packets)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("100", "250", "500", "1000", "2500").forEach { q ->
                            FilterChip(
                                selected = buyQuantity == q,
                                onClick = { buyQuantity = q },
                                label = { Text(q, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Delivery Pincode
                    Text("Delivery Pincode (Warehouse)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = deliveryPincode,
                        onValueChange = { deliveryPincode = it },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Price Breakdown Box
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("$qtyInt Packets @ ₹${String.format("%.2f", baseUnitPrice)}", fontSize = 12.sp, color = TextSecondary)
                                Text("₹${String.format("%.2f", subtotal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("GST (18% Input Tax Credit)", fontSize = 12.sp, color = TextSecondary)
                                Text("₹${String.format("%.2f", gst)}", fontSize = 12.sp, color = TextSecondary)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = DividerColor)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Payable", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text("₹${String.format("%.2f", totalCost)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MeeshoMagenta)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            currentInventoryStock += qtyInt
                            pastOrders.add(0, PackagingOrder("PKG-${(1000..9999).random()}", vendor.name, buyPacketSize, qtyInt, totalCost, "Today", "Processing", "AWB-NEW-${(1000..9999).random()}"))
                            showBuyModal = false
                            Toast.makeText(context, "Order for $qtyInt packets placed successfully with ${vendor.name}!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Pay ₹${String.format("%.2f", totalCost)} & Place Order", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 2. CUSTOM ORDER MODAL (Text Box for custom sizes & logo printing)
    if (showCustomOrderModal) {
        Dialog(onDismissRequest = { showCustomOrderModal = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Custom Packaging Order", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text("Order bespoke bag dimensions with custom brand printing", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Custom Dimensions (Width x Height)", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = customBespokeSize,
                        onValueChange = { customBespokeSize = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Brand Logo / Store Name to Print", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = customLogoName,
                        onValueChange = { customLogoName = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Minimum Quantity (500+ units)", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = customQuantityInput,
                        onValueChange = { customQuantityInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCustomOrderModal = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showCustomOrderModal = false
                                Toast.makeText(context, "Custom quotation requested for $customQuantityInput units of '$customLogoName' packets!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                        ) {
                            Text("Request Custom Quote")
                        }
                    }
                }
            }
        }
    }

    // 3. VIDEO TUTORIAL MODAL
    if (showVideoTutorialDialog) {
        Dialog(onDismissRequest = { showVideoTutorialDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("How to Pack & Scan Packets", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        IconButton(onClick = { showVideoTutorialDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    listOf(
                        "Step 1: Put the folded kurti/saree inside the transparent inner protective cover.",
                        "Step 2: Slide into the official Gyanvi Dresses Barcoded Packet.",
                        "Step 3: Peel the self-adhesive release liner and press firmly to create a permanent tamper-evident seal.",
                        "Step 4: Scan the packet barcode on the seller portal to link with the Order ID before sticking the courier shipping label.",
                        "Step 5: Hand over parcel to delivery partner and retain signed manifest copy."
                    ).forEach { step ->
                        Text("• $step", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.padding(vertical = 4.dp), lineHeight = 17.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showVideoTutorialDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Got It, Thanks!")
                    }
                }
            }
        }
    }

    // 4. REAL CAMERA QR CODE & AWB BARCODE SCANNER MODAL
    if (showBarcodeScanModal) {
        QrAwbCameraScannerDialog(
            onApplyScannedCode = { scanned, _ ->
                scannedBarcodeResult = scanned
                Toast.makeText(context, "Scanned Barcode: $scanned (Verified Authentic)", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showBarcodeScanModal = false }
        )
    }

    // 5. PRINT / SAVE QR SHIPPING LABEL PREVIEW MODAL
    if (showPrintLabelModal && selectedBarcodeForPrint != null) {
        val bc = selectedBarcodeForPrint!!
        val packetQrBitmap = remember(bc.barcodeId, bc.linkedOrderId) {
            QrCodeGenerator.generateShippingLabelBitmap(
                orderId = bc.linkedOrderId,
                awbNumber = bc.barcodeId,
                customerName = "Verified Branded Packet",
                destination = "Gyanvi Dresses Packaging Guard",
                productTitle = "Packet Size: ${bc.sizeName}",
                sku = bc.sku,
                size = bc.sizeName,
                amount = 0,
                paymentMethod = bc.status
            )
        }
        Dialog(onDismissRequest = { showPrintLabelModal = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Shipping Label & QR Preview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = packetQrBitmap.asImageBitmap(),
                            contentDescription = "Packet Shipping Label QR",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                ImageSaver.saveBitmapToGallery(
                                    context = context,
                                    bitmap = packetQrBitmap,
                                    fileNamePrefix = "PacketQR_${bc.barcodeId}"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                ImageSaver.saveBitmapToDownloads(
                                    context = context,
                                    bitmap = packetQrBitmap,
                                    fileNamePrefix = "PacketLabel_${bc.barcodeId}"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showPrintLabelModal = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close", color = TextSecondary)
                    }
                }
            }
        }
    }
}
