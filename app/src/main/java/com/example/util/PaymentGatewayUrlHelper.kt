package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URI
import java.net.URL

data class GatewayUrlValidationResult(
    val isValid: Boolean,
    val normalizedUrl: String = "",
    val host: String = "",
    val errorMessage: String? = null
)

data class GatewayConnectionTestResult(
    val isSuccess: Boolean,
    val statusCode: Int? = null,
    val statusLabel: String,
    val detail: String
)

object PaymentGatewayUrlHelper {

    fun validateHttpsGatewayUrl(rawInput: String): GatewayUrlValidationResult {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) {
            return GatewayUrlValidationResult(
                isValid = false,
                errorMessage = "Please enter a Payment Gateway HTTPS URL."
            )
        }

        val lower = trimmed.lowercase()
        if (lower.startsWith("javascript:") ||
            lower.startsWith("data:") ||
            lower.startsWith("file:") ||
            lower.startsWith("intent:") ||
            lower.startsWith("content:")
        ) {
            return GatewayUrlValidationResult(
                isValid = false,
                errorMessage = "Unsupported URL scheme. Only https:// links are permitted."
            )
        }

        if (lower.startsWith("http://")) {
            return GatewayUrlValidationResult(
                isValid = false,
                errorMessage = "Insecure http:// is not allowed. Use https:// payment URL."
            )
        }

        val candidate = if (!lower.startsWith("https://")) {
            "https://$trimmed"
        } else {
            trimmed
        }

        val parsed = runCatching { URI(candidate) }.getOrNull()
            ?: return GatewayUrlValidationResult(
                isValid = false,
                errorMessage = "Invalid URL format. Example: https://rzp.io/l/your-link"
            )

        if (!parsed.scheme.equals("https", ignoreCase = true)) {
            return GatewayUrlValidationResult(
                isValid = false,
                errorMessage = "Only HTTPS payment gateway URLs are supported."
            )
        }

        val host = parsed.host?.trim()?.lowercase().orEmpty()
        if (host.isBlank() || !host.contains(".") || host.endsWith(".")) {
            return GatewayUrlValidationResult(
                isValid = false,
                errorMessage = "Enter a valid domain host (e.g. rzp.io or pay.yourdomain.com)."
            )
        }

        if (isBlockedHost(host)) {
            return GatewayUrlValidationResult(
                isValid = false,
                errorMessage = "Local or private network hosts are not allowed for payment links."
            )
        }

        val androidUri = runCatching { Uri.parse(candidate) }.getOrNull()
        if (androidUri == null || androidUri.scheme?.lowercase() != "https" || androidUri.host.isNullOrBlank()) {
            return GatewayUrlValidationResult(
                isValid = false,
                errorMessage = "Could not parse payment gateway URL."
            )
        }

        return GatewayUrlValidationResult(
            isValid = true,
            normalizedUrl = candidate,
            host = host
        )
    }

    private fun isBlockedHost(host: String): Boolean {
        if (host == "localhost" ||
            host.endsWith(".localhost") ||
            host.endsWith(".local") ||
            host == "::1" ||
            host.startsWith("127.") ||
            host.startsWith("10.") ||
            host.startsWith("192.168.") ||
            host.startsWith("169.254.") ||
            host == "0.0.0.0"
        ) {
            return true
        }
        val ipv4Match = Regex("""^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$""").matchEntire(host)
        if (ipv4Match != null) {
            val octets = ipv4Match.groupValues.drop(1).mapNotNull { it.toIntOrNull() }
            if (octets.size == 4) {
                val (a, b, _, _) = octets
                if (a == 172 && b in 16..31) return true
                // Block raw IPv4 addresses for security hygiene on payment gateways
                return true
            }
        }
        if (host.startsWith("[") && host.endsWith("]")) return true
        return false
    }

    fun buildCheckoutGatewayUrl(
        baseUrl: String,
        amountInr: Int,
        orderRef: String,
        appendParams: Boolean
    ): String? {
        val validation = validateHttpsGatewayUrl(baseUrl)
        if (!validation.isValid) return null
        if (!appendParams) return validation.normalizedUrl

        return runCatching {
            Uri.parse(validation.normalizedUrl)
                .buildUpon()
                .appendQueryParameter("amount", amountInr.toString())
                .appendQueryParameter("currency", "INR")
                .appendQueryParameter("order_ref", orderRef)
                .build()
                .toString()
        }.getOrNull() ?: validation.normalizedUrl
    }

    suspend fun testPaymentGatewayConnection(rawUrl: String): GatewayConnectionTestResult {
        val validation = validateHttpsGatewayUrl(rawUrl)
        if (!validation.isValid) {
            return GatewayConnectionTestResult(
                isSuccess = false,
                statusLabel = "Invalid URL",
                detail = validation.errorMessage ?: "Invalid HTTPS URL"
            )
        }

        return withContext(Dispatchers.IO) {
            try {
                val addr = InetAddress.getByName(validation.host)
                if (addr.isLoopbackAddress || addr.isSiteLocalAddress || addr.isLinkLocalAddress || addr.isAnyLocalAddress) {
                    return@withContext GatewayConnectionTestResult(
                        isSuccess = false,
                        statusLabel = "Blocked Host",
                        detail = "Private or loopback host cannot be used as a payment gateway."
                    )
                }

                val urlObj = URL(validation.normalizedUrl)
                val conn = (urlObj.openConnection() as HttpURLConnection).apply {
                    requestMethod = "HEAD"
                    instanceFollowRedirects = false
                    connectTimeout = 5000
                    readTimeout = 5000
                    setRequestProperty("User-Agent", "GyanviDresses-Android/1.0")
                }

                var code = conn.responseCode
                conn.disconnect()

                if (code == 405 || code == 501) {
                    val getConn = (urlObj.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        instanceFollowRedirects = false
                        connectTimeout = 5000
                        readTimeout = 5000
                        setRequestProperty("User-Agent", "GyanviDresses-Android/1.0")
                    }
                    code = getConn.responseCode
                    getConn.disconnect()
                }

                if (code in 200..399) {
                    GatewayConnectionTestResult(
                        isSuccess = true,
                        statusCode = code,
                        statusLabel = "Connected (HTTP $code)",
                        detail = "Verified HTTPS reachability for ${validation.host}."
                    )
                } else {
                    GatewayConnectionTestResult(
                        isSuccess = false,
                        statusCode = code,
                        statusLabel = "HTTP $code",
                        detail = "Gateway responded with status $code. Please verify link availability."
                    )
                }
            } catch (e: Exception) {
                GatewayConnectionTestResult(
                    isSuccess = false,
                    statusLabel = "Reachability Check Failed",
                    detail = e.localizedMessage ?: "Unable to reach ${validation.host} right now. You can still save if valid HTTPS link."
                )
            }
        }
    }

    fun openPaymentGatewayUrl(context: Context, rawUrl: String): GatewayUrlValidationResult {
        val validation = validateHttpsGatewayUrl(rawUrl)
        if (!validation.isValid) return validation

        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validation.normalizedUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            validation
        } catch (_: ActivityNotFoundException) {
            GatewayUrlValidationResult(
                isValid = false,
                errorMessage = "No browser app found to open ${validation.host}"
            )
        } catch (e: Exception) {
            GatewayUrlValidationResult(
                isValid = false,
                errorMessage = e.localizedMessage ?: "Unable to launch payment link."
            )
        }
    }
}
