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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.SampleData
import com.example.model.OrderItem
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.SellerOrderActions
import com.example.ui.components.ContactPartyDialog
import com.example.ui.components.HoldAndEditOrderDialog
import com.example.ui.components.HoldOrderPdfDialog
import com.example.ui.components.InvoicePreviewAndDownloadDialog
import com.example.ui.components.OrderDetailsAndHistoryDialog
import com.example.ui.components.QrAwbCameraScannerDialog
import com.example.ui.components.QrCodeStudioDialog
import com.example.ui.components.ShippingLabelQrMetadata
import com.example.ui.theme.*
import com.example.viewmodel.CloudSyncState

// =========================================================================
// MAIN FULL BUSINESS DASHBOARD SCREEN
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullSellerBusinessDashboardScreen(
    orders: List<OrderItem> = SampleData.initialOrders,
    products: List<Product> = SampleData.sampleProducts,
    cloudSyncState: CloudSyncState = CloudSyncState.Idle,
    firebaseUserEmail: String = "",
    sellerOrderActions: SellerOrderActions = SellerOrderActions(),
    initialSelectedTab: Int = 0,
    initialStatusFilter: DashboardOrderStatusFilter = DashboardOrderStatusFilter.PENDING,
    onGoogleCloudSignInClick: () -> Unit = {},
    onSyncAllOrdersClick: () -> Unit = {},
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val liveMetrics = remember(orders, products) { computeOrderDashboardMetrics(orders, products) }

    // Role Switcher: 0 = Seller Store View, 1 = Admin Master Control
    var activeRole by remember { mutableIntStateOf(0) }

    // Date Filter: "Today", "Last 7 Days", "Last 30 Days", "This Year", etc.
    var selectedTimeframe by remember { mutableStateOf("Today") }

    // Sub-Tabs / Analytics Views:
    // 0 = Overview, 1 = Sales & Revenue, 2 = Orders Pipeline,
    // 3 = Customers, 4 = Products & Stock, 5 = P&L Statement, 6 = AI Intelligence, 7 = Admin Master
    var selectedTab by remember(initialSelectedTab) { mutableIntStateOf(initialSelectedTab) }

    // Firestore Order List Filter & Search State
    var selectedStatusFilter by remember(initialStatusFilter) { mutableStateOf(initialStatusFilter) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredDashboardOrders = remember(orders, selectedStatusFilter, searchQuery) {
        val byStatus = filterOrdersByDashboardStatus(orders, selectedStatusFilter)
        val q = searchQuery.trim()
        if (q.isEmpty()) byStatus
        else byStatus.filter { ord ->
            ord.orderId.contains(q, ignoreCase = true) ||
                ord.awbNumber.contains(q, ignoreCase = true) ||
                ord.customerName.contains(q, ignoreCase = true) ||
                ord.product.name.contains(q, ignoreCase = true) ||
                ord.city.contains(q, ignoreCase = true)
        }
    }

    // Export & Order Inspection Dialog State
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf("PDF Document (.pdf)") }
    var selectedStateForDetail by remember { mutableStateOf<String?>(null) }
    var activeModalOrderId by remember { mutableStateOf<String?>(null) }
    var activeModalType by remember { mutableStateOf(SellerOrderModalType.NONE) }
    var showCameraScanner by remember { mutableStateOf(false) }

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
                        IconButton(onClick = onBack, modifier = Modifier.testTag("dashboard_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Business Dashboard", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                            Text("Sales, Orders & Operational Performance", fontSize = 11.sp, color = TextSecondary)
                        }

                        // Export Icon (Matching screenshot 7:07)
                        IconButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.testTag("export_report_btn")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Export Report", tint = MeeshoMagenta)
                        }

                        // Role Switcher Pill (Seller Hub vs Admin Mode)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (activeRole == 0) Color(0xFFF3E5F5) else Color(0xFFFFEBEE))
                                .clickable {
                                    activeRole = if (activeRole == 0) 1 else 0
                                    Toast.makeText(
                                        context,
                                        if (activeRole == 1) "Switched to Admin Master Control" else "Switched to Seller Store View",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (activeRole == 0) Icons.Default.Storefront else Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = if (activeRole == 0) MeeshoMagenta else Color(0xFFD32F2F),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (activeRole == 0) "Seller" else "Admin",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeRole == 0) MeeshoMagenta else Color(0xFFD32F2F)
                                )
                            }
                        }
                    }

                    // Dashboard Tabs
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
                        listOf(
                            "Overview",
                            "Sales & Revenue",
                            "Order Pipeline",
                            "Customers",
                            "Products & Stock",
                            "Profit & Loss (P&L)",
                            "AI Intelligence"
                        ).forEachIndexed { index, tabTitle ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(tabTitle, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) }
                            )
                        }
                        if (activeRole == 1) {
                            Tab(
                                selected = selectedTab == 7,
                                onClick = { selectedTab = 7 },
                                text = { Text("Admin Master", fontWeight = if (selectedTab == 7) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) }
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: OVERVIEW (Exact match to Screenshot 7:07)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        FirestoreSyncStatusCard(
                            cloudSyncState = cloudSyncState,
                            firebaseUserEmail = firebaseUserEmail,
                            totalOrdersCount = orders.size,
                            onGoogleCloudSignInClick = onGoogleCloudSignInClick,
                            onSyncAllOrdersClick = onSyncAllOrdersClick
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Date Filter Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Today", "Last 7 Days", "Last 30 Days", "This Year").forEach { pill ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedTimeframe == pill) MeeshoMagenta else CardWhite)
                                        .border(
                                            1.dp,
                                            if (selectedTimeframe == pill) MeeshoMagenta else Color(0xFFFFECB3),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedTimeframe = pill }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        pill,
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedTimeframe == pill) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTimeframe == pill) Color.White else TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // KPI Row 1: Live Gross Sales & Total Orders
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Gross Sales ($selectedTimeframe)", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("₹${liveMetrics.grossRevenue}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Prepaid: ₹${liveMetrics.prepaidAmount}", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Total Orders", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${liveMetrics.totalOrders} Orders", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("${liveMetrics.shippedTransitCount + liveMetrics.outForDeliveryCount} Active Shipped", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // KPI Row 2: Avg Order Value & RTO & Return Rate
                        val rtoPctText = if (liveMetrics.totalOrders > 0) {
                            "${((liveMetrics.rtoCount + liveMetrics.cancelledCount) * 100) / liveMetrics.totalOrders}%"
                        } else "0%"
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Avg Order Value", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("₹${liveMetrics.avgOrderValue}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("COD: ₹${liveMetrics.codAmount}", fontSize = 11.sp, color = TextSecondary)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("RTO & Cancel Rate", fontSize = 12.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(rtoPctText, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${liveMetrics.rtoCount} RTO • ${liveMetrics.cancelledCount} Cancelled", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Live Firestore Orders List with 5 Status Filters
                        FirestoreFilteredOrdersCard(
                            orders = orders,
                            filteredOrders = filteredDashboardOrders,
                            metrics = liveMetrics,
                            selectedStatusFilter = selectedStatusFilter,
                            onSelectStatusFilter = { selectedStatusFilter = it },
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            cloudSyncState = cloudSyncState,
                            onOpenCameraScanner = { showCameraScanner = true },
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

                        Spacer(modifier = Modifier.height(22.dp))

                        // Visitor to Order Funnel (Matching screenshot 7:07)
                        Text("Visitor to Order Funnel", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Step 1: Catalog Views
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("1", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBA68C8), modifier = Modifier.width(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Catalog Views", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text("54,200 unique customers", fontSize = 11.sp, color = TextSecondary)
                            }
                            Text("100%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Step 2: Added to Cart / Wishlist
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("2", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBA68C8), modifier = Modifier.width(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Added to Cart / Wishlist", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text("4,850 shoppers", fontSize = 11.sp, color = TextSecondary)
                            }
                            Text("8.9%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Step 3: Orders Placed
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("3", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784), modifier = Modifier.width(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Orders Placed", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text("384 successful checkouts", fontSize = 11.sp, color = TextSecondary)
                            }
                            Text("7.9% of carts", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Top Customer States (Matching screenshot 7:07)
                        Text("Top Customer States", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(14.dp))

                        val statesData = listOf(
                            Triple("Uttar Pradesh", 34, "Lucknow, Varanasi, Kanpur"),
                            Triple("Bihar", 28, "Bettiah, Patna, Muzaffarpur"),
                            Triple("Maharashtra", 18, "Mumbai, Pune, Nagpur"),
                            Triple("Rajasthan & Gujarat", 12, "Jaipur, Surat, Ahmedabad"),
                            Triple("Other States", 8, "Delhi NCR, Bengal, MP")
                        )

                        statesData.forEach { (state, share, cities) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { selectedStateForDetail = "$state ($cities)" },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(state, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))

                                // Visual bar matching screenshot 7:07
                                Box(
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color(0xFFF3E5F5))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = share / 100f * 2.5f)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(MeeshoMagenta)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(MeeshoMagenta)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("$share%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.width(36.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }

                1 -> {
                    // TAB 1: SALES & REVENUE DEEP DIVE
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("Sales & Revenue Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Track daily earnings, completed settlements and pending payouts", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                listOf(
                                    "Gross Sales" to "₹1,48,250",
                                    "Platform Commission (0% Zero Fee)" to "₹0.00",
                                    "Customer Delivery Charges Collected" to "₹15,360",
                                    "Input GST Deductions (5%)" to "-₹7,412",
                                    "Customer Returns & RTO Refunds" to "-₹6,250",
                                    "Completed Bank Payouts" to "₹1,06,200",
                                    "Pending Payout (Next Friday Transfer)" to "₹18,400"
                                ).forEachIndexed { idx, (label, value) ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(label, fontSize = 12.sp, color = if (idx == 0 || idx == 5) TextPrimary else TextSecondary, fontWeight = if (idx == 0 || idx == 5) FontWeight.Bold else FontWeight.Normal)
                                        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (value.startsWith("-")) Color(0xFFC62828) else if (idx == 5) Color(0xFF2E7D32) else TextPrimary)
                                    }
                                    if (idx < 6) HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerColor)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sales by Time Horizon
                        Text("Sales by Period", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Today's Sales", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹12,480", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("32 orders", fontSize = 10.sp, color = Color(0xFF2E7D32))
                                }
                            }
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("This Month", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹1,48,250", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                                    Text("384 orders", fontSize = 10.sp, color = MeeshoMagenta)
                                }
                            }
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("This Year", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹14.2 L", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("3,820 orders", fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                2 -> {
                    // TAB 2: ORDER PIPELINE DASHBOARD
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        FirestoreSyncStatusCard(
                            cloudSyncState = cloudSyncState,
                            firebaseUserEmail = firebaseUserEmail,
                            totalOrdersCount = orders.size,
                            onGoogleCloudSignInClick = onGoogleCloudSignInClick,
                            onSyncAllOrdersClick = onSyncAllOrdersClick
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Order Management Pipeline (${liveMetrics.totalOrders} Total)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Tap any stage below to filter the live Firestore order list", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))

                        data class PipelineStageRow(
                            val title: String,
                            val countText: String,
                            val color: Color,
                            val targetFilter: DashboardOrderStatusFilter
                        )
                        val stages = listOf(
                            PipelineStageRow("On Hold Verification", "${liveMetrics.onHoldCount} Orders", Color(0xFFE65100), DashboardOrderStatusFilter.ON_HOLD),
                            PipelineStageRow("Pending Confirmation", "${liveMetrics.pendingCount} Orders", Color(0xFF1976D2), DashboardOrderStatusFilter.PENDING),
                            PipelineStageRow("Ready to Ship (Packed & Barcoded)", "${liveMetrics.readyToShipCount} Orders", Color(0xFF2E7D32), DashboardOrderStatusFilter.READY_TO_SHIP),
                            PipelineStageRow("Shipped / In Transit", "${liveMetrics.shippedTransitCount} Orders", Color(0xFF0288D1), DashboardOrderStatusFilter.SHIPPED),
                            PipelineStageRow("Out for Delivery", "${liveMetrics.outForDeliveryCount} Orders", Color(0xFFF57C00), DashboardOrderStatusFilter.SHIPPED),
                            PipelineStageRow("Delivered Successfully", "${liveMetrics.deliveredCount} Orders", Color(0xFF1B5E20), DashboardOrderStatusFilter.SHIPPED),
                            PipelineStageRow("Cancelled Orders", "${liveMetrics.cancelledCount} Orders", Color(0xFFC62828), DashboardOrderStatusFilter.CANCELLED),
                            PipelineStageRow("Delayed Delivery Alerts", "${liveMetrics.delayedCount} Orders", Color(0xFFB71C1C), DashboardOrderStatusFilter.SHIPPED),
                            PipelineStageRow("Courier RTO Tracked", "${liveMetrics.rtoCount} Orders", Color(0xFFD32F2F), DashboardOrderStatusFilter.SHIPPED)
                        )

                        stages.forEach { row ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedStatusFilter = row.targetFilter }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(row.color))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(row.title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    }
                                    Text(row.countText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = row.color)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        FirestoreFilteredOrdersCard(
                            orders = orders,
                            filteredOrders = filteredDashboardOrders,
                            metrics = liveMetrics,
                            selectedStatusFilter = selectedStatusFilter,
                            onSelectStatusFilter = { selectedStatusFilter = it },
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            cloudSyncState = cloudSyncState,
                            onOpenCameraScanner = { showCameraScanner = true },
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

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                3 -> {
                    // TAB 3: CUSTOMERS & RETENTION
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("Customer Base & Sentiment", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Shopper demographics, repeat behavior and store ratings", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Total Customers", fontSize = 11.sp, color = TextSecondary)
                                    Text("1,840", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("420 new this month", fontSize = 10.sp, color = Color(0xFF2E7D32))
                                }
                            }
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Repeat Rate", fontSize = 11.sp, color = TextSecondary)
                                    Text("34.2%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                                    Text("High ethnic loyalty", fontSize = 10.sp, color = MeeshoMagenta)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Customer Satisfaction & Feedback", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("4.8 ★", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Based on 420 verified buyer ratings", fontSize = 12.sp, color = TextPrimary)
                                        Text("96% positive reviews on fabric quality and fitting", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                4 -> {
                    // TAB 4: PRODUCTS & STOCK
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("Catalog & Inventory Performance", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Stock alerts, top selling items and demand velocity", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        listOf(
                            Triple("Anarkali Embroidered Kurti Set", "142 Sold • Stock: 120 Units", "Rank #1"),
                            Triple("Banarasi Silk Zari Woven Saree", "118 Sold • Stock: 85 Units", "Rank #2"),
                            Triple("Chanderi Cotton Daily Wear Kurti", "64 Sold • Stock: 40 Units", "Rank #3"),
                            Triple("Georgette Embroidered Sharara Set", "42 Sold • Stock: 25 Units", "Trending"),
                            Triple("Pure Cotton Handblock Print Saree", "12 Sold • Stock: 55 Units", "Price Hike Suggested"),
                            Triple("Rayon Solid Casual A-Line Kurti", "6 Sold • Stock: 12 Units (LOW)", "Low Stock Alert")
                        ).forEach { (title, subtitle, rank) ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                        Text(subtitle, fontSize = 11.sp, color = if (subtitle.contains("LOW")) Color(0xFFC62828) else TextSecondary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (rank.contains("Alert")) Color(0xFFFFEBEE) else Color(0xFFE8F5E9))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            rank,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (rank.contains("Alert")) Color(0xFFC62828) else Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                5 -> {
                    // TAB 5: PROFIT & LOSS (P&L STATEMENT)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("Profit & Loss Financial Statement", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Accurate net income calculation after all real operating costs", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Monthly P&L (Current 30-Day Period)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.height(12.dp))

                                listOf(
                                    Triple("Total Selling Revenue", "+₹1,48,250", Color(0xFF2E7D32)),
                                    Triple("Product Purchase & Fabric Cost", "-₹68,400", Color(0xFFC62828)),
                                    Triple("Shipping Logistics Cost", "-₹15,360", Color(0xFFC62828)),
                                    Triple("Barcoded Packaging Bags", "-₹420", Color(0xFFC62828)),
                                    Triple("Platform Commission (0%)", "₹0.00", Color(0xFF2E7D32)),
                                    Triple("Payment Gateway Handling", "-₹1,480", Color(0xFFC62828)),
                                    Triple("GST Input-Output Adjustment", "-₹7,412", Color(0xFFC62828)),
                                    Triple("Return & RTO Damage Cost", "-₹3,120", Color(0xFFC62828)),
                                    Triple("NET SELLER PROFIT", "+₹46,280", MeeshoMagenta)
                                ).forEachIndexed { idx, (item, amount, color) ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(item, fontSize = if (idx == 8) 13.sp else 12.sp, fontWeight = if (idx == 8 || idx == 0) FontWeight.Bold else FontWeight.Normal, color = if (idx == 8) MeeshoMagenta else TextPrimary)
                                        Text(amount, fontSize = if (idx == 8) 16.sp else 13.sp, fontWeight = FontWeight.Bold, color = color)
                                    }
                                    if (idx == 7) HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerColor)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                6 -> {
                    // TAB 6: AI BUSINESS INTELLIGENCE
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("AI Business Intelligence & Predictions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Machine learning insights to multiply orders and eliminate dead stock", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        listOf(
                            Triple(
                                "AI Sales Surge Forecast (+42%)",
                                "Due to upcoming Chhath Puja & wedding season, saree demand will surge by 42%. Maintain at least 150 units of Banarasi Sarees.",
                                Icons.Default.TrendingUp
                            ),
                            Triple(
                                "Smart Restock Recommendation",
                                "Rayon Kurti inventory will run out in 3 days based on current checkout velocity. Reorder 100 units from Surat manufacturer.",
                                Icons.Default.Inventory
                            ),
                            Triple(
                                "RTO Reduction Tip (Size Chart)",
                                "Customer return analysis shows 62% of kurti returns are due to bust fitting. Adding a visual measuring guide will drop RTO to 2.8%.",
                                Icons.Default.Shield
                            ),
                            Triple(
                                "Prepaid Conversion Strategy",
                                "Offering ₹20 instant discount or 5% Smart Coins on UPI payments will increase prepaid share from 58% to 75%, cutting COD RTO risk.",
                                Icons.Default.AccountBalanceWallet
                            )
                        ).forEach { (title, desc, icon) ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFEDE7F6)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(desc, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                7 -> {
                    // TAB 7: ADMIN MASTER CONTROL (Visible when Admin Mode is selected)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("Admin Master Business Control", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Multi-seller marketplace oversight, commission & fraud security", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Marketplace Metrics", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.height(10.dp))

                                listOf(
                                    "Total Registered Sellers" to "14 Active Partners",
                                    "Pending Seller KYC Approvals" to "2 Applications",
                                    "Platform Total GMV (Month)" to "₹18.4 Lakhs",
                                    "Total Platform Commission" to "₹0 (0% Zero Commission Policy)",
                                    "Fraud & Anomaly Shield" to "Active (0 Flagged Violations)"
                                ).forEach { (k, v) ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(k, fontSize = 12.sp, color = TextSecondary)
                                        Text(v, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    // =========================================================================
    // EXPORT REPORT MODAL (PDF, Excel XLSX, CSV)
    // =========================================================================
    if (showExportDialog) {
        Dialog(onDismissRequest = { showExportDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Export Business Report", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text("Download sales, order pipeline & GST summaries", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(14.dp))

                    listOf(
                        "PDF Document (.pdf) - Executive Summary",
                        "Excel Spreadsheet (.xlsx) - Full Itemized Rows",
                        "CSV Raw Data (.csv) - Tally & Accounting Import"
                    ).forEach { fmt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { exportFormat = fmt }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = exportFormat == fmt, onClick = { exportFormat = fmt })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(fmt, fontSize = 12.sp, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showExportDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showExportDialog = false
                                Toast.makeText(context, "Downloading GYANVI_DRESSES_REPORT_$selectedTimeframe (${exportFormat.take(4)})...", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download")
                        }
                    }
                }
            }
        }
    }

    // State Details Dialog
    if (selectedStateForDetail != null) {
        Dialog(onDismissRequest = { selectedStateForDetail = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("State Performance Detail", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(selectedStateForDetail ?: "", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Highest demand categories: Saree (52%), Kurti (36%), Sharara (12%).\n• Average delivery turnaround: 2.4 days.\n• Zero RTO rate in Tier 1 cities.", fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { selectedStateForDetail = null },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Order Inspection, Hold/Edit, Invoice, QR Label & PDF Modals
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
                    onOpenContactCustomer = { activeModalType = SellerOrderModalType.CONTACT_CUSTOMER },
                    onOpenEditOrHold = { activeModalType = SellerOrderModalType.HOLD_OR_EDIT },
                    onOpenInvoice = { activeModalType = SellerOrderModalType.INVOICE_PREVIEW },
                    onOpenQrLabel = { activeModalType = SellerOrderModalType.QR_AND_PRINT_LABEL },
                    onDismiss = closeModal
                )
            }
            SellerOrderModalType.CONTACT_CUSTOMER -> {
                ContactPartyDialog(order = activeModalOrder, isCourierMode = false, onDismiss = closeModal)
            }
            SellerOrderModalType.HOLD_OR_EDIT -> {
                HoldAndEditOrderDialog(order = activeModalOrder, actions = sellerOrderActions, onDismiss = closeModal)
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
                        paymentMethod = activeModalOrder.paymentMethod
                    ),
                    onSavedToGalleryOrDownloaded = { sellerOrderActions.onMarkLabelDownloaded(activeModalOrder.orderId) },
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
            else -> Unit
        }
    }

    if (showCameraScanner) {
        QrAwbCameraScannerDialog(
            orders = orders,
            onApplyScannedCode = { scanned, matchedOrder ->
                searchQuery = matchedOrder?.orderId ?: scanned
                if (matchedOrder != null) {
                    selectedStatusFilter = when {
                        matchedOrder.status == OrderStatus.CANCELLED && !matchedOrder.isRtoFlagged -> DashboardOrderStatusFilter.CANCELLED
                        matchedOrder.status == OrderStatus.SHIPPED || matchedOrder.status == OrderStatus.DELIVERED || matchedOrder.isRtoFlagged -> DashboardOrderStatusFilter.SHIPPED
                        matchedOrder.sellerStage.equals("Ready to Ship", ignoreCase = true) -> DashboardOrderStatusFilter.READY_TO_SHIP
                        matchedOrder.sellerStage.equals("On Hold", ignoreCase = true) -> DashboardOrderStatusFilter.ON_HOLD
                        else -> DashboardOrderStatusFilter.PENDING
                    }
                    Toast.makeText(context, "✓ Found #${matchedOrder.orderId} (${matchedOrder.awbNumber})", Toast.LENGTH_SHORT).show()
                } else {
                    selectedStatusFilter = DashboardOrderStatusFilter.ALL
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
                selectedStatusFilter = DashboardOrderStatusFilter.READY_TO_SHIP
                searchQuery = matchedOrder.orderId
                Toast.makeText(context, "✓ #${matchedOrder.orderId} moved to Ready to Ship!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showCameraScanner = false }
        )
    }
}
