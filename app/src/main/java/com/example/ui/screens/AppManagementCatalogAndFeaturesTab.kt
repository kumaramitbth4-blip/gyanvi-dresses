package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.*
import com.example.ui.components.AppManagedImage
import com.example.ui.theme.*

@Composable
fun AppFeaturesOptionsTab(
    features: AppFeaturesConfig,
    onUpdateFeatures: ((AppFeaturesConfig) -> AppFeaturesConfig) -> Unit,
    onRestoreAllFeatures: () -> Unit,
    onQuickBackup: () -> Unit = {},
    onScrollDown: () -> Unit = {},
    onScrollUp: () -> Unit = {},
    onNavigatePrevTab: () -> Unit = {},
    onNavigateNextTab: () -> Unit = {}
) {
    val context = LocalContext.current
    val accent = MaterialTheme.colorScheme.primary

    Card(
        shape = RoundedCornerShape(14.dp),
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
                        text = "Options & Features Manager",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "ऐप के किसी भी Option या Feature को On/Off या Hide/Delete करें",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                OutlinedButton(
                    onClick = {
                        onRestoreAllFeatures()
                        Toast.makeText(context, "All options & features restored!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("restore_all_features_btn")
                ) {
                    Text("Restore All", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accent)
                }
            }

            FeatureToggleRow(
                title = "Floating Backup, Side Tir, Scroll Down & Tin Tir Bar",
                subtitle = "होम स्क्रीन पर Backup, तीर (Side Arrow), Scroll ↓ और ◀ ▼ ▶ बटन",
                checked = features.showFloatingQuickControls,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showFloatingQuickControls = enabled) } },
                testTag = "toggle_floating_quick_controls"
            )
            FeatureToggleRow(
                title = "Hero Promotional Slider",
                subtitle = "होम पेज का 3-पेज स्लाइडर बैनर",
                checked = features.showHeroSlider,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showHeroSlider = enabled) } },
                testTag = "toggle_hero_slider"
            )
            FeatureToggleRow(
                title = "Countdown Sale Timer Banner",
                subtitle = "UP TO 80% OFF वाला टाइमर बॉक्स",
                checked = features.showCountdownBanner,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showCountdownBanner = enabled) } },
                testTag = "toggle_countdown_banner"
            )
            FeatureToggleRow(
                title = "Weekly Development Progress Strip",
                subtitle = "15 Marketplace Modules वाला बैनर",
                checked = features.showDevProgressStrip,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showDevProgressStrip = enabled) } },
                testTag = "toggle_dev_progress"
            )
            FeatureToggleRow(
                title = "Category Navigation Strip",
                subtitle = "गोल कैटेगरी आइकॉन लिस्ट",
                checked = features.showCategoryStrip,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showCategoryStrip = enabled) } },
                testTag = "toggle_category_strip"
            )
            FeatureToggleRow(
                title = "Sort & Filter Bar",
                subtitle = "Sort / Category / Gender / Filters बार",
                checked = features.showFilterBar,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showFilterBar = enabled) } },
                testTag = "toggle_filter_bar"
            )
            FeatureToggleRow(
                title = "Announcement Marquee Bar",
                subtitle = "होम स्क्रीन टॉप सूचना पट्टी (Text Tab से लिखें)",
                checked = features.showAnnouncementTicker,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showAnnouncementTicker = enabled) } },
                testTag = "toggle_announcement"
            )
            FeatureToggleRow(
                title = "Voice Search Mic Button",
                subtitle = "सर्च बार का माइक आइकॉन",
                checked = features.showVoiceSearch,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showVoiceSearch = enabled) } },
                testTag = "toggle_voice_search"
            )
            FeatureToggleRow(
                title = "Camera Image Search Button",
                subtitle = "सर्च बार का कैमरा आइकॉन",
                checked = features.showCameraSearch,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showCameraSearch = enabled) } },
                testTag = "toggle_camera_search"
            )
            FeatureToggleRow(
                title = "Product Wishlist Heart Button",
                subtitle = "प्रोडक्ट कार्ड पर दिल (Wishlist) आइकॉन",
                checked = features.showWishlistHearts,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showWishlistHearts = enabled) } },
                testTag = "toggle_wishlist_hearts"
            )
            FeatureToggleRow(
                title = "Product Share Button",
                subtitle = "प्रोडक्ट कार्ड पर शेयर आइकॉन",
                checked = features.showShareButtons,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showShareButtons = enabled) } },
                testTag = "toggle_share_buttons"
            )
            FeatureToggleRow(
                title = "Add to Cart Button on Cards",
                subtitle = "प्रोडक्ट कार्ड के नीचे Add to Cart बटन",
                checked = features.showAddToCartButtons,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showAddToCartButtons = enabled) } },
                testTag = "toggle_add_to_cart"
            )
            FeatureToggleRow(
                title = "SALE Badges & Mall Assured Tags",
                subtitle = "SALE ⚡ और ✓ Mall बैज",
                checked = features.showSaleBadges && features.showMallBadges,
                onToggle = { enabled ->
                    onUpdateFeatures { it.copy(showSaleBadges = enabled, showMallBadges = enabled) }
                },
                testTag = "toggle_badges"
            )
            FeatureToggleRow(
                title = "Quick-Edit App Pencil Icon (Home Bar)",
                subtitle = "होम टॉप बार से सीधे Edit Dashboard खोलने का बटन",
                checked = features.showQuickEditShortcut,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showQuickEditShortcut = enabled) } },
                testTag = "toggle_quick_edit"
            )
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Bottom Navigation Tabs Control",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
            )
            Text(
                text = "नीचे वाले नेविगेशन टैब चालू/बंद करें (Home हमेशा रहेगा)",
                fontSize = 11.sp,
                color = TextSecondary
            )
            FeatureToggleRow(
                title = "Categories Tab",
                subtitle = "Bottom bar Categories option",
                checked = features.showCategoriesTab,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showCategoriesTab = enabled) } },
                testTag = "toggle_tab_categories"
            )
            FeatureToggleRow(
                title = "Mall Tab",
                subtitle = "Bottom bar Mall showcase",
                checked = features.showMallTab,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showMallTab = enabled) } },
                testTag = "toggle_tab_mall"
            )
            FeatureToggleRow(
                title = "Video Finds Tab",
                subtitle = "Bottom bar Live Video Shopping",
                checked = features.showVideoFindsTab,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showVideoFindsTab = enabled) } },
                testTag = "toggle_tab_video"
            )
            FeatureToggleRow(
                title = "My Orders Tab",
                subtitle = "Bottom bar Orders tracking",
                checked = features.showOrdersTab,
                onToggle = { enabled -> onUpdateFeatures { it.copy(showOrdersTab = enabled) } },
                testTag = "toggle_tab_orders"
            )
        }
    }

    // All Button Types Quick Action Kit
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2F8)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth().testTag("all_button_types_card")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "All Button Types (सभी प्रकार के बटन)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = accent
            )
            Text(
                text = "Filled, Tonal, Outlined, Elevated, Icon, Pill, Circular Scroll, Backup व 3-Arrow (तीन तीर) बटन",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onQuickBackup,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    modifier = Modifier.weight(1f).testTag("btn_type_filled_backup")
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Backup", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                FilledTonalButton(
                    onClick = onScrollDown,
                    modifier = Modifier.weight(1f).testTag("btn_type_tonal_down")
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Scroll ↓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onScrollUp,
                    modifier = Modifier.weight(1f).testTag("btn_type_outlined_up")
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Top ↑", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElevatedButton(
                    onClick = onRestoreAllFeatures,
                    modifier = Modifier.testTag("btn_type_elevated_restore")
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Elevated Reset", fontSize = 11.sp)
                }

                // Segmented 3-Arrow (Tin Tir) Pill Button
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = CardWhite,
                    border = BorderStroke(1.5.dp, accent)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigatePrevTab, modifier = Modifier.size(38.dp)) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous", tint = accent)
                        }
                        IconButton(onClick = onScrollDown, modifier = Modifier.size(38.dp)) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Scroll Down", tint = accent)
                        }
                        IconButton(onClick = onNavigateNextTab, modifier = Modifier.size(38.dp)) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next", tint = accent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (checked) Color(0xFFFDF2F8).copy(alpha = 0.55f) else Color(0xFFF3F4F6),
        border = BorderStroke(1.dp, if (checked) Color(0xFFFBCFE8) else Color(0xFFE5E7EB)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Text(subtitle, fontSize = 11.sp, color = TextSecondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (checked) "ON" else "HIDDEN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (checked) GreenRating else Color(0xFFDC2626),
                    modifier = Modifier.padding(end = 6.dp)
                )
                Switch(
                    checked = checked,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
fun AppCatalogAndCategoriesEditorTab(
    products: List<Product>,
    categories: List<CategoryItem>,
    onEditProduct: (
        productId: String,
        name: String,
        category: String,
        price: Int,
        originalPrice: Int,
        fabric: String,
        description: String,
        inStock: Boolean,
        isMahaDeal: Boolean,
        isMallAssured: Boolean,
        imageRes: Int,
        customImageUri: String?
    ) -> Unit,
    onAddProduct: (
        name: String,
        category: String,
        price: Int,
        originalPrice: Int,
        fabric: String,
        description: String,
        imageRes: Int,
        customImageUri: String?
    ) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onResetProducts: () -> Unit,
    onUpdateCategory: (String, String, String, Int, String?) -> Unit,
    onAddCategory: (String, String, Int, String?) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onResetCategories: () -> Unit
) {
    val context = LocalContext.current
    var searchCatalog by remember { mutableStateOf("") }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryItem?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    // 1. Category Strip Management
    Card(
        shape = RoundedCornerShape(14.dp),
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
                    Text("Categories Edit / Delete", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Text("${categories.size} categories • नाम, फोटो व बैज बदलें", fontSize = 11.sp, color = TextSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            onResetCategories()
                            Toast.makeText(context, "Categories reset", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Reset", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { showAddCategoryDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("add_category_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            categories.forEach { cat ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppManagedImage(
                            drawableRes = cat.imageRes,
                            customUri = cat.customImageUri,
                            contentDescription = cat.name,
                            modifier = Modifier.size(42.dp).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Text(
                                text = if (cat.offerBadge.isNotBlank()) "Badge: ${cat.offerBadge}" else "No Offer Badge",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        IconButton(
                            onClick = { editingCategory = cat },
                            modifier = Modifier.testTag("edit_cat_${cat.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Category", tint = Color(0xFF2563EB))
                        }
                        if (cat.id != "all") {
                            IconButton(
                                onClick = {
                                    onDeleteCategory(cat.id)
                                    Toast.makeText(context, "${cat.name} deleted", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("delete_cat_${cat.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Category", tint = Color(0xFFDC2626))
                            }
                        }
                    }
                }
            }
        }
    }

    // 2. Live Products Catalog Edit / Delete
    Card(
        shape = RoundedCornerShape(14.dp),
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
                    Text("Products Edit / Delete", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Text("${products.size} products • नाम, कीमत, फोटो, डिस्क्रिप्शन बदलें", fontSize = 11.sp, color = TextSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            onResetProducts()
                            Toast.makeText(context, "Catalog restored to defaults", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Reset", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { showAddProductDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("add_product_cms_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("New Dress", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            OutlinedTextField(
                value = searchCatalog,
                onValueChange = { searchCatalog = it },
                placeholder = { Text("Search product to edit or delete...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            val filtered = remember(products, searchCatalog) {
                if (searchCatalog.isBlank()) products else products.filter {
                    it.name.contains(searchCatalog, true) || it.category.contains(searchCatalog, true)
                }
            }

            filtered.forEach { product ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppManagedImage(
                            drawableRes = product.imageRes,
                            customUri = product.customImageUri,
                            contentDescription = product.name,
                            modifier = Modifier.size(54.dp).clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = TextPrimary
                            )
                            Text(
                                text = "₹${product.price} (MRP ₹${product.originalPrice}) • ${product.category}",
                                fontSize = 11.sp,
                                color = GreenDiscount,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = { editingProduct = product },
                            modifier = Modifier.testTag("edit_prod_${product.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Product", tint = Color(0xFF2563EB))
                        }
                        IconButton(
                            onClick = {
                                onDeleteProduct(product.id)
                                Toast.makeText(context, "Product deleted!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("delete_prod_${product.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = Color(0xFFDC2626))
                        }
                    }
                }
            }
        }
    }

    // Product Edit / Add Dialog
    if (editingProduct != null || showAddProductDialog) {
        val target = editingProduct
        ProductEditorDialog(
            initialProduct = target,
            onDismiss = {
                editingProduct = null
                showAddProductDialog = false
            },
            onSave = { name, cat, price, mrp, fabric, desc, inStock, isMaha, isMall, imgRes, customUri ->
                if (target != null) {
                    onEditProduct(target.id, name, cat, price, mrp, fabric, desc, inStock, isMaha, isMall, imgRes, customUri)
                    Toast.makeText(context, "Product updated live!", Toast.LENGTH_SHORT).show()
                } else {
                    onAddProduct(name, cat, price, mrp, fabric, desc, imgRes, customUri)
                    Toast.makeText(context, "New product added to store!", Toast.LENGTH_SHORT).show()
                }
                editingProduct = null
                showAddProductDialog = false
            }
        )
    }

    // Category Edit / Add Dialog
    if (editingCategory != null || showAddCategoryDialog) {
        val target = editingCategory
        CategoryEditorDialog(
            initialCategory = target,
            onDismiss = {
                editingCategory = null
                showAddCategoryDialog = false
            },
            onSave = { name, badge, imgRes, customUri ->
                if (target != null) {
                    onUpdateCategory(target.id, name, badge, imgRes, customUri)
                    Toast.makeText(context, "Category updated!", Toast.LENGTH_SHORT).show()
                } else {
                    onAddCategory(name, badge, imgRes, customUri)
                    Toast.makeText(context, "Category added!", Toast.LENGTH_SHORT).show()
                }
                editingCategory = null
                showAddCategoryDialog = false
            }
        )
    }
}

@Composable
private fun ProductEditorDialog(
    initialProduct: Product?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: String,
        price: Int,
        mrp: Int,
        fabric: String,
        desc: String,
        inStock: Boolean,
        isMaha: Boolean,
        isMall: Boolean,
        imageRes: Int,
        customUri: String?
    ) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "Kurtis & Suits") }
    var priceStr by remember { mutableStateOf((initialProduct?.price ?: 599).toString()) }
    var mrpStr by remember { mutableStateOf((initialProduct?.originalPrice ?: 1499).toString()) }
    var fabric by remember { mutableStateOf(initialProduct?.fabric ?: "Silk Blend") }
    var desc by remember { mutableStateOf(initialProduct?.description ?: "") }
    var inStock by remember { mutableStateOf(initialProduct?.inStock ?: true) }
    var isMaha by remember { mutableStateOf(initialProduct?.isMahaDeal ?: true) }
    var isMall by remember { mutableStateOf(initialProduct?.isMallAssured ?: true) }
    var selectedDrawable by remember { mutableIntStateOf(initialProduct?.imageRes ?: R.drawable.dress_anarkali_1793417013639) }
    var customUri by remember { mutableStateOf(initialProduct?.customImageUri) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            customUri = uri.toString()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardWhite,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (initialProduct != null) "Edit Product Details & Image" else "Add New Dress Product",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppManagedImage(
                        drawableRes = selectedDrawable,
                        customUri = customUri,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp))
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Gallery Image", fontSize = 11.sp)
                        }
                        if (!customUri.isNullOrBlank()) {
                            TextButton(
                                onClick = { customUri = null },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Text("Remove Custom Photo", fontSize = 10.sp, color = Color(0xFFDC2626))
                            }
                        }
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AppDrawablePresets.all) { preset ->
                        val active = customUri.isNullOrBlank() && selectedDrawable == preset.drawableRes
                        FilterChip(
                            selected = active,
                            onClick = {
                                customUri = null
                                selectedDrawable = preset.drawableRes
                            },
                            label = { Text(preset.label, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Sarees, Kurtis & Suits, Lehenga Choli)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Selling Price ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = mrpStr,
                        onValueChange = { mrpStr = it.filter { c -> c.isDigit() } },
                        label = { Text("MRP ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = fabric,
                    onValueChange = { fabric = it },
                    label = { Text("Fabric / Material") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = inStock, onCheckedChange = { inStock = it })
                        Text("In Stock", fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isMaha, onCheckedChange = { isMaha = it })
                        Text("Sale Badge", fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isMall, onCheckedChange = { isMall = it })
                        Text("Mall Tag", fontSize = 11.sp)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                name,
                                category,
                                priceStr.toIntOrNull() ?: 499,
                                mrpStr.toIntOrNull() ?: 1299,
                                fabric,
                                desc,
                                inStock,
                                isMaha,
                                isMall,
                                selectedDrawable,
                                customUri
                            )
                        },
                        enabled = name.trim().isNotEmpty()
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryEditorDialog(
    initialCategory: CategoryItem?,
    onDismiss: () -> Unit,
    onSave: (name: String, badge: String, imageRes: Int, customUri: String?) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialCategory?.name ?: "") }
    var badge by remember { mutableStateOf(initialCategory?.offerBadge ?: "") }
    var selectedDrawable by remember { mutableIntStateOf(initialCategory?.imageRes ?: R.drawable.dress_saree_1793417052582) }
    var customUri by remember { mutableStateOf(initialCategory?.customImageUri) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            customUri = uri.toString()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = CardWhite, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (initialCategory != null) "Edit Category & Photo" else "Add New Category",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppManagedImage(
                        drawableRes = selectedDrawable,
                        customUri = customUri,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp).clip(CircleShape)
                    )
                    Button(
                        onClick = {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Pick Photo from Gallery", fontSize = 11.sp)
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AppDrawablePresets.all) { preset ->
                        FilterChip(
                            selected = customUri.isNullOrBlank() && selectedDrawable == preset.drawableRes,
                            onClick = {
                                customUri = null
                                selectedDrawable = preset.drawableRes
                            },
                            label = { Text(preset.label, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = badge,
                    onValueChange = { badge = it },
                    label = { Text("Offer Badge (e.g. FROM ₹199 or blank)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(name, badge, selectedDrawable, customUri) },
                        enabled = name.trim().isNotEmpty()
                    ) {
                        Text("Save Category")
                    }
                }
            }
        }
    }
}
