package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.CategoryItem
import com.example.model.Product
import com.example.model.SubCategoryItem
import com.example.ui.components.AppManagedImage
import com.example.ui.theme.*

@Composable
fun CategoriesScreen(
    wishlistCount: Int,
    cartCount: Int,
    onSearchClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onCartClick: () -> Unit,
    onSelectSubCategory: (String) -> Unit,
    categories: List<CategoryItem> = SampleData.categoryList,
    subCategories: List<SubCategoryItem> = SampleData.defaultSubCategories,
    products: List<Product> = SampleData.sampleProducts,
    onProductClick: (Product) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeMainCategories = remember(categories) {
        categories.filter { it.isActive }.sortedBy { it.sortOrder }
    }
    var selectedRailId by remember { mutableStateOf("popular") }

    val selectedMainCategory = remember(selectedRailId, activeMainCategories) {
        activeMainCategories.find { it.id == selectedRailId }
    }

    val activeSubCategories = remember(selectedMainCategory, subCategories) {
        if (selectedMainCategory == null) {
            subCategories.filter { it.isActive }.sortedBy { it.sortOrder }.take(12)
        } else {
            subCategories
                .filter { it.isActive && it.parentCategory.equals(selectedMainCategory.name, ignoreCase = true) }
                .sortedBy { it.sortOrder }
        }
    }

    val matchingProducts = remember(selectedMainCategory, products, activeSubCategories) {
        if (selectedMainCategory == null) {
            products.take(6)
        } else {
            products.filter { prod ->
                prod.matchesCategory(selectedMainCategory.name) ||
                    activeSubCategories.any { sub -> prod.matchesCategory(sub.name) }
            }.take(8)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YellowBackground)
            .statusBarsPadding()
            .testTag("categories_screen")
    ) {
        // Categories Top App Bar
        Surface(
            color = CardWhite,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "CATEGORIES (${activeMainCategories.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${subCategories.count { it.isActive }} Subcategories Available",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSearchClick, modifier = Modifier.testTag("cat_search_btn")) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                    }
                    IconButton(onClick = onWishlistClick, modifier = Modifier.testTag("cat_wishlist_btn")) {
                        BadgedBox(
                            badge = {
                                if (wishlistCount > 0) {
                                    Badge(containerColor = MeeshoMagenta, contentColor = Color.White) {
                                        Text("$wishlistCount")
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Outlined.FavoriteBorder, contentDescription = "Wishlist", tint = TextPrimary)
                        }
                    }
                    IconButton(onClick = onCartClick, modifier = Modifier.testTag("cat_cart_btn")) {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(containerColor = MeeshoMagenta, contentColor = Color.White) {
                                        Text("$cartCount")
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Outlined.ShoppingCart, contentDescription = "Cart", tint = TextPrimary)
                        }
                    }
                }
            }
        }

        // Dual Pane: Dynamic Left Main Category Rail (22+ Categories) + Right Subcategory & Product Pane
        Row(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .width(98.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFF7F7F7))
            ) {
                item(key = "popular") {
                    val isSelected = selectedRailId == "popular"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRailId = "popular" }
                            .background(if (isSelected) CardWhite else Color(0xFFF0F0F0))
                            .padding(vertical = 12.dp, horizontal = 4.dp)
                            .testTag("cat_rail_popular"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .width(4.dp)
                                    .height(34.dp)
                                    .background(MeeshoMagenta, RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Popular",
                                tint = YellowPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Popular",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MeeshoMagenta else TextPrimary,
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                items(activeMainCategories, key = { it.id }) { category ->
                    val isSelected = selectedRailId == category.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRailId = category.id }
                            .background(if (isSelected) CardWhite else Color(0xFFF0F0F0))
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                            .testTag("cat_rail_${category.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .width(4.dp)
                                    .height(34.dp)
                                    .background(MeeshoMagenta, RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AppManagedImage(
                                drawableRes = category.imageRes,
                                customUri = category.customImageUri,
                                contentDescription = category.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, if (isSelected) MeeshoMagenta else Color(0xFFE0E0E0), CircleShape)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MeeshoMagenta else TextPrimary,
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Right Pane: Subcategories + Category Products
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(CardWhite)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    val headerName = selectedMainCategory?.name ?: "Featured Categories"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = headerName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (selectedMainCategory != null && selectedMainCategory.offerBadge.isNotBlank()) {
                                Text(
                                    text = selectedMainCategory.offerBadge,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MeeshoMagenta
                                )
                            }
                        }
                        if (selectedMainCategory != null) {
                            AssistChip(
                                onClick = { onSelectSubCategory(selectedMainCategory.name) },
                                label = { Text("View All", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                trailingIcon = { Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }
                }

                if (selectedMainCategory == null) {
                    item {
                        Text(
                            text = "All ${activeMainCategories.size} Marketplace Categories",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            activeMainCategories.chunked(3).forEach { rowCats ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    rowCats.forEach { cat ->
                                        SubCategoryCircleCard(
                                            id = cat.id,
                                            title = cat.name,
                                            imageRes = cat.imageRes,
                                            customUri = cat.customImageUri,
                                            onClick = { selectedRailId = cat.id }
                                        )
                                    }
                                    repeat(3 - rowCats.size) {
                                        Spacer(modifier = Modifier.width(76.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (activeSubCategories.isNotEmpty()) {
                        item {
                            Text(
                                text = "Subcategories (${activeSubCategories.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                activeSubCategories.chunked(3).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        rowItems.forEach { sub ->
                                            SubCategoryCircleCard(
                                                id = sub.id,
                                                title = sub.name,
                                                imageRes = sub.imageRes,
                                                customUri = sub.customImageUri,
                                                onClick = { onSelectSubCategory(sub.name) }
                                            )
                                        }
                                        repeat(3 - rowItems.size) {
                                            Spacer(modifier = Modifier.width(76.dp))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Surface(
                                color = Color(0xFFFDF2F8),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MeeshoMagenta.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectSubCategory(selectedMainCategory.name) }
                            ) {
                                Text(
                                    text = "Browse all ${selectedMainCategory.name} products →",
                                    color = MeeshoMagenta,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }
                }

                if (matchingProducts.isNotEmpty()) {
                    item {
                        Text(
                            text = if (selectedMainCategory != null) "${selectedMainCategory.name} Highlights (${matchingProducts.size})" else "Popular Products",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    items(matchingProducts, key = { it.id }) { prod ->
                        Surface(
                            color = Color(0xFFFAFAFC),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProductClick(prod) }
                                .testTag("cat_prod_${prod.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppManagedImage(
                                    drawableRes = prod.imageRes,
                                    customUri = prod.customImageUri,
                                    contentDescription = prod.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${prod.category} • ${prod.subcategory}", fontSize = 10.sp, color = TextSecondary)
                                    Text("₹${prod.price}  (${prod.discountPercent}% OFF)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubCategoryCircleCard(
    id: String,
    title: String,
    imageRes: Int,
    customUri: String?,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clickable { onClick() }
            .testTag("subcat_item_$id")
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(2.dp, Color(0xFFFFCC80), CircleShape)
                .background(Color(0xFFFFF8E1)),
            contentAlignment = Alignment.Center
        ) {
            AppManagedImage(
                drawableRes = imageRes,
                customUri = customUri,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            fontSize = 10.5.sp,
            lineHeight = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
