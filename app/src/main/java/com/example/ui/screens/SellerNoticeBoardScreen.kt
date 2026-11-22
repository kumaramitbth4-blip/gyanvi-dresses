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
data class PlatformNotice(
    val id: String,
    val categoryTag: String,
    val tagColor: Color,
    val titleEn: String,
    val titleHi: String,
    val date: String,
    val messageEn: String,
    val messageHi: String,
    val actionTextEn: String?,
    val actionTextHi: String?,
    val targetAudience: String = "Sellers", // "All", "Sellers", "Buyers"
    var isPinned: Boolean = false,
    var isRead: Boolean = false,
    val readCount: Int = 1420
)

// =========================================================================
// MAIN FULL NOTICE BOARD SCREEN
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullSellerNoticeBoardScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    // Role Switch: 0 = User/Seller View, 1 = Admin Management View
    var activeRole by remember { mutableIntStateOf(0) }

    // Language Toggle: false = English, true = Hindi
    var isHindiLanguage by remember { mutableStateOf(false) }

    // Category Filter: "All", "Festive Sales", "Payouts", "Shipping & Packaging", "Policies", "Buyers"
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    // Search query
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    // Dialog States
    var showNoticeDetailDialog by remember { mutableStateOf(false) }
    var selectedNoticeForDetail by remember { mutableStateOf<PlatformNotice?>(null) }

    var showCreateNoticeDialog by remember { mutableStateOf(false) }
    var newNoticeTitle by remember { mutableStateOf("") }
    var newNoticeCategory by remember { mutableStateOf("Festival Sale") }
    var newNoticeAudience by remember { mutableStateOf("Sellers") }
    var newNoticeMessage by remember { mutableStateOf("") }
    var newNoticeActionText by remember { mutableStateOf("View Details") }
    var newNoticeIsPinned by remember { mutableStateOf(false) }

    // Complete Initial Notice List (Matching Screenshot 10:32)
    val noticesList = remember {
        mutableStateListOf(
            PlatformNotice(
                id = "NOT-101",
                categoryTag = "FESTIVE SALE",
                tagColor = Color(0xFFE65100),
                titleEn = "Diwali & Chhath Mega Festive Dhamaka Announced!",
                titleHi = "दिवाली और छठ महापर्व सेल की घोषणा!",
                date = "Today, 10:30 AM",
                messageEn = "Enroll your Kurtis and Sarees now to qualify for 10% platform subsidy and priority shipping banner placement.",
                messageHi = "अपनी कुर्तियों और साड़ियों को अभी एनरोल करें और 10% प्लेटफ़ॉर्म सब्सिडी तथा मुख्य होमस्क्रीन बैनर का लाभ उठाएं।",
                actionTextEn = "Opt-in Products",
                actionTextHi = "प्रोडक्ट्स जोड़ें",
                targetAudience = "Sellers",
                isPinned = true,
                isRead = false,
                readCount = 1840
            ),
            PlatformNotice(
                id = "NOT-102",
                categoryTag = "PAYOUT NOTICE",
                tagColor = Color(0xFF2E7D32),
                titleEn = "Weekly Payout Scheduled Early due to Bank Holiday",
                titleHi = "बैंक अवकाश के कारण साप्ताहिक भुगतान पहले ट्रांसफर होगा",
                date = "Yesterday, 4:15 PM",
                messageEn = "Payout for Nov 3 - Nov 9 will be credited to your verified bank account by Thursday morning instead of Friday.",
                messageHi = "3 नवंबर से 9 नवंबर तक का पेआउट शुक्रवार की जगह गुरुवार सुबह 11:00 बजे तक आपके बैंक खाते में जमा कर दिया जाएगा।",
                actionTextEn = "View Breakdown",
                actionTextHi = "पेआउट विवरण देखें",
                targetAudience = "Sellers",
                isPinned = true,
                isRead = false,
                readCount = 1520
            ),
            PlatformNotice(
                id = "NOT-103",
                categoryTag = "PACKAGING POLICY",
                tagColor = Color(0xFF0288D1),
                titleEn = "100% RTO Protection on Barcoded Packets",
                titleHi = "बारकोड वाले पैकेट पर 100% RTO सुरक्षा नियम",
                date = "Nov 06, 2026",
                messageEn = "Ensure barcoded bags are scanned before handing over to delivery partners to qualify for zero-deduction claims.",
                messageHi = "कूरियर पार्टनर को सौंपने से पहले बारकोड पैकेट को स्कैन करना अनिवार्य है ताकि गलत रिटर्न पर 100% क्लेम मिल सके।",
                actionTextEn = "Order Packets",
                actionTextHi = "पैकेट ऑर्डर करें",
                targetAudience = "Sellers",
                isPinned = false,
                isRead = true,
                readCount = 1380
            ),
            PlatformNotice(
                id = "NOT-104",
                categoryTag = "TAX & COMPLIANCE",
                tagColor = Color(0xFF7B1FA2),
                titleEn = "GST E-Invoicing & Monthly TCS Statement",
                titleHi = "GST ई-इनवॉइसिंग और मासिक TCS रिपोर्ट तैयार",
                date = "Nov 01, 2026",
                messageEn = "Your GSTIN 10ABCDE1234F1Z5 is verified. October 2026 GSTR-1 and TCS reports are generated and ready for CA filing.",
                messageHi = "अक्टूबर माह का GSTR-1 और TCS विवरण तैयार है। इसे आप सीधे डाउनलोड करके अपने CA को भेज सकते हैं।",
                actionTextEn = "Download Summary",
                actionTextHi = "रिपोर्ट डाउनलोड करें",
                targetAudience = "Sellers",
                isPinned = false,
                isRead = true,
                readCount = 980
            ),
            PlatformNotice(
                id = "NOT-105",
                categoryTag = "NEW UPDATES",
                tagColor = Color(0xFF1565C0),
                titleEn = "New AI Price Recommendation Engine Live",
                titleHi = "नया AI स्मार्ट प्राइस सुझाव सिस्टम लाइव",
                date = "Oct 28, 2026",
                messageEn = "Compare competitor prices in real-time and apply recommended prices to unlock up to 2.4x more orders.",
                messageHi = "कॉम्पिटिटर की कीमतों का रीयल-टाइम डेटा देखें और 1-क्लिक में रिकमेंडेड प्राइस लगाकर ऑर्डर्स 2.4 गुना बढ़ाएं।",
                actionTextEn = "Explore Feature",
                actionTextHi = "फीचर देखें",
                targetAudience = "Sellers",
                isPinned = false,
                isRead = true,
                readCount = 1240
            ),
            PlatformNotice(
                id = "NOT-106",
                categoryTag = "SHIPPING UPDATES",
                tagColor = Color(0xFF00796B),
                titleEn = "New Express Courier Hub in Bettiah, Bihar",
                titleHi = "बेतिया (बिहार) में नया एक्सप्रेस कूरियर हब शुरू",
                date = "Oct 25, 2026",
                messageEn = "Delhivery Express Surface & Air Cargo hub active. Same-day pickup for orders packed before 2:00 PM.",
                messageHi = "बेतिया में नया एयर कार्गो हब चालू हो गया है। दोपहर 2 बजे से पहले पैक हुए ऑर्डर्स का उसी दिन पिकअप होगा।",
                actionTextEn = "View Hub Timings",
                actionTextHi = "समय सारणी देखें",
                targetAudience = "All",
                isPinned = false,
                isRead = true,
                readCount = 1680
            ),
            PlatformNotice(
                id = "NOT-107",
                categoryTag = "BUYER ANNOUNCEMENT",
                tagColor = Color(0xFFD81B60),
                titleEn = "Free Delivery on All Prepaid Orders Above ₹499",
                titleHi = "₹499 से ऊपर के सभी प्रीपेड ऑर्डर्स पर मुफ्त डिलीवरी",
                date = "Oct 20, 2026",
                messageEn = "Shoppers paying via UPI or Card enjoy zero delivery charges across all sarees and dress materials.",
                messageHi = "UPI या कार्ड से ऑनलाइन पेमेंट करने वाले सभी खरीदारों को ₹499 से ऊपर फ्री डिलीवरी दी जा रही है।",
                actionTextEn = "Shop Collection",
                actionTextHi = "कलेक्शन देखें",
                targetAudience = "Buyers",
                isPinned = false,
                isRead = true,
                readCount = 3450
            )
        )
    }

    // Filtered notices
    val filteredNotices = remember(selectedCategoryFilter, searchQuery, isHindiLanguage, activeRole) {
        noticesList.filter { notice ->
            val matchesCategory = when (selectedCategoryFilter) {
                "All" -> true
                "Festive Sales" -> notice.categoryTag.contains("FESTIVE") || notice.categoryTag.contains("SALE")
                "Payouts" -> notice.categoryTag.contains("PAYOUT") || notice.categoryTag.contains("TAX")
                "Shipping & Packaging" -> notice.categoryTag.contains("PACKAGING") || notice.categoryTag.contains("SHIPPING")
                "Policies" -> notice.categoryTag.contains("POLICY") || notice.categoryTag.contains("COMPLIANCE")
                "Buyers" -> notice.targetAudience == "Buyers" || notice.targetAudience == "All"
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                notice.titleEn.contains(searchQuery, ignoreCase = true) ||
                        notice.titleHi.contains(searchQuery, ignoreCase = true) ||
                        notice.messageEn.contains(searchQuery, ignoreCase = true) ||
                        notice.categoryTag.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }.sortedByDescending { it.isPinned }
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
                        IconButton(onClick = onBack, modifier = Modifier.testTag("notice_board_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isHindiLanguage) "सूचना पट्ट (Notice Board)" else "Notice Board",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = TextPrimary
                            )
                            Text(
                                if (isHindiLanguage) "प्लेटफ़ॉर्म घोषणाएं, सेल व आवश्यक सूचनाएं" else "Platform announcements, sale events & alerts",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        // Search icon
                        IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                        }

                        // Language Toggle Pill (EN / HI)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFEDE7F6))
                                .clickable { isHindiLanguage = !isHindiLanguage }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (isHindiLanguage) "हिन्दी" else "EN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MeeshoMagenta
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Double checkmark icon: Mark All As Read (Matching screenshot 10:32)
                        IconButton(
                            onClick = {
                                noticesList.forEachIndexed { idx, itm ->
                                    noticesList[idx] = itm.copy(isRead = true)
                                }
                                Toast.makeText(context, if (isHindiLanguage) "सभी सूचनाएं पढ़ी हुई मार्क की गईं" else "All notices marked as read", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("mark_all_read_btn")
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Mark Read", tint = MeeshoMagenta)
                        }

                        // Admin Mode Switcher Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (activeRole == 0) Color(0xFFF3E5F5) else Color(0xFFFFEBEE))
                                .clickable {
                                    activeRole = if (activeRole == 0) 1 else 0
                                    Toast.makeText(
                                        context,
                                        if (activeRole == 1) "Switched to Admin Notice Control" else "Switched to User Notice View",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (activeRole == 0) "User" else "Admin",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeRole == 0) MeeshoMagenta else Color(0xFFD32F2F)
                            )
                        }
                    }

                    // Search Bar Dropdown
                    AnimatedVisibility(visible = isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(if (isHindiLanguage) "नोटिस खोजें (उदा. दिवाली, पेआउट)..." else "Search notices (e.g. Diwali, Payout, RTO)...", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            singleLine = true,
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        )
                    }

                    // Category Filter Scrollable Chips
                    ScrollableTabRow(
                        selectedTabIndex = when (selectedCategoryFilter) {
                            "All" -> 0
                            "Festive Sales" -> 1
                            "Payouts" -> 2
                            "Shipping & Packaging" -> 3
                            "Policies" -> 4
                            "Buyers" -> 5
                            else -> 0
                        },
                        edgePadding = 12.dp,
                        containerColor = CardWhite,
                        contentColor = MeeshoMagenta,
                        indicator = { tabPositions ->
                            val curIdx = when (selectedCategoryFilter) {
                                "All" -> 0
                                "Festive Sales" -> 1
                                "Payouts" -> 2
                                "Shipping & Packaging" -> 3
                                "Policies" -> 4
                                "Buyers" -> 5
                                else -> 0
                            }
                            if (curIdx < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[curIdx]),
                                    color = MeeshoMagenta
                                )
                            }
                        }
                    ) {
                        listOf(
                            "All" to "सभी (${noticesList.size})",
                            "Festive Sales" to "त्योहार सेल",
                            "Payouts" to "पेमेंट व टैक्स",
                            "Shipping & Packaging" to "शिपिंग व पैकिंग",
                            "Policies" to "नियम व RTO",
                            "Buyers" to "क्रेता सूचनाएं"
                        ).forEach { (catKey, catLabelHi) ->
                            Tab(
                                selected = selectedCategoryFilter == catKey,
                                onClick = { selectedCategoryFilter = catKey },
                                text = {
                                    Text(
                                        if (isHindiLanguage) catLabelHi else catKey,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedCategoryFilter == catKey) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (activeRole == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateNoticeDialog = true },
                    containerColor = MeeshoMagenta,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Publish Notice", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_create_notice_fab")
                )
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Admin Action Banner
            if (activeRole == 1) {
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Admin Publishing Mode Active", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFD32F2F))
                                Text("You can create, pin, edit, delete or push broadcast notices to all Sellers & Buyers.", fontSize = 11.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            // Notice Cards (Exact match to Screenshot 10:32)
            items(filteredNotices, key = { it.id }) { notice ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val idx = noticesList.indexOfFirst { it.id == notice.id }
                            if (idx != -1) {
                                noticesList[idx] = notice.copy(isRead = true)
                            }
                            selectedNoticeForDetail = notice
                            showNoticeDetailDialog = true
                        }
                        .testTag("notice_item_${notice.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Tag Pill & Date Row (Matching Screenshot 10:32)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(notice.tagColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        notice.categoryTag,
                                        color = notice.tagColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                if (notice.isPinned) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = Color(0xFFE65100), modifier = Modifier.size(14.dp))
                                }

                                if (!notice.isRead) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MeeshoMagenta))
                                }
                            }

                            Text(notice.date, fontSize = 11.sp, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Title
                        Text(
                            if (isHindiLanguage) notice.titleHi else notice.titleEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Message body
                        Text(
                            if (isHindiLanguage) notice.messageHi else notice.messageEn,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bottom Action Row (Matching screenshot: Opt-in Products ->, View Breakdown ->)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Audience: ${notice.targetAudience} • Read by ${notice.readCount}",
                                fontSize = 10.sp,
                                color = TextMuted
                            )

                            val actionText = if (isHindiLanguage) notice.actionTextHi else notice.actionTextEn
                            if (actionText != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        Toast.makeText(context, "Opening: $actionText", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text(
                                        actionText,
                                        color = MeeshoMagenta,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        // Admin controls on card (Pin & Delete)
                        if (activeRole == 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = DividerColor)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        val idx = noticesList.indexOfFirst { it.id == notice.id }
                                        if (idx != -1) {
                                            noticesList[idx] = notice.copy(isPinned = !notice.isPinned)
                                            Toast.makeText(context, if (notice.isPinned) "Unpinned" else "Pinned to top", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (notice.isPinned) "Unpin" else "Pin to Top", fontSize = 11.sp)
                                }

                                TextButton(
                                    onClick = {
                                        noticesList.remove(notice)
                                        Toast.makeText(context, "Notice deleted", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete", color = Color(0xFFD32F2F), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    // =========================================================================
    // NOTICE DETAIL DIALOG
    // =========================================================================
    if (showNoticeDetailDialog && selectedNoticeForDetail != null) {
        val notice = selectedNoticeForDetail!!
        Dialog(onDismissRequest = { showNoticeDetailDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(notice.tagColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(notice.categoryTag, color = notice.tagColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showNoticeDetailDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        if (isHindiLanguage) notice.titleHi else notice.titleEn,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text("Published: ${notice.date} • For: ${notice.targetAudience}", fontSize = 11.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notice Banner Image Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(notice.tagColor.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = notice.tagColor, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("GYANVI DRESSES OFFICIAL NOTIFICATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = notice.tagColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        if (isHindiLanguage) notice.messageHi else notice.messageEn,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Button
                    val actionText = if (isHindiLanguage) notice.actionTextHi else notice.actionTextEn
                    if (actionText != null) {
                        Button(
                            onClick = {
                                showNoticeDetailDialog = false
                                Toast.makeText(context, "Action: $actionText executed successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text(actionText, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Notification link shared via WhatsApp / SMS", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isHindiLanguage) "शेयर करें (WhatsApp)" else "Share Notice", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // =========================================================================
    // ADMIN: CREATE NOTICE DIALOG
    // =========================================================================
    if (showCreateNoticeDialog) {
        Dialog(onDismissRequest = { showCreateNoticeDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Publish New Announcement", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text("Broadcast updates to Sellers, Buyers or All Platform Users", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Notice Title", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    OutlinedTextField(
                        value = newNoticeTitle,
                        onValueChange = { newNoticeTitle = it },
                        placeholder = { Text("e.g. Chhath Puja Mega Payout Early Release", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Notice Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Festive Sale", "Payout", "Packaging", "General").forEach { cat ->
                            FilterChip(
                                selected = newNoticeCategory == cat,
                                onClick = { newNoticeCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Target Audience", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("All", "Sellers", "Buyers").forEach { aud ->
                            FilterChip(
                                selected = newNoticeAudience == aud,
                                onClick = { newNoticeAudience = aud },
                                label = { Text(aud, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Detailed Message", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    OutlinedTextField(
                        value = newNoticeMessage,
                        onValueChange = { newNoticeMessage = it },
                        placeholder = { Text("Write full announcement details...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Action Button Text", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    OutlinedTextField(
                        value = newNoticeActionText,
                        onValueChange = { newNoticeActionText = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = newNoticeIsPinned, onCheckedChange = { newNoticeIsPinned = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pin to Top (Mark as High Importance)", fontSize = 12.sp, color = TextPrimary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCreateNoticeDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newNoticeTitle.isBlank()) {
                                    Toast.makeText(context, "Please enter notice title", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                noticesList.add(
                                    0,
                                    PlatformNotice(
                                        id = "NOT-${(1000..9999).random()}",
                                        categoryTag = newNoticeCategory.uppercase(),
                                        tagColor = when (newNoticeCategory) {
                                            "Festive Sale" -> Color(0xFFE65100)
                                            "Payout" -> Color(0xFF2E7D32)
                                            "Packaging" -> Color(0xFF0288D1)
                                            else -> Color(0xFF7B1FA2)
                                        },
                                        titleEn = newNoticeTitle,
                                        titleHi = newNoticeTitle,
                                        date = "Just now",
                                        messageEn = newNoticeMessage.ifBlank { "Important operational announcement from Gyanvi Dresses Management." },
                                        messageHi = newNoticeMessage.ifBlank { "ज्ञानवी ड्रेसेज प्रबंधन की ओर से महत्वपूर्ण सूचना।" },
                                        actionTextEn = newNoticeActionText.ifBlank { "View" },
                                        actionTextHi = "देखें",
                                        targetAudience = newNoticeAudience,
                                        isPinned = newNoticeIsPinned,
                                        isRead = false,
                                        readCount = 0
                                    )
                                )
                                showCreateNoticeDialog = false
                                Toast.makeText(context, "Notice published and broadcasted to $newNoticeAudience!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                        ) {
                            Text("Publish Now")
                        }
                    }
                }
            }
        }
    }
}
