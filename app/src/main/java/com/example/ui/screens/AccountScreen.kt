package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.AccountType
import com.example.data.local.UserAccountEntity
import com.example.model.UserAccountRole
import com.example.ui.components.PanelIconAction
import com.example.ui.components.PanelIconActionGrid
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class LanguageOption(
    val letter: String,
    val name: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    cartCount: Int = 3,
    activeRole: UserAccountRole = UserAccountRole.BUYER,
    isDarkMode: Boolean = false,
    appLanguage: String = "English",
    walletBalance: Int = 250,
    rewardPoints: Int = 420,
    unreadNotificationCount: Int = 0,
    wishlistCount: Int = 0,
    ordersCount: Int = 0,
    currentUser: UserAccountEntity? = null,
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    onOpenWishlist: () -> Unit,
    onOpenShared: () -> Unit,
    onNavigateToHome: () -> Unit = onBack,
    onOpenCategories: () -> Unit = onBack,
    onOpenOrders: () -> Unit = onBack,
    onOpenTrackOrder: () -> Unit = onBack,
    onOpenLocationPicker: () -> Unit = onBack,
    onOpenReturns: () -> Unit = onBack,
    onOpenProfile: () -> Unit = {},
    onOpenSellerPanel: () -> Unit = {},
    onOpenMyPayments: () -> Unit = {},
    onOpenFollowedShops: () -> Unit = {},
    onOpenMeeshoBalance: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenLegalPolicies: () -> Unit = {},
    onOpenAdminPanel: () -> Unit = {},
    onOpenDirectMessageOtp: () -> Unit = {},
    onSwitchRole: (UserAccountRole) -> Unit = {},
    onToggleDarkMode: (Boolean) -> Unit = {},
    onLanguageChange: (String) -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenAiAssistant: () -> Unit = {},
    onOpenCompare: () -> Unit = {},
    onOpenSecurityCenter: () -> Unit = {},
    onOpenDevelopmentProgress: () -> Unit = {},
    onOpenUserAuth: (accountType: String, isRegisterMode: Boolean) -> Unit = { _, _ -> },
    onOpenAppManagement: (initialTab: Int) -> Unit = {},
    onOpenBackup: () -> Unit = {},
    onOpenControlPanel: () -> Unit = {},
    onOpenStaffWorkspace: () -> Unit = {},
    onClaimReferralCode: (String) -> Pair<Boolean, String> = { true to "Code $it applied! ₹125 discount will apply on your first order." },
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val accountScrollState = rememberScrollState()
    val accountScope = rememberCoroutineScope()

    // Dialog state controllers for features that open modals
    var showHelpCentreSheet by remember { mutableStateOf(false) }
    var showReferEarnSheet by remember { mutableStateOf(false) }
    var showBankUpiSheet by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showRateAppDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    var selectedLanguage by remember(appLanguage) { mutableStateOf(appLanguage) }
    var userRating by remember { mutableIntStateOf(5) }

    // Dynamic state with text boxes for user input & additions
    var bankAccountsList by remember {
        mutableStateOf(
            listOf(
                "State Bank of India (A/C: •••• 4829) • IFSC: SBIN0001234 • Amit Kumar Patwa"
            )
        )
    }
    var upiList by remember {
        mutableStateOf(
            listOf("amitpatwa@okaxis")
        )
    }
    var isAddingBankForm by remember { mutableStateOf(false) }
    var isAddingUpiForm by remember { mutableStateOf(false) }
    var inputAccountNumber by remember { mutableStateOf("") }
    var inputConfirmAccount by remember { mutableStateOf("") }
    var inputIfscCode by remember { mutableStateOf("") }
    var inputHolderName by remember { mutableStateOf("Amit Kumar Patwa") }
    var inputBankName by remember { mutableStateOf("State Bank of India") }
    var inputUpiId by remember { mutableStateOf("") }

    // Help Centre query state
    var helpSearchQuery by remember { mutableStateOf("") }
    var helpQueryDetail by remember { mutableStateOf("") }
    var helpOrderId by remember { mutableStateOf("") }
    var helpContactPhone by remember { mutableStateOf("+91 9128754982") }
    var helpSelectedTopic by remember { mutableStateOf("Order Delivery") }

    // Refer & Earn state
    var referFriendPhone by remember { mutableStateOf("") }
    var referCustomMessage by remember { mutableStateOf("Download Gyanvi Dresses to shop top quality fashion! Use code GYANVI125 to get flat ₹125 discount.") }
    var referInputCoupon by remember { mutableStateOf("") }

    // Language state
    var languageSearchQuery by remember { mutableStateOf("") }
    var customLanguageInput by remember { mutableStateOf("") }
    var languagesList by remember {
        mutableStateOf(
            listOf(
                LanguageOption("अ", "हिंदी"),
                LanguageOption("A", "English"),
                LanguageOption("అ", "తెలుగు"),
                LanguageOption("அ", "தமிழ்"),
                LanguageOption("ಅ", "ಕನ್ನಡ"),
                LanguageOption("অ", "বাংলা"),
                LanguageOption("अ", "मराठी"),
                LanguageOption("അ", "മലയാളം"),
                LanguageOption("અ", "ગુજરાતી"),
                LanguageOption("ଐ", "ଓଡ଼ିଆ")
            )
        )
    }

    // Rate Play Store state
    var rateReviewFeedback by remember { mutableStateOf("") }
    var selectedFeedbackTags by remember { mutableStateOf(setOf<String>()) }

    // Logout state
    var logoutFeedbackReason by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Surface(
                color = CardWhite,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("account_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "ACCOUNT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = 0.5.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Backup Quick Icon Button
                    IconButton(
                        onClick = onOpenBackup,
                        modifier = Modifier.testTag("account_topbar_backup_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = "App Backup",
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    // Scroll Down Button
                    IconButton(
                        onClick = {
                            accountScope.launch {
                                accountScrollState.animateScrollTo((accountScrollState.value + 550).coerceAtMost(accountScrollState.maxValue))
                            }
                        },
                        modifier = Modifier.testTag("account_scroll_down_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Scroll Down",
                            tint = MeeshoMagenta,
                            modifier = Modifier.size(25.dp)
                        )
                    }

                    // Search Icon
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.testTag("account_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Cart Icon with badge
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier.testTag("account_cart_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(
                                        containerColor = MeeshoMagenta,
                                        contentColor = Color.White
                                    ) {
                                        Text("$cartCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = "Cart",
                                tint = TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    var showAccountMoreMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showAccountMoreMenu = true },
                            modifier = Modifier.testTag("account_more_options_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showAccountMoreMenu,
                            onDismissRequest = { showAccountMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Outlined.Home, contentDescription = null, tint = MeeshoMagenta) },
                                text = { Text("Home (होम पेज)") },
                                onClick = { showAccountMoreMenu = false; onNavigateToHome() }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null, tint = Color(0xFF475569)) },
                                text = { Text("Settings (सेटिंग)") },
                                onClick = { showAccountMoreMenu = false; onOpenSettings() }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Outlined.Security, contentDescription = null, tint = Color(0xFF15803D)) },
                                text = { Text("Security Center (सुरक्षा)") },
                                onClick = { showAccountMoreMenu = false; onOpenSecurityCenter() }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFDC2626)) },
                                text = { Text("Logout (लॉगआउट)") },
                                onClick = { showAccountMoreMenu = false; showLogoutDialog = true }
                            )
                        }
                    }
                }
            }
        },
        containerColor = CardWhite,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(CardWhite)
                .verticalScroll(accountScrollState)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // 0A. Customer & Seller Registration + Mobile/Email Password Login Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color(0xFFE9D5FF), RoundedCornerShape(14.dp))
                    .clickable { onOpenUserAuth(AccountType.CUSTOMER, true) }
                    .testTag("user_auth_entry_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF6D28D9),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.PersonAddAlt1, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Customer & Seller Registration",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF4C1D95)
                                )
                                Text(
                                    text = "Mobile Number या Email ID + Password से अकाउंट बनाएं",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6D28D9)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (currentUser != null) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = currentUser?.accountType ?: "GUEST",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (currentUser != null) Color(0xFF166534) else Color(0xFF92400E),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onOpenUserAuth(AccountType.CUSTOMER, true) },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).height(36.dp).testTag("btn_register_customer")
                        ) {
                            Text("Customer Reg.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Button(
                            onClick = { onOpenUserAuth(AccountType.SELLER, true) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B21B6)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).height(36.dp).testTag("btn_register_seller")
                        ) {
                            Text("Seller Reg.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        OutlinedButton(
                            onClick = { onOpenUserAuth(AccountType.CUSTOMER, false) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp).testTag("btn_login_account")
                        ) {
                            Text("Login", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4C1D95))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 0. Role Switcher (Buyer / Seller / Admin) & Wallet + Rewards Strip
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2F8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color(0xFFFBCFE8), RoundedCornerShape(14.dp))
                    .testTag("role_switcher_card")
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SWITCH ACCOUNT ROLE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MeeshoMagenta,
                            letterSpacing = 0.6.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE6F4EA)
                        ) {
                            Text(
                                text = "₹$walletBalance Wallet • $rewardPoints Pts",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF137333),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserAccountRole.entries.forEach { role ->
                            val selected = activeRole == role
                            FilterChip(
                                selected = selected,
                                onClick = { onSwitchRole(role) },
                                label = {
                                    Text(
                                        text = role.shortTitle,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MeeshoMagenta,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("role_chip_${role.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 0B. Complete 15-Icon Buyer Panel Hub (ग्राहक ऐप)
            val buyerPanelIcons = remember(cartCount, wishlistCount, ordersCount, unreadNotificationCount, walletBalance) {
                listOf(
                    PanelIconAction("home", "Home", "होम पेज", Icons.Filled.Home, Icons.Outlined.Home, MeeshoMagenta, testTag = "buyer_icon_home", onClick = onNavigateToHome),
                    PanelIconAction("search", "Search", "प्रोडक्ट खोजना", Icons.Filled.Search, Icons.Outlined.Search, Color(0xFF2563EB), testTag = "buyer_icon_search", onClick = onSearchClick),
                    PanelIconAction("category", "Category", "कैटेगरी", Icons.Filled.Category, Icons.Outlined.Category, Color(0xFF7C3AED), testTag = "buyer_icon_category", onClick = onOpenCategories),
                    PanelIconAction("cart", "Shopping Cart", "शॉपिंग कार्ट", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, MeeshoMagenta, badgeText = if (cartCount > 0) "$cartCount" else null, testTag = "buyer_icon_cart", onClick = onCartClick),
                    PanelIconAction("wishlist", "Wishlist", "पसंदीदा प्रोडक्ट", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, Color(0xFFE11D48), badgeText = if (wishlistCount > 0) "$wishlistCount" else null, testTag = "buyer_icon_wishlist", onClick = onOpenWishlist),
                    PanelIconAction("orders", "My Orders", "मेरे ऑर्डर", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, Color(0xFF0284C7), badgeText = if (ordersCount > 0) "$ordersCount" else null, testTag = "buyer_icon_orders", onClick = onOpenOrders),
                    PanelIconAction("track", "Track Order", "ऑर्डर ट्रैकिंग", Icons.Filled.LocalShipping, Icons.Outlined.LocalShipping, Color(0xFF059669), testTag = "buyer_icon_track", onClick = onOpenTrackOrder),
                    PanelIconAction("notification", "Notification", "नोटिफिकेशन", Icons.Filled.Notifications, Icons.Outlined.Notifications, Color(0xFFD97706), badgeText = if (unreadNotificationCount > 0) "$unreadNotificationCount" else null, testTag = "buyer_icon_notification", onClick = onOpenNotifications),
                    PanelIconAction("profile", "Profile", "ग्राहक प्रोफाइल", Icons.Filled.Person, Icons.Outlined.Person, Color(0xFF4F46E5), testTag = "buyer_icon_profile", onClick = onOpenProfile),
                    PanelIconAction("address", "Address", "डिलीवरी एड्रेस", Icons.Filled.LocationOn, Icons.Outlined.LocationOn, Color(0xFF0D9488), testTag = "buyer_icon_address", onClick = onOpenLocationPicker),
                    PanelIconAction("wallet", "Wallet", "वॉलेट", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, Color(0xFF15803D), badgeText = "₹$walletBalance", testTag = "buyer_icon_wallet", onClick = onOpenMeeshoBalance),
                    PanelIconAction("payment", "Payment", "भुगतान", Icons.Filled.Payment, Icons.Outlined.Payment, Color(0xFF1D4ED8), testTag = "buyer_icon_payment", onClick = onOpenMyPayments),
                    PanelIconAction("return", "Return", "सामान वापस करना", Icons.Filled.AssignmentReturn, Icons.Outlined.AssignmentReturn, Color(0xFFEA580C), testTag = "buyer_icon_return", onClick = onOpenReturns),
                    PanelIconAction("help", "Help & Support", "सहायता", Icons.Filled.SupportAgent, Icons.Outlined.SupportAgent, Color(0xFF0369A1), testTag = "buyer_icon_help", onClick = { showHelpCentreSheet = true }),
                    PanelIconAction("logout", "Logout", "लॉगआउट", Icons.AutoMirrored.Filled.ExitToApp, Icons.AutoMirrored.Outlined.ExitToApp, Color(0xFFDC2626), testTag = "buyer_icon_logout", onClick = { showLogoutDialog = true })
                )
            }
            PanelIconActionGrid(
                sectionTitle = "Buyer Panel Icons (ग्राहक ऐप)",
                sectionSubtitle = "Tap any icon to open live feature",
                actions = buyerPanelIcons,
                accentColor = MeeshoMagenta,
                containerBorderColor = Color(0xFFFBCFE8),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .testTag("buyer_panel_icons_grid")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 0C. Master Control Panel Card (सब कुछ कंट्रोल करें)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(12.dp))
                    .clickable { onOpenControlPanel() }
                    .testTag("account_master_control_panel_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4338CA),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Filled.Tune, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Master Control Panel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1B4B)
                            )
                            Text(
                                text = "UI, Catalog, Orders, Gateway URL, Users & System Control",
                                fontSize = 11.sp,
                                color = Color(0xFF4338CA)
                            )
                        }
                    }
                    Button(
                        onClick = onOpenControlPanel,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp).testTag("account_open_control_panel_btn")
                    ) {
                        Text("Control", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 0D. Staff & Department Manager Workspace Card (कर्मचारी व मैनेजर पैनल)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(12.dp))
                    .clickable { onOpenStaffWorkspace() }
                    .testTag("account_staff_workspace_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF059669),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Filled.Badge, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Staff & Manager Workspace",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "कर्मचारी पैनल • Manager, Packer, Delivery, Catalog, Support & GST",
                                fontSize = 11.sp,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                    Button(
                        onClick = onOpenStaffWorkspace,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp).testTag("account_open_staff_btn")
                    ) {
                        Text("Staff PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Go to Supplier Hub Card (Proceed button)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color(0xFFEFEFEF), RoundedCornerShape(12.dp))
                    .clickable { onOpenSellerPanel() }
                    .testTag("supplier_hub_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Go to Supplier Hub",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Sell products & manage orders",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = { onOpenSellerPanel() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MeeshoMagenta,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("supplier_proceed_button")
                    ) {
                        Text(
                            text = "Proceed",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1B. Admin Panel Master Control Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                    .clickable { onOpenAdminPanel() }
                    .testTag("admin_panel_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD97706),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Admin Panel",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFB45309)
                                ) {
                                    Text(
                                        "MASTER CONTROL",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "QC Approvals, Sellers, Orders & Payouts",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                    }

                    Button(
                        onClick = { onOpenAdminPanel() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB45309),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Open", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1B2. Master App Management & Live Edit Dashboard Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(12.dp))
                    .clickable { onOpenAppManagement(0) }
                    .testTag("app_management_entry_card")
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF059669),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.DesignServices, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "App Management & Edit Dashboard",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF064E3B)
                                )
                                Text(
                                    text = "Edit/Delete Options • Change Dashboard, Image, Text & लिखावट",
                                    fontSize = 11.sp,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.End) {
                            Button(
                                onClick = { onOpenAppManagement(0) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Manage", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                            }
                            OutlinedButton(
                                onClick = onOpenBackup,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp).testTag("account_open_backup_btn")
                            ) {
                                Icon(Icons.Default.Backup, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Backup", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF047857))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickTabs = listOf(
                            0 to "Dashboard",
                            1 to "Image",
                            2 to "Text",
                            3 to "लिखावट",
                            4 to "Options",
                            5 to "Edit/Del"
                        )
                        quickTabs.forEach { (tabIndex, label) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CardWhite,
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(8.dp))
                                    .clickable { onOpenAppManagement(tabIndex) }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1C. Direct Message, OTP & Share Center Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                    .clickable { onOpenDirectMessageOtp() }
                    .testTag("direct_msg_otp_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.SendToMobile, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Send Message & OTP",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF1D4ED8)
                                ) {
                                    Text(
                                        "SHARE CENTER",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "मोबाइल / ईमेल पर मैसेज, OTP व गैलरी डाउनलोड",
                                fontSize = 11.sp,
                                color = Color(0xFF1E40AF)
                            )
                        }
                    }

                    Button(
                        onClick = { onOpenDirectMessageOtp() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1D4ED8),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Open", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. User Profile Row (Amit Kumar Patwa -> Profile Details)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenProfile() }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .testTag("profile_row"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar with Camera Badge
                Box(
                    modifier = Modifier.size(68.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFE0B2)),
                        contentAlignment = Alignment.Center
                    ) {
                        AccountAvatarIllustration(modifier = Modifier.fillMaxSize())
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(CardWhite)
                            .border(1.dp, Color(0xFFCCCCCC), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Edit Profile Photo",
                            tint = Color(0xFF616161),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser?.fullName ?: "Guest User (Tap to Sign In)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    val contactLine = if (currentUser != null) {
                        val contact = currentUser.mobileNumber.takeIf { it.isNotBlank() }?.let { "+91 $it" }
                            ?: currentUser.email
                        "${currentUser.accountType} • $contact"
                    } else {
                        "Register Customer / Seller Account"
                    }
                    Text(
                        text = contactLine,
                        fontSize = 12.sp,
                        color = MeeshoMagenta,
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Profile",
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Quick Action Cards (Help Centre & Refer & Earn)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Help Centre Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier
                        .weight(1f)
                        .height(105.dp)
                        .border(1.dp, Color(0xFFE2E6EA), RoundedCornerShape(14.dp))
                        .clickable { showHelpCentreSheet = true }
                        .testTag("help_centre_button")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Help Centre",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Help Centre",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }

                // Refer & Earn Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier
                        .weight(1f)
                        .height(105.dp)
                        .border(1.dp, Color(0xFFE2E6EA), RoundedCornerShape(14.dp))
                        .clickable { showReferEarnSheet = true }
                        .testTag("refer_earn_button")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = "Refer & Earn",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Refer & Earn",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==================== SECTION 1: My Payments ====================
            SectionHeader(title = "My Payments")

            AccountRowItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "Bank & UPI Details",
                onClick = { showBankUpiSheet = true },
                testTag = "item_bank_upi"
            )

            HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            AccountRowItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "Payment & Refund",
                onClick = onOpenMyPayments,
                testTag = "item_payment_refund"
            )

            HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            AccountRowItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color(0xFF283593),
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "Supplier Payouts & Reports",
                badgeContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8EAF6))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "₹626.45",
                            color = Color(0xFF1A237E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                onClick = onOpenSellerPanel,
                testTag = "item_supplier_payouts"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ==================== SECTION 2: My Activity ====================
            SectionHeader(title = "My Activity")

            AccountRowItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFCE4EC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "अ A",
                            color = MeeshoMagenta,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                },
                title = "Change Language",
                onClick = { showLanguageDialog = true },
                testTag = "item_change_language"
            )

            HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            AccountRowItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "Wishlisted Products",
                onClick = onOpenWishlist,
                testTag = "item_wishlisted_products"
            )

            HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            AccountRowItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "Shared Products",
                onClick = onOpenShared,
                testTag = "item_shared_products"
            )

            HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            AccountRowItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "Followed Shops",
                badgeContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8F0FE))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "New",
                            color = Color(0xFF1A73E8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                onClick = onOpenFollowedShops,
                testTag = "item_followed_shops"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ==================== SECTION 2B: Smart Marketplace & Alerts ====================
            SectionHeader(title = "Smart Marketplace & Alerts")

            AccountRowItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MeeshoMagenta,
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "Notifications & Alert Settings",
                badgeContent = if (unreadNotificationCount > 0) {
                    {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MeeshoMagenta)
                                .padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$unreadNotificationCount New",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else null,
                onClick = onOpenNotifications,
                testTag = "item_notifications_center"
            )

            HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            AccountRowItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "AI Fashion Stylist & Seller Chat",
                badgeContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3E8FF))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "AI + Chat",
                            color = Color(0xFF6D28D9),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                onClick = onOpenAiAssistant,
                testTag = "item_ai_fashion_assistant"
            )

            HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            AccountRowItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "Compare Dresses Side-by-Side",
                onClick = onOpenCompare,
                testTag = "item_compare_dresses"
            )

            HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            AccountRowItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF15803D),
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "Security Center (2FA, Fraud & Backup)",
                onClick = onOpenSecurityCenter,
                testTag = "item_security_center"
            )

            HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            AccountRowItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = MeeshoMagenta,
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "साप्ताहिक Development Progress (15 Modules)",
                badgeContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFCE7F3))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Tap to Set Up",
                            color = MeeshoMagenta,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                onClick = onOpenDevelopmentProgress,
                testTag = "item_development_progress"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ==================== SECTION 3: Others ====================
            SectionHeader(title = "Others")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleDarkMode(!isDarkMode) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .testTag("item_dark_mode_toggle"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dark Mode Theme",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isDarkMode) "Dark appearance active" else "Light Yellow & Pink active",
                        fontSize = 11.sp,
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

            HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            AccountRowItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MeeshoMagenta),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                },
                title = "Gyanvi Dresses Wallet & Rewards",
                badgeContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE6F4EA))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "₹$walletBalance",
                            color = Color(0xFF137333),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                onClick = onOpenMeeshoBalance,
                testTag = "item_meesho_balance"
            )

            HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            AccountRowItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "Settings",
                onClick = onOpenSettings,
                testTag = "item_settings"
            )

            HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            AccountRowItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MeeshoMagenta,
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "Rate Gyanvi Dresses",
                onClick = { showRateAppDialog = true },
                testTag = "item_rate_meesho"
            )

            HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            AccountRowItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = Color(0xFF8D6E63),
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "Legal and Policies",
                onClick = onOpenLegalPolicies,
                testTag = "item_legal_policies"
            )

            HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            AccountRowItem(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(24.dp)
                    )
                },
                title = "Logout",
                onClick = { showLogoutDialog = true },
                testTag = "item_logout"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ==================== BOTTOM BANNER ====================
            MadeForBharatBanner(modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ==================== POPUPS & MODALS ====================

    // 1. Bank & UPI Details Bottom Sheet
    if (showBankUpiSheet) {
        AccountBottomSheetDialog(
            title = "Bank & UPI Details",
            onDismiss = {
                showBankUpiSheet = false
                isAddingBankForm = false
                isAddingUpiForm = false
            }
        ) {
            Text(
                text = "Refunds and supplier payments are automatically credited here.",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Verified UPI IDs List matching Screenshot 2
            upiList.forEachIndexed { idx, upi ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E7D32)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (idx == 0) "Primary UPI ID (Verified)" else "Linked UPI ID",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(upi, fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }

            // Bank Accounts List matching Screenshot 2
            bankAccountsList.forEach { _ ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E7D32)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "State Bank of India (A/C: •••• 4829)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "IFSC: SBIN0001234 • Amit Kumar Patwa",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Add Bank Form (Text Boxes)
            if (isAddingBankForm) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Add / Change Bank Account", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MeeshoMagenta)
                            TextButton(onClick = {
                                isAddingBankForm = false
                                isAddingUpiForm = true
                            }) {
                                Text("Switch to UPI", fontSize = 12.sp, color = Color(0xFF1976D2))
                            }
                        }
                        OutlinedTextField(
                            value = inputHolderName,
                            onValueChange = { inputHolderName = it },
                            label = { Text("Account Holder Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = inputBankName,
                            onValueChange = { inputBankName = it },
                            label = { Text("Bank Name (e.g. SBI, HDFC, PNB)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = inputAccountNumber,
                            onValueChange = { inputAccountNumber = it },
                            label = { Text("Account Number") },
                            placeholder = { Text("Enter 9-18 digit account number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = inputConfirmAccount,
                            onValueChange = { inputConfirmAccount = it },
                            label = { Text("Re-enter Account Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = inputIfscCode,
                            onValueChange = { inputIfscCode = it.uppercase() },
                            label = { Text("IFSC Code (11 digits)") },
                            placeholder = { Text("e.g. SBIN0001234") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isAddingBankForm = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    if (inputAccountNumber.isNotBlank() && inputIfscCode.isNotBlank()) {
                                        val masked = if (inputAccountNumber.length >= 4)
                                            "•••• " + inputAccountNumber.takeLast(4)
                                        else inputAccountNumber
                                        val newEntry = "$inputBankName (A/C: $masked) • IFSC: $inputIfscCode • $inputHolderName"
                                        bankAccountsList = bankAccountsList + newEntry
                                        isAddingBankForm = false
                                        inputAccountNumber = ""
                                        inputConfirmAccount = ""
                                        inputIfscCode = ""
                                        Toast.makeText(context, "Bank account added successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Please fill account number and IFSC", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Account")
                            }
                        }
                    }
                }
            }

            // Add UPI Form (Text Box)
            if (isAddingUpiForm) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Link New UPI ID", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MeeshoMagenta)
                            TextButton(onClick = {
                                isAddingUpiForm = false
                                isAddingBankForm = true
                            }) {
                                Text("Switch to Bank", fontSize = 12.sp, color = Color(0xFF1976D2))
                            }
                        }
                        OutlinedTextField(
                            value = inputUpiId,
                            onValueChange = { inputUpiId = it },
                            label = { Text("UPI ID (VPA)") },
                            placeholder = { Text("e.g. kumaramit@okaxis or 9128754982@paytm") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isAddingUpiForm = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    if (inputUpiId.contains("@") && inputUpiId.length >= 5) {
                                        upiList = upiList + inputUpiId
                                        isAddingUpiForm = false
                                        inputUpiId = ""
                                        Toast.makeText(context, "UPI ID verified and linked!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Please enter a valid UPI ID (e.g. name@bank)", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Verify & Link")
                            }
                        }
                    }
                }
            }

            if (!isAddingBankForm && !isAddingUpiForm) {
                OutlinedButton(
                    onClick = { isAddingBankForm = true },
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF48FB1)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MeeshoMagenta
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(
                        "+ Add / Change Bank Account or UPI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MeeshoMagenta
                    )
                }
            }
        }
    }

    // 2. Change Language Dialog with Search & Add Text Box
    if (showLanguageDialog) {
        val filteredLanguages = languagesList.filter {
            it.name.contains(languageSearchQuery, ignoreCase = true) ||
            it.letter.contains(languageSearchQuery, ignoreCase = true)
        }

        Dialog(
            onDismissRequest = { showLanguageDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showLanguageDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* absorb clicks */ }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Change Language",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            IconButton(
                                onClick = { showLanguageDialog = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF616161))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Search or Add Custom Language Text Box
                        OutlinedTextField(
                            value = languageSearchQuery,
                            onValueChange = { languageSearchQuery = it },
                            placeholder = { Text("Search or type new language...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MeeshoMagenta) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Grid of matching languages
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                        ) {
                            items(filteredLanguages) { lang ->
                                val isSelected = selectedLanguage.equals(lang.name, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MeeshoMagenta else Color(0xFFE0E0E0),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .background(if (isSelected) Color(0xFFFCE4EC) else CardWhite)
                                        .clickable {
                                            selectedLanguage = lang.name
                                            onLanguageChange(lang.name)
                                            showLanguageDialog = false
                                            Toast.makeText(context, "Language set to ${lang.name}", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = lang.letter,
                                            color = Color(0xFF3F51B5),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = lang.name,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MeeshoMagenta,
                                            modifier = Modifier.align(Alignment.CenterEnd).size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Option to add as custom language if typed query is not present
                        if (languageSearchQuery.isNotBlank() && filteredLanguages.none { it.name.equals(languageSearchQuery, ignoreCase = true) }) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val newLang = LanguageOption("ल", languageSearchQuery.trim())
                                    languagesList = languagesList + newLang
                                    selectedLanguage = newLang.name
                                    onLanguageChange(newLang.name)
                                    languageSearchQuery = ""
                                    showLanguageDialog = false
                                    Toast.makeText(context, "Added and selected ${newLang.name}!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("+ Add & Use \"$languageSearchQuery\"")
                            }
                        }
                    }
                }
            }
        }
    }

    // 3. Rate Play Store Dialog with 5-Stars & Review Text Box
    if (showRateAppDialog) {
        Dialog(
            onDismissRequest = { showRateAppDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showRateAppDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* absorb clicks */ }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { showRateAppDialog = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF616161))
                            }
                        }

                        Text(
                            text = "Would you like to rate us on\nPlay Store?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Your feedback helps us make your experience better",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 5 Interactive Stars
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { userRating = star },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = if (star <= userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "$star stars",
                                        tint = if (star <= userRating) Color(0xFFFFB300) else Color(0xFFBDBDBD),
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("WORST", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text("BEST", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Review Text Box
                        OutlinedTextField(
                            value = rateReviewFeedback,
                            onValueChange = { rateReviewFeedback = it },
                            label = { Text("Write your review (Text Box)") },
                            placeholder = { Text("What did you like about Gyanvi Dresses? Tell us here...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                showRateAppDialog = false
                                Toast.makeText(context, "Review submitted! Thank you for rating $userRating stars.", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Submit Review to Play Store", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    // 4. Logout Dialog with Feedback Text Box
    if (showLogoutDialog) {
        Dialog(
            onDismissRequest = { showLogoutDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showLogoutDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* absorb clicks */ }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Logout Confirmation",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            IconButton(
                                onClick = { showLogoutDialog = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF616161))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Are you sure you want to logout from your Amit Kumar Patwa account?",
                            fontSize = 14.sp,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Feedback Text Box
                        OutlinedTextField(
                            value = logoutFeedbackReason,
                            onValueChange = { logoutFeedbackReason = it },
                            label = { Text("Reason for logging out (optional)") },
                            placeholder = { Text("e.g. Switching phone, account update...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showLogoutDialog = false },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Text("Cancel", fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    showLogoutDialog = false
                                    onLogout()
                                    Toast.makeText(context, "Logged out! Please Sign In or Register to continue.", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // 5. Help Centre Sheet with Search & Description Text Box
    if (showHelpCentreSheet) {
        AccountBottomSheetDialog(
            title = "Help Centre & 24x7 Support",
            onDismiss = { showHelpCentreSheet = false }
        ) {
            Text(
                text = "Describe your issue or choose a topic below to get instant resolution.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Topic selection chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Delivery", "Refund", "Bank/UPI", "Seller Hub").forEach { topic ->
                    FilterChip(
                        selected = helpSelectedTopic == topic,
                        onClick = { helpSelectedTopic = topic },
                        label = { Text(topic, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MeeshoMagenta,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Issue Description Text Box
            OutlinedTextField(
                value = helpQueryDetail,
                onValueChange = { helpQueryDetail = it },
                label = { Text("Describe your question or problem (Text Box)") },
                placeholder = { Text("e.g. Need help with order delivery date or payment refund...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Order ID Text Box
            OutlinedTextField(
                value = helpOrderId,
                onValueChange = { helpOrderId = it },
                label = { Text("Order ID / Sub Order ID (optional)") },
                placeholder = { Text("e.g. #ORD-BTH-9021") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Callback Phone Text Box
            OutlinedTextField(
                value = helpContactPhone,
                onValueChange = { helpContactPhone = it },
                label = { Text("Callback Mobile Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    showHelpCentreSheet = false
                    val ticketId = "TKT-${System.currentTimeMillis().toString().takeLast(6)}"
                    Toast.makeText(context, "Ticket $ticketId created! Our care team will call $helpContactPhone.", Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Submit Support Request", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        showHelpCentreSheet = false
                        Toast.makeText(context, "Callback queued for $helpContactPhone in 10 mins!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call Back")
                }

                Button(
                    onClick = {
                        showHelpCentreSheet = false
                        Toast.makeText(context, "WhatsApp support chat launched!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // 6. Refer & Earn Sheet with Friend Number Text Box & Custom Message
    if (showReferEarnSheet) {
        AccountBottomSheetDialog(
            title = "Refer & Earn ₹125",
            onDismiss = { showReferEarnSheet = false }
        ) {
            Text(
                text = "Earn up to ₹125 in Gyanvi Dresses Balance on every friend's first delivered purchase!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Your Referral Code", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("GYANVI125", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MeeshoMagenta)
                    }
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString("GYANVI125"))
                            Toast.makeText(context, "Referral code GYANVI125 copied!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                    ) {
                        Text("Copy Code")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Friend Phone Number Text Box
            OutlinedTextField(
                value = referFriendPhone,
                onValueChange = { referFriendPhone = it },
                label = { Text("Friend's Mobile Number (Text Box)") },
                placeholder = { Text("Enter 10-digit mobile number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Custom Invite Message Text Box
            OutlinedTextField(
                value = referCustomMessage,
                onValueChange = { referCustomMessage = it },
                label = { Text("Personalized Invite Message (Text Box)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (referFriendPhone.length >= 10) {
                            showReferEarnSheet = false
                            Toast.makeText(context, "Invite SMS sent to $referFriendPhone!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Please enter a 10-digit mobile number", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Send SMS")
                }

                Button(
                    onClick = {
                        showReferEarnSheet = false
                        Toast.makeText(context, "Referral link & message shared on WhatsApp!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Have a code from a friend? Text Box
            Text("Have a referral code from someone?", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = referInputCoupon,
                    onValueChange = { referInputCoupon = it.uppercase() },
                    placeholder = { Text("Enter friend's code") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (referInputCoupon.isNotBlank()) {
                            val (ok, msg) = onClaimReferralCode(referInputCoupon)
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (ok) referInputCoupon = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

// ==================== ROCK-SOLID BOTTOM SHEET COMPONENT ====================

@Composable
fun AccountBottomSheetDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Absorb clicks inside dialog */ }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .navigationBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFDDDDDD))
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    content()
                }
            }
        }
    }
}

// ==================== AVATAR ILLUSTRATION ====================

@Composable
private fun AccountAvatarIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        drawCircle(
            color = Color(0xFFFFD180),
            radius = width / 2f,
            center = Offset(width / 2f, height / 2f)
        )

        val shirtPath = Path().apply {
            moveTo(width * 0.2f, height)
            lineTo(width * 0.35f, height * 0.76f)
            lineTo(width * 0.65f, height * 0.76f)
            lineTo(width * 0.8f, height)
            close()
        }
        drawPath(shirtPath, color = Color(0xFF1976D2))

        val collarPath = Path().apply {
            moveTo(width * 0.42f, height * 0.76f)
            lineTo(width * 0.5f, height * 0.86f)
            lineTo(width * 0.58f, height * 0.76f)
            close()
        }
        drawPath(collarPath, color = Color.White)

        drawCircle(
            color = Color(0xFFFFCC80),
            radius = width * 0.26f,
            center = Offset(width * 0.5f, height * 0.45f)
        )

        val hairPath = Path().apply {
            moveTo(width * 0.25f, height * 0.40f)
            cubicTo(
                width * 0.25f, height * 0.15f,
                width * 0.75f, height * 0.15f,
                width * 0.75f, height * 0.40f
            )
            lineTo(width * 0.68f, height * 0.32f)
            cubicTo(
                width * 0.55f, height * 0.25f,
                width * 0.45f, height * 0.25f,
                width * 0.32f, height * 0.32f
            )
            close()
        }
        drawPath(hairPath, color = Color(0xFF212121))

        drawCircle(color = Color(0xFF212121), radius = 2.dp.toPx(), center = Offset(width * 0.42f, height * 0.45f))
        drawCircle(color = Color(0xFF212121), radius = 2.dp.toPx(), center = Offset(width * 0.58f, height * 0.45f))

        val smilePath = Path().apply {
            moveTo(width * 0.44f, height * 0.55f)
            quadraticTo(width * 0.5f, height * 0.62f, width * 0.56f, height * 0.55f)
        }
        drawPath(smilePath, color = Color(0xFF212121), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

// ==================== REUSABLE ROW COMPONENT ====================

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF333333),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}

@Composable
private fun AccountRowItem(
    icon: @Composable () -> Unit,
    title: String,
    badgeContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        badgeContent?.invoke()
    }
}

// ==================== BOTTOM BANNER ====================

@Composable
private fun MadeForBharatBanner(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
            .padding(horizontal = 16.dp)
            .height(130.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFEDE7F6),
                            Color(0xFFE8EAF6),
                            Color(0xFFFCE4EC)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Made with love",
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Color(0xFF5E6977)
                    )
                    Text(
                        text = "for Bharat",
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = Color(0xFF4A5568)
                    )
                }

                Canvas(modifier = Modifier.size(140.dp, 120.dp)) {
                    val w = size.width
                    val h = size.height

                    drawLine(
                        color = Color(0xFF9E9E9E),
                        start = Offset(w * 0.40f, h * 0.10f),
                        end = Offset(w * 0.40f, h * 0.95f),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = Color(0xFFFFB300), radius = 4.dp.toPx(), center = Offset(w * 0.40f, h * 0.09f))

                    val flagW = w * 0.45f
                    val flagH = h * 0.35f
                    val flagTop = h * 0.12f
                    val flagLeft = w * 0.41f
                    val bandH = flagH / 3f

                    val saffronPath = Path().apply {
                        moveTo(flagLeft, flagTop)
                        cubicTo(flagLeft + flagW * 0.4f, flagTop - 4.dp.toPx(), flagLeft + flagW * 0.7f, flagTop + 4.dp.toPx(), flagLeft + flagW, flagTop)
                        lineTo(flagLeft + flagW, flagTop + bandH)
                        cubicTo(flagLeft + flagW * 0.7f, flagTop + bandH + 4.dp.toPx(), flagLeft + flagW * 0.4f, flagTop + bandH - 4.dp.toPx(), flagLeft, flagTop + bandH)
                        close()
                    }
                    drawPath(saffronPath, color = Color(0xFFFF9933))

                    val whitePath = Path().apply {
                        moveTo(flagLeft, flagTop + bandH)
                        cubicTo(flagLeft + flagW * 0.4f, flagTop + bandH - 4.dp.toPx(), flagLeft + flagW * 0.7f, flagTop + bandH + 4.dp.toPx(), flagLeft + flagW, flagTop + bandH)
                        lineTo(flagLeft + flagW, flagTop + bandH * 2)
                        cubicTo(flagLeft + flagW * 0.7f, flagTop + bandH * 2 + 4.dp.toPx(), flagLeft + flagW * 0.4f, flagTop + bandH * 2 - 4.dp.toPx(), flagLeft, flagTop + bandH * 2)
                        close()
                    }
                    drawPath(whitePath, color = Color.White)

                    val chakraCenter = Offset(flagLeft + flagW * 0.5f, flagTop + bandH * 1.5f)
                    drawCircle(color = Color(0xFF000080), radius = 4.5.dp.toPx(), center = chakraCenter, style = Stroke(width = 1.dp.toPx()))
                    drawCircle(color = Color(0xFF000080), radius = 1.5.dp.toPx(), center = chakraCenter)

                    val greenPath = Path().apply {
                        moveTo(flagLeft, flagTop + bandH * 2)
                        cubicTo(flagLeft + flagW * 0.4f, flagTop + bandH * 2 - 4.dp.toPx(), flagLeft + flagW * 0.7f, flagTop + bandH * 2 + 4.dp.toPx(), flagLeft + flagW, flagTop + bandH * 2)
                        lineTo(flagLeft + flagW, flagTop + flagH)
                        cubicTo(flagLeft + flagW * 0.7f, flagTop + flagH + 4.dp.toPx(), flagLeft + flagW * 0.4f, flagTop + flagH - 4.dp.toPx(), flagLeft, flagTop + flagH)
                        close()
                    }
                    drawPath(greenPath, color = Color(0xFF138808))
                }
            }
        }
    }
}
