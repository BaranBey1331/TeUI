package com.teui

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.teui.core.CommandRunner
import com.teui.core.FilePickerHandler
import com.teui.core.PickedFile
import com.teui.core.TermuxCommandRunner
import com.teui.ui.TeUIScreen
import java.io.File
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                TeUIRoute()
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun TeUIRoute() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var commandText by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }
    var logs by remember { mutableStateOf(listOf("TeUI hazır. Bir komut gir ve çalıştır.")) }

    var selectedFile by remember { mutableStateOf<PickedFile?>(null) }
    var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var termuxReady by remember { mutableStateOf(false) }

    val openDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        selectedFile = FilePickerHandler.describe(context, uri)
        selectedImage = FilePickerHandler.loadImagePreview(context, uri)

        logs = logs + listOf(
            "[Dosya] ${selectedFile?.name ?: "Bilinmeyen"} seçildi.",
        )
    }

    var workingDir by remember(context) {
        mutableStateOf(context.filesDir)
    }

    LaunchedEffect(workingDir) {
        logs = logs + "[Çalışma dizini] ${workingDir.absolutePath}"
    }

    LaunchedEffect(Unit) {
        val termuxIssue = TermuxCommandRunner.getSetupIssue(context)
        termuxReady = termuxIssue == null
        if (termuxIssue == null) {
            logs = logs + "[Termux] Bağlandı: komutlar Termux shell üzerinden çalışacak."
        } else {
            logs = logs + "[Termux] Yerel shell fallback: $termuxIssue"
        }
    }

    TeUIScreen(
        commandText = commandText,
        onCommandTextChange = { commandText = it },
        onRunCommand = {
            val trimmed = commandText.trim()
            if (trimmed.isEmpty() || isRunning) return@TeUIScreen

            scope.launch {
                isRunning = true
                logs = logs + "\$ $trimmed"

                val termuxIssue = TermuxCommandRunner.getSetupIssue(context)
                termuxReady = termuxIssue == null
                val result = if (termuxIssue == null) {
                    TermuxCommandRunner.run(
                        command = trimmed,
                        workingDirectory = workingDir,
                        context = context,
                    )
                } else {
                    CommandRunner.run(
                        command = trimmed,
                        workingDirectory = workingDir,
                    )
                }

                val updatedLogs = mutableListOf<String>()
                if (result.stdout.isNotBlank()) {
                    updatedLogs += result.stdout
                }
                if (result.stderr.isNotBlank()) {
                    updatedLogs += "[stderr]\n${result.stderr}"
                }
                updatedLogs += "[exit=${result.exitCode}] ${result.durationMs}ms"

                logs = logs + updatedLogs
                workingDir = normalizeWorkingDirectory(result.workingDirectory, context.filesDir)
                isRunning = false
            }
        },
        onPickFile = {
            openDocument.launch(arrayOf("*/*"))
        },
        onRequestTermuxPermission = {
            runCatching {
                context.startActivity(TermuxCommandRunner.requestPermissionIntent(context))
            }.onFailure {
                logs = logs + "[Termux] İzin ekranı açılamadı: ${it.message ?: "bilinmeyen hata"}"
            }
        },
        termuxReady = termuxReady,
        logs = logs,
        isRunning = isRunning,
        selectedFile = selectedFile,
        selectedImage = selectedImage
    )
}

private fun normalizeWorkingDirectory(candidate: File, fallback: File): File {
    return try {
        val dir = if (candidate.isDirectory) candidate else fallback
        if (dir.exists() && dir.canRead() && dir.canExecute()) dir else fallback
    } catch (_: SecurityException) {
        fallback
    }
}
