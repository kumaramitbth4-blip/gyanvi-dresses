package com.example.ui.components

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.OrderItem
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.SellerOrderActions
import com.example.ui.theme.*
import com.example.util.HoldOrderPdfGenerator
import com.example.util.ImageSaver
import com.example.util.QrCodeGenerator

val SELLER_COURIER_PARTNERS = listOf(
    "Delhivery Express",
    "Ekart Logistics",
    "Bluedart Air",
    "Xpressbees",
    "Shadowfax"
)

@Composable
fun OrderDetailsAndHistoryDialog(
    order: OrderItem,
    actions: SellerOrderActions,
    onOpenContactCustomer: () -> Unit,
    onOpenEditOrHold: () -> Unit,
    onOpenInvoice: () -> Unit,
    onOpenQrLabel: () -> Unit,
    onDismiss: () -> Unit
) {
    val journeySteps = listOf(
        "Pending",
        "Confirmed",
        "Processing",
        "Ready to Ship",
        "Shipped",
        "Out for Delivery",
        "Delivered"
    )
    val currentIdx = when {
        order.status == OrderStatus.DELIVERED -> 6
        order.sellerStage.equals("Out for Delivery", true) -> 5
        order.status == OrderStatus.SHIPPED -> 4
        order.sellerStage.equals("Ready to Ship", true) -> 3
        order.isPacked -> 2
        order.isOrderConfirmed || order.sellerStage.equals("Confirmed", true) -> 1
        else -> 0
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A237E))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Order #${order.orderId}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "Stage: ${order.sellerStage.ifBlank { order.status.label }} • ${order.orderDate}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 7-Stage Journey Stepper
                    Surface(
                        color = Color(0xFFF3E5F5),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Complete Order Journey (ऑर्डर का सफर)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MeeshoMagenta
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                journeySteps.forEachIndexed { idx, label ->
                                    val done = order.status != OrderStatus.CANCELLED && idx <= currentIdx
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (done) MeeshoMagenta else Color.White,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (done) MeeshoMagenta else Color(0xFFBDBDBD)
                                        )
                                    ) {
                                        Text(
                                            text = if (done) "✓ $label" else label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (done) Color.White else TextSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                    if (idx < journeySteps.lastIndex) {
                                        Text("→", fontSize = 12.sp, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }

                    // Customer & Delivery Info
                    Surface(
                        color = Color(0xFFF8F9FA),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Customer & Delivery Address", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Text("Customer Name: ${order.customerName}", fontSize = 12.sp, color = TextPrimary)
                            Text("Mobile Number: ${order.customerPhone}", fontSize = 12.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold)
                            Text("Address: ${order.deliveryAddress}", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                "City: ${order.city.ifBlank { "Bettiah" }}  •  State: ${order.state.ifBlank { "Bihar" }}  •  Pin Code: ${order.pinCode.ifBlank { "845438" }}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A237E)
                            )
                        }
                    }

                    // Product, Size, Color & Payment Status
                    Surface(
                        color = Color(0xFFF8F9FA),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppManagedImage(
                                drawableRes = order.product.imageRes,
                                customUri = order.customProductImageUri ?: order.product.customImageUri,
                                contentDescription = order.product.name,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Product & Payment Check", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text(order.product.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text(
                                    "Category: ${order.product.category}  •  Size: ${order.selectedSize}  •  Colour: ${order.selectedColor}  •  Qty: ${order.quantity}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                    Surface(
                        color = Color(0xFFF8F9FA),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Price: ₹${order.totalAmount} (Shipping: ₹${order.shippingCharges}) • SKU: ${order.product.id.uppercase()}",
                                fontSize = 12.sp,
                                color = MeeshoMagenta,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Payment: ${order.paymentMethod} • Status: ${order.paymentStatus}",
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Courier: ${order.courierPartner} • AWB: ${order.awbNumber} • Pickup: ${order.pickupStatus}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary
                            )
                        }
                    }

                    // Shipped & Live Courier Tracking Summary
                    if (order.status == OrderStatus.SHIPPED || order.status == OrderStatus.DELIVERED || order.isRtoFlagged) {
                        Surface(
                            color = if (order.isDeliveryDelayed || order.isRtoFlagged) Color(0xFFFFEBEE) else Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (order.isDeliveryDelayed || order.isRtoFlagged) Color(0xFFEF9A9A) else Color(0xFF90CAF9),
                                    RoundedCornerShape(10.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Live Shipment & Courier Tracking",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0D47A1)
                                )
                                Text("Delivery Status: ${order.statusText}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Expected Delivery: ${order.expectedDeliveryDate}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Text("Live Location: ${order.currentTrackingLocation}", fontSize = 12.sp, color = Color(0xFF1565C0))
                                Text(
                                    if (order.isDeliveryDelayed) "⚠️ Delayed Delivery Alert: Active" else "✓ On-Time Delivery Schedule",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (order.isDeliveryDelayed) Color(0xFFC62828) else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    OrderTrackingGoogleMapCard(order = order)

                    // Verification / Hold / Cancellation / RTO Metadata
                    if (order.holdReason.isNotBlank() || order.cancelReason.isNotBlank() || order.isRtoFlagged) {
                        Surface(
                            color = Color(0xFFFFF8E1),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(10.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (order.holdReason.isNotBlank()) {
                                    Text("Hold Reason: ${order.holdReason}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                    Text("Hold Date: ${order.holdDate} • ${order.holdExpiryAlert}", fontSize = 11.sp, color = TextSecondary)
                                    if (order.holdNotes.isNotBlank()) {
                                        Text("Notes: ${order.holdNotes}", fontSize = 11.sp, color = TextPrimary)
                                    }
                                }
                                if (order.cancelReason.isNotBlank()) {
                                    Text("Cancel Reason: ${order.cancelReason}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                    Text("Cancelled By: ${order.cancelledBy} on ${order.cancellationDate}", fontSize = 11.sp, color = TextSecondary)
                                    Text("Refund: ${order.refundStatus} (₹${order.refundAmount}) • Stock Restored: ${if (order.isStockRestored) "Yes" else "No"}", fontSize = 11.sp, color = TextPrimary)
                                }
                                if (order.isRtoFlagged) {
                                    Text("RTO Status: ${order.rtoReason}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
                                }
                            }
                        }
                    }

                    // Audit Trail / Order History
                    Text("Order & Shipment History", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    val logs = if (order.orderHistoryLog.isNotEmpty()) {
                        order.orderHistoryLog
                    } else {
                        listOf("${order.orderDate}: ${order.statusText}")
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        logs.forEach { entry ->
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 5.dp)
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(MeeshoMagenta)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(entry, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenContactCustomer()
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text("Contact", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenEditOrHold()
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text("Edit / Hold", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenInvoice()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                        modifier = Modifier.weight(1f).height(44.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text("Invoice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenQrLabel()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        modifier = Modifier.weight(1f).height(44.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text("QR Label", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun VerifyProductAndBarcodeDialog(
    order: OrderItem,
    actions: SellerOrderActions,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var qualityOk by remember(order.orderId) { mutableStateOf(order.isQualityVerified) }
    var sizeColorOk by remember(order.orderId) { mutableStateOf(order.isQualityVerified) }
    var packedOk by remember(order.orderId) { mutableStateOf(order.isPacked) }
    var barcodeText by remember(order.orderId) { mutableStateOf("GYN-PKT-${order.orderId.takeLast(5)}") }
    var selectedCourier by remember(order.orderId) { mutableStateOf(order.courierPartner) }
    var showCameraScanner by remember { mutableStateOf(false) }

    if (showCameraScanner) {
        QrAwbCameraScannerDialog(
            orders = listOf(order),
            initialAwbHint = order.awbNumber,
            onApplyScannedCode = { scanned, _ ->
                barcodeText = scanned
                actions.onScanBarcode(order.orderId)
                Toast.makeText(context, "✓ Camera scanned & verified: $scanned", Toast.LENGTH_SHORT).show()
            },
            onVerifyScannedOrder = { _, scanned ->
                barcodeText = scanned
                actions.onScanBarcode(order.orderId)
                Toast.makeText(context, "✓ Barcode $scanned verified via Camera!", Toast.LENGTH_SHORT).show()
            },
            onMarkReadyToShip = {
                actions.onReadyToShip(order.orderId)
                Toast.makeText(context, "✓ Order #${order.orderId} marked Ready to Ship!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showCameraScanner = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verify, Pack & Scan Barcode", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "${order.product.name} • Size: ${order.selectedSize} • Colour: ${order.selectedColor}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { qualityOk = !qualityOk }
                ) {
                    Checkbox(checked = qualityOk, onCheckedChange = { qualityOk = it })
                    Text("1. Product Quality & Stitching Verified", fontSize = 12.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { sizeColorOk = !sizeColorOk }
                ) {
                    Checkbox(checked = sizeColorOk, onCheckedChange = { sizeColorOk = it })
                    Text("2. Correct Size (${order.selectedSize}) & Colour (${order.selectedColor})", fontSize = 12.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { packedOk = !packedOk }
                ) {
                    Checkbox(checked = packedOk, onCheckedChange = { packedOk = it })
                    Text("3. Packed in Gyanvi Branded Packet", fontSize = 12.sp)
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text("Packet Barcode / QR Scanner", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                OutlinedTextField(
                    value = barcodeText,
                    onValueChange = { barcodeText = it },
                    label = { Text("Packet Barcode / AWB") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { showCameraScanner = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1f).height(42.dp).testTag("dialog_open_camera_scan_btn")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Camera Scan", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = {
                            actions.onScanBarcode(order.orderId)
                            Toast.makeText(context, "✓ Barcode $barcodeText scanned & verified!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (order.isBarcodeScanned) "✓ Verified" else "Verify Code",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                Text("Courier Partner & AWB", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SELLER_COURIER_PARTNERS.forEach { cp ->
                        FilterChip(
                            selected = selectedCourier == cp,
                            onClick = { selectedCourier = cp },
                            label = { Text(cp.substringBefore(" "), fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedButton(
                    onClick = {
                        actions.onGenerateAwb(order.orderId, selectedCourier)
                        Toast.makeText(context, "✓ AWB generated for $selectedCourier", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("Generate AWB (${order.awbNumber})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    actions.onVerifyProduct(order.orderId, qualityOk, sizeColorOk, packedOk)
                    Toast.makeText(context, "✓ Verification saved for #${order.orderId}", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                modifier = Modifier.testTag("confirm_verify_product_btn")
            ) {
                Text("Save Verification")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun InvoicePreviewAndDownloadDialog(
    order: OrderItem,
    onInvoiceGenerated: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val invoiceBitmap: Bitmap = remember(order.orderId, order.totalAmount, order.paymentStatus, order.refundStatus) {
        QrCodeGenerator.generateInvoiceBitmap(order)
    }
    var savedMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(order.orderId) {
        onInvoiceGenerated()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.86f)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tax Invoice • INV-${order.orderId.takeLast(6)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MeeshoMagenta)
                        Text("Order #${order.orderId} • ₹${order.totalAmount}", fontSize = 11.sp, color = TextSecondary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF5F5F5))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = invoiceBitmap.asImageBitmap(),
                        contentDescription = "Tax Invoice Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(6.dp)
                    )
                }

                savedMsg?.let { msg ->
                    Text(msg, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), modifier = Modifier.padding(bottom = 6.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val res = ImageSaver.saveBitmapToGallery(
                                context = context,
                                bitmap = invoiceBitmap,
                                fileNamePrefix = "Invoice_${order.orderId}",
                                showToast = true
                            )
                            savedMsg = res.message
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.weight(1f).height(46.dp).testTag("invoice_save_gallery_btn")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Save Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val res = ImageSaver.saveBitmapToDownloads(
                                context = context,
                                bitmap = invoiceBitmap,
                                fileNamePrefix = "Invoice_${order.orderId}",
                                showToast = true
                            )
                            savedMsg = res.message
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        modifier = Modifier.weight(1f).height(46.dp).testTag("invoice_download_btn")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Download", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ContactPartyDialog(
    order: OrderItem,
    isCourierMode: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val targetName = if (isCourierMode) order.courierPartner else order.customerName
    val targetPhone = if (isCourierMode) "+91 18001036622" else order.customerPhone
    var customMsg by remember(order.orderId, isCourierMode) {
        mutableStateOf(
            if (isCourierMode) {
                "Hi ${order.courierPartner}, please expedite pickup/delivery for AWB ${order.awbNumber} (Order #${order.orderId}, Bettiah Hub)."
            } else {
                "Namaste ${order.customerName} ji, Gyanvi Dresses se aapke Order #${order.orderId} (${order.product.name}, ₹${order.totalAmount}) ke baare me jankari: Address: ${order.deliveryAddress}."
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isCourierMode) Icons.Default.LocalShipping else Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = MeeshoMagenta
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isCourierMode) "Contact Courier Partner" else "Contact Customer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = Color(0xFFF3E5F5),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(targetName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MeeshoMagenta)
                        Text("Phone: $targetPhone", fontSize = 12.sp, color = TextPrimary)
                        if (!isCourierMode) {
                            Text("Address: ${order.deliveryAddress}", fontSize = 11.sp, color = TextSecondary)
                        } else {
                            Text("AWB: ${order.awbNumber} • Status: ${order.pickupStatus}", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
                OutlinedTextField(
                    value = customMsg,
                    onValueChange = { customMsg = it },
                    label = { Text("Message to Send") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { ImageSaver.dialPhoneNumber(context, targetPhone) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { ImageSaver.sendDirectWhatsAppMessage(context, targetPhone, customMsg) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { ImageSaver.sendDirectSms(context, targetPhone, customMsg) },
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("SMS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
fun HoldAndEditOrderDialog(
    order: OrderItem,
    actions: SellerOrderActions,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val holdPresets = listOf(
        "ग्राहक का पता अधूरा / गलत है",
        "ग्राहक से जानकारी की पुष्टि करनी है",
        "प्रोडक्ट का Size / Colour उपलब्ध नहीं है",
        "पेमेंट की पुष्टि नहीं हुई है",
        "ग्राहक ने कुछ समय रुकने को कहा है",
        "प्रोडक्ट की क्वालिटी चेक करनी है",
        "एडमिन द्वारा ऑर्डर रोका गया है"
    )
    var selectedReason by remember(order.orderId) {
        mutableStateOf(order.holdReason.ifBlank { holdPresets.first() })
    }
    var holdNotes by remember(order.orderId) { mutableStateOf(order.holdNotes) }
    var adminApproved by remember(order.orderId) { mutableStateOf(order.isAdminApprovedHold) }

    var editName by remember(order.orderId) { mutableStateOf(order.customerName) }
    var editPhone by remember(order.orderId) { mutableStateOf(order.customerPhone) }
    var editAddress by remember(order.orderId) { mutableStateOf(order.deliveryAddress) }
    var editCity by remember(order.orderId) { mutableStateOf(order.city) }
    var editState by remember(order.orderId) { mutableStateOf(order.state) }
    var editPin by remember(order.orderId) { mutableStateOf(order.pinCode) }
    var editSize by remember(order.orderId) { mutableStateOf(order.selectedSize) }
    var editColor by remember(order.orderId) { mutableStateOf(order.selectedColor) }
    var editCustomImageUri by remember(order.orderId) {
        mutableStateOf(order.customProductImageUri ?: order.product.customImageUri)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            editCustomImageUri = uri.toString()
            Toast.makeText(context, "✓ Product image attached for Hold Order & PDF!", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit / Hold Order #${order.orderId}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Product Image Upload (PDF & Hold Card)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MeeshoMagenta)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8F9FA))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppManagedImage(
                        drawableRes = order.product.imageRes,
                        customUri = editCustomImageUri,
                        contentDescription = order.product.name,
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            order.product.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            if (!editCustomImageUri.isNullOrBlank()) "✓ Custom Photo Selected" else "Catalog Default Photo",
                            fontSize = 10.sp,
                            color = if (!editCustomImageUri.isNullOrBlank()) Color(0xFF2E7D32) else TextSecondary
                        )
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp).testTag("edit_hold_upload_image_btn")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload Product Image", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text("Customer, Address & Variant Details", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Customer Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Mobile Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editAddress, onValueChange = { editAddress = it }, label = { Text("Address (Gali / Road / Landmark)") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editCity, onValueChange = { editCity = it }, label = { Text("City") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = editState, onValueChange = { editState = it }, label = { Text("State") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(
                    value = editPin,
                    onValueChange = { editPin = it.filter { ch -> ch.isDigit() }.take(6) },
                    label = { Text("Pin Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editSize, onValueChange = { editSize = it }, label = { Text("Size Name / No.") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = editColor, onValueChange = { editColor = it }, label = { Text("Colour Name") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedButton(
                    onClick = {
                        actions.onEditHoldFullOrderDetails(
                            order.orderId, editName, editPhone, editAddress, editCity, editState, editPin, editSize, editColor, editCustomImageUri
                        )
                        Toast.makeText(context, "✓ Hold Order details & photo updated!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp).testTag("save_hold_full_details_btn")
                ) {
                    Text("Save Full Order & Photo Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text("Hold Reason & Expiry Alert (${order.holdExpiryAlert})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFE65100))
                holdPresets.forEach { preset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = preset }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedReason == preset, onClick = { selectedReason = preset })
                        Text(preset, fontSize = 11.sp)
                    }
                }
                OutlinedTextField(
                    value = holdNotes,
                    onValueChange = { holdNotes = it },
                    label = { Text("Hold Notes (जरूरी जानकारी)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = adminApproved, onCheckedChange = { adminApproved = it })
                    Text("Admin Approval & Seller Hold Permission Active", fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (order.sellerStage.equals("On Hold", true)) {
                    Button(
                        onClick = {
                            actions.onEditHoldFullOrderDetails(
                                order.orderId, editName, editPhone, editAddress, editCity, editState, editPin, editSize, editColor, editCustomImageUri
                            )
                            actions.onReleaseHold(order.orderId)
                            Toast.makeText(context, "▶ Hold Released! Ready for Confirm & Pack.", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.testTag("dialog_release_hold_btn")
                    ) {
                        Text("Release Hold")
                    }
                }
                Button(
                    onClick = {
                        actions.onEditHoldFullOrderDetails(
                            order.orderId, editName, editPhone, editAddress, editCity, editState, editPin, editSize, editColor, editCustomImageUri
                        )
                        actions.onHoldOrder(order.orderId, selectedReason, holdNotes, adminApproved)
                        Toast.makeText(context, "⏸️ Order placed On Hold", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                    modifier = Modifier.testTag("dialog_confirm_hold_btn")
                ) {
                    Text(if (order.sellerStage.equals("On Hold", true)) "Update Hold" else "Hold Order")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun CancelOrRejectOrderDialog(
    order: OrderItem,
    isRejectFlow: Boolean,
    actions: SellerOrderActions,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val reasons = listOf(
        "ग्राहक ने खुद ऑर्डर कैंसिल किया",
        "ग्राहक ने गलत प्रोडक्ट ऑर्डर किया",
        "गलत Size या Colour चुना गया",
        "ग्राहक का पता गलत / अधूरा है",
        "प्रोडक्ट स्टॉक में उपलब्ध नहीं है",
        "पेमेंट फेल / वेरिफिकेशन फेल",
        "विक्रेता ऑर्डर पूरा नहीं कर पाया"
    )
    var selectedReason by remember(order.orderId) {
        mutableStateOf(if (isRejectFlow) reasons[4] else reasons[0])
    }
    var customReasonInput by remember(order.orderId) { mutableStateOf("") }
    var cancelledBy by remember { mutableStateOf(if (isRejectFlow) "Seller" else "Customer") }
    var restoreStock by remember { mutableStateOf(true) }

    val alreadyHandedToCourier = order.status == OrderStatus.SHIPPED || order.status == OrderStatus.DELIVERED

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (alreadyHandedToCourier) "Shipped Order RTO Notice"
                else if (isRejectFlow) "Reject Pending Order"
                else "Cancel Order & Refund",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFFC62828)
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (alreadyHandedToCourier) {
                    Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            "⚠️ यह ऑर्डर कूरियर को पहले ही दिया जा चुका है (${order.courierPartner})। इसे सामान्य Cancellation के बजाय RTO प्रक्रिया में भेजना होगा।",
                            fontSize = 12.sp,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                } else {
                    Text("Cancellation Reason (कारण चुनें):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    reasons.forEach { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = r },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedReason == r && customReasonInput.isBlank(), onClick = {
                                selectedReason = r
                                customReasonInput = ""
                            })
                            Text(r, fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = customReasonInput,
                        onValueChange = { customReasonInput = it },
                        label = { Text("Or Enter Custom Reason (अन्य कारण)") },
                        placeholder = { Text("e.g. Size exchange or address correction") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("cancel_custom_reason_input")
                    )

                    Text("Cancelled By (किसने कैंसिल किया):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Customer", "Seller", "Admin").forEach { actor ->
                            FilterChip(
                                selected = cancelledBy == actor,
                                onClick = { cancelledBy = actor },
                                label = { Text(actor, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { restoreStock = !restoreStock }
                    ) {
                        Checkbox(checked = restoreStock, onCheckedChange = { restoreStock = it })
                        Text("Restore Product Stock (+${order.quantity} qty)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        val isCod = order.paymentMethod.contains("COD", true)
                        Text(
                            if (isCod) "COD Order: No online refund required."
                            else "Prepaid Order: Automatic Refund of ₹${order.totalAmount} will be initiated.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (alreadyHandedToCourier) {
                Button(
                    onClick = {
                        actions.onMarkRto(order.orderId, "Seller/Customer Cancellation after Dispatch -> RTO Process")
                        Toast.makeText(context, "↩️ Order moved to separate RTO process!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                ) {
                    Text("Start RTO Process")
                }
            } else {
                Button(
                    onClick = {
                        val finalReason = customReasonInput.trim().ifBlank { selectedReason }
                        actions.onCancelOrderDetailed(order.orderId, finalReason, cancelledBy, restoreStock)
                        Toast.makeText(context, "Order #${order.orderId} cancelled & recorded", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.testTag("confirm_cancel_detailed_btn")
                ) {
                    Text(if (isRejectFlow) "Reject Order" else "Confirm Cancel")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Keep Order") }
        }
    )
}

@Composable
fun ShipmentTrackingAndIssueDialog(
    order: OrderItem,
    actions: SellerOrderActions,
    onOpenContactCourier: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var awbInput by remember(order.orderId) { mutableStateOf(order.awbNumber) }
    var selectedCourier by remember(order.orderId) { mutableStateOf(order.courierPartner) }
    var locationInput by remember(order.orderId) { mutableStateOf(order.currentTrackingLocation) }
    var expectedDeliveryInput by remember(order.orderId) { mutableStateOf(order.expectedDeliveryDate) }

    var showFullDetailsEdit by remember { mutableStateOf(false) }
    var editName by remember(order.orderId) { mutableStateOf(order.customerName) }
    var editPhone by remember(order.orderId) { mutableStateOf(order.customerPhone) }
    var editAddress by remember(order.orderId) { mutableStateOf(order.deliveryAddress) }
    var editCity by remember(order.orderId) { mutableStateOf(order.city) }
    var editState by remember(order.orderId) { mutableStateOf(order.state) }
    var editPin by remember(order.orderId) { mutableStateOf(order.pinCode) }
    var editColor by remember(order.orderId) { mutableStateOf(order.selectedColor) }
    var editSize by remember(order.orderId) { mutableStateOf(order.selectedSize) }
    var editImageUri by remember(order.orderId) { mutableStateOf(order.customProductImageUri) }

    var delayNote by remember { mutableStateOf("Weather / Highway Transit Delay") }
    var rtoReasonInput by remember { mutableStateOf("Failed Delivery Attempt / Customer Unreachable") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            editImageUri = uri.toString()
            Toast.makeText(context, "✓ Shipped Product image updated!", Toast.LENGTH_SHORT).show()
        }
    }

    val currentStepIndex = when {
        order.status == OrderStatus.DELIVERED || order.sellerStage.equals("Delivered", true) -> 6
        order.sellerStage.equals("Out for Delivery", true) -> 5
        order.status == OrderStatus.SHIPPED || order.sellerStage.equals("Shipped", true) -> 4
        order.sellerStage.equals("Ready to Ship", true) -> 3
        order.isPacked || order.isQualityVerified -> 2
        order.isOrderConfirmed -> 1
        else -> 0
    }
    val journeySteps = listOf(
        "Pending", "Confirmed", "Processing", "Ready to Ship", "Shipped", "Out for Delivery", "Delivered"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF1565C0))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Shipped AWB Update & Live Tracking", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 7-Step Customer & Admin Order Journey
                Surface(color = Color(0xFFF3E5F5), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "📍 7-Step Order Journey (Live Customer & Admin Sync)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF4A148C)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            journeySteps.forEachIndexed { idx, stepName ->
                                val done = idx <= currentStepIndex
                                Surface(
                                    color = if (done) Color(0xFF2E7D32) else Color.White,
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (done) Color(0xFF2E7D32) else Color(0xFFCE93D8))
                                ) {
                                    Text(
                                        "${idx + 1}. $stepName",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (done) Color.White else Color(0xFF6A1B9A),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Current Live Summary
                Surface(
                    color = if (order.isDeliveryDelayed || order.isRtoFlagged) Color(0xFFFFF3E0) else Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Courier: ${order.courierPartner} • AWB: ${order.awbNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1565C0))
                        Text("Status: ${order.statusText}", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Expected Delivery: ${order.expectedDeliveryDate}", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        Text("Live Location: ${order.currentTrackingLocation}", fontSize = 11.sp, color = TextSecondary)
                        if (order.isDeliveryDelayed) {
                            Text("⚠️ DELAYED DELIVERY ALERT ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                        }
                        if (order.isRtoFlagged) {
                            Text("↩️ RTO TRACKING: ${order.rtoReason.ifBlank { "Return to Origin Active" }}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
                        }
                    }
                }

                // AWB Number & Courier Update Section
                Text("1. Update AWB Number, Courier & Location", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SELLER_COURIER_PARTNERS.forEach { partner ->
                        FilterChip(
                            selected = selectedCourier == partner,
                            onClick = { selectedCourier = partner },
                            label = { Text(partner.substringBefore(" "), fontSize = 10.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = awbInput,
                    onValueChange = { awbInput = it },
                    label = { Text("AWB Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("shipped_awb_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = expectedDeliveryInput,
                        onValueChange = { expectedDeliveryInput = it },
                        label = { Text("Expected Delivery") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = { locationInput = it },
                        label = { Text("Courier Location") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                // Optional Edit Customer, Address, City, State, Pincode, Colour, Size, Photo
                OutlinedButton(
                    onClick = { showFullDetailsEdit = !showFullDetailsEdit },
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (showFullDetailsEdit) "Hide Customer & Photo Fields" else "Edit Customer, City/Pincode, Size/Colour & Image Upload",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showFullDetailsEdit) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        com.example.ui.components.AppManagedImage(
                            drawableRes = order.product.imageRes,
                            customUri = editImageUri ?: order.product.customImageUri,
                            contentDescription = order.product.name,
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        )
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f).height(40.dp).testTag("shipped_edit_upload_photo_btn")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (editImageUri != null) "✓ Photo Changed" else "Image Upload", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Customer Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Mobile Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editAddress, onValueChange = { editAddress = it }, label = { Text("Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = editCity, onValueChange = { editCity = it }, label = { Text("City") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = editState, onValueChange = { editState = it }, label = { Text("State") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(
                        value = editPin,
                        onValueChange = { editPin = it.filter { ch -> ch.isDigit() }.take(6) },
                        label = { Text("Pincode") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = editColor, onValueChange = { editColor = it }, label = { Text("Colour Name") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = editSize, onValueChange = { editSize = it }, label = { Text("Size Name & No.") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                }

                Button(
                    onClick = {
                        actions.onUpdateShippedAwbAndDetails(
                            order.orderId,
                            awbInput,
                            selectedCourier,
                            locationInput,
                            expectedDeliveryInput,
                            editName,
                            editPhone,
                            editAddress,
                            editCity,
                            editState,
                            editPin,
                            editSize,
                            editColor,
                            editImageUri
                        )
                        Toast.makeText(context, "✓ AWB & Shipment Tracking Updated!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("update_awb_tracking_btn")
                ) {
                    Text("Update AWB, Tracking & Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Automatic Tracking Updater & Customer SMS
                if (order.status == OrderStatus.SHIPPED && !order.isRtoFlagged) {
                    val nextLabel = if (order.sellerStage.equals("Out for Delivery", true)) {
                        "Auto-Update: Mark as Delivered ✅"
                    } else {
                        "Auto-Update: Move to Out for Delivery 🛵"
                    }
                    Button(
                        onClick = {
                            actions.onAdvanceTracking(order.orderId)
                            Toast.makeText(context, "✓ Live tracking updated & customer notified!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("auto_advance_tracking_btn")
                    ) {
                        Text(nextLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = {
                        actions.onSendCustomerTrackingAlert(order.orderId)
                        Toast.makeText(context, "📲 SMS & Tracking Notification sent to ${order.customerPhone}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp).testTag("send_customer_tracking_sms_btn")
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF1565C0))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Customer SMS / Delivery Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Delay Alert Toggle
                OutlinedTextField(
                    value = delayNote,
                    onValueChange = { delayNote = it },
                    label = { Text("Delay / Issue Reason") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        actions.onToggleDelayAlert(order.orderId, !order.isDeliveryDelayed, delayNote)
                        Toast.makeText(
                            context,
                            if (!order.isDeliveryDelayed) "⚠️ Delayed Delivery Alert Sent!" else "✓ Delay Alert Cleared",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text(
                        if (order.isDeliveryDelayed) "Clear Delay Alert" else "⚠️ Raise Delayed Delivery Alert",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                }

                // Failed Delivery Management & Separate RTO Process
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text("Failed Delivery Management & RTO Tracking", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF6A1B9A))
                OutlinedTextField(
                    value = rtoReasonInput,
                    onValueChange = { rtoReasonInput = it },
                    label = { Text("Failed Delivery / RTO Reason") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            actions.onLogFailedDeliveryAttempt(order.orderId, rtoReasonInput)
                            Toast.makeText(context, "⚠️ Failed Delivery Attempt Logged & Re-attempt Set!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(40.dp).testTag("log_failed_delivery_btn")
                    ) {
                        Text("Failed Attempt", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                    }
                    Button(
                        onClick = {
                            actions.onMarkRto(order.orderId, rtoReasonInput)
                            Toast.makeText(context, "↩️ Marked for Separate RTO Tracking", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                        modifier = Modifier.weight(1.2f).height(40.dp)
                    ) {
                        Text(
                            if (order.isRtoFlagged) "✓ RTO Active" else "Initiate RTO Tracking",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenContactCourier()
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Contact ${order.courierPartner}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Shipment History Trail
                if (order.orderHistoryLog.isNotEmpty()) {
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    Text("Shipment History (${order.orderHistoryLog.size} updates)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                    Surface(color = Color(0xFFF8F9FA), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            order.orderHistoryLog.takeLast(5).reversed().forEach { logEntry ->
                                Text("• $logEntry", fontSize = 10.sp, color = TextSecondary, lineHeight = 14.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun AddHoldProductDialog(
    products: List<Product>,
    actions: SellerOrderActions,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct()
    }
    var selectedCategory by remember { mutableStateOf("All") }
    val filteredProducts = remember(products, selectedCategory) {
        if (selectedCategory == "All") products else products.filter { it.category == selectedCategory }
    }
    var selectedProduct by remember(products) { mutableStateOf(products.firstOrNull()) }

    var customerName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("+91 9128754982") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Bettiah") }
    var state by remember { mutableStateOf("Bihar") }
    var pinCode by remember { mutableStateOf("845438") }
    var colorName by remember { mutableStateOf("Festive Maroon") }
    var sizeNameOrNumber by remember { mutableStateOf("38 (M)") }
    var qtyText by remember { mutableStateOf("1") }
    var customImageUri by remember { mutableStateOf<String?>(null) }

    val holdPresets = listOf(
        "ग्राहक से जानकारी की पुष्टि करनी है",
        "ग्राहक का पता गलत / अधूरा है",
        "प्रोडक्ट का Size / Colour उपलब्ध नहीं है",
        "पेमेंट की पुष्टि नहीं हुई है",
        "ग्राहक ने कुछ समय रुकने को कहा है",
        "प्रोडक्ट की क्वालिटी चेक करनी है",
        "एडमिन ने ऑर्डर रोक दिया है"
    )
    var selectedReason by remember { mutableStateOf(holdPresets.first()) }
    var holdNotes by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            customImageUri = uri.toString()
            Toast.makeText(context, "✓ Product image uploaded for Hold Order!", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE65100))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Add Any Product to On Hold",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "सभी प्रकार के प्रोडक्ट एक साथ On Hold और PDF के लिए जोड़ें",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("1. Select Product Category (सभी कैटेगरी)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = cat
                                    val firstMatch = if (cat == "All") products.firstOrNull() else products.firstOrNull { it.category == cat }
                                    if (firstMatch != null) selectedProduct = firstMatch
                                },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("2. Choose Product", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredProducts.forEach { prod ->
                            val isSel = selectedProduct?.id == prod.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) Color(0xFFFFF3E0) else Color(0xFFF5F5F5),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) Color(0xFFE65100) else Color(0xFFE0E0E0)
                                ),
                                modifier = Modifier
                                    .width(150.dp)
                                    .clickable { selectedProduct = prod }
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    AppManagedImage(
                                        drawableRes = prod.imageRes,
                                        customUri = prod.customImageUri,
                                        contentDescription = prod.name,
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp))
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        Text("${prod.category} • ₹${prod.price}", fontSize = 9.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Text("3. Product Image Upload (Optional Custom Photo)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8F9FA))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val activeProd = selectedProduct ?: products.firstOrNull()
                        if (activeProd != null) {
                            AppManagedImage(
                                drawableRes = activeProd.imageRes,
                                customUri = customImageUri ?: activeProd.customImageUri,
                                contentDescription = activeProd.name,
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f).height(42.dp).testTag("add_hold_upload_photo_btn")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (customImageUri != null) "✓ Photo Attached (Change)" else "Upload Product Photo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text("4. Customer, Full Address & Size/Colour", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        placeholder = { Text("e.g. Anjali Kumari") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_hold_cust_name_input")
                    )
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = { Text("Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_hold_mobile_input")
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address (House / Road / Landmark)") },
                        placeholder = { Text("e.g. Ward 12, Near Durga Mandir") },
                        modifier = Modifier.fillMaxWidth().testTag("add_hold_address_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { pinCode = it.filter { ch -> ch.isDigit() }.take(6) },
                            label = { Text("Pin Code") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = qtyText,
                            onValueChange = { qtyText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Qty") },
                            singleLine = true,
                            modifier = Modifier.weight(0.6f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = colorName,
                            onValueChange = { colorName = it },
                            label = { Text("Colour Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sizeNameOrNumber,
                            onValueChange = { sizeNameOrNumber = it },
                            label = { Text("Size Name ya Number") },
                            placeholder = { Text("M / L / 38 / 40") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("5. Hold Reason & Notes", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFE65100))
                    holdPresets.forEach { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = preset }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedReason == preset, onClick = { selectedReason = preset })
                            Text(preset, fontSize = 11.sp)
                        }
                    }
                    OutlinedTextField(
                        value = holdNotes,
                        onValueChange = { holdNotes = it },
                        label = { Text("Hold Notes (जरूरी जानकारी)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val prod = selectedProduct ?: products.firstOrNull() ?: return@Button
                            val qty = (qtyText.toIntOrNull() ?: 1).coerceAtLeast(1)
                            actions.onAddHoldProductOrder(
                                customerName,
                                mobileNumber,
                                address.ifBlank { "$city Market Road" },
                                city,
                                state,
                                pinCode,
                                prod,
                                sizeNameOrNumber,
                                colorName,
                                qty,
                                selectedReason,
                                holdNotes,
                                customImageUri
                            )
                            Toast.makeText(context, "✓ ${prod.category} added to On Hold!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                        modifier = Modifier.weight(1.4f).height(46.dp).testTag("confirm_add_hold_product_btn")
                    ) {
                        Icon(Icons.Default.PauseCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to On Hold", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HoldOrderPdfDialog(
    ordersToExport: List<OrderItem>,
    actions: SellerOrderActions,
    isCombinedAllReport: Boolean,
    isPendingMode: Boolean = false,
    isShippedCourierMode: Boolean = false,
    isCancelledMode: Boolean = false,
    isReadyToShipMode: Boolean = false,
    customReportTitle: String? = null,
    customFilePrefix: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var pdfResultMsg by remember { mutableStateOf<String?>(null) }
    var targetPhotoOrderId by remember { mutableStateOf<String?>(ordersToExport.firstOrNull()?.orderId) }
    var showRenderedPdfPages by remember { mutableStateOf(true) }
    var zoomPdfPage by remember { mutableStateOf(false) }

    val quickPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val ord = ordersToExport.find { it.orderId == targetPhotoOrderId } ?: ordersToExport.firstOrNull()
        if (uri != null && ord != null) {
            if (isCancelledMode || ord.status == OrderStatus.CANCELLED) {
                actions.onEditCancelledFullDetails(
                    ord.orderId,
                    ord.awbNumber,
                    ord.courierPartner,
                    ord.customerName,
                    ord.customerPhone,
                    ord.deliveryAddress,
                    ord.city,
                    ord.state,
                    ord.pinCode,
                    ord.customerAgeYears,
                    ord.selectedSize,
                    ord.selectedColor,
                    ord.cancelReason,
                    ord.cancelledBy,
                    ord.refundStatus,
                    ord.refundAmount,
                    ord.isStockRestored,
                    uri.toString()
                )
            } else {
                actions.onEditHoldFullOrderDetails(
                    ord.orderId,
                    ord.customerName,
                    ord.customerPhone,
                    ord.deliveryAddress,
                    ord.city,
                    ord.state,
                    ord.pinCode,
                    ord.selectedSize,
                    ord.selectedColor,
                    uri.toString()
                )
            }
            Toast.makeText(context, "✓ Product Image updated for #${ord.orderId} PDF!", Toast.LENGTH_SHORT).show()
        }
    }

    val categoriesText = remember(ordersToExport) {
        ordersToExport.map { it.product.category }.distinct().joinToString(" • ").ifBlank { "All Products" }
    }
    val reportTitle = customReportTitle ?: when {
        isCancelledMode && isCombinedAllReport -> "GYANVI DRESSES • CANCELLED ORDERS PDF (${ordersToExport.size} ITEMS)"
        isCancelledMode -> "GYANVI DRESSES • CANCELLED ORDER #${ordersToExport.firstOrNull()?.orderId ?: ""}"
        isReadyToShipMode && isCombinedAllReport -> "GYANVI DRESSES • READY TO SHIP PDF (${ordersToExport.size} ITEMS)"
        isReadyToShipMode -> "GYANVI DRESSES • READY TO SHIP #${ordersToExport.firstOrNull()?.orderId ?: ""}"
        isShippedCourierMode && isCombinedAllReport -> "GYANVI DRESSES • COURIER DISPATCH PDF (${ordersToExport.size} ITEMS)"
        isShippedCourierMode -> "GYANVI DRESSES • COURIER DISPATCH #${ordersToExport.firstOrNull()?.orderId ?: ""}"
        isPendingMode && isCombinedAllReport -> "GYANVI DRESSES • PENDING ORDERS PDF (${ordersToExport.size} ITEMS)"
        isPendingMode -> "GYANVI DRESSES • PENDING ORDER #${ordersToExport.firstOrNull()?.orderId ?: ""}"
        isCombinedAllReport -> "GYANVI DRESSES • COMBINED ON HOLD PDF (${ordersToExport.size} ITEMS)"
        else -> "GYANVI DRESSES • ON HOLD ORDER #${ordersToExport.firstOrNull()?.orderId ?: ""}"
    }
    val filePrefix = customFilePrefix ?: when {
        isCancelledMode && isCombinedAllReport -> "Gyanvi_Cancelled_Orders"
        isCancelledMode -> "CancelledOrder_${ordersToExport.firstOrNull()?.orderId ?: "Doc"}"
        isReadyToShipMode && isCombinedAllReport -> "Gyanvi_ReadyToShip_Orders"
        isReadyToShipMode -> "ReadyToShip_${ordersToExport.firstOrNull()?.orderId ?: "Doc"}"
        isShippedCourierMode && isCombinedAllReport -> "Gyanvi_Shipped_Courier_Dispatch"
        isShippedCourierMode -> "CourierPDF_${ordersToExport.firstOrNull()?.awbNumber ?: ordersToExport.firstOrNull()?.orderId ?: "Doc"}"
        isPendingMode && isCombinedAllReport -> "Gyanvi_All_Pending_Orders"
        isPendingMode -> "PendingOrder_${ordersToExport.firstOrNull()?.orderId ?: "Doc"}"
        isCombinedAllReport -> "Gyanvi_All_OnHold_Orders"
        else -> "HoldOrder_${ordersToExport.firstOrNull()?.orderId ?: "Doc"}"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            when {
                                isCancelledMode -> Color(0xFFC62828)
                                isReadyToShipMode -> Color(0xFF2E7D32)
                                isPendingMode || isShippedCourierMode || customReportTitle != null -> Color(0xFF1A237E)
                                else -> MeeshoMagenta
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            when {
                                isCancelledMode -> "Cancelled Orders PDF Show"
                                isReadyToShipMode -> "Ready to Ship PDF Show"
                                isShippedCourierMode -> "Shipped PDF Show (Courier Dispatch)"
                                customReportTitle != null -> "All Orders PDF Show"
                                isPendingMode -> "Pending Order PDF Show"
                                else -> "On Hold PDF Show"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "${ordersToExport.size} Product(s) • $categoriesText",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                val renderedPdfPages = remember(ordersToExport, reportTitle) {
                    if (ordersToExport.isEmpty()) emptyList()
                    else HoldOrderPdfGenerator.renderPdfPagesToBitmaps(context, ordersToExport, reportTitle)
                }

                // Segmented View Switcher: Real A4 PDF Show vs Details & Photo Upload
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFECEFF1))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showRenderedPdfPages = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showRenderedPdfPages) Color(0xFF1565C0) else Color.White,
                            contentColor = if (showRenderedPdfPages) Color.White else Color(0xFF1A237E)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier.weight(1f).height(38.dp).testTag("hold_pdf_show_tab")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("PDF Show (A4)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showRenderedPdfPages = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!showRenderedPdfPages) Color(0xFF1A237E) else Color.White,
                            contentColor = if (!showRenderedPdfPages) Color.White else Color(0xFF1A237E)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier.weight(1f).height(38.dp).testTag("hold_pdf_details_tab")
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Details & Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Live Scrollable A4 PDF Viewer or Order Summary Preview
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFDFE3EA))
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (ordersToExport.isEmpty()) {
                        Surface(color = CardWhite, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Text(
                                when {
                                    isCancelledMode -> "No Cancelled orders available for PDF export."
                                    isReadyToShipMode -> "No Ready to Ship orders available for PDF export."
                                    isShippedCourierMode -> "No Shipped orders available for Courier PDF export."
                                    isPendingMode -> "No Pending products available for PDF export."
                                    else -> "No On Hold products available for PDF export."
                                },
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else if (showRenderedPdfPages) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "📄 Showing Real A4 PDF (${renderedPdfPages.size} Page)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A237E)
                            )
                            TextButton(
                                onClick = { zoomPdfPage = !zoomPdfPage },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    if (zoomPdfPage) Icons.Default.ZoomOut else Icons.Default.ZoomIn,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (zoomPdfPage) "Fit Width" else "Zoom 1.5x", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        val horizontalPdfScroll = rememberScrollState()
                        renderedPdfPages.forEachIndexed { pageIdx, pageBitmap ->
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(6.dp),
                                shadowElevation = 4.dp,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB0BEC5)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("rendered_pdf_page_${pageIdx + 1}")
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF5F7FA))
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Page ${pageIdx + 1} of ${renderedPdfPages.size}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF37474F)
                                        )
                                        Text(
                                            "A4 Print Ready • Barcode Included",
                                            fontSize = 10.sp,
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Box(
                                        modifier = if (zoomPdfPage) {
                                            Modifier.fillMaxWidth().horizontalScroll(horizontalPdfScroll)
                                        } else {
                                            Modifier.fillMaxWidth()
                                        }
                                    ) {
                                        Image(
                                            bitmap = pageBitmap.asImageBitmap(),
                                            contentDescription = "Rendered PDF Page ${pageIdx + 1}",
                                            contentScale = if (zoomPdfPage) ContentScale.None else ContentScale.FillWidth,
                                            modifier = if (zoomPdfPage) {
                                                Modifier.width(560.dp).aspectRatio(595f / 842f)
                                            } else {
                                                Modifier.fillMaxWidth().aspectRatio(595f / 842f)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        ordersToExport.forEachIndexed { idx, order ->
                            Surface(
                                color = CardWhite,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE91E63).copy(alpha = 0.4f)),
                                shadowElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "#${idx + 1} • Order #${order.orderId} (${order.product.category})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF1A237E)
                                        )
                                        Text(
                                            "₹${order.totalAmount}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = MeeshoMagenta
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            AppManagedImage(
                                                drawableRes = order.product.imageRes,
                                                customUri = order.customProductImageUri ?: order.product.customImageUri,
                                                contentDescription = order.product.name,
                                                modifier = Modifier
                                                    .size(84.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            TextButton(
                                                onClick = {
                                                    targetPhotoOrderId = order.orderId
                                                    quickPhotoPicker.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                },
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                                modifier = Modifier.height(28.dp).testTag("pdf_upload_img_${order.orderId}")
                                            ) {
                                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("Upload Photo", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text(order.product.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                            Text("Customer Name: ${order.customerName}", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                            Text("Mobile Number: ${order.customerPhone}", fontSize = 11.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                                            Text("Address: ${order.deliveryAddress}", fontSize = 11.sp, color = TextSecondary)
                                            Text(
                                                "City: ${order.city.ifBlank { "Bettiah" }}  •  State: ${order.state.ifBlank { "Bihar" }}  •  Pin Code: ${order.pinCode.ifBlank { "845438" }}",
                                                fontSize = 11.sp,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                "Colour Name: ${order.selectedColor}  •  Size Name/No.: ${order.selectedSize} (Qty ${order.quantity})",
                                                fontSize = 11.sp,
                                                color = Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Bold
                                            )
                                            val isShippedOrder = !isCancelledMode &&
                                                order.status != OrderStatus.CANCELLED &&
                                                !isReadyToShipMode &&
                                                !order.sellerStage.equals("Ready to Ship", true) &&
                                                (isShippedCourierMode ||
                                                    order.status == OrderStatus.SHIPPED ||
                                                    order.status == OrderStatus.DELIVERED ||
                                                    order.isRtoFlagged)
                                            if (isCancelledMode || order.status == OrderStatus.CANCELLED) {
                                                Text(
                                                    "Age / Years: ${order.customerAgeYears.ifBlank { "26 Years" }}  •  AWB: ${order.awbNumber} (${order.courierPartner})",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF1565C0),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else if (isReadyToShipMode || order.sellerStage.equals("Ready to Ship", true)) {
                                                Text(
                                                    "Courier: ${order.courierPartner} • Pickup: ${order.pickupStatus} • Charges: ₹${order.shippingCharges} • ETA: ${order.expectedDeliveryDate}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF1565C0),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else if (isShippedOrder) {
                                                val journeyTag = when {
                                                    order.status == OrderStatus.DELIVERED || order.sellerStage.equals("Delivered", true) -> "7/7 Delivered"
                                                    order.sellerStage.equals("Out for Delivery", true) -> "6/7 Out for Delivery"
                                                    else -> "5/7 Shipped"
                                                }
                                                Text(
                                                    "Courier: ${order.courierPartner} • AWB: ${order.awbNumber} • ETA: ${order.expectedDeliveryDate}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF1565C0),
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    "Journey ($journeyTag) • Status: ${order.statusText} • Live: ${order.currentTrackingLocation}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF2E7D32),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Text(
                                                when {
                                                    isCancelledMode || order.status == OrderStatus.CANCELLED ->
                                                        "Cancelled By: ${order.cancelledBy.ifBlank { "Customer" }} • Reason: ${order.cancelReason.ifBlank { order.statusText }} • Refund: ${order.refundStatus} (₹${order.refundAmount})"
                                                    isReadyToShipMode || order.sellerStage.equals("Ready to Ship", true) ->
                                                        "Pack Check: Verify=${if (order.isQualityVerified) "✓" else "⏳"} Pack=${if (order.isPacked) "✓" else "⏳"} Inv=${if (order.isInvoiceGenerated) "✓" else "⏳"} Scan=${if (order.isBarcodeScanned) "✓" else "⏳"} • AWB: ${order.awbNumber}"
                                                    isShippedOrder -> buildString {
                                                        if (order.isDeliveryDelayed) append("⚠️ DELAY ALERT • ")
                                                        if (order.isRtoFlagged) append("↩️ RTO: ${order.rtoReason.ifBlank { "Active" }} • ")
                                                        append("Auto Tracking: ON • History: ${order.orderHistoryLog.lastOrNull() ?: order.statusText}")
                                                    }
                                                    isPendingMode -> "Pending • Payment: ${order.paymentMethod} (${order.paymentStatus})"
                                                    else -> "Hold Reason: ${order.holdReason.ifBlank { "Customer Confirmation Pending" }}"
                                                },
                                                fontSize = 10.sp,
                                                color = when {
                                                    isCancelledMode || order.status == OrderStatus.CANCELLED -> Color(0xFFC62828)
                                                    isReadyToShipMode || order.sellerStage.equals("Ready to Ship", true) -> Color(0xFF2E7D32)
                                                    isShippedOrder -> if (order.isDeliveryDelayed || order.isRtoFlagged) Color(0xFFC62828) else Color(0xFF1565C0)
                                                    isPendingMode -> Color(0xFF1A237E)
                                                    else -> Color(0xFFE65100)
                                                },
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Generated Scannable Code-128B Barcode + Verification QR Strip for PDF
                                    val isShippedBarcodeOrder = !isCancelledMode &&
                                        order.status != OrderStatus.CANCELLED &&
                                        !isReadyToShipMode &&
                                        !order.sellerStage.equals("Ready to Ship", true) &&
                                        (isShippedCourierMode ||
                                            order.status == OrderStatus.SHIPPED ||
                                            order.status == OrderStatus.DELIVERED ||
                                            order.isRtoFlagged)
                                    val holdBarcodeCode = when {
                                        isCancelledMode || order.status == OrderStatus.CANCELLED ->
                                            order.awbNumber.replace("[^A-Za-z0-9-]".toRegex(), "").ifBlank { "GYN-CAN-${order.orderId.takeLast(6)}" }
                                        isReadyToShipMode || order.sellerStage.equals("Ready to Ship", true) ->
                                            order.awbNumber.replace("[^A-Za-z0-9-]".toRegex(), "").ifBlank { "GYN-RTS-${order.orderId.takeLast(6)}-${order.pinCode.ifBlank { "845438" }}" }
                                        isShippedBarcodeOrder -> order.awbNumber.replace("[^A-Za-z0-9-]".toRegex(), "").ifBlank { "GYN-SHP-${order.orderId.takeLast(6)}" }
                                        isPendingMode -> "GYN-PND-${order.orderId.takeLast(6)}-${order.pinCode.ifBlank { "845438" }}"
                                        else -> "GYN-${order.orderId.takeLast(6)}-${order.pinCode.ifBlank { "845438" }}"
                                    }
                                    val barcodeBitmap = remember(order.orderId, order.pinCode, order.awbNumber, isPendingMode, isShippedCourierMode, isCancelledMode, isReadyToShipMode) {
                                        QrCodeGenerator.generateBarcodeBitmap(
                                            code = holdBarcodeCode,
                                            widthPx = 460,
                                            heightPx = 88,
                                            labelText = "*$holdBarcodeCode*"
                                        )
                                    }
                                    val qrThumbBitmap = remember(order.orderId, order.customerPhone, order.pinCode, order.awbNumber, isPendingMode, isShippedCourierMode, isCancelledMode, isReadyToShipMode) {
                                        val qrPrefix = when {
                                            isCancelledMode || order.status == OrderStatus.CANCELLED -> "CANCELLED"
                                            isReadyToShipMode || order.sellerStage.equals("Ready to Ship", true) -> "READY"
                                            isShippedBarcodeOrder -> "SHIPPED"
                                            isPendingMode -> "PENDING"
                                            else -> "HOLD"
                                        }
                                        QrCodeGenerator.generateQrBitmap(
                                            content = "GYANVI-$qrPrefix|ORD=${order.orderId}|BC=$holdBarcodeCode|CUST=${order.customerName}|PIN=${order.pinCode}",
                                            sizePx = 128
                                        )
                                    }
                                    Surface(
                                        color = Color(0xFFFAFAFA),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Image(
                                                bitmap = qrThumbBitmap.asImageBitmap(),
                                                contentDescription = "Hold QR Code ${order.orderId}",
                                                modifier = Modifier.size(46.dp)
                                            )
                                            Image(
                                                bitmap = barcodeBitmap.asImageBitmap(),
                                                contentDescription = "Hold Barcode $holdBarcodeCode",
                                                contentScale = ContentScale.FillWidth,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(46.dp)
                                                    .testTag("hold_pdf_barcode_${order.orderId}")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                pdfResultMsg?.let { msg ->
                    Surface(color = Color(0xFFE8F5E9), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            msg,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                // PDF Download + PDF Cancel + PDF Show / Share Buttons
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                HoldOrderPdfGenerator.cancelStagedPdfPreview(context)
                                Toast.makeText(context, "✖️ PDF Cancelled", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC62828)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(46.dp).testTag("hold_pdf_cancel_btn")
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val res = HoldOrderPdfGenerator.saveHoldOrdersPdfToDownloads(
                                    context = context,
                                    orders = ordersToExport,
                                    fileNamePrefix = filePrefix,
                                    reportTitle = reportTitle,
                                    showToast = true
                                )
                                pdfResultMsg = res.message
                                showRenderedPdfPages = true
                            },
                            enabled = ordersToExport.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.25f).height(46.dp).testTag("hold_pdf_download_btn")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF Download", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showRenderedPdfPages = true
                                HoldOrderPdfGenerator.openOrShareHoldOrdersPdf(
                                    context = context,
                                    orders = ordersToExport,
                                    fileNamePrefix = filePrefix,
                                    reportTitle = reportTitle,
                                    preferExternalView = true
                                )
                            },
                            enabled = ordersToExport.isNotEmpty(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(40.dp).testTag("hold_pdf_open_external_btn")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF Show App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                if (isShippedCourierMode) {
                                    val mainCourier = ordersToExport.firstOrNull()?.courierPartner ?: "Delhivery Express"
                                    actions.onMarkCourierPdfSent(ordersToExport.map { it.orderId }, mainCourier)
                                    pdfResultMsg = "✓ Courier PDF Sent to $mainCourier & Logged!"
                                    HoldOrderPdfGenerator.shareHoldOrdersPdf(
                                        context = context,
                                        orders = ordersToExport,
                                        fileNamePrefix = filePrefix,
                                        reportTitle = reportTitle
                                    )
                                } else {
                                    HoldOrderPdfGenerator.shareHoldOrdersPdf(
                                        context = context,
                                        orders = ordersToExport,
                                        fileNamePrefix = filePrefix,
                                        reportTitle = reportTitle
                                    )
                                }
                            },
                            enabled = ordersToExport.isNotEmpty(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.25f).height(40.dp).testTag(if (isShippedCourierMode) "send_pdf_to_courier_btn" else "hold_pdf_share_btn")
                        ) {
                            Icon(if (isShippedCourierMode) Icons.Default.Send else Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isShippedCourierMode) "Send to Courier" else "Share PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddPendingProductDialog(
    products: List<Product>,
    actions: SellerOrderActions,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct()
    }
    var selectedCategory by remember { mutableStateOf("All") }
    val filteredProducts = remember(products, selectedCategory) {
        if (selectedCategory == "All") products else products.filter { it.category == selectedCategory }
    }
    var selectedProduct by remember(products) { mutableStateOf(products.firstOrNull()) }

    // Custom new-type product override fields
    var useCustomNewType by remember { mutableStateOf(false) }
    var customProductName by remember { mutableStateOf("") }
    var customProductCategory by remember { mutableStateOf("Designer Suits") }
    var customPriceText by remember { mutableStateOf("599") }

    var customerName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("+91 9128754982") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Bettiah") }
    var state by remember { mutableStateOf("Bihar") }
    var pinCode by remember { mutableStateOf("845438") }
    var colorName by remember { mutableStateOf("Festive Maroon") }
    var sizeNameOrNumber by remember { mutableStateOf("38 (M)") }
    var qtyText by remember { mutableStateOf("1") }
    var paymentMethod by remember { mutableStateOf("UPI (PhonePe)") }
    var customImageUri by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            customImageUri = uri.toString()
            Toast.makeText(context, "✓ Product image attached for Pending Order & PDF!", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A237E))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Add New Product to Pending + PDF",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "नया प्रोडक्ट जोड़ें • ऑटो नोटिफिकेशन, बारकोड व PDF जनरेट होगा",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("1. Product Type / Catalog", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                        FilterChip(
                            selected = useCustomNewType,
                            onClick = { useCustomNewType = !useCustomNewType },
                            label = { Text(if (useCustomNewType) "✓ New Custom Type" else "+ New Type Product", fontSize = 11.sp) }
                        )
                    }

                    if (useCustomNewType) {
                        OutlinedTextField(
                            value = customProductName,
                            onValueChange = { customProductName = it },
                            label = { Text("New Product Name") },
                            placeholder = { Text("e.g. Floral Anarkali Gown") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("pending_custom_product_name")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customProductCategory,
                                onValueChange = { customProductCategory = it },
                                label = { Text("Category / Type") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = customPriceText,
                                onValueChange = { customPriceText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Price (₹)") },
                                singleLine = true,
                                modifier = Modifier.weight(0.7f)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = {
                                        selectedCategory = cat
                                        val firstMatch = if (cat == "All") products.firstOrNull() else products.firstOrNull { it.category == cat }
                                        if (firstMatch != null) selectedProduct = firstMatch
                                    },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredProducts.forEach { prod ->
                                val isSel = selectedProduct?.id == prod.id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) Color(0xFFE8EAF6) else Color(0xFFF5F5F5),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSel) 2.dp else 1.dp,
                                        color = if (isSel) Color(0xFF1A237E) else Color(0xFFE0E0E0)
                                    ),
                                    modifier = Modifier
                                        .width(150.dp)
                                        .clickable { selectedProduct = prod }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        AppManagedImage(
                                            drawableRes = prod.imageRes,
                                            customUri = prod.customImageUri,
                                            contentDescription = prod.name,
                                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp))
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(prod.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text("${prod.category} • ₹${prod.price}", fontSize = 9.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text("2. Product Image Upload (Optional)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8F9FA))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val activeProd = selectedProduct ?: products.firstOrNull()
                        if (activeProd != null) {
                            AppManagedImage(
                                drawableRes = activeProd.imageRes,
                                customUri = customImageUri ?: activeProd.customImageUri,
                                contentDescription = activeProd.name,
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f).height(42.dp).testTag("add_pending_upload_photo_btn")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (customImageUri != null) "✓ Photo Attached (Change)" else "Upload Product Image",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text("3. Customer, City/State/Pincode, Size & Colour", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        placeholder = { Text("e.g. Sunita Devi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_pending_cust_name_input")
                    )
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = { Text("Customer Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_pending_mobile_input")
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Delivery Address") },
                        placeholder = { Text("e.g. Station Road, Ward 10") },
                        modifier = Modifier.fillMaxWidth().testTag("add_pending_address_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { pinCode = it.filter { ch -> ch.isDigit() }.take(6) },
                            label = { Text("Pincode") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = qtyText,
                            onValueChange = { qtyText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Qty") },
                            singleLine = true,
                            modifier = Modifier.weight(0.6f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = colorName,
                            onValueChange = { colorName = it },
                            label = { Text("Colour Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sizeNameOrNumber,
                            onValueChange = { sizeNameOrNumber = it },
                            label = { Text("Size Number ya Name") },
                            placeholder = { Text("38 / 40 / M / XL") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("4. Payment Status", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("UPI (PhonePe)", "Cash on Delivery (COD)").forEach { payOpt ->
                            FilterChip(
                                selected = paymentMethod == payOpt,
                                onClick = { paymentMethod = payOpt },
                                label = { Text(if (payOpt.contains("COD")) "COD Pending" else "Paid (UPI)", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val baseProd = selectedProduct ?: products.firstOrNull() ?: return@Button
                            val chosenProd = if (useCustomNewType && customProductName.isNotBlank()) {
                                val parsedPrice = (customPriceText.toIntOrNull() ?: baseProd.price).coerceAtLeast(1)
                                baseProd.copy(
                                    id = "custom_${System.currentTimeMillis() % 10000}",
                                    name = customProductName.trim(),
                                    category = customProductCategory.trim().ifBlank { "Custom Dress" },
                                    price = parsedPrice,
                                    originalPrice = parsedPrice + 400,
                                    customImageUri = customImageUri ?: baseProd.customImageUri
                                )
                            } else {
                                baseProd
                            }
                            val qty = (qtyText.toIntOrNull() ?: 1).coerceAtLeast(1)
                            actions.onAddPendingProductOrder(
                                customerName,
                                mobileNumber,
                                address.ifBlank { "$city Market Road" },
                                city,
                                state,
                                pinCode,
                                chosenProd,
                                sizeNameOrNumber,
                                colorName,
                                qty,
                                paymentMethod,
                                customImageUri
                            )
                            Toast.makeText(context, "✓ New Pending Order added with Barcode & Alert!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                        modifier = Modifier.weight(1.4f).height(46.dp).testTag("confirm_add_pending_product_btn")
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Pending", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddShippedCourierOrderDialog(
    products: List<Product>,
    actions: SellerOrderActions,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct()
    }
    var selectedCategory by remember { mutableStateOf("All") }
    val filteredProducts = remember(products, selectedCategory) {
        if (selectedCategory == "All") products else products.filter { it.category == selectedCategory }
    }
    var selectedProduct by remember(products) { mutableStateOf(products.firstOrNull()) }

    var useCustomNewType by remember { mutableStateOf(false) }
    var customProductName by remember { mutableStateOf("") }
    var customProductCategory by remember { mutableStateOf("Kurti & Dupatta Set") }
    var customPriceText by remember { mutableStateOf("799") }

    var customerName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("+91 9128754982") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Patna") }
    var state by remember { mutableStateOf("Bihar") }
    var pinCode by remember { mutableStateOf("800020") }
    var colorName by remember { mutableStateOf("Royal Maroon") }
    var sizeNameOrNumber by remember { mutableStateOf("38 (M)") }
    var qtyText by remember { mutableStateOf("1") }
    var courierPartner by remember { mutableStateOf(SELLER_COURIER_PARTNERS.first()) }
    var awbNumber by remember { mutableStateOf("DLV-GYN-${(100000..999999).random()}") }
    var expectedDeliveryDate by remember { mutableStateOf("19 Nov 2026") }
    var customImageUri by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            customImageUri = uri.toString()
            Toast.makeText(context, "✓ Product image attached for Courier PDF!", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1565C0))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Ship Product & Send Courier PDF",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "कूरियर को पैकेट भेजें • AWB नंबर, ट्रैकिंग, फोटो व कस्टमर SMS अलर्ट",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("1. Product Show / Select Dress", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                        FilterChip(
                            selected = useCustomNewType,
                            onClick = { useCustomNewType = !useCustomNewType },
                            label = { Text(if (useCustomNewType) "✓ Custom Product" else "+ New Type Product", fontSize = 11.sp) }
                        )
                    }

                    if (useCustomNewType) {
                        OutlinedTextField(
                            value = customProductName,
                            onValueChange = { customProductName = it },
                            label = { Text("Product Name") },
                            placeholder = { Text("e.g. Designer Silk Anarkali") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("shipped_custom_product_name")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customProductCategory,
                                onValueChange = { customProductCategory = it },
                                label = { Text("Category") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = customPriceText,
                                onValueChange = { customPriceText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Price (₹)") },
                                singleLine = true,
                                modifier = Modifier.weight(0.7f)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = {
                                        selectedCategory = cat
                                        val firstMatch = if (cat == "All") products.firstOrNull() else products.firstOrNull { it.category == cat }
                                        if (firstMatch != null) selectedProduct = firstMatch
                                    },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredProducts.forEach { prod ->
                                val isSel = selectedProduct?.id == prod.id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) Color(0xFFE3F2FD) else Color(0xFFF5F5F5),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSel) 2.dp else 1.dp,
                                        color = if (isSel) Color(0xFF1565C0) else Color(0xFFE0E0E0)
                                    ),
                                    modifier = Modifier
                                        .width(150.dp)
                                        .clickable { selectedProduct = prod }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        AppManagedImage(
                                            drawableRes = prod.imageRes,
                                            customUri = prod.customImageUri,
                                            contentDescription = prod.name,
                                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp))
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(prod.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text("${prod.category} • ₹${prod.price}", fontSize = 9.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text("2. Product Image Upload", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8F9FA))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val activeProd = selectedProduct ?: products.firstOrNull()
                        if (activeProd != null) {
                            AppManagedImage(
                                drawableRes = activeProd.imageRes,
                                customUri = customImageUri ?: activeProd.customImageUri,
                                contentDescription = activeProd.name,
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f).height(42.dp).testTag("add_shipped_upload_photo_btn")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (customImageUri != null) "✓ Photo Attached (Change)" else "Image Upload (Product)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text("3. Courier Partner, AWB Number & Expected Delivery", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1565C0))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SELLER_COURIER_PARTNERS.forEach { partner ->
                            FilterChip(
                                selected = courierPartner == partner,
                                onClick = { courierPartner = partner },
                                label = { Text(partner.substringBefore(" "), fontSize = 11.sp) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = awbNumber,
                            onValueChange = { awbNumber = it },
                            label = { Text("AWB Number") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f).testTag("add_shipped_awb_input")
                        )
                        OutlinedTextField(
                            value = expectedDeliveryDate,
                            onValueChange = { expectedDeliveryDate = it },
                            label = { Text("Expected Delivery") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("4. Customer, Full Address, City/State/Pincode, Colour & Size", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        placeholder = { Text("e.g. Priya Kumari") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_shipped_cust_name_input")
                    )
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = { Text("Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_shipped_mobile_input")
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Delivery Address") },
                        placeholder = { Text("e.g. Kankarbagh Road, Ward 9") },
                        modifier = Modifier.fillMaxWidth().testTag("add_shipped_address_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { pinCode = it.filter { ch -> ch.isDigit() }.take(6) },
                            label = { Text("Pincode") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = qtyText,
                            onValueChange = { qtyText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Qty") },
                            singleLine = true,
                            modifier = Modifier.weight(0.6f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = colorName,
                            onValueChange = { colorName = it },
                            label = { Text("Colour Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sizeNameOrNumber,
                            onValueChange = { sizeNameOrNumber = it },
                            label = { Text("Size Name & Number") },
                            placeholder = { Text("38 / 40 / M / XL") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val baseProd = selectedProduct ?: products.firstOrNull() ?: return@Button
                            val chosenProd = if (useCustomNewType && customProductName.isNotBlank()) {
                                val parsedPrice = (customPriceText.toIntOrNull() ?: baseProd.price).coerceAtLeast(1)
                                baseProd.copy(
                                    id = "ship_custom_${System.currentTimeMillis() % 10000}",
                                    name = customProductName.trim(),
                                    category = customProductCategory.trim().ifBlank { "Custom Dress" },
                                    price = parsedPrice,
                                    originalPrice = parsedPrice + 400,
                                    customImageUri = customImageUri ?: baseProd.customImageUri
                                )
                            } else {
                                baseProd
                            }
                            val qty = (qtyText.toIntOrNull() ?: 1).coerceAtLeast(1)
                            actions.onAddShippedCourierOrder(
                                customerName,
                                mobileNumber,
                                address.ifBlank { "$city Main Market" },
                                city,
                                state,
                                pinCode,
                                chosenProd,
                                sizeNameOrNumber,
                                colorName,
                                qty,
                                courierPartner,
                                awbNumber,
                                expectedDeliveryDate,
                                customImageUri
                            )
                            Toast.makeText(context, "🚚 Shipped Order Added with AWB $awbNumber & SMS Alert!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        modifier = Modifier.weight(1.45f).height(46.dp).testTag("confirm_add_shipped_product_btn")
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add & Ship to Courier", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditCancelledOrderDialog(
    order: OrderItem,
    actions: SellerOrderActions,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val cancelPresets = listOf(
        "ग्राहक ने खुद ऑर्डर कैंसिल किया",
        "ग्राहक ने गलत प्रोडक्ट ऑर्डर किया",
        "गलत Size या Colour चुना गया",
        "ग्राहक का पता गलत / अधूरा है",
        "प्रोडक्ट स्टॉक में उपलब्ध नहीं है",
        "पेमेंट फेल / वेरिफिकेशन फेल",
        "विक्रेता ऑर्डर पूरा नहीं कर पाया"
    )
    var awbNumber by remember(order.orderId) { mutableStateOf(order.awbNumber) }
    var courierPartner by remember(order.orderId) { mutableStateOf(order.courierPartner.ifBlank { SELLER_COURIER_PARTNERS.first() }) }
    var customerName by remember(order.orderId) { mutableStateOf(order.customerName) }
    var mobileNumber by remember(order.orderId) { mutableStateOf(order.customerPhone) }
    var address by remember(order.orderId) { mutableStateOf(order.deliveryAddress) }
    var city by remember(order.orderId) { mutableStateOf(order.city) }
    var state by remember(order.orderId) { mutableStateOf(order.state) }
    var pinCode by remember(order.orderId) { mutableStateOf(order.pinCode) }
    var customerAgeYears by remember(order.orderId) { mutableStateOf(order.customerAgeYears.ifBlank { "26 Years" }) }
    var colorName by remember(order.orderId) { mutableStateOf(order.selectedColor) }
    var sizeNameOrNumber by remember(order.orderId) { mutableStateOf(order.selectedSize) }
    var cancelReason by remember(order.orderId) { mutableStateOf(order.cancelReason.ifBlank { cancelPresets.first() }) }
    var cancelledBy by remember(order.orderId) { mutableStateOf(order.cancelledBy.ifBlank { "Customer" }) }
    var refundStatus by remember(order.orderId) { mutableStateOf(order.refundStatus.ifBlank { "Initiated" }) }
    var refundAmountText by remember(order.orderId) { mutableStateOf(order.refundAmount.toString()) }
    var restoreStock by remember(order.orderId) { mutableStateOf(order.isStockRestored) }
    var customImageUri by remember(order.orderId) { mutableStateOf(order.customProductImageUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            customImageUri = uri.toString()
            Toast.makeText(context, "✓ Product image attached for Cancelled Record!", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFC62828))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Edit Cancelled Order #${order.orderId}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            "AWB Update, Age/Years, Photo Upload, Address & Refund Details",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("1. Product Image Upload", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFB71C1C))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFAFAFA))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppManagedImage(
                            drawableRes = order.product.imageRes,
                            customUri = customImageUri ?: order.product.customImageUri,
                            contentDescription = order.product.name,
                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))
                        )
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f).height(42.dp).testTag("edit_cancelled_upload_photo_btn")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (!customImageUri.isNullOrBlank()) "✓ Photo Updated (Change)" else "Image Upload (Product)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text("2. AWB Number Update & Courier", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1565C0))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = awbNumber,
                            onValueChange = { awbNumber = it },
                            label = { Text("AWB Number") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("edit_cancelled_awb_input")
                        )
                        OutlinedButton(
                            onClick = { awbNumber = "DLV-CAN-${(100000..999999).random()}" },
                            modifier = Modifier.height(50.dp)
                        ) {
                            Text("Gen AWB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SELLER_COURIER_PARTNERS.forEach { partner ->
                            FilterChip(
                                selected = courierPartner == partner,
                                onClick = { courierPartner = partner },
                                label = { Text(partner.substringBefore(" "), fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("3. Customer Details, Age / Years & Address", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_cancelled_cust_name")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mobileNumber,
                            onValueChange = { mobileNumber = it },
                            label = { Text("Mobile Number") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f).testTag("edit_cancelled_mobile")
                        )
                        OutlinedTextField(
                            value = customerAgeYears,
                            onValueChange = { customerAgeYears = it },
                            label = { Text("Age / Years") },
                            placeholder = { Text("e.g. 26 Years") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("edit_cancelled_age_years")
                        )
                    }
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_cancelled_address")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { pinCode = it.filter { ch -> ch.isDigit() }.take(6) },
                            label = { Text("Pincode") },
                            singleLine = true,
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = colorName, onValueChange = { colorName = it }, label = { Text("Colour Name") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = sizeNameOrNumber, onValueChange = { sizeNameOrNumber = it }, label = { Text("Size Name & Number") }, singleLine = true, modifier = Modifier.weight(1f))
                    }

                    Text("4. Cancellation Reason, Cancelled By & Refund", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFB71C1C))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cancelPresets.forEach { preset ->
                            FilterChip(
                                selected = cancelReason == preset,
                                onClick = { cancelReason = preset },
                                label = { Text(preset.take(26), fontSize = 10.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Cancellation Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Customer", "Seller", "Admin").forEach { actor ->
                            FilterChip(
                                selected = cancelledBy == actor,
                                onClick = { cancelledBy = actor },
                                label = { Text(actor, fontSize = 11.sp) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Not Applicable", "Pending Initiation", "Initiated", "Completed").forEach { st ->
                            FilterChip(
                                selected = refundStatus == st,
                                onClick = { refundStatus = st },
                                label = { Text(st, fontSize = 10.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = refundAmountText,
                        onValueChange = { refundAmountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Refund Amount (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { restoreStock = !restoreStock }
                    ) {
                        Checkbox(checked = restoreStock, onCheckedChange = { restoreStock = it })
                        Text("Stock Restored to Inventory", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(46.dp)) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val parsedRefund = refundAmountText.toIntOrNull() ?: order.refundAmount
                            actions.onEditCancelledFullDetails(
                                order.orderId,
                                awbNumber,
                                courierPartner,
                                customerName,
                                mobileNumber,
                                address,
                                city,
                                state,
                                pinCode,
                                customerAgeYears,
                                sizeNameOrNumber,
                                colorName,
                                cancelReason,
                                cancelledBy,
                                refundStatus,
                                parsedRefund,
                                restoreStock,
                                customImageUri
                            )
                            Toast.makeText(context, "✓ Cancelled Order AWB, Age & Details Updated!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        modifier = Modifier.weight(1.4f).height(46.dp).testTag("save_cancelled_full_details_btn")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Cancelled Update", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddCancelledRecordDialog(
    products: List<Product>,
    actions: SellerOrderActions,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val cancelPresets = listOf(
        "ग्राहक ने खुद ऑर्डर कैंसिल किया",
        "ग्राहक ने गलत प्रोडक्ट ऑर्डर किया",
        "गलत Size या Colour चुना गया",
        "ग्राहक का पता गलत / अधूरा है",
        "प्रोडक्ट स्टॉक में उपलब्ध नहीं है",
        "पेमेंट फेल / वेरिफिकेशन फेल",
        "विक्रेता ऑर्डर पूरा नहीं कर पाया"
    )
    val categories = remember(products) { listOf("All") + products.map { it.category }.distinct() }
    var selectedCategory by remember { mutableStateOf("All") }
    val filteredProducts = remember(products, selectedCategory) {
        if (selectedCategory == "All") products else products.filter { it.category == selectedCategory }
    }
    var selectedProduct by remember(products) { mutableStateOf(products.firstOrNull()) }

    var useCustomNewType by remember { mutableStateOf(false) }
    var customProductName by remember { mutableStateOf("") }
    var customProductCategory by remember { mutableStateOf("Kurti Collection") }
    var customPriceText by remember { mutableStateOf("599") }

    var customerName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("+91 9128754982") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Bettiah") }
    var state by remember { mutableStateOf("Bihar") }
    var pinCode by remember { mutableStateOf("845438") }
    var customerAgeYears by remember { mutableStateOf("26 Years") }
    var colorName by remember { mutableStateOf("Festive Maroon") }
    var sizeNameOrNumber by remember { mutableStateOf("38 (M)") }
    var qtyText by remember { mutableStateOf("1") }
    var courierPartner by remember { mutableStateOf(SELLER_COURIER_PARTNERS.first()) }
    var awbNumber by remember { mutableStateOf("DLV-CAN-${(100000..999999).random()}") }
    var cancelReason by remember { mutableStateOf(cancelPresets.first()) }
    var cancelledBy by remember { mutableStateOf("Customer") }
    var restoreStock by remember { mutableStateOf(true) }
    var customImageUri by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            customImageUri = uri.toString()
            Toast.makeText(context, "✓ Product image attached!", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFB71C1C))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Add Cancelled Order Record",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "Customer, Address, Age/Years, Colour, Size, Photo & AWB Update",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("1. Select Dress / Product", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFB71C1C))
                        FilterChip(
                            selected = useCustomNewType,
                            onClick = { useCustomNewType = !useCustomNewType },
                            label = { Text(if (useCustomNewType) "✓ Custom Product" else "+ Custom Type", fontSize = 11.sp) }
                        )
                    }

                    if (useCustomNewType) {
                        OutlinedTextField(
                            value = customProductName,
                            onValueChange = { customProductName = it },
                            label = { Text("Product Name") },
                            placeholder = { Text("e.g. Floral Kurti Set") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customProductCategory,
                                onValueChange = { customProductCategory = it },
                                label = { Text("Category") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = customPriceText,
                                onValueChange = { customPriceText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Price (₹)") },
                                singleLine = true,
                                modifier = Modifier.weight(0.7f)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = {
                                        selectedCategory = cat
                                        val firstMatch = if (cat == "All") products.firstOrNull() else products.firstOrNull { it.category == cat }
                                        if (firstMatch != null) selectedProduct = firstMatch
                                    },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredProducts.forEach { prod ->
                                val isSel = selectedProduct?.id == prod.id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) Color(0xFFFFEBEE) else Color(0xFFF5F5F5),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSel) 2.dp else 1.dp,
                                        color = if (isSel) Color(0xFFC62828) else Color(0xFFE0E0E0)
                                    ),
                                    modifier = Modifier
                                        .width(150.dp)
                                        .clickable { selectedProduct = prod }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        AppManagedImage(
                                            drawableRes = prod.imageRes,
                                            customUri = prod.customImageUri,
                                            contentDescription = prod.name,
                                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp))
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(prod.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text("${prod.category} • ₹${prod.price}", fontSize = 9.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text("2. Image Upload (Product Photo)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8F9FA))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val activeProd = selectedProduct ?: products.firstOrNull()
                        if (activeProd != null) {
                            AppManagedImage(
                                drawableRes = activeProd.imageRes,
                                customUri = customImageUri ?: activeProd.customImageUri,
                                contentDescription = activeProd.name,
                                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(8.dp))
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f).height(42.dp).testTag("add_cancelled_upload_photo_btn")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (customImageUri != null) "✓ Photo Attached (Change)" else "Image Upload (Product)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text("3. Customer, Age/Years, Address, Colour, Size & AWB", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        placeholder = { Text("e.g. Ritu Sharma") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_cancelled_cust_name")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mobileNumber,
                            onValueChange = { mobileNumber = it },
                            label = { Text("Mobile Number") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f).testTag("add_cancelled_mobile")
                        )
                        OutlinedTextField(
                            value = customerAgeYears,
                            onValueChange = { customerAgeYears = it },
                            label = { Text("Age / Years") },
                            placeholder = { Text("e.g. 24 Years") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("add_cancelled_age_years")
                        )
                    }
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        placeholder = { Text("e.g. Station Road, Ward 5") },
                        modifier = Modifier.fillMaxWidth().testTag("add_cancelled_address")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { pinCode = it.filter { ch -> ch.isDigit() }.take(6) },
                            label = { Text("Pincode") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = qtyText,
                            onValueChange = { qtyText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Qty") },
                            singleLine = true,
                            modifier = Modifier.weight(0.6f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = colorName, onValueChange = { colorName = it }, label = { Text("Colour Name") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = sizeNameOrNumber, onValueChange = { sizeNameOrNumber = it }, label = { Text("Size Name & Number") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(
                        value = awbNumber,
                        onValueChange = { awbNumber = it },
                        label = { Text("AWB Number Update") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_cancelled_awb_input")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SELLER_COURIER_PARTNERS.forEach { partner ->
                            FilterChip(
                                selected = courierPartner == partner,
                                onClick = { courierPartner = partner },
                                label = { Text(partner.substringBefore(" "), fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("4. Cancellation Reason & Attribution", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFB71C1C))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cancelPresets.forEach { preset ->
                            FilterChip(
                                selected = cancelReason == preset,
                                onClick = { cancelReason = preset },
                                label = { Text(preset.take(26), fontSize = 10.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Cancel Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Customer", "Seller", "Admin").forEach { actor ->
                            FilterChip(
                                selected = cancelledBy == actor,
                                onClick = { cancelledBy = actor },
                                label = { Text(actor, fontSize = 11.sp) }
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { restoreStock = !restoreStock }
                    ) {
                        Checkbox(checked = restoreStock, onCheckedChange = { restoreStock = it })
                        Text("Restore Product Stock", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(46.dp)) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val baseProd = selectedProduct ?: products.firstOrNull() ?: return@Button
                            val chosenProd = if (useCustomNewType && customProductName.isNotBlank()) {
                                val parsedPrice = (customPriceText.toIntOrNull() ?: baseProd.price).coerceAtLeast(1)
                                baseProd.copy(
                                    id = "can_custom_${System.currentTimeMillis() % 10000}",
                                    name = customProductName.trim(),
                                    category = customProductCategory.trim().ifBlank { "Custom Dress" },
                                    price = parsedPrice,
                                    originalPrice = parsedPrice + 300,
                                    customImageUri = customImageUri ?: baseProd.customImageUri
                                )
                            } else {
                                baseProd
                            }
                            val qty = (qtyText.toIntOrNull() ?: 1).coerceAtLeast(1)
                            actions.onAddCancelledRecordOrder(
                                customerName,
                                mobileNumber,
                                address.ifBlank { "$city Main Road" },
                                city,
                                state,
                                pinCode,
                                customerAgeYears,
                                chosenProd,
                                sizeNameOrNumber,
                                colorName,
                                qty,
                                awbNumber,
                                courierPartner,
                                cancelReason,
                                cancelledBy,
                                restoreStock,
                                customImageUri
                            )
                            Toast.makeText(context, "❌ Cancelled Record Saved (Age: $customerAgeYears, AWB: $awbNumber)!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                        modifier = Modifier.weight(1.45f).height(46.dp).testTag("confirm_add_cancelled_record_btn")
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Cancelled Order", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditReadyToShipDetailsDialog(
    order: OrderItem,
    actions: SellerOrderActions,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var courierPartner by remember(order.orderId) { mutableStateOf(order.courierPartner.ifBlank { SELLER_COURIER_PARTNERS.first() }) }
    var awbNumber by remember(order.orderId) { mutableStateOf(order.awbNumber) }
    var pickupStatus by remember(order.orderId) { mutableStateOf(order.pickupStatus.ifBlank { "Not Requested" }) }
    var shippingChargesText by remember(order.orderId) { mutableStateOf(order.shippingCharges.toString()) }
    var expectedDeliveryDate by remember(order.orderId) { mutableStateOf(order.expectedDeliveryDate.ifBlank { "21 Nov 2026" }) }
    var customerName by remember(order.orderId) { mutableStateOf(order.customerName) }
    var phone by remember(order.orderId) { mutableStateOf(order.customerPhone) }
    var address by remember(order.orderId) { mutableStateOf(order.deliveryAddress) }
    var city by remember(order.orderId) { mutableStateOf(order.city) }
    var state by remember(order.orderId) { mutableStateOf(order.state) }
    var pinCode by remember(order.orderId) { mutableStateOf(order.pinCode) }
    var size by remember(order.orderId) { mutableStateOf(order.selectedSize) }
    var color by remember(order.orderId) { mutableStateOf(order.selectedColor) }
    var customImageUri by remember(order.orderId) { mutableStateOf(order.customProductImageUri) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            customImageUri = uri.toString()
            Toast.makeText(context, "✓ Product Image Selected", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2E7D32))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ready to Ship • Courier & Charges", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Order #${order.orderId} • ${order.product.name}", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp, maxLines = 1)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Courier Partner Selection", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SELLER_COURIER_PARTNERS.forEach { cp ->
                            FilterChip(
                                selected = courierPartner == cp,
                                onClick = { courierPartner = cp },
                                label = { Text(cp.substringBefore(" "), fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = awbNumber,
                            onValueChange = { awbNumber = it },
                            label = { Text("AWB Number") },
                            singleLine = true,
                            modifier = Modifier.weight(1.4f).testTag("edit_ready_awb_input")
                        )
                        OutlinedButton(
                            onClick = {
                                val prefix = when {
                                    courierPartner.contains("Blue", true) -> "BLUD"
                                    courierPartner.contains("Ekart", true) -> "EKRT"
                                    courierPartner.contains("Xpress", true) -> "XPRS"
                                    courierPartner.contains("Shadow", true) -> "SHDW"
                                    else -> "DLV"
                                }
                                awbNumber = "$prefix-${(10000000..99999999).random()}"
                            },
                            modifier = Modifier.weight(0.8f).height(50.dp)
                        ) {
                            Text("Gen AWB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("Pickup Status", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Not Requested", "Pickup Requested", "Courier Arriving Today", "Handed to Courier").forEach { statusOpt ->
                            FilterChip(
                                selected = pickupStatus == statusOpt,
                                onClick = { pickupStatus = statusOpt },
                                label = { Text(statusOpt, fontSize = 10.sp) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = shippingChargesText,
                            onValueChange = { shippingChargesText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Shipping Charges (₹)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("edit_ready_charges_input")
                        )
                        OutlinedTextField(
                            value = expectedDeliveryDate,
                            onValueChange = { expectedDeliveryDate = it },
                            label = { Text("Delivery Date") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("edit_ready_eta_input")
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        AppManagedImage(
                            drawableRes = order.product.imageRes,
                            customUri = customImageUri ?: order.product.customImageUri,
                            contentDescription = order.product.name,
                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        )
                        OutlinedButton(
                            onClick = {
                                imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload / Change Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Customer Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = pinCode, onValueChange = { pinCode = it.filter { ch -> ch.isDigit() }.take(6) }, label = { Text("Pin") }, singleLine = true, modifier = Modifier.weight(0.9f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Colour") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = size, onValueChange = { size = it }, label = { Text("Size") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(44.dp)) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val charges = (shippingChargesText.toIntOrNull() ?: order.shippingCharges).coerceAtLeast(0)
                            actions.onUpdateReadyToShipDetails(
                                order.orderId,
                                courierPartner,
                                awbNumber,
                                pickupStatus,
                                charges,
                                expectedDeliveryDate,
                                customerName,
                                phone,
                                address,
                                city,
                                state,
                                pinCode,
                                size,
                                color,
                                customImageUri
                            )
                            Toast.makeText(context, "✓ Ready to Ship details updated!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.weight(1.4f).height(44.dp).testTag("save_ready_details_btn")
                    ) {
                        Text("Save Ready-to-Ship", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddReadyToShipProductDialog(
    products: List<Product>,
    actions: SellerOrderActions,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var useCustomNewType by remember { mutableStateOf(false) }
    var customProductName by remember { mutableStateOf("") }
    var customProductCategory by remember { mutableStateOf("Ready Kurti") }
    var customPriceText by remember { mutableStateOf("699") }
    var customImageUri by remember { mutableStateOf<String?>(null) }

    var customerName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("+91 9835012345") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Bettiah") }
    var state by remember { mutableStateOf("Bihar") }
    var pinCode by remember { mutableStateOf("845438") }
    var colorName by remember { mutableStateOf("Peacock Blue") }
    var sizeNameOrNumber by remember { mutableStateOf("38 (M)") }
    var qtyText by remember { mutableStateOf("1") }
    var courierPartner by remember { mutableStateOf(SELLER_COURIER_PARTNERS.first()) }
    var awbNumber by remember { mutableStateOf("DLV-${(10000000..99999999).random()}") }
    var shippingChargesText by remember { mutableStateOf("49") }
    var expectedDeliveryDate by remember { mutableStateOf("22 Nov 2026") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            customImageUri = uri.toString()
            Toast.makeText(context, "✓ Product Image Selected for Ready PDF!", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2E7D32))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Add Ready to Ship Order", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Confirmed, Packed, Invoice + Label + AWB Ready", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !useCustomNewType,
                            onClick = { useCustomNewType = false },
                            label = { Text("Catalog Dress", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = useCustomNewType,
                            onClick = { useCustomNewType = true },
                            label = { Text("New Type Dress", fontSize = 11.sp) }
                        )
                    }

                    if (!useCustomNewType) {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            products.take(6).forEach { prod ->
                                FilterChip(
                                    selected = selectedProduct?.id == prod.id,
                                    onClick = { selectedProduct = prod },
                                    label = { Text("${prod.name.take(16)} (₹${prod.price})", fontSize = 11.sp) }
                                )
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = customProductName,
                            onValueChange = { customProductName = it },
                            label = { Text("Product Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customProductCategory,
                                onValueChange = { customProductCategory = it },
                                label = { Text("Category") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = customPriceText,
                                onValueChange = { customPriceText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Price (₹)") },
                                singleLine = true,
                                modifier = Modifier.weight(0.75f)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (customImageUri != null) "✓ Image Uploaded" else "Upload Product Image", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Customer Name") }, singleLine = true, modifier = Modifier.weight(1f).testTag("add_ready_cust_input"))
                        OutlinedTextField(value = mobileNumber, onValueChange = { mobileNumber = it }, label = { Text("Mobile") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = pinCode, onValueChange = { pinCode = it.filter { ch -> ch.isDigit() }.take(6) }, label = { Text("Pin") }, singleLine = true, modifier = Modifier.weight(0.9f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = colorName, onValueChange = { colorName = it }, label = { Text("Colour") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = sizeNameOrNumber, onValueChange = { sizeNameOrNumber = it }, label = { Text("Size") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = qtyText, onValueChange = { qtyText = it.filter { ch -> ch.isDigit() } }, label = { Text("Qty") }, singleLine = true, modifier = Modifier.weight(0.6f))
                    }

                    Text("Courier Partner", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SELLER_COURIER_PARTNERS.forEach { cp ->
                            FilterChip(
                                selected = courierPartner == cp,
                                onClick = { courierPartner = cp },
                                label = { Text(cp.substringBefore(" "), fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = awbNumber, onValueChange = { awbNumber = it }, label = { Text("AWB Number") }, singleLine = true, modifier = Modifier.weight(1.2f))
                        OutlinedTextField(value = shippingChargesText, onValueChange = { shippingChargesText = it.filter { ch -> ch.isDigit() } }, label = { Text("Charges ₹") }, singleLine = true, modifier = Modifier.weight(0.8f))
                        OutlinedTextField(value = expectedDeliveryDate, onValueChange = { expectedDeliveryDate = it }, label = { Text("Delivery Date") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(44.dp)) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val baseProd = selectedProduct ?: products.firstOrNull() ?: return@Button
                            val chosenProd = if (useCustomNewType && customProductName.isNotBlank()) {
                                val parsedPrice = (customPriceText.toIntOrNull() ?: baseProd.price).coerceAtLeast(1)
                                baseProd.copy(
                                    id = "rts_custom_${System.currentTimeMillis() % 10000}",
                                    name = customProductName.trim(),
                                    category = customProductCategory.trim().ifBlank { "Ready Dress" },
                                    price = parsedPrice,
                                    originalPrice = parsedPrice + 250,
                                    customImageUri = customImageUri ?: baseProd.customImageUri
                                )
                            } else {
                                baseProd
                            }
                            val qty = (qtyText.toIntOrNull() ?: 1).coerceAtLeast(1)
                            val charges = (shippingChargesText.toIntOrNull() ?: 49).coerceAtLeast(0)
                            actions.onAddReadyToShipProductOrder(
                                customerName,
                                mobileNumber,
                                address.ifBlank { "$city Main Road" },
                                city,
                                state,
                                pinCode,
                                chosenProd,
                                sizeNameOrNumber,
                                colorName,
                                qty,
                                courierPartner,
                                awbNumber,
                                charges,
                                expectedDeliveryDate,
                                customImageUri
                            )
                            Toast.makeText(context, "📦 Ready to Ship Order Saved (AWB: $awbNumber)!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.weight(1.45f).height(44.dp).testTag("confirm_add_ready_btn")
                    ) {
                        Text("Save Ready to Ship", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}



