package com.github.pyvenvmanage

import java.nio.file.Files
import java.nio.file.Path

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class VenvUtilsTest {
    @Nested
    inner class GetPythonVersionFromPyVenvTest {
        @TempDir
        lateinit var tempDir: Path

        private lateinit var pyvenvCfgPath: Path

        @BeforeEach
        fun setUp() {
            pyvenvCfgPath = tempDir.resolve("pyvenv.cfg")
        }

        @Test
        fun `returns version when present`() {
            Files.writeString(
                pyvenvCfgPath,
                """
                home = /usr/bin
                include-system-site-packages = false
                version = 3.11.5
                """.trimIndent(),
            )

            val result = VenvUtils.getPythonVersionFromPyVenv(pyvenvCfgPath)

            assertEquals("3.11.5", result)
        }

        @Test
        fun `returns trimmed version`() {
            Files.writeString(
                pyvenvCfgPath,
                """
                version =   3.12.0
                """.trimIndent(),
            )

            val result = VenvUtils.getPythonVersionFromPyVenv(pyvenvCfgPath)

            assertEquals("3.12.0", result)
        }

        @Test
        fun `returns null when version key missing`() {
            Files.writeString(
                pyvenvCfgPath,
                """
                home = /usr/bin
                include-system-site-packages = false
                """.trimIndent(),
            )

            val result = VenvUtils.getPythonVersionFromPyVenv(pyvenvCfgPath)

            assertNull(result)
        }

        @Test
        fun `returns null when file does not exist`() {
            val nonExistentPath = tempDir.resolve("nonexistent.cfg")

            val result = VenvUtils.getPythonVersionFromPyVenv(nonExistentPath)

            assertNull(result)
        }

        @Test
        fun `returns null for empty file`() {
            Files.writeString(pyvenvCfgPath, "")

            val result = VenvUtils.getPythonVersionFromPyVenv(pyvenvCfgPath)

            assertNull(result)
        }

        @Test
        fun `handles version with extra info`() {
            Files.writeString(
                pyvenvCfgPath,
                """
                version = 3.10.12
                version_info = 3.10.12.final.0
                """.trimIndent(),
            )

            val result = VenvUtils.getPythonVersionFromPyVenv(pyvenvCfgPath)

            assertEquals("3.10.12", result)
        }

        @Test
        fun `handles equals sign in value`() {
            Files.writeString(
                pyvenvCfgPath,
                """
                version = 3.9.0
                prompt = (venv) = test
                """.trimIndent(),
            )

            val result = VenvUtils.getPythonVersionFromPyVenv(pyvenvCfgPath)

            assertEquals("3.9.0", result)
        }
    }
}
