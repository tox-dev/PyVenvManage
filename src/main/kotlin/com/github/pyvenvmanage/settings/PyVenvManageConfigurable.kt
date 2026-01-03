package com.github.pyvenvmanage.settings

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

import com.intellij.openapi.options.Configurable
import com.intellij.ui.CheckBoxList
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder

import com.github.pyvenvmanage.VenvInfo

class PyVenvManageConfigurable : Configurable {
    internal var prefixField: JTextField? = null
    internal var suffixField: JTextField? = null
    internal var separatorField: JTextField? = null
    internal var fieldsList: CheckBoxList<DecorationField>? = null
    internal var previewField: JBTextField? = null

    override fun getDisplayName(): String = "PyVenv Manage"

    override fun createComponent(): JComponent {
        prefixField = JTextField().apply { toolTipText = "Text before the decoration (e.g., ' [')" }
        suffixField = JTextField().apply { toolTipText = "Text after the decoration (e.g., ']')" }
        separatorField = JTextField().apply { toolTipText = "Text between fields (e.g., ' - ')" }

        fieldsList =
            object : CheckBoxList<DecorationField>() {
                override fun getSecondaryText(index: Int): String? = getFieldExample(getItemAt(index))
            }.apply {
                selectionMode = ListSelectionModel.SINGLE_SELECTION
                setCheckBoxListListener { _, _ ->
                    updatePreview()
                }
            }

        previewField =
            JBTextField().apply {
                isEditable = false
                toolTipText = "Preview of what the decoration will look like"
            }

        val textChangeListener =
            object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) = updatePreview()

                override fun removeUpdate(e: DocumentEvent?) = updatePreview()

                override fun changedUpdate(e: DocumentEvent?) = updatePreview()
            }

        prefixField?.document?.addDocumentListener(textChangeListener)
        suffixField?.document?.addDocumentListener(textChangeListener)
        separatorField?.document?.addDocumentListener(textChangeListener)

        val moveUpButton =
            JButton("↑").apply {
                toolTipText = "Move selected field up"
                addActionListener {
                    moveSelectedField(-1)
                    updatePreview()
                }
            }
        val moveDownButton =
            JButton("↓").apply {
                toolTipText = "Move selected field down"
                addActionListener {
                    moveSelectedField(1)
                    updatePreview()
                }
            }

        val buttonsPanel =
            JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
                add(moveUpButton)
                add(moveDownButton)
            }

        val scrollPane =
            JBScrollPane(fieldsList).apply {
                preferredSize = Dimension(0, 120)
            }

        val listPanel =
            JPanel(BorderLayout()).apply {
                add(scrollPane, BorderLayout.CENTER)
                add(buttonsPanel, BorderLayout.SOUTH)
            }

        reset()

        val fieldsLabel =
            JBLabel("Fields:").apply {
                toolTipText = "Check to enable, use arrows to reorder"
            }

        return FormBuilder
            .createFormBuilder()
            .addSeparator()
            .addComponent(JBLabel("Python Interpreter Decoration"))
            .addLabeledComponent(JBLabel("Prefix:"), prefixField!!)
            .addLabeledComponent(JBLabel("Suffix:"), suffixField!!)
            .addLabeledComponent(JBLabel("Separator:"), separatorField!!)
            .addLabeledComponent(fieldsLabel, listPanel)
            .addLabeledComponent(JBLabel("Preview:"), previewField!!)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    internal fun updatePreview() {
        val list = fieldsList ?: return
        val preview = previewField ?: return

        val enabledFields =
            (0 until list.itemsCount)
                .filter { list.isItemSelected(it) }
                .mapNotNull { list.getItemAt(it) }

        val sampleInfo = VenvInfo("3.14.2", "CPython", true, " - uv@0.9.21")
        val values =
            enabledFields.mapNotNull { field ->
                when (field) {
                    DecorationField.VERSION -> sampleInfo.version
                    DecorationField.IMPLEMENTATION -> sampleInfo.implementation
                    DecorationField.SYSTEM -> if (sampleInfo.includeSystemSitePackages) "SYSTEM" else null
                    DecorationField.CREATOR -> sampleInfo.creator?.removePrefix(" - ")
                }
            }

        val prefix = prefixField?.text ?: ""
        val suffix = suffixField?.text ?: ""
        val separator = separatorField?.text ?: ""

        val decoration = if (values.isEmpty()) "" else prefix + values.joinToString(separator) + suffix
        preview.text = ".venv$decoration"
    }

    internal fun moveSelectedField(direction: Int) {
        val list = fieldsList ?: return
        val selectedIndex = list.selectedIndex
        if (selectedIndex < 0) return

        val newIndex = selectedIndex + direction
        if (newIndex < 0 || newIndex >= list.itemsCount) return

        val currentItem = list.getItemAt(selectedIndex) ?: return
        val swapItem = list.getItemAt(newIndex) ?: return
        val currentChecked = list.isItemSelected(selectedIndex)
        val swapChecked = list.isItemSelected(newIndex)

        val items = getAllFieldsOrdered()
        val checkedStates = (0 until list.itemsCount).associate { list.getItemAt(it) to list.isItemSelected(it) }

        items[selectedIndex] = swapItem
        items[newIndex] = currentItem

        list.clear()
        items.forEach { field ->
            val wasChecked =
                when (field) {
                    currentItem -> currentChecked
                    swapItem -> swapChecked
                    else -> checkedStates[field] ?: false
                }
            list.addItem(field, field.displayName, wasChecked)
        }
        list.selectedIndex = newIndex
    }

    private fun getAllFieldsOrdered(): MutableList<DecorationField> {
        val list = fieldsList ?: return mutableListOf()
        return (0 until list.itemsCount).mapNotNull { list.getItemAt(it) }.toMutableList()
    }

    override fun isModified(): Boolean {
        val settings = PyVenvManageSettings.getInstance()
        return prefixField?.text != settings.prefix ||
            suffixField?.text != settings.suffix ||
            separatorField?.text != settings.separator ||
            getEnabledFields() != settings.fields
    }

    private fun getEnabledFields(): List<DecorationField> {
        val list = fieldsList ?: return emptyList()
        return (0 until list.itemsCount)
            .filter { list.isItemSelected(it) }
            .mapNotNull { list.getItemAt(it) }
    }

    override fun apply() {
        val settings = PyVenvManageSettings.getInstance()
        prefixField?.text?.let { settings.prefix = it }
        suffixField?.text?.let { settings.suffix = it }
        separatorField?.text?.let { settings.separator = it }
        settings.fields = getEnabledFields()
    }

    override fun reset() {
        val settings = PyVenvManageSettings.getInstance()
        prefixField?.text = settings.prefix
        suffixField?.text = settings.suffix
        separatorField?.text = settings.separator

        val list = fieldsList ?: return
        list.clear()

        val enabledFields = settings.fields.toSet()
        val orderedFields = settings.fields + DecorationField.entries.filter { it !in enabledFields }
        orderedFields.forEach { field ->
            list.addItem(field, field.displayName, field in enabledFields)
        }
        updatePreview()
    }

    override fun disposeUIResources() {
        prefixField = null
        suffixField = null
        separatorField = null
        fieldsList = null
        previewField = null
    }
}

internal fun getFieldExample(field: DecorationField?): String? =
    when (field) {
        DecorationField.VERSION -> "e.g., 3.14.2"
        DecorationField.IMPLEMENTATION -> "e.g., CPython"
        DecorationField.SYSTEM -> "shows SYSTEM"
        DecorationField.CREATOR -> "e.g., uv@0.9.21"
        null -> null
    }
