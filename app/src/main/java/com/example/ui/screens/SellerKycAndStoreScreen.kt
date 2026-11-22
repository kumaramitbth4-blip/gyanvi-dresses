package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerKycAndStoreScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Registration & KYC, 1 = Address & GST, 2 = Bank & UPI

    // Seller Registration & Store Branding
    var shopName by remember { mutableStateOf("GYANVI DRESSES BOUTIQUE") }
    var ownerName by remember { mutableStateOf("Amit Kumar Patwa") }
    var sellerPhone by remember { mutableStateOf("+91 9128754982") }
    var sellerEmail by remember { mutableStateOf("kumaramitbth4@gmail.com") }
    var isSellerRegistered by remember { mutableStateOf(true) }

    // Shop Logo & Shop Banner
    var shopLogoUri by remember { mutableStateOf<Uri?>(null) }
    var shopBannerUri by remember { mutableStateOf<Uri?>(null) }
    var selectedLogoColorIdx by remember { mutableIntStateOf(0) }
    var selectedBannerPresetIdx by remember { mutableIntStateOf(0) }

    // KYC Verification
    var panNumber by remember { mutableStateOf("ABCDE1234F") }
    var aadhaarLast4 by remember { mutableStateOf("4829") }
    var kycDocUri by remember { mutableStateOf<Uri?>(null) }
    var isKycVerified by remember { mutableStateOf(true) }

    // Business Address & GST Details
    var streetAddress by remember { mutableStateOf("Lal Bazar, Main Road, Near Station") }
    var city by remember { mutableStateOf("Bettiah") }
    var stateName by remember { mutableStateOf("Bihar") }
    var pincode by remember { mutableStateOf("845438") }
    var useAsPickupAddress by remember { mutableStateOf(true) }
    var gstNumber by remember { mutableStateOf("10ABCDE1234F1Z5") }
    var gstScheme by remember { mutableStateOf("Regular GST") }
    var defaultHsn by remember { mutableStateOf("6204 (Ethnic Dresses & Kurtis)") }
    var isGstVerified by remember { mutableStateOf(true) }

    // Bank Account & UPI Details
    var accountHolder by remember { mutableStateOf("Amit Kumar Patwa") }
    var bankName by remember { mutableStateOf("State Bank of India") }
    var accountNumber by remember { mutableStateOf("384910294829") }
    var ifscCode by remember { mutableStateOf("SBIN0001234") }
    var isBankVerified by remember { mutableStateOf(true) }

    var upiId by remember { mutableStateOf("gyanvidresses@okaxis") }
    var isUpiVerified by remember { mutableStateOf(true) }

    val logoColors = listOf(MeeshoMagenta, Color(0xFF1D4ED8), Color(0xFFB45309), Color(0xFF15803D))
    val bannerGradients = listOf(
        listOf(Color(0xFF880E4F), MeeshoMagenta, Color(0xFFF59E0B)),
        listOf(Color(0xFF1E3A8A), Color(0xFF6D28D9)),
        listOf(Color(0xFF065F46), Color(0xFF059669))
    )

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            shopLogoUri = uri
            Toast.makeText(context, "Shop Logo updated!", Toast.LENGTH_SHORT).show()
        }
    }

    val bannerPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            shopBannerUri = uri
            Toast.makeText(context, "Shop Banner updated!", Toast.LENGTH_SHORT).show()
        }
    }

    val kycDocPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            kycDocUri = uri
            Toast.makeText(context, "KYC Document attached!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 2.dp) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("seller_kyc_back")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Seller Registration & KYC Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Shop Logo, Banner, GST, Bank & UPI Details",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isKycVerified) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = if (isKycVerified) "KYC Verified ✓" else "KYC Pending",
                                color = if (isKycVerified) Color(0xFF166534) else Color(0xFF92400E),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = CardWhite,
                        contentColor = MeeshoMagenta
                    ) {
                        listOf("Shop & KYC", "Address & GST", "Bank & UPI").forEachIndexed { idx, label ->
                            Tab(
                                selected = selectedTab == idx,
                                onClick = { selectedTab = idx },
                                text = {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA),
        modifier = Modifier.fillMaxSize().testTag("seller_kyc_store_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Storefront Card Preview (Shop Banner + Shop Logo + Shop Name)
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                ) {
                    if (shopBannerUri != null) {
                        AsyncImage(
                            model = shopBannerUri,
                            contentDescription = "Shop Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(bannerGradients[selectedBannerPresetIdx]))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.28f))
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shop Logo
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(logoColors[selectedLogoColorIdx])
                                .border(2.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (shopLogoUri != null) {
                                AsyncImage(
                                    model = shopLogoUri,
                                    contentDescription = "Shop Logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = shopName.take(2).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = shopName.ifBlank { "GYANVI DRESSES" },
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Owner: $ownerName • $city, $stateName",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(shape = RoundedCornerShape(6.dp), color = YellowPrimary) {
                                    Text(
                                        text = if (isSellerRegistered) "ACTIVE SELLER" else "PENDING REGISTRATION",
                                        color = TextPrimary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (isGstVerified) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                                        Text(
                                            text = "GST VERIFIED",
                                            color = Color(0xFF166534),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    // Tab 1: Seller Registration, Shop Name, Shop Logo, Shop Banner & KYC Verification
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("1. Seller Registration & Shop Identity", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)

                            OutlinedTextField(
                                value = shopName,
                                onValueChange = { shopName = it },
                                label = { Text("Shop Name *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("seller_shop_name_input")
                            )
                            OutlinedTextField(
                                value = ownerName,
                                onValueChange = { ownerName = it },
                                label = { Text("Seller Full Name *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = sellerPhone,
                                    onValueChange = { sellerPhone = it },
                                    label = { Text("Mobile *") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = sellerEmail,
                                    onValueChange = { sellerEmail = it },
                                    label = { Text("Email *") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.2f)
                                )
                            }

                            HorizontalDivider()
                            Text("2. Shop Logo & Shop Banner", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = {
                                        logoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    modifier = Modifier.weight(1f).testTag("pick_shop_logo_btn")
                                ) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Upload Logo", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        bannerPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    modifier = Modifier.weight(1f).testTag("pick_shop_banner_btn")
                                ) {
                                    Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Upload Banner", fontSize = 12.sp)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Theme Presets:", fontSize = 11.sp, color = TextSecondary)
                                bannerGradients.indices.forEach { idx ->
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(bannerGradients[idx]))
                                            .border(
                                                if (selectedBannerPresetIdx == idx) 2.dp else 0.dp,
                                                TextPrimary,
                                                CircleShape
                                            )
                                            .clickable {
                                                shopBannerUri = null
                                                selectedBannerPresetIdx = idx
                                                selectedLogoColorIdx = idx % logoColors.size
                                            }
                                    )
                                }
                            }

                            HorizontalDivider()
                            Text("3. Seller KYC Verification", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = panNumber,
                                    onValueChange = { panNumber = it.uppercase() },
                                    label = { Text("PAN Card Number") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.2f)
                                )
                                OutlinedTextField(
                                    value = aadhaarLast4,
                                    onValueChange = { if (it.length <= 4) aadhaarLast4 = it },
                                    label = { Text("Aadhaar Last 4") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    kycDocPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (kycDocUri != null) "KYC Document Uploaded ✓ (Tap to Change)" else "Upload PAN / Cancelled Cheque Photo",
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = {
                                    isSellerRegistered = true
                                    isKycVerified = true
                                    Toast.makeText(context, "✓ Seller Registration & KYC Verified for $shopName!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                modifier = Modifier.fillMaxWidth().height(46.dp)
                            ) {
                                Text("Save Registration & Verify KYC", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                1 -> {
                    // Tab 2: Business Address & GST Details
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Business & Pickup Address", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)

                            OutlinedTextField(
                                value = streetAddress,
                                onValueChange = { streetAddress = it },
                                label = { Text("Registered Business Address *") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    label = { Text("City *") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = stateName,
                                    onValueChange = { stateName = it },
                                    label = { Text("State *") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = pincode,
                                    onValueChange = { if (it.length <= 6) pincode = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Pincode *") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.4f)) {
                                    Checkbox(checked = useAsPickupAddress, onCheckedChange = { useAsPickupAddress = it })
                                    Text("Same as Pickup Hub", fontSize = 12.sp)
                                }
                            }

                            HorizontalDivider()
                            Text("GST Tax Details", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)

                            OutlinedTextField(
                                value = gstNumber,
                                onValueChange = { gstNumber = it.uppercase() },
                                label = { Text("GSTIN Number (15-digit)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Regular GST", "Composition", "Unregistered / Enrolment").forEach { scheme ->
                                    FilterChip(
                                        selected = gstScheme == scheme,
                                        onClick = { gstScheme = scheme },
                                        label = { Text(scheme, fontSize = 10.sp) }
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = defaultHsn,
                                onValueChange = { defaultHsn = it },
                                label = { Text("Default Apparel HSN Code") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    isGstVerified = gstNumber.length >= 15
                                    Toast.makeText(context, "Business Address ($pincode) & GSTIN saved!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                modifier = Modifier.fillMaxWidth().height(46.dp)
                            ) {
                                Text("Save Address & Verify GSTIN", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                else -> {
                    // Tab 3: Bank Account & UPI Details
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Settlement Bank Account", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                if (isBankVerified) {
                                    Text("Penny-Drop Verified ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                }
                            }

                            OutlinedTextField(
                                value = accountHolder,
                                onValueChange = { accountHolder = it },
                                label = { Text("Account Holder Name *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = bankName,
                                onValueChange = { bankName = it },
                                label = { Text("Bank Name *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = accountNumber,
                                    onValueChange = { accountNumber = it },
                                    label = { Text("Account Number *") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.3f)
                                )
                                OutlinedTextField(
                                    value = ifscCode,
                                    onValueChange = { ifscCode = it.uppercase() },
                                    label = { Text("IFSC Code *") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Seller UPI Payout Details", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                if (isUpiVerified) {
                                    Text("UPI Active ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                }
                            }

                            OutlinedTextField(
                                value = upiId,
                                onValueChange = { upiId = it },
                                label = { Text("Primary Seller UPI ID (VPA)") },
                                placeholder = { Text("example@okaxis") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    isBankVerified = accountNumber.length >= 9 && ifscCode.length == 11
                                    isUpiVerified = upiId.contains("@")
                                    Toast.makeText(
                                        context,
                                        "Bank (${bankName}) & UPI ($upiId) verified & saved for payouts!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                modifier = Modifier.fillMaxWidth().height(46.dp)
                            ) {
                                Text("Verify & Save Bank / UPI", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
