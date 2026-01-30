package com.github.pyvenvmanage.settings

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager

import com.github.pyvenvmanage.VenvInfo

class PyVenvManageSettingsTest {
    private lateinit var settings: PyVenvManageSettings

    @BeforeEach
    fun setUp() {
        settings = PyVenvManageSettings()
    }

    @Test
    fun `default prefix is space bracket`() {
        assertEquals(" [", settings.prefix)
    }

    @Test
    fun `default suffix is close bracket`() {
        assertEquals("]", settings.suffix)
    }

    @Test
    fun `default separator is dash with spaces`() {
        assertEquals(" - ", settings.separator)
    }

    @Test
    fun `default fields include all decoration fields in order`() {
        assertEquals(DecorationField.entries, settings.fields)
    }

    @Test
    fun `prefix can be set`() {
        settings.prefix = "("
        assertEquals("(", settings.prefix)
    }

    @Test
    fun `suffix can be set`() {
        settings.suffix = ")"
        assertEquals(")", settings.suffix)
    }

    @Test
    fun `separator can be set`() {
        settings.separator = " | "
        assertEquals(" | ", settings.separator)
    }

    @Test
    fun `fields can be set`() {
        settings.fields = listOf(DecorationField.VERSION, DecorationField.IMPLEMENTATION)
        assertEquals(listOf(DecorationField.VERSION, DecorationField.IMPLEMENTATION), settings.fields)
    }

    @Test
    fun `formatDecoration with all fields`() {
        val info = VenvInfo("3.11.0", "CPython", false, " - uv@0.9.18")
        val result = settings.formatDecoration(info)
        assertEquals(" [3.11.0 - CPython - uv@0.9.18]", result)
    }

    @Test
    fun `formatDecoration with version only`() {
        settings.fields = listOf(DecorationField.VERSION)
        val info = VenvInfo("3.11.0", "CPython", true, " - uv@0.9.18")
        val result = settings.formatDecoration(info)
        assertEquals(" [3.11.0]", result)
    }

    @Test
    fun `formatDecoration with null implementation skips it`() {
        val info = VenvInfo("3.11.0", null, false, null)
        val result = settings.formatDecoration(info)
        assertEquals(" [3.11.0]", result)
    }

    @Test
    fun `formatDecoration with system site packages`() {
        val info = VenvInfo("3.11.0", "CPython", true, null)
        val result = settings.formatDecoration(info)
        assertEquals(" [3.11.0 - CPython - SYSTEM]", result)
    }

    @Test
    fun `formatDecoration with custom prefix suffix separator`() {
        settings.prefix = "("
        settings.suffix = ")"
        settings.separator = " | "
        val info = VenvInfo("3.12.0", "PyPy", false, null)
        val result = settings.formatDecoration(info)
        assertEquals("(3.12.0 | PyPy)", result)
    }

    @Test
    fun `formatDecoration with reordered fields`() {
        settings.fields = listOf(DecorationField.IMPLEMENTATION, DecorationField.VERSION)
        val info = VenvInfo("3.11.0", "CPython", false, null)
        val result = settings.formatDecoration(info)
        assertEquals(" [CPython - 3.11.0]", result)
    }

    @Test
    fun `formatDecoration with empty fields returns empty string`() {
        settings.fields = emptyList()
        val info = VenvInfo("3.11.0", "CPython", true, " - uv@0.9.18")
        val result = settings.formatDecoration(info)
        assertEquals("", result)
    }

    @Test
    fun `formatDecoration strips creator prefix`() {
        settings.fields = listOf(DecorationField.CREATOR)
        val info = VenvInfo("3.11.0", null, false, " - virtualenv@20.35.4")
        val result = settings.formatDecoration(info)
        assertEquals(" [virtualenv@20.35.4]", result)
    }

    @Test
    fun `getState returns current state`() {
        settings.prefix = "["
        settings.suffix = "]"
        settings.separator = ":"
        settings.fields = listOf(DecorationField.VERSION)

        val state = settings.state

        assertEquals("[", state.prefix)
        assertEquals("]", state.suffix)
        assertEquals(":", state.separator)
        assertEquals(listOf("VERSION"), state.fields)
    }

    @Test
    fun `loadState updates settings`() {
        val newState =
            PyVenvManageSettings.SettingsState(
                prefix = "<<",
                suffix = ">>",
                separator = " / ",
                fields = listOf("IMPLEMENTATION", "VERSION"),
            )

        settings.loadState(newState)

        assertEquals("<<", settings.prefix)
        assertEquals(">>", settings.suffix)
        assertEquals(" / ", settings.separator)
        assertEquals(listOf(DecorationField.IMPLEMENTATION, DecorationField.VERSION), settings.fields)
    }

    @Test
    fun `fields getter ignores invalid field names`() {
        val state =
            PyVenvManageSettings.SettingsState(
                fields = listOf("VERSION", "INVALID_FIELD", "IMPLEMENTATION"),
            )
        settings.loadState(state)

        assertEquals(listOf(DecorationField.VERSION, DecorationField.IMPLEMENTATION), settings.fields)
    }

    @Test
    fun `default dismissedPythonWarning is false`() {
        assertEquals(false, settings.dismissedPythonWarning)
    }

    @Test
    fun `dismissedPythonWarning can be set`() {
        settings.dismissedPythonWarning = true
        assertEquals(true, settings.dismissedPythonWarning)
    }

    @Test
    fun `getInstance returns settings instance`() {
        val application: Application = mockk(relaxed = true)
        val mockSettings: PyVenvManageSettings = mockk(relaxed = true)

        mockkStatic(ApplicationManager::class)
        every { ApplicationManager.getApplication() } returns application
        every { application.getService(PyVenvManageSettings::class.java) } returns mockSettings

        val instance = PyVenvManageSettings.getInstance()

        assertNotNull(instance)
        unmockkStatic(ApplicationManager::class)
    }
}
