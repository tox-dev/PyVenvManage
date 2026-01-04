package com.github.pyvenvmanage.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

import com.github.pyvenvmanage.VenvInfo

enum class DecorationField(
    val displayName: String,
) {
    VERSION("Python version"),
    IMPLEMENTATION("Python implementation"),
    SYSTEM("Is a system site package"),
    CREATOR("Virtual environment creator"),
}

@Service(Service.Level.APP)
@State(
    name = "PyVenvManageSettings",
    storages = [Storage("PyVenvManageSettings.xml")],
)
class PyVenvManageSettings : PersistentStateComponent<PyVenvManageSettings.SettingsState> {
    private var state = SettingsState()

    data class SettingsState(
        var prefix: String = " [",
        var suffix: String = "]",
        var separator: String = " - ",
        var fields: List<String> = DecorationField.entries.map { it.name },
    )

    override fun getState(): SettingsState = state

    override fun loadState(state: SettingsState) {
        this.state = state
    }

    var prefix: String
        get() = state.prefix
        set(value) {
            state.prefix = value
        }

    var suffix: String
        get() = state.suffix
        set(value) {
            state.suffix = value
        }

    var separator: String
        get() = state.separator
        set(value) {
            state.separator = value
        }

    var fields: List<DecorationField>
        get() = state.fields.mapNotNull { name -> DecorationField.entries.find { it.name == name } }
        set(value) {
            state.fields = value.map { it.name }
        }

    fun formatDecoration(info: VenvInfo): String {
        val values =
            fields.mapNotNull { field ->
                when (field) {
                    DecorationField.VERSION -> info.version
                    DecorationField.IMPLEMENTATION -> info.implementation
                    DecorationField.SYSTEM -> if (info.includeSystemSitePackages) "SYSTEM" else null
                    DecorationField.CREATOR -> info.creator?.removePrefix(" - ")
                }
            }
        return if (values.isEmpty()) "" else prefix + values.joinToString(separator) + suffix
    }

    companion object {
        fun getInstance(): PyVenvManageSettings = service()
    }
}
