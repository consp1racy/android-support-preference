package net.xpece.gradle.publish

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import net.xpece.gradle.publish.PublishExtension.Companion.DEFAULT_COMPONENT_NAME
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

class PublishPlugin : Plugin<Project> {

    override fun apply(target: Project) = target.run {
        val extension = extensions.create<PublishExtension>("xpecePublish")

        // Automatically apply publishing plugin.
        target.plugins.findPlugin("maven-publish")
            ?: target.apply(plugin = "maven-publish")

        // Wait after DSL is parsed.
        afterEvaluate {
            try {
                // Check if we're running Android build.
                target.android

                target.applyAndroid(extension)
            } catch (_: UnknownDomainObjectException) {
                target.applyJava(extension)
            }
        }
    }

    private val Project.android: BaseExtension
        get() = extensions.getByType()

    private fun Project.applyAndroid(extension: PublishExtension) {
        var componentName = extension.publishReleaseFromComponent ?: return
        if (componentName == DEFAULT_COMPONENT_NAME) {
            componentName = when (android) {
                is LibraryExtension -> "release"
                is AppExtension -> "release_apk"
                else -> throw IllegalStateException("Unsupported Android extension: $android")
            }
        }
        val pom = extension.pom
        val repositories = extension.repositories
        val publisher = AndroidPublisher(this, componentName, pom, repositories)
        publisher.publish()
    }

    private fun Project.applyJava(extension: PublishExtension) {
        var componentName = extension.publishReleaseFromComponent ?: return
        if (componentName == DEFAULT_COMPONENT_NAME) componentName = "java"
        val pom = extension.pom
        val repositories = extension.repositories
        val publisher = JavaPublisher(this, componentName, pom, repositories)
        publisher.publish()
    }
}
