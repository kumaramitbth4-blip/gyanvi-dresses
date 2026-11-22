package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SampleData
import com.example.model.OrderStatus
import com.example.util.HoldOrderPdfGenerator
import com.example.util.QrCodeGenerator
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HoldOrderPdfTest {

    @Test
    fun testBuildCombinedMultiProductHoldPdfAndSaveToDownloads() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val holdOrders = SampleData.initialOrders.filter {
            it.status == OrderStatus.ORDERED && it.sellerStage.equals("On Hold", true)
        }
        assertTrue("SampleData must have On Hold orders across multiple product types", holdOrders.size >= 3)
        assertTrue("On Hold orders must span multiple categories", holdOrders.map { it.product.category }.distinct().size >= 2)

        val pdfBytes = HoldOrderPdfGenerator.buildHoldOrdersPdfBytes(context, holdOrders)
        assertTrue("Generated PDF bytes must be non-empty", pdfBytes.size > 500)
        val header = String(pdfBytes, 0, minOf(5, pdfBytes.size), Charsets.US_ASCII)
        assertEquals("%PDF-", header)

        val saveResult = HoldOrderPdfGenerator.saveHoldOrdersPdfToDownloads(
            context = context,
            orders = holdOrders,
            fileNamePrefix = "Combined_Hold_Test",
            showToast = false
        )
        assertTrue("PDF Download should succeed: ${saveResult.message}", saveResult.success)
        assertTrue("Filename must end with .pdf", saveResult.fileName.endsWith(".pdf"))
        assertTrue("Saved PDF byte size should match valid PDF", saveResult.byteSize > 500)
    }

    @Test
    fun testGenerateCode128HoldBarcodeBitmap() {
        val code = "GYN-019405-845438"
        val bars = QrCodeGenerator.encodeCode128Bars(code)
        assertTrue("Code-128 bars should contain sufficient modules", bars.size > 80)

        val bmp = QrCodeGenerator.generateBarcodeBitmap(code = code, widthPx = 420, heightPx = 88)
        assertEquals(420, bmp.width)
        assertEquals(88, bmp.height)

        var hasBlack = false
        var hasWhite = false
        for (x in 0 until bmp.width) {
            val c = bmp.getPixel(x, 30)
            if (c == android.graphics.Color.BLACK) hasBlack = true
            if (c == android.graphics.Color.WHITE) hasWhite = true
        }
        assertTrue("Generated Barcode bitmap must render high-contrast black & white stripes", hasBlack && hasWhite)
    }

    @Test
    fun testCancelStagedPdfPreviewCleansPreviewFiles() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedDir = File(context.cacheDir, "shared_pdf").apply { mkdirs() }
        val previewFile = File(sharedDir, "preview_temp_hold.pdf").apply { writeText("%PDF-1.4") }
        val otherFile = File(sharedDir, "kept_hold.pdf").apply { writeText("%PDF-1.4") }

        HoldOrderPdfGenerator.cancelStagedPdfPreview(context)

        assertFalse("Staged preview PDF must be removed on PDF Cancel", previewFile.exists())
        assertTrue("Non-preview PDF should remain untouched", otherFile.exists())
    }

    @Test
    fun testBuildPendingOrdersPdfWithBarcodeAndSaveToDownloads() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pendingOrders = SampleData.initialOrders.filter {
            it.status == OrderStatus.ORDERED &&
                !it.sellerStage.equals("On Hold", true) &&
                !it.sellerStage.equals("Ready to Ship", true)
        }
        assertTrue("SampleData must have Pending orders", pendingOrders.isNotEmpty())

        val pdfBytes = HoldOrderPdfGenerator.buildHoldOrdersPdfBytes(
            context = context,
            orders = pendingOrders,
            reportTitle = "GYANVI DRESSES • PENDING ORDERS PDF (${pendingOrders.size} ITEMS)"
        )
        assertTrue("Generated Pending PDF bytes must be non-empty", pdfBytes.size > 500)
        val header = String(pdfBytes, 0, minOf(5, pdfBytes.size), Charsets.US_ASCII)
        assertEquals("%PDF-", header)
        val pdfText = String(pdfBytes, Charsets.US_ASCII)
        assertTrue("Pending PDF must embed GYN-PND barcode code", pdfText.contains("GYN-PND-"))

        val firstPending = pendingOrders.first()
        val pendingBarcode = "GYN-PND-${firstPending.orderId.takeLast(6)}-${firstPending.pinCode.ifBlank { "845438" }}"
        val bmp = QrCodeGenerator.generateBarcodeBitmap(code = pendingBarcode, widthPx = 440, heightPx = 80)
        assertEquals(440, bmp.width)
        assertEquals(80, bmp.height)

        val saveResult = HoldOrderPdfGenerator.saveHoldOrdersPdfToDownloads(
            context = context,
            orders = pendingOrders,
            fileNamePrefix = "PendingOrder_${firstPending.orderId}",
            reportTitle = "GYANVI DRESSES • PENDING ORDERS PDF",
            showToast = false
        )
        assertTrue("Pending PDF Download should succeed: ${saveResult.message}", saveResult.success)
        assertTrue("Pending filename must end with .pdf", saveResult.fileName.endsWith(".pdf"))
        assertTrue("Saved Pending PDF byte size should be > 500", saveResult.byteSize > 500)
    }

    @Test
    fun testRenderPendingAndHoldPdfPagesToBitmaps() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pendingOrders = SampleData.initialOrders.filter {
            it.status == OrderStatus.ORDERED &&
                !it.sellerStage.equals("On Hold", true) &&
                !it.sellerStage.equals("Ready to Ship", true)
        }
        val renderedPages = HoldOrderPdfGenerator.renderPdfPagesToBitmaps(
            context = context,
            orders = pendingOrders,
            reportTitle = "GYANVI DRESSES • PENDING ORDERS PDF"
        )
        assertTrue("At least one rendered PDF page bitmap must be produced", renderedPages.isNotEmpty())
        val firstPage = renderedPages.first()
        assertTrue("Rendered A4 PDF page must have portrait aspect ratio", firstPage.height > firstPage.width)
        assertTrue("Rendered A4 PDF page must have valid width >= 595", firstPage.width >= 595)
    }

    @Test
    fun testBuildAllOrdersCombinedMultiPagePdf() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val allOrders = SampleData.initialOrders
        assertTrue("SampleData must have >= 8 orders across all tabs", allOrders.size >= 8)

        val pdfBytes = HoldOrderPdfGenerator.buildHoldOrdersPdfBytes(
            context = context,
            orders = allOrders,
            reportTitle = "GYANVI DRESSES • ALL ORDERS PDF (${allOrders.size} ITEMS)"
        )
        val pdfText = String(pdfBytes, Charsets.US_ASCII)
        assertTrue("All orders PDF must contain valid PDF header", pdfText.startsWith("%PDF-"))
        assertTrue("All orders multi-page PDF must include Page 2 of", pdfText.contains("Page 2 of"))
        allOrders.forEach { ord ->
            assertTrue("All orders PDF must include Order ${ord.orderId}", pdfText.contains(ord.orderId))
        }
    }

    @Test
    fun testBuildShippedCourierPdfWithAwbBarcodeAndCourierDetails() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shippedOrders = SampleData.initialOrders.filter {
            it.status == OrderStatus.SHIPPED || it.status == OrderStatus.DELIVERED || it.isRtoFlagged
        }
        assertTrue("SampleData must have Shipped orders", shippedOrders.isNotEmpty())

        val pdfBytes = HoldOrderPdfGenerator.buildHoldOrdersPdfBytes(
            context = context,
            orders = shippedOrders,
            reportTitle = "GYANVI DRESSES • COURIER DISPATCH PDF (${shippedOrders.size} ITEMS)"
        )
        val pdfText = String(pdfBytes, Charsets.US_ASCII)
        assertTrue("Courier Dispatch PDF must start with %PDF-", pdfText.startsWith("%PDF-"))

        val firstShipped = shippedOrders.first()
        assertTrue("Courier Dispatch PDF must include Courier Partner", pdfText.contains(firstShipped.courierPartner))
        assertTrue("Courier Dispatch PDF must include AWB Number", pdfText.contains(firstShipped.awbNumber))
        assertTrue("Courier Dispatch PDF must include City", pdfText.contains(firstShipped.city))
        assertTrue("Courier Dispatch PDF must include Pin Code", pdfText.contains(firstShipped.pinCode))

        val saveResult = HoldOrderPdfGenerator.saveHoldOrdersPdfToDownloads(
            context = context,
            orders = shippedOrders,
            fileNamePrefix = "CourierPDF_${firstShipped.awbNumber}",
            reportTitle = "GYANVI DRESSES • COURIER DISPATCH PDF",
            showToast = false
        )
        assertTrue("Courier PDF download must succeed: ${saveResult.message}", saveResult.success)
        assertTrue("Courier PDF filename must end with .pdf", saveResult.fileName.endsWith(".pdf"))
        assertTrue("Courier PDF saved size must be > 500", saveResult.byteSize > 500)
    }

    @Test
    fun testBuildCancelledOrdersPdfWithAgeAwbBarcodeAndSaveToDownloads() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cancelledOrders = SampleData.initialOrders.filter {
            it.status == OrderStatus.CANCELLED
        }
        assertTrue("SampleData must have Cancelled orders", cancelledOrders.isNotEmpty())

        val pdfBytes = HoldOrderPdfGenerator.buildHoldOrdersPdfBytes(
            context = context,
            orders = cancelledOrders,
            reportTitle = "GYANVI DRESSES • CANCELLED ORDERS PDF (${cancelledOrders.size} ITEMS)"
        )
        val pdfText = String(pdfBytes, Charsets.US_ASCII)
        assertTrue("Cancelled PDF must start with %PDF-", pdfText.startsWith("%PDF-"))

        val firstCancelled = cancelledOrders.first()
        assertTrue("Cancelled PDF must include Age/Years", pdfText.contains(firstCancelled.customerAgeYears))
        assertTrue("Cancelled PDF must include AWB Number", pdfText.contains(firstCancelled.awbNumber))
        assertTrue("Cancelled PDF must include Cancelled By", pdfText.contains(firstCancelled.cancelledBy))
        assertTrue("Cancelled PDF must include City", pdfText.contains(firstCancelled.city))
        assertTrue("Cancelled PDF must include Pin Code", pdfText.contains(firstCancelled.pinCode))

        val saveResult = HoldOrderPdfGenerator.saveHoldOrdersPdfToDownloads(
            context = context,
            orders = cancelledOrders,
            fileNamePrefix = "CancelledOrder_${firstCancelled.orderId}",
            reportTitle = "GYANVI DRESSES • CANCELLED ORDERS PDF",
            showToast = false
        )
        assertTrue("Cancelled PDF download must succeed: ${saveResult.message}", saveResult.success)
        assertTrue("Cancelled PDF filename must end with .pdf", saveResult.fileName.endsWith(".pdf"))
        assertTrue("Cancelled PDF saved size must be > 500", saveResult.byteSize > 500)
    }

    @Test
    fun testBuildReadyToShipPdfWithCourierPickupChargesDeliveryDateAndBarcode() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val readyOrders = SampleData.initialOrders.filter {
            it.status == OrderStatus.ORDERED && it.sellerStage.equals("Ready to Ship", true)
        }
        assertTrue("SampleData must have >= 2 Ready to Ship orders", readyOrders.size >= 2)

        val pdfBytes = HoldOrderPdfGenerator.buildHoldOrdersPdfBytes(
            context = context,
            orders = readyOrders,
            reportTitle = "GYANVI DRESSES • READY TO SHIP PDF (${readyOrders.size} ITEMS)"
        )
        val pdfText = String(pdfBytes, Charsets.US_ASCII)
        assertTrue("Ready to Ship PDF must start with %PDF-", pdfText.startsWith("%PDF-"))
        assertTrue("Ready to Ship PDF must include READY TO SHIP details", pdfText.contains("READY TO SHIP"))

        val firstReady = readyOrders.first()
        assertTrue("Ready PDF must include Courier Partner", pdfText.contains(firstReady.courierPartner))
        assertTrue("Ready PDF must include AWB Number", pdfText.contains(firstReady.awbNumber))
        assertTrue("Ready PDF must include Pickup Status", pdfText.contains(firstReady.pickupStatus))
        assertTrue("Ready PDF must include Shipping Charges", pdfText.contains("Rs. ${firstReady.shippingCharges}"))
        assertTrue("Ready PDF must include Expected Delivery Date", pdfText.contains(firstReady.expectedDeliveryDate))

        val renderedPages = HoldOrderPdfGenerator.renderPdfPagesToBitmaps(
            context = context,
            orders = readyOrders,
            reportTitle = "GYANVI DRESSES • READY TO SHIP PDF (${readyOrders.size} ITEMS)"
        )
        assertTrue("Ready to Ship PDF Show must render A4 page bitmaps", renderedPages.isNotEmpty())

        val saveResult = HoldOrderPdfGenerator.saveHoldOrdersPdfToDownloads(
            context = context,
            orders = readyOrders,
            fileNamePrefix = "ReadyToShip_${firstReady.orderId}",
            reportTitle = "GYANVI DRESSES • READY TO SHIP PDF",
            showToast = false
        )
        assertTrue("Ready to Ship PDF download must succeed: ${saveResult.message}", saveResult.success)
        assertTrue("Ready to Ship PDF filename must end with .pdf", saveResult.fileName.endsWith(".pdf"))
        assertTrue("Ready to Ship PDF saved size must be > 500", saveResult.byteSize > 500)
    }

    @Test
    fun testShippedPdfShowIncludesJourneyStatusLocationDelayRtoAndHistory() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shippedOrders = SampleData.initialOrders.filter {
            it.status == OrderStatus.SHIPPED
        }
        assertTrue("SampleData must have >= 2 Shipped orders", shippedOrders.size >= 2)

        val rtoSample = shippedOrders.first().copy(
            orderId = "OD-RTO-TEST-1",
            isRtoFlagged = true,
            rtoReason = "Customer Unavailable 3 Attempts"
        )
        val allShippedAndRto = shippedOrders + rtoSample

        val pdfBytes = HoldOrderPdfGenerator.buildHoldOrdersPdfBytes(
            context = context,
            orders = allShippedAndRto,
            reportTitle = "GYANVI DRESSES • COURIER DISPATCH PDF (${allShippedAndRto.size} ITEMS)"
        )
        val pdfText = String(pdfBytes, Charsets.US_ASCII)
        assertTrue("Shipped PDF must contain SHIPPED section", pdfText.contains("SHIPPED"))
        assertTrue("Shipped PDF must contain Expected Delivery", pdfText.contains("Expected Delivery:"))
        assertTrue("Shipped PDF must contain Live Location", pdfText.contains("Live Location:"))
        assertTrue("Shipped PDF must contain Delay Alert for delayed order", pdfText.contains("DELAY/FAILED ALERT"))
        assertTrue("Shipped PDF must contain RTO Tracking Active for RTO order", pdfText.contains("RTO TRACKING ACTIVE"))
        assertTrue("Shipped PDF must contain History line", pdfText.contains("History:"))

        val renderedPages = HoldOrderPdfGenerator.renderPdfPagesToBitmaps(
            context = context,
            orders = allShippedAndRto,
            reportTitle = "GYANVI DRESSES • COURIER DISPATCH PDF (${allShippedAndRto.size} ITEMS)"
        )
        assertTrue("Shipped PDF Show must render A4 page bitmaps", renderedPages.isNotEmpty())
        assertTrue("Shipped PDF bitmap must be portrait A4", renderedPages.first().height > renderedPages.first().width)
    }

    @Test
    fun testCustomerPurchasedOrderLocalPersistenceAndAllFiveStagePdfs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = com.example.data.local.OrderLocalStorage(context)
        val boughtProduct = SampleData.sampleProducts.first()
        val purchasedOrder = SampleData.initialOrders.first().copy(
            orderId = "OD-CUST-99887766",
            product = boughtProduct,
            status = OrderStatus.ORDERED,
            sellerStage = "Pending",
            customerName = "Riya Sharma",
            customerPhone = "+91 9876501234",
            deliveryAddress = "Main Market, Muzaffarpur, Bihar - 842001",
            city = "Muzaffarpur",
            state = "Bihar",
            pinCode = "842001",
            selectedSize = "XL",
            selectedColor = "Royal Wine",
            quantity = 2,
            totalAmount = boughtProduct.price * 2,
            customProductImageUri = "content://media/external/images/media/42",
            orderHistoryLog = listOf("18 Nov 10:30 AM: 🛒 Customer Purchase Order placed by Riya Sharma")
        )

        storage.saveOrders(listOf(purchasedOrder) + SampleData.initialOrders)
        val reloaded = storage.loadOrders(SampleData.initialOrders, SampleData.sampleProducts)
        val restored = reloaded.find { it.orderId == "OD-CUST-99887766" }
        assertNotNull("Purchased order must persist across OrderLocalStorage reload", restored)
        assertEquals("Muzaffarpur", restored?.city)
        assertEquals("Bihar", restored?.state)
        assertEquals("842001", restored?.pinCode)
        assertEquals("XL", restored?.selectedSize)
        assertEquals("Royal Wine", restored?.selectedColor)
        assertEquals("content://media/external/images/media/42", restored?.customProductImageUri)
        assertTrue(restored?.orderHistoryLog?.any { it.contains("Customer Purchase") } == true)

        // Verify PDF generation + rendered A4 bitmaps across all 5 seller workflow stages
        val stageVariants = listOf(
            "On Hold" to restored!!.copy(status = OrderStatus.ORDERED, sellerStage = "On Hold", holdReason = "Confirm Size"),
            "Pending" to restored.copy(status = OrderStatus.ORDERED, sellerStage = "Pending"),
            "Ready to Ship" to restored.copy(status = OrderStatus.ORDERED, sellerStage = "Ready to Ship", courierPartner = "Delhivery Express"),
            "Shipped" to restored.copy(status = OrderStatus.SHIPPED, sellerStage = "Shipped", awbNumber = "DLV-77665544"),
            "Cancelled" to restored.copy(status = OrderStatus.CANCELLED, sellerStage = "Cancelled", cancelReason = "Wrong Size", cancelledBy = "Customer")
        )

        stageVariants.forEach { (stageName, stageOrder) ->
            val title = "GYANVI DRESSES • ${stageName.uppercase()} PDF (1 ITEMS)"
            val pdfBytes = HoldOrderPdfGenerator.buildHoldOrdersPdfBytes(context, listOf(stageOrder), reportTitle = title)
            val pdfText = String(pdfBytes, Charsets.US_ASCII)
            assertTrue("$stageName PDF must start with %PDF-", pdfText.startsWith("%PDF-"))
            assertTrue("$stageName PDF must contain purchased Order ID", pdfText.contains("OD-CUST-99887766"))

            val bitmaps = HoldOrderPdfGenerator.renderPdfPagesToBitmaps(context, listOf(stageOrder), reportTitle = title)
            assertTrue("$stageName inline PDF viewer must render at least 1 A4 bitmap page", bitmaps.isNotEmpty())
            assertTrue("$stageName rendered PDF page must be portrait A4", bitmaps.first().height > bitmaps.first().width)
        }
    }
}

