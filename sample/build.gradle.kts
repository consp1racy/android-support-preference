plugins {
    id("com.android.application")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "net.xpece.android.support.preference.sample"

        minSdkVersion(14)
        targetSdkVersion(28)

        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs["debug"]
        }
    }

    lintOptions {
        ignore("MissingTranslation")
        warning("PrivateResource", "RestrictedApi")
        isAbortOnError = false
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.1.0")

    implementation(project(":support-preference"))
    implementation(project(":support-preference-color"))
    implementation(project(":support-spinner"))

    implementation("com.google.android.material:material:1.3.0")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.6")
}
