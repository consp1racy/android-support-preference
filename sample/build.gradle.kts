plugins {
    id("com.android.application").version("7.1.3")
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
    implementation(libs.androidx.annotation)

    implementation(projects.supportPreference)
    implementation(projects.supportPreferenceColor)
    implementation(projects.supportSpinner)

    implementation(libs.material)

    debugImplementation(libs.leakcanary)
}
