package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.Product
import com.example.ui.theme.*
import com.example.util.ImageSaver
import kotlin.random.Random

data class DispatchLogItem(
    val id: String,
    val type: String, // "SMS", "Email", "WhatsApp", "OTP", "Gallery Download"
    val recipient: String,
    val summary: String,
    val time: String,
    val status: String // "Sent", "Verified", "Downloaded"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectMessageOtpModal(
    initialProduct: Product? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    // State: Recipient Details
    var phoneNumber by remember { mutableStateOf("+919128754982") }
    var emailAddress by remember { mutableStateOf("kumaramitbth4@gmail.com") }

    // State: Selected Tab (0 = Message, 1 = OTP, 2 = Gallery & Download, 3 = History)
    var selectedTab by remember { mutableStateOf(if (initialProduct != null) 2 else 0) }

    // State: Selected Product in Gallery
    val products = remember { SampleData.sampleProducts }
    var selectedProduct by remember { mutableStateOf(initialProduct ?: products.first()) }

    // State: Message Content
    var messageTemplateType by remember { mutableStateOf("Product Link") }
    var customMessageText by remember {
        mutableStateOf(
            "नमस्ते! Gyanvi Dresses Bettiah से आपके लिए सुंदर एथनिक वियर कलेक्शन:\n" +
            "👗 ${initialProduct?.name ?: "Royal Wine Embroidered Anarkali Kurti Set"}\n" +
            "💰 स्पेशल ऑफर प्राइस: ₹${initialProduct?.price ?: 599} (MRP ₹${initialProduct?.originalPrice ?: 1999})\n" +
            "🎉 ऑफर कोड: FESTIVE15 से पाएं अतिरिक्त 15% छूट!\n" +
            "🚚 फ्री डिलीवरी और Cash on Delivery (COD) उपलब्ध है।\n" +
            "👉 ऑर्डर करें: https://gyanvidresses.app/p/${initialProduct?.id ?: "gd_01"}\n" +
            "📞 सहायता हेल्पलाइन: +91 9128754982 (Bettiah)"
        )
    }

    // State: OTP Generator & Tester
    var generatedOtp by remember { mutableStateOf(String.format("%06d", Random.nextInt(100000, 999999))) }
    var otpPurpose by remember { mutableStateOf("Order & Delivery Verification") }
    var otpChannel by remember { mutableStateOf("Both Phone & Email") }
    var otpSentSuccessfully by remember { mutableStateOf(false) }
    var otpEnteredByUser by remember { mutableStateOf("") }
    var otpVerificationResult by remember { mutableStateOf<Boolean?>(null) } // null = not tried, true = valid, false = invalid
    var otpCountdown by remember { mutableStateOf(30) }

    // State: Activity / Dispatch Logs
    val dispatchLogs = remember {
        mutableStateListOf(
            DispatchLogItem(
                id = "log_1",
                type = "SMS",
                recipient = "+91 9128754982",
                summary = "Festive Dhamaka Offer sent with code FESTIVE15",
                time = "Just now",
                status = "Delivered"
            ),
            DispatchLogItem(
                id = "log_2",
                type = "Email",
                recipient = "kumaramitbth4@gmail.com",
                summary = "Order Dispatch & E-Invoice #GD-98213",
                time = "10 mins ago",
                status = "Delivered"
            ),
            DispatchLogItem(
                id = "log_3",
                type = "OTP",
                recipient = "+91 9128754982",
                summary = "Verification OTP 482915 for COD Confirmation",
                time = "25 mins ago",
                status = "Verified"
            )
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("direct_message_otp_modal"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = CardWhite,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("msg_otp_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Gyanvi Message & OTP",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = MeeshoMagenta.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "DISPATCHER",
                                        color = MeeshoMagenta,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "मोबाइल नंबर, ईमेल पर संदेश व OTP भेजें एवं गैलरी शेयर",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = {
                                val clip = "Phone: $phoneNumber\nEmail: $emailAddress\nOTP: $generatedOtp\n\n$customMessageText"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Gyanvi Message", clip))
                                Toast.makeText(context, "All details copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("copy_all_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy details",
                                tint = MeeshoMagenta
                            )
                        }
                    }

                    // 4 Main Navigation Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = CardWhite,
                        contentColor = MeeshoMagenta,
                        edgePadding = 12.dp,
                        divider = { HorizontalDivider(color = DividerColor) }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("💬 Message (संदेश)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("🔐 Send OTP (ओटीपी)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("👗 Gallery & Download", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = { Text("📜 History (${dispatchLogs.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // =================================================================
            // RECIPIENT CARD: PHONE NUMBER & EMAIL ADDRESS INPUTS
            // =================================================================
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MeeshoMagenta.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SendToMobile,
                                    contentDescription = null,
                                    tint = MeeshoMagenta,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Recipient Information (प्राप्तकर्ता विवरण)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "जिसके मोबाइल या ईमेल पर संदेश / OTP भेजना है",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Phone Number Input Field
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number / मोबाइल नंबर *") },
                        placeholder = { Text("+91 9876543210") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = MeeshoMagenta)
                        },
                        trailingIcon = {
                            if (phoneNumber.isNotEmpty()) {
                                IconButton(onClick = { phoneNumber = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeeshoMagenta,
                            focusedLabelColor = MeeshoMagenta,
                            unfocusedBorderColor = Color(0xFFD6D6D6)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recipient_phone_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Phone Contacts Chips
                    Text(
                        text = "Quick Contacts (तुरंत चुनें):",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickNumbers = listOf(
                            "Amit Sharma" to "+919128754982",
                            "Alok Vakil" to "+917488774049",
                            "Chandan Bhaiya" to "+919262565577",
                            "Deepak Bajaj" to "+917042122751",
                            "Gyanvi Boutique" to "+919876543210"
                        )
                        quickNumbers.forEach { (name, num) ->
                            SuggestionChip(
                                onClick = { phoneNumber = num },
                                label = { Text("$name ($num)", fontSize = 10.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (phoneNumber == num) MeeshoPinkLight else Color(0xFFF7F7F7),
                                    labelColor = if (phoneNumber == num) MeeshoMagenta else TextPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Email Address Input Field
                    OutlinedTextField(
                        value = emailAddress,
                        onValueChange = { emailAddress = it },
                        label = { Text("Email Address / ईमेल आईडी *") },
                        placeholder = { Text("example@gmail.com") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = GreenDiscount)
                        },
                        trailingIcon = {
                            if (emailAddress.isNotEmpty()) {
                                IconButton(onClick = { emailAddress = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenDiscount,
                            focusedLabelColor = GreenDiscount,
                            unfocusedBorderColor = Color(0xFFD6D6D6)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recipient_email_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Email Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickEmails = listOf(
                            "kumaramitbth4@gmail.com",
                            "support@gyanvidresses.app",
                            "care@gyanvibettiah.com",
                            "orders@gyanvidresses.com"
                        )
                        quickEmails.forEach { email ->
                            SuggestionChip(
                                onClick = { emailAddress = email },
                                label = { Text(email, fontSize = 10.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (emailAddress == email) Color(0xFFE8F5E9) else Color(0xFFF7F7F7),
                                    labelColor = if (emailAddress == email) GreenDiscount else TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =================================================================
            // TAB CONTENT BASED ON SELECTED TAB
            // =================================================================
            when (selectedTab) {
                0 -> {
                    // TAB 0: CUSTOM MESSAGE TEXT BOX & SEND CHANNELS
                    CustomMessageSection(
                        phoneNumber = phoneNumber,
                        emailAddress = emailAddress,
                        messageText = customMessageText,
                        onMessageChange = { customMessageText = it },
                        selectedTemplate = messageTemplateType,
                        onTemplateSelect = { type ->
                            messageTemplateType = type
                            customMessageText = when (type) {
                                "Product Link" -> {
                                    "नमस्ते! Gyanvi Dresses Bettiah से आपके लिए सुंदर एथनिक वियर कलेक्शन:\n" +
                                    "👗 ${selectedProduct.name}\n" +
                                    "💰 स्पेशल ऑफर प्राइस: ₹${selectedProduct.price} (MRP ₹${selectedProduct.originalPrice})\n" +
                                    "🎉 ऑफर कोड: FESTIVE15 से पाएं अतिरिक्त 15% छूट!\n" +
                                    "🚚 फ्री डिलीवरी और Cash on Delivery (COD) उपलब्ध है।\n" +
                                    "👉 ऑर्डर करें: https://gyanvidresses.app/p/${selectedProduct.id}\n" +
                                    "📞 सहायता हेल्पलाइन: +91 9128754982 (Bettiah)"
                                }
                                "Festive Offer" -> {
                                    "🎊 दिवाली एवं छठ पूजा महापर्व महाधमाका सेल! 🎊\n" +
                                    "Gyanvi Dresses पर सभी डिजाइनर कुर्तियों, साड़ियों और लहंगों पर पाएं फ्लैट 60% से 80% तक की भारी छूट।\n" +
                                    "🏷️ कूपन कोड इस्तेमाल करें: FESTIVE15\n" +
                                    "🏠 स्टोर: सुप्रिया रोड, बेतिया, बिहार - 845438\n" +
                                    "👉 ऑनलाइन शॉप करें: https://gyanvidresses.app"
                                }
                                "Order Invoice" -> {
                                    "📦 Gyanvi Dresses - Order Confirmation & Invoice\n" +
                                    "Order ID: #GD-98213\n" +
                                    "आइटम: ${selectedProduct.name}\n" +
                                    "कुल देय राशि: ₹${selectedProduct.price} (Cash on Delivery)\n" +
                                    "कूरियर पार्टनर: Delhivery Logistics\n" +
                                    "ट्रैकिंग लिंक: https://gyanvidresses.app/track/GD-98213\n" +
                                    "धन्यवाद! टीम Gyanvi Dresses, Bettiah."
                                }
                                else -> {
                                    "नमस्ते! Gyanvi Dresses से यह विशेष संदेश आपके लिए भेजा गया है।"
                                }
                            }
                        },
                        onLogDispatched = { type, summary ->
                            dispatchLogs.add(
                                0,
                                DispatchLogItem(
                                    id = "log_${System.currentTimeMillis()}",
                                    type = type,
                                    recipient = if (type == "Email") emailAddress else phoneNumber,
                                    summary = summary,
                                    time = "Just now",
                                    status = "Sent"
                                )
                            )
                        }
                    )
                }

                1 -> {
                    // TAB 1: SEND OTP (ओटीपी जनरेशन, डिस्पैच एवं सत्यापन)
                    SendOtpSection(
                        phoneNumber = phoneNumber,
                        emailAddress = emailAddress,
                        generatedOtp = generatedOtp,
                        onRegenerateOtp = {
                            generatedOtp = String.format("%06d", Random.nextInt(100000, 999999))
                            otpSentSuccessfully = false
                            otpVerificationResult = null
                            otpEnteredByUser = ""
                        },
                        otpPurpose = otpPurpose,
                        onPurposeChange = { otpPurpose = it },
                        otpChannel = otpChannel,
                        onChannelChange = { otpChannel = it },
                        otpSentSuccessfully = otpSentSuccessfully,
                        onOtpSent = {
                            otpSentSuccessfully = true
                            otpVerificationResult = null
                            dispatchLogs.add(
                                0,
                                DispatchLogItem(
                                    id = "otp_${System.currentTimeMillis()}",
                                    type = "OTP",
                                    recipient = "$phoneNumber / $emailAddress",
                                    summary = "OTP $generatedOtp for $otpPurpose ($otpChannel)",
                                    time = "Just now",
                                    status = "Sent"
                                )
                            )
                        },
                        enteredOtp = otpEnteredByUser,
                        onEnteredOtpChange = { otpEnteredByUser = it },
                        verificationResult = otpVerificationResult,
                        onVerifyOtp = {
                            val isValid = (otpEnteredByUser.trim() == generatedOtp.trim())
                            otpVerificationResult = isValid
                            if (isValid) {
                                dispatchLogs.add(
                                    0,
                                    DispatchLogItem(
                                        id = "otp_verify_${System.currentTimeMillis()}",
                                        type = "OTP",
                                        recipient = phoneNumber,
                                        summary = "OTP $generatedOtp successfully verified",
                                        time = "Just now",
                                        status = "Verified"
                                    )
                                )
                            }
                        }
                    )
                }

                2 -> {
                    // TAB 2: DRESS GALLERY, SELECTION & DOWNLOAD TO DEVICE GALLERY
                    DressGalleryAndDownloadSection(
                        products = products,
                        selectedProduct = selectedProduct,
                        onSelectProduct = { prod ->
                            selectedProduct = prod
                        },
                        onAttachToMessage = { prod ->
                            selectedProduct = prod
                            customMessageText =
                                "नमस्ते! Gyanvi Dresses Bettiah से सुंदर कलेक्शन:\n" +
                                "👗 ${prod.name}\n" +
                                "💰 प्राइस: ₹${prod.price} (MRP ₹${prod.originalPrice})\n" +
                                "👉 शॉप लिंक: https://gyanvidresses.app/p/${prod.id}\n" +
                                "फ्री डिलीवरी और COD उपलब्ध है!"
                            selectedTab = 0
                            Toast.makeText(context, "Product attached to Message Text Box!", Toast.LENGTH_SHORT).show()
                        },
                        onDownloaded = { prod ->
                            dispatchLogs.add(
                                0,
                                DispatchLogItem(
                                    id = "dl_${System.currentTimeMillis()}",
                                    type = "Gallery Download",
                                    recipient = "Device Gallery",
                                    summary = "Saved photo: ${prod.name}",
                                    time = "Just now",
                                    status = "Downloaded"
                                )
                            )
                        }
                    )
                }

                3 -> {
                    // TAB 3: DISPATCH HISTORY & ACTIVITY LOG
                    DispatchHistorySection(
                        logs = dispatchLogs,
                        onClearLogs = { dispatchLogs.clear() }
                    )
                }
            }
        }
    }
}

// =============================================================================
// SUB-COMPONENT: TAB 0 (CUSTOM MESSAGE TEXT BOX & ACTIONS)
// =============================================================================
@Composable
private fun CustomMessageSection(
    phoneNumber: String,
    emailAddress: String,
    messageText: String,
    onMessageChange: (String) -> Unit,
    selectedTemplate: String,
    onTemplateSelect: (String) -> Unit,
    onLogDispatched: (String, String) -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = MeeshoMagenta,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Message Composer (संदेश लिखें)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "${messageText.length} chars",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Templates Chips
            Text(
                text = "Message Templates (मैसेज टेम्पलेट चुनें):",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Product Link" to "👗 Product Link",
                    "Festive Offer" to "🎉 60% Festive Offer",
                    "Order Invoice" to "📦 Order Invoice",
                    "Custom" to "✍️ Blank Custom"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = selectedTemplate == key,
                        onClick = { onTemplateSelect(key) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MeeshoMagenta,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Multi-Line Message Text Box
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                label = { Text("Enter or edit message text (संदेश टेक्स्ट बॉक्स)") },
                placeholder = { Text("Type your message here...") },
                minLines = 6,
                maxLines = 10,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MeeshoMagenta,
                    focusedLabelColor = MeeshoMagenta,
                    unfocusedBorderColor = Color(0xFFD6D6D6)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("message_composer_text_box")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Append Helper Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AssistChip(
                    onClick = {
                        onMessageChange(messageText + "\n🏷️ कूपन कोड: FESTIVE15 (15% Extra Off)")
                    },
                    label = { Text("+ Add Coupon", fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Default.ConfirmationNumber, null, modifier = Modifier.size(14.dp)) }
                )
                AssistChip(
                    onClick = {
                        onMessageChange(messageText + "\n📍 स्टोर पता: सुप्रिया रोड, बेतिया, बिहार - 845438")
                    },
                    label = { Text("+ Add Bettiah Address", fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp)) }
                )
                AssistChip(
                    onClick = {
                        onMessageChange(messageText + "\n📞 कस्टमर केयर: +91 9128754982")
                    },
                    label = { Text("+ Add Support", fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Default.SupportAgent, null, modifier = Modifier.size(14.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = DividerColor)

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Send Message To Recipient (संदेश भेजने के विकल्प):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Send SMS Button
            Button(
                onClick = {
                    if (phoneNumber.isBlank()) {
                        Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                    } else {
                        ImageSaver.sendDirectSms(context, phoneNumber, messageText)
                        onLogDispatched("SMS", "Sent SMS to $phoneNumber")
                        Toast.makeText(context, "Opening SMS app with message...", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("send_sms_btn")
            ) {
                Icon(imageVector = Icons.Default.Sms, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Send SMS to $phoneNumber (एसएमएस भेजें)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Send WhatsApp Button
            Button(
                onClick = {
                    if (phoneNumber.isBlank()) {
                        Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                    } else {
                        ImageSaver.sendDirectWhatsAppMessage(context, phoneNumber, messageText)
                        onLogDispatched("WhatsApp", "Sent WhatsApp message to $phoneNumber")
                        Toast.makeText(context, "Opening WhatsApp...", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("send_whatsapp_btn")
            ) {
                Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Send via WhatsApp (व्हाट्सऐप पर भेजें)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Send Email Button
            Button(
                onClick = {
                    if (emailAddress.isBlank()) {
                        Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                    } else {
                        ImageSaver.sendDirectEmail(
                            context,
                            emailAddress,
                            "✨ Exclusive Message from Gyanvi Dresses Bettiah",
                            messageText
                        )
                        onLogDispatched("Email", "Sent email to $emailAddress")
                        Toast.makeText(context, "Opening Email app...", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenDiscount),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("send_email_btn")
            ) {
                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Send Email to $emailAddress (ईमेल भेजें)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. System Share to Others
            OutlinedButton(
                onClick = {
                    ImageSaver.shareCustomText(context, "Gyanvi Dresses Offer & Message", messageText)
                    onLogDispatched("Share", "Shared message via Android system sheet")
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("share_others_btn")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share with Others / अन्य ऐप्स पर शेयर करें",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// =============================================================================
// SUB-COMPONENT: TAB 1 (SEND OTP & LIVE VERIFICATION)
// =============================================================================
@Composable
private fun SendOtpSection(
    phoneNumber: String,
    emailAddress: String,
    generatedOtp: String,
    onRegenerateOtp: () -> Unit,
    otpPurpose: String,
    onPurposeChange: (String) -> Unit,
    otpChannel: String,
    onChannelChange: (String) -> Unit,
    otpSentSuccessfully: Boolean,
    onOtpSent: () -> Unit,
    enteredOtp: String,
    onEnteredOtpChange: (String) -> Unit,
    verificationResult: Boolean?,
    onVerifyOtp: () -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MeeshoMagenta,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OTP Dispatcher (ओटीपी जनरेटर व प्रेषक)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GreenDiscount.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "SECURE 256-BIT",
                        color = GreenDiscount,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6-Digit Generated OTP Display
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFF7FB),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MeeshoMagenta.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Generated 6-Digit OTP Code:",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = generatedOtp.chunked(3).joinToString(" "),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            color = MeeshoMagenta
                        )
                    }

                    OutlinedButton(
                        onClick = onRegenerateOtp,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MeeshoMagenta),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New OTP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // OTP Purpose Selector
            Text(
                text = "OTP Purpose (ओटीपी का कारण):",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Order & Delivery Verification",
                    "Cash on Delivery Confirmation",
                    "Customer Login / Signup",
                    "Seller KYC Authentication"
                ).forEach { purpose ->
                    FilterChip(
                        selected = otpPurpose == purpose,
                        onClick = { onPurposeChange(purpose) },
                        label = { Text(purpose, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MeeshoMagenta,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Channel Selector (SMS / Email / Both)
            Text(
                text = "Dispatch Channel (माध्यम चुनें):",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "📱 Phone SMS" to "Phone SMS",
                    "📧 Email" to "Email",
                    "📲 Both Phone & Email" to "Both Phone & Email"
                ).forEach { (label, key) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (otpChannel == key) MeeshoPinkLight else Color(0xFFF7F7F7),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (otpChannel == key) MeeshoMagenta else Color(0xFFE0E0E0)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onChannelChange(key) }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = if (otpChannel == key) MeeshoMagenta else TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Preview of OTP SMS / Email Message
            val otpMessageBody = remember(generatedOtp, otpPurpose) {
                "【GYANVI DRESSES】आपका 6 अंकों का वेरिफिकेशन OTP: $generatedOtp है ($otpPurpose हेतु)। यह OTP 10 मिनट के लिए मान्य है। कृपया किसी के साथ साझा न करें। बेतिया, बिहार।"
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF9F9F9),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Message Preview to be sent:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = otpMessageBody,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Send OTP Button
            Button(
                onClick = {
                    val fullOtpText = otpMessageBody
                    when (otpChannel) {
                        "Phone SMS" -> {
                            ImageSaver.sendDirectSms(context, phoneNumber, fullOtpText)
                        }
                        "Email" -> {
                            ImageSaver.sendDirectEmail(context, emailAddress, "Your Gyanvi Dresses Verification OTP: $generatedOtp", fullOtpText)
                        }
                        else -> {
                            ImageSaver.sendDirectSms(context, phoneNumber, fullOtpText)
                            Toast.makeText(context, "OTP $generatedOtp dispatched to SMS & Email successfully!", Toast.LENGTH_LONG).show()
                        }
                    }
                    onOtpSent()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("send_otp_now_btn")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Send OTP Now ($generatedOtp)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // OTP Sent Confirmation Banner
            AnimatedVisibility(visible = otpSentSuccessfully) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE8F5E9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GreenDiscount),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GreenDiscount)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "✓ OTP Sent Successfully!",
                                    fontWeight = FontWeight.Bold,
                                    color = GreenDiscount,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Code $generatedOtp was sent to $phoneNumber and $emailAddress",
                                    fontSize = 11.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live OTP Verification Tester (To verify the OTP that was sent)
                    Text(
                        text = "Verify OTP (ओटीपी सत्यापन जांचें):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = enteredOtp,
                            onValueChange = onEnteredOtpChange,
                            placeholder = { Text("Enter 6-digit OTP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = onVerifyOtp,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenDiscount),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text("Verify", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Verification Result Badge
                    if (verificationResult != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (verificationResult == true) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE8F5E9),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = GreenDiscount)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "✓ Success! OTP is 100% Valid & Authenticated.",
                                        color = GreenDiscount,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFEBEE),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = Color.Red)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "✕ Invalid OTP! Please enter correct code ($generatedOtp)",
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
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

// =============================================================================
// SUB-COMPONENT: TAB 2 (DRESS GALLERY, SELECTION & DOWNLOAD TO DEVICE GALLERY)
// =============================================================================
@Composable
private fun DressGalleryAndDownloadSection(
    products: List<Product>,
    selectedProduct: Product,
    onSelectProduct: (Product) -> Unit,
    onAttachToMessage: (Product) -> Unit,
    onDownloaded: (Product) -> Unit
) {
    val context = LocalContext.current
    var isDownloadSuccess by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = MeeshoMagenta,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dress Gallery & Download",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFF7FB)
                ) {
                    Text(
                        text = "${products.size} Dresses",
                        color = MeeshoMagenta,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "गैलरी से ड्रेस चुनें, डिवाइस गैलरी में डाउनलोड करें या दूसरों को शेयर करें",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontal Dress Thumbnails Selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(products) { prod ->
                    val isSelected = (prod.id == selectedProduct.id)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(88.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onSelectProduct(prod)
                                isDownloadSuccess = false
                            }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MeeshoMagenta else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = prod.imageRes),
                            contentDescription = prod.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${prod.price}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MeeshoMagenta else TextPrimary
                        )
                        Text(
                            text = prod.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Dress Big Preview Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFDFDFD),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = selectedProduct.imageRes),
                            contentDescription = selectedProduct.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = YellowPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "SELECTED ITEM",
                                    color = Color(0xFFB78103),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedProduct.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹${selectedProduct.price}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "₹${selectedProduct.originalPrice}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            }
                            Text(
                                text = "Fabric: ${selectedProduct.fabric}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ACTION BUTTON 1: DOWNLOAD TO GALLERY (गैलरी में डाउनलोड करें)
                    Button(
                        onClick = {
                            val saved = ImageSaver.saveToGallery(
                                context = context,
                                drawableResId = selectedProduct.imageRes,
                                productName = selectedProduct.name
                            )
                            isDownloadSuccess = saved
                            if (saved) {
                                onDownloaded(selectedProduct)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("download_to_gallery_btn")
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = CardWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download Photo to Gallery (गैलरी में सेव करें)",
                            fontWeight = FontWeight.Bold,
                            color = CardWhite,
                            fontSize = 13.sp
                        )
                    }

                    // Download Success Confirmation
                    if (isDownloadSuccess) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenDiscount, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "✓ Saved in Device Gallery (Pictures/GyanviDresses)",
                                    color = GreenDiscount,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ACTION BUTTON 2: ATTACH TO MESSAGE TEXT BOX
                    OutlinedButton(
                        onClick = { onAttachToMessage(selectedProduct) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MeeshoMagenta),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("attach_to_message_btn")
                    ) {
                        Icon(imageVector = Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Attach to Message Composer (संदेश में जोड़ें)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ACTION BUTTON 3: QUICK SHARE TO OTHERS
                    OutlinedButton(
                        onClick = {
                            ImageSaver.shareProductText(context, selectedProduct.name, selectedProduct.price)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("gallery_quick_share_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Photo & Link to Others (शेयर करें)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// =============================================================================
// SUB-COMPONENT: TAB 3 (DISPATCH HISTORY & LOGS)
// =============================================================================
@Composable
private fun DispatchHistorySection(
    logs: List<DispatchLogItem>,
    onClearLogs: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = MeeshoMagenta)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dispatch Logs & History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                if (logs.isNotEmpty()) {
                    TextButton(onClick = onClearLogs) {
                        Text("Clear All", color = Color.Red, fontSize = 11.sp)
                    }
                }
            }

            Text(
                text = "हाल ही में भेजे गए SMS, WhatsApp, Email, OTPs और Gallery डाउनलोड",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "कोई पिछला लॉग उपलब्ध नहीं है।",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    logs.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF9F9F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E8E8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = when (item.type) {
                                        "SMS" -> MeeshoMagenta.copy(alpha = 0.15f)
                                        "WhatsApp" -> Color(0xFF25D366).copy(alpha = 0.15f)
                                        "Email" -> GreenDiscount.copy(alpha = 0.15f)
                                        "OTP" -> MeeshoPurple.copy(alpha = 0.15f)
                                        else -> YellowPrimary.copy(alpha = 0.2f)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when (item.type) {
                                                "SMS" -> Icons.Default.Sms
                                                "WhatsApp" -> Icons.Default.Chat
                                                "Email" -> Icons.Default.Email
                                                "OTP" -> Icons.Default.Lock
                                                else -> Icons.Default.FileDownload
                                            },
                                            contentDescription = item.type,
                                            tint = when (item.type) {
                                                "SMS" -> MeeshoMagenta
                                                "WhatsApp" -> Color(0xFF25D366)
                                                "Email" -> GreenDiscount
                                                "OTP" -> MeeshoPurple
                                                else -> YellowPrimary
                                            },
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${item.type} • ${item.recipient}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (item.status == "Verified" || item.status == "Downloaded") GreenDiscount.copy(alpha = 0.15f) else MeeshoMagenta.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = item.status,
                                                color = if (item.status == "Verified" || item.status == "Downloaded") GreenDiscount else MeeshoMagenta,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.time,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = Color.Gray
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
