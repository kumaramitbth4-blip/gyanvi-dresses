package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.OrderItem
import com.example.model.OrderStatus
import com.example.ui.theme.*
import com.example.util.OrderMapHelper

@Composable
fun OrdersScreen(
    orders: List<OrderItem>,
    selectedStatus: OrderStatus,
    orderSearchQuery: String,
    onStatusSelected: (OrderStatus) -> Unit,
    onSearchChange: (String) -> Unit,
    onOpenFilterSheet: () -> Unit,
    onCompleteFeedback: (OrderItem) -> Unit,
    onTrackOrder: (OrderItem) -> Unit,
    onCancelOrder: (String, String) -> Unit = { _, _ -> },
    onReturnOrder: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var invoiceOrder by remember { mutableStateOf<OrderItem?>(null) }
    var cancellingOrder by remember { mutableStateOf<OrderItem?>(null) }
    var returningOrder by remember { mutableStateOf<OrderItem?>(null) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YellowBackground)
            .statusBarsPadding()
            .testTag("orders_screen")
    ) {
        // Top Header matching Meesho screenshots 8:36
        Surface(
            color = CardWhite,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "MY ORDERS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Search Orders Bar + Filters Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search Input
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF6F6F6),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            TextField(
                                value = orderSearchQuery,
                                onValueChange = onSearchChange,
                                placeholder = {
                                    Text(
                                        text = "Search orders",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF757575),
                                        fontSize = 13.sp
                                    )
                                },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color(0xFF111111),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF111111),
                                    unfocusedTextColor = Color(0xFF111111),
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    cursorColor = Color(0xFF111111),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (orderSearchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchChange("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Filters Button
                    OutlinedButton(
                        onClick = onOpenFilterSheet,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MeeshoMagenta),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MeeshoMagenta),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("orders_filter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint = MeeshoMagenta,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Filters",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MeeshoMagenta
                        )
                    }
                }
            }
        }

        // Horizontal Order Status Filter Chips (All, Ordered, Shipped, Delivered, Cancelled, Return)
        Surface(
            color = Color(0xFFFFFDE7),
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf(OrderStatus.ALL) + OrderStatus.workflowElevenStatuses) { status ->
                    val isSelected = selectedStatus == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { onStatusSelected(status) },
                        label = {
                            Text(
                                text = status.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MeeshoMagenta,
                            selectedLabelColor = Color.White,
                            containerColor = CardWhite,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) MeeshoMagenta else YellowBorder,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }
        }

        // Orders List
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Under ₹99 / Maha Deal Banner
            item {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8E6C9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = SaleRed,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "UNDER ₹99",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gyanvi Festive Clearance Live",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "VIEW ALL >",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MeeshoMagenta
                        )
                    }
                }
            }

            if (orders.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                tint = YellowPrimary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No orders found in this category",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Place an order from Home or Cart to view it here",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(orders, key = { it.orderId }) { order ->
                    OrderItemCard(
                        order = order,
                        onTrackClick = { onTrackOrder(order) },
                        onFeedbackClick = { onCompleteFeedback(order) },
                        onInvoiceClick = { invoiceOrder = order },
                        onCancelClick = { cancellingOrder = order },
                        onReturnClick = { returningOrder = order }
                    )
                }
            }

            // Meesho "All Izzz Well - Just keep shopping" celebratory footer (screenshot 8:36)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "All Izzz Well",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MeeshoMagentaDark
                            )
                            Text(
                                text = "Just keep shopping at Gyanvi Dresses",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(YellowPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = CardWhite,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    invoiceOrder?.let { ord ->
        GstInvoiceDialog(order = ord, onDismiss = { invoiceOrder = null })
    }

    cancellingOrder?.let { ord ->
        AlertDialog(
            onDismissRequest = { cancellingOrder = null },
            title = { Text("Cancel Order #${ord.orderId}?", fontWeight = FontWeight.Bold) },
            text = { Text("₹${ord.totalAmount} will be refunded instantly to your Gyanvi Wallet.", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelOrder(ord.orderId, "Cancelled by Buyer")
                        cancellingOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaleRed)
                ) {
                    Text("Confirm Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { cancellingOrder = null }) { Text("Keep Order") }
            }
        )
    }

    returningOrder?.let { ord ->
        AlertDialog(
            onDismissRequest = { returningOrder = null },
            title = { Text("Return / Exchange #${ord.orderId}", fontWeight = FontWeight.Bold) },
            text = { Text("7-Day Easy Return Pickup will be scheduled at Bettiah and ₹${ord.totalAmount} credited to Gyanvi Balance.", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        onReturnOrder(ord.orderId, "Size / Fit Issue")
                        returningOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Request Return")
                }
            },
            dismissButton = {
                TextButton(onClick = { returningOrder = null }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun OrderItemCard(
    order: OrderItem,
    onTrackClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onInvoiceClick: () -> Unit,
    onCancelClick: () -> Unit,
    onReturnClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTrackClick() }
            .testTag("order_item_${order.orderId}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Thumbnail + Status + Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Product Thumbnail
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Image(
                        painter = painterResource(id = order.product.imageRes),
                        contentDescription = order.product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Status info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = order.statusText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (order.status) {
                                OrderStatus.DELIVERED, OrderStatus.REFUNDED -> GreenRating
                                OrderStatus.CANCELLED -> SaleRed
                                OrderStatus.SHIPPED, OrderStatus.OUT_FOR_DELIVERY -> Color(0xFF0277BD)
                                OrderStatus.RETURN_REQUESTED, OrderStatus.RETURNED -> Color(0xFF7B1FA2)
                                else -> TextPrimary
                            }
                        )

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Track Order",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = order.deliveryDateRange,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Size: ${order.selectedSize}  •  Qty: ${order.quantity}  •  ₹${order.totalAmount}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Text(
                        text = order.product.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 1
                    )
                }
            }

            // Shipped & In-Transit Live Track Action (covering active 11-workflow stages)
            val isTrackableActive = order.status == OrderStatus.ORDERED ||
                order.status == OrderStatus.CONFIRMED ||
                order.status == OrderStatus.PROCESSING ||
                order.status == OrderStatus.READY_TO_SHIP ||
                order.status == OrderStatus.SHIPPED ||
                order.status == OrderStatus.OUT_FOR_DELIVERY ||
                order.isRtoFlagged
            if (isTrackableActive) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DividerColor)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (order.isDeliveryDelayed || order.isRtoFlagged) {
                        Surface(
                            color = if (order.isRtoFlagged) Color(0xFFF3E5F5) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (order.isRtoFlagged) {
                                    "↩️ RTO Tracking Active: ${order.rtoReason.ifBlank { "Return to Origin" }}"
                                } else {
                                    "⚠️ Delayed Delivery Alert: Courier transit delay reported"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (order.isRtoFlagged) Color(0xFF6A1B9A) else Color(0xFFC62828),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFE1F5FE),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = Color(0xFF0277BD),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = when {
                                        order.isRtoFlagged -> "RTO Active • ${order.courierPartner}"
                                        order.status == OrderStatus.SHIPPED || order.status == OrderStatus.OUT_FOR_DELIVERY ->
                                            "${order.sellerStage.ifBlank { order.status.label }} • ${order.courierPartner}"
                                        else -> "${order.sellerStage.ifBlank { order.status.label }} • ${order.courierPartner}"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (order.isRtoFlagged) Color(0xFF6A1B9A) else Color(0xFF0277BD)
                                )
                                Text(
                                    text = if (order.status == OrderStatus.SHIPPED || order.status == OrderStatus.OUT_FOR_DELIVERY || order.isRtoFlagged) {
                                        "AWB: ${order.awbNumber} • ${order.currentTrackingLocation.ifBlank { "${order.city} - ${order.pinCode}" }}"
                                    } else {
                                        "${order.city.ifBlank { "Bettiah" }} - ${order.pinCode.ifBlank { "845438" }}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                if (order.status == OrderStatus.SHIPPED || order.status == OrderStatus.OUT_FOR_DELIVERY) {
                                    Text(
                                        text = "Expected: ${order.expectedDeliveryDate}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = onTrackClick,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("track_btn_${order.orderId}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Track Order",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            val ctx = androidx.compose.ui.platform.LocalContext.current
                            OutlinedButton(
                                onClick = { OrderMapHelper.openGoogleMapsRoute(ctx, order) },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1A73E8)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp).testTag("map_btn_${order.orderId}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Google Map",
                                    tint = Color(0xFF1A73E8),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Google Map",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A73E8)
                                )
                            }
                        }
                    }
                }
            }

            // Feedback Rating Section for Delivered items (screenshot 8:36)
            if (order.status == OrderStatus.DELIVERED) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DividerColor)

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "We are glad you loved the product!",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 5 Green Stars
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(5) { starIndex ->
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (order.userRating >= starIndex + 1) GreenRating else Color(0xFFBDBDBD),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Complete your Feedback button
                        OutlinedButton(
                            onClick = onFeedbackClick,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MeeshoMagenta),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MeeshoMagenta),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("feedback_btn_${order.orderId}")
                        ) {
                            Text(
                                text = if (order.isFeedbackCompleted) "Edit Feedback" else "Complete your Feedback",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MeeshoMagenta
                            )
                        }
                    }

                    if (order.userReviewText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${order.userReviewText}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = GreenDiscount,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            // GST Invoice, Cancel & Return Quick Actions Row
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onInvoiceClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(34.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp), tint = MeeshoMagenta)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GST Invoice", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                }

                val canCancelOrder = order.status == OrderStatus.ORDERED ||
                    order.status == OrderStatus.CONFIRMED ||
                    order.status == OrderStatus.PROCESSING ||
                    order.status == OrderStatus.READY_TO_SHIP ||
                    order.status == OrderStatus.SHIPPED
                if (canCancelOrder) {
                    OutlinedButton(
                        onClick = onCancelClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp), tint = SaleRed)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel Order", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SaleRed)
                    }
                } else if (order.status == OrderStatus.DELIVERED) {
                    OutlinedButton(
                        onClick = onReturnClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF1976D2))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Return / Refund", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                    }
                }
            }
        }
    }
}
