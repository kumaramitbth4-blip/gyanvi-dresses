package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.SocialAdPlatform
import com.example.model.SocialMediaAdCampaign
import com.example.model.SocialMediaAdsConfig
import com.example.ui.theme.CardWhite
import com.example.ui.theme.MeeshoMagenta
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PaymentGatewayUrlHelper

private fun platformIcon(platform: SocialAdPlatform): ImageVector = when (platform) {
    SocialAdPlatform.INSTAGRAM -> Icons.Filled.CameraAlt
    SocialAdPlatform.FACEBOOK -> Icons.Filled.ThumbUp
    SocialAdPlatform.YOUTUBE -> Icons.Filled.PlayCircle
    SocialAdPlatform.WHATSAPP -> Icons.Filled.Chat
    SocialAdPlatform.GOOGLE_ADS -> Icons.Filled.AdsClick
    SocialAdPlatform.SNAPCHAT -> Icons.Filled.AutoAwesome
}

private fun formatCompactCount(count: Int): String = when {
    count >= 100_000 -> String.format("%.1fL", count / 100_000f)
    count >= 1_000 -> String.format("%.1fK", count / 1_000f)
    else -> count.toString()
}

private fun shareAdCampaign(context: android.content.Context, ad: SocialMediaAdCampaign) {
    val text = buildString {
        append("✨ Gyanvi Dresses Sponsored Offer (${ad.platform.badgeLabel})\n")
        append("${ad.headline}\n")
        if (ad.caption.isNotBlank()) append("${ad.caption}\n")
        append("Shop / Watch Ad: ${ad.targetUrl}")
    }
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, ad.headline)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    runCatching {
        val chooser = Intent.createChooser(shareIntent, "Share ${ad.platform.badgeLabel} Ad").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }.onFailure {
        Toast.makeText(context, "Unable to open share menu", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun HomeSocialMediaAdsCarousel(
    config: SocialMediaAdsConfig,
    accentColor: Color = MeeshoMagenta,
    onAdClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeHomeAds = remember(config) {
        if (!config.adsEnabled || !config.showOnHome) emptyList()
        else config.campaigns.filter { it.isActive && it.showOnHomeBanner }
    }
    if (activeHomeAds.isEmpty()) return

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_social_media_ads_carousel"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.14f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Campaign,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.padding(5.dp)
                    )
                }
                Column {
                    Text(
                        text = "SOCIAL MEDIA ADS & REEL DEALS",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Instagram • Facebook • YouTube • WhatsApp",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFDCFCE7)
            ) {
                Text(
                    text = "${activeHomeAds.size} LIVE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF166534),
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(activeHomeAds, key = { it.id }) { ad ->
                val platformColor = Color(ad.platform.accentHex)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .width(278.dp)
                        .clickable {
                            onAdClick(ad.id)
                            val launch = PaymentGatewayUrlHelper.openPaymentGatewayUrl(context, ad.targetUrl)
                            if (!launch.isValid) {
                                Toast.makeText(
                                    context,
                                    launch.errorMessage ?: "Invalid ad URL",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .testTag("home_social_ad_card_${ad.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        platformColor,
                                        platformColor.copy(alpha = 0.85f),
                                        Color(0xFF1E1B4B)
                                    )
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = platformIcon(ad.platform),
                                            contentDescription = ad.platform.badgeLabel,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = ad.platform.badgeLabel,
                                            color = Color.White,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEF08A)
                                ) {
                                    Text(
                                        text = "Sponsored",
                                        color = Color(0xFF713F12),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = ad.headline,
                                color = Color.White,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = ad.caption,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 15.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${formatCompactCount(ad.impressions)} Views • ${formatCompactCount(ad.clicks)} Clicks",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { shareAdCampaign(context, ad) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = "Share ${ad.title}",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        modifier = Modifier
                                            .clickable {
                                                onAdClick(ad.id)
                                                val launch = PaymentGatewayUrlHelper.openPaymentGatewayUrl(context, ad.targetUrl)
                                                if (!launch.isValid) {
                                                    Toast.makeText(
                                                        context,
                                                        launch.errorMessage ?: "Invalid ad URL",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = ad.ctaLabel.ifBlank { "Open Ad" },
                                                color = platformColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                contentDescription = null,
                                                tint = platformColor,
                                                modifier = Modifier.size(12.dp)
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
    }
}

@Composable
fun SocialMediaAdsManagerSection(
    config: SocialMediaAdsConfig,
    accentColor: Color = MeeshoMagenta,
    onToggleAdsEnabled: (Boolean) -> Unit,
    onToggleShowOnHome: (Boolean) -> Unit,
    onSaveCampaign: (
        id: String?,
        title: String,
        platform: SocialAdPlatform,
        headline: String,
        caption: String,
        targetUrl: String,
        dailyBudgetInr: Int,
        ctaLabel: String,
        showOnHomeBanner: Boolean,
        isActive: Boolean
    ) -> Result<SocialMediaAdCampaign>,
    onToggleCampaignActive: (String) -> Unit,
    onDeleteCampaign: (String) -> Unit,
    onRecordAdClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPlatformFilter by remember { mutableStateOf<SocialAdPlatform?>(null) }
    var editingCampaign by remember { mutableStateOf<SocialMediaAdCampaign?>(null) }
    var showCampaignDialog by remember { mutableStateOf(false) }

    val totalImpressions = remember(config.campaigns) { config.campaigns.sumOf { it.impressions } }
    val totalClicks = remember(config.campaigns) { config.campaigns.sumOf { it.clicks } }
    val totalOrders = remember(config.campaigns) { config.campaigns.sumOf { it.ordersGenerated } }
    val activeCount = remember(config.campaigns) { config.campaigns.count { it.isActive } }

    val filteredCampaigns = remember(config.campaigns, selectedPlatformFilter) {
        val f = selectedPlatformFilter
        if (f == null) config.campaigns else config.campaigns.filter { it.platform == f }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("social_media_ads_manager_section")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFE1306C), Color(0xFF1877F2))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Campaign,
                            contentDescription = "Social Media Ads",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Social Media Ads Studio (सोशल मीडिया एड्स)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "$activeCount/${config.campaigns.size} Active • Instagram, FB, YouTube & WhatsApp",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                Button(
                    onClick = {
                        editingCampaign = null
                        showCampaignDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .testTag("create_social_ad_btn")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New Ad", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Metrics strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdSummaryTile(
                    label = "Total Reach",
                    value = formatCompactCount(totalImpressions),
                    tint = Color(0xFFE1306C),
                    modifier = Modifier.weight(1f)
                )
                AdSummaryTile(
                    label = "Ad Clicks",
                    value = formatCompactCount(totalClicks),
                    tint = Color(0xFF1877F2),
                    modifier = Modifier.weight(1f)
                )
                AdSummaryTile(
                    label = "Orders",
                    value = totalOrders.toString(),
                    tint = Color(0xFF15803D),
                    modifier = Modifier.weight(1f)
                )
            }

            // Switches
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Social Media Ads Engine", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = TextPrimary)
                            Text("Run & manage multi-platform campaigns", fontSize = 10.5.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = config.adsEnabled,
                            onCheckedChange = onToggleAdsEnabled,
                            colors = SwitchDefaults.colors(checkedTrackColor = accentColor),
                            modifier = Modifier.testTag("toggle_social_ads_enabled")
                        )
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Show Ads Carousel on Buyer Home", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = TextPrimary)
                            Text("Display sponsored Reels & offer cards on Home", fontSize = 10.5.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = config.showOnHome,
                            onCheckedChange = onToggleShowOnHome,
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF15803D)),
                            modifier = Modifier.testTag("toggle_social_ads_home")
                        )
                    }
                }
            }

            // Platform Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedPlatformFilter == null,
                    onClick = { selectedPlatformFilter = null },
                    label = { Text("All (${config.campaigns.size})", fontSize = 11.sp) }
                )
                SocialAdPlatform.entries.forEach { platform ->
                    val count = config.campaigns.count { it.platform == platform }
                    FilterChip(
                        selected = selectedPlatformFilter == platform,
                        onClick = {
                            selectedPlatformFilter = if (selectedPlatformFilter == platform) null else platform
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = platformIcon(platform),
                                contentDescription = null,
                                tint = Color(platform.accentHex),
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        label = { Text("${platform.badgeLabel} ($count)", fontSize = 11.sp) }
                    )
                }
            }

            // Campaign Cards
            if (filteredCampaigns.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.Campaign, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(28.dp))
                        Text("No Social Ad Campaigns yet", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Text("Tap 'New Ad' above to launch Instagram, Facebook, YouTube, or WhatsApp ads.", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            } else {
                filteredCampaigns.forEach { ad ->
                    val pColor = Color(ad.platform.accentHex)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (ad.isActive) Color(0xFFFFFFFF) else Color(0xFFF8FAFC),
                        border = BorderStroke(
                            1.dp,
                            if (ad.isActive) pColor.copy(alpha = 0.4f) else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("social_ad_item_${ad.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = pColor.copy(alpha = 0.14f),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = platformIcon(ad.platform),
                                            contentDescription = ad.platform.badgeLabel,
                                            tint = pColor,
                                            modifier = Modifier.padding(7.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = ad.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = pColor.copy(alpha = 0.14f)
                                            ) {
                                                Text(
                                                    text = ad.platform.badgeLabel,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = pColor,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = ad.headline,
                                            fontSize = 11.5.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Switch(
                                    checked = ad.isActive,
                                    onCheckedChange = { onToggleCampaignActive(ad.id) },
                                    colors = SwitchDefaults.colors(checkedTrackColor = pColor)
                                )
                            }

                            if (ad.caption.isNotBlank()) {
                                Text(
                                    text = ad.caption,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Budget: ₹${ad.dailyBudgetInr}/day",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Reach: ${formatCompactCount(ad.impressions)} • Clicks: ${ad.clicks} • Orders: ${ad.ordersGenerated}",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "Link: ${ad.targetUrl}",
                                fontSize = 10.sp,
                                color = Color(0xFF2563EB),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        onRecordAdClick(ad.id)
                                        val res = PaymentGatewayUrlHelper.openPaymentGatewayUrl(context, ad.targetUrl)
                                        if (!res.isValid) {
                                            Toast.makeText(context, res.errorMessage ?: "Invalid URL", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = pColor),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Open Ad Link", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { shareAdCampaign(context, ad) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Share", fontSize = 10.5.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        editingCampaign = ad
                                        showCampaignDialog = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Edit", fontSize = 10.5.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        onDeleteCampaign(ad.id)
                                        Toast.makeText(context, "Deleted '${ad.title}'", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Delete", fontSize = 10.5.sp, color = Color(0xFFDC2626))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCampaignDialog) {
        SocialAdCampaignEditorDialog(
            initialCampaign = editingCampaign,
            accentColor = accentColor,
            onDismiss = { showCampaignDialog = false },
            onSave = { title, platform, headline, caption, url, budget, cta, showHome ->
                val result = onSaveCampaign(
                    editingCampaign?.id,
                    title,
                    platform,
                    headline,
                    caption,
                    url,
                    budget,
                    cta,
                    showHome,
                    editingCampaign?.isActive ?: true
                )
                result.onSuccess {
                    showCampaignDialog = false
                    Toast.makeText(context, "Saved '${it.title}' (${it.platform.badgeLabel})", Toast.LENGTH_SHORT).show()
                }
                result.exceptionOrNull()?.localizedMessage
            }
        )
    }
}

@Composable
private fun AdSummaryTile(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = tint.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = tint)
            Text(label, fontSize = 10.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SocialAdCampaignEditorDialog(
    initialCampaign: SocialMediaAdCampaign?,
    accentColor: Color,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        platform: SocialAdPlatform,
        headline: String,
        caption: String,
        url: String,
        budget: Int,
        cta: String,
        showHome: Boolean
    ) -> String?
) {
    var selectedPlatform by remember { mutableStateOf(initialCampaign?.platform ?: SocialAdPlatform.INSTAGRAM) }
    var title by remember { mutableStateOf(initialCampaign?.title ?: "") }
    var headline by remember { mutableStateOf(initialCampaign?.headline ?: "") }
    var caption by remember { mutableStateOf(initialCampaign?.caption ?: "") }
    var targetUrl by remember { mutableStateOf(initialCampaign?.targetUrl ?: selectedPlatform.defaultUrl) }
    var dailyBudgetText by remember { mutableStateOf((initialCampaign?.dailyBudgetInr ?: 500).toString()) }
    var ctaLabel by remember { mutableStateOf(initialCampaign?.ctaLabel ?: "Shop Deal") }
    var showOnHomeBanner by remember { mutableStateOf(initialCampaign?.showOnHomeBanner ?: true) }
    var urlError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialCampaign == null) "Create Social Media Ad" else "Edit Social Media Ad",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = accentColor
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text("Select Social Media Platform:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SocialAdPlatform.entries.forEach { plat ->
                        val isSel = selectedPlatform == plat
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                val prevDefault = selectedPlatform.defaultUrl
                                selectedPlatform = plat
                                if (targetUrl.isBlank() || targetUrl == prevDefault) {
                                    targetUrl = plat.defaultUrl
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = platformIcon(plat),
                                    contentDescription = null,
                                    tint = if (isSel) Color.White else Color(plat.accentHex),
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            label = { Text(plat.badgeLabel, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(plat.accentHex),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Campaign Title *") },
                    placeholder = { Text("e.g. Festive Lehenga Reel Promo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = headline,
                    onValueChange = { headline = it },
                    label = { Text("Ad Headline *") },
                    placeholder = { Text("e.g. Bridal Lehengas Starting ₹999 ✨") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Ad Caption / Offer Text") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetUrl,
                    onValueChange = {
                        targetUrl = it
                        urlError = null
                    },
                    label = { Text("Social Ad / Landing HTTPS URL *") },
                    placeholder = { Text(selectedPlatform.defaultUrl) },
                    isError = urlError != null,
                    supportingText = urlError?.let { err -> { Text(err, color = Color(0xFFDC2626)) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dailyBudgetText,
                        onValueChange = { dailyBudgetText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Daily Budget (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ctaLabel,
                        onValueChange = { ctaLabel = it },
                        label = { Text("Button Label") },
                        placeholder = { Text("Shop Deal") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show on Buyer Home Carousel", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Feature this ad on Storefront Home", fontSize = 10.5.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = showOnHomeBanner,
                        onCheckedChange = { showOnHomeBanner = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = accentColor)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val budget = dailyBudgetText.toIntOrNull() ?: 500
                            val err = onSave(
                                title,
                                selectedPlatform,
                                headline,
                                caption,
                                targetUrl,
                                budget,
                                ctaLabel,
                                showOnHomeBanner
                            )
                            if (err != null) {
                                urlError = err
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Save & Launch Ad", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
