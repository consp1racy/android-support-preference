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

        consumerProguardFile("proguard-consumer-rules.pro")
    }

    resourcePrefix("asp_")

    sourceSets {
        getByName("main") {
            res.srcDir("src/main/res-aosp")
            res.srcDir("src/main/res-media")
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    lint {
        disable += setOf("ResourceName", "InlinedApi")
        warning += setOf("GradleCompatible")
        checkReleaseBuilds = false
        abortOnError = false
        // Revert when lint stops with all the false positives >:-(
    }
}

dependencies {
    implementation(libs.androidx.annotation)

    api(libs.androidx.appcompat)
    api(libs.androidx.fragment)
    api(libs.androidx.preference)

    implementation(project(":support-spinner"))
}

apply(from = rootProject.file("android-metalava.gradle"))
