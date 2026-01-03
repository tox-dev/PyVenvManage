package com.github.pyvenvmanage

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.ui.SimpleTextAttributes

import com.jetbrains.python.icons.PythonIcons.Python.Virtualenv

import com.github.pyvenvmanage.settings.PyVenvManageSettings

class VenvProjectViewNodeDecorator : ProjectViewNodeDecorator {
    override fun decorate(
        node: ProjectViewNode<*>,
        data: PresentationData,
    ) {
        node.virtualFile?.let { vf ->
            thisLogger().debug("Checking node: ${vf.path}, isDir: ${vf.isDirectory}")
        }
        VenvUtils.getPyVenvCfg(node.virtualFile)?.let { pyVenvCfgPath ->
            thisLogger().debug("Found pyvenv.cfg at: $pyVenvCfgPath")
            val settings = PyVenvManageSettings.getInstance()
            val venvInfo = VenvVersionCache.getInstance().getInfo(pyVenvCfgPath.toString())
            thisLogger().debug("VenvInfo from cache: $venvInfo")
            venvInfo?.let { info ->
                data.presentableText?.let { fileName ->
                    val decoration = settings.formatDecoration(info)
                    thisLogger().debug("Decorating $fileName with: $decoration")
                    data.clearText()
                    data.addText(fileName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    data.addText(decoration, SimpleTextAttributes.GRAY_ATTRIBUTES)
                } ?: thisLogger().debug("No presentableText for decoration")
            } ?: thisLogger().debug("No venvInfo found for $pyVenvCfgPath")
            data.setIcon(Virtualenv)
        }
    }
}
