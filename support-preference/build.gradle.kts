plugins {
    id("com.android.library")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(14)

        consumerProguardFile("proguard-consumer-rules.pro")
    }

    resourcePrefix("asp_")

    sourceSets {
        getByName("main") {
            res.srcDir("src/main/res-aosp")
            res.srcDir("src/main/res-media")
        }
    }

    lintOptions {
        disable("ResourceName", "InlinedApi")
        warning("GradleCompatible")
        isCheckReleaseBuilds = false
        isAbortOnError = false
        // Revert when lint stops with all the false positives >:-(
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.1.0")

    api("androidx.appcompat:appcompat:1.2.0")
    api("androidx.preference:preference:1.1.1")

    implementation(project(":support-spinner"))
}

group = rootProject.property("GROUP_ID").toString()
version = rootProject.property("VERSION_NAME").toString()

apply(from = rootProject.file("android-metalava.gradle"))
