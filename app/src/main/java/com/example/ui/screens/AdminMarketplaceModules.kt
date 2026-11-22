package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FraudAlertItem
import com.example.model.MarketplaceCoupon
import com.example.ui.theme.*

data class AdminBuyerUser(
    val id: String,
    val name: String,
    val phone: String,
    val city: String,
    val ordersCount: Int,
    val totalSpend: Int,
    var isBlocked: Boolean = false
)

data class AdminComplaintItem(
    val id: String,
    val customer: String,
    val orderId: String,
    val issue: String,
    val type: String, // "Refund", "RTO", "Complaint"
    val amount: Int,
    var status: String // "Pending", "Resolved", "Refunded"
)

data class AdminRolePermission(
    val roleName: String,
    var canApproveProducts: Boolean,
    var canReleasePayouts: Boolean,
    var canBlockUsers: Boolean,
    var canEditCoupons: Boolean
)

// =========================================================================
// TAB 6: ANALYTICS & P&L REPORTS (Module 12)
// =========================================================================
@Composable
fun AdminAnalyticsReportsModule() {
    var selectedPeriod by remember { mutableStateOf("Monthly") }
    val chartData = when (selectedPeriod) {
        "Daily" -> listOf("9AM" to 0.35f, "12PM" to 0.6f, "3PM" to 0.52f, "6PM" to 0.9f, "9PM" to 0.8f)
        "Weekly" -> listOf("Mon" to 0.55f, "Tue" to 0.65f, "Wed" to 0.5f, "Thu" to 0.78f, "Fri" to 0.85f, "Sat" to 0.96f, "Sun" to 0.92f)
        "Yearly" -> listOf("2022" to 0.4f, "2023" to 0.58f, "2024" to 0.72f, "2025" to 0.88f, "2026" to 1.0f)
        else -> listOf("Jul" to 0.5f, "Aug" to 0.64f, "Sep" to 0.75f, "Oct" to 0.92f, "Nov" to 1.0f)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Sales Analytics & P&L Report", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Daily", "Weekly", "Monthly", "Yearly").forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(period, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MeeshoMagenta,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Revenue Bar Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFFFDF2F8), RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barCount = chartData.size
                    val space = size.width / barCount
                    val barWidth = space * 0.48f
                    chartData.forEachIndexed { idx, (_, ratio) ->
                        val barH = size.height * ratio * 0.85f
                        val x = idx * space + (space - barWidth) / 2f
                        val y = size.height - barH
                        drawRoundRect(
                            color = if (idx == barCount - 1) Color(0xFF9C1B68) else Color(0xFFF472B6),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barH),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                chartData.forEach { (label, _) ->
                    Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                }
            }

            HorizontalDivider()

            // Profit & Loss Metrics
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminMetricCard("Net Revenue", "₹14.82L", "▲ +18.4%", Color(0xFF16A34A), Icons.Default.TrendingUp, Modifier.weight(1f))
                AdminMetricCard("Cancelled / RTO", "3.2%", "Down -1.1%", Color(0xFFDC2626), Icons.Default.AssignmentReturn, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminMetricCard("Net Platform Profit", "₹1,48,260", "After GST/TDS", Color(0xFF2563EB), Icons.Default.AccountBalance, Modifier.weight(1f))
                AdminMetricCard("Customer Growth", "+1,240", "New this month", Color(0xFF7C3AED), Icons.Default.GroupAdd, Modifier.weight(1f))
            }

            // Best Selling & Low Stock Alerts
            Text("Best Selling Products & Low Stock Alerts", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            listOf(
                Triple("Royal Wine Embroidered Anarkali", "412 Sold • ₹599", "In Stock (140)"),
                Triple("Banarasi Zari Soft Silk Saree", "389 Sold • ₹749", "Low Stock (7 left!)"),
                Triple("Heavy Bridal Velvet Lehenga", "196 Sold • ₹999", "Low Stock (4 left!)")
            ).forEach { (title, sales, stock) ->
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF8FAFC), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(sales, fontSize = 11.sp, color = TextSecondary)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (stock.contains("Low")) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                        ) {
                            Text(
                                stock,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (stock.contains("Low")) Color(0xFFDC2626) else Color(0xFF16A34A),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 7: BUYERS, BANNERS, CATEGORIES & COUPONS (Module 6)
// =========================================================================
@Composable
fun AdminBuyersCouponsBannersModule(
    initialSubSection: Int = 0
) {
    val context = LocalContext.current
    var subSection by remember(initialSubSection) { mutableIntStateOf(initialSubSection) } // 0 = Buyers, 1 = Coupons, 2 = Banners & Categories

    val buyers = remember {
        mutableStateListOf(
            AdminBuyerUser("b1", "Amit Kumar Patwa", "+91 9128754982", "Bettiah, Bihar", 14, 12890, false),
            AdminBuyerUser("b2", "Pooja Kumari", "+91 9835107144", "Patna, Bihar", 8, 6420, false),
            AdminBuyerUser("b3", "Ramesh Singh", "+91 7488774049", "Motihari, Bihar", 3, 2150, true)
        )
    }

    val coupons = remember {
        mutableStateListOf(
            MarketplaceCoupon("FESTIVE15", "15% Extra OFF on Diwali & Chhath Ethnic Wear", 150, 499, true),
            MarketplaceCoupon("GYANVI50", "Flat ₹50 OFF on First Order", 50, 299, true),
            MarketplaceCoupon("MEGA100", "₹100 OFF on Bridal Lehengas", 100, 999, true)
        )
    }

    var newCouponCode by remember { mutableStateOf("") }
    var newCouponFlat by remember { mutableStateOf("75") }
    var bannerFestiveActive by remember { mutableStateOf(true) }
    var bannerFlashSaleActive by remember { mutableStateOf(true) }
    val categories = remember { mutableStateListOf("Kurtis & Suits", "Banarasi Sarees", "Bridal Lehengas", "Kids Ethnic", "Men's Kurta", "Dupattas") }
    var newCategoryInput by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Buyers (Block/Unblock)", "Coupon Manager", "Banners & Categories").forEachIndexed { idx, label ->
                FilterChip(
                    selected = subSection == idx,
                    onClick = { subSection = idx },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MeeshoMagenta,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        when (subSection) {
            0 -> {
                buyers.forEach { buyer ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(buyer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${buyer.phone} • ${buyer.city}", fontSize = 11.sp, color = TextSecondary)
                                Text("Orders: ${buyer.ordersCount} • Total Spend: ₹${buyer.totalSpend}", fontSize = 11.sp, color = GreenRating, fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = {
                                    val idx = buyers.indexOf(buyer)
                                    buyers[idx] = buyer.copy(isBlocked = !buyer.isBlocked)
                                    Toast.makeText(context, "${buyer.name} ${if (!buyer.isBlocked) "Blocked" else "Unblocked"}", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (buyer.isBlocked) Color(0xFF16A34A) else Color(0xFFDC2626)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(if (buyer.isBlocked) "Unblock" else "Block User", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            1 -> {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Create New Discount Coupon", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newCouponCode,
                                onValueChange = { newCouponCode = it.uppercase() },
                                label = { Text("Code") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newCouponFlat,
                                onValueChange = { newCouponFlat = it },
                                label = { Text("₹ Off") },
                                singleLine = true,
                                modifier = Modifier.width(90.dp)
                            )
                        }
                        Button(
                            onClick = {
                                if (newCouponCode.isNotBlank()) {
                                    coupons.add(0, MarketplaceCoupon(newCouponCode, "Custom Admin Offer", newCouponFlat.toIntOrNull() ?: 50, 399, true))
                                    newCouponCode = ""
                                    Toast.makeText(context, "Coupon published live!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Publish Coupon", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                coupons.forEachIndexed { idx, coupon ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${coupon.code} (₹${coupon.discountFlat} OFF)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MeeshoMagenta)
                                Text(coupon.description, fontSize = 11.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = coupon.isActive,
                                onCheckedChange = { coupons[idx] = coupon.copy(isActive = it) }
                            )
                        }
                    }
                }
            }
            2 -> {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Promotional Banner Controls", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("Maha Dhamaka Festive Banner", fontSize = 13.sp)
                            Switch(checked = bannerFestiveActive, onCheckedChange = { bannerFestiveActive = it })
                        }
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("Countdown Flash Sale Timer Strip", fontSize = 13.sp)
                            Switch(checked = bannerFlashSaleActive, onCheckedChange = { bannerFlashSaleActive = it })
                        }
                        HorizontalDivider()
                        Text("Marketplace Categories (${categories.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newCategoryInput,
                                onValueChange = { newCategoryInput = it },
                                placeholder = { Text("Add new category...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (newCategoryInput.isNotBlank()) {
                                        categories.add(newCategoryInput.trim())
                                        newCategoryInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Text("Add")
                            }
                        }
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            categories.forEach { cat ->
                                SuggestionChip(onClick = {}, label = { Text(cat, fontSize = 11.sp) })
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 8: REFUNDS, RTO, COMPLAINTS & RBAC SECURITY LOGS (Module 6 & 11)
// =========================================================================
@Composable
fun AdminRefundsRtoAndSecurityModule(
    initialSubSection: Int = 0
) {
    val context = LocalContext.current
    var subSection by remember(initialSubSection) { mutableIntStateOf(initialSubSection) } // 0 = All / Support & Refunds, 1 = Security & RBAC
    val complaints = remember {
        mutableStateListOf(
            AdminComplaintItem("CMP-204", "Pooja Kumari", "#ORD-9844", "Size mismatch exchange requested", "Refund", 999, "Pending"),
            AdminComplaintItem("RTO-118", "Maa Sharda Garments", "#ORD-9844", "Customer unreachable • RTO Claim", "RTO", 999, "Pending"),
            AdminComplaintItem("CMP-201", "Anjali Sharma", "#ORD-9842", "Urgent delivery requested for wedding", "Complaint", 699, "Resolved")
        )
    }

    val rbacRoles = remember {
        mutableStateListOf(
            AdminRolePermission("Super Admin (Owner)", true, true, true, true),
            AdminRolePermission("Catalog & QC Manager", true, false, false, true),
            AdminRolePermission("Finance & Payout Officer", false, true, false, false),
            AdminRolePermission("Support & Complaint Agent", false, false, true, false)
        )
    }

    val auditLogs = remember {
        mutableStateListOf(
            "18:10 • Super Admin approved 2 catalogs (QC-103, QC-104)",
            "17:45 • NEFT Batch #941 settled ₹84,200 to 24 sellers",
            "16:20 • Coupon FESTIVE15 activated for Chhath Puja Sale"
        )
    }

    val fraudAlerts = remember {
        mutableStateListOf(
            FraudAlertItem(
                id = "FRD-901",
                targetLabel = "Ramesh Singh (+91 7488774049) • #ORD-9844",
                reason = "High COD RTO Spike (64% returns across 7 orders) • Motihari",
                riskScore = 88,
                status = "Flagged"
            ),
            FraudAlertItem(
                id = "FRD-902",
                targetLabel = "Bulk Order Cluster • Pincode 845401 • #ORD-9851",
                reason = "4 COD orders > ₹12,000 placed within 8 mins from same device",
                riskScore = 76,
                status = "Flagged"
            ),
            FraudAlertItem(
                id = "FRD-903",
                targetLabel = "Coupon Abuse Check (FESTIVE15) • #ORD-9839",
                reason = "Duplicate referral & coupon device fingerprint detected",
                riskScore = 62,
                status = "Verified"
            )
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Support & Refunds (सहायता)",
                "Security & RBAC (सुरक्षा)",
                "Fraud Detection (धोखाधड़ी)"
            ).forEachIndexed { idx, label ->
                FilterChip(
                    selected = subSection == idx,
                    onClick = { subSection = idx },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MeeshoMagenta,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (subSection == 0) {
            Text("Refunds, RTO & Customer Support (ग्राहक सहायता)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)

            complaints.forEachIndexed { idx, item ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("${item.id} • ${item.type.uppercase()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MeeshoMagenta)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (item.status == "Pending") Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    item.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.status == "Pending") Color(0xFFB45309) else Color(0xFF16A34A),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text("${item.customer} (${item.orderId}) • ₹${item.amount}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(item.issue, fontSize = 12.sp, color = TextSecondary)
                        if (item.status == "Pending") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        complaints[idx] = item.copy(status = if (item.type == "Refund") "Refunded" else "Resolved")
                                        auditLogs.add(0, "Now • Admin resolved ${item.id} (${item.type})")
                                        Toast.makeText(context, "${item.id} approved & processed!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                    modifier = Modifier.weight(1f).height(36.dp)
                                ) {
                                    Text(if (item.type == "Refund") "Approve Refund" else "Resolve Issue", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else if (subSection == 1) {
            // Role-Based Access Control (RBAC) Permission Matrix
            Text("Role-Based Access Control & Security (सुरक्षा)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
            rbacRoles.forEachIndexed { idx, role ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(role.roleName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MeeshoMagentaDark)
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("Approve Catalogs", fontSize = 11.sp)
                            Checkbox(checked = role.canApproveProducts, onCheckedChange = { rbacRoles[idx] = role.copy(canApproveProducts = it) })
                            Text("Release Payouts", fontSize = 11.sp)
                            Checkbox(checked = role.canReleasePayouts, onCheckedChange = { rbacRoles[idx] = role.copy(canReleasePayouts = it) })
                        }
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("Block/Unblock Users", fontSize = 11.sp)
                            Checkbox(checked = role.canBlockUsers, onCheckedChange = { rbacRoles[idx] = role.copy(canBlockUsers = it) })
                            Text("Manage Coupons", fontSize = 11.sp)
                            Checkbox(checked = role.canEditCoupons, onCheckedChange = { rbacRoles[idx] = role.copy(canEditCoupons = it) })
                        }
                    }
                }
            }

            // Admin Activity Logs
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Admin Activity & Audit Logs", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    auditLogs.forEach { log ->
                        Text("• $log", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        } else {
            Text("AI Fraud Detection & COD Risk Shield", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
            fraudAlerts.forEachIndexed { idx, alert ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("${alert.id} • Risk ${alert.riskScore}/100", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFDC2626))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (alert.status) {
                                    "Blocked" -> Color(0xFFFEE2E2)
                                    "Verified" -> Color(0xFFDCFCE7)
                                    else -> Color(0xFFFEF3C7)
                                }
                            ) {
                                Text(
                                    alert.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (alert.status) {
                                        "Blocked" -> Color(0xFFDC2626)
                                        "Verified" -> Color(0xFF16A34A)
                                        else -> Color(0xFFB45309)
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(alert.targetLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(alert.reason, fontSize = 11.5.sp, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    fraudAlerts[idx] = alert.copy(status = "Verified")
                                    auditLogs.add(0, "Now • Marked ${alert.id} Verified Safe")
                                    Toast.makeText(context, "${alert.id} marked Genuine", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text("Verify Genuine", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    fraudAlerts[idx] = alert.copy(status = "Blocked")
                                    auditLogs.add(0, "Now • Blocked risky account for ${alert.id}")
                                    Toast.makeText(context, "${alert.targetLabel} blocked for COD!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text("Block Account", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
