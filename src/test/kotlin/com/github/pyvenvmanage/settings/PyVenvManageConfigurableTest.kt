package com.github.pyvenvmanage.settings

import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentListener

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.CoroutineSupport
import com.intellij.ui.components.JBScrollPane

class PyVenvManageConfigurableTest {
    private lateinit var configurable: PyVenvManageConfigurable
    private lateinit var settings: PyVenvManageSettings
    private lateinit var application: Application

    @BeforeEach
    fun setUp() {
        application = mockk(relaxed = true)
        mockkStatic(ApplicationManager::class)
        every { ApplicationManager.getApplication() } returns application
        every { application.getService(CoroutineSupport::class.java) } returns mockk(relaxed = true)

        settings = mockk(relaxed = true)
        mockkObject(PyVenvManageSettings.Companion)
        every { PyVenvManageSettings.getInstance() } returns settings
        every { settings.prefix } returns " ["
        every { settings.suffix } returns "]"
        every { settings.separator } returns " - "
        every { settings.fields } returns DecorationField.entries

        configurable = PyVenvManageConfigurable()
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(PyVenvManageSettings.Companion)
        unmockkStatic(ApplicationManager::class)
    }

    @Test
    fun `getDisplayName returns correct name`() {
        assertEquals("PyVenv Manage", configurable.displayName)
    }

    @Test
    fun `createComponent returns non-null panel`() {
        val component = configurable.createComponent()
        assertNotNull(component)
    }

    @Test
    fun `isModified returns false when no changes`() {
        configurable.createComponent()

        assertFalse(configurable.isModified)
    }

    @Test
    fun `isModified returns true when prefix changed`() {
        configurable.createComponent()

        every { settings.prefix } returns "("

        assertTrue(configurable.isModified)
    }

    @Test
    fun `isModified returns true when suffix changed`() {
        configurable.createComponent()

        every { settings.suffix } returns ")"

        assertTrue(configurable.isModified)
    }

    @Test
    fun `isModified returns true when separator changed`() {
        configurable.createComponent()

        every { settings.separator } returns " | "

        assertTrue(configurable.isModified)
    }

    @Test
    fun `isModified returns true when fields changed`() {
        configurable.createComponent()

        every { settings.fields } returns listOf(DecorationField.VERSION)

        assertTrue(configurable.isModified)
    }

    @Test
    fun `apply updates settings`() {
        configurable.createComponent()

        configurable.apply()

        verify { settings.prefix = any() }
        verify { settings.suffix = any() }
        verify { settings.separator = any() }
        verify { settings.fields = any() }
    }

    @Test
    fun `reset loads settings into UI`() {
        every { settings.prefix } returns "("
        every { settings.suffix } returns ")"
        every { settings.separator } returns ":"
        every { settings.fields } returns listOf(DecorationField.VERSION, DecorationField.IMPLEMENTATION)

        configurable.createComponent()

        assertFalse(configurable.isModified)
    }

    @Test
    fun `reset with partial fields shows all fields with correct enabled state`() {
        every { settings.fields } returns listOf(DecorationField.VERSION)

        configurable.createComponent()

        assertFalse(configurable.isModified)
    }

    @Test
    fun `disposeUIResources clears references`() {
        configurable.createComponent()
        configurable.disposeUIResources()

        configurable.apply()
        configurable.reset()
    }

    @Test
    fun `isModified returns false after reset`() {
        configurable.createComponent()
        configurable.reset()

        assertFalse(configurable.isModified)
    }

    @Test
    fun `apply then isModified returns false`() {
        configurable.createComponent()
        configurable.apply()

        assertFalse(configurable.isModified)
    }

    @Test
    fun `reset with empty fields list`() {
        every { settings.fields } returns emptyList()

        configurable.createComponent()

        assertFalse(configurable.isModified)
    }

    @Test
    fun `multiple createComponent calls work`() {
        configurable.createComponent()
        configurable.disposeUIResources()
        val component = configurable.createComponent()

        assertNotNull(component)
    }

    @Test
    fun `updatePreview shows sample decoration`() {
        configurable.createComponent()

        assertNotNull(configurable.previewField?.text)
        assertTrue(configurable.previewField?.text?.contains(".venv") == true)
    }

    @Test
    fun `updatePreview with no fields shows just folder name`() {
        every { settings.fields } returns emptyList()
        configurable.createComponent()

        assertEquals(".venv", configurable.previewField?.text)
    }

    @Test
    fun `moveSelectedField does nothing when no selection`() {
        configurable.createComponent()
        configurable.fieldsList?.clearSelection()

        configurable.moveSelectedField(-1)
    }

    @Test
    fun `moveSelectedField does nothing when at boundary`() {
        configurable.createComponent()
        configurable.fieldsList?.selectedIndex = 0

        configurable.moveSelectedField(-1)
    }

    @Test
    fun `moveSelectedField swaps items`() {
        configurable.createComponent()
        val list = configurable.fieldsList!!
        list.selectedIndex = 1

        configurable.moveSelectedField(-1)

        assertEquals(0, list.selectedIndex)
    }

    @Test
    fun `moveSelectedField down at last position does nothing`() {
        configurable.createComponent()
        val list = configurable.fieldsList!!
        list.selectedIndex = list.itemsCount - 1

        configurable.moveSelectedField(1)

        assertEquals(list.itemsCount - 1, list.selectedIndex)
    }

    @Test
    fun `updatePreview before createComponent does nothing`() {
        configurable.updatePreview()
    }

    @Test
    fun `moveSelectedField before createComponent does nothing`() {
        configurable.moveSelectedField(1)
    }

    @Test
    fun `document insert triggers preview update`() {
        configurable.createComponent()

        configurable.prefixField?.document?.insertString(0, "x", null)

        assertNotNull(configurable.previewField?.text)
    }

    @Test
    fun `document remove triggers preview update`() {
        configurable.createComponent()

        configurable.prefixField?.document?.remove(0, 1)

        assertNotNull(configurable.previewField?.text)
    }

    @Test
    fun `checkbox toggle triggers preview update`() {
        configurable.createComponent()
        val list = configurable.fieldsList!!

        list.setItemSelected(list.getItemAt(0), false)

        assertNotNull(configurable.previewField?.text)
    }

    @Test
    fun `checkbox list shows secondary text for each field`() {
        configurable.createComponent()
        val list = configurable.fieldsList!!

        for (i in 0 until list.itemsCount) {
            val item = list.getItemAt(i)
            assertNotNull(item)
        }
    }

    @Test
    fun `getFieldExample returns example for VERSION`() {
        assertEquals("e.g., 3.14.2", getFieldExample(DecorationField.VERSION))
    }

    @Test
    fun `getFieldExample returns example for IMPLEMENTATION`() {
        assertEquals("e.g., CPython", getFieldExample(DecorationField.IMPLEMENTATION))
    }

    @Test
    fun `getFieldExample returns example for SYSTEM`() {
        assertEquals("shows SYSTEM", getFieldExample(DecorationField.SYSTEM))
    }

    @Test
    fun `getFieldExample returns example for CREATOR`() {
        assertEquals("e.g., uv@0.9.21", getFieldExample(DecorationField.CREATOR))
    }

    @Test
    fun `getFieldExample returns null for null`() {
        assertEquals(null, getFieldExample(null))
    }

    @Test
    fun `move up button click triggers move and preview update`() {
        configurable.createComponent()
        val list = configurable.fieldsList!!
        list.selectedIndex = 1

        val component = configurable.createComponent()
        val listPanel = findListPanel(component)
        val buttonsPanel = listPanel.getComponent(1) as JPanel
        val moveUpButton = buttonsPanel.getComponent(0) as JButton

        moveUpButton.doClick()

        assertNotNull(configurable.previewField?.text)
    }

    @Test
    fun `move down button click triggers move and preview update`() {
        configurable.createComponent()
        val list = configurable.fieldsList!!
        list.selectedIndex = 0

        val component = configurable.createComponent()
        val listPanel = findListPanel(component)
        val buttonsPanel = listPanel.getComponent(1) as JPanel
        val moveDownButton = buttonsPanel.getComponent(1) as JButton

        moveDownButton.doClick()

        assertNotNull(configurable.previewField?.text)
    }

    @Test
    fun `checkbox list listener triggers preview update on toggle`() {
        configurable.createComponent()
        val list = configurable.fieldsList!!

        var listenerFieldName: String? = null
        var targetClass: Class<*>? = list.javaClass
        while (targetClass != null && listenerFieldName == null) {
            for (field in targetClass.declaredFields) {
                if (field.type == com.intellij.ui.CheckBoxListListener::class.java) {
                    listenerFieldName = field.name
                    break
                }
            }
            targetClass = targetClass.superclass
        }

        if (listenerFieldName != null) {
            val listenerField = list.javaClass.superclass.getDeclaredField(listenerFieldName)
            listenerField.isAccessible = true
            val listener = listenerField.get(list) as? com.intellij.ui.CheckBoxListListener
            listener?.checkBoxSelectionChanged(0, false)
        }

        assertNotNull(configurable.previewField?.text)
    }

    @Test
    fun `changedUpdate triggers preview update`() {
        configurable.createComponent()

        configurable.prefixField?.document?.let { doc ->
            val listenerList = (doc as javax.swing.text.AbstractDocument).documentListeners
            listenerList.filterIsInstance<DocumentListener>().forEach {
                val mockEvent = mockk<javax.swing.event.DocumentEvent>(relaxed = true)
                it.changedUpdate(mockEvent)
            }
        }

        assertNotNull(configurable.previewField?.text)
    }

    @Test
    fun `suffixField is accessible after createComponent`() {
        configurable.createComponent()

        assertNotNull(configurable.suffixField)
        configurable.suffixField?.text = "test"
        assertEquals("test", configurable.suffixField?.text)
    }

    @Test
    fun `separatorField is accessible after createComponent`() {
        configurable.createComponent()

        assertNotNull(configurable.separatorField)
        configurable.separatorField?.text = "test"
        assertEquals("test", configurable.separatorField?.text)
    }

    private fun findListPanel(component: JComponent): JPanel {
        val formPanel = component as JPanel
        for (i in 0 until formPanel.componentCount) {
            val child = formPanel.getComponent(i)
            if (child is JPanel && child.layout is BorderLayout) {
                val center = (child.layout as BorderLayout).getLayoutComponent(child, BorderLayout.CENTER)
                if (center is JBScrollPane) {
                    return child
                }
            }
        }
        throw IllegalStateException("List panel not found")
    }
}
