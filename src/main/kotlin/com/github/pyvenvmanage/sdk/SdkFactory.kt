package com.github.pyvenvmanage.sdk

import java.nio.file.Path
import javax.swing.Icon

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.python.community.impl.conda.icons.PythonCommunityImplCondaIcons
import com.intellij.python.community.impl.pipenv.PIPENV_ICON
import com.intellij.python.community.impl.poetry.common.icons.PythonCommunityImplPoetryCommonIcons
import com.intellij.python.hatch.icons.PythonHatchIcons
import com.intellij.python.uv.common.icons.PythonUvCommonIcons
import com.intellij.python.venv.icons.PythonVenvIcons
import com.intellij.python.venv.sdk.flavors.VirtualEnvSdkFlavor

import com.jetbrains.python.hatch.sdk.HatchSdkAdditionalData
import com.jetbrains.python.sdk.PythonSdkAdditionalData
import com.jetbrains.python.sdk.PythonSdkType
import com.jetbrains.python.sdk.flavors.PyFlavorAndData
import com.jetbrains.python.sdk.flavors.PyFlavorData
import com.jetbrains.python.sdk.pipenv.PyPipEnvSdkFlavor
import com.jetbrains.python.sdk.poetry.PyPoetrySdkFlavor
import com.jetbrains.python.sdk.uv.UvSdkAdditionalData

object SdkFactory {
    fun createSdk(
        pythonExecutable: String,
        envType: PythonEnvironmentType,
        projectBasePath: Path,
    ): Sdk? {
        val sdkType = PythonSdkType.getInstance()
        val suggestedName = SdkConfigurationUtil.createUniqueSdkName(sdkType, pythonExecutable, emptyList())

        val sdk =
            WriteAction.computeAndWait<Sdk?, Exception> {
                val sdk = ProjectJdkImpl(suggestedName, sdkType)

                val modificator = sdk.sdkModificator
                modificator.homePath = pythonExecutable

                val additionalData = createAdditionalData(envType, pythonExecutable, projectBasePath)

                val venvPath = Path.of(pythonExecutable).parent?.parent
                if (venvPath != null && venvPath.startsWith(projectBasePath)) {
                    additionalData.setAssociatedModulePath(projectBasePath.toString())
                }

                modificator.sdkAdditionalData = additionalData
                modificator.commitChanges()

                SdkConfigurationUtil.addSdk(sdk)
                sdk
            }

        sdk?.let { sdkType.setupSdkPaths(it) }

        return sdk
    }

    private fun createAdditionalData(
        envType: PythonEnvironmentType,
        pythonExecutable: String,
        projectBasePath: Path,
    ): PythonSdkAdditionalData =
        when (envType) {
            PythonEnvironmentType.HATCH -> {
                val hatchWorkingDir = findHatchWorkingDir(projectBasePath)
                HatchSdkAdditionalData(hatchWorkingDir, null)
            }

            PythonEnvironmentType.UV -> {
                val uvWorkingDir = findUvWorkingDir(projectBasePath)
                val venvPath = Path.of(pythonExecutable).parent?.parent
                UvSdkAdditionalData(uvWorkingDir, null, venvPath?.toString(), null)
            }

            PythonEnvironmentType.POETRY -> {
                PythonSdkAdditionalData(
                    PyFlavorAndData(PyFlavorData.Empty, PyPoetrySdkFlavor),
                )
            }

            PythonEnvironmentType.PIPENV -> {
                PythonSdkAdditionalData(
                    PyFlavorAndData(PyFlavorData.Empty, PyPipEnvSdkFlavor),
                )
            }

            PythonEnvironmentType.CONDA,
            PythonEnvironmentType.VIRTUALENV,
            PythonEnvironmentType.SYSTEM,
            -> {
                PythonSdkAdditionalData(
                    PyFlavorAndData(PyFlavorData.Empty, VirtualEnvSdkFlavor.getInstance()),
                )
            }
        }

    private fun findHatchWorkingDir(projectBasePath: Path): Path? {
        var current: Path? = projectBasePath
        while (current != null) {
            val pyprojectToml = current.resolve("pyproject.toml")
            if (pyprojectToml.toFile().exists()) {
                val content = pyprojectToml.toFile().readText()
                if (content.contains("[tool.hatch")) {
                    return current
                }
            }
            current = current.parent
        }
        return null
    }

    private fun findUvWorkingDir(projectBasePath: Path): Path? {
        var current: Path? = projectBasePath
        while (current != null) {
            if (current.resolve("uv.lock").toFile().exists()) {
                return current
            }
            current = current.parent
        }
        return null
    }

    fun getIconForEnvironmentType(envType: PythonEnvironmentType): Icon =
        when (envType) {
            PythonEnvironmentType.CONDA -> PythonCommunityImplCondaIcons.Anaconda
            PythonEnvironmentType.POETRY -> PythonCommunityImplPoetryCommonIcons.Poetry
            PythonEnvironmentType.HATCH -> PythonHatchIcons.Logo
            PythonEnvironmentType.UV -> PythonUvCommonIcons.UV
            PythonEnvironmentType.PIPENV -> PIPENV_ICON
            PythonEnvironmentType.VIRTUALENV -> PythonVenvIcons.VirtualEnv
            PythonEnvironmentType.SYSTEM -> PythonVenvIcons.VirtualEnv
        }
}
