package com.github.pyvenvmanage

import java.nio.file.Files
import java.nio.file.Path

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import com.intellij.openapi.vfs.VirtualFile

import com.jetbrains.python.sdk.PythonSdkUtil

class VenvUtilsTest {
    @Nested
    inner class GetPyVenvCfgTest {
        private lateinit var virtualFile: VirtualFile
        private lateinit var pyvenvCfgFile: VirtualFile

        @BeforeEach
        fun setUp() {
            mockkStatic(PythonSdkUtil::class)
            virtualFile = mockk(relaxed = true)
            pyvenvCfgFile = mockk(relaxed = true)
        }

        @AfterEach
        fun tearDown() {
            unmockkStatic(PythonSdkUtil::class)
        }

        @Test
        fun `returns null when file is null`() {
            val result = VenvUtils.getPyVenvCfg(null)
            assertNull(result)
        }

        @Test
        fun `returns null when file is not a directory`() {
            every { virtualFile.isDirectory } returns false

            val result = VenvUtils.getPyVenvCfg(virtualFile)

            assertNull(result)
        }

        @Test
        fun `returns null when no Python executable found`() {
            every { virtualFile.isDirectory } returns true
            every { virtualFile.path } returns "/some/path"
            every { PythonSdkUtil.getPythonExecutable("/some/path") } returns null

            val result = VenvUtils.getPyVenvCfg(virtualFile)

            assertNull(result)
        }

        @Test
        fun `returns null when pyvenv cfg not found`() {
            every { virtualFile.isDirectory } returns true
            every { virtualFile.path } returns "/some/venv"
            every { PythonSdkUtil.getPythonExecutable("/some/venv") } returns "/some/venv/bin/python"
            every { virtualFile.findChild("pyvenv.cfg") } returns null

            val result = VenvUtils.getPyVenvCfg(virtualFile)

            assertNull(result)
        }

        @Test
        fun `returns path when venv directory with pyvenv cfg`() {
            every { virtualFile.isDirectory } returns true
            every { virtualFile.path } returns "/some/venv"
            every { PythonSdkUtil.getPythonExecutable("/some/venv") } returns "/some/venv/bin/python"
            every { virtualFile.findChild("pyvenv.cfg") } returns pyvenvCfgFile
            every { pyvenvCfgFile.path } returns "/some/venv/pyvenv.cfg"

            val result = VenvUtils.getPyVenvCfg(virtualFile)

            assertNotNull(result)
            assertEquals(Path.of("/some/venv/pyvenv.cfg"), result)
        }
    }

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

    @Nested
    inner class GetVenvInfoTest {
        @TempDir
        lateinit var tempDir: Path

        private lateinit var pyvenvCfgPath: Path

        @BeforeEach
        fun setUp() {
            pyvenvCfgPath = tempDir.resolve("pyvenv.cfg")
        }

        @Test
        fun `returns info when version and implementation present`() {
            Files.writeString(
                pyvenvCfgPath,
                """
                home = /usr/bin
                version = 3.11.5
                implementation = CPython
                include-system-site-packages = false
                """.trimIndent(),
            )

            val result = VenvUtils.getVenvInfo(pyvenvCfgPath)

            assertNotNull(result)
            assertEquals("3.11.5", result?.version)
            assertEquals("CPython", result?.implementation)
            assertEquals(false, result?.includeSystemSitePackages)
            assertNull(result?.creator)
        }

        @Test
        fun `returns info with null implementation when implementation missing`() {
            Files.writeString(
                pyvenvCfgPath,
                """
                home = /usr/bin
                version = 3.11.5
                """.trimIndent(),
            )

            val result = VenvUtils.getVenvInfo(pyvenvCfgPath)

            assertNotNull(result)
            assertEquals("3.11.5", result?.version)
            assertNull(result?.implementation)
            assertEquals(false, result?.includeSystemSitePackages)
            assertNull(result?.creator)
        }

        @Test
        fun `returns info with system site packages enabled`() {
            Files.writeString(
                pyvenvCfgPath,
                """
                home = /usr/bin
                version = 3.11.5
                implementation = CPython
                include-system-site-packages = true
                """.trimIndent(),
            )

            val result = VenvUtils.getVenvInfo(pyvenvCfgPath)

            assertNotNull(result)
            assertEquals("3.11.5", result?.version)
            assertEquals("CPython", result?.implementation)
            assertEquals(true, result?.includeSystemSitePackages)
            assertNull(result?.creator)
        }

        @Test
        fun `uses version_info when version missing`() {
            Files.writeString(
                pyvenvCfgPath,
                """
                home = /usr/bin
                version_info = 3.13.10.final.0
                implementation = CPython
                """.trimIndent(),
            )

            val result = VenvUtils.getVenvInfo(pyvenvCfgPath)

            assertNotNull(result)
            assertEquals("3.13.10.final.0", result?.version)
            assertEquals("CPython", result?.implementation)
            assertNull(result?.creator)
        }

        @Test
        fun `returns null when version missing`() {
            Files.writeString(
                pyvenvCfgPath,
                """
                home = /usr/bin
                implementation = CPython
                """.trimIndent(),
            )

            val result = VenvUtils.getVenvInfo(pyvenvCfgPath)

            assertNull(result)
        }

        @Test
        fun `returns null when file does not exist`() {
            val nonExistentPath = tempDir.resolve("nonexistent.cfg")

            val result = VenvUtils.getVenvInfo(nonExistentPath)

            assertNull(result)
        }

        @Test
        fun `trims version and implementation`() {
            Files.writeString(
                pyvenvCfgPath,
                """
                version =   3.12.0
                implementation =  PyPy
                """.trimIndent(),
            )

            val result = VenvUtils.getVenvInfo(pyvenvCfgPath)

            assertNotNull(result)
            assertEquals("3.12.0", result?.version)
            assertEquals("PyPy", result?.implementation)
            assertNull(result?.creator)
        }

        @Test
        fun `returns creator when uv present`() {
            Files.writeString(
                pyvenvCfgPath,
                """
                version = 3.11.5
                implementation = CPython
                uv = 0.9.18
                """.trimIndent(),
            )

            val result = VenvUtils.getVenvInfo(pyvenvCfgPath)

            assertNotNull(result)
            assertEquals("3.11.5", result?.version)
            assertEquals("CPython", result?.implementation)
            assertEquals(" - uv@0.9.18", result?.creator)
        }

        @Test
        fun `returns creator when virtualenv present`() {
            Files.writeString(
                pyvenvCfgPath,
                """
                version = 3.11.5
                implementation = CPython
                virtualenv = 20.35.4
                """.trimIndent(),
            )

            val result = VenvUtils.getVenvInfo(pyvenvCfgPath)

            assertNotNull(result)
            assertEquals("3.11.5", result?.version)
            assertEquals("CPython", result?.implementation)
            assertEquals(" - virtualenv@20.35.4", result?.creator)
        }

        @Test
        fun `prefers uv over virtualenv when both present`() {
            Files.writeString(
                pyvenvCfgPath,
                """
                version = 3.11.5
                uv = 0.9.18
                virtualenv = 20.35.4
                """.trimIndent(),
            )

            val result = VenvUtils.getVenvInfo(pyvenvCfgPath)

            assertNotNull(result)
            assertEquals("3.11.5", result?.version)
            assertEquals(" - uv@0.9.18", result?.creator)
        }
    }
}
