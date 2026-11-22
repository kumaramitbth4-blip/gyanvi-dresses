package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.data.local.AccountType
import com.example.data.local.AppManagementStorage
import com.example.data.local.AuthResult
import com.example.data.local.CategoryManagementRepository
import com.example.data.local.GyanviDatabase
import com.example.data.local.OrderLocalStorage
import com.example.data.local.PasswordHasher
import com.example.data.local.ProductCatalogRepository
import com.example.data.local.ProductEntity
import com.example.data.local.UserAccountEntity
import com.example.data.local.UserAccountRepository
import com.example.data.local.toEntity
import com.example.data.remote.AiDressRecommendationResult
import com.example.data.remote.AiFashionService
import com.example.data.remote.AiInferenceState
import com.example.data.remote.AiSmartSearchResult
import com.example.data.remote.FirestoreOrderRepository
import com.example.model.*
import com.example.util.GoogleSignInHelper
import com.example.util.authStateFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

sealed interface CloudSyncState {
    data object Idle : CloudSyncState
    data object Syncing : CloudSyncState
    data class Synced(val uid: String, val email: String, val orderCount: Int) : CloudSyncState
    data class Error(val message: String) : CloudSyncState
}

data class LoginOtpSession(
    val mobileNumber: String = "",
    val generatedOtp: String = "",
    val isOtpGenerated: Boolean = false,
    val generatedAtMillis: Long = 0L,
    val attemptCount: Int = 0
)

enum class AppSubScreen {
    NONE,
    SEARCH,
    CART,
    WISHLIST,
    PRODUCT_DETAIL,
    FILTER_SHEET,
    SORT_SHEET,
    ORDER_FILTER_SHEET,
    SHARE_SHEET,
    SHARE_CONTACTS_SHEET,
    FEEDBACK,
    ORDER_SUCCESS,
    TRACK_ORDER,
    LOCATION_PICKER,
    VOICE_SEARCH,
    CAMERA_SEARCH,
    ACCOUNT,
    PROFILE,
    SELLER_PANEL,
    MY_PAYMENTS,
    FOLLOWED_SHOPS,
    MEESHO_BALANCE,
    BALANCE_TERMS,
    BALANCE_FAQS,
    APP_SETTINGS,
    LEGAL_POLICIES,
    ADMIN_PANEL,
    DIRECT_MESSAGE_OTP,
    NOTIFICATIONS,
    PRODUCT_COMPARE,
    AI_FASHION_ASSISTANT,
    SECURITY_CENTER,
    DEVELOPMENT_PROGRESS,
    USER_AUTH,
    APP_MANAGEMENT,
    APP_BACKUP,
    CONTROL_PANEL,
    STAFF_WORKSPACE
}

class GyanviViewModel(
    enableTimer: Boolean = true,
    private val productRepository: ProductCatalogRepository? = null,
    private val userRepository: UserAccountRepository? = null,
    private val appContext: Context? = null,
    private val firestoreOrderRepository: FirestoreOrderRepository? = null,
    private val categoryRepository: CategoryManagementRepository? = null
) : ViewModel() {

    private val authPrefs by lazy {
        appContext?.getSharedPreferences("gyanvi_auth_prefs", Context.MODE_PRIVATE)
    }

    private val managementStorage: AppManagementStorage? by lazy {
        appContext?.let { AppManagementStorage(it) }
    }

    private val orderStorage: OrderLocalStorage? by lazy {
        appContext?.let { OrderLocalStorage(it) }
    }

    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab = _currentTab.asStateFlow()

    private val _subScreen = MutableStateFlow(AppSubScreen.NONE)
    val subScreen = _subScreen.asStateFlow()

    private val _sellerPreferredBottomTab = MutableStateFlow(0)
    val sellerPreferredBottomTab = _sellerPreferredBottomTab.asStateFlow()

    private val _sellerPreferredOrderTab = MutableStateFlow(1)
    val sellerPreferredOrderTab = _sellerPreferredOrderTab.asStateFlow()

    fun openSellerOrdersScreen(orderTab: Int = 1) {
        _sellerPreferredBottomTab.value = 1
        _sellerPreferredOrderTab.value = orderTab.coerceIn(0, 5)
        _subScreen.value = AppSubScreen.SELLER_PANEL
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _recentSearches = MutableStateFlow(
        listOf("short kurti", "banarasi saree", "lehenga choli", "anarkali suit")
    )
    val recentSearches = _recentSearches.asStateFlow()

    val popularSearches = SampleData.popularSearches

    private val _filterState = MutableStateFlow(FilterState())
    val filterState = _filterState.asStateFlow()

    private val _products = MutableStateFlow(SampleData.sampleProducts)
    val allProducts = _products.asStateFlow()

    private val _cartItems = MutableStateFlow(
        listOf(
            CartItem(SampleData.sampleProducts[0], selectedSize = "XL", quantity = 1)
        )
    )
    val cartItems = _cartItems.asStateFlow()

    private val _wishlistIds = MutableStateFlow(setOf("gd_01", "gd_02"))
    val wishlistIds = _wishlistIds.asStateFlow()

    private val _orders = MutableStateFlow(
        appContext?.let { OrderLocalStorage(it).loadOrders(SampleData.initialOrders, SampleData.sampleProducts) }
            ?: SampleData.initialOrders
    )
    val orders = _orders.asStateFlow()

    private val _autoPendingNotificationEnabled = MutableStateFlow(true)
    val autoPendingNotificationEnabled = _autoPendingNotificationEnabled.asStateFlow()

    private val _autoTrackingEnabled = MutableStateFlow(true)
    val autoTrackingEnabled = _autoTrackingEnabled.asStateFlow()

    private val _autoRefundTrackingEnabled = MutableStateFlow(true)
    val autoRefundTrackingEnabled = _autoRefundTrackingEnabled.asStateFlow()

    private val _firebaseUid = MutableStateFlow<String?>(null)
    val firebaseUid = _firebaseUid.asStateFlow()

    private val _firebaseUserEmail = MutableStateFlow("")
    val firebaseUserEmail = _firebaseUserEmail.asStateFlow()

    private val _cloudSyncState = MutableStateFlow<CloudSyncState>(CloudSyncState.Idle)
    val cloudSyncState = _cloudSyncState.asStateFlow()

    private var firestoreSyncJob: Job? = null

    private val _selectedOrderStatus = MutableStateFlow(OrderStatus.ALL)
    val selectedOrderStatus = _selectedOrderStatus.asStateFlow()

    private val _orderSearchQuery = MutableStateFlow("")
    val orderSearchQuery = _orderSearchQuery.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct = _selectedProduct.asStateFlow()

    private val _selectedProductForShare = MutableStateFlow<Product?>(null)
    val selectedProductForShare = _selectedProductForShare.asStateFlow()

    private val _selectedOrderForFeedback = MutableStateFlow<OrderItem?>(null)
    val selectedOrderForFeedback = _selectedOrderForFeedback.asStateFlow()

    private val _selectedOrderForTracking = MutableStateFlow<OrderItem?>(null)
    val selectedOrderForTracking = _selectedOrderForTracking.asStateFlow()

    private val _deliveryAddress = MutableStateFlow("Bettiah - 845438")
    val deliveryAddress = _deliveryAddress.asStateFlow()

    private val _videoFinds = MutableStateFlow(SampleData.sampleVideoFinds)
    val videoFinds = _videoFinds.asStateFlow()

    // Maha Sale Countdown timer (starts at 2 days, 15 hours, 24 mins, 28 secs)
    private val _countdownSeconds = MutableStateFlow(2 * 86400 + 15 * 3600 + 24 * 60 + 28)
    val countdownSeconds = _countdownSeconds.asStateFlow()

    // Feedback Stepper State (Step 1, 2, 3)
    val feedbackStep = MutableStateFlow(1)
    val feedbackOverallRating = MutableStateFlow(5)
    val feedbackMaterialRating = MutableStateFlow(5)
    val feedbackComment = MutableStateFlow("")
    val feedbackPhotosAdded = MutableStateFlow(listOf<String>())

    // Marketplace Theme, Language & Account Role (Frontend + RBAC Modules)
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode = _isDarkMode.asStateFlow()

    private val _appLanguage = MutableStateFlow("English")
    val appLanguage = _appLanguage.asStateFlow()

    private val _activeRole = MutableStateFlow(UserAccountRole.BUYER)
    val activeRole = _activeRole.asStateFlow()

    // Wallet & Reward Points (Buyer Panel Module 3)
    private val _walletBalance = MutableStateFlow(250)
    val walletBalance = _walletBalance.asStateFlow()

    private val _rewardPoints = MutableStateFlow(420)
    val rewardPoints = _rewardPoints.asStateFlow()

    // Product Comparison List (Module 3 & 13)
    private val _compareProducts = MutableStateFlow<List<Product>>(emptyList())
    val compareProducts = _compareProducts.asStateFlow()

    // Advanced Marketplace Setup State (Currency, Warehouse, VIP Subscription, Sellers)
    private val _selectedCurrency = MutableStateFlow("INR ₹")
    val selectedCurrency = _selectedCurrency.asStateFlow()

    private val _activeWarehouse = MutableStateFlow("Bettiah Main Hub (845438)")
    val activeWarehouse = _activeWarehouse.asStateFlow()

    private val _isVipSubscribed = MutableStateFlow(false)
    val isVipSubscribed = _isVipSubscribed.asStateFlow()

    private val _registeredSellersCount = MutableStateFlow(4)
    val registeredSellersCount = _registeredSellersCount.asStateFlow()

    // Real Cloud AI Service (Firebase AI Logic Gemini)
    private val aiFashionService = AiFashionService()

    private val _aiStylistState = MutableStateFlow<AiInferenceState<AiDressRecommendationResult>>(AiInferenceState.Idle)
    val aiStylistState = _aiStylistState.asStateFlow()

    private val _aiSearchState = MutableStateFlow<AiInferenceState<AiSmartSearchResult>>(AiInferenceState.Idle)
    val aiSearchState = _aiSearchState.asStateFlow()

    // Admin Login Gate
    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated = _isAdminAuthenticated.asStateFlow()

    // Claimed referral codes (per session)
    private val claimedReferralCodes = mutableSetOf<String>()

    // Persistent Buyer-Seller Marketplace Chat
    private val _buyerSellerMessages = MutableStateFlow(
        listOf(
            BuyerSellerChatMessage(
                id = "msg_1",
                senderName = "Gyanvi Boutique Seller",
                senderRole = "SELLER",
                productName = "Royal Wine Floral Anarkali Kurti",
                text = "Namaste! Ready-to-dispatch stock available in S, M, L, XL & XXL with 7-day easy return.",
                timeLabel = "10:15 AM"
            )
        )
    )
    val buyerSellerMessages = _buyerSellerMessages.asStateFlow()

    // Role & Department Work Division (Owner vs Manager vs Karamchari/Staff vs Customer)
    private val _staffMembers = MutableStateFlow(
        listOf(
            StaffMember(
                id = "emp-100",
                name = "Vikram Raj",
                staffCode = "MGR-GD-100",
                phone = "9128700100",
                pin = "0000",
                department = StaffDepartment.DEPARTMENT_MANAGER,
                shift = "Full Shift (9 AM - 7 PM)",
                tasksCompleted = 31,
                monthlySalary = 32000,
                attendanceStatus = "Present",
                checkInTime = "09:00 AM",
                attendanceDaysThisMonth = 24,
                performanceScore = 98
            ),
            StaffMember(
                id = "emp-101",
                name = "Ravi Kumar",
                staffCode = "EMP-GD-101",
                phone = "9128700101",
                pin = "1111",
                department = StaffDepartment.ORDER_PACKER,
                shift = "Morning (9 AM - 6 PM)",
                tasksCompleted = 18,
                monthlySalary = 16500,
                attendanceStatus = "Present",
                checkInTime = "09:10 AM",
                attendanceDaysThisMonth = 23,
                performanceScore = 95
            ),
            StaffMember(
                id = "emp-102",
                name = "Vikash Paswan",
                staffCode = "EMP-GD-102",
                phone = "9128700102",
                pin = "2222",
                department = StaffDepartment.DISPATCH_DELIVERY,
                shift = "Day Field (10 AM - 7 PM)",
                tasksCompleted = 24,
                monthlySalary = 17500,
                attendanceStatus = "Present",
                checkInTime = "09:50 AM",
                attendanceDaysThisMonth = 22,
                performanceScore = 96
            ),
            StaffMember(
                id = "emp-103",
                name = "Priya Kumari",
                staffCode = "EMP-GD-103",
                phone = "9128700103",
                pin = "3333",
                department = StaffDepartment.CATALOG_INVENTORY,
                shift = "Morning (9 AM - 6 PM)",
                tasksCompleted = 14,
                monthlySalary = 18000,
                attendanceStatus = "Present",
                checkInTime = "09:15 AM",
                attendanceDaysThisMonth = 21,
                performanceScore = 92
            ),
            StaffMember(
                id = "emp-104",
                name = "Neha Verma",
                staffCode = "EMP-GD-104",
                phone = "9128700104",
                pin = "4444",
                department = StaffDepartment.SUPPORT_RETURNS,
                shift = "Morning (9 AM - 6 PM)",
                tasksCompleted = 19,
                monthlySalary = 17000,
                attendanceStatus = "Present",
                checkInTime = "09:05 AM",
                attendanceDaysThisMonth = 23,
                performanceScore = 94
            ),
            StaffMember(
                id = "emp-105",
                name = "Suresh Prasad",
                staffCode = "EMP-GD-105",
                phone = "9128700105",
                pin = "5555",
                department = StaffDepartment.FINANCE_ACCOUNTS,
                shift = "Accounts (10 AM - 6 PM)",
                tasksCompleted = 12,
                monthlySalary = 24000,
                attendanceStatus = "Present",
                checkInTime = "09:55 AM",
                attendanceDaysThisMonth = 24,
                performanceScore = 97
            )
        )
    )
    val staffMembers = _staffMembers.asStateFlow()

    private val _activityHistory = MutableStateFlow(
        listOf(
            SecurityActivityEntry(
                id = "act-1",
                actorName = "Amit Kumar Patwa",
                roleBadge = "OWNER",
                action = "Configured 5-Department RBAC Permissions & Payment Gateways",
                timestamp = "Today, 09:00 AM"
            ),
            SecurityActivityEntry(
                id = "act-2",
                actorName = "Vikram Raj (MGR-GD-100)",
                roleBadge = "MANAGER",
                action = "Checked In & Reviewed Morning Attendance (6/6 Staff Present)",
                timestamp = "Today, 09:10 AM"
            ),
            SecurityActivityEntry(
                id = "act-3",
                actorName = "Ravi Kumar (EMP-GD-101)",
                roleBadge = "PACKER",
                action = "Assigned Packer Tag PKR-GD-845438 to morning ethnic orders",
                timestamp = "Today, 09:25 AM"
            )
        )
    )
    val activityHistory = _activityHistory.asStateFlow()

    private val _recentlyViewedIds = MutableStateFlow<List<String>>(emptyList())
    val recentlyViewedIds = _recentlyViewedIds.asStateFlow()

    private val _activeStaffId = MutableStateFlow("emp-101")
    val activeStaffMember: StateFlow<StaffMember?> = combine(_staffMembers, _activeStaffId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _staffTasks = MutableStateFlow(
        listOf(
            StaffAssignedTask(
                id = "tsk-1",
                title = "Pack New Pending Ethnic Orders & Print Labels",
                description = "Verify dress size, attach Packer ID PKR-GD-845438 and move orders to Ready to Ship.",
                department = StaffDepartment.ORDER_PACKER,
                assignedToStaffId = "emp-101",
                assignedToName = "Ravi Kumar",
                priority = "High",
                status = "Pending"
            ),
            StaffAssignedTask(
                id = "tsk-2",
                title = "Deliver Today's Bettiah & Patna Parcels",
                description = "Pick Ready-to-Ship bags from Bettiah Hub and update Out for Delivery -> Delivered.",
                department = StaffDepartment.DISPATCH_DELIVERY,
                assignedToStaffId = "emp-102",
                assignedToName = "Vikash Paswan",
                priority = "High",
                status = "In Progress"
            ),
            StaffAssignedTask(
                id = "tsk-3",
                title = "Restock Low-Stock Banarasi Sarees & Kurtis",
                description = "Add +10 stock for fast-selling wedding items and verify color tags.",
                department = StaffDepartment.CATALOG_INVENTORY,
                assignedToStaffId = "emp-103",
                assignedToName = "Priya Kumari",
                priority = "Medium",
                status = "Pending"
            ),
            StaffAssignedTask(
                id = "tsk-4",
                title = "Resolve Return Pickup & Reply Buyer Queries",
                description = "Respond to size/fabric messages in Customer Chat and schedule Delhivery reverse pickup.",
                department = StaffDepartment.SUPPORT_RETURNS,
                assignedToStaffId = "emp-104",
                assignedToName = "Neha Verma",
                priority = "High",
                status = "Pending"
            ),
            StaffAssignedTask(
                id = "tsk-5",
                title = "Download GST Tax Invoices & Tally Daily COD",
                description = "Verify Prepaid UPI vs Cash on Delivery sheet before 6 PM Owner Payout Review.",
                department = StaffDepartment.FINANCE_ACCOUNTS,
                assignedToStaffId = "emp-105",
                assignedToName = "Suresh Prasad",
                priority = "Normal",
                status = "Pending"
            )
        )
    )
    val staffTasks = _staffTasks.asStateFlow()

    // Notification System & Settings (Modules 2, 6, 10)
    private val _notificationPrefs = MutableStateFlow(NotificationPreferences())
    val notificationPrefs = _notificationPrefs.asStateFlow()

    private val _notifications = MutableStateFlow(
        listOf(
            AppNotification(
                id = "n1",
                title = "Festive Maha Sale Live! 🎉",
                message = "Flat 60%–80% OFF on Banarasi Sarees & Designer Kurtis. Use code FESTIVE15.",
                category = "Offers",
                timeAgo = "5 mins ago",
                isRead = false
            ),
            AppNotification(
                id = "n2",
                title = "Order Shipped via Delhivery 🚚",
                message = "Your order #OD84592011 is out for delivery to Bettiah - 845438. AWB: DLV-845438912.",
                category = "Orders",
                timeAgo = "1 hr ago",
                isRead = false
            ),
            AppNotification(
                id = "n3",
                title = "₹250 Gyanvi Wallet Cashback Credited 💰",
                message = "Festive bonus & 420 Reward Points added to your Gyanvi Dresses Wallet.",
                category = "Wallet",
                timeAgo = "3 hrs ago",
                isRead = false
            ),
            AppNotification(
                id = "n4",
                title = "Cart Item Price Dropped! ⚡",
                message = "Royal Wine Floral Anarkali Kurti Set price dropped by ₹100. Grab before stock ends!",
                category = "Offers",
                timeAgo = "Yesterday",
                isRead = true
            ),
            AppNotification(
                id = "n5",
                title = "2FA Login Verified 🔒",
                message = "New login authenticated via OTP from Bettiah, Bihar (+91 9128754982).",
                category = "Security",
                timeAgo = "2 days ago",
                isRead = true
            )
        )
    )
    val notifications = _notifications.asStateFlow()

    val offlineCatalogEntities: StateFlow<List<ProductEntity>> =
        productRepository?.allCachedEntities
            ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
            ?: MutableStateFlow(emptyList())

    // Registered Customer & Seller Accounts + Active Authenticated Session (starts null -> requires login/register on front page)
    private val _currentUser = MutableStateFlow<UserAccountEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _inMemoryAccounts = MutableStateFlow(
        listOf(
            UserAccountEntity(
                id = "cust_default_01",
                accountType = AccountType.CUSTOMER,
                fullName = "Amit Kumar Patwa",
                mobileNumber = "9128754982",
                email = "kumaramitbth4@gmail.com",
                passwordHash = PasswordHasher.hash("Gyanvi@123"),
                businessAddress = "Ward No. 12, Bettiah, Bihar - 845438",
                isVerified = true
            ),
            UserAccountEntity(
                id = "seller_default_01",
                accountType = AccountType.SELLER,
                fullName = "Amit Kumar Patwa",
                mobileNumber = "9835107144",
                email = "seller@gyanvidresses.in",
                passwordHash = PasswordHasher.hash("Seller@123"),
                shopName = "Gyanvi Dresses Boutique",
                businessAddress = "Lal Bazar, Main Road, Bettiah - 845438",
                gstin = "10ABCDE1234F1Z5",
                isVerified = true
            )
        )
    )

    val registeredAccounts: StateFlow<List<UserAccountEntity>> =
        userRepository?.allAccounts
            ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _inMemoryAccounts.value)
            ?: _inMemoryAccounts.asStateFlow()

    private val _authPreferredType = MutableStateFlow(AccountType.CUSTOMER)
    val authPreferredType = _authPreferredType.asStateFlow()

    private val _authPreferredRegisterMode = MutableStateFlow(false)
    val authPreferredRegisterMode = _authPreferredRegisterMode.asStateFlow()

    private val _loginOtpSession = MutableStateFlow(LoginOtpSession())
    val loginOtpSession = _loginOtpSession.asStateFlow()

    // Master App Management & Live CMS Configuration (Theme, Dashboard, Images, Texts, Likhavat, Options, Categories)
    private val _managementConfig = MutableStateFlow(
        appContext?.let { AppManagementStorage(it).loadConfig() } ?: AppManagementConfig()
    )
    val managementConfig = _managementConfig.asStateFlow()

    private fun mergeCategoriesWithoutDuplicates(
        existing: List<CategoryItem>,
        defaults: List<CategoryItem>
    ): List<CategoryItem> {
        val seenIds = existing.map { it.id }.toSet()
        val seenNames = existing.map { it.name.trim().lowercase() }.toSet()
        val missing = defaults.filter { it.id !in seenIds && it.name.trim().lowercase() !in seenNames }
        return if (missing.isEmpty()) existing else (existing + missing).sortedBy { it.sortOrder }
    }

    private fun mergeSubCategoriesWithoutDuplicates(
        existing: List<SubCategoryItem>,
        defaults: List<SubCategoryItem>
    ): List<SubCategoryItem> {
        val seenIds = existing.map { it.id }.toSet()
        val seenKeys = existing.map { "${it.parentCategory.trim().lowercase()}|${it.name.trim().lowercase()}" }.toSet()
        val missing = defaults.filter {
            it.id !in seenIds &&
                "${it.parentCategory.trim().lowercase()}|${it.name.trim().lowercase()}" !in seenKeys
        }
        return if (missing.isEmpty()) existing else (existing + missing).sortedBy { it.sortOrder }
    }

    private val _categories = MutableStateFlow(
        appContext?.let {
            mergeCategoriesWithoutDuplicates(
                AppManagementStorage(it).loadCategories(SampleData.categoryList),
                SampleData.categoryList
            )
        } ?: SampleData.categoryList
    )
    val categories = _categories.asStateFlow()

    private val _subCategories = MutableStateFlow(
        appContext?.let {
            mergeSubCategoriesWithoutDuplicates(
                AppManagementStorage(it).loadSubCategories(SampleData.defaultSubCategories),
                SampleData.defaultSubCategories
            )
        } ?: SampleData.defaultSubCategories
    )
    val subCategories = _subCategories.asStateFlow()

    private var productImageOverrides: Map<String, String> =
        appContext?.let { AppManagementStorage(it).loadProductImageOverrides() } ?: emptyMap()

    // App Backup & Restore Snapshots
    private val _appBackups = MutableStateFlow(
        appContext?.let { AppManagementStorage(it).loadBackupSnapshots() } ?: emptyList()
    )
    val appBackups = _appBackups.asStateFlow()

    private val _lastBackupJsonText = MutableStateFlow("")
    val lastBackupJsonText = _lastBackupJsonText.asStateFlow()

    private data class InMemoryBackupPayload(
        val snapshot: AppBackupSnapshot,
        val config: AppManagementConfig,
        val categories: List<CategoryItem>,
        val productImageOverrides: Map<String, String>,
        val products: List<Product>,
        val wishlistIds: Set<String>,
        val deliveryAddress: String
    )
    private val inMemoryBackups = mutableMapOf<String, InMemoryBackupPayload>()

    init {
        // Apply any persisted custom product image URIs immediately on startup
        if (productImageOverrides.isNotEmpty()) {
            _products.update { list ->
                list.map { p ->
                    productImageOverrides[p.id]?.let { uri -> p.copy(customImageUri = uri) } ?: p
                }
            }
        }

        // Seed and observe separated Category & SubCategory Room tables
        if (categoryRepository != null) {
            viewModelScope.launch {
                categoryRepository.seedDefaultsIfEmpty(
                    defaultCategories = SampleData.categoryList,
                    defaultSubCategories = SampleData.defaultSubCategories
                )
            }
            viewModelScope.launch {
                categoryRepository.allCategories.collect { storedCats ->
                    if (storedCats.isNotEmpty()) {
                        _categories.value = storedCats
                        managementStorage?.saveCategories(storedCats)
                    }
                }
            }
            viewModelScope.launch {
                categoryRepository.allSubCategories.collect { storedSubs ->
                    if (storedSubs.isNotEmpty()) {
                        _subCategories.value = storedSubs
                        managementStorage?.saveSubCategories(storedSubs)
                    }
                }
            }
        }

        // Seed and observe offline Room catalog cache
        if (productRepository != null) {
            viewModelScope.launch {
                productRepository.seedInitialCatalogIfEmpty(SampleData.sampleProducts)
                productRepository.allCachedProducts.collect { cached ->
                    _products.value = cached.map { prod ->
                        val customUri = productImageOverrides[prod.id]
                        if (customUri != null) {
                            prod.copy(imageUrl = prod.imageUrl.ifBlank { customUri }, customImageUri = customUri)
                        } else prod
                    }
                }
            }
        }

        // Seed Room customer & seller accounts and restore explicit saved session if present
        if (userRepository != null) {
            viewModelScope.launch {
                userRepository.seedInitialAccountsIfEmpty()
                val savedUserId = authPrefs?.getString(KEY_LOGGED_IN_USER_ID, null)
                if (!savedUserId.isNullOrBlank()) {
                    val savedAccount = userRepository.getAccountById(savedUserId)
                    if (savedAccount != null) {
                        _currentUser.value = savedAccount
                        _activeRole.value = if (savedAccount.accountType == AccountType.SELLER) {
                            UserAccountRole.SELLER
                        } else {
                            UserAccountRole.BUYER
                        }
                    }
                }
                userRepository.allAccounts.collect { accounts ->
                    val sellersCount = accounts.count { it.accountType == AccountType.SELLER }
                    if (sellersCount > 0) {
                        _registeredSellersCount.value = sellersCount.coerceAtLeast(4)
                    }
                    val activeId = _currentUser.value?.id
                    if (activeId != null) {
                        val refreshed = accounts.find { it.id == activeId }
                        if (refreshed != null) {
                            _currentUser.value = refreshed
                        }
                    }
                }
            }
        }

        // Ticking countdown timer
        if (enableTimer) {
            viewModelScope.launch {
                while (true) {
                    delay(1000)
                    _countdownSeconds.update { if (it > 0) it - 1 else 86400 * 2 }
                }
            }
        }

        // Persist any order mutations (customer purchases + seller lifecycle changes) locally
        if (orderStorage != null) {
            viewModelScope.launch {
                _orders.drop(1).collect { updatedOrders ->
                    orderStorage?.saveOrders(updatedOrders)
                }
            }
        }

        // Firebase Auth + Firestore Realtime Order Sync (auth-gated)
        if (firestoreOrderRepository != null) {
            viewModelScope.launch {
                FirebaseAuth.getInstance().authStateFlow().collectLatest { firebaseUser ->
                    if (firebaseUser != null) {
                        _firebaseUid.value = firebaseUser.uid
                        _firebaseUserEmail.value = firebaseUser.email.orEmpty()
                        startFirestoreOrderSync(firebaseUser.uid, firebaseUser.email.orEmpty())
                    } else {
                        stopFirestoreOrderSync()
                    }
                }
            }
        }
    }

    private fun startFirestoreOrderSync(uid: String, email: String) {
        val repo = firestoreOrderRepository ?: return
        firestoreSyncJob?.cancel()
        _cloudSyncState.value = CloudSyncState.Syncing
        firestoreSyncJob = viewModelScope.launch {
            val localSnapshot = _orders.value
            repo.seedInitialOrdersIfEmpty(uid, localSnapshot)
            var pushedLocalOnly = false
            repo.observeOrders(uid, _products.value)
                .catch { error ->
                    val root = FirestoreOrderRepository.unwrapFirestoreError(error)
                    if (root !is com.google.firebase.firestore.FirebaseFirestoreException && error is CancellationException) {
                        throw error
                    }
                    _cloudSyncState.value = CloudSyncState.Error(
                        root.localizedMessage ?: "Failed to sync orders with Firestore"
                    )
                }
                .collect { remoteOrders ->
                    if (remoteOrders.isNotEmpty()) {
                        val remoteIds = remoteOrders.mapTo(HashSet()) { it.orderId }
                        val localOnly = _orders.value.filter { it.orderId !in remoteIds }
                        if (!pushedLocalOnly && localOnly.isNotEmpty()) {
                            pushedLocalOnly = true
                            repo.syncOrdersBatch(uid, localOnly)
                        }
                        val merged = localOnly + remoteOrders
                        val existingIndex = _orders.value.withIndex().associate { it.value.orderId to it.index }
                        _orders.value = merged.sortedBy { existingIndex[it.orderId] ?: -1 }
                    }
                    _cloudSyncState.value = CloudSyncState.Synced(uid, email, _orders.value.size)
                }
        }
    }

    private fun stopFirestoreOrderSync() {
        firestoreSyncJob?.cancel()
        firestoreSyncJob = null
        _firebaseUid.value = null
        _firebaseUserEmail.value = ""
        _cloudSyncState.value = CloudSyncState.Idle
    }

    fun syncOrderNow(orderId: String) {
        val repo = firestoreOrderRepository ?: return
        val uid = _firebaseUid.value ?: return
        val order = _orders.value.find { it.orderId == orderId } ?: return
        viewModelScope.launch {
            repo.upsertOrder(uid, order)
        }
    }

    fun syncAllOrdersToCloud() {
        val repo = firestoreOrderRepository ?: return
        val uid = _firebaseUid.value ?: return
        val email = _firebaseUserEmail.value
        val snapshotOrders = _orders.value
        viewModelScope.launch {
            _cloudSyncState.value = CloudSyncState.Syncing
            val res = repo.syncOrdersBatch(uid, snapshotOrders)
            _cloudSyncState.value = if (res.isSuccess) {
                CloudSyncState.Synced(uid, email, snapshotOrders.size)
            } else {
                CloudSyncState.Error(res.exceptionOrNull()?.localizedMessage ?: "Cloud sync failed")
            }
        }
    }

    // Filtered Products computation (supports Main Category, Subcategory, Multi-Category & Custom Categories)
    val filteredProducts = combine(
        _products,
        _filterState,
        _searchQuery,
        _subCategories
    ) { prods, filters, query, subs ->
        val selectedCat = filters.selectedCategory.trim()
        val childSubNames = if (selectedCat.isNotBlank() && !selectedCat.equals("All", ignoreCase = true)) {
            subs.filter {
                it.isActive && (
                    it.parentCategory.equals(selectedCat, ignoreCase = true) ||
                        it.parentCategoryId.equals(selectedCat, ignoreCase = true)
                    )
            }.map { it.name }
        } else {
            emptyList()
        }

        prods.filter { prod ->
            val matchesQuery = query.isBlank() ||
                prod.name.contains(query, ignoreCase = true) ||
                prod.allAssignedCategories.any { it.contains(query, ignoreCase = true) } ||
                prod.fabric.contains(query, ignoreCase = true)

            val matchesCategory = selectedCat.isBlank() ||
                selectedCat.equals("All", ignoreCase = true) ||
                prod.matchesCategory(selectedCat) ||
                childSubNames.any { subName -> prod.matchesCategory(subName) } ||
                (selectedCat.equals("Offers & Discounts", ignoreCase = true) && (prod.isMahaDeal || prod.discountPercent >= 50)) ||
                (selectedCat.equals("Best Sellers", ignoreCase = true) && prod.ratingCount >= 10000) ||
                (selectedCat.equals("New Arrivals", ignoreCase = true) && (prod.id.startsWith("gd_custom") || prod.isMallAssured))

            val matchesPrice = prod.price <= filters.maxPrice
            val matchesRating = prod.rating >= filters.minRating
            val matchesStock = !filters.onlyInStock || prod.inStock
            val matchesMaha = !filters.onlyMahaDeals || prod.isMahaDeal

            matchesQuery && matchesCategory && matchesPrice && matchesRating && matchesStock && matchesMaha
        }.let { list ->
            when (filters.sortOption) {
                SortOption.RELEVANCE -> list
                SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.price }
                SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.price }
                SortOption.RATING_HIGH -> list.sortedByDescending { it.rating }
                SortOption.DISCOUNT -> list.sortedByDescending { it.discountPercent }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SampleData.sampleProducts)

    // Navigation & Screen management
    private val subScreenStack = mutableListOf<AppSubScreen>()

    fun switchTab(tab: MainTab) {
        _currentTab.value = tab
        subScreenStack.clear()
        _subScreen.value = AppSubScreen.NONE
    }

    fun openSubScreen(screen: AppSubScreen) {
        if (_subScreen.value != AppSubScreen.NONE && _subScreen.value != screen) {
            subScreenStack.add(_subScreen.value)
        }
        _subScreen.value = screen
    }

    fun closeSubScreen() {
        if (subScreenStack.isNotEmpty()) {
            _subScreen.value = subScreenStack.removeAt(subScreenStack.lastIndex)
        } else {
            _subScreen.value = AppSubScreen.NONE
        }
    }

    // Search Operations
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun submitSearch(query: String) {
        if (query.isNotBlank()) {
            _searchQuery.value = query
            _recentSearches.update { list ->
                (listOf(query) + list.filter { it != query }).take(8)
            }
        }
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    // Filter Operations
    fun updateFilterState(updater: (FilterState) -> FilterState) {
        _filterState.update(updater)
    }

    fun resetFilters() {
        _filterState.value = FilterState(query = _searchQuery.value)
    }

    fun selectCategoryFilter(category: String) {
        _filterState.update { it.copy(selectedCategory = category) }
    }

    // Wishlist Operations
    fun toggleWishlist(productId: String) {
        _wishlistIds.update { set ->
            if (set.contains(productId)) set - productId else set + productId
        }
    }

    // Cart Operations
    fun addToCart(product: Product, size: String = "L", quantity: Int = 1) {
        _cartItems.update { items ->
            val existing = items.find { it.product.id == product.id && it.selectedSize == size }
            if (existing != null) {
                items.map {
                    if (it.product.id == product.id && it.selectedSize == size) {
                        it.copy(quantity = it.quantity + quantity)
                    } else it
                }
            } else {
                items + CartItem(product, selectedSize = size, quantity = quantity)
            }
        }
    }

    fun updateCartQuantity(productId: String, size: String, newQty: Int) {
        if (newQty <= 0) {
            removeFromCart(productId, size)
        } else {
            _cartItems.update { items ->
                items.map {
                    if (it.product.id == productId && it.selectedSize == size) {
                        it.copy(quantity = newQty)
                    } else it
                }
            }
        }
    }

    fun updateCartSize(productId: String, oldSize: String, newSize: String) {
        _cartItems.update { items ->
            items.map {
                if (it.product.id == productId && it.selectedSize == oldSize) {
                    it.copy(selectedSize = newSize)
                } else it
            }
        }
    }

    fun removeFromCart(productId: String, size: String) {
        _cartItems.update { items ->
            items.filterNot { it.product.id == productId && it.selectedSize == size }
        }
    }

    fun moveCartItemToWishlist(productId: String, size: String) {
        toggleWishlist(productId)
        removeFromCart(productId, size)
    }

    fun checkoutAndPlaceOrder(
        paymentMethod: String = "UPI (PhonePe)",
        discountAmount: Int = 0,
        walletUsed: Int = 0
    ) {
        val currentCart = _cartItems.value
        if (currentCart.isNotEmpty()) {
            val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val totalCart = currentCart.sumOf { it.totalUpiPrice }
            val extraDeductionPerItem = if (currentCart.isNotEmpty()) (discountAmount + walletUsed) / currentCart.size else 0
            val customerDisplayName = _currentUser.value?.fullName?.takeIf { it.isNotBlank() } ?: "Amit Customer"
            val customerPhone = _currentUser.value?.mobileNumber?.takeIf { it.isNotBlank() } ?: "+91 9128754982"
            val rawAddr = _deliveryAddress.value.ifBlank { "Bettiah, Bihar - 845438" }
            val customerAddr = rawAddr.removePrefix("Delivering to ").trim().ifBlank { "Bettiah, Bihar - 845438" }
            val parsedPin = Regex("\\b(\\d{6})\\b").find(customerAddr)?.groupValues?.getOrNull(1) ?: "845438"
            val addrWithoutPin = customerAddr.replace(Regex("[-,]?\\s*\\b\\d{6}\\b"), "").trim()
            val addrParts = addrWithoutPin.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val parsedState = if (addrParts.size >= 2) addrParts.last() else "Bihar"
            val parsedCity = when {
                addrParts.size >= 2 -> addrParts[addrParts.size - 2]
                addrParts.isNotEmpty() -> addrParts.first()
                else -> "Bettiah"
            }
            val isCod = paymentMethod.contains("COD", ignoreCase = true)
            val timestampFmt = SimpleDateFormat("dd MMM hh:mm a", Locale.getDefault()).format(Date())
            val newOrders = currentCart.map { item ->
                val paidAmount = (item.totalUpiPrice - extraDeductionPerItem).coerceAtLeast(1)
                val awb = "DLV-" + (10000000..99999999).random()
                val inferredColor = when {
                    item.product.name.contains("Wine", true) -> "Royal Wine"
                    item.product.name.contains("Gold", true) -> "Banarasi Gold"
                    item.product.name.contains("Pink", true) -> "Rani Pink"
                    item.product.name.contains("Red", true) || item.product.name.contains("Crimson", true) -> "Bridal Crimson"
                    item.product.name.contains("Blue", true) -> "Peacock Blue"
                    item.product.name.contains("Yellow", true) || item.product.name.contains("Mustard", true) -> "Mustard Yellow"
                    item.product.name.contains("Green", true) -> "Emerald Green"
                    else -> "Festive Collection"
                }
                OrderItem(
                    orderId = "OD" + (1000000000..9999999999).random(),
                    product = item.product,
                    orderDate = dateFmt,
                    status = OrderStatus.ORDERED,
                    statusText = "Pending Confirmation ($paymentMethod)",
                    deliveryDateRange = "Estimated Delivery in 3-4 Days",
                    selectedSize = item.selectedSize,
                    quantity = item.quantity,
                    totalAmount = paidAmount,
                    trackingStages = listOf(
                        TrackingStage("Order Placed", "Confirmed via $paymentMethod", true, "Today"),
                        TrackingStage("Processing", "Seller review & barcode packing", false, "Tomorrow"),
                        TrackingStage("Shipped", "Courier: Delhivery • AWB: $awb", false, "In 2 days"),
                        TrackingStage("Delivery", "Delivering to $customerAddr", false, "In 4 days")
                    ),
                    isFeedbackCompleted = false,
                    paymentMethod = paymentMethod,
                    paymentStatus = if (isCod) "COD Pending" else "Paid",
                    awbNumber = awb,
                    courierPartner = "Delhivery Express",
                    expectedDeliveryDate = "Within 3-4 Days",
                    currentTrackingLocation = "Customer Order Received • Gyanvi Hub ($parsedCity)",
                    customerName = customerDisplayName,
                    customerPhone = customerPhone,
                    selectedColor = inferredColor,
                    deliveryAddress = customerAddr,
                    city = parsedCity,
                    state = parsedState,
                    pinCode = parsedPin,
                    customProductImageUri = item.product.customImageUri,
                    isLabelDownloaded = false,
                    sellerStage = "Pending",
                    orderHistoryLog = listOf(
                        "$timestampFmt: 🛒 Customer Purchase Order placed by $customerDisplayName (₹$paidAmount • $paymentMethod)"
                    )
                )
            }
            if (walletUsed > 0) {
                _walletBalance.update { (it - walletUsed).coerceAtLeast(0) }
            }
            val earnedPts = (totalCart / 50).coerceAtLeast(10)
            _rewardPoints.update { it + earnedPts }
            _orders.update { newOrders + it }
            _cartItems.value = emptyList()
            pushNotification(
                title = "Order Placed Successfully! 🛍️",
                message = "${newOrders.size} item(s) confirmed via $paymentMethod. +$earnedPts Reward Points earned!",
                category = "Orders"
            )
            if (_autoPendingNotificationEnabled.value) {
                val first = newOrders.first()
                pushNotification(
                    title = "🔔 New Customer Order in Seller Panel! (#${first.orderId})",
                    message = "$customerDisplayName bought ${first.product.name} (Size ${first.selectedSize}, ₹${first.totalAmount}). Open Seller Orders -> Pending PDF!",
                    category = "Seller"
                )
            }
            newOrders.forEach { syncOrderNow(it.orderId) }
            _subScreen.value = AppSubScreen.ORDER_SUCCESS
        }
    }

    // Seller Order Lifecycle Management
    private fun nowLogTime(): String = SimpleDateFormat("dd MMM hh:mm a", Locale.getDefault()).format(Date())

    private fun courierPrefix(courierPartner: String): String = when {
        courierPartner.contains("Blue", ignoreCase = true) -> "BLUD"
        courierPartner.contains("Ekart", ignoreCase = true) -> "EKRT"
        courierPartner.contains("Xpress", ignoreCase = true) -> "XPRS"
        courierPartner.contains("Shadow", ignoreCase = true) -> "SHDW"
        else -> "DLV"
    }

    fun toggleAutoPendingNotification(enabled: Boolean) {
        _autoPendingNotificationEnabled.value = enabled
        pushNotification(
            title = if (enabled) "Pending Auto-Notification ON 🔔" else "Pending Auto-Notification Paused 🔕",
            message = if (enabled) "नया ऑर्डर आते ही तुरंत मोबाइल नोटिफिकेशन अलर्ट मिलेगा।" else "Pending ऑर्डर के लिए ऑटो अलर्ट बंद किया गया है।",
            category = "Seller"
        )
    }

    fun confirmSellerOrder(orderId: String) {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    val updatedStages = ord.trackingStages.mapIndexed { idx, st ->
                        if (idx == 0) st.copy(isCompleted = true) else st
                    }
                    ord.copy(
                        status = OrderStatus.CONFIRMED,
                        isOrderConfirmed = true,
                        sellerStage = "Confirmed",
                        statusText = "Confirmed • Processing for Packing",
                        trackingStages = updatedStages,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Order Confirmed by Seller (Ready for Quality Check & Packing)"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "Order Confirmed ✓ (#$orderId)",
            message = "ऑर्डर #$orderId कन्फर्म हो गया है। अब पैकिंग और शिपिंग शुरू करें।",
            category = "Seller"
        )
        syncOrderNow(orderId)
    }

    fun holdSellerOrder(orderId: String, reason: String, notes: String, adminApproved: Boolean = true) {
        val ts = nowLogTime()
        val safeReason = reason.trim().ifBlank { "Customer verification pending" }
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    ord.copy(
                        status = OrderStatus.ORDERED,
                        sellerStage = "On Hold",
                        statusText = "On Hold ($safeReason)",
                        holdReason = safeReason,
                        holdDate = ts,
                        holdNotes = notes.trim().ifBlank { "Waiting for confirmation from ${ord.customerName}" },
                        holdExpiryAlert = "Expires in 48 hrs",
                        isAdminApprovedHold = adminApproved,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Placed On Hold ($safeReason)"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "⏸️ Order Placed On Hold (#$orderId)",
            message = "Reason: $safeReason • Customer को संपर्क कर जानकारी कन्फर्म करें।",
            category = "Seller"
        )
        syncOrderNow(orderId)
    }

    fun releaseHoldOrder(orderId: String) {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    val nextStage = if (ord.isOrderConfirmed) "Confirmed" else "Pending"
                    ord.copy(
                        status = OrderStatus.ORDERED,
                        sellerStage = nextStage,
                        statusText = "Hold Released • $nextStage",
                        holdReason = "",
                        holdNotes = "",
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Hold Released -> Returned to $nextStage"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "▶ Hold Released (#$orderId)",
            message = "ऑर्डर #$orderId अब दोबारा चालू (Pending) हो गया है। Confirm करके पैकिंग शुरू करें।",
            category = "Seller"
        )
        syncOrderNow(orderId)
    }

    fun sendHoldOrderReminder(orderId: String) {
        val ord = _orders.value.find { it.orderId == orderId } ?: return
        val ts = nowLogTime()
        _orders.update { list ->
            list.map {
                if (it.orderId == orderId) {
                    it.copy(orderHistoryLog = it.orderHistoryLog + "$ts: Hold Reminder sent to ${it.customerName}")
                } else it
            }
        }
        pushNotification(
            title = "⏰ Hold Order Reminder (#$orderId)",
            message = "Reminder sent for ${ord.customerName} (${ord.holdReason.ifBlank { "Verification pending" }}).",
            category = "Seller"
        )
        syncOrderNow(orderId)
    }

    fun editOrderDetails(
        orderId: String,
        customerName: String,
        phone: String,
        address: String,
        size: String,
        color: String
    ) {
        val current = _orders.value.find { it.orderId == orderId } ?: return
        editHoldFullOrderDetails(
            orderId = orderId,
            customerName = customerName,
            phone = phone,
            address = address,
            city = current.city,
            state = current.state,
            pinCode = current.pinCode,
            size = size,
            color = color,
            customImageUri = current.customProductImageUri
        )
    }

    fun editHoldFullOrderDetails(
        orderId: String,
        customerName: String,
        phone: String,
        address: String,
        city: String,
        state: String,
        pinCode: String,
        size: String,
        color: String,
        customImageUri: String?
    ) {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    ord.copy(
                        customerName = customerName.trim().ifBlank { ord.customerName },
                        customerPhone = phone.trim().ifBlank { ord.customerPhone },
                        deliveryAddress = address.trim().ifBlank { ord.deliveryAddress },
                        city = city.trim().ifBlank { ord.city },
                        state = state.trim().ifBlank { ord.state },
                        pinCode = pinCode.trim().ifBlank { ord.pinCode },
                        selectedSize = size.trim().ifBlank { ord.selectedSize },
                        selectedColor = color.trim().ifBlank { ord.selectedColor },
                        customProductImageUri = customImageUri?.takeIf { it.isNotBlank() } ?: ord.customProductImageUri,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Hold Order updated (Address/City/State/Pin/Size/Color/Photo)"
                    )
                } else ord
            }
        }
        syncOrderNow(orderId)
    }

    fun addHoldProductOrder(
        customerName: String,
        phone: String,
        address: String,
        city: String,
        state: String,
        pinCode: String,
        product: Product,
        size: String,
        color: String,
        quantity: Int,
        holdReason: String,
        holdNotes: String,
        customImageUri: String?
    ) {
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val ts = nowLogTime()
        val newOrderId = "OD" + (1000000000..9999999999).random()
        val cleanCust = customerName.trim().ifBlank { "Gyanvi Customer" }
        val cleanPhone = phone.trim().ifBlank { "+91 9128754982" }
        val cleanAddr = address.trim().ifBlank { "Main Market Road" }
        val cleanCity = city.trim().ifBlank { "Bettiah" }
        val cleanState = state.trim().ifBlank { "Bihar" }
        val cleanPin = pinCode.trim().ifBlank { "845438" }
        val cleanSize = size.trim().ifBlank { "L" }
        val cleanColor = color.trim().ifBlank { "Festive Maroon" }
        val safeQty = quantity.coerceAtLeast(1)
        val totalAmt = product.price * safeQty
        val safeReason = holdReason.trim().ifBlank { "ग्राहक से जानकारी की पुष्टि करनी है" }

        val order = OrderItem(
            orderId = newOrderId,
            product = product,
            orderDate = dateFmt,
            status = OrderStatus.ORDERED,
            statusText = "On Hold ($safeReason)",
            deliveryDateRange = "On Hold (48 hrs limit)",
            selectedSize = cleanSize,
            quantity = safeQty,
            totalAmount = totalAmt,
            trackingStages = listOf(
                TrackingStage("Order Placed", "Order gathered in On Hold (${product.category})", true, "Today"),
                TrackingStage("On Hold", safeReason, false, "Hold"),
                TrackingStage("Processing", "Starts after Hold Release", false, "Pending"),
                TrackingStage("Shipped", "Awaiting Confirmation", false, "Pending")
            ),
            isFeedbackCompleted = false,
            paymentMethod = "Cash on Delivery (COD)",
            paymentStatus = "COD Pending",
            awbNumber = "PENDING-HOLD",
            customerName = cleanCust,
            customerPhone = cleanPhone,
            selectedColor = cleanColor,
            deliveryAddress = cleanAddr,
            city = cleanCity,
            state = cleanState,
            pinCode = cleanPin,
            customProductImageUri = customImageUri?.takeIf { it.isNotBlank() },
            isLabelDownloaded = false,
            sellerStage = "On Hold",
            holdReason = safeReason,
            holdDate = ts,
            holdNotes = holdNotes.trim().ifBlank { "Hold item ready for verification & PDF export" },
            holdExpiryAlert = "Expires in 48 hrs",
            isAdminApprovedHold = true,
            sellerHoldPermission = true,
            orderHistoryLog = listOf(
                "$ts: Added ${product.category} (${product.name}) to On Hold for $cleanCust ($cleanCity, $cleanState - $cleanPin)"
            )
        )
        _orders.update { listOf(order) + it }
        pushNotification(
            title = "⏸️ Added to On Hold (#$newOrderId)",
            message = "${product.category}: ${product.name} • $cleanCust ($cleanCity, $cleanPin) • $safeReason",
            category = "Seller"
        )
        syncOrderNow(newOrderId)
    }

    fun verifySellerOrderProduct(orderId: String, qualityOk: Boolean, sizeColorOk: Boolean, packedOk: Boolean) {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    val ready = qualityOk && sizeColorOk && packedOk
                    val updatedStages = ord.trackingStages.mapIndexed { idx, st ->
                        if (idx <= 1 && ready) st.copy(isCompleted = true) else st
                    }
                    ord.copy(
                        isOrderConfirmed = true,
                        isQualityVerified = qualityOk && sizeColorOk,
                        isPacked = packedOk,
                        sellerStage = if (ready) "Ready to Ship" else ord.sellerStage,
                        statusText = if (ready) "Verified & Packed (Ready to Ship)" else "Quality Check Updated",
                        trackingStages = updatedStages,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Verification (Quality=$qualityOk, Size/Color=$sizeColorOk, Packed=$packedOk)"
                    )
                } else ord
            }
        }
        syncOrderNow(orderId)
    }

    fun generateOrderInvoice(orderId: String) {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    ord.copy(
                        isInvoiceGenerated = true,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Tax Invoice INV-${orderId.takeLast(6)} generated"
                    )
                } else ord
            }
        }
        syncOrderNow(orderId)
    }

    fun generateOrderAwb(orderId: String, courierPartner: String) {
        val ts = nowLogTime()
        val newAwb = "${courierPrefix(courierPartner)}-${(10000000..99999999).random()}"
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    ord.copy(
                        awbNumber = newAwb,
                        courierPartner = courierPartner,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: New AWB $newAwb generated for $courierPartner"
                    )
                } else ord
            }
        }
        syncOrderNow(orderId)
    }

    fun scanOrderBarcode(orderId: String) {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    ord.copy(
                        isBarcodeScanned = true,
                        isPacked = true,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Branded Packet QR & Barcode Scanned (${ord.awbNumber})"
                    )
                } else ord
            }
        }
        syncOrderNow(orderId)
    }

    fun requestCourierPickup(orderId: String, courierPartner: String) {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    val ensuredAwb = if (ord.awbNumber.isBlank() || ord.awbNumber.contains("PENDING", true)) {
                        "${courierPrefix(courierPartner)}-${(10000000..99999999).random()}"
                    } else ord.awbNumber
                    ord.copy(
                        courierPartner = courierPartner,
                        awbNumber = ensuredAwb,
                        pickupStatus = "Pickup Requested",
                        isLabelDownloaded = true,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Pickup Requested with $courierPartner (AWB: $ensuredAwb)"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "Courier Pickup Requested 🚐",
            message = "Pickup scheduled with $courierPartner for Order #$orderId.",
            category = "Seller"
        )
        syncOrderNow(orderId)
    }

    fun bulkRequestCourierPickup(orderIds: List<String>, courierPartner: String) {
        if (orderIds.isEmpty()) return
        val idSet = orderIds.toSet()
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId in idSet && ord.status != OrderStatus.CANCELLED) {
                    val ensuredAwb = if (ord.awbNumber.isBlank() || ord.awbNumber.contains("PENDING", true)) {
                        "${courierPrefix(courierPartner)}-${(10000000..99999999).random()}"
                    } else ord.awbNumber
                    ord.copy(
                        courierPartner = courierPartner,
                        awbNumber = ensuredAwb,
                        pickupStatus = "Pickup Requested",
                        isLabelDownloaded = true,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Bulk Pickup Requested with $courierPartner (AWB: $ensuredAwb)"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "Bulk Courier Pickup Requested 🚐 (${orderIds.size} Orders)",
            message = "${orderIds.size} Ready-to-Ship packets scheduled for $courierPartner pickup.",
            category = "Seller"
        )
        orderIds.forEach { syncOrderNow(it) }
    }

    fun updateReadyToShipDetails(
        orderId: String,
        courierPartner: String,
        awbNumber: String,
        pickupStatus: String,
        shippingCharges: Int,
        expectedDeliveryDate: String,
        customerName: String,
        phone: String,
        address: String,
        city: String,
        state: String,
        pinCode: String,
        size: String,
        color: String,
        customImageUri: String?
    ) {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    val cleanCourier = courierPartner.trim().ifBlank { ord.courierPartner }
                    val cleanAwb = awbNumber.trim().ifBlank { ord.awbNumber }
                    val cleanPickup = pickupStatus.trim().ifBlank { ord.pickupStatus }
                    val cleanCharges = shippingCharges.coerceAtLeast(0)
                    val cleanEta = expectedDeliveryDate.trim().ifBlank { ord.expectedDeliveryDate }
                    ord.copy(
                        courierPartner = cleanCourier,
                        awbNumber = cleanAwb,
                        pickupStatus = cleanPickup,
                        shippingCharges = cleanCharges,
                        expectedDeliveryDate = cleanEta,
                        deliveryDateRange = "Expected Delivery $cleanEta",
                        customerName = customerName.trim().ifBlank { ord.customerName },
                        customerPhone = phone.trim().ifBlank { ord.customerPhone },
                        deliveryAddress = address.trim().ifBlank { ord.deliveryAddress },
                        city = city.trim().ifBlank { ord.city },
                        state = state.trim().ifBlank { ord.state },
                        pinCode = pinCode.trim().ifBlank { ord.pinCode },
                        selectedSize = size.trim().ifBlank { ord.selectedSize },
                        selectedColor = color.trim().ifBlank { ord.selectedColor },
                        customProductImageUri = customImageUri?.takeIf { it.isNotBlank() } ?: ord.customProductImageUri,
                        statusText = "Ready to Ship ($cleanCourier • $cleanPickup)",
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Ready-to-Ship Updated • $cleanCourier • Pickup: $cleanPickup • Charges: ₹$cleanCharges • ETA: $cleanEta • AWB: $cleanAwb"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "📦 Ready-to-Ship Updated (#$orderId)",
            message = "${courierPartner.trim()} • ${pickupStatus.trim()} • Shipping ₹$shippingCharges • ETA ${expectedDeliveryDate.trim()}",
            category = "Seller"
        )
        syncOrderNow(orderId)
    }

    fun addReadyToShipProductOrder(
        customerName: String,
        phone: String,
        address: String,
        city: String,
        state: String,
        pinCode: String,
        product: Product,
        size: String,
        color: String,
        quantity: Int,
        courierPartner: String,
        awbNumber: String,
        shippingCharges: Int,
        expectedDeliveryDate: String,
        customImageUri: String?
    ) {
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val ts = nowLogTime()
        val newOrderId = "OD" + (1000000000..9999999999).random()
        val cleanCourier = courierPartner.trim().ifBlank { "Delhivery Express" }
        val cleanAwb = awbNumber.trim().ifBlank { "${courierPrefix(cleanCourier)}-${(10000000..99999999).random()}" }
        val cleanCust = customerName.trim().ifBlank { "Gyanvi Customer" }
        val cleanPhone = phone.trim().ifBlank { "+91 9128754982" }
        val cleanAddr = address.trim().ifBlank { "Main Market Road" }
        val cleanCity = city.trim().ifBlank { "Bettiah" }
        val cleanState = state.trim().ifBlank { "Bihar" }
        val cleanPin = pinCode.trim().ifBlank { "845438" }
        val cleanSize = size.trim().ifBlank { "38 (M)" }
        val cleanColor = color.trim().ifBlank { "Festive Maroon" }
        val safeQty = quantity.coerceAtLeast(1)
        val safeCharges = shippingCharges.coerceAtLeast(0)
        val cleanEta = expectedDeliveryDate.trim().ifBlank { "22 Nov 2026" }
        val totalAmt = product.price * safeQty

        val order = OrderItem(
            orderId = newOrderId,
            product = product,
            orderDate = dateFmt,
            status = OrderStatus.ORDERED,
            statusText = "Ready to Ship (Packed • $cleanCourier)",
            deliveryDateRange = "Expected Delivery $cleanEta",
            selectedSize = cleanSize,
            quantity = safeQty,
            totalAmount = totalAmt,
            trackingStages = listOf(
                TrackingStage("Order Placed", "Confirmed from $cleanCust ($cleanCity)", true, dateFmt),
                TrackingStage("Processing", "Quality & Size/Colour Verified, Packed in Branded Packet", true, dateFmt),
                TrackingStage("Shipped", "Awaiting Courier Acceptance ($cleanCourier • AWB: $cleanAwb)", false, "Pending"),
                TrackingStage("Delivered", "Delivering to $cleanCity - $cleanPin", false, cleanEta)
            ),
            customerName = cleanCust,
            customerPhone = cleanPhone,
            deliveryAddress = cleanAddr,
            city = cleanCity,
            state = cleanState,
            pinCode = cleanPin,
            selectedColor = cleanColor,
            paymentMethod = "UPI (PhonePe)",
            paymentStatus = "Paid",
            awbNumber = cleanAwb,
            courierPartner = cleanCourier,
            pickupStatus = "Pickup Requested",
            shippingCharges = safeCharges,
            expectedDeliveryDate = cleanEta,
            isOrderConfirmed = true,
            isQualityVerified = true,
            isPacked = true,
            isInvoiceGenerated = true,
            isBarcodeScanned = true,
            isLabelDownloaded = true,
            sellerStage = "Ready to Ship",
            customProductImageUri = customImageUri?.takeIf { it.isNotBlank() },
            orderHistoryLog = listOf(
                "$ts: Packed, Invoice & Label Attached, QR/Barcode Scanned • Ready for $cleanCourier Pickup (AWB: $cleanAwb, Charges: ₹$safeCharges)"
            )
        )
        _orders.update { listOf(order) + it }
        pushNotification(
            title = "📦 New Ready-to-Ship Packet (#$newOrderId)",
            message = "$cleanCust ($cleanCity - $cleanPin) • $cleanCourier • AWB: $cleanAwb • Charges: ₹$safeCharges",
            category = "Seller"
        )
        syncOrderNow(newOrderId)
    }

    fun markSellerOrderReadyToShip(orderId: String) {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    val updatedStages = ord.trackingStages.mapIndexed { idx, st ->
                        if (idx <= 1) st.copy(isCompleted = true) else st
                    }
                    val ensuredAwb = if (ord.awbNumber.isBlank() || ord.awbNumber.contains("PENDING", true)) {
                        "${courierPrefix(ord.courierPartner)}-${(10000000..99999999).random()}"
                    } else ord.awbNumber
                    ord.copy(
                        status = OrderStatus.READY_TO_SHIP,
                        sellerStage = "Ready to Ship",
                        isOrderConfirmed = true,
                        isQualityVerified = true,
                        isPacked = true,
                        isInvoiceGenerated = true,
                        isLabelDownloaded = true,
                        awbNumber = ensuredAwb,
                        statusText = "Ready to Ship (Label & Invoice Generated)",
                        trackingStages = updatedStages,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Confirmed, Quality Checked & Moved to Ready to Ship (AWB: $ensuredAwb)"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "Order Packed & Ready to Ship 📦",
            message = "Order #$orderId label generated. Ready for courier pickup.",
            category = "Seller"
        )
        syncOrderNow(orderId)
    }

    fun dispatchSellerOrder(orderId: String, courierName: String = "Delhivery Express") {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId && ord.status != OrderStatus.CANCELLED) {
                    val updatedStages = ord.trackingStages.mapIndexed { idx, st ->
                        if (idx <= 2) st.copy(isCompleted = true) else st
                    }
                    val ensuredAwb = if (ord.awbNumber.isBlank() || ord.awbNumber.contains("PENDING", true)) {
                        "${courierPrefix(courierName)}-${(10000000..99999999).random()}"
                    } else ord.awbNumber
                    ord.copy(
                        status = OrderStatus.SHIPPED,
                        sellerStage = "Shipped",
                        courierPartner = courierName,
                        awbNumber = ensuredAwb,
                        pickupStatus = "Picked Up",
                        isOrderConfirmed = true,
                        isQualityVerified = true,
                        isPacked = true,
                        isLabelDownloaded = true,
                        statusText = "Shipped via $courierName",
                        currentTrackingLocation = "Dispatched • $courierName Origin Hub",
                        expectedDeliveryDate = "Within 2-3 Days",
                        trackingStages = updatedStages,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Handed over to $courierName (Shipped • AWB: $ensuredAwb)"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "Order Dispatched! 🚚 (#$orderId)",
            message = "Handed over to $courierName. Customer SMS/Notification sent.",
            category = "Orders"
        )
        syncOrderNow(orderId)
    }

    fun bulkDispatchReadyOrders(orderIds: List<String>, courierName: String) {
        if (orderIds.isEmpty()) return
        val idSet = orderIds.toSet()
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId in idSet && ord.status != OrderStatus.CANCELLED) {
                    val updatedStages = ord.trackingStages.mapIndexed { idx, st ->
                        if (idx <= 2) st.copy(isCompleted = true) else st
                    }
                    ord.copy(
                        status = OrderStatus.SHIPPED,
                        sellerStage = "Shipped",
                        courierPartner = courierName,
                        pickupStatus = "Picked Up",
                        isOrderConfirmed = true,
                        isQualityVerified = true,
                        isPacked = true,
                        isLabelDownloaded = true,
                        statusText = "Bulk Shipped via $courierName",
                        currentTrackingLocation = "Dispatched in Bulk • $courierName Hub",
                        trackingStages = updatedStages,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Bulk Dispatched via $courierName"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "Bulk Dispatch Completed 🚚 (${orderIds.size} Orders)",
            message = "${orderIds.size} Ready-to-Ship orders handed to $courierName.",
            category = "Seller"
        )
        orderIds.forEach { syncOrderNow(it) }
    }

    fun advanceShipmentTracking(orderId: String) {
        val ts = nowLogTime()
        val shortDate = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
        var notifTitle = ""
        var notifMsg = ""
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    when {
                        ord.status == OrderStatus.SHIPPED && !ord.sellerStage.equals("Out for Delivery", true) -> {
                            notifTitle = "Auto-Tracking: Out for Delivery 🛵"
                            notifMsg = "Order #$orderId (${ord.customerName}) is now Out for Delivery via ${ord.courierPartner}! Customer & Admin synced."
                            val liveLoc = "Out for Delivery • Near ${ord.deliveryAddress.take(28)}, ${ord.city}"
                            val hasOutStage = ord.trackingStages.any { it.title.contains("Out for Delivery", true) }
                            val updatedStages = if (hasOutStage) {
                                ord.trackingStages.map { st ->
                                    when {
                                        st.title.contains("Out for Delivery", true) -> st.copy(
                                            subtitle = "With ${ord.courierPartner} Delivery Agent ($liveLoc)",
                                            isCompleted = true,
                                            dateStr = shortDate
                                        )
                                        st.title.equals("Delivery", true) || st.title.contains("Delivered", true) -> st
                                        else -> st.copy(isCompleted = true)
                                    }
                                }
                            } else {
                                val outStage = TrackingStage(
                                    title = "Out for Delivery",
                                    subtitle = "With ${ord.courierPartner} Delivery Agent",
                                    isCompleted = true,
                                    dateStr = shortDate
                                )
                                val nonFinalStages = ord.trackingStages
                                    .filterNot { it.title.equals("Delivery", true) || it.title.contains("Delivered", true) }
                                    .map { it.copy(isCompleted = true) }
                                val finalStages = ord.trackingStages
                                    .filter { it.title.equals("Delivery", true) || it.title.contains("Delivered", true) }
                                nonFinalStages + outStage + finalStages
                            }
                            ord.copy(
                                status = OrderStatus.OUT_FOR_DELIVERY,
                                sellerStage = "Out for Delivery",
                                statusText = "Out for Delivery (${ord.courierPartner})",
                                currentTrackingLocation = liveLoc,
                                isDeliveryDelayed = false,
                                trackingStages = updatedStages,
                                orderHistoryLog = ord.orderHistoryLog + "$ts: Live Tracking Update -> Out for Delivery ($liveLoc)"
                            )
                        }
                        ord.status == OrderStatus.OUT_FOR_DELIVERY || (ord.status == OrderStatus.SHIPPED && ord.sellerStage.equals("Out for Delivery", true)) -> {
                            notifTitle = "Delivered Successfully! ✅"
                            notifMsg = "Order #$orderId delivered to ${ord.customerName}. POD Verified & synced to Customer & Admin."
                            val hasDeliveredStage = ord.trackingStages.any { it.title.contains("Delivered", true) }
                            val doneStages = (if (hasDeliveredStage) {
                                ord.trackingStages
                            } else {
                                ord.trackingStages + TrackingStage(
                                    title = "Delivered",
                                    subtitle = "Delivered to ${ord.customerName}",
                                    isCompleted = true,
                                    dateStr = shortDate
                                )
                            }).map { it.copy(isCompleted = true) }
                            ord.copy(
                                status = OrderStatus.DELIVERED,
                                sellerStage = "Delivered",
                                statusText = "Delivered to Customer",
                                deliveryDateRange = "Delivered on $shortDate",
                                paymentStatus = if (ord.paymentMethod.contains("COD", true)) "COD Collected" else ord.paymentStatus,
                                currentTrackingLocation = "Delivered at Customer Doorstep (${ord.city})",
                                isDeliveryDelayed = false,
                                trackingStages = doneStages,
                                orderHistoryLog = ord.orderHistoryLog + "$ts: Delivered Successfully to ${ord.customerName}"
                            )
                        }
                        else -> ord
                    }
                } else ord
            }
        }
        if (notifTitle.isNotBlank()) {
            pushNotification(title = notifTitle, message = notifMsg, category = "Orders")
        }
        syncOrderNow(orderId)
    }

    fun syncLiveCourierTracking() {
        val ts = nowLogTime()
        val updatedIds = mutableListOf<String>()
        _orders.update { list ->
            list.map { ord ->
                if (ord.status == OrderStatus.SHIPPED && !ord.isRtoFlagged) {
                    updatedIds.add(ord.orderId)
                    val refreshedLoc = if (ord.sellerStage.equals("Out for Delivery", true)) {
                        "Out for Delivery • Agent 2 km from ${ord.city} (${ord.pinCode}) [Synced $ts]"
                    } else if (ord.isDeliveryDelayed) {
                        "${ord.courierPartner} ${ord.city} Hub • Priority Escalation [Synced $ts]"
                    } else {
                        "${ord.courierPartner} ${ord.city} Dispatch Hub • Live Scan [Synced $ts]"
                    }
                    ord.copy(
                        currentTrackingLocation = refreshedLoc,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Automatic Courier Sync -> $refreshedLoc"
                    )
                } else ord
            }
        }
        if (updatedIds.isNotEmpty() && _autoTrackingEnabled.value) {
            pushNotification(
                title = "📡 Live Courier Sync (${updatedIds.size} Shipments)",
                message = "Automatic tracking refreshed across Customer & Admin dashboards.",
                category = "Orders"
            )
        }
        updatedIds.forEach { syncOrderNow(it) }
    }

    fun toggleDeliveryDelayAlert(orderId: String, delayed: Boolean, note: String = "") {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    val msg = note.ifBlank { "Courier route congestion delay" }
                    ord.copy(
                        isDeliveryDelayed = delayed,
                        currentTrackingLocation = if (delayed) "Delayed Alert: $msg" else "Back on Schedule (${ord.courierPartner})",
                        orderHistoryLog = ord.orderHistoryLog + "$ts: ${if (delayed) "Delay Alert Raised ($msg)" else "Delay Cleared"}"
                    )
                } else ord
            }
        }
        if (delayed) {
            pushNotification(
                title = "⚠️ Delayed Delivery Alert (#$orderId)",
                message = "Shipment delayed: ${note.ifBlank { "Courier transit delay" }}. Customer notified.",
                category = "Seller"
            )
        }
        syncOrderNow(orderId)
    }

    fun markOrderRto(orderId: String, reason: String) {
        val ts = nowLogTime()
        val safeReason = reason.trim().ifBlank { "Failed Delivery Attempt / Customer Unreachable" }
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    ord.copy(
                        isRtoFlagged = true,
                        rtoReason = safeReason,
                        sellerStage = "RTO Initiated",
                        statusText = "RTO In Progress ($safeReason)",
                        currentTrackingLocation = "Returning to Seller Hub (RTO Tracked)",
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Separate Courier RTO Initiated ($safeReason)"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "↩️ RTO Return Tracked (#$orderId)",
            message = "Already-shipped order marked under RTO process: $safeReason",
            category = "Seller"
        )
        syncOrderNow(orderId)
    }

    fun toggleAutoTracking(enabled: Boolean) {
        _autoTrackingEnabled.value = enabled
        pushNotification(
            title = if (enabled) "📡 Automatic Tracking System ON" else "Automatic Tracking Paused",
            message = if (enabled) "Customer & Admin now receive live automatic shipment status updates."
            else "Automatic customer/admin tracking updates paused.",
            category = "Seller"
        )
    }

    fun toggleAutoRefundTracking(enabled: Boolean) {
        _autoRefundTrackingEnabled.value = enabled
        pushNotification(
            title = if (enabled) "💳 Auto Refund Tracking ON" else "Auto Refund Tracking Paused",
            message = if (enabled) "Prepaid cancelled orders automatically track and initiate instant refund status."
            else "Automatic refund status initiation paused.",
            category = "Wallet"
        )
    }

    fun updateShippedAwbAndDetails(
        orderId: String,
        awbNumber: String,
        courierPartner: String,
        trackingLocation: String,
        expectedDelivery: String,
        customerName: String,
        phone: String,
        address: String,
        city: String,
        state: String,
        pinCode: String,
        size: String,
        color: String,
        customImageUri: String?
    ) {
        val ts = nowLogTime()
        val cleanAwb = awbNumber.trim().ifBlank { "${courierPrefix(courierPartner)}-${(10000000..99999999).random()}" }
        val cleanCourier = courierPartner.trim().ifBlank { "Delhivery Express" }
        val cleanLoc = trackingLocation.trim().ifBlank { "$cleanCourier Sort Hub" }
        val cleanEta = expectedDelivery.trim().ifBlank { "Within 2 Days" }
        val cleanCity = city.trim().ifBlank { "Bettiah" }
        val cleanState = state.trim().ifBlank { "Bihar" }
        val cleanPin = pinCode.trim().ifBlank { "845438" }

        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    val cleanImg = customImageUri?.takeIf { it.isNotBlank() } ?: ord.customProductImageUri
                    ord.copy(
                        awbNumber = cleanAwb,
                        courierPartner = cleanCourier,
                        currentTrackingLocation = cleanLoc,
                        expectedDeliveryDate = cleanEta,
                        deliveryDateRange = "Expected Delivery: $cleanEta",
                        customerName = customerName.trim().ifBlank { ord.customerName },
                        customerPhone = phone.trim().ifBlank { ord.customerPhone },
                        deliveryAddress = address.trim().ifBlank { ord.deliveryAddress },
                        city = cleanCity,
                        state = cleanState,
                        pinCode = cleanPin,
                        selectedSize = size.trim().ifBlank { ord.selectedSize },
                        selectedColor = color.trim().ifBlank { ord.selectedColor },
                        customProductImageUri = cleanImg,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: AWB updated to $cleanAwb ($cleanCourier) • Loc: $cleanLoc • ETA: $cleanEta"
                    )
                } else ord
            }
        }
        if (_autoTrackingEnabled.value) {
            pushNotification(
                title = "📦 AWB & Tracking Updated (#$orderId)",
                message = "New AWB: $cleanAwb ($cleanCourier) • Expected: $cleanEta • Synced to Customer & Admin.",
                category = "Orders"
            )
        }
        syncOrderNow(orderId)
    }

    fun addShippedCourierOrder(
        customerName: String,
        phone: String,
        address: String,
        city: String,
        state: String,
        pinCode: String,
        product: Product,
        size: String,
        color: String,
        quantity: Int,
        courierPartner: String,
        awbNumber: String,
        expectedDelivery: String,
        customImageUri: String?
    ) {
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val ts = nowLogTime()
        val cleanCourier = courierPartner.trim().ifBlank { "Delhivery Express" }
        val cleanAwb = awbNumber.trim().ifBlank { "${courierPrefix(cleanCourier)}-${(10000000..99999999).random()}" }
        val newOrderId = "OD" + (1000000000..9999999999).random()
        val cleanCust = customerName.trim().ifBlank { "Gyanvi Customer" }
        val cleanPhone = phone.trim().ifBlank { "+91 9128754982" }
        val cleanCity = city.trim().ifBlank { "Bettiah" }
        val cleanState = state.trim().ifBlank { "Bihar" }
        val cleanPin = pinCode.trim().ifBlank { "845438" }
        val cleanAddr = address.trim().ifBlank { "Main Market Road" }
        val cleanEta = expectedDelivery.trim().ifBlank { "Tomorrow, 7:00 PM" }
        val safeQty = quantity.coerceAtLeast(1)

        val order = OrderItem(
            orderId = newOrderId,
            product = product,
            orderDate = dateFmt,
            status = OrderStatus.SHIPPED,
            statusText = "Shipped via $cleanCourier",
            deliveryDateRange = "Expected Delivery: $cleanEta",
            selectedSize = size.trim().ifBlank { "38 (M)" },
            quantity = safeQty,
            totalAmount = product.price * safeQty,
            trackingStages = listOf(
                TrackingStage("Order Placed", "Confirmed Order", true, dateFmt),
                TrackingStage("Packed & Verified", "Quality, Size & Colour checked", true, dateFmt),
                TrackingStage("Shipped", "Handed to $cleanCourier (AWB: $cleanAwb)", true, dateFmt),
                TrackingStage("Out for Delivery", "Heading to $cleanCity - $cleanPin", false, cleanEta)
            ),
            customerName = cleanCust,
            customerPhone = cleanPhone,
            deliveryAddress = cleanAddr,
            city = cleanCity,
            state = cleanState,
            pinCode = cleanPin,
            selectedColor = color.trim().ifBlank { "Festive Maroon" },
            paymentMethod = "UPI (PhonePe)",
            paymentStatus = "Paid",
            awbNumber = cleanAwb,
            courierPartner = cleanCourier,
            pickupStatus = "Picked Up",
            expectedDeliveryDate = cleanEta,
            currentTrackingLocation = "Dispatched • $cleanCourier Hub ($cleanCity)",
            isOrderConfirmed = true,
            isQualityVerified = true,
            isPacked = true,
            isInvoiceGenerated = true,
            isBarcodeScanned = true,
            isLabelDownloaded = true,
            sellerStage = "Shipped",
            customProductImageUri = customImageUri?.takeIf { it.isNotBlank() },
            orderHistoryLog = listOf(
                "$ts: Dispatched via $cleanCourier • AWB: $cleanAwb • Courier PDF Ready"
            )
        )
        _orders.update { listOf(order) + it }
        if (_autoTrackingEnabled.value) {
            pushNotification(
                title = "🚚 Shipped via $cleanCourier (#$newOrderId)",
                message = "AWB $cleanAwb active for $cleanCust ($cleanCity - $cleanPin). Customer & Admin notified.",
                category = "Orders"
            )
        }
        syncOrderNow(newOrderId)
    }

    fun sendCustomerTrackingAlert(orderId: String) {
        val ts = nowLogTime()
        val ord = _orders.value.find { it.orderId == orderId } ?: return
        _orders.update { list ->
            list.map {
                if (it.orderId == orderId) {
                    it.copy(
                        orderHistoryLog = it.orderHistoryLog + "$ts: SMS/Notification sent to ${it.customerName} (${it.customerPhone}) • AWB ${it.awbNumber}"
                    )
                } else it
            }
        }
        pushNotification(
            title = "📲 Customer SMS & Tracking Alert Sent",
            message = "Sent AWB ${ord.awbNumber} (${ord.courierPartner}) ETA ${ord.expectedDeliveryDate} to ${ord.customerPhone}.",
            category = "Orders"
        )
        syncOrderNow(orderId)
    }

    fun logFailedDeliveryAttempt(orderId: String, reason: String) {
        val ts = nowLogTime()
        val safeReason = reason.trim().ifBlank { "Customer Not Available / Address Unreachable" }
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    ord.copy(
                        isDeliveryDelayed = true,
                        statusText = "Failed Delivery Attempt ($safeReason)",
                        currentTrackingLocation = "Failed Attempt: $safeReason (Re-attempt Scheduled)",
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Failed Delivery Attempt ($safeReason)"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "⚠️ Failed Delivery Management (#$orderId)",
            message = "Reason: $safeReason. Reschedule or initiate RTO Tracking.",
            category = "Seller"
        )
        syncOrderNow(orderId)
    }

    fun markCourierPdfSent(orderIds: List<String>, courierPartner: String) {
        if (orderIds.isEmpty()) return
        val idSet = orderIds.toSet()
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId in idSet) {
                    ord.copy(
                        isLabelDownloaded = true,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Courier Dispatch PDF sent to ${ord.courierPartner.ifBlank { courierPartner }}"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "📄 Courier PDF Sent (${orderIds.size} Shipment)",
            message = "Product, Customer Address, Pincode, Size/Colour & AWB Barcode PDF dispatched to $courierPartner.",
            category = "Seller"
        )
        orderIds.forEach { syncOrderNow(it) }
    }

    fun markOrderLabelDownloaded(orderId: String) {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    ord.copy(
                        isLabelDownloaded = true,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: QR Shipping Label downloaded/saved"
                    )
                } else ord
            }
        }
        syncOrderNow(orderId)
    }

    fun cancelSellerOrder(orderId: String, reason: String = "Cancelled by Seller") {
        cancelSellerOrderDetailed(
            orderId = orderId,
            reason = reason,
            cancelledBy = "Seller",
            restoreStock = true
        )
    }

    fun cancelSellerOrderDetailed(
        orderId: String,
        reason: String,
        cancelledBy: String,
        restoreStock: Boolean
    ) {
        val ts = nowLogTime()
        val dateOnly = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val targetOrder = _orders.value.find { it.orderId == orderId } ?: return
        // Cancellation Policy: Shipped/Delivered orders go via separate RTO process
        if ((targetOrder.status == OrderStatus.SHIPPED || targetOrder.status == OrderStatus.DELIVERED) && !targetOrder.isRtoFlagged) {
            markOrderRto(
                orderId = orderId,
                reason = "RTO Required (Shipped Order Cancel Request by $cancelledBy): ${reason.trim()}"
            )
            return
        }
        val isPrepaid = !targetOrder.paymentMethod.contains("COD", ignoreCase = true)
        val refundAmt = if (isPrepaid) targetOrder.totalAmount else 0
        val autoInitiateRefund = refundAmt > 0 && _autoRefundTrackingEnabled.value
        val canRestoreStock = restoreStock && targetOrder.status != OrderStatus.SHIPPED && targetOrder.status != OrderStatus.DELIVERED

        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    val nextRefundStatus = when {
                        refundAmt <= 0 -> "Not Applicable (COD)"
                        autoInitiateRefund -> "Initiated"
                        else -> "Pending Initiation"
                    }
                    val updatedStages = ord.trackingStages + TrackingStage(
                        title = "Cancelled",
                        subtitle = "Cancelled by $cancelledBy • $nextRefundStatus",
                        isCompleted = true,
                        dateStr = dateOnly
                    )
                    ord.copy(
                        status = OrderStatus.CANCELLED,
                        sellerStage = "Cancelled",
                        statusText = "Order Cancelled ($reason)",
                        deliveryDateRange = "Cancelled on $dateOnly",
                        cancelReason = reason,
                        cancelledBy = cancelledBy,
                        cancellationDate = dateOnly,
                        refundAmount = refundAmt,
                        refundStatus = nextRefundStatus,
                        paymentStatus = if (refundAmt > 0) "Refund $nextRefundStatus" else "COD Cancelled",
                        isStockRestored = canRestoreStock,
                        trackingStages = updatedStages,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Cancelled by $cancelledBy ($reason)" +
                            (if (canRestoreStock) " • Stock Restored" else "") +
                            (if (refundAmt > 0) " • Refund ₹$refundAmt $nextRefundStatus" else "")
                    )
                } else ord
            }
        }
        if (canRestoreStock) {
            _products.update { prods ->
                prods.map { p -> if (p.id == targetOrder.product.id) p.copy(inStock = true) else p }
            }
        }
        if (refundAmt > 0) {
            _walletBalance.update { it + refundAmt }
        }
        pushNotification(
            title = "Order Cancelled ($cancelledBy) • #$orderId",
            message = "Reason: $reason" + if (refundAmt > 0) " • Auto Refund ₹$refundAmt tracked." else " • Stock updated.",
            category = "Seller"
        )
        syncOrderNow(orderId)
    }

    fun editCancelledFullDetails(
        orderId: String,
        awbNumber: String,
        courierPartner: String,
        customerName: String,
        phone: String,
        address: String,
        city: String,
        state: String,
        pinCode: String,
        customerAgeYears: String,
        size: String,
        color: String,
        cancelReason: String,
        cancelledBy: String,
        refundStatus: String,
        refundAmount: Int,
        restoreStock: Boolean,
        customImageUri: String?
    ) {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    val cleanAwb = awbNumber.trim().ifBlank { ord.awbNumber }
                    val cleanCourier = courierPartner.trim().ifBlank { ord.courierPartner }
                    val cleanAge = customerAgeYears.trim().ifBlank { ord.customerAgeYears }
                    val cleanReason = cancelReason.trim().ifBlank { ord.cancelReason.ifBlank { "Customer Request" } }
                    val cleanCancelledBy = cancelledBy.trim().ifBlank { ord.cancelledBy.ifBlank { "Customer" } }
                    val cleanRefundStatus = refundStatus.trim().ifBlank { ord.refundStatus }
                    val safeRefundAmt = refundAmount.coerceAtLeast(0)
                    ord.copy(
                        awbNumber = cleanAwb,
                        courierPartner = cleanCourier,
                        customerName = customerName.trim().ifBlank { ord.customerName },
                        customerPhone = phone.trim().ifBlank { ord.customerPhone },
                        deliveryAddress = address.trim().ifBlank { ord.deliveryAddress },
                        city = city.trim().ifBlank { ord.city },
                        state = state.trim().ifBlank { ord.state },
                        pinCode = pinCode.trim().ifBlank { ord.pinCode },
                        customerAgeYears = cleanAge,
                        selectedSize = size.trim().ifBlank { ord.selectedSize },
                        selectedColor = color.trim().ifBlank { ord.selectedColor },
                        cancelReason = cleanReason,
                        cancelledBy = cleanCancelledBy,
                        refundStatus = cleanRefundStatus,
                        refundAmount = safeRefundAmt,
                        isStockRestored = restoreStock || ord.isStockRestored,
                        customProductImageUri = customImageUri?.takeIf { it.isNotBlank() } ?: ord.customProductImageUri,
                        statusText = "Order Cancelled ($cleanReason)",
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Cancelled Record Updated • Age: $cleanAge • AWB: $cleanAwb • Refund: $cleanRefundStatus (₹$safeRefundAmt)"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "📝 Cancelled Order & AWB Updated (#$orderId)",
            message = "Updated AWB ${awbNumber.trim()}, Age/Years ${customerAgeYears.trim()}, Address & Refund tracking.",
            category = "Seller"
        )
        syncOrderNow(orderId)
    }

    fun addCancelledRecordOrder(
        customerName: String,
        phone: String,
        address: String,
        city: String,
        state: String,
        pinCode: String,
        customerAgeYears: String,
        product: Product,
        size: String,
        color: String,
        quantity: Int,
        awbNumber: String,
        courierPartner: String,
        cancelReason: String,
        cancelledBy: String,
        restoreStock: Boolean,
        customImageUri: String?
    ) {
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val ts = nowLogTime()
        val newOrderId = "OD" + (1000000000..9999999999).random()
        val cleanCourier = courierPartner.trim().ifBlank { "Delhivery Express" }
        val cleanAwb = awbNumber.trim().ifBlank { "${courierPrefix(cleanCourier)}-${(10000000..99999999).random()}" }
        val cleanCust = customerName.trim().ifBlank { "Gyanvi Customer" }
        val cleanPhone = phone.trim().ifBlank { "+91 9128754982" }
        val cleanAddr = address.trim().ifBlank { "Main Market Road" }
        val cleanCity = city.trim().ifBlank { "Bettiah" }
        val cleanState = state.trim().ifBlank { "Bihar" }
        val cleanPin = pinCode.trim().ifBlank { "845438" }
        val cleanAge = customerAgeYears.trim().ifBlank { "26 Years" }
        val cleanSize = size.trim().ifBlank { "38 (M)" }
        val cleanColor = color.trim().ifBlank { "Festive Maroon" }
        val cleanReason = cancelReason.trim().ifBlank { "ग्राहक ने खुद ऑर्डर कैंसिल किया" }
        val cleanBy = cancelledBy.trim().ifBlank { "Customer" }
        val safeQty = quantity.coerceAtLeast(1)
        val totalAmt = product.price * safeQty
        val refundState = if (_autoRefundTrackingEnabled.value) "Initiated" else "Pending Initiation"

        val order = OrderItem(
            orderId = newOrderId,
            product = product,
            orderDate = dateFmt,
            status = OrderStatus.CANCELLED,
            statusText = "Order Cancelled ($cleanReason)",
            deliveryDateRange = "Cancelled on $dateFmt",
            selectedSize = cleanSize,
            quantity = safeQty,
            totalAmount = totalAmt,
            trackingStages = listOf(
                TrackingStage("Order Placed", "Received from $cleanCust ($cleanCity)", true, dateFmt),
                TrackingStage("Cancelled", "Cancelled by $cleanBy • $cleanReason", true, dateFmt)
            ),
            customerName = cleanCust,
            customerPhone = cleanPhone,
            deliveryAddress = cleanAddr,
            city = cleanCity,
            state = cleanState,
            pinCode = cleanPin,
            customerAgeYears = cleanAge,
            selectedColor = cleanColor,
            paymentMethod = "UPI (PhonePe)",
            paymentStatus = "Refund $refundState",
            awbNumber = cleanAwb,
            courierPartner = cleanCourier,
            sellerStage = "Cancelled",
            cancelReason = cleanReason,
            cancelledBy = cleanBy,
            cancellationDate = dateFmt,
            refundStatus = refundState,
            refundAmount = totalAmt,
            isStockRestored = restoreStock,
            customProductImageUri = customImageUri?.takeIf { it.isNotBlank() },
            orderHistoryLog = listOf(
                "$ts: Cancelled Order Recorded by $cleanBy • Age/Years: $cleanAge • AWB: $cleanAwb • Reason: $cleanReason • Refund ₹$totalAmt $refundState"
            )
        )
        _orders.update { listOf(order) + it }
        if (restoreStock) {
            _products.update { prods ->
                prods.map { p -> if (p.id == product.id) p.copy(inStock = true) else p }
            }
        }
        pushNotification(
            title = "❌ Cancelled Order Logged (#$newOrderId)",
            message = "$cleanCust ($cleanAge, $cleanCity - $cleanPin) • Cancelled by $cleanBy • AWB: $cleanAwb",
            category = "Seller"
        )
        syncOrderNow(newOrderId)
    }

    fun initiateOrderRefund(orderId: String) {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    val amt = if (ord.refundAmount > 0) ord.refundAmount else ord.totalAmount
                    ord.copy(
                        refundAmount = amt,
                        refundStatus = "Initiated",
                        paymentStatus = "Refund Initiated",
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Refund of ₹$amt Initiated"
                    )
                } else ord
            }
        }
        syncOrderNow(orderId)
    }

    fun completeOrderRefund(orderId: String) {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    val amt = if (ord.refundAmount > 0) ord.refundAmount else ord.totalAmount
                    ord.copy(
                        status = OrderStatus.REFUNDED,
                        sellerStage = "Refunded",
                        statusText = "Refunded (₹$amt Credited)",
                        refundAmount = amt,
                        refundStatus = "Completed",
                        paymentStatus = "Refund Completed",
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Refund of ₹$amt Completed to Customer"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "Refund Completed 💸 (#$orderId)",
            message = "ग्राहक को रिफंड सफलतापूर्वक पूरा कर दिया गया है।",
            category = "Wallet"
        )
        syncOrderNow(orderId)
    }

    fun restoreOrderStock(orderId: String) {
        val ts = nowLogTime()
        val ord = _orders.value.find { it.orderId == orderId } ?: return
        if (ord.status == OrderStatus.SHIPPED || ord.status == OrderStatus.DELIVERED) return
        _orders.update { list ->
            list.map {
                if (it.orderId == orderId) {
                    it.copy(
                        isStockRestored = true,
                        orderHistoryLog = it.orderHistoryLog + "$ts: Product Stock Restored (+${it.quantity} qty)"
                    )
                } else it
            }
        }
        _products.update { prods ->
            prods.map { p -> if (p.id == ord.product.id) p.copy(inStock = true) else p }
        }
        syncOrderNow(orderId)
    }

    fun createSellerManualOrder(
        customerName: String,
        address: String,
        product: Product,
        size: String,
        quantity: Int,
        paymentMethod: String
    ) {
        createSellerManualOrder(
            customerName = customerName,
            phone = "+91 9128754982",
            address = address,
            product = product,
            size = size,
            color = "Festive Maroon",
            quantity = quantity,
            paymentMethod = paymentMethod
        )
    }

    fun createSellerManualOrder(
        customerName: String,
        phone: String,
        address: String,
        product: Product,
        size: String,
        color: String,
        quantity: Int,
        paymentMethod: String
    ) {
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val ts = nowLogTime()
        val awb = "DLV-" + (10000000..99999999).random()
        val newOrderId = "OD" + (1000000000..9999999999).random()
        val cleanCust = customerName.trim().ifBlank { "Walk-in Customer" }
        val cleanPhone = phone.trim().ifBlank { "+91 9128754982" }
        val cleanAddr = address.trim().ifBlank { "Bettiah, Bihar - 845438" }
        val cleanColor = color.trim().ifBlank { "Festive Maroon" }
        val safeQty = quantity.coerceAtLeast(1)
        val totalAmt = product.price * safeQty
        val isCod = paymentMethod.contains("COD", ignoreCase = true)
        val order = OrderItem(
            orderId = newOrderId,
            product = product,
            orderDate = dateFmt,
            status = OrderStatus.ORDERED,
            statusText = "Pending Confirmation ($paymentMethod)",
            deliveryDateRange = "Dispatch by Tomorrow",
            selectedSize = size.ifBlank { "L" },
            quantity = safeQty,
            totalAmount = totalAmt,
            trackingStages = listOf(
                TrackingStage("Order Placed", "New Order ($paymentMethod)", true, "Today"),
                TrackingStage("Processing", "Quality check & QR packing", false, "Tomorrow"),
                TrackingStage("Shipped", "Courier: Delhivery • AWB: $awb", false, "In 2 days"),
                TrackingStage("Delivery", "Delivering to $cleanAddr", false, "In 4 days")
            ),
            isFeedbackCompleted = false,
            paymentMethod = paymentMethod,
            paymentStatus = if (isCod) "COD Pending" else "Paid",
            awbNumber = awb,
            customerName = cleanCust,
            customerPhone = cleanPhone,
            selectedColor = cleanColor,
            deliveryAddress = cleanAddr,
            isLabelDownloaded = false,
            sellerStage = "Pending",
            orderHistoryLog = listOf(
                "$ts: New Pending Order created for $cleanCust ($cleanPhone) • ₹$totalAmt"
            )
        )
        _orders.update { listOf(order) + it }
        if (_autoPendingNotificationEnabled.value) {
            pushNotification(
                title = "🔔 New Pending Order (#$newOrderId)!",
                message = "$cleanCust ordered ${product.name} (Size ${order.selectedSize}, ₹$totalAmt). Confirm now!",
                category = "Seller"
            )
        }
        syncOrderNow(newOrderId)
    }

    fun addPendingProductOrder(
        customerName: String,
        phone: String,
        address: String,
        city: String,
        state: String,
        pinCode: String,
        product: Product,
        size: String,
        color: String,
        quantity: Int,
        paymentMethod: String,
        customImageUri: String?
    ) {
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val ts = nowLogTime()
        val awb = "DLV-" + (10000000..99999999).random()
        val newOrderId = "OD" + (1000000000..9999999999).random()
        val cleanCust = customerName.trim().ifBlank { "Gyanvi Customer" }
        val cleanPhone = phone.trim().ifBlank { "+91 9128754982" }
        val cleanAddr = address.trim().ifBlank { "Main Market Road" }
        val cleanCity = city.trim().ifBlank { "Bettiah" }
        val cleanState = state.trim().ifBlank { "Bihar" }
        val cleanPin = pinCode.trim().ifBlank { "845438" }
        val cleanSize = size.trim().ifBlank { "38 (M)" }
        val cleanColor = color.trim().ifBlank { "Festive Maroon" }
        val safeQty = quantity.coerceAtLeast(1)
        val totalAmt = product.price * safeQty
        val isCod = paymentMethod.contains("COD", ignoreCase = true)
        val order = OrderItem(
            orderId = newOrderId,
            product = product,
            orderDate = dateFmt,
            status = OrderStatus.ORDERED,
            statusText = "Pending Confirmation ($paymentMethod)",
            deliveryDateRange = "Dispatch by Tomorrow",
            selectedSize = cleanSize,
            quantity = safeQty,
            totalAmount = totalAmt,
            trackingStages = listOf(
                TrackingStage("Order Placed", "New Order ($paymentMethod)", true, "Today"),
                TrackingStage("Processing", "Quality check & barcode packing", false, "Tomorrow"),
                TrackingStage("Shipped", "Courier: Delhivery • AWB: $awb", false, "In 2 days"),
                TrackingStage("Delivery", "Delivering to $cleanCity, $cleanState - $cleanPin", false, "In 4 days")
            ),
            isFeedbackCompleted = false,
            paymentMethod = paymentMethod,
            paymentStatus = if (isCod) "COD Pending" else "Paid",
            awbNumber = awb,
            customerName = cleanCust,
            customerPhone = cleanPhone,
            selectedColor = cleanColor,
            deliveryAddress = cleanAddr,
            city = cleanCity,
            state = cleanState,
            pinCode = cleanPin,
            customProductImageUri = customImageUri?.takeIf { it.isNotBlank() },
            isLabelDownloaded = false,
            sellerStage = "Pending",
            orderHistoryLog = listOf(
                "$ts: New Pending ${product.category} (${product.name}) order created for $cleanCust ($cleanCity, $cleanState - $cleanPin) • ₹$totalAmt"
            )
        )
        _orders.update { listOf(order) + it }
        if (_autoPendingNotificationEnabled.value) {
            pushNotification(
                title = "🔔 New Pending Order (#$newOrderId)!",
                message = "$cleanCust ordered ${product.name} (${product.category}, Size $cleanSize, ₹$totalAmt). Confirm now!",
                category = "Seller"
            )
        }
        syncOrderNow(newOrderId)
    }

    fun buildSellerOrderActions(): SellerOrderActions = SellerOrderActions(
        autoPendingNotificationEnabled = _autoPendingNotificationEnabled.value,
        onToggleAutoNotification = ::toggleAutoPendingNotification,
        autoTrackingEnabled = _autoTrackingEnabled.value,
        onToggleAutoTracking = ::toggleAutoTracking,
        autoRefundTrackingEnabled = _autoRefundTrackingEnabled.value,
        onToggleAutoRefundTracking = ::toggleAutoRefundTracking,
        onConfirmOrder = ::confirmSellerOrder,
        onHoldOrder = ::holdSellerOrder,
        onReleaseHold = ::releaseHoldOrder,
        onSendHoldReminder = ::sendHoldOrderReminder,
        onEditOrderDetails = ::editOrderDetails,
        onEditHoldFullOrderDetails = ::editHoldFullOrderDetails,
        onEditCancelledFullDetails = ::editCancelledFullDetails,
        onAddHoldProductOrder = ::addHoldProductOrder,
        onAddPendingProductOrder = ::addPendingProductOrder,
        onAddShippedCourierOrder = ::addShippedCourierOrder,
        onAddCancelledRecordOrder = ::addCancelledRecordOrder,
        onUpdateShippedAwbAndDetails = ::updateShippedAwbAndDetails,
        onSendCustomerTrackingAlert = ::sendCustomerTrackingAlert,
        onLogFailedDeliveryAttempt = ::logFailedDeliveryAttempt,
        onMarkCourierPdfSent = ::markCourierPdfSent,
        onVerifyProduct = ::verifySellerOrderProduct,
        onGenerateInvoice = ::generateOrderInvoice,
        onGenerateAwb = ::generateOrderAwb,
        onScanBarcode = ::scanOrderBarcode,
        onRequestPickup = ::requestCourierPickup,
        onBulkRequestPickup = ::bulkRequestCourierPickup,
        onUpdateReadyToShipDetails = ::updateReadyToShipDetails,
        onAddReadyToShipProductOrder = ::addReadyToShipProductOrder,
        onReadyToShip = ::markSellerOrderReadyToShip,
        onDispatchOrder = ::dispatchSellerOrder,
        onBulkDispatchOrders = ::bulkDispatchReadyOrders,
        onAdvanceTracking = ::advanceShipmentTracking,
        onSyncLiveCourierTracking = ::syncLiveCourierTracking,
        onToggleDelayAlert = ::toggleDeliveryDelayAlert,
        onMarkRto = ::markOrderRto,
        onCancelOrderDetailed = ::cancelSellerOrderDetailed,
        onInitiateRefund = ::initiateOrderRefund,
        onCompleteRefund = ::completeOrderRefund,
        onRestoreStock = ::restoreOrderStock,
        onMarkLabelDownloaded = ::markOrderLabelDownloaded,
        onAddManualOrder = ::createSellerManualOrder
    )

    fun cancelOrder(orderId: String, reason: String) {
        cancelSellerOrderDetailed(
            orderId = orderId,
            reason = reason,
            cancelledBy = "Customer",
            restoreStock = true
        )
    }

    fun requestReturnOrder(orderId: String, reason: String) {
        val ts = nowLogTime()
        val cleanReason = reason.trim().ifBlank { "Size / Fit Issue" }
        var matched = false
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId && ord.status == OrderStatus.DELIVERED) {
                    matched = true
                    ord.copy(
                        status = OrderStatus.RETURN_REQUESTED,
                        sellerStage = "Return Requested",
                        statusText = "Return Requested ($cleanReason)",
                        refundAmount = ord.totalAmount,
                        refundStatus = "Return Requested",
                        pickupStatus = "Return Pickup Pending",
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Return Requested ($cleanReason)"
                    )
                } else ord
            }
        }
        if (matched) {
            pushNotification(
                title = "Return Request Submitted ↩️ (#$orderId)",
                message = "Return requested ($cleanReason). Courier pickup will be assigned shortly.",
                category = "Orders"
            )
            syncOrderNow(orderId)
        }
    }

    // Orders Filter Operations
    fun setOrderStatusFilter(status: OrderStatus) {
        _selectedOrderStatus.value = status
    }

    fun setOrderSearchQuery(query: String) {
        _orderSearchQuery.value = query
    }

    val filteredOrders = combine(_orders, _selectedOrderStatus, _orderSearchQuery) { orderList, status, q ->
        orderList.filter { order ->
            val matchesStatus = when (status) {
                OrderStatus.ALL -> true
                OrderStatus.RETURN -> order.status == OrderStatus.RETURN ||
                    order.status == OrderStatus.RETURN_REQUESTED ||
                    order.status == OrderStatus.RETURNED ||
                    order.status == OrderStatus.REFUNDED
                else -> order.status == status
            }
            val matchesSearch = q.isBlank() ||
                order.product.name.contains(q, ignoreCase = true) ||
                order.orderId.contains(q, ignoreCase = true)
            matchesStatus && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SampleData.initialOrders)

    // Product Detail
    fun selectProduct(product: Product) {
        _selectedProduct.value = product
        _recentlyViewedIds.update { cur ->
            (listOf(product.id) + cur.filterNot { it == product.id }).take(10)
        }
        _subScreen.value = AppSubScreen.PRODUCT_DETAIL
    }

    // Share Operations
    fun openShare(product: Product) {
        _selectedProductForShare.value = product
        _subScreen.value = AppSubScreen.SHARE_SHEET
    }

    fun openShareContacts() {
        _subScreen.value = AppSubScreen.SHARE_CONTACTS_SHEET
    }

    // Feedback Dialog
    fun startFeedback(order: OrderItem) {
        _selectedOrderForFeedback.value = order
        feedbackStep.value = 1
        feedbackOverallRating.value = 5
        feedbackMaterialRating.value = 5
        feedbackComment.value = ""
        feedbackPhotosAdded.value = emptyList()
        _subScreen.value = AppSubScreen.FEEDBACK
    }

    fun submitFeedback() {
        val order = _selectedOrderForFeedback.value ?: return
        _orders.update { list ->
            list.map {
                if (it.orderId == order.orderId) {
                    it.copy(
                        userRating = feedbackOverallRating.value,
                        userReviewText = feedbackComment.value.ifBlank { "Loved the dress! Great fit and premium quality." },
                        isFeedbackCompleted = true
                    )
                } else it
            }
        }
        _subScreen.value = AppSubScreen.NONE
    }

    fun startTracking(order: OrderItem) {
        _selectedOrderForTracking.value = order
        _subScreen.value = AppSubScreen.TRACK_ORDER
    }

    // Delivery Address
    fun updateDeliveryAddress(newAddress: String) {
        _deliveryAddress.value = newAddress
    }

    // Video Reels Interaction
    fun toggleLikeVideo(videoId: String) {
        _videoFinds.update { list ->
            list.map { vf ->
                if (vf.id == videoId) {
                    val newLiked = !vf.isLiked
                    val newCount = if (newLiked) vf.likesCount + 1 else vf.likesCount - 1
                    vf.copy(isLiked = newLiked, likesCount = newCount)
                } else vf
            }
        }
    }

    fun toggleMuteVideo(videoId: String) {
        _videoFinds.update { list ->
            list.map { vf ->
                if (vf.id == videoId) vf.copy(isMuted = !vf.isMuted) else vf
            }
        }
    }

    // Theme, Language & Account Role Switcher
    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun setAppLanguage(language: String) {
        _appLanguage.value = language
    }

    fun switchUserRole(role: UserAccountRole) {
        _activeRole.value = role
        when (role) {
            UserAccountRole.STAFF -> openSubScreen(AppSubScreen.STAFF_WORKSPACE)
            UserAccountRole.SELLER -> openSubScreen(AppSubScreen.SELLER_PANEL)
            UserAccountRole.ADMIN -> openSubScreen(AppSubScreen.ADMIN_PANEL)
            UserAccountRole.BUYER -> {
                subScreenStack.clear()
                _subScreen.value = AppSubScreen.NONE
            }
        }
    }

    // Notification System Operations
    fun pushNotification(title: String, message: String, category: String) {
        val newNotif = AppNotification(
            id = "n_${System.currentTimeMillis()}",
            title = title,
            message = message,
            category = category,
            timeAgo = "Just now",
            isRead = false
        )
        _notifications.update { listOf(newNotif) + it }
    }

    fun markNotificationRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }

    fun markAllNotificationsRead() {
        _notifications.update { list -> list.map { it.copy(isRead = true) } }
    }

    fun updateNotificationPrefs(updater: (NotificationPreferences) -> NotificationPreferences) {
        _notificationPrefs.update(updater)
    }

    // Product Comparison Operations
    fun toggleCompareProduct(product: Product): Boolean {
        var added = false
        _compareProducts.update { list ->
            if (list.any { it.id == product.id }) {
                added = false
                list.filterNot { it.id == product.id }
            } else if (list.size < 3) {
                added = true
                list + product
            } else {
                added = true
                list.drop(1) + product
            }
        }
        return added
    }

    fun clearCompareProducts() {
        _compareProducts.value = emptyList()
    }

    // Advanced Marketplace Setup Actions
    fun setCurrency(currency: String) {
        _selectedCurrency.value = currency
    }

    fun setWarehouse(warehouse: String) {
        _activeWarehouse.value = warehouse
    }

    fun toggleVipSubscription(enabled: Boolean) {
        _isVipSubscribed.value = enabled
        if (enabled) {
            pushNotification(
                title = "Gyanvi Gold VIP Activated 👑",
                message = "Enjoy free express shipping and +2x Reward Points across all warehouses!",
                category = "Offers"
            )
        }
    }

    fun registerNewSeller(shopName: String, ownerName: String, gstin: String) {
        _registeredSellersCount.update { it + 1 }
        pushNotification(
            title = "New Seller KYC Submitted 🏪",
            message = "$shopName ($ownerName • GSTIN: $gstin) registered for Marketplace approval.",
            category = "Orders"
        )
    }

    // Offline Room Catalog Operations
    fun saveOfflineProduct(entity: ProductEntity) {
        viewModelScope.launch {
            productRepository?.insertOrUpdateProduct(entity)
        }
    }

    fun updateOfflineProductStock(productId: String, newStock: Int) {
        viewModelScope.launch {
            productRepository?.updateStock(productId, newStock)
        }
    }

    fun updateOfflineProductPricing(productId: String, mrp: Int, sellingPrice: Int) {
        viewModelScope.launch {
            productRepository?.updatePricing(productId, mrp, sellingPrice)
        }
    }

    fun toggleOfflineProductPaused(productId: String, paused: Boolean) {
        viewModelScope.launch {
            productRepository?.setProductPaused(productId, paused)
        }
    }

    fun deleteOfflineProduct(productId: String) {
        deleteCatalogProduct(productId)
    }

    // ==================== MASTER APP MANAGEMENT & LIVE CMS ACTIONS ====================

    fun updateAppManagement(transform: (AppManagementConfig) -> AppManagementConfig) {
        _managementConfig.update { current ->
            val updated = transform(current)
            managementStorage?.saveConfig(updated)
            updated
        }
    }

    fun updateAppTexts(transform: (AppTextsConfig) -> AppTextsConfig) {
        updateAppManagement { it.copy(texts = transform(it.texts)) }
    }

    fun updateAppFeatures(transform: (AppFeaturesConfig) -> AppFeaturesConfig) {
        updateAppManagement { it.copy(features = transform(it.features)) }
    }

    fun updateAppImages(transform: (AppImagesConfig) -> AppImagesConfig) {
        updateAppManagement { it.copy(images = transform(it.images)) }
    }

    fun updateAppTypography(transform: (AppTypographyConfig) -> AppTypographyConfig) {
        updateAppManagement { it.copy(typography = transform(it.typography)) }
    }

    fun setDashboardThemePreset(preset: DashboardThemePreset) {
        updateAppManagement { it.copy(themePreset = preset) }
    }

    fun setHomeLayoutMode(mode: HomeLayoutMode) {
        updateAppManagement { it.copy(homeLayoutMode = mode) }
    }

    fun setCardCornerPreset(preset: CardCornerPreset) {
        updateAppManagement { it.copy(cardCornerPreset = preset) }
    }

    fun updatePaymentGatewayConfig(transform: (PaymentGatewaySettingsConfig) -> PaymentGatewaySettingsConfig) {
        updateAppManagement { it.copy(paymentGateways = transform(it.paymentGateways)) }
    }

    fun saveAndConnectPaymentGateway(
        id: String?,
        name: String,
        rawUrl: String,
        provider: PaymentGatewayProviderPreset,
        lastStatus: String = "Connected",
        connectNow: Boolean = true
    ): Result<PaymentGatewayLinkItem> {
        val validation = com.example.util.PaymentGatewayUrlHelper.validateHttpsGatewayUrl(rawUrl)
        if (!validation.isValid) {
            return Result.failure(IllegalArgumentException(validation.errorMessage ?: "Invalid HTTPS URL"))
        }
        val cleanName = name.trim().ifBlank { provider.displayName }
        val itemId = id?.takeIf { it.isNotBlank() } ?: "pg-${System.currentTimeMillis()}"
        val item = PaymentGatewayLinkItem(
            id = itemId,
            name = cleanName,
            url = validation.normalizedUrl,
            provider = provider,
            isEnabled = true,
            lastStatus = lastStatus
        )
        updatePaymentGatewayConfig { current ->
            val updatedList = if (current.gateways.any { it.id == itemId }) {
                current.gateways.map { if (it.id == itemId) item else it }
            } else {
                listOf(item) + current.gateways
            }
            current.copy(
                gateways = updatedList,
                activeGatewayId = if (connectNow) itemId else (current.activeGatewayId ?: itemId)
            )
        }
        return Result.success(item)
    }

    fun connectPaymentGateway(gatewayId: String) {
        updatePaymentGatewayConfig { current ->
            val exists = current.gateways.any { it.id == gatewayId }
            if (!exists) current
            else current.copy(
                activeGatewayId = gatewayId,
                gateways = current.gateways.map {
                    if (it.id == gatewayId) it.copy(isEnabled = true, lastStatus = "Connected") else it
                }
            )
        }
    }

    fun disconnectPaymentGateway() {
        updatePaymentGatewayConfig { current ->
            current.copy(activeGatewayId = null)
        }
    }

    fun deletePaymentGateway(gatewayId: String) {
        updatePaymentGatewayConfig { current ->
            val remaining = current.gateways.filterNot { it.id == gatewayId }
            val nextActive = if (current.activeGatewayId == gatewayId) {
                remaining.firstOrNull { it.isEnabled }?.id
            } else {
                current.activeGatewayId
            }
            current.copy(
                activeGatewayId = nextActive,
                gateways = remaining
            )
        }
    }

    fun toggleAppendPaymentParams(enabled: Boolean) {
        updatePaymentGatewayConfig { it.copy(appendOrderParams = enabled) }
    }

    fun loadAllPresetPaymentGateways() {
        updatePaymentGatewayConfig { current ->
            val existingById = current.gateways.associateBy { it.id }
            val existingProviders = current.gateways.map { it.provider }.toSet()
            val missingDefaults = com.example.model.PaymentGatewayDefaults.allGateways.filter { def ->
                !existingById.containsKey(def.id) && !existingProviders.contains(def.provider)
            }
            val merged = current.gateways + missingDefaults
            current.copy(
                gateways = merged,
                activeGatewayId = current.activeGatewayId ?: merged.firstOrNull()?.id,
                hasSeededDefaults = true
            )
        }
    }

    fun updateSocialAdsConfig(transform: (SocialMediaAdsConfig) -> SocialMediaAdsConfig) {
        updateAppManagement { it.copy(socialAds = transform(it.socialAds)) }
    }

    fun toggleSocialAdsEnabled(enabled: Boolean) {
        updateSocialAdsConfig { it.copy(adsEnabled = enabled) }
    }

    fun toggleSocialAdsOnHome(show: Boolean) {
        updateSocialAdsConfig { it.copy(showOnHome = show) }
    }

    fun saveSocialAdCampaign(
        id: String?,
        title: String,
        platform: SocialAdPlatform,
        headline: String,
        caption: String,
        rawTargetUrl: String,
        dailyBudgetInr: Int,
        ctaLabel: String,
        showOnHomeBanner: Boolean = true,
        isActive: Boolean = true
    ): Result<SocialMediaAdCampaign> {
        val urlInput = rawTargetUrl.trim().ifBlank { platform.defaultUrl }
        val validation = com.example.util.PaymentGatewayUrlHelper.validateHttpsGatewayUrl(urlInput)
        if (!validation.isValid) {
            return Result.failure(IllegalArgumentException(validation.errorMessage ?: "Invalid HTTPS Ad URL"))
        }
        val cleanTitle = title.trim().ifBlank { "${platform.badgeLabel} Festive Ad" }
        val cleanHeadline = headline.trim().ifBlank { cleanTitle }
        val cleanCaption = caption.trim().ifBlank { "Shop Exclusive Gyanvi Dresses Collection" }
        val cleanCta = ctaLabel.trim().ifBlank { "Shop Deal" }
        val safeBudget = dailyBudgetInr.coerceIn(100, 100000)

        val itemId = id?.takeIf { it.isNotBlank() } ?: "ad-${System.currentTimeMillis()}"
        var savedCampaign: SocialMediaAdCampaign? = null
        updateSocialAdsConfig { current ->
            val existing = current.campaigns.find { it.id == itemId }
            val next = SocialMediaAdCampaign(
                id = itemId,
                title = cleanTitle,
                platform = platform,
                headline = cleanHeadline,
                caption = cleanCaption,
                targetUrl = validation.normalizedUrl,
                dailyBudgetInr = safeBudget,
                impressions = existing?.impressions ?: (safeBudget * 24),
                clicks = existing?.clicks ?: (safeBudget / 3).coerceAtLeast(15),
                ordersGenerated = existing?.ordersGenerated ?: (safeBudget / 60).coerceAtLeast(1),
                isActive = isActive,
                showOnHomeBanner = showOnHomeBanner,
                ctaLabel = cleanCta
            )
            savedCampaign = next
            val updatedList = if (existing != null) {
                current.campaigns.map { if (it.id == itemId) next else it }
            } else {
                listOf(next) + current.campaigns
            }
            current.copy(campaigns = updatedList)
        }
        val resultItem = savedCampaign ?: return Result.failure(IllegalStateException("Could not save ad campaign"))
        pushNotification(
            title = "Social Ad Campaign Saved (${platform.badgeLabel})",
            message = "$cleanTitle is ${if (isActive) "LIVE" else "paused"} on ${platform.displayName}.",
            category = "Offers"
        )
        return Result.success(resultItem)
    }

    fun toggleSocialAdActive(campaignId: String) {
        updateSocialAdsConfig { current ->
            current.copy(
                campaigns = current.campaigns.map { ad ->
                    if (ad.id == campaignId) ad.copy(isActive = !ad.isActive) else ad
                }
            )
        }
    }

    fun deleteSocialAdCampaign(campaignId: String) {
        updateSocialAdsConfig { current ->
            current.copy(campaigns = current.campaigns.filterNot { it.id == campaignId })
        }
    }

    fun recordSocialAdClick(campaignId: String) {
        updateSocialAdsConfig { current ->
            current.copy(
                campaigns = current.campaigns.map { ad ->
                    if (ad.id == campaignId) {
                        ad.copy(
                            clicks = ad.clicks + 1,
                            impressions = ad.impressions + 3
                        )
                    } else ad
                }
            )
        }
    }

    // ==================== ADMIN LOGIN, CONFIGS, 11-STAGE WORKFLOW & REAL AI ====================

    fun authenticateAdmin(email: String, pin: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        val cleanPin = pin.trim()
        val isValid = (cleanEmail == "admin@gyanvidresses.com" || cleanEmail == "owner@gyanvi.com" || cleanEmail.contains("admin")) &&
            (cleanPin == "2026" || cleanPin == "123456" || cleanPin.length >= 4)
        if (isValid) {
            _isAdminAuthenticated.value = true
            pushNotification(
                title = "Admin Session Unlocked 🔐",
                message = "$cleanEmail logged into Super Admin Control Center.",
                category = "Security"
            )
        }
        return isValid
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
    }

    fun updateWebsiteIntegrationConfig(transform: (WebsiteIntegrationConfig) -> WebsiteIntegrationConfig) {
        updateAppManagement { it.copy(websiteIntegration = transform(it.websiteIntegration)) }
    }

    fun updateCommissionConfig(transform: (CommissionConfig) -> CommissionConfig) {
        updateAppManagement { it.copy(commissions = transform(it.commissions)) }
    }

    fun markOrderProcessing(orderId: String) {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    ord.copy(
                        status = OrderStatus.PROCESSING,
                        sellerStage = "Processing",
                        isOrderConfirmed = true,
                        statusText = "Processing • Quality Check & Packing",
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Order moved to Processing (Packing Started)"
                    )
                } else ord
            }
        }
        syncOrderNow(orderId)
    }

    fun markOrderOutForDelivery(orderId: String) {
        val ts = nowLogTime()
        val shortDate = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    val liveLoc = "Out for Delivery • Near ${ord.deliveryAddress.take(28)}, ${ord.city}"
                    ord.copy(
                        status = OrderStatus.OUT_FOR_DELIVERY,
                        sellerStage = "Out for Delivery",
                        statusText = "Out for Delivery (${ord.courierPartner})",
                        currentTrackingLocation = liveLoc,
                        trackingStages = ord.trackingStages.mapIndexed { idx, st ->
                            if (idx <= 2) st.copy(isCompleted = true) else st.copy(dateStr = shortDate)
                        },
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Out for Delivery ($liveLoc)"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "Out for Delivery 🛵 (#$orderId)",
            message = "Rider is out to deliver order #$orderId in ${ _orders.value.find { it.orderId == orderId }?.city ?: "customer city" }.",
            category = "Orders"
        )
        syncOrderNow(orderId)
    }

    fun markOrderDelivered(orderId: String) {
        val ts = nowLogTime()
        val shortDate = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    ord.copy(
                        status = OrderStatus.DELIVERED,
                        sellerStage = "Delivered",
                        statusText = "Delivered to Customer",
                        deliveryDateRange = "Delivered on $shortDate",
                        paymentStatus = if (ord.paymentMethod.contains("COD", true)) "COD Collected" else ord.paymentStatus,
                        currentTrackingLocation = "Delivered at Customer Doorstep (${ord.city})",
                        trackingStages = ord.trackingStages.map { it.copy(isCompleted = true) },
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Delivered to Customer"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "Order Delivered ✅ (#$orderId)",
            message = "Order #$orderId successfully delivered!",
            category = "Orders"
        )
        syncOrderNow(orderId)
    }

    fun scheduleReturnPickup(orderId: String, courierPartner: String = "Delhivery Reverse") {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    ord.copy(
                        status = OrderStatus.RETURN_REQUESTED,
                        sellerStage = "Return Pickup Scheduled",
                        pickupStatus = "Return Pickup Scheduled ($courierPartner)",
                        statusText = "Return Pickup Scheduled ($courierPartner)",
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Return Pickup Scheduled via $courierPartner"
                    )
                } else ord
            }
        }
        pushNotification(
            title = "Return Pickup Scheduled 📦 (#$orderId)",
            message = "$courierPartner assigned for reverse pickup of #$orderId.",
            category = "Orders"
        )
        syncOrderNow(orderId)
    }

    fun markOrderReturned(orderId: String) {
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    ord.copy(
                        status = OrderStatus.RETURNED,
                        sellerStage = "Returned",
                        pickupStatus = "Return Picked & Received",
                        statusText = "Returned to Boutique",
                        isStockRestored = true,
                        refundAmount = if (ord.refundAmount > 0) ord.refundAmount else ord.totalAmount,
                        refundStatus = "Pending Refund",
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Returned Item Received & Stock Restored"
                    )
                } else ord
            }
        }
        syncOrderNow(orderId)
    }

    fun updateOrderStatusAdmin(orderId: String, newStatus: OrderStatus) {
        when (newStatus) {
            OrderStatus.ORDERED -> {
                val ts = nowLogTime()
                _orders.update { list ->
                    list.map { ord ->
                        if (ord.orderId == orderId) {
                            ord.copy(
                                status = OrderStatus.ORDERED,
                                sellerStage = "Pending",
                                statusText = "Pending Confirmation",
                                orderHistoryLog = ord.orderHistoryLog + "$ts: Admin set status to Pending"
                            )
                        } else ord
                    }
                }
                syncOrderNow(orderId)
            }
            OrderStatus.CONFIRMED -> confirmSellerOrder(orderId)
            OrderStatus.PROCESSING -> markOrderProcessing(orderId)
            OrderStatus.READY_TO_SHIP -> markSellerOrderReadyToShip(orderId)
            OrderStatus.SHIPPED -> dispatchSellerOrder(orderId)
            OrderStatus.OUT_FOR_DELIVERY -> markOrderOutForDelivery(orderId)
            OrderStatus.DELIVERED -> markOrderDelivered(orderId)
            OrderStatus.CANCELLED -> cancelSellerOrderDetailed(orderId, "Cancelled via Admin Control", "Admin", true)
            OrderStatus.RETURN_REQUESTED -> scheduleReturnPickup(orderId)
            OrderStatus.RETURNED -> markOrderReturned(orderId)
            OrderStatus.REFUNDED -> completeOrderRefund(orderId)
            else -> {}
        }
    }

    fun updateOrderShippingCharges(orderId: String, charges: Int) {
        val safeCharges = charges.coerceAtLeast(0)
        val ts = nowLogTime()
        _orders.update { list ->
            list.map { ord ->
                if (ord.orderId == orderId) {
                    ord.copy(
                        shippingCharges = safeCharges,
                        orderHistoryLog = ord.orderHistoryLog + "$ts: Shipping Charges updated to ₹$safeCharges"
                    )
                } else ord
            }
        }
        syncOrderNow(orderId)
    }

    fun sendBuyerSellerMessage(
        text: String,
        productName: String = "",
        asSeller: Boolean = false,
        customSenderName: String? = null,
        customSenderRole: String? = null
    ) {
        val clean = text.trim()
        if (clean.isBlank()) return
        val sender = customSenderName ?: if (asSeller) "Gyanvi Boutique Seller" else (_currentUser.value?.fullName ?: "Buyer")
        val roleLabel = customSenderRole ?: if (asSeller) "SELLER" else "BUYER"
        val timeLabel = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val msg = BuyerSellerChatMessage(
            id = "chat_${System.currentTimeMillis()}_${Random.nextInt(10, 99)}",
            senderName = sender,
            senderRole = roleLabel,
            productName = productName,
            text = clean,
            timeLabel = timeLabel
        )
        _buyerSellerMessages.update { it + msg }
    }

    // ==================== OWNER -> KARAMCHARI (STAFF) TASK DIVISION ====================

    fun logSecurityActivity(actorName: String, roleBadge: String, action: String) {
        val ts = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
        val entry = SecurityActivityEntry(
            id = "act-${System.currentTimeMillis()}-${Random.nextInt(10, 99)}",
            actorName = actorName,
            roleBadge = roleBadge,
            action = action,
            timestamp = ts
        )
        _activityHistory.update { listOf(entry) + it.take(49) }
    }

    fun selectActiveStaffMember(staffId: String) {
        if (_staffMembers.value.any { it.id == staffId }) {
            _activeStaffId.value = staffId
        }
    }

    fun verifyAndSwitchStaff(codeOrPhone: String, pin: String): Pair<Boolean, String> {
        val query = codeOrPhone.trim().uppercase()
        val cleanPin = pin.trim()
        val matched = _staffMembers.value.find {
            (it.staffCode.equals(query, ignoreCase = true) ||
                it.phone.endsWith(query.takeLast(10)) ||
                it.name.equals(codeOrPhone.trim(), ignoreCase = true)) &&
                it.pin == cleanPin
        } ?: return false to "Invalid Staff Code/Phone or PIN."

        if (!matched.isActive) {
            return false to "${matched.name} account is currently Paused by Owner."
        }
        _activeStaffId.value = matched.id
        _activeRole.value = UserAccountRole.STAFF
        logSecurityActivity(
            actorName = "${matched.name} (${matched.staffCode})",
            roleBadge = matched.department.shortBadge,
            action = "PIN Login verified into ${matched.department.title}"
        )
        pushNotification(
            title = "Staff Duty Active 👷 (${matched.department.shortBadge})",
            message = "${matched.name} (${matched.staffCode}) signed into ${matched.department.title}.",
            category = "Security"
        )
        return true to "Welcome ${matched.name} • ${matched.department.title}"
    }

    fun addStaffMember(
        name: String,
        phone: String,
        department: StaffDepartment,
        shift: String = "Morning (9 AM - 6 PM)",
        pin: String = "1234",
        monthlySalary: Int = 18000
    ): StaffMember {
        val nextNum = 100 + _staffMembers.value.size
        val prefix = if (department == StaffDepartment.DEPARTMENT_MANAGER) "MGR-GD" else "EMP-GD"
        val member = StaffMember(
            id = "emp-${System.currentTimeMillis()}",
            name = name.trim(),
            staffCode = "$prefix-$nextNum",
            phone = phone.trim(),
            pin = pin.trim().ifBlank { "1234" },
            department = department,
            shift = shift.ifBlank { "Morning (9 AM - 6 PM)" },
            tasksCompleted = 0,
            isActive = true,
            monthlySalary = monthlySalary.coerceAtLeast(5000)
        )
        _staffMembers.update { it + member }
        logSecurityActivity("Owner", "OWNER", "Added ${member.name} (${member.staffCode}) to ${department.title} • Salary ₹${member.monthlySalary}")
        pushNotification(
            title = "New Staff Added (${department.shortBadge})",
            message = "${member.name} (${member.staffCode}) assigned to ${department.title} by Owner.",
            category = "Seller"
        )
        return member
    }

    fun toggleStaffActive(staffId: String) {
        _staffMembers.update { list ->
            list.map {
                if (it.id == staffId) {
                    val next = !it.isActive
                    logSecurityActivity("Owner", "OWNER", "${if (next) "Activated" else "Paused"} ${it.name} (${it.staffCode})")
                    it.copy(isActive = next)
                } else it
            }
        }
    }

    fun checkInOutStaff(staffId: String, newStatus: String) {
        val nowTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        _staffMembers.update { list ->
            list.map { emp ->
                if (emp.id == staffId) {
                    val addDay = if (newStatus == "Present" && emp.attendanceStatus != "Present") 1 else 0
                    logSecurityActivity(
                        actorName = "${emp.name} (${emp.staffCode})",
                        roleBadge = emp.department.shortBadge,
                        action = "Attendance updated to $newStatus ($nowTime)"
                    )
                    emp.copy(
                        attendanceStatus = newStatus,
                        checkInTime = if (newStatus == "Present") nowTime else emp.checkInTime,
                        attendanceDaysThisMonth = emp.attendanceDaysThisMonth + addDay
                    )
                } else emp
            }
        }
    }

    fun payStaffSalary(staffId: String) {
        val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        var paidMember: StaffMember? = null
        _staffMembers.update { list ->
            list.map { emp ->
                if (emp.id == staffId) {
                    paidMember = emp.copy(isSalaryPaidThisMonth = true, lastSalaryPaidDate = today)
                    paidMember!!
                } else emp
            }
        }
        paidMember?.let { emp ->
            logSecurityActivity("Owner", "OWNER", "Paid Salary ₹${emp.monthlySalary} to ${emp.name} (${emp.staffCode})")
            pushNotification(
                title = "Salary Credited 💸 (${emp.staffCode})",
                message = "₹${emp.monthlySalary} monthly salary released for ${emp.name} (${emp.department.shortBadge}).",
                category = "Wallet"
            )
        }
    }

    fun updateStaffPermissions(
        staffId: String,
        canPackOrders: Boolean,
        canDeliverOrders: Boolean,
        canEditStock: Boolean,
        canHandleSupport: Boolean,
        canViewGstFinance: Boolean,
        canManageStaff: Boolean
    ) {
        _staffMembers.update { list ->
            list.map { emp ->
                if (emp.id == staffId) {
                    logSecurityActivity("Owner", "OWNER", "Updated RBAC permissions for ${emp.name} (${emp.staffCode})")
                    emp.copy(
                        canPackOrders = canPackOrders,
                        canDeliverOrders = canDeliverOrders,
                        canEditStock = canEditStock,
                        canHandleSupport = canHandleSupport,
                        canViewGstFinance = canViewGstFinance,
                        canManageStaff = canManageStaff
                    )
                } else emp
            }
        }
    }

    fun assignTaskToStaff(
        title: String,
        description: String,
        department: StaffDepartment,
        staffId: String,
        priority: String = "High",
        orderId: String = ""
    ) {
        val assignee = _staffMembers.value.find { it.id == staffId }
        val newTask = StaffAssignedTask(
            id = "tsk-${System.currentTimeMillis()}",
            title = title.trim(),
            description = description.trim().ifBlank { "Assigned by Owner for ${department.title}" },
            department = assignee?.department ?: department,
            assignedToStaffId = staffId,
            assignedToName = assignee?.name ?: department.title,
            relatedOrderId = orderId.trim(),
            priority = priority,
            status = "Pending"
        )
        _staffTasks.update { listOf(newTask) + it }
        logSecurityActivity("Owner / Manager", "CONTROL", "Assigned '${newTask.title}' to ${newTask.assignedToName}")
        pushNotification(
            title = "New Work Assigned 📋 (${newTask.department.shortBadge})",
            message = "${newTask.assignedToName}: ${newTask.title}",
            category = "Orders"
        )
    }

    fun updateStaffTaskStatus(taskId: String, newStatus: String) {
        var completedByStaffId: String? = null
        var taskTitle = ""
        _staffTasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    taskTitle = task.title
                    if (newStatus == "Completed" && task.status != "Completed") {
                        completedByStaffId = task.assignedToStaffId
                    }
                    task.copy(status = newStatus)
                } else task
            }
        }
        completedByStaffId?.let { empId ->
            _staffMembers.update { members ->
                members.map { m ->
                    if (m.id == empId) {
                        logSecurityActivity("${m.name} (${m.staffCode})", m.department.shortBadge, "Completed task: $taskTitle")
                        m.copy(
                            tasksCompleted = m.tasksCompleted + 1,
                            performanceScore = (m.performanceScore + 1).coerceAtMost(100)
                        )
                    } else m
                }
            }
        }
    }

    fun updateProductStockQuick(productId: String, delta: Int) {
        _products.update { list ->
            list.map { prod ->
                if (prod.id == productId) {
                    val nextStock = (prod.stockCount + delta).coerceAtLeast(0)
                    prod.copy(stockCount = nextStock, inStock = nextStock > 0)
                } else prod
            }
        }
        logSecurityActivity("Catalog Staff", "CATALOG", "Adjusted stock ($delta) for product #$productId")
    }

    fun toggleProductStockPause(productId: String) {
        _products.update { list ->
            list.map { prod ->
                if (prod.id == productId) {
                    val nextInStock = !prod.inStock
                    logSecurityActivity("Catalog Staff", "CATALOG", "${if (nextInStock) "Resumed" else "Paused"} product ${prod.name}")
                    prod.copy(
                        inStock = nextInStock,
                        stockCount = if (nextInStock && prod.stockCount <= 0) 20 else prod.stockCount
                    )
                } else prod
            }
        }
    }

    fun triggerAbandonedCartAlert() {
        val count = _cartItems.value.sumOf { it.quantity }.coerceAtLeast(1)
        val firstProduct = _cartItems.value.firstOrNull()?.product?.name ?: "Festive Kurti & Saree"
        pushNotification(
            title = "🛒 Your Bag Misses You! ($count item${if (count > 1) "s" else ""})",
            message = "$firstProduct is still waiting in your cart. Complete checkout with code FESTIVE15 before stock runs out!",
            category = "Offers"
        )
    }

    fun claimReferralCode(code: String): Pair<Boolean, String> {
        val normalized = code.trim().uppercase()
        if (normalized.length < 4) return false to "Enter a valid referral code (e.g. GYANVI125)."
        if (claimedReferralCodes.contains(normalized)) {
            return false to "Referral code $normalized already redeemed on this account."
        }
        claimedReferralCodes.add(normalized)
        _walletBalance.update { it + 125 }
        _rewardPoints.update { it + 50 }
        pushNotification(
            title = "🎁 Referral Bonus Credited (+₹125)",
            message = "Code $normalized applied! ₹125 Gyanvi Cash & +50 Loyalty Points added.",
            category = "Wallet"
        )
        return true to "₹125 Gyanvi Cash & 50 Loyalty Points credited!"
    }

    fun runAiDressRecommendations(query: String, occasion: String, maxBudget: Int) {
        val cleanQuery = query.trim().ifBlank { "Recommend best dresses for $occasion under ₹$maxBudget" }
        _aiStylistState.value = AiInferenceState.Loading
        viewModelScope.launch {
            val res = aiFashionService.getDressRecommendations(
                userQuery = cleanQuery,
                occasion = occasion,
                maxBudget = maxBudget,
                catalog = _products.value
            )
            _aiStylistState.value = res.fold(
                onSuccess = { AiInferenceState.Success(it) },
                onFailure = { AiInferenceState.Error(it.message ?: "AI Stylist error") }
            )
        }
    }

    fun runAiSmartSearch(query: String) {
        val clean = query.trim()
        if (clean.isBlank()) return
        _aiSearchState.value = AiInferenceState.Loading
        viewModelScope.launch {
            val res = aiFashionService.smartProductSearch(
                query = clean,
                catalog = _products.value
            )
            _aiSearchState.value = res.fold(
                onSuccess = { AiInferenceState.Success(it) },
                onFailure = { AiInferenceState.Error(it.message ?: "AI Search failed") }
            )
        }
    }

    fun clearAiSearch() {
        _aiSearchState.value = AiInferenceState.Idle
    }

    private fun slugifySeo(text: String): String =
        text.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

    fun updateCategory(
        categoryId: String,
        newName: String,
        newOfferBadge: String,
        newImageRes: Int,
        newCustomImageUri: String?
    ) {
        val existing = _categories.value.find { it.id == categoryId } ?: return
        editMainCategory(
            originalId = categoryId,
            newId = categoryId,
            newName = newName,
            offerBadge = newOfferBadge,
            seoName = existing.seoName,
            isActive = existing.isActive,
            sortOrder = existing.sortOrder,
            imageRes = newImageRes,
            customImageUri = newCustomImageUri
        )
    }

    fun addCategory(
        name: String,
        offerBadge: String,
        imageRes: Int,
        customImageUri: String? = null
    ) {
        val nextSort = (_categories.value.maxOfOrNull { it.sortOrder } ?: 0) + 1
        addMainCategory(
            customId = "",
            name = name,
            offerBadge = offerBadge,
            seoName = "",
            isActive = true,
            sortOrder = nextSort,
            imageRes = imageRes,
            customImageUri = customImageUri
        )
    }

    fun addMainCategory(
        customId: String = "",
        name: String,
        offerBadge: String = "",
        seoName: String = "",
        isActive: Boolean = true,
        sortOrder: Int = (_categories.value.maxOfOrNull { it.sortOrder } ?: 0) + 1,
        imageRes: Int = com.example.R.drawable.dress_anarkali_1793417013639,
        customImageUri: String? = null
    ): CategoryItem? {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return null
        val resolvedId = customId.trim().ifBlank {
            val prefix = cleanName.uppercase().replace(Regex("[^A-Z0-9]+"), "").take(4).ifBlank { "CAT" }
            "CAT-$prefix-${System.currentTimeMillis() % 10000}"
        }
        val resolvedSeo = seoName.trim().ifBlank { slugifySeo(cleanName) }
        val newCat = CategoryItem(
            id = resolvedId,
            name = cleanName,
            imageRes = imageRes,
            offerBadge = offerBadge.trim(),
            customImageUri = customImageUri?.takeIf { it.isNotBlank() },
            seoName = resolvedSeo,
            isActive = isActive,
            sortOrder = sortOrder
        )
        _categories.update { list ->
            val updated = (list.filterNot { it.id == resolvedId } + newCat).sortedBy { it.sortOrder }
            managementStorage?.saveCategories(updated)
            updated
        }
        viewModelScope.launch { categoryRepository?.upsertCategory(newCat) }
        return newCat
    }

    fun editMainCategory(
        originalId: String,
        newId: String = originalId,
        newName: String,
        offerBadge: String,
        seoName: String = "",
        isActive: Boolean = true,
        sortOrder: Int = 0,
        imageRes: Int,
        customImageUri: String?
    ) {
        val oldCat = _categories.value.find { it.id == originalId } ?: return
        val cleanName = newName.trim().ifBlank { oldCat.name }
        val cleanId = newId.trim().ifBlank { originalId }
        val resolvedSeo = seoName.trim().ifBlank { slugifySeo(cleanName) }
        val updatedCat = oldCat.copy(
            id = cleanId,
            name = cleanName,
            offerBadge = offerBadge.trim(),
            seoName = resolvedSeo,
            isActive = isActive,
            sortOrder = sortOrder,
            imageRes = imageRes,
            customImageUri = customImageUri?.takeIf { it.isNotBlank() }
        )

        _categories.update { list ->
            val updated = list.map { if (it.id == originalId) updatedCat else it }.sortedBy { it.sortOrder }
            managementStorage?.saveCategories(updated)
            updated
        }

        val updatedSubs = _subCategories.value.map { sub ->
            if (sub.parentCategoryId == originalId || sub.parentCategory.equals(oldCat.name, ignoreCase = true)) {
                sub.copy(parentCategoryId = cleanId, parentCategory = cleanName)
            } else sub
        }
        _subCategories.value = updatedSubs
        managementStorage?.saveSubCategories(updatedSubs)

        viewModelScope.launch {
            if (cleanId != originalId) {
                categoryRepository?.deleteCategory(originalId)
            }
            categoryRepository?.upsertCategory(updatedCat)
            categoryRepository?.replaceAllSubCategories(updatedSubs)
        }
    }

    fun toggleCategoryActive(categoryId: String, isActive: Boolean) {
        var changed: CategoryItem? = null
        _categories.update { list ->
            val updated = list.map { cat ->
                if (cat.id == categoryId) cat.copy(isActive = isActive).also { changed = it } else cat
            }
            managementStorage?.saveCategories(updated)
            updated
        }
        changed?.let { cat ->
            viewModelScope.launch { categoryRepository?.upsertCategory(cat) }
        }
    }

    fun moveCategorySortOrder(categoryId: String, delta: Int) {
        val sorted = _categories.value.sortedBy { it.sortOrder }.toMutableList()
        val idx = sorted.indexOfFirst { it.id == categoryId }
        val targetIdx = (idx + delta).coerceIn(0, sorted.lastIndex)
        if (idx < 0 || idx == targetIdx) return
        val moved = sorted.removeAt(idx)
        sorted.add(targetIdx, moved)
        val reindexed = sorted.mapIndexed { i, item -> item.copy(sortOrder = i + 1) }
        _categories.value = reindexed
        managementStorage?.saveCategories(reindexed)
        viewModelScope.launch { categoryRepository?.replaceAllCategories(reindexed) }
    }

    fun deleteCategory(categoryId: String) {
        _categories.update { list ->
            val updated = list.filterNot { it.id == categoryId }
            managementStorage?.saveCategories(updated)
            updated
        }
        _subCategories.update { subs ->
            val updated = subs.filterNot { it.parentCategoryId == categoryId }
            managementStorage?.saveSubCategories(updated)
            updated
        }
        viewModelScope.launch { categoryRepository?.deleteCategory(categoryId) }
    }

    fun addSubCategory(
        customId: String = "",
        parentCategoryId: String,
        parentCategoryName: String,
        name: String,
        seoName: String = "",
        isActive: Boolean = true,
        sortOrder: Int = (_subCategories.value.count { it.parentCategoryId == parentCategoryId || it.parentCategory.equals(parentCategoryName, ignoreCase = true) }) + 1,
        imageRes: Int = com.example.R.drawable.dress_anarkali_1793417013639,
        customImageUri: String? = null
    ): SubCategoryItem? {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return null
        val resolvedId = customId.trim().ifBlank { "SUB-${System.currentTimeMillis() % 100000}" }
        val resolvedSeo = seoName.trim().ifBlank { slugifySeo("$parentCategoryName-$cleanName") }
        val newSub = SubCategoryItem(
            id = resolvedId,
            parentCategory = parentCategoryName.trim().ifBlank { "Women" },
            name = cleanName,
            imageRes = imageRes,
            parentCategoryId = parentCategoryId.trim(),
            customImageUri = customImageUri?.takeIf { it.isNotBlank() },
            seoName = resolvedSeo,
            isActive = isActive,
            sortOrder = sortOrder
        )
        _subCategories.update { list ->
            val updated = (list.filterNot { it.id == resolvedId } + newSub).sortedBy { it.sortOrder }
            managementStorage?.saveSubCategories(updated)
            updated
        }
        viewModelScope.launch { categoryRepository?.upsertSubCategory(newSub) }
        return newSub
    }

    fun editSubCategory(
        originalId: String,
        newId: String = originalId,
        parentCategoryId: String,
        parentCategoryName: String,
        newName: String,
        seoName: String = "",
        isActive: Boolean = true,
        sortOrder: Int = 1,
        imageRes: Int,
        customImageUri: String?
    ) {
        val existing = _subCategories.value.find { it.id == originalId } ?: return
        val cleanName = newName.trim().ifBlank { existing.name }
        val cleanId = newId.trim().ifBlank { originalId }
        val updatedSub = existing.copy(
            id = cleanId,
            parentCategoryId = parentCategoryId.trim(),
            parentCategory = parentCategoryName.trim().ifBlank { existing.parentCategory },
            name = cleanName,
            seoName = seoName.trim().ifBlank { slugifySeo("$parentCategoryName-$cleanName") },
            isActive = isActive,
            sortOrder = sortOrder,
            imageRes = imageRes,
            customImageUri = customImageUri?.takeIf { it.isNotBlank() }
        )
        _subCategories.update { list ->
            val updated = list.map { if (it.id == originalId) updatedSub else it }.sortedBy { it.sortOrder }
            managementStorage?.saveSubCategories(updated)
            updated
        }
        viewModelScope.launch {
            if (cleanId != originalId) {
                categoryRepository?.deleteSubCategory(originalId)
            }
            categoryRepository?.upsertSubCategory(updatedSub)
        }
    }

    fun toggleSubCategoryActive(subCategoryId: String, isActive: Boolean) {
        var changed: SubCategoryItem? = null
        _subCategories.update { list ->
            val updated = list.map { sub ->
                if (sub.id == subCategoryId) sub.copy(isActive = isActive).also { changed = it } else sub
            }
            managementStorage?.saveSubCategories(updated)
            updated
        }
        changed?.let { sub ->
            viewModelScope.launch { categoryRepository?.upsertSubCategory(sub) }
        }
    }

    fun deleteSubCategory(subCategoryId: String) {
        _subCategories.update { list ->
            val updated = list.filterNot { it.id == subCategoryId }
            managementStorage?.saveSubCategories(updated)
            updated
        }
        viewModelScope.launch { categoryRepository?.deleteSubCategory(subCategoryId) }
    }

    fun updateProductCategories(
        productId: String,
        primaryCategory: String,
        primarySubcategory: String,
        additionalCategories: List<String> = emptyList()
    ) {
        val cleanPrimary = primaryCategory.trim().ifBlank { "Ethnic Wear" }
        val cleanSub = primarySubcategory.trim()
        val matchedCat = _categories.value.find { it.name.equals(cleanPrimary, ignoreCase = true) }
        val matchedSub = _subCategories.value.find { it.name.equals(cleanSub, ignoreCase = true) }
        val cleanAdditional = additionalCategories.map { it.trim() }.filter {
            it.isNotBlank() && !it.equals(cleanPrimary, ignoreCase = true) && !it.equals(cleanSub, ignoreCase = true)
        }.distinct()

        var updatedProd: Product? = null
        _products.update { list ->
            list.map { p ->
                if (p.id == productId) {
                    p.copy(
                        category = cleanPrimary,
                        subcategory = cleanSub.ifBlank { p.subcategory },
                        categoryId = matchedCat?.id ?: p.categoryId,
                        subcategoryId = matchedSub?.id ?: p.subcategoryId,
                        additionalCategories = cleanAdditional
                    ).also { updatedProd = it }
                } else p
            }
        }
        updatedProd?.let { prod ->
            viewModelScope.launch {
                productRepository?.insertOrUpdateProduct(prod.toEntity())
            }
        }
    }

    fun resetCategoriesToDefault() {
        _categories.value = SampleData.categoryList
        _subCategories.value = SampleData.defaultSubCategories
        managementStorage?.saveCategories(SampleData.categoryList)
        managementStorage?.saveSubCategories(SampleData.defaultSubCategories)
        viewModelScope.launch {
            categoryRepository?.replaceAllCategories(SampleData.categoryList)
            categoryRepository?.replaceAllSubCategories(SampleData.defaultSubCategories)
        }
    }

    fun editCatalogProduct(
        productId: String,
        name: String,
        category: String,
        price: Int,
        originalPrice: Int,
        fabric: String,
        description: String,
        inStock: Boolean,
        isMahaDeal: Boolean,
        isMallAssured: Boolean,
        imageRes: Int,
        customImageUri: String?,
        subcategory: String? = null,
        additionalCategories: List<String>? = null
    ) {
        val safePrice = price.coerceAtLeast(1)
        val safeMrp = originalPrice.coerceAtLeast(safePrice)

        productImageOverrides = if (!customImageUri.isNullOrBlank()) {
            productImageOverrides + (productId to customImageUri)
        } else {
            productImageOverrides - productId
        }
        managementStorage?.saveProductImageOverrides(productImageOverrides)

        val matchedCat = _categories.value.find { it.name.equals(category.trim(), ignoreCase = true) }
        val resolvedSub = subcategory?.trim()?.takeIf { it.isNotBlank() }
        val matchedSub = resolvedSub?.let { s -> _subCategories.value.find { it.name.equals(s, ignoreCase = true) } }

        var updatedProduct: Product? = null
        _products.update { list ->
            list.map { p ->
                if (p.id == productId) {
                    val cleanCat = category.trim().ifBlank { p.category }
                    p.copy(
                        name = name.trim().ifBlank { p.name },
                        category = cleanCat,
                        subcategory = resolvedSub ?: p.subcategory,
                        categoryId = matchedCat?.id ?: p.categoryId,
                        subcategoryId = matchedSub?.id ?: p.subcategoryId,
                        additionalCategories = additionalCategories ?: p.additionalCategories,
                        price = safePrice,
                        originalPrice = safeMrp,
                        codPrice = safePrice + 29,
                        upiPrice = safePrice,
                        fabric = fabric.trim().ifBlank { p.fabric },
                        description = description.trim().ifBlank { p.description },
                        inStock = inStock,
                        isMahaDeal = isMahaDeal,
                        isMallAssured = isMallAssured,
                        imageRes = imageRes,
                        imageUrl = customImageUri?.takeIf { it.isNotBlank() } ?: p.imageUrl,
                        customImageUri = customImageUri
                    ).also { updatedProduct = it }
                } else p
            }
        }
        updatedProduct?.let { prod ->
            viewModelScope.launch {
                productRepository?.insertOrUpdateProduct(prod.toEntity())
            }
        }
    }

    fun addCatalogProduct(
        name: String,
        category: String,
        price: Int,
        originalPrice: Int,
        fabric: String,
        description: String,
        imageRes: Int,
        customImageUri: String? = null,
        subcategory: String = "",
        additionalCategories: List<String> = emptyList(),
        brandName: String = "GYANVI DRESSES",
        color: String = "",
        weight: String = "350g",
        sizes: List<String> = listOf("S", "M", "L", "XL", "XXL"),
        numericSizes: List<String> = emptyList(),
        stock: Int = 100,
        sku: String = "",
        extraImageUrls: List<String> = emptyList(),
        isMahaDeal: Boolean = true,
        isMallAssured: Boolean = true
    ) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        val safePrice = price.coerceAtLeast(1)
        val safeMrp = originalPrice.coerceAtLeast(safePrice)
        val newId = "gd_custom_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}"
        val cleanCat = category.trim().ifBlank { "Kurti & Suit" }
        val cleanSub = subcategory.trim().ifBlank { cleanCat }
        val resolvedBrand = brandName.trim().ifBlank { "GYANVI DRESSES" }
        val resolvedSku = sku.trim().ifBlank { "GYN-${newId.takeLast(6).uppercase()}" }
        val resolvedWeight = weight.trim().ifBlank { "350g" }
        val resolvedSizes = sizes.map { it.trim() }.filter { it.isNotBlank() }.distinct().ifEmpty { listOf("Free Size") }
        val resolvedNumeric = numericSizes.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        val resolvedExtras = extraImageUrls.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        val matchedCat = _categories.value.find { it.name.equals(cleanCat, ignoreCase = true) }
        val matchedSub = _subCategories.value.find { it.name.equals(cleanSub, ignoreCase = true) }

        val primaryUri = customImageUri?.trim()?.takeIf { it.isNotBlank() } ?: resolvedExtras.firstOrNull()
        if (!primaryUri.isNullOrBlank()) {
            productImageOverrides = productImageOverrides + (newId to primaryUri)
            managementStorage?.saveProductImageOverrides(productImageOverrides)
        }

        val newProd = Product(
            id = newId,
            name = cleanName,
            category = cleanCat,
            subcategory = cleanSub,
            categoryId = matchedCat?.id ?: "",
            subcategoryId = matchedSub?.id ?: "",
            additionalCategories = additionalCategories.map { it.trim() }.filter { it.isNotBlank() }.distinct(),
            price = safePrice,
            originalPrice = safeMrp,
            rating = 4.6f,
            ratingCount = 120,
            imageRes = imageRes,
            fabric = fabric.trim().ifBlank { "Premium Silk Blend" },
            sizes = resolvedSizes,
            isMahaDeal = isMahaDeal,
            isMallAssured = isMallAssured,
            inStock = stock > 0,
            description = description.trim().ifBlank { "Added via Gyanvi Dresses Seller Catalog Upload." },
            imageUrl = primaryUri.orEmpty(),
            customImageUri = primaryUri,
            brandName = resolvedBrand,
            color = color.trim(),
            weight = resolvedWeight,
            numericSizes = resolvedNumeric,
            extraImageUrls = resolvedExtras,
            sku = resolvedSku
        )

        _products.update { listOf(newProd) + it }
        viewModelScope.launch {
            productRepository?.insertOrUpdateProduct(
                newProd.toEntity(
                    stock = stock.coerceAtLeast(0),
                    sku = resolvedSku,
                    brandName = resolvedBrand
                )
            )
        }
    }

    fun deleteCatalogProduct(productId: String) {
        productImageOverrides = productImageOverrides - productId
        managementStorage?.saveProductImageOverrides(productImageOverrides)
        _products.update { list -> list.filterNot { it.id == productId } }
        viewModelScope.launch {
            productRepository?.deleteProductById(productId)
        }
    }

    fun resetCatalogProductsToDefault() {
        productImageOverrides = emptyMap()
        managementStorage?.saveProductImageOverrides(emptyMap())
        _products.value = SampleData.sampleProducts
        viewModelScope.launch {
            productRepository?.insertOrUpdateProducts(SampleData.sampleProducts.map { it.toEntity() })
        }
    }

    fun resetAllAppManagementToDefaults() {
        _managementConfig.value = AppManagementConfig()
        resetCategoriesToDefault()
        productImageOverrides = emptyMap()
        managementStorage?.clearAll()
        _products.value = SampleData.sampleProducts
        viewModelScope.launch {
            productRepository?.insertOrUpdateProducts(SampleData.sampleProducts.map { it.toEntity() })
        }
    }

    // ==================== APP BACKUP & RESTORE OPERATIONS ====================

    fun createAppBackup(customLabel: String = ""): AppBackupSnapshot {
        val currentConfig = _managementConfig.value
        val currentCats = _categories.value
        val currentProds = _products.value
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val timeStr = sdf.format(Date())
        val backupId = "bkp_${System.currentTimeMillis()}"
        val cleanLabel = customLabel.trim().ifBlank { "Gyanvi Backup (${timeStr})" }

        val snapshot = AppBackupSnapshot(
            id = backupId,
            label = cleanLabel,
            createdAt = timeStr,
            summary = "Theme: ${currentConfig.themePreset.displayName} • Font: ${currentConfig.typography.fontStyle.displayName} • ${currentProds.size} Products • ${currentCats.size} Categories",
            themeName = currentConfig.themePreset.displayName,
            fontName = currentConfig.typography.fontStyle.displayName,
            productCount = currentProds.size,
            categoryCount = currentCats.size
        )

        val payload = InMemoryBackupPayload(
            snapshot = snapshot,
            config = currentConfig,
            categories = currentCats,
            productImageOverrides = productImageOverrides,
            products = currentProds,
            wishlistIds = _wishlistIds.value,
            deliveryAddress = _deliveryAddress.value
        )
        inMemoryBackups[backupId] = payload

        val persistedJson = managementStorage?.saveBackupPayload(
            snapshot = snapshot,
            config = currentConfig,
            categories = currentCats,
            productImageOverrides = productImageOverrides,
            wishlistIds = _wishlistIds.value,
            deliveryAddress = _deliveryAddress.value
        )
        _lastBackupJsonText.value = persistedJson ?: buildBackupSummaryText(snapshot, currentConfig)
        _appBackups.update { list -> listOf(snapshot) + list.filterNot { it.id == snapshot.id }.take(9) }

        pushNotification(
            title = "App Backup Created 💾",
            message = "$cleanLabel saved safely (${currentProds.size} products, theme & लिखावट settings).",
            category = "Security"
        )
        return snapshot
    }

    fun restoreAppBackup(backupId: String): Boolean {
        val inMem = inMemoryBackups[backupId]
        if (inMem != null) {
            _managementConfig.value = inMem.config
            _categories.value = inMem.categories
            productImageOverrides = inMem.productImageOverrides
            _products.value = inMem.products
            _wishlistIds.value = inMem.wishlistIds
            _deliveryAddress.value = inMem.deliveryAddress
            managementStorage?.saveConfig(inMem.config)
            managementStorage?.saveCategories(inMem.categories)
            managementStorage?.saveProductImageOverrides(inMem.productImageOverrides)
            pushNotification(
                title = "Backup Restored ✅",
                message = "Restored '${inMem.snapshot.label}' across Gyanvi Dresses.",
                category = "Security"
            )
            return true
        }

        val disk = managementStorage?.loadBackupBundle(backupId) ?: return false
        _managementConfig.value = disk.config
        if (disk.categories.isNotEmpty()) {
            _categories.value = disk.categories
            managementStorage?.saveCategories(disk.categories)
        }
        productImageOverrides = disk.productImageOverrides
        managementStorage?.saveConfig(disk.config)
        managementStorage?.saveProductImageOverrides(disk.productImageOverrides)
        _products.update { list ->
            list.map { p ->
                val uri = productImageOverrides[p.id]
                p.copy(customImageUri = uri)
            }
        }
        _wishlistIds.value = disk.wishlistIds
        _deliveryAddress.value = disk.deliveryAddress
        _lastBackupJsonText.value = disk.rawJson
        pushNotification(
            title = "Backup Restored ✅",
            message = "Restored '${disk.snapshot.label}' across Gyanvi Dresses.",
            category = "Security"
        )
        return true
    }

    fun deleteAppBackup(backupId: String) {
        inMemoryBackups.remove(backupId)
        managementStorage?.deleteBackup(backupId)
        _appBackups.update { list -> list.filterNot { it.id == backupId } }
    }

    private fun buildBackupSummaryText(snapshot: AppBackupSnapshot, config: AppManagementConfig): String {
        return """
            |=== GYANVI DRESSES APP BACKUP ===
            |Backup ID: ${snapshot.id}
            |Label: ${snapshot.label}
            |Created: ${snapshot.createdAt}
            |Theme: ${config.themePreset.displayName}
            |Font (लिखावट): ${config.typography.fontStyle.displayName} (${(config.typography.fontScale * 100).toInt()}%)
            |Layout: ${config.homeLayoutMode.label}
            |Brand Title: ${config.texts.brandTitle}
            |Products: ${snapshot.productCount} | Categories: ${snapshot.categoryCount}
        """.trimMargin()
    }

    // Customer & Seller Account Registration / Login / Password Operations
    fun openAuthScreen(
        accountType: String = AccountType.CUSTOMER,
        isRegisterMode: Boolean = true
    ) {
        _authPreferredType.value = accountType
        _authPreferredRegisterMode.value = isRegisterMode
        openSubScreen(AppSubScreen.USER_AUTH)
    }

    fun registerUserAccount(
        accountType: String,
        fullName: String,
        mobileNumber: String,
        email: String,
        password: String,
        shopName: String = "",
        businessAddress: String = "",
        gstin: String = "",
        onResult: (AuthResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = if (userRepository != null) {
                userRepository.registerAccount(
                    accountType = accountType,
                    fullName = fullName,
                    mobileNumber = mobileNumber,
                    email = email,
                    password = password,
                    shopName = shopName,
                    businessAddress = businessAddress,
                    gstin = gstin
                )
            } else {
                val cleanMobile = UserAccountRepository.normalizeMobile(mobileNumber)
                val cleanEmail = email.trim().lowercase()
                when {
                    fullName.trim().length < 2 -> AuthResult.Error("Please enter your full name.")
                    cleanMobile.isBlank() && cleanEmail.isBlank() -> AuthResult.Error("Enter Mobile Number or Email ID.")
                    password.length < 6 -> AuthResult.Error("Password must be at least 6 characters.")
                    else -> {
                        val account = UserAccountEntity(
                            id = "${accountType.lowercase()}_${System.currentTimeMillis()}",
                            accountType = accountType,
                            fullName = fullName.trim(),
                            mobileNumber = cleanMobile,
                            email = cleanEmail,
                            passwordHash = PasswordHasher.hash(password),
                            shopName = shopName.trim(),
                            businessAddress = businessAddress.trim(),
                            gstin = gstin.trim().uppercase()
                        )
                        _inMemoryAccounts.update { listOf(account) + it }
                        AuthResult.Success(account, "Account created successfully!")
                    }
                }
            }

            if (result is AuthResult.Success) {
                applyAuthenticatedAccount(result.account, isNewRegistration = true)
            }
            onResult(result)
        }
    }

    fun loginUserAccount(
        identifier: String,
        password: String,
        accountType: String? = null,
        onResult: (AuthResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = if (userRepository != null) {
                userRepository.login(identifier, password, accountType)
            } else {
                val norm = UserAccountRepository.normalizeIdentifier(identifier)
                val match = _inMemoryAccounts.value.find {
                    (it.mobileNumber.isNotBlank() && it.mobileNumber == norm) ||
                        (it.email.isNotBlank() && it.email.equals(norm, ignoreCase = true))
                }
                when {
                    match == null -> AuthResult.Error("Account not found.")
                    match.passwordHash != PasswordHasher.hash(password) -> AuthResult.Error("Incorrect password.")
                    else -> AuthResult.Success(match, "Welcome back, ${match.fullName}!")
                }
            }

            if (result is AuthResult.Success) {
                applyAuthenticatedAccount(result.account, isNewRegistration = false)
            }
            onResult(result)
        }
    }

    fun resetUserPassword(
        identifier: String,
        newPassword: String,
        accountType: String? = null,
        onResult: (AuthResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = if (userRepository != null) {
                userRepository.resetPassword(identifier, newPassword, accountType)
            } else {
                val norm = UserAccountRepository.normalizeIdentifier(identifier)
                val match = _inMemoryAccounts.value.find {
                    (it.mobileNumber.isNotBlank() && it.mobileNumber == norm) ||
                        (it.email.isNotBlank() && it.email.equals(norm, ignoreCase = true))
                }
                when {
                    match == null -> AuthResult.Error("Account not found.")
                    newPassword.length < 6 -> AuthResult.Error("Password must be at least 6 characters.")
                    else -> {
                        val updated = match.copy(passwordHash = PasswordHasher.hash(newPassword))
                        _inMemoryAccounts.update { list -> list.map { if (it.id == match.id) updated else it } }
                        AuthResult.Success(updated, "Password updated successfully!")
                    }
                }
            }
            if (result is AuthResult.Success) {
                applyAuthenticatedAccount(result.account, isNewRegistration = false)
            }
            onResult(result)
        }
    }

    fun generateMobileLoginOtp(mobileNumber: String): Pair<Boolean, String> {
        val cleanMobile = UserAccountRepository.normalizeMobile(mobileNumber)
        if (!UserAccountRepository.isValidMobile(cleanMobile)) {
            return false to "Please enter a valid 10-digit Mobile Number first."
        }
        val otp = String.format(Locale.US, "%06d", Random.nextInt(100000, 999999))
        _loginOtpSession.value = LoginOtpSession(
            mobileNumber = cleanMobile,
            generatedOtp = otp,
            isOtpGenerated = true,
            generatedAtMillis = System.currentTimeMillis(),
            attemptCount = 0
        )
        pushNotification(
            title = "Gyanvi Login OTP: $otp 🔐",
            message = "Enter 6-digit OTP $otp in the login box to unlock +91 $cleanMobile.",
            category = "Security"
        )
        return true to otp
    }

    fun clearMobileLoginOtp() {
        _loginOtpSession.value = LoginOtpSession()
    }

    fun verifyOtpAndLoginCustomer(
        mobileNumber: String,
        enteredOtp: String,
        accountType: String = AccountType.CUSTOMER,
        onResult: (AuthResult) -> Unit
    ) {
        val cleanMobile = UserAccountRepository.normalizeMobile(mobileNumber)
        val cleanOtp = enteredOtp.trim()
        if (!UserAccountRepository.isValidMobile(cleanMobile)) {
            onResult(AuthResult.Error("Please enter a valid 10-digit Mobile Number."))
            return
        }
        val session = _loginOtpSession.value
        if (!session.isOtpGenerated || session.mobileNumber != cleanMobile) {
            onResult(AuthResult.Error("Please tap 'Generate OTP' for +91 $cleanMobile first."))
            return
        }
        if (cleanOtp.length != 6) {
            onResult(AuthResult.Error("Please enter the complete 6-digit OTP in the OTP box."))
            return
        }
        if (cleanOtp != session.generatedOtp) {
            _loginOtpSession.update { it.copy(attemptCount = it.attemptCount + 1) }
            onResult(AuthResult.Error("Incorrect OTP! Please enter the exact 6-digit OTP shown."))
            return
        }

        _loginOtpSession.value = LoginOtpSession()
        viewModelScope.launch {
            val result = if (userRepository != null) {
                userRepository.loginOrRegisterWithMobileOtp(cleanMobile, accountType)
            } else {
                val match = _inMemoryAccounts.value.find {
                    it.mobileNumber == cleanMobile && it.accountType == accountType
                } ?: _inMemoryAccounts.value.find { it.mobileNumber == cleanMobile }

                if (match != null) {
                    AuthResult.Success(match, "OTP Verified! Logged in as ${match.fullName}.")
                } else if (accountType == AccountType.SELLER) {
                    AuthResult.Error("No Seller account found for +91 $cleanMobile. Please register Seller details first.")
                } else {
                    val newCustomer = UserAccountEntity(
                        id = "customer_${System.currentTimeMillis()}",
                        accountType = AccountType.CUSTOMER,
                        fullName = "Customer ${cleanMobile.takeLast(4)}",
                        mobileNumber = cleanMobile,
                        email = "",
                        passwordHash = PasswordHasher.hash("OTP_$cleanMobile"),
                        businessAddress = "Bettiah, Bihar - 845438",
                        isVerified = true
                    )
                    _inMemoryAccounts.update { listOf(newCustomer) + it }
                    AuthResult.Success(newCustomer, "OTP Verified! Customer logged in (+91 $cleanMobile).")
                }
            }

            if (result is AuthResult.Success) {
                applyAuthenticatedAccount(result.account, isNewRegistration = false)
            }
            onResult(result)
        }
    }

    fun switchSavedAccount(account: UserAccountEntity) {
        applyAuthenticatedAccount(account, isNewRegistration = false)
    }

    fun deleteSavedAccount(accountId: String) {
        viewModelScope.launch {
            userRepository?.deleteAccount(accountId)
            _inMemoryAccounts.update { list -> list.filterNot { it.id == accountId } }
            if (_currentUser.value?.id == accountId) {
                logoutCurrentUser()
            }
        }
    }

    fun onGoogleSignInAuthenticated(
        firebaseUser: FirebaseUser,
        preferredAccountType: String = AccountType.SELLER
    ) {
        viewModelScope.launch {
            val email = firebaseUser.email.orEmpty().trim().lowercase()
            val displayName = firebaseUser.displayName?.takeIf { it.isNotBlank() }
                ?: email.substringBefore("@").ifBlank { "Gyanvi User" }
            val existingLocal = registeredAccounts.value.find {
                email.isNotBlank() && it.email.equals(email, ignoreCase = true)
            }
            val resolvedAccount = existingLocal ?: run {
                val created = UserAccountEntity(
                    id = "google_${firebaseUser.uid}",
                    accountType = preferredAccountType,
                    fullName = displayName,
                    mobileNumber = firebaseUser.phoneNumber.orEmpty(),
                    email = email,
                    passwordHash = PasswordHasher.hash("GOOGLE_${firebaseUser.uid}"),
                    shopName = if (preferredAccountType == AccountType.SELLER) "Gyanvi Dresses Boutique" else "",
                    businessAddress = "Bettiah, Bihar - 845438",
                    isVerified = true
                )
                if (userRepository != null) {
                    val regResult = userRepository.registerAccount(
                        accountType = created.accountType,
                        fullName = created.fullName,
                        mobileNumber = created.mobileNumber,
                        email = created.email,
                        password = "GoogleSync#${firebaseUser.uid.take(6)}",
                        shopName = created.shopName,
                        businessAddress = created.businessAddress
                    )
                    if (regResult is AuthResult.Success) regResult.account else created
                } else {
                    _inMemoryAccounts.update { listOf(created) + it }
                    created
                }
            }
            applyAuthenticatedAccount(resolvedAccount, isNewRegistration = existingLocal == null)
        }
    }

    fun logoutCurrentUser() {
        val ctx = appContext
        if (firestoreOrderRepository != null && ctx != null) {
            viewModelScope.launch {
                withContext(NonCancellable) {
                    stopFirestoreOrderSync()
                    GoogleSignInHelper.signOutSafely(ctx)
                }
            }
        } else {
            stopFirestoreOrderSync()
        }
        authPrefs?.edit()?.remove(KEY_LOGGED_IN_USER_ID)?.apply()
        _currentUser.value = null
        _activeRole.value = UserAccountRole.BUYER
        subScreenStack.clear()
        _subScreen.value = AppSubScreen.NONE
        _currentTab.value = MainTab.HOME
    }

    private fun applyAuthenticatedAccount(account: UserAccountEntity, isNewRegistration: Boolean) {
        authPrefs?.edit()?.putString(KEY_LOGGED_IN_USER_ID, account.id)?.apply()
        val wasLoggedOut = _currentUser.value == null
        _currentUser.value = account
        if (wasLoggedOut) {
            subScreenStack.clear()
            _subScreen.value = if (account.accountType == AccountType.SELLER) {
                AppSubScreen.SELLER_PANEL
            } else {
                AppSubScreen.NONE
            }
            _currentTab.value = MainTab.HOME
        } else if (_subScreen.value == AppSubScreen.USER_AUTH) {
            closeSubScreen()
        }

        if (account.accountType == AccountType.SELLER) {
            _activeRole.value = UserAccountRole.SELLER
            if (isNewRegistration) {
                _registeredSellersCount.update { it + 1 }
            }
            pushNotification(
                title = if (isNewRegistration) "Seller Account Registered 🏪" else "Seller Signed In 🏪",
                message = "${account.shopName.ifBlank { account.fullName }} signed in via ${account.mobileNumber.ifBlank { account.email }}.",
                category = "Security"
            )
        } else {
            _activeRole.value = UserAccountRole.BUYER
            pushNotification(
                title = if (isNewRegistration) "Welcome to Gyanvi Dresses 🎉" else "Customer Signed In 🔐",
                message = "${account.fullName} signed in with ${account.mobileNumber.ifBlank { account.email }}.",
                category = "Security"
            )
        }
    }

    companion object {
        private const val KEY_LOGGED_IN_USER_ID = "logged_in_user_id"

        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = GyanviDatabase.getInstance(appContext)
                    val productRepo = ProductCatalogRepository(db.productDao())
                    val userRepo = UserAccountRepository(db.userAccountDao())
                    val categoryRepo = CategoryManagementRepository(db.categoryDao())
                    val firestoreRepo = FirestoreOrderRepository(appContext)
                    return GyanviViewModel(
                        enableTimer = true,
                        productRepository = productRepo,
                        userRepository = userRepo,
                        appContext = appContext,
                        firestoreOrderRepository = firestoreRepo,
                        categoryRepository = categoryRepo
                    ) as T
                }
            }
        }
    }
}
