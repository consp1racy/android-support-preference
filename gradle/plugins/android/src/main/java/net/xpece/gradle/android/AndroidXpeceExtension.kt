package net.xpece.gradle.android

import com.android.build.gradle.BaseExtension
import org.gradle.api.Incubating
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.getByType

open class AndroidXpeceExtension {

    internal var withSourcesJar = false
    internal var withJavadocJar = false

    @Incubating
    fun withSourcesJar() {
        withSourcesJar = true
    }

    @Incubating
    fun withJavadocJar() {
        withJavadocJar = true
    }
}

@Incubating
fun BaseExtension.withSourcesJar() {
    (this as ExtensionAware).extensions
        .getByType<AndroidXpeceExtension>()
        .withSourcesJar()
}

@Incubating
fun BaseExtension.withJavadocJar() {
    (this as ExtensionAware).extensions
        .getByType<AndroidXpeceExtension>()
        .withJavadocJar()
}
