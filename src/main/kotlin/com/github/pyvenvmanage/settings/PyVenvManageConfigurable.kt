package com.github.pyvenvmanage.settings

import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder

class PyVenvManageConfigurable : Configurable {
    private var showVersionCheckBox: JCheckBox? = null
    private var versionFormatField: JTextField? = null

    override fun getDisplayName(): String = "PyVenv Manage"

    override fun createComponent(): JComponent {
        showVersionCheckBox = JCheckBox("Show Python version in project view")
        versionFormatField =
            JTextField().apply {
                toolTipText = "Use \$version as placeholder for the version number"
            }

        return FormBuilder
            .createFormBuilder()
            .addComponent(showVersionCheckBox!!)
            .addLabeledComponent(JBLabel("Version format:"), versionFormatField!!)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        val settings = PyVenvManageSettings.getInstance()
        return showVersionCheckBox?.isSelected != settings.showVersionInProjectView ||
            versionFormatField?.text != settings.versionFormat
    }

    override fun apply() {
        val settings = PyVenvManageSettings.getInstance()
        showVersionCheckBox?.isSelected?.let { settings.showVersionInProjectView = it }
        versionFormatField?.text?.let { settings.versionFormat = it }
    }

    override fun reset() {
        val settings = PyVenvManageSettings.getInstance()
        showVersionCheckBox?.isSelected = settings.showVersionInProjectView
        versionFormatField?.text = settings.versionFormat
    }

    override fun disposeUIResources() {
        showVersionCheckBox = null
        versionFormatField = null
    }
}
