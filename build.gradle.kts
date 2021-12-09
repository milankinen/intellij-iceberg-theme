import org.jetbrains.changelog.markdownToHTML

fun prop(key: String) = project.findProperty(key).toString()

plugins {
    id("org.jetbrains.intellij") version "1.3.0"
    id("org.jetbrains.changelog") version "1.3.1"
}

group = prop("pluginGroup")
version = prop("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(prop("pluginName"))
    version.set(prop("platformVersion"))
    type.set(prop("platformType"))
    updateSinceUntilBuild.set(false)
}

changelog {
    version.set(prop("pluginVersion"))
    groups.set(emptyList())
}

tasks {
    wrapper {
        gradleVersion = prop("gradleVersion")
    }

    patchPluginXml {
        version.set(prop("pluginVersion"))
        sinceBuild.set(prop("pluginSinceBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider {
            changelog.run {
                getOrNull(prop("pluginVersion")) ?: getLatest()
            }.toHTML()
        })
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        channels.set(listOf(prop("pluginVersion").split("-").getOrElse(1) { "default" }.split('.').first()))
    }
}