package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.PaymentGatewayLinkItem
import com.example.model.PaymentGatewayProviderPreset
import com.example.model.PaymentGatewaySettingsConfig
import com.example.ui.components.PaymentGatewayConnectorSection
import com.example.ui.theme.*

enum class PaymentsSubView {
    MAIN,
    NEXT_7_DAYS,
    UNSCHEDULED,
    COMPENSATION_RECOVERIES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerPaymentsScreen(
    onBack: () -> Unit,
    onNavigateToHelp: () -> Unit,
    paymentGatewayConfig: PaymentGatewaySettingsConfig = LocalAppConfig.current.paymentGateways,
    onSaveAndConnectPaymentGateway: (id: String?, name: String, url: String, provider: PaymentGatewayProviderPreset, lastStatus: String, connectNow: Boolean) -> Result<PaymentGatewayLinkItem> = { id, name, url, provider, status, _ ->
        Result.success(PaymentGatewayLinkItem(id ?: "pg-seller", name, url, provider, true, status))
    },
    onConnectPaymentGateway: (String) -> Unit = {},
    onDisconnectPaymentGateway: () -> Unit = {},
    onDeletePaymentGateway: (String) -> Unit = {},
    onToggleAppendPaymentParams: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var currentSubView by remember { mutableStateOf(PaymentsSubView.MAIN) }
    var showDownloadSheet by remember { mutableStateOf(false) }

    when (currentSubView) {
        PaymentsSubView.NEXT_7_DAYS -> {
            Next7DaysPaymentsScreen(onBack = { currentSubView = PaymentsSubView.MAIN })
        }
        PaymentsSubView.UNSCHEDULED -> {
            UnscheduledPaymentsScreen(onBack = { currentSubView = PaymentsSubView.MAIN })
        }
        PaymentsSubView.COMPENSATION_RECOVERIES -> {
            CompensationRecoveriesScreen(onBack = { currentSubView = PaymentsSubView.MAIN })
        }
        PaymentsSubView.MAIN -> {
            // Main Payments Screen
            var upcomingExpanded by remember { mutableStateOf(true) }
            var completedExpanded by remember { mutableStateOf(true) }
            var selectedCompletedDateIndex by remember { mutableIntStateOf(0) }

            val completedDates = listOf(
                Triple("13 Apr, 26", "₹626.45", "AXISCN1310350665"),
                Triple("6 Apr, 26", "₹104.95", "AXISCN1310311204"),
                Triple("2 Apr, 26", "₹761.33", "AXISCN1310289041")
            )

            Scaffold(
                topBar = {
                    Surface(color = CardWhite, shadowElevation = 1.dp) {
                        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = onBack) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                    Text("Payments", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { Toast.makeText(context, "Search payments", Toast.LENGTH_SHORT).show() }) {
                                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                                    }
                                    IconButton(onClick = onNavigateToHelp) {
                                        Icon(Icons.Default.HelpOutline, contentDescription = "Help", tint = TextPrimary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                        }
                    }
                },
                containerColor = Color(0xFFF9F9F9)
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Download Purple Button
                    Button(
                        onClick = { showDownloadSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283593)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .height(46.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download ▾", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        PaymentGatewayConnectorSection(
                            config = paymentGatewayConfig,
                            onSaveAndConnect = onSaveAndConnectPaymentGateway,
                            onConnectGateway = onConnectPaymentGateway,
                            onDisconnectGateway = onDisconnectPaymentGateway,
                            onDeleteGateway = onDeletePaymentGateway,
                            onToggleAppendParams = onToggleAppendPaymentParams,
                            sectionTitle = "Seller Payment Gateway URL 🔗",
                            sectionSubtitle = "Connect Razorpay, Cashfree, PhonePe PG, or custom payout/collection URL"
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Upcoming Payments Section
                    Card(
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { upcomingExpanded = !upcomingExpanded },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFF8E1)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🕒", fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Upcoming Payments", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        Text(
                                            "Next 7 days (₹0.0) >",
                                            fontSize = 12.sp,
                                            color = Color(0xFF1A237E),
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.clickable { currentSubView = PaymentsSubView.NEXT_7_DAYS }
                                        )
                                    }
                                }
                                Icon(
                                    if (upcomingExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }

                            AnimatedVisibility(visible = upcomingExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp, bottom = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .border(1.dp, Color(0xFFB0BEC5), RoundedCornerShape(4.dp))
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("₹ ₹", fontSize = 20.sp, color = Color(0xFF78909C), fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("No upcoming payments to show", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Your upcoming payouts will appear here once orders are delivered", fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Completed Payments Section (Screenshots 10:45)
                    Card(
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { completedExpanded = !completedExpanded },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE8F5E9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Completed Payments", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        Text("Last 30 days (₹1.79K) >", fontSize = 12.sp, color = Color(0xFF1A237E), fontWeight = FontWeight.Medium)
                                    }
                                }
                                Icon(
                                    if (completedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }

                            AnimatedVisibility(visible = completedExpanded) {
                                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                                    // Date Pills Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        completedDates.forEachIndexed { idx, (date, amt, _) ->
                                            val isSel = selectedCompletedDateIndex == idx
                                            Card(
                                                shape = RoundedCornerShape(8.dp),
                                                colors = CardDefaults.cardColors(containerColor = if (isSel) Color(0xFFEDE7F6) else Color(0xFFF5F5F5)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { selectedCompletedDateIndex = idx }
                                                    .border(1.dp, if (isSel) Color(0xFF3F51B5) else Color.Transparent, RoundedCornerShape(8.dp))
                                            ) {
                                                Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(date, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) Color(0xFF1A237E) else TextSecondary)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(amt, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    val currentPayment = completedDates[selectedCompletedDateIndex]

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Order amount", fontSize = 13.sp, color = TextPrimary)
                                        Text(currentPayment.second, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1A237E))
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Net platform recovery", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                                    Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Ads costs", fontSize = 12.sp, color = TextMuted)
                                        Text("₹0", fontSize = 12.sp)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Program costs", fontSize = 12.sp, color = TextMuted)
                                        Text("₹0", fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Net platform compensation", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                                    Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Program benefits", fontSize = 12.sp, color = TextMuted)
                                        Text("₹0", fontSize = 12.sp)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Referral bonus", fontSize = 12.sp, color = TextMuted)
                                        Text("₹0", fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = Color(0xFFEEEEEE))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text("Net amount", fontSize = 12.sp, color = TextSecondary)
                                            Text(currentPayment.second, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = TextPrimary)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFEEEEEE))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("NEFT ID: ${currentPayment.third}", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Unscheduled Payments Card (Screenshot 10:45)
                    Card(
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentSubView = PaymentsSubView.UNSCHEDULED }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🚚", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Unscheduled Payments", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Payments expected from your shipped orders. Once delivered, we will automatically move it to your upcoming payments.",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Payments Over Time Card (Screenshot 10:45)
                    Card(
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Payments over time", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).background(Color(0xFF64B5F6)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Payments to Date", fontSize = 11.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Box(modifier = Modifier.size(12.dp).background(Color(0xFFB0BEC5)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Outstanding Payment", fontSize = 11.sp, color = TextSecondary)
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📉", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No Trend to Show", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("There is no sufficient data to show the results in the selected timeframe.", fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.Center)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Ads Cost Card (Screenshot 10:46)
                    Card(
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Ads Cost ℹ️", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                            }
                            Text("You have not spent on Ads in the last 30 days", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📈🪙", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Gain 40% growth by Ads", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("80% of our top sellers run ads to grow their business, Try Ads", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Compensation & Recoveries Card (Screenshot 10:46)
                    Card(
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentSubView = PaymentsSubView.COMPENSATION_RECOVERIES }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Compensation & Recoveries ℹ️", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Compensations", fontSize = 13.sp, color = TextSecondary)
                                Text("₹0", fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Recoveries", fontSize = 13.sp, color = TextSecondary)
                                Text("₹0", fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("₹0", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Referral Payments Card (Screenshot 10:46)
                    Card(
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Referral Payments", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                            }
                            Text("No referral payments to show. Refer now and earn.", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("₹0", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(60.dp))
                }
            }

            // Download Bottom Sheet (Screenshot 10:45)
            if (showDownloadSheet) {
                Dialog(
                    onDismissRequest = { showDownloadSheet = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { showDownloadSheet = false },
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
                                    Text("DOWNLOAD", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                    IconButton(onClick = { showDownloadSheet = false }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close")
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 6.dp))

                                listOf(
                                    "GST Report" to false,
                                    "Tax Invoice" to false,
                                    "Supplier Tax Invoice" to true,
                                    "Payments to Date" to true,
                                    "Outstanding Payments" to true
                                ).forEach { (item, isNew) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                showDownloadSheet = false
                                                Toast.makeText(context, "Downloading $item (Excel)...", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(item, fontSize = 14.sp, color = TextPrimary)
                                        if (isNew) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFFE91E63))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("New", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Next 7 Days Payments Sub-screen (Screenshot 10:45 #2)
@Composable
fun Next7DaysPaymentsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text("Next 7 days payments", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Help")
                    }
                }
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .border(1.dp, Color(0xFFB0BEC5), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("₹ ₹", fontSize = 24.sp, color = Color(0xFF78909C), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text("You have no outstanding payments", fontSize = 14.sp, color = TextSecondary)
        }
    }
}

// Unscheduled Payments Sub-screen (Screenshot 10:47)
@Composable
fun UnscheduledPaymentsScreen(onBack: () -> Unit) {
    var selectedUnscheduledTab by remember { mutableIntStateOf(0) } // 0=Order Details (0), 1=Platform Recovery (0), 2=Platform Compensation

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text("Unscheduled", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Help")
                    }
                }
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Summary Card
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Net Order Amount ℹ️", fontSize = 13.sp, color = TextSecondary)
                        Text("₹0", fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Net Platform Recovery", fontSize = 13.sp, color = TextSecondary)
                        Text("₹0", fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Net Platform Compensation", fontSize = 13.sp, color = TextSecondary)
                        Text("₹0", fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount ℹ️", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("₹0", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TabRow(
                selectedTabIndex = selectedUnscheduledTab,
                containerColor = CardWhite,
                contentColor = Color(0xFF3F51B5)
            ) {
                Tab(
                    selected = selectedUnscheduledTab == 0,
                    onClick = { selectedUnscheduledTab = 0 },
                    text = { Text("Order Details (0)", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedUnscheduledTab == 1,
                    onClick = { selectedUnscheduledTab = 1 },
                    text = { Text("Platform Recovery (0)", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedUnscheduledTab == 2,
                    onClick = { selectedUnscheduledTab = 2 },
                    text = { Text("Platform Compensation", fontSize = 12.sp) }
                )
            }

            if (selectedUnscheduledTab == 2) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Program Benefits", fontSize = 12.sp, color = Color(0xFF1A237E), fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹0", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier.weight(1f).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Referral Earnings", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹0", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No records in this section", color = TextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

// Compensation & Recoveries Sub-screen (Screenshot 10:48)
@Composable
fun CompensationRecoveriesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text("Compensation & Recoveries", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Help")
                    }
                }
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Upgraded reasons yellow banner
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💼", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upgraded reasons for better clarity", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "The reasons for payments of Claims, Compensations, Recoveries and other deductions have been upgraded to provide more transparency. For more detailed description of each reason, please download the Upgraded Reasons document from here.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { Toast.makeText(context, "Downloading Upgraded Reasons PDF...", Toast.LENGTH_SHORT).show() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF303F9F)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Download", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary Card
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { Toast.makeText(context, "Downloading Payment Excel...", Toast.LENGTH_SHORT).show() }
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF1A237E), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download Payment Excel", fontSize = 12.sp, color = Color(0xFF1A237E), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Charges ℹ️", fontSize = 13.sp, color = TextSecondary)
                        Text("- ₹62.53", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFB71C1C))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Waivers and Compensation ℹ️", fontSize = 13.sp, color = TextSecondary)
                        Text("₹0", fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Other Settlements", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("- ₹62.53", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFB71C1C))
                    }
                    Text("Detailed data on Compensations, Claims, Waivers, Recoveries and Other Support Service Charges with reason", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Orders (1)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(6.dp))

            // Order Card (from screenshot 10:48)
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Date", fontSize = 11.sp, color = TextMuted)
                            Text("11 Apr 2026", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Amount", fontSize = 11.sp, color = TextMuted)
                            Text("- ₹62.53", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFB71C1C))
                        }
                        Column(modifier = Modifier.width(190.dp)) {
                            Text("Sub Order Num: 270731955090373696_1", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Type: RECOVERY", fontSize = 11.sp, color = Color(0xFFB71C1C), fontWeight = FontWeight.Bold)
                            Text("Reason: Affiliate Fee", fontSize = 11.sp, color = TextSecondary)
                            Text("Status: SUCCESS", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            Text("Apply Date: 13 Apr 2026", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
