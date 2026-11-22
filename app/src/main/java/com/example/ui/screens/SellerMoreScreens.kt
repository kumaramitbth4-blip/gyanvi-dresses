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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

// =========================================================================
// 1. PRICE RECOMMENDATION SCREEN (Screenshot 6:05 - Boost Sales)
// =========================================================================
@Composable
fun SellerPriceRecommendationScreen(onBack: () -> Unit) {
    FullSellerPriceRecommendationScreen(onBack = onBack)
}

// =========================================================================
// 2. BUSINESS DASHBOARD SCREEN (Screenshot 6:05 - Performance)
// =========================================================================
@Composable
fun SellerBusinessDashboardScreen(
    orders: List<com.example.model.OrderItem> = com.example.data.SampleData.initialOrders,
    products: List<com.example.model.Product> = com.example.data.SampleData.sampleProducts,
    cloudSyncState: com.example.viewmodel.CloudSyncState = com.example.viewmodel.CloudSyncState.Idle,
    firebaseUserEmail: String = "",
    sellerOrderActions: com.example.model.SellerOrderActions = com.example.model.SellerOrderActions(),
    initialSelectedTab: Int = 0,
    initialStatusFilter: DashboardOrderStatusFilter = DashboardOrderStatusFilter.PENDING,
    onGoogleCloudSignInClick: () -> Unit = {},
    onSyncAllOrdersClick: () -> Unit = {},
    onBack: () -> Unit
) {
    FullSellerBusinessDashboardScreen(
        orders = orders,
        products = products,
        cloudSyncState = cloudSyncState,
        firebaseUserEmail = firebaseUserEmail,
        sellerOrderActions = sellerOrderActions,
        initialSelectedTab = initialSelectedTab,
        initialStatusFilter = initialStatusFilter,
        onGoogleCloudSignInClick = onGoogleCloudSignInClick,
        onSyncAllOrdersClick = onSyncAllOrdersClick,
        onBack = onBack
    )
}

// =========================================================================
// 3. LEARNING HUB SCREEN (Screenshot 6:05 - More)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerLearningHubScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }

    data class TutorialVideo(
        val title: String,
        val duration: String,
        val views: String,
        val category: String,
        val description: String
    )

    val tutorials = remember {
        listOf(
            TutorialVideo(
                "How to get 100+ Orders in your first 7 days",
                "8 mins",
                "48K views",
                "Growth",
                "Learn pricing sweet spots, optimal listing tags and smart coin strategies for ethnic wear."
            ),
            TutorialVideo(
                "Packaging tips to win 100% RTO courier compensation",
                "6 mins",
                "32K views",
                "Operations",
                "Guide on barcode placement, sealing standards, and video evidence requirements."
            ),
            TutorialVideo(
                "Catalog Photography on Mobile: Kurtis & Sarees",
                "10 mins",
                "29K views",
                "Photography",
                "Capture natural daylight angles, accurate colors, and fabric weave details without a studio."
            ),
            TutorialVideo(
                "Understanding Weekly Payouts, TCS & GST Deductions",
                "12 mins",
                "19K views",
                "Finance",
                "Complete walkthrough of net bank settlements, shipping adjustments, and credit notes."
            ),
            TutorialVideo(
                "Partnering with Instagram Creators for 5x Sales",
                "9 mins",
                "22K views",
                "Marketing",
                "How to send sample dresses, generate creator coupon codes, and approve viral reels."
            )
        )
    }

    val filteredTutorials = remember(selectedCategory) {
        if (selectedCategory == "All") tutorials else tutorials.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("learning_hub_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Learning Hub", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                        Text("Masterclasses & guides to scale your business", fontSize = 11.sp, color = TextSecondary)
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
                .padding(16.dp)
        ) {
            // Seller Certification Badge Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MeeshoMagenta),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gyanvi Seller Academy", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MeeshoMagenta)
                        Text("Level 2 Certified Partner (75% completed). Finish 1 more lesson to earn the Trusted Seller badge.", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Filter Pills
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Growth", "Operations", "Photography", "Finance", "Marketing").forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MeeshoMagenta,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Lessons & Masterclasses", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            filteredTutorials.forEach { tutorial ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            Toast.makeText(context, "Playing lesson: ${tutorial.title}", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF3E5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayCircleFilled, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tutorial.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(tutorial.description, fontSize = 11.sp, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tutorial.duration, fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                Text(" • ", fontSize = 11.sp, color = Color.Gray)
                                Text(tutorial.views, fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 4. NOTICE BOARD SCREEN (Screenshot 6:05 - More)
// =========================================================================
@Composable
fun SellerNoticeBoardScreen(onBack: () -> Unit) {
    FullSellerNoticeBoardScreen(onBack = onBack)
}

// =========================================================================
// 5. SUPPORT SCREEN (Screenshot 6:05 - More)
// =========================================================================
@Composable
fun SellerSupportScreen(onBack: () -> Unit) {
    FullSellerSupportHelpdeskScreen(onBack = onBack)
}

// =========================================================================
// 6. SETTINGS SCREEN (Screenshot 6:05 - More)
// =========================================================================
@Composable
fun SellerSettingsScreen(onBack: () -> Unit) {
    FullSellerSettingsDashboardScreen(onBack = onBack)
}

