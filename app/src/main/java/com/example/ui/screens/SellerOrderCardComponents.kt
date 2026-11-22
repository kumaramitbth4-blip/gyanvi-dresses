package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderItem
import com.example.model.OrderStatus
import com.example.model.SellerOrderActions
import com.example.ui.components.AppManagedImage
import com.example.ui.components.SELLER_COURIER_PARTNERS
import com.example.ui.theme.*
import com.example.util.QrCodeGenerator

enum class SellerOrderModalType {
    NONE,
    VIEW_DETAILS_AND_HISTORY,
    VERIFY_AND_BARCODE,
    INVOICE_PREVIEW,
    QR_AND_PRINT_LABEL,
    CONTACT_CUSTOMER,
    CONTACT_COURIER,
    HOLD_OR_EDIT,
    CANCEL_ORDER,
    REJECT_ORDER,
    TRACK_AND_ISSUE,
    HOLD_PDF_SINGLE,
    HOLD_PDF_ALL,
    ADD_HOLD_PRODUCT,
    PENDING_PDF_SINGLE,
    SHIPPED_COURIER_PDF_SINGLE,
    CANCELLED_PDF_SINGLE,
    EDIT_CANCELLED_FULL,
    READY_TO_SHIP_PDF_SINGLE,
    EDIT_READY_TO_SHIP,
    GENERAL_PDF_SINGLE
}

@Composable
fun SellerTabStageBanner(
    selectedTab: Int,
    ordersInTab: List<OrderItem>,
    selectedReadyIds: List<String>,
    selectedCourier: String,
    onCourierChange: (String) -> Unit,
    actions: SellerOrderActions,
    onOpenAddHoldProduct: () -> Unit = {},
    onOpenAllHoldPdf: () -> Unit = {},
    onOpenAddPendingProduct: () -> Unit = {},
    onOpenAllPendingPdf: () -> Unit = {},
    onOpenAddReadyProduct: () -> Unit = {},
    onOpenAllReadyPdf: () -> Unit = {},
    onOpenAddShippedCourier: () -> Unit = {},
    onOpenAllShippedCourierPdf: () -> Unit = {},
    onOpenAddCancelledRecord: () -> Unit = {},
    onOpenAllCancelledPdf: () -> Unit = {}
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (selectedTab) {
            1 -> {
                // Pending Tab Banner: Auto-Notification + Pending vs Ready to Ship comparison
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFFCA28), RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "🔔 Pending Auto Notification",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF5D4037)
                                )
                                Text(
                                    "नया ऑर्डर आते ही मोबाइल अलर्ट • Name, Phone, Address, Size, Colour & Payment चेक कर Confirm करें",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                            Switch(
                                checked = actions.autoPendingNotificationEnabled,
                                onCheckedChange = { actions.onToggleAutoNotification(it) },
                                modifier = Modifier.testTag("pending_auto_notif_switch")
                            )
                        }
                        Surface(color = Color.White, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Pending (पेंडिंग)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFE65100))
                                    Text("• नया ऑर्डर आया है\n• जांच व कन्फर्म बाकी", fontSize = 10.sp, color = TextSecondary, lineHeight = 13.sp)
                                }
                                VerticalDivider(modifier = Modifier.height(34.dp), color = Color(0xFFE0E0E0))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Ready to Ship", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF2E7D32))
                                    Text("• पैकिंग पूरी हो गई है\n• कूरियर को देना बाकी", fontSize = 10.sp, color = TextSecondary, lineHeight = 13.sp)
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onOpenAddPendingProduct,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.weight(1f).height(40.dp).testTag("pending_add_product_btn")
                            ) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF1A237E))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ New Type Product", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E), maxLines = 1)
                            }
                            Button(
                                onClick = onOpenAllPendingPdf,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.weight(1.05f).height(40.dp).testTag("pending_combined_pdf_btn")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pending PDF Show", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
            }
            2 -> {
                // Ready to Ship Banner: 7-Step Journey, Courier Selection, Bulk Pickup, Bulk Shipping, Add Ready & PDF Show
                val totalShippingCharges = ordersInTab.sumOf { it.shippingCharges }
                val scannedCount = ordersInTab.count { it.isBarcodeScanned }
                val pickupRequestedCount = ordersInTab.count { !it.pickupStatus.equals("Not Requested", ignoreCase = true) }
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "📦 Ready to Ship • Bulk Shipping, Pickup & PDF Show",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            "पैकेट भेजने के लिए तैयार है (${ordersInTab.size} Packets • Scan: $scannedCount • Pickup Req: $pickupRequestedCount • Charges: ₹$totalShippingCharges) — कूरियर द्वारा स्वीकार किए जाने पर ही Mark as Shipped दबाएँ।",
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32),
                            lineHeight = 15.sp
                        )
                        Surface(color = Color.White, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "ऑर्डर का सफर: Pending → Confirmed → Processing → Ready to Ship → Shipped → Out for Delivery → Delivered",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1B5E20),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SELLER_COURIER_PARTNERS.forEach { partner ->
                                FilterChip(
                                    selected = selectedCourier == partner,
                                    onClick = { onCourierChange(partner) },
                                    label = { Text(partner.substringBefore(" "), fontSize = 11.sp) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onOpenAddReadyProduct,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.weight(1f).height(40.dp).testTag("ready_add_product_btn")
                            ) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF1B5E20))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Add Ready Order", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), maxLines = 1)
                            }
                            Button(
                                onClick = onOpenAllReadyPdf,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.weight(1.05f).height(40.dp).testTag("ready_combined_pdf_btn")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ready to Ship PDF Show", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                        if (ordersInTab.isNotEmpty()) {
                            val idsToShip = if (selectedReadyIds.isNotEmpty()) selectedReadyIds else ordersInTab.map { it.orderId }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        actions.onBulkRequestPickup(idsToShip, selectedCourier)
                                        Toast.makeText(
                                            context,
                                            "🚐 Pickup requested for ${idsToShip.size} packet(s) via $selectedCourier",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.weight(0.95f).height(40.dp).testTag("bulk_pickup_ready_btn")
                                ) {
                                    Text("Bulk Pickup (${idsToShip.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0), maxLines = 1)
                                }
                                Button(
                                    onClick = {
                                        actions.onBulkDispatchOrders(idsToShip, selectedCourier)
                                        Toast.makeText(
                                            context,
                                            "🚚 Bulk Shipped ${idsToShip.size} order(s) via $selectedCourier!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.weight(1.25f).height(40.dp).testTag("bulk_ship_ready_btn")
                                ) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        if (selectedReadyIds.isNotEmpty()) "Bulk Ship (${selectedReadyIds.size})"
                                        else "Bulk Ship All (${ordersInTab.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                // Shipped Banner: Automatic Tracking System + 7-Step Journey + Add Courier Shipment & Shipped PDF Show
                val delayedCount = ordersInTab.count { it.isDeliveryDelayed }
                val rtoCount = ordersInTab.count { it.isRtoFlagged }
                val outForDeliveryCount = ordersInTab.count { it.sellerStage.equals("Out for Delivery", true) }
                val deliveredCount = ordersInTab.count { it.status == OrderStatus.DELIVERED || it.sellerStage.equals("Delivered", true) }
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF90CAF9), RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "🚚 Automatic Tracking & Courier Dispatch",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0D47A1)
                                )
                                Text(
                                    "Active: ${ordersInTab.size} • Out for Del: $outForDeliveryCount • Delayed: $delayedCount • RTO: $rtoCount • Delivered: $deliveredCount",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0)
                                )
                            }
                            Switch(
                                checked = actions.autoTrackingEnabled,
                                onCheckedChange = { actions.onToggleAutoTracking(it) },
                                modifier = Modifier.testTag("shipped_auto_tracking_switch")
                            )
                        }
                        Surface(color = Color.White, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    "ऑर्डर का पूरा सफर (Live Order Journey):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFF1A237E)
                                )
                                Text(
                                    "1. Pending → 2. Confirmed → 3. Processing → 4. Ready to Ship → 5. Shipped → 6. Out for Delivery → 7. Delivered",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2E7D32),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = onOpenAddShippedCourier,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp),
                                modifier = Modifier.weight(1f).height(40.dp).testTag("shipped_add_product_btn")
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF1565C0))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("+ Ship / AWB", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0), maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = {
                                    actions.onSyncLiveCourierTracking()
                                    Toast.makeText(context, "🔄 Live courier tracking synced to Customer & Admin!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp),
                                modifier = Modifier.weight(1f).height(40.dp).testTag("shipped_sync_tracking_btn")
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Sync Live", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), maxLines = 1)
                            }
                            Button(
                                onClick = onOpenAllShippedCourierPdf,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp),
                                modifier = Modifier.weight(1.1f).height(40.dp).testTag("shipped_combined_pdf_btn")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Shipped PDF Show", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
            }
            4 -> {
                // Cancelled Tab Banner: Cancellation Analytics, Auto Refund Tracking & Policy
                val totalRefund = ordersInTab.sumOf { it.refundAmount }
                val custCnt = ordersInTab.count { it.cancelledBy.equals("Customer", true) }
                val sellerCnt = ordersInTab.count { it.cancelledBy.equals("Seller", true) }
                val adminCnt = ordersInTab.count { it.cancelledBy.equals("Admin", true) }
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFEF9A9A), RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "📊 Cancellation Analytics & Auto Refund Tracking",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFB71C1C)
                                )
                                Text(
                                    "Cancelled: ${ordersInTab.size} • Refund Total: ₹$totalRefund • Customer: $custCnt | Seller: $sellerCnt | Admin: $adminCnt",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                            }
                            Switch(
                                checked = actions.autoRefundTrackingEnabled,
                                onCheckedChange = { actions.onToggleAutoRefundTracking(it) },
                                modifier = Modifier.testTag("cancelled_auto_refund_switch")
                            )
                        }
                        Text(
                            "Policy: कूरियर को पहले से सौंपे गए ऑर्डर सामान्य Cancel के बजाय अलग RTO प्रक्रिया से संभाले जाते हैं।",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onOpenAddCancelledRecord,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.weight(1f).height(40.dp).testTag("cancelled_add_record_btn")
                            ) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFB71C1C))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Add Cancelled Order", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C), maxLines = 1)
                            }
                            Button(
                                onClick = onOpenAllCancelledPdf,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.weight(1.05f).height(40.dp).testTag("cancelled_combined_pdf_btn")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancelled PDF Show", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
            }
            0 -> {
                // On Hold Banner: All Product Types Consolidated + Add Hold Product + Combined PDF
                val gatheredCategories = ordersInTab.map { it.product.category }.distinct()
                    .joinToString(", ").ifBlank { "Kurtis, Sarees, Lehengas, Western" }
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "⏸️ All Hold Products Ekatha (${ordersInTab.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFE65100)
                            )
                            Surface(color = Color.White, shape = RoundedCornerShape(12.dp)) {
                                Text(
                                    "Admin Approval ✓",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text(
                            "Categories Ekatha: $gatheredCategories",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037)
                        )
                        Text(
                            "Auto Hold Notification • Hold Reason Tracking • Hold Order Reminder • Hold Expiry Alert (48 hrs) • Seller Hold Permission",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 14.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onOpenAddHoldProduct,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.weight(1f).height(40.dp).testTag("on_hold_add_product_btn")
                            ) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFE65100))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Add Hold Product", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            }
                            Button(
                                onClick = onOpenAllHoldPdf,
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.weight(1.1f).height(40.dp).testTag("on_hold_combined_pdf_btn")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Hold PDF Show", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SellerLifecycleOrderCard(
    order: OrderItem,
    selectedTab: Int,
    selectedCourier: String,
    isBulkChecked: Boolean,
    onToggleBulkSelect: (String) -> Unit,
    actions: SellerOrderActions,
    onOpenModal: (OrderItem, SellerOrderModalType) -> Unit,
    onQuickMoveTab: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val badgeText = when {
        order.sellerStage.equals("On Hold", true) -> "On Hold ⏸️"
        order.status == OrderStatus.CANCELLED -> "Cancelled"
        order.isRtoFlagged -> "RTO Tracked ↩️"
        order.status == OrderStatus.DELIVERED -> "Delivered ✅"
        order.sellerStage.equals("Out for Delivery", true) -> "Out for Delivery 🛵"
        order.status == OrderStatus.SHIPPED -> if (order.isDeliveryDelayed) "Shipped • Delayed ⚠️" else "Shipped 🚚"
        order.sellerStage.equals("Ready to Ship", true) -> "Ready to Ship 📦"
        order.isOrderConfirmed || order.sellerStage.equals("Confirmed", true) -> "Confirmed ✓"
        else -> "Pending Review"
    }
    val badgeBg = when {
        order.sellerStage.equals("On Hold", true) -> Color(0xFFFFF3E0)
        order.status == OrderStatus.CANCELLED -> Color(0xFFFFEBEE)
        order.isRtoFlagged -> Color(0xFFF3E5F5)
        order.status == OrderStatus.DELIVERED -> Color(0xFFE8F5E9)
        order.status == OrderStatus.SHIPPED -> Color(0xFFE3F2FD)
        order.sellerStage.equals("Ready to Ship", true) -> Color(0xFFE8F5E9)
        order.isOrderConfirmed -> Color(0xFFE0F2F1)
        else -> Color(0xFFFFF8E1)
    }
    val badgeFg = when {
        order.sellerStage.equals("On Hold", true) -> Color(0xFFE65100)
        order.status == OrderStatus.CANCELLED -> Color(0xFFC62828)
        order.isRtoFlagged -> Color(0xFF6A1B9A)
        order.status == OrderStatus.DELIVERED -> Color(0xFF2E7D32)
        order.status == OrderStatus.SHIPPED -> Color(0xFF1565C0)
        order.sellerStage.equals("Ready to Ship", true) -> Color(0xFF2E7D32)
        order.isOrderConfirmed -> Color(0xFF00695C)
        else -> Color(0xFFF57C00)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .testTag("seller_order_card_${order.orderId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Checkbox (in Ready to Ship) + Order ID + Stage Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (selectedTab == 2) {
                        Checkbox(
                            checked = isBulkChecked,
                            onCheckedChange = { onToggleBulkSelect(order.orderId) },
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Column {
                        Text(
                            "Order #${order.orderId}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1A237E)
                        )
                        Text(
                            "AWB: ${order.awbNumber} • ${order.orderDate}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }
                }
                val isCustomerPurchase = order.orderHistoryLog.any {
                    it.contains("Customer Purchase", ignoreCase = true)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = badgeBg) {
                        Text(
                            badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeFg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (isCustomerPurchase) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F5E9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7))
                        ) {
                            Text(
                                "🛍️ Customer Purchase",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1B5E20),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 8.dp))

            // Product Row (Name, Size, Colour, Price, Payment Status + Custom Image Support)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AppManagedImage(
                    drawableRes = order.product.imageRes,
                    customUri = order.customProductImageUri ?: order.product.customImageUri,
                    contentDescription = order.product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(66.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${order.product.name} (${order.product.category})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Size: ${order.selectedSize} • Colour: ${order.selectedColor} • Qty: ${order.quantity}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹${order.totalAmount}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MeeshoMagenta)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${order.paymentMethod} (${order.paymentStatus})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (order.paymentStatus.contains("Pending", true)) Color(0xFFE65100) else Color(0xFF2E7D32)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer & Delivery Info Box
            Surface(color = Color(0xFFF8F9FA), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "${order.customerName} (${order.customerPhone})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                        Text(
                            if (order.isLabelDownloaded) "✓ Label" else "Label Pending",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (order.isLabelDownloaded) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                    Text(order.deliveryAddress, fontSize = 11.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        "City: ${order.city.ifBlank { "Bettiah" }} • State: ${order.state.ifBlank { "Bihar" }} • Pin: ${order.pinCode.ifBlank { "845438" }}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A237E)
                    )
                    Text(
                        "Packer: ${order.packerName} • ID: ${order.packerId}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )

                    // Stage-specific Metadata Line
                    when (selectedTab) {
                        0 -> {
                            Text(
                                "Hold Reason: ${order.holdReason.ifBlank { "Verification Pending" }} • ${order.holdDate}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                "Notes: ${order.holdNotes.ifBlank { "Confirm details with customer" }} • ${order.holdExpiryAlert}",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            val holdBarcodeCode = "GYN-${order.orderId.takeLast(6)}-${order.pinCode.ifBlank { "845438" }}"
                            val holdBarcodeBitmap = remember(order.orderId, order.pinCode) {
                                QrCodeGenerator.generateBarcodeBitmap(
                                    code = holdBarcodeCode,
                                    widthPx = 440,
                                    heightPx = 80,
                                    labelText = "*$holdBarcodeCode*"
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    bitmap = holdBarcodeBitmap.asImageBitmap(),
                                    contentDescription = "On Hold Barcode $holdBarcodeCode",
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("on_hold_card_barcode_${order.orderId}")
                                )
                            }
                        }
                        1 -> {
                            Text(
                                "Pending Verification: ${if (order.isOrderConfirmed) "Confirmed ✓" else "Name, Mobile, Address, Size, Colour & Payment Pending"}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (order.isOrderConfirmed) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                            val pendingBarcodeCode = "GYN-PND-${order.orderId.takeLast(6)}-${order.pinCode.ifBlank { "845438" }}"
                            val pendingBarcodeBitmap = remember(order.orderId, order.pinCode) {
                                QrCodeGenerator.generateBarcodeBitmap(
                                    code = pendingBarcodeCode,
                                    widthPx = 440,
                                    heightPx = 80,
                                    labelText = "*$pendingBarcodeCode*"
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    bitmap = pendingBarcodeBitmap.asImageBitmap(),
                                    contentDescription = "Pending Barcode $pendingBarcodeCode",
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("pending_card_barcode_${order.orderId}")
                                )
                            }
                        }
                        2 -> {
                            Text(
                                "Courier: ${order.courierPartner} • Pickup: ${order.pickupStatus} • Charges: ₹${order.shippingCharges} • ETA: ${order.expectedDeliveryDate}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1565C0)
                            )
                            Text(
                                "Packing Verify: Confirm=${if (order.isOrderConfirmed) "✓" else "⏳"} Quality=${if (order.isQualityVerified) "✓" else "⏳"} Pack=${if (order.isPacked) "✓" else "⏳"} Inv=${if (order.isInvoiceGenerated) "✓" else "⏳"} Scan=${if (order.isBarcodeScanned) "✓" else "⏳"}",
                                fontSize = 10.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                            order.orderHistoryLog.lastOrNull()?.let { latestHist ->
                                Text(
                                    "History: $latestHist",
                                    fontSize = 9.5.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            val readyBarcodeCode = order.awbNumber.replace("[^A-Za-z0-9-]".toRegex(), "")
                                .ifBlank { "GYN-RTS-${order.orderId.takeLast(6)}-${order.pinCode.ifBlank { "845438" }}" }
                            val readyBarcodeBitmap = remember(order.orderId, order.awbNumber, order.pinCode) {
                                QrCodeGenerator.generateBarcodeBitmap(
                                    code = readyBarcodeCode,
                                    widthPx = 440,
                                    heightPx = 80,
                                    labelText = "*$readyBarcodeCode*"
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    bitmap = readyBarcodeBitmap.asImageBitmap(),
                                    contentDescription = "Ready to Ship AWB Barcode $readyBarcodeCode",
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("ready_card_barcode_${order.orderId}")
                                )
                            }
                        }
                        3 -> {
                            val journeyTag = when {
                                order.status == OrderStatus.DELIVERED || order.sellerStage.equals("Delivered", true) -> "7/7 Delivered"
                                order.sellerStage.equals("Out for Delivery", true) -> "6/7 Out for Delivery"
                                else -> "5/7 Shipped"
                            }
                            Text(
                                "Journey ($journeyTag) • Status: ${order.statusText}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A237E)
                            )
                            Text(
                                "Courier: ${order.courierPartner} • AWB: ${order.awbNumber} • Expected: ${order.expectedDeliveryDate}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1565C0)
                            )
                            Text(
                                "Live Location: ${order.currentTrackingLocation}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (order.isDeliveryDelayed) Color(0xFFC62828) else Color(0xFF2E7D32)
                            )
                            if (order.isDeliveryDelayed) {
                                Text(
                                    "⚠️ DELAYED DELIVERY ALERT ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                            }
                            if (order.isRtoFlagged) {
                                Text(
                                    "↩️ RTO TRACKING: ${order.rtoReason.ifBlank { "Return to Origin Active" }}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6A1B9A)
                                )
                            }
                            order.orderHistoryLog.lastOrNull()?.let { latestHistory ->
                                Text(
                                    "History: $latestHistory",
                                    fontSize = 9.5.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            val awbBarcodeCode = order.awbNumber.replace("[^A-Za-z0-9-]".toRegex(), "").ifBlank { "GYN-SHP-${order.orderId.takeLast(6)}" }
                            val shippedBarcodeBitmap = remember(order.orderId, order.awbNumber) {
                                QrCodeGenerator.generateBarcodeBitmap(
                                    code = awbBarcodeCode,
                                    widthPx = 440,
                                    heightPx = 80,
                                    labelText = "*$awbBarcodeCode*"
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF90CAF9)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    bitmap = shippedBarcodeBitmap.asImageBitmap(),
                                    contentDescription = "Shipped AWB Barcode $awbBarcodeCode",
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("shipped_card_barcode_${order.orderId}")
                                )
                            }
                        }
                        4 -> {
                            Text(
                                "Age / Years: ${order.customerAgeYears.ifBlank { "26 Years" }} • AWB: ${order.awbNumber} (${order.courierPartner})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1565C0)
                            )
                            Text(
                                "Reason: ${order.cancelReason.ifBlank { order.statusText }}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Text(
                                "By: ${order.cancelledBy.ifBlank { "Customer" }} • ${order.cancellationDate.ifBlank { order.orderDate }} • Refund: ${order.refundStatus} (₹${order.refundAmount}) • Stock: ${if (order.isStockRestored) "Restored ✓" else "Pending"}",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            val cancelledBarcodeCode = order.awbNumber.replace("[^A-Za-z0-9-]".toRegex(), "").ifBlank { "GYN-CAN-${order.orderId.takeLast(6)}" }
                            val cancelledBarcodeBitmap = remember(order.orderId, order.awbNumber) {
                                QrCodeGenerator.generateBarcodeBitmap(
                                    code = cancelledBarcodeCode,
                                    widthPx = 440,
                                    heightPx = 80,
                                    labelText = "*$cancelledBarcodeCode*"
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    bitmap = cancelledBarcodeBitmap.asImageBitmap(),
                                    contentDescription = "Cancelled Barcode $cancelledBarcodeCode",
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("cancelled_card_barcode_${order.orderId}")
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab-Specific Action Buttons
            when (selectedTab) {
                // =========================================================================
                // 1. PENDING BUTTONS: View Order, Confirm Order, Reject Order, Contact Customer, Cancel Order (+ Hold / Pack)
                // =========================================================================
                1 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { onOpenModal(order, SellerOrderModalType.VIEW_DETAILS_AND_HISTORY) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.weight(1f).height(40.dp).testTag("pending_view_order_${order.orderId}")
                            ) {
                                Text("View Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            if (!order.isOrderConfirmed) {
                                Button(
                                    onClick = {
                                        actions.onConfirmOrder(order.orderId)
                                        Toast.makeText(context, "✓ Order #${order.orderId} Confirmed! अब पैकिंग व लेबल शुरू करें।", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.weight(1f).height(40.dp).testTag("pending_confirm_order_${order.orderId}")
                                ) {
                                    Text("Confirm Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        actions.onReadyToShip(order.orderId)
                                        Toast.makeText(context, "📦 Order #${order.orderId} moved to Ready to Ship!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.weight(1f).height(40.dp).testTag("pending_move_ready_${order.orderId}")
                                ) {
                                    Text("Pack -> Ready", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            OutlinedButton(
                                onClick = { onOpenModal(order, SellerOrderModalType.HOLD_OR_EDIT) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp),
                                modifier = Modifier.height(40.dp).testTag("pending_hold_btn_${order.orderId}")
                            ) {
                                Text("Hold", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { onOpenModal(order, SellerOrderModalType.CONTACT_CUSTOMER) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp),
                                modifier = Modifier.weight(1f).height(38.dp).testTag("pending_contact_customer_${order.orderId}")
                            ) {
                                Text("Contact Customer", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { onOpenModal(order, SellerOrderModalType.REJECT_ORDER) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp),
                                modifier = Modifier.weight(1f).height(38.dp).testTag("pending_reject_order_${order.orderId}")
                            ) {
                                Text("Reject Order", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                            }
                            OutlinedButton(
                                onClick = { onOpenModal(order, SellerOrderModalType.CANCEL_ORDER) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp),
                                modifier = Modifier.weight(1f).height(38.dp).testTag("pending_cancel_order_${order.orderId}")
                            ) {
                                Text("Cancel Order", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                            }
                        }

                        Button(
                            onClick = { onOpenModal(order, SellerOrderModalType.PENDING_PDF_SINGLE) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp).testTag("pending_pdf_btn_${order.orderId}")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF Show & Download (Barcode)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // =========================================================================
                // 2. READY TO SHIP BUTTONS: View Order, Verify Product, Generate Invoice,
                //    Generate Label, Scan Barcode, Generate AWB, Print Label, Request Pickup, Mark as Shipped
                // =========================================================================
                2 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ActionChipButton("View Order", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.VIEW_DETAILS_AND_HISTORY)
                            }
                            ActionChipButton("Verify Product", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.VERIFY_AND_BARCODE)
                            }
                            ActionChipButton("Generate Invoice", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.INVOICE_PREVIEW)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ActionChipButton("Generate Label", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.QR_AND_PRINT_LABEL)
                            }
                            ActionChipButton("Scan Barcode", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.VERIFY_AND_BARCODE)
                            }
                            ActionChipButton("Generate AWB", Modifier.weight(1f)) {
                                actions.onGenerateAwb(order.orderId, selectedCourier)
                                Toast.makeText(context, "✓ New AWB generated for $selectedCourier", Toast.LENGTH_SHORT).show()
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ActionChipButton("Print Label", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.QR_AND_PRINT_LABEL)
                            }
                            ActionChipButton("Request Pickup", Modifier.weight(1f)) {
                                actions.onRequestPickup(order.orderId, selectedCourier)
                                Toast.makeText(context, "🚐 Pickup requested with $selectedCourier!", Toast.LENGTH_SHORT).show()
                            }
                            Button(
                                onClick = {
                                    actions.onDispatchOrder(order.orderId, selectedCourier)
                                    Toast.makeText(context, "🚚 Courier accepted parcel! Order #${order.orderId} Marked as Shipped!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp),
                                modifier = Modifier.weight(1f).height(38.dp).testTag("mark_shipped_btn_${order.orderId}")
                            ) {
                                Text("Mark as Shipped", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ActionChipButton("Courier / Charges", Modifier.weight(1f), textColor = Color(0xFF2E7D32)) {
                                onOpenModal(order, SellerOrderModalType.EDIT_READY_TO_SHIP)
                            }
                            ActionChipButton("Shipping Tracking", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.TRACK_AND_ISSUE)
                            }
                            ActionChipButton("Shipment History", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.VIEW_DETAILS_AND_HISTORY)
                            }
                        }
                        Button(
                            onClick = { onOpenModal(order, SellerOrderModalType.READY_TO_SHIP_PDF_SINGLE) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp).testTag("ready_pdf_btn_${order.orderId}")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ready to Ship PDF Show & Download (AWB Barcode)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // =========================================================================
                // 3. SHIPPED BUTTONS: View Shipment, Track Order, AWB Number, Courier Details,
                //    Delivery Status, Expected Delivery, Contact Courier, Download Invoice, Report Issue
                // =========================================================================
                3 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ActionChipButton("View Shipment", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.VIEW_DETAILS_AND_HISTORY)
                            }
                            Button(
                                onClick = { onOpenModal(order, SellerOrderModalType.TRACK_AND_ISSUE) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp),
                                modifier = Modifier.weight(1f).height(38.dp).testTag("track_order_btn_${order.orderId}")
                            ) {
                                Text("Track Order", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            ActionChipButton("AWB Number", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.TRACK_AND_ISSUE)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ActionChipButton("Courier Details", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.TRACK_AND_ISSUE)
                            }
                            ActionChipButton("Delivery Status", Modifier.weight(1f)) {
                                if (order.status == OrderStatus.DELIVERED || order.isRtoFlagged) {
                                    onOpenModal(order, SellerOrderModalType.TRACK_AND_ISSUE)
                                    Toast.makeText(context, "Status: ${order.statusText}", Toast.LENGTH_SHORT).show()
                                } else {
                                    actions.onAdvanceTracking(order.orderId)
                                    Toast.makeText(context, "✓ Automatic Tracking Status Refreshed!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            ActionChipButton("Expected Delivery", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.TRACK_AND_ISSUE)
                                Toast.makeText(context, "Expected: ${order.expectedDeliveryDate} (${order.courierPartner})", Toast.LENGTH_SHORT).show()
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ActionChipButton("Contact Courier", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.CONTACT_COURIER)
                            }
                            ActionChipButton("Download Invoice", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.INVOICE_PREVIEW)
                            }
                            ActionChipButton("Report Issue", Modifier.weight(1f), textColor = Color(0xFFC62828)) {
                                onOpenModal(order, SellerOrderModalType.TRACK_AND_ISSUE)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    actions.onSendCustomerTrackingAlert(order.orderId)
                                    Toast.makeText(context, "📲 Customer SMS & Tracking Alert Sent!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp),
                                modifier = Modifier.weight(0.95f).height(38.dp).testTag("shipped_sms_alert_btn_${order.orderId}")
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Customer SMS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                            Button(
                                onClick = { onOpenModal(order, SellerOrderModalType.SHIPPED_COURIER_PDF_SINGLE) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.weight(1.35f).height(38.dp).testTag("shipped_pdf_btn_${order.orderId}")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Shipped PDF Show & Send", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // =========================================================================
                // 4. CANCELLED BUTTONS: View Details, Cancel Reason, Refund Initiate, Refund Completed,
                //    Restore Stock, Contact Customer, Download Invoice, Order History
                // =========================================================================
                4 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ActionChipButton("View Details", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.VIEW_DETAILS_AND_HISTORY)
                            }
                            ActionChipButton("Cancel Reason", Modifier.weight(1f)) {
                                Toast.makeText(context, "Reason: ${order.cancelReason.ifBlank { order.statusText }}", Toast.LENGTH_LONG).show()
                            }
                            ActionChipButton("Order History", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.VIEW_DETAILS_AND_HISTORY)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ActionChipButton("Refund Initiate", Modifier.weight(1f)) {
                                actions.onInitiateRefund(order.orderId)
                                Toast.makeText(context, "Refund initiated for #${order.orderId}", Toast.LENGTH_SHORT).show()
                            }
                            ActionChipButton("Refund Completed", Modifier.weight(1f), textColor = Color(0xFF2E7D32)) {
                                actions.onCompleteRefund(order.orderId)
                                Toast.makeText(context, "✓ Refund marked Completed!", Toast.LENGTH_SHORT).show()
                            }
                            ActionChipButton("Restore Stock", Modifier.weight(1f)) {
                                actions.onRestoreStock(order.orderId)
                                Toast.makeText(context, "✓ Stock restored (+${order.quantity})", Toast.LENGTH_SHORT).show()
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ActionChipButton("Contact Customer", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.CONTACT_CUSTOMER)
                            }
                            ActionChipButton("Download Invoice", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.INVOICE_PREVIEW)
                            }
                            ActionChipButton("AWB / Age / Edit", Modifier.weight(1f), textColor = Color(0xFF1565C0)) {
                                onOpenModal(order, SellerOrderModalType.EDIT_CANCELLED_FULL)
                            }
                        }
                        Button(
                            onClick = { onOpenModal(order, SellerOrderModalType.CANCELLED_PDF_SINGLE) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp).testTag("cancelled_pdf_btn_${order.orderId}")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cancelled PDF Show & Download (AWB Barcode)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // =========================================================================
                // 0. ON HOLD BUTTONS: View Order, Hold Reason, Edit Order, Contact Customer,
                //    Release Hold, Cancel Order, Order History
                // =========================================================================
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ActionChipButton("View Order", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.VIEW_DETAILS_AND_HISTORY)
                            }
                            ActionChipButton("Hold Reason", Modifier.weight(1f), textColor = Color(0xFFE65100)) {
                                onOpenModal(order, SellerOrderModalType.HOLD_OR_EDIT)
                            }
                            ActionChipButton("Edit Order", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.HOLD_OR_EDIT)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ActionChipButton("Contact Customer", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.CONTACT_CUSTOMER)
                            }
                            Button(
                                onClick = {
                                    actions.onReleaseHold(order.orderId)
                                    Toast.makeText(context, "▶ Hold Released! Order moved back to Pending.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp),
                                modifier = Modifier.weight(1f).height(38.dp).testTag("release_hold_btn_${order.orderId}")
                            ) {
                                Text("Release Hold", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            ActionChipButton("Cancel Order", Modifier.weight(1f), textColor = Color(0xFFC62828)) {
                                onOpenModal(order, SellerOrderModalType.CANCEL_ORDER)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ActionChipButton("Order History", Modifier.weight(1f)) {
                                onOpenModal(order, SellerOrderModalType.VIEW_DETAILS_AND_HISTORY)
                            }
                            ActionChipButton("Hold Reminder 🔔", Modifier.weight(1f)) {
                                actions.onSendHoldReminder(order.orderId)
                                Toast.makeText(context, "⏰ Hold Reminder Notification Sent!", Toast.LENGTH_SHORT).show()
                            }
                            Button(
                                onClick = { onOpenModal(order, SellerOrderModalType.HOLD_PDF_SINGLE) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp),
                                modifier = Modifier.weight(1f).height(38.dp).testTag("order_hold_pdf_btn_${order.orderId}")
                            ) {
                                Text("PDF Show", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            val showQuickMoveStrip = order.status == OrderStatus.ORDERED || (order.status == OrderStatus.SHIPPED && !order.isRtoFlagged && selectedTab != 4)
            if (showQuickMoveStrip) {
                Spacer(modifier = Modifier.height(8.dp))

                // Quick Stage Move Strip: Move any purchased/managed order across all 5 PDF stages in 1 tap
                Surface(
                    color = Color(0xFFF5F7FA),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Move PDF:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        if (selectedTab != 0 && order.status == OrderStatus.ORDERED) {
                            QuickStageChip("On Hold", Color(0xFFE65100)) {
                                actions.onHoldOrder(order.orderId, "Seller Hold Review", "Moved to On Hold PDF", true)
                                onQuickMoveTab(0)
                                Toast.makeText(context, "Moved #${order.orderId} -> On Hold PDF", Toast.LENGTH_SHORT).show()
                            }
                        }
                        if (selectedTab != 1 && order.sellerStage.equals("On Hold", true)) {
                            QuickStageChip("Pending", Color(0xFF1565C0)) {
                                actions.onReleaseHold(order.orderId)
                                onQuickMoveTab(1)
                                Toast.makeText(context, "Moved #${order.orderId} -> Pending PDF", Toast.LENGTH_SHORT).show()
                            }
                        }
                        if (selectedTab != 2 && order.status == OrderStatus.ORDERED) {
                            QuickStageChip("Ready to Ship", Color(0xFF2E7D32)) {
                                actions.onReadyToShip(order.orderId)
                                onQuickMoveTab(2)
                                Toast.makeText(context, "Moved #${order.orderId} -> Ready to Ship PDF", Toast.LENGTH_SHORT).show()
                            }
                        }
                        if (selectedTab != 3 && order.status == OrderStatus.ORDERED) {
                            QuickStageChip("Shipped", Color(0xFF0277BD)) {
                                actions.onDispatchOrder(order.orderId, selectedCourier)
                                onQuickMoveTab(3)
                                Toast.makeText(context, "Moved #${order.orderId} -> Shipped PDF", Toast.LENGTH_SHORT).show()
                            }
                        }
                        if (selectedTab != 4) {
                            QuickStageChip(if (order.status == OrderStatus.SHIPPED) "RTO / Cancel" else "Cancelled", Color(0xFFC62828)) {
                                actions.onCancelOrderDetailed(order.orderId, "Cancelled from Seller Quick Switch", "Seller", true)
                                if (order.status == OrderStatus.ORDERED) onQuickMoveTab(4)
                                Toast.makeText(context, "Updated #${order.orderId}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStageChip(
    label: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        color = accent.copy(alpha = 0.1f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = "→ $label",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = accent,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ActionChipButton(
    label: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color(0xFF1A237E),
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
        modifier = modifier.height(38.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
