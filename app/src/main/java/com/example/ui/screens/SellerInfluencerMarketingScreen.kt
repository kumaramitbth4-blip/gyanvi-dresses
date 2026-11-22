package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.SocialAdPlatform
import com.example.model.SocialMediaAdCampaign
import com.example.model.SocialMediaAdsConfig
import com.example.ui.components.QrCodeStudioDialog
import com.example.ui.components.SocialMediaAdsManagerSection
import com.example.ui.theme.*

// =============================================================================
// DATA MODELS FOR INFLUENCER & AFFILIATE MARKETING
// =============================================================================
data class InfluencerCreator(
    val id: String,
    val name: String,
    val instagramHandle: String,
    val youtubeChannel: String,
    val category: String,
    val followers: String,
    val avgViews: String,
    val engagementRate: String,
    val commissionPercent: Int,
    val fixedCollabFee: Int,
    val audienceLocation: String = "Bihar, UP, Delhi, Mumbai",
    val verifiedBadge: Boolean = true,
    val couponCode: String,
    val totalSalesGenerated: String = "₹48,500",
    val rating: Double = 4.9,
    @DrawableRes val avatarRes: Int = R.drawable.dress_anarkali_1793417013639
)

data class CollabCampaignItem(
    val id: String,
    val creatorName: String,
    val campaignName: String,
    val dressName: String,
    val status: String, // "Active", "Sample Dispatched", "Under Review", "Completed"
    val couponCode: String,
    val ordersCount: Int,
    val salesTotal: Int,
    val commissionEarned: Int,
    val paymentStatus: String = "Pending" // "Pending", "Paid"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullSellerInfluencerMarketingScreen(
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
    val context = LocalContext.current

    // Active Section Tab (0: Directory & Creators, 1: Campaigns, 2: Affiliate & Coupons, 3: Payments & Payouts, 4: Analytics & Fraud Guard, 5: AI Marketing Suite)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Account Role Mode: 0 = Seller Account, 1 = Admin Portal, 2 = Influencer Creator View
    var accountRoleMode by remember { mutableIntStateOf(0) }

    // Campaign Marketing Budget state (matching Screenshot 5:40)
    var campaignBudget by remember { mutableStateOf("₹5,000") }
    var dailyBudget by remember { mutableStateOf("₹500") }
    var commissionDefault by remember { mutableStateOf("10%") }
    var selectedCategoryFilter by remember { mutableStateOf("All Categories") }

    // 10 Categories specified by user
    val influencerCategories = listOf(
        "All Categories",
        "Fashion Influencers",
        "Beauty Influencers",
        "Saree Influencers",
        "Kurti Influencers",
        "Kids Fashion",
        "Women's Fashion",
        "Lifestyle Influencers",
        "Shopping Influencers",
        "YouTube Creators"
    )

    // Creators List matching user screenshot
    val creatorsList = remember {
        mutableStateListOf(
            InfluencerCreator(
                id = "inf1",
                name = "Priya Sharma",
                instagramHandle = "@priya_ethnic_glam",
                youtubeChannel = "Priya Ethnic Diaries",
                category = "Saree Influencers",
                followers = "240K Followers",
                avgViews = "45K Views/Reel",
                engagementRate = "4.8% High",
                commissionPercent = 8,
                fixedCollabFee = 1500,
                couponCode = "PRIYA10",
                totalSalesGenerated = "₹84,000",
                avatarRes = R.drawable.dress_anarkali_1793417013639
            ),
            InfluencerCreator(
                id = "inf2",
                name = "Simran Kaur",
                instagramHandle = "@simran_lehenga_couture",
                youtubeChannel = "Simran Styling Hub",
                category = "Fashion Influencers",
                followers = "180K Followers",
                avgViews = "38K Views/Reel",
                engagementRate = "5.2% Very High",
                commissionPercent = 10,
                fixedCollabFee = 2000,
                couponCode = "SIMRAN15",
                totalSalesGenerated = "₹62,500",
                avatarRes = R.drawable.dress_lehenga_1793417067159
            ),
            InfluencerCreator(
                id = "inf3",
                name = "Rhea Malhotra",
                instagramHandle = "@rhea_daily_kurtis",
                youtubeChannel = "Rhea Fashion Hauls",
                category = "Kurti Influencers",
                followers = "320K Followers",
                avgViews = "62K Views/Reel",
                engagementRate = "4.5% High",
                commissionPercent = 7,
                fixedCollabFee = 1800,
                couponCode = "RHEA07",
                totalSalesGenerated = "₹1,12,000",
                avatarRes = R.drawable.dress_anarkali_1793417013639
            ),
            InfluencerCreator(
                id = "inf4",
                name = "Ananya Sen",
                instagramHandle = "@ananya_saree_stories",
                youtubeChannel = "Ananya Silk Hauls",
                category = "Beauty Influencers",
                followers = "150K Followers",
                avgViews = "28K Views/Reel",
                engagementRate = "4.9% High",
                commissionPercent = 9,
                fixedCollabFee = 1200,
                couponCode = "ANANYA10",
                totalSalesGenerated = "₹45,000",
                avatarRes = R.drawable.dress_saree_1793417052582
            ),
            InfluencerCreator(
                id = "inf5",
                name = "Pooja & Aryan (Mom & Kids)",
                instagramHandle = "@pooja_kids_glam",
                youtubeChannel = "Pooja Kids Festive",
                category = "Kids Fashion",
                followers = "210K Followers",
                avgViews = "41K Views/Reel",
                engagementRate = "5.5% Top",
                commissionPercent = 8,
                fixedCollabFee = 1600,
                couponCode = "KIDSGLAM",
                totalSalesGenerated = "₹58,000",
                avatarRes = R.drawable.dress_anarkali_1793417013639
            )
        )
    }

    // Active Collaborations / Campaigns List
    val campaignsList = remember {
        mutableStateListOf(
            CollabCampaignItem("cp1", "Priya Sharma", "Festive Silk Saree Reel Collab", "Banarasi Silk Saree", "Active", "PRIYA10", 84, 84000, 6720, "Paid"),
            CollabCampaignItem("cp2", "Simran Kaur", "Bridal Lehenga Unboxing & Trial", "Designer Georgette Lehenga", "Sample Dispatched", "SIMRAN15", 42, 62500, 6250, "Pending"),
            CollabCampaignItem("cp3", "Rhea Malhotra", "Daily Casual Anarkali Kurti Reel", "Anarkali Embroidered Kurti", "Active", "RHEA07", 126, 112000, 7840, "Paid")
        )
    }

    // Modal Dialog states
    var showCollabRequestDialogForCreator by remember { mutableStateOf<InfluencerCreator?>(null) }
    var showAiScriptGeneratorDialog by remember { mutableStateOf(false) }
    var showPaymentReceiptDialog by remember { mutableStateOf(false) }

    // Reusable active text field colors
    val activeTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedContainerColor = CardWhite,
        unfocusedContainerColor = CardWhite,
        focusedBorderColor = MeeshoMagenta,
        unfocusedBorderColor = Color(0xFFCCCCCC),
        cursorColor = MeeshoMagenta,
        focusedLabelColor = MeeshoMagenta,
        unfocusedLabelColor = TextSecondary
    )

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack, modifier = Modifier.testTag("influencer_back_btn")) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Column {
                                Text(
                                    text = "Influencer Marketing",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = when (accountRoleMode) {
                                        1 -> "Admin Control Panel"
                                        2 -> "Creator / Influencer Portal"
                                        else -> "Gyanvi Dresses • Seller Hub"
                                    },
                                    fontSize = 11.sp,
                                    color = if (accountRoleMode == 1) MeeshoMagenta else TextSecondary
                                )
                            }
                        }

                        // Role Switcher: Seller, Admin, Creator
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = when (accountRoleMode) {
                                1 -> Color(0xFFF3E5F5)
                                2 -> Color(0xFFFFF3E0)
                                else -> Color(0xFFE8F5E9)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (accountRoleMode) {
                                    1 -> MeeshoMagenta
                                    2 -> Color(0xFFFF9800)
                                    else -> Color(0xFF4CAF50)
                                }
                            ),
                            modifier = Modifier
                                .clickable {
                                    accountRoleMode = (accountRoleMode + 1) % 3
                                    val modeName = when (accountRoleMode) {
                                        1 -> "Admin Control Mode"
                                        2 -> "Influencer / Creator Mode"
                                        else -> "Seller Account Mode"
                                    }
                                    Toast.makeText(context, "✓ $modeName Switched!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when (accountRoleMode) {
                                    1 -> "Admin View"
                                    2 -> "Creator View"
                                    else -> "Seller View"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (accountRoleMode) {
                                    1 -> MeeshoMagenta
                                    2 -> Color(0xFFE65100)
                                    else -> Color(0xFF2E7D32)
                                }
                            )
                        }
                    }

                    // 6 Comprehensive Navigation Tabs (matching user's sections)
                    val tabs = listOf(
                        "Creators Directory",
                        "Campaigns (${campaignsList.size})",
                        "Affiliate & Coupons",
                        "Payouts & Invoices",
                        "Analytics & Fraud Guard",
                        "AI Marketing Suite",
                        "Social Media Ads 📣"
                    )

                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = CardWhite,
                        contentColor = MeeshoMagenta,
                        edgePadding = 12.dp
                    ) {
                        tabs.forEachIndexed { idx, title ->
                            Tab(
                                selected = selectedTab == idx,
                                onClick = { selectedTab = idx },
                                text = {
                                    Text(
                                        title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == idx) MeeshoMagenta else TextSecondary
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // =================================================================
            // TAB 0: CREATORS DIRECTORY (SCREENSHOT 5:40 EXACT MATCH + ENHANCEMENTS)
            // =================================================================
            if (selectedTab == 0) {
                // Banner matching Screenshot 5:40
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Boost Orders with Top Creators",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF283593)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Collaborate with verified fashion creators who post video reels wearing your collection.",
                            fontSize = 12.sp,
                            color = Color(0xFF3F51B5),
                            lineHeight = 17.sp
                        )
                    }
                }

                // Campaign Marketing Budget Text Box (Screenshot 5:40)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Campaign Marketing Budget (मार्केटिंग बजट)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)

                        OutlinedTextField(
                            value = campaignBudget,
                            onValueChange = { campaignBudget = it },
                            label = { Text("Campaign Marketing Budget (Text Box)") },
                            singleLine = true,
                            colors = activeTextFieldColors,
                            modifier = Modifier.fillMaxWidth().testTag("campaign_budget_input")
                        )

                        // Quick budget preset pills
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("₹2,000", "₹5,000", "₹10,000", "₹25,000").forEach { pVal ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (campaignBudget == pVal) Color(0xFFEDE7F6) else Color(0xFFF5F5F5),
                                    border = if (campaignBudget == pVal) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5E35B1)) else null,
                                    modifier = Modifier.clickable { campaignBudget = pVal }
                                ) {
                                    Text(pVal, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }

                // 10 Influencer Categories Filter Chips
                Text("Influencer Categories (श्रेणियां)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(influencerCategories) { cat ->
                        val isSel = selectedCategoryFilter == cat
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSel) MeeshoMagenta else Color(0xFFEEEEEE),
                            modifier = Modifier.clickable { selectedCategoryFilter = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else TextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Verified Fashion Creators Header & Cards matching user screenshot
                Text("Verified Fashion Creators (${creatorsList.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)

                val filteredCreators = creatorsList.filter {
                    selectedCategoryFilter == "All Categories" || it.category.contains(selectedCategoryFilter, ignoreCase = true)
                }

                filteredCreators.forEach { creator ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Star Circle Icon Avatar (matching screenshot)
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFF9C4)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⭐", fontSize = 20.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(creator.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        if (creator.verifiedBadge) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFF1976D2), modifier = Modifier.size(15.dp))
                                        }
                                    }
                                    Text(
                                        text = "${creator.followers} • ${creator.category}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${creator.commissionPercent}% Commission • Collab: ₹${creator.fixedCollabFee}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Magenta Collab Button (Screenshot 5:40)
                                Button(
                                    onClick = {
                                        showCollabRequestDialogForCreator = creator
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp).testTag("collab_btn_${creator.id}")
                                ) {
                                    Text("Collab", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            // Social Handles & Engagement Details Strip
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF9F9F9),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(creator.instagramHandle, fontSize = 11.sp, color = Color(0xFFE91E63), fontWeight = FontWeight.SemiBold)
                                    Text("Avg Views: ${creator.avgViews}", fontSize = 10.sp, color = TextSecondary)
                                    Text("Eng: ${creator.engagementRate}", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 1: CAMPAIGNS & COLLABORATION MANAGEMENT
            // =================================================================
            if (selectedTab == 1) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Active Creator Campaigns (${campaignsList.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE8F5E9),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text("ROI: 3.8x", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                        Text("Track sample shipments, dress reels, and customer purchases per influencer.", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                campaignsList.forEach { camp ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(camp.campaignName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Text("Creator: ${camp.creatorName} • Dress: ${camp.dressName}", fontSize = 11.sp, color = TextSecondary)
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (camp.status == "Active") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                ) {
                                    Text(
                                        camp.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (camp.status == "Active") Color(0xFF2E7D32) else Color(0xFFE65100),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFFEEEEEE))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Orders", fontSize = 10.sp, color = TextSecondary)
                                    Text("${camp.ordersCount}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("Total Sales", fontSize = 10.sp, color = TextSecondary)
                                    Text("₹${camp.salesTotal}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1976D2))
                                }
                                Column {
                                    Text("Commission", fontSize = 10.sp, color = TextSecondary)
                                    Text("₹${camp.commissionEarned}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                                }
                                Column {
                                    Text("Payout", fontSize = 10.sp, color = TextSecondary)
                                    Text(camp.paymentStatus, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (camp.paymentStatus == "Paid") Color(0xFF2E7D32) else Color(0xFFFFA000))
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Coupon: ${camp.couponCode}", fontSize = 11.sp, color = Color(0xFF5E35B1), fontWeight = FontWeight.Bold)

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "✓ Sample tracking for ${camp.creatorName}: Out for Delivery via Delhivery", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Track Sample", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 2: AFFILIATE MARKETING & REFERRAL COUPON CODES
            // =================================================================
            if (selectedTab == 2) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Affiliate System & Creator Referral Links", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Each creator gets an exclusive coupon code and referral link for automated commission tracking.", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                listOf(
                    Triple("Priya Sharma", "PRIYA10", "https://gyanvi.com/c/priya10"),
                    Triple("Simran Kaur", "SIMRAN15", "https://gyanvi.com/c/simran15"),
                    Triple("Rhea Malhotra", "RHEA07", "https://gyanvi.com/c/rhea07")
                ).forEach { (creator, code, link) ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
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
                                Text(creator, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEDE7F6)
                                ) {
                                    Text(
                                        "Code: $code",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF5E35B1),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text("Referral Link: $link", fontSize = 11.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "✓ Referral link copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f).height(34.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Link", fontSize = 11.sp)
                                }

                                var showAffiliateQrDialog by remember(code) { mutableStateOf(false) }

                                OutlinedButton(
                                    onClick = { showAffiliateQrDialog = true },
                                    modifier = Modifier.weight(1f).height(34.dp)
                                ) {
                                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("QR Code", fontSize = 11.sp)
                                }

                                if (showAffiliateQrDialog) {
                                    QrCodeStudioDialog(
                                        initialContent = link,
                                        headerTitle = "AFFILIATE CODE: $code",
                                        footerSubtitle = link,
                                        fileNamePrefix = "Affiliate_QR_$code",
                                        onDismiss = { showAffiliateQrDialog = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 3: PAYOUTS & INVOICE MANAGEMENT
            // =================================================================
            if (selectedTab == 3) {
                // Payout Summary Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Influencer Payout & Commission Management", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Paid to Creators", fontSize = 11.sp, color = TextSecondary)
                                Text("₹14,560", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF2E7D32))
                            }
                            Column {
                                Text("Pending Commission", fontSize = 11.sp, color = TextSecondary)
                                Text("₹6,250", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFFFFA000))
                            }
                        }
                    }
                }

                listOf(
                    Triple("Simran Kaur", "₹6,250 Pending", "UPI: simran@okhdfcbank"),
                    Triple("Priya Sharma", "₹6,720 Paid", "Bank: HDFC Bank A/c ...4892"),
                    Triple("Rhea Malhotra", "₹7,840 Paid", "UPI: rhea@paytm")
                ).forEach { (name, amount, method) ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(method, fontSize = 11.sp, color = TextSecondary)
                                Text(amount, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (amount.contains("Paid")) Color(0xFF2E7D32) else Color(0xFFFFA000))
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, "✓ Instant Payout initiated via UPI to $name!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (amount.contains("Pending")) MeeshoMagenta else Color(0xFF3F51B5)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(if (amount.contains("Pending")) "Pay Now" else "Invoice", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "✓ Monthly Influencer TDS & Payment Report (.xlsx) Exported!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download Monthly Payout & Tax Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // =================================================================
            // TAB 4: ANALYTICS & FRAUD GUARD
            // =================================================================
            if (selectedTab == 4) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Campaign Performance & ROI Analytics", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Real-time revenue attribution and fake followers detection.", fontSize = 11.sp, color = TextSecondary)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricItem(title = "Total Views", value = "1.25M", sub = "Across 14 Reels", modifier = Modifier.weight(1f))
                            MetricItem(title = "Total Clicks", value = "42,800", sub = "4.2% CTR", modifier = Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricItem(title = "Orders Created", value = "252 Units", sub = "Conversion: 3.4%", modifier = Modifier.weight(1f))
                            MetricItem(title = "Total Sales", value = "₹2,58,500", sub = "Marketing ROI: 3.8x", modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Security & Fraud Guard Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF5E35B1))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Security & Fraud Protection Guard", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF311B92))
                        }
                        listOf(
                            "Fake Followers Detection: 96.2% Genuine Indian Audience verified" to true,
                            "Invalid Click Detection: Automated Bot filtration active" to true,
                            "Duplicate Account & Self-referral Order blocker enabled" to true,
                            "Creator Legal Collab Agreement digitally signed on file" to true
                        ).forEach { (secItem, isChecked) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(secItem, fontSize = 11.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 5: AI MARKETING SUITE
            // =================================================================
            if (selectedTab == 5) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MeeshoMagenta)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Influencer Marketing Suite", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Text("Generate viral Instagram captions, video scripts for reels, and trending hashtags.", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                // AI Generator Cards
                listOf(
                    Triple("AI Reel Video Script Generator", "Writes 15-30s hook scripts for creators showcasing Gyanvi gowns", Icons.Default.MovieFilter),
                    Triple("AI Viral Caption & Hashtags", "Instant trending hashtags #GyanviDresses #EthnicFestive", Icons.Default.Tag),
                    Triple("AI Influencer Matchmaker", "Scans best match creators based on your ₹5,000 budget", Icons.Default.Psychology),
                    Triple("AI Sales Prediction", "Forecasts 60-80 orders from current weekend creator campaign", Icons.Default.TrendingUp)
                ).forEach { (toolTitle, toolDesc, icon) ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFFCE4EC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(toolTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(toolDesc, fontSize = 11.sp, color = TextSecondary)
                            }
                            Button(
                                onClick = { showAiScriptGeneratorDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Generate", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 6: SOCIAL MEDIA ADS STUDIO (IG / FB / YT / WHATSAPP / GOOGLE)
            // =================================================================
            if (selectedTab == 6) {
                SocialMediaAdsManagerSection(
                    config = socialAdsConfig,
                    accentColor = MeeshoMagenta,
                    onToggleAdsEnabled = onToggleSocialAdsEnabled,
                    onToggleShowOnHome = onToggleSocialAdsHome,
                    onSaveCampaign = onSaveSocialAdCampaign,
                    onToggleCampaignActive = onToggleSocialAdActive,
                    onDeleteCampaign = onDeleteSocialAdCampaign,
                    onRecordAdClick = onRecordSocialAdClick,
                    modifier = Modifier.testTag("seller_marketing_social_ads")
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // =========================================================================
    // MODAL: SEND COLLABORATION REQUEST
    // =========================================================================
    showCollabRequestDialogForCreator?.let { creator ->
        var selectedDress by remember { mutableStateOf("Girls Premium Net Embroidered Ethnic Gown") }
        var proposedFee by remember { mutableStateOf("₹${creator.fixedCollabFee}") }
        var proposedComm by remember { mutableStateOf("${creator.commissionPercent}%") }

        AlertDialog(
            onDismissRequest = { showCollabRequestDialogForCreator = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Handshake, contentDescription = null, tint = MeeshoMagenta)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Collab with ${creator.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Send collaboration offer and dispatch product sample dress for Instagram Reel.", fontSize = 11.sp, color = TextSecondary)

                    OutlinedTextField(
                        value = selectedDress,
                        onValueChange = { selectedDress = it },
                        label = { Text("Product to Promote") },
                        singleLine = true,
                        colors = activeTextFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = proposedFee,
                            onValueChange = { proposedFee = it },
                            label = { Text("Fixed Fee") },
                            singleLine = true,
                            colors = activeTextFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = proposedComm,
                            onValueChange = { proposedComm = it },
                            label = { Text("Commission") },
                            singleLine = true,
                            colors = activeTextFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Includes free sample dispatch to ${creator.name} with unique coupon ${creator.couponCode}.",
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        campaignsList.add(
                            0,
                            CollabCampaignItem(
                                id = System.currentTimeMillis().toString(),
                                creatorName = creator.name,
                                campaignName = "$selectedDress Reel",
                                dressName = selectedDress,
                                status = "Sample Dispatched",
                                couponCode = creator.couponCode,
                                ordersCount = 0,
                                salesTotal = 0,
                                commissionEarned = 0,
                                paymentStatus = "Pending"
                            )
                        )
                        showCollabRequestDialogForCreator = null
                        selectedTab = 1 // Switch to campaigns tab
                        Toast.makeText(context, "🎉 Collab request sent to ${creator.name}! Sample scheduled for pickup.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Send Offer & Sample")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCollabRequestDialogForCreator = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // =========================================================================
    // MODAL: AI SCRIPT & CAPTION GENERATOR
    // =========================================================================
    if (showAiScriptGeneratorDialog) {
        AlertDialog(
            onDismissRequest = { showAiScriptGeneratorDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MeeshoMagenta)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Reel Script & Caption", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Hook (0-3s): 'Stop scrolling girls! If you need a royal wedding gown under ₹999...'", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MeeshoMagenta)
                    Text("Body (3-15s): 'Look at the flare of this pure georgette gown from Gyanvi Dresses! Sequin work is so delicate and comes with ready-to-wear bottom and dupatta.'", fontSize = 11.sp, color = TextPrimary)
                    Text("Call to Action: 'Use my code PRIYA10 for extra 10% off on Gyanvi Dresses app! Link in bio.'", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    Text("Hashtags: #GyanviDresses #EthnicFashion #WeddingWear #KurtiHaul #OOTDIndia", fontSize = 10.sp, color = Color(0xFF3F51B5), fontFamily = FontFamily.Monospace)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "✓ AI Script & Hashtags Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                        showAiScriptGeneratorDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Copy Script")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiScriptGeneratorDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun MetricItem(
    title: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF9F9F9),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 10.sp, color = TextSecondary)
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextPrimary)
            Text(sub, fontSize = 9.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
        }
    }
}
