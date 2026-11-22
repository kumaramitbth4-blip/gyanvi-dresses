package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.SampleData
import com.example.model.OrderItem
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.SellerOrderActions
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.HoldOrderPdfGenerator
import com.example.util.ImageSaver
import com.example.util.QrCodeGenerator
import com.example.viewmodel.CloudSyncState

data class SellerOrderItem(
    val id: String,
    val subOrderId: String,
    val customerName: String,
    val productTitle: String,
    val sku: String,
    val size: String,
    val price: Int,
    val destination: String,
    var status: String, // "On Hold", "Pending", "Ready to Ship", "Shipped", "Cancelled"
    val awb: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrdersTab(
    onNavigateTo: (SellerSubScreen) -> Unit,
    orders: List<OrderItem> = SampleData.initialOrders,
    products: List<Product> = SampleData.sampleProducts,
    sellerOrderActions: SellerOrderActions = SellerOrderActions(),
    onReadyToShip: (String) -> Unit = sellerOrderActions.onReadyToShip,
    onDispatchOrder: (String) -> Unit = { id -> sellerOrderActions.onDispatchOrder(id, SELLER_COURIER_PARTNERS.first()) },
    onCancelOrder: (String) -> Unit = { id -> sellerOrderActions.onCancelOrderDetailed(id, "Cancelled by Seller", "Seller", true) },
    onMarkLabelDownloaded: (String) -> Unit = sellerOrderActions.onMarkLabelDownloaded,
    onAddManualOrder: (customerName: String, address: String, product: Product, size: String, quantity: Int, paymentMethod: String) -> Unit = { _, _, _, _, _, _ -> },
    cloudSyncState: CloudSyncState = CloudSyncState.Idle,
    firebaseUserEmail: String = "",
    onGoogleCloudSignInClick: () -> Unit = {},
    onSyncAllOrdersClick: () -> Unit = {},
    initialOrderTab: Int = 1
) {
    val context = LocalContext.current
    var selectedOrderTab by remember(initialOrderTab) { mutableIntStateOf(initialOrderTab) } // 0=On Hold, 1=Pending, 2=Ready to Ship, 3=Shipped, 4=Cancelled, 5=Dashboard
    var stagePdfDisplayMode by remember { mutableIntStateOf(0) } // 0=PDF + Cards, 1=Sirf PDF Roop, 2=Sirf Cards
    var activeModalOrderId by remember { mutableStateOf<String?>(null) }
    var activeModalType by remember { mutableStateOf(SellerOrderModalType.NONE) }
    var showCustomQrStudio by remember { mutableStateOf(false) }
    var showCameraScannerDialog by remember { mutableStateOf(false) }
    var showAddHoldProductDialog by remember { mutableStateOf(false) }
    var showAllHoldPdfDialog by remember { mutableStateOf(false) }
    var showAddPendingProductDialog by remember { mutableStateOf(false) }
    var showAllPendingPdfDialog by remember { mutableStateOf(false) }
    var showAddReadyToShipDialog by remember { mutableStateOf(false) }
    var showAllReadyToShipPdfDialog by remember { mutableStateOf(false) }
    var showAddShippedCourierDialog by remember { mutableStateOf(false) }
    var showAllShippedCourierPdfDialog by remember { mutableStateOf(false) }
    var showAddCancelledRecordDialog by remember { mutableStateOf(false) }
    var showAllCancelledPdfDialog by remember { mutableStateOf(false) }
    var showAllOrdersPdfDialog by remember { mutableStateOf(false) }
    var showCurrentTabAllPdfDialog by remember { mutableStateOf(false) }

    // Ready to Ship bulk shipping & courier selection
    var selectedCourier by remember { mutableStateOf(SELLER_COURIER_PARTNERS.first()) }
    val selectedReadyIds = remember { mutableStateListOf<String>() }

    // Add Order Dialog state
    var showAddOrderDialog by remember { mutableStateOf(false) }
    var inputCustName by remember { mutableStateOf("") }
    var inputCustPhone by remember { mutableStateOf("+91 9128754982") }
    var inputCityPincode by remember { mutableStateOf("Station Road, Bettiah - 845438") }
    var selectedProductIndex by remember { mutableIntStateOf(0) }
    var inputSize by remember { mutableStateOf("L") }
    var inputColor by remember { mutableStateOf("Festive Maroon") }
    var inputQuantity by remember { mutableStateOf("1") }
    var inputPaymentMethod by remember { mutableStateOf("UPI (PhonePe)") }

    // Dialog & Sheet States
    var showPolicyModal by remember { mutableStateOf(false) }
    var showSearchIdSheet by remember { mutableStateOf(false) }
    var showSlaFilterSheet by remember { mutableStateOf(false) }
    var showLabelFilterSheet by remember { mutableStateOf(false) }
    var showFullFilterSheet by remember { mutableStateOf(false) }
    var showDispatchHealthModal by remember { mutableStateOf(false) }

    // Filter values
    var selectedSearchIdType by remember { mutableStateOf("Order ID / SKU") }
    var searchQuery by remember { mutableStateOf("") }
    val slaOptions = listOf("Breached", "Breaching Soon", "Others")
    val selectedSla = remember { mutableStateListOf<String>() }
    var selectedLabel by remember { mutableStateOf<String?>(null) } // "Yes", "No", or null
    var selectedDispatchDate by remember { mutableStateOf("All") }

    // Categorize real synced orders across all 5 seller workflow tabs
    val onHoldOrders = remember(orders) {
        orders.filter {
            it.status == OrderStatus.ORDERED && it.sellerStage.equals("On Hold", ignoreCase = true)
        }
    }
    val pendingOrders = remember(orders) {
        orders.filter {
            (it.status == OrderStatus.ORDERED ||
                it.status == OrderStatus.CONFIRMED ||
                it.status == OrderStatus.PROCESSING) &&
                !it.sellerStage.equals("Ready to Ship", ignoreCase = true) &&
                !it.sellerStage.equals("On Hold", ignoreCase = true)
        }
    }
    val readyToShipOrders = remember(orders) {
        orders.filter {
            it.status == OrderStatus.READY_TO_SHIP ||
                (it.status == OrderStatus.ORDERED && it.sellerStage.equals("Ready to Ship", ignoreCase = true))
        }
    }
    val shippedOrders = remember(orders) {
        orders.filter {
            it.status == OrderStatus.SHIPPED ||
                it.status == OrderStatus.OUT_FOR_DELIVERY ||
                it.status == OrderStatus.DELIVERED ||
                it.isRtoFlagged
        }
    }
    val cancelledOrders = remember(orders) {
        orders.filter { it.status == OrderStatus.CANCELLED && !it.isRtoFlagged }
    }

    val orderTabs = listOf(
        "On Hold (${onHoldOrders.size})",
        "Pending (${pendingOrders.size})",
        "Ready to Ship (${readyToShipOrders.size})",
        "Shipped (${shippedOrders.size})",
        "Cancelled (${cancelledOrders.size})",
        "Dashboard"
    )

    val tabOrders = when (selectedOrderTab) {
        0 -> onHoldOrders
        1 -> pendingOrders
        2 -> readyToShipOrders
        3 -> shippedOrders
        4 -> cancelledOrders
        else -> orders
    }

    val filteredTabOrders = remember(tabOrders, searchQuery, selectedLabel) {
        tabOrders.filter { ord ->
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                ord.orderId.contains(q, ignoreCase = true) ||
                ord.awbNumber.contains(q, ignoreCase = true) ||
                ord.product.id.contains(q, ignoreCase = true) ||
                ord.product.name.contains(q, ignoreCase = true) ||
                ord.customerName.contains(q, ignoreCase = true)
            val matchesLabel = when (selectedLabel) {
                "Yes" -> ord.isLabelDownloaded
                "No" -> !ord.isLabelDownloaded
                else -> true
            }
            matchesQuery && matchesLabel
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Surface(color = CardWhite, shadowElevation = 1.dp) {
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.widthIn(min = 108.dp)) {
                        Text("Orders (${orders.size})", fontWeight = FontWeight.Bold, fontSize = 19.sp, color = TextPrimary, maxLines = 1)
                        Text("${pendingOrders.size} Pending • ${readyToShipOrders.size} Ready", fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.End)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedOrderTab = if (selectedOrderTab == 5) 1 else 5
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedOrderTab == 5) Color(0xFFEDE7F6) else Color.Transparent,
                                contentColor = Color(0xFF1A237E)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp).testTag("seller_orders_dashboard_btn")
                        ) {
                            Icon(Icons.Default.Dashboard, contentDescription = "Dashboard", tint = Color(0xFF1A237E), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E), maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { showCameraScannerDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF047857)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp).testTag("seller_orders_camera_scan_btn")
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR & AWB", tint = Color(0xFF047857), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Scan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857), maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { showCustomQrStudio = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp).testTag("seller_orders_qr_studio_btn")
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("QR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta, maxLines = 1)
                        }
                        Button(
                            onClick = { showAddOrderDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp).testTag("seller_orders_add_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("New", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        IconButton(onClick = { onNavigateTo(SellerSubScreen.RETURNS_HELP) }) {
                            Icon(Icons.Default.HelpOutline, contentDescription = "Help", tint = TextSecondary)
                        }
                    }
                }

                // Alert Banner: Barcoded Packet Mandatory (Screenshot 11:10, 11:14)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF9C4))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Using and scanning original Barcoded Packet is mandatory to raise RTO claims",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB71C1C)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { onNavigateTo(SellerSubScreen.BUY_PACKETS) },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A237E)),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1A237E))),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Buy Barcoded Packets", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Row: Learn how to process orders
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { Toast.makeText(context, "Opening Order Processing Video Guide...", Toast.LENGTH_SHORT).show() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Learn how to process your orders?", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                // Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedOrderTab,
                    containerColor = CardWhite,
                    contentColor = Color(0xFF3F51B5),
                    edgePadding = 16.dp
                ) {
                    orderTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedOrderTab == index,
                            onClick = { selectedOrderTab = index },
                            text = {
                                Text(
                                    title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedOrderTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedOrderTab == index) Color(0xFF3F51B5) else TextSecondary
                                )
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Firestore Cloud Sync Status Card
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (cloudSyncState) {
                    is CloudSyncState.Synced -> Color(0xFFE8F5E9)
                    is CloudSyncState.Syncing -> Color(0xFFE3F2FD)
                    is CloudSyncState.Error -> Color(0xFFFFEBEE)
                    CloudSyncState.Idle -> Color(0xFFF3E5F5)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(
                    1.dp,
                    when (cloudSyncState) {
                        is CloudSyncState.Synced -> Color(0xFFA5D6A7)
                        is CloudSyncState.Syncing -> Color(0xFF90CAF9)
                        is CloudSyncState.Error -> Color(0xFFEF9A9A)
                        CloudSyncState.Idle -> Color(0xFFCE93D8)
                    },
                    RoundedCornerShape(10.dp)
                )
                .testTag("firestore_cloud_sync_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (cloudSyncState) {
                            is CloudSyncState.Synced -> "☁️ Firestore Cloud Sync Active"
                            is CloudSyncState.Syncing -> "☁️ Syncing Orders to Firestore..."
                            is CloudSyncState.Error -> "⚠️ Cloud Sync Issue"
                            CloudSyncState.Idle -> "☁️ Connect Google for Cloud Sync"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (cloudSyncState) {
                            is CloudSyncState.Synced -> Color(0xFF1B5E20)
                            is CloudSyncState.Syncing -> Color(0xFF0D47A1)
                            is CloudSyncState.Error -> Color(0xFFB71C1C)
                            CloudSyncState.Idle -> Color(0xFF4A148C)
                        }
                    )
                    Text(
                        text = when (cloudSyncState) {
                            is CloudSyncState.Synced -> "${cloudSyncState.email.ifBlank { firebaseUserEmail }} • ${cloudSyncState.orderCount} orders synced"
                            is CloudSyncState.Syncing -> "Updating Pending, Hold, Ready, Shipped & Cancelled orders..."
                            is CloudSyncState.Error -> cloudSyncState.message
                            CloudSyncState.Idle -> "Back up all orders live with Google Sign-In"
                        },
                        fontSize = 10.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (cloudSyncState is CloudSyncState.Synced || cloudSyncState is CloudSyncState.Error) {
                    OutlinedButton(
                        onClick = onSyncAllOrdersClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("firestore_sync_now_btn")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Now", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (cloudSyncState is CloudSyncState.Idle) {
                    Button(
                        onClick = onGoogleCloudSignInClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("firestore_connect_google_btn")
                    ) {
                        Text("Google Sync", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // =========================================================================
        // TOP QUICK PDF HUB: All 5 Workflow PDFs + Combined PDF visible in a 2x3 grid
        // (On Hold, Pending, Ready to Ship, Shipped, Cancelled, All PDF)
        // =========================================================================
        val customerPurchasedOrders = remember(orders) {
            orders.filter { ord ->
                ord.orderHistoryLog.any { it.contains("Customer Purchase", ignoreCase = true) }
            }
        }
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, Color(0xFFC5CAE9), RoundedCornerShape(10.dp))
                .testTag("quick_all_pdf_bar")
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📄 Sabhi PDF Show (5 Stages + Barcode)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF1A237E)
                    )
                    Text(
                        "${orders.size} Total Orders",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MeeshoMagenta
                    )
                }
                // Row 1: On Hold, Pending, Ready to Ship PDFs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { showAllHoldPdfDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f).height(36.dp).testTag("quick_hold_pdf_btn")
                    ) {
                        Text("Hold PDF (${onHoldOrders.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Button(
                        onClick = { showAllPendingPdfDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f).height(36.dp).testTag("quick_pending_pdf_btn")
                    ) {
                        Text("Pending PDF (${pendingOrders.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Button(
                        onClick = { showAllReadyToShipPdfDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f).height(36.dp).testTag("quick_ready_pdf_btn")
                    ) {
                        Text("Ready PDF (${readyToShipOrders.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
                // Row 2: Shipped, Cancelled, All Orders PDFs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { showAllShippedCourierPdfDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f).height(36.dp).testTag("quick_shipped_courier_pdf_btn")
                    ) {
                        Text("Shipped PDF (${shippedOrders.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Button(
                        onClick = { showAllCancelledPdfDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f).height(36.dp).testTag("quick_cancelled_pdf_btn")
                    ) {
                        Text("Cancelled PDF (${cancelledOrders.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Button(
                        onClick = { showAllOrdersPdfDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f).height(36.dp).testTag("quick_all_orders_pdf_btn")
                    ) {
                        Text("All PDF (${orders.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }

                if (customerPurchasedOrders.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7)),
                        modifier = Modifier.fillMaxWidth().testTag("customer_purchases_synced_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "🛍️ ${customerPurchasedOrders.size} Online Purchased Order(s) Synced!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "Open PDF →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MeeshoMagenta,
                                modifier = Modifier
                                    .clickable { showCurrentTabAllPdfDialog = true }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedOrderTab == 5) {
            SellerOrdersDashboardSection(
                orders = orders,
                products = products,
                autoTrackingEnabled = sellerOrderActions.autoTrackingEnabled,
                onJumpToOrderTab = { targetTab -> selectedOrderTab = targetTab },
                onOpenAllOrdersPdf = { showAllOrdersPdfDialog = true },
                onOpenShippedPdf = { showAllShippedCourierPdfDialog = true },
                onSyncLiveTracking = {
                    sellerOrderActions.onSyncLiveCourierTracking()
                    Toast.makeText(context, "🔄 Live courier tracking synced!", Toast.LENGTH_SHORT).show()
                },
                onOpenFullBusinessDashboard = { onNavigateTo(SellerSubScreen.BUSINESS_DASHBOARD) },
                cloudSyncState = cloudSyncState,
                firebaseUserEmail = firebaseUserEmail,
                onGoogleCloudSignInClick = onGoogleCloudSignInClick,
                onSyncAllOrdersClick = onSyncAllOrdersClick,
                onOpenCameraScanner = { showCameraScannerDialog = true },
                onInspectOrder = { ord ->
                    activeModalOrderId = ord.orderId
                    activeModalType = SellerOrderModalType.VIEW_DETAILS_AND_HISTORY
                },
                onOpenOrderPdf = { ord ->
                    activeModalOrderId = ord.orderId
                    activeModalType = SellerOrderModalType.GENERAL_PDF_SINGLE
                },
                sellerOrderActions = sellerOrderActions
            )
        } else {
            // =========================================================================
            // INLINE STAGE PDF DISPLAY ("On Hold, Pending, Ready to Ship, Shipped, Cancelled me PDF ke roop show ho")
            // =========================================================================
            val activeStageName = when (selectedOrderTab) {
                0 -> "On Hold"
                1 -> "Pending"
                2 -> "Ready to Ship"
                3 -> "Shipped"
                4 -> "Cancelled"
                else -> "Orders"
            }
            val activeStageTitle = "GYANVI DRESSES • ${activeStageName.uppercase()} PDF (${filteredTabOrders.size} ITEMS)"
            val activeStagePrefix = "Gyanvi_${activeStageName.replace(" ", "_")}_PDF"

            StageInlinePdfViewerSection(
                stageName = activeStageName,
                stageTabIndex = selectedOrderTab,
                ordersInStage = filteredTabOrders,
                reportTitle = activeStageTitle,
                fileNamePrefix = activeStagePrefix,
                displayMode = stagePdfDisplayMode,
                onChangeDisplayMode = { stagePdfDisplayMode = it },
                onOpenFullPdfDialog = { showCurrentTabAllPdfDialog = true }
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (stagePdfDisplayMode != 1) {
                SellerTabStageBanner(
                    selectedTab = selectedOrderTab,
                    ordersInTab = tabOrders,
                    selectedReadyIds = selectedReadyIds,
                    selectedCourier = selectedCourier,
                    onCourierChange = { selectedCourier = it },
                    actions = sellerOrderActions,
                    onOpenAddHoldProduct = { showAddHoldProductDialog = true },
                    onOpenAllHoldPdf = { showAllHoldPdfDialog = true },
                    onOpenAddPendingProduct = { showAddPendingProductDialog = true },
                    onOpenAllPendingPdf = { showAllPendingPdfDialog = true },
                    onOpenAddReadyProduct = { showAddReadyToShipDialog = true },
                    onOpenAllReadyPdf = { showAllReadyToShipPdfDialog = true },
                    onOpenAddShippedCourier = { showAddShippedCourierDialog = true },
                    onOpenAllShippedCourierPdf = { showAllShippedCourierPdfDialog = true },
                    onOpenAddCancelledRecord = { showAddCancelledRecordDialog = true },
                    onOpenAllCancelledPdf = { showAllCancelledPdfDialog = true }
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Search ID filter box
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSearchIdSheet = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Select Search ID", fontSize = 11.sp, color = TextMuted)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("$selectedSearchIdType ▾", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Enter $selectedSearchIdType...", fontSize = 13.sp) },
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // SLA, Label, Filter action buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showSlaFilterSheet = true },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedSla.isNotEmpty()) Color(0xFFEDE7F6) else CardWhite
                    )
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (selectedSla.isEmpty()) "SLA" else "SLA (${selectedSla.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = { showLabelFilterSheet = true },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedLabel != null) Color(0xFFEDE7F6) else CardWhite
                    )
                ) {
                    Icon(Icons.Default.Label, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (selectedLabel == null) "Label" else "Label: $selectedLabel",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = { showFullFilterSheet = true },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedSla.isNotEmpty() || selectedLabel != null) Color(0xFFEDE7F6) else CardWhite
                    )
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filter", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (stagePdfDisplayMode != 1) {
                if (filteredTabOrders.isEmpty()) {
                    // Empty state when no orders match current tab/search filter
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF0F4F8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AllInbox, contentDescription = null, tint = Color(0xFF90A4AE), modifier = Modifier.size(54.dp))
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                if (searchQuery.isNotEmpty()) "No matching orders found" else "No orders in this tab",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (searchQuery.isNotEmpty()) "Try searching with a different Order ID or SKU" else "Tap '+ New' above or place a customer order to see it here immediately.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showAddOrderDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Sample Order", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Quick bulk action bar: Save all displayed order labels & QR to Gallery
                    if (filteredTabOrders.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${filteredTabOrders.size} Order(s) Found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            TextButton(
                                onClick = {
                                    var savedCount = 0
                                    filteredTabOrders.forEach { ord ->
                                        val bmp = QrCodeGenerator.generateShippingLabelBitmap(
                                            orderId = ord.orderId,
                                            awbNumber = ord.awbNumber,
                                            customerName = ord.customerName,
                                            destination = ord.deliveryAddress,
                                            productTitle = ord.product.name,
                                            sku = ord.product.id.uppercase(),
                                            size = ord.selectedSize,
                                            amount = ord.totalAmount,
                                            paymentMethod = ord.paymentMethod,
                                            packerName = ord.packerName,
                                            packerId = ord.packerId
                                        )
                                        if (ImageSaver.saveBitmapToGallery(context, bmp, "Label_${ord.orderId}", showToast = false).success) {
                                            savedCount++
                                            onMarkLabelDownloaded(ord.orderId)
                                        }
                                    }
                                    Toast.makeText(
                                        context,
                                        "✓ Saved $savedCount order QR & Shipping Label(s) to Gallery (Pictures/GyanviDresses)!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                modifier = Modifier.testTag("save_all_order_labels_gallery_btn")
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save All Labels to Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        filteredTabOrders.forEach { order ->
                            SellerLifecycleOrderCard(
                                order = order,
                                selectedTab = selectedOrderTab,
                                selectedCourier = selectedCourier,
                                isBulkChecked = selectedReadyIds.contains(order.orderId),
                                onToggleBulkSelect = { id ->
                                    if (selectedReadyIds.contains(id)) selectedReadyIds.remove(id)
                                    else selectedReadyIds.add(id)
                                },
                                actions = sellerOrderActions,
                                onOpenModal = { ord, modalType ->
                                    activeModalOrderId = ord.orderId
                                    activeModalType = modalType
                                },
                                onQuickMoveTab = { targetTab ->
                                    selectedOrderTab = targetTab
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Policy & Dispatch Health Insights placed cleanly below active order cards
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { showPolicyModal = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("📣", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Upcoming Policy Update: Next Day Dispatch is becoming the new platform standard for all orders.",
                        fontSize = 13.sp,
                        color = Color(0xFF5D4037),
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Know more",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { showPolicyModal = true }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📈", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Get better visibility into your dispatch health",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Track dispatch health, catalog status and key order performance insights in one place.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showDispatchHealthModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("Check Dispatch health", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    // =========================================================================
    // MODAL 1: UPCOMING POLICY UPDATE (Exact Screenshot 11:10)
    // =========================================================================
    if (showPolicyModal) {
        Dialog(
            onDismissRequest = { showPolicyModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showPolicyModal = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth().clickable { /* absorb */ }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Header: Title + Close X
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Upcoming Policy Update", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                            IconButton(onClick = { showPolicyModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 8.dp))

                        // Faster dispatch policy badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE91E63))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Faster dispatch policy", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Heading
                        Text("Dispatch all orders by 'next day'", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Subtitle
                        Text(
                            text = "Starting 23rd Jun, sellers on Gyanvi Dresses will need to dispatch all orders by the next day. You'll be informed at least 1 week in advance before it applies to you.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3 Checkmarks
                        listOf(
                            "Faster Dispatch = Potentially more orders",
                            "Expected reduction in cancellations, returns & RTO",
                            "Builds customer trust"
                        ).forEach { checkText ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(checkText, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Read seller guidelines link
                        Text(
                            text = "Read seller guidelines",
                            color = Color(0xFF3F51B5),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "Opening Next-Day Dispatch Policy Guidelines...", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Got it button (Purple)
                        Button(
                            onClick = { showPolicyModal = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.width(110.dp).height(42.dp)
                        ) {
                            Text("Got it", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    // =========================================================================
    // MODAL 2: SELECT SEARCH ID (Exact Screenshot 11:14 #1)
    // =========================================================================
    if (showSearchIdSheet) {
        Dialog(
            onDismissRequest = { showSearchIdSheet = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showSearchIdSheet = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth().clickable { /* absorb */ }
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Select Search ID", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                            IconButton(onClick = { showSearchIdSheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 8.dp))

                        listOf("SKU ID", "Gyanvi Dresses ID", "VSKU ID", "Sub Order ID").forEach { idOption ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedSearchIdType = idOption
                                        showSearchIdSheet = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedSearchIdType == idOption,
                                    onClick = {
                                        selectedSearchIdType = idOption
                                        showSearchIdSheet = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3F51B5))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(idOption, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // =========================================================================
    // MODAL 3: LABEL FILTER (Exact Screenshot 11:14 #2)
    // =========================================================================
    if (showLabelFilterSheet) {
        Dialog(
            onDismissRequest = { showLabelFilterSheet = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showLabelFilterSheet = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth().clickable { /* absorb */ }
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Label", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                            IconButton(onClick = { showLabelFilterSheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 8.dp))

                        listOf("Yes", "No").forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedLabel = option }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedLabel == option,
                                    onClick = { selectedLabel = option },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3F51B5))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(option, fontSize = 14.sp, color = TextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Bottom bar: 0 Orders and Done button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${filteredTabOrders.size} Orders", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Button(
                                onClick = { showLabelFilterSheet = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0BEC5)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.width(110.dp)
                            ) {
                                Text("Done", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // MODAL 4: SLA STATUS FILTER (Exact Screenshot 11:14 #3)
    // =========================================================================
    if (showSlaFilterSheet) {
        Dialog(
            onDismissRequest = { showSlaFilterSheet = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showSlaFilterSheet = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth().clickable { /* absorb */ }
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SLA Status", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                            IconButton(onClick = { showSlaFilterSheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 8.dp))

                        slaOptions.forEach { sla ->
                            val isChecked = selectedSla.contains(sla)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) selectedSla.remove(sla) else selectedSla.add(sla)
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        if (it) selectedSla.add(sla) else selectedSla.remove(sla)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3F51B5))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(sla, fontSize = 14.sp, color = TextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${filteredTabOrders.size} Orders", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Button(
                                onClick = { showSlaFilterSheet = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0BEC5)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.width(110.dp)
                            ) {
                                Text("Done", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // MODAL 5: FULL 2-COLUMN FILTERS SHEET
    // =========================================================================
    if (showFullFilterSheet) {
        var selectedFilterCategory by remember { mutableStateOf("SLA Status") }
        Dialog(
            onDismissRequest = { showFullFilterSheet = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showFullFilterSheet = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth().height(420.dp).clickable { /* absorb */ }
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Filters", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            IconButton(onClick = { showFullFilterSheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE))

                        Row(modifier = Modifier.weight(1f)) {
                            // Left Column: Filter Categories
                            Column(
                                modifier = Modifier
                                    .width(140.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFFF5F5F5))
                            ) {
                                listOf("SLA Status", "Label", "Dispatch Date", "Order Date").forEach { cat ->
                                    val isSelected = selectedFilterCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (isSelected) CardWhite else Color.Transparent)
                                            .clickable { selectedFilterCategory = cat }
                                            .padding(horizontal = 14.dp, vertical = 14.dp)
                                    ) {
                                        Text(
                                            cat,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFF3F51B5) else TextSecondary
                                        )
                                    }
                                }
                            }

                            // Right Column: Options
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(14.dp)
                            ) {
                                when (selectedFilterCategory) {
                                    "SLA Status" -> {
                                        slaOptions.forEach { sla ->
                                            val isChecked = selectedSla.contains(sla)
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = {
                                                        if (it) selectedSla.add(sla) else selectedSla.remove(sla)
                                                    }
                                                )
                                                Text(sla, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                    "Label" -> {
                                        listOf("Yes", "No").forEach { opt ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = selectedLabel == opt,
                                                    onClick = { selectedLabel = opt }
                                                )
                                                Text(opt, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                    "Dispatch Date" -> {
                                        listOf("All", "Today", "Tomorrow", "Next 3 Days").forEach { dt ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = selectedDispatchDate == dt,
                                                    onClick = { selectedDispatchDate = dt }
                                                )
                                                Text(dt, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                    "Order Date" -> {
                                        listOf("Last 24 Hours", "Last 7 Days", "Last 30 Days").forEach { dt ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = selectedDispatchDate == dt,
                                                    onClick = { selectedDispatchDate = dt }
                                                )
                                                Text(dt, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE))

                        // Footer Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    selectedSla.clear()
                                    selectedLabel = null
                                    selectedDispatchDate = "All"
                                }
                            ) {
                                Text("Clear all", color = TextSecondary, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showFullFilterSheet = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Apply Filters", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // MODAL 6: DISPATCH HEALTH DIALOG (Screenshot 11:14)
    // =========================================================================
    if (showDispatchHealthModal) {
        AlertDialog(
            onDismissRequest = { showDispatchHealthModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📊", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dispatch Health Insights", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text("Your current delivery and dispatch health metrics:", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("On-Time Dispatch Rate", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                            Text("98.4% (Target: >95%)", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20))
                            Text("Next-Day Dispatch readiness: High", fontSize = 11.sp, color = Color(0xFF2E7D32))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Breached Orders: 0", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Avg Dispatch Time: 14 hrs", fontSize = 12.sp, color = TextSecondary)
                            Text("Fast dispatch boosts product visibility by 25%", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDispatchHealthModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
                ) {
                    Text("Got it")
                }
            }
        )
    }

    // =========================================================================
    // MODAL 7: LIFECYCLE & QR STUDIO DIALOGS
    // =========================================================================
    val activeModalOrder = activeModalOrderId?.let { id -> orders.find { it.orderId == id } }
    if (activeModalOrder != null && activeModalType != SellerOrderModalType.NONE) {
        val closeModal = {
            activeModalType = SellerOrderModalType.NONE
            activeModalOrderId = null
        }
        when (activeModalType) {
            SellerOrderModalType.VIEW_DETAILS_AND_HISTORY -> {
                OrderDetailsAndHistoryDialog(
                    order = activeModalOrder,
                    actions = sellerOrderActions,
                    onOpenContactCustomer = {
                        activeModalOrderId = activeModalOrder.orderId
                        activeModalType = SellerOrderModalType.CONTACT_CUSTOMER
                    },
                    onOpenEditOrHold = {
                        activeModalOrderId = activeModalOrder.orderId
                        activeModalType = SellerOrderModalType.HOLD_OR_EDIT
                    },
                    onOpenInvoice = {
                        activeModalOrderId = activeModalOrder.orderId
                        activeModalType = SellerOrderModalType.INVOICE_PREVIEW
                    },
                    onOpenQrLabel = {
                        activeModalOrderId = activeModalOrder.orderId
                        activeModalType = SellerOrderModalType.QR_AND_PRINT_LABEL
                    },
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.VERIFY_AND_BARCODE -> {
                VerifyProductAndBarcodeDialog(
                    order = activeModalOrder,
                    actions = sellerOrderActions,
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.INVOICE_PREVIEW -> {
                InvoicePreviewAndDownloadDialog(
                    order = activeModalOrder,
                    onInvoiceGenerated = { sellerOrderActions.onGenerateInvoice(activeModalOrder.orderId) },
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.QR_AND_PRINT_LABEL -> {
                QrCodeStudioDialog(
                    initialContent = "GYANVI|ORD=${activeModalOrder.orderId}|AWB=${activeModalOrder.awbNumber}|AMT=${activeModalOrder.totalAmount}|CUST=${activeModalOrder.customerName}",
                    headerTitle = "ORDER #${activeModalOrder.orderId}",
                    footerSubtitle = "AWB: ${activeModalOrder.awbNumber} • ₹${activeModalOrder.totalAmount}",
                    fileNamePrefix = "Order_${activeModalOrder.orderId}",
                    labelMetadata = ShippingLabelQrMetadata(
                        orderId = activeModalOrder.orderId,
                        awbNumber = activeModalOrder.awbNumber,
                        customerName = activeModalOrder.customerName,
                        destination = activeModalOrder.deliveryAddress,
                        productTitle = activeModalOrder.product.name,
                        sku = activeModalOrder.product.id.uppercase(),
                        size = activeModalOrder.selectedSize,
                        amount = activeModalOrder.totalAmount,
                        paymentMethod = activeModalOrder.paymentMethod,
                        packerName = activeModalOrder.packerName,
                        packerId = activeModalOrder.packerId
                    ),
                    onSavedToGalleryOrDownloaded = {
                        onMarkLabelDownloaded(activeModalOrder.orderId)
                    },
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.CONTACT_CUSTOMER -> {
                ContactPartyDialog(
                    order = activeModalOrder,
                    isCourierMode = false,
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.CONTACT_COURIER -> {
                ContactPartyDialog(
                    order = activeModalOrder,
                    isCourierMode = true,
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.HOLD_OR_EDIT -> {
                HoldAndEditOrderDialog(
                    order = activeModalOrder,
                    actions = sellerOrderActions,
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.CANCEL_ORDER -> {
                CancelOrRejectOrderDialog(
                    order = activeModalOrder,
                    isRejectFlow = false,
                    actions = sellerOrderActions,
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.REJECT_ORDER -> {
                CancelOrRejectOrderDialog(
                    order = activeModalOrder,
                    isRejectFlow = true,
                    actions = sellerOrderActions,
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.TRACK_AND_ISSUE -> {
                ShipmentTrackingAndIssueDialog(
                    order = activeModalOrder,
                    actions = sellerOrderActions,
                    onOpenContactCourier = {
                        activeModalOrderId = activeModalOrder.orderId
                        activeModalType = SellerOrderModalType.CONTACT_COURIER
                    },
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.HOLD_PDF_SINGLE -> {
                HoldOrderPdfDialog(
                    ordersToExport = listOf(activeModalOrder),
                    actions = sellerOrderActions,
                    isCombinedAllReport = false,
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.HOLD_PDF_ALL -> {
                HoldOrderPdfDialog(
                    ordersToExport = onHoldOrders,
                    actions = sellerOrderActions,
                    isCombinedAllReport = true,
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.ADD_HOLD_PRODUCT -> {
                if (products.isNotEmpty()) {
                    AddHoldProductDialog(
                        products = products,
                        actions = sellerOrderActions,
                        onDismiss = closeModal
                    )
                }
            }
            SellerOrderModalType.PENDING_PDF_SINGLE -> {
                HoldOrderPdfDialog(
                    ordersToExport = listOf(activeModalOrder),
                    actions = sellerOrderActions,
                    isCombinedAllReport = false,
                    isPendingMode = true,
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.SHIPPED_COURIER_PDF_SINGLE -> {
                HoldOrderPdfDialog(
                    ordersToExport = listOf(activeModalOrder),
                    actions = sellerOrderActions,
                    isCombinedAllReport = false,
                    isPendingMode = false,
                    isShippedCourierMode = true,
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.CANCELLED_PDF_SINGLE -> {
                HoldOrderPdfDialog(
                    ordersToExport = listOf(activeModalOrder),
                    actions = sellerOrderActions,
                    isCombinedAllReport = false,
                    isCancelledMode = true,
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.EDIT_CANCELLED_FULL -> {
                EditCancelledOrderDialog(
                    order = activeModalOrder,
                    actions = sellerOrderActions,
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.READY_TO_SHIP_PDF_SINGLE -> {
                HoldOrderPdfDialog(
                    ordersToExport = listOf(activeModalOrder),
                    actions = sellerOrderActions,
                    isCombinedAllReport = false,
                    isReadyToShipMode = true,
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.EDIT_READY_TO_SHIP -> {
                EditReadyToShipDetailsDialog(
                    order = activeModalOrder,
                    actions = sellerOrderActions,
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.GENERAL_PDF_SINGLE -> {
                HoldOrderPdfDialog(
                    ordersToExport = listOf(activeModalOrder),
                    actions = sellerOrderActions,
                    isCombinedAllReport = false,
                    isPendingMode = false,
                    customReportTitle = "GYANVI DRESSES • ORDER #${activeModalOrder.orderId} (${activeModalOrder.sellerStage.uppercase()})",
                    customFilePrefix = "Order_${activeModalOrder.orderId}",
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.NONE -> Unit
        }
    }

    if (showAddHoldProductDialog && products.isNotEmpty()) {
        AddHoldProductDialog(
            products = products,
            actions = sellerOrderActions,
            onDismiss = {
                showAddHoldProductDialog = false
                selectedOrderTab = 0
            }
        )
    }

    if (showAllHoldPdfDialog) {
        HoldOrderPdfDialog(
            ordersToExport = onHoldOrders,
            actions = sellerOrderActions,
            isCombinedAllReport = true,
            onDismiss = { showAllHoldPdfDialog = false }
        )
    }

    if (showAddPendingProductDialog && products.isNotEmpty()) {
        AddPendingProductDialog(
            products = products,
            actions = sellerOrderActions,
            onDismiss = {
                showAddPendingProductDialog = false
                selectedOrderTab = 1
            }
        )
    }

    if (showAllPendingPdfDialog) {
        HoldOrderPdfDialog(
            ordersToExport = pendingOrders,
            actions = sellerOrderActions,
            isCombinedAllReport = true,
            isPendingMode = true,
            onDismiss = { showAllPendingPdfDialog = false }
        )
    }

    if (showAddReadyToShipDialog && products.isNotEmpty()) {
        AddReadyToShipProductDialog(
            products = products,
            actions = sellerOrderActions,
            onDismiss = {
                showAddReadyToShipDialog = false
                selectedOrderTab = 2
            }
        )
    }

    if (showAllReadyToShipPdfDialog) {
        HoldOrderPdfDialog(
            ordersToExport = readyToShipOrders,
            actions = sellerOrderActions,
            isCombinedAllReport = true,
            isReadyToShipMode = true,
            onDismiss = { showAllReadyToShipPdfDialog = false }
        )
    }

    if (showAddShippedCourierDialog && products.isNotEmpty()) {
        AddShippedCourierOrderDialog(
            products = products,
            actions = sellerOrderActions,
            onDismiss = {
                showAddShippedCourierDialog = false
                selectedOrderTab = 3
            }
        )
    }

    if (showAllShippedCourierPdfDialog) {
        HoldOrderPdfDialog(
            ordersToExport = shippedOrders,
            actions = sellerOrderActions,
            isCombinedAllReport = true,
            isShippedCourierMode = true,
            onDismiss = { showAllShippedCourierPdfDialog = false }
        )
    }

    if (showAddCancelledRecordDialog && products.isNotEmpty()) {
        AddCancelledRecordDialog(
            products = products,
            actions = sellerOrderActions,
            onDismiss = {
                showAddCancelledRecordDialog = false
                selectedOrderTab = 4
            }
        )
    }

    if (showAllCancelledPdfDialog) {
        HoldOrderPdfDialog(
            ordersToExport = cancelledOrders,
            actions = sellerOrderActions,
            isCombinedAllReport = true,
            isCancelledMode = true,
            onDismiss = { showAllCancelledPdfDialog = false }
        )
    }

    if (showAllOrdersPdfDialog) {
        HoldOrderPdfDialog(
            ordersToExport = orders,
            actions = sellerOrderActions,
            isCombinedAllReport = true,
            customReportTitle = "GYANVI DRESSES • ALL ORDERS PDF (${orders.size} ITEMS)",
            customFilePrefix = "Gyanvi_All_Orders",
            onDismiss = { showAllOrdersPdfDialog = false }
        )
    }

    if (showCurrentTabAllPdfDialog) {
        val tabLabel = when (selectedOrderTab) {
            0 -> "ON HOLD"
            1 -> "PENDING"
            2 -> "READY TO SHIP"
            3 -> "SHIPPED"
            4 -> "CANCELLED"
            else -> "ALL ORDERS"
        }
        HoldOrderPdfDialog(
            ordersToExport = tabOrders,
            actions = sellerOrderActions,
            isCombinedAllReport = true,
            isPendingMode = selectedOrderTab == 1,
            isReadyToShipMode = selectedOrderTab == 2,
            isShippedCourierMode = selectedOrderTab == 3,
            isCancelledMode = selectedOrderTab == 4,
            customReportTitle = "GYANVI DRESSES • $tabLabel PDF (${tabOrders.size} ITEMS)",
            customFilePrefix = "Gyanvi_${tabLabel.replace(" ", "_")}_Orders",
            onDismiss = { showCurrentTabAllPdfDialog = false }
        )
    }

    if (showCustomQrStudio) {
        QrCodeStudioDialog(
            initialContent = "https://gyanvidresses.app/seller/amit-patwa",
            headerTitle = "GYANVI DRESSES QR",
            footerSubtitle = "Scan to Shop / Verify Packet",
            fileNamePrefix = "Gyanvi_Seller_QR",
            onDismiss = { showCustomQrStudio = false }
        )
    }

    if (showCameraScannerDialog) {
        QrAwbCameraScannerDialog(
            orders = orders,
            onApplyScannedCode = { scanned, matchedOrder ->
                searchQuery = matchedOrder?.orderId ?: scanned
                if (matchedOrder != null) {
                    selectedOrderTab = when {
                        matchedOrder.status == OrderStatus.CANCELLED && !matchedOrder.isRtoFlagged -> 4
                        matchedOrder.status == OrderStatus.SHIPPED || matchedOrder.status == OrderStatus.DELIVERED || matchedOrder.isRtoFlagged -> 3
                        matchedOrder.sellerStage.equals("Ready to Ship", ignoreCase = true) -> 2
                        matchedOrder.sellerStage.equals("On Hold", ignoreCase = true) -> 0
                        else -> 1
                    }
                    Toast.makeText(context, "✓ Found #${matchedOrder.orderId} (${matchedOrder.awbNumber})", Toast.LENGTH_SHORT).show()
                } else {
                    if (selectedOrderTab == 5) selectedOrderTab = 1
                    Toast.makeText(context, "Scanned filter applied: $scanned", Toast.LENGTH_SHORT).show()
                }
            },
            onVerifyScannedOrder = { matchedOrder, scanned ->
                sellerOrderActions.onScanBarcode(matchedOrder.orderId)
                searchQuery = matchedOrder.orderId
                Toast.makeText(context, "✓ Barcode verified for #${matchedOrder.orderId} ($scanned)", Toast.LENGTH_SHORT).show()
            },
            onMarkReadyToShip = { matchedOrder ->
                sellerOrderActions.onReadyToShip(matchedOrder.orderId)
                selectedOrderTab = 2
                searchQuery = matchedOrder.orderId
                Toast.makeText(context, "✓ #${matchedOrder.orderId} moved to Ready to Ship!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showCameraScannerDialog = false }
        )
    }

    // =========================================================================
    // MODAL 8: CREATE NEW / TEST SELLER ORDER DIALOG
    // =========================================================================
    if (showAddOrderDialog && products.isNotEmpty()) {
        val chosenProduct = products.getOrElse(selectedProductIndex) { products.first() }
        AlertDialog(
            onDismissRequest = { showAddOrderDialog = false },
            title = {
                Text("Create New Customer Order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = inputCustName,
                        onValueChange = { inputCustName = it },
                        label = { Text("Customer Name") },
                        placeholder = { Text("e.g. Sunita Devi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_order_customer_input")
                    )
                    OutlinedTextField(
                        value = inputCustPhone,
                        onValueChange = { inputCustPhone = it },
                        label = { Text("Customer Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputCityPincode,
                        onValueChange = { inputCityPincode = it },
                        label = { Text("Delivery Address & Pincode") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Select Dress / Product:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        products.take(3).forEachIndexed { idx, prod ->
                            FilterChip(
                                selected = selectedProductIndex == idx,
                                onClick = { selectedProductIndex = idx },
                                label = { Text(prod.name.take(14) + " (₹${prod.price})", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputSize,
                            onValueChange = { inputSize = it },
                            label = { Text("Size") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = inputColor,
                            onValueChange = { inputColor = it },
                            label = { Text("Colour") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = inputQuantity,
                            onValueChange = { inputQuantity = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Qty") },
                            singleLine = true,
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("UPI (PhonePe)", "Cash on Delivery (COD)").forEach { payOpt ->
                            FilterChip(
                                selected = inputPaymentMethod == payOpt,
                                onClick = { inputPaymentMethod = payOpt },
                                label = { Text(if (payOpt.contains("COD")) "COD" else "Prepaid UPI", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = (inputQuantity.toIntOrNull() ?: 1).coerceAtLeast(1)
                        sellerOrderActions.onAddManualOrder(
                            inputCustName,
                            inputCustPhone,
                            inputCityPincode,
                            chosenProduct,
                            inputSize,
                            inputColor,
                            qty,
                            inputPaymentMethod
                        )
                        selectedOrderTab = 1
                        showAddOrderDialog = false
                        inputCustName = ""
                        Toast.makeText(context, "✓ New Pending Order added with notification!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                    modifier = Modifier.testTag("confirm_create_order_btn")
                ) {
                    Text("Add Order")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddOrderDialog = false }) { Text("Cancel") }
            }
        )
    }
}
