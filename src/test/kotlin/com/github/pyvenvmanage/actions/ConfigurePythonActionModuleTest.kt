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

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile

class ConfigurePythonActionModuleTest {
    private lateinit var action: TestableConfigurePythonActionModule
    private lateinit var project: Project
    private lateinit var selectedPath: VirtualFile
    private lateinit var sdk: Sdk
    private lateinit var projectFileIndex: ProjectFileIndex
    private lateinit var module: Module

    @BeforeEach
    fun setUp() {
        action = TestableConfigurePythonActionModule()
        project = mockk(relaxed = true)
        selectedPath = mockk(relaxed = true)
        sdk = mockk(relaxed = true)
        projectFileIndex = mockk(relaxed = true)
        module = mockk(relaxed = true)

        mockkStatic(ProjectFileIndex::class)
        mockkStatic(ModuleRootModificationUtil::class)
        every { ProjectFileIndex.getInstance(project) } returns projectFileIndex
        every { selectedPath.name } returns "venv"
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(ProjectFileIndex::class)
        unmockkStatic(ModuleRootModificationUtil::class)
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
        every { projectFileIndex.getModuleForFile(selectedPath, false) } returns module
        every { module.name } returns "my-module"
        every { ModuleRootModificationUtil.setModuleSdk(module, sdk) } returns Unit

        val result = action.testSetSdk(project, selectedPath, sdk)

        assertTrue(result is ConfigurePythonActionAbstract.SetSdkResult.Success)
        assertEquals(
            "module my-module",
            (result as ConfigurePythonActionAbstract.SetSdkResult.Success).target,
        )
        verify { ModuleRootModificationUtil.setModuleSdk(module, sdk) }
    }

    class TestableConfigurePythonActionModule : ConfigurePythonActionModule() {
        fun testSetSdk(
            project: Project,
            selectedPath: VirtualFile,
            sdk: Sdk,
        ): SetSdkResult = setSdk(project, selectedPath, sdk)
    }
}
