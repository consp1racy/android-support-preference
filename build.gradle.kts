buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter {
            content {
                includeVersion("org.jetbrains.trove4j", "trove4j", "20160824")
            }
        }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.1.2")
    }
}

subprojects {
    plugins.whenPluginAdded {
        if (this is com.android.build.gradle.LibraryPlugin) {
            extensions.configure<com.android.build.gradle.LibraryExtension> {
                libraryVariants.all {
                        tasks.register<Javadoc>(name + "Javadoc") {
                            setSource(javaCompileProvider.map { it.source })
                            classpath = getCompileClasspath(null) + files(bootClasspath)
                            isFailOnError = false
                        }
                    }
            }
        }
    }

    repositories {
        google()
        mavenCentral()
        jcenter {
            content {
                includeVersion("org.jetbrains.trove4j", "trove4j", "20160824")
            }
        }
    }
}

tasks.named<Wrapper>("wrapper") {
    distributionType = Wrapper.DistributionType.ALL
}
