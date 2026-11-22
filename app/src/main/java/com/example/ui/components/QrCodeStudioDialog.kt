package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CardWhite
import com.example.ui.theme.MeeshoMagenta
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ImageSaver
import com.example.util.QrCodeGenerator

data class ShippingLabelQrMetadata(
    val orderId: String,
    val awbNumber: String,
    val customerName: String,
    val destination: String,
    val productTitle: String,
    val sku: String,
    val size: String,
    val amount: Int,
    val paymentMethod: String,
    val packerName: String = "Gyanvi Dresses Packaging & Logistics Hub",
    val packerId: String = "PKR-GD-845438"
)

/**
 * Interactive QR Code & Shipping Label Studio Dialog.
 * Supports generating live QR Codes, switching to 4x6 Shipping Label view (if metadata provided),
 * Downloading to Downloads/GyanviDresses, Saving directly to Gallery (Pictures/GyanviDresses),
 * and Sharing via Android Share Sheet.
 */
@Composable
fun QrCodeStudioDialog(
    initialContent: String,
    headerTitle: String = "GYANVI DRESSES QR CODE",
    footerSubtitle: String = initialContent.take(36),
    fileNamePrefix: String = "Gyanvi_QR",
    labelMetadata: ShippingLabelQrMetadata? = null,
    onSavedToGalleryOrDownloaded: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var qrContentInput by remember(initialContent) { mutableStateOf(initialContent) }
    var activeQrPayload by remember(initialContent) { mutableStateOf(initialContent) }
    var showShippingLabelMode by remember(labelMetadata) { mutableStateOf(labelMetadata != null) }
    var savedBannerMessage by remember { mutableStateOf<String?>(null) }

    val qrBitmap: Bitmap = remember(activeQrPayload, headerTitle, footerSubtitle) {
        QrCodeGenerator.generateQrBitmap(
            content = activeQrPayload,
            sizePx = 720,
            headerTitle = headerTitle,
            footerSubtitle = activeQrPayload.take(36)
        )
    }

    val shippingLabelBitmap: Bitmap? = remember(labelMetadata) {
        labelMetadata?.let { meta ->
            QrCodeGenerator.generateShippingLabelBitmap(
                orderId = meta.orderId,
                awbNumber = meta.awbNumber,
                customerName = meta.customerName,
                destination = meta.destination,
                productTitle = meta.productTitle,
                sku = meta.sku,
                size = meta.size,
                amount = meta.amount,
                paymentMethod = meta.paymentMethod,
                packerName = meta.packerName,
                packerId = meta.packerId
            )
        }
    }

    val activeBitmap = if (showShippingLabelMode && shippingLabelBitmap != null) {
        shippingLabelBitmap
    } else {
        qrBitmap
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF3E5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, tint = MeeshoMagenta)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "QR Code & Label Studio",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                "Generate • Download • Save to Gallery",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("qr_dialog_close_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                }

                // Mode toggle if order shipping label metadata is present
                if (shippingLabelBitmap != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = showShippingLabelMode,
                            onClick = { showShippingLabelMode = true },
                            label = { Text("Shipping Label + QR", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !showShippingLabelMode,
                            onClick = { showShippingLabelMode = false },
                            label = { Text("Pure QR Code", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Live Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFAFAFA))
                        .border(1.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = activeBitmap.asImageBitmap(),
                        contentDescription = "Generated QR Code Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = if (showShippingLabelMode) 310.dp else 250.dp)
                            .testTag("qr_preview_image")
                    )
                }

                // Custom QR Text Input + Generate button (when in Pure QR mode or standalone)
                if (!showShippingLabelMode) {
                    OutlinedTextField(
                        value = qrContentInput,
                        onValueChange = { qrContentInput = it },
                        label = { Text("QR Text / Order ID / UPI / Link") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("qr_payload_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val trimmed = qrContentInput.trim()
                                if (trimmed.isNotEmpty()) {
                                    activeQrPayload = trimmed
                                    savedBannerMessage = "✓ Fresh QR generated for: ${trimmed.take(28)}"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("qr_generate_btn")
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate QR", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Quick Preset Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                val code = "GYANVI-ORD-${(10000..99999).random()}"
                                qrContentInput = code
                                activeQrPayload = code
                            },
                            label = { Text("Order QR", fontSize = 11.sp) }
                        )
                        AssistChip(
                            onClick = {
                                val upi = "upi://pay?pa=gyanvidresses@okicici&pn=Gyanvi%20Dresses&cu=INR"
                                qrContentInput = upi
                                activeQrPayload = upi
                            },
                            label = { Text("UPI Pay QR", fontSize = 11.sp) }
                        )
                        AssistChip(
                            onClick = {
                                val pkt = "GYN-PKT-${(1000..9999).random()}"
                                qrContentInput = pkt
                                activeQrPayload = pkt
                            },
                            label = { Text("Packet QR", fontSize = 11.sp) }
                        )
                    }
                } else {
                    Text(
                        text = "Payload: $activeQrPayload",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }

                // Confirmation Banner
                if (!savedBannerMessage.isNullOrBlank()) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                savedBannerMessage.orEmpty(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }
                }

                // Primary Save / Download / Share Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val res = ImageSaver.saveBitmapToGallery(
                                context = context,
                                bitmap = activeBitmap,
                                fileNamePrefix = if (showShippingLabelMode) "${fileNamePrefix}_Label" else "${fileNamePrefix}_QR",
                                showToast = true
                            )
                            savedBannerMessage = res.message
                            if (res.success) onSavedToGalleryOrDownloaded?.invoke()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("qr_save_gallery_btn")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Gallery", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val res = ImageSaver.saveBitmapToDownloads(
                                context = context,
                                bitmap = activeBitmap,
                                fileNamePrefix = if (showShippingLabelMode) "${fileNamePrefix}_Label" else "${fileNamePrefix}_QR",
                                showToast = true
                            )
                            savedBannerMessage = res.message
                            if (res.success) onSavedToGalleryOrDownloaded?.invoke()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("qr_download_png_btn")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = {
                        ImageSaver.shareBitmapImage(
                            context = context,
                            bitmap = activeBitmap,
                            fileNamePrefix = "${fileNamePrefix}_Share",
                            caption = "Gyanvi Dresses QR Code / Shipping Label\n$activeQrPayload"
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("qr_share_image_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share QR Image (WhatsApp / Others)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
