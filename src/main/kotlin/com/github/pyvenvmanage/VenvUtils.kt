package com.github.pyvenvmanage

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

import com.intellij.openapi.vfs.VirtualFile

import com.jetbrains.python.sdk.PythonSdkUtil

object VenvUtils {
    fun getPyVenvCfg(file: VirtualFile?): Path? =
        file
            ?.takeIf { it.isDirectory }
            ?.takeIf { PythonSdkUtil.getPythonExecutable(it.path) != null }
            ?.findChild("pyvenv.cfg")
            ?.let { Path.of(it.path) }

    fun getPythonVersionFromPyVenv(pyvenvCfgPath: Path): String? =
        runCatching {
            Properties()
                .apply {
                    Files.newBufferedReader(pyvenvCfgPath, StandardCharsets.UTF_8).use { load(it) }
                }.getProperty("version")
                ?.trim()
        }.getOrNull()
}
