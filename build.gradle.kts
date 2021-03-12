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
