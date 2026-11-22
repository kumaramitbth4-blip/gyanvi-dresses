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
import androidx.compose.material.icons.automirrored.filled.Send
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
// DATA MODELS FOR SUPPORT & HELPDESK
// =========================================================================
data class SupportTicket(
    val ticketId: String,
    val sellerId: String = "SLR-AMIT-9821",
    val orderId: String = "ODR-98213",
    val productSku: String = "GWN-ANK-101",
    val category: String,
    val subjectEn: String,
    val subjectHi: String,
    val description: String,
    val status: TicketStatus,
    val assignedAgent: String,
    val createdDate: String,
    val lastUpdated: String,
    val priority: String = "Medium",
    var threadReplies: List<TicketReply> = emptyList()
)

data class TicketReply(
    val senderName: String,
    val isAgent: Boolean,
    val timestamp: String,
    val message: String
)

enum class TicketStatus(val labelEn: String, val labelHi: String, val color: Color, val bgColor: Color) {
    NEW("New", "नया", Color(0xFF1565C0), Color(0xFFE3F2FD)),
    IN_PROGRESS("In Progress", "प्रगति पर", Color(0xFFE65100), Color(0xFFFFF3E0)),
    UNDER_REVIEW("Under Review", "जांच में", Color(0xFF7B1FA2), Color(0xFFF3E5F5)),
    RESOLVED("Resolved", "हल किया गया", Color(0xFF2E7D32), Color(0xFFE8F5E9)),
    REJECTED("Rejected", "अस्वीकृत", Color(0xFFC62828), Color(0xFFFFEBEE)),
    CLOSED("Closed", "बंद", Color(0xFF757575), Color(0xFFEEEEEE))
}

data class ChatMessage(
    val id: String,
    val sender: String,
    val isMe: Boolean,
    val text: String,
    val time: String
)

// =========================================================================
// MAIN FULL SELLER SUPPORT SCREEN
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullSellerSupportHelpdeskScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    // Audience / Dashboard Mode:
    // 0 = Seller Support (Default), 1 = Buyer Support, 2 = Admin Helpdesk
    var supportModeTab by remember { mutableIntStateOf(0) }

    // Language Toggle: false = English, true = Hindi
    var isHindiLanguage by remember { mutableStateOf(false) }

    // Callback Request State
    var isCallbackRequested by remember { mutableStateOf(false) }
    var showCallbackModal by remember { mutableStateOf(false) }
    var callbackPhoneInput by remember { mutableStateOf("+91 9128754982") }
    var callbackSlot by remember { mutableStateOf("Immediate (within 15 mins)") }

    // Dialog States
    var showRaiseTicketDialog by remember { mutableStateOf(false) }
    var newTicketCategory by remember { mutableStateOf("Payment & Payout") }
    var newTicketOrderId by remember { mutableStateOf("ODR-98213") }
    var newTicketSku by remember { mutableStateOf("GWN-ANK-101") }
    var newTicketSubject by remember { mutableStateOf("") }
    var newTicketDescription by remember { mutableStateOf("") }
    var newTicketPriority by remember { mutableStateOf("Medium") }
    var attachedFileSimulated by remember { mutableStateOf(false) }

    // Live Chat State
    var showLiveChatModal by remember { mutableStateOf(false) }
    var chatInputText by remember { mutableStateOf("") }
    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage("1", "Priya Verma (Partner Support)", false, "Hello Amit ji! Welcome to Gyanvi Dresses Partner Support. How can I assist you with your store today?", "10:55 AM"),
            ChatMessage("2", "Amit kumar patwa (Seller)", true, "Namaste Priya ji, mujhe weekly payout timing aur return barcode verification ke bare me jankari chahiye thi.", "10:56 AM"),
            ChatMessage("3", "Priya Verma (Partner Support)", false, "Sure Amit ji! Your upcoming payout for Nov 3-9 will be credited on Thursday morning by 11:00 AM due to the bank holiday. All your parcels dispatched in Barcoded packets are 100% protected against transit claims.", "10:57 AM")
        )
    }

    // Ticket Details Modal
    var showTicketDetailDialog by remember { mutableStateOf(false) }
    var selectedTicketForDetail by remember { mutableStateOf<SupportTicket?>(null) }
    var ticketReplyInput by remember { mutableStateOf("") }

    // Tickets List (Matching Screenshot 5:33: TKT-8921 In Progress, TKT-8410 Resolved)
    val ticketsList = remember {
        mutableStateListOf(
            SupportTicket(
                ticketId = "TKT-8921",
                sellerId = "SLR-AMIT-9821",
                orderId = "ODR-98213",
                productSku = "GWN-ANK-101",
                category = "Weight & Shipping",
                subjectEn = "Weight discrepancy on Kurti order #98213",
                subjectHi = "कुर्ती ऑर्डर #98213 पर वजन में विसंगति",
                description = "Courier partner charged 1.2 kg weight slab for an 380g Anarkali Kurti packet. Please adjust the shipping fee deduction.",
                status = TicketStatus.IN_PROGRESS,
                assignedAgent = "Rahul Sharma (Logistics Lead)",
                createdDate = "Nov 08, 2026",
                lastUpdated = "Today, 10:15 AM",
                priority = "High",
                threadReplies = listOf(
                    TicketReply("Rahul Sharma (Agent)", true, "Nov 08, 2:30 PM", "We have received your weight dispute with courier Delhivery. Digital weighing scale video requested."),
                    TicketReply("Amit kumar patwa (Seller)", false, "Nov 08, 3:15 PM", "Attached dispatch packaging video showing 380g weight on scale."),
                    TicketReply("Rahul Sharma (Agent)", true, "Today, 10:15 AM", "Weight video verified successfully. Adjustment of ₹85 will be credited to next Friday payout.")
                )
            ),
            SupportTicket(
                ticketId = "TKT-8410",
                sellerId = "SLR-AMIT-9821",
                orderId = "ODR-88120",
                productSku = "GWN-BNS-202",
                category = "Packaging & Barcode",
                subjectEn = "Barcode packets order shipment status",
                subjectHi = "बारकोड पैकेट ऑर्डर शिपमेंट स्थिति",
                description = "Purchased 500 units of 10x12 inch barcoded packets from Parin Polymers. Inquiring on courier tracking.",
                status = TicketStatus.RESOLVED,
                assignedAgent = "Sunita Rao (Supply Chain)",
                createdDate = "Nov 04, 2026",
                lastUpdated = "Nov 06, 2026",
                priority = "Medium",
                threadReplies = listOf(
                    TicketReply("Sunita Rao (Agent)", true, "Nov 05, 11:00 AM", "Parin Polymers dispatched via BlueDart AWB-98410."),
                    TicketReply("Sunita Rao (Agent)", true, "Nov 06, 4:00 PM", "Packets delivered to Bettiah warehouse. Compensation claim eligibility active.")
                )
            ),
            SupportTicket(
                ticketId = "TKT-9102",
                sellerId = "SLR-AMIT-9821",
                orderId = "ODR-99411",
                productSku = "GWN-SHR-404",
                category = "Return & RTO",
                subjectEn = "Wrong return item received (torn dupatta)",
                subjectHi = "गलत रिटर्न प्राप्त हुआ (फटा हुआ दुपट्टा)",
                description = "Customer returned old cotton dupatta instead of embroidered Sharara set. Unboxing video and barcoded parcel intact.",
                status = TicketStatus.UNDER_REVIEW,
                assignedAgent = "Vikas Mehra (RTO Claims Team)",
                createdDate = "Nov 09, 2026",
                lastUpdated = "Today, 9:30 AM",
                priority = "Urgent",
                threadReplies = listOf(
                    TicketReply("Vikas Mehra (Agent)", true, "Today, 9:30 AM", "Claim under 100% Barcode Protection Program. Full product cost ₹899 compensation initiated.")
                )
            )
        )
    }

    // FAQs List (Matching Screenshot 5:33)
    val faqList = remember {
        listOf(
            Triple(
                "When will my payment be transferred to bank?",
                "मेरा पेमेंट बैंक में कब ट्रांसफर होगा?",
                "Payouts are transferred directly to your verified HDFC bank account every Friday morning for all orders that have successfully crossed the 7-day return window. You receive instant SMS and WhatsApp confirmation."
            ),
            Triple(
                "What if customer returned a damaged/different item?",
                "ग्राहक ने खराब या अलग सामान वापस किया तो क्या करें?",
                "Go to Seller Hub > Returns > Raise Claim within 72 hours of parcel receipt. Since you pack orders in authorized Barcoded Packets, your 100% compensation claim is approved automatically within 48 hours without deductions."
            ),
            Triple(
                "How to change my warehouse pickup address?",
                "वेयरहाउस पिकअप का पता कैसे बदलें?",
                "Navigate to Seller Hub > Menu > Warehouse & Dispatch Settings. You can update your contact person name, mobile number (+91 9128754982), building address and pin code (845438)."
            ),
            Triple(
                "How can I participate in Gyanvi Dresses Festive Mega Sales?",
                "फेस्टिव महासेल में कैसे भाग लें?",
                "Go to Seller Hub > Price Recommendation or Notice Board to enroll your catalog items with recommended festive discounts. You will receive 10% platform subsidy and top banner visibility."
            ),
            Triple(
                "Why was my order courier pickup delayed?",
                "कूरियर पिकअप में देरी क्यों हुई?",
                "Orders packed before 2:00 PM are picked up on the same day. If your area has heavy rainfall or transit restrictions, courier pickup automatically reschedules for next morning 10:00 AM."
            ),
            Triple(
                "How to download monthly GST invoice and TCS reports?",
                "मासिक GST इनवॉइस और TCS रिपोर्ट कैसे डाउनलोड करें?",
                "Go to Seller Hub > Business Dashboard > Export Report or Notice Board to download monthly GSTR-1, TCS summary and platform zero-commission invoices in Excel or PDF."
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
                        IconButton(onClick = onBack, modifier = Modifier.testTag("seller_support_back_btn")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isHindiLanguage) "सेलर सहायता केंद्र" else "Seller Support",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = TextPrimary
                            )
                            Text(
                                if (isHindiLanguage) "24/7 सेलर पार्टनर हेल्पडेस्क" else "Dedicated 24/7 seller partner helpdesk",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        // Language Switcher (EN ⟷ हिन्दी)
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

                        // Role & Audience Selector Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    when (supportModeTab) {
                                        0 -> Color(0xFFF3E5F5)
                                        1 -> Color(0xFFE8F5E9)
                                        else -> Color(0xFFFFEBEE)
                                    }
                                )
                                .clickable {
                                    supportModeTab = (supportModeTab + 1) % 3
                                    val modeName = when (supportModeTab) {
                                        0 -> "Seller Helpdesk"
                                        1 -> "Buyer Support Center"
                                        else -> "Admin Support Panel"
                                    }
                                    Toast.makeText(context, "Switched to $modeName", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                when (supportModeTab) {
                                    0 -> "Seller"
                                    1 -> "Buyer"
                                    else -> "Admin"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (supportModeTab) {
                                    0 -> MeeshoMagenta
                                    1 -> Color(0xFF2E7D32)
                                    else -> Color(0xFFD32F2F)
                                }
                            )
                        }
                    }

                    // Sub-Tabs
                    ScrollableTabRow(
                        selectedTabIndex = supportModeTab,
                        edgePadding = 12.dp,
                        containerColor = CardWhite,
                        contentColor = MeeshoMagenta,
                        indicator = { tabPositions ->
                            if (supportModeTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[supportModeTab]),
                                    color = MeeshoMagenta
                                )
                            }
                        }
                    ) {
                        Tab(
                            selected = supportModeTab == 0,
                            onClick = { supportModeTab = 0 },
                            text = { Text("Seller Support", fontSize = 13.sp, fontWeight = if (supportModeTab == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = supportModeTab == 1,
                            onClick = { supportModeTab = 1 },
                            text = { Text("Buyer Help Center", fontSize = 13.sp, fontWeight = if (supportModeTab == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = supportModeTab == 2,
                            onClick = { supportModeTab = 2 },
                            text = { Text("Admin Tickets Panel", fontSize = 13.sp, fontWeight = if (supportModeTab == 2) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (supportModeTab) {
                0 -> {
                    // TAB 0: SELLER SUPPORT (Exact replica of Screenshot 5:33)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Instant Resolution Header (Screenshot 5:33)
                        Text(
                            if (isHindiLanguage) "तुरंत सहायता (Instant Resolution)" else "Instant Resolution",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // 3 Primary Action Cards: Call Me Back, WhatsApp Chat, Raise Ticket (Matching Screenshot 5:33)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // 1. Call Me Back (Purple/Pink Tint)
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showCallbackModal = true }
                                    .testTag("action_call_me_back"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF7FA)),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF8BBD0).copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFFC2185B), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Call Me Back", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF880E4F))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Within 15 mins", fontSize = 10.sp, color = TextSecondary)
                                }
                            }

                            // 2. WhatsApp Chat (Light Green Tint)
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        Toast.makeText(context, "Opening WhatsApp Seller Helpline (+91 9128754982)...", Toast.LENGTH_SHORT).show()
                                    }
                                    .testTag("action_whatsapp_chat"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F5)),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7).copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("WhatsApp\nChat", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1B5E20), textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Quick text help", fontSize = 10.sp, color = TextSecondary)
                                }
                            }

                            // 3. Raise Ticket (Warm Orange/Amber Tint)
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showRaiseTicketDialog = true }
                                    .testTag("action_raise_ticket"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF2)),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE082).copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Raise Ticket", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFBF360C))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Track issue", fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                        }

                        // Secondary Quick Channels: Live Chat with Priya & Email Support
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showLiveChatModal = true },
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Forum, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Live In-App Chat", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextPrimary)
                                        Text("Agent Online Now", fontSize = 9.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        Toast.makeText(context, "Support Email: partner-support@gyanvidresses.com", Toast.LENGTH_LONG).show()
                                    },
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Email Support", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextPrimary)
                                        Text("partner-support@", fontSize = 9.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }

                        // Callback active banner
                        if (isCallbackRequested) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Callback Request Active", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                        Text("Our senior partner manager will call on $callbackPhoneInput ($callbackSlot).", fontSize = 11.sp, color = TextPrimary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Recent Support Tickets Section (Matching Screenshot 5:33)
                        Text(
                            if (isHindiLanguage) "हालिया शिकायतें (Recent Support Tickets)" else "Recent Support Tickets",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Ticket Items: TKT-8921 In Progress, TKT-8410 Resolved (Matching Screenshot 5:33)
                        ticketsList.forEach { ticket ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clickable {
                                        selectedTicketForDetail = ticket
                                        showTicketDetailDialog = true
                                    }
                                    .testTag("ticket_${ticket.ticketId}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(ticket.ticketId, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF880E4F))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                if (isHindiLanguage) ticket.status.labelHi else ticket.status.labelEn,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ticket.status.color
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            if (isHindiLanguage) ticket.subjectHi else ticket.subjectEn,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "${ticket.status.labelEn} • Agent: ${ticket.assignedAgent}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(26.dp))

                        // Top Seller FAQs Section (Matching Screenshot 5:33)
                        Text(
                            if (isHindiLanguage) "अक्सर पूछे जाने वाले सवाल (Top Seller FAQs)" else "Top Seller FAQs",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        faqList.forEach { (qEn, qHi, answer) ->
                            var expanded by remember { mutableStateOf(false) }
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expanded = !expanded }
                                        .padding(14.dp)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            if (isHindiLanguage) qHi else qEn,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = TextPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = TextSecondary
                                        )
                                    }
                                    AnimatedVisibility(visible = expanded) {
                                        Column(modifier = Modifier.padding(top = 10.dp)) {
                                            HorizontalDivider(color = DividerColor)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(answer, fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }

                1 -> {
                    // TAB 1: BUYER SUPPORT CENTER (Customer Care)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("Buyer Support Center (Customer Care)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Customer order status, returns, sizing and helpline", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        listOf(
                            Triple("Track My Dress Order", "Live courier transit tracking & delivery estimate", Icons.Default.LocalShipping),
                            Triple("Return / Exchange Request", "Initiate 7-day hassle-free return or size change", Icons.Default.Autorenew),
                            Triple("Refund Status to UPI / Bank", "Track instant bank refund after returned parcel pickup", Icons.Default.AccountBalance),
                            Triple("Size & Fit Measuring Guide", "Exact chest, waist and length guide for Kurtis & Sarees", Icons.Default.Straighten),
                            Triple("Call Customer Care Helpline", "Toll-free customer support line +91 9128754982", Icons.Default.HeadsetMic)
                        ).forEach { (title, subtitle, icon) ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        Toast.makeText(context, "Opening Buyer Service: $title", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFFCE4EC)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                        Text(subtitle, fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                2 -> {
                    // TAB 2: ADMIN TICKETS & AGENTS PANEL
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("Admin Support Command Center", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text("Manage support tickets, assign agents and resolve seller complaints", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Open", fontSize = 11.sp, color = TextSecondary)
                                    Text("3 Tickets", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                                }
                            }
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("In Progress", fontSize = 11.sp, color = TextSecondary)
                                    Text("2 Tickets", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                }
                            }
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Resolved", fontSize = 11.sp, color = TextSecondary)
                                    Text("18 Tickets", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Actionable Seller Tickets", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        ticketsList.forEachIndexed { idx, ticket ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${ticket.ticketId} • ${ticket.category}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                        Text(ticket.status.labelEn, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ticket.status.color)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(ticket.subjectEn, fontSize = 12.sp, color = TextPrimary)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                        TextButton(onClick = {
                                            ticketsList[idx] = ticket.copy(status = TicketStatus.RESOLVED)
                                            Toast.makeText(context, "${ticket.ticketId} marked as Resolved!", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Text("Mark Resolved", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Button(
                                            onClick = {
                                                selectedTicketForDetail = ticket
                                                showTicketDetailDialog = true
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Reply", fontSize = 11.sp)
                                        }
                                    }
                                }
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

    // 1. CALL ME BACK MODAL
    if (showCallbackModal) {
        Dialog(onDismissRequest = { showCallbackModal = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Request Partner Call Back", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text("Our dedicated senior seller manager will call to resolve your queries", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Registered Contact Mobile", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    OutlinedTextField(
                        value = callbackPhoneInput,
                        onValueChange = { callbackPhoneInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Preferred Call Slot", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    listOf(
                        "Immediate (within 15 mins)",
                        "Today Afternoon (2:00 PM - 4:00 PM)",
                        "Tomorrow Morning (10:00 AM - 12:00 PM)"
                    ).forEach { slot ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { callbackSlot = slot }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = callbackSlot == slot, onClick = { callbackSlot = slot })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(slot, fontSize = 12.sp, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCallbackModal = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                isCallbackRequested = true
                                showCallbackModal = false
                                Toast.makeText(context, "Callback scheduled! We will ring you on $callbackPhoneInput within 15 minutes.", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                        ) {
                            Text("Confirm Call Request")
                        }
                    }
                }
            }
        }
    }

    // 2. RAISE TICKET MODAL
    if (showRaiseTicketDialog) {
        Dialog(onDismissRequest = { showRaiseTicketDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Raise Support Ticket", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text("Detailed complaint submission with 24-hour resolution SLA", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Payment", "Return/RTO", "Shipping", "Listing").forEach { cat ->
                            FilterChip(
                                selected = newTicketCategory.contains(cat),
                                onClick = { newTicketCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Order ID (Optional)", fontSize = 11.sp, color = TextSecondary)
                            OutlinedTextField(
                                value = newTicketOrderId,
                                onValueChange = { newTicketOrderId = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Product SKU", fontSize = 11.sp, color = TextSecondary)
                            OutlinedTextField(
                                value = newTicketSku,
                                onValueChange = { newTicketSku = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Complaint Subject", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    OutlinedTextField(
                        value = newTicketSubject,
                        onValueChange = { newTicketSubject = it },
                        placeholder = { Text("e.g. Courier damaged package in transit", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Detailed Description", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    OutlinedTextField(
                        value = newTicketDescription,
                        onValueChange = { newTicketDescription = it },
                        placeholder = { Text("Explain full issue, tracking number, and barcode ID...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Attachment Simulator
                    OutlinedButton(
                        onClick = {
                            attachedFileSimulated = true
                            Toast.makeText(context, "Attached unboxing_video.mp4 & parcel_photo.jpg", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (attachedFileSimulated) "✓ 2 Files Attached (Video & Photo)" else "+ Attach Photo / Unboxing Video / PDF", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showRaiseTicketDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newTicketSubject.isBlank()) {
                                    Toast.makeText(context, "Please enter subject", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val newId = "TKT-${(1000..9999).random()}"
                                ticketsList.add(
                                    0,
                                    SupportTicket(
                                        ticketId = newId,
                                        sellerId = "SLR-AMIT-9821",
                                        orderId = newTicketOrderId,
                                        productSku = newTicketSku,
                                        category = newTicketCategory,
                                        subjectEn = newTicketSubject,
                                        subjectHi = newTicketSubject,
                                        description = newTicketDescription.ifBlank { "Ticket registered by Seller Amit." },
                                        status = TicketStatus.IN_PROGRESS,
                                        assignedAgent = "Siddharth Jain (Partner Lead)",
                                        createdDate = "Just now",
                                        lastUpdated = "Just now",
                                        priority = newTicketPriority,
                                        threadReplies = listOf(
                                            TicketReply("Siddharth Jain (Agent)", true, "Just now", "We have received ticket $newId. Our team is investigating.")
                                        )
                                    )
                                )
                                showRaiseTicketDialog = false
                                Toast.makeText(context, "Ticket $newId created! Agent assigned for resolution.", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                        ) {
                            Text("Submit Ticket")
                        }
                    }
                }
            }
        }
    }

    // 3. LIVE CHAT MODAL (Real-time in-app chat)
    if (showLiveChatModal) {
        Dialog(onDismissRequest = { showLiveChatModal = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().height(480.dp).padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEDE7F6))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(MeeshoMagenta),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("PV", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Priya Verma", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Text("Partner Support Lead • Active Now", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showLiveChatModal = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    // Chat messages scrollable
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatMessages, key = { it.id }) { msg ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 240.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (msg.isMe) MeeshoMagenta else Color(0xFFF1F1F1))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(msg.text, fontSize = 12.sp, color = if (msg.isMe) Color.White else TextPrimary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            msg.time,
                                            fontSize = 9.sp,
                                            color = if (msg.isMe) Color.White.copy(alpha = 0.7f) else TextSecondary,
                                            modifier = Modifier.align(Alignment.End)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Quick Chips
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Payout time?", "Courier pickup?", "Claim status").forEach { chip ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF3E5F5))
                                    .clickable {
                                        chatMessages.add(ChatMessage("${chatMessages.size + 1}", "Amit", true, chip, "Just now"))
                                        chatMessages.add(ChatMessage("${chatMessages.size + 2}", "Priya", false, "Sure Amit ji! Looking into $chip for you immediately.", "Just now"))
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(chip, fontSize = 10.sp, color = MeeshoMagenta)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Input Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text("Type your message...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (chatInputText.isNotBlank()) {
                                    chatMessages.add(ChatMessage("${chatMessages.size + 1}", "Amit", true, chatInputText, "Just now"))
                                    val sentText = chatInputText
                                    chatInputText = ""
                                    chatMessages.add(ChatMessage("${chatMessages.size + 2}", "Priya", false, "Thank you for the update. We are checking the details for '$sentText'.", "Just now"))
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MeeshoMagenta)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    // 4. TICKET DETAILS & REPLY MODAL
    if (showTicketDetailDialog && selectedTicketForDetail != null) {
        val ticket = selectedTicketForDetail!!
        Dialog(onDismissRequest = { showTicketDetailDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().height(520.dp).padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ticket.ticketId, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MeeshoMagenta)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ticket.status.bgColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(ticket.status.labelEn, color = ticket.status.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(ticket.subjectEn, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("Order: ${ticket.orderId} • SKU: ${ticket.productSku} • Agent: ${ticket.assignedAgent}", fontSize = 11.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = DividerColor)

                    // Reply Thread
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ticket.threadReplies) { reply ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = if (reply.isAgent) Color(0xFFF1F8E9) else Color(0xFFF5F5F5)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(reply.senderName, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (reply.isAgent) Color(0xFF2E7D32) else TextPrimary)
                                        Text(reply.timestamp, fontSize = 9.sp, color = TextSecondary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(reply.message, fontSize = 12.sp, color = TextPrimary)
                                }
                            }
                        }
                    }

                    // Reply input
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = ticketReplyInput,
                            onValueChange = { ticketReplyInput = it },
                            placeholder = { Text("Add reply / information...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (ticketReplyInput.isNotBlank()) {
                                    val updatedReplies = ticket.threadReplies + TicketReply("Amit kumar patwa (Seller)", false, "Just now", ticketReplyInput)
                                    val idx = ticketsList.indexOfFirst { it.ticketId == ticket.ticketId }
                                    if (idx != -1) {
                                        ticketsList[idx] = ticket.copy(threadReplies = updatedReplies)
                                    }
                                    ticketReplyInput = ""
                                    Toast.makeText(context, "Reply sent to support agent!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Send", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Downloading Complaint Report (PDF)...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Report PDF", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { showTicketDetailDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = TextPrimary),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Close", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
