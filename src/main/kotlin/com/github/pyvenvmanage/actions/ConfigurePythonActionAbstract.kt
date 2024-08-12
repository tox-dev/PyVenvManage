package com.github.pyvenvmanage.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.vfs.VirtualFile

import com.jetbrains.python.configuration.PyConfigurableInterpreterList
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.PythonSdkUtil
import com.jetbrains.python.statistics.executionType
import com.jetbrains.python.statistics.interpreterType

abstract class ConfigurePythonActionAbstract : AnAction() {
    private val logger = Logger.getInstance(SdkConfigurationUtil::class.java)

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        // Enable action menu when the selected path is the:
        //  - virtual environment root
        //  - virtual environment binary (Scripts) folder
        //  - any files within the binary folder.
        e.presentation.isEnabledAndVisible =
            when (val selectedPath = e.getData(CommonDataKeys.VIRTUAL_FILE)) {
                null -> false

                else ->
                    when (selectedPath.isDirectory) {
                        true -> {
                            // check if there is a python executable available under this folder -> name match for binary
                            PythonSdkUtil.getPythonExecutable(selectedPath.path) != null
                        }

                        false -> {
                            // check for presence of the activate_this.py + activate alongside or pyvenv.cfg above
                            PythonSdkUtil.isVirtualEnv(selectedPath.path)
                        }
                    }
            }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        var selectedPath = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        selectedPath = if (selectedPath.isDirectory) selectedPath else selectedPath.parent
        val pythonExecutable = PythonSdkUtil.getPythonExecutable(selectedPath.path) ?: return
        val sdk: Sdk =
            PyConfigurableInterpreterList
                .getInstance(project)
                .model
                .projectSdks
                .values
                .firstOrNull { it.homePath == pythonExecutable }
                ?: (createSdk(pythonExecutable, project) ?: return)

        val notificationFor = setSdk(project, selectedPath, sdk) ?: return
        NotificationGroupManager
            .getInstance()
            .getNotificationGroup("Python SDK change")
            .createNotification(
                "Updated SDK for $notificationFor to:\n${sdk.name} " +
                    "of type ${sdk.interpreterType.toString().lowercase()} " +
                    sdk.executionType.toString().lowercase(),
                MessageType.INFO,
            ).notify(project)
    }

    private fun createSdk(
        pythonExecutable: String,
        project: Project,
    ): Sdk? {
        logger.info("Create SDK for $pythonExecutable inside ${project.name}")
        val sdk = SdkConfigurationUtil.createAndAddSDK(pythonExecutable, PythonSdkType.getInstance()) ?: return null

        // create project level interpreters
//        val sdk =
//            SdkConfigurationUtil.setupSdk(
//                ProjectJdkTable.getInstance().allJdks,
//                sdkHome,
//                PythonSdkType.getInstance(),
//                false,
//                null,
//                null,
//            )
//                ?: return null

//        LOG.info("Add SDK ${sdk.name} for ${project.name}")
//        SdkConfigurationUtil.addSdk(sdk)

        // set better name for interpreter
//        val base =
//            Paths.get(project.basePath ?: return null).relativize(
//                (sdk.homeDirectory ?: return null)
//                    .toNioPath()
//                    .parent.parent,
//            )
//        sdk.sdkModificator.name = "${project.name} ${sdk.versionString} ($base)"
        logger.info("Created SDK ${sdk.name} for ${project.name}")
        return sdk
    }

    protected abstract fun setSdk(
        project: Project,
        selectedPath: VirtualFile,
        sdk: Sdk,
    ): String?
}
