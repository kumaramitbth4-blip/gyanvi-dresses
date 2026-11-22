package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*
import com.example.ui.components.AppManagedImage
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppManagementScreen(
    config: AppManagementConfig,
    isDarkMode: Boolean,
    products: List<Product>,
    categories: List<CategoryItem>,
    initialTab: Int = 0,
    onBack: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectThemePreset: (DashboardThemePreset) -> Unit,
    onSelectLayoutMode: (HomeLayoutMode) -> Unit,
    onSelectCornerPreset: (CardCornerPreset) -> Unit,
    onUpdateTexts: ((AppTextsConfig) -> AppTextsConfig) -> Unit,
    onUpdateFeatures: ((AppFeaturesConfig) -> AppFeaturesConfig) -> Unit,
    onUpdateImages: ((AppImagesConfig) -> AppImagesConfig) -> Unit,
    onUpdateTypography: ((AppTypographyConfig) -> AppTypographyConfig) -> Unit,
    onEditProduct: (String, String, String, Int, Int, String, String, Boolean, Boolean, Boolean, Int, String?) -> Unit,
    onAddProduct: (String, String, Int, Int, String, String, Int, String?) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onResetProducts: () -> Unit,
    onUpdateCategory: (String, String, String, Int, String?) -> Unit,
    onAddCategory: (String, String, Int, String?) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onResetCategories: () -> Unit,
    onResetAll: () -> Unit,
    onOpenBackup: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 5)) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val primaryAccent = config.themePreset.accentColor
    val warmHighlight = config.themePreset.primaryColor

    Scaffold(
        topBar = {
            Surface(color = CardWhite, shadowElevation = 3.dp) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("app_management_back")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "App Management Studio",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = primaryAccent
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(shape = RoundedCornerShape(4.dp), color = config.themePreset.containerColor) {
                                        Text(
                                            text = "LIVE CMS",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TextPrimary,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Edit • Delete • Dashboard • Image • Text • लिखावट",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onOpenBackup,
                                modifier = Modifier.testTag("app_management_backup_btn")
                            ) {
                                Icon(Icons.Default.Backup, contentDescription = "App Backup", tint = Color(0xFF059669))
                            }
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        scrollState.animateScrollTo((scrollState.value + 550).coerceAtMost(scrollState.maxValue))
                                    }
                                },
                                modifier = Modifier.testTag("app_management_scroll_down_btn")
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Scroll Down", tint = primaryAccent)
                            }
                            TextButton(
                                onClick = { showResetConfirmDialog = true },
                                modifier = Modifier.testTag("app_management_reset_all")
                            ) {
                                Text("Reset", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    val tabs = listOf(
                        "Edit Dashboard" to Icons.Default.DashboardCustomize,
                        "Image Change" to Icons.Default.AddPhotoAlternate,
                        "Text Change" to Icons.Default.EditNote,
                        "लिखावट (Font)" to Icons.Default.TextFields,
                        "Options & Features" to Icons.Default.Tune,
                        "Catalog Edit/Delete" to Icons.Default.Inventory
                    )

                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = CardWhite,
                        contentColor = primaryAccent,
                        edgePadding = 12.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tabs.forEachIndexed { idx, (label, icon) ->
                            Tab(
                                selected = selectedTab == idx,
                                onClick = { selectedTab = idx },
                                modifier = Modifier.testTag("cms_tab_$idx"),
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        containerColor = config.themePreset.backgroundColor,
        modifier = modifier
            .fillMaxSize()
            .testTag("app_management_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedTab) {
                0 -> ChangeAndEditDashboardTab(
                    config = config,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode,
                    onSelectThemePreset = onSelectThemePreset,
                    onSelectLayoutMode = onSelectLayoutMode,
                    onSelectCornerPreset = onSelectCornerPreset,
                    onJumpToTab = { selectedTab = it }
                )

                1 -> ImageChangeDashboardTab(
                    images = config.images,
                    onUpdateImages = onUpdateImages
                )

                2 -> TextChangeDashboardTab(
                    texts = config.texts,
                    onUpdateTexts = onUpdateTexts,
                    onResetTexts = {
                        onUpdateTexts { AppTextsConfig() }
                        Toast.makeText(context, "All texts restored to default!", Toast.LENGTH_SHORT).show()
                    }
                )

                3 -> LikhavatChangeDashboardTab(
                    typography = config.typography,
                    brandTitle = config.texts.brandTitle,
                    onUpdateTypography = onUpdateTypography,
                    onResetTypography = {
                        onUpdateTypography { AppTypographyConfig() }
                        Toast.makeText(context, "Likhavat / Font reset to default!", Toast.LENGTH_SHORT).show()
                    }
                )

                4 -> AppFeaturesOptionsTab(
                    features = config.features,
                    onUpdateFeatures = onUpdateFeatures,
                    onRestoreAllFeatures = {
                        onUpdateFeatures { AppFeaturesConfig() }
                    },
                    onQuickBackup = onOpenBackup,
                    onScrollDown = {
                        scope.launch {
                            scrollState.animateScrollTo((scrollState.value + 550).coerceAtMost(scrollState.maxValue))
                        }
                    },
                    onScrollUp = {
                        scope.launch { scrollState.animateScrollTo(0) }
                    },
                    onNavigatePrevTab = {
                        selectedTab = (selectedTab - 1 + 6) % 6
                    },
                    onNavigateNextTab = {
                        selectedTab = (selectedTab + 1) % 6
                    }
                )

                5 -> AppCatalogAndCategoriesEditorTab(
                    products = products,
                    categories = categories,
                    onEditProduct = onEditProduct,
                    onAddProduct = onAddProduct,
                    onDeleteProduct = onDeleteProduct,
                    onResetProducts = onResetProducts,
                    onUpdateCategory = onUpdateCategory,
                    onAddCategory = onAddCategory,
                    onDeleteCategory = onDeleteCategory,
                    onResetCategories = onResetCategories
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset Entire App Design?", fontWeight = FontWeight.Bold) },
            text = {
                Text("यह सभी Theme, Image, Text, Likhavat (Font), Options और Catalog बदलावों को Default पर वापस कर देगा।")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAll()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "App reset to factory defaults!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Reset Everything", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// =========================================================================
// TAB 0: CHANGE & EDIT DASHBOARD
// =========================================================================

@Composable
private fun ChangeAndEditDashboardTab(
    config: AppManagementConfig,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectThemePreset: (DashboardThemePreset) -> Unit,
    onSelectLayoutMode: (HomeLayoutMode) -> Unit,
    onSelectCornerPreset: (CardCornerPreset) -> Unit,
    onJumpToTab: (Int) -> Unit
) {
    val context = LocalContext.current

    // Quick Overview Banner + Quick Jump Tiles
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(config.themePreset.accentColor, config.themePreset.primaryColor)
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "DASHBOARD MASTER CONTROLLER",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "ऐप का पूरा लुक, टेक्स्ट, इमेज और लिखावट बदलें",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Active Theme: ${config.themePreset.displayName} • Font: ${config.typography.fontStyle.displayName}",
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 11.sp
                )
            }
        }
    }

    // 4 Quick Editor Shortcuts
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QuickCmsShortcutCard("Image Change", Icons.Default.AddPhotoAlternate, Color(0xFF0284C7), Modifier.weight(1f)) {
            onJumpToTab(1)
        }
        QuickCmsShortcutCard("Text Change", Icons.Default.EditNote, Color(0xFF16A34A), Modifier.weight(1f)) {
            onJumpToTab(2)
        }
        QuickCmsShortcutCard("लिखावट", Icons.Default.TextFields, Color(0xFF9333EA), Modifier.weight(1f)) {
            onJumpToTab(3)
        }
        QuickCmsShortcutCard("Edit/Delete", Icons.Default.DeleteSweep, Color(0xFFDC2626), Modifier.weight(1f)) {
            onJumpToTab(5)
        }
    }

    // 1. Change Dashboard Theme Palette
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Change Dashboard Color Theme",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )
            Text(
                text = "ऐप के डैशबोर्ड का कलर थीम चुनें (Live Preview तुरंत दिखेगा)",
                fontSize = 11.sp,
                color = TextSecondary
            )

            DashboardThemePreset.entries.forEach { preset ->
                val selected = config.themePreset == preset
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) preset.containerColor else Color(0xFFF9FAFB),
                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) preset.accentColor else Color(0xFFE5E7EB)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectThemePreset(preset)
                            Toast.makeText(context, "Theme changed to ${preset.displayName}", Toast.LENGTH_SHORT).show()
                        }
                        .testTag("theme_preset_${preset.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(preset.primaryColor, preset.accentColor))
                                    )
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(preset.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text(preset.hindiLabel, fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        if (selected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = preset.accentColor)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF3F4F6))
                    .clickable { onToggleDarkMode(!isDarkMode) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, contentDescription = null, tint = config.themePreset.accentColor)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Dark Dashboard Mode", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                        Text("रात्रि मोड चालू या बंद करें", fontSize = 11.sp, color = TextSecondary)
                    }
                }
                Switch(checked = isDarkMode, onCheckedChange = onToggleDarkMode)
            }
        }
    }

    // 2. Edit Dashboard Grid & Card Shape
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Edit Dashboard Layout & Card Style",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )

            Text("Home Screen Product Grid Columns", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HomeLayoutMode.entries.forEach { mode ->
                    val active = config.homeLayoutMode == mode
                    FilterChip(
                        selected = active,
                        onClick = { onSelectLayoutMode(mode) },
                        label = {
                            Text(mode.label, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                        },
                        leadingIcon = if (active) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.fillMaxWidth().testTag("layout_mode_${mode.name.lowercase()}")
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("Card Corner Style", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CardCornerPreset.entries.forEach { corner ->
                    val active = config.cardCornerPreset == corner
                    FilterChip(
                        selected = active,
                        onClick = { onSelectCornerPreset(corner) },
                        label = { Text(corner.label, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickCmsShortcutCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .border(1.dp, tint.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(shape = CircleShape, color = tint.copy(alpha = 0.14f), modifier = Modifier.size(32.dp)) {
                Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.padding(6.dp))
            }
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
        }
    }
}

// =========================================================================
// TAB 1: IMAGE CHANGE DASHBOARD
// =========================================================================

@Composable
private fun ImageChangeDashboardTab(
    images: AppImagesConfig,
    onUpdateImages: ((AppImagesConfig) -> AppImagesConfig) -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Image Change Dashboard", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E3A8A))
            Text(
                text = "गैलरी से अपनी फोटो चुनें या नीचे दिए Pre-built बैनर/ड्रेस इमेज से तुरंत बदलें या डिलीट करें।",
                fontSize = 11.sp,
                color = Color(0xFF1E40AF)
            )
        }
    }

    // 1. Brand Profile / Logo Image
    ImageSlotEditorCard(
        title = "Top Bar Brand / Profile Photo",
        subtitle = "होम स्क्रीन के ऊपर बाईं तरफ दिखने वाला लोगो/अवतार",
        drawableRes = images.brandAvatarDrawable ?: R.drawable.gyanvi_brand_logo,
        customUri = images.brandAvatarCustomUri,
        isEnabled = true,
        showEnableToggle = false,
        onToggleEnabled = {},
        onPickCustomUri = { uri ->
            onUpdateImages { it.copy(brandAvatarCustomUri = uri) }
            Toast.makeText(context, "Brand Avatar updated!", Toast.LENGTH_SHORT).show()
        },
        onSelectDrawable = { resId ->
            onUpdateImages { it.copy(brandAvatarCustomUri = null, brandAvatarDrawable = resId) }
            Toast.makeText(context, "Brand Avatar preset applied!", Toast.LENGTH_SHORT).show()
        },
        onResetOrDelete = {
            onUpdateImages { it.copy(brandAvatarCustomUri = null, brandAvatarDrawable = R.drawable.gyanvi_brand_logo) }
            Toast.makeText(context, "Brand Logo reset to Gyanvi official logo", Toast.LENGTH_SHORT).show()
        },
        testTag = "img_editor_brand_avatar"
    )

    // 2. Hero Banner Slide 1
    ImageSlotEditorCard(
        title = "Hero Banner Slide 1 (Main Banner)",
        subtitle = "होम पेज का पहला बड़ा सेल बैनर",
        drawableRes = images.slide1Drawable,
        customUri = images.slide1CustomUri,
        isEnabled = images.slide1Enabled,
        showEnableToggle = true,
        onToggleEnabled = { en -> onUpdateImages { it.copy(slide1Enabled = en) } },
        onPickCustomUri = { uri ->
            onUpdateImages { it.copy(slide1CustomUri = uri, slide1Enabled = true) }
            Toast.makeText(context, "Slide 1 image updated from gallery!", Toast.LENGTH_SHORT).show()
        },
        onSelectDrawable = { resId ->
            onUpdateImages { it.copy(slide1CustomUri = null, slide1Drawable = resId, slide1Enabled = true) }
        },
        onResetOrDelete = {
            onUpdateImages {
                it.copy(
                    slide1CustomUri = null,
                    slide1Drawable = R.drawable.gyanvi_hero_banner_1793416998706
                )
            }
            Toast.makeText(context, "Slide 1 reset to default banner", Toast.LENGTH_SHORT).show()
        },
        testTag = "img_editor_slide1"
    )

    // 3. Hero Banner Slide 2
    ImageSlotEditorCard(
        title = "Hero Banner Slide 2 (Festive Showcase)",
        subtitle = "दूसरा स्लाइडर बैनर और उसकी साइड इमेज",
        drawableRes = images.slide2Drawable,
        customUri = images.slide2CustomUri,
        isEnabled = images.slide2Enabled,
        showEnableToggle = true,
        onToggleEnabled = { en -> onUpdateImages { it.copy(slide2Enabled = en) } },
        onPickCustomUri = { uri ->
            onUpdateImages { it.copy(slide2CustomUri = uri, slide2Enabled = true) }
            Toast.makeText(context, "Slide 2 image updated!", Toast.LENGTH_SHORT).show()
        },
        onSelectDrawable = { resId ->
            onUpdateImages { it.copy(slide2CustomUri = null, slide2Drawable = resId, slide2Enabled = true) }
        },
        onResetOrDelete = {
            onUpdateImages { it.copy(slide2CustomUri = null, slide2Drawable = R.drawable.dress_lehenga_1793417067159) }
        },
        testTag = "img_editor_slide2"
    )

    // 4. Hero Banner Slide 3
    ImageSlotEditorCard(
        title = "Hero Banner Slide 3 (Marketplace Banner)",
        subtitle = "तीसरा स्लाइडर बैनर (कस्टम फोटो जोड़ सकते हैं)",
        drawableRes = images.slide3Drawable ?: R.drawable.search_banner_illustration_1793417120669,
        customUri = images.slide3CustomUri,
        isEnabled = images.slide3Enabled,
        showEnableToggle = true,
        onToggleEnabled = { en -> onUpdateImages { it.copy(slide3Enabled = en) } },
        onPickCustomUri = { uri ->
            onUpdateImages { it.copy(slide3CustomUri = uri, slide3Enabled = true) }
            Toast.makeText(context, "Slide 3 photo added!", Toast.LENGTH_SHORT).show()
        },
        onSelectDrawable = { resId ->
            onUpdateImages { it.copy(slide3CustomUri = null, slide3Drawable = resId, slide3Enabled = true) }
        },
        onResetOrDelete = {
            onUpdateImages { it.copy(slide3CustomUri = null, slide3Drawable = null) }
            Toast.makeText(context, "Slide 3 custom image cleared!", Toast.LENGTH_SHORT).show()
        },
        testTag = "img_editor_slide3"
    )
}

@Composable
private fun ImageSlotEditorCard(
    title: String,
    subtitle: String,
    drawableRes: Int,
    customUri: String?,
    isEnabled: Boolean,
    showEnableToggle: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onPickCustomUri: (String) -> Unit,
    onSelectDrawable: (Int) -> Unit,
    onResetOrDelete: () -> Unit,
    testTag: String
) {
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            onPickCustomUri(uri.toString())
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth().testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text(subtitle, fontSize = 11.sp, color = TextSecondary)
                }
                if (showEnableToggle) {
                    Switch(checked = isEnabled, onCheckedChange = onToggleEnabled)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
            ) {
                AppManagedImage(
                    drawableRes = drawableRes,
                    customUri = customUri,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize()
                )
                if (!isEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("HIDDEN FROM DASHBOARD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Change from Gallery", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onResetOrDelete,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Reset", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reset", color = Color(0xFFDC2626), fontSize = 11.sp)
                }
            }

            Text("Quick Preset Photos:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(AppDrawablePresets.all) { preset ->
                    val selected = customUri.isNullOrBlank() && drawableRes == preset.drawableRes
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectDrawable(preset.drawableRes) },
                        label = { Text(preset.label, fontSize = 10.sp) }
                    )
                }
            }
        }
    }
}

// =========================================================================
// TAB 2: TEXT CHANGE DASHBOARD
// =========================================================================

@Composable
private fun TextChangeDashboardTab(
    texts: AppTextsConfig,
    onUpdateTexts: ((AppTextsConfig) -> AppTextsConfig) -> Unit,
    onResetTexts: () -> Unit
) {
    val context = LocalContext.current
    var filterQuery by remember { mutableStateOf("") }

    // Draft editable strings synced with current config
    var brandTitle by remember(texts.brandTitle) { mutableStateOf(texts.brandTitle) }
    var brandHindi by remember(texts.brandHindiTitle) { mutableStateOf(texts.brandHindiTitle) }
    var announcement by remember(texts.announcementText) { mutableStateOf(texts.announcementText) }
    var searchHint by remember(texts.searchPlaceholder) { mutableStateOf(texts.searchPlaceholder) }
    var deliveryPrefix by remember(texts.deliveryPrefix) { mutableStateOf(texts.deliveryPrefix) }
    var saleBadge by remember(texts.saleBadgeText) { mutableStateOf(texts.saleBadgeText) }
    var saleTimerLabel by remember(texts.saleTimerLabel) { mutableStateOf(texts.saleTimerLabel) }
    var dealsFirst by remember(texts.dealsHeadingFirst) { mutableStateOf(texts.dealsHeadingFirst) }
    var dealsSecond by remember(texts.dealsHeadingSecond) { mutableStateOf(texts.dealsHeadingSecond) }
    var slide2Title by remember(texts.slide2Title) { mutableStateOf(texts.slide2Title) }
    var slide3Title by remember(texts.slide3Title) { mutableStateOf(texts.slide3Title) }
    var devTitle by remember(texts.devProgressTitle) { mutableStateOf(texts.devProgressTitle) }
    var addCartText by remember(texts.addToCartButtonText) { mutableStateOf(texts.addToCartButtonText) }
    var tabHome by remember(texts.tabHomeLabel) { mutableStateOf(texts.tabHomeLabel) }
    var tabCats by remember(texts.tabCategoriesLabel) { mutableStateOf(texts.tabCategoriesLabel) }
    var tabMall by remember(texts.tabMallLabel) { mutableStateOf(texts.tabMallLabel) }
    var tabVideo by remember(texts.tabVideoFindsLabel) { mutableStateOf(texts.tabVideoFindsLabel) }
    var tabOrders by remember(texts.tabOrdersLabel) { mutableStateOf(texts.tabOrdersLabel) }
    var footerText by remember(texts.footerText) { mutableStateOf(texts.footerText) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Text Change Dashboard", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF14532D))
                    Text("ऐप का कोई भी टेक्स्ट बदलें, नया सूचना संदेश जोड़ें या टेक्स्ट हटाएं", fontSize = 11.sp, color = Color(0xFF166534))
                }
                OutlinedButton(onClick = onResetTexts, modifier = Modifier.testTag("reset_all_texts_btn")) {
                    Text("Reset Texts", fontSize = 11.sp)
                }
            }
            OutlinedTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                placeholder = { Text("Search text field (e.g. Sale, Brand, Tab)...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = CardWhite, unfocusedContainerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    val fields = listOf(
        Triple("Top Brand Title (English)", brandTitle) { v: String -> brandTitle = v },
        Triple("Top Brand Subtitle (Hindi)", brandHindi) { v: String -> brandHindi = v },
        Triple("Live Announcement Notice Bar (Blank to Hide)", announcement) { v: String -> announcement = v },
        Triple("Search Bar Placeholder Text", searchHint) { v: String -> searchHint = v },
        Triple("Delivery Prefix Text", deliveryPrefix) { v: String -> deliveryPrefix = v },
        Triple("Sale Badge Text", saleBadge) { v: String -> saleBadge = v },
        Triple("Sale Timer Label", saleTimerLabel) { v: String -> saleTimerLabel = v },
        Triple("Deals Heading (First Word)", dealsFirst) { v: String -> dealsFirst = v },
        Triple("Deals Heading (Second Part)", dealsSecond) { v: String -> dealsSecond = v },
        Triple("Banner Slide 2 Title", slide2Title) { v: String -> slide2Title = v },
        Triple("Banner Slide 3 Title", slide3Title) { v: String -> slide3Title = v },
        Triple("Development Strip Title", devTitle) { v: String -> devTitle = v },
        Triple("Product Card CTA Button Text", addCartText) { v: String -> addCartText = v },
        Triple("Bottom Tab 1: Home Label", tabHome) { v: String -> tabHome = v },
        Triple("Bottom Tab 2: Categories Label", tabCats) { v: String -> tabCats = v },
        Triple("Bottom Tab 3: Mall Label", tabMall) { v: String -> tabMall = v },
        Triple("Bottom Tab 4: Video Finds Label", tabVideo) { v: String -> tabVideo = v },
        Triple("Bottom Tab 5: Orders Label", tabOrders) { v: String -> tabOrders = v },
        Triple("Footer Banner Text", footerText) { v: String -> footerText = v }
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            fields.filter { filterQuery.isBlank() || it.first.contains(filterQuery, true) || it.second.contains(filterQuery, true) }
                .forEach { (label, value, onChange) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = value,
                            onValueChange = onChange,
                            label = { Text(label, fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        if (value.isNotEmpty()) {
                            IconButton(
                                onClick = { onChange("") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Clear Text", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

            Button(
                onClick = {
                    onUpdateTexts {
                        it.copy(
                            brandTitle = brandTitle,
                            brandHindiTitle = brandHindi,
                            announcementText = announcement.trim(),
                            searchPlaceholder = searchHint,
                            deliveryPrefix = deliveryPrefix,
                            saleBadgeText = saleBadge,
                            saleTimerLabel = saleTimerLabel,
                            dealsHeadingFirst = dealsFirst,
                            dealsHeadingSecond = dealsSecond,
                            slide2Title = slide2Title,
                            slide3Title = slide3Title,
                            devProgressTitle = devTitle,
                            addToCartButtonText = addCartText.ifBlank { "Add to Cart" },
                            tabHomeLabel = tabHome.ifBlank { "Home" },
                            tabCategoriesLabel = tabCats.ifBlank { "Categories" },
                            tabMallLabel = tabMall.ifBlank { "Mall" },
                            tabVideoFindsLabel = tabVideo.ifBlank { "Video Finds" },
                            tabOrdersLabel = tabOrders.ifBlank { "My Orders" },
                            footerText = footerText
                        )
                    }
                    Toast.makeText(context, "All app texts saved & updated live!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("save_all_texts_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save & Apply Text Changes Live", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// =========================================================================
// TAB 3: LIKHAVAT (TYPOGRAPHY / FONT STYLE) CHANGE DASHBOARD
// =========================================================================

@Composable
private fun LikhavatChangeDashboardTab(
    typography: AppTypographyConfig,
    brandTitle: String,
    onUpdateTypography: ((AppTypographyConfig) -> AppTypographyConfig) -> Unit,
    onResetTypography: () -> Unit
) {
    val context = LocalContext.current
    val previewTypo = remember(typography) { buildGyanviTypography(typography) }

    // 1. Live Likhavat Preview Card
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("LIVE लिखावट PREVIEW", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                TextButton(
                    onClick = onResetTypography,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("Reset Font", fontSize = 11.sp)
                }
            }
            Text(
                text = brandTitle.ifBlank { "Gyanvi Dresses Boutique" },
                style = previewTypo.headlineMedium,
                color = TextPrimary
            )
            Text(
                text = "ज्ञानवी ड्रेसेस — शाही साड़ी, लहंगा व कुर्ती कलेक्शन • ₹599",
                style = previewTypo.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Font: ${typography.fontStyle.displayName} • Size: ${(typography.fontScale * 100).roundToInt()}%",
                style = previewTypo.labelSmall,
                color = TextSecondary
            )
        }
    }

    // 2. Font Family Selector (Likhavat Style)
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "लिखावट स्टाइल चुनें (Choose Font Style)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )
            AppFontStyle.entries.forEach { style ->
                val selected = typography.fontStyle == style
                val styleFamily = displayFontFamilyFor(style)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF9FAFB),
                    border = BorderStroke(
                        if (selected) 2.dp else 1.dp,
                        if (selected) MaterialTheme.colorScheme.primary else Color(0xFFE5E7EB)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onUpdateTypography { it.copy(fontStyle = style) }
                            Toast.makeText(context, "Likhavat changed to ${style.displayName}", Toast.LENGTH_SHORT).show()
                        }
                        .testTag("font_style_${style.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = style.sampleText,
                                fontFamily = styleFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${style.displayName} • ${style.subtitle}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        if (selected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // 3. Size, Weight & Spacing Controls
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "अक्षर आकार व मोटाई (Text Scale, Weight & Spacing)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
            )

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Text Size Scale", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("${(typography.fontScale * 100).roundToInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = typography.fontScale,
                    onValueChange = { scale ->
                        onUpdateTypography { it.copy(fontScale = scale) }
                    },
                    valueRange = 0.85f..1.25f,
                    steps = 7
                )
            }

            Text("Heading Boldness", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeadingWeightStyle.entries.forEach { w ->
                    FilterChip(
                        selected = typography.headingWeight == w,
                        onClick = { onUpdateTypography { it.copy(headingWeight = w) } },
                        label = { Text(w.label, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text("Letter Spacing (अक्षर दूरी)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LetterSpacingPreset.entries.forEach { spPreset ->
                    FilterChip(
                        selected = typography.letterSpacing == spPreset,
                        onClick = { onUpdateTypography { it.copy(letterSpacing = spPreset) } },
                        label = { Text(spPreset.label, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
