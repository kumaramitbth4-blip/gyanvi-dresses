package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

data class SavedImageResult(
    val success: Boolean,
    val uri: Uri?,
    val fileName: String,
    val message: String
)

object ImageSaver {

    private fun sanitizeFileName(raw: String): String {
        val cleaned = raw.replace("[^A-Za-z0-9._-]".toRegex(), "_")
            .replace("_+".toRegex(), "_")
            .trim('_')
        return cleaned.ifBlank { "Gyanvi_Image" }.take(48)
    }

    /**
     * Saves any dynamically generated Bitmap (e.g. QR code, Shipping Label, Invoice)
     * as a crisp lossless PNG directly to the device Gallery (Pictures/GyanviDresses).
     */
    fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        fileNamePrefix: String,
        subFolder: String = "GyanviDresses",
        showToast: Boolean = true
    ): SavedImageResult {
        val filename = "${sanitizeFileName(fileNamePrefix)}_${System.currentTimeMillis()}.png"
        val resolver = context.contentResolver

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + File.separator + subFolder
                    )
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val table = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val insertedUri = resolver.insert(table, values)
                if (insertedUri != null) {
                    val stream: OutputStream? = resolver.openOutputStream(insertedUri)
                    if (stream != null) {
                        val compressed = stream.use { out ->
                            val ok = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            out.flush()
                            ok
                        }
                        if (compressed) {
                            val publishValues = ContentValues().apply {
                                put(MediaStore.MediaColumns.IS_PENDING, 0)
                            }
                            resolver.update(insertedUri, publishValues, null, null)
                            val msg = "✓ Saved to Gallery: Pictures/$subFolder/$filename"
                            if (showToast) {
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                            return SavedImageResult(true, insertedUri, filename, msg)
                        }
                    }
                    resolver.delete(insertedUri, null, null)
                }
            }

            // Legacy API < 29 or MediaStore fallback
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val targetDir = File(picturesDir, subFolder)
            val writableDir = if (targetDir.exists() || targetDir.mkdirs()) {
                targetDir
            } else {
                File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir,
                    subFolder
                ).apply { mkdirs() }
            }
            val imageFile = File(writableDir, filename)
            val ok = FileOutputStream(imageFile).use { out ->
                val res = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                res
            }
            if (!ok) {
                val failMsg = "Failed to encode QR image."
                if (showToast) Toast.makeText(context, failMsg, Toast.LENGTH_SHORT).show()
                return SavedImageResult(false, null, filename, failMsg)
            }

            MediaScannerConnection.scanFile(
                context,
                arrayOf(imageFile.absolutePath),
                arrayOf("image/png"),
                null
            )
            val uri = Uri.fromFile(imageFile)
            val msg = "✓ Saved to Gallery: ${imageFile.parentFile?.name ?: subFolder}/$filename"
            if (showToast) {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
            return SavedImageResult(true, uri, filename, msg)
        } catch (e: Exception) {
            e.printStackTrace()
            // Last-resort app-scoped gallery directory so user still gets a real saved PNG file
            return try {
                val fallbackDir = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir,
                    subFolder
                ).apply { mkdirs() }
                val fallbackFile = File(fallbackDir, filename)
                FileOutputStream(fallbackFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }
                val uri = runCatching {
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", fallbackFile)
                }.getOrDefault(Uri.fromFile(fallbackFile))
                val msg = "✓ QR image saved: ${fallbackFile.name}"
                if (showToast) Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                SavedImageResult(true, uri, filename, msg)
            } catch (inner: Exception) {
                val errMsg = "Could not save QR to Gallery: ${inner.localizedMessage ?: "Storage error"}"
                if (showToast) Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                SavedImageResult(false, null, filename, errMsg)
            }
        }
    }

    /**
     * Downloads a generated QR Code / Shipping Label Bitmap directly into Downloads/GyanviDresses.
     */
    fun saveBitmapToDownloads(
        context: Context,
        bitmap: Bitmap,
        fileNamePrefix: String,
        subFolder: String = "GyanviDresses",
        showToast: Boolean = true
    ): SavedImageResult {
        val filename = "${sanitizeFileName(fileNamePrefix)}_${System.currentTimeMillis()}.png"
        val resolver = context.contentResolver

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + File.separator + subFolder
                    )
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val table = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                val insertedUri = resolver.insert(table, values)
                if (insertedUri != null) {
                    val stream: OutputStream? = resolver.openOutputStream(insertedUri)
                    if (stream != null) {
                        val compressed = stream.use { out ->
                            val ok = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            out.flush()
                            ok
                        }
                        if (compressed) {
                            val publishValues = ContentValues().apply {
                                put(MediaStore.MediaColumns.IS_PENDING, 0)
                            }
                            resolver.update(insertedUri, publishValues, null, null)
                            val msg = "✓ Downloaded to Downloads/$subFolder/$filename"
                            if (showToast) Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            return SavedImageResult(true, insertedUri, filename, msg)
                        }
                    }
                    resolver.delete(insertedUri, null, null)
                }
            }

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetDir = File(downloadsDir, subFolder)
            val writableDir = if (targetDir.exists() || targetDir.mkdirs()) {
                targetDir
            } else {
                File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir,
                    subFolder
                ).apply { mkdirs() }
            }
            val file = File(writableDir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("image/png"),
                null
            )
            val msg = "✓ Downloaded: Downloads/$subFolder/$filename"
            if (showToast) Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            return SavedImageResult(true, Uri.fromFile(file), filename, msg)
        } catch (e: Exception) {
            e.printStackTrace()
            return saveBitmapToGallery(
                context = context,
                bitmap = bitmap,
                fileNamePrefix = fileNamePrefix,
                subFolder = subFolder,
                showToast = showToast
            )
        }
    }

    /**
     * Shares a generated QR Code / Shipping Label Bitmap as a real PNG image via Android Share Sheet.
     */
    fun shareBitmapImage(
        context: Context,
        bitmap: Bitmap,
        fileNamePrefix: String,
        caption: String
    ): Boolean {
        return try {
            val sharedDir = File(context.cacheDir, "shared_qr").apply { mkdirs() }
            val imageFile = File(sharedDir, "${sanitizeFileName(fileNamePrefix)}.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Share QR / Label via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            shareCustomText(context, fileNamePrefix, caption)
            false
        }
    }

    /**
     * Saves the product drawable to the device's Gallery / MediaStore Pictures directory.
     */
    fun saveToGallery(
        context: Context,
        @DrawableRes drawableResId: Int,
        productName: String
    ): Boolean {
        val bitmap = BitmapFactory.decodeResource(context.resources, drawableResId)
            ?: return false
        return saveBitmapToGallery(
            context = context,
            bitmap = bitmap,
            fileNamePrefix = "Gyanvi_$productName",
            showToast = true
        ).success
    }

    /**
     * Shares product details and link via standard Android Intent
     */
    fun shareProductText(context: Context, productName: String, price: Int) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out $productName on Gyanvi Dresses!")
            putExtra(
                Intent.EXTRA_TEXT,
                "✨ Beautiful Dress from Gyanvi Dresses! ✨\n\n" +
                "👗 $productName\n" +
                "💰 Special Price: ₹$price (Upto 80% OFF Maha Dhamaka Sale!)\n" +
                "🚚 Free Delivery & Cash on Delivery Available in Bettiah.\n\n" +
                "👉 Download Gyanvi Dresses App or shop online: https://gyanvidresses.app/product/$productName"
            )
        }
        val chooser = Intent.createChooser(shareIntent, "Share Gyanvi Dresses via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Shares directly to WhatsApp
     */
    fun shareToWhatsApp(context: Context, productName: String, price: Int, phoneNumber: String? = null) {
        try {
            val message = "✨ Check out *$productName* on *Gyanvi Dresses*!\nPrice: *₹$price* (Maha Dhamaka Discounted)\nFree Delivery & COD available. Order now: https://gyanvidresses.app/share"
            val uri = if (!phoneNumber.isNullOrBlank()) {
                val cleanPhone = phoneNumber.replace("[^0-9]".toRegex(), "")
                Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
            } else {
                Uri.parse("whatsapp://send?text=${Uri.encode(message)}")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            shareProductText(context, productName, price)
        }
    }

    /**
     * Sends custom SMS message to a phone number using standard SMS intent
     */
    fun sendDirectSms(context: Context, phoneNumber: String, messageText: String) {
        try {
            val cleanPhone = phoneNumber.replace("[^0-9+]".toRegex(), "")
            val uri = Uri.parse("smsto:$cleanPhone")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Send SMS via"))
        }
    }

    /**
     * Sends custom Email to an email address using standard mailto intent
     */
    fun sendDirectEmail(context: Context, emailAddress: String, subject: String, body: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${emailAddress.trim()}")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Send Email via"))
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Send Email via"))
        }
    }

    /**
     * Sends custom message to WhatsApp with target phone number
     */
    fun sendDirectWhatsAppMessage(context: Context, phoneNumber: String, messageText: String) {
        try {
            val cleanPhone = phoneNumber.replace("[^0-9]".toRegex(), "")
            val uri = if (cleanPhone.isNotEmpty()) {
                Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(messageText)}")
            } else {
                Uri.parse("whatsapp://send?text=${Uri.encode(messageText)}")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Message via"))
        }
    }

    /**
     * Opens system phone dialer pre-filled with the customer or courier phone number (no CALL_PHONE permission needed).
     */
    fun dialPhoneNumber(context: Context, phoneNumber: String) {
        try {
            val cleanPhone = phoneNumber.replace("[^0-9+]".toRegex(), "").ifBlank { "+919128754982" }
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Phone: $phoneNumber", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares any custom text via Android System Share sheet
     */
    fun shareCustomText(context: Context, title: String, text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "Share via Gyanvi Dresses")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
