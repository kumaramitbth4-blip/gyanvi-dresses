package com.example.data.remote

import android.content.Context
import androidx.annotation.Keep
import com.example.R
import com.example.model.OrderItem
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.TrackingStage
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

@Keep
@IgnoreExtraProperties
data class FirestoreTrackingStage(
    val title: String = "",
    val subtitle: String = "",
    val completed: Boolean = false,
    val dateStr: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "title" to title,
        "subtitle" to subtitle,
        "completed" to completed,
        "dateStr" to dateStr
    )

    fun toDomain(): TrackingStage = TrackingStage(
        title = title,
        subtitle = subtitle,
        isCompleted = completed,
        dateStr = dateStr
    )

    companion object {
        fun fromDomain(stage: TrackingStage): FirestoreTrackingStage = FirestoreTrackingStage(
            title = stage.title,
            subtitle = stage.subtitle,
            completed = stage.isCompleted,
            dateStr = stage.dateStr
        )

        fun fromRawMap(raw: Map<*, *>): FirestoreTrackingStage = FirestoreTrackingStage(
            title = raw["title"] as? String ?: "",
            subtitle = raw["subtitle"] as? String ?: "",
            completed = raw["completed"] as? Boolean ?: false,
            dateStr = raw["dateStr"] as? String ?: ""
        )
    }
}

@Keep
@IgnoreExtraProperties
data class FirestoreOrderDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val orderId: String = "",
    val productId: String = "",
    val productName: String = "",
    val productCategory: String = "",
    val productPrice: Long = 0L,
    val productImageRes: Long = 0L,
    val orderDate: String = "",
    val status: String = OrderStatus.ORDERED.name,
    val statusText: String = "",
    val deliveryDateRange: String = "",
    val selectedSize: String = "M",
    val quantity: Long = 1L,
    val totalAmount: Long = 0L,
    val trackingStages: List<FirestoreTrackingStage> = emptyList(),
    val userRating: Long = 0L,
    val userReviewText: String = "",
    val feedbackCompleted: Boolean = false,
    val paymentMethod: String = "UPI (PhonePe)",
    val awbNumber: String = "",
    val customerName: String = "",
    val deliveryAddress: String = "",
    val labelDownloaded: Boolean = false,
    val sellerStage: String = "",
    val customerPhone: String = "",
    val customerAgeYears: String = "26 Years",
    val selectedColor: String = "Festive Maroon",
    val paymentStatus: String = "Paid",
    val orderConfirmed: Boolean = false,
    val qualityVerified: Boolean = false,
    val packed: Boolean = false,
    val invoiceGenerated: Boolean = false,
    val barcodeScanned: Boolean = false,
    val courierPartner: String = "Delhivery Express",
    val pickupStatus: String = "Not Requested",
    val shippingCharges: Long = 49L,
    val expectedDeliveryDate: String = "",
    val currentTrackingLocation: String = "",
    val deliveryDelayed: Boolean = false,
    val rtoFlagged: Boolean = false,
    val rtoReason: String = "",
    val cancelReason: String = "",
    val cancelledBy: String = "",
    val cancellationDate: String = "",
    val refundStatus: String = "Not Applicable",
    val refundAmount: Long = 0L,
    val stockRestored: Boolean = false,
    val holdReason: String = "",
    val holdDate: String = "",
    val holdNotes: String = "",
    val holdExpiryAlert: String = "48 hrs Hold Limit",
    val adminApprovedHold: Boolean = true,
    val sellerHoldPermission: Boolean = true,
    val orderHistoryLog: List<String> = emptyList(),
    val city: String = "Bettiah",
    val state: String = "Bihar",
    val pinCode: String = "845438",
    val customProductImageUri: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(ownerUid: String): Map<String, Any> {
        val resolvedOrderId = orderId.ifBlank { id }
        return buildMutableFieldsMap() + mapOf(
            "userId" to ownerUid,
            "orderId" to resolvedOrderId,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    fun toUpdateMap(): Map<String, Any> {
        return buildMutableFieldsMap() + mapOf(
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    private fun buildMutableFieldsMap(): Map<String, Any> {
        val raw = mutableMapOf<String, Any?>(
            "productId" to productId.take(128),
            "productName" to productName.ifBlank { "Gyanvi Dress" }.take(250),
            "productCategory" to productCategory.take(120),
            "productPrice" to productPrice.coerceIn(0L, 10_000_000L),
            "productImageRes" to productImageRes.coerceAtLeast(0L),
            "orderDate" to orderDate.take(80),
            "status" to (enumValues<OrderStatus>().firstOrNull { it.name == status }?.name ?: OrderStatus.ORDERED.name),
            "statusText" to statusText.take(300),
            "deliveryDateRange" to deliveryDateRange.take(160),
            "selectedSize" to selectedSize.take(80),
            "quantity" to quantity.coerceIn(1L, 10_000L),
            "totalAmount" to totalAmount.coerceIn(0L, 100_000_000L),
            "trackingStages" to trackingStages.take(20).map { it.toMap() },
            "userRating" to userRating.coerceIn(0L, 5L),
            "userReviewText" to userReviewText.take(1000),
            "feedbackCompleted" to feedbackCompleted,
            "paymentMethod" to paymentMethod.take(120),
            "awbNumber" to awbNumber.take(100),
            "customerName" to customerName.ifBlank { "Gyanvi Customer" }.take(160),
            "deliveryAddress" to deliveryAddress.take(500),
            "labelDownloaded" to labelDownloaded,
            "sellerStage" to sellerStage.take(60),
            "customerPhone" to customerPhone.take(40),
            "customerAgeYears" to customerAgeYears.take(40),
            "selectedColor" to selectedColor.take(80),
            "paymentStatus" to paymentStatus.take(80),
            "orderConfirmed" to orderConfirmed,
            "qualityVerified" to qualityVerified,
            "packed" to packed,
            "invoiceGenerated" to invoiceGenerated,
            "barcodeScanned" to barcodeScanned,
            "courierPartner" to courierPartner.take(120),
            "pickupStatus" to pickupStatus.take(80),
            "shippingCharges" to shippingCharges.coerceIn(0L, 100_000L),
            "expectedDeliveryDate" to expectedDeliveryDate.take(80),
            "currentTrackingLocation" to currentTrackingLocation.take(250),
            "deliveryDelayed" to deliveryDelayed,
            "rtoFlagged" to rtoFlagged,
            "rtoReason" to rtoReason.take(400),
            "cancelReason" to cancelReason.take(500),
            "cancelledBy" to cancelledBy.take(60),
            "cancellationDate" to cancellationDate.take(80),
            "refundStatus" to refundStatus.take(80),
            "refundAmount" to refundAmount.coerceIn(0L, 100_000_000L),
            "stockRestored" to stockRestored,
            "holdReason" to holdReason.take(400),
            "holdDate" to holdDate.take(80),
            "holdNotes" to holdNotes.take(500),
            "holdExpiryAlert" to holdExpiryAlert.take(120),
            "adminApprovedHold" to adminApprovedHold,
            "sellerHoldPermission" to sellerHoldPermission,
            "orderHistoryLog" to orderHistoryLog.takeLast(100),
            "city" to city.take(120),
            "state" to state.take(120),
            "pinCode" to pinCode.take(20),
            "customProductImageUri" to customProductImageUri.take(1000)
        )
        @Suppress("UNCHECKED_CAST")
        return raw.filterValues { it != null } as Map<String, Any>
    }

    fun toOrderItem(catalog: List<Product>): OrderItem {
        val fallbackProduct = catalog.firstOrNull { it.id == productId }
            ?: catalog.firstOrNull()
            ?: Product(
                id = productId.ifBlank { "gd_custom" },
                name = productName.ifBlank { "Gyanvi Dress" },
                category = productCategory.ifBlank { "Ethnic Wear" },
                subcategory = productCategory.ifBlank { "Ethnic Wear" },
                price = productPrice.toInt(),
                originalPrice = (productPrice + 200L).toInt(),
                rating = 4.6f,
                ratingCount = 120,
                imageRes = R.drawable.dress_anarkali_1793417013639,
                fabric = "Premium Cotton Blend"
            )
        val resolvedProduct = if (fallbackProduct.id == productId && fallbackProduct.name == productName) {
            fallbackProduct
        } else {
            fallbackProduct.copy(
                id = productId.ifBlank { fallbackProduct.id },
                name = productName.ifBlank { fallbackProduct.name },
                category = productCategory.ifBlank { fallbackProduct.category },
                price = if (productPrice > 0L) productPrice.toInt() else fallbackProduct.price,
                imageRes = if (productImageRes > 0L) productImageRes.toInt() else fallbackProduct.imageRes
            )
        }
        val resolvedOrderId = orderId.ifBlank { id }
        val safeStatus = enumValues<OrderStatus>().firstOrNull { it.name == status } ?: OrderStatus.ORDERED

        return OrderItem(
            orderId = resolvedOrderId,
            product = resolvedProduct,
            orderDate = orderDate,
            status = safeStatus,
            statusText = statusText,
            deliveryDateRange = deliveryDateRange,
            selectedSize = selectedSize,
            quantity = quantity.toInt().coerceAtLeast(1),
            totalAmount = totalAmount.toInt().coerceAtLeast(0),
            trackingStages = trackingStages.map { it.toDomain() },
            userRating = userRating.toInt().coerceIn(0, 5),
            userReviewText = userReviewText,
            isFeedbackCompleted = feedbackCompleted,
            paymentMethod = paymentMethod,
            awbNumber = awbNumber,
            customerName = customerName,
            deliveryAddress = deliveryAddress,
            isLabelDownloaded = labelDownloaded,
            sellerStage = sellerStage,
            customerPhone = customerPhone,
            customerAgeYears = customerAgeYears.ifBlank { "26 Years" },
            selectedColor = selectedColor,
            paymentStatus = paymentStatus,
            isOrderConfirmed = orderConfirmed,
            isQualityVerified = qualityVerified,
            isPacked = packed,
            isInvoiceGenerated = invoiceGenerated,
            isBarcodeScanned = barcodeScanned,
            courierPartner = courierPartner,
            pickupStatus = pickupStatus,
            shippingCharges = shippingCharges.toInt(),
            expectedDeliveryDate = expectedDeliveryDate,
            currentTrackingLocation = currentTrackingLocation,
            isDeliveryDelayed = deliveryDelayed,
            isRtoFlagged = rtoFlagged,
            rtoReason = rtoReason,
            cancelReason = cancelReason,
            cancelledBy = cancelledBy,
            cancellationDate = cancellationDate,
            refundStatus = refundStatus,
            refundAmount = refundAmount.toInt(),
            isStockRestored = stockRestored,
            holdReason = holdReason,
            holdDate = holdDate,
            holdNotes = holdNotes,
            holdExpiryAlert = holdExpiryAlert,
            isAdminApprovedHold = adminApprovedHold,
            sellerHoldPermission = sellerHoldPermission,
            orderHistoryLog = orderHistoryLog,
            city = city,
            state = state,
            pinCode = pinCode,
            customProductImageUri = customProductImageUri.takeIf { it.isNotBlank() }
        )
    }

    companion object {
        fun fromOrderItem(order: OrderItem, ownerUid: String = ""): FirestoreOrderDoc = FirestoreOrderDoc(
            id = "",
            userId = ownerUid,
            orderId = sanitizeOrderId(order.orderId),
            productId = order.product.id.ifBlank { "gd_custom" },
            productName = order.product.name.ifBlank { "Gyanvi Dress" },
            productCategory = order.product.category,
            productPrice = order.product.price.toLong().coerceAtLeast(0L),
            productImageRes = order.product.imageRes.toLong().coerceAtLeast(0L),
            orderDate = order.orderDate,
            status = order.status.name,
            statusText = order.statusText,
            deliveryDateRange = order.deliveryDateRange,
            selectedSize = order.selectedSize,
            quantity = order.quantity.toLong().coerceAtLeast(1L),
            totalAmount = order.totalAmount.toLong().coerceAtLeast(0L),
            trackingStages = order.trackingStages.map { FirestoreTrackingStage.fromDomain(it) },
            userRating = order.userRating.toLong().coerceIn(0L, 5L),
            userReviewText = order.userReviewText,
            feedbackCompleted = order.isFeedbackCompleted,
            paymentMethod = order.paymentMethod,
            awbNumber = order.awbNumber,
            customerName = order.customerName.ifBlank { "Gyanvi Customer" },
            deliveryAddress = order.deliveryAddress,
            labelDownloaded = order.isLabelDownloaded,
            sellerStage = order.sellerStage,
            customerPhone = order.customerPhone,
            customerAgeYears = order.customerAgeYears.ifBlank { "26 Years" },
            selectedColor = order.selectedColor,
            paymentStatus = order.paymentStatus,
            orderConfirmed = order.isOrderConfirmed,
            qualityVerified = order.isQualityVerified,
            packed = order.isPacked,
            invoiceGenerated = order.isInvoiceGenerated,
            barcodeScanned = order.isBarcodeScanned,
            courierPartner = order.courierPartner,
            pickupStatus = order.pickupStatus,
            shippingCharges = order.shippingCharges.toLong().coerceAtLeast(0L),
            expectedDeliveryDate = order.expectedDeliveryDate,
            currentTrackingLocation = order.currentTrackingLocation,
            deliveryDelayed = order.isDeliveryDelayed,
            rtoFlagged = order.isRtoFlagged,
            rtoReason = order.rtoReason,
            cancelReason = order.cancelReason,
            cancelledBy = order.cancelledBy,
            cancellationDate = order.cancellationDate,
            refundStatus = order.refundStatus,
            refundAmount = order.refundAmount.toLong().coerceAtLeast(0L),
            stockRestored = order.isStockRestored,
            holdReason = order.holdReason,
            holdDate = order.holdDate,
            holdNotes = order.holdNotes,
            holdExpiryAlert = order.holdExpiryAlert,
            adminApprovedHold = order.isAdminApprovedHold,
            sellerHoldPermission = order.sellerHoldPermission,
            orderHistoryLog = order.orderHistoryLog,
            city = order.city,
            state = order.state,
            pinCode = order.pinCode,
            customProductImageUri = order.customProductImageUri ?: ""
        )

        fun sanitizeOrderId(rawId: String): String {
            val cleaned = rawId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            return cleaned.ifBlank { "OD_${System.currentTimeMillis()}" }.take(128)
        }
    }
}

class FirestoreOrderRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun userOrdersCollection(userId: String) =
        db.collection("users").document(userId).collection("orders")

    fun observeOrders(userId: String, catalog: List<Product>): Flow<List<OrderItem>> {
        require(userId.isNotBlank()) { "userId must not be blank" }
        return userOrdersCollection(userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        doc.toObject(
                            FirestoreOrderDoc::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )?.toOrderItem(catalog)
                    }.getOrNull()
                }
            }
    }

    suspend fun upsertOrder(userId: String, order: OrderItem): Result<Unit> {
        val docId = FirestoreOrderDoc.sanitizeOrderId(order.orderId)
        val path = "users/$userId/orders/$docId"
        return try {
            val uid = auth.currentUser?.uid
                ?: error("User must be signed in with Google before writing orders.")
            check(uid == userId) { "Signed-in user changed; refusing cross-user write." }
            val docRef = userOrdersCollection(userId).document(docId)
            val docModel = FirestoreOrderDoc.fromOrderItem(order.copy(orderId = docId), userId)
            val exists = docRef.get().await().exists()
            if (exists) {
                docRef.update(docModel.toUpdateMap()).await()
            } else {
                docRef.set(docModel.toCreateMap(userId)).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun seedInitialOrdersIfEmpty(userId: String, orders: List<OrderItem>): Result<Unit> {
        val colPath = "users/$userId/orders"
        return try {
            val uid = auth.currentUser?.uid
                ?: error("User must be signed in with Google before seeding orders.")
            check(uid == userId) { "Signed-in user changed; refusing cross-user seed." }
            val existingSnapshot = userOrdersCollection(userId).limit(1).get().await()
            if (!existingSnapshot.isEmpty || orders.isEmpty()) {
                return Result.success(Unit)
            }
            orders.chunked(200).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { order ->
                    val docId = FirestoreOrderDoc.sanitizeOrderId(order.orderId)
                    val docRef = userOrdersCollection(userId).document(docId)
                    val model = FirestoreOrderDoc.fromOrderItem(order.copy(orderId = docId), userId)
                    batch.set(docRef, model.toCreateMap(userId))
                }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colPath)
            Result.failure(e)
        }
    }

    suspend fun syncOrdersBatch(userId: String, orders: List<OrderItem>): Result<Unit> {
        val colPath = "users/$userId/orders"
        return try {
            val uid = auth.currentUser?.uid
                ?: error("User must be signed in with Google before syncing orders.")
            check(uid == userId) { "Signed-in user changed; refusing cross-user sync." }
            val existingIds = userOrdersCollection(userId).get().await().documents.map { it.id }.toSet()
            orders.chunked(200).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { order ->
                    val docId = FirestoreOrderDoc.sanitizeOrderId(order.orderId)
                    val docRef = userOrdersCollection(userId).document(docId)
                    val model = FirestoreOrderDoc.fromOrderItem(order.copy(orderId = docId), userId)
                    if (docId in existingIds) {
                        batch.update(docRef, model.toUpdateMap())
                    } else {
                        batch.set(docRef, model.toCreateMap(userId))
                    }
                }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, colPath)
            Result.failure(e)
        }
    }

    suspend fun deleteOrder(userId: String, orderId: String): Result<Unit> {
        val docId = FirestoreOrderDoc.sanitizeOrderId(orderId)
        val path = "users/$userId/orders/$docId"
        return try {
            val uid = auth.currentUser?.uid
                ?: error("User must be signed in with Google before deleting.")
            check(uid == userId) { "Signed-in user changed; refusing cross-user delete." }
            userOrdersCollection(userId).document(docId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    companion object {
        fun unwrapFirestoreError(error: Throwable): Throwable {
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            return firestoreError ?: error
        }
    }
}
