package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AccountType
import com.example.data.local.UserAccountEntity
import com.example.ui.theme.*

data class SellerProduct(
    val id: String,
    val title: String,
    val price: Int,
    val mrp: Int,
    val category: String,
    val inStock: Boolean,
    val ordersCount: Int
)

data class PendingDispatchOrder(
    val orderId: String,
    val customerName: String,
    val city: String,
    val item: String,
    val price: Int,
    var isDispatched: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    initialTab: Int = 0, // 0 = Profile Details, 1 = Seller Panel
    currentUser: UserAccountEntity? = null,
    onOpenUserAuth: (accountType: String, isRegisterMode: Boolean) -> Unit = { _, _ -> },
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }

    // Profile Details Editable State (synced with logged-in user)
    var fullName by remember(currentUser?.id) { mutableStateOf(currentUser?.fullName ?: "Amit Kumar Patwa") }
    var phoneNumber by remember(currentUser?.id) {
        mutableStateOf(
            if (!currentUser?.mobileNumber.isNullOrBlank()) "+91 ${currentUser?.mobileNumber}" else "+91 9128754982"
        )
    }
    var emailAddress by remember(currentUser?.id) {
        mutableStateOf(currentUser?.email?.ifBlank { "kumaramitbth4@gmail.com" } ?: "kumaramitbth4@gmail.com")
    }
    var selectedGender by remember { mutableStateOf("Male") }
    var selectedLanguage by remember { mutableStateOf("Hindi & English") }
    var businessName by remember(currentUser?.id) {
        mutableStateOf(currentUser?.shopName?.ifBlank { "Gyanvi Dresses Boutique" } ?: "Gyanvi Dresses Boutique")
    }
    var gstNumber by remember(currentUser?.id) {
        mutableStateOf(currentUser?.gstin?.ifBlank { "10ABCDE1234F1Z5" } ?: "10ABCDE1234F1Z5")
    }
    var addressLine by remember(currentUser?.id) {
        mutableStateOf(
            currentUser?.businessAddress?.ifBlank {
                "Ward No. 12, Main Road, Near City Plaza, Bettiah, West Champaran, Bihar - 845438"
            } ?: "Ward No. 12, Main Road, Near City Plaza, Bettiah, West Champaran, Bihar - 845438"
        )
    }

    // Seller Panel State
    var showAddProductDialog by remember { mutableStateOf(false) }
    var newProdName by remember { mutableStateOf("") }
    var newProdPrice by remember { mutableStateOf("") }
    var newProdMrp by remember { mutableStateOf("") }
    var newProdCategory by remember { mutableStateOf("Kurtis") }

    var sellerProducts by remember {
        mutableStateOf(
            listOf(
                SellerProduct("sp_1", "Embroidered Anarkali Kurti Set with Dupatta", 449, 1299, "Kurtis", true, 42),
                SellerProduct("sp_2", "Pure Banarasi Soft Silk Festive Saree", 499, 1599, "Sarees", true, 38),
                SellerProduct("sp_3", "Heavy Bridal Velvet Lehenga Choli", 999, 2999, "Lehenga", true, 19),
                SellerProduct("sp_4", "Casual Rayon Floral Print Kurti", 227, 499, "Kurtis", true, 25)
            )
        )
    }

    var pendingOrders by remember {
        mutableStateOf(
            listOf(
                PendingDispatchOrder("#ORD-BTH-9021", "Pooja Verma", "Patna, Bihar", "Embroidered Anarkali (Size L)", 449),
                PendingDispatchOrder("#ORD-BTH-9022", "Sneha Kumari", "Muzaffarpur", "Banarasi Silk Saree (Red)", 499),
                PendingDispatchOrder("#ORD-BTH-9023", "Ritu Raj", "Darbhanga", "Velvet Lehenga Choli (M)", 999),
                PendingDispatchOrder("#ORD-BTH-9024", "Anjali Singh", "Gaya, Bihar", "Rayon Floral Kurti (XL)", 227)
            )
        )
    }

    Scaffold(
        topBar = {
            Surface(
                color = CardWhite,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("profile_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = if (selectedTab == 0) "Profile Details" else "Supplier / Seller Panel",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Switch Mode Chip
                        AssistChip(
                            onClick = { selectedTab = if (selectedTab == 0) 1 else 0 },
                            label = {
                                Text(
                                    text = if (selectedTab == 0) "Open Seller Hub" else "View Profile",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MeeshoMagenta
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Default.Storefront else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MeeshoMagenta,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFF3E5F5)),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    // 2 Navigation Tabs: Profile Details & Seller Panel
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = CardWhite,
                        contentColor = MeeshoMagenta
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    "Profile Details",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) MeeshoMagenta else TextSecondary
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profile",
                                    tint = if (selectedTab == 0) MeeshoMagenta else TextSecondary
                                )
                            }
                        )

                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    "Seller Panel (सप्लायर)",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) MeeshoMagenta else TextSecondary
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = "Seller Hub",
                                    tint = if (selectedTab == 1) MeeshoMagenta else TextSecondary
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA),
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (selectedTab == 0) {
            // ==================== TAB 0: PROFILE DETAILS ====================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Top Avatar & Bio Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(86.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFE0B2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ProfileMaleAvatar(modifier = Modifier.fillMaxSize())
                                }

                                // Camera Change Icon
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MeeshoMagenta)
                                        .clickable {
                                            Toast.makeText(context, "Choose photo from Gallery or Camera", Toast.LENGTH_SHORT).show()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Change Photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = fullName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Text(
                                text = businessName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MeeshoMagenta,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentUser?.accountType == AccountType.SELLER) {
                                        "Verified Seller Account • Password Protected"
                                    } else {
                                        "Verified Customer Account • Password Protected"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { onOpenUserAuth(AccountType.CUSTOMER, true) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Switch / Register Customer", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { onOpenUserAuth(AccountType.SELLER, true) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Register Seller", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Personal Details Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Personal Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MeeshoMagenta) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF111111),
                                    unfocusedTextColor = Color(0xFF111111)
                                )
                            )

                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Phone Number") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MeeshoMagenta) },
                                trailingIcon = {
                                    Text("Verified", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF111111),
                                    unfocusedTextColor = Color(0xFF111111)
                                )
                            )

                            OutlinedTextField(
                                value = emailAddress,
                                onValueChange = { emailAddress = it },
                                label = { Text("Email ID") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MeeshoMagenta) },
                                trailingIcon = {
                                    Text("Verified", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF111111),
                                    unfocusedTextColor = Color(0xFF111111)
                                )
                            )

                            // Gender Chips
                            Column {
                                Text("Gender", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Male", "Female", "Other").forEach { gender ->
                                        FilterChip(
                                            selected = selectedGender == gender,
                                            onClick = { selectedGender = gender },
                                            label = { Text(gender) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MeeshoMagenta,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Boutique / Business Details Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Boutique & Business Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            OutlinedTextField(
                                value = businessName,
                                onValueChange = { businessName = it },
                                label = { Text("Shop / Boutique Name") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = MeeshoMagenta) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF111111),
                                    unfocusedTextColor = Color(0xFF111111)
                                )
                            )

                            OutlinedTextField(
                                value = gstNumber,
                                onValueChange = { gstNumber = it },
                                label = { Text("GSTIN / Business Registration") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MeeshoMagenta) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF111111),
                                    unfocusedTextColor = Color(0xFF111111)
                                )
                            )

                            OutlinedTextField(
                                value = addressLine,
                                onValueChange = { addressLine = it },
                                label = { Text("Boutique / Delivery Address") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MeeshoMagenta) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF111111),
                                    unfocusedTextColor = Color(0xFF111111)
                                )
                            )
                        }
                    }
                }

                // Save Changes Button
                item {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Profile details updated successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_profile_button")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Profile Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            // ==================== TAB 1: SELLER PANEL (सप्लायर व सेलर हब) ====================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Seller Store Header Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF3E5F5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = MeeshoMagenta,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = businessName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Seller ID: #GYN-845438 • Bettiah, Bihar",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "0% Commission",
                                        color = Color(0xFF2E7D32),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            Spacer(modifier = Modifier.height(14.dp))

                            // 4 Key Business Metrics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricItem(title = "Total Sales", value = "₹48,250", color = Color(0xFF1B5E20))
                                MetricItem(title = "Orders", value = "128", color = MeeshoMagenta)
                                MetricItem(title = "To Dispatch", value = "${pendingOrders.count { !it.isDispatched }}", color = Color(0xFFE65100))
                                MetricItem(title = "Next Payout", value = "₹12,400", color = Color(0xFF0D47A1))
                            }
                        }
                    }
                }

                // Add New Product Action Button
                item {
                    Button(
                        onClick = { showAddProductDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("add_product_button")
                    ) {
                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+ Add New Dress / Product to Shop", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // Pending Orders for Dispatch Section
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Orders to Dispatch (${pendingOrders.count { !it.isDispatched }})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )

                                Text(
                                    text = "Express Pickup",
                                    fontSize = 11.sp,
                                    color = MeeshoMagenta,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            pendingOrders.forEach { order ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(order.orderId, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                        Text("${order.customerName} • ${order.city}", fontSize = 12.sp, color = TextSecondary)
                                        Text("${order.item} • ₹${order.price}", fontSize = 12.sp, color = MeeshoMagenta, fontWeight = FontWeight.Medium)
                                    }

                                    if (order.isDispatched) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFE8F5E9))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("Shipped ✓", color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                order.isDispatched = true
                                                Toast.makeText(context, "${order.orderId} dispatched! Shipping label printed.", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Dispatch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFF5F5F5))
                            }
                        }
                    }
                }

                // Manage Listed Products Section
                item {
                    Text(
                        text = "My Listed Products (${sellerProducts.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                }

                items(sellerProducts) { product ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFF3E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Checkroom,
                                    contentDescription = null,
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.title,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("₹${product.price}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("MRP ₹${product.mrp}", fontSize = 11.sp, color = TextMuted)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("• ${product.ordersCount} sold", fontSize = 11.sp, color = TextSecondary)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (product.inStock) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (product.inStock) "In Stock" else "Out",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (product.inStock) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(30.dp)) }
            }
        }
    }

    // ==================== ADD PRODUCT DIALOG ====================
    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("+ Add New Dress / Product", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newProdName,
                        onValueChange = { newProdName = it },
                        label = { Text("Dress Title / Name") },
                        placeholder = { Text("e.g. Georgette Anarkali Suit") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newProdPrice,
                            onValueChange = { newProdPrice = it },
                            label = { Text("Price (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newProdMrp,
                            onValueChange = { newProdMrp = it },
                            label = { Text("MRP (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = newProdCategory,
                        onValueChange = { newProdCategory = it },
                        label = { Text("Category (Kurtis, Sarees, Lehengas)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProdName.isNotBlank() && newProdPrice.isNotBlank()) {
                            val priceVal = newProdPrice.toIntOrNull() ?: 299
                            val mrpVal = newProdMrp.toIntOrNull() ?: (priceVal * 2)
                            sellerProducts = listOf(
                                SellerProduct(
                                    id = "sp_${System.currentTimeMillis()}",
                                    title = newProdName,
                                    price = priceVal,
                                    mrp = mrpVal,
                                    category = newProdCategory,
                                    inStock = true,
                                    ordersCount = 0
                                )
                            ) + sellerProducts

                            showAddProductDialog = false
                            newProdName = ""
                            newProdPrice = ""
                            newProdMrp = ""
                            Toast.makeText(context, "Product listed in Gyanvi Dresses Boutique!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter dress title and price", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Publish to Shop")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MetricItem(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = color)
        Text(text = title, fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
private fun ProfileMaleAvatar(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Skin Tone Base
        drawCircle(
            color = Color(0xFFFFD180),
            radius = width / 2f,
            center = Offset(width / 2f, height / 2f)
        )

        // Shirt
        val shirt = Path().apply {
            moveTo(width * 0.2f, height)
            lineTo(width * 0.35f, height * 0.76f)
            lineTo(width * 0.65f, height * 0.76f)
            lineTo(width * 0.8f, height)
            close()
        }
        drawPath(shirt, color = Color(0xFF1976D2))

        // Head
        drawCircle(
            color = Color(0xFFFFCC80),
            radius = width * 0.28f,
            center = Offset(width * 0.5f, height * 0.44f)
        )

        // Hair
        val hair = Path().apply {
            moveTo(width * 0.23f, height * 0.40f)
            cubicTo(
                width * 0.23f, height * 0.12f,
                width * 0.77f, height * 0.12f,
                width * 0.77f, height * 0.40f
            )
            lineTo(width * 0.70f, height * 0.30f)
            cubicTo(
                width * 0.55f, height * 0.22f,
                width * 0.45f, height * 0.22f,
                width * 0.30f, height * 0.30f
            )
            close()
        }
        drawPath(hair, color = Color(0xFF212121))

        // Eyes
        drawCircle(color = Color(0xFF212121), radius = 2.5.dp.toPx(), center = Offset(width * 0.42f, height * 0.44f))
        drawCircle(color = Color(0xFF212121), radius = 2.5.dp.toPx(), center = Offset(width * 0.58f, height * 0.44f))

        // Eyebrows
        drawLine(
            color = Color(0xFF212121),
            start = Offset(width * 0.36f, height * 0.38f),
            end = Offset(width * 0.46f, height * 0.40f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF212121),
            start = Offset(width * 0.64f, height * 0.38f),
            end = Offset(width * 0.54f, height * 0.40f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Smile
        val smile = Path().apply {
            moveTo(width * 0.44f, height * 0.55f)
            quadraticTo(width * 0.5f, height * 0.62f, width * 0.56f, height * 0.55f)
        }
        drawPath(smile, color = Color(0xFF212121), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}
