package com.teui.core

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CommandResult(
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val durationMs: Long,
)

object CommandRunner {
    suspend fun run(command: String, workingDirectory: File): CommandResult = withContext(Dispatchers.IO) {
        val startedAt = System.currentTimeMillis()

        val processBuilder = ProcessBuilder("sh", "-c", command)
            .redirectErrorStream(false)
            .directory(workingDirectory)

        processBuilder.environment()["HOME"] = workingDirectory.absolutePath
        processBuilder.environment()["PWD"] = workingDirectory.absolutePath

        val process = processBuilder.start()

        val stdout = process.inputStream.bufferedReader().use { it.readText() }.trimEnd()
        val stderr = process.errorStream.bufferedReader().use { it.readText() }.trimEnd()
        val exitCode = process.waitFor()
        val durationMs = System.currentTimeMillis() - startedAt

        CommandResult(
            stdout = stdout,
            stderr = stderr,
            exitCode = exitCode,
            durationMs = durationMs,
        )
    }
}
