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
            // target supporting the following platforms
            create(IntelliJPlatformType.PyCharmCommunity, platformVersion)
            create(IntelliJPlatformType.PyCharmProfessional, platformVersion)
            create(IntelliJPlatformType.IntellijIdeaCommunity, platformVersion)
            create(IntelliJPlatformType.IntellijIdeaUltimate, platformVersion)
            create(IntelliJPlatformType.GoLand, platformVersion)
            create(IntelliJPlatformType.CLion, platformVersion)
            create(IntelliJPlatformType.RustRover, platformVersion)
        }
    }
}

changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}
kover {
    reports {
        total {
            xml {
                onCheck = true
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
    test {
        useJUnitPlatform()
    }
}

val runIdeForUiTests by intellijPlatformTesting.runIde.registering {
    task {
        jvmArgumentProviders +=
            CommandLineArgumentProvider {
                listOf(
                    "-Drobot-server.port=8082",
                    "-Dide.mac.message.dialogs.as.sheets=false",
                    "-Djb.privacy.policy.text=<!--999.999-->",
                    "-Djb.consents.confirmation.enabled=false",
                    "-Didea.trust.all.projects=true",
                    "-Dide.mac.file.chooser.native=false",
                    "-Dide.show.tips.on.startup.default.value=false",
                )
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
