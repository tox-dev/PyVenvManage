package com.github.pyvenvmanage

import java.nio.file.Files
import java.nio.file.Path

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent

class VenvVersionCacheTest {
    @TempDir
    lateinit var tempDir: Path

    private lateinit var virtualFileManager: VirtualFileManager
    private var capturedListener: AsyncFileListener? = null

    @BeforeEach
    fun setUp() {
        mockkObject(VenvUtils)
        mockkStatic(VirtualFileManager::class)

        virtualFileManager = mockk(relaxed = true)
        every { VirtualFileManager.getInstance() } returns virtualFileManager

        val listenerSlot = slot<AsyncFileListener>()
        every {
            virtualFileManager.addAsyncFileListener(capture(listenerSlot), any<Disposable>())
        } answers {
            capturedListener = listenerSlot.captured
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(VenvUtils)
        unmockkStatic(VirtualFileManager::class)
        capturedListener = null
    }

    @Test
    fun `getVersion returns cached version on second call`() {
        val pyvenvCfgPath = tempDir.resolve("pyvenv.cfg")
        Files.writeString(pyvenvCfgPath, "version = 3.11.0")

        every { VenvUtils.getVenvInfo(pyvenvCfgPath) } returns VenvInfo("3.11.0", "CPython", false)

        val cache = VenvVersionCache()

        val firstResult = cache.getVersion(pyvenvCfgPath.toString())
        val secondResult = cache.getVersion(pyvenvCfgPath.toString())

        assertEquals("3.11.0", firstResult)
        assertEquals("3.11.0", secondResult)
        verify(exactly = 1) { VenvUtils.getVenvInfo(pyvenvCfgPath) }
    }

    @Test
    fun `getVersion returns null when version not found`() {
        val pyvenvCfgPath = tempDir.resolve("pyvenv.cfg")
        Files.writeString(pyvenvCfgPath, "home = /usr/bin")

        every { VenvUtils.getVenvInfo(pyvenvCfgPath) } returns null

        val cache = VenvVersionCache()
        val result = cache.getVersion(pyvenvCfgPath.toString())

        assertNull(result)
    }

    @Test
    fun `getInfo returns cached info on second call`() {
        val pyvenvCfgPath = tempDir.resolve("pyvenv.cfg")
        Files.writeString(pyvenvCfgPath, "version = 3.11.0\nimplementation = CPython")

        every { VenvUtils.getVenvInfo(pyvenvCfgPath) } returns VenvInfo("3.11.0", "CPython", false)

        val cache = VenvVersionCache()

        val firstResult = cache.getInfo(pyvenvCfgPath.toString())
        val secondResult = cache.getInfo(pyvenvCfgPath.toString())

        assertEquals(VenvInfo("3.11.0", "CPython"), firstResult)
        assertEquals(VenvInfo("3.11.0", "CPython"), secondResult)
        verify(exactly = 1) { VenvUtils.getVenvInfo(pyvenvCfgPath) }
    }

    @Test
    fun `getInfo returns null when info not found`() {
        val pyvenvCfgPath = tempDir.resolve("pyvenv.cfg")
        Files.writeString(pyvenvCfgPath, "home = /usr/bin")

        every { VenvUtils.getVenvInfo(pyvenvCfgPath) } returns null

        val cache = VenvVersionCache()
        val result = cache.getInfo(pyvenvCfgPath.toString())

        assertNull(result)
    }

    @Test
    fun `invalidate removes entry from cache`() {
        val pyvenvCfgPath = tempDir.resolve("pyvenv.cfg")
        Files.writeString(pyvenvCfgPath, "version = 3.11.0")

        every { VenvUtils.getVenvInfo(pyvenvCfgPath) } returns VenvInfo("3.11.0", "CPython", false) andThen
            VenvInfo("3.12.0", "CPython", false)

        val cache = VenvVersionCache()

        cache.getVersion(pyvenvCfgPath.toString())
        cache.invalidate(pyvenvCfgPath.toString())
        val afterInvalidate = cache.getVersion(pyvenvCfgPath.toString())

        assertEquals("3.12.0", afterInvalidate)
        verify(exactly = 2) { VenvUtils.getVenvInfo(pyvenvCfgPath) }
    }

    @Test
    fun `clear removes all entries from cache`() {
        val pyvenvCfgPath1 = tempDir.resolve("venv1/pyvenv.cfg")
        val pyvenvCfgPath2 = tempDir.resolve("venv2/pyvenv.cfg")
        Files.createDirectories(pyvenvCfgPath1.parent)
        Files.createDirectories(pyvenvCfgPath2.parent)
        Files.writeString(pyvenvCfgPath1, "version = 3.11.0")
        Files.writeString(pyvenvCfgPath2, "version = 3.12.0")

        every { VenvUtils.getVenvInfo(pyvenvCfgPath1) } returns VenvInfo("3.11.0", "CPython") andThen
            VenvInfo("3.11.1", "CPython", false)
        every { VenvUtils.getVenvInfo(pyvenvCfgPath2) } returns VenvInfo("3.12.0", "CPython", false) andThen
            VenvInfo("3.12.1", "CPython", false)

        val cache = VenvVersionCache()

        cache.getVersion(pyvenvCfgPath1.toString())
        cache.getVersion(pyvenvCfgPath2.toString())
        cache.clear()
        val after1 = cache.getVersion(pyvenvCfgPath1.toString())
        val after2 = cache.getVersion(pyvenvCfgPath2.toString())

        assertEquals("3.11.1", after1)
        assertEquals("3.12.1", after2)
    }

    @Test
    fun `dispose clears the cache`() {
        val pyvenvCfgPath = tempDir.resolve("pyvenv.cfg")
        Files.writeString(pyvenvCfgPath, "version = 3.11.0")

        every { VenvUtils.getVenvInfo(pyvenvCfgPath) } returns VenvInfo("3.11.0", "CPython", false) andThen
            VenvInfo("3.12.0", "CPython", false)

        val cache = VenvVersionCache()

        cache.getVersion(pyvenvCfgPath.toString())
        cache.dispose()
        val afterDispose = cache.getVersion(pyvenvCfgPath.toString())

        assertEquals("3.12.0", afterDispose)
    }

    @Test
    fun `registers async file listener on init`() {
        VenvVersionCache()

        verify { virtualFileManager.addAsyncFileListener(any(), any<Disposable>()) }
    }

    @Nested
    inner class FileListenerTest {
        @Test
        fun `file listener invalidates cache on pyvenv cfg content change`() {
            val pyvenvCfgPath = tempDir.resolve("pyvenv.cfg")
            Files.writeString(pyvenvCfgPath, "version = 3.11.0")

            every { VenvUtils.getVenvInfo(pyvenvCfgPath) } returns VenvInfo("3.11.0", "CPython", false) andThen
                VenvInfo("3.12.0", "CPython", false)

            val cache = VenvVersionCache()
            cache.getVersion(pyvenvCfgPath.toString())

            val event: VFileContentChangeEvent = mockk(relaxed = true)
            every { event.path } returns pyvenvCfgPath.toString()

            val changeApplier = capturedListener?.prepareChange(listOf(event))
            changeApplier?.afterVfsChange()

            val afterChange = cache.getVersion(pyvenvCfgPath.toString())
            assertEquals("3.12.0", afterChange)
        }

        @Test
        fun `file listener invalidates cache on pyvenv cfg delete`() {
            val pyvenvCfgPath = tempDir.resolve("pyvenv.cfg")
            Files.writeString(pyvenvCfgPath, "version = 3.11.0")

            every { VenvUtils.getVenvInfo(pyvenvCfgPath) } returns VenvInfo("3.11.0", "CPython", false) andThen null

            val cache = VenvVersionCache()
            cache.getVersion(pyvenvCfgPath.toString())

            val event: VFileDeleteEvent = mockk(relaxed = true)
            every { event.path } returns pyvenvCfgPath.toString()

            val changeApplier = capturedListener?.prepareChange(listOf(event))
            changeApplier?.afterVfsChange()

            val afterDelete = cache.getVersion(pyvenvCfgPath.toString())
            assertNull(afterDelete)
        }

        @Test
        fun `file listener ignores non-pyvenv cfg files`() {
            val pyvenvCfgPath = tempDir.resolve("pyvenv.cfg")
            Files.writeString(pyvenvCfgPath, "version = 3.11.0")

            every { VenvUtils.getVenvInfo(pyvenvCfgPath) } returns VenvInfo("3.11.0", "CPython", false)

            val cache = VenvVersionCache()
            cache.getVersion(pyvenvCfgPath.toString())

            val event: VFileContentChangeEvent = mockk(relaxed = true)
            every { event.path } returns "/some/other/file.txt"

            val changeApplier = capturedListener?.prepareChange(listOf(event))

            assertNull(changeApplier)
            verify(exactly = 1) { VenvUtils.getVenvInfo(pyvenvCfgPath) }
        }
    }

    @Test
    fun `getInstance returns cache instance`() {
        val application: Application = mockk(relaxed = true)
        val mockCache: VenvVersionCache = mockk(relaxed = true)

        mockkStatic(ApplicationManager::class)
        every { ApplicationManager.getApplication() } returns application
        every { application.getService(VenvVersionCache::class.java) } returns mockCache

        val instance = VenvVersionCache.getInstance()

        assertEquals(mockCache, instance)
        unmockkStatic(ApplicationManager::class)
    }
}
