package com.github.pyvenvmanage.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.vfs.VirtualFile

import com.jetbrains.python.configuration.PyConfigurableInterpreterList
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.PythonSdkUtil
import com.jetbrains.python.statistics.executionType
import com.jetbrains.python.statistics.interpreterType

abstract class ConfigurePythonActionAbstract : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible =
            e.getData(CommonDataKeys.VIRTUAL_FILE)?.let { selectedPath ->
                if (selectedPath.isDirectory) {
                    PythonSdkUtil.getPythonExecutable(selectedPath.path) != null
                } else {
                    PythonSdkUtil.isVirtualEnv(selectedPath.path)
                }
            } ?: false
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selectedPath =
            e.getData(CommonDataKeys.VIRTUAL_FILE)?.let {
                if (it.isDirectory) it else it.parent
            } ?: return
        val pythonExecutable = PythonSdkUtil.getPythonExecutable(selectedPath.path) ?: return
        val sdk: Sdk =
            PyConfigurableInterpreterList
                .getInstance(project)
                .model
                .projectSdks
                .values
                .firstOrNull { it.homePath == pythonExecutable }
                ?: (SdkConfigurationUtil.createAndAddSDK(pythonExecutable, PythonSdkType.getInstance()) ?: return)

        val notificationFor = setSdk(project, selectedPath, sdk) ?: return
        NotificationGroupManager
            .getInstance()
            .getNotificationGroup("Python SDK change")
            .createNotification(
                "Python SDK Updated",
                "Updated SDK for $notificationFor to:\n${sdk.name} " +
                    "of type ${sdk.interpreterType.toString().lowercase()} " +
                    sdk.executionType.toString().lowercase(),
                NotificationType.INFORMATION,
            ).notify(project)
    }

    protected abstract fun setSdk(
        project: Project,
        selectedPath: VirtualFile,
        sdk: Sdk,
    ): String?
}
