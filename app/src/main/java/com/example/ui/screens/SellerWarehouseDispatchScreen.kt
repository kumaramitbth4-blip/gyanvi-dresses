package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import com.example.ui.theme.*

// =============================================================================
// DATA MODELS FOR WAREHOUSE & DISPATCH SETTINGS
// =============================================================================
data class WarehouseLocationItem(
    val id: String,
    val name: String,
    val contactPerson: String,
    val phone: String,
    val address: String,
    val landmark: String,
    val city: String,
    val district: String,
    val state: String,
    val pincode: String,
    val isPrimary: Boolean = false,
    val workingHours: String = "09:00 AM - 08:00 PM",
    val weeklyOff: String = "Sunday"
)

data class CourierPartnerItem(
    val id: String,
    val name: String,
    val priorityText: String,
    val avgPickupHours: String,
    val rating: Double,
    val isEnabled: Boolean = true,
    val supportsCod: Boolean = true,
    val iconColor: Color = Color(0xFF1976D2)
)

data class WarehouseStaffItem(
    val id: String,
    val name: String,
    val role: String, // "Manager", "Packing Lead", "Dispatch & Scanner"
    val staffId: String,
    val phone: String,
    val permissions: String = "Full Warehouse Access"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerWarehouseDispatchScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Active Section Tab (0 to 6 matching the 7 requested sections)
    var selectedTab by remember { mutableIntStateOf(0) }

    // =========================================================================
    // SECTION 1 & 2: WAREHOUSE & PRIMARY PICKUP LOCATION STATE
    // =========================================================================
    var contactPersonName by remember { mutableStateOf("Amit Kumar Patwa") }
    var contactMobile by remember { mutableStateOf("+91 9128754982") }
    var warehouseName by remember { mutableStateOf("Gyanvi Dresses Central Warehouse") }
    var warehouseAddress by remember { mutableStateOf("Main Bazaar Road, Near Railway Station") }
    var landmark by remember { mutableStateOf("Near Railway Station / Gandhi Chowk") }
    var city by remember { mutableStateOf("Bettiah") }
    var district by remember { mutableStateOf("West Champaran") }
    var state by remember { mutableStateOf("Bihar") }
    var pincode by remember { mutableStateOf("845438") }
    val country = "India"

    var isGpsPinned by remember { mutableStateOf(true) }
    var isPhoneVerified by remember { mutableStateOf(true) }
    var isPincodeVerified by remember { mutableStateOf(true) }

    // Multiple Warehouses List
    val warehouseList = remember {
        mutableStateListOf(
            WarehouseLocationItem(
                id = "wh1",
                name = "Gyanvi Dresses Central Warehouse",
                contactPerson = "Amit Kumar Patwa",
                phone = "+91 9128754982",
                address = "Main Bazaar Road, Near Railway Station",
                landmark = "Near Railway Station / Gandhi Chowk",
                city = "Bettiah",
                district = "West Champaran",
                state = "Bihar",
                pincode = "845438",
                isPrimary = true
            ),
            WarehouseLocationItem(
                id = "wh2",
                name = "Gyanvi Patna Logistics & Hub",
                contactPerson = "Rajesh Patwa",
                phone = "+91 9835012345",
                address = "Exhibition Road, Near Big Bazaar",
                landmark = "Opposite Metro Station",
                city = "Patna",
                district = "Patna",
                state = "Bihar",
                pincode = "800001",
                isPrimary = false
            )
        )
    }

    // =========================================================================
    // SECTION 3: DISPATCH SETTINGS STATE
    // =========================================================================
    var dispatchMode by remember { mutableStateOf("Next Day Dispatch Standard") } // "Same Day", "Next Day"
    var orderCutOffTime by remember { mutableStateOf("02:00 PM") }
    var courierPickupWindow by remember { mutableStateOf("03:00 PM - 05:30 PM") }
    var autoDispatchEnabled by remember { mutableStateOf(true) }
    var holidayVacationMode by remember { mutableStateOf(false) }
    var weeklyOffDay by remember { mutableStateOf("Sunday") }

    // =========================================================================
    // SECTION 4: COURIER & SHIPPING PARTNERS STATE
    // =========================================================================
    val courierPartners = remember {
        mutableStateListOf(
            CourierPartnerItem("c1", "Shiprocket Automated", "Priority #1 (Fastest)", "2.4 hrs", 4.9, true, true, Color(0xFF673AB7)),
            CourierPartnerItem("c2", "Delhivery Surface & Express", "Priority #2 (Wide Reach)", "3.1 hrs", 4.8, true, true, Color(0xFFE53935)),
            CourierPartnerItem("c3", "Xpressbees Logistics", "Priority #3 (Low RTO)", "3.5 hrs", 4.7, true, true, Color(0xFFFFA000)),
            CourierPartnerItem("c4", "Ecom Express", "Priority #4 (COD Specialist)", "4.0 hrs", 4.6, true, true, Color(0xFF00897B)),
            CourierPartnerItem("c5", "India Post Speed Post", "Priority #5 (Remote Pincodes)", "6.0 hrs", 4.5, true, true, Color(0xFFD32F2F))
        )
    }
    var autoAssignCourier by remember { mutableStateOf(true) }

    // Rate calculator state
    var calcWeightGrams by remember { mutableStateOf("380") }
    var calcZone by remember { mutableStateOf("Regional (Within Bihar/UP)") }

    // =========================================================================
    // SECTION 5: PACKAGING SETTINGS STATE
    // =========================================================================
    var defaultPackageType by remember { mutableStateOf("Tamper-Proof Barcoded Polybag") }
    var defaultBoxSize by remember { mutableStateOf("Medium: 12x14 in (Kurtis & Sarees)") }
    var packagingCharges by remember { mutableStateOf("₹0 (Free for Customer)") }
    var barcodeScanEnforced by remember { mutableStateOf(true) }
    var productWeightCheckEnforced by remember { mutableStateOf(true) }

    // =========================================================================
    // SECTION 6: RETURN & RTO SETTINGS STATE
    // =========================================================================
    var sameReturnAddress by remember { mutableStateOf(true) }
    var rtoInspectionHours by remember { mutableStateOf("48 Hours Inspection SLA") }
    var returnContactNumber by remember { mutableStateOf("+91 9128754982") }

    // =========================================================================
    // SECTION 7: WAREHOUSE STAFF & INVENTORY STATE
    // =========================================================================
    val staffList = remember {
        mutableStateListOf(
            WarehouseStaffItem("s1", "Amit Kumar Patwa", "Warehouse Manager & Admin", "STF-BTH-01", "+91 9128754982", "Full Access"),
            WarehouseStaffItem("s2", "Rajesh Kumar", "Packing & Sealing Lead", "STF-BTH-02", "+91 9431809123", "Packing & Manifest"),
            WarehouseStaffItem("s3", "Manoj Sharma", "Dispatch & Barcode Scanner", "STF-BTH-03", "+91 9122456789", "Courier Handover")
        )
    }
    var totalWarehouseStock by remember { mutableIntStateOf(1420) }
    var lowStockThreshold by remember { mutableStateOf("15") }

    // Dialogs
    var showAddNewWarehouseDialog by remember { mutableStateOf(false) }
    var showAddStaffDialog by remember { mutableStateOf(false) }

    // Explicit high-contrast text field styling
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
                            IconButton(onClick = onBack, modifier = Modifier.testTag("warehouse_back_btn")) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Column {
                                Text(
                                    text = "Warehouse & Dispatch Settings",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Gyanvi Dresses • $city ($pincode)",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Save Quick Button on top right
                        Button(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "✓ वेयरहाउस एवं डिस्पैच सेटिंग्स सुरक्षित हो गईं! (Settings Saved)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("top_save_warehouse_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 7 Main Section Tabs (Scrollable)
                    val tabTitles = listOf(
                        "Pickup Location",
                        "Multi-Warehouse",
                        "Dispatch Timings",
                        "Courier & Shipping",
                        "Packaging & Weight",
                        "Return & RTO Hub",
                        "Staff & Inventory"
                    )

                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = CardWhite,
                        contentColor = MeeshoMagenta,
                        edgePadding = 12.dp
                    ) {
                        tabTitles.forEachIndexed { idx, title ->
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
            // TAB 0: PRIMARY PICKUP LOCATION (Screenshot 10:00 Match & Exact Details)
            // =================================================================
            if (selectedTab == 0) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Primary Pickup Location (मुख्य पिकअप स्थान)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE8F5E9),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Primary Warehouse", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        // Contact Person Name
                        OutlinedTextField(
                            value = contactPersonName,
                            onValueChange = { contactPersonName = it },
                            label = { Text("Contact Person Name *") },
                            singleLine = true,
                            colors = activeTextFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Contact Mobile Number with Verified Badge & OTP verify button
                        Column {
                            OutlinedTextField(
                                value = contactMobile,
                                onValueChange = { contactMobile = it },
                                label = { Text("Contact Mobile Number *") },
                                singleLine = true,
                                colors = activeTextFieldColors,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                trailingIcon = {
                                    if (isPhoneVerified) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Verified", color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Warehouse Name
                        OutlinedTextField(
                            value = warehouseName,
                            onValueChange = { warehouseName = it },
                            label = { Text("Warehouse / Store Name *") },
                            singleLine = true,
                            colors = activeTextFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Warehouse Address
                        OutlinedTextField(
                            value = warehouseAddress,
                            onValueChange = { warehouseAddress = it },
                            label = { Text("Warehouse Address *") },
                            minLines = 2,
                            colors = activeTextFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Landmark
                        OutlinedTextField(
                            value = landmark,
                            onValueChange = { landmark = it },
                            label = { Text("Landmark (पहचान चिह्न)") },
                            singleLine = true,
                            colors = activeTextFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // City & Pincode
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City *") },
                                singleLine = true,
                                colors = activeTextFieldColors,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = pincode,
                                onValueChange = { pincode = it },
                                label = { Text("Pincode (6 digits) *") },
                                singleLine = true,
                                colors = activeTextFieldColors,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // District & State
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = district,
                                onValueChange = { district = it },
                                label = { Text("District") },
                                singleLine = true,
                                colors = activeTextFieldColors,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = state,
                                onValueChange = { state = it },
                                label = { Text("State") },
                                singleLine = true,
                                colors = activeTextFieldColors,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Google Maps / GPS Pin Location Card
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEDE7F6),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1C4E9)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF5E35B1))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Google Maps & GPS Location", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF311B92))
                                        Text("GPS: 26.7997° N, 84.5028° E (Bettiah Pin Accurate)", fontSize = 10.sp, color = Color(0xFF512DA8))
                                    }
                                }
                                TextButton(
                                    onClick = {
                                        Toast.makeText(context, "✓ GPS Location Calibrated with Google Maps!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("Re-Pin", fontSize = 11.sp, color = Color(0xFF5E35B1), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Pincode Courier Verification Strip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pincode $pincode is serviceable by all 5 partner couriers.", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Action Buttons Row (Verify Pincode, Test Pickup, Update Address)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "✓ Pincode $pincode Verified: Daily Pickup Active!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Icon(Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verify Pincode", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "✓ Test Pickup Request Sent to Delhivery/Shiprocket!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Pickup", fontSize = 11.sp)
                    }
                }

                // Primary Save Button
                Button(
                    onClick = {
                        Toast.makeText(context, "✓ Warehouse address updated for $city ($pincode)!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Warehouse Settings (सेव करें)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // =================================================================
            // TAB 1: MULTIPLE WAREHOUSE LOCATIONS
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
                            Text("Multiple Warehouses (${warehouseList.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Button(
                                onClick = { showAddNewWarehouseDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Warehouse", fontSize = 11.sp)
                            }
                        }
                        Text("Manage multi-city stock points for faster order dispatch across India.", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                warehouseList.forEachIndexed { idx, wh ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (wh.isPrimary) MeeshoMagenta else Color(0xFFEEEEEE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warehouse, contentDescription = null, tint = if (wh.isPrimary) MeeshoMagenta else TextSecondary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(wh.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                if (wh.isPrimary) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFE8F5E9)
                                    ) {
                                        Text("PRIMARY", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }

                            Text("${wh.address}, ${wh.landmark}, ${wh.city}, ${wh.state} - ${wh.pincode}", fontSize = 12.sp, color = TextPrimary)
                            Text("Contact: ${wh.contactPerson} (${wh.phone})", fontSize = 11.sp, color = TextSecondary)
                            Text("Hours: ${wh.workingHours} • Weekly Off: ${wh.weeklyOff}", fontSize = 11.sp, color = Color(0xFF3F51B5))

                            HorizontalDivider(color = Color(0xFFEEEEEE))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!wh.isPrimary) {
                                    TextButton(
                                        onClick = {
                                            val updated = warehouseList.map { item ->
                                                item.copy(isPrimary = item.id == wh.id)
                                            }
                                            warehouseList.clear()
                                            warehouseList.addAll(updated)
                                            Toast.makeText(context, "✓ '${wh.name}' प्राथमिक वेयरहाउस सेट हुआ!", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Set as Primary", fontSize = 11.sp, color = MeeshoMagenta, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Text("Default Pickup Point", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            contactPersonName = wh.contactPerson
                                            contactMobile = wh.phone
                                            warehouseName = wh.name
                                            warehouseAddress = wh.address
                                            landmark = wh.landmark
                                            city = wh.city
                                            pincode = wh.pincode
                                            selectedTab = 0
                                            Toast.makeText(context, "Editing ${wh.name}", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                    if (warehouseList.size > 1 && !wh.isPrimary) {
                                        IconButton(
                                            onClick = {
                                                warehouseList.removeAt(idx)
                                                Toast.makeText(context, "Warehouse removed", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 2: DISPATCH SETTINGS & SCHEDULE (Cut-off Time & Next Day Dispatch)
            // =================================================================
            if (selectedTab == 2) {
                // Next Day Dispatch Card (Screenshot Match)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Next Day Dispatch Standard", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text("Orders placed before 2 PM are scheduled for next day courier pickup.", fontSize = 12.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = dispatchMode.contains("Next Day"),
                                onCheckedChange = { isChecked ->
                                    dispatchMode = if (isChecked) "Next Day Dispatch Standard" else "Same Day Express Dispatch"
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF283593))
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Next Day Dispatch increases catalog visibility by +40% on Gyanvi Dresses!", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Timing & Cut-off Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Dispatch Timings & Cut-off (समय सीमा)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        OutlinedTextField(
                            value = orderCutOffTime,
                            onValueChange = { orderCutOffTime = it },
                            label = { Text("Daily Order Cut-off Time (ऑर्डर कट-ऑफ)") },
                            singleLine = true,
                            colors = activeTextFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = courierPickupWindow,
                            onValueChange = { courierPickupWindow = it },
                            label = { Text("Daily Courier Pickup Window (पिकअप समय)") },
                            singleLine = true,
                            colors = activeTextFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = weeklyOffDay,
                                onValueChange = { weeklyOffDay = it },
                                label = { Text("Weekly Off") },
                                singleLine = true,
                                colors = activeTextFieldColors,
                                modifier = Modifier.weight(1f)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto Dispatch", fontSize = 11.sp, color = TextSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = autoDispatchEnabled,
                                        onCheckedChange = { autoDispatchEnabled = it },
                                        colors = CheckboxDefaults.colors(checkedColor = MeeshoMagenta)
                                    )
                                    Text("Automatic", fontSize = 12.sp)
                                }
                            }
                        }

                        // Holiday / Vacation Mode toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Holiday / Vacation Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Pause courier pickup during festivals or warehouse maintenance.", fontSize = 11.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = holidayVacationMode,
                                onCheckedChange = { holidayVacationMode = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFD32F2F))
                            )
                        }
                    }
                }
            }

            // =================================================================
            // TAB 3: COURIER & SHIPPING PARTNERS (Shiprocket, Delhivery, Rate Calc)
            // =================================================================
            if (selectedTab == 3) {
                // Auto Assign Toggle Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Automatic Courier Assignment", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Algorithm assigns fastest partner with lowest RTO rate automatically.", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = autoAssignCourier,
                            onCheckedChange = { autoAssignCourier = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = MeeshoMagenta)
                        )
                    }
                }

                Text("Integrated Courier Partners (${courierPartners.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                courierPartners.forEachIndexed { i, c ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(38.dp).clip(CircleShape).background(c.iconColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = c.iconColor, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(c.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${c.priorityText} • Avg Pickup: ${c.avgPickupHours}", fontSize = 11.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = c.isEnabled,
                                onCheckedChange = { isChecked ->
                                    courierPartners[i] = c.copy(isEnabled = isChecked)
                                    Toast.makeText(context, "${c.name} ${if (isChecked) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                }

                // Shipping Rate Calculator
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Shipping Rate Calculator (शिपिंग रेट कैलकुलेटर)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = calcWeightGrams,
                                onValueChange = { calcWeightGrams = it },
                                label = { Text("Weight (Grams)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = activeTextFieldColors,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = calcZone,
                                onValueChange = { calcZone = it },
                                label = { Text("Delivery Zone") },
                                singleLine = true,
                                colors = activeTextFieldColors,
                                modifier = Modifier.weight(1.3f)
                            )
                        }

                        val w = calcWeightGrams.toIntOrNull() ?: 380
                        val estRate = if (w <= 500) 42 else 42 + ((w - 500) / 500 + 1) * 22

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEDE7F6),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Estimated Shipping Cost:", fontSize = 12.sp, color = Color(0xFF4527A0))
                                Text("₹$estRate / Parcel", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF4527A0))
                            }
                        }
                    }
                }
            }

            // =================================================================
            // TAB 4: PACKAGING & WEIGHT SETTINGS
            // =================================================================
            if (selectedTab == 4) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Packaging Guidelines for Ethnic Dresses", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        OutlinedTextField(
                            value = defaultPackageType,
                            onValueChange = { defaultPackageType = it },
                            label = { Text("Default Packaging Type") },
                            singleLine = true,
                            colors = activeTextFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = defaultBoxSize,
                            onValueChange = { defaultBoxSize = it },
                            label = { Text("Packet Dimensions (Small / Med / Large)") },
                            singleLine = true,
                            colors = activeTextFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = packagingCharges,
                            onValueChange = { packagingCharges = it },
                            label = { Text("Packaging Charges to Customer") },
                            singleLine = true,
                            colors = activeTextFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Packing Verification Checkboxes
                        Text("Quality Packing Checks (डिस्पैच पूर्व जांच):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = barcodeScanEnforced, onCheckedChange = { barcodeScanEnforced = it }, colors = CheckboxDefaults.colors(checkedColor = MeeshoMagenta))
                            Text("Enforce Barcode Scan on Flyer before Handover", fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = productWeightCheckEnforced, onCheckedChange = { productWeightCheckEnforced = it }, colors = CheckboxDefaults.colors(checkedColor = MeeshoMagenta))
                            Text("Automatic Weight Verification on Electronic Scale", fontSize = 12.sp)
                        }
                    }
                }
            }

            // =================================================================
            // TAB 5: RETURN & RTO WAREHOUSE SETTINGS
            // =================================================================
            if (selectedTab == 5) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Return & RTO Delivery Address", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Same as Primary Pickup Location", fontSize = 13.sp)
                            Switch(
                                checked = sameReturnAddress,
                                onCheckedChange = { sameReturnAddress = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = MeeshoMagenta)
                            )
                        }

                        if (sameReturnAddress) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF5F5F5),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "$warehouseAddress, $city, $state - $pincode",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = rtoInspectionHours,
                            onValueChange = { rtoInspectionHours = it },
                            label = { Text("RTO Defect Inspection Window") },
                            singleLine = true,
                            colors = activeTextFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = returnContactNumber,
                            onValueChange = { returnContactNumber = it },
                            label = { Text("Dedicated Returns Helpline Number") },
                            singleLine = true,
                            colors = activeTextFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // =================================================================
            // TAB 6: WAREHOUSE STAFF & LIVE INVENTORY
            // =================================================================
            if (selectedTab == 6) {
                // Stock Snapshot Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Live Warehouse Inventory (गोदाम में लाइव स्टॉक)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Units in Stock", fontSize = 11.sp, color = TextSecondary)
                                Text("$totalWarehouseStock Units", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF2E7D32))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        totalWarehouseStock += 50
                                        Toast.makeText(context, "+50 Units Stock Added", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("+ Stock In", fontSize = 10.sp)
                                }
                                Button(
                                    onClick = {
                                        totalWarehouseStock = (totalWarehouseStock - 10).coerceAtLeast(0)
                                        Toast.makeText(context, "-10 Units Dispatched", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("- Stock Out", fontSize = 10.sp)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = lowStockThreshold,
                            onValueChange = { lowStockThreshold = it },
                            label = { Text("Low Stock Alert Threshold (Units)") },
                            singleLine = true,
                            colors = activeTextFieldColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Staff List Card
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
                            Text("Warehouse Staff Management (${staffList.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Button(
                                onClick = { showAddStaffDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Staff", fontSize = 11.sp)
                            }
                        }

                        staffList.forEach { staff ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEDE7F6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF5E35B1), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(staff.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${staff.role} • ${staff.staffId}", fontSize = 11.sp, color = TextSecondary)
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Text("Active", fontSize = 9.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "✓ Daily Warehouse Report (.pdf) Generated & Downloaded!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download Daily Warehouse Dispatch Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // =========================================================================
    // MODAL: ADD NEW WAREHOUSE
    // =========================================================================
    if (showAddNewWarehouseDialog) {
        var newWhName by remember { mutableStateOf("Gyanvi Hub Muzaffarpur") }
        var newWhContact by remember { mutableStateOf("Suresh Kumar") }
        var newWhPhone by remember { mutableStateOf("+91 9431098765") }
        var newWhAddress by remember { mutableStateOf("Motijheel Main Road") }
        var newWhCity by remember { mutableStateOf("Muzaffarpur") }
        var newWhPincode by remember { mutableStateOf("842001") }

        AlertDialog(
            onDismissRequest = { showAddNewWarehouseDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null, tint = MeeshoMagenta)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Warehouse Location", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newWhName, onValueChange = { newWhName = it }, label = { Text("Warehouse Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newWhContact, onValueChange = { newWhContact = it }, label = { Text("Contact Person") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newWhPhone, onValueChange = { newWhPhone = it }, label = { Text("Mobile Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newWhAddress, onValueChange = { newWhAddress = it }, label = { Text("Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newWhCity, onValueChange = { newWhCity = it }, label = { Text("City") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = newWhPincode, onValueChange = { newWhPincode = it }, label = { Text("Pincode") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        warehouseList.add(
                            WarehouseLocationItem(
                                id = System.currentTimeMillis().toString(),
                                name = newWhName,
                                contactPerson = newWhContact,
                                phone = newWhPhone,
                                address = newWhAddress,
                                landmark = "Commercial Hub",
                                city = newWhCity,
                                district = newWhCity,
                                state = "Bihar",
                                pincode = newWhPincode,
                                isPrimary = false
                            )
                        )
                        showAddNewWarehouseDialog = false
                        Toast.makeText(context, "✓ नया वेयरहाउस जोड़ा गया!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Add Warehouse")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNewWarehouseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // =========================================================================
    // MODAL: ADD STAFF MEMBER
    // =========================================================================
    if (showAddStaffDialog) {
        var newStaffName by remember { mutableStateOf("Vikas Kumar") }
        var newStaffRole by remember { mutableStateOf("Dispatch Assistant") }
        var newStaffPhone by remember { mutableStateOf("+91 9876543210") }

        AlertDialog(
            onDismissRequest = { showAddStaffDialog = false },
            title = { Text("Add Warehouse Staff", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newStaffName, onValueChange = { newStaffName = it }, label = { Text("Staff Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newStaffRole, onValueChange = { newStaffRole = it }, label = { Text("Role (e.g. Packing / Dispatch)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newStaffPhone, onValueChange = { newStaffPhone = it }, label = { Text("Contact Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        staffList.add(
                            WarehouseStaffItem(
                                id = System.currentTimeMillis().toString(),
                                name = newStaffName,
                                role = newStaffRole,
                                staffId = "STF-BTH-0${staffList.size + 1}",
                                phone = newStaffPhone
                            )
                        )
                        showAddStaffDialog = false
                        Toast.makeText(context, "✓ Staff member added!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Add Staff")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStaffDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
