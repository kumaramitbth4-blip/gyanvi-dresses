package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderItem
import com.example.ui.theme.*
import com.example.util.HoldOrderPdfGenerator

/**
 * Renders the active Seller Order Stage (On Hold, Pending, Ready to Ship, Shipped, Cancelled)
 * directly in PDF format ("PDF ke roop me") right inside the Seller Orders screen.
 */
@Composable
fun StageInlinePdfViewerSection(
    stageName: String,
    stageTabIndex: Int,
    ordersInStage: List<OrderItem>,
    reportTitle: String,
    fileNamePrefix: String,
    displayMode: Int, // 0 = PDF + Cards, 1 = Sirf PDF Roop, 2 = Sirf Cards
    onChangeDisplayMode: (Int) -> Unit,
    onOpenFullPdfDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = when (stageTabIndex) {
        0 -> Color(0xFFE65100)
        1 -> Color(0xFF1565C0)
        2 -> Color(0xFF2E7D32)
        3 -> Color(0xFF0277BD)
        4 -> Color(0xFFC62828)
        else -> Color(0xFF1A237E)
    }

    val renderedPages = remember(ordersInStage, reportTitle, displayMode) {
        if (displayMode == 2 || ordersInStage.isEmpty()) emptyList()
        else HoldOrderPdfGenerator.renderPdfPagesToBitmaps(context, ordersInStage, reportTitle)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .testTag("stage_inline_pdf_section_$stageTabIndex")
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "📄 $stageName Orders — PDF Roop (${ordersInStage.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (ordersInStage.isEmpty()) "Is tab me koi order nahi hai"
                        else "Customer Name, Photo, Size, Colour, Address & Barcode PDF me",
                        fontSize = 10.5.sp,
                        color = TextSecondary
                    )
                }
                OutlinedButton(
                    onClick = onOpenFullPdfDialog,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp).testTag("stage_inline_open_full_pdf_btn_$stageTabIndex")
                ) {
                    Icon(Icons.Default.OpenInFull, contentDescription = null, modifier = Modifier.size(13.dp), tint = accentColor)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Full PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentColor)
                }
            }

            // View Mode Switcher: PDF + Cards | Sirf PDF Roop | Sirf Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val modes = listOf(
                    0 to "📄 PDF + Cards",
                    1 to "📄 Sirf PDF Roop",
                    2 to "📋 Sirf Cards"
                )
                modes.forEach { (modeIdx, label) ->
                    val isSelected = displayMode == modeIdx
                    Button(
                        onClick = { onChangeDisplayMode(modeIdx) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) accentColor else Color(0xFFF1F5F9),
                            contentColor = if (isSelected) Color.White else TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("stage_pdf_mode_btn_$modeIdx")
                    ) {
                        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }

            if (displayMode != 2 && ordersInStage.isNotEmpty() && renderedPages.isNotEmpty()) {
                val pagesToShow = if (displayMode == 1) renderedPages else renderedPages.take(1)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pagesToShow.forEachIndexed { pageIdx, pageBitmap ->
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 2.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCFD8DC)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("inline_pdf_page_${stageTabIndex}_${pageIdx + 1}")
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC))
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "$stageName A4 PDF • Page ${pageIdx + 1}/${renderedPages.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF334155)
                                    )
                                    Text(
                                        "${ordersInStage.size} Product(s) • QR + Barcode",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                Image(
                                    bitmap = pageBitmap.asImageBitmap(),
                                    contentDescription = "$stageName Order PDF Page ${pageIdx + 1}",
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(595f / 842f)
                                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                                )
                            }
                        }
                    }

                    if (displayMode == 0 && renderedPages.size > 1) {
                        TextButton(
                            onClick = { onChangeDisplayMode(1) },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                "📄 Show All ${renderedPages.size} PDF Pages →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }

                    // 1-Tap Download / Share / Open PDF Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                val res = HoldOrderPdfGenerator.saveHoldOrdersPdfToDownloads(
                                    context = context,
                                    orders = ordersInStage,
                                    fileNamePrefix = fileNamePrefix,
                                    reportTitle = reportTitle,
                                    showToast = false
                                )
                                Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("inline_download_pdf_btn_$stageTabIndex")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                HoldOrderPdfGenerator.openOrShareHoldOrdersPdf(
                                    context = context,
                                    orders = ordersInStage,
                                    fileNamePrefix = fileNamePrefix,
                                    reportTitle = reportTitle,
                                    preferExternalView = false
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("inline_share_pdf_btn_$stageTabIndex")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp), tint = accentColor)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        }
                    }
                }
            }
        }
    }
}
