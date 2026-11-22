package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppManagementConfig
import com.example.ui.theme.*

@Composable
fun GyanviTopBar(
    userName: String = "Amit Kumar Patwa",
    deliveryLocation: String = "Bettiah - 845438",
    wishlistCount: Int = 0,
    cartCount: Int = 0,
    notificationCount: Int = 0,
    appLanguage: String = "English",
    managementConfig: AppManagementConfig = LocalAppConfig.current,
    onSearchClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onCameraClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onCartClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    onLocationClick: () -> Unit,
    onAccountClick: () -> Unit = {},
    onQuickEditClick: () -> Unit = {},
    onOpenOrders: () -> Unit = {},
    onOpenSellerPanel: () -> Unit = {},
    onOpenStaffWorkspace: () -> Unit = {},
    onOpenAdminPanel: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenControlPanel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    val texts = managementConfig.texts
    val features = managementConfig.features
    val images = managementConfig.images
    val topBarBg = managementConfig.themePreset.primaryColor
    val accent = managementConfig.themePreset.accentColor

    Surface(
        color = topBarBg,
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Profile + Brand + Icons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // User Avatar and Name (Clickable to open Account screen)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onAccountClick() }
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                        .testTag("profile_account_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        AppManagedImage(
                            drawableRes = images.brandAvatarDrawable ?: com.example.R.drawable.gyanvi_brand_logo,
                            customUri = images.brandAvatarCustomUri,
                            contentDescription = "Gyanvi Dresses Logo",
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (appLanguage == "हिंदी") {
                                texts.brandHindiTitle.ifBlank { texts.brandTitle }
                            } else {
                                texts.brandTitle
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Quick Edit App Management, Control Panel, Notifications, Wishlist & Cart
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenControlPanel,
                        modifier = Modifier.testTag("top_bar_control_panel_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Master Control Panel",
                            tint = accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    if (features.showQuickEditShortcut) {
                        IconButton(
                            onClick = onQuickEditClick,
                            modifier = Modifier.testTag("top_bar_quick_edit_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DesignServices,
                                contentDescription = "Edit App Dashboard",
                                tint = accent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Notifications icon
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier.testTag("notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (notificationCount > 0) {
                                    Badge(
                                        containerColor = SaleRed,
                                        contentColor = Color.White
                                    ) {
                                        Text("$notificationCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = TextPrimary,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }

                    // Wishlist icon
                    IconButton(
                        onClick = onWishlistClick,
                        modifier = Modifier.testTag("wishlist_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (wishlistCount > 0) {
                                    Badge(
                                        containerColor = accent,
                                        contentColor = Color.White
                                    ) {
                                        Text("$wishlistCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = TextPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Cart icon
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier.testTag("cart_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(
                                        containerColor = accent,
                                        contentColor = Color.White
                                    ) {
                                        Text("$cartCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = "Cart",
                                tint = TextPrimary,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    }

                    // More Options Menu (⋮)
                    Box {
                        IconButton(
                            onClick = { showMoreMenu = true },
                            modifier = Modifier.testTag("topbar_more_options_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Filled.Tune, contentDescription = null, tint = accent) },
                                text = { Text("Master Control Panel", fontWeight = FontWeight.Bold) },
                                trailingIcon = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    onOpenControlPanel()
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = accent) },
                                text = { Text("Buyer Hub & Profile") },
                                trailingIcon = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    onAccountClick()
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = Color(0xFF2563EB)) },
                                text = { Text("My Orders & Tracking") },
                                trailingIcon = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    onOpenOrders()
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Outlined.Storefront, contentDescription = null, tint = Color(0xFF7C3AED)) },
                                text = { Text("Seller Panel") },
                                trailingIcon = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    onOpenSellerPanel()
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null, tint = Color(0xFF0369A1)) },
                                text = { Text("Staff Panel (कर्मचारी)") },
                                trailingIcon = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    onOpenStaffWorkspace()
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null, tint = Color(0xFFB45309)) },
                                text = { Text("Admin Panel") },
                                trailingIcon = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    onOpenAdminPanel()
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null, tint = TextSecondary) },
                                text = { Text("Settings") },
                                trailingIcon = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                                onClick = {
                                    showMoreMenu = false
                                    onOpenSettings()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar (Meesho style with configurable Mic & Camera)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardWhite,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSearchClick() }
                    .testTag("search_bar_input")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = texts.searchPlaceholder.ifBlank { "Search..." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (features.showVoiceSearch) {
                        IconButton(
                            onClick = onVoiceClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("voice_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Search",
                                tint = accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (features.showCameraSearch) {
                        IconButton(
                            onClick = onCameraClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("camera_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Camera Search",
                                tint = accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onLocationClick() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${texts.deliveryPrefix} $deliveryLocation".trim(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Change Location",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
