plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("net.xpece.android") {
            id = "net.xpece.android"
            implementationClass = "net.xpece.gradle.android.AndroidXpecePlugin"
        }
    }
}

dependencies {
    compileOnly("com.android.tools.build:gradle:4.1.2")
}

repositories {
    google()
    gradlePluginPortal()
}
