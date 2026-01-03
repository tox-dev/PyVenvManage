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

        val pythonExecutable = PythonSdkUtil.getPythonExecutable(selectedPath.path)
        if (pythonExecutable == null) {
            notifyError(project, "No Python executable found in ${selectedPath.name}")
            return
        }

        val sdk: Sdk =
            PyConfigurableInterpreterList
                .getInstance(project)
                .model
                .projectSdks
                .values
                .firstOrNull { it.homePath == pythonExecutable }
                ?: run {
                    val newSdk = SdkConfigurationUtil.createAndAddSDK(pythonExecutable, PythonSdkType.getInstance())
                    if (newSdk == null) {
                        notifyError(project, "Failed to create SDK from $pythonExecutable")
                        return
                    }
                    newSdk
                }

        when (val result = setSdk(project, selectedPath, sdk)) {
            is SetSdkResult.Success -> notifySuccess(project, result.target, sdk)
            is SetSdkResult.Error -> notifyError(project, result.message)
        }
    }

    private fun notifySuccess(
        project: Project,
        target: String,
        sdk: Sdk,
    ) {
        NotificationGroupManager
            .getInstance()
            .getNotificationGroup("Python SDK change")
            .createNotification(
                "Python SDK Updated",
                "Updated SDK for $target to:\n${sdk.name} " +
                    "of type ${sdk.interpreterType.toString().lowercase()} " +
                    sdk.executionType.toString().lowercase(),
                NotificationType.INFORMATION,
            ).notify(project)
    }

    private fun notifyError(
        project: Project,
        message: String,
    ) {
        NotificationGroupManager
            .getInstance()
            .getNotificationGroup("Python SDK change")
            .createNotification(
                "Python SDK Error",
                message,
                NotificationType.ERROR,
            ).notify(project)
    }

    protected abstract fun setSdk(
        project: Project,
        selectedPath: VirtualFile,
        sdk: Sdk,
    ): SetSdkResult

    sealed class SetSdkResult {
        data class Success(
            val target: String,
        ) : SetSdkResult()

        data class Error(
            val message: String,
        ) : SetSdkResult()
    }
}
