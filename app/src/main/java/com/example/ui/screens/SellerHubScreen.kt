package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import com.example.R
import com.example.util.ImageSaver
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.SampleData
import com.example.model.CategoryItem
import com.example.model.OrderItem
import com.example.model.OrderStatus
import com.example.model.PaymentGatewayLinkItem
import com.example.model.PaymentGatewayProviderPreset
import com.example.model.PaymentGatewaySettingsConfig
import com.example.model.Product
import com.example.model.SellerOrderActions
import com.example.model.SocialAdPlatform
import com.example.model.SocialMediaAdCampaign
import com.example.model.SocialMediaAdsConfig
import com.example.model.SubCategoryItem
import com.example.ui.components.PanelIconAction
import com.example.ui.components.PanelIconActionGrid
import com.example.ui.components.QrCodeStudioDialog
import com.example.ui.components.ShippingLabelQrMetadata
import com.example.ui.theme.*
import com.example.util.QrCodeGenerator
import com.example.viewmodel.CloudSyncState

enum class SellerSubScreen {
    MAIN,
    BUY_PACKETS,
    PRICING,
    REDUCE_RTO,
    CLAIMS,
    RETURNS_HELP,
    CATALOG_UPLOAD,
    PAYMENTS,
    IMAGE_BULK_UPLOAD,
    QUALITY_SCORE,
    WAREHOUSE_SETTINGS,
    INFLUENCER_MARKETING,
    INSTANT_CASH,
    PRICE_RECOMMENDATION,
    BUSINESS_DASHBOARD,
    LEARNING_HUB,
    NOTICE_BOARD,
    SUPPORT,
    SETTINGS,
    STORE_KYC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerHubScreen(
    onBack: () -> Unit,
    onOpenAdminPanel: () -> Unit = {},
    orders: List<OrderItem> = SampleData.initialOrders,
    products: List<Product> = SampleData.sampleProducts,
    sellerOrderActions: SellerOrderActions = SellerOrderActions(),
    onReadyToShip: (String) -> Unit = sellerOrderActions.onReadyToShip,
    onDispatchOrder: (String) -> Unit = { id -> sellerOrderActions.onDispatchOrder(id, "Delhivery Express") },
    onCancelOrder: (String) -> Unit = { id -> sellerOrderActions.onCancelOrderDetailed(id, "Cancelled by Seller", "Seller", true) },
    onMarkLabelDownloaded: (String) -> Unit = sellerOrderActions.onMarkLabelDownloaded,
    onAddManualOrder: (customerName: String, address: String, product: Product, size: String, quantity: Int, paymentMethod: String) -> Unit = { _, _, _, _, _, _ -> },
    cloudSyncState: CloudSyncState = CloudSyncState.Idle,
    firebaseUserEmail: String = "",
    onGoogleCloudSignInClick: () -> Unit = {},
    onSyncAllOrdersClick: () -> Unit = {},
    initialBottomTab: Int = 0,
    initialOrderTab: Int = 1,
    roomProducts: List<com.example.data.local.ProductEntity> = emptyList(),
    onSaveRoomProduct: (com.example.data.local.ProductEntity) -> Unit = {},
    onUpdateRoomStock: (String, Int) -> Unit = { _, _ -> },
    onTogglePauseRoomProduct: (String, Boolean) -> Unit = { _, _ -> },
    onDeleteRoomProduct: (String) -> Unit = {},
    availableCategories: List<String> = emptyList(),
    availableSubCategories: List<String> = emptyList(),
    categories: List<CategoryItem> = SampleData.categoryList,
    subCategories: List<SubCategoryItem> = SampleData.defaultSubCategories,
    onPublishCatalogProduct: (
        name: String,
        category: String,
        subcategory: String,
        brandName: String,
        description: String,
        price: Int,
        mrp: Int,
        color: String,
        fabric: String,
        sizes: List<String>,
        numericSizes: List<String>,
        stock: Int,
        sku: String,
        weight: String,
        imageRes: Int,
        customImageUri: String?,
        extraImageUrls: List<String>
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    paymentGatewayConfig: PaymentGatewaySettingsConfig = LocalAppConfig.current.paymentGateways,
    onSaveAndConnectPaymentGateway: (id: String?, name: String, url: String, provider: PaymentGatewayProviderPreset, lastStatus: String, connectNow: Boolean) -> Result<PaymentGatewayLinkItem> = { id, name, url, provider, status, _ ->
        Result.success(PaymentGatewayLinkItem(id ?: "pg-seller", name, url, provider, true, status))
    },
    onConnectPaymentGateway: (String) -> Unit = {},
    onDisconnectPaymentGateway: () -> Unit = {},
    onDeletePaymentGateway: (String) -> Unit = {},
    onToggleAppendPaymentParams: (Boolean) -> Unit = {},
    socialAdsConfig: SocialMediaAdsConfig = LocalAppConfig.current.socialAds,
    onToggleSocialAdsEnabled: (Boolean) -> Unit = {},
    onToggleSocialAdsHome: (Boolean) -> Unit = {},
    onSaveSocialAdCampaign: (String?, String, SocialAdPlatform, String, String, String, Int, String, Boolean, Boolean) -> Result<SocialMediaAdCampaign> = { id, title, platform, headline, caption, url, budget, cta, showHome, active ->
        Result.success(SocialMediaAdCampaign(id ?: "ad-seller", title, platform, headline, caption, url, budget, isActive = active, showOnHomeBanner = showHome, ctaLabel = cta))
    },
    onToggleSocialAdActive: (String) -> Unit = {},
    onDeleteSocialAdCampaign: (String) -> Unit = {},
    onRecordSocialAdClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentSellerSubScreen by remember { mutableStateOf(SellerSubScreen.MAIN) }
    var selectedBottomTab by remember(initialBottomTab) { mutableIntStateOf(initialBottomTab) } // 0=Home, 1=Orders, 2=Returns, 3=Inventory, 4=Menu
    var initialDashboardStatusFilter by remember { mutableStateOf(DashboardOrderStatusFilter.PENDING) }

    when (currentSellerSubScreen) {
        SellerSubScreen.MAIN -> {
            SellerHubMainScaffold(
                selectedBottomTab = selectedBottomTab,
                onSelectBottomTab = { selectedBottomTab = it },
                onBack = onBack,
                onNavigateTo = { currentSellerSubScreen = it },
                onOpenDashboardWithFilter = { filter ->
                    initialDashboardStatusFilter = filter
                    currentSellerSubScreen = SellerSubScreen.BUSINESS_DASHBOARD
                },
                onOpenAdminPanel = onOpenAdminPanel,
                orders = orders,
                products = products,
                sellerOrderActions = sellerOrderActions,
                onReadyToShip = onReadyToShip,
                onDispatchOrder = onDispatchOrder,
                onCancelOrder = onCancelOrder,
                onMarkLabelDownloaded = onMarkLabelDownloaded,
                onAddManualOrder = onAddManualOrder,
                cloudSyncState = cloudSyncState,
                firebaseUserEmail = firebaseUserEmail,
                onGoogleCloudSignInClick = onGoogleCloudSignInClick,
                onSyncAllOrdersClick = onSyncAllOrdersClick,
                initialOrderTab = initialOrderTab,
                roomProducts = roomProducts,
                onSaveRoomProduct = onSaveRoomProduct,
                onUpdateRoomStock = onUpdateRoomStock,
                onTogglePauseRoomProduct = onTogglePauseRoomProduct,
                onDeleteRoomProduct = onDeleteRoomProduct,
                availableCategories = availableCategories,
                availableSubCategories = availableSubCategories
            )
        }
        SellerSubScreen.BUY_PACKETS -> {
            BuyBarcodedPacketsScreen(onBack = { currentSellerSubScreen = SellerSubScreen.MAIN })
        }
        SellerSubScreen.PRICING -> {
            SellerPricingScreen(
                onBack = { currentSellerSubScreen = SellerSubScreen.MAIN },
                onNavigateToReduceRto = { currentSellerSubScreen = SellerSubScreen.REDUCE_RTO }
            )
        }
        SellerSubScreen.REDUCE_RTO -> {
            ReduceRtoScreen(onBack = { currentSellerSubScreen = SellerSubScreen.MAIN })
        }
        SellerSubScreen.CLAIMS -> {
            SellerClaimsScreen(onBack = { currentSellerSubScreen = SellerSubScreen.MAIN })
        }
        SellerSubScreen.RETURNS_HELP -> {
            ReturnsHelpScreen(onBack = { currentSellerSubScreen = SellerSubScreen.MAIN })
        }
        SellerSubScreen.CATALOG_UPLOAD -> {
            SellerCatalogUploadScreen(
                onBack = { currentSellerSubScreen = SellerSubScreen.MAIN },
                categories = categories,
                subCategories = subCategories,
                existingProducts = products,
                onPublishProductToCatalog = onPublishCatalogProduct
            )
        }
        SellerSubScreen.PAYMENTS -> {
            SellerPaymentsScreen(
                onBack = { currentSellerSubScreen = SellerSubScreen.MAIN },
                onNavigateToHelp = { currentSellerSubScreen = SellerSubScreen.RETURNS_HELP },
                paymentGatewayConfig = paymentGatewayConfig,
                onSaveAndConnectPaymentGateway = onSaveAndConnectPaymentGateway,
                onConnectPaymentGateway = onConnectPaymentGateway,
                onDisconnectPaymentGateway = onDisconnectPaymentGateway,
                onDeletePaymentGateway = onDeletePaymentGateway,
                onToggleAppendPaymentParams = onToggleAppendPaymentParams
            )
        }
        SellerSubScreen.IMAGE_BULK_UPLOAD -> {
            SellerImageBulkUploadScreen(onBack = { currentSellerSubScreen = SellerSubScreen.MAIN })
        }
        SellerSubScreen.QUALITY_SCORE -> {
            SellerQualityScreen(onBack = { currentSellerSubScreen = SellerSubScreen.MAIN })
        }
        SellerSubScreen.WAREHOUSE_SETTINGS -> {
            SellerWarehouseScreen(onBack = { currentSellerSubScreen = SellerSubScreen.MAIN })
        }
        SellerSubScreen.INFLUENCER_MARKETING -> {
            SellerInfluencerMarketingScreen(
                onBack = { currentSellerSubScreen = SellerSubScreen.MAIN },
                socialAdsConfig = socialAdsConfig,
                onToggleSocialAdsEnabled = onToggleSocialAdsEnabled,
                onToggleSocialAdsHome = onToggleSocialAdsHome,
                onSaveSocialAdCampaign = onSaveSocialAdCampaign,
                onToggleSocialAdActive = onToggleSocialAdActive,
                onDeleteSocialAdCampaign = onDeleteSocialAdCampaign,
                onRecordSocialAdClick = onRecordSocialAdClick
            )
        }
        SellerSubScreen.INSTANT_CASH -> {
            SellerInstantCashScreen(onBack = { currentSellerSubScreen = SellerSubScreen.MAIN })
        }
        SellerSubScreen.PRICE_RECOMMENDATION -> {
            SellerPriceRecommendationScreen(onBack = { currentSellerSubScreen = SellerSubScreen.MAIN })
        }
        SellerSubScreen.BUSINESS_DASHBOARD -> {
            SellerBusinessDashboardScreen(
                orders = orders,
                products = products,
                cloudSyncState = cloudSyncState,
                firebaseUserEmail = firebaseUserEmail,
                sellerOrderActions = sellerOrderActions,
                initialStatusFilter = initialDashboardStatusFilter,
                onGoogleCloudSignInClick = onGoogleCloudSignInClick,
                onSyncAllOrdersClick = onSyncAllOrdersClick,
                onBack = { currentSellerSubScreen = SellerSubScreen.MAIN }
            )
        }
        SellerSubScreen.LEARNING_HUB -> {
            SellerLearningHubScreen(onBack = { currentSellerSubScreen = SellerSubScreen.MAIN })
        }
        SellerSubScreen.NOTICE_BOARD -> {
            SellerNoticeBoardScreen(onBack = { currentSellerSubScreen = SellerSubScreen.MAIN })
        }
        SellerSubScreen.SUPPORT -> {
            SellerSupportScreen(onBack = { currentSellerSubScreen = SellerSubScreen.MAIN })
        }
        SellerSubScreen.SETTINGS -> {
            SellerSettingsScreen(onBack = { currentSellerSubScreen = SellerSubScreen.MAIN })
        }
        SellerSubScreen.STORE_KYC -> {
            SellerKycAndStoreScreen(onBack = { currentSellerSubScreen = SellerSubScreen.MAIN })
        }
    }
}

// =========================================================================
// MAIN SELLER HUB SCAFFOLD WITH 5 BOTTOM TABS
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerHubMainScaffold(
    selectedBottomTab: Int,
    onSelectBottomTab: (Int) -> Unit,
    onBack: () -> Unit,
    onNavigateTo: (SellerSubScreen) -> Unit,
    onOpenDashboardWithFilter: (DashboardOrderStatusFilter) -> Unit = {},
    onOpenAdminPanel: () -> Unit = {},
    orders: List<OrderItem> = SampleData.initialOrders,
    products: List<Product> = SampleData.sampleProducts,
    sellerOrderActions: SellerOrderActions = SellerOrderActions(),
    onReadyToShip: (String) -> Unit = {},
    onDispatchOrder: (String) -> Unit = {},
    onCancelOrder: (String) -> Unit = {},
    onMarkLabelDownloaded: (String) -> Unit = {},
    onAddManualOrder: (customerName: String, address: String, product: Product, size: String, quantity: Int, paymentMethod: String) -> Unit = { _, _, _, _, _, _ -> },
    cloudSyncState: CloudSyncState = CloudSyncState.Idle,
    firebaseUserEmail: String = "",
    onGoogleCloudSignInClick: () -> Unit = {},
    onSyncAllOrdersClick: () -> Unit = {},
    initialOrderTab: Int = 1,
    roomProducts: List<com.example.data.local.ProductEntity> = emptyList(),
    onSaveRoomProduct: (com.example.data.local.ProductEntity) -> Unit = {},
    onUpdateRoomStock: (String, Int) -> Unit = { _, _ -> },
    onTogglePauseRoomProduct: (String, Boolean) -> Unit = { _, _ -> },
    onDeleteRoomProduct: (String) -> Unit = {},
    availableCategories: List<String> = emptyList(),
    availableSubCategories: List<String> = emptyList()
) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = CardWhite,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = selectedBottomTab == 0,
                    onClick = { onSelectBottomTab(0) },
                    icon = {
                        Icon(
                            if (selectedBottomTab == 0) Icons.Filled.Storefront else Icons.Outlined.Storefront,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = if (selectedBottomTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MeeshoMagenta,
                        selectedTextColor = MeeshoMagenta,
                        indicatorColor = Color(0xFFF3E5F5)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 1,
                    onClick = { onSelectBottomTab(1) },
                    icon = {
                        Icon(
                            if (selectedBottomTab == 1) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                            contentDescription = "Orders"
                        )
                    },
                    label = { Text("Orders", fontSize = 11.sp, fontWeight = if (selectedBottomTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MeeshoMagenta,
                        selectedTextColor = MeeshoMagenta,
                        indicatorColor = Color(0xFFF3E5F5)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 2,
                    onClick = { onSelectBottomTab(2) },
                    icon = {
                        Icon(
                            if (selectedBottomTab == 2) Icons.Filled.AssignmentReturn else Icons.Outlined.AssignmentReturn,
                            contentDescription = "Returns"
                        )
                    },
                    label = { Text("Returns", fontSize = 11.sp, fontWeight = if (selectedBottomTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MeeshoMagenta,
                        selectedTextColor = MeeshoMagenta,
                        indicatorColor = Color(0xFFF3E5F5)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 3,
                    onClick = { onSelectBottomTab(3) },
                    icon = {
                        Icon(
                            if (selectedBottomTab == 3) Icons.Filled.Assignment else Icons.Outlined.Assignment,
                            contentDescription = "Inventory"
                        )
                    },
                    label = { Text("Inventory", fontSize = 11.sp, fontWeight = if (selectedBottomTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MeeshoMagenta,
                        selectedTextColor = MeeshoMagenta,
                        indicatorColor = Color(0xFFF3E5F5)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 4,
                    onClick = { onSelectBottomTab(4) },
                    icon = {
                        Box {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3F51B5))
                                    .align(Alignment.TopEnd)
                            )
                        }
                    },
                    label = { Text("Menu", fontSize = 11.sp, fontWeight = if (selectedBottomTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MeeshoMagenta,
                        selectedTextColor = MeeshoMagenta,
                        indicatorColor = Color(0xFFF3E5F5)
                    )
                )
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedBottomTab) {
                0 -> SellerHomeTab(
                    onBack = onBack,
                    onNavigateTo = onNavigateTo,
                    onOpenDashboardWithFilter = onOpenDashboardWithFilter,
                    onSelectBottomTab = onSelectBottomTab,
                    onOpenAdminPanel = onOpenAdminPanel,
                    orders = orders,
                    products = products,
                    cloudSyncState = cloudSyncState,
                    onMarkLabelDownloaded = onMarkLabelDownloaded
                )
                1 -> SellerOrdersTab(
                    onNavigateTo = onNavigateTo,
                    orders = orders,
                    products = products,
                    sellerOrderActions = sellerOrderActions,
                    onReadyToShip = onReadyToShip,
                    onDispatchOrder = onDispatchOrder,
                    onCancelOrder = onCancelOrder,
                    onMarkLabelDownloaded = onMarkLabelDownloaded,
                    onAddManualOrder = onAddManualOrder,
                    cloudSyncState = cloudSyncState,
                    firebaseUserEmail = firebaseUserEmail,
                    onGoogleCloudSignInClick = onGoogleCloudSignInClick,
                    onSyncAllOrdersClick = onSyncAllOrdersClick,
                    initialOrderTab = initialOrderTab
                )
                2 -> SellerReturnsTab(
                    onNavigateTo = onNavigateTo,
                    orders = orders
                )
                3 -> SellerInventoryScreen(
                    onBack = { onSelectBottomTab(0) },
                    onNavigateToUpload = { onNavigateTo(SellerSubScreen.CATALOG_UPLOAD) },
                    onNavigateToHelp = { onNavigateTo(SellerSubScreen.RETURNS_HELP) },
                    roomProducts = roomProducts,
                    onSaveRoomProduct = onSaveRoomProduct,
                    onUpdateRoomStock = onUpdateRoomStock,
                    onTogglePauseRoomProduct = onTogglePauseRoomProduct,
                    onDeleteRoomProduct = onDeleteRoomProduct,
                    availableCategories = availableCategories,
                    availableSubCategories = availableSubCategories
                )
                4 -> SellerMenuTab(onBack = onBack, onNavigateTo = onNavigateTo)
            }
        }
    }
}

// =========================================================================
// TAB 0: SELLER HOME TAB (Screenshots 11:48, 11:49)
@Composable
private fun SellerHomeTab(
    onBack: () -> Unit,
    onNavigateTo: (SellerSubScreen) -> Unit,
    onOpenDashboardWithFilter: (DashboardOrderStatusFilter) -> Unit = {},
    onSelectBottomTab: (Int) -> Unit,
    onOpenAdminPanel: () -> Unit = {},
    orders: List<OrderItem> = SampleData.initialOrders,
    products: List<Product> = SampleData.sampleProducts,
    cloudSyncState: CloudSyncState = CloudSyncState.Idle,
    onMarkLabelDownloaded: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var showPolicyModal by remember { mutableStateOf(false) }
    var showDownloadLabelsDialog by remember { mutableStateOf(false) }
    var showQrStudioDialog by remember { mutableStateOf(false) }
    var activeLabelQrOrder by remember { mutableStateOf<OrderItem?>(null) }
    var showRtoFloatingBanner by remember { mutableStateOf(true) }
    var showSellerMoreMenu by remember { mutableStateOf(false) }

    val pendingOrdersCount = remember(orders) {
        orders.count {
            it.status == OrderStatus.ORDERED &&
                !it.sellerStage.equals("Ready to Ship", ignoreCase = true) &&
                !it.sellerStage.equals("On Hold", ignoreCase = true)
        }
    }
    val dispatchableOrders = remember(orders) {
        orders.filter { it.status == OrderStatus.ORDERED || it.status == OrderStatus.SHIPPED }
    }
    val outOfStockCount = remember(products) {
        products.count { !it.inStock }.coerceAtLeast(1)
    }
    val lowStockCount = remember(products) {
        products.count { it.inStock && it.sizes.size <= 4 }.coerceAtLeast(3)
    }
    val totalSalesAmount = remember(orders) {
        orders.filter { it.status != OrderStatus.CANCELLED }.sumOf { it.totalAmount }
    }
    val totalViewsCount = remember(products, orders) {
        products.size * 145 + orders.size * 42
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Header
        Surface(color = CardWhite, shadowElevation = 1.dp) {
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Back to Home button
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.heightIn(min = 36.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gyanvi Dresses", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                        }

                        // Switch to Admin Panel Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFEF3C7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier
                                .heightIn(min = 34.dp)
                                .clickable { onOpenAdminPanel() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309), maxLines = 1)
                            }
                        }

                        // Direct Business Dashboard Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE8EAF6),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC5CAE9)),
                            modifier = Modifier
                                .heightIn(min = 34.dp)
                                .clickable { onNavigateTo(SellerSubScreen.BUSINESS_DASHBOARD) }
                                .testTag("seller_home_dashboard_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Dashboard, contentDescription = null, tint = Color(0xFF1A237E), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E), maxLines = 1)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onNavigateTo(SellerSubScreen.RETURNS_HELP) }
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = "Help", tint = Color(0xFF616161), modifier = Modifier.size(22.dp))
                        }
                        Box {
                            IconButton(
                                onClick = { showSellerMoreMenu = true },
                                modifier = Modifier.testTag("seller_topbar_more_btn")
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = TextPrimary, modifier = Modifier.size(22.dp))
                            }
                            DropdownMenu(
                                expanded = showSellerMoreMenu,
                                onDismissRequest = { showSellerMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Outlined.Dashboard, contentDescription = null, tint = Color(0xFF1A237E)) },
                                    text = { Text("Dashboard (डैशबोर्ड)") },
                                    onClick = { showSellerMoreMenu = false; onNavigateTo(SellerSubScreen.BUSINESS_DASHBOARD) }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Outlined.AddCircleOutline, contentDescription = null, tint = MeeshoMagenta) },
                                    text = { Text("Add Product (प्रोडक्ट जोड़ें)") },
                                    onClick = { showSellerMoreMenu = false; onNavigateTo(SellerSubScreen.CATALOG_UPLOAD) }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Outlined.AccountBalance, contentDescription = null, tint = Color(0xFF15803D)) },
                                    text = { Text("Bank & Payments (बैंक व कमाई)") },
                                    onClick = { showSellerMoreMenu = false; onNavigateTo(SellerSubScreen.PAYMENTS) }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null, tint = Color(0xFFB45309)) },
                                    text = { Text("Admin Panel (एडमिन)") },
                                    onClick = { showSellerMoreMenu = false; onOpenAdminPanel() }
                                )
                            }
                        }
                    }
                }

                // Welcome back, Amit kumar patwa!
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(
                        text = "Welcome back, Amit kumar patwa!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "Manage and grow your business",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Upcoming Policy Update Yellow Banner (Screenshot 11:48)
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { showPolicyModal = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("📣", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Upcoming Policy Update: Next Day Dispatch is becoming the new platform standard for all orders.",
                        fontSize = 13.sp,
                        color = Color(0xFF5D4037),
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Know more",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { showPolicyModal = true }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // To do list Section (Screenshot 11:48)
        Text(
            text = "To do list",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Grid of 4 to-do cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToDoCard(
                    title = "Pending Orders",
                    count = "$pendingOrdersCount",
                    icon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFFFFA000)) },
                    onClick = { onSelectBottomTab(1) },
                    modifier = Modifier.weight(1f).testTag("seller_todo_pending_orders")
                )
                ToDoCard(
                    title = "Download Labels",
                    count = "${dispatchableOrders.size}",
                    icon = { Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF1976D2)) },
                    onClick = {
                        showDownloadLabelsDialog = true
                    },
                    modifier = Modifier.weight(1f).testTag("seller_todo_download_labels")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToDoCard(
                    title = "Out of Stock",
                    count = "$outOfStockCount",
                    icon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFD32F2F)) },
                    onClick = { onSelectBottomTab(3) },
                    modifier = Modifier.weight(1f)
                )
                ToDoCard(
                    title = "Low Stock",
                    count = "$lowStockCount",
                    icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFED6C02)) },
                    onClick = { onSelectBottomTab(3) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Live Firestore Orders Dashboard Card (5 Quick Status Filters)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFC5CAE9), RoundedCornerShape(14.dp))
                    .testTag("seller_home_firestore_dashboard_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Dashboard, contentDescription = null, tint = Color(0xFF1A237E), modifier = Modifier.size(16.dp))
                                Text("Firestore Orders Dashboard", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                                if (cloudSyncState is CloudSyncState.Synced) {
                                    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            "LIVE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF166534),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                "Tap any status to open live filtered order lists (${orders.size} total)",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        TextButton(
                            onClick = { onOpenDashboardWithFilter(DashboardOrderStatusFilter.ALL) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Open All →", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            DashboardOrderStatusFilter.PENDING,
                            DashboardOrderStatusFilter.READY_TO_SHIP,
                            DashboardOrderStatusFilter.SHIPPED,
                            DashboardOrderStatusFilter.CANCELLED,
                            DashboardOrderStatusFilter.ON_HOLD
                        ).forEach { statusFilter ->
                            val count = filterOrdersByDashboardStatus(orders, statusFilter).size
                            Surface(
                                color = statusFilter.accentColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, statusFilter.accentColor.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onOpenDashboardWithFilter(statusFilter) }
                                    .testTag("seller_home_dash_pill_${statusFilter.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(statusFilter.icon, contentDescription = null, tint = statusFilter.accentColor, modifier = Modifier.size(14.dp))
                                    Text(
                                        "${statusFilter.label} ($count)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusFilter.accentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Unified 15-Icon Seller Panel Quick Action Hub
            val readyToShipCount = filterOrdersByDashboardStatus(orders, DashboardOrderStatusFilter.READY_TO_SHIP).size
            val shippedCount = filterOrdersByDashboardStatus(orders, DashboardOrderStatusFilter.SHIPPED).size
            val cancelledCount = filterOrdersByDashboardStatus(orders, DashboardOrderStatusFilter.CANCELLED).size
            val returnsCount = orders.count { it.status == OrderStatus.RETURN || it.isRtoFlagged }
            PanelIconActionGrid(
                sectionTitle = "Seller Panel Quick Icons (सेलर पैनल)",
                sectionSubtitle = "Tap any icon to open Products, Orders, Payouts & Reports",
                accentColor = Color(0xFF303F9F),
                containerBorderColor = Color(0xFFC5CAE9),
                modifier = Modifier.testTag("seller_panel_icons_grid"),
                actions = listOf(
                    PanelIconAction("dashboard", "Dashboard", "डैशबोर्ड", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, Color(0xFF303F9F), testTag = "seller_icon_dashboard") {
                        onNavigateTo(SellerSubScreen.BUSINESS_DASHBOARD)
                    },
                    PanelIconAction("add_product", "Add Product", "प्रोडक्ट जोड़ना", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline, MeeshoMagenta, testTag = "seller_icon_add_product") {
                        onNavigateTo(SellerSubScreen.CATALOG_UPLOAD)
                    },
                    PanelIconAction("product_list", "Product List", "प्रोडक्ट लिस्ट", Icons.Filled.ViewList, Icons.Outlined.ViewList, Color(0xFF2563EB), badgeText = "${products.size}", testTag = "seller_icon_product_list") {
                        onSelectBottomTab(3)
                    },
                    PanelIconAction("edit_product", "Edit Product", "प्रोडक्ट एडिट", Icons.Filled.Edit, Icons.Outlined.Edit, Color(0xFF0284C7), testTag = "seller_icon_edit_product") {
                        Toast.makeText(context, "Select Edit on any product in Inventory", Toast.LENGTH_SHORT).show()
                        onSelectBottomTab(3)
                    },
                    PanelIconAction("delete_product", "Delete Product", "प्रोडक्ट डिलीट", Icons.Filled.Delete, Icons.Outlined.Delete, Color(0xFFDC2626), testTag = "seller_icon_delete_product") {
                        Toast.makeText(context, "Select Delete on any product in Inventory", Toast.LENGTH_SHORT).show()
                        onSelectBottomTab(3)
                    },
                    PanelIconAction("inventory", "Inventory", "स्टॉक मैनेजमेंट", Icons.Filled.Inventory, Icons.Outlined.Inventory2, Color(0xFFD97706), badgeText = "$lowStockCount low", testTag = "seller_icon_inventory") {
                        onSelectBottomTab(3)
                    },
                    PanelIconAction("orders", "Orders", "ऑर्डर मैनेजमेंट", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag, Color(0xFF7C3AED), badgeText = "${orders.size}", testTag = "seller_icon_orders") {
                        onSelectBottomTab(1)
                    },
                    PanelIconAction("pending", "Pending", "पेंडिंग ऑर्डर", Icons.Filled.HourglassTop, Icons.Outlined.HourglassEmpty, Color(0xFFE65100), badgeText = if (pendingOrdersCount > 0) "$pendingOrdersCount" else null, testTag = "seller_icon_pending") {
                        onOpenDashboardWithFilter(DashboardOrderStatusFilter.PENDING)
                    },
                    PanelIconAction("ready_to_ship", "Ready to Ship", "भेजने के लिए तैयार", Icons.Filled.LocalShipping, Icons.Outlined.LocalShipping, Color(0xFF0284C7), badgeText = if (readyToShipCount > 0) "$readyToShipCount" else null, testTag = "seller_icon_ready_to_ship") {
                        onOpenDashboardWithFilter(DashboardOrderStatusFilter.READY_TO_SHIP)
                    },
                    PanelIconAction("shipped", "Shipped", "भेज दिया गया", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, Color(0xFF16A34A), badgeText = if (shippedCount > 0) "$shippedCount" else null, testTag = "seller_icon_shipped") {
                        onOpenDashboardWithFilter(DashboardOrderStatusFilter.SHIPPED)
                    },
                    PanelIconAction("cancelled", "Cancelled", "कैंसिल ऑर्डर", Icons.Filled.Cancel, Icons.Outlined.Cancel, Color(0xFFDC2626), badgeText = if (cancelledCount > 0) "$cancelledCount" else null, testTag = "seller_icon_cancelled") {
                        onOpenDashboardWithFilter(DashboardOrderStatusFilter.CANCELLED)
                    },
                    PanelIconAction("returns", "Returns", "रिटर्न", Icons.Filled.AssignmentReturn, Icons.Outlined.AssignmentReturn, Color(0xFFB45309), badgeText = if (returnsCount > 0) "$returnsCount" else null, testTag = "seller_icon_returns") {
                        onSelectBottomTab(2)
                    },
                    PanelIconAction("earnings", "Earnings", "कमाई", Icons.Filled.CurrencyRupee, Icons.Outlined.CurrencyRupee, Color(0xFF15803D), testTag = "seller_icon_earnings") {
                        onNavigateTo(SellerSubScreen.PAYMENTS)
                    },
                    PanelIconAction("bank_account", "Bank Account", "बैंक अकाउंट", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance, Color(0xFF1D4ED8), testTag = "seller_icon_bank_account") {
                        onNavigateTo(SellerSubScreen.STORE_KYC)
                    },
                    PanelIconAction("reports", "Reports", "बिक्री रिपोर्ट", Icons.Filled.BarChart, Icons.Outlined.BarChart, Color(0xFF6D28D9), testTag = "seller_icon_reports") {
                        onNavigateTo(SellerSubScreen.BUSINESS_DASHBOARD)
                    }
                )
            )

            // QR Code Generator, Download & Save to Gallery Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MeeshoMagenta.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .clickable { showQrStudioDialog = true }
                    .testTag("seller_home_qr_studio_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MeeshoMagenta),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("QR Code Generate & Save to Gallery", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Order QR, Shipping Label & Store QR • Download PNG / Save Gallery", fontSize = 11.sp, color = TextSecondary)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MeeshoMagenta)
                }
            }

            // Branded Packets Card (Screenshot 11:48)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                    .clickable { onNavigateTo(SellerSubScreen.BUY_PACKETS) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEDE7F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color(0xFF5E35B1))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Branded Packets", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Scan Now", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF3F51B5))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reduce Prices to boost orders Banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onNavigateTo(SellerSubScreen.PRICING) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB300)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("₹", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reduce prices to boost orders", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("Sellers who reduced prices saw 3.4x order jump", fontSize = 11.sp, color = TextSecondary)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Payments & Settlements Card (Direct link to SellerPaymentsScreen)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .clickable { onNavigateTo(SellerSubScreen.PAYMENTS) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8EAF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF283593), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Payments & Settlements", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Active", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("Completed: ₹626.45 (13 Apr) • Upcoming (7d): ₹0.0", fontSize = 12.sp, color = TextSecondary)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Seller Registration, KYC, Shop Logo, Banner, GST, Bank & UPI Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2F8)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, MeeshoMagenta.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .clickable { onNavigateTo(SellerSubScreen.STORE_KYC) }
                .testTag("seller_home_kyc_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(MeeshoMagenta),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Seller Registration, KYC & Shop Branding", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    Text("Shop Name, Logo, Banner, Business Address, GST, Bank & UPI", fontSize = 11.sp, color = TextSecondary)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MeeshoMagenta)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Shipping, Courier & Dispatch Management Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                .clickable { onNavigateTo(SellerSubScreen.WAREHOUSE_SETTINGS) }
                .testTag("seller_home_shipping_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF1D4ED8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Shipping & Return / RTO Management", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E3A8A))
                    Text("AWB Tracking, Courier Pickup, Warehouse & RTO Shield", fontSize = 11.sp, color = Color(0xFF1D4ED8))
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF1D4ED8))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Business Performance Overview (Screenshot 11:49)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Business Performance", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFE8EAF6),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateTo(SellerSubScreen.BUSINESS_DASHBOARD) }
                    .testTag("seller_home_open_dashboard_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Dashboard, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open Dashboard →", fontSize = 11.sp, color = Color(0xFF3F51B5), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3 Key Metrics Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                    .clickable { onNavigateTo(SellerSubScreen.BUSINESS_DASHBOARD) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Sales (₹)", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("₹$totalSalesAmount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                }
            }

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                    .clickable { onNavigateTo(SellerSubScreen.BUSINESS_DASHBOARD) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Views", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$totalViewsCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                }
            }

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                    .clickable { onNavigateTo(SellerSubScreen.BUSINESS_DASHBOARD) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Orders", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${orders.size}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reduce RTO & Returns Promo Card (Screenshot 11:49)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Reduce RTO & Returns", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Reduce RTO", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFEDE7F6)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("18%", fontSize = 12.sp, color = Color(0xFF3949AB), fontWeight = FontWeight.Bold)
                    }
                    Text("  ———>  ", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFE8F5E9)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("2%", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
                Text("Add Prepaid Discount (COD Order -> Prepaid)", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Reduce Returns", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFEDE7F6)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("9%", fontSize = 12.sp, color = Color(0xFF3949AB), fontWeight = FontWeight.Bold)
                    }
                    Text("  ———>  ", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFE8F5E9)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("3%", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
                Text("Add WDRP Discount (Unwanted -> Genuine Returns)", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onNavigateTo(SellerSubScreen.REDUCE_RTO) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF303F9F)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Reduce RTO & Returns", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(140.dp))
    }

    // Floating wrong RTOs Banner (Screenshot 11:48)
    if (showRtoFloatingBanner) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.BottomCenter)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Issues with wrong RTOs?", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    IconButton(onClick = { showRtoFloatingBanner = false }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Scan branded packets to prevent wrong RTOs.", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onNavigateTo(SellerSubScreen.BUY_PACKETS) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                ) {
                    Text("Scan branded packets", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }

    if (showDownloadLabelsDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadLabelsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF1976D2))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Shipping Labels & QR (${dispatchableOrders.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("Tap any order to open QR Code & Save to Gallery or Download PNG:", fontSize = 12.sp, color = TextSecondary)
                    dispatchableOrders.take(4).forEach { ord ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDownloadLabelsDialog = false
                                    activeLabelQrOrder = ord
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Order #${ord.orderId}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A237E))
                                    Text("₹${ord.totalAmount}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MeeshoMagenta)
                                }
                                Text("AWB: ${ord.awbNumber} • ${ord.customerName}", fontSize = 11.sp, color = TextSecondary)
                                Text(ord.deliveryAddress, fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            val bmp = QrCodeGenerator.generateShippingLabelBitmap(
                                                orderId = ord.orderId,
                                                awbNumber = ord.awbNumber,
                                                customerName = ord.customerName,
                                                destination = ord.deliveryAddress,
                                                productTitle = ord.product.name,
                                                sku = ord.product.id.uppercase(),
                                                size = ord.selectedSize,
                                                amount = ord.totalAmount,
                                                paymentMethod = ord.paymentMethod,
                                                packerName = ord.packerName,
                                                packerId = ord.packerId
                                            )
                                            if (ImageSaver.saveBitmapToGallery(context, bmp, "Label_${ord.orderId}").success) {
                                                onMarkLabelDownloaded(ord.orderId)
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.height(34.dp).weight(1f)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Save Gallery", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            showDownloadLabelsDialog = false
                                            activeLabelQrOrder = ord
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.height(34.dp).weight(1f)
                                    ) {
                                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("QR Preview", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        var saved = 0
                        dispatchableOrders.forEach { ord ->
                            val bmp = QrCodeGenerator.generateShippingLabelBitmap(
                                orderId = ord.orderId,
                                awbNumber = ord.awbNumber,
                                customerName = ord.customerName,
                                destination = ord.deliveryAddress,
                                productTitle = ord.product.name,
                                sku = ord.product.id.uppercase(),
                                size = ord.selectedSize,
                                amount = ord.totalAmount,
                                paymentMethod = ord.paymentMethod,
                                packerName = ord.packerName,
                                packerId = ord.packerId
                            )
                            if (ImageSaver.saveBitmapToDownloads(context, bmp, "Label_${ord.orderId}", showToast = false).success) {
                                saved++
                                onMarkLabelDownloaded(ord.orderId)
                            }
                        }
                        Toast.makeText(context, "✓ Downloaded $saved Label + QR PNG(s) cleanly!", Toast.LENGTH_LONG).show()
                        showDownloadLabelsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("Download All (${dispatchableOrders.size})")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadLabelsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showQrStudioDialog) {
        val firstOrd = dispatchableOrders.firstOrNull()
        QrCodeStudioDialog(
            initialContent = firstOrd?.let {
                "GYANVI|ORD=${it.orderId}|AWB=${it.awbNumber}|AMT=${it.totalAmount}"
            } ?: "https://gyanvidresses.app/seller/amit-patwa",
            headerTitle = "GYANVI DRESSES QR",
            footerSubtitle = firstOrd?.let { "Order #${it.orderId} • ₹${it.totalAmount}" } ?: "Amit Kumar Patwa Boutique",
            fileNamePrefix = firstOrd?.let { "Order_${it.orderId}" } ?: "Gyanvi_Seller_QR",
            labelMetadata = firstOrd?.let { ord ->
                ShippingLabelQrMetadata(
                    orderId = ord.orderId,
                    awbNumber = ord.awbNumber,
                    customerName = ord.customerName,
                    destination = ord.deliveryAddress,
                    productTitle = ord.product.name,
                    sku = ord.product.id.uppercase(),
                    size = ord.selectedSize,
                    amount = ord.totalAmount,
                    paymentMethod = ord.paymentMethod,
                    packerName = ord.packerName,
                    packerId = ord.packerId
                )
            },
            onSavedToGalleryOrDownloaded = {
                firstOrd?.let { onMarkLabelDownloaded(it.orderId) }
            },
            onDismiss = { showQrStudioDialog = false }
        )
    }

    activeLabelQrOrder?.let { ord ->
        QrCodeStudioDialog(
            initialContent = "GYANVI|ORD=${ord.orderId}|AWB=${ord.awbNumber}|AMT=${ord.totalAmount}",
            headerTitle = "ORDER #${ord.orderId}",
            footerSubtitle = "AWB: ${ord.awbNumber} • ₹${ord.totalAmount}",
            fileNamePrefix = "Order_${ord.orderId}",
            labelMetadata = ShippingLabelQrMetadata(
                orderId = ord.orderId,
                awbNumber = ord.awbNumber,
                customerName = ord.customerName,
                destination = ord.deliveryAddress,
                productTitle = ord.product.name,
                sku = ord.product.id.uppercase(),
                size = ord.selectedSize,
                amount = ord.totalAmount,
                paymentMethod = ord.paymentMethod,
                packerName = ord.packerName,
                packerId = ord.packerId
            ),
            onSavedToGalleryOrDownloaded = { onMarkLabelDownloaded(ord.orderId) },
            onDismiss = { activeLabelQrOrder = null }
        )
    }

    if (showPolicyModal) {
        Dialog(
            onDismissRequest = { showPolicyModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showPolicyModal = false },
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
                            Text("Upcoming Policy Update", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            IconButton(onClick = { showPolicyModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE91E63))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Faster dispatch policy", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Dispatch all orders by 'next day'", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Starting 23rd Jun, sellers on Gyanvi Dresses will need to dispatch all orders by the next day. You'll be informed at least 1 week in advance before it applies to you.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        listOf(
                            "Faster Dispatch = Potentially more orders",
                            "Expected reduction in cancellations, returns & RTO",
                            "Builds customer trust"
                        ).forEach { checkText ->
                            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFF4CAF50)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(checkText, fontSize = 13.sp, color = TextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { showPolicyModal = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.width(110.dp).height(42.dp)
                        ) {
                            Text("Got it", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun ToDoCard(
    title: String,
    count: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(count, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF3F51B5))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// =========================================================================
// TAB 2: SELLER RETURNS TAB (Screenshots 12:02, 12:03)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerReturnsTab(
    onNavigateTo: (SellerSubScreen) -> Unit,
    orders: List<OrderItem> = SampleData.initialOrders
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Overview, 1=Return Tracking, 2=Claim Tracking
    var showTrendModal by remember { mutableStateOf(false) }
    var selectedTrendTab by remember { mutableIntStateOf(0) }
    val returnOrders = remember(orders) {
        orders.filter { it.status == OrderStatus.RETURN || it.status == OrderStatus.EXCHANGE }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Surface(color = CardWhite, shadowElevation = 1.dp) {
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Return/RTO Orders", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                    Row {
                        IconButton(onClick = { Toast.makeText(context, "Search returns", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                        }
                        IconButton(onClick = { onNavigateTo(SellerSubScreen.RETURNS_HELP) }) {
                            Icon(Icons.Default.HelpOutline, contentDescription = "Help", tint = TextPrimary)
                        }
                    }
                }

                // Learn how Return / RTO works
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { Toast.makeText(context, "Opening RTO Guide...", Toast.LENGTH_SHORT).show() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Learn how Return / RTO works", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CardWhite,
                    contentColor = Color(0xFF3F51B5)
                ) {
                    listOf("Overview", "Return Tracking", "Claim Tracking").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) Color(0xFF3F51B5) else TextSecondary
                                )
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> {
                // Overview Tab
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Summary: Last 1 Month ▾", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "View Trend",
                                color = Color(0xFF3F51B5),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable { showTrendModal = true }
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Customer Returns", fontSize = 11.sp, color = TextSecondary)
                                Text("${returnOrders.size}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Courier Returns (RTO)", fontSize = 11.sp, color = TextSecondary)
                                Text("0", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }

                        if (returnOrders.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFEEEEEE))
                            Spacer(modifier = Modifier.height(8.dp))
                            returnOrders.forEach { retOrd ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("#${retOrd.orderId} • ${retOrd.product.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${retOrd.statusText} • AWB: ${retOrd.awbNumber}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Text("₹${retOrd.totalAmount}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MeeshoMagenta)
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Return Tracking Tab
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("In transit (0)", "Out for Delivery (0)", "Delivered (0)", "Lost (0)").forEach { sub ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFEEEEEE))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(sub, fontSize = 11.sp, color = TextPrimary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                    Text("No returns currently in transit", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = TextSecondary)
                }
            }
            2 -> {
                // Claim Tracking
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.FactCheck, contentDescription = null, tint = Color(0xFF90A4AE), modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Looking for Claim Tracking?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onNavigateTo(SellerSubScreen.CLAIMS) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF303F9F))
                    ) {
                        Text("Go to Claims Tab")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    if (showTrendModal) {
        Dialog(
            onDismissRequest = { showTrendModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showTrendModal = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth().clickable { /* absorb */ }
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Returns Trend", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showTrendModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Text("Duration: Last 1 Month", fontSize = 13.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Customer Return", "Avg Reverse Cost", "Courier (RTO)", "Dual Pricing").forEachIndexed { idx, label ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selectedTrendTab == idx) Color(0xFFEDE7F6) else CardWhite)
                                        .border(1.dp, if (selectedTrendTab == idx) Color(0xFF3F51B5) else Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
                                        .clickable { selectedTrendTab = idx }
                                        .padding(vertical = 8.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, fontSize = 10.sp, textAlign = TextAlign.Center, lineHeight = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.FilterAlt, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(60.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No data as of now.", fontSize = 14.sp, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 4: SELLER MENU TAB
// =========================================================================
@Composable
private fun SellerMenuTab(
    onBack: () -> Unit,
    onNavigateTo: (SellerSubScreen) -> Unit
) {
    val context = LocalContext.current
    var isPricingExpanded by remember { mutableStateOf(true) }
    var isPromotionsExpanded by remember { mutableStateOf(false) }
    var showAddPromotionDialog by remember { mutableStateOf(false) }
    var promoName by remember { mutableStateOf("Festive Mega Discount") }
    var promoDiscount by remember { mutableStateOf("15") }
    var promoCouponCode by remember { mutableStateOf("FESTIVE15") }
    var promoMinOrder by remember { mutableStateOf("₹499") }

    val promotionsList = remember {
        mutableStateListOf(
            Triple("Smart Coins Cashback", "Customers earn 5% coins on prepaid orders", "Active"),
            Triple("Flash Price Drop", "Additional ₹30 instant discount on Sharara sets", "Active"),
            Triple("Free Delivery Voucher", "Free shipping on orders above ₹499", "Active")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Surface(color = CardWhite, shadowElevation = 1.dp) {
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEDE7F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Amit kumar patwa", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFEDE7F6))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("0.0 ★", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("0 Ratings", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("0 Followers", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Manage Business Section
        Text("Manage Business", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Expandable Pricing Menu Item (Screenshot 6:36)
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPricingExpanded = !isPricingExpanded }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFF3E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pricing", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    }
                    Icon(
                        if (isPricingExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }

                AnimatedVisibility(visible = isPricingExpanded) {
                    Column(modifier = Modifier.padding(start = 32.dp, end = 16.dp, bottom = 12.dp)) {
                        // Sub-item 1: Manage Pricing
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateTo(SellerSubScreen.PRICING) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("├── ", color = Color.Gray, fontSize = 14.sp)
                            Text("Manage Pricing", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        // Sub-item 2: Reduce RTOs & Returns
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateTo(SellerSubScreen.REDUCE_RTO) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("└── ", color = Color.Gray, fontSize = 14.sp)
                            Text("Reduce RTOs & Returns", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Claims, Catalog Upload, Image Bulk Upload, Payments, Quality, Warehouse (Screenshot 6:36)
        listOf(
            Triple("Claims", "Track claims and raise new issues", Icons.Default.FactCheck) to { onNavigateTo(SellerSubScreen.CLAIMS) },
            Triple("Catalog Upload", "Add single or multiple catalogs", Icons.Default.CloudUpload) to { onNavigateTo(SellerSubScreen.CATALOG_UPLOAD) },
            Triple("Image Bulk Upload", "Upload multiple high-res product photos", Icons.Default.PhotoLibrary) to {
                onNavigateTo(SellerSubScreen.IMAGE_BULK_UPLOAD)
            },
            Triple("Payments", "Next payout, bank settlement and deductions", Icons.Default.AccountBalance) to {
                onNavigateTo(SellerSubScreen.PAYMENTS)
            },
            Triple("Quality", "Customer return quality and rating reports", Icons.Default.Verified) to {
                onNavigateTo(SellerSubScreen.QUALITY_SCORE)
            },
            Triple("Warehouse", "Pickup address, slots and operational settings", Icons.Default.Warehouse) to {
                onNavigateTo(SellerSubScreen.WAREHOUSE_SETTINGS)
            }
        ).forEach { (item, action) ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { action() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEDE7F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(item.third, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.first, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text(item.second, fontSize = 11.sp, color = TextSecondary)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Boost Sales Section (Screenshot 6:36)
        Text("Boost Sales", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))

        // Influencer Marketing
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { onNavigateTo(SellerSubScreen.INFLUENCER_MARKETING) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE8EAF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFF283593), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Influencer Marketing", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("Collaborate with top social creators", fontSize = 11.sp, color = TextSecondary)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        // Promotions Accordion (Screenshot 6:36)
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPromotionsExpanded = !isPromotionsExpanded }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFCE4EC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Percent, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Promotions", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text("Discounts, coupons & smart coins", fontSize = 11.sp, color = TextSecondary)
                    }
                    Icon(
                        if (isPromotionsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }

                AnimatedVisibility(visible = isPromotionsExpanded) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        promotionsList.forEach { (title, subtitle, status) ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                        Text(subtitle, fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Box(
                                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFE8F5E9)).padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(status, color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showAddPromotionDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Create Custom Promotion (Text Box)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 0. Seller Registration, KYC & Shop Profile (Logo, Banner, GST, Bank, UPI)
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { onNavigateTo(SellerSubScreen.STORE_KYC) }
                .testTag("menu_store_kyc")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFCE7F3)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Seller Registration, KYC & Shop Logo/Banner", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("Shop Name, Address, GST, Bank Account & UPI Setup", fontSize = 11.sp, color = TextSecondary)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        // 3. Price Recommendation (Screenshot 6:05)
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { onNavigateTo(SellerSubScreen.PRICE_RECOMMENDATION) }
                .testTag("menu_price_recommendation")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Price Recommendation", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("Competitive pricing suggestions to boost daily orders", fontSize = 11.sp, color = TextSecondary)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        // 4. Buy Barcoded Packets (Screenshot 6:04)
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { onNavigateTo(SellerSubScreen.BUY_PACKETS) }
                .testTag("menu_buy_barcoded_packets")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Buy Barcoded Packets", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("Mandatory packaging for RTO claims", fontSize = 11.sp, color = TextSecondary)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        // 5. Instant Cash (Screenshots 6:04 & 6:05)
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { onNavigateTo(SellerSubScreen.INSTANT_CASH) }
                .testTag("menu_instant_cash")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE0F7FA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF00838F), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Instant Cash", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(Color(0xFF00838F)).padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("MPPL", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("Pre-approved loans for working capital", fontSize = 11.sp, color = TextSecondary)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Performance Section (Screenshot 6:05)
        Text("Performance", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { onNavigateTo(SellerSubScreen.BUSINESS_DASHBOARD) }
                .testTag("menu_business_dashboard")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Poll, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Business Dashboard", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("Gross sales, visitor funnel & operational analytics", fontSize = 11.sp, color = TextSecondary)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // More Section (Screenshot 6:05)
        Text("More", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Learning Hub (Screenshot 6:05)
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { onNavigateTo(SellerSubScreen.LEARNING_HUB) }
                .testTag("menu_learning_hub")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEDE7F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Learning Hub", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("Seller academy, video guides & masterclasses", fontSize = 11.sp, color = TextSecondary)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        // 2. Notice Board (Screenshot 6:05)
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { onNavigateTo(SellerSubScreen.NOTICE_BOARD) }
                .testTag("menu_notice_board")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFF8E1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Notice Board", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("Festive sales, platform updates & policy notices", fontSize = 11.sp, color = TextSecondary)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        // 3. Support (Screenshot 6:05)
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { onNavigateTo(SellerSubScreen.SUPPORT) }
                .testTag("menu_support")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE0F2F1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.HeadsetMic, contentDescription = null, tint = Color(0xFF00796B), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Support", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("24/7 Seller partner helpdesk & ticket tracking", fontSize = 11.sp, color = TextSecondary)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        // 4. Settings (Screenshot 6:05)
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { onNavigateTo(SellerSubScreen.SETTINGS) }
                .testTag("menu_settings")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFECEFF1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF455A64), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("Store profile, bank details & order alerts", fontSize = 11.sp, color = TextSecondary)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp)
                .testTag("switch_to_customer_mode_btn")
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Switch to Customer Mode (Buyer App)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showAddPromotionDialog) {
        AlertDialog(
            onDismissRequest = { showAddPromotionDialog = false },
            title = { Text("Create Promotion (Text Box)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = promoName,
                        onValueChange = { promoName = it },
                        label = { Text("Promotion / Campaign Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = promoDiscount,
                            onValueChange = { promoDiscount = it },
                            label = { Text("Discount (%)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = promoCouponCode,
                            onValueChange = { promoCouponCode = it.uppercase() },
                            label = { Text("Coupon Code") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = promoMinOrder,
                        onValueChange = { promoMinOrder = it },
                        label = { Text("Minimum Order Value") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        promotionsList.add(
                            Triple(promoName, "Flat $promoDiscount% off using $promoCouponCode on min $promoMinOrder", "Active")
                        )
                        showAddPromotionDialog = false
                        Toast.makeText(context, "Promotion '$promoName' activated!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Activate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPromotionDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// =========================================================================
// SUB-SCREEN 1: BUY BARCODED PACKETS SCREEN
// =========================================================================
@Composable
fun BuyBarcodedPacketsScreen(onBack: () -> Unit) {
    FullBuyBarcodedPacketsScreen(onBack = onBack)
}

// =========================================================================
// SUB-SCREEN 2: PRICING SCREEN
// =========================================================================
data class PricingProductItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val catalogInfo: String,
    val price: Int,
    @DrawableRes val imageRes: Int = R.drawable.dress_anarkali_1793417013639
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerPricingScreen(
    onBack: () -> Unit,
    onNavigateToReduceRto: () -> Unit
) {
    val context = LocalContext.current
    var editingProduct by remember { mutableStateOf<PricingProductItem?>(null) }
    var editedPrice by remember { mutableStateOf("") }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var newProductTitle by remember { mutableStateOf("") }
    var newProductStyleId by remember { mutableStateOf("") }
    var newProductCatalogId by remember { mutableStateOf("4533" + (10000..99999).random()) }
    var newProductPrice by remember { mutableStateOf("") }
    var newProductImageRes by remember { mutableIntStateOf(R.drawable.dress_anarkali_1793417013639) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0=All Products, 1=Losing Orders (0), 2=High Demand
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBox by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("Default") }

    val products = remember {
        mutableStateListOf(
            PricingProductItem(
                id = "p1",
                title = "Peplum Kurti & Tiered Sharara Set",
                catalogInfo = "Catalog ID: 453322234 • Style ID: SEA-GRN-SHR-02",
                price = 425,
                imageRes = R.drawable.dress_lehenga_1793417067159
            ),
            PricingProductItem(
                id = "p2",
                title = "Girls Premium Yellow Georgette Embroidered Peplum Sharara Set",
                catalogInfo = "Catalog ID: 453316827 • Style ID: Yellow -09",
                price = 425,
                imageRes = R.drawable.dress_saree_1793417052582
            ),
            PricingProductItem(
                id = "p3",
                title = "Girls Sharara Set - Black Festive Edition",
                catalogInfo = "Catalog ID: 453305717 • Style ID: Black -emb-01",
                price = 405,
                imageRes = R.drawable.dress_anarkali_1793417013639
            )
        )
    }

    val displayProducts = products.filter {
        searchQuery.isBlank() ||
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.catalogInfo.contains(searchQuery, ignoreCase = true)
    }.let { list ->
        when (selectedSort) {
            "Price: Low to High" -> list.sortedBy { it.price }
            "Price: High to Low" -> list.sortedByDescending { it.price }
            else -> list
        }
    }

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
                            Text("Pricing", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showSearchBox = !showSearchBox }) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                            }
                            IconButton(onClick = { Toast.makeText(context, "Pricing tips: Competitive prices improve views by 3x", Toast.LENGTH_SHORT).show() }) {
                                Icon(Icons.Default.HelpOutline, contentDescription = "Help", tint = TextPrimary)
                            }
                        }
                    }

                    if (showSearchBox) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search catalog or style ID...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(color = CardWhite, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(52.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { showSortSheet = true },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sort", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    VerticalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.height(28.dp))
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { showFilterSheet = true },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Filter", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    text = "Overview",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            // Price Performance Card (Screenshot 6:36)
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Price Performance", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Last 30 days", fontSize = 13.sp, color = Color(0xFF3F51B5), fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Order Growth", fontSize = 12.sp, color = TextSecondary)
                                Text("0", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                            }
                            Column {
                                Text("Price Updates", fontSize = 12.sp, color = TextSecondary)
                                Text("0", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Losing Order", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("0 Products", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        }
                        Text("Reduce price and increase more orders", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }

            // Reduce RTO & Returns Card (Screenshot 6:36)
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Reduce RTO & Returns", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Reduce RTO", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFEDE7F6)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text("18%", fontSize = 12.sp, color = Color(0xFF3949AB), fontWeight = FontWeight.Bold)
                            }
                            Text("  ———>  ", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFE8F5E9)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text("2%", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("Add Prepaid Discount (COD Order -> Prepaid)", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Reduce Returns", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFEDE7F6)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text("9%", fontSize = 12.sp, color = Color(0xFF3949AB), fontWeight = FontWeight.Bold)
                            }
                            Text("  ———>  ", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFE8F5E9)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text("3%", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("Add WDRP Discount (Unwanted -> Genuine Returns)", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onNavigateToReduceRto,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283593)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Text("Reduce RTO & Returns", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tabs Row: All Products (3) | Losing Orders (0) | High Demand (0) (Screenshot 6:36)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CardWhite,
                    contentColor = Color(0xFF3F51B5)
                ) {
                    listOf("All Products (${products.size})", "Losing Orders (0)", "High Demand (0)").forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) Color(0xFF3F51B5) else TextSecondary
                                )
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${displayProducts.size} Products Found",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    OutlinedButton(
                        onClick = { showAddProductDialog = true },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Product Price", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (selectedTab == 0) {
                items(displayProducts.size) { index ->
                    val prod = displayProducts[index]
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Product Image with Download Button
                            Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp))) {
                                Image(
                                    painter = painterResource(id = prod.imageRes),
                                    contentDescription = prod.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = {
                                        ImageSaver.saveToGallery(context, prod.imageRes, prod.title)
                                        Toast.makeText(context, "✓ फोटो गैलरी में सेव हो गई!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(prod.catalogInfo, fontSize = 11.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Customer Price", fontSize = 10.sp, color = TextSecondary)
                                        Text("₹${prod.price}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextPrimary)
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                val allImages = listOf(
                                                    R.drawable.dress_anarkali_1793417013639,
                                                    R.drawable.dress_lehenga_1793417067159,
                                                    R.drawable.dress_saree_1793417052582
                                                )
                                                val currIdx = allImages.indexOf(prod.imageRes)
                                                val nextRes = allImages[(currIdx + 1) % allImages.size]
                                                val idx = products.indexOfFirst { it.id == prod.id }
                                                if (idx != -1) {
                                                    products[idx] = prod.copy(imageRes = nextRes)
                                                    Toast.makeText(context, "✓ प्रोडक्ट इमेज बदली गई!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Image", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                editingProduct = prod
                                                editedPrice = prod.price.toString()
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF303F9F)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Edit Price", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No products in this category", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("All your product prices are competitive and optimized.", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(50.dp)) }
        }
    }

    // Dialog: Edit Price (Text Box) with Image Preview
    editingProduct?.let { prod ->
        AlertDialog(
            onDismissRequest = { editingProduct = null },
            title = { Text("Update Customer Price", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = prod.imageRes),
                            contentDescription = prod.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(50.dp).clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(prod.catalogInfo, fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    OutlinedTextField(
                        value = editedPrice,
                        onValueChange = { editedPrice = it },
                        label = { Text("Customer Price (₹) *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newPriceInt = editedPrice.toIntOrNull() ?: prod.price
                        val index = products.indexOfFirst { it.id == prod.id }
                        if (index != -1) {
                            products[index] = prod.copy(price = newPriceInt)
                        }
                        editingProduct = null
                        Toast.makeText(context, "Price updated to ₹$newPriceInt", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingProduct = null }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Add Product Price (Text Box) with Image Option
    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("+ Add New Product Price", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Image Selector
                    Text("Choose Product Image:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            R.drawable.dress_anarkali_1793417013639,
                            R.drawable.dress_lehenga_1793417067159,
                            R.drawable.dress_saree_1793417052582
                        ).forEach { resId ->
                            Card(
                                shape = RoundedCornerShape(6.dp),
                                border = if (newProductImageRes == resId) androidx.compose.foundation.BorderStroke(2.dp, MeeshoMagenta) else null,
                                modifier = Modifier.size(54.dp).clickable { newProductImageRes = resId }
                            ) {
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newProductTitle,
                        onValueChange = { newProductTitle = it },
                        label = { Text("Product Title *") },
                        placeholder = { Text("e.g. Georgette Kurti Set") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newProductStyleId,
                        onValueChange = { newProductStyleId = it },
                        label = { Text("Style ID *") },
                        placeholder = { Text("e.g. GYN-KRT-08") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newProductPrice,
                        onValueChange = { newProductPrice = it },
                        label = { Text("Customer Price (₹) *") },
                        placeholder = { Text("e.g. 449") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceInt = newProductPrice.toIntOrNull() ?: 399
                        if (newProductTitle.isNotBlank()) {
                            products.add(
                                0,
                                PricingProductItem(
                                    title = newProductTitle,
                                    catalogInfo = "Catalog ID: $newProductCatalogId • Style ID: ${if (newProductStyleId.isBlank()) "GYN-STYLE-01" else newProductStyleId}",
                                    price = priceInt,
                                    imageRes = newProductImageRes
                                )
                            )
                            showAddProductDialog = false
                            newProductTitle = ""
                            newProductStyleId = ""
                            newProductPrice = ""
                            Toast.makeText(context, "Product pricing added successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter product title", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                ) {
                    Text("Add Product")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showSortSheet) {
        AlertDialog(
            onDismissRequest = { showSortSheet = false },
            title = { Text("Sort By", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("Default", "Price: Low to High", "Price: High to Low").forEach { sortOpt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSort = sortOpt
                                    showSortSheet = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedSort == sortOpt, onClick = {
                                selectedSort = sortOpt
                                showSortSheet = false
                            })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(sortOpt, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortSheet = false }) { Text("Close") }
            }
        )
    }

    if (showFilterSheet) {
        AlertDialog(
            onDismissRequest = { showFilterSheet = false },
            title = { Text("Filter Products", fontWeight = FontWeight.Bold) },
            text = {
                Text("Showing all items across Gyanvi ethnic collections. Use search bar above for instant keyword search.")
            },
            confirmButton = {
                Button(onClick = { showFilterSheet = false }) { Text("Apply") }
            }
        )
    }
}

// =========================================================================
// SUB-SCREEN 3: REDUCE RTO SCREEN
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReduceRtoScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var prepaidDiscount by remember { mutableStateOf("15") }
    var wdrpDiscount by remember { mutableStateOf("20") }

    val rtoProducts = remember {
        mutableStateListOf(
            PricingProductItem(
                id = "r1",
                title = "Peplum Kurti & Tiered Sharara Set",
                catalogInfo = "Catalog ID: 453322234 • Fast Moving",
                price = 425,
                imageRes = R.drawable.dress_lehenga_1793417067159
            ),
            PricingProductItem(
                id = "r2",
                title = "Girls Premium Yellow Georgette Sharara Set",
                catalogInfo = "Catalog ID: 453316827 • Festive Special",
                price = 425,
                imageRes = R.drawable.dress_saree_1793417052582
            ),
            PricingProductItem(
                id = "r3",
                title = "Girls Premium Net Embroidered Ethnic Gown",
                catalogInfo = "Catalog ID: 453305717 • Top Seller",
                price = 405,
                imageRes = R.drawable.dress_anarkali_1793417013639
            )
        )
    }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("Reduce RTO & Returns", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Prepaid Discount Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Incentivize customers to pay online to reduce Return To Origin (RTO).", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = prepaidDiscount,
                        onValueChange = { prepaidDiscount = it },
                        label = { Text("Prepaid Discount Amount (₹)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Wrong / Defective Return Price (WDRP)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Offer lower prices for customers who agree to only return if product is defective.", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = wdrpDiscount,
                        onValueChange = { wdrpDiscount = it },
                        label = { Text("WDRP Discount Amount (₹)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Products with Image Section (Prompt 5 request)
            Text(
                text = "Products Eligible for RTO & Return Reduction (${rtoProducts.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
            )

            val pDisc = prepaidDiscount.toIntOrNull() ?: 15
            val wDisc = wdrpDiscount.toIntOrNull() ?: 20

            rtoProducts.forEachIndexed { index, prod ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Product Image with Download Button
                        Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp))) {
                            Image(
                                painter = painterResource(id = prod.imageRes),
                                contentDescription = prod.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = {
                                    ImageSaver.saveToGallery(context, prod.imageRes, prod.title)
                                    Toast.makeText(context, "✓ फोटो गैलरी में सेव हो गई!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(prod.catalogInfo, fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Prepaid: ₹${(prod.price - pDisc).coerceAtLeast(100)} (-₹$pDisc)", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                    Text("WDRP: ₹${(prod.price - wDisc).coerceAtLeast(100)} (-₹$wDisc)", fontSize = 11.sp, color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val allImages = listOf(
                                            R.drawable.dress_anarkali_1793417013639,
                                            R.drawable.dress_lehenga_1793417067159,
                                            R.drawable.dress_saree_1793417052582
                                        )
                                        val currIdx = allImages.indexOf(prod.imageRes)
                                        val nextRes = allImages[(currIdx + 1) % allImages.size]
                                        rtoProducts[index] = prod.copy(imageRes = nextRes)
                                        Toast.makeText(context, "✓ प्रोडक्ट इमेज बदली गई!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Image", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Discounts saved: Prepaid ₹$prepaidDiscount, WDRP ₹$wdrpDiscount across all products!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply Discounts Across Catalogs", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// =========================================================================
// SUB-SCREEN 4: CLAIMS SCREEN
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerClaimsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var showRaiseModal by remember { mutableStateOf(false) }
    var claimOrderId by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf("Wrong product received in return") }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("Claims", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Overview: Last 30 Days ▾", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Button(
                        onClick = { showRaiseModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                    ) {
                        Text("+ Raise Claim")
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Raising claims is super-easy on Gyanvi Dresses", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf(
                            "Cut down on losses due to wrong returns",
                            "Transparent resolution process",
                            "Upto 100% Claim Approval Rate*"
                        ).forEach { bullet ->
                            Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(bullet, fontSize = 13.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text("Recently Enhanced Features", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(10.dp))

                listOf(
                    "Transparent Barcoded Packaging" to "Using transparent, barcoded packaging helps reduce wrong returns.",
                    "Extensive Quality Checks" to "Thorough quality checks at every step to eliminate fraudulent activities.",
                    "Secured Ecosystem" to "Strengthened trust and safety measures against suspicious carrier partners."
                ).forEach { (title, desc) ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(desc, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showRaiseModal) {
        AlertDialog(
            onDismissRequest = { showRaiseModal = false },
            title = { Text("Raise Claim Ticket") },
            text = {
                Column {
                    OutlinedTextField(
                        value = claimOrderId,
                        onValueChange = { claimOrderId = it },
                        label = { Text("Order ID / Sub Order ID *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Reason: $selectedReason", fontSize = 12.sp, color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRaiseModal = false
                        Toast.makeText(context, "Claim ticket created for Order $claimOrderId", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                ) {
                    Text("Submit Ticket")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRaiseModal = false }) { Text("Cancel") }
            }
        )
    }
}

// =========================================================================
// SUB-SCREEN 5: RETURNS HELP SCREEN
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnsHelpScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showCustomTicketDialog by remember { mutableStateOf(false) }
    var ticketOrderId by remember { mutableStateOf("") }
    var ticketCategory by remember { mutableStateOf("Wrong product received in return") }
    var ticketDescription by remember { mutableStateOf("") }
    var ticketPhone by remember { mutableStateOf("+91 9128754982") }

    val userTickets = remember {
        mutableStateListOf<Triple<String, String, String>>()
    }

    val issues = listOf(
        "I have received wrong return",
        "I have received damaged return",
        "I have not received my Return/RTO shipment",
        "Item/s are missing in my return",
        "I have received a wrong barcoded package in RTO",
        "I have received used product as return",
        "Return/RTO product not received but marked delivered",
        "I have an issue with Exchange order"
    )

    val filteredIssues = issues.filter {
        searchQuery.isBlank() || it.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text("Returns/RTO & Exchange Help", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search your return issue or question...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        },
        containerColor = CardWhite
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Can't find your issue?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A237E))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Submit a custom support ticket with your order ID and description.", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { showCustomTicketDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Raise Custom Support Ticket (Text Box)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (userTickets.isNotEmpty()) {
                item {
                    Text("YOUR RECENT TICKETS", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                }
                items(userTickets) { (ticketId, category, status) ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Ticket #$ticketId", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(category, fontSize = 11.sp, color = TextSecondary)
                            }
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFFFF3E0)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                Text(status, color = Color(0xFFE65100), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(10.dp)) }
            }

            item {
                Text("SELECT YOUR ISSUE", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
            }

            items(filteredIssues) { issue ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            ticketCategory = issue
                            showCustomTicketDialog = true
                        }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(issue, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
                HorizontalDivider(color = Color(0xFFF1F1F1))
            }
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    if (showCustomTicketDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTicketDialog = false },
            title = { Text("Raise Support Ticket (Text Box)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = ticketOrderId,
                        onValueChange = { ticketOrderId = it },
                        label = { Text("Order ID / Sub Order ID *") },
                        placeholder = { Text("e.g. 192837465") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ticketCategory,
                        onValueChange = { ticketCategory = it },
                        label = { Text("Issue Subject / Category") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ticketDescription,
                        onValueChange = { ticketDescription = it },
                        label = { Text("Describe Your Issue (Text Box) *") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ticketPhone,
                        onValueChange = { ticketPhone = it },
                        label = { Text("Contact Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newId = "TK" + (100000..999999).random()
                        userTickets.add(0, Triple(newId, ticketCategory, "In Review"))
                        showCustomTicketDialog = false
                        ticketOrderId = ""
                        ticketDescription = ""
                        Toast.makeText(context, "Support ticket #$newId submitted! Our team will contact you.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Submit Ticket")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTicketDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// =========================================================================
// SUB-SCREEN 6: IMAGE BULK UPLOAD SCREEN (SINGLE & MULTIPLE UPLOAD)
// =========================================================================
data class BulkUploadItem(
    val id: String,
    val title: String,
    val sku: String,
    val mrp: String,
    val discountPrice: String,
    val color: String,
    val fabric: String,
    val selectedSizes: List<String>,
    val stockQuantity: String,
    val description: String,
    @DrawableRes val drawableRes: Int
)

@Composable
fun SellerImageBulkUploadScreen(onBack: () -> Unit) {
    FullSellerProductUploadScreen(onBack = onBack)
}

// =========================================================================
// SUB-SCREEN 7: QUALITY SCORE SCREEN
// =========================================================================
@Composable
fun SellerQualityScreen(onBack: () -> Unit) {
    SellerQualityFeedbackScreen(onBack = onBack)
}


// =========================================================================
// SUB-SCREEN 8: WAREHOUSE SETTINGS SCREEN
// =========================================================================
@Composable
fun SellerWarehouseScreen(onBack: () -> Unit) {
    SellerWarehouseDispatchScreen(onBack = onBack)
}


// =========================================================================
// SUB-SCREEN 9: INFLUENCER MARKETING SCREEN
// =========================================================================
@Composable
fun SellerInfluencerMarketingScreen(
    onBack: () -> Unit,
    socialAdsConfig: SocialMediaAdsConfig = LocalAppConfig.current.socialAds,
    onToggleSocialAdsEnabled: (Boolean) -> Unit = {},
    onToggleSocialAdsHome: (Boolean) -> Unit = {},
    onSaveSocialAdCampaign: (String?, String, SocialAdPlatform, String, String, String, Int, String, Boolean, Boolean) -> Result<SocialMediaAdCampaign> = { id, title, platform, headline, caption, url, budget, cta, showHome, active ->
        Result.success(SocialMediaAdCampaign(id ?: "ad-seller", title, platform, headline, caption, url, budget, isActive = active, showOnHomeBanner = showHome, ctaLabel = cta))
    },
    onToggleSocialAdActive: (String) -> Unit = {},
    onDeleteSocialAdCampaign: (String) -> Unit = {},
    onRecordSocialAdClick: (String) -> Unit = {}
) {
    FullSellerInfluencerMarketingScreen(
        onBack = onBack,
        socialAdsConfig = socialAdsConfig,
        onToggleSocialAdsEnabled = onToggleSocialAdsEnabled,
        onToggleSocialAdsHome = onToggleSocialAdsHome,
        onSaveSocialAdCampaign = onSaveSocialAdCampaign,
        onToggleSocialAdActive = onToggleSocialAdActive,
        onDeleteSocialAdCampaign = onDeleteSocialAdCampaign,
        onRecordSocialAdClick = onRecordSocialAdClick
    )
}

// =========================================================================
// SUB-SCREEN 10: INSTANT CASH WORKING CAPITAL SCREEN
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerInstantCashScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var loanAmount by remember { mutableStateOf("₹30,000") }
    var selectedTenure by remember { mutableIntStateOf(6) }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("Gyanvi Dresses Instant Cash", fontWeight = FontWeight.Bold, fontSize = 17.sp)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Pre-Approved Working Capital", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("₹50,000", fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Disbursal to: State Bank of India (A/C: •••• 4829)", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                }
            }

            OutlinedTextField(
                value = loanAmount,
                onValueChange = { loanAmount = it },
                label = { Text("Enter Amount to Withdraw (₹) (Text Box)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Repayment Tenure", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3, 6, 12).forEach { months ->
                    val isSel = selectedTenure == months
                    OutlinedButton(
                        onClick = { selectedTenure = months },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSel) Color(0xFFEDE7F6) else Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("$months Months", fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) Color(0xFF3F51B5) else TextPrimary)
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Monthly Interest Rate", fontSize = 12.sp, color = TextSecondary)
                        Text("1.2% per month", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Processing Fee", fontSize = 12.sp, color = TextSecondary)
                        Text("₹0 (Zero Fee)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Estimated Monthly EMI", fontSize = 12.sp, color = TextSecondary)
                        Text("₹5,360 / mo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                    }
                }
            }

            Button(
                onClick = {
                    Toast.makeText(context, "$loanAmount credited to your State Bank of India account instantly!", Toast.LENGTH_LONG).show()
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Disburse Now to Bank", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
