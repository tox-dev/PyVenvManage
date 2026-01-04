package com.github.pyvenvmanage.pages

import java.time.Duration
import java.time.Duration.ofSeconds

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor

fun RemoteRobot.actionMenuItem(
    text: String,
    timeout: Duration = ofSeconds(30),
): ActionMenuItemFixture {
    val xpath = byXpath("text '$text'", "//div[@class='ActionMenuItem' and @text='$text']")
    waitFor(timeout) {
        findAll<ActionMenuItemFixture>(xpath).isNotEmpty()
    }
    return findAll<ActionMenuItemFixture>(xpath).first()
}

fun RemoteRobot.hasActionMenuItem(text: String): Boolean {
    val xpath = byXpath("text '$text'", "//div[@class='ActionMenuItem' and @text='$text']")
    return findAll<ActionMenuItemFixture>(xpath).isNotEmpty()
}

fun RemoteRobot.pressEscape() {
    runJs("robot.pressAndReleaseKey(java.awt.event.KeyEvent.VK_ESCAPE)")
}

@FixtureName("ActionMenuItem")
class ActionMenuItemFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent,
) : ComponentFixture(remoteRobot, remoteComponent)
