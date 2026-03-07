package com.example.maxx.domain.usecase.proxy

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import com.example.maxx.domain.models.ProxyProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ExportProxiesUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    sealed class ExportResult {
        data class Success(val file: File) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }

    suspend operator fun invoke(proxies: List<ProxyProfile>): ExportResult = withContext(Dispatchers.IO) {
        try {
            val exportContent = formatProxiesForExport(proxies)
            val fileName = createExportFileName()
            val file = createExportFile(fileName)

            FileOutputStream(file).use { outputStream ->
                outputStream.write(exportContent.toByteArray())
            }

            notifyFileCreated(file)
            ExportResult.Success(file)
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun formatProxiesForExport(proxies: List<ProxyProfile>): String {
        val stringBuilder = StringBuilder()
        val timestamp = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())

        stringBuilder.append("Proxy Export - $timestamp\n\n")

        proxies.forEachIndexed { index, proxy ->
            stringBuilder.append("${index + 1}. ${proxy.name}\n")
            stringBuilder.append("   IP: ${proxy.ip}\n")
            stringBuilder.append("   Port: ${proxy.port}\n")
            stringBuilder.append("   Protocol: ${proxy.protocol.uppercase()}\n")
            stringBuilder.append("   Username: ${proxy.username ?: "N/A"}\n")
            stringBuilder.append("   Password: ${proxy.password ?: "N/A"}\n")
            stringBuilder.append("   Flag URL: ${proxy.flagUrl ?: "N/A"}\n\n")
        }

        return stringBuilder.toString()
    }

    private fun createExportFileName(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "proxies_$timeStamp.txt"
    }

    private fun createExportFile(fileName: String): File {
        val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return if (publicDir != null && publicDir.exists()) {
            File(publicDir, fileName)
        } else {
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        }
    }

    private fun notifyFileCreated(file: File) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = Uri.fromFile(file)
        context.sendBroadcast(mediaScanIntent)
    }
}

