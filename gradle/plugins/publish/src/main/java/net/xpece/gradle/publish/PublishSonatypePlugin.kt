package net.xpece.gradle.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension
import java.io.BufferedInputStream
import java.util.*

class PublishSonatypePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.plugins.findPlugin(PublishPlugin::class)
            ?: target.apply<PublishPlugin>()

        val publishing = target.extensions.getByName<PublishingExtension>("publishing")

        val extension = target.extensions.getByType<PublishExtension>()
        extension.releaseFromDefaultComponent()

        extension.pom {
            name.set(target.name)
            description.set(target.description ?: "Material theme for preference widgets.")
            url.set("https://github.com/consp1racy/android-support-preference")

            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    name.set("Eugen Pechanec")
                    email.set("e.pechanec@gmail.com")
                    url.set("https://github.com/consp1racy")
                }
            }

            scm {
                connection.set("scm:git:https://github.com/consp1racy/android-support-preference.git")
                developerConnection.set("scm:git:ssh://git@github.com/consp1racy/android-support-preference.git")
                url.set("https://github.com/consp1racy/android-support-preference")
            }
        }

        if (System.getenv("JITPACK").toBoolean()) {
            target.logger.warn("Will not setup signing and Sonatype publishing in Jitpack.")
            return
        }

        target.plugins.findPlugin("signing")
            ?: target.apply(plugin = "signing")

        val signing = target.extensions.getByName<SigningExtension>("signing")
        signing.sign(publishing.publications)

        if (System.getenv("CI") == null) {
            signing.useGpgCmd()
        }

        val signingProps by lazy {
            Properties().apply {
                target.rootProject.file("publishing.properties")
                    .inputStream().buffered().use(this::load)
            }
        }

        extension.repositories { version ->
            when {
                version.endsWith("-SNAPSHOT") -> {
                    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
                        name = "ossrh"

                        credentials {
                            username = System.getProperty("OSSRH_USERNAME")
                                ?: signingProps.getProperty("OSSRH_USERNAME")
                            password = System.getProperty("OSSRH_PASSWORD")
                                ?: signingProps.getProperty("OSSRH_PASSWORD")
                        }
                    }
                }
                else -> {
                    maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
                        name = "ossrh"

                        credentials {
                            username = System.getProperty("OSSRH_USERNAME")
                                ?: signingProps.getProperty("OSSRH_USERNAME")
                            password = System.getProperty("OSSRH_PASSWORD")
                                ?: signingProps.getProperty("OSSRH_PASSWORD")
                        }
                    }
                }
            }
        }
    }
}