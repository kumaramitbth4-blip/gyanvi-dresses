package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.CategoryItem
import com.example.model.Product
import com.example.model.SubCategoryItem
import com.example.ui.components.AppManagedImage
import com.example.ui.theme.*

@Composable
fun AdminCategoryManagementTab(
    categories: List<CategoryItem>,
    subCategories: List<SubCategoryItem>,
    products: List<Product>,
    onAddMainCategory: (name: String, badge: String, seoName: String, customId: String, sortOrder: Int, isActive: Boolean, imageRes: Int, customImageUri: String) -> Unit,
    onEditMainCategory: (id: String, name: String, badge: String, seoName: String, sortOrder: Int, isActive: Boolean, imageRes: Int, customImageUri: String) -> Unit,
    onToggleCategoryActive: (id: String, active: Boolean) -> Unit,
    onMoveCategorySortOrder: (id: String, delta: Int) -> Unit,
    onDeleteCategory: (id: String) -> Unit,
    onAddSubCategory: (parentCategoryName: String, name: String, seoName: String, customId: String, sortOrder: Int, isActive: Boolean, imageRes: Int, customImageUri: String) -> Unit,
    onEditSubCategory: (id: String, parentCategoryName: String, name: String, seoName: String, sortOrder: Int, isActive: Boolean, imageRes: Int, customImageUri: String) -> Unit,
    onToggleSubCategoryActive: (id: String, active: Boolean) -> Unit,
    onDeleteSubCategory: (id: String) -> Unit,
    onUpdateProductCategories: (productId: String, primaryCategory: String, subcategory: String, additionalCategories: List<String>) -> Unit,
    onAddProduct: (name: String, category: String, subcategory: String, price: Int, mrp: Int, fabric: String, desc: String, additionalCategories: List<String>) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onResetCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var section by remember { mutableIntStateOf(0) } // 0 = Main Categories, 1 = Subcategories, 2 = Product Mapping
    var searchQuery by remember { mutableStateOf("") }

    var showAddMainDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryItem?>(null) }
    var showAddSubDialog by remember { mutableStateOf(false) }
    var editingSubCategory by remember { mutableStateOf<SubCategoryItem?>(null) }
    var editingProductMapping by remember { mutableStateOf<Product?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Category Management (कैटेगरी मैनेजमेंट)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${categories.size} Main • ${subCategories.size} Subcategories • ${products.size} Products",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            onResetCategories()
                            Toast.makeText(context, "Categories reset to default taxonomy", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search category, subcategory or product...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = MeeshoMagenta) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Main Categories (${categories.size})",
                        "Subcategories (${subCategories.size})",
                        "Product Mapping (${products.size})"
                    ).forEachIndexed { idx, label ->
                        FilterChip(
                            selected = section == idx,
                            onClick = { section = idx },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MeeshoMagenta,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        when (section) {
            0 -> {
                Button(
                    onClick = { showAddMainDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("admin_add_category_btn")
                ) {
                    Icon(Icons.Outlined.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Add Main Category", fontWeight = FontWeight.Bold)
                }

                val filteredCats = categories.sortedBy { it.sortOrder }.filter {
                    searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.offerBadge.contains(searchQuery, ignoreCase = true)
                }

                filteredCats.forEach { cat ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppManagedImage(
                                drawableRes = cat.imageRes,
                                customUri = cat.customImageUri,
                                contentDescription = cat.name,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (cat.offerBadge.isNotBlank()) {
                                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFCE7F3)) {
                                            Text(cat.offerBadge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Text("ID: ${cat.id} • Order: ${cat.sortOrder} • /${cat.seoName}", fontSize = 10.5.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Switch(
                                checked = cat.isActive,
                                onCheckedChange = { onToggleCategoryActive(cat.id, it) }
                            )
                            IconButton(onClick = { onMoveCategorySortOrder(cat.id, -1) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", tint = TextSecondary)
                            }
                            IconButton(onClick = { onMoveCategorySortOrder(cat.id, 1) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", tint = TextSecondary)
                            }
                            IconButton(onClick = { editingCategory = cat }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit Category", tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onDeleteCategory(cat.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete Category", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            1 -> {
                Button(
                    onClick = { showAddSubDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("admin_add_subcategory_btn")
                ) {
                    Icon(Icons.Outlined.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Add Subcategory", fontWeight = FontWeight.Bold)
                }

                val filteredSubs = subCategories.filter {
                    searchQuery.isBlank() ||
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.parentCategory.contains(searchQuery, ignoreCase = true)
                }

                filteredSubs.take(40).forEach { sub ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sub.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text("Parent: ${sub.parentCategory} • /${sub.seoName}", fontSize = 11.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = sub.isActive,
                                onCheckedChange = { onToggleSubCategoryActive(sub.id, it) }
                            )
                            IconButton(onClick = { editingSubCategory = sub }, modifier = Modifier.size(34.dp)) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit Subcategory", tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onDeleteSubCategory(sub.id) }, modifier = Modifier.size(34.dp)) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete Subcategory", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            2 -> {
                Button(
                    onClick = { showAddProductDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Outlined.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Quick Add Catalog Product", fontWeight = FontWeight.Bold)
                }

                val filteredProds = products.filter {
                    searchQuery.isBlank() ||
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
                }

                filteredProds.forEach { prod ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Primary: ${prod.category} > ${prod.subcategory}", fontSize = 11.sp, color = MeeshoMagenta)
                                if (prod.additionalCategories.isNotEmpty()) {
                                    Text("Extra: ${prod.additionalCategories.joinToString(", ")}", fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                            IconButton(onClick = { editingProductMapping = prod }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Assign Categories", tint = Color(0xFF2563EB))
                            }
                            IconButton(
                                onClick = {
                                    onDeleteProduct(prod.id)
                                    Toast.makeText(context, "Deleted ${prod.name}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete Product", tint = Color(0xFFDC2626))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddMainDialog || editingCategory != null) {
        val editing = editingCategory
        var name by remember(editing) { mutableStateOf(editing?.name ?: "") }
        var badge by remember(editing) { mutableStateOf(editing?.offerBadge ?: "NEW") }
        var seo by remember(editing) { mutableStateOf(editing?.seoName ?: "") }
        var sortOrder by remember(editing) { mutableStateOf((editing?.sortOrder ?: (categories.size + 1)).toString()) }
        var active by remember(editing) { mutableStateOf(editing?.isActive ?: true) }

        Dialog(onDismissRequest = { showAddMainDialog = false; editingCategory = null }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (editing == null) "Add Main Category" else "Edit Category", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MeeshoMagenta)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Category Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = badge, onValueChange = { badge = it }, label = { Text("Offer Badge") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = seo, onValueChange = { seo = it }, label = { Text("SEO Slug") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = sortOrder, onValueChange = { sortOrder = it.filter { ch -> ch.isDigit() } }, label = { Text("Sort Order") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Active on Storefront", fontSize = 13.sp)
                        Switch(checked = active, onCheckedChange = { active = it })
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.End) {
                        TextButton(onClick = { showAddMainDialog = false; editingCategory = null }) { Text("Cancel") }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val order = sortOrder.toIntOrNull() ?: (categories.size + 1)
                                    if (editing == null) {
                                        onAddMainCategory(name.trim(), badge.trim(), seo.trim(), "", order, active, R.drawable.dress_anarkali_1793417013639, "")
                                    } else {
                                        onEditMainCategory(editing.id, name.trim(), badge.trim(), seo.trim(), order, active, editing.imageRes, editing.customImageUri.orEmpty())
                                    }
                                    showAddMainDialog = false
                                    editingCategory = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    if (showAddSubDialog || editingSubCategory != null) {
        val editing = editingSubCategory
        var parentName by remember(editing) { mutableStateOf(editing?.parentCategory ?: (categories.firstOrNull()?.name ?: "Women")) }
        var name by remember(editing) { mutableStateOf(editing?.name ?: "") }
        var seo by remember(editing) { mutableStateOf(editing?.seoName ?: "") }
        var active by remember(editing) { mutableStateOf(editing?.isActive ?: true) }

        Dialog(onDismissRequest = { showAddSubDialog = false; editingSubCategory = null }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (editing == null) "Add Subcategory" else "Edit Subcategory", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0284C7))
                    OutlinedTextField(value = parentName, onValueChange = { parentName = it }, label = { Text("Parent Category *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Subcategory Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = seo, onValueChange = { seo = it }, label = { Text("SEO Slug") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Active", fontSize = 13.sp)
                        Switch(checked = active, onCheckedChange = { active = it })
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.End) {
                        TextButton(onClick = { showAddSubDialog = false; editingSubCategory = null }) { Text("Cancel") }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    if (editing == null) {
                                        onAddSubCategory(parentName.trim(), name.trim(), seo.trim(), "", subCategories.size + 1, active, R.drawable.dress_anarkali_1793417013639, "")
                                    } else {
                                        onEditSubCategory(editing.id, parentName.trim(), name.trim(), seo.trim(), editing.sortOrder, active, editing.imageRes, editing.customImageUri.orEmpty())
                                    }
                                    showAddSubDialog = false
                                    editingSubCategory = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    editingProductMapping?.let { prod ->
        var primaryCat by remember(prod) { mutableStateOf(prod.category) }
        var subCat by remember(prod) { mutableStateOf(prod.subcategory) }
        var extraCsv by remember(prod) { mutableStateOf(prod.additionalCategories.joinToString(", ")) }
        Dialog(onDismissRequest = { editingProductMapping = null }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Map Product Categories", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MeeshoMagenta)
                    Text(prod.name, fontSize = 12.sp, color = TextSecondary, maxLines = 2)
                    OutlinedTextField(value = primaryCat, onValueChange = { primaryCat = it }, label = { Text("Primary Category") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = subCat, onValueChange = { subCat = it }, label = { Text("Subcategory") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = extraCsv, onValueChange = { extraCsv = it }, label = { Text("Additional Categories (comma separated)") }, modifier = Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), Arrangement.End) {
                        TextButton(onClick = { editingProductMapping = null }) { Text("Cancel") }
                        Button(
                            onClick = {
                                val extras = extraCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                onUpdateProductCategories(prod.id, primaryCat.trim(), subCat.trim(), extras)
                                editingProductMapping = null
                                Toast.makeText(context, "Product categories updated!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }

    if (showAddProductDialog) {
        var pName by remember { mutableStateOf("") }
        var pCat by remember { mutableStateOf(categories.firstOrNull()?.name ?: "Sarees") }
        var pSub by remember { mutableStateOf("Designer Wear") }
        var pPrice by remember { mutableStateOf("499") }
        var pMrp by remember { mutableStateOf("1299") }
        var pFabric by remember { mutableStateOf("Silk Blend") }
        var pDesc by remember { mutableStateOf("Premium ethnic wear from Gyanvi Dresses.") }

        Dialog(onDismissRequest = { showAddProductDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(
                    modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Add Catalog Product", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF15803D))
                    OutlinedTextField(value = pName, onValueChange = { pName = it }, label = { Text("Product Name *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pCat, onValueChange = { pCat = it }, label = { Text("Category *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pSub, onValueChange = { pSub = it }, label = { Text("Subcategory") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = pPrice, onValueChange = { pPrice = it.filter { c -> c.isDigit() } }, label = { Text("Price ₹") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = pMrp, onValueChange = { pMrp = it.filter { c -> c.isDigit() } }, label = { Text("MRP ₹") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = pFabric, onValueChange = { pFabric = it }, label = { Text("Fabric") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pDesc, onValueChange = { pDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), Arrangement.End) {
                        TextButton(onClick = { showAddProductDialog = false }) { Text("Cancel") }
                        Button(
                            onClick = {
                                if (pName.isNotBlank()) {
                                    onAddProduct(
                                        pName.trim(),
                                        pCat.trim().ifBlank { "Ethnic Wear" },
                                        pSub.trim(),
                                        pPrice.toIntOrNull() ?: 499,
                                        pMrp.toIntOrNull() ?: 1299,
                                        pFabric.trim(),
                                        pDesc.trim(),
                                        emptyList()
                                    )
                                    showAddProductDialog = false
                                    Toast.makeText(context, "Product added to live catalog!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                        ) {
                            Text("Publish")
                        }
                    }
                }
            }
        }
    }
}
