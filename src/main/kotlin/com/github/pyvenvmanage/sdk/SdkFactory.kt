package com.github.pyvenvmanage.sdk

import java.nio.file.Path
import javax.swing.Icon

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.util.IconLoader
import com.intellij.python.community.impl.conda.icons.PythonCommunityImplCondaIcons
import com.intellij.python.community.impl.pipenv.icons.PythonCommunityImplPipenvIcons
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
    // 2026.2 exposes the pipenv icon as PIPENV_ICON (pythonClosed.svg), 2026.3 as
    // PythonCommunityImplPipenvIcons.Pipenv (pipenv.svg). Resolving the resource keeps both working.
    private val PIPENV_ICON: Icon =
        sequenceOf("pipenv", "pythonClosed")
            .mapNotNull {
                IconLoader.findIcon(
                    "icons/com/intellij/python/community/impl/pipenv/expui/$it.svg",
                    PythonCommunityImplPipenvIcons::class.java.classLoader,
                )
            }.firstOrNull() ?: PythonVenvIcons.VirtualEnv

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
                HatchSdkAdditionalData(findHatchWorkingDir(projectBasePath) ?: projectBasePath, null)
            }

            PythonEnvironmentType.UV -> {
                val uvWorkingDir = findUvWorkingDir(projectBasePath) ?: projectBasePath
                val venvPath = Path.of(pythonExecutable).parent?.parent
                UvSdkAdditionalData(uvWorkingDir, null, venvPath?.toString(), null)
            }

            PythonEnvironmentType.POETRY -> {
                PythonSdkAdditionalData(PyFlavorAndData(PyFlavorData.Empty, PyPoetrySdkFlavor), projectBasePath)
            }

            PythonEnvironmentType.PIPENV -> {
                PythonSdkAdditionalData(PyFlavorAndData(PyFlavorData.Empty, PyPipEnvSdkFlavor), projectBasePath)
            }

            PythonEnvironmentType.CONDA,
            PythonEnvironmentType.VIRTUALENV,
            PythonEnvironmentType.SYSTEM,
            -> {
                PythonSdkAdditionalData(
                    PyFlavorAndData(PyFlavorData.Empty, VirtualEnvSdkFlavor.getInstance()),
                    projectBasePath,
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
