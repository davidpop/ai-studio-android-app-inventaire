package com.example.data.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class PhotoStorageManager(private val context: Context) {

    private val photosDir: File by lazy {
        File(context.filesDir, "photos").apply {
            if (!exists()) mkdirs()
        }
    }

    private val thumbsDir: File by lazy {
        File(photosDir, "thumbs").apply {
            if (!exists()) mkdirs()
        }
    }

    suspend fun saveFromUri(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val targetFile = File(photosDir, fileName)

            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return@withContext null

            // Read rotation from EXIF if available
            val rotationDegrees = getExifOrientation(uri)
            val rotatedBitmap = if (rotationDegrees != 0) {
                rotateBitmap(originalBitmap, rotationDegrees)
            } else {
                originalBitmap
            }

            // Scale down if larger than 1600px width/height
            val scaledBitmap = scaleBitmapDown(rotatedBitmap, maxDimension = 1600)

            FileOutputStream(targetFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            // Also create thumbnail
            createThumbnail(targetFile, fileName)

            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveBitmap(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val targetFile = File(photosDir, fileName)
            val scaledBitmap = scaleBitmapDown(bitmap, maxDimension = 1600)

            FileOutputStream(targetFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            createThumbnail(targetFile, fileName)
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveBitmapFromByteArray(bytes: ByteArray, rotationDegrees: Int): String? = withContext(Dispatchers.IO) {
        try {
            val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@withContext null
            val rotatedBitmap = if (rotationDegrees != 0) {
                rotateBitmap(originalBitmap, rotationDegrees)
            } else {
                originalBitmap
            }
            saveBitmap(rotatedBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createThumbnail(sourceFile: File, fileName: String) {
        try {
            val thumbFile = File(thumbsDir, "thumb_$fileName")
            val bmp = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return
            val thumb = scaleBitmapDown(bmp, maxDimension = 320)
            FileOutputStream(thumbFile).use { out ->
                thumb.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getThumbnailPath(originalPath: String): String {
        val originalFile = File(originalPath)
        val thumbFile = File(thumbsDir, "thumb_${originalFile.name}")
        return if (thumbFile.exists()) thumbFile.absolutePath else originalPath
    }

    suspend fun deletePhoto(filePath: String) = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) file.delete()
            val thumb = File(thumbsDir, "thumb_${file.name}")
            if (thumb.exists()) thumb.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun getExifOrientation(uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
