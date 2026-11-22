package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.MainTab
import com.example.model.OrderStatus
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.GyanviDressesTheme
import com.example.ui.theme.YellowBackground
import com.example.util.AppCheckInitializer
import com.example.util.GoogleSignInHelper
import com.example.viewmodel.AppSubScreen
import com.example.viewmodel.GyanviViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCheckInitializer.configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            GyanviDressesTheme {
                GyanviApp()
            }
        }
    }
}

@Composable
fun GyanviApp(
    viewModel: GyanviViewModel = viewModel(
        factory = GyanviViewModel.provideFactory(LocalContext.current.applicationContext)
    )
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val subScreen by viewModel.subScreen.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val wishlistIds by viewModel.wishlistIds.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val autoPendingNotificationEnabled by viewModel.autoPendingNotificationEnabled.collectAsStateWithLifecycle()
    val autoTrackingEnabled by viewModel.autoTrackingEnabled.collectAsStateWithLifecycle()
    val autoRefundTrackingEnabled by viewModel.autoRefundTrackingEnabled.collectAsStateWithLifecycle()
    val firebaseUid by viewModel.firebaseUid.collectAsStateWithLifecycle()
    val firebaseUserEmail by viewModel.firebaseUserEmail.collectAsStateWithLifecycle()
    val cloudSyncState by viewModel.cloudSyncState.collectAsStateWithLifecycle()
    val sellerPreferredBottomTab by viewModel.sellerPreferredBottomTab.collectAsStateWithLifecycle()
    val sellerPreferredOrderTab by viewModel.sellerPreferredOrderTab.collectAsStateWithLifecycle()
    val sellerOrderActions = remember(
        autoPendingNotificationEnabled,
        autoTrackingEnabled,
        autoRefundTrackingEnabled
    ) { viewModel.buildSellerOrderActions() }
    val filteredOrders by viewModel.filteredOrders.collectAsStateWithLifecycle()
    val selectedOrderStatus by viewModel.selectedOrderStatus.collectAsStateWithLifecycle()
    val orderSearchQuery by viewModel.orderSearchQuery.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val selectedProductForShare by viewModel.selectedProductForShare.collectAsStateWithLifecycle()
    val selectedOrderForFeedback by viewModel.selectedOrderForFeedback.collectAsStateWithLifecycle()
    val selectedOrderForTracking by viewModel.selectedOrderForTracking.collectAsStateWithLifecycle()
    val deliveryAddress by viewModel.deliveryAddress.collectAsStateWithLifecycle()
    val videoFinds by viewModel.videoFinds.collectAsStateWithLifecycle()
    val countdownSeconds by viewModel.countdownSeconds.collectAsStateWithLifecycle()

    val feedbackStep by viewModel.feedbackStep.collectAsStateWithLifecycle()
    val feedbackOverallRating by viewModel.feedbackOverallRating.collectAsStateWithLifecycle()
    val feedbackMaterialRating by viewModel.feedbackMaterialRating.collectAsStateWithLifecycle()
    val feedbackComment by viewModel.feedbackComment.collectAsStateWithLifecycle()

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val activeRole by viewModel.activeRole.collectAsStateWithLifecycle()
    val walletBalance by viewModel.walletBalance.collectAsStateWithLifecycle()
    val rewardPoints by viewModel.rewardPoints.collectAsStateWithLifecycle()
    val compareProducts by viewModel.compareProducts.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val notificationPrefs by viewModel.notificationPrefs.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val offlineCatalogEntities by viewModel.offlineCatalogEntities.collectAsStateWithLifecycle()
    val selectedCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val activeWarehouse by viewModel.activeWarehouse.collectAsStateWithLifecycle()
    val isVipSubscribed by viewModel.isVipSubscribed.collectAsStateWithLifecycle()
    val registeredSellersCount by viewModel.registeredSellersCount.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val registeredAccounts by viewModel.registeredAccounts.collectAsStateWithLifecycle()
    val authPreferredType by viewModel.authPreferredType.collectAsStateWithLifecycle()
    val authPreferredRegisterMode by viewModel.authPreferredRegisterMode.collectAsStateWithLifecycle()
    val loginOtpSession by viewModel.loginOtpSession.collectAsStateWithLifecycle()
    val managementConfig by viewModel.managementConfig.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val subCategories by viewModel.subCategories.collectAsStateWithLifecycle()
    val appBackups by viewModel.appBackups.collectAsStateWithLifecycle()
    val lastBackupJsonText by viewModel.lastBackupJsonText.collectAsStateWithLifecycle()
    val aiStylistState by viewModel.aiStylistState.collectAsStateWithLifecycle()
    val aiSearchState by viewModel.aiSearchState.collectAsStateWithLifecycle()
    val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsStateWithLifecycle()
    val buyerSellerMessages by viewModel.buyerSellerMessages.collectAsStateWithLifecycle()
    val staffMembers by viewModel.staffMembers.collectAsStateWithLifecycle()
    val activeStaffMember by viewModel.activeStaffMember.collectAsStateWithLifecycle()
    val staffTasks by viewModel.staffTasks.collectAsStateWithLifecycle()
    val activityHistory by viewModel.activityHistory.collectAsStateWithLifecycle()
    val recentlyViewedIds by viewModel.recentlyViewedIds.collectAsStateWithLifecycle()
    val recentlyViewedProducts = remember(recentlyViewedIds, allProducts) {
        recentlyViewedIds.mapNotNull { id -> allProducts.find { it.id == id } }
    }
    val unreadNotificationsCount = notifications.count { !it.isRead }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val silentUser = GoogleSignInHelper.attemptAutoSignIn(context)
        if (silentUser != null && currentUser == null) {
            viewModel.onGoogleSignInAuthenticated(silentUser)
        }
    }

    val visibleTabs = remember(managementConfig.features) {
        buildList {
            add(MainTab.HOME)
            if (managementConfig.features.showCategoriesTab) add(MainTab.CATEGORIES)
            if (managementConfig.features.showMallTab) add(MainTab.MALL)
            if (managementConfig.features.showVideoFindsTab) add(MainTab.VIDEO_FINDS)
            if (managementConfig.features.showOrdersTab) add(MainTab.MY_ORDERS)
        }
    }

    var activeFilterTab by remember { mutableStateOf(FilterTab.CATEGORY) }
    var wishlistInitialTab by remember { mutableIntStateOf(0) }
    var appManagementInitialTab by remember { mutableIntStateOf(0) }

    // Handle Hardware/Gesture Back Button (only active after user logs in / registers)
    BackHandler(enabled = currentUser != null && (subScreen != AppSubScreen.NONE || currentTab != MainTab.HOME)) {
        if (subScreen != AppSubScreen.NONE) {
            viewModel.closeSubScreen()
        } else if (currentTab != MainTab.HOME) {
            viewModel.switchTab(MainTab.HOME)
        }
    }

    // Main App Shell wrapped with reactive Dark/Light Theme, Dashboard Palette & Live Typography (Likhavat)
    GyanviDressesTheme(
        darkTheme = isDarkMode,
        managementConfig = managementConfig
    ) {
        if (currentUser == null) {
            // Mandatory Front-Page Customer / Seller Mobile-OTP Login & Registration Gate
            UserAuthScreen(
                currentUser = null,
                registeredAccounts = registeredAccounts,
                initialAccountType = authPreferredType,
                initialRegisterMode = authPreferredRegisterMode,
                isMandatoryGate = true,
                loginOtpSession = loginOtpSession,
                onBack = {},
                onRegister = { type, name, mobile, email, pwd, shop, addr, gst, onRes ->
                    viewModel.registerUserAccount(type, name, mobile, email, pwd, shop, addr, gst, onRes)
                },
                onLogin = { identifier, pwd, type, onRes ->
                    viewModel.loginUserAccount(identifier, pwd, type, onRes)
                },
                onGenerateLoginOtp = { mobile -> viewModel.generateMobileLoginOtp(mobile) },
                onLoginWithOtp = { mobile, otp, type, onRes ->
                    viewModel.verifyOtpAndLoginCustomer(mobile, otp, type, onRes)
                },
                onClearLoginOtp = { viewModel.clearMobileLoginOtp() },
                onResetPassword = { identifier, newPwd, type, onRes ->
                    viewModel.resetUserPassword(identifier, newPwd, type, onRes)
                },
                onSwitchAccount = { acc -> viewModel.switchSavedAccount(acc) },
                onDeleteAccount = { id -> viewModel.deleteSavedAccount(id) },
                onLogout = { viewModel.logoutCurrentUser() },
                onGoogleSignInSuccess = { user, acctType ->
                    viewModel.onGoogleSignInAuthenticated(user, acctType)
                }
            )
        } else {
        key(currentUser?.id ?: firebaseUid ?: "guest") {
        Scaffold(
            topBar = {
                if (subScreen == AppSubScreen.NONE && currentTab == MainTab.HOME) {
                    GyanviTopBar(
                        userName = currentUser?.fullName ?: "Guest User",
                        deliveryLocation = deliveryAddress,
                        wishlistCount = wishlistIds.size,
                        cartCount = cartItems.size,
                        notificationCount = unreadNotificationsCount,
                        appLanguage = appLanguage,
                        managementConfig = managementConfig,
                        onSearchClick = { viewModel.openSubScreen(AppSubScreen.SEARCH) },
                        onVoiceClick = { viewModel.openSubScreen(AppSubScreen.VOICE_SEARCH) },
                        onCameraClick = { viewModel.openSubScreen(AppSubScreen.CAMERA_SEARCH) },
                        onWishlistClick = { viewModel.openSubScreen(AppSubScreen.WISHLIST) },
                        onCartClick = { viewModel.openSubScreen(AppSubScreen.CART) },
                        onNotificationsClick = { viewModel.openSubScreen(AppSubScreen.NOTIFICATIONS) },
                        onLocationClick = { viewModel.openSubScreen(AppSubScreen.LOCATION_PICKER) },
                        onAccountClick = { viewModel.openSubScreen(AppSubScreen.ACCOUNT) },
                        onQuickEditClick = {
                            appManagementInitialTab = 0
                            viewModel.openSubScreen(AppSubScreen.APP_MANAGEMENT)
                        },
                        onOpenOrders = { viewModel.switchTab(MainTab.MY_ORDERS) },
                        onOpenSellerPanel = { viewModel.openSubScreen(AppSubScreen.SELLER_PANEL) },
                        onOpenStaffWorkspace = { viewModel.openSubScreen(AppSubScreen.STAFF_WORKSPACE) },
                        onOpenAdminPanel = { viewModel.openSubScreen(AppSubScreen.ADMIN_PANEL) },
                        onOpenSettings = { viewModel.openSubScreen(AppSubScreen.APP_SETTINGS) },
                        onOpenControlPanel = { viewModel.openSubScreen(AppSubScreen.CONTROL_PANEL) }
                    )
                }
            },
            bottomBar = {
                if (subScreen == AppSubScreen.NONE) {
                    GyanviBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { tab -> viewModel.switchTab(tab) },
                        ordersCount = orders.count { !it.isFeedbackCompleted },
                        cartCount = cartItems.size,
                        onOpenSearch = { viewModel.openSubScreen(AppSubScreen.SEARCH) },
                        onOpenCart = { viewModel.openSubScreen(AppSubScreen.CART) },
                        onOpenProfile = { viewModel.openSubScreen(AppSubScreen.ACCOUNT) },
                        onOpenTrackOrder = {
                            val activeOrder = orders.firstOrNull {
                                it.status in setOf(
                                    OrderStatus.ORDERED,
                                    OrderStatus.CONFIRMED,
                                    OrderStatus.PROCESSING,
                                    OrderStatus.READY_TO_SHIP,
                                    OrderStatus.SHIPPED,
                                    OrderStatus.OUT_FOR_DELIVERY
                                )
                            }
                            if (activeOrder != null) {
                                viewModel.startTracking(activeOrder)
                            } else {
                                viewModel.switchTab(MainTab.MY_ORDERS)
                            }
                        },
                        managementConfig = managementConfig
                    )
                }
            },
            containerColor = if (isDarkMode) MaterialTheme.colorScheme.background else managementConfig.themePreset.backgroundColor,
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(if (isDarkMode) MaterialTheme.colorScheme.background else managementConfig.themePreset.backgroundColor)
        ) {
            // Main Tab Screens
            when (currentTab) {
                MainTab.HOME -> {
                    HomeScreen(
                        products = filteredProducts,
                        wishlistIds = wishlistIds,
                        countdownSeconds = countdownSeconds,
                        selectedCategory = filterState.selectedCategory,
                        selectedGender = filterState.selectedGender,
                        selectedSortLabel = filterState.sortOption.label,
                        categories = categories,
                        managementConfig = managementConfig,
                        onCategorySelect = { category ->
                            viewModel.selectCategoryFilter(category)
                        },
                        onProductClick = { product ->
                            viewModel.selectProduct(product)
                        },
                        onWishlistToggle = { id ->
                            viewModel.toggleWishlist(id)
                        },
                        onAddToCart = { product ->
                            viewModel.addToCart(product)
                        },
                        onOpenSort = {
                            viewModel.openSubScreen(AppSubScreen.SORT_SHEET)
                        },
                        onOpenCategoryFilter = {
                            activeFilterTab = FilterTab.CATEGORY
                            viewModel.openSubScreen(AppSubScreen.FILTER_SHEET)
                        },
                        onOpenGenderFilter = {
                            activeFilterTab = FilterTab.GENDER
                            viewModel.openSubScreen(AppSubScreen.FILTER_SHEET)
                        },
                        onOpenFilter = {
                            activeFilterTab = FilterTab.CATEGORY
                            viewModel.openSubScreen(AppSubScreen.FILTER_SHEET)
                        },
                        onShareProduct = { product -> viewModel.openShare(product) },
                        onOpenDevelopmentProgress = { viewModel.openSubScreen(AppSubScreen.DEVELOPMENT_PROGRESS) },
                        onQuickBackupNow = {
                            val created = viewModel.createAppBackup()
                            Toast.makeText(context, "Backup Saved: ${created.label}", Toast.LENGTH_SHORT).show()
                            viewModel.openSubScreen(AppSubScreen.APP_BACKUP)
                        },
                        onOpenBackupManager = { viewModel.openSubScreen(AppSubScreen.APP_BACKUP) },
                        onOpenAppManagement = {
                            appManagementInitialTab = 0
                            viewModel.openSubScreen(AppSubScreen.APP_MANAGEMENT)
                        },
                        onSideNavigateBack = {
                            val idx = visibleTabs.indexOf(currentTab).coerceAtLeast(0)
                            val prevTab = visibleTabs[(idx - 1 + visibleTabs.size) % visibleTabs.size]
                            viewModel.switchTab(prevTab)
                        },
                        onSideNavigateNext = {
                            val idx = visibleTabs.indexOf(currentTab).coerceAtLeast(0)
                            val nextTab = visibleTabs[(idx + 1) % visibleTabs.size]
                            viewModel.switchTab(nextTab)
                        },
                        onOpenSearch = { viewModel.openSubScreen(AppSubScreen.SEARCH) },
                        onOpenCart = { viewModel.openSubScreen(AppSubScreen.CART) },
                        onOpenWishlist = { viewModel.openSubScreen(AppSubScreen.WISHLIST) },
                        onOpenAccount = { viewModel.openSubScreen(AppSubScreen.ACCOUNT) },
                        onToggleDarkMode = { viewModel.toggleDarkMode(!isDarkMode) },
                        onSocialAdClick = { adId -> viewModel.recordSocialAdClick(adId) },
                        recentlyViewedProducts = recentlyViewedProducts
                    )
                }

                MainTab.CATEGORIES -> {
                    CategoriesScreen(
                        wishlistCount = wishlistIds.size,
                        cartCount = cartItems.size,
                        onSearchClick = { viewModel.openSubScreen(AppSubScreen.SEARCH) },
                        onWishlistClick = { viewModel.openSubScreen(AppSubScreen.WISHLIST) },
                        onCartClick = { viewModel.openSubScreen(AppSubScreen.CART) },
                        onSelectSubCategory = { categoryKeyword ->
                            viewModel.selectCategoryFilter(categoryKeyword)
                            viewModel.switchTab(MainTab.HOME)
                        },
                        categories = categories,
                        subCategories = subCategories,
                        products = allProducts,
                        onProductClick = { product -> viewModel.selectProduct(product) }
                    )
                }

                MainTab.MALL -> {
                    MallScreen(
                        products = filteredProducts,
                        wishlistIds = wishlistIds,
                        deliveryLocation = deliveryAddress,
                        wishlistCount = wishlistIds.size,
                        cartCount = cartItems.size,
                        onSearchClick = { viewModel.openSubScreen(AppSubScreen.SEARCH) },
                        onWishlistClick = { viewModel.openSubScreen(AppSubScreen.WISHLIST) },
                        onCartClick = { viewModel.openSubScreen(AppSubScreen.CART) },
                        onProductClick = { product ->
                            viewModel.selectProduct(product)
                        },
                        onWishlistToggle = { id ->
                            viewModel.toggleWishlist(id)
                        },
                        onAddToCart = { product ->
                            viewModel.addToCart(product)
                        }
                    )
                }

                MainTab.VIDEO_FINDS -> {
                    VideoFindsScreen(
                        videoFinds = videoFinds,
                        onToggleLike = { id -> viewModel.toggleLikeVideo(id) },
                        onToggleMute = { id -> viewModel.toggleMuteVideo(id) },
                        onProductClick = { product -> viewModel.selectProduct(product) },
                        onAddToCart = { product -> viewModel.addToCart(product) }
                    )
                }

                MainTab.MY_ORDERS -> {
                    OrdersScreen(
                        orders = filteredOrders,
                        selectedStatus = selectedOrderStatus,
                        orderSearchQuery = orderSearchQuery,
                        onStatusSelected = { status -> viewModel.setOrderStatusFilter(status) },
                        onSearchChange = { q -> viewModel.setOrderSearchQuery(q) },
                        onOpenFilterSheet = {
                            viewModel.openSubScreen(AppSubScreen.ORDER_FILTER_SHEET)
                        },
                        onCompleteFeedback = { order -> viewModel.startFeedback(order) },
                        onTrackOrder = { order -> viewModel.startTracking(order) },
                        onCancelOrder = { orderId, reason -> viewModel.cancelOrder(orderId, reason) },
                        onReturnOrder = { orderId, reason -> viewModel.requestReturnOrder(orderId, reason) }
                    )
                }
            }

            if (subScreen != AppSubScreen.NONE) {
                GyanviCommerceSubScreenHost(
                    viewModel = viewModel,
                    subScreen = subScreen,
                    wishlistInitialTab = wishlistInitialTab,
                    onWishlistInitialTabChange = { wishlistInitialTab = it },
                    appManagementInitialTabChange = { appManagementInitialTab = it }
                )
                GyanviOperationsSubScreenHost(
                    viewModel = viewModel,
                    subScreen = subScreen,
                    appManagementInitialTab = appManagementInitialTab,
                    onAppManagementInitialTabChange = { appManagementInitialTab = it }
                )
                GyanviModalSheetsHost(
                    viewModel = viewModel,
                    subScreen = subScreen,
                    activeFilterTab = activeFilterTab
                )
            }
        }
        }
        }
        }
    }
}

@Composable
private fun GyanviCommerceSubScreenHost(
    viewModel: GyanviViewModel,
    subScreen: AppSubScreen,
    wishlistInitialTab: Int,
    onWishlistInitialTabChange: (Int) -> Unit,
    appManagementInitialTabChange: (Int) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val wishlistIds by viewModel.wishlistIds.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val selectedProductForShare by viewModel.selectedProductForShare.collectAsStateWithLifecycle()
    val selectedOrderForFeedback by viewModel.selectedOrderForFeedback.collectAsStateWithLifecycle()
    val deliveryAddress by viewModel.deliveryAddress.collectAsStateWithLifecycle()
    val feedbackStep by viewModel.feedbackStep.collectAsStateWithLifecycle()
    val feedbackOverallRating by viewModel.feedbackOverallRating.collectAsStateWithLifecycle()
    val feedbackMaterialRating by viewModel.feedbackMaterialRating.collectAsStateWithLifecycle()
    val feedbackComment by viewModel.feedbackComment.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val activeRole by viewModel.activeRole.collectAsStateWithLifecycle()
    val walletBalance by viewModel.walletBalance.collectAsStateWithLifecycle()
    val rewardPoints by viewModel.rewardPoints.collectAsStateWithLifecycle()
    val compareProducts by viewModel.compareProducts.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val notificationPrefs by viewModel.notificationPrefs.collectAsStateWithLifecycle()
    val selectedCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val activeWarehouse by viewModel.activeWarehouse.collectAsStateWithLifecycle()
    val isVipSubscribed by viewModel.isVipSubscribed.collectAsStateWithLifecycle()
    val registeredSellersCount by viewModel.registeredSellersCount.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val registeredAccounts by viewModel.registeredAccounts.collectAsStateWithLifecycle()
    val authPreferredType by viewModel.authPreferredType.collectAsStateWithLifecycle()
    val authPreferredRegisterMode by viewModel.authPreferredRegisterMode.collectAsStateWithLifecycle()
    val loginOtpSession by viewModel.loginOtpSession.collectAsStateWithLifecycle()
    val managementConfig by viewModel.managementConfig.collectAsStateWithLifecycle()
    val aiStylistState by viewModel.aiStylistState.collectAsStateWithLifecycle()
    val aiSearchState by viewModel.aiSearchState.collectAsStateWithLifecycle()
    val buyerSellerMessages by viewModel.buyerSellerMessages.collectAsStateWithLifecycle()
    val unreadNotificationsCount = notifications.count { !it.isRead }

    when (subScreen) {
        AppSubScreen.SEARCH -> {
            SearchScreen(
                searchQuery = searchQuery,
                recentSearches = recentSearches,
                popularSearches = viewModel.popularSearches,
                matchingProducts = filteredProducts,
                wishlistIds = wishlistIds,
                onQueryChange = { q -> viewModel.onSearchQueryChange(q) },
                onSubmitSearch = { q -> viewModel.submitSearch(q) },
                onClearRecent = { viewModel.clearRecentSearches() },
                onProductClick = { product -> viewModel.selectProduct(product) },
                onWishlistToggle = { id -> viewModel.toggleWishlist(id) },
                onAddToCart = { product -> viewModel.addToCart(product) },
                onVoiceClick = { viewModel.openSubScreen(AppSubScreen.VOICE_SEARCH) },
                onCameraClick = { viewModel.openSubScreen(AppSubScreen.CAMERA_SEARCH) },
                onBack = { viewModel.closeSubScreen() },
                allProducts = allProducts,
                aiSearchState = aiSearchState,
                onAiSmartSearch = { q -> viewModel.runAiSmartSearch(q) },
                onClearAiSearch = { viewModel.clearAiSearch() }
            )
        }

        AppSubScreen.CART -> {
            CartScreen(
                cartItems = cartItems,
                deliveryAddress = deliveryAddress,
                walletBalance = walletBalance,
                rewardPoints = rewardPoints,
                onBack = { viewModel.closeSubScreen() },
                onUpdateQuantity = { id, size, qty -> viewModel.updateCartQuantity(id, size, qty) },
                onUpdateSize = { id, oldSize, newSize -> viewModel.updateCartSize(id, oldSize, newSize) },
                onRemoveItem = { id, size -> viewModel.removeFromCart(id, size) },
                onMoveToWishlist = { id, size -> viewModel.moveCartItemToWishlist(id, size) },
                onPlaceOrder = { viewModel.checkoutAndPlaceOrder() },
                onConfirmPayment = { method, discountAmt, walletAmt ->
                    viewModel.checkoutAndPlaceOrder(method, discountAmt, walletAmt)
                },
                onChangeAddress = { viewModel.openSubScreen(AppSubScreen.LOCATION_PICKER) },
                onOpenWishlist = { viewModel.openSubScreen(AppSubScreen.WISHLIST) },
                paymentGatewayConfig = managementConfig.paymentGateways,
                onSaveAndConnectPaymentGateway = { id, name, url, provider, status, connect ->
                    viewModel.saveAndConnectPaymentGateway(id, name, url, provider, status, connect)
                },
                onConnectPaymentGateway = { id -> viewModel.connectPaymentGateway(id) }
            )
        }

        AppSubScreen.WISHLIST -> {
            val allWishlisted = allProducts.filter { wishlistIds.contains(it.id) }
            WishlistScreen(
                initialTab = wishlistInitialTab,
                wishlistProducts = allWishlisted,
                cartCount = cartItems.size,
                onBack = { viewModel.closeSubScreen() },
                onSearchClick = { viewModel.openSubScreen(AppSubScreen.SEARCH) },
                onCartClick = { viewModel.openSubScreen(AppSubScreen.CART) },
                onProductClick = { product -> viewModel.selectProduct(product) },
                onWishlistToggle = { id -> viewModel.toggleWishlist(id) },
                onAddToCart = { product -> viewModel.addToCart(product) },
                onShareClick = { product -> viewModel.openShare(product) }
            )
        }

        AppSubScreen.PRODUCT_DETAIL -> {
            selectedProduct?.let { prod ->
                val similarList = remember(prod.id, allProducts) {
                    allProducts.filter {
                        it.id != prod.id && (it.category.equals(prod.category, true) || it.subcategory.equals(prod.subcategory, true))
                    }.ifEmpty {
                        allProducts.filter { it.id != prod.id }
                    }.take(8)
                }
                ProductDetailModal(
                    product = prod,
                    isWishlisted = wishlistIds.contains(prod.id),
                    onBack = { viewModel.closeSubScreen() },
                    onWishlistToggle = { viewModel.toggleWishlist(prod.id) },
                    onAddToCart = { size -> viewModel.addToCart(prod, size = size) },
                    onBuyNow = { size ->
                        viewModel.addToCart(prod, size = size)
                        viewModel.openSubScreen(AppSubScreen.CART)
                    },
                    onShareClick = { viewModel.openShare(prod) },
                    onCompareClick = {
                        viewModel.toggleCompareProduct(prod)
                        viewModel.openSubScreen(AppSubScreen.PRODUCT_COMPARE)
                    },
                    onOpenAiAssistant = {
                        viewModel.openSubScreen(AppSubScreen.AI_FASHION_ASSISTANT)
                    },
                    similarProducts = similarList,
                    onProductClick = { sim -> viewModel.selectProduct(sim) }
                )
            }
        }

        AppSubScreen.FEEDBACK -> {
            selectedOrderForFeedback?.let { order ->
                FeedbackModal(
                    order = order,
                    currentStep = feedbackStep,
                    overallRating = feedbackOverallRating,
                    materialRating = feedbackMaterialRating,
                    commentText = feedbackComment,
                    onStepChange = { step -> viewModel.feedbackStep.value = step },
                    onOverallRatingChange = { r -> viewModel.feedbackOverallRating.value = r },
                    onMaterialRatingChange = { r -> viewModel.feedbackMaterialRating.value = r },
                    onCommentChange = { c -> viewModel.feedbackComment.value = c },
                    onSubmit = { viewModel.submitFeedback() },
                    onBack = { viewModel.closeSubScreen() }
                )
            }
        }

        AppSubScreen.ACCOUNT -> {
            AccountScreen(
                cartCount = cartItems.size,
                activeRole = activeRole,
                isDarkMode = isDarkMode,
                appLanguage = appLanguage,
                walletBalance = walletBalance,
                rewardPoints = rewardPoints,
                unreadNotificationCount = unreadNotificationsCount,
                wishlistCount = wishlistIds.size,
                ordersCount = orders.size,
                currentUser = currentUser,
                onBack = { viewModel.closeSubScreen() },
                onSearchClick = { viewModel.openSubScreen(AppSubScreen.SEARCH) },
                onCartClick = { viewModel.openSubScreen(AppSubScreen.CART) },
                onOpenWishlist = {
                    onWishlistInitialTabChange(0)
                    viewModel.openSubScreen(AppSubScreen.WISHLIST)
                },
                onOpenShared = {
                    onWishlistInitialTabChange(1)
                    viewModel.openSubScreen(AppSubScreen.WISHLIST)
                },
                onNavigateToHome = {
                    viewModel.switchTab(MainTab.HOME)
                    viewModel.closeSubScreen()
                },
                onOpenCategories = {
                    viewModel.switchTab(MainTab.CATEGORIES)
                    viewModel.closeSubScreen()
                },
                onOpenOrders = {
                    viewModel.setOrderStatusFilter(OrderStatus.ALL)
                    viewModel.switchTab(MainTab.MY_ORDERS)
                    viewModel.closeSubScreen()
                },
                onOpenTrackOrder = {
                    val activeOrder = orders.firstOrNull {
                        it.status in setOf(
                            OrderStatus.ORDERED,
                            OrderStatus.CONFIRMED,
                            OrderStatus.PROCESSING,
                            OrderStatus.READY_TO_SHIP,
                            OrderStatus.SHIPPED,
                            OrderStatus.OUT_FOR_DELIVERY
                        )
                    }
                    if (activeOrder != null) {
                        viewModel.startTracking(activeOrder)
                    } else {
                        viewModel.switchTab(MainTab.MY_ORDERS)
                        viewModel.closeSubScreen()
                    }
                },
                onOpenLocationPicker = { viewModel.openSubScreen(AppSubScreen.LOCATION_PICKER) },
                onOpenReturns = {
                    viewModel.setOrderStatusFilter(OrderStatus.RETURN)
                    viewModel.switchTab(MainTab.MY_ORDERS)
                    viewModel.closeSubScreen()
                },
                onOpenProfile = { viewModel.openSubScreen(AppSubScreen.PROFILE) },
                onOpenSellerPanel = { viewModel.openSubScreen(AppSubScreen.SELLER_PANEL) },
                onOpenMyPayments = { viewModel.openSubScreen(AppSubScreen.MY_PAYMENTS) },
                onOpenFollowedShops = { viewModel.openSubScreen(AppSubScreen.FOLLOWED_SHOPS) },
                onOpenMeeshoBalance = { viewModel.openSubScreen(AppSubScreen.MEESHO_BALANCE) },
                onOpenSettings = { viewModel.openSubScreen(AppSubScreen.APP_SETTINGS) },
                onOpenLegalPolicies = { viewModel.openSubScreen(AppSubScreen.LEGAL_POLICIES) },
                onOpenAdminPanel = { viewModel.openSubScreen(AppSubScreen.ADMIN_PANEL) },
                onOpenDirectMessageOtp = { viewModel.openSubScreen(AppSubScreen.DIRECT_MESSAGE_OTP) },
                onSwitchRole = { role -> viewModel.switchUserRole(role) },
                onToggleDarkMode = { enabled -> viewModel.toggleDarkMode(enabled) },
                onLanguageChange = { lang -> viewModel.setAppLanguage(lang) },
                onOpenNotifications = { viewModel.openSubScreen(AppSubScreen.NOTIFICATIONS) },
                onOpenAiAssistant = { viewModel.openSubScreen(AppSubScreen.AI_FASHION_ASSISTANT) },
                onOpenCompare = { viewModel.openSubScreen(AppSubScreen.PRODUCT_COMPARE) },
                onOpenSecurityCenter = { viewModel.openSubScreen(AppSubScreen.SECURITY_CENTER) },
                onOpenDevelopmentProgress = { viewModel.openSubScreen(AppSubScreen.DEVELOPMENT_PROGRESS) },
                onOpenUserAuth = { type, isReg -> viewModel.openAuthScreen(type, isReg) },
                onOpenAppManagement = { tabIdx ->
                    appManagementInitialTabChange(tabIdx)
                    viewModel.openSubScreen(AppSubScreen.APP_MANAGEMENT)
                },
                onOpenBackup = { viewModel.openSubScreen(AppSubScreen.APP_BACKUP) },
                onOpenControlPanel = { viewModel.openSubScreen(AppSubScreen.CONTROL_PANEL) },
                onOpenStaffWorkspace = { viewModel.openSubScreen(AppSubScreen.STAFF_WORKSPACE) },
                onClaimReferralCode = { code -> viewModel.claimReferralCode(code) },
                onLogout = { viewModel.logoutCurrentUser() }
            )
        }

        AppSubScreen.PROFILE -> {
            ProfileScreen(
                initialTab = 0,
                currentUser = currentUser,
                onOpenUserAuth = { type, isReg -> viewModel.openAuthScreen(type, isReg) },
                onBack = { viewModel.closeSubScreen() }
            )
        }

        AppSubScreen.MY_PAYMENTS -> {
            MyPaymentsScreen(
                onBack = { viewModel.closeSubScreen() },
                paymentGatewayConfig = managementConfig.paymentGateways,
                onSaveAndConnectPaymentGateway = { id, name, url, provider, status, connect ->
                    viewModel.saveAndConnectPaymentGateway(id, name, url, provider, status, connect)
                },
                onConnectPaymentGateway = { id -> viewModel.connectPaymentGateway(id) },
                onDisconnectPaymentGateway = { viewModel.disconnectPaymentGateway() },
                onDeletePaymentGateway = { id -> viewModel.deletePaymentGateway(id) },
                onToggleAppendPaymentParams = { enabled -> viewModel.toggleAppendPaymentParams(enabled) }
            )
        }

        AppSubScreen.FOLLOWED_SHOPS -> FollowedShopsScreen(onBack = { viewModel.closeSubScreen() })
        AppSubScreen.MEESHO_BALANCE -> MeeshoBalanceScreen(
            onBack = { viewModel.closeSubScreen() },
            onNavigateToTerms = { viewModel.openSubScreen(AppSubScreen.BALANCE_TERMS) },
            onNavigateToFaqs = { viewModel.openSubScreen(AppSubScreen.BALANCE_FAQS) }
        )
        AppSubScreen.BALANCE_TERMS -> BalanceTermsScreen(onBack = { viewModel.closeSubScreen() })
        AppSubScreen.BALANCE_FAQS -> BalanceFaqsScreen(onBack = { viewModel.closeSubScreen() })

        AppSubScreen.APP_SETTINGS -> {
            AppSettingsScreen(
                onBack = { viewModel.closeSubScreen() },
                isDarkMode = isDarkMode,
                onToggleDarkMode = { enabled -> viewModel.toggleDarkMode(enabled) },
                appLanguage = appLanguage,
                onLanguageChange = { lang -> viewModel.setAppLanguage(lang) },
                onOpenNotificationSettings = { viewModel.openSubScreen(AppSubScreen.NOTIFICATIONS) }
            )
        }

        AppSubScreen.LEGAL_POLICIES -> LegalPoliciesScreen(onBack = { viewModel.closeSubScreen() })

        AppSubScreen.DIRECT_MESSAGE_OTP -> {
            DirectMessageOtpModal(
                initialProduct = selectedProductForShare,
                onBack = { viewModel.closeSubScreen() }
            )
        }

        AppSubScreen.NOTIFICATIONS -> {
            NotificationCenterScreen(
                notifications = notifications,
                prefs = notificationPrefs,
                onBack = { viewModel.closeSubScreen() },
                onMarkRead = { id -> viewModel.markNotificationRead(id) },
                onMarkAllRead = { viewModel.markAllNotificationsRead() },
                onUpdatePrefs = { updater -> viewModel.updateNotificationPrefs(updater) },
                onSendTestAlert = {
                    viewModel.pushNotification(
                        title = "Test Alert: Festive Flash Deal ⚡",
                        message = "Notification channel test successful on Gyanvi Dresses Marketplace.",
                        category = "Offers"
                    )
                }
            )
        }

        AppSubScreen.PRODUCT_COMPARE -> {
            ProductComparisonScreen(
                compareList = compareProducts,
                allProducts = allProducts,
                onBack = { viewModel.closeSubScreen() },
                onToggleProduct = { prod -> viewModel.toggleCompareProduct(prod) },
                onAddToCart = { prod -> viewModel.addToCart(prod) },
                onClear = { viewModel.clearCompareProducts() }
            )
        }

        AppSubScreen.AI_FASHION_ASSISTANT -> {
            AiFashionAssistantScreen(
                allProducts = allProducts,
                onBack = { viewModel.closeSubScreen() },
                onProductClick = { prod -> viewModel.selectProduct(prod) },
                onAddToCart = { prod -> viewModel.addToCart(prod) },
                aiStylistState = aiStylistState,
                onRunAiRecommend = { q, occ, budget -> viewModel.runAiDressRecommendations(q, occ, budget) },
                chatMessages = buyerSellerMessages,
                onSendChatMessage = { msg -> viewModel.sendBuyerSellerMessage(msg, selectedProduct?.name ?: "") }
            )
        }

        AppSubScreen.SECURITY_CENTER -> {
            SecurityAuthCenterScreen(
                onBack = { viewModel.closeSubScreen() },
                onOpenOtpDispatcher = { viewModel.openSubScreen(AppSubScreen.DIRECT_MESSAGE_OTP) }
            )
        }

        AppSubScreen.DEVELOPMENT_PROGRESS -> {
            MarketplaceProgressSetupScreen(
                activeRole = activeRole,
                isDarkMode = isDarkMode,
                appLanguage = appLanguage,
                selectedCurrency = selectedCurrency,
                activeWarehouse = activeWarehouse,
                isVipSubscribed = isVipSubscribed,
                registeredSellersCount = registeredSellersCount,
                onBack = { viewModel.closeSubScreen() },
                onSwitchRole = { role -> viewModel.switchUserRole(role) },
                onNavigateSubScreen = { screen -> viewModel.openSubScreen(screen) },
                onOpenHome = { viewModel.switchTab(MainTab.HOME) },
                onOpenLiveVideoShopping = { viewModel.switchTab(MainTab.VIDEO_FINDS) },
                onToggleDarkMode = { enabled -> viewModel.toggleDarkMode(enabled) },
                onLanguageChange = { lang -> viewModel.setAppLanguage(lang) },
                onCurrencyChange = { curr -> viewModel.setCurrency(curr) },
                onWarehouseChange = { wh -> viewModel.setWarehouse(wh) },
                onToggleVip = { enabled -> viewModel.toggleVipSubscription(enabled) },
                onRegisterNewSeller = { shop, owner, gst -> viewModel.registerNewSeller(shop, owner, gst) }
            )
        }

        AppSubScreen.USER_AUTH -> {
            UserAuthScreen(
                currentUser = currentUser,
                registeredAccounts = registeredAccounts,
                initialAccountType = authPreferredType,
                initialRegisterMode = authPreferredRegisterMode,
                loginOtpSession = loginOtpSession,
                onBack = { viewModel.closeSubScreen() },
                onRegister = { type, name, mobile, email, pwd, shop, addr, gst, onRes ->
                    viewModel.registerUserAccount(type, name, mobile, email, pwd, shop, addr, gst, onRes)
                },
                onLogin = { identifier, pwd, type, onRes ->
                    viewModel.loginUserAccount(identifier, pwd, type, onRes)
                },
                onGenerateLoginOtp = { mobile -> viewModel.generateMobileLoginOtp(mobile) },
                onLoginWithOtp = { mobile, otp, type, onRes ->
                    viewModel.verifyOtpAndLoginCustomer(mobile, otp, type, onRes)
                },
                onClearLoginOtp = { viewModel.clearMobileLoginOtp() },
                onResetPassword = { identifier, newPwd, type, onRes ->
                    viewModel.resetUserPassword(identifier, newPwd, type, onRes)
                },
                onSwitchAccount = { acc -> viewModel.switchSavedAccount(acc) },
                onDeleteAccount = { id -> viewModel.deleteSavedAccount(id) },
                onLogout = { viewModel.logoutCurrentUser() },
                onGoogleSignInSuccess = { user, acctType ->
                    viewModel.onGoogleSignInAuthenticated(user, acctType)
                }
            )
        }

        else -> Unit
    }
}

@Composable
private fun GyanviOperationsSubScreenHost(
    viewModel: GyanviViewModel,
    subScreen: AppSubScreen,
    appManagementInitialTab: Int,
    onAppManagementInitialTabChange: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val offlineCatalogEntities by viewModel.offlineCatalogEntities.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val subCategories by viewModel.subCategories.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val wishlistIds by viewModel.wishlistIds.collectAsStateWithLifecycle()
    val managementConfig by viewModel.managementConfig.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val selectedCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val activeWarehouse by viewModel.activeWarehouse.collectAsStateWithLifecycle()
    val isVipSubscribed by viewModel.isVipSubscribed.collectAsStateWithLifecycle()
    val activeRole by viewModel.activeRole.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val registeredAccounts by viewModel.registeredAccounts.collectAsStateWithLifecycle()
    val registeredSellersCount by viewModel.registeredSellersCount.collectAsStateWithLifecycle()
    val walletBalance by viewModel.walletBalance.collectAsStateWithLifecycle()
    val rewardPoints by viewModel.rewardPoints.collectAsStateWithLifecycle()
    val autoTrackingEnabled by viewModel.autoTrackingEnabled.collectAsStateWithLifecycle()
    val autoPendingNotificationEnabled by viewModel.autoPendingNotificationEnabled.collectAsStateWithLifecycle()
    val autoRefundTrackingEnabled by viewModel.autoRefundTrackingEnabled.collectAsStateWithLifecycle()
    val notificationPrefs by viewModel.notificationPrefs.collectAsStateWithLifecycle()
    val cloudSyncState by viewModel.cloudSyncState.collectAsStateWithLifecycle()
    val firebaseUserEmail by viewModel.firebaseUserEmail.collectAsStateWithLifecycle()
    val sellerPreferredBottomTab by viewModel.sellerPreferredBottomTab.collectAsStateWithLifecycle()
    val sellerPreferredOrderTab by viewModel.sellerPreferredOrderTab.collectAsStateWithLifecycle()
    val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsStateWithLifecycle()
    val appBackups by viewModel.appBackups.collectAsStateWithLifecycle()
    val lastBackupJsonText by viewModel.lastBackupJsonText.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val buyerSellerMessages by viewModel.buyerSellerMessages.collectAsStateWithLifecycle()
    val staffMembers by viewModel.staffMembers.collectAsStateWithLifecycle()
    val activeStaffMember by viewModel.activeStaffMember.collectAsStateWithLifecycle()
    val staffTasks by viewModel.staffTasks.collectAsStateWithLifecycle()
    val activityHistory by viewModel.activityHistory.collectAsStateWithLifecycle()

    val sellerOrderActions = remember(
        autoPendingNotificationEnabled,
        autoTrackingEnabled,
        autoRefundTrackingEnabled
    ) { viewModel.buildSellerOrderActions() }

    when (subScreen) {
        AppSubScreen.SELLER_PANEL -> {
            SellerHubScreen(
                onBack = { viewModel.closeSubScreen() },
                onOpenAdminPanel = { viewModel.openSubScreen(AppSubScreen.ADMIN_PANEL) },
                orders = orders,
                products = allProducts,
                sellerOrderActions = sellerOrderActions,
                initialBottomTab = sellerPreferredBottomTab,
                initialOrderTab = sellerPreferredOrderTab,
                roomProducts = offlineCatalogEntities,
                onSaveRoomProduct = { viewModel.saveOfflineProduct(it) },
                onUpdateRoomStock = { id, stock -> viewModel.updateOfflineProductStock(id, stock) },
                onTogglePauseRoomProduct = { id, paused -> viewModel.toggleOfflineProductPaused(id, paused) },
                onDeleteRoomProduct = { id -> viewModel.deleteOfflineProduct(id) },
                onReadyToShip = { viewModel.markSellerOrderReadyToShip(it) },
                onDispatchOrder = { viewModel.dispatchSellerOrder(it) },
                onCancelOrder = { viewModel.cancelSellerOrder(it) },
                onMarkLabelDownloaded = { viewModel.markOrderLabelDownloaded(it) },
                onAddManualOrder = { cust, addr, prod, size, qty, pay ->
                    viewModel.createSellerManualOrder(cust, addr, prod, size, qty, pay)
                },
                cloudSyncState = cloudSyncState,
                firebaseUserEmail = firebaseUserEmail,
                onGoogleCloudSignInClick = {
                    coroutineScope.launch {
                        GoogleSignInHelper.signInWithGoogleExplicit(context)
                            .onSuccess { user ->
                                viewModel.onGoogleSignInAuthenticated(user)
                                Toast.makeText(context, "☁️ Firestore Synced: ${user.email}", Toast.LENGTH_SHORT).show()
                            }
                            .onFailure { err ->
                                Toast.makeText(context, err.localizedMessage ?: "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                onSyncAllOrdersClick = {
                    viewModel.syncAllOrdersToCloud()
                    Toast.makeText(context, "☁️ Syncing all orders to Firestore...", Toast.LENGTH_SHORT).show()
                },
                availableCategories = categories.filter { it.isActive }.map { it.name },
                availableSubCategories = subCategories.filter { it.isActive }.map { it.name },
                categories = categories,
                subCategories = subCategories,
                onPublishCatalogProduct = { name, cat, sub, brand, desc, price, mrp, color, fabric, sizes, numSizes, stock, sku, weight, imgRes, customUri, extraUrls ->
                    viewModel.addCatalogProduct(
                        name = name,
                        category = cat,
                        price = price,
                        originalPrice = mrp,
                        fabric = fabric,
                        description = desc,
                        imageRes = imgRes,
                        customImageUri = customUri,
                        subcategory = sub,
                        brandName = brand,
                        color = color,
                        weight = weight,
                        sizes = sizes,
                        numericSizes = numSizes,
                        stock = stock,
                        sku = sku,
                        extraImageUrls = extraUrls
                    )
                },
                paymentGatewayConfig = managementConfig.paymentGateways,
                onSaveAndConnectPaymentGateway = { id, name, url, provider, status, connect ->
                    viewModel.saveAndConnectPaymentGateway(id, name, url, provider, status, connect)
                },
                onConnectPaymentGateway = { id -> viewModel.connectPaymentGateway(id) },
                onDisconnectPaymentGateway = { viewModel.disconnectPaymentGateway() },
                onDeletePaymentGateway = { id -> viewModel.deletePaymentGateway(id) },
                onToggleAppendPaymentParams = { enabled -> viewModel.toggleAppendPaymentParams(enabled) },
                socialAdsConfig = managementConfig.socialAds,
                onToggleSocialAdsEnabled = { enabled -> viewModel.toggleSocialAdsEnabled(enabled) },
                onToggleSocialAdsHome = { show -> viewModel.toggleSocialAdsOnHome(show) },
                onSaveSocialAdCampaign = { id, title, platform, headline, caption, url, budget, cta, showHome, active ->
                    viewModel.saveSocialAdCampaign(id, title, platform, headline, caption, url, budget, cta, showHome, active)
                },
                onToggleSocialAdActive = { id -> viewModel.toggleSocialAdActive(id) },
                onDeleteSocialAdCampaign = { id -> viewModel.deleteSocialAdCampaign(id) },
                onRecordSocialAdClick = { id -> viewModel.recordSocialAdClick(id) }
            )
        }

        AppSubScreen.ADMIN_PANEL -> {
            AdminPanelScreen(
                onBack = { viewModel.closeSubScreen() },
                onOpenSellerPanel = { viewModel.openSubScreen(AppSubScreen.SELLER_PANEL) },
                onOpenAppManagement = { tabIdx ->
                    onAppManagementInitialTabChange(tabIdx)
                    viewModel.openSubScreen(AppSubScreen.APP_MANAGEMENT)
                },
                onOpenBackup = { viewModel.openSubScreen(AppSubScreen.APP_BACKUP) },
                onOpenControlPanel = { viewModel.openSubScreen(AppSubScreen.CONTROL_PANEL) },
                onPublishBroadcast = { title, message ->
                    viewModel.pushNotification(title, message, "Offers")
                },
                liveOrders = orders,
                autoTrackingEnabled = autoTrackingEnabled,
                onToggleAutoTracking = { viewModel.toggleAutoTracking(it) },
                onAdvanceTracking = { viewModel.advanceShipmentTracking(it) },
                onToggleDelayAlert = { id, delayed, note -> viewModel.toggleDeliveryDelayAlert(id, delayed, note) },
                onMarkRto = { id, reason -> viewModel.markOrderRto(id, reason) },
                onSyncLiveTracking = { viewModel.syncLiveCourierTracking() },
                categories = categories,
                subCategories = subCategories,
                products = allProducts,
                onAddMainCategory = { name, badge, seo, customId, sortOrder, active, imgRes, uri ->
                    viewModel.addMainCategory(
                        customId = customId,
                        name = name,
                        offerBadge = badge,
                        seoName = seo,
                        isActive = active,
                        sortOrder = sortOrder,
                        imageRes = imgRes,
                        customImageUri = uri
                    )
                },
                onEditMainCategory = { id, name, badge, seo, sortOrder, active, imgRes, uri ->
                    viewModel.editMainCategory(
                        originalId = id,
                        newName = name,
                        offerBadge = badge,
                        seoName = seo,
                        isActive = active,
                        sortOrder = sortOrder,
                        imageRes = imgRes,
                        customImageUri = uri
                    )
                },
                onToggleCategoryActive = { id, active -> viewModel.toggleCategoryActive(id, active) },
                onMoveCategorySortOrder = { id, delta -> viewModel.moveCategorySortOrder(id, delta) },
                onDeleteCategory = { id -> viewModel.deleteCategory(id) },
                onAddSubCategory = { parent, name, seo, customId, sortOrder, active, imgRes, uri ->
                    val parentId = categories.find { it.name.equals(parent, ignoreCase = true) }?.id ?: ""
                    viewModel.addSubCategory(
                        customId = customId,
                        parentCategoryId = parentId,
                        parentCategoryName = parent,
                        name = name,
                        seoName = seo,
                        isActive = active,
                        sortOrder = sortOrder,
                        imageRes = imgRes,
                        customImageUri = uri
                    )
                },
                onEditSubCategory = { id, parent, name, seo, sortOrder, active, imgRes, uri ->
                    val parentId = categories.find { it.name.equals(parent, ignoreCase = true) }?.id ?: ""
                    viewModel.editSubCategory(
                        originalId = id,
                        parentCategoryId = parentId,
                        parentCategoryName = parent,
                        newName = name,
                        seoName = seo,
                        isActive = active,
                        sortOrder = sortOrder,
                        imageRes = imgRes,
                        customImageUri = uri
                    )
                },
                onToggleSubCategoryActive = { id, active -> viewModel.toggleSubCategoryActive(id, active) },
                onDeleteSubCategory = { id -> viewModel.deleteSubCategory(id) },
                onUpdateProductCategories = { id, primary, sub, extras ->
                    viewModel.updateProductCategories(id, primary, sub, extras)
                },
                onAddCatalogProduct = { name, cat, sub, price, mrp, fabric, desc, extras ->
                    viewModel.addCatalogProduct(
                        name = name,
                        category = cat,
                        price = price,
                        originalPrice = mrp,
                        fabric = fabric,
                        description = desc,
                        imageRes = com.example.R.drawable.dress_anarkali_1793417013639,
                        subcategory = sub,
                        additionalCategories = extras
                    )
                },
                onDeleteCatalogProduct = { id -> viewModel.deleteCatalogProduct(id) },
                onResetCategories = { viewModel.resetCategoriesToDefault() },
                paymentGatewayConfig = managementConfig.paymentGateways,
                onSaveAndConnectPaymentGateway = { id, name, url, provider, status, connect ->
                    viewModel.saveAndConnectPaymentGateway(id, name, url, provider, status, connect)
                },
                onConnectPaymentGateway = { id -> viewModel.connectPaymentGateway(id) },
                onDisconnectPaymentGateway = { viewModel.disconnectPaymentGateway() },
                onDeletePaymentGateway = { id -> viewModel.deletePaymentGateway(id) },
                onToggleAppendPaymentParams = { enabled -> viewModel.toggleAppendPaymentParams(enabled) },
                socialAdsConfig = managementConfig.socialAds,
                onToggleSocialAdsEnabled = { enabled -> viewModel.toggleSocialAdsEnabled(enabled) },
                onToggleSocialAdsHome = { show -> viewModel.toggleSocialAdsOnHome(show) },
                onSaveSocialAdCampaign = { id, title, platform, headline, caption, url, budget, cta, showHome, active ->
                    viewModel.saveSocialAdCampaign(id, title, platform, headline, caption, url, budget, cta, showHome, active)
                },
                onToggleSocialAdActive = { id -> viewModel.toggleSocialAdActive(id) },
                onDeleteSocialAdCampaign = { id -> viewModel.deleteSocialAdCampaign(id) },
                onRecordSocialAdClick = { id -> viewModel.recordSocialAdClick(id) },
                isAdminAuthenticated = isAdminAuthenticated,
                onAuthenticateAdmin = { email, pin -> viewModel.authenticateAdmin(email, pin) },
                onLogoutAdmin = { viewModel.logoutAdmin() },
                registeredUsersCount = registeredAccounts.size,
                onUpdateOrderStatusAdmin = { id, status -> viewModel.updateOrderStatusAdmin(id, status) },
                onScheduleReturnPickup = { id -> viewModel.scheduleReturnPickup(id) },
                onUpdateOrderShippingCharges = { id, ch -> viewModel.updateOrderShippingCharges(id, ch) },
                websiteConfig = managementConfig.websiteIntegration,
                onUpdateWebsiteConfig = { transform -> viewModel.updateWebsiteIntegrationConfig(transform) },
                commissionConfig = managementConfig.commissions,
                onUpdateCommissionConfig = { transform -> viewModel.updateCommissionConfig(transform) },
                onTriggerAbandonedCartAlert = { viewModel.triggerAbandonedCartAlert() },
                staffMembers = staffMembers,
                staffTasks = staffTasks,
                activityHistory = activityHistory,
                onAddStaffMember = { name, phone, dept, shift, pin, sal ->
                    viewModel.addStaffMember(name, phone, dept, shift, pin, sal)
                },
                onToggleStaffActive = { id -> viewModel.toggleStaffActive(id) },
                onPayStaffSalary = { id -> viewModel.payStaffSalary(id) },
                onCheckInOutStaff = { id, status -> viewModel.checkInOutStaff(id, status) },
                onUpdateStaffPermissions = { id, pack, deliv, stock, supp, gst, mgr ->
                    viewModel.updateStaffPermissions(id, pack, deliv, stock, supp, gst, mgr)
                },
                onAssignTaskToStaff = { title, desc, dept, staffId, priority ->
                    viewModel.assignTaskToStaff(title, desc, dept, staffId, priority)
                },
                onOpenStaffWorkspace = { viewModel.openSubScreen(AppSubScreen.STAFF_WORKSPACE) }
            )
        }

        AppSubScreen.APP_MANAGEMENT -> {
            AppManagementScreen(
                config = managementConfig,
                isDarkMode = isDarkMode,
                products = allProducts,
                categories = categories,
                initialTab = appManagementInitialTab,
                onBack = { viewModel.closeSubScreen() },
                onToggleDarkMode = { enabled -> viewModel.toggleDarkMode(enabled) },
                onSelectThemePreset = { preset -> viewModel.setDashboardThemePreset(preset) },
                onSelectLayoutMode = { mode -> viewModel.setHomeLayoutMode(mode) },
                onSelectCornerPreset = { corner -> viewModel.setCardCornerPreset(corner) },
                onUpdateTexts = { tx -> viewModel.updateAppTexts(tx) },
                onUpdateFeatures = { fx -> viewModel.updateAppFeatures(fx) },
                onUpdateImages = { im -> viewModel.updateAppImages(im) },
                onUpdateTypography = { ty -> viewModel.updateAppTypography(ty) },
                onEditProduct = { id, name, cat, price, mrp, fabric, desc, stock, maha, mall, img, uri ->
                    viewModel.editCatalogProduct(id, name, cat, price, mrp, fabric, desc, stock, maha, mall, img, uri)
                },
                onAddProduct = { name, cat, price, mrp, fabric, desc, img, uri ->
                    viewModel.addCatalogProduct(name, cat, price, mrp, fabric, desc, img, uri)
                },
                onDeleteProduct = { id -> viewModel.deleteCatalogProduct(id) },
                onResetProducts = { viewModel.resetCatalogProductsToDefault() },
                onUpdateCategory = { id, name, badge, img, uri ->
                    viewModel.updateCategory(id, name, badge, img, uri)
                },
                onAddCategory = { name, badge, img, uri ->
                    viewModel.addCategory(name, badge, img, uri)
                },
                onDeleteCategory = { id -> viewModel.deleteCategory(id) },
                onResetCategories = { viewModel.resetCategoriesToDefault() },
                onResetAll = { viewModel.resetAllAppManagementToDefaults() },
                onOpenBackup = { viewModel.openSubScreen(AppSubScreen.APP_BACKUP) }
            )
        }

        AppSubScreen.APP_BACKUP -> {
            AppBackupScreen(
                backups = appBackups,
                lastBackupJsonText = lastBackupJsonText,
                managementConfig = managementConfig,
                onBack = { viewModel.closeSubScreen() },
                onCreateBackup = { label -> viewModel.createAppBackup(label) },
                onRestoreBackup = { backupId -> viewModel.restoreAppBackup(backupId) },
                onDeleteBackup = { backupId -> viewModel.deleteAppBackup(backupId) },
                onOpenAppManagement = {
                    onAppManagementInitialTabChange(0)
                    viewModel.openSubScreen(AppSubScreen.APP_MANAGEMENT)
                }
            )
        }

        AppSubScreen.CONTROL_PANEL -> {
            MasterControlPanelScreen(
                managementConfig = managementConfig,
                isDarkMode = isDarkMode,
                appLanguage = appLanguage,
                selectedCurrency = selectedCurrency,
                activeWarehouse = activeWarehouse,
                isVipSubscribed = isVipSubscribed,
                activeRole = activeRole,
                currentUser = currentUser,
                registeredAccountsCount = registeredAccounts.size,
                registeredSellersCount = registeredSellersCount,
                products = allProducts,
                roomProducts = offlineCatalogEntities,
                categories = categories,
                orders = orders,
                cartCount = cartItems.size,
                wishlistCount = wishlistIds.size,
                walletBalance = walletBalance,
                rewardPoints = rewardPoints,
                autoTrackingEnabled = autoTrackingEnabled,
                notificationPrefs = notificationPrefs,
                cloudSyncState = cloudSyncState,
                onBack = { viewModel.closeSubScreen() },
                onSwitchRole = { role -> viewModel.switchUserRole(role) },
                onToggleDarkMode = { enabled -> viewModel.toggleDarkMode(enabled) },
                onLanguageChange = { lang -> viewModel.setAppLanguage(lang) },
                onCurrencyChange = { curr -> viewModel.setCurrency(curr) },
                onWarehouseChange = { wh -> viewModel.setWarehouse(wh) },
                onToggleVip = { enabled -> viewModel.toggleVipSubscription(enabled) },
                onSelectThemePreset = { preset -> viewModel.setDashboardThemePreset(preset) },
                onSelectLayoutMode = { mode -> viewModel.setHomeLayoutMode(mode) },
                onSelectCornerPreset = { corner -> viewModel.setCardCornerPreset(corner) },
                onUpdateTexts = { tx -> viewModel.updateAppTexts(tx) },
                onUpdateFeatures = { fx -> viewModel.updateAppFeatures(fx) },
                onOpenAppManagement = { tabIdx ->
                    onAppManagementInitialTabChange(tabIdx)
                    viewModel.openSubScreen(AppSubScreen.APP_MANAGEMENT)
                },
                onOpenBuyerHub = { viewModel.openSubScreen(AppSubScreen.ACCOUNT) },
                onOpenSellerHub = { viewModel.openSubScreen(AppSubScreen.SELLER_PANEL) },
                onOpenStaffWorkspace = { viewModel.openSubScreen(AppSubScreen.STAFF_WORKSPACE) },
                onOpenAdminPanel = { viewModel.openSubScreen(AppSubScreen.ADMIN_PANEL) },
                onOpenUserAuth = { viewModel.openAuthScreen("CUSTOMER", false) },
                onOpenSecurityCenter = { viewModel.openSubScreen(AppSubScreen.SECURITY_CENTER) },
                onOpenBackup = { viewModel.openSubScreen(AppSubScreen.APP_BACKUP) },
                onCreateBackup = { label -> viewModel.createAppBackup(label) },
                onSyncCloudOrders = { viewModel.syncAllOrdersToCloud() },
                onToggleAutoTracking = { enabled -> viewModel.toggleAutoTracking(enabled) },
                onSyncLiveTracking = { viewModel.syncLiveCourierTracking() },
                onMarkOrderReadyToShip = { id -> viewModel.markSellerOrderReadyToShip(id) },
                onDispatchOrder = { id -> viewModel.dispatchSellerOrder(id) },
                onAdvanceTracking = { id -> viewModel.advanceShipmentTracking(id) },
                onCancelOrder = { id -> viewModel.cancelSellerOrder(id) },
                onToggleCategoryActive = { id, active -> viewModel.toggleCategoryActive(id, active) },
                onUpdateStock = { id, stock -> viewModel.updateOfflineProductStock(id, stock) },
                onTogglePauseProduct = { id, paused -> viewModel.toggleOfflineProductPaused(id, paused) },
                onDeleteProduct = { id -> viewModel.deleteCatalogProduct(id) },
                onAddQuickProduct = { name, cat, price, mrp, stock ->
                    viewModel.addCatalogProduct(
                        name = name,
                        category = cat,
                        price = price,
                        originalPrice = mrp,
                        fabric = "Cotton Blend",
                        description = "Added via Master Control Panel",
                        imageRes = com.example.R.drawable.dress_anarkali_1793417013639,
                        stock = stock
                    )
                },
                onPushBroadcast = { title, msg, cat -> viewModel.pushNotification(title, msg, cat) },
                onUpdateNotificationPrefs = { updater -> viewModel.updateNotificationPrefs(updater) },
                onSaveAndConnectPaymentGateway = { id, name, url, provider, status, connect ->
                    viewModel.saveAndConnectPaymentGateway(id, name, url, provider, status, connect)
                },
                onConnectPaymentGateway = { id -> viewModel.connectPaymentGateway(id) },
                onDisconnectPaymentGateway = { viewModel.disconnectPaymentGateway() },
                onDeletePaymentGateway = { id -> viewModel.deletePaymentGateway(id) },
                onToggleAppendPaymentParams = { enabled -> viewModel.toggleAppendPaymentParams(enabled) },
                onToggleSocialAdsEnabled = { enabled -> viewModel.toggleSocialAdsEnabled(enabled) },
                onToggleSocialAdsHome = { show -> viewModel.toggleSocialAdsOnHome(show) },
                onSaveSocialAdCampaign = { id, title, platform, headline, caption, url, budget, cta, showHome, active ->
                    viewModel.saveSocialAdCampaign(id, title, platform, headline, caption, url, budget, cta, showHome, active)
                },
                onToggleSocialAdActive = { id -> viewModel.toggleSocialAdActive(id) },
                onDeleteSocialAdCampaign = { id -> viewModel.deleteSocialAdCampaign(id) },
                onRecordSocialAdClick = { id -> viewModel.recordSocialAdClick(id) },
                onResetAllSettings = { viewModel.resetAllAppManagementToDefaults() }
            )
        }

        AppSubScreen.STAFF_WORKSPACE -> {
            StaffWorkspaceScreen(
                staffMembers = staffMembers,
                activeStaff = activeStaffMember,
                staffTasks = staffTasks,
                orders = orders,
                products = allProducts,
                chatMessages = buyerSellerMessages,
                activityHistory = activityHistory,
                onBack = { viewModel.closeSubScreen() },
                onSelectStaff = { id -> viewModel.selectActiveStaffMember(id) },
                onVerifyStaffPin = { code, pin -> viewModel.verifyAndSwitchStaff(code, pin) },
                onCheckInOutStaff = { id, st -> viewModel.checkInOutStaff(id, st) },
                onAssignTaskFromManager = { title, desc, dept, staffId, pri ->
                    viewModel.assignTaskToStaff(title, desc, dept, staffId, pri)
                },
                onUpdateTaskStatus = { id, st -> viewModel.updateStaffTaskStatus(id, st) },
                onUpdateOrderStatus = { id, st -> viewModel.updateOrderStatusAdmin(id, st) },
                onAdvanceTracking = { id -> viewModel.advanceShipmentTracking(id) },
                onScheduleReturnPickup = { id -> viewModel.scheduleReturnPickup(id) },
                onUpdateStockQuick = { id, delta -> viewModel.updateProductStockQuick(id, delta) },
                onToggleStockPause = { id -> viewModel.toggleProductStockPause(id) },
                onSendSupportChat = { text, name ->
                    viewModel.sendBuyerSellerMessage(
                        text = text,
                        productName = selectedProduct?.name ?: "",
                        customSenderName = name,
                        customSenderRole = "STAFF"
                    )
                },
                onOpenOwnerPanel = { viewModel.openSubScreen(AppSubScreen.ADMIN_PANEL) },
                onOpenCustomerShop = {
                    viewModel.closeSubScreen()
                    viewModel.switchTab(MainTab.HOME)
                }
            )
        }

        else -> Unit
    }
}

@Composable
private fun GyanviModalSheetsHost(
    viewModel: GyanviViewModel,
    subScreen: AppSubScreen,
    activeFilterTab: FilterTab
) {
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val subCategories by viewModel.subCategories.collectAsStateWithLifecycle()
    val selectedOrderStatus by viewModel.selectedOrderStatus.collectAsStateWithLifecycle()
    val selectedProductForShare by viewModel.selectedProductForShare.collectAsStateWithLifecycle()
    val deliveryAddress by viewModel.deliveryAddress.collectAsStateWithLifecycle()
    val selectedOrderForTracking by viewModel.selectedOrderForTracking.collectAsStateWithLifecycle()

    if (subScreen == AppSubScreen.SORT_SHEET) {
        SortBottomSheet(
            selectedSort = filterState.sortOption,
            onDismiss = { viewModel.closeSubScreen() },
            onSelectSort = { newSort ->
                viewModel.updateFilterState { it.copy(sortOption = newSort) }
            }
        )
    }

    if (subScreen == AppSubScreen.FILTER_SHEET) {
        val dynamicFilterCats = remember(categories, subCategories) {
            (categories.filter { it.isActive }.sortedBy { it.sortOrder }.map { it.name } +
                subCategories.filter { it.isActive }.map { it.name }).distinctBy { it.lowercase() }
        }
        FilterBottomSheet(
            filterState = filterState,
            initialTab = activeFilterTab,
            matchingCount = filteredProducts.size,
            onDismiss = { viewModel.closeSubScreen() },
            onApplyFilter = { newFilter -> viewModel.updateFilterState { newFilter } },
            onResetFilter = { viewModel.resetFilters() },
            dynamicCategories = dynamicFilterCats
        )
    }

    if (subScreen == AppSubScreen.ORDER_FILTER_SHEET) {
        OrderStatusFilterSheet(
            currentStatus = selectedOrderStatus,
            onDismiss = { viewModel.closeSubScreen() },
            onApplyStatus = { status ->
                viewModel.setOrderStatusFilter(status)
                viewModel.closeSubScreen()
            }
        )
    }

    if (subScreen == AppSubScreen.SHARE_SHEET) {
        selectedProductForShare?.let { prod ->
            ShareBottomSheet(
                product = prod,
                onDismiss = { viewModel.closeSubScreen() },
                onOpenContacts = { viewModel.openShareContacts() },
                onOpenDirectMessageOtp = { viewModel.openSubScreen(AppSubScreen.DIRECT_MESSAGE_OTP) }
            )
        }
    }

    if (subScreen == AppSubScreen.SHARE_CONTACTS_SHEET) {
        selectedProductForShare?.let { prod ->
            ShareContactsSheet(
                product = prod,
                onDismiss = { viewModel.closeSubScreen() }
            )
        }
    }

    if (subScreen == AppSubScreen.CAMERA_SEARCH) {
        PhotoSearchModal(
            onDismiss = { viewModel.closeSubScreen() },
            onPhotoSelected = { keyword ->
                viewModel.submitSearch(keyword)
                viewModel.openSubScreen(AppSubScreen.SEARCH)
            }
        )
    }

    if (subScreen == AppSubScreen.VOICE_SEARCH) {
        VoiceSearchModal(
            onDismiss = { viewModel.closeSubScreen() },
            onVoiceResult = { phrase ->
                viewModel.submitSearch(phrase)
                viewModel.openSubScreen(AppSubScreen.SEARCH)
            }
        )
    }

    if (subScreen == AppSubScreen.LOCATION_PICKER) {
        LocationPickerModal(
            currentLocation = deliveryAddress,
            onDismiss = { viewModel.closeSubScreen() },
            onSelectLocation = { loc -> viewModel.updateDeliveryAddress(loc) }
        )
    }

    if (subScreen == AppSubScreen.TRACK_ORDER) {
        selectedOrderForTracking?.let { order ->
            TrackOrderModal(
                order = order,
                onDismiss = { viewModel.closeSubScreen() }
            )
        }
    }

    if (subScreen == AppSubScreen.ORDER_SUCCESS) {
        OrderSuccessDialog(
            onDismiss = { viewModel.closeSubScreen() },
            onViewOrders = {
                viewModel.closeSubScreen()
                viewModel.switchTab(MainTab.MY_ORDERS)
            },
            onOpenSellerOrders = {
                viewModel.openSellerOrdersScreen(orderTab = 1)
            }
        )
    }
}
