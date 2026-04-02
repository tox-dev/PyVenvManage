package com.github.pyvenvmanage.sdk

import java.nio.file.Files
import java.nio.file.Path

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager
import com.intellij.python.community.impl.conda.icons.PythonCommunityImplCondaIcons
import com.intellij.python.community.impl.pipenv.PIPENV_ICON
import com.intellij.python.community.impl.poetry.common.icons.PythonCommunityImplPoetryCommonIcons
import com.intellij.python.community.impl.uv.common.icons.PythonCommunityImplUVCommonIcons
import com.intellij.python.hatch.icons.PythonHatchIcons
import com.intellij.python.venv.icons.PythonVenvIcons

import com.jetbrains.python.PythonPluginDisposable
import com.jetbrains.python.sdk.flavors.VirtualEnvSdkFlavor

class SdkFactoryTest {
    @BeforeEach
    fun setUp() {
        mockkStatic(ApplicationManager::class)
        val app = mockk<Application>(relaxed = true)
        every { ApplicationManager.getApplication() } returns app
        every { app.getService(VirtualFilePointerManager::class.java) } returns mockk(relaxed = true)
        mockkStatic(PythonPluginDisposable::class)
        every { PythonPluginDisposable.getInstance() } returns mockk<Disposable>(relaxed = true)
        mockkStatic(VirtualEnvSdkFlavor::class)
        every { VirtualEnvSdkFlavor.getInstance() } returns mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(ApplicationManager::class)
        unmockkStatic(PythonPluginDisposable::class)
        unmockkStatic(VirtualEnvSdkFlavor::class)
    }

    @Test
    fun `getIconForEnvironmentType returns UV icon`() {
        assertEquals(
            PythonCommunityImplUVCommonIcons.UV,
            SdkFactory.getIconForEnvironmentType(PythonEnvironmentType.UV),
        )
    }

    @Test
    fun `getIconForEnvironmentType returns Conda icon`() {
        assertEquals(
            PythonCommunityImplCondaIcons.Anaconda,
            SdkFactory.getIconForEnvironmentType(PythonEnvironmentType.CONDA),
        )
    }

    @Test
    fun `getIconForEnvironmentType returns Poetry icon`() {
        assertEquals(
            PythonCommunityImplPoetryCommonIcons.Poetry,
            SdkFactory.getIconForEnvironmentType(PythonEnvironmentType.POETRY),
        )
    }

    @Test
    fun `getIconForEnvironmentType returns Hatch icon`() {
        assertEquals(PythonHatchIcons.Logo, SdkFactory.getIconForEnvironmentType(PythonEnvironmentType.HATCH))
    }

    @Test
    fun `getIconForEnvironmentType returns Pipenv icon`() {
        assertEquals(PIPENV_ICON, SdkFactory.getIconForEnvironmentType(PythonEnvironmentType.PIPENV))
    }

    @Test
    fun `getIconForEnvironmentType returns VirtualEnv icon for VIRTUALENV`() {
        assertEquals(
            PythonVenvIcons.VirtualEnv,
            SdkFactory.getIconForEnvironmentType(PythonEnvironmentType.VIRTUALENV),
        )
    }

    @Test
    fun `getIconForEnvironmentType returns VirtualEnv icon for SYSTEM`() {
        assertEquals(PythonVenvIcons.VirtualEnv, SdkFactory.getIconForEnvironmentType(PythonEnvironmentType.SYSTEM))
    }

    @Test
    fun `findHatchWorkingDir finds pyproject with tool hatch`(
        @TempDir tempDir: Path,
    ) {
        val projectDir = tempDir.resolve("project")
        projectDir.createDirectories()
        projectDir.resolve("pyproject.toml").writeText("[tool.hatch]\nbuild.targets.wheel.packages = [\"src\"]\n")

        val method = SdkFactory::class.java.getDeclaredMethod("findHatchWorkingDir", Path::class.java)
        method.isAccessible = true
        val result = method.invoke(SdkFactory, projectDir) as Path?

        assertEquals(projectDir, result)
    }

    @Test
    fun `findHatchWorkingDir returns null when no pyproject`(
        @TempDir tempDir: Path,
    ) {
        val method = SdkFactory::class.java.getDeclaredMethod("findHatchWorkingDir", Path::class.java)
        method.isAccessible = true
        val result = method.invoke(SdkFactory, tempDir) as Path?

        assertNull(result)
    }

    @Test
    fun `findHatchWorkingDir returns null when pyproject has no hatch config`(
        @TempDir tempDir: Path,
    ) {
        tempDir.resolve("pyproject.toml").writeText("[project]\nname = \"test\"\n")

        val method = SdkFactory::class.java.getDeclaredMethod("findHatchWorkingDir", Path::class.java)
        method.isAccessible = true
        val result = method.invoke(SdkFactory, tempDir) as Path?

        assertNull(result)
    }

    @Test
    fun `findHatchWorkingDir walks up to parent`(
        @TempDir tempDir: Path,
    ) {
        tempDir.resolve("pyproject.toml").writeText("[tool.hatch]\n")
        val subDir = tempDir.resolve("sub/dir")
        subDir.createDirectories()

        val method = SdkFactory::class.java.getDeclaredMethod("findHatchWorkingDir", Path::class.java)
        method.isAccessible = true
        val result = method.invoke(SdkFactory, subDir) as Path?

        assertEquals(tempDir, result)
    }

    @Test
    fun `findUvWorkingDir finds uv lock`(
        @TempDir tempDir: Path,
    ) {
        val projectDir = tempDir.resolve("project")
        projectDir.createDirectories()
        Files.createFile(projectDir.resolve("uv.lock"))

        val method = SdkFactory::class.java.getDeclaredMethod("findUvWorkingDir", Path::class.java)
        method.isAccessible = true
        val result = method.invoke(SdkFactory, projectDir) as Path?

        assertEquals(projectDir, result)
    }

    @Test
    fun `findUvWorkingDir returns null when no uv lock`(
        @TempDir tempDir: Path,
    ) {
        val method = SdkFactory::class.java.getDeclaredMethod("findUvWorkingDir", Path::class.java)
        method.isAccessible = true
        val result = method.invoke(SdkFactory, tempDir) as Path?

        assertNull(result)
    }

    @Test
    fun `findUvWorkingDir walks up to find uv lock in parent`(
        @TempDir tempDir: Path,
    ) {
        Files.createFile(tempDir.resolve("uv.lock"))
        val subDir = tempDir.resolve("sub/dir")
        subDir.createDirectories()

        val method = SdkFactory::class.java.getDeclaredMethod("findUvWorkingDir", Path::class.java)
        method.isAccessible = true
        val result = method.invoke(SdkFactory, subDir) as Path?

        assertEquals(tempDir, result)
    }

    @Test
    fun `createAdditionalData returns UvSdkAdditionalData for UV`(
        @TempDir tempDir: Path,
    ) {
        val method =
            SdkFactory::class.java.getDeclaredMethod(
                "createAdditionalData",
                PythonEnvironmentType::class.java,
                String::class.java,
                Path::class.java,
            )
        method.isAccessible = true
        val result = method.invoke(SdkFactory, PythonEnvironmentType.UV, "/venv/bin/python", tempDir)

        assertNotNull(result)
        assertEquals("com.jetbrains.python.sdk.uv.UvSdkAdditionalData", result!!::class.java.name)
    }

    @Test
    fun `createAdditionalData returns HatchSdkAdditionalData for HATCH`(
        @TempDir tempDir: Path,
    ) {
        val method =
            SdkFactory::class.java.getDeclaredMethod(
                "createAdditionalData",
                PythonEnvironmentType::class.java,
                String::class.java,
                Path::class.java,
            )
        method.isAccessible = true
        val result = method.invoke(SdkFactory, PythonEnvironmentType.HATCH, "/venv/bin/python", tempDir)

        assertNotNull(result)
        assertEquals("com.jetbrains.python.hatch.sdk.HatchSdkAdditionalData", result!!::class.java.name)
    }

    @Test
    fun `createAdditionalData returns PythonSdkAdditionalData for VIRTUALENV`(
        @TempDir tempDir: Path,
    ) {
        val method =
            SdkFactory::class.java.getDeclaredMethod(
                "createAdditionalData",
                PythonEnvironmentType::class.java,
                String::class.java,
                Path::class.java,
            )
        method.isAccessible = true
        val result = method.invoke(SdkFactory, PythonEnvironmentType.VIRTUALENV, "/venv/bin/python", tempDir)

        assertNotNull(result)
        assertEquals("com.jetbrains.python.sdk.PythonSdkAdditionalData", result!!::class.java.name)
    }

    @Test
    fun `createAdditionalData returns PythonSdkAdditionalData for POETRY`(
        @TempDir tempDir: Path,
    ) {
        val method =
            SdkFactory::class.java.getDeclaredMethod(
                "createAdditionalData",
                PythonEnvironmentType::class.java,
                String::class.java,
                Path::class.java,
            )
        method.isAccessible = true
        val result = method.invoke(SdkFactory, PythonEnvironmentType.POETRY, "/venv/bin/python", tempDir)

        assertNotNull(result)
        assertEquals("com.jetbrains.python.sdk.PythonSdkAdditionalData", result!!::class.java.name)
    }

    @Test
    fun `createAdditionalData returns PythonSdkAdditionalData for PIPENV`(
        @TempDir tempDir: Path,
    ) {
        val method =
            SdkFactory::class.java.getDeclaredMethod(
                "createAdditionalData",
                PythonEnvironmentType::class.java,
                String::class.java,
                Path::class.java,
            )
        method.isAccessible = true
        val result = method.invoke(SdkFactory, PythonEnvironmentType.PIPENV, "/venv/bin/python", tempDir)

        assertNotNull(result)
        assertEquals("com.jetbrains.python.sdk.PythonSdkAdditionalData", result!!::class.java.name)
    }

    @Test
    fun `createAdditionalData returns PythonSdkAdditionalData for CONDA`(
        @TempDir tempDir: Path,
    ) {
        val method =
            SdkFactory::class.java.getDeclaredMethod(
                "createAdditionalData",
                PythonEnvironmentType::class.java,
                String::class.java,
                Path::class.java,
            )
        method.isAccessible = true
        val result = method.invoke(SdkFactory, PythonEnvironmentType.CONDA, "/venv/bin/python", tempDir)

        assertNotNull(result)
        assertEquals("com.jetbrains.python.sdk.PythonSdkAdditionalData", result!!::class.java.name)
    }

    @Test
    fun `createAdditionalData returns PythonSdkAdditionalData for SYSTEM`(
        @TempDir tempDir: Path,
    ) {
        val method =
            SdkFactory::class.java.getDeclaredMethod(
                "createAdditionalData",
                PythonEnvironmentType::class.java,
                String::class.java,
                Path::class.java,
            )
        method.isAccessible = true
        val result = method.invoke(SdkFactory, PythonEnvironmentType.SYSTEM, "/venv/bin/python", tempDir)

        assertNotNull(result)
        assertEquals("com.jetbrains.python.sdk.PythonSdkAdditionalData", result!!::class.java.name)
    }
}
