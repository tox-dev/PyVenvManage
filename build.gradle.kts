import org.gradle.internal.os.OperatingSystem
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.Constants.Constraints
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode

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
version =
    providers
        .gradleProperty("pluginVersion")
        .get()
        .let { baseVersion ->
            if (baseVersion.endsWith("-dev")) {
                val gitHash =
                    providers
                        .exec { commandLine("git", "rev-parse", "--short=8", "HEAD") }
                        .standardOutput
                        .asText
                        .get()
                        .trim()
                "$baseVersion+$gitHash"
            } else {
                baseVersion
            }
        }
val platformVersion = providers.gradleProperty("platformVersion").get()
kotlin {
    jvmToolchain(17)
    compilerOptions {
        // Suppress synthetic ACC_BRIDGE methods for inherited interface defaults.
        // Without this, Kotlin emits a bridge in our implementing class for ProjectViewNodeDecorator's
        // @Deprecated default decorate() overload, which the plugin verifier reports as an override.
        jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        // platformVersion targets a 2026.2 EAP build, which lives in the snapshots channel.
        snapshots()
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
        // platformVersion is a 2026.2 EAP build, available only as a snapshot maven artifact
        // (no installer at download.jetbrains.com), so resolve it from the repository.
        // Community (not Professional) carries every Python SDK API the plugin uses and has no
        // EAP evaluation-login wall, which would otherwise block the headless UI tests.
        pycharmCommunity(platformVersion) { useInstaller = false }
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
        // The verifier's ignoredProblemsFile filters CompatibilityProblem instances only,
        // not ApiUsage (which is what INTERNAL_API_USAGES is). The internal usages from
        // SdkFactory/EnvironmentDetector reach into per-tool PyCharm SDK packages
        // (uv, hatch.sdk, poetry, pipenv) and per-tool icon classes, plus the
        // PluginManagerCore plugin lookup, all sealed behind @ApiStatus.Internal with no
        // public alternative on 262.
        failureLevel =
            listOf(
                FailureLevel.COMPATIBILITY_PROBLEMS,
                FailureLevel.OVERRIDE_ONLY_API_USAGES,
            )
        // PyCharm Pro macOS dmg distribution packs nested modules into a single jar per plugin
        // (e.g. plugins/grazie/lib/modules/*.jar paths declared by product-info.json don't exist
        // on disk; the classes live inside the parent plugin jar instead). The default
        // skip-warn behaviour drops these layout components — including transitive paths that
        // make com.intellij.modules.python unresolvable on macOS. Linux tar.gz ships split jars
        // matching the layout and "ignore" would crash trying to read truly absent jars there,
        // so this override is macOS-only.
        if (OperatingSystem.current().isMacOsX) {
            freeArgs.add("-missing-layout-classpath-file")
            freeArgs.add("ignore")
        }
        ides {
            val verifyIde = providers.gradleProperty("verifyIde").orNull
            val ideTypes =
                if (verifyIde != null) {
                    listOf(IntelliJPlatformType.fromCode(verifyIde))
                } else {
                    listOf(
                        IntelliJPlatformType.PyCharm,
                        IntelliJPlatformType.PyCharmProfessional,
                    )
                }
            ideTypes.forEach { create(it, platformVersion) { useInstaller = false } }
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
        filters {
            excludes {
                // SdkFactory.createSdk requires full IntelliJ platform (WriteAction, ProjectJdkTable);
                // EnvironmentDetector has platform-specific branches (Windows/Linux) untestable on macOS.
                // Both are covered by UI tests.
                classes(
                    "com.github.pyvenvmanage.sdk.SdkFactory",
                    "com.github.pyvenvmanage.sdk.EnvironmentDetector",
                )
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
                    add("-Dskiko.renderApi=SOFTWARE")
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
