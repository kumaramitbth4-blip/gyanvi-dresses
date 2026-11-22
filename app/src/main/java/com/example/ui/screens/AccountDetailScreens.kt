package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.R
import com.example.model.PaymentGatewayLinkItem
import com.example.model.PaymentGatewayProviderPreset
import com.example.model.PaymentGatewaySettingsConfig
import com.example.ui.components.PaymentGatewayConnectorSection
import com.example.ui.theme.*
import com.example.util.PaymentGatewayUrlHelper

// =========================================================================
// 1. MY PAYMENTS SCREEN (Matches user screenshot 6:32)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPaymentsScreen(
    onBack: () -> Unit,
    paymentGatewayConfig: PaymentGatewaySettingsConfig = LocalAppConfig.current.paymentGateways,
    onSaveAndConnectPaymentGateway: (id: String?, name: String, url: String, provider: PaymentGatewayProviderPreset, lastStatus: String, connectNow: Boolean) -> Result<PaymentGatewayLinkItem> = { id, name, url, provider, status, _ ->
        Result.success(PaymentGatewayLinkItem(id ?: "pg-temp", name, url, provider, true, status))
    },
    onConnectPaymentGateway: (String) -> Unit = {},
    onDisconnectPaymentGateway: () -> Unit = {},
    onDeletePaymentGateway: (String) -> Unit = {},
    onToggleAppendPaymentParams: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(1) } // 0 = Transactions, 1 = Payment Modes
    var savedUpiIds by remember {
        mutableStateOf(
            listOf(
                Triple("PhonePe", "9835107144@ybl", Color(0xFF5F259F))
            )
        )
    }
    var showAddUpiDialog by remember { mutableStateOf(false) }
    var newUpiId by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Text(
                            text = "MY PAYMENTS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                    }

                    // 2 Tabs: Transactions & Payment Modes
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = CardWhite,
                        contentColor = MeeshoMagenta
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    "Transactions",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) TextPrimary else TextSecondary
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    "Payment Modes",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) TextPrimary else TextSecondary
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF9F9F9),
        modifier = modifier.fillMaxSize()
    ) { padding ->
        if (selectedTab == 1) {
            // Payment Modes Tab + Connected Payment Gateway URL Connector
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                PaymentGatewayConnectorSection(
                    config = paymentGatewayConfig,
                    onSaveAndConnect = onSaveAndConnectPaymentGateway,
                    onConnectGateway = onConnectPaymentGateway,
                    onDisconnectGateway = onDisconnectPaymentGateway,
                    onDeleteGateway = onDeletePaymentGateway,
                    onToggleAppendParams = onToggleAppendPaymentParams,
                    modifier = Modifier.testTag("buyer_payment_gateway_connector")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Saved Payment Methods",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "UPI IDs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                savedUpiIds.forEach { (app, upi, iconColor) ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // PhonePe App Icon
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(iconColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("पे", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(app, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(upi, fontSize = 13.sp, color = TextSecondary)
                            }

                            TextButton(
                                onClick = {
                                    savedUpiIds = savedUpiIds.filter { it.second != upi }
                                    Toast.makeText(context, "UPI ID deleted", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("DELETE", color = MeeshoMagenta, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showAddUpiDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MeeshoMagenta)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("+ Add Another UPI ID", color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Transactions Tab
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                listOf(
                    Triple("₹272.00 Refund Credited", "Order #GY-2026-904 • UPI: 9835107144@ybl", "Oct 30, 2026"),
                    Triple("₹399.00 Refund Credited", "Order #GY-2026-871 • Bank A/C: ••••4829", "Oct 24, 2026"),
                    Triple("₹450.00 Order Payment", "Banarasi Silk Saree • Paid via PhonePe", "Oct 20, 2026")
                ).forEach { (title, subtitle, date) ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (title.contains("Refund")) Color(0xFF2E7D32) else TextPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(subtitle, fontSize = 12.sp, color = TextSecondary)
                            }
                            Text(date, fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }

    if (showAddUpiDialog) {
        AlertDialog(
            onDismissRequest = { showAddUpiDialog = false },
            title = { Text("Add UPI ID", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newUpiId,
                    onValueChange = { newUpiId = it },
                    label = { Text("Enter UPI ID (e.g. mobile@upi)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUpiId.isNotBlank()) {
                            savedUpiIds = savedUpiIds + Triple("UPI", newUpiId, MeeshoMagenta)
                            showAddUpiDialog = false
                            Toast.makeText(context, "UPI ID saved successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUpiDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// =========================================================================
// 2. FOLLOWED SHOPS SCREEN (Matches user screenshot 6:33)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowedShopsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(1) } // 0 = Followers, 1 = Shops Followed, 2 = Profiles Followed
    var shopSearchOrAddQuery by remember { mutableStateOf("") }
    var followedShopsList by remember {
        mutableStateOf(
            listOf(
                "Gyanvi Dresses Boutique • Bettiah (4.8 ★)",
                "Patwa Ethnic Collections • Bihar (4.6 ★)",
                "Bettiah Textile Hub • Silk & Kurtis (4.7 ★)"
            )
        )
    }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Text(
                            text = "AMIT KUMAR PATWA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                    }

                    // 3 Tabs: Followers | Shops Followed | Profiles Followed
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = CardWhite,
                        contentColor = MeeshoMagenta
                    ) {
                        listOf("Followers (142)", "Shops Followed (${followedShopsList.size})", "Profiles Followed (18)").forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == index) TextPrimary else TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        containerColor = CardWhite,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Text Box to Search or Add new shop to follow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = shopSearchOrAddQuery,
                    onValueChange = { shopSearchOrAddQuery = it },
                    placeholder = { Text("Search or type shop name (Text Box)") },
                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = MeeshoMagenta) },
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        if (shopSearchOrAddQuery.isNotBlank()) {
                            val newShop = "${shopSearchOrAddQuery.trim()} (New ★)"
                            followedShopsList = followedShopsList + newShop
                            Toast.makeText(context, "Now following $shopSearchOrAddQuery!", Toast.LENGTH_SHORT).show()
                            shopSearchOrAddQuery = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+ Follow", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Shops You Follow (${followedShopsList.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val displayList = if (shopSearchOrAddQuery.isBlank()) followedShopsList
                else followedShopsList.filter { it.contains(shopSearchOrAddQuery, ignoreCase = true) }

                items(displayList) { shop ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF3E5F5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(shop, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                            }

                            OutlinedButton(
                                onClick = {
                                    followedShopsList = followedShopsList.filter { it != shop }
                                    Toast.makeText(context, "Unfollowed $shop", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Following ✓", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. MEESHO BALANCE SCREEN (Matches user screenshots 6:33 #7 & #8)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeeshoBalanceScreen(
    onBack: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToFaqs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expandedAccordion by remember { mutableIntStateOf(0) } // 0 = none, 1 = How to get, 2 = How to use, 3 = Why use
    var currentBalance by remember { mutableIntStateOf(125) }
    var promoCodeInput by remember { mutableStateOf("") }
    var addMoneyAmountInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        text = "GYANVI DRESSES BALANCE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                }
            }
        },
        containerColor = Color(0xFFF9F9F9),
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Your Balance Top Card
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Balance",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "₹$currentBalance",
                        fontWeight = FontWeight.Bold,
                        fontSize = 44.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Voucher / Promo Code Text Box Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                        modifier = Modifier.fillMaxWidth(0.92f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Redeem Gift Card / Promo Code", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = promoCodeInput,
                                    onValueChange = { promoCodeInput = it.uppercase() },
                                    placeholder = { Text("Enter code (e.g. GYANVI100)") },
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        if (promoCodeInput.isNotBlank()) {
                                            currentBalance += 100
                                            Toast.makeText(context, "Code $promoCodeInput applied! Added ₹100 to your Gyanvi Dresses Balance.", Toast.LENGTH_LONG).show()
                                            promoCodeInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                                ) {
                                    Text("Apply")
                                }
                            }

                            HorizontalDivider(color = Color(0xFFEBEBEB))

                            Text("Add Money to Balance", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = addMoneyAmountInput,
                                    onValueChange = { addMoneyAmountInput = it },
                                    placeholder = { Text("Amount (₹) (Text Box)") },
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        val amt = addMoneyAmountInput.toIntOrNull()
                                        if (amt != null && amt > 0) {
                                            currentBalance += amt
                                            Toast.makeText(context, "₹$amt added to Gyanvi Dresses Balance via UPI!", Toast.LENGTH_SHORT).show()
                                            addMoneyAmountInput = ""
                                        } else {
                                            Toast.makeText(context, "Enter valid amount", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                ) {
                                    Text("+ Add")
                                }
                            }
                            val walletGatewayConfig = LocalAppConfig.current.paymentGateways
                            val activeWalletGateway = walletGatewayConfig.activeGateway ?: walletGatewayConfig.gateways.firstOrNull { it.isEnabled }
                            if (activeWalletGateway != null) {
                                OutlinedButton(
                                    onClick = {
                                        val amt = addMoneyAmountInput.toIntOrNull()?.takeIf { it > 0 } ?: 500
                                        val url = PaymentGatewayUrlHelper.buildCheckoutGatewayUrl(
                                            baseUrl = activeWalletGateway.url,
                                            amountInr = amt,
                                            orderRef = "WALLET-${System.currentTimeMillis() % 1000000}",
                                            appendParams = walletGatewayConfig.appendOrderParams
                                        ) ?: activeWalletGateway.url
                                        val res = PaymentGatewayUrlHelper.openPaymentGatewayUrl(context, url)
                                        if (!res.isValid) {
                                            Toast.makeText(context, res.errorMessage ?: "Invalid URL", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Icon(Icons.Default.Link, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Pay via ${activeWalletGateway.name} URL 🔗",
                                        color = MeeshoMagenta,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Accordion 1: How to get Gyanvi Dresses Balance?
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedAccordion = if (expandedAccordion == 1) 0 else 1
                            }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("How to get Gyanvi Dresses Balance?", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Choose Gyanvi Dresses balance while returning", fontSize = 13.sp, color = TextSecondary)
                        }
                        Icon(
                            imageVector = if (expandedAccordion == 1) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }

                    // Video Player Mockup matching Screenshot 6:33
                    AnimatedVisibility(visible = expandedAccordion == 1) {
                        VideoPlayerMockup(durationText = "0:00 / 0:11")
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // Accordion 2: How to use on the next order?
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedAccordion = if (expandedAccordion == 2) 0 else 2
                            }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("How to use on the next order?", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Auto adjusted on your next order", fontSize = 13.sp, color = TextSecondary)
                        }
                        Icon(
                            imageVector = if (expandedAccordion == 2) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }

                    AnimatedVisibility(visible = expandedAccordion == 2) {
                        VideoPlayerMockup(durationText = "0:00 / 0:09")
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // Accordion 3: Why use Meesho Balance
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedAccordion = if (expandedAccordion == 3) 0 else 3
                            }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Why use Gyanvi Dresses Balance", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Icon(
                            imageVector = if (expandedAccordion == 3) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }

                    AnimatedVisibility(visible = expandedAccordion == 3) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("• Instant refund credit right after delivery partner picks up returned product.", fontSize = 13.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("• 100% usable on your next checkout without bank OTP delays.", fontSize = 13.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // Navigation Row: Terms of Use
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTerms() }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Terms of Use", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // Navigation Row: FAQs
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToFaqs() }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("FAQs", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Video Player Container Mockup
@Composable
private fun VideoPlayerMockup(durationText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Big play button
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.Black,
                modifier = Modifier.size(36.dp)
            )
        }

        // Bottom Controls bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(durationText, color = Color.White, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeOff, contentDescription = "Mute", tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// =========================================================================
// 4. BALANCE TERMS SCREEN (Matches user screenshot 6:33 #9)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceTermsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        text = "TERMS OF USE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                }
            }
        },
        containerColor = CardWhite,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text(
                text = "Gyanvi Dresses Balance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Item 1: Refund Added Immediately
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8EAF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CurrencyRupee,
                        contentDescription = null,
                        tint = Color(0xFF3F51B5),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Refund will be added to Gyanvi Dresses Balance immediately after order pickup",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Item 2: Shopping Cart
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8EAF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color(0xFF3F51B5),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Gyanvi Dresses Balance can be used to shop on Gyanvi Dresses",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Please Note",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "• You cannot withdraw Gyanvi Dresses Balance or transfer it to your bank account",
                fontSize = 13.sp,
                color = TextPrimary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "• Gyanvi Dresses Balance expires 1 year after it is credited",
                fontSize = 13.sp,
                color = TextPrimary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Read All Terms & Conditions",
                fontSize = 13.sp,
                color = Color(0xFF5E6977),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Full Terms & Conditions loaded", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// =========================================================================
// 5. BALANCE FAQS SCREEN (Matches user screenshots 6:33 #10 & #11)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceFaqsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val faqQuestions = listOf(
        "Can I add money into Gyanvi Dresses Balance?" to "No, Gyanvi Dresses Balance is credited exclusively for approved returns and promotional referral bonuses.",
        "How can I check my Gyanvi Dresses Balance?" to "Go to Account > Gyanvi Dresses Balance to see your available balance and active transaction history.",
        "How do I use Gyanvi Dresses Balance to purchase items on Gyanvi Dresses?" to "Gyanvi Dresses Balance is automatically applied on checkout and subtracted from the payable total.",
        "Can I redeem Gyanvi Dresses Balance to Cash?" to "Gyanvi Dresses Balance is non-transferable to cash or bank accounts and is reserved for purchasing clothing and products on Gyanvi Dresses.",
        "Can I buy items on other e-commerce platforms with Gyanvi Dresses Balance?" to "No, Gyanvi Dresses Balance is strictly valid on the Gyanvi Dresses platform.",
        "Can I transfer my Gyanvi Dresses Balance to another Account?" to "Gyanvi Dresses Balance is tied to your verified mobile number (+91 9128754982) and cannot be transferred to another user.",
        "What happens to my Gyanvi Dresses Balance if I uninstall Gyanvi Dresses App?" to "Your balance stays safe in your account. Just log back in with your mobile number to access it.",
        "What happens if someone else uses Gyanvi Dresses Balance using my account without my permission?" to "Please reach out to Help Centre immediately on +91 9128754982 to freeze your account.",
        "What happens if I return the product bought using Gyanvi Dresses Balance?" to "The refund amount is immediately re-credited to your Gyanvi Dresses Balance within 15 minutes of pickup.",
        "Can I pay for a product partially through Gyanvi Dresses Balance and partially from other payment options?" to "Yes! Gyanvi Dresses Balance will cover part of the price, and you can pay the remainder via UPI, Card, or COD.",
        "Do I have the option to decide the amount I can pay using Gyanvi Dresses Balance and the rest using other payment methods?" to "Yes, on the checkout review step you can toggle the amount deducted from your Gyanvi Dresses Balance.",
        "Is it necessary to pay using Gyanvi Dresses Balance if I have amount available?" to "No, you can uncheck the 'Use Gyanvi Dresses Balance' checkbox if you want to pay entirely via UPI or COD.",
        "What is the validity of Gyanvi Dresses Balance?" to "Gyanvi Dresses Balance is valid for 1 full year (365 days) from the date of credit.",
        "Is there any minimum amount to be maintained in Gyanvi Dresses Balance?" to "No, there is zero minimum balance requirement.",
        "Why should I take my refunds in Gyanvi Dresses Balance?" to "You receive instant refunds right upon pickup without waiting 3-5 days for bank NEFT processing.",
        "Can I select Gyanvi Dresses Balance as refund method for orders paid through Prepaid methods?" to "Yes, during return initiation you can choose between original payment source or instant Gyanvi Dresses Balance.",
        "How can I get help regarding Gyanvi Dresses Balance?" to "Contact Gyanvi 24x7 Customer Care via the Help Centre tab.",
        "Why am I not able to see Gyanvi Dresses Balance on the App" to "Please ensure you have updated to the latest app version v14.3.2 and are logged in with your phone number."
    )

    var expandedIndex by remember { mutableIntStateOf(-1) }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        text = "HELP CENTRE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                }
            }
        },
        containerColor = CardWhite,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    text = "Gyanvi Dresses Balance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            items(faqQuestions.size) { index ->
                val (q, a) = faqQuestions[index]
                val isExpanded = expandedIndex == index

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedIndex = if (isExpanded) -1 else index }
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = q,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text(
                                text = a,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F1F1))
            }
        }
    }
}

// =========================================================================
// 6. SETTINGS SCREEN (Matches user screenshot 6:34 #12)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    onBack: () -> Unit,
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    appLanguage: String = "English",
    onLanguageChange: (String) -> Unit = {},
    onOpenNotificationSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var includeTextAsImage by remember { mutableStateOf(true) }
    var orderNotificationPush by remember { mutableStateOf(true) }
    var defaultPincodeInput by remember { mutableStateOf("845438") }
    var appFeedbackInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        text = "SETTINGS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                }
            }
        },
        containerColor = CardWhite,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Appearance & Language",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dark Mode Theme",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isDarkMode) "Dark UI enabled" else "Light Yellow & Pink UI enabled",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MeeshoMagenta
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Language:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                listOf("English", "हिंदी", "मराठी").forEach { lang ->
                    FilterChip(
                        selected = appLanguage == lang,
                        onClick = { onLanguageChange(lang) },
                        label = { Text(lang, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MeeshoMagenta,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            OutlinedButton(
                onClick = onOpenNotificationSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Full Notification Channels & Alerts", fontWeight = FontWeight.SemiBold, color = MeeshoMagenta)
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            Text(
                text = "Share Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Include text as image while sharing",
                    fontSize = 15.sp,
                    color = TextPrimary
                )

                Switch(
                    checked = includeTextAsImage,
                    onCheckedChange = { includeTextAsImage = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MeeshoMagenta
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Order delivery notification alerts",
                    fontSize = 15.sp,
                    color = TextPrimary
                )

                Switch(
                    checked = orderNotificationPush,
                    onCheckedChange = { orderNotificationPush = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MeeshoMagenta
                    )
                )
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            Text(
                text = "Default Delivery Location (Text Box)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            OutlinedTextField(
                value = defaultPincodeInput,
                onValueChange = { defaultPincodeInput = it },
                label = { Text("Default Pincode (6 digits)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = {
                        Toast.makeText(context, "Pincode $defaultPincodeInput saved for faster checkout!", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Save", fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                    }
                }
            )

            HorizontalDivider(color = Color(0xFFF0F0F0))

            Text(
                text = "App Feedback & Feature Request (Text Box)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            OutlinedTextField(
                value = appFeedbackInput,
                onValueChange = { appFeedbackInput = it },
                label = { Text("What options or features would you like us to add?") },
                placeholder = { Text("Write your suggestions here...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Button(
                onClick = {
                    if (appFeedbackInput.isNotBlank()) {
                        Toast.makeText(context, "Thank you! Feature suggestion recorded.", Toast.LENGTH_LONG).show()
                        appFeedbackInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Suggestion", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "App cache cleared! 16.4 MB freed.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear App Cache")
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // App & Packer Identity Card
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            val appNameText = androidx.compose.ui.res.stringResource(R.string.app_name)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Application & Packer Info",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text("Application Name: $appNameText", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Application ID: ${BuildConfig.APPLICATION_ID}",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(BuildConfig.APPLICATION_ID))
                            Toast.makeText(context, "Application ID copied", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                        }
                    }
                    Text("Packer Name: Gyanvi Dresses Packaging & Logistics Hub", fontSize = 12.sp, color = TextPrimary)
                    Text("Packer ID: PKR-GD-845438", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MeeshoMagenta)
                }
            }
        }
    }
}

// =========================================================================
// 7. LEGAL AND POLICIES SCREEN (Matches user screenshots 6:34 #14 & #15)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalPoliciesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPolicyTitle by remember { mutableStateOf<String?>(null) }
    var policySearchQuery by remember { mutableStateOf("") }

    val policyItems = listOf(
        "Terms and Conditions",
        "Responsible Disclosure Policy",
        "Privacy Policy",
        "Anti Phishing Alert",
        "Intellectual Property Policy",
        "Returns, Refunds and Replacement Policy",
        "Hall of Fame",
        "Cancellation Policy",
        "Whistleblower Policy",
        "M-deliver",
        "Third Party Functionality - Terms and Conditions",
        "influencer-marketing-tncs",
        "GYANVI DRESSES MALL AFFILIATE MARKETING PROGRAM T&Cs"
    )

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        text = "LEGAL AND POLICIES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                }
            }
        },
        containerColor = CardWhite,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        val filteredPolicies = if (policySearchQuery.isBlank()) policyItems
        else policyItems.filter { it.contains(policySearchQuery, ignoreCase = true) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                OutlinedTextField(
                    value = policySearchQuery,
                    onValueChange = { policySearchQuery = it },
                    placeholder = { Text("Search legal policy clauses (Text Box)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MeeshoMagenta) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            items(filteredPolicies) { policy ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPolicyTitle = policy }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = policy,
                        fontSize = 15.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
                HorizontalDivider(color = Color(0xFFF1F1F1))
            }
        }
    }

    selectedPolicyTitle?.let { title ->
        val webCfg = LocalAppConfig.current.websiteIntegration
        val policyBody = when {
            title.contains("Terms", ignoreCase = true) -> webCfg.termsAndConditions
            title.contains("Privacy", ignoreCase = true) -> webCfg.privacyPolicy
            else -> "Gyanvi Dresses (${webCfg.websiteDomain}) adheres strictly to Indian consumer protection laws and IT Act. All transactions, personal data, and seller commitments are regulated by the standard $title."
        }
        AlertDialog(
            onDismissRequest = { selectedPolicyTitle = null },
            title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Text(
                    text = policyBody,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { selectedPolicyTitle = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
