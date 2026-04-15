package com.teui.core

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TermuxCommandRunner {
    private const val TERMUX_PACKAGE = "com.termux"
    private const val RUN_COMMAND_ACTION = "com.termux.RUN_COMMAND"
    private const val RUN_COMMAND_SERVICE = "com.termux.app.RunCommandService"
    private const val RUN_COMMAND_PERMISSION = "com.termux.permission.RUN_COMMAND"

    private const val EXTRA_COMMAND_PATH = "com.termux.RUN_COMMAND_PATH"
    private const val EXTRA_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
    private const val EXTRA_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
    private const val EXTRA_RUNNER = "com.termux.RUN_COMMAND_RUNNER"
    private const val EXTRA_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
    private const val EXTRA_SHELL_NAME = "com.termux.RUN_COMMAND_SHELL_NAME"
    private const val EXTRA_SHELL_CREATE_MODE = "com.termux.RUN_COMMAND_SHELL_CREATE_MODE"
    private const val EXTRA_PENDING_INTENT = "com.termux.RUN_COMMAND_PENDING_INTENT"

    private const val EXTRA_PLUGIN_RESULT_BUNDLE = "result"
    private const val EXTRA_PLUGIN_STDOUT = "stdout"
    private const val EXTRA_PLUGIN_STDERR = "stderr"
    private const val EXTRA_PLUGIN_EXIT_CODE = "exitCode"
    private const val EXTRA_PLUGIN_ERRMSG = "errmsg"

    private const val RUNNER_APP_SHELL = "app-shell"
    private const val SHELL_CREATE_MODE_NO_SHELL_WITH_NAME = "no-shell-with-name"

    private const val TERMUX_SHELL_NAME = "teui-shell"
    private const val RESULT_TIMEOUT_MS = 45_000L

    private val requestCounter = AtomicInteger(1000)

    suspend fun run(command: String, workingDirectory: File, context: Context): CommandResult = withContext(Dispatchers.IO) {
        val setupIssue = getSetupIssue(context)
        if (setupIssue != null) {
            return@withContext CommandResult(
                stdout = "",
                stderr = setupIssue,
                exitCode = 1,
                durationMs = 0,
                workingDirectory = workingDirectory,
            )
        }

        val startedAt = System.currentTimeMillis()
        val marker = "TEUI_TERMUX_${System.nanoTime()}"
        val pwdPrefix = "__${marker}_PWD__"

        val wrappedCommand = """
            $command
            __teui_exit=${'$'}?
            printf '\n${pwdPrefix}%s\n' "${'$'}(pwd)"
            exit ${'$'}__teui_exit
        """.trimIndent()

        val requestCode = requestCounter.incrementAndGet()
        val callbackAction = "${context.packageName}.TERMUX_RESULT.$requestCode"

        val receiverResult = ReceiverResult()
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent != null) receiverResult.intent = intent
                receiverResult.notified = true
            }
        }

        context.registerReceiver(receiver, IntentFilter(callbackAction), Context.RECEIVER_NOT_EXPORTED)

        try {
            val callbackIntent = Intent(callbackAction).setPackage(context.packageName)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                callbackIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
            )

            val runIntent = Intent(RUN_COMMAND_ACTION).setClassName(TERMUX_PACKAGE, RUN_COMMAND_SERVICE).apply {
                putExtra(EXTRA_COMMAND_PATH, "/data/data/com.termux/files/usr/bin/sh")
                putExtra(EXTRA_ARGUMENTS, arrayOf("-lc", wrappedCommand))
                putExtra(EXTRA_WORKDIR, workingDirectory.absolutePath)
                putExtra(EXTRA_RUNNER, RUNNER_APP_SHELL)
                putExtra(EXTRA_BACKGROUND, true)
                putExtra(EXTRA_SHELL_NAME, TERMUX_SHELL_NAME)
                putExtra(EXTRA_SHELL_CREATE_MODE, SHELL_CREATE_MODE_NO_SHELL_WITH_NAME)
                putExtra(EXTRA_PENDING_INTENT, pendingIntent)
            }

            val started = context.startService(runIntent)
            if (started == null) {
                return@withContext CommandResult(
                    stdout = "",
                    stderr = "Termux RUN_COMMAND servisi başlatılamadı.",
                    exitCode = 1,
                    durationMs = System.currentTimeMillis() - startedAt,
                    workingDirectory = workingDirectory,
                )
            }

            val responseIntent = waitForResult(receiverResult)
            if (responseIntent == null) {
                return@withContext CommandResult(
                    stdout = "",
                    stderr = "Termux komut sonucu zaman aşımına uğradı.",
                    exitCode = 124,
                    durationMs = System.currentTimeMillis() - startedAt,
                    workingDirectory = workingDirectory,
                )
            }

            val bundle = responseIntent.bundle(EXTRA_PLUGIN_RESULT_BUNDLE)
            val rawStdout = bundle?.getString(EXTRA_PLUGIN_STDOUT).orEmpty()
            val stderrBase = bundle?.getString(EXTRA_PLUGIN_STDERR).orEmpty()
            val errMsg = bundle?.getString(EXTRA_PLUGIN_ERRMSG).orEmpty()
            val exitCode = bundle?.getInt(EXTRA_PLUGIN_EXIT_CODE, 1) ?: 1

            val markerIndex = rawStdout.lastIndexOf(pwdPrefix)
            val (stdout, nextDir) = if (markerIndex >= 0) {
                val pathPart = rawStdout.substring(markerIndex + pwdPrefix.length)
                val resolvedPath = pathPart.substringBefore('\n').trim()
                val cleanedStdout = rawStdout.substring(0, markerIndex).trimEnd()
                val resolvedDir = if (resolvedPath.isNotBlank()) File(resolvedPath) else workingDirectory
                cleanedStdout to resolvedDir
            } else {
                rawStdout.trimEnd() to workingDirectory
            }

            val stderr = listOf(stderrBase.trim(), errMsg.trim())
                .filter { it.isNotEmpty() }
                .joinToString(separator = "\n")

            CommandResult(
                stdout = stdout,
                stderr = stderr,
                exitCode = exitCode,
                durationMs = System.currentTimeMillis() - startedAt,
                workingDirectory = nextDir,
            )
        } finally {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    fun requestPermissionIntent(context: Context): Intent {
        return Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun getSetupIssue(context: Context): String? {
        if (!isTermuxInstalled(context)) {
            return "Termux bulunamadı. Lütfen Termux uygulamasını kur."
        }

        val granted = ContextCompat.checkSelfPermission(context, RUN_COMMAND_PERMISSION) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            return "Termux RUN_COMMAND izni yok. Termux'ta allow-external-apps=true ayarla ve izin ver."
        }

        return null
    }

    private fun isTermuxInstalled(context: Context): Boolean {
        val intent = Intent(RUN_COMMAND_ACTION).setClassName(TERMUX_PACKAGE, RUN_COMMAND_SERVICE)
        return context.packageManager.resolveService(intent, 0) != null
    }

    private suspend fun waitForResult(holder: ReceiverResult): Intent? {
        val deadline = System.currentTimeMillis() + RESULT_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            if (holder.notified) return holder.intent
            withContext(Dispatchers.IO) {
                Thread.sleep(50)
            }
        }
        return null
    }

    private fun Intent.bundle(key: String): Bundle? = getBundleExtra(key)

    private data class ReceiverResult(
        var notified: Boolean = false,
        var intent: Intent? = null,
    )
}
