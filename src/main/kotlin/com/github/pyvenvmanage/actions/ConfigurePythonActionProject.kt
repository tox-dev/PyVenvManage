package com.github.pyvenvmanage.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.vfs.VirtualFile

open class ConfigurePythonActionProject : ConfigurePythonActionAbstract() {
    override fun setSdk(
        project: Project,
        selectedPath: VirtualFile,
        sdk: Sdk,
    ): SetSdkResult {
        SdkConfigurationUtil.setDirectoryProjectSdk(project, sdk)
        return SetSdkResult.Success("project ${project.name}")
    }
}
