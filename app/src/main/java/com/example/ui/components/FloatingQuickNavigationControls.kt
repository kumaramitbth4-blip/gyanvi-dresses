package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Floating Quick Controls Overlay providing:
 * 1. Side Tir Button (◀ / ▶ edge tab opening Side Quick Drawer with all quick action buttons)
 * 2. App Backup Button (1-tap full app backup + Backup Manager)
 * 3. Scroll Down Button (↓) + Scroll Up (↑)
 * 4. Tin Tir Button (◀  ▼  ▶ 3-Arrow Navigation Capsule)
 */
@Composable
fun FloatingQuickNavigationControls(
    accentColor: Color,
    canScrollDown: Boolean = true,
    onBackupQuickClick: () -> Unit,
    onOpenBackupManager: () -> Unit,
    onOpenAppManagement: () -> Unit,
    onScrollDownClick: () -> Unit,
    onScrollUpClick: () -> Unit,
    onLeftArrowClick: () -> Unit,
    onRightArrowClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onOpenSearch: () -> Unit = {},
    onOpenCart: () -> Unit = {},
    onOpenWishlist: () -> Unit = {},
    onOpenAccount: () -> Unit = {},
    onToggleDarkMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isSideDrawerOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. SIDE TIR BUTTON (Anchored on right side edge — opens Slide Quick Panel)
        Surface(
            color = accentColor,
            shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp),
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .defaultMinSize(minWidth = 38.dp, minHeight = 56.dp)
                .clickable { isSideDrawerOpen = !isSideDrawerOpen }
                .testTag("side_tir_button")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isSideDrawerOpen) {
                        Icons.AutoMirrored.Filled.KeyboardArrowRight
                    } else {
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft
                    },
                    contentDescription = "Side Tir Menu",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "तीर",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Slide-out Side Quick Panel triggered by Side Tir Button
        AnimatedVisibility(
            visible = isSideDrawerOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 42.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.35f)),
                modifier = Modifier
                    .width(225.dp)
                    .heightIn(max = 420.dp)
                    .testTag("side_tir_drawer_panel")
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "All Quick Buttons",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = accentColor
                        )
                        IconButton(
                            onClick = { isSideDrawerOpen = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Side Panel", modifier = Modifier.size(16.dp))
                        }
                    }

                    // Compact Row of Circular Icon Buttons (Search, Cart, Wishlist, Account, Theme)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickMiniCircleButton(Icons.Default.Search, "Search", Color(0xFF0284C7)) {
                            isSideDrawerOpen = false
                            onOpenSearch()
                        }
                        QuickMiniCircleButton(Icons.Default.ShoppingCart, "Cart", Color(0xFF16A34A)) {
                            isSideDrawerOpen = false
                            onOpenCart()
                        }
                        QuickMiniCircleButton(Icons.Default.Favorite, "Wishlist", Color(0xFFEC4899)) {
                            isSideDrawerOpen = false
                            onOpenWishlist()
                        }
                        QuickMiniCircleButton(Icons.Default.Person, "Account", Color(0xFF7C3AED)) {
                            isSideDrawerOpen = false
                            onOpenAccount()
                        }
                        QuickMiniCircleButton(Icons.Default.DarkMode, "Theme", Color(0xFF334155)) {
                            onToggleDarkMode()
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    SidePanelItem(
                        icon = Icons.Default.Backup,
                        label = "App Backup & Restore",
                        subtitle = "सेव व रीस्टोर",
                        tint = Color(0xFF16A34A),
                        testTag = "side_panel_backup_btn"
                    ) {
                        isSideDrawerOpen = false
                        onOpenBackupManager()
                    }
                    SidePanelItem(
                        icon = Icons.Default.DesignServices,
                        label = "App Edit Dashboard",
                        subtitle = "Text, Image, लिखावट",
                        tint = accentColor,
                        testTag = "side_panel_edit_btn"
                    ) {
                        isSideDrawerOpen = false
                        onOpenAppManagement()
                    }
                    SidePanelItem(
                        icon = Icons.Default.KeyboardArrowDown,
                        label = "Scroll Down (नीचे)",
                        subtitle = "पेज नीचे स्क्रॉल करें",
                        tint = Color(0xFF2563EB),
                        testTag = "side_panel_scroll_down_btn"
                    ) {
                        onScrollDownClick()
                    }
                    SidePanelItem(
                        icon = Icons.Default.KeyboardArrowUp,
                        label = "Scroll Up (ऊपर)",
                        subtitle = "पेज के टॉप पर जाएं",
                        tint = Color(0xFF7C3AED),
                        testTag = "side_panel_scroll_up_btn"
                    ) {
                        onScrollUpClick()
                    }
                    SidePanelItem(
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        label = "Back / Side Navigation",
                        subtitle = "पिछले पेज पर लौटें",
                        tint = Color(0xFFD97706),
                        testTag = "side_panel_back_btn"
                    ) {
                        isSideDrawerOpen = false
                        onNavigateBack()
                    }
                }
            }
        }

        // 2. BOTTOM QUICK DOCK: Backup Button + Scroll Down Button + Tin Tir (◀ ▼ ▶) Button
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Dedicated App Backup Quick Button
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF059669),
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                modifier = Modifier
                    .defaultMinSize(minHeight = 44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { onBackupQuickClick() }
                    .testTag("floating_backup_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = "App Backup",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Backup",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Dedicated Scroll Down Button (↓)
            Surface(
                shape = CircleShape,
                color = if (canScrollDown) accentColor else TextSecondary,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)),
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (canScrollDown) onScrollDownClick() else onScrollUpClick()
                    }
                    .testTag("scroll_down_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (canScrollDown) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = if (canScrollDown) "Scroll Down" else "Scroll Up",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // TIN TIR BUTTON (तीन तीर कंट्रोल: ◀ Left | ▼ Down | ▶ Right/Menu)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = CardWhite.copy(alpha = 0.96f),
                shadowElevation = 8.dp,
                border = BorderStroke(1.5.dp, accentColor),
                modifier = Modifier.testTag("tin_tir_button_bar")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onLeftArrowClick,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("tin_tir_left")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Left Arrow",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(accentColor.copy(alpha = 0.25f))
                    )
                    IconButton(
                        onClick = onScrollDownClick,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("tin_tir_down")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Down Arrow",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(accentColor.copy(alpha = 0.25f))
                    )
                    IconButton(
                        onClick = onRightArrowClick,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("tin_tir_right")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Right Arrow",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SidePanelItem(
    icon: ImageVector,
    label: String,
    subtitle: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = tint.copy(alpha = 0.08f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = tint, modifier = Modifier.size(28.dp)) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.padding(5.dp))
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextPrimary)
                Text(subtitle, fontSize = 9.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun QuickMiniCircleButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = tint.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
