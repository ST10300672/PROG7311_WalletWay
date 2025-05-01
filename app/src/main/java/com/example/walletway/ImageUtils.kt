package com.example.walletway

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "expense_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)

        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        file.absolutePath // Return full path
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
