package net.xpece.gradle.android

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.api.AndroidSourceSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.DocsType
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.component.SoftwareComponentContainer
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.plugins.internal.JvmPluginsHelper
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import java.io.File

class AndroidXpecePlugin : Plugin<Project> {

    override fun apply(target: Project) = target.run {
        plugins.whenPluginAdded<BasePlugin> {
            val xpece = android.extensions.create<AndroidXpeceExtension>("xpece")

            afterEvaluate {
                if (xpece.withSourcesJar) {
                    withSourcesJarImpl("release")
                }
                if (xpece.withJavadocJar) {
                    withJavadocJarImpl("release")
                }
            }
        }
    }

    private inline fun <reified T : Plugin<Project>> PluginContainer.whenPluginAdded(
        crossinline block: T.() -> Unit
    ) {
        whenPluginAdded {
            if (this is T) {
                this.block()
            }
        }
    }

    private val Project.android: BaseExtension
        get() = extensions.getByType()

    private val Any.extensions: ExtensionContainer
        get() = (this as ExtensionAware).extensions

    private fun findVariantComponent(
        components: SoftwareComponentContainer,
        variantName: String
    ): AdhocComponentWithVariants? {
        val component = components.findByName(variantName)
        return component as? AdhocComponentWithVariants
    }

    private val AndroidSourceSet.sourcesJarTaskName: String
        get() = name + "SourcesJar"

    private val AndroidSourceSet.allSource: Set<File>
        get() = java.srcDirs

    @Suppress("UnstableApiUsage")
    private fun Project.withSourcesJarImpl(variantName: String) {
        val main = android.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        val component = findVariantComponent(components, variantName)
        JvmPluginsHelper.configureDocumentationVariantWithArtifact(
            variantName + JavaPlugin.SOURCES_ELEMENTS_CONFIGURATION_NAME.capitalize(),
            null,
            DocsType.SOURCES,
            emptyList(),
            main.sourcesJarTaskName,
            main.allSource,
            component,
            configurations,
            tasks,
            objects
        )
    }

    @Suppress("UnstableApiUsage")
    private fun Project.withJavadocJarImpl(variantName: String) {
        val component = findVariantComponent(components, variantName)
        JvmPluginsHelper.configureDocumentationVariantWithArtifact(
            variantName + JavaPlugin.JAVADOC_ELEMENTS_CONFIGURATION_NAME.capitalize(),
            null,
            DocsType.JAVADOC,
            emptyList(),
            variantName + "JavadocJar",
            tasks.named(variantName + "Javadoc"),
            component,
            configurations,
            tasks,
            objects
        )
    }
}
