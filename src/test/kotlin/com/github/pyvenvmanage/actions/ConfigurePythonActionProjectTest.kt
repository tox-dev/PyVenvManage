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

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.vfs.VirtualFile

class ConfigurePythonActionProjectTest {
    private lateinit var action: TestableConfigurePythonActionProject
    private lateinit var project: Project
    private lateinit var selectedPath: VirtualFile
    private lateinit var sdk: Sdk

    @BeforeEach
    fun setUp() {
        action = TestableConfigurePythonActionProject()
        project = mockk(relaxed = true)
        selectedPath = mockk(relaxed = true)
        sdk = mockk(relaxed = true)

        mockkStatic(SdkConfigurationUtil::class)
        every { project.name } returns "my-project"
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(SdkConfigurationUtil::class)
    }

    @Test
    fun `setSdk sets project SDK and returns success`() {
        every { SdkConfigurationUtil.setDirectoryProjectSdk(project, sdk) } returns Unit

        val result = action.testSetSdk(project, selectedPath, sdk)

        assertTrue(result is ConfigurePythonActionAbstract.SetSdkResult.Success)
        assertEquals(
            "project my-project",
            (result as ConfigurePythonActionAbstract.SetSdkResult.Success).target,
        )
        verify { SdkConfigurationUtil.setDirectoryProjectSdk(project, sdk) }
    }

    class TestableConfigurePythonActionProject : ConfigurePythonActionProject() {
        fun testSetSdk(
            project: Project,
            selectedPath: VirtualFile,
            sdk: Sdk,
        ): SetSdkResult = setSdk(project, selectedPath, sdk)
    }
}
