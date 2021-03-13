package net.xpece.gradle.publish

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.maven.MavenPom

open class PublishExtension {

    internal var publishReleaseFromComponent: String? = null
        private set
    internal var pom: MavenPom.() -> Unit = {}
        private set
    internal var repositories: RepositoryHandler.(version: String) -> Unit = {}
        private set

    fun releaseFromDefaultComponent() {
        releaseFromComponent(DEFAULT_COMPONENT_NAME)
    }

    fun releaseFromComponent(componentName: String) {
        check(publishReleaseFromComponent == null) { "Release already set up." }
        publishReleaseFromComponent = componentName
    }

    fun pom(block: MavenPom.() -> Unit) {
        pom = block
    }

    fun repositories(block: RepositoryHandler.(version: String) -> Unit) {
        repositories = block
    }

    internal companion object {

        const val DEFAULT_COMPONENT_NAME = "__DEFAULT__"
    }
}
