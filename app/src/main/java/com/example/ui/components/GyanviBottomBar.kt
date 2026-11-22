package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppManagementConfig
import com.example.model.MainTab
import com.example.ui.theme.*

@Composable
fun GyanviBottomBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    ordersCount: Int = 0,
    cartCount: Int = 0,
    onOpenSearch: () -> Unit = {},
    onOpenCart: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenTrackOrder: () -> Unit = {},
    managementConfig: AppManagementConfig = LocalAppConfig.current,
    modifier: Modifier = Modifier
) {
    val features = managementConfig.features
    val texts = managementConfig.texts
    val accent = managementConfig.themePreset.accentColor
    val indicator = managementConfig.themePreset.containerColor

    Surface(
        color = CardWhite,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("main_bottom_nav")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Unified Quick Bottom Dock: Home, Search, Cart, Track Order, Profile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(indicator.copy(alpha = 0.25f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomQuickDockChip(
                    label = "Home",
                    icon = if (currentTab == MainTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    isHighlighted = currentTab == MainTab.HOME,
                    accent = accent,
                    badgeCount = 0,
                    testTag = "bottom_dock_home",
                    onClick = { onTabSelected(MainTab.HOME) },
                    modifier = Modifier.weight(1f)
                )
                BottomQuickDockChip(
                    label = "Search",
                    icon = Icons.Outlined.Search,
                    isHighlighted = false,
                    accent = accent,
                    badgeCount = 0,
                    testTag = "bottom_dock_search",
                    onClick = onOpenSearch,
                    modifier = Modifier.weight(1f)
                )
                BottomQuickDockChip(
                    label = "Cart",
                    icon = if (cartCount > 0) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                    isHighlighted = cartCount > 0,
                    accent = accent,
                    badgeCount = cartCount,
                    testTag = "bottom_dock_cart",
                    onClick = onOpenCart,
                    modifier = Modifier.weight(1f)
                )
                BottomQuickDockChip(
                    label = "Track",
                    icon = Icons.Outlined.LocalShipping,
                    isHighlighted = false,
                    accent = accent,
                    badgeCount = 0,
                    testTag = "bottom_dock_track",
                    onClick = onOpenTrackOrder,
                    modifier = Modifier.weight(1f)
                )
                BottomQuickDockChip(
                    label = "Profile",
                    icon = Icons.Outlined.Person,
                    isHighlighted = false,
                    accent = accent,
                    badgeCount = 0,
                    testTag = "bottom_dock_profile",
                    onClick = onOpenProfile,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            NavigationBar(
                containerColor = CardWhite,
                contentColor = TextPrimary,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Home Tab (always active)
                NavigationBarItem(
                    selected = currentTab == MainTab.HOME,
                    onClick = { onTabSelected(MainTab.HOME) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = texts.tabHomeLabel
                        )
                    },
                    label = {
                        Text(
                            text = texts.tabHomeLabel,
                            fontWeight = if (currentTab == MainTab.HOME) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = accent,
                        selectedTextColor = accent,
                        indicatorColor = indicator,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_home")
                )

                // 2. Categories Tab
                if (features.showCategoriesTab) {
                    NavigationBarItem(
                        selected = currentTab == MainTab.CATEGORIES,
                        onClick = { onTabSelected(MainTab.CATEGORIES) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.CATEGORIES) Icons.Filled.Category else Icons.Outlined.Category,
                                contentDescription = texts.tabCategoriesLabel
                            )
                        },
                        label = {
                            Text(
                                text = texts.tabCategoriesLabel,
                                fontWeight = if (currentTab == MainTab.CATEGORIES) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accent,
                            selectedTextColor = accent,
                            indicatorColor = indicator,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("tab_categories")
                    )
                }

                // 3. Mall Tab
                if (features.showMallTab) {
                    NavigationBarItem(
                        selected = currentTab == MainTab.MALL,
                        onClick = { onTabSelected(MainTab.MALL) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.MALL) Icons.Filled.Verified else Icons.Outlined.Verified,
                                contentDescription = texts.tabMallLabel
                            )
                        },
                        label = {
                            Text(
                                text = texts.tabMallLabel,
                                fontWeight = if (currentTab == MainTab.MALL) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accent,
                            selectedTextColor = accent,
                            indicatorColor = indicator,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("tab_mall")
                    )
                }

                // 4. Video Finds Tab
                if (features.showVideoFindsTab) {
                    NavigationBarItem(
                        selected = currentTab == MainTab.VIDEO_FINDS,
                        onClick = { onTabSelected(MainTab.VIDEO_FINDS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.VIDEO_FINDS) Icons.Filled.SlowMotionVideo else Icons.Outlined.SlowMotionVideo,
                                contentDescription = texts.tabVideoFindsLabel
                            )
                        },
                        label = {
                            Text(
                                text = texts.tabVideoFindsLabel,
                                fontWeight = if (currentTab == MainTab.VIDEO_FINDS) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accent,
                            selectedTextColor = accent,
                            indicatorColor = indicator,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("tab_video_finds")
                    )
                }

                // 5. My Orders Tab
                if (features.showOrdersTab) {
                    NavigationBarItem(
                        selected = currentTab == MainTab.MY_ORDERS,
                        onClick = { onTabSelected(MainTab.MY_ORDERS) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (ordersCount > 0) {
                                        Badge(
                                            containerColor = accent,
                                            contentColor = Color.White
                                        ) {
                                            Text("$ordersCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == MainTab.MY_ORDERS) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                                    contentDescription = texts.tabOrdersLabel
                                )
                            }
                        },
                        label = {
                            Text(
                                text = texts.tabOrdersLabel,
                                fontWeight = if (currentTab == MainTab.MY_ORDERS) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accent,
                            selectedTextColor = accent,
                            indicatorColor = indicator,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("tab_orders")
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomQuickDockChip(
    label: String,
    icon: ImageVector,
    isHighlighted: Boolean,
    accent: Color,
    badgeCount: Int,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 42.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(
                        containerColor = accent,
                        contentColor = Color.White
                    ) {
                        Text("$badgeCount", fontSize = 8.sp)
                    }
                }
            }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isHighlighted) accent else TextSecondary,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlighted) accent else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

