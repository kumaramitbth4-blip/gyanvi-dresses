package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import androidx.compose.ui.res.stringResource
import com.example.data.local.ProductEntity
import com.example.data.local.UserAccountEntity
import com.example.model.*
import com.example.ui.components.PanelIconAction
import com.example.ui.components.PanelIconActionGrid
import com.example.ui.components.PaymentGatewayConnectorSection
import com.example.ui.components.SocialMediaAdsManagerSection
import com.example.ui.theme.*
import com.example.viewmodel.CloudSyncState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterControlPanelScreen(
    managementConfig: AppManagementConfig,
    isDarkMode: Boolean,
    appLanguage: String,
    selectedCurrency: String,
    activeWarehouse: String,
    isVipSubscribed: Boolean,
    activeRole: UserAccountRole,
    currentUser: UserAccountEntity?,
    registeredAccountsCount: Int,
    registeredSellersCount: Int,
    products: List<Product>,
    roomProducts: List<ProductEntity>,
    categories: List<CategoryItem>,
    orders: List<OrderItem>,
    cartCount: Int,
    wishlistCount: Int,
    walletBalance: Int,
    rewardPoints: Int,
    autoTrackingEnabled: Boolean,
    notificationPrefs: NotificationPreferences,
    cloudSyncState: CloudSyncState,
    onBack: () -> Unit,
    onSwitchRole: (UserAccountRole) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onWarehouseChange: (String) -> Unit,
    onToggleVip: (Boolean) -> Unit,
    onSelectThemePreset: (DashboardThemePreset) -> Unit,
    onSelectLayoutMode: (HomeLayoutMode) -> Unit,
    onSelectCornerPreset: (CardCornerPreset) -> Unit,
    onUpdateTexts: ((AppTextsConfig) -> AppTextsConfig) -> Unit,
    onUpdateFeatures: ((AppFeaturesConfig) -> AppFeaturesConfig) -> Unit,
    onOpenAppManagement: (initialTab: Int) -> Unit,
    onOpenBuyerHub: () -> Unit,
    onOpenSellerHub: () -> Unit,
    onOpenStaffWorkspace: () -> Unit = {},
    onOpenAdminPanel: () -> Unit,
    onOpenUserAuth: () -> Unit,
    onOpenSecurityCenter: () -> Unit,
    onOpenBackup: () -> Unit,
    onCreateBackup: (String) -> Unit,
    onSyncCloudOrders: () -> Unit,
    onToggleAutoTracking: (Boolean) -> Unit,
    onSyncLiveTracking: () -> Unit,
    onMarkOrderReadyToShip: (String) -> Unit,
    onDispatchOrder: (String) -> Unit,
    onAdvanceTracking: (String) -> Unit,
    onCancelOrder: (String) -> Unit,
    onToggleCategoryActive: (String, Boolean) -> Unit,
    onUpdateStock: (String, Int) -> Unit,
    onTogglePauseProduct: (String, Boolean) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onAddQuickProduct: (name: String, category: String, price: Int, mrp: Int, stock: Int) -> Unit,
    onPushBroadcast: (title: String, message: String, category: String) -> Unit,
    onUpdateNotificationPrefs: ((NotificationPreferences) -> NotificationPreferences) -> Unit,
    onSaveAndConnectPaymentGateway: (id: String?, name: String, url: String, provider: PaymentGatewayProviderPreset, lastStatus: String, connectNow: Boolean) -> Result<PaymentGatewayLinkItem>,
    onConnectPaymentGateway: (String) -> Unit,
    onDisconnectPaymentGateway: () -> Unit,
    onDeletePaymentGateway: (String) -> Unit,
    onToggleAppendPaymentParams: (Boolean) -> Unit,
    onToggleSocialAdsEnabled: (Boolean) -> Unit = {},
    onToggleSocialAdsHome: (Boolean) -> Unit = {},
    onSaveSocialAdCampaign: (String?, String, SocialAdPlatform, String, String, String, Int, String, Boolean, Boolean) -> Result<SocialMediaAdCampaign> = { id, title, platform, headline, caption, url, budget, cta, showHome, active ->
        Result.success(SocialMediaAdCampaign(id ?: "ad-preview", title, platform, headline, caption, url, budget, isActive = active, showOnHomeBanner = showHome, ctaLabel = cta))
    },
    onToggleSocialAdActive: (String) -> Unit = {},
    onDeleteSocialAdCampaign: (String) -> Unit = {},
    onRecordSocialAdClick: (String) -> Unit = {},
    onResetAllSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var selectedSection by remember { mutableIntStateOf(0) }
    var productSearchQuery by remember { mutableStateOf("") }
    var orderStatusFilter by remember { mutableStateOf("All") }
    var announcementInput by remember(managementConfig.texts.announcementText) {
        mutableStateOf(managementConfig.texts.announcementText)
    }
    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastMsg by remember { mutableStateOf("") }
    var showQuickAddProductDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val primaryAccent = managementConfig.themePreset.accentColor
    val pendingCount = orders.count { it.status == OrderStatus.ORDERED }
    val shippedCount = orders.count { it.status == OrderStatus.SHIPPED }
    val deliveredCount = orders.count { it.status == OrderStatus.DELIVERED }
    val activeGateway = managementConfig.paymentGateways.activeGateway

    val sectionTabs = listOf(
        Triple("All", Icons.Filled.Tune, Icons.Outlined.Tune),
        Triple("UI & Theme", Icons.Filled.DashboardCustomize, Icons.Outlined.DashboardCustomize),
        Triple("Catalog & Stock", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
        Triple("Orders", Icons.Filled.LocalShipping, Icons.Outlined.LocalShipping),
        Triple("Payments", Icons.Filled.Payments, Icons.Outlined.Payments),
        Triple("Users & System", Icons.Filled.Security, Icons.Outlined.Security)
    )

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 3.dp) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("control_panel_back_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "CONTROL PANEL",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = primaryAccent
                                    )
                                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFDCFCE7)) {
                                        Text(
                                            text = "ALL-IN-ONE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "यहाँ से पूरा ऐप, यूज़र, सेलर, ऑर्डर व पेमेंट कंट्रोल करें",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    onSyncCloudOrders()
                                    Toast.makeText(context, "Cloud Sync triggered", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("control_panel_cloud_sync_btn")
                            ) {
                                Icon(Icons.Outlined.CloudSync, contentDescription = "Cloud Sync", tint = Color(0xFF2563EB))
                            }
                            IconButton(
                                onClick = {
                                    onCreateBackup("Control Panel Snapshot")
                                    Toast.makeText(context, "Backup snapshot saved", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("control_panel_quick_backup_btn")
                            ) {
                                Icon(Icons.Outlined.Backup, contentDescription = "Quick Backup", tint = Color(0xFF059669))
                            }
                        }
                    }

                    ScrollableTabRow(
                        selectedTabIndex = selectedSection,
                        containerColor = CardWhite,
                        contentColor = primaryAccent,
                        edgePadding = 12.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        sectionTabs.forEachIndexed { index, (label, filled, outlined) ->
                            val isSel = selectedSection == index
                            Tab(
                                selected = isSel,
                                onClick = { selectedSection = index },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isSel) filled else outlined,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC),
        modifier = modifier.fillMaxSize().testTag("master_control_panel_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Live Command Summary Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1E1B4B), primaryAccent, Color(0xFFBE185D))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("GYANVI MASTER COMMAND CENTER", color = Color(0xFFFDE68A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = currentUser?.fullName ?: "Super Admin",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Role: ${activeRole.label} • Hub: $activeWarehouse",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.5.sp
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.18f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Filled.Tune, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ControlMetricPill("Products", "${products.size}", Modifier.weight(1f))
                            ControlMetricPill("Categories", "${categories.size}", Modifier.weight(1f))
                            ControlMetricPill("Orders", "${orders.size}", Modifier.weight(1f))
                            ControlMetricPill("Sellers", "$registeredSellersCount", Modifier.weight(1f))
                        }
                    }
                }
            }

            // 2. Role Switcher + Quick Workspace Grid (shown on All or System)
            if (selectedSection == 0 || selectedSection == 5) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Active Panel Role Switcher (पैनल रोल बदलें)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            UserAccountRole.entries.forEach { role ->
                                val selected = activeRole == role
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        onSwitchRole(role)
                                        Toast.makeText(context, "Switched to ${role.label}", Toast.LENGTH_SHORT).show()
                                    },
                                    label = {
                                        Text(
                                            role.shortTitle,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = primaryAccent,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("control_role_${role.name.lowercase()}")
                                )
                            }
                        }
                    }
                }

                PanelIconActionGrid(
                    sectionTitle = "Quick Control Shortcuts (सभी पैनल खोलें)",
                    sectionSubtitle = "1-tap access to Buyer, Staff, Seller, Admin, CMS, Security & Backup",
                    accentColor = primaryAccent,
                    containerBorderColor = Color(0xFFE2E8F0),
                    actions = listOf(
                        PanelIconAction("cp_buyer", "Buyer Hub", "ग्राहक पैनल", Icons.Filled.Person, Icons.Outlined.Person, primaryAccent, badgeText = if (cartCount > 0) "$cartCount" else null, testTag = "cp_open_buyer", onClick = onOpenBuyerHub),
                        PanelIconAction("cp_staff", "Staff Hub", "कर्मचारी पैनल", Icons.Filled.Badge, Icons.Outlined.Badge, Color(0xFF0369A1), testTag = "cp_open_staff", onClick = onOpenStaffWorkspace),
                        PanelIconAction("cp_seller", "Seller Hub", "सेलर पैनल", Icons.Filled.Storefront, Icons.Outlined.Storefront, Color(0xFF7C3AED), testTag = "cp_open_seller", onClick = onOpenSellerHub),
                        PanelIconAction("cp_admin", "Admin Panel", "एडमिन पैनल", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings, Color(0xFFB45309), testTag = "cp_open_admin", onClick = onOpenAdminPanel),
                        PanelIconAction("cp_cms", "Dashboard CMS", "डिज़ाइन कंट्रोल", Icons.Filled.DashboardCustomize, Icons.Outlined.DashboardCustomize, Color(0xFF0284C7), testTag = "cp_open_cms") { onOpenAppManagement(0) },
                        PanelIconAction("cp_images", "Images", "इमेज बदलें", Icons.Filled.AddPhotoAlternate, Icons.Outlined.AddPhotoAlternate, Color(0xFFDB2777), testTag = "cp_open_images") { onOpenAppManagement(1) },
                        PanelIconAction("cp_texts", "Text & Font", "लिखावट बदलें", Icons.Filled.TextFields, Icons.Outlined.TextFields, Color(0xFF6D28D9), testTag = "cp_open_texts") { onOpenAppManagement(3) },
                        PanelIconAction("cp_accounts", "Accounts", "अकाउंट ($registeredAccountsCount)", Icons.Filled.ManageAccounts, Icons.Outlined.ManageAccounts, Color(0xFF2563EB), testTag = "cp_open_accounts", onClick = onOpenUserAuth),
                        PanelIconAction("cp_security", "Security", "सुरक्षा केंद्र", Icons.Filled.Security, Icons.Outlined.Security, Color(0xFF15803D), testTag = "cp_open_security", onClick = onOpenSecurityCenter),
                        PanelIconAction("cp_backup", "Backup", "बैकअप व रीस्टोर", Icons.Filled.Backup, Icons.Outlined.Backup, Color(0xFF0F766E), testTag = "cp_open_backup", onClick = onOpenBackup)
                    )
                )
            }

            // 3. UI, Storefront & Feature Visibility Controls
            if (selectedSection == 0 || selectedSection == 1) {
                ControlSectionCard(
                    title = "Storefront, Theme & App Controls (ऐप सेटिंग्स)",
                    subtitle = "Dark Mode, Language, Currency, Warehouse, Theme & Home Sections",
                    icon = Icons.Outlined.Palette,
                    accent = primaryAccent
                ) {
                    ControlToggleRow(
                        title = "Dark Mode Theme",
                        subtitle = "Switch app between Light & Dark mode instantly",
                        checked = isDarkMode,
                        onCheckedChange = onToggleDarkMode,
                        accent = primaryAccent,
                        testTag = "cp_toggle_dark_mode"
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Text("App Language (भाषा):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("English", "हिंदी", "Hinglish").forEach { lang ->
                            FilterChip(
                                selected = appLanguage == lang,
                                onClick = { onLanguageChange(lang) },
                                label = { Text(lang, fontSize = 11.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryAccent,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Text("Store Currency (मुद्रा):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("INR (₹)", "USD ($)", "NPR (₨)").forEach { curr ->
                            FilterChip(
                                selected = selectedCurrency == curr,
                                onClick = { onCurrencyChange(curr) },
                                label = { Text(curr, fontSize = 11.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0284C7),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Text("Active Dispatch Warehouse:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Bettiah Hub (Bihar)", "Patna Mega Hub", "Delhi NCR Hub").forEach { hub ->
                            FilterChip(
                                selected = activeWarehouse == hub,
                                onClick = { onWarehouseChange(hub) },
                                label = { Text(hub, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF7C3AED),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Text("Dashboard Theme Palette:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DashboardThemePreset.entries.forEach { preset ->
                            val isSel = managementConfig.themePreset == preset
                            FilterChip(
                                selected = isSel,
                                onClick = { onSelectThemePreset(preset) },
                                label = { Text(preset.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = preset.accentColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Text("Product Grid Columns:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HomeLayoutMode.entries.forEach { mode ->
                            FilterChip(
                                selected = managementConfig.homeLayoutMode == mode,
                                onClick = { onSelectLayoutMode(mode) },
                                label = { Text("${mode.columns} Col", fontSize = 11.5.sp) }
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Text("Live Announcement Ticker Banner:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = announcementInput,
                            onValueChange = { announcementInput = it },
                            placeholder = { Text("e.g. Mega Wedding Sale 50% OFF Today!") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                onUpdateTexts { it.copy(announcementText = announcementInput.trim()) }
                                Toast.makeText(context, "Announcement updated on Home", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryAccent),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Text("Home & Bottom Bar Module Switches:", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = TextPrimary)

                    val f = managementConfig.features
                    ControlToggleRow("Hero Offer Slider", "Show top promotional banners", f.showHeroSlider, { v -> onUpdateFeatures { it.copy(showHeroSlider = v) } }, primaryAccent)
                    ControlToggleRow("Announcement Ticker", "Show live announcement bar", f.showAnnouncementTicker, { v -> onUpdateFeatures { it.copy(showAnnouncementTicker = v) } }, primaryAccent)
                    ControlToggleRow("Flash Sale Countdown", "Show sale timer banner", f.showCountdownBanner, { v -> onUpdateFeatures { it.copy(showCountdownBanner = v) } }, primaryAccent)
                    ControlToggleRow("Category Strip", "Show horizontal categories on Home", f.showCategoryStrip, { v -> onUpdateFeatures { it.copy(showCategoryStrip = v) } }, primaryAccent)
                    ControlToggleRow("Filter & Sort Bar", "Show Sort / Category / Price filters", f.showFilterBar, { v -> onUpdateFeatures { it.copy(showFilterBar = v) } }, primaryAccent)
                    ControlToggleRow("Voice & Camera Search", "Show Mic & Camera buttons in search", f.showVoiceSearch && f.showCameraSearch, { v -> onUpdateFeatures { it.copy(showVoiceSearch = v, showCameraSearch = v) } }, primaryAccent)
                    ControlToggleRow("Add to Cart Buttons", "Show Add to Cart on product cards", f.showAddToCartButtons, { v -> onUpdateFeatures { it.copy(showAddToCartButtons = v) } }, primaryAccent)
                    ControlToggleRow("Wishlist Hearts", "Show favorite hearts on cards", f.showWishlistHearts, { v -> onUpdateFeatures { it.copy(showWishlistHearts = v) } }, primaryAccent)
                    ControlToggleRow("Categories Bottom Tab", "Show Categories tab in Bottom Bar", f.showCategoriesTab, { v -> onUpdateFeatures { it.copy(showCategoriesTab = v) } }, primaryAccent)
                    ControlToggleRow("Mall Bottom Tab", "Show Mall tab in Bottom Bar", f.showMallTab, { v -> onUpdateFeatures { it.copy(showMallTab = v) } }, primaryAccent)
                    ControlToggleRow("Video Finds Tab", "Show Video shopping tab", f.showVideoFindsTab, { v -> onUpdateFeatures { it.copy(showVideoFindsTab = v) } }, primaryAccent)
                    ControlToggleRow("My Orders Bottom Tab", "Show My Orders tab in Bottom Bar", f.showOrdersTab, { v -> onUpdateFeatures { it.copy(showOrdersTab = v) } }, primaryAccent)
                }

                SocialMediaAdsManagerSection(
                    config = managementConfig.socialAds,
                    accentColor = primaryAccent,
                    onToggleAdsEnabled = onToggleSocialAdsEnabled,
                    onToggleShowOnHome = onToggleSocialAdsHome,
                    onSaveCampaign = onSaveSocialAdCampaign,
                    onToggleCampaignActive = onToggleSocialAdActive,
                    onDeleteCampaign = onDeleteSocialAdCampaign,
                    onRecordAdClick = onRecordSocialAdClick,
                    modifier = Modifier.testTag("control_panel_social_ads")
                )
            }

            // 4. Catalog, Categories & Stock Control
            if (selectedSection == 0 || selectedSection == 2) {
                ControlSectionCard(
                    title = "Catalog, Categories & Stock Control (प्रोडक्ट व स्टॉक)",
                    subtitle = "${products.size} Products • ${categories.count { it.isActive }}/${categories.size} Active Categories",
                    icon = Icons.Outlined.Inventory2,
                    accent = Color(0xFFD97706)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showQuickAddProductDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryAccent),
                            modifier = Modifier.weight(1f).heightIn(min = 46.dp).testTag("cp_quick_add_product_btn")
                        ) {
                            Icon(Icons.Outlined.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Product", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onOpenAppManagement(5) },
                            modifier = Modifier.weight(1f).heightIn(min = 46.dp)
                        ) {
                            Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Full Catalog Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("Main Categories Active Status:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = cat.isActive,
                                onClick = {
                                    onToggleCategoryActive(cat.id, !cat.isActive)
                                    Toast.makeText(
                                        context,
                                        "${cat.name}: ${if (!cat.isActive) "Active" else "Hidden"}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                leadingIcon = {
                                    Icon(
                                        if (cat.isActive) Icons.Filled.CheckCircle else Icons.Outlined.VisibilityOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                label = { Text(cat.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = productSearchQuery,
                        onValueChange = { productSearchQuery = it },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        placeholder = { Text("Search product to adjust stock or delete...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val filteredItems = remember(products, productSearchQuery) {
                        val q = productSearchQuery.trim().lowercase()
                        val list = if (q.isBlank()) products else products.filter {
                            it.name.lowercase().contains(q) || it.category.lowercase().contains(q)
                        }
                        list.take(if (selectedSection == 2) 12 else 4)
                    }

                    filteredItems.forEach { prod ->
                        val roomEntity = roomProducts.firstOrNull { it.id == prod.id }
                        val currentStock = roomEntity?.stock ?: if (prod.inStock) 25 else 0
                        val isPaused = roomEntity?.isPaused ?: false

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${prod.category} • ₹${prod.price} • Stock: $currentStock", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onUpdateStock(prod.id, (currentStock - 5).coerceAtLeast(0)) },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Outlined.RemoveCircleOutline, contentDescription = "Decrease Stock", tint = Color(0xFFD97706))
                                        }
                                        IconButton(
                                            onClick = { onUpdateStock(prod.id, currentStock + 10) },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Outlined.AddCircleOutline, contentDescription = "Increase Stock", tint = Color(0xFF15803D))
                                        }
                                        IconButton(
                                            onClick = { onTogglePauseProduct(prod.id, !isPaused) },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                if (isPaused) Icons.Outlined.PlayCircle else Icons.Outlined.PauseCircle,
                                                contentDescription = if (isPaused) "Resume" else "Pause",
                                                tint = Color(0xFF2563EB)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                onDeleteProduct(prod.id)
                                                Toast.makeText(context, "Deleted ${prod.name}", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Outlined.Delete, contentDescription = "Delete Product", tint = Color(0xFFDC2626))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Orders & Live Tracking Control
            if (selectedSection == 0 || selectedSection == 3) {
                ControlSectionCard(
                    title = "Orders & Delivery Tracking Control (ऑर्डर कंट्रोल)",
                    subtitle = "Pending: $pendingCount • Shipped: $shippedCount • Delivered: $deliveredCount",
                    icon = Icons.Outlined.LocalShipping,
                    accent = Color(0xFF2563EB)
                ) {
                    ControlToggleRow(
                        title = "Auto Live Shipment Tracking",
                        subtitle = "Automatically update courier milestones for active orders",
                        checked = autoTrackingEnabled,
                        onCheckedChange = onToggleAutoTracking,
                        accent = Color(0xFF2563EB)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("All", "Ordered", "Shipped", "Delivered", "Cancelled").forEach { status ->
                            FilterChip(
                                selected = orderStatusFilter == status,
                                onClick = { orderStatusFilter = status },
                                label = { Text(status, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            onSyncLiveTracking()
                            Toast.makeText(context, "Synced live courier tracking for all shipments", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp)
                    ) {
                        Icon(Icons.Outlined.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync All Courier Tracking Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    val displayedOrders = remember(orders, orderStatusFilter, selectedSection) {
                        val filtered = when (orderStatusFilter) {
                            "Ordered" -> orders.filter { it.status == OrderStatus.ORDERED }
                            "Shipped" -> orders.filter { it.status == OrderStatus.SHIPPED }
                            "Delivered" -> orders.filter { it.status == OrderStatus.DELIVERED }
                            "Cancelled" -> orders.filter { it.status == OrderStatus.CANCELLED }
                            else -> orders
                        }
                        filtered.take(if (selectedSection == 3) 12 else 4)
                    }

                    if (displayedOrders.isEmpty()) {
                        Text("No orders matching $orderStatusFilter filter.", fontSize = 12.sp, color = TextSecondary)
                    } else {
                        displayedOrders.forEach { ord ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("#${ord.orderId} • ₹${ord.totalAmount}", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDBEAFE)) {
                                            Text(
                                                text = ord.sellerStage.ifBlank { ord.status.label },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1D4ED8),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text("${ord.product.name} • ${ord.customerName}", fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (ord.status == OrderStatus.ORDERED) {
                                            AssistChip(
                                                onClick = { onMarkOrderReadyToShip(ord.orderId) },
                                                label = { Text("Ready to Ship", fontSize = 10.5.sp) }
                                            )
                                            AssistChip(
                                                onClick = { onDispatchOrder(ord.orderId) },
                                                label = { Text("Dispatch", fontSize = 10.5.sp) }
                                            )
                                        }
                                        if (ord.status == OrderStatus.SHIPPED) {
                                            AssistChip(
                                                onClick = { onAdvanceTracking(ord.orderId) },
                                                label = { Text("Advance Tracking", fontSize = 10.5.sp) }
                                            )
                                        }
                                        if (ord.status != OrderStatus.CANCELLED && ord.status != OrderStatus.DELIVERED) {
                                            AssistChip(
                                                onClick = { onCancelOrder(ord.orderId) },
                                                label = { Text("Cancel", fontSize = 10.5.sp, color = Color(0xFFDC2626)) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Payments, VIP & Payment Gateway URL Link Control
            if (selectedSection == 0 || selectedSection == 4) {
                ControlSectionCard(
                    title = "Wallet, VIP & Gateway Control (पेमेंट कंट्रोल)",
                    subtitle = "Wallet: ₹$walletBalance • Rewards: $rewardPoints pts • Gateway: ${activeGateway?.name ?: "Not Linked"}",
                    icon = Icons.Outlined.Payments,
                    accent = Color(0xFF15803D)
                ) {
                    ControlToggleRow(
                        title = "Gyanvi Gold VIP Membership",
                        subtitle = "Free express delivery & 2x reward points across store",
                        checked = isVipSubscribed,
                        onCheckedChange = onToggleVip,
                        accent = Color(0xFF15803D),
                        testTag = "cp_toggle_vip"
                    )
                }

                PaymentGatewayConnectorSection(
                    config = managementConfig.paymentGateways,
                    onSaveAndConnect = onSaveAndConnectPaymentGateway,
                    onConnectGateway = onConnectPaymentGateway,
                    onDisconnectGateway = onDisconnectPaymentGateway,
                    onDeleteGateway = onDeletePaymentGateway,
                    onToggleAppendParams = onToggleAppendPaymentParams,
                    modifier = Modifier.testTag("control_panel_gateway_connector")
                )
            }

            // 7. Users, Broadcast Alerts, Notification Channels & Reset
            if (selectedSection == 0 || selectedSection == 5) {
                val syncStatusText = when (cloudSyncState) {
                    is CloudSyncState.Idle -> "Idle (Local Ready)"
                    is CloudSyncState.Syncing -> "Syncing..."
                    is CloudSyncState.Synced -> "Synced (${cloudSyncState.orderCount} orders)"
                    is CloudSyncState.Error -> "Error"
                }
                ControlSectionCard(
                    title = "Users, Broadcasts & Security (यूज़र व सिस्टम)",
                    subtitle = "Cloud Sync: $syncStatusText • $registeredAccountsCount Saved Accounts",
                    icon = Icons.Outlined.Security,
                    accent = Color(0xFF7C3AED)
                ) {
                    Text("Push Instant Broadcast Notice:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
                    OutlinedTextField(
                        value = broadcastTitle,
                        onValueChange = { broadcastTitle = it },
                        label = { Text("Notice Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = broadcastMsg,
                        onValueChange = { broadcastMsg = it },
                        label = { Text("Notice Message") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (broadcastTitle.isBlank() || broadcastMsg.isBlank()) {
                                Toast.makeText(context, "Enter both notice title and message", Toast.LENGTH_SHORT).show()
                            } else {
                                onPushBroadcast(broadcastTitle.trim(), broadcastMsg.trim(), "Offers")
                                broadcastTitle = ""
                                broadcastMsg = ""
                                Toast.makeText(context, "Broadcast alert sent!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp)
                    ) {
                        Icon(Icons.Outlined.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Broadcast Alert", fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Text("Notification Channels:", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = TextPrimary)
                    ControlToggleRow("Push Notifications", "Order & flash offer alerts", notificationPrefs.pushEnabled, { v -> onUpdateNotificationPrefs { it.copy(pushEnabled = v) } }, primaryAccent)
                    ControlToggleRow("WhatsApp Alerts", "Send order tracking on WhatsApp", notificationPrefs.whatsappEnabled, { v -> onUpdateNotificationPrefs { it.copy(whatsappEnabled = v) } }, primaryAccent)
                    ControlToggleRow("SMS Updates", "Dispatch & OTP SMS alerts", notificationPrefs.smsEnabled, { v -> onUpdateNotificationPrefs { it.copy(smsEnabled = v) } }, primaryAccent)
                    ControlToggleRow("Email Receipts", "Invoice & account notifications", notificationPrefs.emailEnabled, { v -> onUpdateNotificationPrefs { it.copy(emailEnabled = v) } }, primaryAccent)

                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("App & Packer Identity", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                            Text("Application Name: ${stringResource(R.string.app_name)}", fontSize = 11.5.sp, color = TextPrimary)
                            Text("Application ID: ${BuildConfig.APPLICATION_ID}", fontSize = 11.5.sp, color = Color(0xFF2563EB))
                            Text("Packer Name: Gyanvi Dresses Packaging & Logistics Hub", fontSize = 11.5.sp, color = TextPrimary)
                            Text("Packer ID: PKR-GD-845438", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MeeshoMagenta)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onOpenBackup,
                            modifier = Modifier.weight(1f).heightIn(min = 46.dp)
                        ) {
                            Icon(Icons.Outlined.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Backup Manager", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.weight(1f).heightIn(min = 46.dp)
                        ) {
                            Icon(Icons.Outlined.RestartAlt, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset App UI", fontSize = 12.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showQuickAddProductDialog) {
        var newProdName by remember { mutableStateOf("") }
        var newProdCat by remember { mutableStateOf(categories.firstOrNull()?.name ?: "Kurti & Suit") }
        var newProdPrice by remember { mutableStateOf("499") }
        var newProdMrp by remember { mutableStateOf("999") }
        var newProdStock by remember { mutableStateOf("50") }

        AlertDialog(
            onDismissRequest = { showQuickAddProductDialog = false },
            title = { Text("Quick Add Product", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newProdName,
                        onValueChange = { newProdName = it },
                        label = { Text("Product Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newProdCat,
                        onValueChange = { newProdCat = it },
                        label = { Text("Category") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newProdPrice,
                            onValueChange = { newProdPrice = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Price ₹") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newProdMrp,
                            onValueChange = { newProdMrp = it.filter { ch -> ch.isDigit() } },
                            label = { Text("MRP ₹") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newProdStock,
                            onValueChange = { newProdStock = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Stock") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProdName.isNotBlank()) {
                            val p = newProdPrice.toIntOrNull()?.coerceAtLeast(1) ?: 499
                            val m = newProdMrp.toIntOrNull()?.coerceAtLeast(p) ?: (p * 2)
                            val s = newProdStock.toIntOrNull()?.coerceAtLeast(0) ?: 50
                            onAddQuickProduct(newProdName.trim(), newProdCat.trim(), p, m, s)
                            showQuickAddProductDialog = false
                            Toast.makeText(context, "Product added to live catalog!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Enter product name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryAccent)
                ) {
                    Text("Publish Product", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickAddProductDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Dashboard & UI Settings?", fontWeight = FontWeight.Bold) },
            text = { Text("This restores theme, fonts, layout, and feature visibility defaults.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllSettings()
                        showResetDialog = false
                        Toast.makeText(context, "App settings restored to defaults", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Reset", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ControlMetricPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.16f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            Text(label, color = Color(0xFFFDE68A), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ControlSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = TextPrimary)
                    Text(subtitle, fontSize = 11.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            content()
        }
    }
}

@Composable
private fun ControlToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accent: Color,
    testTag: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = accent),
            modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
        )
    }
}
