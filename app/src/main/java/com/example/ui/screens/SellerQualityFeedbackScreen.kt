package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import com.example.R
import com.example.ui.theme.*
import com.example.util.ImageSaver
import com.example.util.QrCodeGenerator
import kotlinx.coroutines.delay

// =============================================================================
// DATA MODELS FOR QUALITY & CUSTOMER FEEDBACK
// =============================================================================
data class QualityReturnReason(
    val id: Int,
    val titleHindi: String,
    val titleEnglish: String,
    val percentage: String,
    val affectedUnits: Int,
    val color: Color,
    val actionAdvice: String
)

data class CustomerReviewItem(
    val id: String,
    val customerName: String,
    val city: String,
    val rating: Int,
    val date: String,
    val productName: String,
    val comment: String,
    val verifiedPurchase: Boolean = true,
    @DrawableRes val photoRes: Int? = null,
    val hasVideo: Boolean = false,
    val videoDurationSec: Int = 18,
    var sellerReply: String? = null
)

data class ProductQualityItem(
    val id: String,
    val name: String,
    val sku: String,
    val soldQuantity: Int,
    val defectQuantity: Int,
    val qualityScore: Double,
    val returnRate: Double,
    val fabricStatus: String = "Passed",
    val stitchingStatus: String = "Double Lock Passed",
    val sizeChartStatus: String = "Verified 100%",
    @DrawableRes val imageRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerQualityFeedbackScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Active Tab in the Quality & Feedback hub (0 to 7 matching user's 8 sections)
    var selectedSectionTab by remember { mutableIntStateOf(0) }

    // Role switcher: Seller view vs Admin Audit view
    var isAdminMode by remember { mutableStateOf(false) }

    // Dialog states
    var showUploadMediaDialog by remember { mutableStateOf(false) }
    var showReplyDialogForReview by remember { mutableStateOf<CustomerReviewItem?>(null) }
    var sellerReplyText by remember { mutableStateOf("") }
    var showCertificateDialog by remember { mutableStateOf(false) }
    var showDamagedReportDialog by remember { mutableStateOf(false) }
    var activeVideoPlayingReview by remember { mutableStateOf<CustomerReviewItem?>(null) }

    // Active Reviews State
    val reviewsList = remember {
        mutableStateListOf(
            CustomerReviewItem(
                id = "rev1",
                customerName = "Pooja Sharma",
                city = "Patna, Bihar",
                rating = 5,
                date = "Yesterday",
                productName = "Anarkali Embroidered Kurti Set",
                comment = "Fabric quality is pure soft georgette. Stitching is neat with double lining, no color bleeding after wash. High quality festive dress!",
                photoRes = R.drawable.dress_anarkali_1793417013639,
                hasVideo = true,
                videoDurationSec = 15,
                sellerReply = "Thank you Pooja ji! We use authentic 100% pure fabrics and precision double-seam stitching for Gyanvi Dresses."
            ),
            CustomerReviewItem(
                id = "rev2",
                customerName = "Anjali Kumari",
                city = "Muzaffarpur",
                rating = 5,
                date = "2 days ago",
                productName = "Banarasi Silk Saree",
                comment = "Pallu zari work is stunning! Shipped fast in secure barcoded box. Look at the unboxing video, rich royal shine.",
                photoRes = R.drawable.dress_saree_1793417052582,
                hasVideo = true,
                videoDurationSec = 22
            ),
            CustomerReviewItem(
                id = "rev3",
                customerName = "Kavita Verma",
                city = "Varanasi",
                rating = 4,
                date = "3 days ago",
                productName = "Designer Georgette Lehenga",
                comment = "Lehenga flare and cancan are heavy. Fitting is very good, only sleeve needed minor 0.5 inch adjustment. Highly recommended!",
                photoRes = R.drawable.dress_lehenga_1793417067159,
                hasVideo = false
            ),
            CustomerReviewItem(
                id = "rev4",
                customerName = "Sunita Devi",
                city = "Bettiah",
                rating = 5,
                date = "5 days ago",
                productName = "Peplum Kurti & Sharara Set",
                comment = "Gyanvi Dresses quality is always superior to marketplace generic sellers. Fabric is breathable and color matches photo 100%.",
                photoRes = R.drawable.dress_anarkali_1793417013639,
                hasVideo = true,
                videoDurationSec = 19
            )
        )
    }

    // Product quality items
    val productQualityList = remember {
        mutableStateListOf(
            ProductQualityItem("pq1", "Anarkali Embroidered Kurti Set", "GWN-ANR-01", 620, 2, 4.9, 1.2, imageRes = R.drawable.dress_anarkali_1793417013639),
            ProductQualityItem("pq2", "Banarasi Silk Festive Saree", "GWN-BAN-02", 480, 1, 4.9, 0.8, imageRes = R.drawable.dress_saree_1793417052582),
            ProductQualityItem("pq3", "Designer Georgette Lehenga", "GWN-LEH-03", 320, 2, 4.8, 1.5, imageRes = R.drawable.dress_lehenga_1793417067159)
        )
    }

    // 10 Detailed Return Reasons specified by the user
    val returnReasonsList = remember {
        listOf(
            QualityReturnReason(1, "Fabric Quality Issue", "कपड़े की खराब क्वालिटी", "0.04%", 2, Color(0xFF2E7D32), "Lab tested 100% pure thread count. Defect rate negligible."),
            QualityReturnReason(2, "Stitching Issue", "सिलाई की समस्या", "0.03%", 1, Color(0xFF2E7D32), "Double interlock stitching checked on all hem lines."),
            QualityReturnReason(3, "Size Mismatch", "साइज छोटा या बड़ा होना", "0.08%", 5, Color(0xFF1976D2), "Indian standard size chart updated with bust/waist in inches."),
            QualityReturnReason(4, "Color Mismatch / Lighting", "रंग अलग होना", "0.06%", 3, Color(0xFFFFA000), "True-tone 5500K studio photography without excessive filters."),
            QualityReturnReason(5, "Product Damaged in Transit", "सामान खराब मिलना", "0.02%", 1, Color(0xFFD32F2F), "Using 3-ply heavy corrugated cartons with bubble seal."),
            QualityReturnReason(6, "Wrong Product Delivered", "गलत सामान डिलीवर होना", "0.01%", 1, Color(0xFFD32F2F), "Automated barcode scan matching at dispatch center."),
            QualityReturnReason(7, "Missing Item / Dupatta", "सामान गायब होना", "0.02%", 1, Color(0xFFE65100), "3-piece check (Kurti, Bottom, Dupatta) before packing."),
            QualityReturnReason(8, "Product Description Mismatch", "फोटो और असली सामान में अंतर", "0.03%", 2, Color(0xFF0288D1), "Accurate fabric & flare measurements listed on detail page."),
            QualityReturnReason(9, "Late Delivery by Courier", "सामान देर से पहुँचना", "0.05%", 3, Color(0xFF7B1FA2), "Dispatched in under 18 hours; courier escalated on delay."),
            QualityReturnReason(10, "Customer Changed Mind", "ग्राहक का मन बदलना", "0.12%", 7, Color(0xFF757575), "Standard non-defect return; instant restocking enabled.")
        )
    }

    BackHandler {
        if (activeVideoPlayingReview != null) {
            activeVideoPlayingReview = null
        } else {
            onBack()
        }
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
                            IconButton(onClick = onBack, modifier = Modifier.testTag("quality_back_btn")) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Column {
                                Text(
                                    text = "Quality & Customer Feedback",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isAdminMode) "Admin Quality Audit Portal" else "Gyanvi Dresses • Seller Hub",
                                    fontSize = 11.sp,
                                    color = if (isAdminMode) MeeshoMagenta else TextSecondary
                                )
                            }
                        }

                        // Seller vs Admin Toggle & Download PDF Report
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isAdminMode) Color(0xFFF3E5F5) else Color(0xFFE8F5E9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isAdminMode) MeeshoMagenta else Color(0xFF4CAF50)),
                                modifier = Modifier
                                    .clickable {
                                        isAdminMode = !isAdminMode
                                        Toast.makeText(
                                            context,
                                            if (isAdminMode) "✓ Admin Quality Audit Mode Activated" else "✓ Seller Store Mode Activated",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isAdminMode) "Admin View" else "Seller View",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAdminMode) MeeshoMagenta else Color(0xFF2E7D32)
                                )
                            }

                            IconButton(onClick = {
                                Toast.makeText(context, "✓ Quality Report (PDF & Excel) Downloaded to device storage!", Toast.LENGTH_LONG).show()
                            }) {
                                Icon(Icons.Default.Download, contentDescription = "Download Report", tint = TextPrimary)
                            }
                        }
                    }

                    // 8 Main Section Navigation Tabs (Horizontal scrollable tabs)
                    val sectionTitles = listOf(
                        "Overview Score",
                        "Return Reasons (10)",
                        "Reviews & Video",
                        "Product Quality",
                        "Dashboard (Qty)",
                        "Packaging & AWB",
                        "Top Seller Badges",
                        "AI Improvement"
                    )

                    ScrollableTabRow(
                        selectedTabIndex = selectedSectionTab,
                        containerColor = CardWhite,
                        contentColor = MeeshoMagenta,
                        edgePadding = 12.dp
                    ) {
                        sectionTitles.forEachIndexed { idx, title ->
                            Tab(
                                selected = selectedSectionTab == idx,
                                onClick = { selectedSectionTab = idx },
                                text = {
                                    Text(
                                        title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedSectionTab == idx) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedSectionTab == idx) MeeshoMagenta else TextSecondary
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
            // TAB 0: OVERALL QUALITY SCORE (Screenshot Match + Deep Metrics)
            // =================================================================
            if (selectedSectionTab == 0) {
                // Hero Score Card matching the user's screenshot
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Overall Quality Score", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Tier 1 Seller (Top 5%)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("4.8", fontWeight = FontWeight.ExtraBold, fontSize = 42.sp, color = Color(0xFF2E7D32))
                            Text(
                                " / 5.0 ★",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Customer Satisfaction", fontSize = 11.sp, color = TextSecondary)
                                Text("96.4% Happy", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                            }
                        }

                        HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 12.dp))

                        // Detailed Score Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScoreMiniItem(title = "Product Quality", score = "4.9/5", sub = "Zero Defects", modifier = Modifier.weight(1f))
                            ScoreMiniItem(title = "Packaging", score = "4.8/5", sub = "3-Ply Boxes", modifier = Modifier.weight(1f))
                            ScoreMiniItem(title = "Delivery Speed", score = "4.7/5", sub = "18h Dispatch", modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Seller Performance Rank & Badges Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Seller Performance Rank", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Rank #142 among 12,500+ Indian Ethnic Wear Sellers", fontSize = 11.sp, color = TextSecondary)
                            }
                            IconButton(onClick = { showCertificateDialog = true }) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = "Certificate", tint = Color(0xFFFFB300))
                            }
                        }

                        // Badges Row
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BadgePill(title = "Top Seller", color = Color(0xFFFFB300), icon = Icons.Default.Star)
                            BadgePill(title = "Fast Dispatch", color = Color(0xFF1976D2), icon = Icons.Default.ElectricBolt)
                            BadgePill(title = "Quality Champion", color = Color(0xFF2E7D32), icon = Icons.Default.Verified)
                        }
                    }
                }

                // Return Reasons Summary
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Customer Return Reasons (Top Factors)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            TextButton(onClick = { selectedSectionTab = 1 }) {
                                Text("View All 10 Reasons >", fontSize = 11.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                            }
                        }

                        returnReasonsList.take(4).forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.titleEnglish, fontSize = 13.sp, color = TextPrimary)
                                Text(item.percentage, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = item.color)
                            }
                        }
                    }
                }

                // Tips Card from user screenshot
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tips to Maintain Top Seller Rating", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF3F51B5))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Dispatch orders within 24 hours to earn Faster Dispatch badges.", fontSize = 12.sp, color = Color(0xFF303F9F))
                        Text("• Double-check size labels on Kurtis and Blouses before sealing.", fontSize = 12.sp, color = Color(0xFF303F9F))
                        Text("• Use original Barcoded Packets to qualify for 100% RTO compensation.", fontSize = 12.sp, color = Color(0xFF303F9F))
                        Text("• Provide accurate real-lighting photos to reduce color mismatch claims.", fontSize = 12.sp, color = Color(0xFF303F9F))
                    }
                }
            }

            // =================================================================
            // TAB 1: 10 DETAILED CUSTOMER RETURN REASONS ANALYSIS
            // =================================================================
            if (selectedSectionTab == 1) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Customer Return Reasons Analysis (10 मुख्य कारण)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Text("Total Return Rate: 4.1% • Industry Average: 12.5% (Exceptional Control)", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                    }
                }

                returnReasonsList.forEach { reason ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(24.dp).clip(CircleShape).background(reason.color.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${reason.id}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = reason.color)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(reason.titleEnglish, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                        Text(reason.titleHindi, fontSize = 11.sp, color = TextSecondary)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(reason.percentage, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = reason.color)
                                    Text("${reason.affectedUnits} orders", fontSize = 10.sp, color = TextMuted)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF9F9F9),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Action Advice: ${reason.actionAdvice}", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 2: CUSTOMER FEEDBACK & REVIEWS (Photos + Playing Video + Upload)
            // =================================================================
            if (selectedSectionTab == 2) {
                // Star Breakdown Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ratings & Reviews Distribution", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Button(
                                onClick = { showUploadMediaDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upload Video/Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Star Bars
                        listOf(
                            5 to 0.82f,
                            4 to 0.12f,
                            3 to 0.04f,
                            2 to 0.01f,
                            1 to 0.01f
                        ).forEach { (star, frac) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("$star ★", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                                LinearProgressIndicator(
                                    progress = { frac },
                                    modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = if (star >= 4) Color(0xFF2E7D32) else if (star == 3) Color(0xFFFFA000) else Color(0xFFD32F2F),
                                    trackColor = Color(0xFFEEEEEE)
                                )
                                Text("${(frac * 100).toInt()}%", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.width(32.dp))
                            }
                        }
                    }
                }

                // Interactive Video Player if a review's video is currently active!
                activeVideoPlayingReview?.let { activeVideo ->
                    InteractiveVideoPlayerCard(
                        review = activeVideo,
                        onClose = { activeVideoPlayingReview = null }
                    )
                }

                // Reviews List
                Text("Verified Customer Reviews (${reviewsList.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)

                reviewsList.forEach { review ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(review.customerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${review.city} • ${review.date}", fontSize = 11.sp, color = TextMuted)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text("${review.rating} ★", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    if (review.verifiedPurchase) {
                                        Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Text("Product: ${review.productName}", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Text(review.comment, fontSize = 12.sp, color = TextPrimary, lineHeight = 17.sp)

                            // Attached Media: Photo & Video Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                review.photoRes?.let { pRes ->
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                ImageSaver.saveToGallery(context, pRes, review.productName)
                                                Toast.makeText(context, "✓ फोटो गैलरी में सेव हो गई!", Toast.LENGTH_SHORT).show()
                                            }
                                    ) {
                                        Image(
                                            painter = painterResource(id = pRes),
                                            contentDescription = "Review Photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(topStart = 4.dp),
                                            modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }

                                if (review.hasVideo) {
                                    // Interactive Play Video Card
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF311B92),
                                        modifier = Modifier
                                            .height(68.dp)
                                            .weight(1f)
                                            .clickable {
                                                activeVideoPlayingReview = review
                                                Toast.makeText(context, "▶ वीडियो शुरू हुआ! (Video Playing)", Toast.LENGTH_SHORT).show()
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color(0xFF311B92), modifier = Modifier.size(24.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text("Customer Review Video", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("Tap to play video (${review.videoDurationSec}s)", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            // Seller Reply if exists
                            review.sellerReply?.let { reply ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF3E5F5),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCE93D8)),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Storefront, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Seller Reply (विक्रेता का जवाब)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MeeshoMagenta)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(reply, fontSize = 11.sp, color = TextPrimary)
                                    }
                                }
                            }

                            // Action buttons: Reply, Report Fake
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        showReplyDialogForReview = review
                                        sellerReplyText = review.sellerReply ?: ""
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(15.dp), tint = MeeshoMagenta)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (review.sellerReply != null) "Edit Reply" else "Reply as Seller", fontSize = 11.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                                }

                                TextButton(
                                    onClick = {
                                        Toast.makeText(context, "Review flagged for moderation audit", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextMuted)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Report Fake Review", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 3: PRODUCT QUALITY MANAGEMENT (Product-wise Quality & Quantity)
            // =================================================================
            if (selectedSectionTab == 3) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Product-wise Quality Management", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Monitor defect ratios, fabric testing and size verification per catalog item.", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                productQualityList.forEach { pItem ->
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
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(68.dp).clip(RoundedCornerShape(8.dp))) {
                                    Image(
                                        painter = painterResource(id = pItem.imageRes),
                                        contentDescription = pItem.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    IconButton(
                                        onClick = {
                                            ImageSaver.saveToGallery(context, pItem.imageRes, pItem.name)
                                            Toast.makeText(context, "✓ फोटो गैलरी में सेव हो गई!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.align(Alignment.BottomEnd).size(22.dp).background(Color.Black.copy(alpha = 0.6f))
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pItem.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("SKU: ${pItem.sku} • Sold: ${pItem.soldQuantity} Units", fontSize = 11.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("Score: ${pItem.qualityScore} ★", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 12.sp)
                                        Text("Returns: ${pItem.returnRate}%", fontWeight = FontWeight.Bold, color = Color(0xFF1976D2), fontSize = 12.sp)
                                        Text("Defects: ${pItem.defectQuantity}", fontSize = 12.sp, color = TextSecondary)
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFFEEEEEE))

                            // Checks row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                QualityCheckChip(label = "Fabric", status = pItem.fabricStatus)
                                QualityCheckChip(label = "Stitching", status = pItem.stitchingStatus)
                                QualityCheckChip(label = "Size Chart", status = pItem.sizeChartStatus)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        Toast.makeText(context, "✓ Inspection report for ${pItem.sku} exported to PDF", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFFD32F2F))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Download Inspection Report", fontSize = 11.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFE8F5E9),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("QC Verified", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 4: SELLER PERFORMANCE DASHBOARD (Quantity & Operations)
            // =================================================================
            if (selectedSectionTab == 4) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Seller Performance Dashboard (मात्रा एवं परफॉर्मेंस)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("All order quantities, RTOs, fulfillment timelines and customer metrics.", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                // Grid of 8 key metrics from prompt
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(title = "Total Orders", value = "1,420", sub = "कुल ऑर्डर", color = Color(0xFF1976D2), modifier = Modifier.weight(1f))
                    MetricCard(title = "Delivered", value = "1,310", sub = "डिलीवर हुए (92.3%)", color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(title = "Return Orders", value = "58", sub = "वापस आए (4.1%)", color = Color(0xFFFFA000), modifier = Modifier.weight(1f))
                    MetricCard(title = "RTO Orders", value = "32", sub = "डिलीवरी न होने पर (2.2%)", color = Color(0xFFD32F2F), modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(title = "Cancellation Rate", value = "1.4%", sub = "कैंसिल ऑर्डर (< 3%)", color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                    MetricCard(title = "Return Percentage", value = "4.1%", sub = "रिटर्न प्रतिशत", color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(title = "Customer Rating", value = "4.8 ★", sub = "ग्राहक रेटिंग (1,280+)", color = Color(0xFFFFB300), modifier = Modifier.weight(1f))
                    MetricCard(title = "Dispatch Time", value = "18.2 Hrs", sub = "सामान भेजने का समय", color = Color(0xFF7B1FA2), modifier = Modifier.weight(1f))
                }

                // Performance Graph Visualizer
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Weekly Quality & Orders Graph (Mon - Sun)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        WeeklyPerformanceChart()
                    }
                }
            }

            // =================================================================
            // TAB 5: PACKAGING & SHIPPING QUALITY (Barcode, QR, AWB Tracking)
            // =================================================================
            if (selectedSectionTab == 5) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Packaging & Shipping Quality", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Generate barcodes, shipping labels, verify packing and report damages.", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                // Barcode & QR Code Visual Generator Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var qrCodeText by remember { mutableStateOf("GYANVI-849204") }
                    var activeQrPayload by remember { mutableStateOf("GYANVI-849204") }
                    var qrSaveStatus by remember { mutableStateOf<String?>(null) }
                    val generatedQrBitmap = remember(activeQrPayload) {
                        QrCodeGenerator.generateQrBitmap(
                            content = activeQrPayload,
                            sizePx = 640,
                            headerTitle = "GYANVI DISPATCH QR",
                            footerSubtitle = activeQrPayload
                        )
                    }

                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Interactive Barcode & QR Code Generator", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)

                        OutlinedTextField(
                            value = qrCodeText,
                            onValueChange = { qrCodeText = it },
                            label = { Text("Enter Order ID / AWB / Link for QR") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val trimmed = qrCodeText.trim().ifBlank { "GYANVI-849204" }
                                activeQrPayload = trimmed
                                qrSaveStatus = "✓ Generated QR Code for $trimmed"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate New QR Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Barcode visual
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                BarcodeVisualComponent(codeText = activeQrPayload)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("AWB: $activeQrPayload", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }

                            // Real scannable QR code preview
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    bitmap = generatedQrBitmap.asImageBitmap(),
                                    contentDescription = "Generated Dispatch QR Code",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(104.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Scan for Dispatch", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (!qrSaveStatus.isNullOrBlank()) {
                            Text(
                                qrSaveStatus.orEmpty(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val res = ImageSaver.saveBitmapToGallery(
                                        context = context,
                                        bitmap = generatedQrBitmap,
                                        fileNamePrefix = "Dispatch_QR_$activeQrPayload"
                                    )
                                    qrSaveStatus = res.message
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val labelBmp = QrCodeGenerator.generateShippingLabelBitmap(
                                        orderId = activeQrPayload,
                                        awbNumber = activeQrPayload,
                                        customerName = "Gyanvi Dispatch Consignee",
                                        destination = "Bettiah, Bihar - 845438",
                                        productTitle = "Gyanvi Designer Dress",
                                        sku = "GYN-SKU-01",
                                        size = "L",
                                        amount = 599,
                                        paymentMethod = "Prepaid UPI"
                                    )
                                    val res = ImageSaver.saveBitmapToDownloads(
                                        context = context,
                                        bitmap = labelBmp,
                                        fileNamePrefix = "ShippingLabel_$activeQrPayload"
                                    )
                                    qrSaveStatus = res.message
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download Label", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Shipping Checklist & Status
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Fulfillment Quality Verification", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        listOf(
                            "AWB Tracking Active (Bluedart, Delhivery, Ekart)" to true,
                            "Product Weight Verification: 380g per dress recorded" to true,
                            "Tamper-Proof Barcoded Packaging Seal verified" to true,
                            "Dispatch Confirmation within 24 Hours guaranteed" to true,
                            "Courier Pickup Status: Scheduled 2:00 PM Today" to true
                        ).forEach { (task, isDone) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(task, fontSize = 11.sp, color = TextPrimary)
                            }
                        }

                        OutlinedButton(
                            onClick = { showDamagedReportDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Report Damaged Parcel / RTO Issue", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // =================================================================
            // TAB 6: TOP SELLER BENEFITS & BADGES
            // =================================================================
            if (selectedSectionTab == 6) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Top Seller Benefits & Rewards (टॉप सेलर लाभ)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Based on your 4.8★ rating, Gyanvi Dresses has unlocked Tier 1 seller privileges.", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                listOf(
                    Triple("Top Seller Badge", "Featured on homepage & search results with gold crown", Icons.Default.WorkspacePremium),
                    Triple("Fast Dispatch Star", "Enables 1-day delivery eligibility for customers", Icons.Default.Bolt),
                    Triple("Zero Defect Leader", "Low return protection and priority support from Gyanvi Dresses", Icons.Default.Security),
                    Triple("Higher Product Visibility", "+35% organic impression boost across all catalog", Icons.Default.TrendingUp),
                    Triple("Seller Achievement Rewards", "Earned ₹5,000 promotion credits this month", Icons.Default.MonetizationOn),
                    Triple("Performance Certificate", "Download officially certified Gyanvi Dresses Quality Award", Icons.Default.CardMembership),
                    Triple("Special 0% Commission", "0% platform fee on next 100 fulfilled orders", Icons.Default.Percent)
                ).forEach { (title, desc, icon) ->
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
                                modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0xFFFFF3E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text(desc, fontSize = 11.sp, color = TextSecondary)
                            }
                            Icon(Icons.Default.Check, contentDescription = "Active", tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Button(
                    onClick = { showCertificateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download Official Seller Quality Certificate", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // =================================================================
            // TAB 7: AI QUALITY IMPROVEMENT & ADVANCED ANALYSIS
            // =================================================================
            if (selectedSectionTab == 7) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Quality & Sentiment Analysis", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        }
                        Text("Automatic return reason detection & sentiment analytics powered for Indian fashion.", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                // AI Sentiment Breakdown Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("AI Customer Feedback Sentiment", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MeeshoMagenta)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Positive: 94.2%", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 12.sp)
                            Text("Neutral: 4.6%", fontWeight = FontWeight.Bold, color = Color(0xFF1976D2), fontSize = 12.sp)
                            Text("Critical: 1.2%", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), fontSize = 12.sp)
                        }
                        LinearProgressIndicator(
                            progress = { 0.942f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF2E7D32),
                            trackColor = Color(0xFFE1BEE7)
                        )
                    }
                }

                // AI Generated Recommendations
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Smart System Recommendations (सिस्टम सुझाव)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        AiSuggestionItem(
                            number = 1,
                            title = "कपड़े की क्वालिटी एवं फैब्रिक",
                            detail = "Pure georgette fabric ratings are high. Keep using 100% natural soft lining on all girls gowns."
                        )
                        AiSuggestionItem(
                            number = 2,
                            title = "सही साइज चार्ट अपलोड करें",
                            detail = "Add exact bust (32-40 in) and length measurements in product images to eliminate 0.08% size returns."
                        )
                        AiSuggestionItem(
                            number = 3,
                            title = "प्राकृतिक रोशनी में फोटो खींचें",
                            detail = "Use calibrated 5500K daylight bulbs so colors in customer hands match the screen 100%."
                        )
                        AiSuggestionItem(
                            number = 4,
                            title = "ऑर्डर 24 घंटे के अंदर डिस्पैच करें",
                            detail = "You have 18.2 hr average dispatch. Continue to maintain under 24 hr for next day badge."
                        )
                        AiSuggestionItem(
                            number = 5,
                            title = "मजबूत 3-Ply पैकिंग इस्तेमाल करें",
                            detail = "Zero damage reported in last 200 orders due to standard Gyanvi barcoded envelopes."
                        )
                        AiSuggestionItem(
                            number = 6,
                            title = "गलत प्रोडक्ट भेजने से बचें",
                            detail = "Use barcode scan matching to maintain 0.01% error rate."
                        )
                    }
                }

                // Export buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "✓ Monthly Quality Report (PDF) Exported!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "✓ Excel Performance Data (.xlsx) Exported!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Excel Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // =========================================================================
    // MODAL: UPLOAD REVIEW VIDEO / PHOTO
    // =========================================================================
    if (showUploadMediaDialog) {
        var uploadTitle by remember { mutableStateOf("Girls Festival Lehenga Unboxing") }
        var uploadCustomer by remember { mutableStateOf("Meera Patwa") }
        var uploadCity by remember { mutableStateOf("Bettiah, Bihar") }
        var uploadComment by remember { mutableStateOf("Cloth quality is amazing! Beautiful stitching and pure silk shine.") }
        var uploadMediaType by remember { mutableIntStateOf(1) } // 0=Photo, 1=Video

        AlertDialog(
            onDismissRequest = { showUploadMediaDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MeeshoMagenta)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Review Video/Photo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add customer review video or quality inspection photo to the store.", fontSize = 11.sp, color = TextSecondary)

                    // Media Type Selector: Photo vs Video
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEEEEEE))
                            .padding(2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (uploadMediaType == 0) MeeshoMagenta else Color.Transparent,
                            modifier = Modifier.weight(1f).clickable { uploadMediaType = 0 }
                        ) {
                            Text("Customer Photo", textAlign = TextAlign.Center, color = if (uploadMediaType == 0) Color.White else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (uploadMediaType == 1) MeeshoMagenta else Color.Transparent,
                            modifier = Modifier.weight(1f).clickable { uploadMediaType = 1 }
                        ) {
                            Text("Video (चलने वाला वीडियो)", textAlign = TextAlign.Center, color = if (uploadMediaType == 1) Color.White else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp))
                        }
                    }

                    OutlinedTextField(
                        value = uploadCustomer,
                        onValueChange = { uploadCustomer = it },
                        label = { Text("Customer Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uploadTitle,
                        onValueChange = { uploadTitle = it },
                        label = { Text("Product / Video Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uploadComment,
                        onValueChange = { uploadComment = it },
                        label = { Text("Customer Review Comment") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ready to upload: High-Res 1080p Video attached (18s)", fontSize = 10.sp, color = Color(0xFF2E7D32))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        reviewsList.add(
                            0,
                            CustomerReviewItem(
                                id = System.currentTimeMillis().toString(),
                                customerName = uploadCustomer.ifBlank { "Verified Buyer" },
                                city = uploadCity,
                                rating = 5,
                                date = "Just now",
                                productName = uploadTitle.ifBlank { "Gyanvi Festive Ethnic Wear" },
                                comment = uploadComment.ifBlank { "Great quality and fabric!" },
                                photoRes = R.drawable.dress_anarkali_1793417013639,
                                hasVideo = uploadMediaType == 1,
                                videoDurationSec = 18
                            )
                        )
                        showUploadMediaDialog = false
                        selectedSectionTab = 2 // Switch to reviews tab
                        Toast.makeText(context, "✓ वीडियो/फोटो समीक्षा सफलतापूर्वक जोड़ी गई! (Review Video Added)", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Upload Now (अपलोड करें)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadMediaDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // =========================================================================
    // MODAL: SELLER REPLY DIALOG
    // =========================================================================
    showReplyDialogForReview?.let { rev ->
        AlertDialog(
            onDismissRequest = { showReplyDialogForReview = null },
            title = { Text("Reply as Gyanvi Dresses", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Replying to ${rev.customerName}'s review for '${rev.productName}'", fontSize = 11.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = sellerReplyText,
                        onValueChange = { sellerReplyText = it },
                        label = { Text("Your Response to Customer") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idx = reviewsList.indexOfFirst { it.id == rev.id }
                        if (idx != -1) {
                            reviewsList[idx] = rev.copy(sellerReply = sellerReplyText.trim())
                        }
                        showReplyDialogForReview = null
                        Toast.makeText(context, "✓ विक्रेता का जवाब पब्लिश हो गया! (Reply Posted)", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Post Reply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReplyDialogForReview = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // =========================================================================
    // MODAL: SELLER PERFORMANCE CERTIFICATE
    // =========================================================================
    if (showCertificateDialog) {
        AlertDialog(
            onDismissRequest = { showCertificateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFFFFB300))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quality Excellence Certificate", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFBE8), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("CERTIFICATE OF EXCELLENCE", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE65100))
                    Text("Presented to", fontSize = 10.sp, color = TextSecondary)
                    Text("GYANVI DRESSES", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Overall Quality Score: 4.8 / 5.0 ★\nRanked Top 5% in Indian Ethnic Fashion\nZero Defect Manufacturing Award", textAlign = TextAlign.Center, fontSize = 11.sp, color = TextPrimary, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Certified by Quality Control Board • 2026", fontSize = 9.sp, color = TextMuted)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "✓ Certificate Saved to Gallery as PDF/Image!", Toast.LENGTH_SHORT).show()
                        showCertificateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                ) {
                    Text("Download Certificate", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCertificateDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // =========================================================================
    // MODAL: REPORT DAMAGED PARCEL
    // =========================================================================
    if (showDamagedReportDialog) {
        var awbNumber by remember { mutableStateOf("AWB-849204-BLUEDART") }
        var damageType by remember { mutableStateOf("Outer flyer torn during transit") }

        AlertDialog(
            onDismissRequest = { showDamagedReportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color(0xFFD32F2F))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Report Damaged Parcel", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("File a damage claim with courier proof for 100% reimbursement.", fontSize = 11.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = awbNumber,
                        onValueChange = { awbNumber = it },
                        label = { Text("AWB / Tracking Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = damageType,
                        onValueChange = { damageType = it },
                        label = { Text("Damage Description") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "✓ Damaged parcel ticket #CLM-9402 created with 100% claim coverage!", Toast.LENGTH_LONG).show()
                        showDamagedReportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Submit Claim (दावा दर्ज करें)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDamagedReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// =============================================================================
// INTERACTIVE VIDEO PLAYER COMPONENT ("aur vedio chale bhi")
// Plays real animated customer review video frames with play/pause, time & seek!
// =============================================================================
@Composable
fun InteractiveVideoPlayerCard(
    review: CustomerReviewItem,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var currentSeconds by remember { mutableFloatStateOf(3f) }
    var isMuted by remember { mutableStateOf(false) }

    // Dynamic playback loop when isPlaying == true
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(500)
            currentSeconds += 0.5f
            if (currentSeconds >= review.videoDurationSec) {
                currentSeconds = 0f
            }
        }
    }

    // Infinite wave animation representing video movement
    val infiniteTransition = rememberInfiniteTransition(label = "videoPlay")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A24)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Player Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isPlaying) Color(0xFF00E676) else Color.Red))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlaying) "PLAYING VIDEO (चल रहा है)" else "PAUSED",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Mute",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Screen Canvas with Dress Thumbnail & Animated Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Background Product Preview
                review.photoRes?.let { pRes ->
                    Image(
                        painter = painterResource(id = pRes),
                        contentDescription = "Video Frame",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().alpha(0.6f)
                    )
                }

                // Dynamic Audio / Fabric Draping Motion Wave
                if (isPlaying) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path()
                        val width = size.width
                        val height = size.height
                        val midY = height * 0.75f

                        path.moveTo(0f, midY)
                        var x = 0f
                        while (x <= width) {
                            val angle = Math.toRadians((x + waveOffset).toDouble())
                            val y = midY + (kotlin.math.sin(angle) * 16f).toFloat()
                            path.lineTo(x, y)
                            x += 10f
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }

                // Center Tap-to-toggle Play/Pause Overlay
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.size(48.dp).clickable { isPlaying = !isPlaying }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Title Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column {
                        Text(review.productName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Customer Unboxing & Fabric Test • ${review.customerName}", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                    }
                }
            }

            // Seek Bar & Time Indicator
            val progressFrac = (currentSeconds / review.videoDurationSec.toFloat()).coerceIn(0f, 1f)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { progressFrac },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .clickable {
                            currentSeconds = 0f
                        },
                    color = Color(0xFFFF4081),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currInt = currentSeconds.toInt()
                    val totalInt = review.videoDurationSec
                    Text("00:${if (currInt < 10) "0$currInt" else "$currInt"}", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                    Text("00:${if (totalInt < 10) "0$totalInt" else "$totalInt"}", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                }
            }

            // Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { currentSeconds = (currentSeconds - 5f).coerceAtLeast(0f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Replay5, contentDescription = "Rewind 5s", tint = Color.White)
                    }
                    Button(
                        onClick = { isPlaying = !isPlaying },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) Color(0xFFFF4081) else Color(0xFF00E676)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPlaying) "Pause" else "Play", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = { currentSeconds = (currentSeconds + 5f).coerceAtMost(review.videoDurationSec.toFloat()) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Forward5, contentDescription = "Forward 5s", tint = Color.White)
                    }
                }

                TextButton(
                    onClick = {
                        Toast.makeText(context, "✓ Video saved to local gallery", Toast.LENGTH_SHORT).show()
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download Video", color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

// =============================================================================
// HELPER COMPONENTS
// =============================================================================
@Composable
private fun ScoreMiniItem(
    title: String,
    score: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF9F9F9),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, color = TextSecondary, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(2.dp))
            Text(score, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            Text(sub, fontSize = 9.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(sub, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
private fun BadgePill(
    title: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun QualityCheckChip(
    label: String,
    status: String
) {
    Column {
        Text(label, fontSize = 9.sp, color = TextSecondary)
        Text(status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
    }
}

@Composable
private fun AiSuggestionItem(
    number: Int,
    title: String,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(20.dp).clip(CircleShape).background(MeeshoMagenta.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("$number", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
            Text(detail, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun BarcodeVisualComponent(codeText: String) {
    Canvas(modifier = Modifier.width(130.dp).height(50.dp).background(Color.White)) {
        val totalBars = 35
        val barWidth = size.width / totalBars
        val pattern = listOf(2, 1, 3, 1, 2, 1, 1, 3, 2, 1, 2, 3, 1, 1, 2, 1, 3, 2, 1, 1, 2, 3, 1, 2, 1, 1, 3, 2, 1, 2, 1, 3, 1, 2, 1)

        var x = 0f
        pattern.forEachIndexed { i, p ->
            val isBlack = i % 2 == 0
            if (isBlack) {
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth * p.toFloat() * 0.7f, size.height)
                )
            }
            x += barWidth
        }
    }
}

@Composable
private fun QrCodeVisualComponent() {
    Canvas(modifier = Modifier.size(54.dp).background(Color.White).padding(4.dp)) {
        val s = size.width
        val cell = s / 7f

        // 3 Corner finder patterns
        drawRect(Color.Black, Offset(0f, 0f), Size(cell * 2.5f, cell * 2.5f))
        drawRect(Color.White, Offset(cell * 0.5f, cell * 0.5f), Size(cell * 1.5f, cell * 1.5f))
        drawRect(Color.Black, Offset(cell * 0.9f, cell * 0.9f), Size(cell * 0.7f, cell * 0.7f))

        drawRect(Color.Black, Offset(s - cell * 2.5f, 0f), Size(cell * 2.5f, cell * 2.5f))
        drawRect(Color.White, Offset(s - cell * 2f, cell * 0.5f), Size(cell * 1.5f, cell * 1.5f))
        drawRect(Color.Black, Offset(s - cell * 1.6f, cell * 0.9f), Size(cell * 0.7f, cell * 0.7f))

        drawRect(Color.Black, Offset(0f, s - cell * 2.5f), Size(cell * 2.5f, cell * 2.5f))
        drawRect(Color.White, Offset(cell * 0.5f, s - cell * 2f), Size(cell * 1.5f, cell * 1.5f))
        drawRect(Color.Black, Offset(cell * 0.9f, s - cell * 1.6f), Size(cell * 0.7f, cell * 0.7f))

        // Center randomized blocks
        drawRect(Color.Black, Offset(cell * 3.2f, cell * 3.2f), Size(cell * 1.2f, cell * 1.2f))
        drawRect(Color.Black, Offset(cell * 4.5f, cell * 2f), Size(cell * 0.8f, cell * 0.8f))
        drawRect(Color.Black, Offset(cell * 2f, cell * 4.5f), Size(cell * 0.8f, cell * 0.8f))
    }
}

@Composable
private fun WeeklyPerformanceChart() {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val scores = listOf(4.6f, 4.7f, 4.8f, 4.7f, 4.9f, 4.8f, 4.8f)
    val orders = listOf(140, 165, 210, 195, 260, 310, 280)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            orders.forEachIndexed { i, qty ->
                val barHeightFrac = (qty / 310f).coerceIn(0.1f, 1f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("${scores[i]}★", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height((barHeightFrac * 60).dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(MeeshoMagenta, Color(0xFFE91E63))
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(days[i], fontSize = 10.sp, color = TextSecondary)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Avg Daily: 223 Orders", fontSize = 10.sp, color = TextSecondary)
            Text("Highest: 310 Orders (Sat)", fontSize = 10.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
        }
    }
}
