package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import com.example.model.OrderItem
import kotlin.math.abs

/**
 * Pure Kotlin ISO/IEC 18004 QR Code Generator (Versions 1..9, Byte mode, Error Correction Level M)
 * plus branded Gyanvi Dresses Shipping Label & QR Card Bitmap renderer.
 *
 * Works completely offline with zero external library dependencies and generates real,
 * standards-compliant scannable QR codes.
 */
object QrCodeGenerator {

    // Total codewords, EC codewords per block, Group 1 (blocks, dataCw), Group 2 (blocks, dataCw) for Level M
    private data class VersionSpec(
        val version: Int,
        val totalCodewords: Int,
        val ecPerBlock: Int,
        val g1Blocks: Int,
        val g1DataCw: Int,
        val g2Blocks: Int,
        val g2DataCw: Int
    ) {
        val totalDataCodewords: Int get() = g1Blocks * g1DataCw + g2Blocks * g2DataCw
        val maxBytePayload: Int get() = ((totalDataCodewords * 8 - 12) / 8).coerceAtLeast(1)
    }

    private val VERSION_SPECS = listOf(
        VersionSpec(1, 26, 10, 1, 16, 0, 0),
        VersionSpec(2, 44, 16, 1, 28, 0, 0),
        VersionSpec(3, 70, 26, 1, 44, 0, 0),
        VersionSpec(4, 100, 18, 2, 32, 0, 0),
        VersionSpec(5, 134, 24, 2, 43, 0, 0),
        VersionSpec(6, 172, 16, 4, 27, 0, 0),
        VersionSpec(7, 196, 18, 4, 31, 0, 0),
        VersionSpec(8, 242, 22, 2, 38, 2, 39),
        VersionSpec(9, 292, 22, 3, 36, 2, 37)
    )

    private val ALIGNMENT_POSITIONS = mapOf(
        1 to emptyList(),
        2 to listOf(6, 18),
        3 to listOf(6, 22),
        4 to listOf(6, 26),
        5 to listOf(6, 30),
        6 to listOf(6, 34),
        7 to listOf(6, 22, 38),
        8 to listOf(6, 24, 42),
        9 to listOf(6, 26, 46)
    )

    private val VERSION_INFO_BITS = mapOf(
        7 to 0x07C94,
        8 to 0x085BC,
        9 to 0x09A99
    )

    // GF(256) with primitive polynomial 0x11D
    private val gfExp = IntArray(512)
    private val gfLog = IntArray(256)

    init {
        var x = 1
        for (i in 0 until 255) {
            gfExp[i] = x
            gfLog[x] = i
            x = x shl 1
            if (x >= 256) x = x xor 0x11D
        }
        for (i in 255 until 512) {
            gfExp[i] = gfExp[i - 255]
        }
    }

    private fun gfMul(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return gfExp[gfLog[a] + gfLog[b]]
    }

    private fun buildGeneratorPoly(ecLen: Int): IntArray {
        var poly = intArrayOf(1)
        for (i in 0 until ecLen) {
            val next = IntArray(poly.size + 1)
            val root = gfExp[i]
            for (j in poly.indices) {
                next[j] = next[j] xor poly[j]
                next[j + 1] = next[j + 1] xor gfMul(poly[j], root)
            }
            poly = next
        }
        return poly
    }

    private fun computeReedSolomonEc(data: IntArray, ecLen: Int): IntArray {
        val gen = buildGeneratorPoly(ecLen)
        val remainder = IntArray(ecLen)
        for (b in data) {
            val factor = b xor remainder[0]
            System.arraycopy(remainder, 1, remainder, 0, ecLen - 1)
            remainder[ecLen - 1] = 0
            if (factor != 0) {
                for (j in 0 until ecLen) {
                    remainder[j] = remainder[j] xor gfMul(gen[j + 1], factor)
                }
            }
        }
        return remainder
    }

    /**
     * Encodes [content] into a boolean QR module matrix (`true` = black module, `false` = white).
     */
    fun encodeQrMatrix(content: String): Array<BooleanArray> {
        val safeText = content.ifBlank { "https://gyanvidresses.app" }
        var bytes = safeText.toByteArray(Charsets.UTF_8)
        val maxSupported = VERSION_SPECS.last().maxBytePayload
        if (bytes.size > maxSupported) {
            bytes = bytes.copyOf(maxSupported)
        }

        val spec = VERSION_SPECS.firstOrNull { bytes.size <= it.maxBytePayload } ?: VERSION_SPECS.last()
        val size = 17 + spec.version * 4

        // Build bitstream: Byte Mode (0100) + 8-bit length + UTF-8 bytes + terminator + padding
        val totalDataBits = spec.totalDataCodewords * 8
        val bits = ArrayList<Int>(totalDataBits)
        fun appendBits(value: Int, count: Int) {
            for (i in count - 1 downTo 0) {
                bits.add((value ushr i) and 1)
            }
        }

        appendBits(0b0100, 4) // Byte mode
        appendBits(bytes.size, 8) // Char count for versions 1..9
        for (b in bytes) {
            appendBits(b.toInt() and 0xFF, 8)
        }
        val terminatorLen = minOf(4, totalDataBits - bits.size)
        appendBits(0, terminatorLen)
        while (bits.size % 8 != 0) bits.add(0)
        var padIndex = 0
        while (bits.size < totalDataBits) {
            appendBits(if (padIndex % 2 == 0) 0xEC else 0x11, 8)
            padIndex++
        }

        val dataCodewords = IntArray(spec.totalDataCodewords) { idx ->
            var v = 0
            for (b in 0 until 8) {
                v = (v shl 1) or bits[idx * 8 + b]
            }
            v
        }

        // Split into blocks & compute EC
        val dataBlocks = ArrayList<IntArray>()
        val ecBlocks = ArrayList<IntArray>()
        var offset = 0
        repeat(spec.g1Blocks) {
            val blk = dataCodewords.copyOfRange(offset, offset + spec.g1DataCw)
            offset += spec.g1DataCw
            dataBlocks.add(blk)
            ecBlocks.add(computeReedSolomonEc(blk, spec.ecPerBlock))
        }
        repeat(spec.g2Blocks) {
            val blk = dataCodewords.copyOfRange(offset, offset + spec.g2DataCw)
            offset += spec.g2DataCw
            dataBlocks.add(blk)
            ecBlocks.add(computeReedSolomonEc(blk, spec.ecPerBlock))
        }

        // Interleave data codewords + EC codewords
        val interleaved = ArrayList<Int>(spec.totalCodewords)
        val maxDataLen = maxOf(spec.g1DataCw, spec.g2DataCw)
        for (i in 0 until maxDataLen) {
            for (blk in dataBlocks) {
                if (i < blk.size) interleaved.add(blk[i])
            }
        }
        for (i in 0 until spec.ecPerBlock) {
            for (ec in ecBlocks) {
                if (i < ec.size) interleaved.add(ec[i])
            }
        }

        val finalBits = ArrayList<Int>(interleaved.size * 8 + 7)
        for (cw in interleaved) {
            for (b in 7 downTo 0) {
                finalBits.add((cw ushr b) and 1)
            }
        }
        // Remainder bits (Versions 2..6 have 7 remainder bits; V7..9 have 0; V1 has 0)
        val remainderCount = if (spec.version in 2..6) 7 else 0
        repeat(remainderCount) { finalBits.add(0) }

        // Place function patterns & reserved regions
        val modules = Array(size) { BooleanArray(size) }
        val isFunction = Array(size) { BooleanArray(size) }

        fun setFunctionModule(r: Int, c: Int, dark: Boolean) {
            if (r in 0 until size && c in 0 until size) {
                modules[r][c] = dark
                isFunction[r][c] = true
            }
        }

        fun drawFinder(topR: Int, leftC: Int) {
            for (dr in -1..7) {
                for (dc in -1..7) {
                    val r = topR + dr
                    val c = leftC + dc
                    if (r in 0 until size && c in 0 until size) {
                        val isDark = if (dr in 0..6 && dc in 0..6) {
                            dr == 0 || dr == 6 || dc == 0 || dc == 6 || (dr in 2..4 && dc in 2..4)
                        } else {
                            false
                        }
                        setFunctionModule(r, c, isDark)
                    }
                }
            }
        }

        drawFinder(0, 0)
        drawFinder(0, size - 7)
        drawFinder(size - 7, 0)

        // Timing patterns
        for (i in 8 until size - 8) {
            setFunctionModule(6, i, i % 2 == 0)
            setFunctionModule(i, 6, i % 2 == 0)
        }

        // Alignment patterns
        val alignCoords = ALIGNMENT_POSITIONS[spec.version].orEmpty()
        for (rCenter in alignCoords) {
            for (cCenter in alignCoords) {
                val nearTopLeft = rCenter <= 8 && cCenter <= 8
                val nearTopRight = rCenter <= 8 && cCenter >= size - 9
                val nearBottomLeft = rCenter >= size - 9 && cCenter <= 8
                if (!nearTopLeft && !nearTopRight && !nearBottomLeft) {
                    for (dr in -2..2) {
                        for (dc in -2..2) {
                            val isDark = abs(dr) == 2 || abs(dc) == 2 || (dr == 0 && dc == 0)
                            setFunctionModule(rCenter + dr, cCenter + dc, isDark)
                        }
                    }
                }
            }
        }

        // Reserve format info cells & dark module
        for (i in 0..8) {
            if (i != 6) {
                isFunction[8][i] = true
                isFunction[i][8] = true
            }
        }
        for (i in size - 8 until size) {
            isFunction[8][i] = true
            isFunction[i][8] = true
        }
        setFunctionModule(4 * spec.version + 9, 8, true)

        // Reserve & draw version info for V7+
        VERSION_INFO_BITS[spec.version]?.let { vBits ->
            for (i in 0 until 18) {
                val dark = ((vBits ushr i) and 1) != 0
                val a = size - 11 + (i % 3)
                val b = i / 3
                setFunctionModule(b, a, dark)
                setFunctionModule(a, b, dark)
            }
        }

        // Place data bits in zig-zag pattern
        var bitCursor = 0
        var col = size - 1
        var upward = true
        while (col > 0) {
            if (col == 6) col-- // Skip vertical timing column
            for (step in 0 until size) {
                val r = if (upward) size - 1 - step else step
                for (dx in 0..1) {
                    val c = col - dx
                    if (!isFunction[r][c]) {
                        val bit = if (bitCursor < finalBits.size) finalBits[bitCursor++] else 0
                        modules[r][c] = bit == 1
                    }
                }
            }
            col -= 2
            upward = !upward
        }

        // Apply best mask out of 0..7 based on standard penalty rules
        var bestMask = 0
        var lowestPenalty = Int.MAX_VALUE
        for (mask in 0..7) {
            val candidate = applyMaskAndFormat(modules, isFunction, mask)
            val penalty = computePenalty(candidate)
            if (penalty < lowestPenalty) {
                lowestPenalty = penalty
                bestMask = mask
            }
        }
        return applyMaskAndFormat(modules, isFunction, bestMask)
    }

    private fun maskMatches(mask: Int, r: Int, c: Int): Boolean = when (mask) {
        0 -> (r + c) % 2 == 0
        1 -> r % 2 == 0
        2 -> c % 3 == 0
        3 -> (r + c) % 3 == 0
        4 -> ((r / 2) + (c / 3)) % 2 == 0
        5 -> ((r * c) % 2 + (r * c) % 3) == 0
        6 -> (((r * c) % 2) + ((r * c) % 3)) % 2 == 0
        else -> (((r + c) % 2) + ((r * c) % 3)) % 2 == 0
    }

    private fun applyMaskAndFormat(
        base: Array<BooleanArray>,
        isFunction: Array<BooleanArray>,
        mask: Int
    ): Array<BooleanArray> {
        val n = base.size
        val out = Array(n) { r ->
            BooleanArray(n) { c ->
                if (isFunction[r][c]) base[r][c] else (base[r][c] xor maskMatches(mask, r, c))
            }
        }

        // Format info for EC Level M (00) + 3-bit mask -> BCH(15,5) XOR 0x5412
        val data = (0b00 shl 3) or mask
        var rem = data
        repeat(10) { rem = (rem shl 1) xor (((rem ushr 9) and 1) * 0x537) }
        val fmtBits = ((data shl 10) or rem) xor 0x5412

        for (i in 0..5) {
            out[8][i] = ((fmtBits ushr i) and 1) != 0
        }
        out[8][7] = ((fmtBits ushr 6) and 1) != 0
        out[8][8] = ((fmtBits ushr 7) and 1) != 0
        out[7][8] = ((fmtBits ushr 8) and 1) != 0
        for (i in 9..14) {
            out[14 - i][8] = ((fmtBits ushr i) and 1) != 0
        }

        for (i in 0..7) {
            out[n - 1 - i][8] = ((fmtBits ushr i) and 1) != 0
        }
        for (i in 8..14) {
            out[8][n - 15 + i] = ((fmtBits ushr i) and 1) != 0
        }
        return out
    }

    private fun computePenalty(grid: Array<BooleanArray>): Int {
        val n = grid.size
        var penalty = 0

        // Rule 1: runs of >= 5 same-color modules
        for (r in 0 until n) {
            var runLen = 1
            for (c in 1 until n) {
                if (grid[r][c] == grid[r][c - 1]) {
                    runLen++
                } else {
                    if (runLen >= 5) penalty += 3 + (runLen - 5)
                    runLen = 1
                }
            }
            if (runLen >= 5) penalty += 3 + (runLen - 5)
        }
        for (c in 0 until n) {
            var runLen = 1
            for (r in 1 until n) {
                if (grid[r][c] == grid[r - 1][c]) {
                    runLen++
                } else {
                    if (runLen >= 5) penalty += 3 + (runLen - 5)
                    runLen = 1
                }
            }
            if (runLen >= 5) penalty += 3 + (runLen - 5)
        }

        // Rule 2: 2x2 blocks
        for (r in 0 until n - 1) {
            for (c in 0 until n - 1) {
                val v = grid[r][c]
                if (grid[r][c + 1] == v && grid[r + 1][c] == v && grid[r + 1][c + 1] == v) {
                    penalty += 3
                }
            }
        }

        // Rule 4: dark ratio deviation from 50%
        var darkCount = 0
        for (r in 0 until n) {
            for (c in 0 until n) {
                if (grid[r][c]) darkCount++
            }
        }
        val percent = (darkCount * 100) / (n * n)
        val prev5 = (percent / 5) * 5
        val next5 = prev5 + 5
        penalty += minOf(abs(prev5 - 50) / 5, abs(next5 - 50) / 5) * 10
        return penalty
    }

    /**
     * Renders a crisp high-contrast QR Code Bitmap, with optional header/footer text banner
     * so saved Gallery / Download PNG images remain self-descriptive.
     */
    fun generateQrBitmap(
        content: String,
        sizePx: Int = 720,
        headerTitle: String? = null,
        footerSubtitle: String? = null
    ): Bitmap {
        val matrix = encodeQrMatrix(content)
        val quietModules = 4
        val totalModules = matrix.size + quietModules * 2

        val hasHeader = !headerTitle.isNullOrBlank()
        val hasFooter = !footerSubtitle.isNullOrBlank()
        val headerHeight = if (hasHeader) (sizePx * 0.14f).toInt().coerceAtLeast(64) else 0
        val footerHeight = if (hasFooter) (sizePx * 0.12f).toInt().coerceAtLeast(56) else 0

        val bitmapHeight = sizePx + headerHeight + footerHeight
        val bitmap = Bitmap.createBitmap(sizePx, bitmapHeight, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(sizePx * bitmapHeight) { Color.WHITE }

        val modulePx = (sizePx / totalModules).coerceAtLeast(1)
        val qrDrawSize = modulePx * totalModules
        val startX = (sizePx - qrDrawSize) / 2
        val startY = headerHeight + (sizePx - qrDrawSize) / 2

        for (r in matrix.indices) {
            for (c in matrix[r].indices) {
                if (matrix[r][c]) {
                    val pxX = startX + (c + quietModules) * modulePx
                    val pxY = startY + (r + quietModules) * modulePx
                    for (dy in 0 until modulePx) {
                        val y = pxY + dy
                        if (y in 0 until bitmapHeight) {
                            val rowOffset = y * sizePx
                            for (dx in 0 until modulePx) {
                                val x = pxX + dx
                                if (x in 0 until sizePx) {
                                    pixels[rowOffset + x] = Color.BLACK
                                }
                            }
                        }
                    }
                }
            }
        }
        bitmap.setPixels(pixels, 0, sizePx, 0, 0, sizePx, bitmapHeight)
        val canvas = Canvas(bitmap)

        if (hasHeader) {
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#880E4F")
                textSize = (headerHeight * 0.44f).coerceIn(22f, 40f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val bounds = Rect()
            val titleText = headerTitle.orEmpty()
            titlePaint.getTextBounds(titleText, 0, titleText.length, bounds)
            canvas.drawText(titleText, sizePx / 2f, headerHeight * 0.65f, titlePaint)

            val linePaint = Paint().apply {
                color = Color.parseColor("#E0E0E0")
                strokeWidth = 2f
            }
            canvas.drawLine(32f, headerHeight.toFloat(), sizePx - 32f, headerHeight.toFloat(), linePaint)
        }

        if (hasFooter) {
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#212121")
                textSize = (footerHeight * 0.40f).coerceIn(18f, 32f)
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val linePaint = Paint().apply {
                color = Color.parseColor("#E0E0E0")
                strokeWidth = 2f
            }
            val footerTop = (headerHeight + sizePx).toFloat()
            canvas.drawLine(32f, footerTop, sizePx - 32f, footerTop, linePaint)
            canvas.drawText(
                footerSubtitle.orEmpty().take(42),
                sizePx / 2f,
                footerTop + footerHeight * 0.62f,
                footerPaint
            )
        }

        return bitmap
    }

    /**
     * Renders a complete high-resolution 4x6-style Shipping Label PNG containing Order & Consignee
     * info, linear courier barcode, and a real scannable QR code.
     */
    fun generateShippingLabelBitmap(
        orderId: String,
        awbNumber: String,
        customerName: String,
        destination: String,
        productTitle: String,
        sku: String,
        size: String,
        amount: Int,
        paymentMethod: String,
        packerName: String = "Gyanvi Dresses Packaging & Logistics Hub",
        packerId: String = "PKR-GD-845438"
    ): Bitmap {
        val width = 900
        val height = 1220
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRect(18f, 18f, width - 18f, height - 18f, borderPaint)

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#212121")
            typeface = Typeface.DEFAULT
        }
        val monoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val dividerPaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 3f
        }

        // Top Seller & Courier Header
        boldPaint.textSize = 36f
        canvas.drawText("GYANVI DRESSES EXPRESS", 44f, 76f, boldPaint)
        bodyPaint.textSize = 24f
        canvas.drawText("PRE-DISPATCH COURIER SHIPPING LABEL", 44f, 112f, bodyPaint)
        canvas.drawLine(18f, 134f, width - 18f, 134f, dividerPaint)

        // Consignee Details
        boldPaint.textSize = 26f
        canvas.drawText("SHIP TO:", 44f, 178f, boldPaint)
        boldPaint.textSize = 32f
        canvas.drawText(customerName.ifBlank { "Gyanvi Customer" }, 44f, 220f, boldPaint)
        bodyPaint.textSize = 26f
        canvas.drawText(destination.take(44), 44f, 260f, bodyPaint)
        canvas.drawText("Payment: $paymentMethod • Total: Rs. $amount", 44f, 300f, bodyPaint)
        canvas.drawLine(18f, 326f, width - 18f, 326f, dividerPaint)

        // Order & Product Box
        boldPaint.textSize = 28f
        canvas.drawText("Order ID: $orderId", 44f, 372f, boldPaint)
        monoPaint.textSize = 26f
        canvas.drawText("AWB: $awbNumber", 44f, 412f, monoPaint)
        bodyPaint.textSize = 24f
        canvas.drawText("Item: ${productTitle.take(42)}", 44f, 452f, bodyPaint)
        canvas.drawText("SKU: $sku • Size: $size • Qty: 1", 44f, 484f, bodyPaint)
        bodyPaint.textSize = 21f
        canvas.drawText("Packer: ${packerName.take(30)} | ID: $packerId", 44f, 514f, bodyPaint)
        canvas.drawLine(18f, 534f, width - 18f, 534f, dividerPaint)

        // Embedded Scannable QR Code
        val qrPayload = "GYANVI|ORD=$orderId|AWB=$awbNumber|SKU=$sku|AMT=$amount|PKR=$packerId"
        val qrBmp = generateQrBitmap(content = qrPayload, sizePx = 400)
        canvas.drawBitmap(qrBmp, (width - qrBmp.width) / 2f, 552f, null)

        // Deterministic 1D Barcode Stripes at bottom
        val barcodeTop = 985f
        val barcodeHeight = 120f
        var bx = 90f
        val barSeed = (orderId + awbNumber).hashCode()
        val barPaint = Paint().apply { color = Color.BLACK; style = Paint.Style.FILL }
        for (i in 0 until 72) {
            val w = 4f + ((abs(barSeed + i * 31) % 3) * 3f)
            if (i % 2 == 0 && bx + w < width - 90f) {
                canvas.drawRect(bx, barcodeTop, bx + w, barcodeTop + barcodeHeight, barPaint)
            }
            bx += w + 4f
        }

        monoPaint.textSize = 26f
        monoPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("*$awbNumber*", width / 2f, 1152f, monoPaint)
        bodyPaint.textSize = 20f
        bodyPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Scan QR to verify authentic Gyanvi Dresses branded packet", width / 2f, 1186f, bodyPaint)

        return bitmap
    }

    /**
     * Renders an official Gyanvi Dresses Tax Invoice & Order Summary PNG with embedded
     * GSTIN, Order ID, AWB, Customer details, Price breakdown, Refund/Payment status,
     * and a real scannable Invoice Verification QR Code.
     */
    fun generateInvoiceBitmap(order: OrderItem): Bitmap {
        val width = 900
        val height = 1180
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        val canvas = Canvas(bitmap)

        val borderPaint = Paint().apply {
            color = Color.parseColor("#880E4F")
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRect(20f, 20f, width - 20f, height - 20f, borderPaint)

        val headerBg = Paint().apply { color = Color.parseColor("#880E4F"); style = Paint.Style.FILL }
        canvas.drawRect(20f, 20f, width - 20f, 150f, headerBg)

        val whiteBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 36f
        }
        val whiteBody = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FCE4EC")
            textSize = 22f
        }
        canvas.drawText("GYANVI DRESSES BOUTIQUE • TAX INVOICE", 48f, 78f, whiteBold)
        canvas.drawText("GSTIN: 10ABCDE1234F1Z5  •  Lal Bazar, Bettiah - 845438", 48f, 120f, whiteBody)

        val bold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 26f
        }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#333333")
            textSize = 23f
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#CCCCCC")
            strokeWidth = 2f
        }

        val invNumber = "INV-${order.orderId.takeLast(6)}"
        canvas.drawText("Invoice No: $invNumber", 48f, 205f, bold)
        canvas.drawText("Order Date: ${order.orderDate}", 520f, 205f, body)
        canvas.drawText("Order ID: ${order.orderId}", 48f, 245f, body)
        canvas.drawText("AWB: ${order.awbNumber} (${order.courierPartner})", 48f, 282f, body)
        canvas.drawLine(40f, 308f, width - 40f, 308f, linePaint)

        canvas.drawText("BILLED & SHIPPED TO:", 48f, 352f, bold)
        canvas.drawText("${order.customerName}  •  ${order.customerPhone}", 48f, 390f, body)
        canvas.drawText(order.deliveryAddress.take(54), 48f, 426f, body)
        canvas.drawLine(40f, 456f, width - 40f, 456f, linePaint)

        canvas.drawText("ITEM DESCRIPTION", 48f, 502f, bold)
        canvas.drawText(order.product.name.take(44), 48f, 542f, body)
        canvas.drawText(
            "SKU: ${order.product.id.uppercase()}  |  Size: ${order.selectedSize}  |  Color: ${order.selectedColor}  |  Qty: ${order.quantity}",
            48f,
            580f,
            body
        )
        canvas.drawLine(40f, 610f, width - 40f, 610f, linePaint)

        val gstAmt = (order.totalAmount * 0.05).toInt()
        val taxableAmt = (order.totalAmount - gstAmt).coerceAtLeast(1)
        canvas.drawText("Taxable Value:", 48f, 658f, body)
        canvas.drawText("Rs. $taxableAmt", 680f, 658f, body)
        canvas.drawText("GST (5% Textile):", 48f, 696f, body)
        canvas.drawText("Rs. $gstAmt", 680f, 696f, body)
        canvas.drawText("Shipping Charges:", 48f, 734f, body)
        canvas.drawText("Rs. ${order.shippingCharges} (Included/Seller)", 560f, 734f, body)

        bold.textSize = 30f
        bold.color = Color.parseColor("#880E4F")
        canvas.drawText("GRAND TOTAL: Rs. ${order.totalAmount}", 48f, 790f, bold)
        body.textSize = 22f
        canvas.drawText("Payment Mode: ${order.paymentMethod}  •  Status: ${order.paymentStatus}", 48f, 830f, body)
        if (order.refundAmount > 0) {
            canvas.drawText("Refund Status: ${order.refundStatus} (Rs. ${order.refundAmount})", 48f, 866f, body)
        }

        val qrPayload = "GYANVI-INV|$invNumber|ORD=${order.orderId}|AMT=${order.totalAmount}|PAY=${order.paymentStatus}"
        val qrBmp = generateQrBitmap(content = qrPayload, sizePx = 220)
        canvas.drawBitmap(qrBmp, 48f, 900f, null)

        bold.color = Color.BLACK
        bold.textSize = 23f
        canvas.drawText("Authorized Signatory", 560f, 955f, bold)
        canvas.drawText("Gyanvi Dresses Boutique", 560f, 992f, body)
        body.textSize = 19f
        canvas.drawText("Packer: ${order.packerName.take(26)}", 500f, 1032f, body)
        canvas.drawText("Packer ID: ${order.packerId}", 500f, 1062f, body)
        body.textSize = 22f
        canvas.drawText("Scan QR to verify authentic tax invoice", 300f, 1130f, body)

        return bitmap
    }

    // Standard Code-128 module width patterns (symbols 0..106: 6 widths each except 106 which has 7)
    private val CODE128_PATTERNS = arrayOf(
        "212222", "222122", "222221", "121223", "121322", "131222", "122213", "122312", "132212", "221213",
        "221312", "231212", "112232", "122132", "122231", "113222", "123122", "123221", "223211", "221132",
        "221231", "213212", "223112", "312131", "311222", "321122", "321221", "312212", "322112", "322211",
        "212123", "212321", "232121", "111323", "131123", "131321", "112313", "132113", "132311", "211313",
        "231113", "231311", "112133", "112331", "132131", "113123", "113321", "133121", "313121", "211331",
        "231131", "213113", "213311", "213131", "311123", "311321", "331121", "312113", "312311", "332111",
        "314111", "221411", "431111", "111224", "111422", "121124", "121421", "141122", "141221", "112214",
        "112412", "122114", "122411", "142112", "142211", "241211", "221114", "413111", "241112", "134111",
        "111242", "121142", "121241", "114212", "124112", "124211", "411212", "421112", "421211", "212141",
        "214121", "412121", "111143", "111341", "131141", "114113", "114311", "411113", "411311", "113141",
        "114131", "311141", "411131", "211412", "211214", "211232", "2331112"
    )

    /**
     * Encodes ASCII [code] using standards-compliant Code-128B (Start B = 104 + checksum mod 103 + Stop).
     * Returns module booleans where `true` = black bar and `false` = white space.
     */
    fun encodeCode128Bars(code: String): BooleanArray {
        val clean = code.filter { it.code in 32..126 }.ifBlank { "GYN-HOLD" }.take(28)
        val symbols = ArrayList<Int>(clean.length + 3)
        symbols.add(104) // Start Code B
        var checksum = 104
        clean.forEachIndexed { idx, ch ->
            val value = ch.code - 32
            symbols.add(value)
            checksum += value * (idx + 1)
        }
        symbols.add(checksum % 103)
        symbols.add(106) // Stop symbol

        val bars = ArrayList<Boolean>(symbols.size * 11 + 20)
        repeat(8) { bars.add(false) } // Left quiet zone
        for (sym in symbols) {
            val pattern = CODE128_PATTERNS[sym]
            pattern.forEachIndexed { pos, widthChar ->
                val isBar = pos % 2 == 0
                val width = widthChar - '0'
                repeat(width) { bars.add(isBar) }
            }
        }
        repeat(8) { bars.add(false) } // Right quiet zone
        return bars.toBooleanArray()
    }

    /**
     * Generates a high-contrast 1D Code-128B Barcode Bitmap (with optional human-readable text below).
     */
    fun generateBarcodeBitmap(
        code: String,
        widthPx: Int = 480,
        heightPx: Int = 110,
        labelText: String? = "*$code*"
    ): Bitmap {
        val bars = encodeCode128Bars(code)
        val hasLabel = !labelText.isNullOrBlank()
        val labelSpace = if (hasLabel) (heightPx * 0.28f).toInt().coerceIn(20, 34) else 4
        val barBottom = (heightPx - labelSpace).coerceAtLeast(24)

        val safeWidth = widthPx.coerceAtLeast(bars.size)
        val safeHeight = heightPx.coerceAtLeast(48)
        val bitmap = Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(safeWidth * safeHeight) { Color.WHITE }

        val moduleWidth = (safeWidth.toFloat() / bars.size).coerceAtLeast(1f)
        val totalBarDrawWidth = (moduleWidth * bars.size).toInt().coerceAtMost(safeWidth)
        val offsetX = (safeWidth - totalBarDrawWidth) / 2

        for (i in bars.indices) {
            if (bars[i]) {
                val x0 = offsetX + (i * moduleWidth).toInt()
                val x1 = (offsetX + ((i + 1) * moduleWidth).toInt()).coerceAtLeast(x0 + 1).coerceAtMost(safeWidth)
                for (y in 4 until barBottom) {
                    val rowStart = y * safeWidth
                    for (x in x0 until x1) {
                        pixels[rowStart + x] = Color.BLACK
                    }
                }
            }
        }
        bitmap.setPixels(pixels, 0, safeWidth, 0, 0, safeWidth, safeHeight)

        if (hasLabel) {
            val canvas = Canvas(bitmap)
            val txtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textSize = (labelSpace * 0.68f).coerceIn(13f, 24f)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                labelText.orEmpty().take(36),
                safeWidth / 2f,
                safeHeight - (labelSpace * 0.22f),
                txtPaint
            )
        }
        return bitmap
    }
}
