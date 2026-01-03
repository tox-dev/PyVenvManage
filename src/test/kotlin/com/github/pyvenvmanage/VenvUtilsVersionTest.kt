package com.github.pyvenvmanage

import java.nio.file.Files

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Tests for VenvUtils.getPythonVersionFromPyVenv
 * VenvVersionCache is tested via UI tests since it requires IntelliJ Application context
 */
class VenvUtilsVersionTest {
    @TempDir
    lateinit var tempDir: java.nio.file.Path

    @Test
    fun `getPythonVersionFromPyVenv returns version from pyvenv cfg`() {
        val pyvenvCfg = tempDir.resolve("pyvenv.cfg")
        Files.writeString(pyvenvCfg, "version = 3.11.5\nhome = /usr/bin")

        val version = VenvUtils.getPythonVersionFromPyVenv(pyvenvCfg)

        assertEquals("3.11.5", version)
    }

    @Test
    fun `getPythonVersionFromPyVenv trims whitespace`() {
        val pyvenvCfg = tempDir.resolve("pyvenv.cfg")
        Files.writeString(pyvenvCfg, "version =   3.11.5   \nhome = /usr/bin")

        val version = VenvUtils.getPythonVersionFromPyVenv(pyvenvCfg)

        assertEquals("3.11.5", version)
    }

    @Test
    fun `getPythonVersionFromPyVenv returns null for missing file`() {
        val pyvenvCfg = tempDir.resolve("nonexistent").resolve("pyvenv.cfg")

        val version = VenvUtils.getPythonVersionFromPyVenv(pyvenvCfg)

        assertNull(version)
    }

    @Test
    fun `getPythonVersionFromPyVenv returns null for file without version`() {
        val pyvenvCfg = tempDir.resolve("pyvenv.cfg")
        Files.writeString(pyvenvCfg, "home = /usr/bin\ninclude-system-site-packages = false")

        val version = VenvUtils.getPythonVersionFromPyVenv(pyvenvCfg)

        assertNull(version)
    }

    @Test
    fun `getPythonVersionFromPyVenv handles complex pyvenv cfg`() {
        val pyvenvCfg = tempDir.resolve("pyvenv.cfg")
        Files.writeString(
            pyvenvCfg,
            """
            home = /usr/local/bin
            include-system-site-packages = false
            version = 3.12.0
            executable = /usr/local/bin/python3.12
            """.trimIndent(),
        )

        val version = VenvUtils.getPythonVersionFromPyVenv(pyvenvCfg)

        assertEquals("3.12.0", version)
    }
}
