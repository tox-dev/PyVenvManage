package com.github.pyvenvmanage.sdk

import java.nio.file.Path

import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.pathString
import kotlin.io.path.readText

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread

enum class PythonEnvironmentType {
    UV,
    CONDA,
    POETRY,
    HATCH,
    PIPENV,
    VIRTUALENV,
    SYSTEM,
}

object EnvironmentDetector {
    private val LOG = Logger.getInstance(EnvironmentDetector::class.java)

    private val poetryDirsCache = lazy { computePoetryDirs() }
    private val hatchDirsCache = lazy { computeHatchDirs() }
    private val pipenvDirsCache = lazy { computePipenvDirs() }

    @RequiresBackgroundThread(generateAssertion = false)
    fun detectEnvironmentType(pythonExecutablePath: String): PythonEnvironmentType {
        LOG.info("Detecting environment type for: $pythonExecutablePath")
        val executablePath = Path.of(pythonExecutablePath)
        val binDir = executablePath.parent
        if (binDir == null) {
            LOG.info("No bin directory, returning SYSTEM")
            return PythonEnvironmentType.SYSTEM
        }
        val venvRoot = binDir.parent
        if (venvRoot == null) {
            LOG.info("No venv root, returning SYSTEM")
            return PythonEnvironmentType.SYSTEM
        }

        LOG.info("Python executable: $pythonExecutablePath")
        LOG.info("Bin directory: $binDir")
        LOG.info("Venv root: $venvRoot")

        val type =
            when {
                isUv(venvRoot).also { LOG.info("isUv: $it") } -> {
                    PythonEnvironmentType.UV
                }

                isConda(venvRoot).also { LOG.info("isConda: $it") } -> {
                    PythonEnvironmentType.CONDA
                }

                isPoetry(venvRoot).also { LOG.info("isPoetry: $it") } -> {
                    PythonEnvironmentType.POETRY
                }

                isHatch(venvRoot).also { LOG.info("isHatch: $it") } -> {
                    PythonEnvironmentType.HATCH
                }

                isPipenv(venvRoot).also { LOG.info("isPipenv: $it") } -> {
                    PythonEnvironmentType.PIPENV
                }

                isVirtualEnv(venvRoot).also { LOG.info("isVirtualEnv: $it") } -> {
                    PythonEnvironmentType.VIRTUALENV
                }

                else -> {
                    PythonEnvironmentType.SYSTEM
                }
            }

        LOG.info("Final detected type: $type")
        return type
    }

    private fun isUv(venvRoot: Path): Boolean {
        val pyvenvCfg = venvRoot.resolve("pyvenv.cfg")
        if (!pyvenvCfg.exists()) {
            return false
        }
        return try {
            val content = pyvenvCfg.readText()
            content.contains("uv = ")
        } catch (e: Exception) {
            LOG.warn("Failed to read pyvenv.cfg", e)
            false
        }
    }

    private fun isConda(venvRoot: Path): Boolean =
        venvRoot.resolve("conda-meta").isDirectory() || venvRoot.parent?.resolve("conda-meta")?.isDirectory() == true

    private fun isPoetry(venvRoot: Path): Boolean {
        val dirs = poetryDirsCache.value
        LOG.info("Checking Poetry directories (cached): ${dirs.map { it.pathString }}")
        return dirs.any { venvRoot.startsWith(it) }
    }

    private fun isHatch(venvRoot: Path): Boolean {
        val gitignore = venvRoot.resolve(".gitignore")
        if (gitignore.exists()) {
            try {
                if (gitignore.readText().contains("# This file was automatically created by Hatch")) {
                    LOG.info("Found Hatch marker in .gitignore")
                    return true
                }
            } catch (e: Exception) {
                LOG.warn("Failed to read .gitignore", e)
            }
        }

        val dirs = hatchDirsCache.value
        LOG.info("Checking Hatch directories (cached): ${dirs.map { it.pathString }}")
        return dirs.any { venvRoot.startsWith(it) }
    }

    private fun isPipenv(venvRoot: Path): Boolean {
        val dirs = pipenvDirsCache.value
        LOG.info("Checking Pipenv directories (cached): ${dirs.map { it.pathString }}")
        return dirs.any { venvRoot.startsWith(it) }
    }

    private fun isVirtualEnv(venvRoot: Path): Boolean = venvRoot.resolve("pyvenv.cfg").exists()

    private fun computePoetryDirs(): List<Path> {
        val dirs = mutableListOf<Path>()

        System.getenv("POETRY_CACHE_DIR")?.let { dirs.add(Path.of(it, "virtualenvs")) }

        getPoetryConfigPath()?.let { configPath ->
            if (configPath.exists()) {
                try {
                    val config = configPath.readText()
                    Regex("""virtualenvs\.path\s*=\s*"([^"]+)"""").find(config)?.groupValues?.get(1)?.let {
                        dirs.add(Path.of(it).toAbsolutePath().normalize())
                    }
                } catch (e: Exception) {
                    LOG.warn("Failed to read Poetry config at $configPath", e)
                }
            }
        }

        dirs.addAll(getPoetryDefaultPaths())
        return dirs.distinct()
    }

    private fun computeHatchDirs(): List<Path> {
        val dirs = mutableListOf<Path>()

        System.getenv("HATCH_DATA_DIR")?.let { dirs.add(Path.of(it, "env", "virtual")) }

        dirs.addAll(getHatchDefaultPaths())
        return dirs.distinct()
    }

    private fun computePipenvDirs(): List<Path> {
        val dirs = mutableListOf<Path>()

        System.getenv("WORKON_HOME")?.let { dirs.add(Path.of(it)) }

        dirs.addAll(getPipenvDefaultPaths())
        return dirs.distinct()
    }

    private fun getPoetryConfigPath(): Path? {
        System.getenv("POETRY_CONFIG_DIR")?.let { return Path.of(it, "config.toml") }

        return when {
            SystemInfo.isWindows -> {
                System.getenv("APPDATA")?.let { Path.of(it, "pypoetry", "config.toml") }
            }

            else -> {
                val xdgConfig = System.getenv("XDG_CONFIG_HOME")
                val home = System.getenv("HOME")
                when {
                    xdgConfig != null -> Path.of(xdgConfig, "pypoetry", "config.toml")
                    home != null -> Path.of(home, ".config", "pypoetry", "config.toml")
                    else -> null
                }
            }
        }
    }

    private fun getPoetryDefaultPaths(): List<Path> {
        val home = System.getenv("HOME") ?: System.getenv("USERPROFILE") ?: return emptyList()

        return when {
            SystemInfo.isWindows -> {
                val localAppData = System.getenv("LOCALAPPDATA") ?: "$home\\AppData\\Local"
                listOf(Path.of(localAppData, "pypoetry", "Cache", "virtualenvs"))
            }

            SystemInfo.isMac -> {
                listOf(Path.of(home, "Library", "Caches", "pypoetry", "virtualenvs"))
            }

            else -> {
                val xdgCache = System.getenv("XDG_CACHE_HOME") ?: "$home/.cache"
                listOf(Path.of(xdgCache, "pypoetry", "virtualenvs"))
            }
        }
    }

    private fun getHatchDefaultPaths(): List<Path> {
        val home = System.getenv("HOME") ?: System.getenv("USERPROFILE") ?: return emptyList()

        return when {
            SystemInfo.isWindows -> {
                val localAppData = System.getenv("LOCALAPPDATA") ?: "$home\\AppData\\Local"
                listOf(Path.of(localAppData, "hatch", "env", "virtual"))
            }

            SystemInfo.isMac -> {
                listOf(Path.of(home, "Library", "Application Support", "hatch", "env", "virtual"))
            }

            else -> {
                val xdgData = System.getenv("XDG_DATA_HOME") ?: "$home/.local/share"
                listOf(Path.of(xdgData, "hatch", "env", "virtual"))
            }
        }
    }

    private fun getPipenvDefaultPaths(): List<Path> {
        val home = System.getenv("HOME") ?: System.getenv("USERPROFILE") ?: return emptyList()

        return when {
            SystemInfo.isWindows -> {
                listOf(Path.of(home, ".virtualenvs"))
            }

            else -> {
                val xdgData = System.getenv("XDG_DATA_HOME") ?: "$home/.local/share"
                listOf(Path.of(xdgData, "virtualenvs"))
            }
        }
    }
}
