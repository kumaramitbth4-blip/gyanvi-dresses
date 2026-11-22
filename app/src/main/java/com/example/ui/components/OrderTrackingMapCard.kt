package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderItem
import com.example.model.OrderStatus
import com.example.ui.theme.MeeshoMagenta
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.OrderMapHelper

@Composable
fun OrderTrackingGoogleMapCard(
    order: OrderItem,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val route = remember(
        order.orderId,
        order.status,
        order.sellerStage,
        order.currentTrackingLocation,
        order.city,
        order.pinCode,
        order.deliveryAddress
    ) {
        OrderMapHelper.buildRouteSnapshot(order)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "courierPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("order_tracking_google_map_card")
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Map,
                            contentDescription = "Google Map Route",
                            tint = Color(0xFF1D4ED8),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Google Maps Live Tracking 🗺️",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = route.etaLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (order.status == OrderStatus.CANCELLED) Color(0xFFDC2626) else Color(0xFF15803D)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE0F2FE)
                ) {
                    Text(
                        text = "${route.totalDistanceKm} km route",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0369A1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Google Maps Styled Interactive Route Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFF5EC))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Map Parks & Water Bands (Google Maps light palette)
                    drawRect(
                        color = Color(0xFFD8EAD2),
                        topLeft = Offset(w * 0.06f, h * 0.12f),
                        size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.30f)
                    )
                    drawRect(
                        color = Color(0xFFCDE7F8),
                        topLeft = Offset(w * 0.62f, h * 0.54f),
                        size = androidx.compose.ui.geometry.Size(w * 0.30f, h * 0.32f)
                    )

                    // Map Road Grid Lines
                    val roadColor = Color.White
                    val minorRoadColor = Color(0xFFF6F8F5)
                    for (i in 1..4) {
                        val yPos = h * (i / 5f)
                        drawLine(
                            color = minorRoadColor,
                            start = Offset(0f, yPos),
                            end = Offset(w, yPos + 8f),
                            strokeWidth = 6f
                        )
                    }
                    for (i in 1..5) {
                        val xPos = w * (i / 6f)
                        drawLine(
                            color = minorRoadColor,
                            start = Offset(xPos, 0f),
                            end = Offset(xPos - 14f, h),
                            strokeWidth = 6f
                        )
                    }
                    // Highway Arterials
                    drawLine(
                        color = Color(0xFFFDE68A),
                        start = Offset(0f, h * 0.56f),
                        end = Offset(w, h * 0.34f),
                        strokeWidth = 10f
                    )
                    drawLine(
                        color = roadColor,
                        start = Offset(w * 0.16f, 0f),
                        end = Offset(w * 0.52f, h),
                        strokeWidth = 9f
                    )

                    val startPt = Offset(w * 0.12f, h * 0.74f)
                    val midControl = Offset(w * 0.46f, h * 0.24f)
                    val endPt = Offset(w * 0.88f, h * 0.32f)

                    // Full Planned Route (Dashed Gray-Blue)
                    val plannedPath = Path().apply {
                        moveTo(startPt.x, startPt.y)
                        quadraticTo(midControl.x, midControl.y, endPt.x, endPt.y)
                    }
                    drawPath(
                        path = plannedPath,
                        color = Color(0xFF94A3B8),
                        style = Stroke(
                            width = 7f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                        )
                    )

                    // Completed Route Approximation using sampled quadratic Bezier points
                    val tMax = route.progressFraction.coerceIn(0.05f, 1.0f)
                    fun bezierAt(t: Float): Offset {
                        val inv = 1f - t
                        val x = inv * inv * startPt.x + 2 * inv * t * midControl.x + t * t * endPt.x
                        val y = inv * inv * startPt.y + 2 * inv * t * midControl.y + t * t * endPt.y
                        return Offset(x, y)
                    }

                    val completedPath = Path().apply {
                        moveTo(startPt.x, startPt.y)
                        val steps = 24
                        for (s in 1..steps) {
                            val pt = bezierAt(tMax * (s / steps.toFloat()))
                            lineTo(pt.x, pt.y)
                        }
                    }
                    drawPath(
                        path = completedPath,
                        color = Color(0xFF1A73E8),
                        style = Stroke(width = 9f, cap = StrokeCap.Round)
                    )

                    // Origin Pin (Seller Hub)
                    drawCircle(color = Color(0xFF1E293B), radius = 10f, center = startPt)
                    drawCircle(color = Color.White, radius = 4.5f, center = startPt)

                    // Destination Pin (Google Maps Red Pin)
                    drawCircle(color = Color(0xFFEA4335), radius = 12f, center = endPt)
                    drawCircle(color = Color.White, radius = 4.5f, center = endPt)

                    // Animated Current Courier Marker
                    val courierPt = bezierAt(tMax)
                    drawCircle(
                        color = Color(0xFF1A73E8).copy(alpha = pulseAlpha),
                        radius = pulseRadius,
                        center = courierPt
                    )
                    drawCircle(color = Color.White, radius = 13f, center = courierPt)
                    drawCircle(color = Color(0xFF1A73E8), radius = 9.5f, center = courierPt)
                }

                // Origin Chip (Bottom Left)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.94f),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Origin: Bettiah Hub",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Destination Chip (Top Right)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.94f),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Drop: ${route.destination.label}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB91C1C),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Live Courier Hub Floating Badge (Top Left)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1A73E8),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4ADE80))
                        )
                        Text(
                            text = "${(route.progressFraction * 100).toInt()}% • ${route.currentHub.label}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Real Google Maps Launch Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val launched = OrderMapHelper.openGoogleMapsRoute(context, order)
                        if (!launched) {
                            Toast.makeText(context, "Unable to open Google Maps on this device.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .testTag("open_google_maps_route_btn")
                ) {
                    Icon(Icons.Filled.Directions, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Google Maps Route", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = {
                        val query = order.currentTrackingLocation.ifBlank { "${order.city} ${order.pinCode}" }
                        val launched = OrderMapHelper.openGoogleMapsPin(context, query)
                        if (!launched) {
                            Toast.makeText(context, "Unable to open Google Maps Pin.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MeeshoMagenta),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .testTag("open_google_maps_hub_pin_btn")
                ) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MeeshoMagenta, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Live Hub Pin 📍", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MeeshoMagenta)
                }
            }
        }
    }
}
