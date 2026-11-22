package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppNotification
import com.example.model.NotificationPreferences
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    notifications: List<AppNotification>,
    prefs: NotificationPreferences,
    onBack: () -> Unit,
    onMarkRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onUpdatePrefs: ((NotificationPreferences) -> NotificationPreferences) -> Unit,
    onSendTestAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Inbox, 1 = Notification Settings
    var selectedFilter by remember { mutableStateOf("All") }

    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("notif_back_btn")
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column {
                                Text(
                                    text = "NOTIFICATIONS & ALERTS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$unreadCount unread • Real-time Notification System",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (selectedTab == 0 && unreadCount > 0) {
                            TextButton(onClick = {
                                onMarkAllRead()
                                Toast.makeText(context, "All notifications marked read", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Mark all read", color = MeeshoMagenta, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MeeshoMagenta
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("🔔 Inbox ($unreadCount)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("⚙️ Notification Settings", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize().testTag("notification_center_screen")
    ) { padding ->
        if (selectedTab == 0) {
            val filtered = notifications.filter {
                selectedFilter == "All" || it.category.equals(selectedFilter, ignoreCase = true)
            }

            LazyColumn(
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("All", "Orders", "Offers", "Wallet", "Security").forEach { cat ->
                            val isSel = selectedFilter == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedFilter = cat },
                                label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MeeshoMagenta,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                if (filtered.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = YellowPrimary, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No notifications in $selectedFilter", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    items(filtered, key = { it.id }) { item ->
                        NotificationCard(
                            item = item,
                            onClick = { onMarkRead(item.id) }
                        )
                    }
                }
            }
        } else {
            // Notification Settings Panel
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Delivery Channels Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Notification Channels (संदेश माध्यम)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        NotifToggleRow(
                            icon = Icons.Default.NotificationsActive,
                            title = "Push Notifications (FCM)",
                            subtitle = "Instant order, offer & delivery alerts on phone",
                            checked = prefs.pushEnabled,
                            onCheckedChange = { v -> onUpdatePrefs { it.copy(pushEnabled = v) } }
                        )
                        HorizontalDivider(color = DividerColor)
                        NotifToggleRow(
                            icon = Icons.Default.Chat,
                            title = "WhatsApp Notifications",
                            subtitle = "Order invoices, AWB & delivery alerts on WhatsApp",
                            checked = prefs.whatsappEnabled,
                            onCheckedChange = { v -> onUpdatePrefs { it.copy(whatsappEnabled = v) } }
                        )
                        HorizontalDivider(color = DividerColor)
                        NotifToggleRow(
                            icon = Icons.Default.Sms,
                            title = "SMS Alerts",
                            subtitle = "OTP verification & critical dispatch SMS updates",
                            checked = prefs.smsEnabled,
                            onCheckedChange = { v -> onUpdatePrefs { it.copy(smsEnabled = v) } }
                        )
                        HorizontalDivider(color = DividerColor)
                        NotifToggleRow(
                            icon = Icons.Default.Email,
                            title = "Email Notifications",
                            subtitle = "GST invoices, weekly offers & account statements",
                            checked = prefs.emailEnabled,
                            onCheckedChange = { v -> onUpdatePrefs { it.copy(emailEnabled = v) } }
                        )
                    }
                }

                // Alert Categories Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Alert Preferences (अलर्ट श्रेणियाँ)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        NotifToggleRow(
                            icon = Icons.Default.LocalShipping,
                            title = "Order & Tracking Updates",
                            subtitle = "Packed, Shipped, Out for Delivery & Refund updates",
                            checked = prefs.orderUpdates,
                            onCheckedChange = { v -> onUpdatePrefs { it.copy(orderUpdates = v) } }
                        )
                        HorizontalDivider(color = DividerColor)
                        NotifToggleRow(
                            icon = Icons.Default.LocalOffer,
                            title = "Festival Sales & Coupons",
                            subtitle = "Maha Dhamaka Sale, Flash Sale & Festive15 alerts",
                            checked = prefs.festiveOffers,
                            onCheckedChange = { v -> onUpdatePrefs { it.copy(festiveOffers = v) } }
                        )
                        HorizontalDivider(color = DividerColor)
                        NotifToggleRow(
                            icon = Icons.Default.ShoppingCartCheckout,
                            title = "Abandoned Cart Reminders",
                            subtitle = "Remind me when cart items go on extra discount",
                            checked = prefs.abandonedCartAlerts,
                            onCheckedChange = { v -> onUpdatePrefs { it.copy(abandonedCartAlerts = v) } }
                        )
                        HorizontalDivider(color = DividerColor)
                        NotifToggleRow(
                            icon = Icons.Default.TrendingDown,
                            title = "Wishlist Price Drop Alerts",
                            subtitle = "Notify immediately when saved dresses get cheaper",
                            checked = prefs.priceDropAlerts,
                            onCheckedChange = { v -> onUpdatePrefs { it.copy(priceDropAlerts = v) } }
                        )
                        HorizontalDivider(color = DividerColor)
                        NotifToggleRow(
                            icon = Icons.Default.Security,
                            title = "Login & Security Alerts (2FA)",
                            subtitle = "New device logins & password/OTP security warnings",
                            checked = prefs.securityAlerts,
                            onCheckedChange = { v -> onUpdatePrefs { it.copy(securityAlerts = v) } }
                        )
                        HorizontalDivider(color = DividerColor)
                        NotifToggleRow(
                            icon = Icons.Default.VolumeUp,
                            title = "Notification Sound & Vibration",
                            subtitle = "Play alert chime for new orders and flash deals",
                            checked = prefs.soundAndVibration,
                            onCheckedChange = { v -> onUpdatePrefs { it.copy(soundAndVibration = v) } }
                        )
                    }
                }

                // Trigger Test Notification Button
                Button(
                    onClick = {
                        onSendTestAlert()
                        selectedTab = 0
                        Toast.makeText(context, "Test notification triggered!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeeshoMagenta),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("test_notification_btn")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Test Notification Alert", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: AppNotification,
    onClick: () -> Unit
) {
    val (bgTint, iconTint, icon) = when (item.category) {
        "Orders" -> Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), Icons.Default.LocalShipping)
        "Wallet" -> Triple(Color(0xFFE8F5E9), GreenRating, Icons.Default.AccountBalanceWallet)
        "Security" -> Triple(Color(0xFFF3E5F5), Color(0xFF7B1FA2), Icons.Default.Security)
        else -> Triple(Color(0xFFFCE4EC), MeeshoMagenta, Icons.Default.LocalOffer)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (item.isRead) 1.dp else 1.5.dp,
                color = if (item.isRead) Color(0xFFEAEAEA) else MeeshoMagenta.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MeeshoMagenta)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.message,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = bgTint,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.category.uppercase(),
                            color = iconTint,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = item.timeAgo,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun NotifToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MeeshoMagenta
            )
        )
    }
}
