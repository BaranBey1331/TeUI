package com.teui.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.teui.core.PickedFile
import com.teui.core.toReadableSize

@Composable
fun TeUIScreen(
    commandText: String,
    onCommandTextChange: (String) -> Unit,
    onRunCommand: () -> Unit,
    onPickFile: () -> Unit,
    logs: List<String>,
    isRunning: Boolean,
    selectedFile: PickedFile?,
    selectedImage: ImageBitmap?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101114))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "TeUI (Termux UI)",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFFE8EAED),
        )

        Text(
            text = "CLI/TUI komutlarını görsel şekilde çalıştır.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF9AA0A6),
        )

        OutlinedTextField(
            value = commandText,
            onValueChange = onCommandTextChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Komut", color = Color(0xFFC7CCD1)) },
            placeholder = { Text("ör. ls -la", color = Color(0xFF81868C)) },
            singleLine = true,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onRunCommand,
                enabled = !isRunning,
                modifier = Modifier.weight(1f),
            ) {
                if (isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Komutu Çalıştır")
                }
            }

            Button(
                onClick = onPickFile,
                modifier = Modifier.weight(1f),
            ) {
                Text("Dosya/Resim Yükle")
            }
        }

        FilePreviewSection(
            selectedFile = selectedFile,
            selectedImage = selectedImage,
        )

        TerminalLogPanel(logs = logs)
    }
}

@Composable
private fun FilePreviewSection(
    selectedFile: PickedFile?,
    selectedImage: ImageBitmap?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1F24))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Yüklenen İçerik",
            color = Color(0xFFE8EAED),
            style = MaterialTheme.typography.titleSmall,
        )

        if (selectedFile == null) {
            Text(
                text = "Henüz dosya seçilmedi.",
                color = Color(0xFF9AA0A6),
            )
            return
        }

        Text(text = "Ad: ${selectedFile.name}", color = Color(0xFFD2D7DD))
        Text(text = "Tür: ${selectedFile.mimeType ?: "Bilinmiyor"}", color = Color(0xFFD2D7DD))
        Text(
            text = "Boyut: ${selectedFile.sizeBytes?.toReadableSize() ?: "Bilinmiyor"}",
            color = Color(0xFFD2D7DD),
        )

        if (selectedImage != null) {
            Image(
                bitmap = selectedImage,
                contentDescription = "Seçilen resim önizleme",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
        }
    }
}

@Composable
private fun TerminalLogPanel(logs: List<String>) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0C0D10))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            logs.forEach { line ->
                Text(
                    text = line,
                    color = Color(0xFFB8F6C1),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Text(
            text = "Terminal",
            color = Color(0xFF7A838F),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(Color(0xFF0C0D10))
                .padding(horizontal = 4.dp),
        )
    }
}
