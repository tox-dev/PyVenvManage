package com.github.pyvenvmanage.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile

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
        return SetSdkResult.Success("module ${module.name}")
    }
}
