package com.example

import com.example.data.SampleData
import com.example.data.remote.FirestoreOrderDoc
import com.example.model.OrderStatus
import com.example.model.SortOption
import com.example.ui.components.QrAwbPayloadMatcher
import com.example.ui.screens.DashboardOrderStatusFilter
import com.example.ui.screens.filterOrdersByDashboardStatus
import com.example.viewmodel.GyanviViewModel
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GyanviViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: GyanviViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = GyanviViewModel(enableTimer = false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialProductsLoaded() {
        val products = viewModel.allProducts.value
        assertTrue("Products should not be empty", products.isNotEmpty())
    }

    @Test
    fun testSearchFiltersProducts() = runTest {
        viewModel.onSearchQueryChange("Saree")
        testScheduler.runCurrent()

        val results = viewModel.filteredProducts.value
        assertTrue("Should return saree products", results.all {
            it.name.contains("Saree", ignoreCase = true) ||
            it.category.contains("Saree", ignoreCase = true) ||
            it.subcategory.contains("Saree", ignoreCase = true)
        })
    }

    @Test
    fun testCartOperations() {
        val product = viewModel.allProducts.value.first()
        viewModel.addToCart(product, size = "XL", quantity = 1)

        val cart = viewModel.cartItems.value
        assertTrue(cart.any { it.product.id == product.id && it.selectedSize == "XL" })

        // Update quantity
        viewModel.updateCartQuantity(product.id, "XL", 3)
        val updatedItem = viewModel.cartItems.value.first { it.product.id == product.id && it.selectedSize == "XL" }
        assertEquals(3, updatedItem.quantity)

        // Remove item
        viewModel.removeFromCart(product.id, "XL")
        assertFalse(viewModel.cartItems.value.any { it.product.id == product.id && it.selectedSize == "XL" })
    }

    @Test
    fun testWishlistToggle() {
        val testId = "gd_01"
        val initiallyContains = viewModel.wishlistIds.value.contains(testId)

        viewModel.toggleWishlist(testId)
        assertEquals(!initiallyContains, viewModel.wishlistIds.value.contains(testId))

        viewModel.toggleWishlist(testId)
        assertEquals(initiallyContains, viewModel.wishlistIds.value.contains(testId))
    }

    @Test
    fun testOrderPlacement() {
        val initialOrdersCount = viewModel.orders.value.size
        val initialCartCount = viewModel.cartItems.value.size
        viewModel.addToCart(viewModel.allProducts.value.first(), size = "L", quantity = 1)
        val expectedNewOrders = initialCartCount + 1
        viewModel.checkoutAndPlaceOrder()

        assertEquals(initialOrdersCount + expectedNewOrders, viewModel.orders.value.size)
        assertTrue("Cart should be empty after checkout", viewModel.cartItems.value.isEmpty())
    }

    @Test
    fun testShippedOrderFilter() = runTest {
        viewModel.setOrderStatusFilter(OrderStatus.SHIPPED)
        testScheduler.runCurrent()

        val shippedOrders = viewModel.filteredOrders.value
        assertTrue("Shipped orders should not be empty", shippedOrders.isNotEmpty())
        assertTrue("All returned orders must have SHIPPED status", shippedOrders.all { it.status == OrderStatus.SHIPPED })
    }

    @Test
    fun testOpenShareFlow() {
        val product = viewModel.allProducts.value.first()
        viewModel.openShare(product)

        assertEquals(com.example.viewmodel.AppSubScreen.SHARE_SHEET, viewModel.subScreen.value)
        assertEquals(product.id, viewModel.selectedProductForShare.value?.id)

        viewModel.openShareContacts()
        assertEquals(com.example.viewmodel.AppSubScreen.SHARE_CONTACTS_SHEET, viewModel.subScreen.value)
    }

    @Test
    fun testSubScreenNavigation() {
        viewModel.openSubScreen(com.example.viewmodel.AppSubScreen.SELLER_PANEL)
        assertEquals(com.example.viewmodel.AppSubScreen.SELLER_PANEL, viewModel.subScreen.value)

        viewModel.closeSubScreen()
        assertEquals(com.example.viewmodel.AppSubScreen.NONE, viewModel.subScreen.value)

        viewModel.openSubScreen(com.example.viewmodel.AppSubScreen.ACCOUNT)
        assertEquals(com.example.viewmodel.AppSubScreen.ACCOUNT, viewModel.subScreen.value)

        viewModel.openSubScreen(com.example.viewmodel.AppSubScreen.MY_PAYMENTS)
        assertEquals(com.example.viewmodel.AppSubScreen.MY_PAYMENTS, viewModel.subScreen.value)

        viewModel.openSubScreen(com.example.viewmodel.AppSubScreen.NOTIFICATIONS)
        assertEquals(com.example.viewmodel.AppSubScreen.NOTIFICATIONS, viewModel.subScreen.value)
    }

    @Test
    fun testDarkModeLanguageAndRoleSwitch() {
        assertFalse(viewModel.isDarkMode.value)
        viewModel.toggleDarkMode(true)
        assertTrue(viewModel.isDarkMode.value)

        viewModel.setAppLanguage("हिंदी")
        assertEquals("हिंदी", viewModel.appLanguage.value)

        viewModel.switchUserRole(com.example.model.UserAccountRole.ADMIN)
        assertEquals(com.example.model.UserAccountRole.ADMIN, viewModel.activeRole.value)
        assertEquals(com.example.viewmodel.AppSubScreen.ADMIN_PANEL, viewModel.subScreen.value)
    }

    @Test
    fun testNotificationSystemAndPrefs() {
        val initialSize = viewModel.notifications.value.size
        viewModel.pushNotification("Flash Sale", "Flat 70% off", "Offers")
        assertEquals(initialSize + 1, viewModel.notifications.value.size)

        val newest = viewModel.notifications.value.first()
        assertFalse(newest.isRead)
        viewModel.markNotificationRead(newest.id)
        assertTrue(viewModel.notifications.value.first { it.id == newest.id }.isRead)

        viewModel.markAllNotificationsRead()
        assertTrue(viewModel.notifications.value.all { it.isRead })

        viewModel.updateNotificationPrefs { it.copy(whatsappEnabled = false, smsEnabled = false) }
        assertFalse(viewModel.notificationPrefs.value.whatsappEnabled)
        assertFalse(viewModel.notificationPrefs.value.smsEnabled)
    }

    @Test
    fun testProductCompareAndOrderCancelRefund() {
        val products = viewModel.allProducts.value
        assertTrue(products.size >= 4)

        viewModel.toggleCompareProduct(products[0])
        viewModel.toggleCompareProduct(products[1])
        viewModel.toggleCompareProduct(products[2])
        viewModel.toggleCompareProduct(products[3])
        assertEquals(3, viewModel.compareProducts.value.size)
        assertFalse(viewModel.compareProducts.value.any { it.id == products[0].id })

        viewModel.clearCompareProducts()
        assertTrue(viewModel.compareProducts.value.isEmpty())

        // Checkout with wallet and verify reward points + cancel refund
        val startWallet = viewModel.walletBalance.value
        val startPoints = viewModel.rewardPoints.value
        viewModel.checkoutAndPlaceOrder(
            paymentMethod = "Google Pay (UPI)",
            discountAmount = 50,
            walletUsed = 50
        )
        assertTrue(viewModel.rewardPoints.value > startPoints)
        assertEquals(startWallet - 50, viewModel.walletBalance.value)

        val placedOrder = viewModel.orders.value.first()
        viewModel.cancelOrder(placedOrder.orderId, "Ordered wrong size")
        assertEquals(OrderStatus.CANCELLED, viewModel.orders.value.first { it.orderId == placedOrder.orderId }.status)
        assertEquals(startWallet - 50 + placedOrder.totalAmount, viewModel.walletBalance.value)
    }

    @Test
    fun testMarketplaceAdvancedSetupAndProgress() {
        viewModel.openSubScreen(com.example.viewmodel.AppSubScreen.DEVELOPMENT_PROGRESS)
        assertEquals(com.example.viewmodel.AppSubScreen.DEVELOPMENT_PROGRESS, viewModel.subScreen.value)

        viewModel.setCurrency("USD $")
        assertEquals("USD $", viewModel.selectedCurrency.value)

        viewModel.setWarehouse("Patna Fulfillment Center (800001)")
        assertEquals("Patna Fulfillment Center (800001)", viewModel.activeWarehouse.value)

        val beforeNotifs = viewModel.notifications.value.size
        viewModel.toggleVipSubscription(true)
        assertTrue(viewModel.isVipSubscribed.value)
        assertEquals(beforeNotifs + 1, viewModel.notifications.value.size)

        val startSellers = viewModel.registeredSellersCount.value
        viewModel.registerNewSeller("Gyanvi Bridal Silk", "Amit Kumar Patwa", "10GYNVI1234F1Z5")
        assertEquals(startSellers + 1, viewModel.registeredSellersCount.value)
    }

    @Test
    fun testMandatoryAuthGateAndRegistrationUnlock() = runTest {
        // 1. Fresh launch starts locked on Front-Page Auth Gate (currentUser == null)
        assertNull("App must start locked until user registers or logs in", viewModel.currentUser.value)

        // 2. Registering a Customer account unlocks the app
        var registerSuccess = false
        viewModel.registerUserAccount(
            accountType = com.example.data.local.AccountType.CUSTOMER,
            fullName = "Priya Sharma",
            mobileNumber = "9876543210",
            email = "priya@example.com",
            password = "Password@123"
        ) { result ->
            registerSuccess = result is com.example.data.local.AuthResult.Success
        }
        testScheduler.runCurrent()

        assertTrue("Registration should succeed", registerSuccess)
        assertNotNull("Customer should be signed in after registration", viewModel.currentUser.value)
        assertEquals("Priya Sharma", viewModel.currentUser.value?.fullName)
        assertEquals(com.example.viewmodel.AppSubScreen.NONE, viewModel.subScreen.value)

        // 3. Logging out re-locks the app back to the Front-Page Auth Gate
        viewModel.logoutCurrentUser()
        assertNull("Logout should clear currentUser and lock the app", viewModel.currentUser.value)
        assertEquals(com.example.viewmodel.AppSubScreen.NONE, viewModel.subScreen.value)

        // 4. Wrong password must keep app locked
        var wrongPasswordRejected = false
        viewModel.loginUserAccount(
            identifier = "9876543210",
            password = "WrongPassword",
            accountType = com.example.data.local.AccountType.CUSTOMER
        ) { result ->
            wrongPasswordRejected = result is com.example.data.local.AuthResult.Error
        }
        testScheduler.runCurrent()

        assertTrue("Wrong password must return Error", wrongPasswordRejected)
        assertNull("App must remain locked on wrong password", viewModel.currentUser.value)

        // 5. Correct Mobile / Email + Password unlocks the app again
        var loginSuccess = false
        viewModel.loginUserAccount(
            identifier = "priya@example.com",
            password = "Password@123",
            accountType = com.example.data.local.AccountType.CUSTOMER
        ) { result ->
            loginSuccess = result is com.example.data.local.AuthResult.Success
        }
        testScheduler.runCurrent()

        assertTrue("Valid login should succeed", loginSuccess)
        assertNotNull("Valid login must unlock app", viewModel.currentUser.value)
        assertEquals("9876543210", viewModel.currentUser.value?.mobileNumber)
    }

    @Test
    fun testAppManagementCmsDashboardControls() {
        viewModel.openSubScreen(com.example.viewmodel.AppSubScreen.APP_MANAGEMENT)
        assertEquals(com.example.viewmodel.AppSubScreen.APP_MANAGEMENT, viewModel.subScreen.value)

        // 1. Change Dashboard Theme, Layout & Card Corners
        viewModel.setDashboardThemePreset(com.example.model.DashboardThemePreset.ROYAL_MAROON_GOLD)
        viewModel.setHomeLayoutMode(com.example.model.HomeLayoutMode.THREE_COLUMN)
        viewModel.setCardCornerPreset(com.example.model.CardCornerPreset.ROUNDED)
        assertEquals(
            com.example.model.DashboardThemePreset.ROYAL_MAROON_GOLD,
            viewModel.managementConfig.value.themePreset
        )
        assertEquals(3, viewModel.managementConfig.value.homeLayoutMode.columns)
        assertEquals(20, viewModel.managementConfig.value.cardCornerPreset.radiusDp)

        // 2. Image Change Dashboard
        viewModel.updateAppImages { it.copy(slide1CustomUri = "content://gallery/banner_1.jpg") }
        assertEquals("content://gallery/banner_1.jpg", viewModel.managementConfig.value.images.slide1CustomUri)

        // 3. Text Change Dashboard
        viewModel.updateAppTexts { it.copy(brandTitle = "Gyanvi Bridal Studio", announcementText = "Flat 50% OFF Today") }
        assertEquals("Gyanvi Bridal Studio", viewModel.managementConfig.value.texts.brandTitle)
        assertEquals("Flat 50% OFF Today", viewModel.managementConfig.value.texts.announcementText)

        // 4. Likhavat (Font & Typography) Change Dashboard
        viewModel.updateAppTypography {
            it.copy(
                fontStyle = com.example.model.AppFontStyle.PLAYFAIR_SERIF,
                fontScale = 1.15f,
                headingWeight = com.example.model.HeadingWeightStyle.EXTRA_BOLD,
                letterSpacing = com.example.model.LetterSpacingPreset.WIDE
            )
        }
        assertEquals(
            com.example.model.AppFontStyle.PLAYFAIR_SERIF,
            viewModel.managementConfig.value.typography.fontStyle
        )
        assertEquals(1.15f, viewModel.managementConfig.value.typography.fontScale, 0.001f)

        // 5. Options & Features Hide / Toggle
        viewModel.updateAppFeatures { it.copy(showVoiceSearch = false, showMallTab = false) }
        assertFalse(viewModel.managementConfig.value.features.showVoiceSearch)
        assertFalse(viewModel.managementConfig.value.features.showMallTab)

        // 6. Edit / Add / Delete Categories & Products
        val initialCats = viewModel.categories.value.size
        viewModel.addCategory("Bridal Special", "NEW", com.example.R.drawable.dress_lehenga_1793417067159)
        assertEquals(initialCats + 1, viewModel.categories.value.size)
        val addedCat = viewModel.categories.value.last()
        viewModel.deleteCategory(addedCat.id)
        assertEquals(initialCats, viewModel.categories.value.size)

        val firstProd = viewModel.allProducts.value.first()
        viewModel.editCatalogProduct(
            productId = firstProd.id,
            name = "Edited Anarkali Suit",
            category = firstProd.category,
            price = 499,
            originalPrice = 1599,
            fabric = "Pure Silk",
            description = "Edited desc",
            inStock = true,
            isMahaDeal = true,
            isMallAssured = true,
            imageRes = firstProd.imageRes,
            customImageUri = "content://gallery/anarkali_custom.jpg"
        )
        val edited = viewModel.allProducts.value.first { it.id == firstProd.id }
        assertEquals("Edited Anarkali Suit", edited.name)
        assertEquals(499, edited.price)
        assertEquals("content://gallery/anarkali_custom.jpg", edited.customImageUri)

        val beforeDeleteCount = viewModel.allProducts.value.size
        viewModel.deleteCatalogProduct(firstProd.id)
        assertEquals(beforeDeleteCount - 1, viewModel.allProducts.value.size)

        // 7. Reset All restores factory defaults
        viewModel.resetAllAppManagementToDefaults()
        assertEquals(
            com.example.model.DashboardThemePreset.YELLOW_PINK,
            viewModel.managementConfig.value.themePreset
        )
        assertEquals(
            com.example.model.AppFontStyle.MODERN_BOUTIQUE,
            viewModel.managementConfig.value.typography.fontStyle
        )
        assertTrue(viewModel.managementConfig.value.features.showVoiceSearch)
        assertEquals(beforeDeleteCount, viewModel.allProducts.value.size)
    }

    @Test
    fun testAppBackupCreateRestoreAndDelete() {
        // Configure custom theme, font, floating controls, text & product
        viewModel.setDashboardThemePreset(com.example.model.DashboardThemePreset.ROYAL_MAROON_GOLD)
        viewModel.updateAppTexts { it.copy(brandTitle = "Gyanvi Backup Edition") }
        viewModel.updateAppTypography { it.copy(fontStyle = com.example.model.AppFontStyle.PLAYFAIR_SERIF) }
        viewModel.updateAppFeatures { it.copy(showFloatingQuickControls = false) }
        val initialProdCount = viewModel.allProducts.value.size

        val snapshot = viewModel.createAppBackup("Diwali Custom State")
        assertEquals("Diwali Custom State", snapshot.label)
        assertEquals(1, viewModel.appBackups.value.size)
        assertTrue(viewModel.lastBackupJsonText.value.contains("Gyanvi Backup Edition"))

        // Mutate everything away from backup
        viewModel.setDashboardThemePreset(com.example.model.DashboardThemePreset.SAPPHIRE_ROSE)
        viewModel.updateAppTexts { it.copy(brandTitle = "Changed Store") }
        viewModel.updateAppFeatures { it.copy(showFloatingQuickControls = true) }
        viewModel.deleteCatalogProduct(viewModel.allProducts.value.first().id)
        assertEquals(initialProdCount - 1, viewModel.allProducts.value.size)

        // Restore backup
        val restored = viewModel.restoreAppBackup(snapshot.id)
        assertTrue(restored)
        assertEquals(com.example.model.DashboardThemePreset.ROYAL_MAROON_GOLD, viewModel.managementConfig.value.themePreset)
        assertEquals("Gyanvi Backup Edition", viewModel.managementConfig.value.texts.brandTitle)
        assertEquals(com.example.model.AppFontStyle.PLAYFAIR_SERIF, viewModel.managementConfig.value.typography.fontStyle)
        assertFalse(viewModel.managementConfig.value.features.showFloatingQuickControls)
        assertEquals(initialProdCount, viewModel.allProducts.value.size)

        // Delete backup
        viewModel.deleteAppBackup(snapshot.id)
        assertTrue(viewModel.appBackups.value.isEmpty())
    }

    @Test
    fun testMobileOtpGenerationAndCustomerOtpLoginGate() = runTest {
        assertNull("App must start locked", viewModel.currentUser.value)

        // 1. Short mobile number must reject OTP generation
        val (badOk, _) = viewModel.generateMobileLoginOtp("98765")
        assertFalse(badOk)
        assertFalse(viewModel.loginOtpSession.value.isOtpGenerated)

        // 2. Trying to login without OTP must keep customer logged out
        var ungeneratedResult: com.example.data.local.AuthResult? = null
        viewModel.verifyOtpAndLoginCustomer("9988776655", "123456") { ungeneratedResult = it }
        assertTrue(ungeneratedResult is com.example.data.local.AuthResult.Error)
        assertNull(viewModel.currentUser.value)

        // 3. Valid 10-digit mobile generates a 6-digit OTP
        val (genOk, otp) = viewModel.generateMobileLoginOtp("9988776655")
        assertTrue(genOk)
        assertEquals(6, otp.length)
        assertTrue(otp.all { it.isDigit() })
        assertTrue(viewModel.loginOtpSession.value.isOtpGenerated)
        assertEquals("9988776655", viewModel.loginOtpSession.value.mobileNumber)

        // 4. Wrong OTP keeps customer locked out
        val wrongOtp = if (otp == "000000") "111111" else "000000"
        var wrongResult: com.example.data.local.AuthResult? = null
        viewModel.verifyOtpAndLoginCustomer("9988776655", wrongOtp) { wrongResult = it }
        assertTrue(wrongResult is com.example.data.local.AuthResult.Error)
        assertNull("Customer must NOT log in with wrong OTP", viewModel.currentUser.value)
        assertEquals(1, viewModel.loginOtpSession.value.attemptCount)

        // 5. Exact generated OTP unlocks app and logs customer in
        var validResult: com.example.data.local.AuthResult? = null
        viewModel.verifyOtpAndLoginCustomer("9988776655", otp) { validResult = it }
        testScheduler.runCurrent()

        assertTrue(validResult is com.example.data.local.AuthResult.Success)
        assertNotNull("Customer must be logged in after valid OTP", viewModel.currentUser.value)
        assertEquals("9988776655", viewModel.currentUser.value?.mobileNumber)
        assertEquals(com.example.data.local.AccountType.CUSTOMER, viewModel.currentUser.value?.accountType)
        assertFalse("OTP session should reset after successful login", viewModel.loginOtpSession.value.isOtpGenerated)
    }

    @Test
    fun testSellerOrderLifecycleAndQuickOrderCreation() {
        val initialPending = viewModel.orders.value.count {
            it.status == OrderStatus.ORDERED && !it.sellerStage.equals("Ready to Ship", ignoreCase = true)
        }
        assertTrue("Seller Hub should start with pending orders populated", initialPending > 0)

        val product = viewModel.allProducts.value.first()
        viewModel.createSellerManualOrder(
            customerName = "Sunita Devi",
            address = "Bettiah, Bihar - 845438",
            product = product,
            size = "XL",
            quantity = 2,
            paymentMethod = "Cash on Delivery (COD)"
        )

        val createdOrder = viewModel.orders.value.first()
        assertEquals("Sunita Devi", createdOrder.customerName)
        assertEquals(OrderStatus.ORDERED, createdOrder.status)
        assertEquals("Pending", createdOrder.sellerStage)
        assertFalse(createdOrder.isLabelDownloaded)
        assertEquals(product.price * 2, createdOrder.totalAmount)

        viewModel.markSellerOrderReadyToShip(createdOrder.orderId)
        val readyOrder = viewModel.orders.value.first { it.orderId == createdOrder.orderId }
        assertEquals("Ready to Ship", readyOrder.sellerStage)
        assertTrue(readyOrder.isLabelDownloaded)

        viewModel.dispatchSellerOrder(createdOrder.orderId)
        val shippedOrder = viewModel.orders.value.first { it.orderId == createdOrder.orderId }
        assertEquals(OrderStatus.SHIPPED, shippedOrder.status)
        assertEquals("Shipped", shippedOrder.sellerStage)
    }

    @Test
    fun testCompleteHoldConfirmPackShipTrackRtoAndCancelRefundLifecycle() {
        val product = viewModel.allProducts.value.first()
        val beforeNotifs = viewModel.notifications.value.size

        // 1. Auto Pending Notification on new ₹599 Kurti order
        viewModel.createSellerManualOrder(
            customerName = "Anjali Kumari",
            phone = "+91 9988776655",
            address = "Old Market, Bettiah (Pin Pending)",
            product = product,
            size = "M",
            color = "Crimson Red",
            quantity = 1,
            paymentMethod = "UPI (GPay)"
        )
        assertTrue(viewModel.notifications.value.size > beforeNotifs)
        val order = viewModel.orders.value.first()
        assertEquals("Pending", order.sellerStage)
        assertEquals("Paid", order.paymentStatus)

        // 2. Place On Hold -> Edit Order -> Release Hold
        viewModel.holdSellerOrder(
            orderId = order.orderId,
            reason = "Incomplete Delivery Address",
            notes = "Confirm pincode",
            adminApproved = true
        )
        assertEquals("On Hold", viewModel.orders.value.first { it.orderId == order.orderId }.sellerStage)

        viewModel.editOrderDetails(
            orderId = order.orderId,
            customerName = "Anjali Kumari",
            phone = "+91 9988776655",
            address = "Lal Bazar, Bettiah, Bihar - 845438",
            size = "L",
            color = "Wine Red"
        )
        val edited = viewModel.orders.value.first { it.orderId == order.orderId }
        assertEquals("L", edited.selectedSize)
        assertEquals("Wine Red", edited.selectedColor)

        viewModel.releaseHoldOrder(order.orderId)
        assertEquals("Pending", viewModel.orders.value.first { it.orderId == order.orderId }.sellerStage)

        // 3. Confirm Order -> Verify Product & Scan Barcode -> Generate Invoice & AWB -> Request Pickup
        viewModel.confirmSellerOrder(order.orderId)
        assertTrue(viewModel.orders.value.first { it.orderId == order.orderId }.isOrderConfirmed)

        viewModel.verifySellerOrderProduct(order.orderId, qualityOk = true, sizeColorOk = true, packedOk = true)
        viewModel.scanOrderBarcode(order.orderId)
        viewModel.generateOrderInvoice(order.orderId)
        viewModel.generateOrderAwb(order.orderId, "Ekart Logistics")
        viewModel.requestCourierPickup(order.orderId, "Ekart Logistics")

        val ready = viewModel.orders.value.first { it.orderId == order.orderId }
        assertEquals("Ready to Ship", ready.sellerStage)
        assertTrue(ready.isQualityVerified)
        assertTrue(ready.isBarcodeScanned)
        assertTrue(ready.isInvoiceGenerated)
        assertEquals("Pickup Requested", ready.pickupStatus)
        assertTrue(ready.awbNumber.startsWith("EKRT-"))

        // 4. Bulk Dispatch -> Automatic Live Tracking (Out for Delivery -> Delivered)
        viewModel.bulkDispatchReadyOrders(listOf(order.orderId), "Ekart Logistics")
        assertEquals(OrderStatus.SHIPPED, viewModel.orders.value.first { it.orderId == order.orderId }.status)

        viewModel.toggleDeliveryDelayAlert(order.orderId, true, "Heavy Rain Transit")
        assertTrue(viewModel.orders.value.first { it.orderId == order.orderId }.isDeliveryDelayed)

        viewModel.advanceShipmentTracking(order.orderId)
        assertEquals("Out for Delivery", viewModel.orders.value.first { it.orderId == order.orderId }.sellerStage)

        viewModel.advanceShipmentTracking(order.orderId)
        val delivered = viewModel.orders.value.first { it.orderId == order.orderId }
        assertEquals(OrderStatus.DELIVERED, delivered.status)
        assertEquals("Delivered", delivered.sellerStage)

        // 5. Separate RTO on already-shipped order & Cancelled Refund/Stock Restore on pending order
        viewModel.markOrderRto(order.orderId, "Customer Requested Return at Doorstep")
        assertTrue(viewModel.orders.value.first { it.orderId == order.orderId }.isRtoFlagged)

        viewModel.createSellerManualOrder(
            customerName = "Cancel Test User",
            phone = "+91 9123456789",
            address = "Patna - 800001",
            product = product,
            size = "S",
            color = "Pink",
            quantity = 1,
            paymentMethod = "UPI (PhonePe)"
        )
        val cancelTarget = viewModel.orders.value.first()
        viewModel.cancelSellerOrderDetailed(
            orderId = cancelTarget.orderId,
            reason = "Wrong Size Selected",
            cancelledBy = "Customer",
            restoreStock = true
        )
        val cancelled = viewModel.orders.value.first { it.orderId == cancelTarget.orderId }
        assertEquals(OrderStatus.CANCELLED, cancelled.status)
        assertEquals("Customer", cancelled.cancelledBy)
        assertEquals("Initiated", cancelled.refundStatus)
        assertTrue(cancelled.isStockRestored)

        viewModel.completeOrderRefund(cancelTarget.orderId)
        assertEquals("Completed", viewModel.orders.value.first { it.orderId == cancelTarget.orderId }.refundStatus)
    }

    @Test
    fun testOnHoldMultiProductBundlingAndFullFieldEdits() {
        val allProds = viewModel.allProducts.value
        val sareeProduct = allProds.firstOrNull { it.category.contains("Saree", true) } ?: allProds.first()
        val lehengaProduct = allProds.firstOrNull { it.category.contains("Lehenga", true) } ?: allProds.last()

        val initialHoldCount = viewModel.orders.value.count {
            it.status == OrderStatus.ORDERED && it.sellerStage.equals("On Hold", true)
        }
        assertTrue("SampleData should contain multi-product On Hold orders initially", initialHoldCount >= 3)

        val notifsBefore = viewModel.notifications.value.size
        viewModel.addHoldProductOrder(
            customerName = "Meera Devi",
            phone = "+91 9811223344",
            address = "Station Chowk, Lane 4",
            city = "Patna",
            state = "Bihar",
            pinCode = "800001",
            product = sareeProduct,
            size = "42",
            color = "Royal Gold",
            quantity = 2,
            holdReason = "ग्राहक से जानकारी की पुष्टि करनी है",
            holdNotes = "Verify saree fall-pico instruction",
            customImageUri = "content://media/external/images/media/901"
        )
        viewModel.addHoldProductOrder(
            customerName = "Roshni Verma",
            phone = "+91 9822334455",
            address = "Kankarbagh Main Road",
            city = "Patna",
            state = "Bihar",
            pinCode = "800020",
            product = lehengaProduct,
            size = "38 (M)",
            color = "Bridal Crimson",
            quantity = 1,
            holdReason = "प्रोडक्ट का Size / Colour उपलब्ध नहीं है",
            holdNotes = "Check choli custom sizing",
            customImageUri = null
        )

        val holdOrdersAfter = viewModel.orders.value.filter {
            it.status == OrderStatus.ORDERED && it.sellerStage.equals("On Hold", true)
        }
        assertEquals(initialHoldCount + 2, holdOrdersAfter.size)
        assertTrue("Automatic Hold Notifications must be pushed", viewModel.notifications.value.size >= notifsBefore + 2)

        val addedSareeOrder = holdOrdersAfter.first { it.customerName == "Meera Devi" }
        assertEquals("Patna", addedSareeOrder.city)
        assertEquals("Bihar", addedSareeOrder.state)
        assertEquals("800001", addedSareeOrder.pinCode)
        assertEquals("42", addedSareeOrder.selectedSize)
        assertEquals("Royal Gold", addedSareeOrder.selectedColor)
        assertEquals("content://media/external/images/media/901", addedSareeOrder.customProductImageUri)

        viewModel.editHoldFullOrderDetails(
            orderId = addedSareeOrder.orderId,
            customerName = "Meera Kumari",
            phone = "+91 9900112233",
            address = "Boring Road Crossing",
            city = "Muzaffarpur",
            state = "Bihar",
            pinCode = "842001",
            size = "44 (XXL)",
            color = "Peacock Green",
            customImageUri = "content://media/external/images/media/902"
        )
        val updated = viewModel.orders.value.first { it.orderId == addedSareeOrder.orderId }
        assertEquals("Meera Kumari", updated.customerName)
        assertEquals("+91 9900112233", updated.customerPhone)
        assertEquals("Boring Road Crossing", updated.deliveryAddress)
        assertEquals("Muzaffarpur", updated.city)
        assertEquals("Bihar", updated.state)
        assertEquals("842001", updated.pinCode)
        assertEquals("44 (XXL)", updated.selectedSize)
        assertEquals("Peacock Green", updated.selectedColor)
        assertEquals("content://media/external/images/media/902", updated.customProductImageUri)
    }

    @Test
    fun testAddPendingNewTypeProductWithFullMetadataAndAutoAlert() {
        val baseProduct = viewModel.allProducts.value.first()
        val customNewTypeProduct = baseProduct.copy(
            id = "custom_bridal_99",
            name = "Silk Mirror Work Anarkali",
            category = "Custom Bridal",
            price = 1299
        )
        val pendingBefore = viewModel.orders.value.count {
            it.status == OrderStatus.ORDERED && it.sellerStage.equals("Pending", true)
        }
        val notifsBefore = viewModel.notifications.value.size

        viewModel.addPendingProductOrder(
            customerName = "Kavita Kumari",
            phone = "+91 9801122334",
            address = "Gandhi Chowk, Ward 7",
            city = "Motihari",
            state = "Bihar",
            pinCode = "845401",
            product = customNewTypeProduct,
            size = "40 (L)",
            color = "Royal Emerald",
            quantity = 2,
            paymentMethod = "Cash on Delivery (COD)",
            customImageUri = "content://media/external/images/media/777"
        )

        val pendingOrders = viewModel.orders.value.filter {
            it.status == OrderStatus.ORDERED && it.sellerStage.equals("Pending", true)
        }
        assertEquals(pendingBefore + 1, pendingOrders.size)
        val created = pendingOrders.first { it.customerName == "Kavita Kumari" }
        assertEquals("Silk Mirror Work Anarkali", created.product.name)
        assertEquals("Custom Bridal", created.product.category)
        assertEquals("+91 9801122334", created.customerPhone)
        assertEquals("Motihari", created.city)
        assertEquals("Bihar", created.state)
        assertEquals("845401", created.pinCode)
        assertEquals("40 (L)", created.selectedSize)
        assertEquals("Royal Emerald", created.selectedColor)
        assertEquals(2598, created.totalAmount)
        assertEquals("COD Pending", created.paymentStatus)
        assertEquals("content://media/external/images/media/777", created.customProductImageUri)
        assertTrue("Pending Auto Notification must be triggered", viewModel.notifications.value.size > notifsBefore)
    }

    @Test
    fun testShippedAwbUpdateCourierOrderFailedDeliveryAndAutoTracking() {
        val baseProduct = viewModel.allProducts.value.first()
        assertTrue("Auto-tracking should start enabled", viewModel.autoTrackingEnabled.value)

        // 1. Add Shipped Courier Order with full customer, city, state, pincode, colour, size, image & AWB
        val notifsBefore = viewModel.notifications.value.size
        viewModel.addShippedCourierOrder(
            customerName = "Pooja Singh",
            phone = "+91 9812345670",
            address = "Boring Canal Road, Near Shiva Temple",
            city = "Patna",
            state = "Bihar",
            pinCode = "800001",
            product = baseProduct,
            size = "40 (L)",
            color = "Royal Indigo",
            quantity = 1,
            courierPartner = "BlueDart Air",
            awbNumber = "BLUD-77889900",
            expectedDelivery = "20 Nov 2026",
            customImageUri = "content://media/external/images/media/501"
        )

        val shipped = viewModel.orders.value.first { it.customerName == "Pooja Singh" }
        assertEquals(OrderStatus.SHIPPED, shipped.status)
        assertEquals("BlueDart Air", shipped.courierPartner)
        assertEquals("BLUD-77889900", shipped.awbNumber)
        assertEquals("Patna", shipped.city)
        assertEquals("Bihar", shipped.state)
        assertEquals("800001", shipped.pinCode)
        assertEquals("40 (L)", shipped.selectedSize)
        assertEquals("Royal Indigo", shipped.selectedColor)
        assertEquals("content://media/external/images/media/501", shipped.customProductImageUri)
        assertTrue("Auto-tracking notification pushed", viewModel.notifications.value.size > notifsBefore)

        // 2. Update AWB Number, Tracking Location, Courier & Details
        viewModel.updateShippedAwbAndDetails(
            orderId = shipped.orderId,
            awbNumber = "EKRT-99001122",
            courierPartner = "Ekart Logistics",
            trackingLocation = "Muzaffarpur Mother Hub",
            expectedDelivery = "21 Nov 2026",
            customerName = "Pooja Kumari",
            phone = "+91 9812345679",
            address = "Juran Chapra Main Road",
            city = "Muzaffarpur",
            state = "Bihar",
            pinCode = "842001",
            size = "42 (XL)",
            color = "Magenta Gold",
            customImageUri = "content://media/external/images/media/502"
        )
        val updated = viewModel.orders.value.first { it.orderId == shipped.orderId }
        assertEquals("EKRT-99001122", updated.awbNumber)
        assertEquals("Ekart Logistics", updated.courierPartner)
        assertEquals("Muzaffarpur Mother Hub", updated.currentTrackingLocation)
        assertEquals("21 Nov 2026", updated.expectedDeliveryDate)
        assertEquals("Pooja Kumari", updated.customerName)
        assertEquals("Muzaffarpur", updated.city)
        assertEquals("842001", updated.pinCode)
        assertEquals("42 (XL)", updated.selectedSize)
        assertEquals("Magenta Gold", updated.selectedColor)
        assertEquals("content://media/external/images/media/502", updated.customProductImageUri)

        // 3. Send Customer SMS Alert, Log Failed Delivery Attempt, Mark Courier PDF Sent, Toggle Auto-Tracking
        val beforeAlert = viewModel.notifications.value.size
        viewModel.sendCustomerTrackingAlert(shipped.orderId)
        assertEquals(beforeAlert + 1, viewModel.notifications.value.size)

        viewModel.logFailedDeliveryAttempt(shipped.orderId, "Customer Phone Unreachable")
        val failedOrd = viewModel.orders.value.first { it.orderId == shipped.orderId }
        assertTrue(failedOrd.isDeliveryDelayed)
        assertTrue(failedOrd.currentTrackingLocation.contains("Failed Attempt"))
        assertTrue(failedOrd.statusText.contains("Failed Delivery Attempt"))

        viewModel.markCourierPdfSent(listOf(shipped.orderId), "Ekart Logistics")
        val sentOrd = viewModel.orders.value.first { it.orderId == shipped.orderId }
        assertTrue(sentOrd.orderHistoryLog.any { it.contains("Courier Dispatch PDF sent") })

        viewModel.toggleAutoTracking(false)
        assertFalse(viewModel.autoTrackingEnabled.value)
        viewModel.toggleAutoTracking(true)
        assertTrue(viewModel.autoTrackingEnabled.value)
    }

    @Test
    fun testAddCancelledRecordAndEditCancelledFullDetailsWithAutoRefundToggle() {
        val baseProduct = viewModel.allProducts.value.first()
        assertTrue("Auto refund tracking should start enabled", viewModel.autoRefundTrackingEnabled.value)

        viewModel.toggleAutoRefundTracking(false)
        assertFalse(viewModel.autoRefundTrackingEnabled.value)
        viewModel.toggleAutoRefundTracking(true)
        assertTrue(viewModel.autoRefundTrackingEnabled.value)

        viewModel.addCancelledRecordOrder(
            customerName = "Ritu Verma",
            phone = "+91 9876501234",
            address = "Station Road, Near Bus Stand",
            city = "Darbhanga",
            state = "Bihar",
            pinCode = "846004",
            customerAgeYears = "29 Years",
            product = baseProduct,
            size = "40 (L)",
            color = "Wine Velvet",
            quantity = 1,
            awbNumber = "DLV-99887766",
            courierPartner = "Delhivery Express",
            cancelReason = "ग्राहक ने खुद ऑर्डर कैंसिल किया",
            cancelledBy = "Customer",
            restoreStock = true,
            customImageUri = "content://media/external/images/media/801"
        )

        val added = viewModel.orders.value.first { it.customerName == "Ritu Verma" }
        assertEquals(OrderStatus.CANCELLED, added.status)
        assertEquals("29 Years", added.customerAgeYears)
        assertEquals("DLV-99887766", added.awbNumber)
        assertEquals("Delhivery Express", added.courierPartner)
        assertEquals("Darbhanga", added.city)
        assertEquals("Bihar", added.state)
        assertEquals("846004", added.pinCode)
        assertEquals("40 (L)", added.selectedSize)
        assertEquals("Wine Velvet", added.selectedColor)
        assertEquals("Customer", added.cancelledBy)
        assertEquals("Initiated", added.refundStatus)
        assertTrue(added.isStockRestored)
        assertEquals("content://media/external/images/media/801", added.customProductImageUri)

        viewModel.editCancelledFullDetails(
            orderId = added.orderId,
            awbNumber = "EKRT-55443322",
            courierPartner = "Ekart Logistics",
            customerName = "Ritu Kumari",
            phone = "+91 9876501299",
            address = "Tower Chowk, Ward 12",
            city = "Samastipur",
            state = "Bihar",
            pinCode = "848101",
            customerAgeYears = "31 Years",
            size = "42 (XL)",
            color = "Emerald Silk",
            cancelReason = "गलत Size या Colour चुन लिया",
            cancelledBy = "Seller",
            refundStatus = "Completed",
            refundAmount = 699,
            restoreStock = true,
            customImageUri = "content://media/external/images/media/802"
        )

        val edited = viewModel.orders.value.first { it.orderId == added.orderId }
        assertEquals("EKRT-55443322", edited.awbNumber)
        assertEquals("Ekart Logistics", edited.courierPartner)
        assertEquals("Ritu Kumari", edited.customerName)
        assertEquals("+91 9876501299", edited.customerPhone)
        assertEquals("Tower Chowk, Ward 12", edited.deliveryAddress)
        assertEquals("Samastipur", edited.city)
        assertEquals("848101", edited.pinCode)
        assertEquals("31 Years", edited.customerAgeYears)
        assertEquals("42 (XL)", edited.selectedSize)
        assertEquals("Emerald Silk", edited.selectedColor)
        assertEquals("Seller", edited.cancelledBy)
        assertEquals("Completed", edited.refundStatus)
        assertEquals(699, edited.refundAmount)
        assertEquals("content://media/external/images/media/802", edited.customProductImageUri)
    }

    @Test
    fun testCancelShippedOrderRoutesToRtoPolicyGuard() {
        val shippedOrder = viewModel.orders.value.first {
            it.status == OrderStatus.SHIPPED && !it.isRtoFlagged
        }
        viewModel.cancelSellerOrderDetailed(
            orderId = shippedOrder.orderId,
            reason = "Customer refused delivery at doorstep",
            cancelledBy = "Customer",
            restoreStock = true
        )
        val after = viewModel.orders.value.first { it.orderId == shippedOrder.orderId }
        assertEquals("Shipped order must stay SHIPPED and enter RTO rather than plain CANCELLED", OrderStatus.SHIPPED, after.status)
        assertTrue("RTO flag must be raised on shipped order cancel request", after.isRtoFlagged)
        assertTrue("RTO reason must include Seller/Customer cancel reason", after.rtoReason.contains("Customer refused delivery"))
    }

    @Test
    fun testFirestoreOrderDocMappingParity() {
        val cancelledSample = SampleData.initialOrders.first { it.status == OrderStatus.CANCELLED }
            .copy(customerAgeYears = "27 Years", customProductImageUri = "content://sample/dress/1")
        val ownerUid = "google_uid_test_123"
        val doc = FirestoreOrderDoc.fromOrderItem(cancelledSample, ownerUid)

        val createMap = doc.toCreateMap(ownerUid)
        assertFalse("Create map must not serialize DocumentId 'id'", createMap.containsKey("id"))
        assertEquals(ownerUid, createMap["userId"])
        assertEquals(cancelledSample.orderId, createMap["orderId"])
        assertEquals("27 Years", createMap["customerAgeYears"])
        assertEquals(cancelledSample.awbNumber, createMap["awbNumber"])
        assertTrue(createMap.containsKey("createdAt"))
        assertTrue(createMap.containsKey("updatedAt"))
        assertTrue(createMap.containsKey("stockRestored"))
        assertFalse("Must not use is-prefixed boolean field names", createMap.containsKey("isStockRestored"))
        assertFalse("Must not use is-prefixed boolean field names", createMap.containsKey("isPacked"))

        val updateMap = doc.toUpdateMap()
        assertFalse("Update map must not mutate userId", updateMap.containsKey("userId"))
        assertFalse("Update map must not mutate orderId", updateMap.containsKey("orderId"))
        assertFalse("Update map must not overwrite createdAt", updateMap.containsKey("createdAt"))
        assertTrue(updateMap.containsKey("updatedAt"))

        val roundTripped = doc.toOrderItem(viewModel.allProducts.value)
        assertEquals(cancelledSample.orderId, roundTripped.orderId)
        assertEquals(OrderStatus.CANCELLED, roundTripped.status)
        assertEquals("27 Years", roundTripped.customerAgeYears)
        assertEquals(cancelledSample.awbNumber, roundTripped.awbNumber)
        assertEquals(cancelledSample.city, roundTripped.city)
        assertEquals(cancelledSample.pinCode, roundTripped.pinCode)
        assertEquals("content://sample/dress/1", roundTripped.customProductImageUri)
    }

    @Test
    fun testReadyToShipAddEditBulkPickupAndHandoverGuard() {
        val baseProduct = viewModel.allProducts.value.first()

        // 1. Add Ready to Ship order — must stay ORDERED + sellerStage = "Ready to Ship" (NOT SHIPPED yet)
        viewModel.addReadyToShipProductOrder(
            customerName = "Roshni Gupta",
            phone = "+91 9811223344",
            address = "Saraiyaganj Tower Road",
            city = "Muzaffarpur",
            state = "Bihar",
            pinCode = "842001",
            product = baseProduct,
            size = "40 (L)",
            color = "Peacock Blue",
            quantity = 1,
            courierPartner = "Ekart Logistics",
            awbNumber = "EKRT-44556677",
            shippingCharges = 65,
            expectedDeliveryDate = "24 Nov 2026",
            customImageUri = "content://media/external/images/media/601"
        )

        val added = viewModel.orders.value.first { it.customerName == "Roshni Gupta" }
        assertEquals(OrderStatus.ORDERED, added.status)
        assertEquals("Ready to Ship", added.sellerStage)
        assertEquals("Ekart Logistics", added.courierPartner)
        assertEquals("EKRT-44556677", added.awbNumber)
        assertEquals(65, added.shippingCharges)
        assertEquals("24 Nov 2026", added.expectedDeliveryDate)
        assertTrue(added.isOrderConfirmed && added.isQualityVerified && added.isPacked && added.isInvoiceGenerated && added.isBarcodeScanned)

        // 2. Edit Courier, Pickup Status, Shipping Charges, Delivery Date & AWB
        viewModel.updateReadyToShipDetails(
            orderId = added.orderId,
            courierPartner = "Bluedart Air",
            awbNumber = "BLUD-88776655",
            pickupStatus = "Courier Arriving Today",
            shippingCharges = 89,
            expectedDeliveryDate = "23 Nov 2026",
            customerName = "Roshni Kumari",
            phone = "+91 9811223399",
            address = "Motijheel Main Road",
            city = "Muzaffarpur",
            state = "Bihar",
            pinCode = "842001",
            size = "42 (XL)",
            color = "Emerald Silk",
            customImageUri = "content://media/external/images/media/602"
        )
        val edited = viewModel.orders.value.first { it.orderId == added.orderId }
        assertEquals(OrderStatus.ORDERED, edited.status)
        assertEquals("Ready to Ship", edited.sellerStage)
        assertEquals("Bluedart Air", edited.courierPartner)
        assertEquals("BLUD-88776655", edited.awbNumber)
        assertEquals("Courier Arriving Today", edited.pickupStatus)
        assertEquals(89, edited.shippingCharges)
        assertEquals("23 Nov 2026", edited.expectedDeliveryDate)
        assertEquals("content://media/external/images/media/602", edited.customProductImageUri)

        // 3. Bulk Request Pickup keeps orders in Ready to Ship until courier accepts parcel
        val readyIds = viewModel.orders.value.filter {
            it.status == OrderStatus.ORDERED && it.sellerStage.equals("Ready to Ship", true)
        }.map { it.orderId }
        assertTrue(readyIds.size >= 2)
        viewModel.bulkRequestCourierPickup(readyIds, "Delhivery Express")
        val afterPickup = viewModel.orders.value.filter { it.orderId in readyIds }
        assertTrue("Bulk pickup must keep orders Ready to Ship until Mark as Shipped", afterPickup.all {
            it.status == OrderStatus.ORDERED && it.sellerStage == "Ready to Ship" && it.pickupStatus == "Pickup Requested"
        })

        // 4. Courier Acceptance -> Mark as Shipped updates status to SHIPPED
        viewModel.dispatchSellerOrder(added.orderId, "Delhivery Express")
        val shipped = viewModel.orders.value.first { it.orderId == added.orderId }
        assertEquals(OrderStatus.SHIPPED, shipped.status)
        assertEquals("Shipped", shipped.sellerStage)
    }

    @Test
    fun testShippedAutomaticTrackingAndCourierSync() {
        val shippedOrder = viewModel.orders.value.first {
            it.status == OrderStatus.SHIPPED && !it.isRtoFlagged && it.sellerStage.equals("Shipped", true)
        }

        // 1. Advance Shipped -> Out for Delivery
        viewModel.advanceShipmentTracking(shippedOrder.orderId)
        val outForDelivery = viewModel.orders.value.first { it.orderId == shippedOrder.orderId }
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, outForDelivery.status)
        assertEquals("Out for Delivery", outForDelivery.sellerStage)
        assertTrue(
            "Out for Delivery TrackingStage should be marked completed for Customer view",
            outForDelivery.trackingStages.any { it.title.contains("Out for Delivery", true) && it.isCompleted }
        )
        assertTrue(
            "Auto-tracking notification must be pushed",
            viewModel.notifications.value.any { it.title.contains("Out for Delivery", true) }
        )

        // 2. Sync live courier tracking while auto-tracking is enabled
        val countBeforeSync = viewModel.notifications.value.size
        viewModel.syncLiveCourierTracking()
        val synced = viewModel.orders.value.first { it.orderId != shippedOrder.orderId && it.status == OrderStatus.SHIPPED }
        assertTrue("Synced live location must include Synced timestamp", synced.currentTrackingLocation.contains("Synced"))
        assertTrue(
            "Live courier sync should push a notification when autoTrackingEnabled is true",
            viewModel.notifications.value.size > countBeforeSync
        )

        // 3. Disabling autoTracking suppresses auto-sync notifications
        viewModel.toggleAutoTracking(false)
        val countAfterDisable = viewModel.notifications.value.size
        viewModel.syncLiveCourierTracking()
        assertEquals(
            "No new auto-sync notification should fire when autoTrackingEnabled is false",
            countAfterDisable,
            viewModel.notifications.value.size
        )

        // 4. Advance Out for Delivery -> Delivered
        viewModel.advanceShipmentTracking(shippedOrder.orderId)
        val delivered = viewModel.orders.value.first { it.orderId == shippedOrder.orderId }
        assertEquals(OrderStatus.DELIVERED, delivered.status)
        assertEquals("Delivered", delivered.sellerStage)
        assertTrue("All tracking stages must be completed on Delivered", delivered.trackingStages.all { it.isCompleted })
    }

    @Test
    fun testOrderDashboardMetricsComputation() {
        val initialOrders = viewModel.orders.value
        val products = viewModel.allProducts.value
        val initialMetrics = com.example.ui.screens.computeOrderDashboardMetrics(initialOrders, products)

        assertEquals(initialOrders.size, initialMetrics.totalOrders)
        val expectedGross = initialOrders.filter { it.status != OrderStatus.CANCELLED }.sumOf { it.totalAmount }
        assertEquals(expectedGross, initialMetrics.grossRevenue)
        assertEquals(initialMetrics.grossRevenue, initialMetrics.prepaidAmount + initialMetrics.codAmount)
        assertTrue(initialMetrics.onHoldCount > 0)
        assertTrue(initialMetrics.pendingCount > 0)
        assertTrue(initialMetrics.shippedTransitCount > 0)
        assertTrue(initialMetrics.courierBreakdown.isNotEmpty())
        assertTrue(initialMetrics.topCities.isNotEmpty())
        assertTrue(initialMetrics.topProducts.isNotEmpty())

        // Add a new Pending order and verify metrics update dynamically
        val prod = products.first()
        viewModel.addPendingProductOrder(
            customerName = "Dashboard Buyer",
            phone = "+91 9811002200",
            address = "Main Road",
            city = "Patna",
            state = "Bihar",
            pinCode = "800001",
            product = prod,
            size = "L",
            color = "Pink",
            quantity = 2,
            paymentMethod = "UPI (GPay)",
            customImageUri = null
        )
        val afterAddMetrics = com.example.ui.screens.computeOrderDashboardMetrics(viewModel.orders.value, products)
        assertEquals(initialMetrics.totalOrders + 1, afterAddMetrics.totalOrders)
        assertEquals(initialMetrics.pendingCount + 1, afterAddMetrics.pendingCount)
        assertEquals(initialMetrics.grossRevenue + prod.price * 2, afterAddMetrics.grossRevenue)
        assertEquals(initialMetrics.prepaidCount + 1, afterAddMetrics.prepaidCount)

        // Dispatch the order -> moves from pendingCount to shippedTransitCount
        val created = viewModel.orders.value.first { it.customerName == "Dashboard Buyer" }
        viewModel.dispatchSellerOrder(created.orderId, "Ekart Logistics")
        val afterDispatchMetrics = com.example.ui.screens.computeOrderDashboardMetrics(viewModel.orders.value, products)
        assertEquals(afterAddMetrics.pendingCount - 1, afterDispatchMetrics.pendingCount)
        assertEquals(afterAddMetrics.shippedTransitCount + 1, afterDispatchMetrics.shippedTransitCount)

        // Add a Cancelled order -> increments cancelledCount & refundAmount without increasing grossRevenue
        viewModel.addCancelledRecordOrder(
            customerName = "Cancelled Dashboard User",
            phone = "+91 9811009999",
            address = "Ward 2",
            city = "Gaya",
            state = "Bihar",
            pinCode = "823001",
            customerAgeYears = "26 Years",
            product = prod,
            size = "M",
            color = "Blue",
            quantity = 1,
            awbNumber = "CNL-998877",
            courierPartner = "Delhivery Express",
            cancelReason = "Wrong Size",
            cancelledBy = "Customer",
            restoreStock = true,
            customImageUri = null
        )
        val afterCancelMetrics = com.example.ui.screens.computeOrderDashboardMetrics(viewModel.orders.value, products)
        assertEquals(afterDispatchMetrics.cancelledCount + 1, afterCancelMetrics.cancelledCount)
        assertEquals(afterDispatchMetrics.grossRevenue, afterCancelMetrics.grossRevenue)
        assertEquals(afterDispatchMetrics.refundAmount + prod.price, afterCancelMetrics.refundAmount)
    }

    @Test
    fun testCheckoutPurchasedOrderSyncsToSellerWorkflowAndTransitionsAllFiveStages() {
        viewModel.updateDeliveryAddress("Station Road, Patna, Bihar - 800001")
        // Empty existing cart first
        viewModel.cartItems.value.toList().forEach { item ->
            viewModel.removeFromCart(item.product.id, item.selectedSize)
        }
        val boughtProduct = viewModel.allProducts.value.first()
        viewModel.addToCart(boughtProduct, size = "XL", quantity = 2)

        val initialCount = viewModel.orders.value.size
        viewModel.checkoutAndPlaceOrder(paymentMethod = "Google Pay (UPI)")

        assertEquals(initialCount + 1, viewModel.orders.value.size)
        val created = viewModel.orders.value.first()
        assertEquals(boughtProduct.id, created.product.id)
        assertEquals("XL", created.selectedSize)
        assertEquals(2, created.quantity)
        assertEquals("Pending", created.sellerStage)
        assertEquals(OrderStatus.ORDERED, created.status)
        assertEquals("Patna", created.city)
        assertEquals("Bihar", created.state)
        assertEquals("800001", created.pinCode)
        assertTrue(
            "Purchased order history log must tag Customer Purchase",
            created.orderHistoryLog.any { it.contains("Customer Purchase") }
        )

        // Bridge to Seller Orders screen opens Orders bottom tab + Pending order tab
        viewModel.openSellerOrdersScreen(orderTab = 1)
        assertEquals(1, viewModel.sellerPreferredBottomTab.value)
        assertEquals(1, viewModel.sellerPreferredOrderTab.value)
        assertEquals(com.example.viewmodel.AppSubScreen.SELLER_PANEL, viewModel.subScreen.value)

        // 1. Move Pending -> On Hold
        viewModel.holdSellerOrder(created.orderId, reason = "Customer requested afternoon call", notes = "Call at 4 PM", adminApproved = true)
        assertEquals("On Hold", viewModel.orders.value.first { it.orderId == created.orderId }.sellerStage)

        // 2. Release On Hold -> Pending
        viewModel.releaseHoldOrder(created.orderId)
        assertEquals("Pending", viewModel.orders.value.first { it.orderId == created.orderId }.sellerStage)

        // 3. Move Pending -> Ready to Ship
        viewModel.markSellerOrderReadyToShip(created.orderId)
        assertEquals("Ready to Ship", viewModel.orders.value.first { it.orderId == created.orderId }.sellerStage)

        // 4. Dispatch Ready to Ship -> Shipped
        viewModel.dispatchSellerOrder(created.orderId, "Delhivery Express")
        val shipped = viewModel.orders.value.first { it.orderId == created.orderId }
        assertEquals(OrderStatus.SHIPPED, shipped.status)
        assertEquals("Shipped", shipped.sellerStage)

        // 5. Place a second customer purchase and Cancel -> Cancelled
        val secondProduct = viewModel.allProducts.value.last()
        viewModel.addToCart(secondProduct, size = "M", quantity = 1)
        viewModel.checkoutAndPlaceOrder(paymentMethod = "Cash on Delivery (COD)")
        val secondOrder = viewModel.orders.value.first()
        assertEquals("Pending", secondOrder.sellerStage)

        viewModel.cancelSellerOrderDetailed(secondOrder.orderId, reason = "Customer changed size", cancelledBy = "Customer", restoreStock = true)
        val cancelled = viewModel.orders.value.first { it.orderId == secondOrder.orderId }
        assertEquals(OrderStatus.CANCELLED, cancelled.status)
        assertEquals("Cancelled", cancelled.sellerStage)
    }

    @Test
    fun testTwentyTwoCategoriesSubcategoriesCrudSortAndMultiCategoryProductFiltering() = runTest {
        val cats = viewModel.categories.value
        val subs = viewModel.subCategories.value

        // Verify all 22 core + 10 expanded clothing Main Categories with zero duplicate names
        assertEquals(com.example.data.SampleData.categoryList.size, cats.size)
        assertTrue(cats.size >= 32)
        assertEquals(cats.size, cats.map { it.name.trim().lowercase() }.distinct().size)
        val expectedMain = listOf(
            "Men", "Women", "Girls", "Boys", "Kids & Baby",
            "Saree", "Kurti & Suit", "Western Wear", "Ethnic Wear",
            "Footwear", "Jewellery", "Fashion Accessories", "Beauty & Personal Care",
            "Home & Kitchen", "Toys & Games", "Bags & Luggage", "Electronics",
            "Sports & Fitness", "Grocery", "New Arrivals", "Best Sellers", "Offers & Discounts",
            "Girls Ethnic Wear", "Girls Western Wear", "Women's Ethnic Wear",
            "Wedding & Festive Wear", "Boys Clothing", "Kids Clothing",
            "Baby Clothing", "Plus Size Fashion", "Winter Wear", "Innerwear & Lingerie"
        )
        expectedMain.forEach { expected ->
            assertTrue("Missing main category: $expected", cats.any { it.name.equals(expected, ignoreCase = true) })
        }

        // Verify key subcategories under Women (>=13), Men (>=10), Kids & Baby (7), and Footwear (Men Shoes & Women Shoes)
        val womenSubs = subs.filter { it.parentCategory == "Women" }
        assertTrue(womenSubs.size >= 13)
        val menSubs = subs.filter { it.parentCategory == "Men" }
        assertTrue(menSubs.size >= 10)
        val kidsSubs = subs.filter { it.parentCategory == "Kids & Baby" }
        assertEquals(7, kidsSubs.size)
        val footwearSubs = subs.filter { it.parentCategory == "Footwear" }
        assertTrue(footwearSubs.any { it.name == "Men Shoes" })
        assertTrue(footwearSubs.any { it.name == "Women Shoes" })

        // Add custom main category & verify SEO auto-slug
        val createdCat = viewModel.addMainCategory(
            customId = "CAT-FEST-88",
            name = "Festive Bridal Edit",
            offerBadge = "Flat 40% Off",
            seoName = "",
            isActive = true,
            sortOrder = cats.size + 1
        )
        assertNotNull(createdCat)
        assertEquals("CAT-FEST-88", createdCat!!.id)
        assertEquals("festive-bridal-edit", createdCat.seoName)

        // Add child subcategory under it
        val createdSub = viewModel.addSubCategory(
            customId = "SUB-FEST-1",
            parentCategoryId = "CAT-FEST-88",
            parentCategoryName = "Festive Bridal Edit",
            name = "Zardozi Dupatta"
        )
        assertNotNull(createdSub)

        // Edit parent category name -> cascades to child subcategory parentCategory
        viewModel.editMainCategory(
            originalId = "CAT-FEST-88",
            newName = "Royal Bridal Edit",
            offerBadge = "VIP",
            seoName = "royal-bridal-edit",
            isActive = true,
            sortOrder = cats.size + 1,
            imageRes = createdCat.imageRes,
            customImageUri = null
        )
        val updatedParent = viewModel.categories.value.first { it.id == "CAT-FEST-88" }
        val updatedChild = viewModel.subCategories.value.first { it.id == "SUB-FEST-1" }
        assertEquals("Royal Bridal Edit", updatedParent.name)
        assertEquals("Royal Bridal Edit", updatedChild.parentCategory)

        // Toggle active/inactive & move sort order
        viewModel.toggleCategoryActive("CAT-FEST-88", false)
        assertFalse(viewModel.categories.value.first { it.id == "CAT-FEST-88" }.isActive)

        val lastId = viewModel.categories.value.sortedBy { it.sortOrder }.last().id
        val initialOrder = viewModel.categories.value.first { it.id == lastId }.sortOrder
        viewModel.moveCategorySortOrder(lastId, -1)
        assertTrue(viewModel.categories.value.first { it.id == lastId }.sortOrder < initialOrder)

        // Multi-category & custom category product assignment updates filteredProducts
        val targetProd = viewModel.allProducts.value.first()
        viewModel.updateProductCategories(
            productId = targetProd.id,
            primaryCategory = "Women",
            primarySubcategory = "Saree",
            additionalCategories = listOf("New Arrivals", "My Custom VIP Drop")
        )
        viewModel.selectCategoryFilter("My Custom VIP Drop")
        testScheduler.runCurrent()
        assertTrue(viewModel.filteredProducts.value.any { it.id == targetProd.id })

        // Delete parent removes both category & child subcategory
        viewModel.deleteCategory("CAT-FEST-88")
        assertFalse(viewModel.categories.value.any { it.id == "CAT-FEST-88" })
        assertFalse(viewModel.subCategories.value.any { it.id == "SUB-FEST-1" })
    }

    @Test
    fun testDashboardOrderStatusFiltersForAllFiveStatuses() {
        val expectedLabels = listOf("Pending", "Ready to Ship", "Shipped", "Cancelled", "On Hold", "All")
        assertEquals(expectedLabels, DashboardOrderStatusFilter.entries.map { it.label })

        val allOrders = viewModel.orders.value
        assertEquals(allOrders.size, filterOrdersByDashboardStatus(allOrders, DashboardOrderStatusFilter.ALL).size)

        val pending = filterOrdersByDashboardStatus(allOrders, DashboardOrderStatusFilter.PENDING)
        val ready = filterOrdersByDashboardStatus(allOrders, DashboardOrderStatusFilter.READY_TO_SHIP)
        val shipped = filterOrdersByDashboardStatus(allOrders, DashboardOrderStatusFilter.SHIPPED)
        val cancelled = filterOrdersByDashboardStatus(allOrders, DashboardOrderStatusFilter.CANCELLED)
        val onHold = filterOrdersByDashboardStatus(allOrders, DashboardOrderStatusFilter.ON_HOLD)

        assertTrue("Pending orders must exist", pending.isNotEmpty())
        assertTrue(pending.all { it.status == OrderStatus.ORDERED && !it.sellerStage.equals("Ready to Ship", true) && !it.sellerStage.equals("On Hold", true) })

        assertTrue("Ready to Ship orders must exist", ready.isNotEmpty())
        assertTrue(ready.all { it.status == OrderStatus.ORDERED && it.sellerStage.equals("Ready to Ship", true) })

        assertTrue("Shipped orders must exist", shipped.isNotEmpty())
        assertTrue(shipped.all { it.status == OrderStatus.SHIPPED || it.status == OrderStatus.DELIVERED || it.isRtoFlagged })

        assertTrue("Cancelled orders must exist", cancelled.isNotEmpty())
        assertTrue(cancelled.all { it.status == OrderStatus.CANCELLED && !it.isRtoFlagged })

        assertTrue("On Hold orders must exist", onHold.isNotEmpty())
        assertTrue(onHold.all { it.status == OrderStatus.ORDERED && it.sellerStage.equals("On Hold", true) })

        // Verify transitions move an order cleanly across all 5 dashboard filter buckets
        val product = viewModel.allProducts.value.first()
        viewModel.createSellerManualOrder(
            customerName = "Filter Flow Buyer",
            address = "Patna, Bihar - 800001",
            product = product,
            size = "L",
            quantity = 1,
            paymentMethod = "UPI (PhonePe)"
        )
        val targetId = viewModel.orders.value.first().orderId
        assertTrue(filterOrdersByDashboardStatus(viewModel.orders.value, DashboardOrderStatusFilter.PENDING).any { it.orderId == targetId })

        viewModel.holdSellerOrder(targetId, reason = "Confirm pincode", notes = "Check", adminApproved = true)
        assertTrue(filterOrdersByDashboardStatus(viewModel.orders.value, DashboardOrderStatusFilter.ON_HOLD).any { it.orderId == targetId })
        assertFalse(filterOrdersByDashboardStatus(viewModel.orders.value, DashboardOrderStatusFilter.PENDING).any { it.orderId == targetId })

        viewModel.releaseHoldOrder(targetId)
        assertTrue(filterOrdersByDashboardStatus(viewModel.orders.value, DashboardOrderStatusFilter.PENDING).any { it.orderId == targetId })

        viewModel.markSellerOrderReadyToShip(targetId)
        assertTrue(filterOrdersByDashboardStatus(viewModel.orders.value, DashboardOrderStatusFilter.READY_TO_SHIP).any { it.orderId == targetId })

        viewModel.dispatchSellerOrder(targetId, "Delhivery Express")
        assertTrue(filterOrdersByDashboardStatus(viewModel.orders.value, DashboardOrderStatusFilter.SHIPPED).any { it.orderId == targetId })

        viewModel.createSellerManualOrder(
            customerName = "Cancel Bucket Buyer",
            address = "Bettiah - 845438",
            product = product,
            size = "M",
            quantity = 1,
            paymentMethod = "COD"
        )
        val cancelId = viewModel.orders.value.first().orderId
        viewModel.cancelSellerOrderDetailed(cancelId, reason = "Customer changed size", cancelledBy = "Customer", restoreStock = true)
        assertTrue(filterOrdersByDashboardStatus(viewModel.orders.value, DashboardOrderStatusFilter.CANCELLED).any { it.orderId == cancelId })
    }

    @Test
    fun testQrAndAwbBarcodeScannerPayloadMatcher() {
        val allOrders = viewModel.orders.value
        val sampleOrder = allOrders.first { it.awbNumber.isNotBlank() }

        // 1. Pipe-delimited Gyanvi QR payload
        val qrPipe = "GYANVI|ORD=${sampleOrder.orderId}|AWB=${sampleOrder.awbNumber}|CUR=${sampleOrder.courierPartner}"
        val parsedPipe = QrAwbPayloadMatcher.parseScannedPayload(qrPipe, Barcode.FORMAT_QR_CODE)
        assertEquals("QR Code", parsedPipe.codeKind)
        assertEquals(sampleOrder.orderId, parsedPipe.extractedOrderId)
        assertEquals(sampleOrder.awbNumber, parsedPipe.extractedAwb)
        assertEquals(sampleOrder.orderId, QrAwbPayloadMatcher.findMatchingOrder(allOrders, qrPipe)?.orderId)

        // 2. JSON QR payload
        val qrJson = """{"orderId":"${sampleOrder.orderId}","awbNumber":"${sampleOrder.awbNumber}"}"""
        val parsedJson = QrAwbPayloadMatcher.parseScannedPayload(qrJson, Barcode.FORMAT_QR_CODE)
        assertEquals(sampleOrder.orderId, parsedJson.extractedOrderId)
        assertEquals(sampleOrder.awbNumber, parsedJson.extractedAwb)
        assertEquals(sampleOrder.orderId, QrAwbPayloadMatcher.findMatchingOrder(allOrders, qrJson)?.orderId)

        // 3. Plain 1D Code-128 AWB Barcode string
        val parsedAwb = QrAwbPayloadMatcher.parseScannedPayload(sampleOrder.awbNumber, Barcode.FORMAT_CODE_128)
        assertEquals("AWB Barcode", parsedAwb.codeKind)
        assertEquals(sampleOrder.orderId, QrAwbPayloadMatcher.findMatchingOrder(allOrders, sampleOrder.awbNumber)?.orderId)

        // 4. Unknown AWB does not falsely match
        assertNull(QrAwbPayloadMatcher.findMatchingOrder(allOrders, "UNKNOWN-ZZZ-000000"))
    }

    @Test
    fun testClothingAndFootwearCatalogTaxonomySearchAndSellerUploadSync() = runTest {
        // 1. All 15 taxonomy categories present including Footwear (जूते-चप्पल)
        val taxonomyCats = com.example.data.ClothingCatalogTaxonomy.clothingCategories
        assertEquals(15, taxonomyCats.size)
        val footwearSpec = taxonomyCats.first { it.linkedMainCategoryName == "Footwear" }
        assertTrue(footwearSpec.subcategories.any { it.name == "Men Shoes" })
        assertTrue(footwearSpec.subcategories.any { it.name == "Women Shoes" })

        // 2. Searchable taxonomy matches product names, subcategories & Hindi/Footwear terms
        val banarasiMatches = com.example.data.ClothingCatalogTaxonomy.searchEntries("Banarasi Saree")
        assertTrue(banarasiMatches.any { it.linkedMainCategoryName == "Women" && it.subcategoryName == "Sarees" })

        val menShoesMatches = com.example.data.ClothingCatalogTaxonomy.searchEntries("Men Shoes")
        assertTrue(menShoesMatches.any { it.linkedMainCategoryName == "Footwear" && it.subcategoryName == "Men Shoes" })

        val womenShoesMatches = com.example.data.ClothingCatalogTaxonomy.searchEntries("Women Shoes")
        assertTrue(womenShoesMatches.any { it.linkedMainCategoryName == "Footwear" && it.subcategoryName == "Women Shoes" })

        // 3. Seller Catalog Upload creates product synced into ViewModel & Buyer Category Filtering
        val initialCount = viewModel.allProducts.value.size
        viewModel.addCatalogProduct(
            name = "Royal Zari Bridal Punjabi Jutti Women Shoes",
            category = "Footwear",
            price = 549,
            originalPrice = 1599,
            fabric = "Silk Velvet & Cushioned Sole",
            description = "Festive embroidered women shoes & jutti.",
            imageRes = com.example.R.drawable.dress_lehenga_1793417067159,
            subcategory = "Women Shoes",
            brandName = "GYANVI FOOTWEAR",
            color = "Rani Pink",
            weight = "450g",
            sizes = listOf("UK 4", "UK 5", "UK 6", "UK 7"),
            numericSizes = listOf("36", "38", "40"),
            stock = 60,
            sku = "GYN-FTW-WSH-99",
            extraImageUrls = listOf("content://mock/shoe1.jpg", "content://mock/shoe2.jpg")
        )
        assertEquals(initialCount + 1, viewModel.allProducts.value.size)

        viewModel.selectCategoryFilter("Footwear")
        testScheduler.runCurrent()
        assertTrue(viewModel.filteredProducts.value.any { it.sku == "GYN-FTW-WSH-99" && it.color == "Rani Pink" })

        viewModel.selectCategoryFilter("Women Shoes")
        testScheduler.runCurrent()
        assertTrue(viewModel.filteredProducts.value.any { it.sku == "GYN-FTW-WSH-99" })
    }
}

