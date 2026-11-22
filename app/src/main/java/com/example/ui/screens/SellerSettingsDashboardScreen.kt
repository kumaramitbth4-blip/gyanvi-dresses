package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

// =========================================================================
// 15-MODULE COMPREHENSIVE SELLER SETTINGS DASHBOARD
// Inspired by Meesho, Amazon & Flipkart Seller Hubs
// Matches Screenshot 5:48 (Store & Legal, Operational Controls, Bank, Logout)
// =========================================================================

data class StaffMember(
    val id: String,
    val name: String,
    val role: String,
    val phone: String,
    val permissions: String
)

data class BankAccount(
    val bankName: String,
    val accountNumberMasked: String,
    val ifsc: String,
    val isPrimary: Boolean,
    val verificationMethod: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullSellerSettingsDashboardScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    // Navigation / Category filter tabs
    val sectionTabs = listOf(
        "All Sections",
        "Store & Legal",
        "Operations",
        "Notifications",
        "Bank & Payout",
        "Security",
        "Languages",
        "Orders & Shipping",
        "Tax & Invoice",
        "Staff & Team",
        "Billing & Plan"
    )
    var selectedSectionTab by remember { mutableStateOf("All Sections") }

    // Module 1: Store & Legal Identity
    var storeName by remember { mutableStateOf("Gyanvi Dresses") }
    var ownerName by remember { mutableStateOf("Amit kumar patwa") }
    var businessAddress by remember { mutableStateOf("Lal Bazar, Main Road, Bettiah, Bihar - 845438") }
    var gstinNumber by remember { mutableStateOf("10ABCDE1234F1Z5") }
    var panNumber by remember { mutableStateOf("ABCDE1234F") }
    var contactEmail by remember { mutableStateOf("support@gyanvidresses.in") }
    var contactPhone by remember { mutableStateOf("+91 9128754982") }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Module 2: Operational Controls (Screenshot 5:48)
    var vacationMode by remember { mutableStateOf(false) }
    var autoAcceptPrepaid by remember { mutableStateOf(true) }
    var autoAcceptCOD by remember { mutableStateOf(false) }
    var newOrderSoundAlert by remember { mutableStateOf(true) }
    var autoStockManagement by remember { mutableStateOf(true) }
    var lowStockThreshold by remember { mutableFloatStateOf(5f) }
    var autoThermalLabelGen by remember { mutableStateOf(true) }
    var cancellationGracePeriod by remember { mutableStateOf(true) }

    // Module 3: Notifications
    var whatsappDailySummary by remember { mutableStateOf(true) }
    var pushNewOrder by remember { mutableStateOf(true) }
    var pushPaymentReceived by remember { mutableStateOf(true) }
    var pushReturnRTO by remember { mutableStateOf(true) }
    var emailWeeklyDigest by remember { mutableStateOf(true) }
    var smsDispatchPickup by remember { mutableStateOf(true) }

    // Module 4: Settlement Bank Account (Screenshot 5:48)
    var bankAccounts by remember {
        mutableStateOf(
            listOf(
                BankAccount("HDFC Bank", "•••• 4912", "HDFC0001824", isPrimary = true, "Verified by Penny Drop"),
                BankAccount("State Bank of India", "•••• 8190", "SBIN0000412", isPrimary = false, "Verified via UPI")
            )
        )
    }
    var showAddBankDialog by remember { mutableStateOf(false) }
    var upiSettlementId by remember { mutableStateOf("gyanvidresses@hdfcbank") }

    // Module 5: Security & Privacy
    var twoFactorAuth by remember { mutableStateOf(true) }
    var biometricLogin by remember { mutableStateOf(true) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    // Module 6: Languages
    val availableLanguages = listOf(
        "हिन्दी (Hindi)" to "hi",
        "English" to "en",
        "বাংলা (Bengali)" to "bn",
        "मराठी (Marathi)" to "mr",
        "ગુજરાતી (Gujarati)" to "gu",
        "தமிழ் (Tamil)" to "ta",
        "తెలుగు (Telugu)" to "te",
        "ਪੰਜਾਬੀ (Punjabi)" to "pa"
    )
    var selectedLanguage by remember { mutableStateOf("हिन्दी (Hindi)") }

    // Module 7 & 8: Store & Order Preferences
    var storeDisplayName by remember { mutableStateOf("Gyanvi Dresses - Premium Ethnic Wear") }
    var storeTagline by remember { mutableStateOf("Direct manufacturer of Kurtis, Anarkalis & Banarasi Sarees") }
    var sameDayCutoffTime by remember { mutableStateOf("04:00 PM IST") }
    var minOrderValue by remember { mutableStateOf("₹0 (No Minimum)") }

    // Module 9 & 10: Shipping, Warehouse & Return
    var courierPriority by remember { mutableStateOf("Shadowfax (Fastest) > Delhivery > Ekart") }
    var shippingLabelFormat by remember { mutableStateOf("4 x 6 inch Thermal Sticker") }
    var returnWindowDays by remember { mutableStateOf("7 Days from Delivery") }
    var mandatoryBarcodeRTO by remember { mutableStateOf(true) }

    // Module 11: Tax & Invoicing
    var invoicePrefix by remember { mutableStateOf("GD-2026-") }
    var hsnCodeEthnicKurtis by remember { mutableStateOf("6204") }
    var hsnCodeSarees by remember { mutableStateOf("5208") }
    var gstSlabRate by remember { mutableStateOf("5% (up to ₹1000) / 12% (> ₹1000)") }

    // Module 12: Team & Staff
    var staffList by remember {
        mutableStateOf(
            listOf(
                StaffMember("EMP-01", "Rajesh Kumar", "Warehouse & Dispatch Manager", "+91 98765 43210", "Packing, Labels, RTO Claims"),
                StaffMember("EMP-02", "Pooja Devi", "Catalog & Inventory Specialist", "+91 98123 45678", "Product Uploads, Stock Updates")
            )
        )
    }
    var showAddStaffDialog by remember { mutableStateOf(false) }

    // Module 15: Deactivation / Logout
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("settings_back_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Settings", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                            Text("Store preferences, notifications & account", fontSize = 11.sp, color = TextSecondary)
                        }

                        // Quick Language Toggle Chip
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFDF2F8),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBCFE8)),
                            modifier = Modifier.clickable {
                                selectedLanguage = if (selectedLanguage.startsWith("हिन्दी")) "English" else "हिन्दी (Hindi)"
                                Toast.makeText(context, "Language switched to $selectedLanguage", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (selectedLanguage.startsWith("हिन्दी")) "हिन्दी" else "Eng", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                            }
                        }
                    }

                    // Section Quick Scroll Tabs
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sectionTabs) { tab ->
                            val isSelected = selectedSectionTab == tab
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSectionTab = tab },
                                label = { Text(tab, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MeeshoMagenta,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // =================================================================
            // 1. STORE & LEGAL IDENTITY (Screenshot 5:48)
            // =================================================================
            if (selectedSectionTab in listOf("All Sections", "Store & Legal")) {
                SettingsCard(title = "Store & Legal Identity", icon = Icons.Default.Storefront) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFDF2F8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(storeName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Text("Owner: $ownerName • Bettiah, Bihar", fontSize = 12.sp, color = TextSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("GSTIN: $gstinNumber", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Verified", color = Color(0xFF16A34A), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                        IconButton(onClick = { showEditProfileDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MeeshoMagenta, modifier = Modifier.size(20.dp))
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                    // Secondary Details Grid
                    DetailRow(label = "Business PAN", value = panNumber, badge = "Active")
                    DetailRow(label = "Registered Address", value = businessAddress)
                    DetailRow(label = "Contact Mobile", value = contactPhone, verified = true)
                    DetailRow(label = "Support Email", value = contactEmail, verified = true)
                    DetailRow(label = "Business Category", value = "Women's Ethnic Wear Manufacturing")

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MeeshoMagenta)
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Store Profile & Documents", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // =================================================================
            // 2. OPERATIONAL CONTROLS (Screenshot 5:48)
            // =================================================================
            if (selectedSectionTab in listOf("All Sections", "Operations")) {
                SettingsCard(title = "Operational Controls", icon = Icons.Default.Tune) {
                    // Vacation / Holiday Mode
                    SwitchRow(
                        title = "Vacation / Holiday Mode",
                        subtitle = "Temporarily pause incoming orders without affecting seller rating",
                        checked = vacationMode,
                        onCheckedChange = {
                            vacationMode = it
                            val msg = if (it) "Vacation Mode activated. Catalog temporarily paused." else "Store is now LIVE and accepting orders!"
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    )

                    if (vacationMode) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Vacation active: Buyers will see your catalog as temporarily out-of-stock.", fontSize = 11.sp, color = Color(0xFFB45309))
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    // Auto-Accept Prepaid Orders
                    SwitchRow(
                        title = "Auto-Accept Prepaid Orders",
                        subtitle = "Immediately allocate stock & generate shipping labels",
                        checked = autoAcceptPrepaid,
                        onCheckedChange = { autoAcceptPrepaid = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    // Auto-Accept COD Orders
                    SwitchRow(
                        title = "Auto-Accept COD Orders",
                        subtitle = "Auto-confirm COD orders after RTO risk algorithm check",
                        checked = autoAcceptCOD,
                        onCheckedChange = { autoAcceptCOD = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    // New Order Sound Alert
                    SwitchRow(
                        title = "New Order Sound Alert",
                        subtitle = "Ring loud alert when customer buys",
                        checked = newOrderSoundAlert,
                        onCheckedChange = { newOrderSoundAlert = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    // Automatic Stock Management
                    SwitchRow(
                        title = "Automatic Stock Sync",
                        subtitle = "Auto-deduct Kurti & Saree inventory upon successful checkout",
                        checked = autoStockManagement,
                        onCheckedChange = { autoStockManagement = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    // Low Stock Alert Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Low Stock Warning Alert", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("${lowStockThreshold.toInt()} units", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                        }
                        Text("Trigger warning notification when SKU stock drops below this limit", fontSize = 11.sp, color = TextSecondary)
                        Slider(
                            value = lowStockThreshold,
                            onValueChange = { lowStockThreshold = it },
                            valueRange = 1f..25f,
                            steps = 23,
                            colors = SliderDefaults.colors(thumbColor = MeeshoMagenta, activeTrackColor = MeeshoMagenta)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    // Auto Shipping Label Generation
                    SwitchRow(
                        title = "Auto Thermal Label Generation",
                        subtitle = "Create 4x6 inch courier barcode sticker immediately after pack confirmation",
                        checked = autoThermalLabelGen,
                        onCheckedChange = { autoThermalLabelGen = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    // Order Cancellation Grace Period
                    SwitchRow(
                        title = "Buyer 15-Min Cancellation Window",
                        subtitle = "Allow buyers to cancel accidental orders before warehouse packaging starts",
                        checked = cancellationGracePeriod,
                        onCheckedChange = { cancellationGracePeriod = it }
                    )
                }
            }

            // =================================================================
            // 3. NOTIFICATION SETTINGS (WhatsApp Daily Summary, Orders, SMS)
            // =================================================================
            if (selectedSectionTab in listOf("All Sections", "Notifications")) {
                SettingsCard(title = "Notification Settings", icon = Icons.Default.Notifications) {
                    SwitchRow(
                        title = "WhatsApp Daily Summary",
                        subtitle = "Receive dispatch reminder & bank payout notification",
                        checked = whatsappDailySummary,
                        onCheckedChange = { whatsappDailySummary = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    SwitchRow(
                        title = "New Order Instant Push Notification",
                        subtitle = "Instant alert for new Kurti, Saree and Combo orders",
                        checked = pushNewOrder,
                        onCheckedChange = { pushNewOrder = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    SwitchRow(
                        title = "Bank Payout & Settlement Alert",
                        subtitle = "WhatsApp & SMS notification on every Friday payout",
                        checked = pushPaymentReceived,
                        onCheckedChange = { pushPaymentReceived = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    SwitchRow(
                        title = "Return & RTO Early Warning Alert",
                        subtitle = "Notify when buyer initiates return or courier marks delivery attempt failed",
                        checked = pushReturnRTO,
                        onCheckedChange = { pushReturnRTO = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    SwitchRow(
                        title = "Courier Pickup Partner Alert (SMS)",
                        subtitle = "SMS when Shadowfax / Delhivery van arrives at Bettiah warehouse",
                        checked = smsDispatchPickup,
                        onCheckedChange = { smsDispatchPickup = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    SwitchRow(
                        title = "Weekly Business PDF Digest (Email)",
                        subtitle = "Receive financial P&L and GST summary on registered email",
                        checked = emailWeeklyDigest,
                        onCheckedChange = { emailWeeklyDigest = it }
                    )
                }
            }

            // =================================================================
            // 4. SETTLEMENT BANK ACCOUNT (Screenshot 5:48)
            // =================================================================
            if (selectedSectionTab in listOf("All Sections", "Bank & Payout")) {
                SettingsCard(title = "Settlement Bank Account", icon = Icons.Default.AccountBalance) {
                    Text("Weekly sales payouts are settled to your primary account every Friday morning.", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    bankAccounts.forEach { bank ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (bank.isPrimary) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (bank.isPrimary) Color(0xFFBBF7D0) else Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = if (bank.isPrimary) Color(0xFF16A34A) else TextSecondary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${bank.bankName} ${bank.accountNumberMasked}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        if (bank.isPrimary) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = Color(0xFFDCFCE7),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("Primary", color = Color(0xFF16A34A), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Text("IFSC: ${bank.ifsc} • ${bank.verificationMethod}", fontSize = 11.sp, color = TextSecondary)
                                }

                                if (!bank.isPrimary) {
                                    TextButton(onClick = {
                                        bankAccounts = bankAccounts.map { it.copy(isPrimary = it.accountNumberMasked == bank.accountNumberMasked) }
                                        Toast.makeText(context, "${bank.bankName} set as Primary Settlement Account", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Text("Set Primary", fontSize = 11.sp, color = MeeshoMagenta)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // UPI ID Settlement Option
                    DetailRow(label = "Instant Payout UPI ID", value = upiSettlementId, verified = true)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showAddBankDialog = true },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Add Bank", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { Toast.makeText(context, "Opening Bank Settlement & UTR History...", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Payout Logs", fontSize = 12.sp)
                        }
                    }
                }
            }

            // =================================================================
            // 5. SECURITY & PRIVACY
            // =================================================================
            if (selectedSectionTab in listOf("All Sections", "Security")) {
                SettingsCard(title = "Security & Privacy", icon = Icons.Default.Security) {
                    SwitchRow(
                        title = "Two-Factor Authentication (2FA)",
                        subtitle = "Require 6-digit OTP on mobile for financial changes & payouts",
                        checked = twoFactorAuth,
                        onCheckedChange = { twoFactorAuth = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    SwitchRow(
                        title = "Biometric / Fingerprint Login",
                        subtitle = "Quick fingerprint unlock for seller hub dashboard",
                        checked = biometricLogin,
                        onCheckedChange = { biometricLogin = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    // Active Sessions
                    Text("Active Login Sessions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))

                    SessionRow(
                        device = "Samsung Galaxy S24 Ultra (Android 14)",
                        location = "Bettiah, Bihar • Current App",
                        isCurrent = true
                    )
                    SessionRow(
                        device = "Chrome on Windows 11 PC",
                        location = "Patna, Bihar • Last active 2 hours ago",
                        isCurrent = false
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showChangePasswordDialog = true },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Password", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { Toast.makeText(context, "Logged out of all other active sessions!", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                        ) {
                            Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Logout Other Devices", fontSize = 11.sp, color = Color(0xFFDC2626))
                        }
                    }
                }
            }

            // =================================================================
            // 6. LANGUAGE SETTINGS (8 Languages)
            // =================================================================
            if (selectedSectionTab in listOf("All Sections", "Languages")) {
                SettingsCard(title = "Language Settings (भाषा चयन)", icon = Icons.Default.Language) {
                    Text("Select preferred language for seller dashboard, reports & notices:", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    availableLanguages.chunked(2).forEach { rowPair ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowPair.forEach { (langName, _) ->
                                val isSelected = selectedLanguage == langName
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Color(0xFFFDF2F8) else CardWhite,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MeeshoMagenta else Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedLanguage = langName
                                            Toast.makeText(context, "Language set to $langName", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                selectedLanguage = langName
                                                Toast.makeText(context, "Language set to $langName", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = MeeshoMagenta)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(langName, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MeeshoMagenta else TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // 7 & 8. STORE PREFERENCES & ORDER CONTROLS
            // =================================================================
            if (selectedSectionTab in listOf("All Sections", "Orders & Shipping")) {
                SettingsCard(title = "Store & Order Preferences", icon = Icons.Default.ShoppingCart) {
                    DetailRow(label = "Store Display Name", value = storeDisplayName)
                    DetailRow(label = "Store Headline", value = storeTagline)
                    DetailRow(label = "Same-Day Dispatch Cut-off", value = sameDayCutoffTime)
                    DetailRow(label = "Minimum Order Value (MOV)", value = minOrderValue)
                    DetailRow(label = "Default Currency", value = "INR (₹) Indian Rupee")
                    DetailRow(label = "Timezone & Clock", value = "Asia/Kolkata (IST +05:30)")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Cancellation Policy:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Customers can cancel orders freely within 15 minutes of placement or until the shipping label is scanned by warehouse staff.", fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
                }

                // =================================================================
                // 9 & 10. SHIPPING, WAREHOUSE & RETURN POLICIES
                // =================================================================
                SettingsCard(title = "Shipping, Pickup & Return Policy", icon = Icons.Default.LocalShipping) {
                    DetailRow(label = "Primary Pickup Warehouse", value = "Bettiah Factory Warehouse (Bihar - 845438)")
                    DetailRow(label = "Priority Logistics Partners", value = courierPriority)
                    DetailRow(label = "Shipping Label Format", value = shippingLabelFormat)
                    DetailRow(label = "Customer Return Window", value = returnWindowDays)
                    DetailRow(label = "Damaged / Wrong Return SLA", value = "72 Hours Claim Window with Video")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    SwitchRow(
                        title = "Mandatory Barcoded Packaging",
                        subtitle = "Require scanning barcode bags for zero-deduction RTO dispute protection",
                        checked = mandatoryBarcodeRTO,
                        onCheckedChange = { mandatoryBarcodeRTO = it }
                    )
                }
            }

            // =================================================================
            // 11. TAX & INVOICE SETTINGS
            // =================================================================
            if (selectedSectionTab in listOf("All Sections", "Tax & Invoice")) {
                SettingsCard(title = "Tax, GST & Invoicing", icon = Icons.Default.Receipt) {
                    DetailRow(label = "Registered GSTIN", value = gstinNumber, verified = true)
                    DetailRow(label = "Tax Jurisdiction", value = "Bihar Commercial Taxes Dept (State Code 10)")
                    DetailRow(label = "Apparel HSN Codes", value = "6204 (Kurtis), 5208 (Cotton Sarees)")
                    DetailRow(label = "Standard GST Rates", value = gstSlabRate)
                    DetailRow(label = "Tax Invoice Series Prefix", value = invoicePrefix)

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { Toast.makeText(context, "Downloading GSTR-1, GSTR-3B & TCS Summary Sheet...", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download Monthly GST & TCS Reports (Excel)", fontSize = 12.sp)
                    }
                }
            }

            // =================================================================
            // 12. TEAM & STAFF MANAGEMENT
            // =================================================================
            if (selectedSectionTab in listOf("All Sections", "Staff & Team")) {
                SettingsCard(title = "Team & Staff Management", icon = Icons.Default.Group) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Authorized Store Staff (${staffList.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        TextButton(onClick = { showAddStaffDialog = true }) {
                            Text("+ Add Member", fontSize = 12.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                        }
                    }

                    staffList.forEach { staff ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEDE7F6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(staff.name.take(1), fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(staff.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    Text("${staff.role} • ${staff.phone}", fontSize = 11.sp, color = TextSecondary)
                                    Text("Permissions: ${staff.permissions}", fontSize = 10.sp, color = Color(0xFF16A34A))
                                }
                                IconButton(onClick = {
                                    staffList = staffList.filter { it.id != staff.id }
                                    Toast.makeText(context, "${staff.name} removed from store staff", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Remove Staff", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // 13. SUBSCRIPTION & BILLING
            // =================================================================
            if (selectedSectionTab in listOf("All Sections", "Billing & Plan")) {
                SettingsCard(title = "Subscription & Tier Plan", icon = Icons.Default.WorkspacePremium) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFDF2F8),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBCFE8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Stars, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Gyanvi Platinum Seller Tier", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MeeshoMagenta)
                                    Text("0% Marketplace Commission • Free Priority Logistics", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFFCE7F3))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Monthly Subscription Fee:", fontSize = 12.sp, color = TextSecondary)
                                Text("₹0 (Sponsored Partner)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Settlement Frequency:", fontSize = 12.sp, color = TextSecondary)
                                Text("Weekly (Every Friday)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { Toast.makeText(context, "Downloading Platform Invoices & Tax Receipts...", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Marketplace Platform Invoices", fontSize = 12.sp)
                    }
                }
            }

            // =================================================================
            // 14. HELP & SUPPORT QUICK LAUNCHER
            // =================================================================
            SettingsCard(title = "Help & Support Links", icon = Icons.AutoMirrored.Filled.Help) {
                Text("Need assistance with orders, returns, bank settlements or catalog?", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onBack() },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Seller Helpdesk", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { Toast.makeText(context, "Opening WhatsApp Support Chat...", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A))
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF16A34A))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp Chat", fontSize = 12.sp, color = Color(0xFF16A34A))
                    }
                }
            }

            // =================================================================
            // 15. ACCOUNT MANAGEMENT & LOGOUT (Screenshot 5:48)
            // =================================================================
            SettingsCard(title = "Account Management", icon = Icons.Default.ManageAccounts) {
                OutlinedButton(
                    onClick = { Toast.makeText(context, "Exporting full store data archive (JSON/CSV) to email...", Toast.LENGTH_LONG).show() },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download Full Store Data Archive", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showDeactivateDialog = true },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706))
                ) {
                    Icon(Icons.Default.PauseCircleOutline, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Temporarily Deactivate Store", fontSize = 12.sp, color = Color(0xFFD97706))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Log Out from Seller Hub (Exact match to Screenshot 5:48)
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CardWhite,
                        contentColor = Color(0xFFD32F2F)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("logout_seller_hub_btn")
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFD32F2F))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out from Seller Hub", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // =========================================================================
    // DIALOGS: EDIT PROFILE, ADD BANK, ADD STAFF, LOGOUT CONFIRMATION
    // =========================================================================

    // 1. Edit Profile Dialog
    if (showEditProfileDialog) {
        var tempStoreName by remember { mutableStateOf(storeName) }
        var tempOwnerName by remember { mutableStateOf(ownerName) }
        var tempAddress by remember { mutableStateOf(businessAddress) }
        var tempGstin by remember { mutableStateOf(gstinNumber) }
        var tempPan by remember { mutableStateOf(panNumber) }
        var tempEmail by remember { mutableStateOf(contactEmail) }
        var tempPhone by remember { mutableStateOf(contactPhone) }

        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Edit Store Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        IconButton(onClick = { showEditProfileDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempStoreName,
                        onValueChange = { tempStoreName = it },
                        label = { Text("Store Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempOwnerName,
                        onValueChange = { tempOwnerName = it },
                        label = { Text("Owner / Proprietor Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempAddress,
                        onValueChange = { tempAddress = it },
                        label = { Text("Factory / Business Address") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tempGstin,
                            onValueChange = { tempGstin = it },
                            label = { Text("GSTIN") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = tempPan,
                            onValueChange = { tempPan = it },
                            label = { Text("PAN") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempEmail,
                        onValueChange = { tempEmail = it },
                        label = { Text("Support Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempPhone,
                        onValueChange = { tempPhone = it },
                        label = { Text("Mobile Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEditProfileDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                storeName = tempStoreName
                                ownerName = tempOwnerName
                                businessAddress = tempAddress
                                gstinNumber = tempGstin
                                panNumber = tempPan
                                contactEmail = tempEmail
                                contactPhone = tempPhone
                                showEditProfileDialog = false
                                Toast.makeText(context, "Store profile updated successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }

    // 2. Add Bank Account Dialog
    if (showAddBankDialog) {
        var newBankName by remember { mutableStateOf("") }
        var newAccountNo by remember { mutableStateOf("") }
        var newIfsc by remember { mutableStateOf("") }
        var newBeneficiary by remember { mutableStateOf(ownerName) }

        Dialog(onDismissRequest = { showAddBankDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add Settlement Bank Account", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text("A ₹1 penny-drop test will be initiated to verify account holder name.", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newBankName,
                        onValueChange = { newBankName = it },
                        label = { Text("Bank Name (e.g. ICICI Bank, Axis Bank)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newAccountNo,
                        onValueChange = { newAccountNo = it },
                        label = { Text("Account Number") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newIfsc,
                        onValueChange = { newIfsc = it.uppercase() },
                        label = { Text("11-Digit IFSC Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newBeneficiary,
                        onValueChange = { newBeneficiary = it },
                        label = { Text("Beneficiary Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddBankDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newBankName.isNotBlank() && newAccountNo.length >= 4) {
                                    val masked = "•••• " + newAccountNo.takeLast(4)
                                    val newBank = BankAccount(newBankName, masked, newIfsc.ifBlank { "HDFC0001824" }, isPrimary = false, "Verified via Penny Drop (₹1.00)")
                                    bankAccounts = bankAccounts + newBank
                                    showAddBankDialog = false
                                    Toast.makeText(context, "Bank $newBankName added & verified via Penny Drop!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please enter valid bank details", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                        ) {
                            Text("Verify & Save")
                        }
                    }
                }
            }
        }
    }

    // 3. Add Staff Dialog
    if (showAddStaffDialog) {
        var staffNameInput by remember { mutableStateOf("") }
        var staffRoleInput by remember { mutableStateOf("Dispatch & Packaging Specialist") }
        var staffPhoneInput by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddStaffDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add Staff / Employee Access", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text("Grant operational login to packing or catalog staff without exposing banking data.", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = staffNameInput,
                        onValueChange = { staffNameInput = it },
                        label = { Text("Staff Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = staffPhoneInput,
                        onValueChange = { staffPhoneInput = it },
                        label = { Text("Mobile Number (For Login OTP)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = staffRoleInput,
                        onValueChange = { staffRoleInput = it },
                        label = { Text("Assigned Role / Department") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddStaffDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (staffNameInput.isNotBlank()) {
                                    val newMember = StaffMember(
                                        id = "EMP-0${staffList.size + 1}",
                                        name = staffNameInput,
                                        role = staffRoleInput,
                                        phone = staffPhoneInput.ifBlank { "+91 99887 76655" },
                                        permissions = "Order Processing & Shipping Labels"
                                    )
                                    staffList = staffList + newMember
                                    showAddStaffDialog = false
                                    Toast.makeText(context, "$staffNameInput added to store team with staff login credentials!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                        ) {
                            Text("Grant Access")
                        }
                    }
                }
            }
        }
    }

    // 4. Change Password Dialog
    if (showChangePasswordDialog) {
        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showChangePasswordDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Change Seller Password", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Current Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Strong Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showChangePasswordDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showChangePasswordDialog = false
                                Toast.makeText(context, "Password updated successfully! An SMS confirmation has been sent.", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                        ) {
                            Text("Update Password")
                        }
                    }
                }
            }
        }
    }

    // 5. Deactivation Dialog
    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            icon = { Icon(Icons.Default.PauseCircleFilled, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(36.dp)) },
            title = { Text("Deactivate Store Temporarily?", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Your product listings will be hidden from buyers. All pending orders and returns must still be fulfilled before complete deactivation.", fontSize = 13.sp, color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeactivateDialog = false
                        vacationMode = true
                        Toast.makeText(context, "Store deactivated. Vacation mode enabled.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                ) {
                    Text("Deactivate Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 6. Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(36.dp)) },
            title = { Text("Log Out from Seller Hub?", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Are you sure you want to log out of Gyanvi Dresses Seller Account on this device?", fontSize = 13.sp, color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        Toast.makeText(context, "Logged out successfully from Gyanvi Dresses Seller Hub", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Confirm Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// =========================================================================
// HELPER REUSABLE COMPONENTS
// =========================================================================

@Composable
fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MeeshoMagenta,
                checkedTrackColor = Color(0xFFFCE7F3),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFFE2E8F0)
            )
        )
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    verified: Boolean = false,
    badge: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary, textAlign = TextAlign.End)
            if (verified) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
            }
            if (badge != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(badge, color = Color(0xFF16A34A), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
fun SessionRow(
    device: String,
    location: String,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isCurrent) Icons.Default.PhoneAndroid else Icons.Default.Laptop,
            contentDescription = null,
            tint = if (isCurrent) MeeshoMagenta else TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(device, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(location, fontSize = 10.sp, color = TextSecondary)
        }
        if (isCurrent) {
            Surface(
                color = Color(0xFFFDF2F8),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("This Device", color = MeeshoMagenta, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}
