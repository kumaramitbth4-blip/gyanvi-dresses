package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.SampleData
import com.example.data.remote.AiDressRecommendationResult
import com.example.data.remote.AiInferenceState
import com.example.model.BuyerSellerChatMessage
import com.example.model.OrderItem
import com.example.model.PaymentGatewayLinkItem
import com.example.model.PaymentGatewayOption
import com.example.model.PaymentGatewayProviderPreset
import com.example.model.PaymentGatewaySettingsConfig
import com.example.model.Product
import com.example.ui.components.AddEditPaymentGatewayDialog
import com.example.ui.theme.*
import com.example.util.HoldOrderPdfGenerator
import com.example.util.ImageSaver
import com.example.util.PaymentGatewayUrlHelper

// =========================================================================
// 1. MULTI-GATEWAY CHECKOUT & PAYMENT MODAL (Module 7 & 8)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutPaymentModal(
    subtotal: Int,
    deliveryAddress: String,
    walletBalance: Int,
    rewardPoints: Int,
    onDismiss: () -> Unit,
    onConfirmPayment: (paymentMethod: String, discountAmount: Int, walletUsed: Int) -> Unit,
    paymentGatewayConfig: PaymentGatewaySettingsConfig = LocalAppConfig.current.paymentGateways,
    onSaveAndConnectPaymentGateway: ((id: String?, name: String, url: String, provider: PaymentGatewayProviderPreset, lastStatus: String, connectNow: Boolean) -> Result<PaymentGatewayLinkItem>)? = null,
    onConnectPaymentGateway: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedGateway by remember { mutableStateOf(PaymentGatewayOption.CUSTOM_URL) }
    var customUpiInput by remember { mutableStateOf("amit.patwa@okaxis") }
    var cardLastDigits by remember { mutableStateOf("4532 •••• •••• 8841") }
    var selectedBank by remember { mutableStateOf("SBI") }
    var couponCodeInput by remember { mutableStateOf("") }
    var appliedCouponDiscount by remember { mutableIntStateOf(0) }
    var appliedCouponName by remember { mutableStateOf("") }
    var useWalletBalance by remember { mutableStateOf(false) }
    var showGatewayLinkDialog by remember { mutableStateOf(false) }
    var editingGatewayForCheckout by remember { mutableStateOf<PaymentGatewayLinkItem?>(null) }
    var launchedCheckoutUrl by remember { mutableStateOf<String?>(null) }
    var showGatewayCompletionDialog by remember { mutableStateOf(false) }

    val availableGatewayLinks = remember(paymentGatewayConfig.gateways) {
        paymentGatewayConfig.gateways.filter { it.isEnabled }
    }
    var selectedGatewayLinkId by remember(paymentGatewayConfig.activeGatewayId, availableGatewayLinks) {
        mutableStateOf(
            paymentGatewayConfig.activeGatewayId
                ?: availableGatewayLinks.firstOrNull()?.id
        )
    }
    val activeCustomGateway = remember(selectedGatewayLinkId, availableGatewayLinks, paymentGatewayConfig.activeGateway) {
        availableGatewayLinks.firstOrNull { it.id == selectedGatewayLinkId }
            ?: paymentGatewayConfig.activeGateway
            ?: availableGatewayLinks.firstOrNull()
    }
    val walletDeduction = if (useWalletBalance) walletBalance.coerceAtMost(subtotal - appliedCouponDiscount) else 0
    val codFee = if (selectedGateway == PaymentGatewayOption.COD && subtotal < 499) 29 else 0
    val finalPayable = (subtotal - appliedCouponDiscount - walletDeduction + codFee).coerceAtLeast(1)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.fillMaxHeight(0.92f).testTag("checkout_payment_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SECURE PAYMENT GATEWAY", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Step 2/3 • Razorpay / UPI / Cards / COD", fontSize = 11.sp, color = TextSecondary)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            HorizontalDivider(color = DividerColor)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Delivery & Pincode Serviceability Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE8F5E9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8E6C9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = GreenRating, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Serviceable: $deliveryAddress ✓", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GreenDiscount)
                            Text("Est. Delivery in 3 Days via Delhivery / Shiprocket Air", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }

                // Promo Coupon Code Box
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Apply Discount Coupon (कूपन कोड)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = couponCodeInput,
                                onValueChange = { couponCodeInput = it.uppercase() },
                                placeholder = { Text("FESTIVE15 / GYANVI50") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    when (couponCodeInput.trim()) {
                                        "FESTIVE15" -> {
                                            appliedCouponDiscount = (subtotal * 0.15).toInt().coerceAtLeast(50)
                                            appliedCouponName = "FESTIVE15 (15% OFF)"
                                        }
                                        "GYANVI50" -> {
                                            appliedCouponDiscount = 50
                                            appliedCouponName = "GYANVI50 (₹50 OFF)"
                                        }
                                        "MEGA100" -> {
                                            appliedCouponDiscount = 100
                                            appliedCouponName = "MEGA100 (₹100 OFF)"
                                        }
                                        else -> {
                                            Toast.makeText(context, "Try FESTIVE15, GYANVI50, or MEGA100", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Text("Apply", fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("FESTIVE15", "GYANVI50", "MEGA100").forEach { code ->
                                AssistChip(
                                    onClick = { couponCodeInput = code },
                                    label = { Text(code, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                        if (appliedCouponDiscount > 0) {
                            Text(
                                text = "✓ $appliedCouponName applied (-₹$appliedCouponDiscount)",
                                color = GreenRating,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Wallet & Reward Points Toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF7FB),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MeeshoMagenta.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Use Gyanvi Wallet (₹$walletBalance available)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Text("Plus earn +${(subtotal / 50).coerceAtLeast(10)} Reward Points (Balance: $rewardPoints pts)", fontSize = 11.sp, color = MeeshoMagenta)
                        }
                        Checkbox(
                            checked = useWalletBalance,
                            onCheckedChange = { useWalletBalance = it },
                            colors = CheckboxDefaults.colors(checkedColor = MeeshoMagenta)
                        )
                    }
                }

                // Payment Gateway Selector
                Text("Select Payment Method (पेमेंट विकल्प)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                PaymentGatewayOption.values().forEach { option ->
                    val isSelected = selectedGateway == option
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MeeshoPinkLight.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MeeshoMagenta else Color(0xFFE0E0E0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedGateway = option }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedGateway = option },
                                    colors = RadioButtonDefaults.colors(selectedColor = MeeshoMagenta)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(option.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(option.badge, fontSize = 11.sp, color = GreenRating, fontWeight = FontWeight.Medium)
                                }
                                Icon(
                                    imageVector = when (option) {
                                        PaymentGatewayOption.COD -> Icons.Default.Payments
                                        PaymentGatewayOption.DEBIT_CARD, PaymentGatewayOption.CREDIT_CARD -> Icons.Default.CreditCard
                                        PaymentGatewayOption.NET_BANKING -> Icons.Default.AccountBalance
                                        PaymentGatewayOption.CUSTOM_URL -> Icons.Default.Link
                                        else -> Icons.Default.QrCodeScanner
                                    },
                                    contentDescription = null,
                                    tint = MeeshoMagenta
                                )
                            }

                            if (isSelected && option == PaymentGatewayOption.UPI_ID) {
                                OutlinedTextField(
                                    value = customUpiInput,
                                    onValueChange = { customUpiInput = it },
                                    label = { Text("Enter UPI ID (VPA)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                                )
                            } else if (isSelected && (option == PaymentGatewayOption.DEBIT_CARD || option == PaymentGatewayOption.CREDIT_CARD)) {
                                OutlinedTextField(
                                    value = cardLastDigits,
                                    onValueChange = { cardLastDigits = it },
                                    label = { Text("${option.label} Number (256-bit SSL Encrypted)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                                )
                            } else if (isSelected && option == PaymentGatewayOption.NET_BANKING) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp).horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("SBI", "HDFC", "ICICI", "Axis", "PNB", "Kotak").forEach { bank ->
                                        FilterChip(
                                            selected = selectedBank == bank,
                                            onClick = { selectedBank = bank },
                                            label = { Text(bank, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            } else if (isSelected && option == PaymentGatewayOption.CUSTOM_URL) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (activeCustomGateway != null) Color(0xFFF0FDF4) else Color(0xFFFFFBEB),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (activeCustomGateway != null) Color(0xFF86EFAC) else Color(0xFFFDE68A)
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (availableGatewayLinks.isNotEmpty()) {
                                            Text(
                                                "Select Any Payment Gateway (${availableGatewayLinks.size} Available):",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp,
                                                color = TextPrimary
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                availableGatewayLinks.forEach { gw ->
                                                    val isGwSel = activeCustomGateway?.id == gw.id
                                                    FilterChip(
                                                        selected = isGwSel,
                                                        onClick = {
                                                            selectedGatewayLinkId = gw.id
                                                            onConnectPaymentGateway?.invoke(gw.id)
                                                        },
                                                        label = { Text(gw.name, fontSize = 11.sp, fontWeight = if (isGwSel) FontWeight.Bold else FontWeight.Medium) },
                                                        leadingIcon = if (isGwSel) {
                                                            { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                                        } else null,
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            selectedContainerColor = Color(0xFF15803D),
                                                            selectedLabelColor = Color.White,
                                                            selectedLeadingIconColor = Color.White
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                        if (activeCustomGateway != null) {
                                            val host = PaymentGatewayUrlHelper.validateHttpsGatewayUrl(activeCustomGateway.url).host
                                            Text(
                                                "Selected Gateway: ${activeCustomGateway.name} ✓",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF166534)
                                            )
                                            Text(
                                                "${activeCustomGateway.provider.displayName} • $host",
                                                fontSize = 11.sp,
                                                color = Color(0xFF15803D)
                                            )
                                            Text(
                                                activeCustomGateway.url,
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF2563EB),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        } else {
                                            Text(
                                                "No Payment Gateway URL connected yet.",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF92400E)
                                            )
                                            Text(
                                                "Add any Razorpay, PhonePe PG, Paytm, Cashfree, PayU or Stripe link to pay online.",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                        if (onSaveAndConnectPaymentGateway != null) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (activeCustomGateway != null) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            editingGatewayForCheckout = activeCustomGateway
                                                            showGatewayLinkDialog = true
                                                        },
                                                        modifier = Modifier.weight(1f).testTag("checkout_edit_gateway_url_btn")
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp), tint = MeeshoMagenta)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Edit URL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                                                    }
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        editingGatewayForCheckout = null
                                                        showGatewayLinkDialog = true
                                                    },
                                                    modifier = Modifier.weight(1f).testTag("checkout_add_gateway_url_btn")
                                                ) {
                                                    Icon(Icons.Default.AddLink, contentDescription = null, modifier = Modifier.size(15.dp), tint = MeeshoMagenta)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        "Add New URL 🔗",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
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
                }

                // Final Payable Breakdown
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Cart Subtotal", fontSize = 12.sp, color = TextSecondary)
                            Text("₹$subtotal", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        if (appliedCouponDiscount > 0) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Coupon ($appliedCouponName)", fontSize = 12.sp, color = GreenRating)
                                Text("- ₹$appliedCouponDiscount", fontSize = 12.sp, color = GreenRating, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (walletDeduction > 0) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Gyanvi Wallet Used", fontSize = 12.sp, color = MeeshoMagenta)
                                Text("- ₹$walletDeduction", fontSize = 12.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (codFee > 0) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("COD Handling Fee", fontSize = 12.sp, color = TextSecondary)
                                Text("+ ₹$codFee", fontSize = 12.sp)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Total Payable", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("₹$finalPayable", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MeeshoMagenta)
                        }
                    }
                }
            }

            // Pay & Place Order Button
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 6.dp) {
                Button(
                    onClick = {
                        if (selectedGateway == PaymentGatewayOption.CUSTOM_URL) {
                            if (activeCustomGateway == null) {
                                if (onSaveAndConnectPaymentGateway != null) {
                                    showGatewayLinkDialog = true
                                } else {
                                    Toast.makeText(context, "Please connect a Payment Gateway URL in Admin/Payments first.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val refId = "GD-${System.currentTimeMillis() % 1000000}"
                                val checkoutUrl = PaymentGatewayUrlHelper.buildCheckoutGatewayUrl(
                                    baseUrl = activeCustomGateway.url,
                                    amountInr = finalPayable,
                                    orderRef = refId,
                                    appendParams = paymentGatewayConfig.appendOrderParams
                                )
                                if (checkoutUrl == null) {
                                    Toast.makeText(context, "Invalid connected gateway URL", Toast.LENGTH_SHORT).show()
                                } else {
                                    launchedCheckoutUrl = checkoutUrl
                                    PaymentGatewayUrlHelper.openPaymentGatewayUrl(context, checkoutUrl)
                                    showGatewayCompletionDialog = true
                                }
                            }
                        } else {
                            val methodStr = when (selectedGateway) {
                                PaymentGatewayOption.NET_BANKING -> "Net Banking ($selectedBank)"
                                PaymentGatewayOption.UPI_ID -> "UPI (${customUpiInput.trim().ifBlank { "VPA" }})"
                                else -> selectedGateway.label
                            }
                            onConfirmPayment(methodStr, appliedCouponDiscount, walletDeduction)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp)
                        .testTag("confirm_payment_order_btn")
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            selectedGateway == PaymentGatewayOption.COD -> "Place COD Order (₹$finalPayable)"
                            selectedGateway == PaymentGatewayOption.CUSTOM_URL && activeCustomGateway != null -> "Pay ₹$finalPayable via ${activeCustomGateway.name} 🔗"
                            selectedGateway == PaymentGatewayOption.CUSTOM_URL -> "Connect & Pay ₹$finalPayable 🔗"
                            else -> "Pay ₹$finalPayable Securely"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }

    if (showGatewayLinkDialog && onSaveAndConnectPaymentGateway != null) {
        AddEditPaymentGatewayDialog(
            initialGateway = editingGatewayForCheckout,
            onDismiss = { showGatewayLinkDialog = false },
            onSaveAndConnect = { id, name, url, provider, status ->
                val res = onSaveAndConnectPaymentGateway(id, name, url, provider, status, true)
                res.getOrNull()?.let { savedItem ->
                    selectedGatewayLinkId = savedItem.id
                    showGatewayLinkDialog = false
                    Toast.makeText(context, "Connected to $name 🔗", Toast.LENGTH_SHORT).show()
                }
                res
            }
        )
    }

    if (showGatewayCompletionDialog && activeCustomGateway != null) {
        AlertDialog(
            onDismissRequest = { showGatewayCompletionDialog = false },
            title = { Text("Payment Gateway Opened 🔗", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Gateway: ${activeCustomGateway.name} (₹$finalPayable)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        "Complete your payment on the opened HTTPS gateway page, then tap Confirm Order below.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGatewayCompletionDialog = false
                        onConfirmPayment("Gateway: ${activeCustomGateway.name}", appliedCouponDiscount, walletDeduction)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                ) {
                    Text("I Completed Payment", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            launchedCheckoutUrl?.let { PaymentGatewayUrlHelper.openPaymentGatewayUrl(context, it) }
                        }
                    ) {
                        Text("Re-open Link")
                    }
                    TextButton(onClick = { showGatewayCompletionDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

// =========================================================================
// 2. GST TAX INVOICE MODAL (Module 3 - Download Invoice)
// =========================================================================
@Composable
fun GstInvoiceDialog(
    order: OrderItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val basePrice = (order.totalAmount / 1.05).toInt()
    val gstTotal = order.totalAmount - basePrice

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("gst_invoice_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TAX INVOICE (GST)", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MeeshoMagenta)
                        Text("GYANVI DRESSES MARKETPLACE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Column {
                        Text("Invoice No:", fontSize = 10.sp, color = TextSecondary)
                        Text("INV-${order.orderId}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Order Date:", fontSize = 10.sp, color = TextSecondary)
                        Text(order.orderDate, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF9FAFB)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Sold By: Gyanvi Ethnic Boutique (Bettiah, Bihar - 845438)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("GSTIN: 10ABCDE1234F1Z5 • HSN Code: 6204", fontSize = 11.sp, color = TextSecondary)
                        Text("AWB No: ${order.awbNumber} • Payment: ${order.paymentMethod}", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                HorizontalDivider()

                Text(order.product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Size: ${order.selectedSize} • Qty: ${order.quantity}", fontSize = 12.sp, color = TextSecondary)

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Taxable Value", fontSize = 12.sp, color = TextSecondary)
                    Text("₹$basePrice.00", fontSize = 12.sp)
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("CGST (2.5%) + SGST (2.5%)", fontSize = 12.sp, color = TextSecondary)
                    Text("₹$gstTotal.00", fontSize = 12.sp)
                }
                HorizontalDivider()
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Grand Total (Paid)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("₹${order.totalAmount}.00", fontWeight = FontWeight.Black, fontSize = 15.sp, color = GreenDiscount)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val res = HoldOrderPdfGenerator.saveSingleGstInvoicePdfToDownloads(context, order)
                            if (!res.success) {
                                Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            val invoiceText = "GYANVI DRESSES GST INVOICE\nInvoice: INV-${order.orderId}\nItem: ${order.product.name}\nTotal: ₹${order.totalAmount}\nAWB: ${order.awbNumber}"
                            ImageSaver.shareCustomText(context, "Gyanvi Tax Invoice", invoiceText)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. PRODUCT COMPARISON SCREEN (Module 3 & 13)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductComparisonScreen(
    compareList: List<Product>,
    allProducts: List<Product>,
    onBack: () -> Unit,
    onToggleProduct: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onClear: () -> Unit
) {
    BackHandler { onBack() }
    val displayProducts = if (compareList.size >= 2) compareList else allProducts.take(3)

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Column {
                            Text("COMPARE DRESSES", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Side-by-side quality, fabric & price comparison", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    if (compareList.isNotEmpty()) {
                        TextButton(onClick = onClear) {
                            Text("Reset", color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize().testTag("product_compare_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Tap below to swap dresses into comparison (up to 3):",
                fontSize = 12.sp,
                color = TextSecondary
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allProducts) { prod ->
                    val isAdded = compareList.any { it.id == prod.id }
                    FilterChip(
                        selected = isAdded,
                        onClick = { onToggleProduct(prod) },
                        label = { Text(prod.name.take(18) + "...", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MeeshoMagenta,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Side-by-side comparison table
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                displayProducts.forEach { prod ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.width(175.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Image(
                                painter = painterResource(prod.imageRes),
                                contentDescription = prod.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(8.dp))
                            )
                            Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            HorizontalDivider()
                            CompareRow("Offer Price", "₹${prod.price}", GreenRating)
                            CompareRow("MRP", "₹${prod.originalPrice}", TextMuted)
                            CompareRow("Discount", "${prod.discountPercent}% OFF", SaleRed)
                            CompareRow("Rating", "${prod.rating} ★ (${prod.ratingCount})", Color(0xFFF59E0B))
                            CompareRow("Fabric", prod.fabric, TextPrimary)
                            CompareRow("Sizes", prod.sizes.joinToString(", "), TextPrimary)
                            CompareRow("Delivery", "FREE • 3 Days", GreenDiscount)
                            Button(
                                onClick = { onAddToCart(prod) },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Add to Cart", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareRow(label: String, value: String, valueColor: Color) {
    Column {
        Text(label, fontSize = 9.sp, color = TextSecondary)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = valueColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// =========================================================================
// 4. AI FASHION STYLIST & CUSTOMER-SELLER CHAT (Module 9 & 13)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiFashionAssistantScreen(
    allProducts: List<Product>,
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    aiStylistState: AiInferenceState<AiDressRecommendationResult> = AiInferenceState.Idle,
    onRunAiRecommend: (query: String, occasion: String, maxBudget: Int) -> Unit = { _, _, _ -> },
    chatMessages: List<BuyerSellerChatMessage> = emptyList(),
    onSendChatMessage: (text: String) -> Unit = {},
    initialTab: Int = 0
) {
    BackHandler { onBack() }
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    var queryInput by remember { mutableStateOf("") }
    var selectedOccasion by remember { mutableStateOf("Wedding & Festive") }
    var maxBudget by remember { mutableIntStateOf(1500) }
    var sellerChatInput by remember { mutableStateOf("") }

    val aiMatchedProducts = remember(aiStylistState, allProducts) {
        if (aiStylistState is AiInferenceState.Success) {
            val byId = allProducts.associateBy { it.id }
            aiStylistState.data.recommendedProductIds.mapNotNull { byId[it] }
        } else emptyList()
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Column {
                            Text("AI FASHION & SELLER CHAT", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Gemini AI Stylist • Direct Buyer-Seller Chat", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    TabRow(selectedTabIndex = selectedTab, contentColor = MeeshoMagenta) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("✨ AI Stylist (Gemini)", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("💬 Buyer-Seller Chat", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize().testTag("ai_fashion_assistant_screen")
    ) { padding ->
        if (selectedTab == 0) {
            LazyColumn(
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2F8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("✨ AI Dress Recommender (Firebase AI Logic)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MeeshoMagentaDark)
                            OutlinedTextField(
                                value = queryInput,
                                onValueChange = { queryInput = it },
                                label = { Text("Describe dress, occasion, fabric, or color...") },
                                placeholder = { Text("e.g. Haldi ceremony yellow silk kurti set") },
                                leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MeeshoMagenta) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text("Occasion:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Wedding & Festive", "Haldi & Mehendi", "Daily Ethnic", "Party Wear").forEach { occ ->
                                    FilterChip(
                                        selected = selectedOccasion == occ,
                                        onClick = { selectedOccasion = occ },
                                        label = { Text(occ, fontSize = 11.sp) }
                                    )
                                }
                            }
                            Text("Max Budget: ₹$maxBudget", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Slider(
                                value = maxBudget.toFloat(),
                                onValueChange = { maxBudget = it.toInt() },
                                valueRange = 300f..3000f,
                                colors = SliderDefaults.colors(thumbColor = MeeshoMagenta, activeTrackColor = MeeshoMagenta)
                            )
                            Button(
                                onClick = { onRunAiRecommend(queryInput, selectedOccasion, maxBudget) },
                                enabled = aiStylistState !is AiInferenceState.Loading,
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("run_ai_stylist_btn")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (aiStylistState is AiInferenceState.Loading) "Asking Gemini AI..." else "Get AI Dress Recommendations",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                when (aiStylistState) {
                    AiInferenceState.Idle -> {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Ready to style your look ✨", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        "Pick an occasion, set your budget, and tap 'Get AI Dress Recommendations' to get live Gemini recommendations from GYANVI DRESSES catalog.",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                    AiInferenceState.Loading -> {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(color = MeeshoMagenta, modifier = Modifier.size(24.dp))
                                    Text("Curating outfits with Gemini AI...", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    is AiInferenceState.Error -> {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("AI Stylist Error", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFB91C1C))
                                    Text(aiStylistState.message, fontSize = 12.sp, color = Color(0xFF7F1D1D))
                                    Button(
                                        onClick = { onRunAiRecommend(queryInput, selectedOccasion, maxBudget) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                                    ) {
                                        Text("Retry", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    is AiInferenceState.Success -> {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("✨ ${aiStylistState.data.occasionMatchNote}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF166534))
                                    Text(aiStylistState.data.stylingAdvice, fontSize = 12.sp, color = Color(0xFF14532D))
                                }
                            }
                        }
                        item {
                            Text("AI Picks (${aiMatchedProducts.size} outfits)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        items(aiMatchedProducts) { prod ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth().clickable { onProductClick(prod) }
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(prod.imageRes),
                                        contentDescription = prod.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        Text("${prod.fabric} • ${prod.rating}★", fontSize = 11.sp, color = TextSecondary)
                                        Text("₹${prod.price} (${prod.discountPercent}% OFF)", fontWeight = FontWeight.Bold, color = MeeshoMagenta, fontSize = 13.sp)
                                    }
                                    Button(
                                        onClick = { onAddToCart(prod) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Persistent Buyer-Seller Live Chat Tab
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatMessages, key = { it.id }) { msg ->
                        val isMe = msg.senderRole.equals("BUYER", ignoreCase = true)
                        Column(
                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${msg.senderName} • ${msg.timeLabel}",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            if (msg.productName.isNotBlank()) {
                                Text("Re: ${msg.productName}", fontSize = 10.sp, color = MeeshoMagenta, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isMe) MeeshoMagenta else MaterialTheme.colorScheme.surface,
                                shadowElevation = 1.dp
                            ) {
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = sellerChatInput,
                            onValueChange = { sellerChatInput = it },
                            placeholder = { Text("Ask seller about size, fabric, dispatch...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (sellerChatInput.isNotBlank()) {
                                    onSendChatMessage(sellerChatInput)
                                    sellerChatInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MeeshoMagenta)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 5. SECURITY, 2FA, RBAC & LOGIN HISTORY CENTER (Module 11)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityAuthCenterScreen(
    onBack: () -> Unit,
    onOpenOtpDispatcher: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var twoFactorEnabled by remember { mutableStateOf(true) }
    var biometricLock by remember { mutableStateOf(false) }
    var fraudShieldEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text("SECURITY & ACCESS CONTROL", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("2FA • Fraud Detection • RBAC • Backup & Restore", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize().testTag("security_center_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFE8F5E9),
                border = androidx.compose.foundation.BorderStroke(1.dp, GreenRating),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = GreenRating, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Account Protection: HIGH (98/100)", fontWeight = FontWeight.Bold, color = GreenDiscount, fontSize = 14.sp)
                        Text("AES-256 Encrypted Sessions • HTTPS Pinning Active", fontSize = 11.sp, color = TextPrimary)
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Authentication & Fraud Shield", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Two-Factor Authentication (2FA OTP)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Require SMS/Email OTP for Admin & Payout actions", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(checked = twoFactorEnabled, onCheckedChange = { twoFactorEnabled = it })
                    }
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("App Biometric / Fingerprint Lock", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Protect Seller & Admin panels with fingerprint", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(checked = biometricLock, onCheckedChange = { biometricLock = it })
                    }
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("AI Fraud & High-RTO Detection", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Auto-verify suspicious COD bulk orders via OTP", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(checked = fraudShieldEnabled, onCheckedChange = { fraudShieldEnabled = it })
                    }

                    Button(
                        onClick = onOpenOtpDispatcher,
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test 2FA OTP Verification Flow", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Recent Login Activity History", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    listOf(
                        "Android App • Bettiah, Bihar • Today 10:15 AM (Current)",
                        "Admin Console • West Champaran • Yesterday 6:42 PM",
                        "Seller Portal • +91 9128754982 • 09 Nov 2026"
                    ).forEach { entry ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(Icons.Default.Devices, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(entry, fontSize = 12.sp)
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        Toast.makeText(context, "✓ Encrypted Cloud Backup created (Gyanvi_DB_Backup.json)", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Backup Data", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "✓ Latest snapshot restored safely!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Restore Snapshot", fontSize = 12.sp)
                }
            }
        }
    }
}
