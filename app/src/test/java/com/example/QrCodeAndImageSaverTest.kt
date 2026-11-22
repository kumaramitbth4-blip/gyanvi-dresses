package com.example

import android.content.Context
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.util.ImageSaver
import com.example.util.QrCodeGenerator
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QrCodeAndImageSaverTest {

    @Test
    fun testQrMatrixFinderPatternsAndSizes() {
        val shortMatrix = QrCodeGenerator.encodeQrMatrix("ORD-12345")
        assertTrue("Short payload matrix must be valid square >= 21x21", shortMatrix.size >= 21)
        assertEquals(shortMatrix.size, shortMatrix[0].size)

        // Check top-left 7x7 finder pattern border is dark
        for (i in 0 until 7) {
            assertTrue(shortMatrix[0][i])
            assertTrue(shortMatrix[6][i])
            assertTrue(shortMatrix[i][0])
            assertTrue(shortMatrix[i][6])
        }
        // Center of top-left finder pattern (3,3) must be dark
        assertTrue(shortMatrix[3][3])

        val upiMatrix = QrCodeGenerator.encodeQrMatrix(
            "upi://pay?pa=gyanvidresses@okicici&pn=Gyanvi%20Dresses&am=599&cu=INR"
        )
        assertTrue("UPI QR payload must fit in larger version matrix", upiMatrix.size >= 29)
    }

    @Test
    fun testGenerateQrBitmapAndShippingLabelBitmap() {
        val qrBmp = QrCodeGenerator.generateQrBitmap(
            content = "GYANVI|ORD=OD8492019401|AWB=DLV-928371904",
            sizePx = 400,
            headerTitle = "ORDER #OD8492019401",
            footerSubtitle = "AWB: DLV-928371904"
        )
        assertEquals(400, qrBmp.width)
        assertTrue(qrBmp.height > 400)

        var hasBlack = false
        var hasWhite = false
        for (y in 0 until qrBmp.height step 4) {
            for (x in 0 until qrBmp.width step 4) {
                val p = qrBmp.getPixel(x, y)
                if (p == Color.BLACK) hasBlack = true
                if (p == Color.WHITE) hasWhite = true
                if (hasBlack && hasWhite) break
            }
            if (hasBlack && hasWhite) break
        }
        assertTrue("QR bitmap must contain both black modules and white background", hasBlack && hasWhite)

        val labelBmp = QrCodeGenerator.generateShippingLabelBitmap(
            orderId = "OD8492019401",
            awbNumber = "DLV-928371904",
            customerName = "Pooja Sharma",
            destination = "Bettiah, Bihar - 845438",
            productTitle = "Peplum Kurti & Tiered Sharara Set",
            sku = "GD-01",
            size = "L",
            amount = 599,
            paymentMethod = "UPI (PhonePe)"
        )
        assertEquals(900, labelBmp.width)
        assertEquals(1220, labelBmp.height)
    }

    @Test
    fun testSaveQrBitmapToGalleryAndDownloads() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val qrBmp = QrCodeGenerator.generateQrBitmap(
            content = "https://gyanvidresses.app/order/OD8492019401",
            sizePx = 320,
            headerTitle = "GYANVI QR",
            footerSubtitle = "OD8492019401"
        )

        val galleryResult = ImageSaver.saveBitmapToGallery(
            context = context,
            bitmap = qrBmp,
            fileNamePrefix = "Test_Order_QR",
            showToast = false
        )
        assertTrue("Saving QR to Gallery should succeed: ${galleryResult.message}", galleryResult.success)
        assertNotNull(galleryResult.uri)
        assertTrue(galleryResult.fileName.endsWith(".png"))

        val downloadResult = ImageSaver.saveBitmapToDownloads(
            context = context,
            bitmap = qrBmp,
            fileNamePrefix = "Test_Order_Download",
            showToast = false
        )
        assertTrue("Downloading QR should succeed: ${downloadResult.message}", downloadResult.success)
        assertNotNull(downloadResult.uri)
        assertTrue(downloadResult.fileName.endsWith(".png"))
    }

    @Test
    fun testGenerateAndSaveInvoiceBitmap() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sampleOrder = com.example.data.SampleData.initialOrders.first()
        val invoiceBmp = QrCodeGenerator.generateInvoiceBitmap(sampleOrder)
        assertEquals(900, invoiceBmp.width)
        assertEquals(1180, invoiceBmp.height)

        val savedInvoice = ImageSaver.saveBitmapToDownloads(
            context = context,
            bitmap = invoiceBmp,
            fileNamePrefix = "Invoice_${sampleOrder.orderId}",
            showToast = false
        )
        assertTrue("Invoice PNG download should succeed", savedInvoice.success)
        assertTrue(savedInvoice.fileName.endsWith(".png"))
    }
}
