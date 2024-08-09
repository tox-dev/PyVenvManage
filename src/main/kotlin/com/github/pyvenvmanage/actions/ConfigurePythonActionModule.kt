package com.github.pyvenvmanage.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile

class ConfigurePythonActionModule : ConfigurePythonActionAbstract() {
    override fun setSdk(
        project: Project,
        selectedPath: VirtualFile,
        sdk: Sdk,
    ): String? {
        val module = ProjectFileIndex.getInstance(project).getModuleForFile(selectedPath, false) ?: return null
        ModuleRootModificationUtil.setModuleSdk(module, sdk)
        return "module ${module.name}"
    }
}
