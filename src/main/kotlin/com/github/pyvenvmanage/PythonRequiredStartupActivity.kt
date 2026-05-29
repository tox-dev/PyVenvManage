package com.github.pyvenvmanage

import com.intellij.ide.plugins.PluginManager
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

import com.github.pyvenvmanage.settings.PyVenvManageSettings

class PythonRequiredStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        if (isPythonPluginAvailable()) return
        if (PyVenvManageSettings.getInstance().dismissedPythonWarning) return
        NotificationGroupManager
            .getInstance()
            .getNotificationGroup("PyVenv Manage")
            .createNotification(
                "PyVenv Manage requires Python support",
                "Please install the Python plugin or use PyCharm for full functionality.",
                NotificationType.WARNING,
            ).addAction(
                NotificationAction.createExpiring("Don't show again") { _, notification ->
                    PyVenvManageSettings.getInstance().dismissedPythonWarning = true
                    notification.expire()
                },
            ).notify(project)
    }

    private fun isPythonPluginAvailable(): Boolean =
        isEnabled(PluginId.getId("com.intellij.modules.python")) || isEnabled(PluginId.getId("PythonCore"))

    private fun isEnabled(id: PluginId): Boolean =
        PluginManager.isPluginInstalled(id) && !PluginManagerCore.isDisabled(id)
}
