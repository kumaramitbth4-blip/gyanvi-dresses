package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.OrderItem
import com.example.ui.theme.*
import com.google.mlkit.vision.barcode.common.Barcode

data class ParsedQrAwbPayload(
    val rawValue: String,
    val codeKind: String,
    val extractedOrderId: String,
    val extractedAwb: String
)

object QrAwbPayloadMatcher {
    private val jsonOrderRegex = Regex(""""(?:orderId|ord)"\s*:\s*"([^"]*)"""", RegexOption.IGNORE_CASE)
    private val jsonAwbRegex = Regex(""""(?:awbNumber|awb)"\s*:\s*"([^"]*)"""", RegexOption.IGNORE_CASE)
    private val orderRegex = Regex("""(?i)(?:ORD|ORDERID|ORDER)[=:\s]+([A-Za-z0-9-]+)""")
    private val awbRegex = Regex("""(?i)(?:AWB|AWBNUMBER|TRACKING)[=:\s]+([A-Za-z0-9-]+)""")

    fun parseScannedPayload(raw: String, format: Int = Barcode.FORMAT_UNKNOWN): ParsedQrAwbPayload {
        val trimmed = raw.trim()
        val isQr = format == Barcode.FORMAT_QR_CODE ||
            trimmed.startsWith("GYANVI", ignoreCase = true) ||
            trimmed.startsWith("{")
        val codeKind = if (isQr) "QR Code" else "AWB Barcode"

        var orderId = ""
        var awb = ""

        if (trimmed.startsWith("{")) {
            jsonOrderRegex.find(trimmed)?.groupValues?.getOrNull(1)?.let { orderId = it.trim() }
            jsonAwbRegex.find(trimmed)?.groupValues?.getOrNull(1)?.let { awb = it.trim() }
        }

        if (orderId.isBlank() && trimmed.contains("|")) {
            trimmed.split("|").forEach { segment ->
                val parts = segment.split("=", limit = 2)
                if (parts.size == 2) {
                    when (parts[0].trim().uppercase()) {
                        "ORD", "ORDER", "ORDERID" -> orderId = parts[1].trim()
                        "AWB", "AWBNUMBER" -> awb = parts[1].trim()
                    }
                }
            }
        }

        if (orderId.isBlank()) {
            orderRegex.find(trimmed)?.groupValues?.getOrNull(1)?.let { orderId = it.trim() }
        }
        if (awb.isBlank()) {
            awbRegex.find(trimmed)?.groupValues?.getOrNull(1)?.let { awb = it.trim() }
        }

        if (!isQr && awb.isBlank() && trimmed.isNotBlank()) {
            awb = trimmed.removeSurrounding("*")
        }

        return ParsedQrAwbPayload(
            rawValue = trimmed,
            codeKind = codeKind,
            extractedOrderId = orderId,
            extractedAwb = awb
        )
    }

    fun findMatchingOrder(orders: List<OrderItem>, raw: String): OrderItem? {
        val q = raw.trim()
        if (q.isBlank()) return null
        val parsed = parseScannedPayload(q)
        return orders.firstOrNull { order ->
            (parsed.extractedOrderId.isNotBlank() && order.orderId.equals(parsed.extractedOrderId, ignoreCase = true)) ||
                (parsed.extractedAwb.isNotBlank() && order.awbNumber.equals(parsed.extractedAwb, ignoreCase = true)) ||
                order.awbNumber.equals(q, ignoreCase = true) ||
                order.orderId.equals(q, ignoreCase = true) ||
                (order.orderId.isNotBlank() && q.contains(order.orderId, ignoreCase = true)) ||
                (order.awbNumber.isNotBlank() && q.contains(order.awbNumber, ignoreCase = true))
        }
    }
}

@Composable
fun QrAwbCameraScannerDialog(
    orders: List<OrderItem> = emptyList(),
    initialAwbHint: String = "",
    onApplyScannedCode: (scanned: String, matchedOrder: OrderItem?) -> Unit,
    onVerifyScannedOrder: (matchedOrder: OrderItem, scanned: String) -> Unit = { _, _ -> },
    onMarkReadyToShip: (matchedOrder: OrderItem) -> Unit = {},
    onDismiss: () -> Unit
) {
    var codeInput by remember(initialAwbHint) {
        mutableStateOf(initialAwbHint.ifBlank { orders.firstOrNull()?.awbNumber.orEmpty() })
    }

    val matchedOrder = remember(codeInput, orders) {
        QrAwbPayloadMatcher.findMatchingOrder(orders, codeInput)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = MeeshoMagenta)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AWB / QR Barcode Scanner", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.5.dp, MeeshoMagenta, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.QrCode2, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Enter or Tap AWB / Order QR Code", color = Color(0xFFE2E8F0), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it },
                    label = { Text("AWB / Order ID / Packet Barcode") },
                    placeholder = { Text("e.g. DLV-849201948 or OD9827461947") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (orders.isNotEmpty()) {
                    Text("Quick Select Active AWB:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    orders.take(3).forEach { ord ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .clickable { codeInput = ord.awbNumber.ifBlank { ord.orderId } }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("#${ord.orderId} • ${ord.customerName}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    Text("AWB: ${ord.awbNumber} (${ord.sellerStage.ifBlank { ord.status.label }})", fontSize = 10.5.sp, color = TextSecondary)
                                }
                                Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                if (matchedOrder != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFDCFCE7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Matched Order #${matchedOrder.orderId}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF166534))
                            Text("${matchedOrder.product.name} • ${matchedOrder.customerName}", fontSize = 11.sp, color = Color(0xFF15803D))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            onApplyScannedCode(codeInput.trim(), matchedOrder)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply Filter", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                    if (matchedOrder != null) {
                        Button(
                            onClick = {
                                onVerifyScannedOrder(matchedOrder, codeInput.trim())
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Verify Scan", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (matchedOrder != null) {
                    Button(
                        onClick = {
                            onMarkReadyToShip(matchedOrder)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark Ready to Ship", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
