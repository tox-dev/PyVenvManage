package com.github.pyvenvmanage.sdk

import java.nio.file.Files
import java.nio.file.Path

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class EnvironmentDetectorTest {
    @TempDir
    lateinit var tempDir: Path

    private lateinit var venvRoot: Path
    private lateinit var binDir: Path
    private lateinit var pythonExe: Path

    @BeforeEach
    fun setUp() {
        venvRoot = tempDir.resolve("venv")
        binDir = venvRoot.resolve("bin")
        pythonExe = binDir.resolve("python")

        binDir.createDirectories()
        Files.createFile(pythonExe)
    }

    @Test
    fun `detects UV environment from pyvenv cfg`() {
        val pyvenvCfg = venvRoot.resolve("pyvenv.cfg")
        pyvenvCfg.writeText(
            """
            home = /usr/bin
            uv = 0.1.0
            """.trimIndent(),
        )

        val result = EnvironmentDetector.detectEnvironmentType(pythonExe.toString())

        assertEquals(PythonEnvironmentType.UV, result)
    }

    @Test
    fun `detects conda environment from conda-meta directory`() {
        venvRoot.resolve("conda-meta").createDirectories()

        val result = EnvironmentDetector.detectEnvironmentType(pythonExe.toString())

        assertEquals(PythonEnvironmentType.CONDA, result)
    }

    @Test
    fun `detects conda from parent conda-meta directory`() {
        venvRoot.parent.resolve("conda-meta").createDirectories()

        val result = EnvironmentDetector.detectEnvironmentType(pythonExe.toString())

        assertEquals(PythonEnvironmentType.CONDA, result)
    }

    @Test
    fun `detects Hatch from gitignore marker`() {
        venvRoot.resolve(".gitignore").writeText("# This file was automatically created by Hatch\n*\n")

        val result = EnvironmentDetector.detectEnvironmentType(pythonExe.toString())

        assertEquals(PythonEnvironmentType.HATCH, result)
    }

    @Test
    fun `detects virtualenv from pyvenv cfg`() {
        venvRoot.resolve("pyvenv.cfg").writeText("home = /usr/bin")

        val result = EnvironmentDetector.detectEnvironmentType(pythonExe.toString())

        assertEquals(PythonEnvironmentType.VIRTUALENV, result)
    }

    @Test
    fun `returns SYSTEM when no parent directory`() {
        val result = EnvironmentDetector.detectEnvironmentType("/python")

        assertEquals(PythonEnvironmentType.SYSTEM, result)
    }

    @Test
    fun `returns SYSTEM when not a venv`() {
        val result = EnvironmentDetector.detectEnvironmentType(pythonExe.toString())

        assertEquals(PythonEnvironmentType.SYSTEM, result)
    }

    @Test
    fun `UV takes precedence over virtualenv`() {
        val pyvenvCfg = venvRoot.resolve("pyvenv.cfg")
        pyvenvCfg.writeText("home = /usr/bin\nuv = 0.1.0")

        val result = EnvironmentDetector.detectEnvironmentType(pythonExe.toString())

        assertEquals(PythonEnvironmentType.UV, result)
    }

    @Test
    fun `conda takes precedence over virtualenv`() {
        venvRoot.resolve("conda-meta").createDirectories()
        venvRoot.resolve("pyvenv.cfg").writeText("home = /usr/bin")

        val result = EnvironmentDetector.detectEnvironmentType(pythonExe.toString())

        assertEquals(PythonEnvironmentType.CONDA, result)
    }

    @Test
    fun `pyvenv cfg without uv marker is virtualenv`() {
        venvRoot.resolve("pyvenv.cfg").writeText("home = /usr/bin\nversion = 3.14")

        val result = EnvironmentDetector.detectEnvironmentType(pythonExe.toString())

        assertEquals(PythonEnvironmentType.VIRTUALENV, result)
    }

    @Test
    fun `missing pyvenv cfg is SYSTEM`() {
        val result = EnvironmentDetector.detectEnvironmentType(pythonExe.toString())

        assertEquals(PythonEnvironmentType.SYSTEM, result)
    }

    @Test
    fun `gitignore without Hatch marker falls back to virtualenv`() {
        venvRoot.resolve(".gitignore").writeText("*.pyc\n__pycache__/\n")
        venvRoot.resolve("pyvenv.cfg").writeText("home = /usr/bin")

        val result = EnvironmentDetector.detectEnvironmentType(pythonExe.toString())

        assertEquals(PythonEnvironmentType.VIRTUALENV, result)
    }

    @Test
    fun `detects Pipenv from workon home`() {
        val workonDir = tempDir.resolve("workon-envs")
        val pipenvVenv = workonDir.resolve("myproject-abc123")
        val pipenvBin = pipenvVenv.resolve("bin")
        pipenvBin.createDirectories()
        val pipenvPython = pipenvBin.resolve("python")
        Files.createFile(pipenvPython)

        mockkStatic(System::class)
        every { System.getenv("WORKON_HOME") } returns workonDir.toString()
        every { System.getenv("HOME") } returns tempDir.toString()
        every { System.getenv("USERPROFILE") } returns null
        every { System.getenv("POETRY_CACHE_DIR") } returns null
        every { System.getenv("POETRY_CONFIG_DIR") } returns null
        every { System.getenv("HATCH_DATA_DIR") } returns null
        every { System.getenv("XDG_CACHE_HOME") } returns null
        every { System.getenv("XDG_CONFIG_HOME") } returns null
        every { System.getenv("XDG_DATA_HOME") } returns null
        every { System.getenv("LOCALAPPDATA") } returns null
        every { System.getenv("APPDATA") } returns null

        try {
            val detector = EnvironmentDetector
            val method = detector::class.java.getDeclaredMethod("computePipenvDirs")
            method.isAccessible = true
            val dirs = method.invoke(detector) as List<*>
            assert(dirs.any { pipenvVenv.startsWith(it as Path) })
        } finally {
            unmockkStatic(System::class)
        }
    }
}
