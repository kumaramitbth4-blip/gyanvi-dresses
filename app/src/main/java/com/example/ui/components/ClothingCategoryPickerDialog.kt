package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.ClothingCatalogTaxonomy
import com.example.model.CategoryItem
import com.example.model.SubCategoryItem
import com.example.ui.theme.*

@Composable
fun ClothingCategoryPickerDialog(
    initialCategory: String,
    initialSubcategory: String,
    categories: List<CategoryItem>,
    subCategories: List<SubCategoryItem>,
    onDismiss: () -> Unit,
    onSelect: (mainCat: String, subCat: String, suggestedName: String?, imgRes: Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMainCat by remember(initialCategory, categories) {
        mutableStateOf(
            initialCategory.ifBlank { categories.firstOrNull()?.name ?: "Women's Ethnic Wear" }
        )
    }

    val allMainCategories = remember(categories) {
        (categories.filter { it.isActive }.map { it.name } +
            ClothingCatalogTaxonomy.clothingCategories.map { it.linkedMainCategoryName })
            .distinct()
    }

    val matchingSubCategories = remember(selectedMainCat, searchQuery, subCategories) {
        val q = searchQuery.trim().lowercase()
        val dynamicSubs = subCategories.filter { sub ->
            sub.isActive && (q.isNotBlank() || sub.parentCategory.equals(selectedMainCat, ignoreCase = true))
        }.map { Triple(it.parentCategory, it.name, it.imageRes) }

        val taxonomySubs = ClothingCatalogTaxonomy.clothingCategories
            .filter { q.isNotBlank() || it.linkedMainCategoryName.equals(selectedMainCat, ignoreCase = true) }
            .flatMap { cat ->
                cat.subcategories.map { sub ->
                    Triple(cat.linkedMainCategoryName, sub.name, sub.imageRes)
                }
            }

        val combined = (dynamicSubs + taxonomySubs).distinctBy { "${it.first.lowercase()}::${it.second.lowercase()}" }
        if (q.isBlank()) {
            combined.ifEmpty {
                listOf(Triple(selectedMainCat, initialSubcategory.ifBlank { "General Collection" }, R.drawable.dress_anarkali_1793417013639))
            }
        } else {
            combined.filter {
                it.first.lowercase().contains(q) || it.second.lowercase().contains(q)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.82f)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Category, contentDescription = null, tint = MeeshoMagenta)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Clothing Category", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary) },
                    placeholder = { Text("Search Saree, Kurti, Lehenga, Kids, Footwear...") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Left: Main Categories
                    LazyColumn(
                        modifier = Modifier
                            .weight(0.44f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(allMainCategories) { mainCat ->
                            val isSelected = selectedMainCat.equals(mainCat, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFFFDF2F8) else Color.Transparent,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MeeshoMagenta) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedMainCat = mainCat
                                        searchQuery = ""
                                    }
                            ) {
                                Text(
                                    text = mainCat,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MeeshoMagenta else TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }

                    // Right: Subcategories
                    LazyColumn(
                        modifier = Modifier.weight(0.56f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(matchingSubCategories) { (mainCat, subCat, imgRes) ->
                            val isCurrent = mainCat.equals(initialCategory, ignoreCase = true) &&
                                subCat.equals(initialSubcategory, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrent) Color(0xFFFDF2F8) else CardWhite,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCurrent) MeeshoMagenta else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val suggested = "Gyanvi $subCat Designer Collection"
                                        onSelect(mainCat, subCat, suggested, imgRes)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = imgRes),
                                        contentDescription = subCat,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = subCat,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isCurrent) MeeshoMagenta else TextPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = mainCat,
                                            fontSize = 10.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (isCurrent) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(16.dp))
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
