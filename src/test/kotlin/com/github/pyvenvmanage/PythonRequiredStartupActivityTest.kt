package com.github.pyvenvmanage

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project

import com.github.pyvenvmanage.settings.PyVenvManageSettings

class PythonRequiredStartupActivityTest {
    private lateinit var project: Project
    private lateinit var notificationGroupManager: NotificationGroupManager
    private lateinit var notificationGroup: NotificationGroup
    private lateinit var notification: Notification
    private lateinit var settings: PyVenvManageSettings

    @BeforeEach
    fun setUp() {
        project = mockk(relaxed = true)
        notificationGroupManager = mockk(relaxed = true)
        notificationGroup = mockk(relaxed = true)
        notification = mockk(relaxed = true)
        settings = mockk(relaxed = true)

        mockkStatic(NotificationGroupManager::class)
        mockkStatic(PluginManagerCore::class)
        mockkObject(PyVenvManageSettings)

        every { NotificationGroupManager.getInstance() } returns notificationGroupManager
        every { notificationGroupManager.getNotificationGroup("PyVenv Manage") } returns notificationGroup
        every {
            notificationGroup.createNotification(any<String>(), any<String>(), any<NotificationType>())
        } returns notification
        every { notification.addAction(any<NotificationAction>()) } returns notification
        every { PyVenvManageSettings.getInstance() } returns settings
        every { settings.dismissedPythonWarning } returns false
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(NotificationGroupManager::class)
        unmockkStatic(PluginManagerCore::class)
        unmockkObject(PyVenvManageSettings)
    }

    @Test
    fun `does not show notification when python module is available`(): Unit =
        runBlocking {
            val pythonPlugin: IdeaPluginDescriptor = mockk(relaxed = true)
            every { pythonPlugin.isEnabled } returns true
            every {
                PluginManagerCore.getPlugin(PluginId.getId("com.intellij.modules.python"))
            } returns pythonPlugin

            PythonRequiredStartupActivity().execute(project)

            verify(exactly = 0) { notification.notify(any()) }
        }

    @Test
    fun `does not show notification when PythonCore plugin is available`(): Unit =
        runBlocking {
            val pythonCorePlugin: IdeaPluginDescriptor = mockk(relaxed = true)
            every { pythonCorePlugin.isEnabled } returns true
            every { PluginManagerCore.getPlugin(PluginId.getId("com.intellij.modules.python")) } returns null
            every { PluginManagerCore.getPlugin(PluginId.getId("PythonCore")) } returns pythonCorePlugin

            PythonRequiredStartupActivity().execute(project)

            verify(exactly = 0) { notification.notify(any()) }
        }

    @Test
    fun `shows warning notification when python is not available`(): Unit =
        runBlocking {
            every { PluginManagerCore.getPlugin(PluginId.getId("com.intellij.modules.python")) } returns null
            every { PluginManagerCore.getPlugin(PluginId.getId("PythonCore")) } returns null

            PythonRequiredStartupActivity().execute(project)

            verify {
                notificationGroup.createNotification(
                    "PyVenv Manage requires Python support",
                    "Please install the Python plugin or use PyCharm for full functionality.",
                    NotificationType.WARNING,
                )
            }
            verify { notification.notify(project) }
        }

    @Test
    fun `shows warning when python plugin exists but is disabled`(): Unit =
        runBlocking {
            val disabledPlugin: IdeaPluginDescriptor = mockk(relaxed = true)
            every { disabledPlugin.isEnabled } returns false
            every { PluginManagerCore.getPlugin(PluginId.getId("com.intellij.modules.python")) } returns disabledPlugin
            every { PluginManagerCore.getPlugin(PluginId.getId("PythonCore")) } returns null

            PythonRequiredStartupActivity().execute(project)

            verify { notification.notify(project) }
        }

    @Test
    fun `does not show notification when warning was dismissed`(): Unit =
        runBlocking {
            every { PluginManagerCore.getPlugin(PluginId.getId("com.intellij.modules.python")) } returns null
            every { PluginManagerCore.getPlugin(PluginId.getId("PythonCore")) } returns null
            every { settings.dismissedPythonWarning } returns true

            PythonRequiredStartupActivity().execute(project)

            verify(exactly = 0) { notification.notify(any()) }
        }

    @Test
    fun `notification includes dont show again action`(): Unit =
        runBlocking {
            every { PluginManagerCore.getPlugin(PluginId.getId("com.intellij.modules.python")) } returns null
            every { PluginManagerCore.getPlugin(PluginId.getId("PythonCore")) } returns null

            PythonRequiredStartupActivity().execute(project)

            verify { notification.addAction(any<NotificationAction>()) }
        }

    @Test
    fun `dont show again action sets dismissed flag and expires notification`(): Unit =
        runBlocking {
            every { PluginManagerCore.getPlugin(PluginId.getId("com.intellij.modules.python")) } returns null
            every { PluginManagerCore.getPlugin(PluginId.getId("PythonCore")) } returns null

            val actionSlot = slot<NotificationAction>()
            every { notification.addAction(capture(actionSlot)) } returns notification

            PythonRequiredStartupActivity().execute(project)

            val event: com.intellij.openapi.actionSystem.AnActionEvent = mockk(relaxed = true)
            actionSlot.captured.actionPerformed(event, notification)

            verify { settings.dismissedPythonWarning = true }
            verify { notification.expire() }
        }
}
