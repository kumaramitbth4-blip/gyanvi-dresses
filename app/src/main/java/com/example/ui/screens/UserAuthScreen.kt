package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.AccountType
import com.example.data.local.AuthResult
import com.example.data.local.UserAccountEntity
import com.example.ui.theme.*
import com.example.util.GoogleSignInHelper
import com.example.viewmodel.LoginOtpSession
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kotlin.random.Random

private enum class ContactInputOption(val label: String) {
    MOBILE_ONLY("Mobile Number"),
    EMAIL_ONLY("Email ID"),
    BOTH("Mobile + Email")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAuthScreen(
    currentUser: UserAccountEntity?,
    registeredAccounts: List<UserAccountEntity>,
    initialAccountType: String = AccountType.CUSTOMER,
    initialRegisterMode: Boolean = false,
    isMandatoryGate: Boolean = false,
    loginOtpSession: LoginOtpSession = LoginOtpSession(),
    onBack: () -> Unit,
    onRegister: (
        accountType: String,
        fullName: String,
        mobileNumber: String,
        email: String,
        password: String,
        shopName: String,
        businessAddress: String,
        gstin: String,
        onResult: (AuthResult) -> Unit
    ) -> Unit,
    onLogin: (
        identifier: String,
        password: String,
        accountType: String?,
        onResult: (AuthResult) -> Unit
    ) -> Unit,
    onGenerateLoginOtp: (mobileNumber: String) -> Pair<Boolean, String> = { false to "" },
    onLoginWithOtp: (
        mobileNumber: String,
        enteredOtp: String,
        accountType: String,
        onResult: (AuthResult) -> Unit
    ) -> Unit = { _, _, _, _ -> },
    onClearLoginOtp: () -> Unit = {},
    onResetPassword: (
        identifier: String,
        newPassword: String,
        accountType: String?,
        onResult: (AuthResult) -> Unit
    ) -> Unit,
    onSwitchAccount: (UserAccountEntity) -> Unit,
    onDeleteAccount: (String) -> Unit,
    onLogout: () -> Unit,
    onGoogleSignInSuccess: (FirebaseUser, String) -> Unit = { _, _ -> }
) {
    BackHandler(enabled = !isMandatoryGate) { onBack() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isGoogleSigningIn by remember { mutableStateOf(false) }

    var selectedAccountType by remember(initialAccountType) { mutableStateOf(initialAccountType) }
    var isRegisterMode by remember(initialRegisterMode) { mutableStateOf(initialRegisterMode) }
    var useOtpLogin by remember { mutableStateOf(true) }
    var contactOption by remember { mutableStateOf(ContactInputOption.MOBILE_ONLY) }

    // Registration Fields
    var fullName by remember { mutableStateOf("") }
    var mobileInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Seller-Only Registration Fields
    var shopNameInput by remember { mutableStateOf("") }
    var businessAddressInput by remember { mutableStateOf("") }
    var gstinInput by remember { mutableStateOf("") }

    // Mobile OTP Login Fields
    var otpMobileInput by remember { mutableStateOf("") }
    var otpCodeInput by remember { mutableStateOf("") }

    // Password Login Fields
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Inline Feedback Banner
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Forgot Password via OTP Modal State
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetIdentifier by remember { mutableStateOf("") }
    var resetOtpCode by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("") }
    var resetOtpVerified by remember { mutableStateOf(false) }
    var resetNewPassword by remember { mutableStateOf("") }

    val isSeller = selectedAccountType == AccountType.SELLER

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = if (isMandatoryGate) 16.dp else 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isMandatoryGate) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("user_auth_back")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    }
                    Image(
                        painter = painterResource(id = R.drawable.gyanvi_brand_logo),
                        contentDescription = "Gyanvi Dresses Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MeeshoMagenta, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isMandatoryGate) "GYANVI DRESSES" else "Customer & Seller Accounts",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (isMandatoryGate) MeeshoMagenta else TextPrimary
                        )
                        Text(
                            text = if (isMandatoryGate) {
                                "Mobile Number डालकर OTP जनरेट करें और OTP बॉक्स में भरकर लॉगिन करें"
                            } else {
                                "Mobile OTP Login या Email + Password Login"
                            },
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    if (currentUser != null) {
                        TextButton(onClick = {
                            onLogout()
                            successMessage = "Signed out successfully."
                            errorMessage = null
                        }) {
                            Text("Sign Out", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFFFFBF0),
        modifier = Modifier
            .fillMaxSize()
            .testTag("user_auth_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Hero Header Banner with Active Session Summary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = if (isSeller) {
                                listOf(Color(0xFF312E81), Color(0xFF6D28D9))
                            } else {
                                listOf(Color(0xFF9F2089), Color(0xFFD81B60))
                            }
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.gyanvi_brand_logo),
                            contentDescription = "Gyanvi Dresses Official Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(2.dp, YellowPrimary, CircleShape)
                        )
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "GYANVI DRESSES",
                                    color = YellowPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = if (currentUser != null) "Active: ${currentUser.accountType}" else "Guest Session",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Fashion for Everyone • Marketplace",
                                color = Color.White.copy(alpha = 0.88f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Text(
                        text = if (currentUser != null) {
                            "Signed in as ${currentUser.fullName}"
                        } else {
                            "Create or Sign In to Your Account"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    if (currentUser != null) {
                        val contactText = buildList {
                            if (currentUser.mobileNumber.isNotBlank()) add("+91 ${currentUser.mobileNumber}")
                            if (currentUser.email.isNotBlank()) add(currentUser.email)
                        }.joinToString(" • ")
                        Text(
                            text = contactText + if (currentUser.shopName.isNotBlank()) " • ${currentUser.shopName}" else "",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = "Customer Shopping Account या Seller Store Account बनाएं",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 2. Choose Account Type: CUSTOMER vs SELLER
            Text(
                text = "1. Select Account Type (अकाउंट चुनें)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AccountTypeCard(
                    title = "Customer Account",
                    subtitle = "ग्राहक रजिस्ट्रेशन\nOrders, Wishlist & Wallet",
                    icon = Icons.Default.ShoppingBag,
                    selected = selectedAccountType == AccountType.CUSTOMER,
                    accentColor = MeeshoMagenta,
                    onClick = {
                        selectedAccountType = AccountType.CUSTOMER
                        errorMessage = null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("select_customer_account")
                )

                AccountTypeCard(
                    title = "Seller Account",
                    subtitle = "विक्रेता रजिस्ट्रेशन\nShop, Catalog & Payouts",
                    icon = Icons.Default.Storefront,
                    selected = selectedAccountType == AccountType.SELLER,
                    accentColor = Color(0xFF5B21B6),
                    onClick = {
                        selectedAccountType = AccountType.SELLER
                        errorMessage = null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("select_seller_account")
                )
            }

            // 2b. Fast Cloud Auth: Google Sign-In via Jetpack Credential Manager
            OutlinedButton(
                onClick = {
                    if (isGoogleSigningIn) return@OutlinedButton
                    isGoogleSigningIn = true
                    errorMessage = null
                    coroutineScope.launch {
                        val res = GoogleSignInHelper.signInWithGoogleExplicit(context)
                        isGoogleSigningIn = false
                        res.onSuccess { firebaseUser ->
                            successMessage = "Signed in with Google: ${firebaseUser.email ?: firebaseUser.displayName}"
                            onGoogleSignInSuccess(firebaseUser, selectedAccountType)
                        }.onFailure { err ->
                            errorMessage = err.localizedMessage ?: "Google Sign-In failed. Check Play Services & network."
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, Color(0xFF4285F4)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1F2937)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("google_sign_in_btn")
            ) {
                if (isGoogleSigningIn) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFF4285F4))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connecting Google Account...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF4285F4), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Continue with Google (Cloud Sync & Backup)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                }
            }

            // 3. Mode Tabs: Register vs Login
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TabRow(
                        selectedTabIndex = if (isRegisterMode) 0 else 1,
                        containerColor = Color(0xFFFDF2F8),
                        contentColor = if (isSeller) Color(0xFF5B21B6) else MeeshoMagenta,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = isRegisterMode,
                            onClick = {
                                isRegisterMode = true
                                errorMessage = null
                            },
                            text = {
                                Text(
                                    text = "Register (नया अकाउंट)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isRegisterMode) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.testTag("tab_register_account")
                        )
                        Tab(
                            selected = !isRegisterMode,
                            onClick = {
                                isRegisterMode = false
                                errorMessage = null
                            },
                            text = {
                                Text(
                                    text = "Sign In (लॉगिन)",
                                    fontSize = 12.sp,
                                    fontWeight = if (!isRegisterMode) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.testTag("tab_login_account")
                        )
                    }

                    // Inline Error / Success Banners
                    errorMessage?.let { err ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEE2E2),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFB91C1C), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(err, color = Color(0xFF991B1B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    successMessage?.let { ok ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(ok, color = Color(0xFF166534), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    if (isRegisterMode) {
                        // ==================== REGISTRATION FORM ====================
                        Text(
                            text = if (isSeller) {
                                "Create Seller Account (विक्रेता पंजीकरण)"
                            } else {
                                "Create Customer Account (ग्राहक पंजीकरण)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                errorMessage = null
                            },
                            label = { Text(if (isSeller) "Owner / Seller Full Name *" else "Customer Full Name *") },
                            placeholder = { Text("e.g. Priya Sharma / Rahul Verma") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MeeshoMagenta) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input")
                        )

                        // Option Picker: Mobile Number OR Email ID OR Both
                        Text(
                            text = "Register With (मोबाइल नंबर या ईमेल आईडी चुनें):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ContactInputOption.entries.forEach { opt ->
                                FilterChip(
                                    selected = contactOption == opt,
                                    onClick = {
                                        contactOption = opt
                                        errorMessage = null
                                    },
                                    label = { Text(opt.label, fontSize = 11.sp, maxLines = 1) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (isSeller) Color(0xFF5B21B6) else MeeshoMagenta,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        if (contactOption == ContactInputOption.MOBILE_ONLY || contactOption == ContactInputOption.BOTH) {
                            OutlinedTextField(
                                value = mobileInput,
                                onValueChange = { raw ->
                                    mobileInput = raw.filter { it.isDigit() || it == '+' }.take(13)
                                    errorMessage = null
                                },
                                label = { Text("Mobile Number *") },
                                placeholder = { Text("10-digit mobile number (e.g. 9876543210)") },
                                leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MeeshoMagenta) },
                                prefix = { Text("+91 ", fontWeight = FontWeight.Bold, color = TextSecondary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_mobile_input")
                            )
                        }

                        if (contactOption == ContactInputOption.EMAIL_ONLY || contactOption == ContactInputOption.BOTH) {
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = {
                                    emailInput = it
                                    errorMessage = null
                                },
                                label = { Text("Email ID *") },
                                placeholder = { Text("e.g. name@gmail.com") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MeeshoMagenta) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_email_input")
                            )
                        }

                        // Seller-Specific Shop & GST Details
                        AnimatedVisibility(visible = isSeller) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = shopNameInput,
                                    onValueChange = {
                                        shopNameInput = it
                                        errorMessage = null
                                    },
                                    label = { Text("Shop / Boutique Name *") },
                                    placeholder = { Text("e.g. Maa Durga Saree Center") },
                                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF5B21B6)) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("register_shop_input")
                                )
                                OutlinedTextField(
                                    value = businessAddressInput,
                                    onValueChange = { businessAddressInput = it },
                                    label = { Text("City / Business Address") },
                                    placeholder = { Text("e.g. Main Market, Bettiah, Bihar - 845438") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF5B21B6)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = gstinInput,
                                    onValueChange = { gstinInput = it.uppercase().take(15) },
                                    label = { Text("GSTIN Number (Optional)") },
                                    placeholder = { Text("15-digit GSTIN (optional for small sellers)") },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF5B21B6)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Password & Confirm Password Fields
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                errorMessage = null
                            },
                            label = { Text("Create Password * (min 6 chars)") },
                            placeholder = { Text("Enter strong password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MeeshoMagenta) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input")
                        )

                        // Password Strength Bar
                        if (passwordInput.isNotEmpty()) {
                            val strength = calculatePasswordStrength(passwordInput)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { strength.progress },
                                    color = strength.color,
                                    trackColor = Color(0xFFE5E7EB),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                                Text(
                                    text = strength.label,
                                    color = strength.color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        OutlinedTextField(
                            value = confirmPasswordInput,
                            onValueChange = {
                                confirmPasswordInput = it
                                errorMessage = null
                            },
                            label = { Text("Confirm Password *") },
                            leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MeeshoMagenta) },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (confirmPasswordVisible) "Hide confirm password" else "Show confirm password"
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            isError = confirmPasswordInput.isNotEmpty() && confirmPasswordInput != passwordInput,
                            supportingText = {
                                if (confirmPasswordInput.isNotEmpty() && confirmPasswordInput != passwordInput) {
                                    Text("Passwords do not match", color = Color(0xFFDC2626), fontSize = 11.sp)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_confirm_password_input")
                        )

                        Button(
                            onClick = {
                                if (passwordInput != confirmPasswordInput) {
                                    errorMessage = "Password and Confirm Password do not match."
                                    return@Button
                                }
                                val finalMobile = if (contactOption == ContactInputOption.EMAIL_ONLY) "" else mobileInput
                                val finalEmail = if (contactOption == ContactInputOption.MOBILE_ONLY) "" else emailInput

                                onRegister(
                                    selectedAccountType,
                                    fullName,
                                    finalMobile,
                                    finalEmail,
                                    passwordInput,
                                    shopNameInput,
                                    businessAddressInput,
                                    gstinInput
                                ) { result ->
                                    when (result) {
                                        is AuthResult.Success -> {
                                            errorMessage = null
                                            successMessage = result.message
                                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                            fullName = ""
                                            mobileInput = ""
                                            emailInput = ""
                                            passwordInput = ""
                                            confirmPasswordInput = ""
                                            shopNameInput = ""
                                            businessAddressInput = ""
                                            gstinInput = ""
                                        }
                                        is AuthResult.Error -> {
                                            successMessage = null
                                            errorMessage = result.reason
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSeller) Color(0xFF5B21B6) else MeeshoMagenta
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("register_submit_button")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSeller) "Register Seller Account" else "Register Customer Account",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        // ==================== LOGIN FORM (MOBILE OTP + PASSWORD) ====================
                        Text(
                            text = if (isSeller) "Sign In to Seller Account" else "Customer Login Dashboard (OTP Verify)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )

                        // Toggle between Mobile OTP Login (default) and Password Login
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = useOtpLogin,
                                onClick = {
                                    useOtpLogin = true
                                    errorMessage = null
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                label = { Text("Mobile + OTP Login", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (isSeller) Color(0xFF5B21B6) else MeeshoMagenta,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                modifier = Modifier.weight(1f).testTag("auth_mode_otp_chip")
                            )
                            FilterChip(
                                selected = !useOtpLogin,
                                onClick = {
                                    useOtpLogin = false
                                    errorMessage = null
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                label = { Text("Password Login", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (isSeller) Color(0xFF5B21B6) else MeeshoMagenta,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                modifier = Modifier.weight(1f).testTag("auth_mode_password_chip")
                            )
                        }

                        if (useOtpLogin) {
                            OutlinedTextField(
                                value = otpMobileInput,
                                onValueChange = { raw ->
                                    val digits = raw.filter { it.isDigit() }.take(10)
                                    otpMobileInput = digits
                                    errorMessage = null
                                    if (digits.length == 10) {
                                        if (!loginOtpSession.isOtpGenerated || loginOtpSession.mobileNumber != digits) {
                                            val (ok, res) = onGenerateLoginOtp(digits)
                                            if (ok) {
                                                otpCodeInput = ""
                                                successMessage = "6-Digit OTP Generated for +91 $digits!"
                                                Toast.makeText(context, "OTP Generated: $res", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else if (loginOtpSession.isOtpGenerated) {
                                        onClearLoginOtp()
                                        otpCodeInput = ""
                                        successMessage = null
                                    }
                                },
                                label = { Text("Enter 10-Digit Mobile Number *") },
                                placeholder = { Text("e.g. 9128754982") },
                                leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MeeshoMagenta) },
                                prefix = { Text("+91 ", fontWeight = FontWeight.Bold, color = TextSecondary) },
                                trailingIcon = {
                                    Text(
                                        text = "${otpMobileInput.length}/10",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (otpMobileInput.length == 10) Color(0xFF15803D) else TextMuted,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_login_mobile_input")
                            )

                            Button(
                                onClick = {
                                    val (ok, msgOrOtp) = onGenerateLoginOtp(otpMobileInput)
                                    if (ok) {
                                        otpCodeInput = ""
                                        errorMessage = null
                                        successMessage = "OTP Generated for +91 $otpMobileInput!"
                                        Toast.makeText(context, "Your OTP is: $msgOrOtp", Toast.LENGTH_LONG).show()
                                    } else {
                                        successMessage = null
                                        errorMessage = msgOrOtp
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFB300),
                                    contentColor = Color(0xFF1F2937)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("generate_otp_button")
                            ) {
                                Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (loginOtpSession.isOtpGenerated) "Resend / New OTP भेजें" else "Generate OTP (OTP प्राप्त करें)",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }

                            // OTP Verification Dashboard Box
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFFFBEB),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (loginOtpSession.isOtpGenerated) MeeshoMagenta else Color(0xFFFDE68A)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_dashboard_box")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.MarkEmailRead,
                                                contentDescription = null,
                                                tint = MeeshoMagenta,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = "OTP Verification Dashboard Box",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = TextPrimary
                                            )
                                        }
                                        if (loginOtpSession.isOtpGenerated) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFDCFCE7)
                                            ) {
                                                Text(
                                                    text = "OTP Active ✓",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF166534),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (loginOtpSession.isOtpGenerated && loginOtpSession.mobileNumber == otpMobileInput) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFECFDF5),
                                            border = BorderStroke(1.dp, Color(0xFF6EE7B7)),
                                            modifier = Modifier.fillMaxWidth().testTag("generated_otp_banner")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "SMS OTP for +91 ${loginOtpSession.mobileNumber}:",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF065F46)
                                                    )
                                                    Text(
                                                        text = loginOtpSession.generatedOtp,
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Black,
                                                        letterSpacing = 4.sp,
                                                        color = Color(0xFF047857)
                                                    )
                                                }
                                                OutlinedButton(
                                                    onClick = { otpCodeInput = loginOtpSession.generatedOtp },
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp).testTag("autofill_otp_btn")
                                                ) {
                                                    Text("Auto-Fill", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                                                }
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "10 अंकों का Mobile Number डालें — OTP अपने आप यहाँ दिखाई देगा।",
                                            fontSize = 11.sp,
                                            color = Color(0xFF92400E),
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Text(
                                        text = "नीचे दिए 6 बॉक्स में OTP नंबर डालें:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary
                                    )

                                    OtpSixBoxInput(
                                        otpValue = otpCodeInput,
                                        accentColor = if (isSeller) Color(0xFF5B21B6) else MeeshoMagenta,
                                        enabled = loginOtpSession.isOtpGenerated,
                                        onOtpChange = { updated ->
                                            otpCodeInput = updated
                                            errorMessage = null
                                        },
                                        modifier = Modifier.testTag("otp_login_code_input")
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    onLoginWithOtp(otpMobileInput, otpCodeInput, selectedAccountType) { result ->
                                        when (result) {
                                            is AuthResult.Success -> {
                                                errorMessage = null
                                                successMessage = result.message
                                                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                                otpCodeInput = ""
                                            }
                                            is AuthResult.Error -> {
                                                successMessage = null
                                                errorMessage = result.reason
                                            }
                                        }
                                    }
                                },
                                enabled = loginOtpSession.isOtpGenerated && otpCodeInput.length == 6,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSeller) Color(0xFF5B21B6) else MeeshoMagenta
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("otp_login_submit_button")
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSeller) "Verify OTP & Login Seller" else "Verify OTP & Login Customer",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            OutlinedTextField(
                                value = loginIdentifier,
                                onValueChange = {
                                    loginIdentifier = it
                                    errorMessage = null
                                },
                                label = { Text("Mobile Number or Email ID *") },
                                placeholder = { Text("9128754982 या kumaramitbth4@gmail.com") },
                                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MeeshoMagenta) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_identifier_input")
                            )

                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = {
                                    loginPassword = it
                                    errorMessage = null
                                },
                                label = { Text("Password *") },
                                placeholder = { Text("Enter account password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MeeshoMagenta) },
                                trailingIcon = {
                                    IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                        Icon(
                                            imageVector = if (loginPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (loginPasswordVisible) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_password_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        resetIdentifier = loginIdentifier
                                        generatedOtp = ""
                                        resetOtpCode = ""
                                        resetOtpVerified = false
                                        resetNewPassword = ""
                                        showForgotPasswordDialog = true
                                    },
                                    modifier = Modifier.testTag("forgot_password_btn")
                                ) {
                                    Text("Forgot Password? Reset via OTP", fontSize = 12.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    onLogin(loginIdentifier, loginPassword, selectedAccountType) { result ->
                                        when (result) {
                                            is AuthResult.Success -> {
                                                errorMessage = null
                                                successMessage = result.message
                                                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                                loginPassword = ""
                                            }
                                            is AuthResult.Error -> {
                                                successMessage = null
                                                errorMessage = result.reason
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSeller) Color(0xFF5B21B6) else MeeshoMagenta
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("login_submit_button")
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSeller) "Login as Seller" else "Login as Customer",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // 4. Saved / Registered Customer & Seller Accounts Directory (visible once logged in to prevent lock-gate bypass)
            if (!isMandatoryGate) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Registered Accounts (${registeredAccounts.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Saved locally in Room Database • Tap to Switch",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF3E8FF)
                        ) {
                            Text(
                                text = "Customer + Seller",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6D28D9),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (registeredAccounts.isEmpty()) {
                        Text(
                            text = "No accounts registered yet. Create a Customer or Seller account above.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    } else {
                        registeredAccounts.forEach { acc ->
                            val isActive = currentUser?.id == acc.id
                            val isSellerAcc = acc.accountType == AccountType.SELLER
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isActive) Color(0xFFFDF2F8) else Color(0xFFF9FAFB),
                                border = BorderStroke(
                                    1.dp,
                                    if (isActive) MeeshoMagenta else Color(0xFFE5E7EB)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isSellerAcc) Color(0xFF5B21B6) else MeeshoMagenta),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSellerAcc) Icons.Default.Storefront else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = if (isSellerAcc && acc.shopName.isNotBlank()) acc.shopName else acc.fullName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (isSellerAcc) Color(0xFFEDE9FE) else Color(0xFFFCE7F3)
                                            ) {
                                                Text(
                                                    text = acc.accountType,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isSellerAcc) Color(0xFF5B21B6) else MeeshoMagenta,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        val contactSummary = buildList {
                                            if (acc.mobileNumber.isNotBlank()) add("+91 ${acc.mobileNumber}")
                                            if (acc.email.isNotBlank()) add(acc.email)
                                        }.joinToString(" • ")

                                        Text(
                                            text = contactSummary,
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (isActive) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFDCFCE7)
                                        ) {
                                            Text(
                                                text = "Active ✓",
                                                color = Color(0xFF166534),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else {
                                        TextButton(
                                            onClick = {
                                                onSwitchAccount(acc)
                                                successMessage = "Switched to ${acc.fullName} (${acc.accountType})"
                                                errorMessage = null
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("Switch", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            onDeleteAccount(acc.id)
                                            Toast.makeText(context, "Removed ${acc.fullName}", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Account", tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            } else {
                // Locked front-page gate notice reminding user that registration or password login opens the app
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Mobile Number डालकर OTP जनरेट करें और डैशबोर्ड के 6-बॉक्स में OTP डालकर Verify करें — तभी Customer Login होगा और Gyanvi Dresses ऐप खुलेगा।",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF78350F),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Forgot Password via OTP Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password via OTP", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter your registered Mobile Number or Email ID to verify OTP and set a new password.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = resetIdentifier,
                        onValueChange = { resetIdentifier = it },
                        label = { Text("Mobile Number or Email ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = {
                                if (resetIdentifier.isNotBlank()) {
                                    generatedOtp = String.format("%06d", Random.nextInt(100000, 999999))
                                    resetOtpVerified = false
                                    Toast.makeText(context, "OTP sent to $resetIdentifier: $generatedOtp", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (generatedOtp.isEmpty()) "Send OTP" else "Resend OTP", fontSize = 12.sp)
                        }
                        if (generatedOtp.isNotEmpty()) {
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFEF3C7)) {
                                Text(
                                    text = "OTP: $generatedOtp",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    if (generatedOtp.isNotEmpty()) {
                        OutlinedTextField(
                            value = resetOtpCode,
                            onValueChange = {
                                resetOtpCode = it.filter { ch -> ch.isDigit() }.take(6)
                                resetOtpVerified = resetOtpCode == generatedOtp
                            },
                            label = { Text("Enter 6-digit OTP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            trailingIcon = {
                                if (resetOtpVerified) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF15803D))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = resetNewPassword,
                        onValueChange = { resetNewPassword = it },
                        label = { Text("New Password (min 6 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = resetOtpVerified,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = resetOtpVerified && resetNewPassword.length >= 6,
                    onClick = {
                        onResetPassword(resetIdentifier, resetNewPassword, selectedAccountType) { res ->
                            when (res) {
                                is AuthResult.Success -> {
                                    showForgotPasswordDialog = false
                                    errorMessage = null
                                    successMessage = res.message
                                    Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                                }
                                is AuthResult.Error -> {
                                    Toast.makeText(context, res.reason, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Update Password", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AccountTypeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) accentColor.copy(alpha = 0.08f) else CardWhite,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) accentColor else Color(0xFFE5E7EB)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (selected) accentColor else Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) Color.White else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (selected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = accentColor, modifier = Modifier.size(18.dp))
                }
            }
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (selected) accentColor else TextPrimary)
            Text(subtitle, fontSize = 10.sp, lineHeight = 14.sp, color = TextSecondary)
        }
    }
}

private data class PasswordStrength(val label: String, val progress: Float, val color: Color)

private fun calculatePasswordStrength(password: String): PasswordStrength {
    var score = 0
    if (password.length >= 6) score++
    if (password.length >= 8) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { it.isUpperCase() } || password.any { !it.isLetterOrDigit() }) score++
    return when {
        score <= 1 -> PasswordStrength("Weak", 0.3f, Color(0xFFDC2626))
        score == 2 -> PasswordStrength("Fair", 0.6f, Color(0xFFD97706))
        else -> PasswordStrength("Strong ✓", 1.0f, Color(0xFF15803D))
    }
}

@Composable
private fun OtpSixBoxInput(
    otpValue: String,
    accentColor: Color,
    enabled: Boolean,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = otpValue,
        onValueChange = { raw ->
            onOtpChange(raw.filter { it.isDigit() }.take(6))
        },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    val char = otpValue.getOrNull(index)?.toString() ?: ""
                    val isCurrentBox = enabled && otpValue.length == index
                    val isFilled = char.isNotEmpty()
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (enabled) CardWhite else Color(0xFFF3F4F6),
                        border = BorderStroke(
                            width = if (isCurrentBox || isFilled) 2.dp else 1.dp,
                            color = when {
                                !enabled -> Color(0xFFD1D5DB)
                                isCurrentBox -> accentColor
                                isFilled -> Color(0xFF16A34A)
                                else -> Color(0xFFD1D5DB)
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isFilled) char else "•",
                                fontSize = if (isFilled) 18.sp else 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isFilled) TextPrimary else Color(0xFF9CA3AF),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    )
}
