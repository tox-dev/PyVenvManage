package com.github.pyvenvmanage.pages

import java.time.Duration

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ActionLinkFixture
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath

fun RemoteRobot.welcomeFrame(function: WelcomeFrame.() -> Unit) {
    find(WelcomeFrame::class.java, Duration.ofSeconds(10)).apply(function)
}

@FixtureName("Welcome Frame")
@DefaultXpath("type", "//div[@class='FlatWelcomeFrame']")
class WelcomeFrame(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent,
) : CommonContainerFixture(remoteRobot, remoteComponent) {
    private val xpath = "//div[@myiconbutton='createNewProjectTabSelected.svg']"
    val createNewProjectLink: ActionLinkFixture get() = actionLink(byXpath("New Project", xpath))
    val openLink: ActionLinkFixture get() = actionLink(byXpath("Open", "//div[@myiconbutton='openSelected.svg']"))
}
