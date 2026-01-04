package com.github.pyvenvmanage

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

import com.intellij.openapi.vfs.VirtualFile

import com.jetbrains.python.sdk.PythonSdkUtil

data class VenvInfo(
    val version: String,
    val implementation: String? = null,
    val includeSystemSitePackages: Boolean = false,
    val creator: String? = null,
)

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

    fun getVenvInfo(pyvenvCfgPath: Path): VenvInfo? =
        runCatching {
            val props =
                Properties().apply {
                    Files.newBufferedReader(pyvenvCfgPath, StandardCharsets.UTF_8).use { load(it) }
                }
            val version = props.getProperty("version") ?: props.getProperty("version_info")
            if (version == null) {
                com.intellij.openapi.diagnostic.Logger
                    .getInstance(VenvUtils::class.java)
                    .debug("No version found in $pyvenvCfgPath")
                return@runCatching null
            }
            val implementation = props.getProperty("implementation")
            val includeSystemSitePackages =
                props.getProperty("include-system-site-packages")?.toBoolean() ?: false
            val creator =
                props.getProperty("uv")?.let { " - uv@$it" }
                    ?: props.getProperty("virtualenv")?.let { " - virtualenv@$it" }
            com.intellij.openapi.diagnostic.Logger
                .getInstance(VenvUtils::class.java)
                .debug(
                    "Found venv info: version=$version, implementation=$implementation, systemSitePackages=$includeSystemSitePackages, creator=$creator from $pyvenvCfgPath",
                )
            VenvInfo(version, implementation, includeSystemSitePackages, creator)
        }.getOrNull()
}
