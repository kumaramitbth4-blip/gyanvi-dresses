package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.StaffMember
import com.example.model.StaffDepartment
import com.example.model.StaffAssignedTask
import com.example.model.SecurityActivityEntry
import com.example.model.*
import com.example.ui.theme.*
import com.example.util.HoldOrderPdfGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffWorkspaceScreen(
    staffMembers: List<StaffMember>,
    activeStaff: StaffMember?,
    staffTasks: List<StaffAssignedTask>,
    orders: List<OrderItem>,
    products: List<Product>,
    chatMessages: List<BuyerSellerChatMessage>,
    activityHistory: List<SecurityActivityEntry> = emptyList(),
    onBack: () -> Unit,
    onSelectStaff: (String) -> Unit,
    onVerifyStaffPin: (codeOrPhone: String, pin: String) -> Pair<Boolean, String>,
    onCheckInOutStaff: (staffId: String, newStatus: String) -> Unit = { _, _ -> },
    onAssignTaskFromManager: (title: String, desc: String, dept: StaffDepartment, staffId: String, priority: String) -> Unit = { _, _, _, _, _ -> },
    onUpdateTaskStatus: (taskId: String, newStatus: String) -> Unit,
    onUpdateOrderStatus: (orderId: String, status: OrderStatus) -> Unit,
    onAdvanceTracking: (orderId: String) -> Unit,
    onScheduleReturnPickup: (orderId: String) -> Unit,
    onUpdateStockQuick: (productId: String, delta: Int) -> Unit,
    onToggleStockPause: (productId: String) -> Unit,
    onSendSupportChat: (text: String, senderName: String) -> Unit,
    onOpenOwnerPanel: () -> Unit,
    onOpenCustomerShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val currentStaff = activeStaff ?: staffMembers.firstOrNull()
    val activeDept = currentStaff?.department ?: StaffDepartment.ORDER_PACKER
    val deptColor = Color(activeDept.accentHex)

    var showRoleMatrix by remember { mutableStateOf(false) }
    var showPinSwitchDialog by remember { mutableStateOf(false) }
    var catalogSearch by remember { mutableStateOf("") }
    var supportReplyInput by remember { mutableStateOf("") }

    val departmentTasks = remember(staffTasks, activeDept, currentStaff?.id) {
        staffTasks.filter {
            it.department == activeDept || (currentStaff != null && it.assignedToStaffId == currentStaff.id)
        }
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("staff_back_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "STAFF WORKSPACE",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = deptColor
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(shape = RoundedCornerShape(4.dp), color = deptColor.copy(alpha = 0.14f)) {
                                        Text(
                                            text = activeDept.shortBadge,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = deptColor,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${currentStaff?.name ?: "Employee"} (${currentStaff?.staffCode ?: "EMP"}) • कर्मचारी पैनल",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { showPinSwitchDialog = true },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp).testTag("staff_pin_switch_btn")
                            ) {
                                Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(14.dp), tint = deptColor)
                                Spacer(Modifier.width(4.dp))
                                Text("Staff PIN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = deptColor)
                            }
                        }
                    }

                    // Department Staff Selector Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        staffMembers.forEach { emp ->
                            val isSelected = currentStaff?.id == emp.id
                            val empAccent = Color(emp.department.accentHex)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (emp.isActive) {
                                        onSelectStaff(emp.id)
                                    } else {
                                        Toast.makeText(context, "${emp.name} is paused by Owner", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                label = {
                                    Text(
                                        "${emp.department.shortBadge}: ${emp.name.substringBefore(" ")}",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = empAccent,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                modifier = Modifier.testTag("staff_chip_${emp.id}")
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC),
        modifier = modifier.fillMaxSize().testTag("staff_workspace_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Active Karamchari Role & Responsibilities Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(listOf(deptColor, deptColor.copy(alpha = 0.78f)))
                                )
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(activeDept.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                        Text(activeDept.hindiTitle, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                                    }
                                    Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.2f)) {
                                        Text(
                                            "${currentStaff?.tasksCompleted ?: 0} Done",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Text(
                                    "Shift: ${currentStaff?.shift ?: "Morning"} • Phone: ${currentStaff?.phone ?: ""}",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.18f)) {
                                        Text(
                                            "Att: ${currentStaff?.attendanceStatus ?: "Present"} (${currentStaff?.attendanceDaysThisMonth ?: 22}d)",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.18f)) {
                                        Text(
                                            "Score: ${currentStaff?.performanceScore ?: 95}%",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.18f)) {
                                        val salTag = if (currentStaff?.isSalaryPaidThisMonth == true) "Paid ✓" else "Due"
                                        Text(
                                            "₹${currentStaff?.monthlySalary ?: 18000} ($salTag)",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Attendance Check-In / Break / Check-Out Bar
                            if (currentStaff != null) {
                                Text(
                                    "Daily Attendance (हाज़िरी) • Check-In: ${currentStaff.checkInTime}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Present" to "Check In ✓", "On Break" to "Break ☕", "Checked Out" to "Check Out").forEach { (st, lbl) ->
                                        val isCurrent = currentStaff.attendanceStatus == st
                                        FilterChip(
                                            selected = isCurrent,
                                            onClick = {
                                                onCheckInOutStaff(currentStaff.id, st)
                                                Toast.makeText(context, "${currentStaff.name}: $st", Toast.LENGTH_SHORT).show()
                                            },
                                            label = { Text(lbl, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF15803D),
                                                selectedLabelColor = Color.White
                                            ),
                                            modifier = Modifier.testTag("staff_att_${st.lowercase().replace(" ", "_")}")
                                        )
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = Color(0xFFF1F5F9))
                            }

                            Text("Your Assigned Scope (आपकी ज़िम्मेदारी):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                            activeDept.responsibilities.forEach { line ->
                                Text("• $line", fontSize = 11.5.sp, color = TextSecondary)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { showRoleMatrix = !showRoleMatrix }) {
                                    Text(
                                        if (showRoleMatrix) "Hide Role Division ▲" else "Owner / Staff / Customer Division ▼",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = deptColor
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = onOpenCustomerShop,
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Customer View", fontSize = 10.5.sp)
                                    }
                                    Button(
                                        onClick = onOpenOwnerPanel,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Owner Panel", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            AnimatedVisibility(visible = showRoleMatrix) {
                                RoleDivisionMatrixCard()
                            }
                        }
                    }
                }
            }

            // 2. Owner-Assigned Tasks for This Department / Employee
            item {
                Text(
                    "Assigned Work Queue (${departmentTasks.count { it.status != "Completed" }} Pending)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            }

            if (departmentTasks.isEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                        Text(
                            "No pending tasks assigned right now for ${activeDept.shortBadge}.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(departmentTasks, key = { it.id }) { task ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, if (task.status == "Completed") Color(0xFFBBF7D0) else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth().testTag("staff_task_card_${task.id}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text(task.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (task.status) {
                                        "Completed" -> Color(0xFFDCFCE7)
                                        "In Progress" -> Color(0xFFDBEAFE)
                                        else -> Color(0xFFFEF3C7)
                                    }
                                ) {
                                    Text(
                                        task.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (task.status) {
                                            "Completed" -> Color(0xFF16A34A)
                                            "In Progress" -> Color(0xFF1D4ED8)
                                            else -> Color(0xFFB45309)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(task.description, fontSize = 11.5.sp, color = TextSecondary)
                            Text(
                                "Priority: ${task.priority} • Assigned by ${task.assignedBy}" +
                                    if (task.relatedOrderId.isNotBlank()) " • #${task.relatedOrderId}" else "",
                                fontSize = 10.5.sp,
                                color = deptColor,
                                fontWeight = FontWeight.Medium
                            )
                            if (task.status != "Completed") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (task.status == "Pending") {
                                        OutlinedButton(
                                            onClick = { onUpdateTaskStatus(task.id, "In Progress") },
                                            modifier = Modifier.weight(1f).height(38.dp)
                                        ) {
                                            Text("Start Work", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            onUpdateTaskStatus(task.id, "Completed")
                                            Toast.makeText(context, "✓ Task Completed!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Text("Mark Completed ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Department-Specific Live Operational Desk
            item {
                Text(
                    "Live Department Operations (${activeDept.hindiTitle})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            }

            when (activeDept) {
                StaffDepartment.ORDER_PACKER -> {
                    if (currentStaff?.canPackOrders == false) {
                        item { RestrictedPermissionCard("Order Packing Permission") }
                    } else {
                        val packQueue = orders.filter {
                            it.status in listOf(OrderStatus.ORDERED, OrderStatus.CONFIRMED, OrderStatus.PROCESSING, OrderStatus.READY_TO_SHIP)
                        }
                        if (packQueue.isEmpty()) {
                            item { EmptyDeptCard("All pending orders are packed & handed over to Dispatch!") }
                        } else {
                            items(packQueue, key = { "pack_${it.orderId}" }) { ord ->
                                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                            Text("#${ord.orderId} • ${ord.product.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                            Text(ord.status.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = deptColor)
                                        }
                                        Text("Size: ${ord.selectedSize} | Color: ${ord.selectedColor} | Qty: ${ord.quantity}", fontSize = 11.sp, color = TextSecondary)
                                        Text("Packer: ${ord.packerName} (${ord.packerId})", fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (ord.status == OrderStatus.ORDERED) {
                                                Button(onClick = { onUpdateOrderStatus(ord.orderId, OrderStatus.CONFIRMED) }, colors = ButtonDefaults.buttonColors(containerColor = deptColor), modifier = Modifier.height(36.dp)) {
                                                    Text("Confirm", fontSize = 11.sp)
                                                }
                                            }
                                            if (ord.status == OrderStatus.ORDERED || ord.status == OrderStatus.CONFIRMED) {
                                                Button(onClick = { onUpdateOrderStatus(ord.orderId, OrderStatus.PROCESSING) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)), modifier = Modifier.height(36.dp)) {
                                                    Text("Start Packing", fontSize = 11.sp)
                                                }
                                            }
                                            if (ord.status != OrderStatus.READY_TO_SHIP) {
                                                Button(onClick = { onUpdateOrderStatus(ord.orderId, OrderStatus.READY_TO_SHIP) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)), modifier = Modifier.height(36.dp)) {
                                                    Text("Ready to Ship", fontSize = 11.sp)
                                                }
                                            }
                                            OutlinedButton(onClick = { HoldOrderPdfGenerator.saveShippingLabelPdfToDownloads(context, ord) }, modifier = Modifier.height(36.dp)) {
                                                Text("Label PDF", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                StaffDepartment.DISPATCH_DELIVERY -> {
                    if (currentStaff?.canDeliverOrders == false) {
                        item { RestrictedPermissionCard("Delivery & Dispatch Permission") }
                    } else {
                        val deliveryQueue = orders.filter {
                            it.status in listOf(OrderStatus.READY_TO_SHIP, OrderStatus.SHIPPED, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.RETURN_REQUESTED)
                        }
                        if (deliveryQueue.isEmpty()) {
                            item { EmptyDeptCard("No parcels awaiting dispatch or doorstep delivery right now.") }
                        } else {
                            items(deliveryQueue, key = { "del_${it.orderId}" }) { ord ->
                                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                            Text("#${ord.orderId} • ${ord.customerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(ord.status.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = deptColor)
                                        }
                                        Text("${ord.deliveryAddress} (${ord.city} - ${ord.pinCode})", fontSize = 11.sp, color = TextSecondary)
                                        Text("${ord.courierPartner} • AWB: ${ord.awbNumber.ifBlank { "Assigning" }} • ₹${ord.totalAmount} (${ord.paymentMethod})", fontSize = 11.sp, color = Color(0xFF0369A1))
                                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (ord.status == OrderStatus.READY_TO_SHIP) {
                                                Button(onClick = { onUpdateOrderStatus(ord.orderId, OrderStatus.SHIPPED) }, colors = ButtonDefaults.buttonColors(containerColor = deptColor), modifier = Modifier.height(36.dp)) {
                                                    Text("Dispatch Parcel", fontSize = 11.sp)
                                                }
                                            }
                                            if (ord.status == OrderStatus.SHIPPED) {
                                                Button(onClick = { onUpdateOrderStatus(ord.orderId, OrderStatus.OUT_FOR_DELIVERY) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)), modifier = Modifier.height(36.dp)) {
                                                    Text("Out for Delivery", fontSize = 11.sp)
                                                }
                                            }
                                            if (ord.status == OrderStatus.SHIPPED || ord.status == OrderStatus.OUT_FOR_DELIVERY) {
                                                Button(onClick = { onUpdateOrderStatus(ord.orderId, OrderStatus.DELIVERED) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)), modifier = Modifier.height(36.dp)) {
                                                    Text("Mark Delivered", fontSize = 11.sp)
                                                }
                                                OutlinedButton(onClick = { onAdvanceTracking(ord.orderId) }, modifier = Modifier.height(36.dp)) {
                                                    Text("Ping Location", fontSize = 11.sp)
                                                }
                                            }
                                            if (ord.status == OrderStatus.RETURN_REQUESTED) {
                                                Button(onClick = { onUpdateOrderStatus(ord.orderId, OrderStatus.RETURNED) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)), modifier = Modifier.height(36.dp)) {
                                                    Text("Pick Return Parcel", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                StaffDepartment.CATALOG_INVENTORY -> {
                    if (currentStaff?.canEditStock == false) {
                        item { RestrictedPermissionCard("Catalog & Stock Edit Permission") }
                    } else {
                        item {
                            OutlinedTextField(
                                value = catalogSearch,
                                onValueChange = { catalogSearch = it },
                                placeholder = { Text("Search SKU / Dress Name for Stock Update...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        val matchingCatalog = products.filter {
                            catalogSearch.isBlank() || it.name.contains(catalogSearch, true) || it.category.contains(catalogSearch, true)
                        }.take(15)
                        items(matchingCatalog, key = { "inv_${it.id}" }) { prod ->
                            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            "${prod.category} • Stock: ${prod.stockCount} • ${if (prod.inStock) "Live" else "Paused"}",
                                            fontSize = 11.sp,
                                            color = if (prod.stockCount <= 5 || !prod.inStock) Color(0xFFDC2626) else Color(0xFF16A34A),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedButton(onClick = { onUpdateStockQuick(prod.id, -1) }, contentPadding = PaddingValues(horizontal = 8.dp), modifier = Modifier.height(34.dp)) {
                                            Text("-1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(onClick = { onUpdateStockQuick(prod.id, 10) }, colors = ButtonDefaults.buttonColors(containerColor = deptColor), contentPadding = PaddingValues(horizontal = 8.dp), modifier = Modifier.height(34.dp)) {
                                            Text("+10", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        IconButton(onClick = { onToggleStockPause(prod.id) }, modifier = Modifier.size(34.dp)) {
                                            Icon(
                                                if (prod.inStock) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                                contentDescription = "Toggle Stock",
                                                tint = if (prod.inStock) Color(0xFFD97706) else Color(0xFF16A34A)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                StaffDepartment.SUPPORT_RETURNS -> {
                    if (currentStaff?.canHandleSupport == false) {
                        item { RestrictedPermissionCard("Customer Support & Returns Permission") }
                    } else {
                        item {
                            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Live Customer Care Chat Desk", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                    chatMessages.takeLast(5).forEach { msg ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (msg.senderRole == "BUYER") Color(0xFFF1F5F9) else Color(0xFFECFDF5),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("${msg.senderName} (${msg.senderRole}) • ${msg.timeLabel}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                                Text(msg.text, fontSize = 12.sp, color = TextPrimary)
                                            }
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedTextField(
                                            value = supportReplyInput,
                                            onValueChange = { supportReplyInput = it },
                                            placeholder = { Text("Reply as Support Staff...") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(
                                            onClick = {
                                                if (supportReplyInput.isNotBlank()) {
                                                    onSendSupportChat(supportReplyInput, "${currentStaff?.name ?: "Support"} (Care Staff)")
                                                    supportReplyInput = ""
                                                    Toast.makeText(context, "Reply sent to Buyer!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = deptColor),
                                            modifier = Modifier.height(50.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                                        }
                                    }
                                }
                            }
                        }
                        val returnOrders = orders.filter {
                            it.status == OrderStatus.RETURN_REQUESTED || it.status == OrderStatus.DELIVERED || it.status == OrderStatus.RETURNED
                        }
                        items(returnOrders, key = { "sup_${it.orderId}" }) { ord ->
                            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("#${ord.orderId} • ${ord.customerName}", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                        Text("${ord.status.label} • ₹${ord.totalAmount}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    if (ord.status == OrderStatus.RETURN_REQUESTED || ord.status == OrderStatus.DELIVERED) {
                                        OutlinedButton(
                                            onClick = {
                                                onScheduleReturnPickup(ord.orderId)
                                                Toast.makeText(context, "Reverse Pickup scheduled!", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Text("Schedule Pickup", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                StaffDepartment.FINANCE_ACCOUNTS -> {
                    if (currentStaff?.canViewGstFinance == false) {
                        item { RestrictedPermissionCard("Accounts & GST Finance Permission") }
                    } else {
                        val prepaidSum = orders.filter { !it.paymentMethod.contains("COD", true) }.sumOf { it.totalAmount }
                        val codSum = orders.filter { it.paymentMethod.contains("COD", true) }.sumOf { it.totalAmount }
                        val slab5Orders = orders.filter { it.totalAmount <= 1000 }
                        val slab12Orders = orders.filter { it.totalAmount > 1000 }
                        val gst5Amount = (slab5Orders.sumOf { it.totalAmount } * 5) / 105
                        val gst12Amount = (slab12Orders.sumOf { it.totalAmount } * 12) / 112
                        val totalGst = gst5Amount + gst12Amount

                        item {
                            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)), border = BorderStroke(1.dp, Color(0xFFFDE68A))) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Daily Billing & COD Tally (Accounts Staff)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF92400E))
                                    Text("Prepaid Digital Collection: ₹$prepaidSum", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF15803D))
                                    Text("COD Collection Pending/Collected: ₹$codSum", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFB45309))
                                    HorizontalDivider(color = Color(0xFFFDE68A))
                                    Text("GST Tax Summary (Apparel 5% & 12% Slabs)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = TextPrimary)
                                    Text("• 5% Slab (≤₹1000 • ${slab5Orders.size} orders): ₹$gst5Amount GST", fontSize = 11.5.sp, color = TextSecondary)
                                    Text("• 12% Slab (>₹1000 • ${slab12Orders.size} orders): ₹$gst12Amount GST", fontSize = 11.5.sp, color = TextSecondary)
                                    Text("• Total GST: ₹$totalGst (CGST ₹${totalGst / 2} + SGST ₹${totalGst - (totalGst / 2)})", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                    Text("🔒 Note: Final Seller Payout Release & Commission % edits require Owner (मालिक) Panel.", fontSize = 11.sp, color = Color(0xFFDC2626))
                                }
                            }
                        }
                        items(orders.take(10), key = { "acc_${it.orderId}" }) { ord ->
                            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("INV-${ord.orderId} • ₹${ord.totalAmount}", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                        Text("${ord.customerName} • ${ord.paymentMethod} • ${ord.status.label}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    OutlinedButton(
                                        onClick = { HoldOrderPdfGenerator.saveSingleGstInvoicePdfToDownloads(context, ord) }
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("GST PDF", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                StaffDepartment.DEPARTMENT_MANAGER -> {
                    if (currentStaff?.canManageStaff == false) {
                        item { RestrictedPermissionCard("Department Manager Control Permission") }
                    } else {
                        item {
                            var mgrTaskTitle by remember { mutableStateOf("") }
                            var mgrTaskPriority by remember { mutableStateOf("High") }
                            val assignableTeam = staffMembers.filter { it.id != currentStaff?.id }
                            var targetEmpId by remember(assignableTeam) { mutableStateOf(assignableTeam.firstOrNull()?.id ?: "") }
                            val selectedAssignee = staffMembers.find { it.id == targetEmpId }

                            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)), border = BorderStroke(1.dp, Color(0xFFC7D2FE))) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Manager Quick Task Assign (कर्मचारी को काम सौंपें)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF312E81))
                                    OutlinedTextField(
                                        value = mgrTaskTitle,
                                        onValueChange = { mgrTaskTitle = it },
                                        placeholder = { Text("e.g. Clear 5 urgent Patna packing orders") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        assignableTeam.forEach { emp ->
                                            FilterChip(
                                                selected = targetEmpId == emp.id,
                                                onClick = { targetEmpId = emp.id },
                                                label = { Text("${emp.name.substringBefore(" ")} (${emp.department.shortBadge})", fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        listOf("High", "Medium", "Normal").forEach { pri ->
                                            FilterChip(
                                                selected = mgrTaskPriority == pri,
                                                onClick = { mgrTaskPriority = pri },
                                                label = { Text(pri, fontSize = 10.5.sp) }
                                            )
                                        }
                                        Spacer(Modifier.weight(1f))
                                        Button(
                                            onClick = {
                                                if (mgrTaskTitle.isNotBlank() && selectedAssignee != null) {
                                                    onAssignTaskFromManager(
                                                        mgrTaskTitle.trim(),
                                                        "Escalated by Manager ${currentStaff?.name ?: ""}",
                                                        selectedAssignee.department,
                                                        selectedAssignee.id,
                                                        mgrTaskPriority
                                                    )
                                                    mgrTaskTitle = ""
                                                    Toast.makeText(context, "Task assigned to ${selectedAssignee.name}", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Enter task title first", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA))
                                        ) {
                                            Text("Assign", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text("Team Attendance & Performance (${staffMembers.count { it.attendanceStatus == "Present" }}/${staffMembers.size} Present)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = TextPrimary)
                        }

                        items(staffMembers, key = { "mgr_${it.id}" }) { emp ->
                            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${emp.name} (${emp.staffCode})", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                            Text("${emp.department.title} • Tasks: ${emp.tasksCompleted} • Score: ${emp.performanceScore}%", fontSize = 11.sp, color = TextSecondary)
                                        }
                                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                                            Text(emp.attendanceStatus, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(onClick = { onCheckInOutStaff(emp.id, "Present") }, contentPadding = PaddingValues(horizontal = 10.dp), modifier = Modifier.height(32.dp)) {
                                            Text("Present", fontSize = 10.5.sp)
                                        }
                                        OutlinedButton(onClick = { onCheckInOutStaff(emp.id, "On Break") }, contentPadding = PaddingValues(horizontal = 10.dp), modifier = Modifier.height(32.dp)) {
                                            Text("Break", fontSize = 10.5.sp)
                                        }
                                        OutlinedButton(onClick = { onSelectStaff(emp.id) }, contentPadding = PaddingValues(horizontal = 10.dp), modifier = Modifier.height(32.dp)) {
                                            Text("Inspect Desk", fontSize = 10.5.sp)
                                        }
                                    }
                                }
                            }
                        }

                        if (activityHistory.isNotEmpty()) {
                            item {
                                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Recent Security & Work Activity", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                        activityHistory.take(6).forEach { act ->
                                            Text("• [${act.roleBadge}] ${act.actorName}: ${act.action} (${act.timestamp})", fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Staff PIN Login / Quick Switch Dialog
    if (showPinSwitchDialog) {
        var codeInput by remember { mutableStateOf(currentStaff?.staffCode ?: "EMP-GD-101") }
        var pinInput by remember { mutableStateOf("") }
        var pinErr by remember { mutableStateOf<String?>(null) }

        Dialog(onDismissRequest = { showPinSwitchDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Staff PIN Login (कर्मचारी लॉगिन)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = deptColor)
                    Text("Use ID (MGR-GD-100 / EMP-GD-101..105) + PIN (0000 / 1111..5555)", fontSize = 11.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it; pinErr = null },
                        label = { Text("Staff Code or Mobile") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it; pinErr = null },
                        label = { Text("4-Digit PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    pinErr?.let { Text(it, color = Color(0xFFDC2626), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showPinSwitchDialog = false }) { Text("Close") }
                        Spacer(Modifier.width(6.dp))
                        Button(
                            onClick = {
                                val (ok, msg) = onVerifyStaffPin(codeInput, pinInput)
                                if (ok) {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    showPinSwitchDialog = false
                                } else {
                                    pinErr = msg
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = deptColor)
                        ) {
                            Text("Verify & Switch", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RestrictedPermissionCard(permissionTitle: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        border = BorderStroke(1.dp, Color(0xFFFECACA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFDC2626))
            Column {
                Text("🔒 Restricted by Owner Permission", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF991B1B))
                Text("$permissionTitle is disabled for this Staff ID. Ask Owner (मालिक) in Admin -> Staff & Kaam.", fontSize = 11.sp, color = Color(0xFFB91C1C))
            }
        }
    }
}

@Composable
private fun EmptyDeptCard(message: String) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
            Text(message, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun RoleDivisionMatrixCard() {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("9-Account E-Commerce Business Division", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = TextPrimary)
            RoleMatrixRow(
                badge = "OWNER / SUPER ADMIN (मालिक)",
                color = Color(0xFFB45309),
                summary = "Master P&L • Staff Hiring, Salaries, RBAC Permissions & Task Assign • Payout Release • Commission % • Payment Gateways • Legal Policies"
            )
            RoleMatrixRow(
                badge = "MANAGER & KARAMCHARI (मैनेजर व कर्मचारी)",
                color = Color(0xFF7C3AED),
                summary = "1) Dept Manager (Team Attendance & Escalations) • 2) Order Packer • 3) Delivery Rider • 4) Catalog Staff • 5) Support Care • 6) Accountant (GST)"
            )
            RoleMatrixRow(
                badge = "SELLER & CUSTOMER (विक्रेता व ग्राहक)",
                color = MeeshoMagenta,
                summary = "Seller: Bulk Upload, SKU/QR/Barcode, Quality Score & Wallet • Customer: AI Search, Recently Viewed, Similar Items, Live Chat & Map Tracking"
            )
        }
    }
}

@Composable
private fun RoleMatrixRow(badge: String, color: Color, summary: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.14f)) {
            Text(badge, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = color, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        }
        Text(summary, fontSize = 11.sp, color = TextSecondary)
    }
}
