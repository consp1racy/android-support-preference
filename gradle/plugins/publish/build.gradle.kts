plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("net.xpece.publish") {
            id = "net.xpece.publish"
            implementationClass = "net.xpece.gradle.publish.PublishPlugin"
        }
        register("net.xpece.publish.sonatype") {
            id = "net.xpece.publish.sonatype"
            implementationClass = "net.xpece.gradle.publish.PublishSonatypePlugin"
        }
    }
}

dependencies {
    compileOnly("com.android.tools.build:gradle:7.1.3")
}

repositories {
    google()
    gradlePluginPortal()
}
