package com.github.pyvenvmanage

import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent

@Service(Service.Level.APP)
class VenvVersionCache : Disposable {
    private val cache = ConcurrentHashMap<String, Optional<VenvInfo>>()

    init {
        VirtualFileManager.getInstance().addAsyncFileListener(
            { events ->
                val pyvenvChanges =
                    events.filter { event ->
                        val path = event.path
                        path.endsWith("pyvenv.cfg") &&
                            (event is VFileContentChangeEvent || event is VFileDeleteEvent)
                    }
                if (pyvenvChanges.isNotEmpty()) {
                    object : AsyncFileListener.ChangeApplier {
                        override fun afterVfsChange() {
                            pyvenvChanges.forEach { event ->
                                invalidate(event.path)
                            }
                        }
                    }
                } else {
                    null
                }
            },
            this,
        )
    }

    fun getInfo(pyvenvCfgPath: String): VenvInfo? =
        cache
            .computeIfAbsent(pyvenvCfgPath) {
                Optional.ofNullable(
                    VenvUtils.getVenvInfo(
                        java.nio.file.Path
                            .of(it),
                    ),
                )
            }.orElse(null)

    fun getVersion(pyvenvCfgPath: String): String? = getInfo(pyvenvCfgPath)?.version

    fun invalidate(path: String) {
        cache.remove(path)
    }

    fun clear() {
        cache.clear()
    }

    override fun dispose() {
        cache.clear()
    }

    companion object {
        fun getInstance(): VenvVersionCache = service()
    }
}
