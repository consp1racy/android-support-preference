plugins {
    id("com.android.application").version("7.1.3")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "net.xpece.android.support.preference.sample"

        minSdk = 14
        targetSdk = 28

        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs["debug"]
        }
    }

    lint {
        ignore += setOf("MissingTranslation")
        warning += setOf("PrivateResource", "RestrictedApi")
        abortOnError = false
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
