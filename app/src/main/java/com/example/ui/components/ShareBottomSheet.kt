package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Product
import com.example.ui.theme.*
import com.example.util.ImageSaver

data class QuickFriend(
    val id: String,
    val name: String,
    val phone: String,
    val avatarBg: Color
)

val sampleQuickFriends = listOf(
    QuickFriend("f1", "Alok Vak", "+917488774049", Color(0xFFF3E5F5)),
    QuickFriend("f2", "Amit Sha", "+919128754982", Color(0xFFE8EAF6)),
    QuickFriend("f3", "Chandan", "+919262565577", Color(0xFFEDE7F6)),
    QuickFriend("f4", "Created", "+919973781545", Color(0xFFFCE4EC))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    product: Product,
    onDismiss: () -> Unit,
    onOpenContacts: () -> Unit,
    onOpenDirectMessageOtp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isReselling by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = null,
        modifier = modifier.testTag("share_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Header matching Meesho screenshot 2
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SHARE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_share_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextPrimary
                        )
                    }
                }
            }

            HorizontalDivider(color = DividerColor)

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // "Are you reselling this product?" Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Are you reselling this product?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // NO option
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (!isReselling) MeeshoPinkLight else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (!isReselling) MeeshoMagenta else Color(0xFFE0E0E0)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { isReselling = false }
                        ) {
                            Text(
                                text = "NO",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (!isReselling) MeeshoMagenta else TextSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        // YES option
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isReselling) MeeshoPinkLight else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isReselling) MeeshoMagenta else Color(0xFFE0E0E0)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { isReselling = true }
                        ) {
                            Text(
                                text = "YES",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isReselling) MeeshoMagenta else TextSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // "Share & Earn Upto ₹125" Banner (screenshot 2)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF7FB),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF8BBD0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MeeshoMagenta
                            ) {
                                Text(
                                    text = "New",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Share & Earn",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Upto ₹125",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MeeshoMagenta
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3 Steps
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Step 1
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEDE7F6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = MeeshoPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Share a\nProduct Link",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 13.sp,
                                    color = TextSecondary
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFFBDBDBD),
                                modifier = Modifier.size(16.dp)
                            )

                            // Step 2
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = GreenDiscount,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Friend's 1st\nOrder Delivered",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 13.sp,
                                    color = TextSecondary
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFFBDBDBD),
                                modifier = Modifier.size(16.dp)
                            )

                            // Step 3
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFCE4EC)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = MeeshoMagenta,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Earn Cash in\nGyanvi Balance",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // "Friends not on Gyanvi Dresses" / Quick Contacts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Friends not on Gyanvi Dresses",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MeeshoMagenta,
                        modifier = Modifier
                            .clickable { onOpenContacts() }
                            .padding(4.dp)
                            .testTag("view_all_share_friends")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Horizontal Avatars with WhatsApp Icon Badge
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sampleQuickFriends) { friend ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    ImageSaver.shareToWhatsApp(
                                        context,
                                        product.name,
                                        product.price,
                                        friend.phone
                                    )
                                }
                                .padding(4.dp)
                        ) {
                            Box(modifier = Modifier.size(52.dp)) {
                                // Initials circle
                                Surface(
                                    shape = CircleShape,
                                    color = friend.avatarBg,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = friend.name.firstOrNull()?.toString() ?: "U",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MeeshoMagenta
                                        )
                                    }
                                }

                                // Green WhatsApp icon badge at bottom end
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF25D366),
                                    modifier = Modifier
                                        .size(18.dp)
                                        .align(Alignment.BottomEnd)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Chat,
                                            contentDescription = "WhatsApp",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = friend.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // "Choose an option to share" Grid
                Text(
                    text = "Choose an option to share",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Options Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // WhatsApp
                    ShareOptionItem(
                        icon = Icons.Default.Chat,
                        label = "WhatsApp",
                        bgColor = Color(0xFF25D366),
                        iconTint = Color.White,
                        onClick = {
                            ImageSaver.shareToWhatsApp(context, product.name, product.price)
                        }
                    )

                    // Instagram
                    ShareOptionItem(
                        icon = Icons.Default.PhotoCamera,
                        label = "Instagram",
                        bgColor = Color(0xFFE1306C),
                        iconTint = Color.White,
                        onClick = {
                            ImageSaver.shareProductText(context, product.name, product.price)
                        }
                    )

                    // Facebook Wall
                    ShareOptionItem(
                        icon = Icons.Default.Public,
                        label = "Facebook Wall",
                        bgColor = Color(0xFF1877F2),
                        iconTint = Color.White,
                        onClick = {
                            ImageSaver.shareProductText(context, product.name, product.price)
                        }
                    )

                    // Facebook Page
                    ShareOptionItem(
                        icon = Icons.Default.Flag,
                        label = "Facebook Page",
                        bgColor = Color(0xFF1877F2),
                        iconTint = Color.White,
                        onClick = {
                            ImageSaver.shareProductText(context, product.name, product.price)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Options Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ShareOptionItem(
                            icon = Icons.Default.Groups,
                            label = "Facebook Group",
                            bgColor = Color(0xFF1877F2),
                            iconTint = Color.White,
                            onClick = {
                                ImageSaver.shareProductText(context, product.name, product.price)
                            }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        ShareOptionItem(
                            icon = Icons.Default.ContentCopy,
                            label = "Copy to clipboard",
                            bgColor = Color(0xFFF5F5F5),
                            iconTint = TextPrimary,
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Gyanvi Product", "👗 ${product.name}\n💰 Price: ₹${product.price}\n👉 Shop now: https://gyanvidresses.app/p/${product.id}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        ShareOptionItem(
                            icon = Icons.Default.MoreHoriz,
                            label = "Others",
                            bgColor = Color(0xFFF5F5F5),
                            iconTint = TextPrimary,
                            onClick = {
                                ImageSaver.shareProductText(context, product.name, product.price)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // "Direct Message & OTP Dispatcher" Action Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF7FB),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MeeshoMagenta.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onDismiss()
                            onOpenDirectMessageOtp()
                        }
                        .testTag("direct_msg_otp_share_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MeeshoMagenta,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SendToMobile,
                                    contentDescription = "Send Message & OTP",
                                    tint = CardWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Send Message & OTP to Mobile/Email",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MeeshoMagenta
                                ) {
                                    Text(
                                        text = "NEW",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "फोन नंबर या ईमेल पर टेक्स्ट मैसेज व OTP भेजें एवं गैलरी शेयर",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MeeshoMagenta
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                HorizontalDivider(color = DividerColor)

                Spacer(modifier = Modifier.height(12.dp))

                // "Download Photos" / Gallery Save Action (screenshot 2)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF9F9F9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            ImageSaver.saveToGallery(
                                context = context,
                                drawableResId = product.imageRes,
                                productName = product.name
                            )
                        }
                        .testTag("download_photos_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = YellowPrimary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Download Photos",
                                    tint = CardWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Download Photos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Save product images to your device Gallery",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ShareOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    bgColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = bgColor,
            modifier = Modifier.size(46.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp
        )
    }
}
