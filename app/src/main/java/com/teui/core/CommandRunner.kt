package com.teui.core

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class CommandResult(
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val durationMs: Long,
    val workingDirectory: File,
)

object CommandRunner {
    private val lock = Mutex()

    private var shellProcess: Process? = null
    private var shellInput: BufferedWriter? = null
    private var shellOutput: BufferedReader? = null
    private var activeWorkingDirectory: File? = null

    suspend fun run(command: String, workingDirectory: File): CommandResult = withContext(Dispatchers.IO) {
        lock.withLock {
            val startedAt = System.currentTimeMillis()
            ensureShell(workingDirectory)

            val marker = "TEUI_${System.nanoTime()}"
            val startMarker = "__${marker}_START__"
            val pwdPrefix = "__${marker}_PWD__"
            val exitPrefix = "__${marker}_EXIT__"

            writeCommand(command, startMarker, pwdPrefix, exitPrefix)

            val output = StringBuilder()
            var didStart = false
            var exitCode = 0
            var resolvedDirectory = activeWorkingDirectory ?: workingDirectory

            while (true) {
                val line = shellOutput?.readLine() ?: throw IOException("Shell oturumu beklenmedik şekilde kapandı")

                when {
                    !didStart && line == startMarker -> {
                        didStart = true
                    }

                    didStart && line.startsWith(pwdPrefix) -> {
                        val nextPath = line.removePrefix(pwdPrefix).trim()
                        if (nextPath.isNotEmpty()) {
                            resolvedDirectory = File(nextPath)
                        }
                    }

                    didStart && line.startsWith(exitPrefix) -> {
                        exitCode = line.removePrefix(exitPrefix).trim().toIntOrNull() ?: 1
                        break
                    }

                    didStart -> {
                        if (output.isNotEmpty()) output.append('\n')
                        output.append(line)
                    }
                }
            }

            val nextDirectory = if (resolvedDirectory.exists() && resolvedDirectory.isDirectory) {
                resolvedDirectory
            } else {
                workingDirectory
            }

            activeWorkingDirectory = nextDirectory

            CommandResult(
                stdout = output.toString().trimEnd(),
                stderr = "",
                exitCode = exitCode,
                durationMs = System.currentTimeMillis() - startedAt,
                workingDirectory = nextDirectory,
            )
        }
    }

    private fun ensureShell(initialDirectory: File) {
        if (shellProcess?.isAlive == true) return

        shellProcess?.destroy()

        val process = ProcessBuilder("sh")
            .directory(initialDirectory)
            .redirectErrorStream(true)
            .start()

        shellProcess = process
        shellInput = process.outputStream.bufferedWriter()
        shellOutput = process.inputStream.bufferedReader()
        activeWorkingDirectory = initialDirectory
    }

    private fun writeCommand(command: String, startMarker: String, pwdPrefix: String, exitPrefix: String) {
        val input = shellInput ?: throw IOException("Shell stdin hazır değil")

        input.write("printf '%s\\n' '$startMarker'\n")
        input.write("{\n")
        input.write(command)
        input.write("\n}\n")
        input.write("__teui_exit=\$?\n")
        input.write("printf '%s%s\\n' '$pwdPrefix' \"\$(pwd)\"\n")
        input.write("printf '%s%s\\n' '$exitPrefix' \"\$__teui_exit\"\n")
        input.flush()
    }
}
