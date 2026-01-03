import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.Constants.Constraints
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    alias(libs.plugins.changelog)
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
//    alias(libs.plugins.taskinfo) // cache incompatible - https://gitlab.com/barfuin/gradle-taskinfo/-/issues/23
    alias(libs.plugins.testLogger)
    alias(libs.plugins.versionPlugin)
    alias(libs.plugins.versionUpdate)
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()
val platformVersion = providers.gradleProperty("platformVersion").get()
kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation(libs.jupiter)
    testImplementation(libs.mockk)
    testRuntimeOnly(libs.jupiterEngine)
    testRuntimeOnly(libs.junitPlatformLauncher)
    testRuntimeOnly("junit:junit:4.13.2") // legacy JUnit 4 support
    testImplementation(libs.remoteRobot)
    testImplementation(libs.remoteRobotFixtures)
    intellijPlatform {
        pycharmCommunity(platformVersion)
        bundledPlugin("PythonCore")
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.JUnit5)
    }
}

intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")
        description =
            providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"
                with(it.lines()) {
                    if (!containsAll(listOf(start, end))) {
                        throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                    }
                    subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
                }
            }

        val changelog = project.changelog
        changeNotes =
            providers.gradleProperty("pluginVersion").map { pluginVersion ->
                with(changelog) {
                    renderItem(
                        (getOrNull(pluginVersion) ?: getUnreleased())
                            .withHeader(false)
                            .withEmptySections(false),
                        Changelog.OutputType.HTML,
                    )
                }
            }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null }
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        channels =
            providers.gradleProperty("pluginVersion").map {
                listOf(
                    it
                        .substringAfter('-', "")
                        .substringBefore('.')
                        .ifEmpty { "default" },
                )
            }
    }
    pluginVerification {
        ides {
            val verifyIde = providers.gradleProperty("verifyIde").orNull
            val ideTypes =
                if (verifyIde != null) {
                    listOf(IntelliJPlatformType.fromCode(verifyIde))
                } else {
                    listOf(
                        IntelliJPlatformType.PyCharmCommunity,
                        IntelliJPlatformType.PyCharmProfessional,
                    )
                }
            ideTypes.forEach { create(it, platformVersion) }
        }
    }
}

changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}
kover {
    currentProject {
        sources {
            excludeJava = true
        }
        instrumentation {
            disabledForTestTasks.add("uiTest")
        }
    }
    reports {
        total {
            xml {
                onCheck = true
            }
        }
        verify {
            rule {
                minBound(100)
            }
        }
    }
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }
    publishPlugin {
        dependsOn(patchChangelog)
    }
    buildSearchableOptions {
        enabled = false
    }
    prepareJarSearchableOptions {
        enabled = false
    }
    verifyPlugin {
        System.getProperty("http.proxyHost")?.let { host ->
            jvmArgs("-Dhttp.proxyHost=$host")
            System.getProperty("http.proxyPort")?.let { jvmArgs("-Dhttp.proxyPort=$it") }
        }
        System.getProperty("https.proxyHost")?.let { host ->
            jvmArgs("-Dhttps.proxyHost=$host")
            System.getProperty("https.proxyPort")?.let { jvmArgs("-Dhttps.proxyPort=$it") }
        }
        System.getProperty("javax.net.ssl.trustStore")?.let { jvmArgs("-Djavax.net.ssl.trustStore=$it") }
        System
            .getProperty(
                "javax.net.ssl.trustStorePassword",
            )?.let { jvmArgs("-Djavax.net.ssl.trustStorePassword=$it") }
    }
    test {
        useJUnitPlatform()
        exclude("**/UITest.class")
    }
    val uiTest =
        register<Test>("uiTest") {
            description = "Runs UI tests (requires runIdeForUiTests to be running)"
            group = "verification"
            useJUnitPlatform()
            include("**/UITest.class")
            testClassesDirs = sourceSets["test"].output.classesDirs
            classpath = sourceSets["test"].runtimeClasspath
            shouldRunAfter(test)
            jvmArgs(
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
            )
        }

    runIde {
        jvmArgs("-XX:+UnlockDiagnosticVMOptions")
    }
}

val runIdeForUiTests by intellijPlatformTesting.runIde.registering {
    task {
        jvmArgumentProviders +=
            CommandLineArgumentProvider {
                buildList {
                    add("-Drobot-server.port=8082")
                    add("-Djb.privacy.policy.text=<!--999.999-->")
                    add("-Djb.consents.confirmation.enabled=false")
                    add("-Didea.trust.all.projects=true")
                    add("-Dide.show.tips.on.startup.default.value=false")
                    val isMac =
                        org.gradle.internal.os.OperatingSystem
                            .current()
                            .isMacOsX
                    if (isMac) {
                        add("-Dide.mac.message.dialogs.as.sheets=false")
                        add("-Dide.mac.file.chooser.native=false")
                        add("-DjbScreenMenuBar.enabled=false")
                        add("-Dapple.laf.useScreenMenuBar=false")
                    }
                }
            }
    }

    plugins {
        robotServerPlugin(Constraints.LATEST_VERSION)
    }
}

versionCatalogUpdate {
    keep {
        keepUnusedVersions = true
    }
}
