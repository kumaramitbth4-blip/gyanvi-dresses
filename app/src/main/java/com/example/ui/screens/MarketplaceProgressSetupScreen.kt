package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserAccountRole
import com.example.ui.theme.*
import com.example.viewmodel.AppSubScreen

private data class MarketplaceModuleSpec(
    val number: Int,
    val title: String,
    val hindiSubtitle: String,
    val weekLabel: String,
    val priorityLabel: String,
    val isCorePriority: Boolean,
    val actionLabel: String,
    val checklist: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceProgressSetupScreen(
    activeRole: UserAccountRole,
    isDarkMode: Boolean,
    appLanguage: String,
    selectedCurrency: String,
    activeWarehouse: String,
    isVipSubscribed: Boolean,
    registeredSellersCount: Int,
    onBack: () -> Unit,
    onSwitchRole: (UserAccountRole) -> Unit,
    onNavigateSubScreen: (AppSubScreen) -> Unit,
    onOpenHome: () -> Unit,
    onOpenLiveVideoShopping: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onWarehouseChange: (String) -> Unit,
    onToggleVip: (Boolean) -> Unit,
    onRegisterNewSeller: (shopName: String, ownerName: String, gstin: String) -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val modules = remember {
        listOf(
            MarketplaceModuleSpec(
                number = 1,
                title = "1. Frontend Development Features",
                hindiSubtitle = "Home Page, Yellow-Pink Theme, Banners, Voice/Image Search, Dark Mode & Multi-Language",
                weekLabel = "Week 1",
                priorityLabel = "बहुत जरूरी (चरण 1)",
                isCorePriority = true,
                actionLabel = "Tap to Set Up Theme & UI",
                checklist = listOf(
                    "Modern Home Page, Responsive Mobile Design & Bottom Navigation",
                    "Light Yellow & Pink Theme + Reactive Dark Mode",
                    "Product Slider, Promotional Banners & Product Categories",
                    "Search Bar, Voice Search, Image Search & 4-Way Filters",
                    "Multi-language Support (English / हिंदी / मराठी) & Loading Shimmer"
                )
            ),
            MarketplaceModuleSpec(
                number = 2,
                title = "2. Backend & Data Synchronization",
                hindiSubtitle = "Auth, OTP Verification, APIs, Order/Inventory Processing & Error Handling",
                weekLabel = "Week 1",
                priorityLabel = "बहुत जरूरी (चरण 2–3)",
                isCorePriority = true,
                actionLabel = "Tap to Set Up Sync & APIs",
                checklist = listOf(
                    "User Authentication, Login/Registration & OTP Verification",
                    "Buyer, Seller & Admin Role Management + Real-time Data Sync",
                    "Product, Order, Payment & Inventory Processing Pipelines",
                    "Live Notification System & Graceful Error Handling"
                )
            ),
            MarketplaceModuleSpec(
                number = 3,
                title = "3. Buyer Panel Features",
                hindiSubtitle = "Profile, Wishlist, Cart, Buy Now, Compare, Track, Cancel/Return & GST Invoice",
                weekLabel = "Week 2",
                priorityLabel = "बहुत जरूरी (चरण 4)",
                isCorePriority = true,
                actionLabel = "Tap to Set Up Buyer Hub",
                checklist = listOf(
                    "My Profile, Saved Addresses, Wishlist & Shopping Cart / Buy Now",
                    "Product Comparison (up to 3 Dresses), Ratings & Reviews",
                    "Order Tracking, Cancel Order & Return/Refund Request",
                    "Refer & Earn (GYANVI125), Reward Points, Wallet & Download GST Invoice"
                )
            ),
            MarketplaceModuleSpec(
                number = 4,
                title = "4. Seller Panel Features (Multi-Vendor)",
                hindiSubtitle = "Seller Registration, KYC Verification, Bulk Upload, Stock/Price, Returns & Payouts",
                weekLabel = "Week 2",
                priorityLabel = "बहुत जरूरी (चरण 5)",
                isCorePriority = true,
                actionLabel = "Tap to Register / Open Seller Hub",
                checklist = listOf(
                    "Seller Registration & GST/PAN KYC Verification",
                    "Add Product, Bulk Image Upload, Edit & Delete Catalog",
                    "Stock & Price Management + Order & Return Processing",
                    "Seller Payment Reports & Sales Analytics Dashboard"
                )
            ),
            MarketplaceModuleSpec(
                number = 5,
                title = "5. Clothing Product Management",
                hindiSubtitle = "XS–7XL, Numeric 16–50, Waist 24–50, Age Years, Colors, Fabric, Legal & 360° Media",
                weekLabel = "Week 2",
                priorityLabel = "बहुत जरूरी (चरण 5)",
                isCorePriority = true,
                actionLabel = "Tap to Set Up Dress Catalog",
                checklist = listOf(
                    "Sizes: XS–5XL/7XL, Numeric (16–50), Waist (28–50), Kids Age-Years (Custom Chips)",
                    "Multi-Color Swatches (Red, Black, White, Blue, Pink, Yellow, Green)",
                    "Fabric, Chest/Waist/Length, Weight, SKU, MRP, Selling Price & Stock",
                    "Manufacturer & Packer Name/Address/Pincode Compliance",
                    "Multiple Photos, Video, 360°/Front/Back/Side Views & Size Chart"
                )
            ),
            MarketplaceModuleSpec(
                number = 6,
                title = "6. Admin Panel Control (Super Admin)",
                hindiSubtitle = "QC Approvals, Sellers, Buyers, Categories, Commission, Payouts, RTO & Audit Logs",
                weekLabel = "Week 3",
                priorityLabel = "बहुत जरूरी (चरण 6)",
                isCorePriority = true,
                actionLabel = "Tap to Open Admin Panel",
                checklist = listOf(
                    "Super Admin Dashboard, Seller Approval & Product QC Approval",
                    "Buyer Block/Unblock, Complaints, Category & Banner Manager",
                    "Commission Management, Seller Payouts, Refund & RTO Control",
                    "Coupons, Broadcast Notifications, App Settings & Admin Activity Logs"
                )
            ),
            MarketplaceModuleSpec(
                number = 7,
                title = "7. Payment Gateway Features",
                hindiSubtitle = "UPI, Google Pay, PhonePe, Paytm, Cards, Net Banking, COD, Wallet Refunds & Payouts",
                weekLabel = "Week 3",
                priorityLabel = "बहुत जरूरी (चरण 7)",
                isCorePriority = true,
                actionLabel = "Tap to Set Up Payments",
                checklist = listOf(
                    "UPI (Google Pay, PhonePe, Paytm, Custom VPA)",
                    "Debit Card, Credit Card, Net Banking & Cash on Delivery (COD)",
                    "Instant Wallet Refund System & Payment Transaction History",
                    "Seller Bank Transfer (NEFT/IMPS) & Payout Reports"
                )
            ),
            MarketplaceModuleSpec(
                number = 8,
                title = "8. Delivery & Shipping Features",
                hindiSubtitle = "Shiprocket/Delhivery, AWB Number, Shipping Label PDF, Barcode/QR, RTO & Pincode Check",
                weekLabel = "Week 3",
                priorityLabel = "बहुत जरूरी (चरण 8)",
                isCorePriority = true,
                actionLabel = "Tap to Set Up Shipping & QR",
                checklist = listOf(
                    "Courier Integration (Delhivery, BlueDart, Shiprocket Air, Xpressbees)",
                    "AWB Number Generation, Shipping Label PDF, Barcode & QR Generator",
                    "Order Pickup, Return Pickup, RTO Management & Live Tracking",
                    "Shipping Charge Calculator, Pincode Serviceability & Delivery ETA"
                )
            ),
            MarketplaceModuleSpec(
                number = 9,
                title = "9. AI-Based Smart Features",
                hindiSubtitle = "AI Fashion Assistant, Size Fit Recommendation, Chatbot, Image/Description & Search AI",
                weekLabel = "Week 4",
                priorityLabel = "AI & Modern",
                isCorePriority = false,
                actionLabel = "Tap to Launch AI Stylist",
                checklist = listOf(
                    "AI Product Recommendations & Similar Dress Finder",
                    "AI Customer Support Chatbot & Fashion Assistant",
                    "AI Size Recommendation & Automatic Product Categorization",
                    "AI Product Description Generator & Background Remover Tools"
                )
            ),
            MarketplaceModuleSpec(
                number = 10,
                title = "10. Marketing & Business Growth",
                hindiSubtitle = "Refer & Earn, Coupons, Flash/Maha Sale, Push/WhatsApp/Email & Loyalty Points",
                weekLabel = "Week 4",
                priorityLabel = "Growth",
                isCorePriority = false,
                actionLabel = "Tap to Set Up Growth & Alerts",
                checklist = listOf(
                    "Referral Program (GYANVI125), Affiliate Marketing & Reseller Margin",
                    "Discount Coupons (FESTIVE15, GYANVI20), Flash Sale & Maha Sale",
                    "Push, WhatsApp, Email & Abandoned Cart Notification Triggers",
                    "Loyalty & Reward Points Program"
                )
            ),
            MarketplaceModuleSpec(
                number = 11,
                title = "11. Security & Access Control",
                hindiSubtitle = "Secure Login, 2FA OTP, RBAC, Encrypted Data, Fraud Shield, Login History & Backup",
                weekLabel = "Week 3",
                priorityLabel = "बहुत जरूरी (चरण 9)",
                isCorePriority = true,
                actionLabel = "Tap to Open Security Center",
                checklist = listOf(
                    "Secure Login, OTP Verification & Two-Factor Authentication (2FA)",
                    "Role-Based Access Control (RBAC) & Admin Permission Matrix",
                    "API/DB/Payment SSL Security & Encrypted Password Handling",
                    "Fraud Detection Shield, Login Activity History & Backup/Restore"
                )
            ),
            MarketplaceModuleSpec(
                number = 12,
                title = "12. Analytics & Business Reports",
                hindiSubtitle = "Daily/Weekly/Monthly/Yearly Sales, Revenue, Cancelled/RTO, Best Sellers & P&L",
                weekLabel = "Week 4",
                priorityLabel = "Analytics",
                isCorePriority = true,
                actionLabel = "Tap to View P&L & Reports",
                checklist = listOf(
                    "Daily, Weekly, Monthly & Yearly Sales Reports",
                    "Total Revenue, Orders, Cancelled Orders & Returned Orders",
                    "Best Selling Dresses & Low Stock Replenishment Alerts",
                    "Seller Performance, Customer Growth & Profit & Loss Statement"
                )
            ),
            MarketplaceModuleSpec(
                number = 13,
                title = "13. Advanced Marketplace Features",
                hindiSubtitle = "Live/Video Shopping, Seller Chat, Multi-Warehouse, Multi-Currency & VIP Subscription",
                weekLabel = "Week 4",
                priorityLabel = "Advanced (चरण 12)",
                isCorePriority = false,
                actionLabel = "Tap to Set Up Warehouse & VIP",
                checklist = listOf(
                    "Live Shopping, Video Shopping & Product Short Video Reels",
                    "Customer-Seller Live Chat, Voice Assistant & Product Comparison",
                    "Multi-Vendor Marketplace & Multiple Warehouses ($activeWarehouse)",
                    "Multi-Currency ($selectedCurrency), Multi-Location Delivery & Gold VIP Plan"
                )
            ),
            MarketplaceModuleSpec(
                number = 14,
                title = "14. Technical Development Stack",
                hindiSubtitle = "Android Compose, Node.js/Firebase, Cloudinary, Razorpay, Shiprocket, FCM & Analytics",
                weekLabel = "Week 1",
                priorityLabel = "Architecture",
                isCorePriority = true,
                actionLabel = "Tap to View Tech Stack",
                checklist = listOf(
                    "Mobile & UI: Kotlin + Jetpack Compose Material 3",
                    "Auth & Data Layer: ViewModel StateFlow + Firebase Auth / Firestore Ready",
                    "Media, Payments & Logistics: Cloudinary, Razorpay UPI, Shiprocket API",
                    "Messaging & Insights: Firebase Cloud Messaging (FCM) & Analytics"
                )
            ),
            MarketplaceModuleSpec(
                number = 15,
                title = "15. Phase-Wise Development Sequence (1–12)",
                hindiSubtitle = "चरण 1–9 (बहुत जरूरी Core) → चरण 10–12 (AI, Marketing & Advanced Scale)",
                weekLabel = "Week 4",
                priorityLabel = "Roadmap",
                isCorePriority = true,
                actionLabel = "Tap to Open Priority Roadmap",
                checklist = listOf(
                    "Phases 1–3: Frontend, Backend Sync & Database Architecture",
                    "Phases 4–6: Buyer Panel, Multi-Vendor Seller Panel & Super Admin",
                    "Phases 7–9: Payment Gateway, Shiprocket Shipping & 2FA Security",
                    "Phases 10–12: AI Fashion Stylist, Growth Notifications & Advanced Scale"
                )
            )
        )
    }

    var selectedWeekFilter by remember { mutableStateOf("All") }
    var expandedModuleNum by remember { mutableIntStateOf(1) }
    val verifiedCheckItems = remember {
        mutableStateMapOf<String, Boolean>().apply {
            modules.forEach { mod ->
                mod.checklist.indices.forEach { idx ->
                    put("${mod.number}_$idx", true)
                }
            }
        }
    }

    // Active Setup Modal Dialog Controllers
    var showFrontendDialog by remember { mutableStateOf(false) }
    var showSellerKycDialog by remember { mutableStateOf(false) }
    var showShippingDialog by remember { mutableStateOf(false) }
    var showAdvancedDialog by remember { mutableStateOf(false) }
    var showTechStackDialog by remember { mutableStateOf(false) }
    var showPhaseRoadmapDialog by remember { mutableStateOf(false) }

    val totalChecks = verifiedCheckItems.size.coerceAtLeast(1)
    val completedChecks = verifiedCheckItems.values.count { it }
    val progressPercent = (completedChecks * 100) / totalChecks

    val visibleModules = remember(selectedWeekFilter, modules) {
        if (selectedWeekFilter == "All") modules else modules.filter { it.weekLabel == selectedWeekFilter }
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("dev_progress_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "साप्ताहिक Development Progress",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "15 Marketplace Modules • Tap to Set Up",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE6F4EA)
                    ) {
                        Text(
                            text = "$progressPercent% Ready",
                            color = Color(0xFF137333),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .testTag("development_progress_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero Weekly Summary + 3-Account Role Switcher
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF880E4F), MeeshoMagenta, Color(0xFFD81B60))
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "GYANVI DRESSES MARKETPLACE",
                                    color = YellowPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.7.sp
                                )
                                Text(
                                    text = "साप्ताहिक Progress: $completedChecks/$totalChecks Features Active",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = YellowPrimary, modifier = Modifier.size(30.dp))
                        }

                        LinearProgressIndicator(
                            progress = { completedChecks.toFloat() / totalChecks.toFloat() },
                            color = YellowPrimary,
                            trackColor = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Text(
                            text = "Multi-Vendor Accounts ($registeredSellersCount Sellers • $selectedCurrency • $activeWarehouse)",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )

                        // Quick 4-Account Role Buttons (Customer, Staff, Seller, Owner)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UserAccountRole.entries.forEach { role ->
                                val isSel = activeRole == role
                                OutlinedButton(
                                    onClick = { onSwitchRole(role) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSel) YellowPrimary else Color.White.copy(alpha = 0.14f),
                                        contentColor = if (isSel) TextPrimary else Color.White
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(role.shortTitle, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Weekly Sprint Filter Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Week 1", "Week 2", "Week 3", "Week 4").forEach { week ->
                        val selected = selectedWeekFilter == week
                        FilterChip(
                            selected = selected,
                            onClick = { selectedWeekFilter = week },
                            label = {
                                Text(
                                    text = if (week == "All") "All 15 Modules" else week,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MeeshoMagenta,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // 15 Modules Checklist Cards
            itemsIndexed(visibleModules) { _, mod ->
                val isExpanded = expandedModuleNum == mod.number
                val modDoneCount = mod.checklist.indices.count { verifiedCheckItems["${mod.number}_$it"] == true }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isExpanded) MeeshoMagenta.copy(alpha = 0.5f) else Color(0xFFE5E7EB),
                            RoundedCornerShape(14.dp)
                        )
                        .testTag("module_card_${mod.number}")
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedModuleNum = if (isExpanded) -1 else mod.number },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (modDoneCount == mod.checklist.size) Color(0xFF16A34A) else MeeshoMagenta,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${mod.number}",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mod.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(mod.hindiSubtitle, fontSize = 11.sp, color = TextSecondary, maxLines = 2)
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle details",
                                tint = TextSecondary
                            )
                        }

                        // Badges Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFEF3C7)) {
                                Text(
                                    text = mod.weekLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (mod.isCorePriority) Color(0xFFFCE7F3) else Color(0xFFEDE9FE)
                            ) {
                                Text(
                                    text = mod.priorityLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mod.isCorePriority) MeeshoMagenta else Color(0xFF6D28D9),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "$modDoneCount/${mod.checklist.size} Done",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenRating
                            )
                        }

                        // Expandable Checklist
                        AnimatedVisibility(visible = isExpanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                mod.checklist.forEachIndexed { idx, itemText ->
                                    val key = "${mod.number}_$idx"
                                    val checked = verifiedCheckItems[key] == true
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { verifiedCheckItems[key] = !checked }
                                            .padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = { verifiedCheckItems[key] = it },
                                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF16A34A)),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = itemText,
                                            fontSize = 12.sp,
                                            color = if (checked) TextPrimary else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Tap to Set Up Action Button
                        Button(
                            onClick = {
                                when (mod.number) {
                                    1 -> showFrontendDialog = true
                                    2, 14 -> showTechStackDialog = true
                                    3 -> onNavigateSubScreen(AppSubScreen.CART)
                                    4 -> showSellerKycDialog = true
                                    5 -> onNavigateSubScreen(AppSubScreen.SELLER_PANEL)
                                    6, 12 -> onNavigateSubScreen(AppSubScreen.ADMIN_PANEL)
                                    7 -> onNavigateSubScreen(AppSubScreen.CART)
                                    8 -> showShippingDialog = true
                                    9 -> onNavigateSubScreen(AppSubScreen.AI_FASHION_ASSISTANT)
                                    10 -> onNavigateSubScreen(AppSubScreen.NOTIFICATIONS)
                                    11 -> onNavigateSubScreen(AppSubScreen.SECURITY_CENTER)
                                    13 -> showAdvancedDialog = true
                                    15 -> showPhaseRoadmapDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("setup_module_${mod.number}")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(mod.actionLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Dialog 1: Frontend Theme & Multi-Language Quick Setup
    if (showFrontendDialog) {
        AlertDialog(
            onDismissRequest = { showFrontendDialog = false },
            title = { Text("Frontend Theme & Language Setup", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dark Mode Theme", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Switch Light Yellow-Pink vs Dark UI", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(checked = isDarkMode, onCheckedChange = onToggleDarkMode)
                    }
                    Text("App Language:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("English", "हिंदी", "मराठी").forEach { lang ->
                            FilterChip(
                                selected = appLanguage == lang,
                                onClick = { onLanguageChange(lang) },
                                label = { Text(lang, fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            showFrontendDialog = false
                            onOpenHome()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Preview Home Banner Slider & Categories")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showFrontendDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)) {
                    Text("Done", color = Color.White)
                }
            }
        )
    }

    // Dialog 2: Seller Registration & KYC Verification Setup (Module 4)
    if (showSellerKycDialog) {
        var shopName by remember { mutableStateOf("") }
        var ownerName by remember { mutableStateOf("") }
        var gstinInput by remember { mutableStateOf("10GYNVI1234F1Z5") }
        AlertDialog(
            onDismissRequest = { showSellerKycDialog = false },
            title = { Text("Seller Registration & KYC Setup", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Active Verified Sellers: $registeredSellersCount", fontSize = 12.sp, color = GreenRating, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Shop / Boutique Name") },
                        placeholder = { Text("e.g. Bettiah Silk Palace") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Owner Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = gstinInput,
                        onValueChange = { gstinInput = it.uppercase() },
                        label = { Text("GSTIN / Enrolment ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = {
                            showSellerKycDialog = false
                            onNavigateSubScreen(AppSubScreen.USER_AUTH)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Customer / Seller Mobile-Email Password Registration →")
                    }
                    OutlinedButton(
                        onClick = {
                            showSellerKycDialog = false
                            onNavigateSubScreen(AppSubScreen.SELLER_PANEL)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Full Seller Supplier Hub →")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (shopName.isNotBlank() && ownerName.isNotBlank()) {
                            onRegisterNewSeller(shopName.trim(), ownerName.trim(), gstinInput.trim())
                            Toast.makeText(context, "Seller $shopName registered for Admin KYC approval!", Toast.LENGTH_LONG).show()
                            showSellerKycDialog = false
                        } else {
                            Toast.makeText(context, "Enter shop name and owner name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Submit Seller KYC", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSellerKycDialog = false }) { Text("Close") }
            }
        )
    }

    // Dialog 3: Delivery, Shiprocket AWB & Pincode Checker (Module 8)
    if (showShippingDialog) {
        var testPincode by remember { mutableStateOf("845438") }
        var generatedAwb by remember { mutableStateOf("SHP-845438-9021") }
        AlertDialog(
            onDismissRequest = { showShippingDialog = false },
            title = { Text("Delivery, Shiprocket & QR Setup", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Courier Partners Active: Delhivery • Shiprocket Air • BlueDart • Shadowfax", fontSize = 11.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = testPincode,
                        onValueChange = { if (it.length <= 6) testPincode = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Test Pincode Serviceability") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (testPincode.length == 6) {
                        Text(
                            text = "✓ Serviceable: Prepaid + COD Available • Shipping ₹0 (Est. 3 Days)",
                            fontSize = 11.sp,
                            color = GreenRating,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF3F4F6)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                            Text("Latest Generated AWB & Barcode:", fontSize = 10.sp, color = TextSecondary)
                            Text(generatedAwb, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MeeshoMagenta)
                        }
                    }
                    Button(
                        onClick = {
                            generatedAwb = "SHP-${testPincode.ifBlank { "845438" }}-${(1000..9999).random()}"
                            Toast.makeText(context, "New Shiprocket AWB & Shipping Label PDF ready: $generatedAwb", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate New AWB & Shipping Label", color = Color.White, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showShippingDialog = false }) { Text("Close") }
            }
        )
    }

    // Dialog 4: Advanced Marketplace (Multi-Currency, Multi-Warehouse, Live Shopping, VIP Plan)
    if (showAdvancedDialog) {
        AlertDialog(
            onDismissRequest = { showAdvancedDialog = false },
            title = { Text("Advanced Marketplace Setup", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Multi-Currency Support:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("INR ₹", "USD $", "NPR रु").forEach { curr ->
                            FilterChip(
                                selected = selectedCurrency == curr,
                                onClick = { onCurrencyChange(curr) },
                                label = { Text(curr, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Multiple Warehouses:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    listOf(
                        "Bettiah Main Hub (845438)",
                        "Patna Fulfillment Center (800001)",
                        "Surat Textile Depot (395002)"
                    ).forEach { wh ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onWarehouseChange(wh) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = activeWarehouse == wh, onClick = { onWarehouseChange(wh) })
                            Text(wh, fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Gyanvi Gold VIP Subscription", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Free Express Shipping + 2x Loyalty Points", fontSize = 10.sp, color = TextSecondary)
                        }
                        Switch(checked = isVipSubscribed, onCheckedChange = onToggleVip)
                    }

                    OutlinedButton(
                        onClick = {
                            showAdvancedDialog = false
                            onOpenLiveVideoShopping()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Launch Live & Video Shopping Reels →")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAdvancedDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)) {
                    Text("Save Setup", color = Color.White)
                }
            }
        )
    }

    // Dialog 5: Technical Stack & Backend Sync Status (Modules 2 & 14)
    if (showTechStackDialog) {
        AlertDialog(
            onDismissRequest = { showTechStackDialog = false },
            title = { Text("Tech Stack & Backend Status", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val stack = listOf(
                        "Android App" to "Kotlin + Jetpack Compose M3 (Active)",
                        "State & Sync" to "Coroutines + StateFlow Repository (Active)",
                        "Auth & OTP" to "OTP Dispatcher + 2FA Shield (Ready)",
                        "Payments" to "UPI / GPay / PhonePe / Cards / COD (Ready)",
                        "Logistics" to "Shiprocket / Delhivery AWB & QR (Ready)",
                        "Notifications" to "FCM + WhatsApp + Email Channels (Active)"
                    )
                    stack.forEach { (layer, value) ->
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF9FAFB)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(layer, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(value, fontSize = 10.sp, color = GreenRating, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showTechStackDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }

    // Dialog 6: Phase-Wise Development Sequence (1–12)
    if (showPhaseRoadmapDialog) {
        AlertDialog(
            onDismissRequest = { showPhaseRoadmapDialog = false },
            title = { Text("12-Phase Development Sequence", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val phases = listOf(
                        "चरण 1: Frontend UI & Theme" to "बहुत जरूरी",
                        "चरण 2: Backend API & Sync" to "बहुत जरूरी",
                        "चरण 3: Database Models & Storage" to "बहुत जरूरी",
                        "चरण 4: Buyer Panel & Checkout" to "बहुत जरूरी",
                        "चरण 5: Seller Hub & Clothing Sizes" to "बहुत जरूरी",
                        "चरण 6: Super Admin Panel & QC" to "बहुत जरूरी",
                        "चरण 7: Payment Gateway (UPI/Card)" to "बहुत जरूरी",
                        "चरण 8: Shipping & Shiprocket AWB" to "बहुत जरूरी",
                        "चरण 9: Security, 2FA & RBAC" to "बहुत जरूरी",
                        "चरण 10: AI Fashion & Size Fit" to "AI Phase",
                        "चरण 11: Marketing, Coupons & Alerts" to "Growth",
                        "चरण 12: Live Shopping, Warehouses & VIP" to "Advanced"
                    )
                    phases.forEach { (phase, tag) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✓ $phase", fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            Text(tag, fontSize = 10.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showPhaseRoadmapDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)) {
                    Text("Done", color = Color.White)
                }
            }
        )
    }
}
