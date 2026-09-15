package com.github.pyvenvmanage.actions

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile

import com.jetbrains.python.sdk.PythonSdkUtil

open class ConfigurePythonActionModule : ConfigurePythonActionAbstract() {
    override fun setSdk(
        project: Project,
        selectedPath: VirtualFile,
        sdk: Sdk,
    ): SetSdkResult {
        val module =
            ProjectFileIndex.getInstance(project).getModuleForFile(selectedPath, false)
                ?: return SetSdkResult.Error("No module found for ${selectedPath.name}")
        ModuleRootModificationUtil.setModuleSdk(module, sdk)
        // Files outside every module, such as scratch files, resolve against the project SDK only (PY-89831), and
        // PyCharm 2026.2 sets interpreters per module, so a project without one loses builtins in those files.
        val rootManager = ProjectRootManager.getInstance(project)
        if (rootManager.projectSdk?.let(PythonSdkUtil::isPythonSdk) != true) {
            WriteAction.run<Throwable> { rootManager.projectSdk = sdk }
        }
        return SetSdkResult.Success("module ${module.name}")
    }
}
