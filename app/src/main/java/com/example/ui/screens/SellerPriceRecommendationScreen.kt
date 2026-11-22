package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

// =========================================================================
// DATA MODELS
// =========================================================================
data class RecommendationItem(
    val id: String,
    val sku: String,
    val title: String,
    val costPrice: Int,
    val packingCost: Int = 10,
    val shippingCost: Int = 40,
    val miscCost: Int = 10,
    var currentPrice: Int,
    val recommendedPrice: Int,
    val competitorMinPrice: Int,
    val competitorAvgPrice: Int,
    val minFloorPrice: Int,
    val maxCeilingPrice: Int,
    val demandType: DemandType,
    val suggestionType: SuggestionType,
    val orderBoostPct: String,
    val aiReason: String,
    var isApplied: Boolean = false,
    var autoPricingEnabled: Boolean = false
) {
    val totalCost: Int get() = costPrice + packingCost + shippingCost + miscCost
    val netProfit: Int get() = currentPrice - totalCost
    val recommendedNetProfit: Int get() = recommendedPrice - totalCost
    val gstAmount: Double get() = currentPrice * 0.05
}

enum class DemandType(val label: String, val color: Color, val bgColor: Color) {
    VERY_HIGH("Very High Demand", Color(0xFF2E7D32), Color(0xFFE8F5E9)),
    FESTIVE_SURGE("Festive Surge", Color(0xFFE65100), Color(0xFFFFF3E0)),
    HIGH("High Demand", Color(0xFF1565C0), Color(0xFFE3F2FD)),
    TRENDING("Trending Now", Color(0xFF7B1FA2), Color(0xFFF3E5F5)),
    LOW_DEMAND("Low Demand Alert", Color(0xFFC62828), Color(0xFFFFEBEE))
}

enum class SuggestionType(val label: String, val icon: @Composable () -> Unit) {
    DECREASE("Price Drop Suggested", { Icon(Icons.Default.TrendingDown, null, tint = Color(0xFFC62828), modifier = Modifier.size(16.dp)) }),
    INCREASE("Price Hike Opportunity", { Icon(Icons.Default.TrendingUp, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp)) }),
    OPTIMAL("Optimal Price", { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF1565C0), modifier = Modifier.size(16.dp)) })
}

data class PriceHistoryEntry(
    val date: String,
    val oldPrice: Int,
    val newPrice: Int,
    val changedBy: String,
    val reason: String
)

// =========================================================================
// MAIN PRICE RECOMMENDATION SCREEN
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullSellerPriceRecommendationScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    // Screen Mode: 0 = Seller View, 1 = Admin Control Panel
    var activeRoleTab by remember { mutableIntStateOf(0) }

    // Selected Sub-Tab: 0 = Recommendations & My Products, 1 = AI Smart Dashboard, 2 = Competitor Tracker, 3 = Price History
    var selectedViewTab by remember { mutableIntStateOf(0) }

    // Dialog States
    var showProfitCalcModal by remember { mutableStateOf(false) }
    var selectedItemForProfitCalc by remember { mutableStateOf<RecommendationItem?>(null) }

    var showEditPriceModal by remember { mutableStateOf(false) }
    var selectedItemForEdit by remember { mutableStateOf<RecommendationItem?>(null) }
    var customPriceInput by remember { mutableStateOf("") }
    var minPriceInput by remember { mutableStateOf("") }
    var maxPriceInput by remember { mutableStateOf("") }

    var showHistoryModal by remember { mutableStateOf(false) }
    var selectedItemForHistory by remember { mutableStateOf<RecommendationItem?>(null) }

    var showApplyAllConfirmDialog by remember { mutableStateOf(false) }
    var showBulkDiscountDialog by remember { mutableStateOf(false) }

    // Global Admin Controls
    var adminMasterAiEnabled by remember { mutableStateOf(true) }
    var adminMinMarginGuarantee by remember { mutableStateOf("15") }
    var adminMaxDailyDropPct by remember { mutableStateOf("10") }

    // Sample Products Data
    val productList = remember {
        mutableStateListOf(
            RecommendationItem(
                id = "PR-101",
                sku = "GWN-ANK-101",
                title = "Anarkali Embroidered Kurti Set (Rani Pink)",
                costPrice = 250,
                packingCost = 10,
                shippingCost = 40,
                miscCost = 10,
                currentPrice = 499,
                recommendedPrice = 459,
                competitorMinPrice = 465,
                competitorAvgPrice = 485,
                minFloorPrice = 380,
                maxCeilingPrice = 650,
                demandType = DemandType.VERY_HIGH,
                suggestionType = SuggestionType.DECREASE,
                orderBoostPct = "+180% More Orders",
                aiReason = "Dropping ₹40 undercuts lowest competitor (₹465) to capture Rank #1 on search."
            ),
            RecommendationItem(
                id = "PR-102",
                sku = "GWN-BNS-202",
                title = "Banarasi Silk Zari Woven Saree (Royal Blue)",
                costPrice = 480,
                packingCost = 15,
                shippingCost = 45,
                miscCost = 10,
                currentPrice = 899,
                recommendedPrice = 799,
                competitorMinPrice = 820,
                competitorAvgPrice = 860,
                minFloorPrice = 650,
                maxCeilingPrice = 1199,
                demandType = DemandType.FESTIVE_SURGE,
                suggestionType = SuggestionType.DECREASE,
                orderBoostPct = "+240% More Orders",
                aiReason = "Festive wedding surge! ₹799 hits customer psychological threshold under ₹800."
            ),
            RecommendationItem(
                id = "PR-103",
                sku = "GWN-CHD-303",
                title = "Chanderi Cotton Daily Wear Kurti (Yellow)",
                costPrice = 180,
                packingCost = 10,
                shippingCost = 40,
                miscCost = 10,
                currentPrice = 349,
                recommendedPrice = 319,
                competitorMinPrice = 329,
                competitorAvgPrice = 359,
                minFloorPrice = 270,
                maxCeilingPrice = 499,
                demandType = DemandType.HIGH,
                suggestionType = SuggestionType.DECREASE,
                orderBoostPct = "+110% More Orders",
                aiReason = "Competitive daily wear segment. Matching ₹319 unlocks 'Budget Buy' carousel."
            ),
            RecommendationItem(
                id = "PR-104",
                sku = "GWN-SHR-404",
                title = "Georgette Embroidered Sharara Set",
                costPrice = 520,
                packingCost = 15,
                shippingCost = 45,
                miscCost = 10,
                currentPrice = 999,
                recommendedPrice = 899,
                competitorMinPrice = 920,
                competitorAvgPrice = 970,
                minFloorPrice = 720,
                maxCeilingPrice = 1399,
                demandType = DemandType.TRENDING,
                suggestionType = SuggestionType.DECREASE,
                orderBoostPct = "+165% More Orders",
                aiReason = "Trending wedding collection. ₹899 brings massive conversion jump without margin risk."
            ),
            RecommendationItem(
                id = "PR-105",
                sku = "GWN-CTN-505",
                title = "Pure Cotton Handblock Print Saree (Red)",
                costPrice = 220,
                packingCost = 10,
                shippingCost = 40,
                miscCost = 10,
                currentPrice = 399,
                recommendedPrice = 449,
                competitorMinPrice = 470,
                competitorAvgPrice = 495,
                minFloorPrice = 320,
                maxCeilingPrice = 599,
                demandType = DemandType.HIGH,
                suggestionType = SuggestionType.INCREASE,
                orderBoostPct = "+38% Profit Lift",
                aiReason = "High demand & underpriced! Competitors are selling at ₹470+. You can safely hike ₹50."
            ),
            RecommendationItem(
                id = "PR-106",
                sku = "GWN-RYN-606",
                title = "Rayon Solid Casual A-Line Kurti (Mint)",
                costPrice = 160,
                packingCost = 10,
                shippingCost = 40,
                miscCost = 10,
                currentPrice = 320,
                recommendedPrice = 269,
                competitorMinPrice = 279,
                competitorAvgPrice = 299,
                minFloorPrice = 240,
                maxCeilingPrice = 399,
                demandType = DemandType.LOW_DEMAND,
                suggestionType = SuggestionType.DECREASE,
                orderBoostPct = "+310% Clearance Surge",
                aiReason = "Low demand alert: Inventory idle for 24 days. Quick discount liquidates remaining 80 units."
            )
        )
    }

    val priceHistoryMap = remember {
        mutableStateMapOf(
            "PR-101" to listOf(
                PriceHistoryEntry("Nov 02, 2026", 549, 499, "Seller Amit", "Diwali Early Festive Offer"),
                PriceHistoryEntry("Oct 20, 2026", 599, 549, "AI Auto", "Competitor undercut trigger"),
                PriceHistoryEntry("Sep 15, 2026", 649, 599, "Admin", "Catalog Launch Base Price")
            ),
            "PR-102" to listOf(
                PriceHistoryEntry("Nov 05, 2026", 949, 899, "Seller Amit", "Pre-festive discount"),
                PriceHistoryEntry("Oct 12, 2026", 999, 949, "AI Recommendation", "Volume promotion")
            )
        )
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
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("price_rec_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Price Recommendation", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                            Text("Smart pricing suggestions to boost daily orders", fontSize = 11.sp, color = TextSecondary)
                        }

                        // Role Switcher Pill (Seller Hub vs Admin Panel)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (activeRoleTab == 0) Color(0xFFF3E5F5) else Color(0xFFFFEBEE))
                                .clickable {
                                    activeRoleTab = if (activeRoleTab == 0) 1 else 0
                                    Toast.makeText(
                                        context,
                                        if (activeRoleTab == 1) "Switched to Admin Control Panel" else "Switched to Seller Hub View",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (activeRoleTab == 0) Icons.Default.Storefront else Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = if (activeRoleTab == 0) MeeshoMagenta else Color(0xFFD32F2F),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (activeRoleTab == 0) "Seller Hub" else "Admin Mode",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeRoleTab == 0) MeeshoMagenta else Color(0xFFD32F2F)
                                )
                            }
                        }
                    }

                    // Navigation Sub-Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedViewTab,
                        edgePadding = 12.dp,
                        containerColor = CardWhite,
                        contentColor = MeeshoMagenta,
                        indicator = { tabPositions ->
                            if (selectedViewTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedViewTab]),
                                    color = MeeshoMagenta
                                )
                            }
                        }
                    ) {
                        Tab(
                            selected = selectedViewTab == 0,
                            onClick = { selectedViewTab = 0 },
                            text = { Text("My Products & Prices", fontWeight = if (selectedViewTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedViewTab == 1,
                            onClick = { selectedViewTab = 1 },
                            text = { Text("AI Smart Dashboard", fontWeight = if (selectedViewTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedViewTab == 2,
                            onClick = { selectedViewTab = 2 },
                            text = { Text("Competitor Tracker", fontWeight = if (selectedViewTab == 2) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) }
                        )
                        if (activeRoleTab == 1) {
                            Tab(
                                selected = selectedViewTab == 3,
                                onClick = { selectedViewTab = 3 },
                                text = { Text("Admin Controls & Rules", fontWeight = if (selectedViewTab == 3) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) }
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedViewTab) {
                0 -> {
                    // TAB 0: My Products & Recommended Price Cards (Matching Screenshot 6:21)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Hero Banner (Exact match to screenshot 6:21)
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFECB3)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Unlock up to 2.4x More Orders", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF795548))
                                    Text("Sellers who adopted recommended prices experienced an average 68% jump in weekly sales.", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bulk Action Buttons
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showApplyAllConfirmDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("apply_all_recommended_btn")
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Apply Recommended Prices to All (${productList.count { !it.isApplied }})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { showBulkDiscountDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bulk Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text("Suggested Price Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Products List
                        productList.forEachIndexed { index, item ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Title & Demand Badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                            Text("SKU: ${item.sku}", fontSize = 11.sp, color = TextMuted)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(item.demandType.bgColor)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(item.demandType.label, color = item.demandType.color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Prices Comparison (Current, Recommended, Competitor Min)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Current Price", fontSize = 11.sp, color = TextSecondary)
                                            Text("₹${item.currentPrice}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        }
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1.3f)) {
                                            Text("Recommended", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                            Text("₹${item.recommendedPrice}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                                        }
                                        Column(modifier = Modifier.weight(1.2f)) {
                                            Text("Competitor Min", fontSize = 11.sp, color = TextSecondary)
                                            Text("₹${item.competitorMinPrice}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Order Boost & AI Rationale
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(item.orderBoostPct, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        item.aiReason,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        lineHeight = 15.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = DividerColor)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Profit & Cost Quick Line
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.clickable {
                                                selectedItemForProfitCalc = item
                                                showProfitCalcModal = true
                                            },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Calculate, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "Net Profit: ₹${item.netProfit} (Cost: ₹${item.totalCost})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32),
                                                textDecoration = TextDecoration.Underline
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.clickable {
                                                selectedItemForHistory = item
                                                showHistoryModal = true
                                            },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.History, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("History", fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action Buttons (Apply Price, Edit Custom Price, Auto Pricing Toggle)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Auto-pricing toggle
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable {
                                                productList[index] = item.copy(autoPricingEnabled = !item.autoPricingEnabled)
                                                val msg = if (!item.autoPricingEnabled) "Auto-Pricing enabled for ${item.sku}" else "Auto-Pricing turned off"
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Switch(
                                                checked = item.autoPricingEnabled,
                                                onCheckedChange = { checked ->
                                                    productList[index] = item.copy(autoPricingEnabled = checked)
                                                },
                                                modifier = Modifier.size(36.dp),
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = MeeshoMagenta,
                                                    checkedTrackColor = Color(0xFFF3E5F5)
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Auto AI", fontSize = 11.sp, color = TextSecondary)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    selectedItemForEdit = item
                                                    customPriceInput = item.currentPrice.toString()
                                                    minPriceInput = item.minFloorPrice.toString()
                                                    maxPriceInput = item.maxCeilingPrice.toString()
                                                    showEditPriceModal = true
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(36.dp)
                                            ) {
                                                Text("Edit Price", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            if (item.isApplied) {
                                                Button(
                                                    onClick = { },
                                                    enabled = false,
                                                    shape = RoundedCornerShape(6.dp),
                                                    colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFFE8F5E9)),
                                                    modifier = Modifier.height(36.dp)
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Applied", color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Button(
                                                    onClick = {
                                                        productList[index] = item.copy(
                                                            currentPrice = item.recommendedPrice,
                                                            isApplied = true
                                                        )
                                                        Toast.makeText(context, "Price updated to ₹${item.recommendedPrice} for ${item.title}", Toast.LENGTH_SHORT).show()
                                                    },
                                                    shape = RoundedCornerShape(6.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                                    modifier = Modifier.height(36.dp)
                                                ) {
                                                    Text("Apply Price", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

                1 -> {
                    // TAB 1: AI SMART PRICE RECOMMENDATION DASHBOARD
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("AI Smart Price Recommendation Dashboard", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Real-time revenue forecast & catalog pricing optimization", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        // KPI Summary Grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Total Products", fontSize = 11.sp, color = TextSecondary)
                                    Text("${productList.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("6 Active Listings", fontSize = 10.sp, color = Color(0xFF2E7D32))
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("High Demand", fontSize = 11.sp, color = TextSecondary)
                                    Text("4 Items", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                    Text("Festive & Trending", fontSize = 10.sp, color = Color(0xFFE65100))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Expected Sales Jump", fontSize = 11.sp, color = TextSecondary)
                                    Text("+185%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    Text("Based on competitor gap", fontSize = 10.sp, color = TextSecondary)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Expected Profit Lift", fontSize = 11.sp, color = TextSecondary)
                                    Text("+₹42,800", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                                    Text("Per 30-day cycle", fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Demand & Alert Categories
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Catalog Demand Health", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.height(8.dp))

                                listOf(
                                    Triple("Festive Surge & Very High Demand", "3 Products ready for volume jump", Color(0xFF2E7D32)),
                                    Triple("Trending Opportunities", "1 Product (Sharara Set) spiking", Color(0xFF7B1FA2)),
                                    Triple("Price Hike Opportunities", "1 Product underpriced by ₹50", Color(0xFF1565C0)),
                                    Triple("Low Demand Alert (Liquidation needed)", "1 Product (Mint Kurti) requires flash drop", Color(0xFFC62828))
                                ).forEach { (title, subtitle, color) ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                            Text(subtitle, fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // AI Smart Pricing Explanation
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("How GYANVI AI Smart Pricing Works", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MeeshoMagenta)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Our AI model scans 50,000+ fashion listings across Gyanvi Dresses, Amazon & Flipkart. It identifies the exact price point where buyer order rate spikes while keeping your profit above your set Minimum Floor Price.",
                                        fontSize = 11.sp,
                                        color = TextPrimary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                2 -> {
                    // TAB 2: COMPETITOR TRACKING TABLE
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("Live Competitor Price Tracker", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Real-time monitoring of similar ethnic wear catalogs", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        productList.forEach { item ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Your Price", fontSize = 10.sp, color = TextSecondary)
                                            Text("₹${item.currentPrice}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        }
                                        Column {
                                            Text("Competitor Min", fontSize = 10.sp, color = TextSecondary)
                                            Text("₹${item.competitorMinPrice}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                        }
                                        Column {
                                            Text("Competitor Avg", fontSize = 10.sp, color = TextSecondary)
                                            Text("₹${item.competitorAvgPrice}", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                                        }
                                        Column {
                                            Text("Price Gap", fontSize = 10.sp, color = TextSecondary)
                                            val diff = item.currentPrice - item.competitorMinPrice
                                            Text(
                                                if (diff > 0) "+₹$diff Higher" else "-₹${-diff} Cheaper",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (diff > 0) Color(0xFFC62828) else Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                3 -> {
                    // TAB 3: ADMIN CONTROLS & RULES (Visible in Admin Mode)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("Admin Pricing Control Center", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Platform-wide pricing safety rules, AI toggle & anomaly detection", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Platform Pricing Rules", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.height(12.dp))

                                // Master AI Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("AI Dynamic Pricing Engine", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                        Text("Generate real-time suggestions across all active sellers", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Switch(
                                        checked = adminMasterAiEnabled,
                                        onCheckedChange = {
                                            adminMasterAiEnabled = it
                                            Toast.makeText(context, if (it) "AI Dynamic Engine Activated" else "AI Engine Paused", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = MeeshoMagenta, checkedTrackColor = Color(0xFFF3E5F5))
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DividerColor)

                                // Min Margin Guarantee
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Minimum Seller Margin Guarantee", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                        Text("Prevents AI from suggesting any price with margin below this threshold", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    OutlinedTextField(
                                        value = adminMinMarginGuarantee,
                                        onValueChange = { adminMinMarginGuarantee = it },
                                        modifier = Modifier.width(70.dp),
                                        suffix = { Text("%", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DividerColor)

                                // Max Daily Drop %
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Max Single Price Drop Limit", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                        Text("Safety guard against sudden price dumping", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    OutlinedTextField(
                                        value = adminMaxDailyDropPct,
                                        onValueChange = { adminMaxDailyDropPct = it },
                                        modifier = Modifier.width(70.dp),
                                        suffix = { Text("%", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Anomaly Alerts Card
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Price Anomaly Detection (0 Alerts)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFC62828))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("No seller price dumping or margin breaches detected today. All 6 active products are within safe corridor.", fontSize = 11.sp, color = TextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    // =========================================================================
    // MODALS & DIALOGS
    // =========================================================================

    // 1. PROFIT CALCULATOR MODAL (Based on user example: Fabric ₹250, Pack ₹10, Ship ₹40, Misc ₹10 = ₹310 -> Price ₹459 = ₹149 Profit)
    if (showProfitCalcModal && selectedItemForProfitCalc != null) {
        val item = selectedItemForProfitCalc!!
        var simPrice by remember { mutableStateOf(item.recommendedPrice.toString()) }
        val priceInt = simPrice.toIntOrNull() ?: item.recommendedPrice
        val profit = priceInt - item.totalCost
        val marginPct = if (priceInt > 0) (profit.toDouble() / priceInt * 100).toInt() else 0

        Dialog(onDismissRequest = { showProfitCalcModal = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Profit & Cost Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        IconButton(onClick = { showProfitCalcModal = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }
                    Text(item.title, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Costs breakdown
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Product Purchase / Fabric Cost", fontSize = 12.sp, color = TextSecondary)
                                Text("₹${item.costPrice}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Packaging Cost", fontSize = 12.sp, color = TextSecondary)
                                Text("₹${item.packingCost}", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Shipping / Courier Logistics", fontSize = 12.sp, color = TextSecondary)
                                Text("₹${item.shippingCost}", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Other Operational Expenses", fontSize = 12.sp, color = TextSecondary)
                                Text("₹${item.miscCost}", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = TextSecondary)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerColor)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Landed Cost", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text("₹${item.totalCost}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFC62828))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Interactive Price Simulation Input
                    Text("Simulate Selling Price", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = simPrice,
                        onValueChange = { simPrice = it },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Profit Result Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Estimated Net Profit", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                                Text("₹$profit / unit", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF2E7D32))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Profit Margin: $marginPct% • Zero Platform Commission (0%)", fontSize = 11.sp, color = Color(0xFF2E7D32))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val newP = simPrice.toIntOrNull() ?: item.recommendedPrice
                            val idx = productList.indexOfFirst { it.id == item.id }
                            if (idx != -1) {
                                productList[idx] = item.copy(currentPrice = newP, isApplied = true)
                            }
                            showProfitCalcModal = false
                            Toast.makeText(context, "Price updated to ₹$newP with ₹$profit profit!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Set This Price (₹$simPrice)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 2. EDIT CUSTOM PRICE & FLOOR/CEILING MODAL
    if (showEditPriceModal && selectedItemForEdit != null) {
        val item = selectedItemForEdit!!
        Dialog(onDismissRequest = { showEditPriceModal = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Edit Price & Safety Limits", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text(item.title, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Custom Selling Price", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customPriceInput,
                        onValueChange = { customPriceInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Minimum Price", fontSize = 11.sp, color = TextSecondary)
                            OutlinedTextField(
                                value = minPriceInput,
                                onValueChange = { minPriceInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                prefix = { Text("₹ ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Maximum Price", fontSize = 11.sp, color = TextSecondary)
                            OutlinedTextField(
                                value = maxPriceInput,
                                onValueChange = { maxPriceInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                prefix = { Text("₹ ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEditPriceModal = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val newP = customPriceInput.toIntOrNull() ?: item.currentPrice
                                val minP = minPriceInput.toIntOrNull() ?: item.minFloorPrice
                                val maxP = maxPriceInput.toIntOrNull() ?: item.maxCeilingPrice

                                if (newP < item.totalCost) {
                                    Toast.makeText(context, "Price ₹$newP is below total cost ₹${item.totalCost}!", Toast.LENGTH_LONG).show()
                                    return@Button
                                }

                                val idx = productList.indexOfFirst { it.id == item.id }
                                if (idx != -1) {
                                    productList[idx] = item.copy(
                                        currentPrice = newP,
                                        minFloorPrice = minP,
                                        maxCeilingPrice = maxP,
                                        isApplied = true
                                    )
                                }
                                showEditPriceModal = false
                                Toast.makeText(context, "Price updated to ₹$newP", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                        ) {
                            Text("Save Price")
                        }
                    }
                }
            }
        }
    }

    // 3. PRICE HISTORY MODAL
    if (showHistoryModal && selectedItemForHistory != null) {
        val item = selectedItemForHistory!!
        val historyList = priceHistoryMap[item.id] ?: listOf(
            PriceHistoryEntry("Nov 01, 2026", item.currentPrice + 50, item.currentPrice, "Seller Amit", "Standard seasonal adjustment")
        )

        Dialog(onDismissRequest = { showHistoryModal = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Price History Log", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        IconButton(onClick = { showHistoryModal = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }
                    Text(item.title, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(14.dp))

                    historyList.forEach { entry ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(entry.date, fontSize = 11.sp, color = TextSecondary)
                                    Text("By: ${entry.changedBy}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("₹${entry.oldPrice}", fontSize = 13.sp, color = TextSecondary, textDecoration = TextDecoration.LineThrough)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("₹${entry.newPrice}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                                Text(entry.reason, fontSize = 11.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }

    // 4. APPLY ALL CONFIRMATION DIALOG (Protects seller from unintentional loss)
    if (showApplyAllConfirmDialog) {
        val totalPending = productList.count { !it.isApplied }
        AlertDialog(
            onDismissRequest = { showApplyAllConfirmDialog = false },
            title = { Text("Apply All Recommended Prices?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "You are about to update prices for $totalPending products.\n\n" +
                            "• All recommended prices keep your margin above ₹75/unit.\n" +
                            "• Average expected order jump: +185%.\n" +
                            "• You can revert individual prices anytime from Edit Price.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        productList.forEachIndexed { index, item ->
                            productList[index] = item.copy(
                                currentPrice = item.recommendedPrice,
                                isApplied = true
                            )
                        }
                        showApplyAllConfirmDialog = false
                        Toast.makeText(context, "Applied recommended prices to all products!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Confirm & Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyAllConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // 5. BULK PRICE EDIT DIALOG
    if (showBulkDiscountDialog) {
        var bulkMode by remember { mutableStateOf("Drop ₹20 on all") }
        AlertDialog(
            onDismissRequest = { showBulkDiscountDialog = false },
            title = { Text("Bulk Price Adjustment", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select a batch price rule for all active catalog items:", fontSize = 12.sp, color = TextSecondary)
                    listOf("Drop ₹20 on all products", "Drop 5% discount on all", "Match Competitor Min Price", "Increase 5% on all").forEach { rule ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { bulkMode = rule }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = bulkMode == rule, onClick = { bulkMode = rule })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(rule, fontSize = 13.sp, color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        productList.forEachIndexed { idx, itm ->
                            val newPrice = when (bulkMode) {
                                "Drop ₹20 on all products" -> maxOf(itm.minFloorPrice, itm.currentPrice - 20)
                                "Drop 5% discount on all" -> maxOf(itm.minFloorPrice, (itm.currentPrice * 0.95).toInt())
                                "Match Competitor Min Price" -> maxOf(itm.minFloorPrice, itm.competitorMinPrice)
                                else -> itm.currentPrice + (itm.currentPrice * 0.05).toInt()
                            }
                            productList[idx] = itm.copy(currentPrice = newPrice, isApplied = true)
                        }
                        showBulkDiscountDialog = false
                        Toast.makeText(context, "Bulk rule '$bulkMode' applied!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Apply Rule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDiscountDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
