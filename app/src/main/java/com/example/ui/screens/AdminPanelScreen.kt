package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.SampleData
import com.example.model.CategoryItem
import com.example.model.CommissionConfig
import com.example.model.OrderItem
import com.example.model.OrderStatus
import com.example.model.PaymentGatewayLinkItem
import com.example.model.PaymentGatewayProviderPreset
import com.example.model.PaymentGatewaySettingsConfig
import com.example.model.Product
import com.example.model.SecurityActivityEntry
import com.example.model.SocialAdPlatform
import com.example.model.SocialMediaAdCampaign
import com.example.model.SocialMediaAdsConfig
import com.example.model.StaffAssignedTask
import com.example.model.StaffDepartment
import com.example.model.StaffMember
import com.example.model.SubCategoryItem
import com.example.model.WebsiteIntegrationConfig
import com.example.ui.components.PanelIconAction
import com.example.ui.components.PanelIconActionGrid
import com.example.ui.components.PaymentGatewayConnectorSection
import com.example.ui.components.SocialMediaAdsManagerSection
import com.example.ui.theme.*
import com.example.util.HoldOrderPdfGenerator
import kotlinx.coroutines.launch

// =========================================================================
// DATA MODELS FOR ADMIN PANEL
// =========================================================================

data class AdminQcProduct(
    val id: String,
    val title: String,
    val sellerName: String,
    val sellerLocation: String,
    val sku: String,
    val mrp: Int,
    val sellerPrice: Int,
    val category: String,
    val ageGroup: String,
    val ageYears: String,
    val manufacturerName: String,
    val manufacturerPincode: String,
    val packerName: String,
    val packerPincode: String,
    val packerId: String = "PKR-GD-845438",
    var status: String, // "Pending Review", "Approved", "Rejected"
    var rejectionReason: String = "",
    @DrawableRes val imageRes: Int
)

data class AdminSellerItem(
    val id: String,
    val storeName: String,
    val ownerName: String,
    val phone: String,
    val city: String,
    val gstin: String,
    val isGstVerified: Boolean,
    val rating: Double,
    val activeProducts: Int,
    val totalOrders: Int,
    var isSuspended: Boolean = false,
    var commissionRate: String = "0%",
    var kycStatus: String = if (isGstVerified) "Approved" else "Pending KYC"
)

data class AdminOrderItem(
    val orderId: String,
    val customerName: String,
    val customerCity: String,
    val storeName: String,
    val productTitle: String,
    val amount: Int,
    val paymentType: String, // "Prepaid UPI", "Cash on Delivery (COD)"
    val courier: String,
    val awbNumber: String,
    var deliveryStatus: String // "Packed", "Shipped", "In-Transit", "Delivered", "RTO Returned"
)

data class AdminPayoutBatch(
    val batchId: String,
    val totalSellers: Int,
    val totalAmount: Int,
    val scheduledDate: String,
    var payoutStatus: String // "Pending Approval", "Processing", "Settled (NEFT)"
)

// =========================================================================
// GYANVI DRESSES ADMIN PANEL COMPOSABLE
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit,
    onOpenSellerPanel: () -> Unit = {},
    onOpenAppManagement: (initialTab: Int) -> Unit = {},
    onOpenBackup: () -> Unit = {},
    onOpenControlPanel: () -> Unit = {},
    onPublishBroadcast: (title: String, message: String) -> Unit = { _, _ -> },
    liveOrders: List<OrderItem> = emptyList(),
    autoTrackingEnabled: Boolean = true,
    onToggleAutoTracking: (Boolean) -> Unit = {},
    onAdvanceTracking: (String) -> Unit = {},
    onToggleDelayAlert: (String, Boolean, String) -> Unit = { _, _, _ -> },
    onMarkRto: (String, String) -> Unit = { _, _ -> },
    onSyncLiveTracking: () -> Unit = {},
    categories: List<CategoryItem> = SampleData.categoryList,
    subCategories: List<SubCategoryItem> = SampleData.defaultSubCategories,
    products: List<Product> = SampleData.sampleProducts,
    onAddMainCategory: (name: String, badge: String, seoName: String, customId: String, sortOrder: Int, isActive: Boolean, imageRes: Int, customImageUri: String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onEditMainCategory: (id: String, name: String, badge: String, seoName: String, sortOrder: Int, isActive: Boolean, imageRes: Int, customImageUri: String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onToggleCategoryActive: (id: String, active: Boolean) -> Unit = { _, _ -> },
    onMoveCategorySortOrder: (id: String, delta: Int) -> Unit = { _, _ -> },
    onDeleteCategory: (id: String) -> Unit = {},
    onAddSubCategory: (parentCategoryName: String, name: String, seoName: String, customId: String, sortOrder: Int, isActive: Boolean, imageRes: Int, customImageUri: String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onEditSubCategory: (id: String, parentCategoryName: String, name: String, seoName: String, sortOrder: Int, isActive: Boolean, imageRes: Int, customImageUri: String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onToggleSubCategoryActive: (id: String, active: Boolean) -> Unit = { _, _ -> },
    onDeleteSubCategory: (id: String) -> Unit = {},
    onUpdateProductCategories: (productId: String, primaryCategory: String, subcategory: String, additionalCategories: List<String>) -> Unit = { _, _, _, _ -> },
    onAddCatalogProduct: (name: String, category: String, subcategory: String, price: Int, mrp: Int, fabric: String, desc: String, additionalCategories: List<String>) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onDeleteCatalogProduct: (String) -> Unit = {},
    onResetCategories: () -> Unit = {},
    paymentGatewayConfig: PaymentGatewaySettingsConfig = PaymentGatewaySettingsConfig(),
    onSaveAndConnectPaymentGateway: (id: String?, name: String, url: String, provider: PaymentGatewayProviderPreset, lastStatus: String, connectNow: Boolean) -> Result<PaymentGatewayLinkItem> = { id, name, url, provider, status, _ ->
        Result.success(PaymentGatewayLinkItem(id ?: "pg-preview", name, url, provider, true, status))
    },
    onConnectPaymentGateway: (String) -> Unit = {},
    onDisconnectPaymentGateway: () -> Unit = {},
    onDeletePaymentGateway: (String) -> Unit = {},
    onToggleAppendPaymentParams: (Boolean) -> Unit = {},
    socialAdsConfig: SocialMediaAdsConfig = LocalAppConfig.current.socialAds,
    onToggleSocialAdsEnabled: (Boolean) -> Unit = {},
    onToggleSocialAdsHome: (Boolean) -> Unit = {},
    onSaveSocialAdCampaign: (String?, String, SocialAdPlatform, String, String, String, Int, String, Boolean, Boolean) -> Result<SocialMediaAdCampaign> = { id, title, platform, headline, caption, url, budget, cta, showHome, active ->
        Result.success(SocialMediaAdCampaign(id ?: "ad-admin", title, platform, headline, caption, url, budget, isActive = active, showOnHomeBanner = showHome, ctaLabel = cta))
    },
    onToggleSocialAdActive: (String) -> Unit = {},
    onDeleteSocialAdCampaign: (String) -> Unit = {},
    onRecordSocialAdClick: (String) -> Unit = {},
    isAdminAuthenticated: Boolean = true,
    onAuthenticateAdmin: (email: String, pin: String) -> Boolean = { _, _ -> true },
    onLogoutAdmin: () -> Unit = {},
    registeredUsersCount: Int = 4,
    onUpdateOrderStatusAdmin: (String, OrderStatus) -> Unit = { _, _ -> },
    onScheduleReturnPickup: (String) -> Unit = {},
    onUpdateOrderShippingCharges: (String, Int) -> Unit = { _, _ -> },
    websiteConfig: WebsiteIntegrationConfig = LocalAppConfig.current.websiteIntegration,
    onUpdateWebsiteConfig: ((WebsiteIntegrationConfig) -> WebsiteIntegrationConfig) -> Unit = {},
    commissionConfig: CommissionConfig = LocalAppConfig.current.commissions,
    onUpdateCommissionConfig: ((CommissionConfig) -> CommissionConfig) -> Unit = {},
    onTriggerAbandonedCartAlert: () -> Unit = {},
    staffMembers: List<StaffMember> = emptyList(),
    staffTasks: List<StaffAssignedTask> = emptyList(),
    activityHistory: List<SecurityActivityEntry> = emptyList(),
    onAddStaffMember: (name: String, phone: String, dept: StaffDepartment, shift: String, pin: String, monthlySalary: Int) -> Unit = { _, _, _, _, _, _ -> },
    onToggleStaffActive: (String) -> Unit = {},
    onPayStaffSalary: (String) -> Unit = {},
    onCheckInOutStaff: (String, String) -> Unit = { _, _ -> },
    onUpdateStaffPermissions: (staffId: String, canPack: Boolean, canDeliver: Boolean, canEditStock: Boolean, canSupport: Boolean, canGstFinance: Boolean, canManageStaff: Boolean) -> Unit = { _, _, _, _, _, _, _ -> },
    onAssignTaskToStaff: (title: String, desc: String, dept: StaffDepartment, staffId: String, priority: String) -> Unit = { _, _, _, _, _ -> },
    onOpenStaffWorkspace: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val adminScrollState = rememberScrollState()
    val adminScope = rememberCoroutineScope()

    if (!isAdminAuthenticated) {
        var adminEmail by remember { mutableStateOf("admin@gyanvidresses.com") }
        var adminPin by remember { mutableStateOf("") }
        var loginError by remember { mutableStateOf<String?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Admin Login (मालिक लॉगिन)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("admin_login_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite)
                )
            },
            containerColor = Color(0xFFF8FAFC),
            modifier = modifier.fillMaxSize()
        ) { pad ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth().widthIn(max = 460.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(shape = CircleShape, color = Color(0xFFFDF2F8), modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.padding(10.dp))
                            }
                            Column {
                                Text("GYANVI DRESSES", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MeeshoMagenta)
                                Text("Owner Super-Admin Verification", fontSize = 12.sp, color = TextSecondary)
                            }
                        }

                        OutlinedTextField(
                            value = adminEmail,
                            onValueChange = { adminEmail = it; loginError = null },
                            label = { Text("Admin Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MeeshoMagenta) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("admin_login_email")
                        )

                        OutlinedTextField(
                            value = adminPin,
                            onValueChange = { adminPin = it; loginError = null },
                            label = { Text("Security PIN (Default: 2026)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MeeshoMagenta) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("admin_login_pin")
                        )

                        loginError?.let { err ->
                            Text(err, color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    adminEmail = "admin@gyanvidresses.com"
                                    adminPin = "2026"
                                    loginError = null
                                },
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Text("Quick Fill PIN", fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    val ok = onAuthenticateAdmin(adminEmail, adminPin)
                                    if (!ok) {
                                        loginError = "Invalid Admin Email or Security PIN (use PIN 2026)."
                                    } else {
                                        Toast.makeText(context, "✓ Super-Admin Console Unlocked", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                modifier = Modifier.weight(1.3f).height(46.dp).testTag("admin_login_submit_btn")
                            ) {
                                Text("Unlock Admin", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // Active Admin Tab: 0 = Dashboard, 1 = Catalog QC, 2 = Sellers, 3 = Orders, 4 = Payouts, 5 = Analytics, 6 = Buyers & Coupons, 7 = Refunds & RBAC, 8 = Settings, 9 = Categories
    var selectedTab by remember { mutableIntStateOf(0) }
    var tab6SubSection by remember { mutableIntStateOf(0) }
    var tab7SubSection by remember { mutableIntStateOf(0) }
    var showAdminReviewsDialog by remember { mutableStateOf(false) }
    var showAdminMoreMenu by remember { mutableStateOf(false) }

    // System Toggles
    var isStoreOnline by remember { mutableStateOf(true) }
    var isCodActive by remember { mutableStateOf(true) }
    var isSellerRegistrationOpen by remember { mutableStateOf(true) }
    var isAiPriceEngineActive by remember { mutableStateOf(true) }

    // Dialog state for Rejection
    var rejectingProduct by remember { mutableStateOf<AdminQcProduct?>(null) }
    var rejectionInputText by remember { mutableStateOf("Missing complete packer address and MRP compliance tag.") }

    // Dialog state for Payout Release
    var showReleasePayoutDialog by remember { mutableStateOf(false) }

    // Dialog state for New Broadcast Notice
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastMessage by remember { mutableStateOf("") }
    var broadcastTarget by remember { mutableStateOf("All Sellers & Buyers") }
    var broadcastCategory by remember { mutableStateOf("Festival Sale Announcement") }

    // Filter states
    var qcFilter by remember { mutableStateOf("All") }
    var sellerSearchQuery by remember { mutableStateOf("") }

    // Sample QC Products
    val qcProducts = remember {
        mutableStateListOf(
            AdminQcProduct(
                id = "qc-101",
                title = "Girls Premium Net Embroidered Ethnic Gown",
                sellerName = "Gyanvi Ethnic Studio",
                sellerLocation = "Bettiah, West Champaran, Bihar",
                sku = "GWN-PNK-09",
                mrp = 999,
                sellerPrice = 425,
                category = "Ethnic Gowns",
                ageGroup = "Kids & Girls (2-8 Years)",
                ageYears = "2-3 Years, 4-5 Years, 6-7 Years",
                manufacturerName = "Gyanvi Dresses Manufacturing Unit",
                manufacturerPincode = "845438",
                packerName = "Gyanvi Dresses Packaging & Logistics Hub",
                packerPincode = "845438",
                packerId = "PKR-GD-845401",
                status = "Pending Review",
                imageRes = R.drawable.dress_anarkali_1793417013639
            ),
            AdminQcProduct(
                id = "qc-102",
                title = "Banarasi Silk Saree (Pallu Close-up)",
                sellerName = "Champaran Silk Weavers",
                sellerLocation = "Main Road, Bettiah, Bihar",
                sku = "GYANVI-ETHNIC-102",
                mrp = 1999,
                sellerPrice = 699,
                category = "Sarees",
                ageGroup = "Adults (18+ Yrs)",
                ageYears = "Adults (18+)",
                manufacturerName = "Champaran Silk Hub",
                manufacturerPincode = "845438",
                packerName = "Gyanvi Dresses Packaging & Logistics Hub",
                packerPincode = "845438",
                packerId = "PKR-GD-845402",
                status = "Pending Review",
                imageRes = R.drawable.dress_saree_1793417052582
            ),
            AdminQcProduct(
                id = "qc-103",
                title = "Designer Georgette Lehenga Choli",
                sellerName = "Bettiah Bridal Collections",
                sellerLocation = "Lal Bazar, Bettiah, Bihar",
                sku = "GYANVI-ETHNIC-103",
                mrp = 2499,
                sellerPrice = 999,
                category = "Lehenga Sets",
                ageGroup = "Teens & Adults",
                ageYears = "14-16 Years, Adults",
                manufacturerName = "Gyanvi Dresses Manufacturing Unit",
                manufacturerPincode = "845438",
                packerName = "Gyanvi Dresses Packaging & Logistics Hub",
                packerPincode = "845438",
                packerId = "PKR-GD-845403",
                status = "Approved",
                imageRes = R.drawable.dress_lehenga_1793417067159
            ),
            AdminQcProduct(
                id = "qc-104",
                title = "Chikankari Cotton Daily Wear Kurti",
                sellerName = "Patwa Handloom Store",
                sellerLocation = "Station Road, Bettiah, Bihar",
                sku = "GYANVI-ETHNIC-104",
                mrp = 899,
                sellerPrice = 379,
                category = "Kurtis",
                ageGroup = "All Ages",
                ageYears = "All Standard Sizes",
                manufacturerName = "Patwa Textile Works",
                manufacturerPincode = "845438",
                packerName = "Gyanvi Dresses Logistics",
                packerPincode = "845438",
                packerId = "PKR-GD-845404",
                status = "Approved",
                imageRes = R.drawable.dress_anarkali_1793417013639
            )
        )
    }

    // Sample Sellers
    val sellersList = remember {
        mutableStateListOf(
            AdminSellerItem("s-1", "Gyanvi Ethnic Studio", "Amit Kumar Patwa", "+91 9128754982", "Bettiah, Bihar", "10AAAAA0000A1Z5", true, 4.9, 142, 1840),
            AdminSellerItem("s-2", "Champaran Silk Weavers", "Rameshwar Prasad", "+91 9835012345", "Bettiah, Bihar", "10BBBBB1111B2Z6", true, 4.7, 85, 920),
            AdminSellerItem("s-3", "Maa Sharda Garments", "Sunita Devi", "+91 9470123456", "Motihari, Bihar", "10CCCCC2222C3Z7", false, 4.5, 40, 310),
            AdminSellerItem("s-4", "Patna Royal Sarees", "Vikram Singh", "+91 9122334455", "Patna, Bihar", "10DDDDD3333D4Z8", true, 4.8, 120, 1150)
        )
    }

    // Sample Orders
    val ordersList = remember {
        mutableStateListOf(
            AdminOrderItem("#ORD-9841", "Pooja Kumari", "Patna, Bihar", "Gyanvi Ethnic Studio", "Girls Net Ethnic Gown", 425, "Prepaid UPI", "Delhivery", "DEL-849102", "In-Transit"),
            AdminOrderItem("#ORD-9842", "Anjali Sharma", "Muzaffarpur, Bihar", "Champaran Silk Weavers", "Banarasi Silk Saree", 699, "Cash on Delivery (COD)", "BlueDart", "BLU-992314", "Packed"),
            AdminOrderItem("#ORD-9843", "Kavita Verma", "Ranchi, Jharkhand", "Gyanvi Ethnic Studio", "Peplum Kurti Set", 425, "Prepaid UPI", "Shadowfax", "SHD-441290", "Delivered"),
            AdminOrderItem("#ORD-9844", "Sneha Roy", "Kolkata, WB", "Maa Sharda Garments", "Designer Lehenga", 999, "Cash on Delivery (COD)", "Xpressbees", "XPR-110293", "RTO Returned")
        )
    }

    // Sample Payout Batches
    val payoutBatches = remember {
        mutableStateListOf(
            AdminPayoutBatch("BATCH-BTH-942", 18, 120500, "Today, 6:00 PM", "Pending Approval"),
            AdminPayoutBatch("BATCH-BTH-941", 24, 84200, "Yesterday", "Settled (NEFT)"),
            AdminPayoutBatch("BATCH-BTH-940", 31, 165400, "08 Nov 2026", "Settled (NEFT)")
        )
    }

    Scaffold(
        topBar = {
            Surface(
                color = CardWhite,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_btn")) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "GYANVI DRESSES",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = MeeshoMagenta
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFEF3C7)
                                    ) {
                                        Text(
                                            "ADMIN PANEL",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = Color(0xFFB45309),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Super-Admin Console • Bettiah Hub",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            IconButton(
                                onClick = {
                                    onLogoutAdmin()
                                    Toast.makeText(context, "Admin session locked", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(34.dp).testTag("admin_topbar_lock_btn")
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = "Lock Admin", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = onOpenBackup,
                                modifier = Modifier.size(34.dp).testTag("admin_topbar_backup_btn")
                            ) {
                                Icon(Icons.Default.Backup, contentDescription = "Backup", tint = Color(0xFF059669), modifier = Modifier.size(19.dp))
                            }
                            IconButton(
                                onClick = {
                                    adminScope.launch {
                                        adminScrollState.animateScrollTo((adminScrollState.value + 550).coerceAtMost(adminScrollState.maxValue))
                                    }
                                },
                                modifier = Modifier.size(34.dp).testTag("admin_scroll_down_btn")
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Scroll Down", tint = MeeshoMagenta, modifier = Modifier.size(22.dp))
                            }
                            // Switch to Seller Panel Button
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF3E8FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC084FC)),
                                modifier = Modifier.clickable { onOpenSellerPanel() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Seller", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                }
                            }
                            Box {
                                IconButton(
                                    onClick = { showAdminMoreMenu = true },
                                    modifier = Modifier.size(34.dp).testTag("admin_topbar_more_btn")
                                ) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = TextPrimary, modifier = Modifier.size(20.dp))
                                }
                                DropdownMenu(
                                    expanded = showAdminMoreMenu,
                                    onDismissRequest = { showAdminMoreMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Filled.Tune, contentDescription = null, tint = Color(0xFF4338CA)) },
                                        text = { Text("Master Control Panel", fontWeight = FontWeight.Bold) },
                                        onClick = { showAdminMoreMenu = false; onOpenControlPanel() }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null, tint = MeeshoMagenta) },
                                        text = { Text("Admin Dashboard") },
                                        onClick = { showAdminMoreMenu = false; selectedTab = 0 }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Outlined.Category, contentDescription = null, tint = Color(0xFF0284C7)) },
                                        text = { Text("Category Management (${categories.size})") },
                                        onClick = { showAdminMoreMenu = false; selectedTab = 9 }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Outlined.Analytics, contentDescription = null, tint = Color(0xFF7C3AED)) },
                                        text = { Text("Analytics & P&L") },
                                        onClick = { showAdminMoreMenu = false; selectedTab = 5 }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Outlined.Security, contentDescription = null, tint = Color(0xFF15803D)) },
                                        text = { Text("Security & RBAC") },
                                        onClick = { showAdminMoreMenu = false; tab7SubSection = 1; selectedTab = 7 }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Outlined.Backup, contentDescription = null, tint = Color(0xFF059669)) },
                                        text = { Text("App Backup & Restore") },
                                        onClick = { showAdminMoreMenu = false; onOpenBackup() }
                                    )
                                }
                            }
                        }
                    }

                    // Navigation Tabs (uniform Filled active / Outlined inactive icons)
                    val tabs = listOf(
                        Triple("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
                        Triple("Catalog QC (${qcProducts.count { it.status == "Pending Review" }})", Icons.Filled.FactCheck, Icons.Outlined.FactCheck),
                        Triple("Sellers (${sellersList.size})", Icons.Filled.People, Icons.Outlined.People),
                        Triple("Orders", Icons.Filled.LocalShipping, Icons.Outlined.LocalShipping),
                        Triple("Payouts", Icons.Filled.Payments, Icons.Outlined.Payments),
                        Triple("Analytics & P&L", Icons.Filled.Analytics, Icons.Outlined.Analytics),
                        Triple("Buyers & Coupons", Icons.Filled.LocalOffer, Icons.Outlined.LocalOffer),
                        Triple("Refunds & RBAC", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings),
                        Triple("Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
                        Triple("Categories (${categories.size})", Icons.Filled.Category, Icons.Outlined.Category),
                        Triple("Staff & Kaam (${staffMembers.size})", Icons.Filled.Badge, Icons.Outlined.Badge)
                    )

                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = CardWhite,
                        contentColor = MeeshoMagenta,
                        edgePadding = 12.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tabs.forEachIndexed { index, (label, filledIcon, outlinedIcon) ->
                            val isSelected = selectedTab == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTab = index },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isSelected) filledIcon else outlinedIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC),
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(adminScrollState)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // =================================================================
            // TAB 0: MASTER DASHBOARD
            // =================================================================
            if (selectedTab == 0) {
                // Admin Welcome Banner
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF831843), Color(0xFF9C1B68), Color(0xFFBE185D))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Welcome, Super Admin", color = Color(0xFFFBCFE8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("Amit Kumar Patwa", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text("Master Control Center • GYANVI DRESSES", color = Color(0xFFFDF2F8), fontSize = 11.sp)
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp))
                                }
                            }
                        }
                    }
                }

                // Unified 15-Icon Admin Panel Quick Control Hub
                val pendingQcCount = qcProducts.count { it.status == "Pending Review" }
                PanelIconActionGrid(
                    sectionTitle = "Admin Panel Quick Icons (एडमिन पैनल)",
                    sectionSubtitle = "Tap any icon to open Users, Sellers, Products, Orders & Settings",
                    accentColor = MeeshoMagenta,
                    containerBorderColor = Color(0xFFFBCFE8),
                    modifier = Modifier.testTag("admin_panel_icons_grid"),
                    actions = listOf(
                        PanelIconAction("admin_dashboard", "Admin Dashboard", "एडमिन डैशबोर्ड", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings, MeeshoMagenta, isSelected = selectedTab == 0, testTag = "admin_icon_dashboard") {
                            selectedTab = 0
                        },
                        PanelIconAction("users", "Users", "ग्राहक मैनेजमेंट", Icons.Filled.Group, Icons.Outlined.Group, Color(0xFF2563EB), testTag = "admin_icon_users") {
                            tab6SubSection = 0
                            selectedTab = 6
                        },
                        PanelIconAction("sellers", "Sellers", "सेलर मैनेजमेंट", Icons.Filled.Storefront, Icons.Outlined.Storefront, Color(0xFF7C3AED), badgeText = "${sellersList.size}", testTag = "admin_icon_sellers") {
                            selectedTab = 2
                        },
                        PanelIconAction("products", "Products", "प्रोडक्ट मैनेजमेंट", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, Color(0xFFD97706), badgeText = if (pendingQcCount > 0) "$pendingQcCount QC" else "${products.size}", testTag = "admin_icon_products") {
                            selectedTab = 1
                        },
                        PanelIconAction("categories", "Categories", "कैटेगरी मैनेजमेंट", Icons.Filled.Category, Icons.Outlined.Category, Color(0xFF0284C7), badgeText = "${categories.size}", testTag = "admin_icon_categories") {
                            selectedTab = 9
                        },
                        PanelIconAction("orders", "Orders", "सभी ऑर्डर", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag, Color(0xFF4338CA), badgeText = "${(liveOrders.size + ordersList.size)}", testTag = "admin_icon_orders") {
                            selectedTab = 3
                        },
                        PanelIconAction("payments", "Payments", "पेमेंट मैनेजमेंट", Icons.Filled.Payments, Icons.Outlined.Payments, Color(0xFF15803D), testTag = "admin_icon_payments") {
                            selectedTab = 4
                        },
                        PanelIconAction("delivery", "Delivery", "डिलीवरी मैनेजमेंट", Icons.Filled.LocalShipping, Icons.Outlined.LocalShipping, Color(0xFF0F766E), testTag = "admin_icon_delivery") {
                            selectedTab = 3
                        },
                        PanelIconAction("coupons", "Coupons", "कूपन मैनेजमेंट", Icons.Filled.ConfirmationNumber, Icons.Outlined.ConfirmationNumber, Color(0xFFDB2777), testTag = "admin_icon_coupons") {
                            tab6SubSection = 1
                            selectedTab = 6
                        },
                        PanelIconAction("offers", "Offers", "ऑफर मैनेजमेंट", Icons.Filled.LocalOffer, Icons.Outlined.LocalOffer, Color(0xFFEA580C), testTag = "admin_icon_offers") {
                            tab6SubSection = 2
                            selectedTab = 6
                        },
                        PanelIconAction("reviews", "Reviews", "ग्राहक रिव्यू", Icons.Filled.Star, Icons.Outlined.Star, Color(0xFFF59E0B), badgeText = "${products.size}", testTag = "admin_icon_reviews") {
                            showAdminReviewsDialog = true
                        },
                        PanelIconAction("analytics", "Analytics", "बिजनेस एनालिटिक्स", Icons.Filled.Analytics, Icons.Outlined.Analytics, Color(0xFF6D28D9), testTag = "admin_icon_analytics") {
                            selectedTab = 5
                        },
                        PanelIconAction("settings", "Settings", "सेटिंग", Icons.Filled.Settings, Icons.Outlined.Settings, Color(0xFF475569), testTag = "admin_icon_settings") {
                            selectedTab = 8
                        },
                        PanelIconAction("security", "Security", "सुरक्षा", Icons.Filled.Security, Icons.Outlined.Security, Color(0xFF16A34A), testTag = "admin_icon_security") {
                            tab7SubSection = 1
                            selectedTab = 7
                        },
                        PanelIconAction("support", "Support", "ग्राहक सहायता", Icons.Filled.SupportAgent, Icons.Outlined.SupportAgent, Color(0xFF0284C7), testTag = "admin_icon_support") {
                            tab7SubSection = 0
                            selectedTab = 7
                        },
                        PanelIconAction("staff_work", "Staff & Kaam", "कर्मचारी बाँटें", Icons.Filled.Badge, Icons.Outlined.Badge, Color(0xFF059669), badgeText = "${staffMembers.size}", testTag = "admin_icon_staff_kaam") {
                            selectedTab = 10
                        }
                    )
                )

                // Master App Management Studio Card right inside Admin Dashboard
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6EE7B7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenAppManagement(0) }
                        .testTag("admin_open_app_management_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "App Management & Live Edit Dashboard",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF065F46)
                                )
                                Text(
                                    text = "Change Dashboard • Image Change • Text Change • लिखावट • Edit/Delete Options",
                                    fontSize = 11.sp,
                                    color = Color(0xFF047857)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.End) {
                                Button(
                                    onClick = onOpenControlPanel,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp).testTag("admin_open_control_panel_btn")
                                ) {
                                    Text("Control Panel", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Button(
                                    onClick = { onOpenAppManagement(0) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Open Editor", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = onOpenBackup,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp).testTag("admin_open_backup_btn")
                                ) {
                                    Icon(Icons.Default.Backup, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Backup", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                                }
                            }
                        }
                    }
                }

                // 8 Required Admin Dashboard Live KPIs + Financial Metrics
                val pendingOrdersKpi = liveOrders.count {
                    it.status in listOf(OrderStatus.ORDERED, OrderStatus.CONFIRMED, OrderStatus.PROCESSING, OrderStatus.READY_TO_SHIP)
                }
                val completedOrdersKpi = liveOrders.count { it.status == OrderStatus.DELIVERED }
                val cancelledOrdersKpi = liveOrders.count { it.status == OrderStatus.CANCELLED }
                val returnOrdersKpi = liveOrders.count {
                    it.status in listOf(OrderStatus.RETURN_REQUESTED, OrderStatus.RETURNED, OrderStatus.REFUNDED, OrderStatus.RETURN)
                }
                val liveGmv = liveOrders.sumOf { it.totalAmount }

                Text("Live Marketplace Overview (8 प्रमुख आँकड़े)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminMetricCard(
                        title = "Total Users",
                        value = "${registeredUsersCount.coerceAtLeast(1)}",
                        subtitle = "Verified Buyers & Guests",
                        tint = Color(0xFF2563EB),
                        icon = Icons.Default.Group,
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        title = "Total Sellers",
                        value = "${sellersList.size}",
                        subtitle = "${sellersList.count { it.kycStatus == "Pending KYC" }} Pending KYC",
                        tint = Color(0xFF7C3AED),
                        icon = Icons.Default.Storefront,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminMetricCard(
                        title = "Total Products",
                        value = "${products.size}",
                        subtitle = "${qcProducts.count { it.status == "Pending Review" }} In QC Review",
                        tint = Color(0xFFD97706),
                        icon = Icons.Default.Checkroom,
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        title = "Total Orders",
                        value = "${liveOrders.size}",
                        subtitle = "GMV ₹$liveGmv",
                        tint = MeeshoMagenta,
                        icon = Icons.Default.ShoppingBag,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminMetricCard(
                        title = "Pending Orders",
                        value = "$pendingOrdersKpi",
                        subtitle = "Pending / Ready to Ship",
                        tint = Color(0xFFEA580C),
                        icon = Icons.Default.PendingActions,
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        title = "Completed Orders",
                        value = "$completedOrdersKpi",
                        subtitle = "Delivered Successfully",
                        tint = Color(0xFF16A34A),
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminMetricCard(
                        title = "Cancelled Orders",
                        value = "$cancelledOrdersKpi",
                        subtitle = "Stock Auto-Restored",
                        tint = Color(0xFFDC2626),
                        icon = Icons.Default.Cancel,
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        title = "Return Orders",
                        value = "$returnOrdersKpi",
                        subtitle = "Returns & Refunds",
                        tint = Color(0xFF9333EA),
                        icon = Icons.Default.AssignmentReturn,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminMetricCard(
                        title = "Commission Rate",
                        value = "${commissionConfig.globalPercent}%",
                        subtitle = "Saree ${commissionConfig.sareePercent}% • Kurti ${commissionConfig.kurtiPercent}%",
                        tint = Color(0xFF059669),
                        icon = Icons.Default.Percent,
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        title = "Due Seller Payouts",
                        value = "₹2,45,800",
                        subtitle = "Release Today 6 PM",
                        tint = Color(0xFF0F766E),
                        icon = Icons.Default.Payment,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Live Platform Master Controls
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Live Platform System Status", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Storefront Status (ऑनलाइन स्टोर)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(if (isStoreOnline) "Store is Live for all Buyers" else "Maintenance Mode Active", fontSize = 11.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = isStoreOnline,
                                onCheckedChange = {
                                    isStoreOnline = it
                                    Toast.makeText(context, if (it) "Storefront is now LIVE" else "Storefront placed in MAINTENANCE mode", Toast.LENGTH_SHORT).show()
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF16A34A))
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Cash On Delivery (COD) Checkout", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(if (isCodActive) "COD enabled across 19,000+ pin codes" else "Only Prepaid orders allowed", fontSize = 11.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = isCodActive,
                                onCheckedChange = { isCodActive = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF16A34A))
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("New Seller Registrations", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Allow new fashion sellers to register", fontSize = 11.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = isSellerRegistrationOpen,
                                onCheckedChange = { isSellerRegistrationOpen = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF16A34A))
                            )
                        }
                    }
                }

                // Quick Action Buttons
                Text("Quick Admin Actions (त्वरित कार्य)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { selectedTab = 1 },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Review QC (2)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showReleasePayoutDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Release Payouts", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = { showBroadcastDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Broadcast Announcement to All Sellers & Buyers", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MeeshoMagenta)
                }
            }

            // =================================================================
            // TAB 1: CATALOG QUALITY CONTROL (QC) MANAGEMENT
            // =================================================================
            if (selectedTab == 1) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Product Quality Control & Approvals", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Text(
                            "Review newly uploaded catalogs for mandatory legal info (Age, Years, Manufacturer Name/Pincode, Packer details, MRP).",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        // QC Filter Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("All", "Pending Review", "Approved", "Rejected").forEach { filter ->
                                val isSel = qcFilter == filter
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSel) Color(0xFFFDF2F8) else Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) MeeshoMagenta else Color(0xFFE2E8F0)),
                                    modifier = Modifier.clickable { qcFilter = filter }
                                ) {
                                    Text(
                                        filter,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) MeeshoMagenta else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                val filteredQc = qcProducts.filter {
                    if (qcFilter == "All") true else it.status == qcFilter
                }

                if (filteredQc.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No items found in $qcFilter", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                filteredQc.forEach { prod ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = when (prod.status) {
                                            "Approved" -> Color(0xFFDCFCE7)
                                            "Rejected" -> Color(0xFFFEE2E2)
                                            else -> Color(0xFFFEF3C7)
                                        }
                                    ) {
                                        Text(
                                            prod.status,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = when (prod.status) {
                                                "Approved" -> Color(0xFF16A34A)
                                                "Rejected" -> Color(0xFFDC2626)
                                                else -> Color(0xFFB45309)
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SKU: ${prod.sku}", fontSize = 10.sp, color = TextSecondary)
                                }
                                Text("Seller: ${prod.sellerName}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            }

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Image(
                                    painter = painterResource(prod.imageRes),
                                    contentDescription = prod.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("MRP: ₹${prod.mrp} • Seller Price: ₹${prod.sellerPrice}", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                    Text("Category: ${prod.category}", fontSize = 11.sp, color = TextSecondary)
                                }
                            }

                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            // Mandatory Compliance Info Preview
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("Legal Compliance Checks (अनिवार्य विवरण):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextPrimary)
                                Text("• Age Group: ${prod.ageGroup} | Years: ${prod.ageYears}", fontSize = 11.sp, color = TextSecondary)
                                Text("• Manufacturer: ${prod.manufacturerName} (${prod.manufacturerPincode})", fontSize = 11.sp, color = TextSecondary)
                                Text("• Packer Name: ${prod.packerName} (${prod.packerPincode})", fontSize = 11.sp, color = TextSecondary)
                                Text("• Packer ID: ${prod.packerId}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }

                            if (prod.status == "Rejected" && prod.rejectionReason.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEF2F2),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Rejection Reason: ${prod.rejectionReason}", color = Color(0xFFB91C1C), fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                                }
                            }

                            // Action buttons
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (prod.status != "Approved") {
                                    Button(
                                        onClick = {
                                            prod.status = "Approved"
                                            Toast.makeText(context, "✓ '${prod.title}' को पास और लाइव कर दिया गया!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve (पास करें)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (prod.status != "Rejected") {
                                    OutlinedButton(
                                        onClick = { rejectingProduct = prod },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reject (अस्वीकार)", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 2: SELLER MANAGEMENT & KYC APPROVAL
            // =================================================================
            if (selectedTab == 2) {
                var sellerKycFilter by remember { mutableStateOf("All") }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Seller Management & KYC Approval (${sellersList.size} Sellers)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )

                        OutlinedTextField(
                            value = sellerSearchQuery,
                            onValueChange = { sellerSearchQuery = it },
                            placeholder = { Text("Search by Store Name, Owner, GSTIN...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MeeshoMagenta) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("All", "Pending KYC", "Approved", "Rejected").forEach { kFilter ->
                                FilterChip(
                                    selected = sellerKycFilter == kFilter,
                                    onClick = { sellerKycFilter = kFilter },
                                    label = { Text(kFilter, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                                )
                            }
                        }
                    }
                }

                val filteredSellers = sellersList.filter { s ->
                    val matchesSearch = sellerSearchQuery.isBlank() ||
                        s.storeName.contains(sellerSearchQuery, ignoreCase = true) ||
                        s.ownerName.contains(sellerSearchQuery, ignoreCase = true) ||
                        s.gstin.contains(sellerSearchQuery, ignoreCase = true)
                    val matchesKyc = sellerKycFilter == "All" || s.kycStatus == sellerKycFilter
                    matchesSearch && matchesKyc
                }

                filteredSellers.forEachIndexed { idx, seller ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(seller.storeName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Text("Owner: ${seller.ownerName} • ${seller.city}", fontSize = 11.sp, color = TextSecondary)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = when (seller.kycStatus) {
                                            "Approved" -> Color(0xFFDCFCE7)
                                            "Rejected" -> Color(0xFFFEE2E2)
                                            else -> Color(0xFFFEF3C7)
                                        }
                                    ) {
                                        Text(
                                            seller.kycStatus,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = when (seller.kycStatus) {
                                                "Approved" -> Color(0xFF16A34A)
                                                "Rejected" -> Color(0xFFDC2626)
                                                else -> Color(0xFFB45309)
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (seller.isSuspended) Color(0xFFFEE2E2) else Color(0xFFDBEAFE)
                                    ) {
                                        Text(
                                            if (seller.isSuspended) "Suspended" else "Active",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = if (seller.isSuspended) Color(0xFFDC2626) else Color(0xFF1D4ED8),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📞 ${seller.phone}", fontSize = 11.sp, color = TextPrimary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("${seller.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("GSTIN: ${seller.gstin}", fontSize = 11.sp, color = if (seller.isGstVerified) Color(0xFF16A34A) else Color(0xFFDC2626))
                                Text("Orders: ${seller.totalOrders} | Catalogs: ${seller.activeProducts}", fontSize = 11.sp, color = TextSecondary)
                            }

                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (seller.kycStatus != "Approved") {
                                    Button(
                                        onClick = {
                                            val realIdx = sellersList.indexOf(seller)
                                            if (realIdx >= 0) sellersList[realIdx] = seller.copy(kycStatus = "Approved", isGstVerified = true)
                                            Toast.makeText(context, "✓ KYC Approved for ${seller.storeName}", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text("Approve KYC", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (seller.kycStatus != "Rejected") {
                                    OutlinedButton(
                                        onClick = {
                                            val realIdx = sellersList.indexOf(seller)
                                            if (realIdx >= 0) sellersList[realIdx] = seller.copy(kycStatus = "Rejected")
                                            Toast.makeText(context, "KYC Rejected for ${seller.storeName}", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text("Reject KYC", fontSize = 11.sp, color = Color(0xFFDC2626))
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val realIdx = sellersList.indexOf(seller)
                                        if (realIdx >= 0) sellersList[realIdx] = seller.copy(isSuspended = !seller.isSuspended)
                                        Toast.makeText(context, "${seller.storeName} status updated!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (seller.isSuspended) Color(0xFF16A34A) else Color(0xFFDC2626)
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(36.dp)
                                ) {
                                    Text(if (seller.isSuspended) "Activate Store" else "Suspend Store", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Opening Storefront for ${seller.storeName}", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(36.dp)
                                ) {
                                    Text("View Store", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 3: ORDERS, 11-STAGE WORKFLOW & DELIVERY SYSTEM
            // =================================================================
            if (selectedTab == 3) {
                var selectedOrderStatusFilter by remember { mutableStateOf(OrderStatus.ALL) }
                val shippedLiveCount = liveOrders.count {
                    (it.status == OrderStatus.SHIPPED || it.status == OrderStatus.OUT_FOR_DELIVERY) && !it.isRtoFlagged
                }
                val delayedLiveCount = liveOrders.count { it.isDeliveryDelayed }
                val rtoLiveCount = liveOrders.count { it.isRtoFlagged }
                val deliveredLiveCount = liveOrders.count { it.status == OrderStatus.DELIVERED }
                val prepaidCount = liveOrders.count { !it.paymentMethod.contains("COD", true) }
                val codCount = liveOrders.size - prepaidCount
                val totalShippingRevenue = liveOrders.sumOf { it.shippingCharges }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("11-Stage Order & Delivery Control", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                Text(
                                    "Live: ${liveOrders.size} • In-Transit: $shippedLiveCount • Delayed: $delayedLiveCount • RTO: $rtoLiveCount • Delivered: $deliveredLiveCount",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = autoTrackingEnabled,
                                onCheckedChange = onToggleAutoTracking,
                                modifier = Modifier.testTag("admin_auto_tracking_switch")
                            )
                        }

                        // Delivery Reports Summary Strip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF0F9FF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Prepaid: $prepaidCount | COD: $codCount", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0369A1))
                                Text("Shipping Collected: ₹$totalShippingRevenue", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                            }
                        }

                        // 11-Status Horizontal Filter Pills
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            (listOf(OrderStatus.ALL) + OrderStatus.workflowElevenStatuses).forEach { st ->
                                val isSel = selectedOrderStatusFilter == st
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedOrderStatusFilter = st },
                                    label = { Text(st.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MeeshoMagenta,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        if (liveOrders.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    onSyncLiveTracking()
                                    Toast.makeText(context, "🔄 Live courier tracking refreshed for Admin & Customers!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(38.dp).testTag("admin_sync_live_tracking_btn")
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF1565C0))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Live Courier Tracking Updates", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                            }
                        }
                    }
                }

                if (liveOrders.isNotEmpty()) {
                    val orderedForAdmin = liveOrders.filter { ord ->
                        selectedOrderStatusFilter == OrderStatus.ALL || ord.status == selectedOrderStatusFilter
                    }.sortedWith(
                        compareByDescending<OrderItem> { it.status == OrderStatus.SHIPPED || it.status == OrderStatus.OUT_FOR_DELIVERY }
                            .thenByDescending { it.isRtoFlagged }
                            .thenByDescending { it.isDeliveryDelayed }
                    )
                    orderedForAdmin.forEach { ord ->
                        var showStageMenu by remember { mutableStateOf(false) }
                        var shippingInput by remember(ord.orderId, ord.shippingCharges) { mutableStateOf(ord.shippingCharges.toString()) }
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth().testTag("admin_live_order_card_${ord.orderId}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("#${ord.orderId}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MeeshoMagenta)
                                    val badgeLabel = if (ord.isRtoFlagged) "RTO Tracking" else ord.status.label
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = when {
                                            ord.isRtoFlagged || ord.status == OrderStatus.CANCELLED -> Color(0xFFFEE2E2)
                                            ord.status == OrderStatus.DELIVERED || ord.status == OrderStatus.REFUNDED -> Color(0xFFDCFCE7)
                                            ord.status == OrderStatus.SHIPPED || ord.status == OrderStatus.OUT_FOR_DELIVERY -> Color(0xFFDBEAFE)
                                            else -> Color(0xFFFEF3C7)
                                        }
                                    ) {
                                        Text(
                                            badgeLabel,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = when {
                                                ord.isRtoFlagged || ord.status == OrderStatus.CANCELLED -> Color(0xFFDC2626)
                                                ord.status == OrderStatus.DELIVERED || ord.status == OrderStatus.REFUNDED -> Color(0xFF16A34A)
                                                ord.status == OrderStatus.SHIPPED || ord.status == OrderStatus.OUT_FOR_DELIVERY -> Color(0xFF1D4ED8)
                                                else -> Color(0xFFB45309)
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text("Product: ${ord.product.name} (${ord.selectedSize}, ${ord.selectedColor})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
                                Text("Customer: ${ord.customerName} • ${ord.customerPhone} (${ord.city.ifBlank { "Bettiah" }} - ${ord.pinCode.ifBlank { "845438" }})", fontSize = 11.sp, color = TextSecondary)
                                Text("Status: ${ord.statusText} • Live: ${ord.currentTrackingLocation}", fontSize = 11.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("₹${ord.totalAmount} (${ord.paymentMethod}) • Ship: ₹${ord.shippingCharges}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                    Text("${ord.courierPartner}: ${ord.awbNumber.ifBlank { "Assigning AWB" }}", fontSize = 11.sp, color = Color(0xFF2563EB))
                                }

                                if (ord.isDeliveryDelayed) {
                                    Text("⚠️ Delayed Delivery Alert Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                }

                                // Shipping Charges Inline Editor
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = shippingInput,
                                        onValueChange = { ch -> shippingInput = ch.filter { it.isDigit() } },
                                        label = { Text("Shipping ₹", fontSize = 10.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.width(105.dp)
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            val valInt = shippingInput.toIntOrNull() ?: 0
                                            onUpdateOrderShippingCharges(ord.orderId, valInt)
                                            Toast.makeText(context, "Shipping updated to ₹$valInt", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Text("Save Ship", fontSize = 11.sp)
                                    }
                                    Spacer(Modifier.weight(1f))
                                    OutlinedButton(
                                        onClick = {
                                            HoldOrderPdfGenerator.saveShippingLabelPdfToDownloads(context, ord)
                                        },
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Label PDF", fontSize = 11.sp)
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box {
                                        Button(
                                            onClick = { showStageMenu = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("Set Stage (${ord.status.label})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        DropdownMenu(
                                            expanded = showStageMenu,
                                            onDismissRequest = { showStageMenu = false }
                                        ) {
                                            OrderStatus.workflowElevenStatuses.forEach { st ->
                                                DropdownMenuItem(
                                                    text = { Text("${st.label} (${st.hindiLabel})", fontSize = 12.sp) },
                                                    onClick = {
                                                        showStageMenu = false
                                                        onUpdateOrderStatusAdmin(ord.orderId, st)
                                                        Toast.makeText(context, "#${ord.orderId} -> ${st.label}", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    TextButton(
                                        onClick = {
                                            onAdvanceTracking(ord.orderId)
                                            Toast.makeText(context, "✓ Tracking updated for #${ord.orderId}", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("Advance", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                    }

                                    TextButton(
                                        onClick = {
                                            onScheduleReturnPickup(ord.orderId)
                                            Toast.makeText(context, "Return Pickup Scheduled for #${ord.orderId}", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("Return Pickup", fontSize = 11.sp, color = Color(0xFF7C3AED))
                                    }

                                    TextButton(
                                        onClick = {
                                            onToggleDelayAlert(ord.orderId, !ord.isDeliveryDelayed, "Admin Highway Delay Flag")
                                        }
                                    ) {
                                        Text(
                                            if (ord.isDeliveryDelayed) "Clear Delay" else "Delay Alert",
                                            fontSize = 11.sp,
                                            color = Color(0xFFE65100)
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            onMarkRto(ord.orderId, "Admin Initiated RTO Tracking")
                                        }
                                    ) {
                                        Text("RTO", fontSize = 11.sp, color = Color(0xFFDC2626))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    ordersList.forEach { ord ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(ord.orderId, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MeeshoMagenta)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = when (ord.deliveryStatus) {
                                            "Delivered" -> Color(0xFFDCFCE7)
                                            "RTO Returned" -> Color(0xFFFEE2E2)
                                            "In-Transit" -> Color(0xFFDBEAFE)
                                            else -> Color(0xFFFEF3C7)
                                        }
                                    ) {
                                        Text(
                                            ord.deliveryStatus,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = when (ord.deliveryStatus) {
                                                "Delivered" -> Color(0xFF16A34A)
                                                "RTO Returned" -> Color(0xFFDC2626)
                                                "In-Transit" -> Color(0xFF1D4ED8)
                                                else -> Color(0xFFB45309)
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text("Product: ${ord.productTitle}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
                                Text("Customer: ${ord.customerName} (${ord.customerCity})", fontSize = 11.sp, color = TextSecondary)
                                Text("Seller: ${ord.storeName}", fontSize = 11.sp, color = TextSecondary)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Amount: ₹${ord.amount} (${ord.paymentType})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                    Text("${ord.courier}: ${ord.awbNumber}", fontSize = 11.sp, color = Color(0xFF2563EB))
                                }

                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(
                                        onClick = {
                                            ord.deliveryStatus = "Delivered"
                                            Toast.makeText(context, "${ord.orderId} marked as Delivered!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("Mark Delivered", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                    }
                                    TextButton(
                                        onClick = {
                                            Toast.makeText(context, "Tracking parcel on ${ord.courier} portal...", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("Track Courier", fontSize = 11.sp, color = MeeshoMagenta)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 4: FINANCIALS & PAYOUTS
            // =================================================================
            if (selectedTab == 4) {
                PaymentGatewayConnectorSection(
                    config = paymentGatewayConfig,
                    onSaveAndConnect = onSaveAndConnectPaymentGateway,
                    onConnectGateway = onConnectPaymentGateway,
                    onDisconnectGateway = onDisconnectPaymentGateway,
                    onDeleteGateway = onDeletePaymentGateway,
                    onToggleAppendParams = onToggleAppendPaymentParams,
                    modifier = Modifier.testTag("admin_payment_gateway_connector")
                )

                // Commission Management Card (कमीशन मैनेजमेंट)
                var globalCommInput by remember(commissionConfig.globalPercent) { mutableStateOf(commissionConfig.globalPercent.toString()) }
                var sareeCommInput by remember(commissionConfig.sareePercent) { mutableStateOf(commissionConfig.sareePercent.toString()) }
                var kurtiCommInput by remember(commissionConfig.kurtiPercent) { mutableStateOf(commissionConfig.kurtiPercent.toString()) }
                var lehengaCommInput by remember(commissionConfig.lehengaPercent) { mutableStateOf(commissionConfig.lehengaPercent.toString()) }
                var kidsCommInput by remember(commissionConfig.kidsPercent) { mutableStateOf(commissionConfig.kidsPercent.toString()) }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Commission Management (कमीशन मैनेजमेंट)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Text("Configure marketplace seller commission percentages globally and by dress category.", fontSize = 11.sp, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = globalCommInput,
                                onValueChange = { globalCommInput = it },
                                label = { Text("Global %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sareeCommInput,
                                onValueChange = { sareeCommInput = it },
                                label = { Text("Saree %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = kurtiCommInput,
                                onValueChange = { kurtiCommInput = it },
                                label = { Text("Kurti %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = lehengaCommInput,
                                onValueChange = { lehengaCommInput = it },
                                label = { Text("Lehenga %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = kidsCommInput,
                                onValueChange = { kidsCommInput = it },
                                label = { Text("Kids %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Button(
                            onClick = {
                                onUpdateCommissionConfig { cur ->
                                    cur.copy(
                                        globalPercent = globalCommInput.toFloatOrNull() ?: cur.globalPercent,
                                        sareePercent = sareeCommInput.toFloatOrNull() ?: cur.sareePercent,
                                        kurtiPercent = kurtiCommInput.toFloatOrNull() ?: cur.kurtiPercent,
                                        lehengaPercent = lehengaCommInput.toFloatOrNull() ?: cur.lehengaPercent,
                                        kidsPercent = kidsCommInput.toFloatOrNull() ?: cur.kidsPercent
                                    )
                                }
                                Toast.makeText(context, "✓ Commission slabs updated live!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Commission Slabs", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Seller Payouts & Settlement Center", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Text("Automated NEFT/IMPS payout release for fulfilled and non-returned orders.", fontSize = 11.sp, color = TextSecondary)

                        Button(
                            onClick = { showReleasePayoutDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Release All Due Payouts (₹2,45,800)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Text("Payout Batches (पेआउट बैच)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                payoutBatches.forEach { batch ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(batch.batchId, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (batch.payoutStatus == "Settled (NEFT)") Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                ) {
                                    Text(
                                        batch.payoutStatus,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (batch.payoutStatus == "Settled (NEFT)") Color(0xFF16A34A) else Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Amount: ₹${batch.totalAmount}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF16A34A))
                                Text("Sellers: ${batch.totalSellers}", fontSize = 12.sp, color = TextSecondary)
                            }

                            Text("Date: ${batch.scheduledDate}", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // =================================================================
            // TAB 5: ANALYTICS & P&L
            // =================================================================
            if (selectedTab == 5) {
                AdminAnalyticsReportsModule()
            }

            // =================================================================
            // TAB 6: BUYERS, BANNERS & COUPONS
            // =================================================================
            if (selectedTab == 6) {
                AdminBuyersCouponsBannersModule(initialSubSection = tab6SubSection)
                SocialMediaAdsManagerSection(
                    config = socialAdsConfig,
                    accentColor = MeeshoMagenta,
                    onToggleAdsEnabled = onToggleSocialAdsEnabled,
                    onToggleShowOnHome = onToggleSocialAdsHome,
                    onSaveCampaign = onSaveSocialAdCampaign,
                    onToggleCampaignActive = onToggleSocialAdActive,
                    onDeleteCampaign = onDeleteSocialAdCampaign,
                    onRecordAdClick = onRecordSocialAdClick,
                    modifier = Modifier.testTag("admin_offers_social_ads")
                )
            }

            // =================================================================
            // TAB 7: REFUNDS, RTO, COMPLAINTS & RBAC SECURITY
            // =================================================================
            if (selectedTab == 7) {
                AdminRefundsRtoAndSecurityModule(initialSubSection = tab7SubSection)
            }

            // =================================================================
            // TAB 8: BROADCAST, NOTIFICATION MGMT, WEBSITE & LEGAL POLICIES
            // =================================================================
            if (selectedTab == 8) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Notification Management & Abandoned Cart", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Text("Publish instant platform-wide festival broadcasts or trigger abandoned cart recovery reminders.", fontSize = 11.sp, color = TextSecondary)

                        Button(
                            onClick = { showBroadcastDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create & Publish Broadcast Notice", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                onTriggerAbandonedCartAlert()
                                Toast.makeText(context, "🛒 Abandoned Cart Recovery notification dispatched!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp).testTag("admin_abandoned_cart_btn")
                        ) {
                            Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null, tint = Color(0xFFD97706))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Trigger Abandoned Cart Notification", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFD97706))
                        }
                    }
                }

                // Website Settings & Mobile App + Website Integration
                var webDomainInput by remember(websiteConfig.websiteDomain) { mutableStateOf(websiteConfig.websiteDomain) }
                var webTitleInput by remember(websiteConfig.webStoreTitle) { mutableStateOf(websiteConfig.webStoreTitle) }
                var deepLinkInput by remember(websiteConfig.deepLinkBaseUri) { mutableStateOf(websiteConfig.deepLinkBaseUri) }
                var webSyncChecked by remember(websiteConfig.webCheckoutSyncEnabled) { mutableStateOf(websiteConfig.webCheckoutSyncEnabled) }
                var maintModeChecked by remember(websiteConfig.maintenanceMode) { mutableStateOf(websiteConfig.maintenanceMode) }
                var termsInput by remember(websiteConfig.termsAndConditions) { mutableStateOf(websiteConfig.termsAndConditions) }
                var privacyInput by remember(websiteConfig.privacyPolicy) { mutableStateOf(websiteConfig.privacyPolicy) }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Website Settings & App-Web Sync", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        OutlinedTextField(
                            value = webDomainInput,
                            onValueChange = { webDomainInput = it },
                            label = { Text("Website Domain URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = webTitleInput,
                            onValueChange = { webTitleInput = it },
                            label = { Text("Web Storefront Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = deepLinkInput,
                            onValueChange = { deepLinkInput = it },
                            label = { Text("App Deep Link Base URI") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("Sync Mobile App & Website Orders", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Switch(checked = webSyncChecked, onCheckedChange = { webSyncChecked = it })
                        }
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("Website Maintenance Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Switch(checked = maintModeChecked, onCheckedChange = { maintModeChecked = it })
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Text("Terms & Conditions + Privacy Policy Editor", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MeeshoMagentaDark)
                        OutlinedTextField(
                            value = termsInput,
                            onValueChange = { termsInput = it },
                            label = { Text("Terms & Conditions (नियम व शर्तें)") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = privacyInput,
                            onValueChange = { privacyInput = it },
                            label = { Text("Privacy Policy (गोपनीयता नीति)") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                onUpdateWebsiteConfig { cur ->
                                    cur.copy(
                                        websiteDomain = webDomainInput.trim(),
                                        webStoreTitle = webTitleInput.trim(),
                                        deepLinkBaseUri = deepLinkInput.trim(),
                                        webCheckoutSyncEnabled = webSyncChecked,
                                        maintenanceMode = maintModeChecked,
                                        termsAndConditions = termsInput.trim(),
                                        privacyPolicy = privacyInput.trim()
                                    )
                                }
                                Toast.makeText(context, "✓ Website, App Integration & Legal Policies Saved!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Website & Legal Policies", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Platform Security & App Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Smart Price Recommendation AI", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Active market price optimization", fontSize = 11.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = isAiPriceEngineActive,
                                onCheckedChange = { isAiPriceEngineActive = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF16A34A))
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        Text("Admin Credentials & Authorized Hub:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                        Text("• Head Office: Lal Bazar, Bettiah, West Champaran, Bihar - 845438", fontSize = 11.sp, color = TextSecondary)
                        Text("• Support Helpline: +91 9128754982 | kumaramitbth4@gmail.com", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                PaymentGatewayConnectorSection(
                    config = paymentGatewayConfig,
                    onSaveAndConnect = onSaveAndConnectPaymentGateway,
                    onConnectGateway = onConnectPaymentGateway,
                    onDisconnectGateway = onDisconnectPaymentGateway,
                    onDeleteGateway = onDeletePaymentGateway,
                    onToggleAppendParams = onToggleAppendPaymentParams,
                    modifier = Modifier.testTag("admin_settings_gateway_connector")
                )

                SocialMediaAdsManagerSection(
                    config = socialAdsConfig,
                    accentColor = MeeshoMagenta,
                    onToggleAdsEnabled = onToggleSocialAdsEnabled,
                    onToggleShowOnHome = onToggleSocialAdsHome,
                    onSaveCampaign = onSaveSocialAdCampaign,
                    onToggleCampaignActive = onToggleSocialAdActive,
                    onDeleteCampaign = onDeleteSocialAdCampaign,
                    onRecordAdClick = onRecordSocialAdClick,
                    modifier = Modifier.testTag("admin_settings_social_ads")
                )
            }

            if (selectedTab == 9) {
                AdminCategoryManagementTab(
                    categories = categories,
                    subCategories = subCategories,
                    products = products,
                    onAddMainCategory = onAddMainCategory,
                    onEditMainCategory = onEditMainCategory,
                    onToggleCategoryActive = onToggleCategoryActive,
                    onMoveCategorySortOrder = onMoveCategorySortOrder,
                    onDeleteCategory = onDeleteCategory,
                    onAddSubCategory = onAddSubCategory,
                    onEditSubCategory = onEditSubCategory,
                    onToggleSubCategoryActive = onToggleSubCategoryActive,
                    onDeleteSubCategory = onDeleteSubCategory,
                    onUpdateProductCategories = onUpdateProductCategories,
                    onAddProduct = onAddCatalogProduct,
                    onDeleteProduct = onDeleteCatalogProduct,
                    onResetCategories = onResetCategories
                )
            }

            // =================================================================
            // TAB 10: OWNER STAFF, ATTENDANCE, SALARY, PERMISSIONS & ACTIVITY
            // =================================================================
            if (selectedTab == 10) {
                var newEmpName by remember { mutableStateOf("") }
                var newEmpPhone by remember { mutableStateOf("") }
                var newEmpPin by remember { mutableStateOf("1234") }
                var newEmpSalary by remember { mutableStateOf("18000") }
                var newEmpDept by remember { mutableStateOf(StaffDepartment.ORDER_PACKER) }

                var newTaskTitle by remember { mutableStateOf("") }
                var newTaskDesc by remember { mutableStateOf("") }
                var newTaskPriority by remember { mutableStateOf("High") }
                var newTaskAssigneeId by remember(staffMembers) { mutableStateOf(staffMembers.firstOrNull()?.id ?: "") }

                val totalPayroll = staffMembers.sumOf { it.monthlySalary }
                val paidPayroll = staffMembers.filter { it.isSalaryPaidThisMonth }.sumOf { it.monthlySalary }
                val presentCount = staffMembers.count { it.attendanceStatus == "Present" }
                val liveGmvTotal = liveOrders.sumOf { it.totalAmount }
                val estCommission = (liveGmvTotal * commissionConfig.globalPercent / 100f).toInt()
                val estGstTotal = liveOrders.sumOf { ord ->
                    if (ord.totalAmount <= 1000) (ord.totalAmount * 5) / 105 else (ord.totalAmount * 12) / 112
                }
                val estNetProfit = (estCommission + (liveGmvTotal * 0.14).toInt()) - totalPayroll

                // 1. Top Banner + 9-Account Summary + Open Staff Workspace Button
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Owner Staff & Work Division Console", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF14532D))
                                Text("$presentCount/${staffMembers.size} Present Today • ${staffTasks.count { it.status != "Completed" }} Pending Tasks", fontSize = 11.5.sp, color = Color(0xFF166534))
                            }
                            Button(
                                onClick = onOpenStaffWorkspace,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("admin_open_staff_workspace_btn")
                            ) {
                                Text("Staff Desk →", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        RoleDivisionMatrixCard()
                    }
                }

                // 2. Owner Financial P&L, Payroll & GST Summary Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Business P&L, Payroll & GST Report (मालिक वित्तीय रिपोर्ट)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text("• Live Gross Sales (GMV): ₹$liveGmvTotal (${liveOrders.size} Orders)", fontSize = 12.sp, color = TextPrimary)
                        Text("• Marketplace Commission (${commissionConfig.globalPercent}%): ₹$estCommission", fontSize = 12.sp, color = Color(0xFF7C3AED), fontWeight = FontWeight.SemiBold)
                        Text("• Apparel GST Collected (5%/12% Slabs): ₹$estGstTotal", fontSize = 12.sp, color = Color(0xFF0369A1), fontWeight = FontWeight.SemiBold)
                        Text("• Staff Payroll Budget: ₹$totalPayroll/mo (Paid: ₹$paidPayroll)", fontSize = 12.sp, color = Color(0xFFB45309), fontWeight = FontWeight.SemiBold)
                        Text(
                            "• Estimated Net Operating Margin: ₹$estNetProfit",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (estNetProfit >= 0) Color(0xFF15803D) else Color(0xFFDC2626)
                        )
                    }
                }

                // 3. Hire / Add New Staff Member Form
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Add New Karamchari / Manager (नया कर्मचारी जोड़ें)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        OutlinedTextField(
                            value = newEmpName,
                            onValueChange = { newEmpName = it },
                            label = { Text("Employee Full Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("admin_add_staff_name")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newEmpPhone,
                                onValueChange = { newEmpPhone = it.filter { ch -> ch.isDigit() }.take(10) },
                                label = { Text("Mobile") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.weight(1.2f)
                            )
                            OutlinedTextField(
                                value = newEmpSalary,
                                onValueChange = { newEmpSalary = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Salary ₹") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newEmpPin,
                                onValueChange = { newEmpPin = it.filter { ch -> ch.isDigit() }.take(4) },
                                label = { Text("4-PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                        Text("Select Department:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StaffDepartment.entries.forEach { dept ->
                                FilterChip(
                                    selected = newEmpDept == dept,
                                    onClick = { newEmpDept = dept },
                                    label = { Text(dept.shortBadge, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                        Button(
                            onClick = {
                                if (newEmpName.isNotBlank()) {
                                    onAddStaffMember(
                                        newEmpName.trim(),
                                        newEmpPhone.ifBlank { "9128700999" },
                                        newEmpDept,
                                        "Morning (9 AM - 6 PM)",
                                        newEmpPin.ifBlank { "1234" },
                                        newEmpSalary.toIntOrNull() ?: 18000
                                    )
                                    Toast.makeText(context, "Added ${newEmpName.trim()} to ${newEmpDept.title}!", Toast.LENGTH_SHORT).show()
                                    newEmpName = ""
                                    newEmpPhone = ""
                                } else {
                                    Toast.makeText(context, "Please enter Employee Name", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            modifier = Modifier.fillMaxWidth().testTag("admin_add_staff_submit_btn")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Create Staff ID & Assign Department", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 4. Assign Task to Staff Form
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Assign Daily Work / Task (कर्मचारी को काम सौंपें)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            label = { Text("Task Title *") },
                            placeholder = { Text("e.g. Pack 10 Festive Kurti orders before 4 PM") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newTaskDesc,
                            onValueChange = { newTaskDesc = it },
                            label = { Text("Instructions") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            staffMembers.forEach { emp ->
                                FilterChip(
                                    selected = newTaskAssigneeId == emp.id,
                                    onClick = { newTaskAssigneeId = emp.id },
                                    label = { Text("${emp.name.substringBefore(" ")} (${emp.department.shortBadge})", fontSize = 11.sp) }
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            listOf("High", "Medium", "Normal").forEach { p ->
                                FilterChip(
                                    selected = newTaskPriority == p,
                                    onClick = { newTaskPriority = p },
                                    label = { Text(p, fontSize = 11.sp) }
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Button(
                                onClick = {
                                    val assignee = staffMembers.find { it.id == newTaskAssigneeId }
                                    if (newTaskTitle.isNotBlank() && assignee != null) {
                                        onAssignTaskToStaff(
                                            newTaskTitle.trim(),
                                            newTaskDesc.ifBlank { "Assigned by Owner for ${assignee.department.title}" },
                                            assignee.department,
                                            assignee.id,
                                            newTaskPriority
                                        )
                                        Toast.makeText(context, "Task assigned to ${assignee.name}!", Toast.LENGTH_SHORT).show()
                                        newTaskTitle = ""
                                        newTaskDesc = ""
                                    } else {
                                        Toast.makeText(context, "Enter Task Title & select Staff", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA))
                            ) {
                                Text("Assign Task", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 5. Staff Roster with Attendance, Salary, Performance & RBAC Permissions
                Text("Staff Directory, Attendance, Salary & Permissions (${staffMembers.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                staffMembers.forEach { emp ->
                    val deptColor = Color(emp.department.accentHex)
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, if (emp.isActive) Color(0xFFE2E8F0) else Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth().testTag("admin_staff_card_${emp.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${emp.name} (${emp.staffCode})", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = TextPrimary)
                                    Text("${emp.department.title} • PIN: ${emp.pin} • ${emp.phone}", fontSize = 11.sp, color = deptColor, fontWeight = FontWeight.SemiBold)
                                    Text("Perf: ${emp.performanceScore}% • Tasks Done: ${emp.tasksCompleted} • Att: ${emp.attendanceStatus} (${emp.attendanceDaysThisMonth}d)", fontSize = 11.sp, color = TextSecondary)
                                }
                                Switch(
                                    checked = emp.isActive,
                                    onCheckedChange = { onToggleStaffActive(emp.id) },
                                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF15803D))
                                )
                            }

                            // Salary & Attendance Row
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (emp.isSalaryPaidThisMonth) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7)) {
                                        Text(
                                            "Salary Paid ✓ ₹${emp.monthlySalary}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            onPayStaffSalary(emp.id)
                                            Toast.makeText(context, "Paid ₹${emp.monthlySalary} to ${emp.name}!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp).testTag("pay_salary_${emp.id}")
                                    ) {
                                        Text("Pay ₹${emp.monthlySalary}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                OutlinedButton(
                                    onClick = { onCheckInOutStaff(emp.id, "Present") },
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                    modifier = Modifier.height(34.dp)
                                ) { Text("Present", fontSize = 10.5.sp) }
                                OutlinedButton(
                                    onClick = { onCheckInOutStaff(emp.id, "Absent") },
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                    modifier = Modifier.height(34.dp)
                                ) { Text("Absent", fontSize = 10.5.sp, color = Color(0xFFDC2626)) }
                            }

                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Text("Role Permissions (अधिकार नियंत्रण):", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = TextPrimary)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = emp.canPackOrders,
                                    onClick = { onUpdateStaffPermissions(emp.id, !emp.canPackOrders, emp.canDeliverOrders, emp.canEditStock, emp.canHandleSupport, emp.canViewGstFinance, emp.canManageStaff) },
                                    label = { Text("Pack", fontSize = 10.5.sp) }
                                )
                                FilterChip(
                                    selected = emp.canDeliverOrders,
                                    onClick = { onUpdateStaffPermissions(emp.id, emp.canPackOrders, !emp.canDeliverOrders, emp.canEditStock, emp.canHandleSupport, emp.canViewGstFinance, emp.canManageStaff) },
                                    label = { Text("Deliver", fontSize = 10.5.sp) }
                                )
                                FilterChip(
                                    selected = emp.canEditStock,
                                    onClick = { onUpdateStaffPermissions(emp.id, emp.canPackOrders, emp.canDeliverOrders, !emp.canEditStock, emp.canHandleSupport, emp.canViewGstFinance, emp.canManageStaff) },
                                    label = { Text("Stock", fontSize = 10.5.sp) }
                                )
                                FilterChip(
                                    selected = emp.canHandleSupport,
                                    onClick = { onUpdateStaffPermissions(emp.id, emp.canPackOrders, emp.canDeliverOrders, emp.canEditStock, !emp.canHandleSupport, emp.canViewGstFinance, emp.canManageStaff) },
                                    label = { Text("Support", fontSize = 10.5.sp) }
                                )
                                FilterChip(
                                    selected = emp.canViewGstFinance,
                                    onClick = { onUpdateStaffPermissions(emp.id, emp.canPackOrders, emp.canDeliverOrders, emp.canEditStock, emp.canHandleSupport, !emp.canViewGstFinance, emp.canManageStaff) },
                                    label = { Text("GST/Finance", fontSize = 10.5.sp) }
                                )
                                FilterChip(
                                    selected = emp.canManageStaff,
                                    onClick = { onUpdateStaffPermissions(emp.id, emp.canPackOrders, emp.canDeliverOrders, emp.canEditStock, emp.canHandleSupport, emp.canViewGstFinance, !emp.canManageStaff) },
                                    label = { Text("Manage Team", fontSize = 10.5.sp) }
                                )
                            }
                        }
                    }
                }

                // 6. Security & Activity History Feed
                if (activityHistory.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Security & Activity History (${activityHistory.size} Logs)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            activityHistory.take(12).forEach { act ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFEEF2FF)) {
                                        Text(act.roleBadge, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4338CA), modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${act.actorName}: ${act.action}", fontSize = 11.5.sp, color = TextPrimary)
                                        Text(act.timestamp, fontSize = 10.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // =========================================================================
    // DIALOG: REJECT PRODUCT
    // =========================================================================
    rejectingProduct?.let { prod ->
        Dialog(onDismissRequest = { rejectingProduct = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Reject Product QC", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFDC2626))
                    Text("Product: ${prod.title}", fontSize = 12.sp, color = TextSecondary)

                    OutlinedTextField(
                        value = rejectionInputText,
                        onValueChange = { rejectionInputText = it },
                        label = { Text("Reason for Rejection") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { rejectingProduct = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                prod.status = "Rejected"
                                prod.rejectionReason = rejectionInputText
                                rejectingProduct = null
                                Toast.makeText(context, "Product rejected with reason sent to seller.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                        ) {
                            Text("Confirm Reject")
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG: RELEASE PAYOUTS
    // =========================================================================
    if (showReleasePayoutDialog) {
        Dialog(onDismissRequest = { showReleasePayoutDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Release Seller Payouts (पेआउट जारी करें)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF15803D))
                    Text("Total Scheduled Amount: ₹2,45,800 to 18 verified sellers via automated NEFT / IMPS.", fontSize = 12.sp, color = TextSecondary)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCFCE7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• Bank: State Bank of India / HDFC Bank Gateway", fontSize = 11.sp, color = Color(0xFF166534))
                            Text("• TDS (1%) & TCS deduction reports attached", fontSize = 11.sp, color = Color(0xFF166534))
                            Text("• Sellers will receive SMS and in-app credit alert", fontSize = 11.sp, color = Color(0xFF166534))
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showReleasePayoutDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                payoutBatches[0].payoutStatus = "Settled (NEFT)"
                                showReleasePayoutDialog = false
                                Toast.makeText(context, "✓ ₹2,45,800 का पेआउट सभी सेलर्स को बैंक में ट्रांसफर कर दिया गया!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                        ) {
                            Text("Confirm & Release")
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG: BROADCAST NOTICE
    // =========================================================================
    if (showBroadcastDialog) {
        Dialog(onDismissRequest = { showBroadcastDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Broadcast New Notice (नोटिस जारी करें)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MeeshoMagenta)

                    OutlinedTextField(
                        value = broadcastTitle,
                        onValueChange = { broadcastTitle = it },
                        label = { Text("Notice Title *") },
                        placeholder = { Text("e.g. Festive Sale Mega Subsidy Announcement") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = broadcastMessage,
                        onValueChange = { broadcastMessage = it },
                        label = { Text("Notice Description / Body *") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showBroadcastDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (broadcastTitle.isNotBlank()) {
                                    onPublishBroadcast(broadcastTitle.trim(), broadcastMessage.ifBlank { "Important announcement from Gyanvi Dresses Admin" })
                                    showBroadcastDialog = false
                                    Toast.makeText(context, "✓ नोटिस सभी सेलर्स और बायर्स को भेज दिया गया!", Toast.LENGTH_LONG).show()
                                    broadcastTitle = ""
                                    broadcastMessage = ""
                                } else {
                                    Toast.makeText(context, "Title cannot be blank", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                        ) {
                            Text("Publish Notice")
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOG: CUSTOMER REVIEWS & PRODUCT RATINGS
    // =========================================================================
    if (showAdminReviewsDialog) {
        val reviewedOrders = liveOrders.filter { it.isFeedbackCompleted || it.userReviewText.isNotBlank() }
        Dialog(onDismissRequest = { showAdminReviewsDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Customer Reviews (ग्राहक रिव्यू)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Text("${products.size} Catalog Ratings • ${reviewedOrders.size} Order Reviews", fontSize = 11.sp, color = TextSecondary)
                        }
                        IconButton(onClick = { showAdminReviewsDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (reviewedOrders.isNotEmpty()) {
                            Text("Verified Order Feedback", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MeeshoMagenta)
                            reviewedOrders.forEach { ord ->
                                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFDF2F8), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                            Text("${ord.customerName} • #${ord.orderId}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("${ord.userRating.coerceAtLeast(5)} ★", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFD97706))
                                        }
                                        Text(ord.product.name, fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (ord.userReviewText.isNotBlank()) {
                                            Text("\"${ord.userReviewText}\"", fontSize = 11.5.sp, color = TextPrimary)
                                        }
                                    }
                                }
                            }
                        }
                        Text("Top Rated Catalog Items", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E3A8A))
                        products.sortedByDescending { it.rating }.take(8).forEach { prod ->
                            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF8FAFC), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${prod.category} • ${prod.ratingCount} reviews", fontSize = 10.5.sp, color = TextSecondary)
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                                        Text(
                                            "${prod.rating} ★",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF15803D),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showAdminReviewsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Reviews", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =========================================================================
// HELPER COMPOSABLE: ADMIN METRIC CARD
// =========================================================================
@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    subtitle: String,
    tint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = tint, fontWeight = FontWeight.Bold)
        }
    }
}
