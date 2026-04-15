package com.teui.core

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CommandResult(
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val durationMs: Long,
    val workingDirectory: File,
)

object CommandRunner {
    suspend fun run(command: String, workingDirectory: File): CommandResult = withContext(Dispatchers.IO) {
        val startedAt = System.currentTimeMillis()
        val marker = "__TEUI_PWD_MARKER_${System.nanoTime()}__"

        val wrappedCommand = """
            $command
            __teui_exit=$?
            printf '\n${marker}%s\n' "$(pwd)"
            exit $__teui_exit
        """.trimIndent()

        val processBuilder = ProcessBuilder("sh", "-c", wrappedCommand)
            .redirectErrorStream(false)
            .directory(workingDirectory)

        processBuilder.environment()["HOME"] = workingDirectory.absolutePath
        processBuilder.environment()["PWD"] = workingDirectory.absolutePath

        val process = processBuilder.start()

        val rawStdout = process.inputStream.bufferedReader().use { it.readText() }
        val stderr = process.errorStream.bufferedReader().use { it.readText() }.trimEnd()
        val exitCode = process.waitFor()
        val durationMs = System.currentTimeMillis() - startedAt

        val markerIndex = rawStdout.lastIndexOf(marker)

        val (stdout, nextDir) = if (markerIndex >= 0) {
            val pathPart = rawStdout.substring(markerIndex + marker.length)
            val resolvedPath = pathPart.substringBefore('\n').trim()
            val cleanedStdout = rawStdout.substring(0, markerIndex).trimEnd()
            val resolvedDir = if (resolvedPath.isNotBlank()) File(resolvedPath) else workingDirectory
            cleanedStdout to resolvedDir
        } else {
            rawStdout.trimEnd() to workingDirectory
        }

        CommandResult(
            stdout = stdout,
            stderr = stderr,
            exitCode = exitCode,
            durationMs = durationMs,
            workingDirectory = nextDir,
        )
    }
}
