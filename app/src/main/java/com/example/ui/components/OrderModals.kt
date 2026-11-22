package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderItem
import com.example.model.OrderStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOrderModal(
    order: OrderItem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val journeySteps = listOf(
        "Pending", "Confirmed", "Processing", "Ready to Ship", "Shipped", "Out for Delivery", "Delivered"
    )
    val currentStepIndex = when {
        order.status == OrderStatus.DELIVERED || order.status == OrderStatus.RETURN_REQUESTED || order.status == OrderStatus.RETURNED || order.status == OrderStatus.REFUNDED || order.sellerStage.equals("Delivered", true) -> 6
        order.status == OrderStatus.OUT_FOR_DELIVERY || order.sellerStage.equals("Out for Delivery", true) -> 5
        order.status == OrderStatus.SHIPPED || order.sellerStage.equals("Shipped", true) -> 4
        order.status == OrderStatus.READY_TO_SHIP || order.sellerStage.equals("Ready to Ship", true) -> 3
        order.status == OrderStatus.PROCESSING || order.isPacked || order.isQualityVerified -> 2
        order.status == OrderStatus.CONFIRMED || order.isOrderConfirmed -> 1
        else -> 0
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = modifier.testTag("track_order_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE ORDER TRACKING",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Order ID: ${order.orderId} • ${order.statusText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            // Live Courier, AWB, Expected Delivery & Location Card
            Surface(
                color = Color(0xFFE3F2FD),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF90CAF9), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(16.dp))
                        Text(
                            "Courier: ${order.courierPartner} • AWB: ${order.awbNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF0D47A1)
                        )
                    }
                    Text(
                        "Expected Delivery: ${order.expectedDeliveryDate}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        "Live Location: ${order.currentTrackingLocation}",
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                }
            }

            OrderTrackingGoogleMapCard(order = order)

            if (order.isDeliveryDelayed || order.isRtoFlagged) {
                Surface(
                    color = if (order.isRtoFlagged) Color(0xFFF3E5F5) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (order.isRtoFlagged) {
                            "↩️ RTO Tracking Active: ${order.rtoReason.ifBlank { "Return to Origin initiated" }}"
                        } else {
                            "⚠️ Delayed Delivery Alert: Shipment transit delay reported by ${order.courierPartner}."
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.isRtoFlagged) Color(0xFF6A1B9A) else Color(0xFFC62828),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // 7-Step Order Journey Chips
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                journeySteps.forEachIndexed { idx, stepName ->
                    val done = idx <= currentStepIndex
                    Surface(
                        color = if (done) GreenRating else Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "${idx + 1}. $stepName",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (done) Color.White else TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Timeline Steps
            Column {
                order.trackingStages.forEachIndexed { index, stage ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (stage.isCompleted) GreenRating else Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (stage.isCompleted) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }

                            if (index < order.trackingStages.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(36.dp)
                                        .background(if (stage.isCompleted) GreenRating else Color(0xFFE0E0E0))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stage.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = stage.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Text(
                            text = stage.dateStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (order.orderHistoryLog.isNotEmpty()) {
                Surface(color = Color(0xFFF8F9FA), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Shipment History", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                        order.orderHistoryLog.takeLast(5).reversed().forEach { logEntry ->
                            Text("• $logEntry", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerModal(
    currentLocation: String,
    onDismiss: () -> Unit,
    onSelectLocation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var customPincode by remember { mutableStateOf("") }
    val popularLocations = listOf(
        "Bettiah - 845438",
        "Patna - 800001",
        "Motihari - 845401",
        "Muzaffarpur - 842001",
        "Delhi NCR - 110001",
        "Kolkata - 700001",
        "Varanasi - 221001",
        "Surat - 395003"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = modifier.testTag("location_picker_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SELECT DELIVERY LOCATION",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = customPincode,
                onValueChange = { customPincode = it },
                label = { Text("Enter Pincode or City") },
                trailingIcon = {
                    if (customPincode.isNotBlank()) {
                        IconButton(onClick = {
                            onSelectLocation(customPincode)
                            onDismiss()
                        }) {
                            Icon(Icons.Default.Done, contentDescription = "Apply", tint = MeeshoMagenta)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Saved & Popular Cities",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            popularLocations.forEach { loc ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectLocation(loc)
                            onDismiss()
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = loc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = if (loc == currentLocation) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun OrderSuccessDialog(
    onDismiss: () -> Unit,
    onViewOrders: () -> Unit,
    onOpenSellerOrders: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(GreenRating),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
        },
        title = {
            Text(
                text = "Order Placed Successfully! 🎉",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Thank you for shopping with Gyanvi Dresses! Your order has been synced to the Seller Panel (Pending, On Hold, Ready to Ship, Shipped, Cancelled) in PDF format.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                OutlinedButton(
                    onClick = onOpenSellerOrders,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("order_success_open_seller_btn")
                ) {
                    Text(
                        "📄 Seller Panel me PDF Order Dekhe →",
                        color = Color(0xFF1A237E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onViewOrders,
                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("View My Orders", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue Shopping", color = TextPrimary)
            }
        }
    )
}
