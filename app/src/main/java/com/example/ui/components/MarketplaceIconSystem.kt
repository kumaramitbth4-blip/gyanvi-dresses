package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Uniform icon action descriptor used across Buyer, Seller, and Admin panels
 * in Gyanvi Dresses so all three panels share one consistent Material 3
 * iconography, squircle container geometry, badge treatment, and bilingual copy.
 */
data class PanelIconAction(
    val id: String,
    val label: String,
    val hindiSubtitle: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val tint: Color,
    val badgeText: String? = null,
    val isSelected: Boolean = false,
    val testTag: String = "",
    val onClick: () -> Unit
)

/**
 * Uniform 3-column grid card rendering a panel's 15 quick-access icons.
 * Uses a non-lazy Column of chunked Rows so it embeds cleanly inside any
 * vertically scrolling screen without nested-scroll conflicts.
 */
@Composable
fun PanelIconActionGrid(
    sectionTitle: String,
    sectionSubtitle: String,
    actions: List<PanelIconAction>,
    accentColor: Color,
    containerBorderColor: Color,
    modifier: Modifier = Modifier,
    columns: Int = 3
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, containerBorderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sectionTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = sectionSubtitle,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${actions.size} Icons",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            val rows = actions.chunked(columns.coerceAtLeast(1))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { action ->
                            PanelIconTile(
                                action = action,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(columns - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelIconTile(
    action: PanelIconAction,
    modifier: Modifier = Modifier
) {
    val tileShape = RoundedCornerShape(12.dp)
    val bgColor = if (action.isSelected) action.tint.copy(alpha = 0.10f) else Color(0xFFFAFAFC)
    val borderColor = if (action.isSelected) action.tint else Color(0xFFECEFF4)
    val tileModifier = modifier
        .defaultMinSize(minHeight = 88.dp)
        .clip(tileShape)
        .background(bgColor)
        .border(width = if (action.isSelected) 1.5.dp else 1.dp, color = borderColor, shape = tileShape)
        .clickable(onClick = action.onClick)
        .padding(horizontal = 6.dp, vertical = 10.dp)
        .let { base ->
            if (action.testTag.isNotBlank()) base.testTag(action.testTag) else base
        }

    Column(
        modifier = tileModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        if (action.isSelected) action.tint else action.tint.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (action.isSelected) action.filledIcon else action.outlinedIcon,
                    contentDescription = "${action.label} - ${action.hindiSubtitle}",
                    tint = if (action.isSelected) Color.White else action.tint,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (!action.badgeText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = action.tint,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = (-4).dp)
                ) {
                    Text(
                        text = action.badgeText,
                        color = Color.White,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = action.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (action.isSelected) action.tint else TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = action.hindiSubtitle,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
