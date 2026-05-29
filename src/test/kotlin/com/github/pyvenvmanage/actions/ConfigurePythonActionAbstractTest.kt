package com.github.pyvenvmanage.actions

import java.nio.file.Path

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.python.uv.common.icons.PythonUvCommonIcons
import com.intellij.python.venv.icons.PythonVenvIcons

import com.jetbrains.python.configuration.PyConfigurableInterpreterList
import com.jetbrains.python.sdk.PythonSdkUtil

import com.github.pyvenvmanage.sdk.EnvironmentDetector
import com.github.pyvenvmanage.sdk.PythonEnvironmentType
import com.github.pyvenvmanage.sdk.SdkFactory

class ConfigurePythonActionAbstractTest {
    private lateinit var action: TestableConfigurePythonAction
    private lateinit var event: AnActionEvent
    private lateinit var presentation: Presentation
    private lateinit var virtualFile: VirtualFile
    private lateinit var project: Project

    @BeforeEach
    fun setUp() {
        action = TestableConfigurePythonAction()
        event = mockk(relaxed = true)
        presentation = mockk(relaxed = true)
        virtualFile = mockk(relaxed = true)
        project = mockk(relaxed = true)

        every { event.presentation } returns presentation
        every { event.project } returns project
    }

    @Test
    fun `getActionUpdateThread returns BGT`() {
        assertEquals(ActionUpdateThread.BGT, action.actionUpdateThread)
    }

    @Nested
    inner class UpdateTest {
        @BeforeEach
        fun setUpMocks() {
            mockkStatic(PythonSdkUtil::class)
            mockkObject(EnvironmentDetector)
            mockkObject(SdkFactory)
        }

        @AfterEach
        fun tearDown() {
            unmockkAll()
        }

        @Test
        fun `disables action when no file selected`() {
            every { event.getData(CommonDataKeys.VIRTUAL_FILE) } returns null

            action.update(event)

            verify { presentation.isEnabledAndVisible = false }
        }

        @Test
        fun `enables action for directory with Python executable`() {
            every { event.getData(CommonDataKeys.VIRTUAL_FILE) } returns virtualFile
            every { virtualFile.isDirectory } returns true
            every { virtualFile.path } returns "/some/venv"
            every { PythonSdkUtil.getPythonExecutable("/some/venv") } returns "/some/venv/bin/python"
            every { EnvironmentDetector.detectEnvironmentType("/some/venv/bin/python") } returns
                PythonEnvironmentType.UV
            every { SdkFactory.getIconForEnvironmentType(PythonEnvironmentType.UV) } returns PythonVenvIcons.VirtualEnv

            action.update(event)

            verify { presentation.isEnabledAndVisible = true }
            verify { presentation.icon = any() }
        }

        @Test
        fun `disables action for directory without Python executable`() {
            every { event.getData(CommonDataKeys.VIRTUAL_FILE) } returns virtualFile
            every { virtualFile.isDirectory } returns true
            every { virtualFile.path } returns "/some/dir"
            every { PythonSdkUtil.getPythonExecutable("/some/dir") } returns null

            action.update(event)

            verify { presentation.isEnabledAndVisible = false }
        }

        @Test
        fun `sets icon based on detected environment type`() {
            every { event.getData(CommonDataKeys.VIRTUAL_FILE) } returns virtualFile
            every { virtualFile.isDirectory } returns true
            every { virtualFile.path } returns "/some/venv"
            every { PythonSdkUtil.getPythonExecutable("/some/venv") } returns "/some/venv/bin/python"
            every { EnvironmentDetector.detectEnvironmentType("/some/venv/bin/python") } returns
                PythonEnvironmentType.UV
            every { SdkFactory.getIconForEnvironmentType(PythonEnvironmentType.UV) } returns
                PythonUvCommonIcons.UV

            action.update(event)

            verify { presentation.icon = PythonUvCommonIcons.UV }
        }
    }

    @Nested
    inner class ActionPerformedTest {
        private lateinit var parentDir: VirtualFile
        private lateinit var interpreterList: PyConfigurableInterpreterList
        private lateinit var notificationGroupManager: NotificationGroupManager
        private lateinit var notificationGroup: NotificationGroup
        private lateinit var notification: Notification
        private lateinit var jdkTable: ProjectJdkTable

        @BeforeEach
        fun setUpMocks() {
            parentDir = mockk(relaxed = true)
            interpreterList = mockk(relaxed = true)
            notificationGroupManager = mockk(relaxed = true)
            notificationGroup = mockk(relaxed = true)
            notification = mockk(relaxed = true)
            jdkTable = mockk(relaxed = true)

            mockkStatic(PythonSdkUtil::class)
            mockkStatic(PyConfigurableInterpreterList::class)
            mockkStatic(NotificationGroupManager::class)
            mockkStatic(ProjectJdkTable::class)
            mockkObject(EnvironmentDetector)
            mockkObject(SdkFactory)

            every { NotificationGroupManager.getInstance() } returns notificationGroupManager
            every { notificationGroupManager.getNotificationGroup(any()) } returns notificationGroup
            every {
                notificationGroup.createNotification(any(), any(), any<NotificationType>())
            } returns notification
            every { notification.setIcon(any()) } returns notification
            every { notification.notify(any()) } just Runs
            every { ProjectJdkTable.getInstance() } returns jdkTable
            every { jdkTable.allJdks } returns emptyArray()
        }

        @AfterEach
        fun tearDown() {
            unmockkAll()
        }

        @Test
        fun `returns early when project is null`() {
            every { event.project } returns null

            action.actionPerformed(event)

            verify(exactly = 0) { event.getData(CommonDataKeys.VIRTUAL_FILE) }
        }

        @Test
        fun `returns early when no file selected`() {
            every { event.getData(CommonDataKeys.VIRTUAL_FILE) } returns null

            action.actionPerformed(event)

            verify(exactly = 0) { PythonSdkUtil.getPythonExecutable(any()) }
        }

        @Test
        fun `uses parent directory when file is not a directory`() {
            every { event.getData(CommonDataKeys.VIRTUAL_FILE) } returns virtualFile
            every { virtualFile.isDirectory } returns false
            every { virtualFile.parent } returns parentDir
            every { parentDir.path } returns "/some/venv"
            every { PythonSdkUtil.getPythonExecutable("/some/venv") } returns null

            action.actionPerformed(event)

            verify { PythonSdkUtil.getPythonExecutable("/some/venv") }
        }

        @Test
        fun `shows error when no Python executable found`() {
            every { event.getData(CommonDataKeys.VIRTUAL_FILE) } returns virtualFile
            every { virtualFile.isDirectory } returns true
            every { virtualFile.path } returns "/some/dir"
            every { virtualFile.name } returns "dir"
            every { PythonSdkUtil.getPythonExecutable("/some/dir") } returns null

            action.actionPerformed(event)

            verify {
                notificationGroup.createNotification(
                    "Python SDK Error",
                    "No Python executable found in dir",
                    NotificationType.ERROR,
                )
            }
        }

        @Test
        fun `shows error when SDK creation fails`() {
            every { event.getData(CommonDataKeys.VIRTUAL_FILE) } returns virtualFile
            every { virtualFile.isDirectory } returns true
            every { virtualFile.path } returns "/some/venv"
            every { virtualFile.toNioPath() } returns Path.of("/some/venv")
            every { PythonSdkUtil.getPythonExecutable("/some/venv") } returns "/some/venv/bin/python"
            every { EnvironmentDetector.detectEnvironmentType("/some/venv/bin/python") } returns
                PythonEnvironmentType.VIRTUALENV
            every {
                SdkFactory.createSdk("/some/venv/bin/python", PythonEnvironmentType.VIRTUALENV, Path.of("/some/venv"))
            } returns null

            action.actionPerformed(event)

            verify {
                notificationGroup.createNotification(
                    "Python SDK Error",
                    "Failed to create SDK from /some/venv/bin/python",
                    NotificationType.ERROR,
                )
            }
        }

        @Test
        fun `creates new SDK when not found in existing SDKs`() {
            val newSdk: Sdk = mockk(relaxed = true)
            val messageSlot = slot<String>()

            action.lastSetSdkResult = ConfigurePythonActionAbstract.SetSdkResult.Success("module")

            every { event.getData(CommonDataKeys.VIRTUAL_FILE) } returns virtualFile
            every { virtualFile.isDirectory } returns true
            every { virtualFile.path } returns "/some/venv"
            every { virtualFile.toNioPath() } returns Path.of("/some/venv")
            every { PythonSdkUtil.getPythonExecutable("/some/venv") } returns "/some/venv/bin/python"
            every { EnvironmentDetector.detectEnvironmentType("/some/venv/bin/python") } returns
                PythonEnvironmentType.UV
            every {
                SdkFactory.createSdk(
                    "/some/venv/bin/python",
                    PythonEnvironmentType.UV,
                    Path.of("/some/venv"),
                )
            } returns
                newSdk
            every { SdkFactory.getIconForEnvironmentType(PythonEnvironmentType.UV) } returns
                PythonUvCommonIcons.UV
            every { newSdk.name } returns "Python 3.11 (venv)"

            action.actionPerformed(event)

            verify {
                SdkFactory.createSdk("/some/venv/bin/python", PythonEnvironmentType.UV, Path.of("/some/venv"))
            }
            verify {
                notificationGroup.createNotification(
                    eq("Python SDK Updated"),
                    capture(messageSlot),
                    eq(NotificationType.INFORMATION),
                )
            }
            assert(messageSlot.captured.contains("Updated SDK for module to:"))
            assert(messageSlot.captured.contains("(uv)"))
        }

        @Test
        fun `shows error notification on setSdk error`() {
            val existingSdk: Sdk = mockk(relaxed = true)

            action.lastSetSdkResult = ConfigurePythonActionAbstract.SetSdkResult.Error("Module not found")

            every { event.getData(CommonDataKeys.VIRTUAL_FILE) } returns virtualFile
            every { virtualFile.isDirectory } returns true
            every { virtualFile.path } returns "/some/venv"
            every { PythonSdkUtil.getPythonExecutable("/some/venv") } returns "/some/venv/bin/python"
            every { EnvironmentDetector.detectEnvironmentType("/some/venv/bin/python") } returns
                PythonEnvironmentType.VIRTUALENV
            every { jdkTable.allJdks } returns arrayOf(existingSdk)
            every { existingSdk.homePath } returns "/some/venv/bin/python"

            action.actionPerformed(event)

            verify {
                notificationGroup.createNotification(
                    "Python SDK Error",
                    "Module not found",
                    NotificationType.ERROR,
                )
            }
        }

        @Test
        fun `shows success notification on setSdk success`() {
            val existingSdk: Sdk = mockk(relaxed = true)
            val messageSlot = slot<String>()

            action.lastSetSdkResult = ConfigurePythonActionAbstract.SetSdkResult.Success("module")

            every { event.getData(CommonDataKeys.VIRTUAL_FILE) } returns virtualFile
            every { virtualFile.isDirectory } returns true
            every { virtualFile.path } returns "/some/venv"
            every { PythonSdkUtil.getPythonExecutable("/some/venv") } returns "/some/venv/bin/python"
            every { EnvironmentDetector.detectEnvironmentType("/some/venv/bin/python") } returns
                PythonEnvironmentType.VIRTUALENV
            every { SdkFactory.getIconForEnvironmentType(PythonEnvironmentType.VIRTUALENV) } returns
                PythonVenvIcons.VirtualEnv
            every { jdkTable.allJdks } returns arrayOf(existingSdk)
            every { existingSdk.homePath } returns "/some/venv/bin/python"
            every { existingSdk.name } returns "Python 3.11 (venv)"

            action.actionPerformed(event)

            verify {
                notificationGroup.createNotification(
                    eq("Python SDK Updated"),
                    capture(messageSlot),
                    eq(NotificationType.INFORMATION),
                )
            }
            assert(messageSlot.captured.contains("Updated SDK for module to:"))
            assert(messageSlot.captured.contains("Python 3.11 (venv)"))
            assert(messageSlot.captured.contains("(virtualenv)"))
        }
    }

    class TestableConfigurePythonAction : ConfigurePythonActionAbstract() {
        var lastSetSdkResult: SetSdkResult = SetSdkResult.Success("test")

        override fun setSdk(
            project: Project,
            selectedPath: VirtualFile,
            sdk: Sdk,
        ): SetSdkResult = lastSetSdkResult
    }
}
