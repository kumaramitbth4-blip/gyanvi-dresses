package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.AppManagementConfig
import com.example.model.CategoryItem
import com.example.model.Product
import com.example.ui.components.AppManagedImage
import com.example.ui.components.FloatingQuickNavigationControls
import com.example.ui.components.HomeSocialMediaAdsCarousel
import com.example.ui.components.ProductCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    products: List<Product>,
    wishlistIds: Set<String>,
    countdownSeconds: Int,
    selectedCategory: String,
    selectedGender: String,
    selectedSortLabel: String,
    categories: List<CategoryItem> = SampleData.categoryList,
    managementConfig: AppManagementConfig = LocalAppConfig.current,
    onCategorySelect: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onWishlistToggle: (String) -> Unit,
    onAddToCart: (Product) -> Unit,
    onOpenSort: () -> Unit,
    onOpenCategoryFilter: () -> Unit,
    onOpenGenderFilter: () -> Unit,
    onOpenFilter: () -> Unit,
    onShareProduct: ((Product) -> Unit)? = null,
    onOpenDevelopmentProgress: () -> Unit = {},
    onQuickBackupNow: () -> Unit = {},
    onOpenBackupManager: () -> Unit = {},
    onOpenAppManagement: () -> Unit = {},
    onSideNavigateBack: () -> Unit = {},
    onSideNavigateNext: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onOpenCart: () -> Unit = {},
    onOpenWishlist: () -> Unit = {},
    onOpenAccount: () -> Unit = {},
    onToggleDarkMode: () -> Unit = {},
    onSocialAdClick: (String) -> Unit = {},
    recentlyViewedProducts: List<Product> = emptyList(),
    modifier: Modifier = Modifier
) {
    val features = managementConfig.features
    val texts = managementConfig.texts
    val images = managementConfig.images
    val accent = managementConfig.themePreset.accentColor
    val primaryTint = managementConfig.themePreset.primaryColor
    val bgTint = managementConfig.themePreset.backgroundColor
    val columnsCount = managementConfig.homeLayoutMode.columns.coerceIn(1, 3)

    val gridState = rememberLazyGridState()
    val categoryRowState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val canScrollDown by remember { derivedStateOf { gridState.canScrollForward } }

    // Breakdown countdown seconds into Days, Hours, Minutes, Seconds
    val days = countdownSeconds / 86400
    val hours = (countdownSeconds % 86400) / 3600
    val minutes = (countdownSeconds % 3600) / 60
    val seconds = countdownSeconds % 60

    val activeSlideIds = remember(images.slide1Enabled, images.slide2Enabled, images.slide3Enabled) {
        buildList {
            if (images.slide1Enabled) add(0)
            if (images.slide2Enabled) add(1)
            if (images.slide3Enabled) add(2)
        }
    }
    val bannerPagerState = rememberPagerState(pageCount = { activeSlideIds.size.coerceAtLeast(1) })

    Box(modifier = modifier.fillMaxSize()) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(columnsCount),
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 78.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(bgTint)
            .testTag("home_screen_grid")
    ) {
        // Optional Live Custom Announcement Notice Bar (edited via Text Change Dashboard)
        if (features.showAnnouncementTicker && texts.announcementText.isNotBlank()) {
            item(span = { GridItemSpan(columnsCount) }) {
                Surface(
                    color = accent,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("home_announcement_bar")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = texts.announcementText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 1. Promotional Banners Slider (HorizontalPager) with Left/Right Side Tir buttons
        if (features.showHeroSlider && activeSlideIds.isNotEmpty()) {
            item(span = { GridItemSpan(columnsCount) }) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().height(170.dp)) {
                        HorizontalPager(
                            state = bannerPagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("hero_banner")
                        ) { pageIdx ->
                        when (activeSlideIds[pageIdx]) {
                            0 -> {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { onCategorySelect("All") }
                                ) {
                                    AppManagedImage(
                                        drawableRes = images.slide1Drawable,
                                        customUri = images.slide1CustomUri,
                                        contentDescription = "Gyanvi Maha Dhamaka Sale",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            1 -> {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { onCategorySelect("Lehengas") }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFF880E4F), accent, primaryTint)
                                                )
                                            )
                                            .padding(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.align(Alignment.CenterStart).fillMaxWidth(0.66f),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (texts.slide2Badge.isNotBlank()) {
                                                Surface(shape = RoundedCornerShape(6.dp), color = primaryTint) {
                                                    Text(
                                                        text = texts.slide2Badge,
                                                        color = TextPrimary,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = texts.slide2Title,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 2
                                            )
                                            if (texts.slide2Subtitle.isNotBlank()) {
                                                Text(
                                                    text = texts.slide2Subtitle,
                                                    color = Color.White.copy(alpha = 0.9f),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 2
                                                )
                                            }
                                        }
                                        AppManagedImage(
                                            drawableRes = images.slide2Drawable,
                                            customUri = images.slide2CustomUri,
                                            contentDescription = "Bridal Lehenga Banner",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(110.dp, 138.dp)
                                                .align(Alignment.CenterEnd)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                    }
                                }
                            }
                            else -> {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { onOpenDevelopmentProgress() }
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        if (!images.slide3CustomUri.isNullOrBlank() || images.slide3Drawable != null) {
                                            AppManagedImage(
                                                drawableRes = images.slide3Drawable,
                                                customUri = images.slide3CustomUri,
                                                contentDescription = "Marketplace Banner",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.42f))
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            listOf(Color(0xFF1E3A8A), Color(0xFF6D28D9), accent)
                                                        )
                                                    )
                                            )
                                        }
                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.CenterStart)
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (texts.slide3Badge.isNotBlank()) {
                                                Text(
                                                    text = texts.slide3Badge,
                                                    color = primaryTint,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                            Text(
                                                text = texts.slide3Title,
                                                color = Color.White,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (texts.slide3Subtitle.isNotBlank()) {
                                                Text(
                                                    text = texts.slide3Subtitle,
                                                    color = Color.White.copy(alpha = 0.9f),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                        if (activeSlideIds.size > 1) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.45f),
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 6.dp)
                                    .size(32.dp)
                                    .clickable {
                                        scope.launch {
                                            val prev = if (bannerPagerState.currentPage > 0) bannerPagerState.currentPage - 1 else activeSlideIds.lastIndex
                                            bannerPagerState.animateScrollToPage(prev)
                                        }
                                    }
                                    .testTag("hero_left_tir_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous Banner",
                                    tint = Color.White,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.45f),
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 6.dp)
                                    .size(32.dp)
                                    .clickable {
                                        scope.launch {
                                            val next = (bannerPagerState.currentPage + 1) % activeSlideIds.size
                                            bannerPagerState.animateScrollToPage(next)
                                        }
                                    }
                                    .testTag("hero_right_tir_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next Banner",
                                    tint = Color.White,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                    // Slider Indicator Dots
                    if (activeSlideIds.size > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(activeSlideIds.size) { idx ->
                                val active = bannerPagerState.currentPage == idx
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .height(6.dp)
                                        .width(if (active) 18.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(if (active) accent else Color(0xFFD1D5DB))
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Meesho-Style Countdown Timer Banner
        if (features.showCountdownBanner) {
            item(span = { GridItemSpan(columnsCount) }) {
                Surface(
                    color = managementConfig.themePreset.containerColor,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, primaryTint.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenFilter() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Badge
                        if (texts.saleBadgeText.isNotBlank()) {
                            Surface(
                                color = SaleRed,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = texts.saleBadgeText,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Timer text
                        Text(
                            text = texts.saleTimerLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        // Digit boxes
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            TimerDigitBox(String.format("%02d", days), "d")
                            Text(":", fontWeight = FontWeight.Bold, color = SaleRed)
                            TimerDigitBox(String.format("%02d", hours), "h")
                            Text(":", fontWeight = FontWeight.Bold, color = SaleRed)
                            TimerDigitBox(String.format("%02d", minutes), "m")
                            Text(":", fontWeight = FontWeight.Bold, color = SaleRed)
                            TimerDigitBox(String.format("%02d", seconds), "s")
                        }
                    }
                }
            }
        }

        // 2A-Social. Sponsored Social Media Ads Carousel (Instagram / Facebook / YouTube / WhatsApp)
        if (managementConfig.socialAds.adsEnabled &&
            managementConfig.socialAds.showOnHome &&
            managementConfig.socialAds.campaigns.any { it.isActive && it.showOnHomeBanner }
        ) {
            item(span = { GridItemSpan(columnsCount) }) {
                HomeSocialMediaAdsCarousel(
                    config = managementConfig.socialAds,
                    accentColor = accent,
                    onAdClick = onSocialAdClick
                )
            }
        }

        // 2B. Weekly Development Progress & 15-Module Tap-to-Setup Strip
        if (features.showDevProgressStrip) {
            item(span = { GridItemSpan(columnsCount) }) {
                Surface(
                    color = Color(0xFFFDF2F8),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDevelopmentProgress() }
                        .testTag("weekly_dev_progress_banner")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(shape = CircleShape, color = accent, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.padding(6.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = texts.devProgressTitle,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary
                                )
                                if (texts.devProgressSubtitle.isNotBlank()) {
                                    Text(
                                        text = texts.devProgressSubtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                        if (texts.devProgressCta.isNotBlank()) {
                            Surface(shape = RoundedCornerShape(8.dp), color = accent) {
                                Text(
                                    text = texts.devProgressCta,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Circular Category Navigation Strip with Left/Right Side Tir Arrows
        if (features.showCategoryStrip && categories.isNotEmpty()) {
            item(span = { GridItemSpan(columnsCount) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CardWhite,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                scope.launch { categoryRowState.animateScrollBy(-240f) }
                            }
                            .testTag("category_left_tir_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Scroll Categories Left", tint = accent, modifier = Modifier.padding(3.dp))
                    }
                    val displayCategories = remember(categories) {
                        val activeSorted = categories.filter { it.isActive }.sortedBy { it.sortOrder }
                        val allChip = com.example.model.CategoryItem(
                            id = "all",
                            name = "All",
                            imageRes = com.example.R.drawable.gyanvi_brand_logo,
                            offerBadge = "SALE LIVE",
                            sortOrder = 0
                        )
                        listOf(allChip) + activeSorted
                    }
                    LazyRow(
                        state = categoryRowState,
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        items(displayCategories, key = { it.id }) { category ->
                            CategoryCircleItem(
                                category = category,
                                isSelected = selectedCategory.equals(category.name, ignoreCase = true) ||
                                    (selectedCategory == "All" && category.id == "all"),
                                accentColor = accent,
                                ringColor = primaryTint,
                                onClick = {
                                    onCategorySelect(if (category.id == "all") "All" else category.name)
                                }
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = CardWhite,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                scope.launch { categoryRowState.animateScrollBy(240f) }
                            }
                            .testTag("category_right_tir_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Scroll Categories Right", tint = accent, modifier = Modifier.padding(3.dp))
                    }
                }
            }
        }

        // 4. Meesho 4-Segment Horizontal Filter Bar
        if (features.showFilterBar) {
            item(span = { GridItemSpan(columnsCount) }) {
                Surface(
                    color = CardWhite,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Sort segment
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onOpenSort() }
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.SwapVert, contentDescription = "Sort", tint = TextPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (selectedSortLabel.isNotBlank()) selectedSortLabel.take(7) else "Sort",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }

                        VerticalDivider(modifier = Modifier.height(24.dp), color = Color(0xFFE0E0E0))

                        // 2. Category segment
                        Row(
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxHeight()
                                .clickable { onOpenCategoryFilter() }
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (selectedCategory != "All") selectedCategory.take(9) else "Category",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCategory != "All") accent else TextPrimary,
                                maxLines = 1
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }

                        VerticalDivider(modifier = Modifier.height(24.dp), color = Color(0xFFE0E0E0))

                        // 3. Gender segment
                        Row(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight()
                                .clickable { onOpenGenderFilter() }
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (selectedGender != "All") selectedGender else "Gender",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedGender != "All") accent else TextPrimary,
                                maxLines = 1
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }

                        VerticalDivider(modifier = Modifier.height(24.dp), color = Color(0xFFE0E0E0))

                        // 4. Filters segment
                        Row(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight()
                                .clickable { onOpenFilter() }
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filters", tint = accent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Filters",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                        }
                    }
                }
            }
        }

        // 4B. Recently Viewed Products Strip (हाल ही में देखे गए)
        if (recentlyViewedProducts.isNotEmpty()) {
            item(span = { GridItemSpan(columnsCount) }) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth().testTag("home_recently_viewed_card")
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recently Viewed (हाल ही में देखे गए)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${recentlyViewedProducts.size} items",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(recentlyViewedProducts, key = { "rv_${it.id}" }) { rv ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .width(116.dp)
                                        .clickable { onProductClick(rv) }
                                        .testTag("recently_viewed_${rv.id}")
                                ) {
                                    Column {
                                        AppManagedImage(
                                            drawableRes = rv.imageRes,
                                            customUri = rv.customImageUri,
                                            contentDescription = rv.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(92.dp)
                                                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                        )
                                        Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = rv.name,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "₹${rv.price}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Black,
                                                color = accent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. "BEST PRICE" & Maha Deals Header
        item(span = { GridItemSpan(columnsCount) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (texts.dealsHeadingFirst.isNotBlank()) {
                        Text(
                            text = texts.dealsHeadingFirst,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = GreenDiscount
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (texts.dealsHeadingSecond.isNotBlank()) {
                        Text(
                            text = texts.dealsHeadingSecond,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = accent
                        )
                    }
                }

                Text(
                    text = "${products.size} Products Available",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 6. Product Grid Items
        if (products.isEmpty()) {
            item(span = { GridItemSpan(columnsCount) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No dresses match your current filters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onCategorySelect("All") },
                            colors = ButtonDefaults.buttonColors(containerColor = accent)
                        ) {
                            Text("Reset Category", color = Color.White)
                        }
                    }
                }
            }
        } else {
            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    isWishlisted = wishlistIds.contains(product.id),
                    managementConfig = managementConfig,
                    onProductClick = { onProductClick(product) },
                    onWishlistToggle = { onWishlistToggle(product.id) },
                    onAddToCart = { onAddToCart(product) },
                    onShareClick = { onShareProduct?.invoke(product) }
                )
            }
        }

        // 7. Editable Footer Note
        if (texts.footerText.isNotBlank()) {
            item(span = { GridItemSpan(columnsCount) }) {
                Text(
                    text = texts.footerText,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                )
            }
        }
    }

    if (features.showFloatingQuickControls) {
        FloatingQuickNavigationControls(
            accentColor = accent,
            canScrollDown = canScrollDown,
            onBackupQuickClick = onQuickBackupNow,
            onOpenBackupManager = onOpenBackupManager,
            onOpenAppManagement = onOpenAppManagement,
            onScrollDownClick = {
                scope.launch {
                    gridState.animateScrollBy(650f)
                }
            },
            onScrollUpClick = {
                scope.launch {
                    gridState.animateScrollToItem(0)
                }
            },
            onLeftArrowClick = onSideNavigateBack,
            onRightArrowClick = onSideNavigateNext,
            onNavigateBack = onSideNavigateBack,
            onOpenSearch = onOpenSearch,
            onOpenCart = onOpenCart,
            onOpenWishlist = onOpenWishlist,
            onOpenAccount = onOpenAccount,
            onToggleDarkMode = onToggleDarkMode
        )
    }
    }
}

@Composable
private fun TimerDigitBox(digits: String, unit: String) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Surface(
            color = SaleRed,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = digits,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
        Text(
            text = unit,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun CategoryCircleItem(
    category: CategoryItem,
    isSelected: Boolean,
    accentColor: Color,
    ringColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() }
            .testTag("category_${category.id}")
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 2.5.dp else 1.5.dp,
                    color = if (isSelected) accentColor else ringColor,
                    shape = CircleShape
                )
                .background(Color(0xFFFFFDE7)),
            contentAlignment = Alignment.Center
        ) {
            AppManagedImage(
                drawableRes = category.imageRes,
                customUri = category.customImageUri,
                contentDescription = category.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )

            if (category.offerBadge.isNotBlank()) {
                Surface(
                    color = SaleRed,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 2.dp)
                ) {
                    Text(
                        text = category.offerBadge,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) accentColor else TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp
        )
    }
}
