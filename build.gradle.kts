import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.date
import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    id("org.jetbrains.intellij") version "1.12.0"
    id("org.jetbrains.changelog") version "1.3.1"
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.1.0"
}

val pluginGroup: String = "com.github.pyvenvmanage.pyvenv"
val pluginNameG: String = "PyVenv Manage"
val pluginVersion: String = "1.3.8"
val pluginSinceBuild = "211"
val pluginUntilBuild = "231.*"
// https://www.jetbrains.com/idea/download/other.html
val pluginVerifierIdeVersions = "222.4345.14"
val platformType = "IC"
val platformVersion = "2021.1"
// PythonCore https://plugins.jetbrains.com/plugin/631-python/versions
var usePlugins = "PythonCore:211.6693.111"

group = pluginGroup
version = pluginVersion

repositories {
    mavenCentral()
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.21.0")
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    listOf("compileKotlin", "compileTestKotlin").forEach {
        getByName<KotlinCompile>(it) {
            kotlinOptions.jvmTarget = "11"
        }
    }
    withType<Detekt> {
        jvmTarget = "11"
        reports {
            html.required.set(true)
        }
    }
    patchPluginXml {
        version.set(pluginVersion)
        sinceBuild.set(pluginSinceBuild)
        untilBuild.set(pluginUntilBuild)
    }

    withType<PatchPluginXmlTask> {
        pluginDescription.set(provider { file("description.html").readText() })
    }

    runPluginVerifier {
        ideVersions.set(listOf(pluginVerifierIdeVersions))
    }
    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        channels.set(listOf(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first()))
    }
}

intellij {
    pluginName.set(pluginNameG)
    version.set(platformVersion)
    type.set(platformType)
    plugins.set(listOf(usePlugins))
}

detekt {
    config = files("./detekt-config.yml")
    buildUponDefaultConfig = true
}

changelog {
    version.set(pluginVersion)
    path.set("${project.projectDir}/CHANGELOG.md")
    header.set(provider { "[${version.get()}] - ${date()}" })
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
    unreleasedTerm.set("[Unreleased]")
    groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
}
