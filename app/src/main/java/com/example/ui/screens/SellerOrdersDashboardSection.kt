package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderItem
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.SellerOrderActions
import com.example.ui.components.AppManagedImage
import com.example.ui.theme.*
import com.example.viewmodel.CloudSyncState

enum class DashboardOrderStatusFilter(
    val label: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    PENDING("Pending", Icons.Default.HourglassTop, Color(0xFF1565C0)),
    READY_TO_SHIP("Ready to Ship", Icons.Default.Inventory2, Color(0xFF2E7D32)),
    SHIPPED("Shipped", Icons.Default.LocalShipping, Color(0xFF0277BD)),
    CANCELLED("Cancelled", Icons.Default.Cancel, Color(0xFFC62828)),
    ON_HOLD("On Hold", Icons.Default.PauseCircle, Color(0xFFE65100)),
    ALL("All", Icons.Default.Dashboard, Color(0xFF6A1B9A))
}

fun filterOrdersByDashboardStatus(
    orders: List<OrderItem>,
    filter: DashboardOrderStatusFilter
): List<OrderItem> = when (filter) {
    DashboardOrderStatusFilter.ALL -> orders
    DashboardOrderStatusFilter.PENDING -> orders.filter {
        (it.status == OrderStatus.ORDERED || it.status == OrderStatus.CONFIRMED || it.status == OrderStatus.PROCESSING) &&
            !it.sellerStage.equals("Ready to Ship", ignoreCase = true) &&
            !it.sellerStage.equals("On Hold", ignoreCase = true)
    }
    DashboardOrderStatusFilter.READY_TO_SHIP -> orders.filter {
        it.status == OrderStatus.READY_TO_SHIP ||
            (it.status == OrderStatus.ORDERED && it.sellerStage.equals("Ready to Ship", ignoreCase = true))
    }
    DashboardOrderStatusFilter.SHIPPED -> orders.filter {
        it.status == OrderStatus.SHIPPED ||
            it.status == OrderStatus.OUT_FOR_DELIVERY ||
            it.status == OrderStatus.DELIVERED ||
            it.isRtoFlagged
    }
    DashboardOrderStatusFilter.CANCELLED -> orders.filter {
        it.status == OrderStatus.CANCELLED && !it.isRtoFlagged
    }
    DashboardOrderStatusFilter.ON_HOLD -> orders.filter {
        (it.status == OrderStatus.ORDERED || it.status == OrderStatus.CONFIRMED || it.status == OrderStatus.PROCESSING) &&
            it.sellerStage.equals("On Hold", ignoreCase = true)
    }
}

data class OrderDashboardMetrics(
    val totalOrders: Int,
    val grossRevenue: Int,
    val avgOrderValue: Int,
    val onHoldCount: Int,
    val pendingCount: Int,
    val readyToShipCount: Int,
    val shippedTransitCount: Int,
    val outForDeliveryCount: Int,
    val deliveredCount: Int,
    val cancelledCount: Int,
    val rtoCount: Int,
    val delayedCount: Int,
    val prepaidCount: Int,
    val prepaidAmount: Int,
    val codCount: Int,
    val codAmount: Int,
    val refundAmount: Int,
    val fulfillmentRatePct: Int,
    val courierBreakdown: List<CourierDashboardStat>,
    val topCities: List<CityDashboardStat>,
    val topProducts: List<ProductDashboardStat>
)

data class CourierDashboardStat(
    val courierName: String,
    val totalShipments: Int,
    val delayedCount: Int,
    val rtoCount: Int
)

data class CityDashboardStat(
    val cityName: String,
    val orderCount: Int,
    val totalAmount: Int
)

data class ProductDashboardStat(
    val productName: String,
    val category: String,
    val unitsSold: Int,
    val revenue: Int
)

fun computeOrderDashboardMetrics(
    orders: List<OrderItem>,
    products: List<Product>
): OrderDashboardMetrics {
    val onHold = filterOrdersByDashboardStatus(orders, DashboardOrderStatusFilter.ON_HOLD)
    val pending = filterOrdersByDashboardStatus(orders, DashboardOrderStatusFilter.PENDING)
    val readyToShip = filterOrdersByDashboardStatus(orders, DashboardOrderStatusFilter.READY_TO_SHIP)
    val shippedOutForDelivery = orders.filter {
        !it.isRtoFlagged && (it.status == OrderStatus.OUT_FOR_DELIVERY || (it.status == OrderStatus.SHIPPED && it.sellerStage.equals("Out for Delivery", ignoreCase = true)))
    }
    val shippedInTransit = orders.filter {
        it.status == OrderStatus.SHIPPED && !it.isRtoFlagged && !it.sellerStage.equals("Out for Delivery", ignoreCase = true)
    }
    val delivered = orders.filter {
        it.status == OrderStatus.DELIVERED || (it.status == OrderStatus.SHIPPED && it.sellerStage.equals("Delivered", ignoreCase = true))
    }
    val cancelled = filterOrdersByDashboardStatus(orders, DashboardOrderStatusFilter.CANCELLED)
    val rto = orders.filter { it.isRtoFlagged }
    val delayed = orders.filter { it.isDeliveryDelayed }

    val nonCancelled = orders.filter { it.status != OrderStatus.CANCELLED }
    val grossRevenue = nonCancelled.sumOf { it.totalAmount }
    val avgOrderValue = if (nonCancelled.isNotEmpty()) grossRevenue / nonCancelled.size else 0

    val codOrders = nonCancelled.filter { it.paymentMethod.contains("COD", ignoreCase = true) }
    val prepaidOrders = nonCancelled.filterNot { it.paymentMethod.contains("COD", ignoreCase = true) }
    val refundAmount = cancelled.sumOf { if (it.refundAmount > 0) it.refundAmount else it.totalAmount }

    val activeFulfilledOrShipped = shippedInTransit.size + shippedOutForDelivery.size + delivered.size + readyToShip.size
    val fulfillmentPct = if (orders.isNotEmpty()) ((activeFulfilledOrShipped * 100f) / orders.size).toInt().coerceIn(0, 100) else 0

    val courierStats = orders
        .filter {
            it.status == OrderStatus.READY_TO_SHIP ||
                it.status == OrderStatus.SHIPPED ||
                it.status == OrderStatus.OUT_FOR_DELIVERY ||
                it.status == OrderStatus.DELIVERED ||
                it.sellerStage.equals("Ready to Ship", true) ||
                it.isRtoFlagged
        }
        .groupBy { it.courierPartner.ifBlank { "Delhivery Express" } }
        .map { (courier, list) ->
            CourierDashboardStat(
                courierName = courier,
                totalShipments = list.size,
                delayedCount = list.count { it.isDeliveryDelayed },
                rtoCount = list.count { it.isRtoFlagged }
            )
        }
        .sortedByDescending { it.totalShipments }

    val topCities = nonCancelled
        .groupBy { it.city.ifBlank { "Bettiah" } }
        .map { (city, list) ->
            CityDashboardStat(
                cityName = city,
                orderCount = list.size,
                totalAmount = list.sumOf { it.totalAmount }
            )
        }
        .sortedByDescending { it.totalAmount }
        .take(5)

    val topProducts = nonCancelled
        .groupBy { it.product.id }
        .map { (_, list) ->
            val firstProd = list.first().product
            ProductDashboardStat(
                productName = firstProd.name,
                category = firstProd.category,
                unitsSold = list.sumOf { it.quantity },
                revenue = list.sumOf { it.totalAmount }
            )
        }
        .sortedByDescending { it.revenue }
        .take(4)

    return OrderDashboardMetrics(
        totalOrders = orders.size,
        grossRevenue = grossRevenue,
        avgOrderValue = avgOrderValue,
        onHoldCount = onHold.size,
        pendingCount = pending.size,
        readyToShipCount = readyToShip.size,
        shippedTransitCount = shippedInTransit.size,
        outForDeliveryCount = shippedOutForDelivery.size,
        deliveredCount = delivered.size,
        cancelledCount = cancelled.size,
        rtoCount = rto.size,
        delayedCount = delayed.size,
        prepaidCount = prepaidOrders.size,
        prepaidAmount = prepaidOrders.sumOf { it.totalAmount },
        codCount = codOrders.size,
        codAmount = codOrders.sumOf { it.totalAmount },
        refundAmount = refundAmount,
        fulfillmentRatePct = fulfillmentPct,
        courierBreakdown = courierStats,
        topCities = topCities,
        topProducts = topProducts
    )
}

@Composable
fun SellerOrdersDashboardSection(
    orders: List<OrderItem>,
    products: List<Product>,
    autoTrackingEnabled: Boolean,
    onJumpToOrderTab: (Int) -> Unit,
    onOpenAllOrdersPdf: () -> Unit,
    onOpenShippedPdf: () -> Unit,
    onSyncLiveTracking: () -> Unit,
    onOpenFullBusinessDashboard: () -> Unit,
    cloudSyncState: CloudSyncState = CloudSyncState.Idle,
    firebaseUserEmail: String = "",
    onGoogleCloudSignInClick: () -> Unit = {},
    onSyncAllOrdersClick: () -> Unit = {},
    onOpenCameraScanner: () -> Unit = {},
    onInspectOrder: (OrderItem) -> Unit = {},
    onOpenOrderPdf: (OrderItem) -> Unit = {},
    sellerOrderActions: SellerOrderActions = SellerOrderActions(),
    initialStatusFilter: DashboardOrderStatusFilter = DashboardOrderStatusFilter.PENDING,
    modifier: Modifier = Modifier
) {
    val metrics = remember(orders, products) {
        computeOrderDashboardMetrics(orders, products)
    }
    var selectedStatusFilter by remember(initialStatusFilter) { mutableStateOf(initialStatusFilter) }
    var dashboardSearchQuery by remember { mutableStateOf("") }

    val statusFilteredOrders = remember(orders, selectedStatusFilter, dashboardSearchQuery) {
        val byStatus = filterOrdersByDashboardStatus(orders, selectedStatusFilter)
        val q = dashboardSearchQuery.trim()
        if (q.isEmpty()) byStatus
        else byStatus.filter { ord ->
            ord.orderId.contains(q, ignoreCase = true) ||
                ord.awbNumber.contains(q, ignoreCase = true) ||
                ord.customerName.contains(q, ignoreCase = true) ||
                ord.product.name.contains(q, ignoreCase = true) ||
                ord.city.contains(q, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("seller_orders_dashboard_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Hero Executive Dashboard Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1A237E), Color(0xFF6A1B9A), MeeshoMagenta)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "GYANVI DRESSES • FIRESTORE DASHBOARD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFE082),
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                "Orders & Revenue Overview",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.9f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { onOpenCameraScanner() }
                                    .testTag("dashboard_open_scanner_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR/AWB", tint = Color.White, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Scan QR/AWB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.18f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { onOpenFullBusinessDashboard() }
                                    .testTag("dashboard_open_full_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Analytics, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Analytics", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HeroKpiBlock(
                            label = "Net Order Value",
                            value = "₹${metrics.grossRevenue}",
                            sub = "${metrics.totalOrders} Total Orders",
                            modifier = Modifier.weight(1f)
                        )
                        HeroKpiBlock(
                            label = "Avg Basket",
                            value = "₹${metrics.avgOrderValue}",
                            sub = "0% Commission",
                            modifier = Modifier.weight(1f)
                        )
                        HeroKpiBlock(
                            label = "Dispatch Rate",
                            value = "${metrics.fulfillmentRatePct}%",
                            sub = if (autoTrackingEnabled) "Auto-Sync ON" else "Manual Sync",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Interactive Order Pipeline Grid (Tap any card to open that stage tab)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Order Lifecycle Dashboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        "Tap stage to view orders",
                        fontSize = 11.sp,
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashboardStageCard(
                        title = "Pending",
                        count = metrics.pendingCount,
                        accent = Color(0xFF1565C0),
                        bgColor = Color(0xFFE3F2FD),
                        icon = Icons.Default.HourglassTop,
                        onClick = { onJumpToOrderTab(1) },
                        modifier = Modifier.weight(1f).testTag("dash_stage_pending")
                    )
                    DashboardStageCard(
                        title = "On Hold",
                        count = metrics.onHoldCount,
                        accent = Color(0xFFE65100),
                        bgColor = Color(0xFFFFF3E0),
                        icon = Icons.Default.PauseCircle,
                        onClick = { onJumpToOrderTab(0) },
                        modifier = Modifier.weight(1f).testTag("dash_stage_hold")
                    )
                    DashboardStageCard(
                        title = "Ready to Ship",
                        count = metrics.readyToShipCount,
                        accent = Color(0xFF2E7D32),
                        bgColor = Color(0xFFE8F5E9),
                        icon = Icons.Default.Inventory,
                        onClick = { onJumpToOrderTab(2) },
                        modifier = Modifier.weight(1f).testTag("dash_stage_ready")
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashboardStageCard(
                        title = "Shipped",
                        count = metrics.shippedTransitCount + metrics.outForDeliveryCount,
                        accent = Color(0xFF0277BD),
                        bgColor = Color(0xFFE1F5FE),
                        icon = Icons.Default.LocalShipping,
                        onClick = { onJumpToOrderTab(3) },
                        modifier = Modifier.weight(1f).testTag("dash_stage_shipped")
                    )
                    DashboardStageCard(
                        title = "Delivered",
                        count = metrics.deliveredCount,
                        accent = Color(0xFF1B5E20),
                        bgColor = Color(0xFFDCEDC8),
                        icon = Icons.Default.Verified,
                        onClick = { onJumpToOrderTab(3) },
                        modifier = Modifier.weight(1f).testTag("dash_stage_delivered")
                    )
                    DashboardStageCard(
                        title = "Cancelled",
                        count = metrics.cancelledCount,
                        accent = Color(0xFFC62828),
                        bgColor = Color(0xFFFFEBEE),
                        icon = Icons.Default.Cancel,
                        onClick = { onJumpToOrderTab(4) },
                        modifier = Modifier.weight(1f).testTag("dash_stage_cancelled")
                    )
                }

                if (metrics.delayedCount > 0 || metrics.rtoCount > 0 || metrics.outForDeliveryCount > 0) {
                    Surface(
                        color = Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Out for Delivery: ${metrics.outForDeliveryCount} • Delayed Alerts: ${metrics.delayedCount} • RTO: ${metrics.rtoCount}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "View →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MeeshoMagenta,
                                modifier = Modifier
                                    .clickable { onJumpToOrderTab(3) }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2B. Firestore Cloud Sync Status Banner
        FirestoreSyncStatusCard(
            cloudSyncState = cloudSyncState,
            firebaseUserEmail = firebaseUserEmail,
            totalOrdersCount = orders.size,
            onGoogleCloudSignInClick = onGoogleCloudSignInClick,
            onSyncAllOrdersClick = onSyncAllOrdersClick
        )

        // 2C. Firestore Filtered Orders List (Pending, Ready to Ship, Shipped, Cancelled, On Hold)
        FirestoreFilteredOrdersCard(
            orders = orders,
            filteredOrders = statusFilteredOrders,
            metrics = metrics,
            selectedStatusFilter = selectedStatusFilter,
            onSelectStatusFilter = { selectedStatusFilter = it },
            searchQuery = dashboardSearchQuery,
            onSearchQueryChange = { dashboardSearchQuery = it },
            cloudSyncState = cloudSyncState,
            onOpenCameraScanner = onOpenCameraScanner,
            onInspectOrder = onInspectOrder,
            onOpenOrderPdf = onOpenOrderPdf,
            sellerOrderActions = sellerOrderActions
        )

        // 3. Payment & Settlement Breakdown
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Payment & Refund Breakdown",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentMetricTile(
                        label = "Prepaid / UPI",
                        amount = "₹${metrics.prepaidAmount}",
                        subtitle = "${metrics.prepaidCount} orders",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                    PaymentMetricTile(
                        label = "COD Orders",
                        amount = "₹${metrics.codAmount}",
                        subtitle = "${metrics.codCount} orders",
                        color = Color(0xFF1565C0),
                        modifier = Modifier.weight(1f)
                    )
                    PaymentMetricTile(
                        label = "Refunds",
                        amount = "₹${metrics.refundAmount}",
                        subtitle = "${metrics.cancelledCount} cancelled",
                        color = Color(0xFFC62828),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Courier Partner Live Performance
        if (metrics.courierBreakdown.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Courier Partner Tracking",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        TextButton(
                            onClick = onSyncLiveTracking,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp).testTag("dash_sync_live_btn")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF1565C0))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync Live", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                        }
                    }

                    val maxCourierCount = (metrics.courierBreakdown.maxOfOrNull { it.totalShipments } ?: 1).coerceAtLeast(1)
                    metrics.courierBreakdown.forEach { stat ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    stat.courierName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    "${stat.totalShipments} parcels" +
                                        (if (stat.delayedCount > 0) " • ${stat.delayedCount} delayed" else "") +
                                        (if (stat.rtoCount > 0) " • ${stat.rtoCount} RTO" else ""),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (stat.delayedCount > 0 || stat.rtoCount > 0) Color(0xFFC62828) else Color(0xFF2E7D32)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { (stat.totalShipments.toFloat() / maxCourierCount.toFloat()).coerceIn(0.1f, 1f) },
                                color = if (stat.delayedCount > 0) Color(0xFFE65100) else Color(0xFF1565C0),
                                trackColor = Color(0xFFE3F2FD),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }

        // 5. Top Selling Dresses & Top Delivery Cities
        if (metrics.topProducts.isNotEmpty() || metrics.topCities.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (metrics.topProducts.isNotEmpty()) {
                        Text(
                            "Top Selling Dresses (${products.size} Catalog Items)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        metrics.topProducts.forEach { prod ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        prod.productName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${prod.category} • ${prod.unitsSold} unit(s)",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    "₹${prod.revenue}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MeeshoMagenta
                                )
                            }
                        }
                    }

                    if (metrics.topCities.isNotEmpty()) {
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Text(
                            "Top Delivery Cities",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            metrics.topCities.take(3).forEach { city ->
                                Surface(
                                    color = Color(0xFFF3E5F5),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            city.cityName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4A148C),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "${city.orderCount} • ₹${city.totalAmount}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MeeshoMagenta
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Quick PDF & Full Analytics Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onOpenAllOrdersPdf,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(46.dp).testTag("dash_all_pdf_btn")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("All Orders PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onOpenShippedPdf,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(46.dp).testTag("dash_shipped_pdf_btn")
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Shipped PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HeroKpiBlock(
    label: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = 0.14f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f), maxLines = 1)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1)
            Text(sub, fontSize = 9.5.sp, color = Color(0xFFFFE082), fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
private fun DashboardStageCard(
    title: String,
    count: Int,
    accent: Color,
    bgColor: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 68.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(16.dp))
                Text(
                    "$count",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )
            }
            Text(
                title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PaymentMetricTile(
    label: String,
    amount: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFF8F9FA),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, fontSize = 10.sp, color = TextSecondary, maxLines = 1)
            Text(amount, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color, maxLines = 1)
            Text(subtitle, fontSize = 9.5.sp, color = TextMuted, maxLines = 1)
        }
    }
}

@Composable
fun FirestoreSyncStatusCard(
    cloudSyncState: CloudSyncState,
    firebaseUserEmail: String,
    totalOrdersCount: Int,
    onGoogleCloudSignInClick: () -> Unit,
    onSyncAllOrdersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = when (cloudSyncState) {
            is CloudSyncState.Synced -> Color(0xFFE8F5E9)
            is CloudSyncState.Error -> Color(0xFFFFEBEE)
            is CloudSyncState.Syncing -> Color(0xFFE3F2FD)
            is CloudSyncState.Idle -> Color(0xFFF3E8FF)
        },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            when (cloudSyncState) {
                is CloudSyncState.Synced -> Color(0xFFA5D6A7)
                is CloudSyncState.Error -> Color(0xFFEF9A9A)
                is CloudSyncState.Syncing -> Color(0xFF90CAF9)
                is CloudSyncState.Idle -> Color(0xFFCE93D8)
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("firestore_sync_status_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (cloudSyncState) {
                                is CloudSyncState.Synced -> Color(0xFF2E7D32).copy(alpha = 0.14f)
                                is CloudSyncState.Error -> Color(0xFFC62828).copy(alpha = 0.14f)
                                is CloudSyncState.Syncing -> Color(0xFF1565C0).copy(alpha = 0.14f)
                                is CloudSyncState.Idle -> Color(0xFF6A1B9A).copy(alpha = 0.14f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (cloudSyncState is CloudSyncState.Syncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.2.dp,
                            color = Color(0xFF1565C0)
                        )
                    } else {
                        Icon(
                            imageVector = when (cloudSyncState) {
                                is CloudSyncState.Synced -> Icons.Default.CloudDone
                                is CloudSyncState.Error -> Icons.Default.CloudOff
                                is CloudSyncState.Syncing -> Icons.Default.CloudSync
                                is CloudSyncState.Idle -> Icons.Default.CloudQueue
                            },
                            contentDescription = "Firestore Cloud Sync Status",
                            tint = when (cloudSyncState) {
                                is CloudSyncState.Synced -> Color(0xFF2E7D32)
                                is CloudSyncState.Error -> Color(0xFFC62828)
                                is CloudSyncState.Syncing -> Color(0xFF1565C0)
                                is CloudSyncState.Idle -> Color(0xFF6A1B9A)
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = when (cloudSyncState) {
                            is CloudSyncState.Synced -> "Firestore Live Sync (${cloudSyncState.orderCount} orders)"
                            is CloudSyncState.Syncing -> "Syncing $totalOrdersCount orders with Firestore..."
                            is CloudSyncState.Error -> "Firestore Sync Issue"
                            is CloudSyncState.Idle -> "Firestore Cloud Order Sync"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = when (cloudSyncState) {
                            is CloudSyncState.Error -> cloudSyncState.message
                            else -> if (firebaseUserEmail.isNotBlank()) {
                                "Real-time stream active • $firebaseUserEmail"
                            } else {
                                "Sign in with Google for live multi-device Firestore sync"
                            }
                        },
                        fontSize = 11.sp,
                        color = if (cloudSyncState is CloudSyncState.Error) Color(0xFFB71C1C) else TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            if (firebaseUserEmail.isBlank()) {
                Button(
                    onClick = onGoogleCloudSignInClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("firestore_dashboard_google_signin_btn")
                ) {
                    Text("Sign in with Google", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            } else {
                OutlinedButton(
                    onClick = onSyncAllOrdersClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("firestore_dashboard_push_sync_btn")
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Push Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun FirestoreFilteredOrdersCard(
    orders: List<OrderItem>,
    filteredOrders: List<OrderItem>,
    metrics: OrderDashboardMetrics,
    selectedStatusFilter: DashboardOrderStatusFilter,
    onSelectStatusFilter: (DashboardOrderStatusFilter) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    cloudSyncState: CloudSyncState,
    onOpenCameraScanner: () -> Unit = {},
    onInspectOrder: (OrderItem) -> Unit = {},
    onOpenOrderPdf: (OrderItem) -> Unit = {},
    sellerOrderActions: SellerOrderActions = SellerOrderActions(),
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .testTag("dashboard_filtered_orders_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Firestore Order Queue (${filteredOrders.size})",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        if (cloudSyncState is CloudSyncState.Synced) {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "LIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF166534),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        "Status filters: Pending, Ready to Ship, Shipped, Cancelled & On Hold",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                OutlinedButton(
                    onClick = onOpenCameraScanner,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("dash_list_scan_btn")
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan AWB", modifier = Modifier.size(15.dp), tint = MeeshoMagenta)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan AWB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                }
            }

            // 5 Requested Status Filter Chips + All
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardOrderStatusFilter.entries.forEach { filterOption ->
                    val count = when (filterOption) {
                        DashboardOrderStatusFilter.ALL -> orders.size
                        DashboardOrderStatusFilter.PENDING -> metrics.pendingCount
                        DashboardOrderStatusFilter.READY_TO_SHIP -> metrics.readyToShipCount
                        DashboardOrderStatusFilter.SHIPPED -> metrics.shippedTransitCount + metrics.outForDeliveryCount + metrics.deliveredCount + metrics.rtoCount
                        DashboardOrderStatusFilter.CANCELLED -> metrics.cancelledCount
                        DashboardOrderStatusFilter.ON_HOLD -> metrics.onHoldCount
                    }
                    val isSelected = selectedStatusFilter == filterOption
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectStatusFilter(filterOption) },
                        leadingIcon = {
                            Icon(
                                imageVector = filterOption.icon,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = if (isSelected) Color.White else filterOption.accentColor
                            )
                        },
                        label = {
                            Text(
                                "${filterOption.label} ($count)",
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = filterOption.accentColor,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("dash_filter_chip_${filterOption.name}")
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search Order ID, AWB, Customer, City or Dress...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Search", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dash_orders_search_input")
            )

            AnimatedContent(
                targetState = Triple(selectedStatusFilter, filteredOrders.isEmpty(), filteredOrders.size),
                label = "FirestoreOrdersFilterTransition"
            ) { (activeFilter, isEmpty, _) ->
                if (isEmpty) {
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dash_orders_empty_state")
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = activeFilter.icon,
                                contentDescription = null,
                                tint = activeFilter.accentColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(34.dp)
                            )
                            Text(
                                "No orders in ${activeFilter.label}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                if (searchQuery.isNotBlank()) {
                                    "Try clearing your search filter or switching to another order status."
                                } else {
                                    "Orders moved to ${activeFilter.label} will appear here automatically from Firestore."
                                },
                                fontSize = 11.5.sp,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val isWide = maxWidth >= 600.dp
                        val displayed = filteredOrders.take(20)
                        if (isWide) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                displayed.chunked(2).forEach { pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        pair.forEach { ord ->
                                            DashboardFirestoreOrderCard(
                                                order = ord,
                                                onInspectOrder = { onInspectOrder(ord) },
                                                onOpenOrderPdf = { onOpenOrderPdf(ord) },
                                                sellerOrderActions = sellerOrderActions,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (pair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                displayed.forEach { ord ->
                                    DashboardFirestoreOrderCard(
                                        order = ord,
                                        onInspectOrder = { onInspectOrder(ord) },
                                        onOpenOrderPdf = { onOpenOrderPdf(ord) },
                                        sellerOrderActions = sellerOrderActions
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardFirestoreOrderCard(
    order: OrderItem,
    onInspectOrder: () -> Unit,
    onOpenOrderPdf: () -> Unit,
    sellerOrderActions: SellerOrderActions,
    modifier: Modifier = Modifier
) {
    val (statusLabel, stageColor) = when {
        order.status == OrderStatus.CANCELLED && !order.isRtoFlagged -> "Cancelled" to Color(0xFFC62828)
        order.sellerStage.equals("On Hold", true) -> "On Hold" to Color(0xFFE65100)
        order.sellerStage.equals("Ready to Ship", true) -> "Ready to Ship" to Color(0xFF2E7D32)
        order.isRtoFlagged -> "RTO Tracked" to Color(0xFFD84315)
        order.status == OrderStatus.DELIVERED -> "Delivered" to Color(0xFF1B5E20)
        order.status == OrderStatus.SHIPPED -> order.sellerStage.ifBlank { "Shipped" } to Color(0xFF0277BD)
        else -> "Pending" to Color(0xFF1565C0)
    }

    Surface(
        color = Color(0xFFFAFAFC),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onInspectOrder)
            .testTag("dash_order_row_${order.orderId}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppManagedImage(
                    drawableRes = order.product.imageRes,
                    customUri = order.customProductImageUri ?: order.product.resolvedImageUrl,
                    contentDescription = order.product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "#${order.orderId} • ${order.customerName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = stageColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                statusLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = stageColor,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        order.product.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        "Size ${order.selectedSize} • ${order.selectedColor} • Qty ${order.quantity} • ₹${order.totalAmount} (${order.paymentStatus})",
                        fontSize = 11.sp,
                        color = MeeshoMagenta,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "AWB: ${order.awbNumber.ifBlank { "Pending" }} • ${order.courierPartner.substringBefore(" ")} • ${order.city}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            // Inline workflow action row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (statusLabel == "Pending") {
                    FilledTonalButton(
                        onClick = { sellerOrderActions.onReadyToShip(order.orderId) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFDCFCE7),
                            contentColor = Color(0xFF166534)
                        ),
                        modifier = Modifier.height(34.dp).testTag("dash_action_ready_${order.orderId}")
                    ) {
                        Text("Ready to Ship", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            sellerOrderActions.onHoldOrder(
                                order.orderId,
                                "Verification Pending",
                                "Held from Seller Dashboard",
                                true
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("dash_action_hold_${order.orderId}")
                    ) {
                        Text("Put On Hold", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE65100))
                    }
                } else if (statusLabel == "On Hold") {
                    FilledTonalButton(
                        onClick = { sellerOrderActions.onReleaseHold(order.orderId) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFDBEAFE),
                            contentColor = Color(0xFF1E40AF)
                        ),
                        modifier = Modifier.height(34.dp).testTag("dash_action_release_${order.orderId}")
                    ) {
                        Text("Release Hold", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (statusLabel == "Ready to Ship") {
                    FilledTonalButton(
                        onClick = { sellerOrderActions.onDispatchOrder(order.orderId, order.courierPartner.ifBlank { "Delhivery Express" }) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFE0F2FE),
                            contentColor = Color(0xFF0369A1)
                        ),
                        modifier = Modifier.height(34.dp).testTag("dash_action_ship_${order.orderId}")
                    ) {
                        Text("Mark Shipped", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.weight(1f, fill = false))

                TextButton(
                    onClick = onInspectOrder,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Details", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                }
                TextButton(
                    onClick = onOpenOrderPdf,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                }
            }
        }
    }
}

