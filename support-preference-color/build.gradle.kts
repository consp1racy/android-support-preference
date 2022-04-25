plugins {
    id("com.android.library").version("7.1.3")
    id("net.xpece.publish.sonatype")
}

group = rootProject.property("GROUP_ID").toString()
version = rootProject.property("VERSION_NAME").toString()

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 14
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
        // Revert when lint stops with all the false positives >:-(
    }
}

dependencies {
    implementation(libs.androidx.annotation)

    api(projects.supportPreference)
}

apply(from = rootProject.file("android-metalava.gradle"))
