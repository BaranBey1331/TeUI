package com.teui.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CommandResult(
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val durationMs: Long,
)

object CommandRunner {
    suspend fun run(command: String): CommandResult = withContext(Dispatchers.IO) {
        val startedAt = System.currentTimeMillis()

        val process = ProcessBuilder("sh", "-c", command)
            .redirectErrorStream(false)
            .start()

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
