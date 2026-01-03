package com.github.pyvenvmanage

import java.nio.file.Files
import java.nio.file.Path

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes

import com.github.pyvenvmanage.settings.PyVenvManageSettings

class VenvProjectViewNodeDecoratorTest {
    private lateinit var decorator: VenvProjectViewNodeDecorator
    private lateinit var node: ProjectViewNode<*>
    private lateinit var data: PresentationData
    private lateinit var virtualFile: VirtualFile

    @BeforeEach
    fun setUp() {
        decorator = VenvProjectViewNodeDecorator()
        node = mockk(relaxed = true)
        data = mockk(relaxed = true)
        virtualFile = mockk(relaxed = true)

        every { node.virtualFile } returns virtualFile
    }

    @Nested
    inner class DecorateTest {
        private lateinit var versionCache: VenvVersionCache
        private lateinit var settings: PyVenvManageSettings

        @BeforeEach
        fun setUpMocks() {
            mockkObject(VenvUtils)
            versionCache = mockk(relaxed = true)
            mockkObject(VenvVersionCache.Companion)
            every { VenvVersionCache.getInstance() } returns versionCache
            settings = mockk(relaxed = true)
            mockkObject(PyVenvManageSettings.Companion)
            every { PyVenvManageSettings.getInstance() } returns settings
            every { settings.showVersionInProjectView } returns true
            every { settings.formatVersion(any()) } answers { " [${firstArg<String>()}]" }
        }

        @AfterEach
        fun tearDown() {
            unmockkObject(VenvUtils)
            unmockkObject(VenvVersionCache.Companion)
            unmockkObject(PyVenvManageSettings.Companion)
        }

        @Test
        fun `does nothing when virtualFile is null`() {
            every { node.virtualFile } returns null

            decorator.decorate(node, data)

            verify(exactly = 0) { data.setIcon(any()) }
            verify(exactly = 0) { data.clearText() }
        }

        @Test
        fun `does nothing when not a venv directory`() {
            every { VenvUtils.getPyVenvCfg(virtualFile) } returns null

            decorator.decorate(node, data)

            verify(exactly = 0) { data.setIcon(any()) }
            verify(exactly = 0) { data.clearText() }
        }

        @Test
        fun `sets icon when venv detected`(
            @TempDir tempDir: Path,
        ) {
            val pyvenvCfgPath = tempDir.resolve("pyvenv.cfg")
            Files.writeString(pyvenvCfgPath, "version = 3.11.0")

            every { VenvUtils.getPyVenvCfg(virtualFile) } returns pyvenvCfgPath
            every { versionCache.getVersion(pyvenvCfgPath.toString()) } returns "3.11.0"
            every { data.presentableText } returns "venv"

            decorator.decorate(node, data)

            verify { data.setIcon(any()) }
        }

        @Test
        fun `adds version text when version available`(
            @TempDir tempDir: Path,
        ) {
            val pyvenvCfgPath = tempDir.resolve("pyvenv.cfg")
            Files.writeString(pyvenvCfgPath, "version = 3.11.0")

            every { VenvUtils.getPyVenvCfg(virtualFile) } returns pyvenvCfgPath
            every { versionCache.getVersion(pyvenvCfgPath.toString()) } returns "3.11.0"
            every { data.presentableText } returns "venv"

            decorator.decorate(node, data)

            verify { data.clearText() }
            verify { data.addText("venv", SimpleTextAttributes.REGULAR_ATTRIBUTES) }
            verify { data.addText(" [3.11.0]", SimpleTextAttributes.GRAY_ATTRIBUTES) }
        }

        @Test
        fun `does not add version text when version unavailable`(
            @TempDir tempDir: Path,
        ) {
            val pyvenvCfgPath = tempDir.resolve("pyvenv.cfg")
            Files.writeString(pyvenvCfgPath, "home = /usr/bin")

            every { VenvUtils.getPyVenvCfg(virtualFile) } returns pyvenvCfgPath
            every { versionCache.getVersion(pyvenvCfgPath.toString()) } returns null
            every { data.presentableText } returns "venv"

            decorator.decorate(node, data)

            verify(exactly = 0) { data.clearText() }
            verify(exactly = 0) { data.addText(any(), any<SimpleTextAttributes>()) }
            verify { data.setIcon(any()) }
        }

        @Test
        fun `uses cache for version lookup`(
            @TempDir tempDir: Path,
        ) {
            val pyvenvCfgPath = tempDir.resolve("pyvenv.cfg")
            Files.writeString(pyvenvCfgPath, "version = 3.11.0")

            every { VenvUtils.getPyVenvCfg(virtualFile) } returns pyvenvCfgPath
            every { versionCache.getVersion(pyvenvCfgPath.toString()) } returns "3.11.0"
            every { data.presentableText } returns "venv"

            // Call twice
            decorator.decorate(node, data)
            decorator.decorate(node, data)

            // Version cache should be called twice (caching is handled by the cache service)
            verify(exactly = 2) { versionCache.getVersion(pyvenvCfgPath.toString()) }
        }

        @Test
        fun `respects showVersionInProjectView setting`(
            @TempDir tempDir: Path,
        ) {
            val pyvenvCfgPath = tempDir.resolve("pyvenv.cfg")
            Files.writeString(pyvenvCfgPath, "version = 3.11.0")

            every { settings.showVersionInProjectView } returns false
            every { VenvUtils.getPyVenvCfg(virtualFile) } returns pyvenvCfgPath
            every { data.presentableText } returns "venv"

            decorator.decorate(node, data)

            verify(exactly = 0) { data.clearText() }
            verify(exactly = 0) { data.addText(any(), any<SimpleTextAttributes>()) }
            verify { data.setIcon(any()) } // Icon is still set
        }
    }
}
