package com.example.model

enum class MainTab {
    HOME,
    CATEGORIES,
    MALL,
    VIDEO_FINDS,
    MY_ORDERS
}

enum class SortOption(val label: String) {
    RELEVANCE("Relevance"),
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    PRICE_HIGH_TO_LOW("Price: High to Low"),
    RATING_HIGH("Customer Rating"),
    DISCOUNT("Discount %")
}

enum class OrderStatus(val label: String, val hindiLabel: String = label) {
    ALL("All", "सभी"),
    ORDERED("Pending", "नया ऑर्डर"),
    CONFIRMED("Confirmed", "ऑर्डर स्वीकार"),
    PROCESSING("Processing", "सामान तैयार करना"),
    READY_TO_SHIP("Ready to Ship", "भेजने के लिए तैयार"),
    SHIPPED("Shipped", "सामान भेज दिया"),
    OUT_FOR_DELIVERY("Out for Delivery", "डिलीवरी के लिए निकला"),
    DELIVERED("Delivered", "ग्राहक को मिल गया"),
    CANCELLED("Cancelled", "ऑर्डर रद्द"),
    RETURN_REQUESTED("Return Requested", "वापसी का अनुरोध"),
    RETURNED("Returned", "सामान वापस"),
    REFUNDED("Refunded", "पैसा वापस"),
    EXCHANGE("Exchange", "बदली"),
    RETURN("Return", "रिटर्न");

    companion object {
        val workflowElevenStatuses: List<OrderStatus> = listOf(
            ORDERED,
            CONFIRMED,
            PROCESSING,
            READY_TO_SHIP,
            SHIPPED,
            OUT_FOR_DELIVERY,
            DELIVERED,
            CANCELLED,
            RETURN_REQUESTED,
            RETURNED,
            REFUNDED
        )
    }
}

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val subcategory: String,
    val price: Int,
    val originalPrice: Int,
    val rating: Float,
    val ratingCount: Int,
    val imageRes: Int,
    val fabric: String,
    val sizes: List<String> = listOf("S", "M", "L", "XL", "XXL"),
    val isMahaDeal: Boolean = false,
    val isMallAssured: Boolean = true,
    val inStock: Boolean = true,
    val codPrice: Int = price + 29,
    val upiPrice: Int = price,
    val description: String = "Premium designer wear from Gyanvi Dresses with fine artisan stitching, premium fabric, and elegant festive drape.",
    val imageUrl: String = "",
    val customImageUri: String? = null,
    val categoryId: String = "",
    val subcategoryId: String = "",
    val additionalCategories: List<String> = emptyList(),
    val brandName: String = "GYANVI DRESSES",
    val color: String = "",
    val weight: String = "350g",
    val numericSizes: List<String> = emptyList(),
    val extraImageUrls: List<String> = emptyList(),
    val sku: String = "",
    val packerName: String = "Gyanvi Dresses Packaging & Logistics Hub",
    val packerId: String = "PKR-GD-845438",
    val stockCount: Int = 25
) {
    val discountPercent: Int
        get() = if (originalPrice > price) (((originalPrice - price).toFloat() / originalPrice) * 100).toInt() else 0

    val resolvedImageUrl: String
        get() = customImageUri?.takeIf { it.isNotBlank() } ?: imageUrl

    val allAssignedCategories: List<String>
        get() = (listOf(category, subcategory) + additionalCategories)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

    fun matchesCategory(targetCategory: String): Boolean {
        val target = targetCategory.trim()
        if (target.isBlank() || target.equals("All", ignoreCase = true)) return true
        val aliasTokens = com.example.data.ClothingCatalogTaxonomy.resolveAliasGroup(target)
        val normalizedTargets = listOf(target, target.removeSuffix("s"), "${target}s")
        return allAssignedCategories.any { assigned ->
            val assignedLower = assigned.lowercase()
            aliasTokens.contains(assignedLower) ||
                normalizedTargets.any { norm ->
                    assigned.equals(norm, ignoreCase = true) ||
                        assigned.contains(target, ignoreCase = true) ||
                        target.contains(assigned, ignoreCase = true)
                }
        }
    }
}

data class CategoryItem(
    val id: String,
    val name: String,
    val imageRes: Int,
    val offerBadge: String = "",
    val customImageUri: String? = null,
    val seoName: String = name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-'),
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

data class SubCategoryItem(
    val id: String,
    val parentCategory: String,
    val name: String,
    val imageRes: Int,
    val parentCategoryId: String = "",
    val customImageUri: String? = null,
    val seoName: String = name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-'),
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

data class CartItem(
    val product: Product,
    var selectedSize: String,
    var quantity: Int = 1
) {
    val totalCodPrice: Int get() = product.codPrice * quantity
    val totalUpiPrice: Int get() = product.upiPrice * quantity
}

data class TrackingStage(
    val title: String,
    val subtitle: String,
    val isCompleted: Boolean,
    val dateStr: String
)

data class OrderItem(
    val orderId: String,
    val product: Product,
    val orderDate: String,
    val status: OrderStatus,
    val statusText: String, // e.g. "Delivered Early", "Order Cancelled"
    val deliveryDateRange: String, // e.g. "Mon, 17 Jul - Sun, 23 Jul"
    val selectedSize: String,
    val quantity: Int,
    val totalAmount: Int,
    val trackingStages: List<TrackingStage>,
    var userRating: Int = 0,
    var userReviewText: String = "",
    var isFeedbackCompleted: Boolean = false,
    val paymentMethod: String = "UPI (PhonePe)",
    val awbNumber: String = "DLV-845438912",
    val customerName: String = "Gyanvi Customer",
    val deliveryAddress: String = "Bettiah, Bihar - 845438",
    val isLabelDownloaded: Boolean = false,
    val sellerStage: String = "",
    val customerPhone: String = "+91 9876543210",
    val selectedColor: String = "Festive Maroon",
    val paymentStatus: String = "Paid",
    val isOrderConfirmed: Boolean = false,
    val isQualityVerified: Boolean = false,
    val isPacked: Boolean = false,
    val isInvoiceGenerated: Boolean = false,
    val isBarcodeScanned: Boolean = false,
    val courierPartner: String = "Delhivery Express",
    val pickupStatus: String = "Not Requested",
    val shippingCharges: Int = 49,
    val expectedDeliveryDate: String = "20 Nov 2026",
    val currentTrackingLocation: String = "Gyanvi Boutique Hub, Bettiah",
    val isDeliveryDelayed: Boolean = false,
    val isRtoFlagged: Boolean = false,
    val rtoReason: String = "",
    val cancelReason: String = "",
    val cancelledBy: String = "",
    val cancellationDate: String = "",
    val refundStatus: String = "Not Applicable",
    val refundAmount: Int = 0,
    val isStockRestored: Boolean = false,
    val holdReason: String = "",
    val holdDate: String = "",
    val holdNotes: String = "",
    val holdExpiryAlert: String = "48 hrs Hold Limit",
    val isAdminApprovedHold: Boolean = true,
    val sellerHoldPermission: Boolean = true,
    val orderHistoryLog: List<String> = emptyList(),
    val city: String = "Bettiah",
    val state: String = "Bihar",
    val pinCode: String = "845438",
    val customProductImageUri: String? = null,
    val customerAgeYears: String = "26 Years",
    val packerName: String = "Gyanvi Dresses Packaging & Logistics Hub",
    val packerId: String = "PKR-GD-845438"
)

data class SellerOrderActions(
    val autoPendingNotificationEnabled: Boolean = true,
    val onToggleAutoNotification: (Boolean) -> Unit = {},
    val autoTrackingEnabled: Boolean = true,
    val onToggleAutoTracking: (Boolean) -> Unit = {},
    val autoRefundTrackingEnabled: Boolean = true,
    val onToggleAutoRefundTracking: (Boolean) -> Unit = {},
    val onConfirmOrder: (String) -> Unit = {},
    val onHoldOrder: (orderId: String, reason: String, notes: String, adminApproved: Boolean) -> Unit = { _, _, _, _ -> },
    val onReleaseHold: (String) -> Unit = {},
    val onSendHoldReminder: (String) -> Unit = {},
    val onEditOrderDetails: (orderId: String, customerName: String, phone: String, address: String, size: String, color: String) -> Unit = { _, _, _, _, _, _ -> },
    val onEditHoldFullOrderDetails: (orderId: String, customerName: String, phone: String, address: String, city: String, state: String, pinCode: String, size: String, color: String, customImageUri: String?) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    val onEditCancelledFullDetails: (orderId: String, awbNumber: String, courierPartner: String, customerName: String, phone: String, address: String, city: String, state: String, pinCode: String, customerAgeYears: String, size: String, color: String, cancelReason: String, cancelledBy: String, refundStatus: String, refundAmount: Int, restoreStock: Boolean, customImageUri: String?) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    val onAddHoldProductOrder: (customerName: String, phone: String, address: String, city: String, state: String, pinCode: String, product: Product, size: String, color: String, quantity: Int, holdReason: String, holdNotes: String, customImageUri: String?) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    val onAddPendingProductOrder: (customerName: String, phone: String, address: String, city: String, state: String, pinCode: String, product: Product, size: String, color: String, quantity: Int, paymentMethod: String, customImageUri: String?) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
    val onAddShippedCourierOrder: (customerName: String, phone: String, address: String, city: String, state: String, pinCode: String, product: Product, size: String, color: String, quantity: Int, courierPartner: String, awbNumber: String, expectedDelivery: String, customImageUri: String?) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    val onAddCancelledRecordOrder: (customerName: String, phone: String, address: String, city: String, state: String, pinCode: String, customerAgeYears: String, product: Product, size: String, color: String, quantity: Int, awbNumber: String, courierPartner: String, cancelReason: String, cancelledBy: String, restoreStock: Boolean, customImageUri: String?) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    val onUpdateShippedAwbAndDetails: (orderId: String, awbNumber: String, courierPartner: String, trackingLocation: String, expectedDelivery: String, customerName: String, phone: String, address: String, city: String, state: String, pinCode: String, size: String, color: String, customImageUri: String?) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    val onSendCustomerTrackingAlert: (String) -> Unit = {},
    val onLogFailedDeliveryAttempt: (String, String) -> Unit = { _, _ -> },
    val onMarkCourierPdfSent: (List<String>, String) -> Unit = { _, _ -> },
    val onVerifyProduct: (orderId: String, qualityOk: Boolean, sizeColorOk: Boolean, packedOk: Boolean) -> Unit = { _, _, _, _ -> },
    val onGenerateInvoice: (String) -> Unit = {},
    val onGenerateAwb: (orderId: String, courierPartner: String) -> Unit = { _, _ -> },
    val onScanBarcode: (String) -> Unit = {},
    val onRequestPickup: (orderId: String, courierPartner: String) -> Unit = { _, _ -> },
    val onBulkRequestPickup: (orderIds: List<String>, courierPartner: String) -> Unit = { _, _ -> },
    val onUpdateReadyToShipDetails: (orderId: String, courierPartner: String, awbNumber: String, pickupStatus: String, shippingCharges: Int, expectedDeliveryDate: String, customerName: String, phone: String, address: String, city: String, state: String, pinCode: String, size: String, color: String, customImageUri: String?) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    val onAddReadyToShipProductOrder: (customerName: String, phone: String, address: String, city: String, state: String, pinCode: String, product: Product, size: String, color: String, quantity: Int, courierPartner: String, awbNumber: String, shippingCharges: Int, expectedDeliveryDate: String, customImageUri: String?) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    val onReadyToShip: (String) -> Unit = {},
    val onDispatchOrder: (String, String) -> Unit = { _, _ -> },
    val onBulkDispatchOrders: (List<String>, String) -> Unit = { _, _ -> },
    val onAdvanceTracking: (String) -> Unit = {},
    val onSyncLiveCourierTracking: () -> Unit = {},
    val onToggleDelayAlert: (String, Boolean, String) -> Unit = { _, _, _ -> },
    val onMarkRto: (String, String) -> Unit = { _, _ -> },
    val onCancelOrderDetailed: (orderId: String, reason: String, cancelledBy: String, restoreStock: Boolean) -> Unit = { _, _, _, _ -> },
    val onInitiateRefund: (String) -> Unit = {},
    val onCompleteRefund: (String) -> Unit = {},
    val onRestoreStock: (String) -> Unit = {},
    val onMarkLabelDownloaded: (String) -> Unit = {},
    val onAddManualOrder: (customerName: String, phone: String, address: String, product: Product, size: String, color: String, quantity: Int, paymentMethod: String) -> Unit = { _, _, _, _, _, _, _, _ -> }
)

data class VideoFind(
    val id: String,
    val product: Product,
    val creatorName: String,
    val categoryTag: String,
    var likesCount: Int,
    var isLiked: Boolean = false,
    var isMuted: Boolean = false
)

data class FilterState(
    val query: String = "",
    val selectedCategory: String = "All",
    val sortOption: SortOption = SortOption.RELEVANCE,
    val minRating: Float = 0f,
    val maxPrice: Int = 5000,
    val onlyInStock: Boolean = false,
    val onlyMahaDeals: Boolean = false,
    val selectedGender: String = "All"
)

enum class UserAccountRole(val label: String, val hindiLabel: String, val shortTitle: String = label) {
    BUYER("Customer (ग्राहक)", "ग्राहक ऐप", "Customer"),
    STAFF("Staff (कर्मचारी)", "कर्मचारी पैनल", "Staff"),
    SELLER("Seller (विक्रेता)", "सेलर पैनल", "Seller"),
    ADMIN("Owner (मालिक)", "मालिक एडमिन", "Owner")
}

enum class PaymentGatewayOption(val label: String, val badge: String) {
    GPAY("Google Pay (GPay)", "Instant • ₹0 Fee"),
    PHONEPE("PhonePe UPI", "Most Popular"),
    PAYTM("Paytm Wallet / UPI", "Cashback Ready"),
    UPI_ID("UPI Payment (Any ID)", "BHIM / GPay / YBL"),
    DEBIT_CARD("Debit Card", "Visa / Mastercard / RuPay"),
    CREDIT_CARD("Credit Card", "EMI & Reward Cards"),
    NET_BANKING("Net Banking", "SBI / HDFC / ICICI / Axis / PNB"),
    CUSTOM_URL("Razorpay / Cashfree / Gateway URLs 🔗", "All 12+ Gateways Connected"),
    COD("Cash on Delivery (COD)", "Pay on Delivery")
}

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val category: String, // "Orders", "Offers", "Wallet", "Security", "Seller"
    val timeAgo: String,
    val isRead: Boolean = false
)

data class NotificationPreferences(
    val pushEnabled: Boolean = true,
    val whatsappEnabled: Boolean = true,
    val smsEnabled: Boolean = true,
    val emailEnabled: Boolean = true,
    val orderUpdates: Boolean = true,
    val festiveOffers: Boolean = true,
    val abandonedCartAlerts: Boolean = true,
    val priceDropAlerts: Boolean = true,
    val securityAlerts: Boolean = true,
    val soundAndVibration: Boolean = true
)

data class MarketplaceCoupon(
    val code: String,
    val description: String,
    val discountFlat: Int,
    val minOrder: Int,
    val isActive: Boolean = true
)

data class BuyerSellerChatMessage(
    val id: String,
    val senderName: String,
    val senderRole: String, // "BUYER" or "SELLER"
    val productName: String = "",
    val text: String,
    val timeLabel: String
)

data class FraudAlertItem(
    val id: String,
    val targetLabel: String,
    val reason: String,
    val riskScore: Int,
    val status: String = "Flagged" // "Flagged", "Verified", "Blocked"
)

enum class StaffDepartment(
    val title: String,
    val hindiTitle: String,
    val shortBadge: String,
    val accentHex: Long,
    val responsibilities: List<String>
) {
    ORDER_PACKER(
        title = "Order & Packing Staff",
        hindiTitle = "ऑर्डर व पैकिंग कर्मचारी",
        shortBadge = "PACKER",
        accentHex = 0xFF9C1B68,
        responsibilities = listOf(
            "Verify new Pending orders & Confirm them",
            "Pack dresses & attach Packer ID tag",
            "Move orders to Ready to Ship & download Shipping Label PDF"
        )
    ),
    DISPATCH_DELIVERY(
        title = "Dispatch & Delivery Rider",
        hindiTitle = "डिस्पैच व डिलीवरी बॉय",
        shortBadge = "DELIVERY",
        accentHex = 0xFF0369A1,
        responsibilities = listOf(
            "Dispatch Ready-to-Ship parcels via courier hub",
            "Update Out for Delivery & mark Delivered at doorstep",
            "Handle Reverse Return Pickups & flag RTO parcels"
        )
    ),
    CATALOG_INVENTORY(
        title = "Catalog & Stock Staff",
        hindiTitle = "प्रोडक्ट व स्टॉक कर्मचारी",
        shortBadge = "CATALOG",
        accentHex = 0xFF7C3AED,
        responsibilities = listOf(
            "Update live stock count (+/- inventory) across warehouses",
            "Pause Out-of-Stock dresses & restock low-stock SKUs",
            "Verify size/color variants and category tags"
        )
    ),
    SUPPORT_RETURNS(
        title = "Customer Support & Returns",
        hindiTitle = "कस्टमर केयर व रिटर्न स्टाफ",
        shortBadge = "SUPPORT",
        accentHex = 0xFF059669,
        responsibilities = listOf(
            "Reply quickly to live Buyer-Seller Chat queries",
            "Schedule Return Pickups & process Returned stock checks",
            "Resolve customer delivery & sizing complaints"
        )
    ),
    FINANCE_ACCOUNTS(
        title = "Accounts & Billing Staff",
        hindiTitle = "अकाउंट्स व बिलिंग कर्मचारी",
        shortBadge = "ACCOUNTS",
        accentHex = 0xFFD97706,
        responsibilities = listOf(
            "Download GST Tax Invoices for fulfilled & COD orders",
            "Audit daily Prepaid UPI vs Cash on Delivery (COD) tally",
            "Prepare payout sheet (Final Payout Release: Owner Only)"
        )
    ),
    DEPARTMENT_MANAGER(
        title = "Department Manager",
        hindiTitle = "विभाग मैनेजर (Manager)",
        shortBadge = "MANAGER",
        accentHex = 0xFF4338CA,
        responsibilities = listOf(
            "Supervise Packing, Delivery, Catalog, Support & Accounts teams",
            "Track daily Employee Attendance & Work Performance scores",
            "Assign urgent tasks & coordinate shift escalations"
        )
    )
}

data class StaffMember(
    val id: String,
    val name: String,
    val staffCode: String,
    val phone: String,
    val pin: String,
    val department: StaffDepartment,
    val shift: String = "Morning (9 AM - 6 PM)",
    val tasksCompleted: Int = 0,
    val isActive: Boolean = true,
    val monthlySalary: Int = 18000,
    val isSalaryPaidThisMonth: Boolean = false,
    val lastSalaryPaidDate: String = "01 Nov 2026",
    val attendanceStatus: String = "Present", // "Present", "On Break", "Checked Out", "Absent"
    val checkInTime: String = "09:15 AM",
    val attendanceDaysThisMonth: Int = 22,
    val performanceScore: Int = 94,
    val canPackOrders: Boolean = department == StaffDepartment.ORDER_PACKER || department == StaffDepartment.DEPARTMENT_MANAGER,
    val canDeliverOrders: Boolean = department == StaffDepartment.DISPATCH_DELIVERY || department == StaffDepartment.DEPARTMENT_MANAGER,
    val canEditStock: Boolean = department == StaffDepartment.CATALOG_INVENTORY || department == StaffDepartment.DEPARTMENT_MANAGER,
    val canHandleSupport: Boolean = department == StaffDepartment.SUPPORT_RETURNS || department == StaffDepartment.DEPARTMENT_MANAGER,
    val canViewGstFinance: Boolean = department == StaffDepartment.FINANCE_ACCOUNTS || department == StaffDepartment.DEPARTMENT_MANAGER,
    val canManageStaff: Boolean = department == StaffDepartment.DEPARTMENT_MANAGER
)

data class StaffAssignedTask(
    val id: String,
    val title: String,
    val description: String,
    val department: StaffDepartment,
    val assignedToStaffId: String,
    val assignedToName: String,
    val relatedOrderId: String = "",
    val priority: String = "High", // "High", "Medium", "Normal"
    val status: String = "Pending", // "Pending", "In Progress", "Completed"
    val assignedBy: String = "Owner (Amit Kumar Patwa)"
)

data class SecurityActivityEntry(
    val id: String,
    val actorName: String,
    val roleBadge: String,
    val action: String,
    val timestamp: String
)
