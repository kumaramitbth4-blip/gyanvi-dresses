package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.model.OrderItem
import com.example.model.OrderStatus
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class MapGeoPoint(
    val label: String,
    val lat: Double,
    val lng: Double
)

data class OrderRouteSnapshot(
    val origin: MapGeoPoint,
    val currentHub: MapGeoPoint,
    val destination: MapGeoPoint,
    val progressFraction: Float,
    val totalDistanceKm: Int,
    val remainingDistanceKm: Int,
    val etaLabel: String
)

object OrderMapHelper {

    private val originHub = MapGeoPoint("Gyanvi Hub, Bettiah (845438)", 26.8027, 84.5150)

    private val cityCoordinates = listOf(
        "bettiah" to MapGeoPoint("Bettiah Hub", 26.8027, 84.5150),
        "motihari" to MapGeoPoint("Motihari Hub", 26.6540, 84.9186),
        "muzaffarpur" to MapGeoPoint("Muzaffarpur Sort Hub", 26.1209, 85.3647),
        "patna" to MapGeoPoint("Patna Regional Hub", 25.5941, 85.1376),
        "darbhanga" to MapGeoPoint("Darbhanga Hub", 26.1542, 85.8918),
        "gaya" to MapGeoPoint("Gaya Hub", 24.7914, 85.0002),
        "gorakhpur" to MapGeoPoint("Gorakhpur Hub", 26.7606, 83.3732),
        "varanasi" to MapGeoPoint("Varanasi Hub", 25.3176, 82.9739),
        "lucknow" to MapGeoPoint("Lucknow Express Hub", 26.8467, 80.9462),
        "delhi" to MapGeoPoint("Delhi NCR Mega Hub", 28.6139, 77.2090),
        "noida" to MapGeoPoint("Noida Hub", 28.5355, 77.3910),
        "gurgaon" to MapGeoPoint("Gurugram Hub", 28.4595, 77.0266),
        "kolkata" to MapGeoPoint("Kolkata Sort Center", 22.5726, 88.3639),
        "mumbai" to MapGeoPoint("Mumbai Hub", 19.0760, 72.8777),
        "surat" to MapGeoPoint("Surat Textile Hub", 21.1702, 72.8311),
        "ahmedabad" to MapGeoPoint("Ahmedabad Hub", 23.0225, 72.5714),
        "jaipur" to MapGeoPoint("Jaipur Hub", 26.9124, 75.7873),
        "bengaluru" to MapGeoPoint("Bengaluru Hub", 12.9716, 77.5946),
        "hyderabad" to MapGeoPoint("Hyderabad Hub", 17.3850, 78.4867),
        "pune" to MapGeoPoint("Pune Hub", 18.5204, 73.8567),
        "chennai" to MapGeoPoint("Chennai Hub", 13.0827, 80.2707)
    )

    fun resolveGeoPoint(text: String, fallbackLabel: String): MapGeoPoint {
        val lower = text.lowercase()
        val matched = cityCoordinates.firstOrNull { (key, _) -> lower.contains(key) }?.second
        if (matched != null) {
            return matched.copy(label = fallbackLabel.ifBlank { matched.label })
        }
        // Deterministic offset from Bettiah for unknown cities/pincodes
        val hash = text.hashCode()
        val latDelta = ((hash % 40) / 100.0)
        val lngDelta = (((hash / 40) % 40) / 100.0)
        return MapGeoPoint(
            label = fallbackLabel.ifBlank { "Destination" },
            lat = 26.1 + latDelta,
            lng = 84.9 + lngDelta
        )
    }

    fun buildRouteSnapshot(order: OrderItem): OrderRouteSnapshot {
        val progress = when {
            order.status == OrderStatus.CANCELLED -> 0.08f
            order.status == OrderStatus.DELIVERED || order.status == OrderStatus.RETURN_REQUESTED || order.status == OrderStatus.RETURNED || order.status == OrderStatus.REFUNDED || order.sellerStage.equals("Delivered", true) -> 1.0f
            order.status == OrderStatus.OUT_FOR_DELIVERY || order.sellerStage.equals("Out for Delivery", true) -> 0.88f
            order.status == OrderStatus.SHIPPED || order.sellerStage.equals("Shipped", true) -> 0.64f
            order.status == OrderStatus.READY_TO_SHIP || order.sellerStage.equals("Ready to Ship", true) -> 0.36f
            order.status == OrderStatus.PROCESSING || order.isPacked || order.isQualityVerified -> 0.25f
            order.status == OrderStatus.CONFIRMED || order.isOrderConfirmed -> 0.16f
            else -> 0.10f
        }

        val destText = "${order.city} ${order.deliveryAddress} ${order.pinCode}"
        val destLabel = buildString {
            append(order.city.ifBlank { "Delivery City" })
            if (order.pinCode.isNotBlank()) append(" (${order.pinCode})")
        }
        val destination = resolveGeoPoint(destText, destLabel)

        val liveText = order.currentTrackingLocation.ifBlank { "Gyanvi Hub, Bettiah" }
        val currentHub = when {
            progress >= 0.98f -> destination.copy(label = "Delivered: $destLabel")
            progress <= 0.28f -> originHub
            else -> resolveGeoPoint(liveText, liveText.take(32))
        }

        val totalKm = haversineKm(originHub.lat, originHub.lng, destination.lat, destination.lng).coerceAtLeast(18)
        val remainingKm = ((1f - progress) * totalKm).roundToInt().coerceAtLeast(0)
        val eta = when {
            order.status == OrderStatus.CANCELLED -> "Order Cancelled"
            progress >= 1.0f -> "Delivered ✓"
            progress >= 0.85f -> "Arriving Today (${remainingKm.coerceAtLeast(2)} km away)"
            else -> "ETA: ${order.expectedDeliveryDate} (~$remainingKm km left)"
        }

        return OrderRouteSnapshot(
            origin = originHub,
            currentHub = currentHub,
            destination = destination,
            progressFraction = progress,
            totalDistanceKm = totalKm,
            remainingDistanceKm = remainingKm,
            etaLabel = eta
        )
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).roundToInt()
    }

    fun buildGoogleMapsRouteUrl(order: OrderItem): String {
        val origin = Uri.encode("Gyanvi Dresses Hub, Bettiah, Bihar 845438")
        val destRaw = listOf(order.deliveryAddress, order.city, order.state, order.pinCode)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(", ")
            .ifBlank { "Bettiah, Bihar 845438" }
        val destination = Uri.encode(destRaw)
        return "https://www.google.com/maps/dir/?api=1&origin=$origin&destination=$destination&travelmode=driving"
    }

    fun buildGoogleMapsLocationUrl(locationQuery: String): String {
        val encoded = Uri.encode(locationQuery.ifBlank { "Bettiah, Bihar 845438" })
        return "https://www.google.com/maps/search/?api=1&query=$encoded"
    }

    fun openGoogleMapsRoute(context: Context, order: OrderItem): Boolean {
        val url = buildGoogleMapsRouteUrl(order)
        return launchGoogleMapsUrl(context, url)
    }

    fun openGoogleMapsPin(context: Context, locationQuery: String): Boolean {
        val url = buildGoogleMapsLocationUrl(locationQuery)
        return launchGoogleMapsUrl(context, url)
    }

    private fun launchGoogleMapsUrl(context: Context, url: String): Boolean {
        val uri = Uri.parse(url)
        // Prefer Google Maps app if installed, then safely fall back to default browser
        val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(mapsIntent)
            true
        } catch (_: ActivityNotFoundException) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                true
            } catch (_: Exception) {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
