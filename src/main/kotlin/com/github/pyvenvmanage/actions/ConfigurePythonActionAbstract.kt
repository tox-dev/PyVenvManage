package com.github.pyvenvmanage.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
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
    companion object {
        private val LOG = Logger.getInstance(ConfigurePythonActionAbstract::class.java)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val selectedPath = e.getData(CommonDataKeys.VIRTUAL_FILE)
        LOG.info("Update called for path: $selectedPath")
        val isValid =
            selectedPath?.let { path ->
                val dir = if (path.isDirectory) path else path.parent
                LOG.info("Checking directory: ${dir.path}")
                PythonSdkUtil.getPythonExecutable(dir.path)?.let { pythonExe ->
                    LOG.info("Found python executable: $pythonExe")
                    val envType = EnvironmentDetector.detectEnvironmentType(pythonExe)
                    val icon = SdkFactory.getIconForEnvironmentType(envType)
                    LOG.info("Setting icon for type $envType: $icon")
                    e.presentation.icon = icon
                    true
                } ?: false.also { LOG.info("No python executable found") }
            } ?: false.also { LOG.info("No selected path") }

        e.presentation.isEnabledAndVisible = isValid
        LOG.info("Action visible: $isValid")
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
