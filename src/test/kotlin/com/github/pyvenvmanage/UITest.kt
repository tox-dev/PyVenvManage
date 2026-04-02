package com.github.pyvenvmanage

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration.ofMinutes
import java.time.Duration.ofSeconds
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

import org.assertj.swing.core.MouseButton
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestWatcher

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.stepsProcessing.StepLogger
import com.intellij.remoterobot.stepsProcessing.StepWorker
import com.intellij.remoterobot.utils.waitFor

import com.github.pyvenvmanage.pages.IdeaFrame
import com.github.pyvenvmanage.pages.actionMenuItem
import com.github.pyvenvmanage.pages.hasActionMenuItem
import com.github.pyvenvmanage.pages.idea
import com.github.pyvenvmanage.pages.pressEscape

@ExtendWith(UITest.IdeTestWatcher::class)
@Timeout(value = 15, unit = TimeUnit.MINUTES)
class UITest {
    class IdeTestWatcher : TestWatcher {
        override fun testFailed(
            context: ExtensionContext,
            cause: Throwable?,
        ) {
            ImageIO.write(
                remoteRobot.getScreenshot(),
                "png",
                File("build/reports", "${context.displayName}.png"),
            )
        }
    }

    companion object {
        private lateinit var tmpDir: Path
        private lateinit var remoteRobot: RemoteRobot

        @BeforeAll
        @JvmStatic
        fun startIdea() {
            val base = Path.of(System.getProperty("user.home"), "projects")
            Files.createDirectories(base)
            Files.list(base).filter { it.fileName.toString().startsWith("ui-test") }.forEach {
                it.toFile().deleteRecursively()
            }
            tmpDir = Files.createTempDirectory(base, "ui-test")
            val demo = Paths.get(tmpDir.toString(), "demo")
            Files.createDirectory(demo)
            File(demo.toString(), "main.py").printWriter().use { out ->
                out.println("print(1)\n")
            }
            val venv = Paths.get(demo.toString(), "ve").toString()
            val pythonCmd = if (System.getProperty("os.name").lowercase().contains("win")) "python" else "python3"
            val process = ProcessBuilder(pythonCmd, "-m", "venv", venv, "--without-pip")
            assert(process.start().waitFor() == 0)

            StepWorker.registerProcessor(StepLogger())
            remoteRobot = RemoteRobot("http://127.0.0.1:8082")
            Thread.sleep(10000)

            val ideFrame = remoteRobot.find<IdeaFrame>(timeout = ofMinutes(2))
            ideFrame.openProjectViaAction(demo.toString())
            Thread.sleep(10000)
            remoteRobot.find<IdeaFrame>(timeout = ofMinutes(2)).apply {
                waitFor(ofMinutes(2)) { isDumbMode().not() }
                Thread.sleep(5000)
                activateProjectView()
                Thread.sleep(3000)
            }
        }

        @AfterAll
        @JvmStatic
        fun cleanUp() {
        }
    }

    @Test
    fun testSetProjectInterpreter() {
        remoteRobot.pressEscape()
        remoteRobot.idea {
            with(projectViewTree) {
                findText("ve").click(MouseButton.RIGHT_BUTTON)
                remoteRobot.actionMenuItem("Set as Project Interpreter").click()
                waitFor(ofMinutes(1)) { isDumbMode().not() }
            }
        }
    }

    @Test
    fun testSetModuleInterpreter() {
        remoteRobot.pressEscape()
        remoteRobot.idea {
            with(projectViewTree) {
                findText("ve").click(MouseButton.RIGHT_BUTTON)
                remoteRobot.actionMenuItem("Set as Module Interpreter").click()
                waitFor(ofMinutes(1)) { isDumbMode().not() }
            }
        }
    }

    @Test
    fun testVenvDirectoryShowsPythonVersion() {
        remoteRobot.idea {
            with(projectViewTree) {
                waitFor(ofSeconds(10)) {
                    hasText { it.text.contains("[") && it.text.contains("]") }
                }
            }
        }
    }

    @Test
    fun testVenvDirectoryHasVenvIcon() {
        remoteRobot.idea {
            with(projectViewTree) {
                waitFor(ofSeconds(10)) {
                    hasText("ve")
                }
            }
        }
    }

    @Test
    fun testContextMenuOnNonVenvDirectory() {
        remoteRobot.pressEscape()
        remoteRobot.idea {
            with(projectViewTree) {
                findText("demo").click(MouseButton.RIGHT_BUTTON)
                Thread.sleep(500)
                assert(!remoteRobot.hasActionMenuItem("Set as Project Interpreter")) {
                    "Non-venv directory should not show 'Set as Project Interpreter'"
                }
                remoteRobot.pressEscape()
            }
        }
    }

    @Test
    fun testContextMenuOnPythonFile() {
        remoteRobot.pressEscape()
        remoteRobot.idea {
            with(projectViewTree) {
                findText("main.py").click(MouseButton.RIGHT_BUTTON)
                Thread.sleep(500)
                assert(!remoteRobot.hasActionMenuItem("Set as Project Interpreter")) {
                    "Python file should not show 'Set as Project Interpreter'"
                }
                remoteRobot.pressEscape()
            }
        }
    }
}
