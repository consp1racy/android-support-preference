package net.xpece.gradle.publish

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.maven.MavenPom

internal class AndroidPublisher private constructor(
    private val project: Project,
    private val impl: JavaPublisher
) {

    constructor(
        project: Project,
        componentName: String,
        pom: MavenPom.() -> Unit,
        repositories: RepositoryHandler.(version: String) -> Unit
    ) : this(project, JavaPublisher(project, componentName, pom, repositories))

    fun publish() {
        with(project) {
            // Because the components are created only during the afterEvaluate phase, you must
            // configure your publications using the afterEvaluate() lifecycle method.
            // https://github.com/gradle/gradle/issues/11116
            afterEvaluate {
                impl.publish()
            }
        }
    }
}
