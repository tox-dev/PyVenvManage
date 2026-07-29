package com.github.pyvenvmanage.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.VirtualFile

import com.jetbrains.python.sdk.PythonSdkUtil
import com.jetbrains.python.statistics.executionType
import com.jetbrains.python.statistics.interpreterType

import com.github.pyvenvmanage.sdk.EnvironmentDetector
import com.github.pyvenvmanage.sdk.PythonEnvironmentType
import com.github.pyvenvmanage.sdk.SdkFactory

abstract class ConfigurePythonActionAbstract : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        // Only offer the action on a directory that is itself a virtual environment. Resolving a file
        // to its parent made it appear on ordinary files (e.g. main.py) whose folder merely contains
        // a venv, which is not a valid interpreter target.
        val venvDir = e.getData(CommonDataKeys.VIRTUAL_FILE)?.takeIf { it.isDirectory }
        val pythonExecutable = venvDir?.let { PythonSdkUtil.getPythonExecutable(it.path) }
        if (pythonExecutable != null) {
            e.presentation.icon = SdkFactory.getIconForEnvironmentType(EnvironmentDetector.detectEnvironmentType(pythonExecutable))
        }
        e.presentation.isEnabledAndVisible = pythonExecutable != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selectedPath = e.getData(CommonDataKeys.VIRTUAL_FILE)?.takeIf { it.isDirectory } ?: return

        val pythonExecutable = PythonSdkUtil.getPythonExecutable(selectedPath.path)
        if (pythonExecutable == null) {
            notifyError(project, "No Python executable found in ${selectedPath.name}")
            return
        }

        val envType = EnvironmentDetector.detectEnvironmentType(pythonExecutable)

        val existingSdk = ProjectJdkTable.getInstance().allJdks.firstOrNull { it.homePath == pythonExecutable }

        val sdk: Sdk =
            existingSdk ?: run {
                val newSdk = SdkFactory.createSdk(pythonExecutable, envType, selectedPath.toNioPath())
                if (newSdk == null) {
                    notifyError(project, "Failed to create SDK from $pythonExecutable")
                    return
                }
                newSdk
            }

        when (val result = setSdk(project, selectedPath, sdk)) {
            is SetSdkResult.Success -> notifySuccess(project, result.target, sdk, envType)
            is SetSdkResult.Error -> notifyError(project, result.message)
        }
    }

    private fun notifySuccess(
        project: Project,
        target: String,
        sdk: Sdk,
        envType: PythonEnvironmentType,
    ) {
        NotificationGroupManager
            .getInstance()
            .getNotificationGroup("Python SDK change")
            .createNotification(
                "Python SDK Updated",
                "Updated SDK for $target to:\n${sdk.name} " +
                    "(${envType.name.lowercase()}) " +
                    "of type ${sdk.interpreterType.toString().lowercase()} " +
                    sdk.executionType.toString().lowercase(),
                NotificationType.INFORMATION,
            ).setIcon(SdkFactory.getIconForEnvironmentType(envType))
            .notify(project)
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
