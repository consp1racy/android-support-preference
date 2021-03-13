package net.xpece.gradle.publish

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get

internal class JavaPublisher(
    private val project: Project,
    private val componentName: String,
    private val pom: MavenPom.() -> Unit,
    private val repositories: RepositoryHandler.(version: String) -> Unit
) {

    private fun Project.publishing(configure: PublishingExtension.() -> Unit): Unit =
        (this as ExtensionAware).extensions.configure("publishing", configure)

    fun publish() {
        with(project) {
            val theGroupId = project.group.toString()
            val theArtifactId = project.findProperty("ARTIFACT_ID")?.toString()
                ?: project.name
            val theVersion = project.findProperty("VERSION")?.toString()
                ?: project.version.takeUnless { it == Project.DEFAULT_VERSION }?.toString()
                ?: throw IllegalStateException("Project is missing VERSION: ${project.path}")

            publishing {
                publications {
                    create("release", MavenPublication::class.java) {
                        groupId = theGroupId
                        artifactId = theArtifactId
                        version = theVersion

                        from(components[componentName])

                        this.pom(this@JavaPublisher.pom)
                    }
                }

                repositories { this@JavaPublisher.repositories(this, theVersion) }
            }
        }
    }
}