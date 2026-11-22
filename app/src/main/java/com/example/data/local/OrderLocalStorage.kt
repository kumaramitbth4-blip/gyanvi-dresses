package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.R
import com.example.model.OrderItem
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.TrackingStage
import org.json.JSONArray
import org.json.JSONObject

class OrderLocalStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("gyanvi_orders_prefs", Context.MODE_PRIVATE)

    fun loadOrders(defaults: List<OrderItem>, catalogProducts: List<Product>): List<OrderItem> {
        val raw = prefs.getString(KEY_ORDERS_JSON, null) ?: return defaults
        return runCatching {
            val arr = JSONArray(raw)
            if (arr.length() == 0) return defaults
            val list = ArrayList<OrderItem>(arr.length())
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                decodeOrder(obj, catalogProducts)?.let { list.add(it) }
            }
            if (list.isEmpty()) defaults else list
        }.getOrDefault(defaults)
    }

    fun saveOrders(orders: List<OrderItem>) {
        runCatching {
            val arr = JSONArray()
            orders.forEach { arr.put(encodeOrder(it)) }
            prefs.edit().putString(KEY_ORDERS_JSON, arr.toString()).apply()
        }
    }

    private fun encodeOrder(order: OrderItem): JSONObject {
        val obj = JSONObject()
        obj.put("orderId", order.orderId)
        obj.put("productId", order.product.id)
        obj.put("productName", order.product.name)
        obj.put("productCategory", order.product.category)
        obj.put("productSubcategory", order.product.subcategory)
        obj.put("productPrice", order.product.price)
        obj.put("productOriginalPrice", order.product.originalPrice)
        obj.put("productImageRes", order.product.imageRes)
        obj.put("productFabric", order.product.fabric)
        obj.put("productImageUrl", order.product.imageUrl)
        obj.put("productCustomImageUri", order.product.customImageUri ?: "")

        obj.put("orderDate", order.orderDate)
        obj.put("status", order.status.name)
        obj.put("statusText", order.statusText)
        obj.put("deliveryDateRange", order.deliveryDateRange)
        obj.put("selectedSize", order.selectedSize)
        obj.put("quantity", order.quantity)
        obj.put("totalAmount", order.totalAmount)
        obj.put("userRating", order.userRating)
        obj.put("userReviewText", order.userReviewText)
        obj.put("isFeedbackCompleted", order.isFeedbackCompleted)
        obj.put("paymentMethod", order.paymentMethod)
        obj.put("awbNumber", order.awbNumber)
        obj.put("customerName", order.customerName)
        obj.put("deliveryAddress", order.deliveryAddress)
        obj.put("isLabelDownloaded", order.isLabelDownloaded)
        obj.put("sellerStage", order.sellerStage)
        obj.put("customerPhone", order.customerPhone)
        obj.put("selectedColor", order.selectedColor)
        obj.put("paymentStatus", order.paymentStatus)
        obj.put("isOrderConfirmed", order.isOrderConfirmed)
        obj.put("isQualityVerified", order.isQualityVerified)
        obj.put("isPacked", order.isPacked)
        obj.put("isInvoiceGenerated", order.isInvoiceGenerated)
        obj.put("isBarcodeScanned", order.isBarcodeScanned)
        obj.put("courierPartner", order.courierPartner)
        obj.put("pickupStatus", order.pickupStatus)
        obj.put("shippingCharges", order.shippingCharges)
        obj.put("expectedDeliveryDate", order.expectedDeliveryDate)
        obj.put("currentTrackingLocation", order.currentTrackingLocation)
        obj.put("isDeliveryDelayed", order.isDeliveryDelayed)
        obj.put("isRtoFlagged", order.isRtoFlagged)
        obj.put("rtoReason", order.rtoReason)
        obj.put("cancelReason", order.cancelReason)
        obj.put("cancelledBy", order.cancelledBy)
        obj.put("cancellationDate", order.cancellationDate)
        obj.put("refundStatus", order.refundStatus)
        obj.put("refundAmount", order.refundAmount)
        obj.put("isStockRestored", order.isStockRestored)
        obj.put("holdReason", order.holdReason)
        obj.put("holdDate", order.holdDate)
        obj.put("holdNotes", order.holdNotes)
        obj.put("holdExpiryAlert", order.holdExpiryAlert)
        obj.put("isAdminApprovedHold", order.isAdminApprovedHold)
        obj.put("sellerHoldPermission", order.sellerHoldPermission)
        obj.put("city", order.city)
        obj.put("state", order.state)
        obj.put("pinCode", order.pinCode)
        obj.put("customProductImageUri", order.customProductImageUri ?: "")
        obj.put("customerAgeYears", order.customerAgeYears)

        val stagesArr = JSONArray()
        order.trackingStages.forEach { st ->
            stagesArr.put(JSONObject().apply {
                put("title", st.title)
                put("subtitle", st.subtitle)
                put("isCompleted", st.isCompleted)
                put("dateStr", st.dateStr)
            })
        }
        obj.put("trackingStages", stagesArr)

        val logArr = JSONArray()
        order.orderHistoryLog.forEach { logArr.put(it) }
        obj.put("orderHistoryLog", logArr)
        return obj
    }

    private fun decodeOrder(obj: JSONObject, catalogProducts: List<Product>): OrderItem? {
        val orderId = obj.optString("orderId", "")
        if (orderId.isBlank()) return null

        val productId = obj.optString("productId", "")
        val matchedProduct = catalogProducts.find { it.id == productId }
        val fallbackImage = obj.optInt("productImageRes", R.drawable.dress_anarkali_1793417013639)
        val productPrice = obj.optInt("productPrice", 599)
        val product = matchedProduct ?: Product(
            id = productId.ifBlank { "custom_${orderId.takeLast(6)}" },
            name = obj.optString("productName", "Gyanvi Ethnic Dress"),
            category = obj.optString("productCategory", "Ethnic Wear"),
            subcategory = obj.optString("productSubcategory", "Kurtis"),
            price = productPrice,
            originalPrice = obj.optInt("productOriginalPrice", productPrice * 2),
            rating = 4.6f,
            ratingCount = 120,
            imageRes = fallbackImage,
            fabric = obj.optString("productFabric", "Premium Silk"),
            imageUrl = obj.optString("productImageUrl", ""),
            customImageUri = obj.optString("productCustomImageUri", "").ifBlank { null }
        )

        val status = runCatching {
            OrderStatus.valueOf(obj.optString("status", OrderStatus.ORDERED.name))
        }.getOrDefault(OrderStatus.ORDERED)

        val stages = mutableListOf<TrackingStage>()
        val stagesArr = obj.optJSONArray("trackingStages")
        if (stagesArr != null) {
            for (i in 0 until stagesArr.length()) {
                val st = stagesArr.getJSONObject(i)
                stages.add(
                    TrackingStage(
                        title = st.optString("title", ""),
                        subtitle = st.optString("subtitle", ""),
                        isCompleted = st.optBoolean("isCompleted", false),
                        dateStr = st.optString("dateStr", "")
                    )
                )
            }
        }

        val logs = mutableListOf<String>()
        val logArr = obj.optJSONArray("orderHistoryLog")
        if (logArr != null) {
            for (i in 0 until logArr.length()) {
                val entry = logArr.optString(i, "")
                if (entry.isNotBlank()) logs.add(entry)
            }
        }

        return OrderItem(
            orderId = orderId,
            product = product,
            orderDate = obj.optString("orderDate", ""),
            status = status,
            statusText = obj.optString("statusText", ""),
            deliveryDateRange = obj.optString("deliveryDateRange", ""),
            selectedSize = obj.optString("selectedSize", "Free Size"),
            quantity = obj.optInt("quantity", 1),
            totalAmount = obj.optInt("totalAmount", product.price),
            trackingStages = stages,
            userRating = obj.optInt("userRating", 0),
            userReviewText = obj.optString("userReviewText", ""),
            isFeedbackCompleted = obj.optBoolean("isFeedbackCompleted", false),
            paymentMethod = obj.optString("paymentMethod", "UPI (PhonePe)"),
            awbNumber = obj.optString("awbNumber", "DLV-845438912"),
            customerName = obj.optString("customerName", "Gyanvi Customer"),
            deliveryAddress = obj.optString("deliveryAddress", "Bettiah, Bihar - 845438"),
            isLabelDownloaded = obj.optBoolean("isLabelDownloaded", false),
            sellerStage = obj.optString("sellerStage", "Pending"),
            customerPhone = obj.optString("customerPhone", "+91 9876543210"),
            selectedColor = obj.optString("selectedColor", "Festive Collection"),
            paymentStatus = obj.optString("paymentStatus", "Paid"),
            isOrderConfirmed = obj.optBoolean("isOrderConfirmed", false),
            isQualityVerified = obj.optBoolean("isQualityVerified", false),
            isPacked = obj.optBoolean("isPacked", false),
            isInvoiceGenerated = obj.optBoolean("isInvoiceGenerated", false),
            isBarcodeScanned = obj.optBoolean("isBarcodeScanned", false),
            courierPartner = obj.optString("courierPartner", "Delhivery Express"),
            pickupStatus = obj.optString("pickupStatus", "Not Requested"),
            shippingCharges = obj.optInt("shippingCharges", 49),
            expectedDeliveryDate = obj.optString("expectedDeliveryDate", "20 Nov 2026"),
            currentTrackingLocation = obj.optString("currentTrackingLocation", "Gyanvi Boutique Hub, Bettiah"),
            isDeliveryDelayed = obj.optBoolean("isDeliveryDelayed", false),
            isRtoFlagged = obj.optBoolean("isRtoFlagged", false),
            rtoReason = obj.optString("rtoReason", ""),
            cancelReason = obj.optString("cancelReason", ""),
            cancelledBy = obj.optString("cancelledBy", ""),
            cancellationDate = obj.optString("cancellationDate", ""),
            refundStatus = obj.optString("refundStatus", "Not Applicable"),
            refundAmount = obj.optInt("refundAmount", 0),
            isStockRestored = obj.optBoolean("isStockRestored", false),
            holdReason = obj.optString("holdReason", ""),
            holdDate = obj.optString("holdDate", ""),
            holdNotes = obj.optString("holdNotes", ""),
            holdExpiryAlert = obj.optString("holdExpiryAlert", "48 hrs Hold Limit"),
            isAdminApprovedHold = obj.optBoolean("isAdminApprovedHold", true),
            sellerHoldPermission = obj.optBoolean("sellerHoldPermission", true),
            orderHistoryLog = logs,
            city = obj.optString("city", "Bettiah"),
            state = obj.optString("state", "Bihar"),
            pinCode = obj.optString("pinCode", "845438"),
            customProductImageUri = obj.optString("customProductImageUri", "").ifBlank { null },
            customerAgeYears = obj.optString("customerAgeYears", "26 Years")
        )
    }

    companion object {
        private const val KEY_ORDERS_JSON = "persisted_orders_json"
    }
}
