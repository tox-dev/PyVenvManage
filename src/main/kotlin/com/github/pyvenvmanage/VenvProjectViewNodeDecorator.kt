package com.github.pyvenvmanage

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.ui.SimpleTextAttributes

import com.jetbrains.python.icons.PythonIcons.Python.Virtualenv

class VenvProjectViewNodeDecorator : ProjectViewNodeDecorator {
    override fun decorate(
        node: ProjectViewNode<*>,
        data: PresentationData,
    ) {
        val pyVenvCfgPath = VenvUtils.getPyVenvCfg(node.getVirtualFile())
        if (pyVenvCfgPath != null) {
            val pythonVersion = VenvUtils.getPythonVersionFromPyVenv(pyvenvCfgPath = pyVenvCfgPath)
            val fileName: String? = data.getPresentableText()
            data.clearText()
            data.addText(fileName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            data.addText(" [" + pythonVersion + "]", SimpleTextAttributes.GRAY_ATTRIBUTES)
            data.setIcon(Virtualenv)
        }
    }
}
