package com.github.pyvenvmanage.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@Service(Service.Level.APP)
@State(
    name = "PyVenvManageSettings",
    storages = [Storage("PyVenvManageSettings.xml")],
)
class PyVenvManageSettings : PersistentStateComponent<PyVenvManageSettings.State> {
    private var state = State()

    data class State(
        var showVersionInProjectView: Boolean = true,
        var versionFormat: String = " [\$version]",
    )

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var showVersionInProjectView: Boolean
        get() = state.showVersionInProjectView
        set(value) {
            state.showVersionInProjectView = value
        }

    var versionFormat: String
        get() = state.versionFormat
        set(value) {
            state.versionFormat = value
        }

    fun formatVersion(version: String): String = versionFormat.replace("\$version", version)

    companion object {
        fun getInstance(): PyVenvManageSettings = service()
    }
}
