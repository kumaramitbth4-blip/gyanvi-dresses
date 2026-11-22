package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.ProductEntity
import com.example.ui.components.AppManagedImage
import com.example.ui.theme.*

data class InventoryItem(
    val id: String,
    val title: String,
    val styleId: String,
    val catalogId: String,
    val price: Int,
    var stock: Int,
    val category: String,
    val description: String = "",
    val imageUrl: String = "",
    val imageRes: Int = R.drawable.dress_anarkali_1793417013639,
    var isPaused: Boolean = false,
    val mrp: Int = price * 2
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerInventoryScreen(
    onBack: () -> Unit,
    onNavigateToUpload: () -> Unit,
    onNavigateToHelp: () -> Unit,
    roomProducts: List<ProductEntity> = emptyList(),
    onSaveRoomProduct: (ProductEntity) -> Unit = {},
    onUpdateRoomStock: (String, Int) -> Unit = { _, _ -> },
    onTogglePauseRoomProduct: (String, Boolean) -> Unit = { _, _ -> },
    onDeleteRoomProduct: (String) -> Unit = {},
    availableCategories: List<String> = emptyList(),
    availableSubCategories: List<String> = emptyList()
) {
    val context = LocalContext.current
    val isRoomBacked = roomProducts.isNotEmpty()
    var selectedMainTab by remember { mutableIntStateOf(0) } // 0=Active, 1=Low/Out of Stock, 2=Paused/Blocked
    var selectedActiveSubTab by remember { mutableIntStateOf(0) } // 0=All Stock, 1=Out of Stock, 2=Low Stock
    var selectedBlockedSubTab by remember { mutableIntStateOf(0) }

    var showFilterDialog by remember { mutableStateOf(false) }
    var filterCategorySearch by remember { mutableStateOf("") }
    val selectedFilterCategories = remember { mutableStateListOf<String>() }

    var searchQuery by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<InventoryItem?>(null) }
    var newStockText by remember { mutableStateOf("") }

    var fullEditingItem by remember { mutableStateOf<InventoryItem?>(null) }
    var deletingItem by remember { mutableStateOf<InventoryItem?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    val defaultCategories = listOf(
        "Women", "Men", "Girls", "Boys", "Kids & Baby",
        "Saree", "Kurti & Suit", "Western Wear", "Ethnic Wear",
        "Footwear", "Jewellery", "Fashion Accessories",
        "Kurtis & Suits", "Lehengas", "Ethnic Gowns", "Sharara Sets"
    )
    val allCategories = remember(availableCategories, availableSubCategories) {
        (availableCategories + availableSubCategories + defaultCategories).distinctBy { it.lowercase() }
    }

    val fallbackInventoryList = remember {
        mutableStateListOf(
            InventoryItem("1", "Peplum Kurti & Tiered Sharara Set", "SEA-GRN-SHR-02", "453322234", 425, 9, "Sharara Sets", "Pure georgette tiered sharara festive ensemble.", isPaused = false, mrp = 1199),
            InventoryItem("2", "Girls Premium Yellow Georgette Embroidered Peplum Sharara Set", "Yellow -09", "453316827", 425, 12, "Kids Clothing", "Embroidered festive yellow georgette sharara.", isPaused = false, mrp = 1299),
            InventoryItem("3", "Girls Embroidered Sharara Set - Black Festive", "Black -emb-01", "453305717", 405, 5, "Kids Clothing", "Black sequence embroidered ethnic sharara set.", isPaused = false, mrp = 999),
            InventoryItem("4", "Kids Rayon Printed Anarkali Kurta & Pant Set", "KID-ANAR-04", "453298112", 349, 15, "Kurtis & Suits", "Breathable rayon printed flared anarkali kurti.", isPaused = false, mrp = 899),
            InventoryItem("5", "Traditional Floral Silk Blend Lehanga Choli", "LEH-FLR-22", "453289004", 599, 8, "Lehengas", "Rich Banarasi silk blend lehenga with zari border.", isPaused = false, mrp = 1999),
            InventoryItem("6", "Baby Girl Net Tiered Birthday Party Frock", "FRK-NET-07", "453276511", 499, 14, "Ethnic Gowns", "Soft satin-lined tiered net party gown.", isPaused = false, mrp = 1399),
            InventoryItem("7", "Festive Kids Heavy Gown - Royal Blue", "GWN-ROY-11", "453265430", 549, 0, "Ethnic Gowns", "Royal blue ethnic wedding reception gown.", isPaused = false, mrp = 1499),
            InventoryItem("8", "Traditional Kanjivaram Silk Woven Saree", "KAN-SAR-01", "453254399", 799, 20, "Sarees", "Authentic zari-woven Kanjivaram ethnic saree.", isPaused = false, mrp = 2499),
            InventoryItem("9", "Feeding Rayon Maternity Kurti with Zips", "MAT-KURT-03", "453243210", 499, 11, "Maternity Wear", "Soft feeding rayon ethnic kurti.", isPaused = true, mrp = 1199),
            InventoryItem("10", "Girls Designer Chikankari Kurti Set", "CHK-SET-15", "453232190", 475, 7, "Kurtis & Suits", "Lucknowi chikankari ethnic kurti with dupatta.", isPaused = true, mrp = 1299)
        )
    }

    val inventoryList: List<InventoryItem> = if (isRoomBacked) {
        roomProducts.map { entity ->
            InventoryItem(
                id = entity.id,
                title = entity.name,
                styleId = entity.sku,
                catalogId = entity.id.uppercase(),
                price = entity.sellingPrice,
                stock = entity.stock,
                category = entity.category,
                description = entity.description,
                imageUrl = entity.imageUrl,
                imageRes = if (entity.imageRes != 0) entity.imageRes else R.drawable.dress_anarkali_1793417013639,
                isPaused = entity.isPaused,
                mrp = entity.mrp
            )
        }
    } else {
        fallbackInventoryList
    }

    val activeItems = inventoryList.filter { !it.isPaused }
    val lowOrOosItems = inventoryList.filter { it.stock <= 8 }
    val pausedItems = inventoryList.filter { it.isPaused }

    val tabSourceList = when (selectedMainTab) {
        0 -> when (selectedActiveSubTab) {
            1 -> activeItems.filter { it.stock == 0 }
            2 -> activeItems.filter { it.stock in 1..8 }
            else -> activeItems
        }
        1 -> lowOrOosItems
        else -> pausedItems
    }

    val filteredList = tabSourceList.filter { item ->
        val matchesSearch = searchQuery.isEmpty() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.styleId.contains(searchQuery, ignoreCase = true) ||
                item.catalogId.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedFilterCategories.isEmpty() || selectedFilterCategories.contains(item.category)
        matchesSearch && matchesCategory
    }

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 1.dp) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Inventory", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                        }
                        Row {
                            IconButton(onClick = { showFilterDialog = true }) {
                                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = TextPrimary)
                            }
                            IconButton(onClick = onNavigateToHelp) {
                                Icon(Icons.Default.HelpOutline, contentDescription = "Help", tint = TextPrimary)
                            }
                        }
                    }

                    // Upload new catalogs banner (Matching Screenshot 11:51 & 11:53)
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📦", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add / Upload Products", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A237E))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { showAddProductDialog = true },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("+ Quick Add", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF3F51B5))
                                }
                                Button(
                                    onClick = onNavigateToUpload,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Catalog", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Top Main Tabs with live counts
                    TabRow(
                        selectedTabIndex = selectedMainTab,
                        containerColor = CardWhite,
                        contentColor = Color(0xFF3F51B5)
                    ) {
                        Tab(
                            selected = selectedMainTab == 0,
                            onClick = { selectedMainTab = 0 },
                            text = { Text("Active (${activeItems.size})", fontSize = 12.sp, fontWeight = if (selectedMainTab == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedMainTab == 1,
                            onClick = { selectedMainTab = 1 },
                            text = { Text("Low Stock (${lowOrOosItems.size})", fontSize = 12.sp, fontWeight = if (selectedMainTab == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedMainTab == 2,
                            onClick = { selectedMainTab = 2 },
                            text = { Text("Paused (${pausedItems.size})", fontSize = 12.sp, fontWeight = if (selectedMainTab == 2) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }

                    // Sub Tabs based on Main Tab
                    if (selectedMainTab == 0) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedActiveSubTab,
                            containerColor = Color(0xFFF5F5F5),
                            contentColor = Color(0xFF3F51B5),
                            edgePadding = 16.dp
                        ) {
                            listOf(
                                "All Stock (${activeItems.size})",
                                "Out of Stock (${activeItems.count { it.stock == 0 }})",
                                "Low Stock (${activeItems.count { it.stock in 1..8 }})"
                            ).forEachIndexed { idx, title ->
                                Tab(
                                    selected = selectedActiveSubTab == idx,
                                    onClick = { selectedActiveSubTab = idx },
                                    text = { Text(title, fontSize = 11.sp, fontWeight = if (selectedActiveSubTab == idx) FontWeight.Bold else FontWeight.Normal) }
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search & Filter Row
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Product Name, SKU or Catalog ID...", fontSize = 12.sp) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )
            }

            if (selectedFilterCategories.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filtered: ${selectedFilterCategories.joinToString(", ")}", fontSize = 11.sp, color = Color(0xFF3F51B5), modifier = Modifier.weight(1f))
                    TextButton(onClick = { selectedFilterCategories.clear() }) {
                        Text("Clear", fontSize = 11.sp)
                    }
                }
            }

            if (filteredList.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 60.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    AppManagedImage(
                                        drawableRes = item.imageRes,
                                        customUri = item.imageUrl.ifBlank { null },
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF0F4F8))
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text("SKU: ${item.styleId} • Category: ${item.category}", fontSize = 11.sp, color = TextSecondary)
                                        if (item.description.isNotBlank()) {
                                            Text(
                                                item.description,
                                                fontSize = 11.sp,
                                                color = TextMuted,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text("MRP: ₹${item.mrp}" + if (item.imageUrl.isNotBlank()) " • Image URL Linked" else "", fontSize = 10.5.sp, color = TextMuted)
                                    }

                                    IconButton(
                                        onClick = { deletingItem = item },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFFF5F5F5))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Selling: ₹${item.price}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                        Text(
                                            text = if (item.stock == 0) "Out of Stock (0)" else "Stock: ${item.stock} units",
                                            fontSize = 12.sp,
                                            color = if (item.stock > 0) Color(0xFF2E7D32) else Color.Red,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = { fullEditingItem = item },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                editingItem = item
                                                newStockText = item.stock.toString()
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("Stock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                val nextPaused = !item.isPaused
                                                if (isRoomBacked) {
                                                    onTogglePauseRoomProduct(item.id, nextPaused)
                                                } else {
                                                    val idx = fallbackInventoryList.indexOfFirst { it.id == item.id }
                                                    if (idx >= 0) {
                                                        fallbackInventoryList[idx] = item.copy(isPaused = nextPaused)
                                                    }
                                                }
                                                Toast.makeText(
                                                    context,
                                                    if (nextPaused) "Product paused" else "Product activated!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (item.isPaused) Color(0xFF2E7D32) else Color(0xFFC62828)
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text(if (item.isPaused) "Activate" else "Pause", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Empty state for tabs with no matches
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Products in this Filter", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap '+ Quick Add' or 'Catalog' to add new dresses.", fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }

    // Edit Stock Dialog
    editingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("Update Stock - ${item.styleId}") },
            text = {
                Column {
                    Text(item.title, fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newStockText,
                        onValueChange = { newStockText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Available Stock Units") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedStock = newStockText.toIntOrNull() ?: item.stock
                        if (isRoomBacked) {
                            onUpdateRoomStock(item.id, parsedStock)
                        } else {
                            val idx = fallbackInventoryList.indexOfFirst { it.id == item.id }
                            if (idx >= 0) fallbackInventoryList[idx] = item.copy(stock = parsedStock)
                        }
                        editingItem = null
                        Toast.makeText(context, "Stock updated to $parsedStock units", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) { Text("Cancel") }
            }
        )
    }

    // Full Edit Product Dialog (Name, SKU, Category, Price, Description, Image URL, Stock)
    fullEditingItem?.let { item ->
        var editTitle by remember(item.id) { mutableStateOf(item.title) }
        var editSku by remember(item.id) { mutableStateOf(item.styleId) }
        var editCategory by remember(item.id) { mutableStateOf(item.category) }
        var editMrp by remember(item.id) { mutableStateOf(item.mrp.toString()) }
        var editPrice by remember(item.id) { mutableStateOf(item.price.toString()) }
        var editStock by remember(item.id) { mutableStateOf(item.stock.toString()) }
        var editDescription by remember(item.id) { mutableStateOf(item.description) }
        var editImageUrl by remember(item.id) { mutableStateOf(item.imageUrl) }

        AlertDialog(
            onDismissRequest = { fullEditingItem = null },
            title = { Text("Edit Ethnic Wear Product", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editTitle, onValueChange = { editTitle = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = editSku, onValueChange = { editSku = it }, label = { Text("SKU") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = editCategory, onValueChange = { editCategory = it }, label = { Text("Category (Type or Pick)") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allCategories.take(16).forEach { catOpt ->
                            FilterChip(
                                selected = editCategory.equals(catOpt, ignoreCase = true),
                                onClick = { editCategory = catOpt },
                                label = { Text(catOpt, fontSize = 10.sp) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = editMrp, onValueChange = { editMrp = it.filter { c -> c.isDigit() } }, label = { Text("MRP (₹)") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = editPrice, onValueChange = { editPrice = it.filter { c -> c.isDigit() } }, label = { Text("Price (₹)") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = editStock, onValueChange = { editStock = it.filter { c -> c.isDigit() } }, label = { Text("Stock") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = editDescription, onValueChange = { editDescription = it }, label = { Text("Description") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editImageUrl, onValueChange = { editImageUrl = it }, label = { Text("Image URL (https://...)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val safePrice = editPrice.toIntOrNull()?.coerceAtLeast(1) ?: item.price
                        val safeMrp = editMrp.toIntOrNull()?.coerceAtLeast(safePrice) ?: item.mrp
                        val safeStock = editStock.toIntOrNull()?.coerceAtLeast(0) ?: item.stock
                        if (isRoomBacked) {
                            val existing = roomProducts.find { it.id == item.id }
                            onSaveRoomProduct(
                                (existing ?: ProductEntity(
                                    id = item.id,
                                    name = item.title,
                                    category = item.category,
                                    subcategory = item.category,
                                    mrp = safeMrp,
                                    sellingPrice = safePrice,
                                    stock = safeStock,
                                    fabric = "Pure Silk Blend"
                                )).copy(
                                    name = editTitle.trim().ifBlank { item.title },
                                    sku = editSku.trim().ifBlank { item.styleId },
                                    category = editCategory.trim().ifBlank { item.category },
                                    mrp = safeMrp,
                                    sellingPrice = safePrice,
                                    stock = safeStock,
                                    inStock = safeStock > 0,
                                    description = editDescription.trim(),
                                    imageUrl = editImageUrl.trim(),
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                        } else {
                            val idx = fallbackInventoryList.indexOfFirst { it.id == item.id }
                            if (idx >= 0) {
                                fallbackInventoryList[idx] = item.copy(
                                    title = editTitle.ifBlank { item.title },
                                    styleId = editSku.ifBlank { item.styleId },
                                    category = editCategory.ifBlank { item.category },
                                    mrp = safeMrp,
                                    price = safePrice,
                                    stock = safeStock,
                                    description = editDescription.trim(),
                                    imageUrl = editImageUrl.trim()
                                )
                            }
                        }
                        Toast.makeText(context, "Product saved to Room database!", Toast.LENGTH_SHORT).show()
                        fullEditingItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { fullEditingItem = null }) { Text("Cancel") }
            }
        )
    }

    // Add New Ethnic Wear Product Dialog (Name, Price, Description, Category, Image URL, Stock)
    if (showAddProductDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newSku by remember { mutableStateOf("GYANVI-ETH-${(100..999).random()}") }
        var newCategory by remember { mutableStateOf("Kurtis & Suits") }
        var newMrp by remember { mutableStateOf("1499") }
        var newPrice by remember { mutableStateOf("549") }
        var newStock by remember { mutableStateOf("50") }
        var newDescription by remember { mutableStateOf("") }
        var newImageUrl by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("Add Ethnic Wear Product", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Product Name *") }, placeholder = { Text("e.g. Banarasi Zari Anarkali Kurti") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newSku, onValueChange = { newSku = it }, label = { Text("SKU") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = newCategory, onValueChange = { newCategory = it }, label = { Text("Category (Type or Pick)") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allCategories.take(16).forEach { catOpt ->
                            FilterChip(
                                selected = newCategory.equals(catOpt, ignoreCase = true),
                                onClick = { newCategory = catOpt },
                                label = { Text(catOpt, fontSize = 10.sp) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newMrp, onValueChange = { newMrp = it.filter { c -> c.isDigit() } }, label = { Text("MRP (₹)") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = newPrice, onValueChange = { newPrice = it.filter { c -> c.isDigit() } }, label = { Text("Price (₹)") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = newStock, onValueChange = { newStock = it.filter { c -> c.isDigit() } }, label = { Text("Stock") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = newDescription, onValueChange = { newDescription = it }, label = { Text("Description") }, placeholder = { Text("Pure silk festive drape with embroidery...") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newImageUrl, onValueChange = { newImageUrl = it }, label = { Text("Image URL") }, placeholder = { Text("https://...") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            val safePrice = newPrice.toIntOrNull()?.coerceAtLeast(1) ?: 499
                            val safeMrp = newMrp.toIntOrNull()?.coerceAtLeast(safePrice) ?: 1299
                            val safeStock = newStock.toIntOrNull()?.coerceAtLeast(0) ?: 25
                            val newId = "inv_${System.currentTimeMillis()}"
                            val cleanDesc = newDescription.trim().ifBlank { "Premium ethnic wear from Gyanvi Dresses." }
                            val cleanCat = newCategory.trim().ifBlank { "Ethnic Wear" }
                            val cleanUrl = newImageUrl.trim()

                            if (isRoomBacked) {
                                onSaveRoomProduct(
                                    ProductEntity(
                                        id = newId,
                                        name = newTitle.trim(),
                                        category = cleanCat,
                                        subcategory = cleanCat,
                                        mrp = safeMrp,
                                        sellingPrice = safePrice,
                                        stock = safeStock,
                                        inStock = safeStock > 0,
                                        sku = newSku.trim(),
                                        fabric = "Premium Ethnic Weave",
                                        imageRes = R.drawable.dress_anarkali_1793417013639,
                                        imageUrl = cleanUrl,
                                        description = cleanDesc
                                    )
                                )
                            } else {
                                fallbackInventoryList.add(
                                    0,
                                    InventoryItem(
                                        id = newId,
                                        title = newTitle.trim(),
                                        styleId = newSku.trim(),
                                        catalogId = (453400000..453499999).random().toString(),
                                        price = safePrice,
                                        stock = safeStock,
                                        category = cleanCat,
                                        description = cleanDesc,
                                        imageUrl = cleanUrl,
                                        isPaused = false,
                                        mrp = safeMrp
                                    )
                                )
                            }
                            selectedMainTab = 0
                            showAddProductDialog = false
                            Toast.makeText(context, "Product saved to Ethnic Inventory!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter Product Name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                ) {
                    Text("Add Product", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Delete Product Confirmation Dialog
    deletingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            title = { Text("Delete Product?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently remove '${item.title}' (${item.styleId}) from Seller Inventory?") },
            confirmButton = {
                Button(
                    onClick = {
                        if (isRoomBacked) {
                            onDeleteRoomProduct(item.id)
                        } else {
                            fallbackInventoryList.removeAll { it.id == item.id }
                        }
                        deletingItem = null
                        Toast.makeText(context, "Product deleted from inventory", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingItem = null }) { Text("Cancel") }
            }
        )
    }

    // Category Filter Dialog
    if (showFilterDialog) {
        val filteredCats = allCategories.filter {
            filterCategorySearch.isEmpty() || it.contains(filterCategorySearch, ignoreCase = true)
        }

        Dialog(
            onDismissRequest = { showFilterDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showFilterDialog = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth().height(460.dp).clickable { /* absorb */ }
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Filter by Category", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            IconButton(onClick = { showFilterDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        OutlinedTextField(
                            value = filterCategorySearch,
                            onValueChange = { filterCategorySearch = it },
                            placeholder = { Text("Search category...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(filteredCats) { cat ->
                                val isChecked = selectedFilterCategories.contains(cat)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) selectedFilterCategories.remove(cat) else selectedFilterCategories.add(cat)
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            if (it) selectedFilterCategories.add(cat) else selectedFilterCategories.remove(cat)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(cat, fontSize = 13.sp)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { selectedFilterCategories.clear() }) {
                                Text("Clear all")
                            }
                            Button(
                                onClick = { showFilterDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Apply (${selectedFilterCategories.size})", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
