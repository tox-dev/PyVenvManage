package com.github.pyvenvmanage

import java.util.concurrent.ConcurrentHashMap

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.ui.SimpleTextAttributes

import com.jetbrains.python.icons.PythonIcons.Python.Virtualenv

class VenvProjectViewNodeDecorator : ProjectViewNodeDecorator {
    private val versionCache = ConcurrentHashMap<String, String?>()

    override fun decorate(
        node: ProjectViewNode<*>,
        data: PresentationData,
    ) {
        VenvUtils.getPyVenvCfg(node.virtualFile)?.let { pyVenvCfgPath ->
            val pythonVersion =
                versionCache.computeIfAbsent(pyVenvCfgPath.toString()) {
                    VenvUtils.getPythonVersionFromPyVenv(pyVenvCfgPath)
                }
            pythonVersion?.let { version ->
                data.presentableText?.let { fileName ->
                    data.clearText()
                    data.addText(fileName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    data.addText(" [$version]", SimpleTextAttributes.GRAY_ATTRIBUTES)
                }
            }
            data.setIcon(Virtualenv)
        }
    }
}
