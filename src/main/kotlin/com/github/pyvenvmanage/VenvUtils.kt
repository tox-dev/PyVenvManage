package com.github.pyvenvmanage

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

import com.intellij.openapi.vfs.VirtualFile

import com.jetbrains.python.sdk.PythonSdkUtil

object VenvUtils {
    @JvmStatic
    fun getPyVenvCfg(file: VirtualFile?): Path? {
        if (file != null && file.isDirectory()) {
            val venvRootPath = file.getPath()
            val pythonExecutable = PythonSdkUtil.getPythonExecutable(venvRootPath)
            if (pythonExecutable != null) {
                val pyvenvFile = file.findChild("pyvenv.cfg")
                if (pyvenvFile != null) {
                    return Path.of(pyvenvFile.getPath())
                }
            }
        }
        return null
    }

    @JvmStatic
    fun getPythonVersionFromPyVenv(pyvenvCfgPath: Path): String? {
        val props = Properties()

        try {
            Files.newBufferedReader(pyvenvCfgPath, StandardCharsets.UTF_8).use { reader ->
                props.load(reader)
            }
        } catch (e: IOException) {
            return null // file could not be read
        }

        val version = props.getProperty("version")
        if (version != null) {
            return version.trim { it <= ' ' }
        }

        return null
    }
}
