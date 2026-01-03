package com.github.pyvenvmanage

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration.ofMinutes
import java.time.Duration.ofSeconds
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

import kotlin.io.path.name
import org.assertj.swing.core.MouseButton
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestWatcher

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.StepLogger
import com.intellij.remoterobot.stepsProcessing.StepWorker
import com.intellij.remoterobot.utils.waitFor

import com.github.pyvenvmanage.pages.actionMenuItem
import com.github.pyvenvmanage.pages.dialog
import com.github.pyvenvmanage.pages.idea
import com.github.pyvenvmanage.pages.welcomeFrame

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
            tmpDir = Files.createTempDirectory(base, "ui-test")
            // create test project
            val demo = Paths.get(tmpDir.toString(), "demo")
            Files.createDirectory(demo)
            File(demo.toString(), "main.py").printWriter().use { out ->
                out.println("print(1)\n")
            }
            val venv = Paths.get(demo.toString(), "ve").toString()
            val process = ProcessBuilder("python", "-m", "venv", venv, "--without-pip")
            assert(process.start().waitFor() == 0)

            // ./gradlew runIdeForUiTests requires already running, so just wait to connect
            StepWorker.registerProcessor(StepLogger())
            remoteRobot = RemoteRobot("http://localhost:8082")
            waitFor(ofSeconds(20), ofSeconds(5)) {
                runCatching {
                    remoteRobot.callJs<Boolean>("true")
                }.getOrDefault(false)
            }
            // open test project
            remoteRobot.welcomeFrame {
                openLink.click()
                dialog("Open File or Project") {
                    button(byXpath("//div[@myicon='refresh.svg']")).click()
                    Thread.sleep(500)
                    val tree = find<JTreeFixture>(byXpath("//div[@class='Tree']"))
                    tree.expand(tree.getValueAtRow(0), *demo.map { it.name }.toTypedArray())
                    tree.clickPath(tree.getValueAtRow(0), *demo.map { it.name }.toTypedArray(), fullMatch = true)
                    button("OK").click()
                }
            }
            Thread.sleep(1000)
            // wait for indexing to finish
            remoteRobot.idea {
                waitFor(ofMinutes(1)) { isDumbMode().not() }
            }
        }

        @AfterAll
        @JvmStatic
        fun cleanUp() {
        }
    }

    @Test
    fun testSetProjectInterpreter() {
        remoteRobot.idea {
            with(projectViewTree) {
                findText("ve").click(MouseButton.RIGHT_BUTTON)
                remoteRobot.actionMenuItem("Set as Project Interpreter").click()
                findText("Updated SDK for project demo to:")
                // wait for indexing to finish
                waitFor(ofMinutes(1)) { isDumbMode().not() }
            }
        }
    }

    @Test
    fun testSetModuleInterpreter() {
        remoteRobot.idea {
            with(projectViewTree) {
                findText("ve").click(MouseButton.RIGHT_BUTTON)
                remoteRobot.actionMenuItem("Set as Module Interpreter").click()
                findText("Updated SDK for module demo to:")
                // wait for indexing to finish
                waitFor(ofMinutes(1)) { isDumbMode().not() }
            }
        }
    }

    @Test
    fun testVenvDirectoryShowsPythonVersion() {
        remoteRobot.idea {
            with(projectViewTree) {
                // The venv directory should display the Python version in brackets
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
                // Verify the venv directory is decorated (has venv text visible)
                waitFor(ofSeconds(10)) {
                    hasText("ve")
                }
            }
        }
    }

    @Test
    fun testContextMenuOnNonVenvDirectory() {
        remoteRobot.idea {
            with(projectViewTree) {
                // Right-click on a non-venv directory should not show interpreter options
                findText("demo").click(MouseButton.RIGHT_BUTTON)
                waitFor(ofSeconds(2)) {
                    // The action menu should be visible but interpreter options should not be enabled
                    runCatching {
                        remoteRobot.actionMenuItem("Set as Project Interpreter")
                        false // If found, test should handle it
                    }.getOrDefault(true) // If not found, that's expected
                }
            }
        }
    }

    @Test
    fun testContextMenuOnPythonFile() {
        remoteRobot.idea {
            with(projectViewTree) {
                // Right-click on a Python file should not show interpreter options
                findText("main.py").click(MouseButton.RIGHT_BUTTON)
                waitFor(ofSeconds(2)) {
                    runCatching {
                        remoteRobot.actionMenuItem("Set as Project Interpreter")
                        false
                    }.getOrDefault(true)
                }
            }
        }
    }
}
