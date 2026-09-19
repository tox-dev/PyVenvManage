package com.github.pyvenvmanage.sdk

import java.nio.file.Path
import javax.swing.Icon

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.util.IconLoader
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
    // The platform's per-flavor *Icons classes are impl-internal, but the SVG they wrap is resolvable
    // by path through our own class loader (the python modules are a declared plugin dependency).
    // Offering several names keeps this working across platform versions that rename the resource,
    // the way 2026.3 renamed the pipenv icon from pythonClosed.svg to pipenv.svg.
    private fun findIcon(vararg resourcePaths: String): Icon {
        val loader = SdkFactory::class.java.classLoader
        return resourcePaths
            .firstOrNull { loader.getResource(it) != null }
            ?.let { IconLoader.findIcon(it, loader) }
            ?: PythonVenvIcons.VirtualEnv
    }

    private val CONDA_ICON = findIcon("icons/com/intellij/python/community/impl/conda/expui/anaconda.svg")
    private val POETRY_ICON = findIcon("icons/intellij/python/community/impl/poetry/common/expui/poetry.svg")
    private val HATCH_ICON = findIcon("icons/com/intellij/python/hatch/expui/logo.svg")
    private val UV_ICON = findIcon("images/intellij/python/uv/common/expui/uv.svg")
    private val PIPENV_ICON =
        findIcon(
            "icons/com/intellij/python/community/impl/pipenv/expui/pipenv.svg",
            "icons/com/intellij/python/community/impl/pipenv/expui/pythonClosed.svg",
        )

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
            PythonEnvironmentType.CONDA -> CONDA_ICON
            PythonEnvironmentType.POETRY -> POETRY_ICON
            PythonEnvironmentType.HATCH -> HATCH_ICON
            PythonEnvironmentType.UV -> UV_ICON
            PythonEnvironmentType.PIPENV -> PIPENV_ICON
            PythonEnvironmentType.VIRTUALENV -> PythonVenvIcons.VirtualEnv
            PythonEnvironmentType.SYSTEM -> PythonVenvIcons.VirtualEnv
        }
}
