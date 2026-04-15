package com.teui.core

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

data class PickedFile(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long?,
    val mimeType: String?,
)

object FilePickerHandler {
    fun describe(context: Context, uri: Uri): PickedFile {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri)

        var name = "Bilinmeyen dosya"
        var sizeBytes: Long? = null

        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

            if (cursor.moveToFirst()) {
                if (nameIndex >= 0) {
                    name = cursor.getString(nameIndex) ?: name
                }
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    sizeBytes = cursor.getLong(sizeIndex)
                }
            }
        }

        return PickedFile(
            uri = uri,
            name = name,
            sizeBytes = sizeBytes,
            mimeType = mimeType,
        )
    }

    fun loadImagePreview(context: Context, uri: Uri): ImageBitmap? {
        val mime = context.contentResolver.getType(uri) ?: return null
        if (!mime.startsWith("image/")) return null

        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)?.asImageBitmap()
        }
    }
}

fun Long.toReadableSize(): String {
    val kb = 1024.0
    val mb = kb * 1024.0

    return when {
        this >= mb -> String.format("%.2f MB", this / mb)
        this >= kb -> String.format("%.1f KB", this / kb)
        else -> "$this B"
    }
}
