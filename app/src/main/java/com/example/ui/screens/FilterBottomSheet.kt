package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FilterState
import com.example.ui.theme.*

enum class FilterTab(val label: String) {
    CATEGORY("Category"),
    GENDER("Gender"),
    COLOR("Color"),
    FABRIC("Fabric"),
    SIZE("Size"),
    PRICE("Price"),
    RATING("Rating"),
    OCCASION("Occasion"),
    DISCOUNT("Discount"),
    MEESHO_MALL("Gyanvi Dresses Mall")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    filterState: FilterState,
    initialTab: FilterTab = FilterTab.CATEGORY,
    matchingCount: Int = 1200,
    onDismiss: () -> Unit,
    onApplyFilter: (FilterState) -> Unit,
    onResetFilter: () -> Unit,
    dynamicCategories: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(initialTab) }
    var categorySearchText by remember { mutableStateOf("") }

    // Categories merged dynamically from Active Categories + Subcategories
    val defaultCategoryOptions = listOf(
        "Women Dresses",
        "Kurtis",
        "Sarees",
        "Suits & Dress Materials",
        "Kurta Sets",
        "Kids - Girls Frocks & Dresses",
        "Women Tops And Tunics",
        "Women T-shirts",
        "Blouses",
        "Leggings & Tights",
        "Jewellery Set",
        "Earrings & Studs",
        "Bangles & Bracelets",
        "Mangalsutras",
        "Rings",
        "Men Shirts",
        "Men T-shirts",
        "Kids - Boys Tshirts & Polos",
        "Jackets And Coats",
        "Jeans",
        "Shoes",
        "Bedsheets",
        "Women Analog Watches",
        "Men Analog Watches",
        "Mobile Cases & Covers",
        "Shapewear",
        "Tops And Bottom Sets",
        "Track Suit And Pants",
        "Trousers & Pants",
        "Sweater And Sweatshirts",
        "Hair Oils",
        "Face Gels",
        "Kitchen Storage",
        "Kitchen Tools",
        "Appliance Covers",
        "Fridge Covers"
    )
    val allCategoryOptions = remember(dynamicCategories) {
        (dynamicCategories + defaultCategoryOptions).distinctBy { it.lowercase() }
    }

    val genderOptions = listOf("All", "Women", "Men", "Girls", "Boys", "Baby")
    val colorOptions = listOf("Yellow", "Magenta", "Gold", "Pink", "Red", "Blue", "Green", "White", "Black")
    val fabricOptions = listOf("Banarasi Silk", "Chanderi Cotton", "Georgette", "Pure Cotton", "Velvet", "Organza", "Art Silk", "Net", "Crepe")
    val sizeOptions = listOf("Free Size", "XS", "S", "M", "L", "XL", "XXL", "3XL")
    val priceOptions = listOf("Under ₹199", "₹199 - ₹499", "₹500 - ₹999", "₹1000 - ₹1999", "₹2000 and above")
    val ratingOptions = listOf("4.0 & above", "4.3 & above", "3.5 & above", "All Ratings")
    val occasionOptions = listOf("Festive", "Wedding", "Partywear", "Casual Daily", "Traditional")
    val discountOptions = listOf("10% and above", "30% and above", "50% and above", "70% and above")

    // Selected state
    var selectedCategories by remember {
        mutableStateOf(
            if (filterState.selectedCategory != "All") setOf(filterState.selectedCategory) else emptySet()
        )
    }
    var selectedGender by remember { mutableStateOf(filterState.selectedGender) }
    var selectedFabrics by remember { mutableStateOf(setOf<String>()) }
    var selectedSizes by remember { mutableStateOf(setOf<String>()) }
    var selectedColors by remember { mutableStateOf(setOf<String>()) }
    var selectedPriceRange by remember { mutableStateOf<String?>(null) }
    var selectedRatingVal by remember { mutableStateOf(filterState.minRating) }
    var selectedMallOnly by remember { mutableStateOf(filterState.onlyMahaDeals) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = null,
        modifier = modifier
            .fillMaxHeight(0.88f)
            .testTag("dual_pane_filter_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Header: FILTERS with close 'X' (screenshot 9:07)
            Surface(
                color = CardWhite,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FILTERS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("filter_close_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = DividerColor)

            // Dual Pane Body: Left Rail + Right Checkbox List (exact Meesho layout in screenshot 9:07)
            Row(modifier = Modifier.weight(1f)) {
                // Left Navigation Rail
                LazyColumn(
                    modifier = Modifier
                        .width(130.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFF3F3F7))
                ) {
                    items(FilterTab.values()) { tab ->
                        val isSelected = activeTab == tab
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeTab = tab }
                                .background(if (isSelected) CardWhite else Color(0xFFF3F3F7))
                                .padding(vertical = 14.dp, horizontal = 12.dp)
                                .testTag("filter_tab_${tab.name}")
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .width(4.dp)
                                        .height(28.dp)
                                        .background(MeeshoMagenta, RoundedCornerShape(2.dp))
                                )
                            }
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MeeshoMagenta else TextPrimary,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                // Right Content Pane
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(CardWhite)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    when (activeTab) {
                        FilterTab.CATEGORY -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "Category",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                // Category Search Bar (screenshot 9:07)
                                OutlinedTextField(
                                    value = categorySearchText,
                                    onValueChange = { categorySearchText = it },
                                    placeholder = { Text("Search", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF757575)) },
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF111111),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF111111),
                                        unfocusedTextColor = Color(0xFF111111),
                                        cursorColor = Color(0xFF111111),
                                        unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedBorderColor = MeeshoMagenta
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("filter_category_search")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val filteredCats = allCategoryOptions.filter {
                                    it.contains(categorySearchText, ignoreCase = true)
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(filteredCats) { catName ->
                                        val isChecked = selectedCategories.contains(catName)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedCategories = if (isChecked) {
                                                        selectedCategories - catName
                                                    } else {
                                                        selectedCategories + catName
                                                    }
                                                }
                                                .padding(vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { checked ->
                                                    selectedCategories = if (checked) {
                                                        selectedCategories + catName
                                                    } else {
                                                        selectedCategories - catName
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = MeeshoMagenta,
                                                    checkmarkColor = Color.White
                                                ),
                                                modifier = Modifier.testTag("checkbox_$catName")
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = catName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        FilterTab.GENDER -> {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Text("Gender", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                                }
                                items(genderOptions) { gender ->
                                    val isSelected = selectedGender.equals(gender, ignoreCase = true)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedGender = gender }
                                            .padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedGender = gender },
                                            colors = RadioButtonDefaults.colors(selectedColor = MeeshoMagenta)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(gender, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                    }
                                }
                            }
                        }

                        FilterTab.FABRIC -> {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Text("Fabric", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                                }
                                items(fabricOptions) { fabric ->
                                    val isChecked = selectedFabrics.contains(fabric)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedFabrics = if (isChecked) selectedFabrics - fabric else selectedFabrics + fabric
                                            }
                                            .padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { chk ->
                                                selectedFabrics = if (chk) selectedFabrics + fabric else selectedFabrics - fabric
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = MeeshoMagenta)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(fabric, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                    }
                                }
                            }
                        }

                        FilterTab.SIZE -> {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Text("Size", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                                }
                                items(sizeOptions) { size ->
                                    val isChecked = selectedSizes.contains(size)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedSizes = if (isChecked) selectedSizes - size else selectedSizes + size
                                            }
                                            .padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { chk ->
                                                selectedSizes = if (chk) selectedSizes + size else selectedSizes - size
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = MeeshoMagenta)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(size, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                    }
                                }
                            }
                        }

                        FilterTab.PRICE -> {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Text("Price Range", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                                }
                                items(priceOptions) { pOption ->
                                    val isChecked = selectedPriceRange == pOption
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedPriceRange = if (isChecked) null else pOption }
                                            .padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isChecked,
                                            onClick = { selectedPriceRange = if (isChecked) null else pOption },
                                            colors = RadioButtonDefaults.colors(selectedColor = MeeshoMagenta)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(pOption, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                    }
                                }
                            }
                        }

                        FilterTab.RATING -> {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Text("Rating", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                                }
                                items(ratingOptions) { rOption ->
                                    val rVal = when (rOption) {
                                        "4.3 & above" -> 4.3f
                                        "4.0 & above" -> 4.0f
                                        "3.5 & above" -> 3.5f
                                        else -> 0f
                                    }
                                    val isSelected = selectedRatingVal == rVal
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedRatingVal = rVal }
                                            .padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedRatingVal = rVal },
                                            colors = RadioButtonDefaults.colors(selectedColor = MeeshoMagenta)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(rOption, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                    }
                                }
                            }
                        }

                        else -> {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Text(activeTab.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                                }
                                items(occasionOptions) { occ ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { }
                                            .padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = false,
                                            onCheckedChange = { },
                                            colors = CheckboxDefaults.colors(checkedColor = MeeshoMagenta)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(occ, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = DividerColor)

            // Bottom Sticky Bar: "1000+ Products" and "Done" button (screenshot 9:07)
            Surface(
                color = CardWhite,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$matchingCount+ Products",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Button(
                        onClick = {
                            val targetCategory = if (selectedCategories.isNotEmpty()) {
                                selectedCategories.first()
                            } else {
                                "All"
                            }
                            val maxP = when (selectedPriceRange) {
                                "Under ₹199" -> 199
                                "₹199 - ₹499" -> 499
                                "₹500 - ₹999" -> 999
                                "₹1000 - ₹1999" -> 1999
                                else -> 5000
                            }
                            onApplyFilter(
                                filterState.copy(
                                    selectedCategory = targetCategory,
                                    selectedGender = selectedGender,
                                    minRating = selectedRatingVal,
                                    maxPrice = maxP
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("filter_done_btn")
                    ) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
