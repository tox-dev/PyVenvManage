package com.github.pyvenvmanage.actions

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ThrowableRunnable

import com.jetbrains.python.sdk.PythonSdkUtil

class ConfigurePythonActionModuleTest {
    private lateinit var action: TestableConfigurePythonActionModule
    private lateinit var project: Project
    private lateinit var selectedPath: VirtualFile
    private lateinit var sdk: Sdk
    private lateinit var projectFileIndex: ProjectFileIndex
    private lateinit var projectRootManager: ProjectRootManager
    private lateinit var module: Module

    @BeforeEach
    fun setUp() {
        action = TestableConfigurePythonActionModule()
        project = mockk(relaxed = true)
        selectedPath = mockk(relaxed = true)
        sdk = mockk(relaxed = true)
        projectFileIndex = mockk(relaxed = true)
        projectRootManager = mockk(relaxed = true)
        module = mockk(relaxed = true)

        mockkStatic(ProjectFileIndex::class)
        mockkStatic(ProjectRootManager::class)
        mockkStatic(ModuleRootModificationUtil::class)
        mockkStatic(PythonSdkUtil::class)
        mockkStatic(WriteAction::class)
        every { ProjectFileIndex.getInstance(project) } returns projectFileIndex
        every { ProjectRootManager.getInstance(project) } returns projectRootManager
        every { projectRootManager.projectSdk } returns null
        every { WriteAction.run<Throwable>(any()) } answers { firstArg<ThrowableRunnable<Throwable>>().run() }
        every { selectedPath.name } returns "venv"
        every { projectFileIndex.getModuleForFile(selectedPath, false) } returns module
        every { module.name } returns "my-module"
        every { ModuleRootModificationUtil.setModuleSdk(module, sdk) } returns Unit
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(ProjectFileIndex::class)
        unmockkStatic(ProjectRootManager::class)
        unmockkStatic(ModuleRootModificationUtil::class)
        unmockkStatic(PythonSdkUtil::class)
        unmockkStatic(WriteAction::class)
    }

    @Test
    fun `setSdk returns error when no module found`() {
        every { projectFileIndex.getModuleForFile(selectedPath, false) } returns null

        val result = action.testSetSdk(project, selectedPath, sdk)

        assertTrue(result is ConfigurePythonActionAbstract.SetSdkResult.Error)
        assertEquals(
            "No module found for venv",
            (result as ConfigurePythonActionAbstract.SetSdkResult.Error).message,
        )
    }

    @Test
    fun `setSdk sets module SDK and returns success`() {
        val result = action.testSetSdk(project, selectedPath, sdk)

        assertTrue(result is ConfigurePythonActionAbstract.SetSdkResult.Success)
        assertEquals(
            "module my-module",
            (result as ConfigurePythonActionAbstract.SetSdkResult.Success).target,
        )
        verify { ModuleRootModificationUtil.setModuleSdk(module, sdk) }
    }

    @Test
    fun `setSdk sets project SDK when project has none`() {
        action.testSetSdk(project, selectedPath, sdk)

        verify { projectRootManager.projectSdk = sdk }
    }

    @Test
    fun `setSdk replaces non-Python project SDK`() {
        val javaSdk: Sdk = mockk()
        every { projectRootManager.projectSdk } returns javaSdk
        every { PythonSdkUtil.isPythonSdk(javaSdk) } returns false

        action.testSetSdk(project, selectedPath, sdk)

        verify { projectRootManager.projectSdk = sdk }
    }

    @Test
    fun `setSdk keeps existing Python project SDK`() {
        val pythonSdk: Sdk = mockk()
        every { projectRootManager.projectSdk } returns pythonSdk
        every { PythonSdkUtil.isPythonSdk(pythonSdk) } returns true

        action.testSetSdk(project, selectedPath, sdk)

        verify(exactly = 0) { projectRootManager.projectSdk = any() }
    }

    class TestableConfigurePythonActionModule : ConfigurePythonActionModule() {
        fun testSetSdk(
            project: Project,
            selectedPath: VirtualFile,
            sdk: Sdk,
        ): SetSdkResult = setSdk(project, selectedPath, sdk)
    }
}
