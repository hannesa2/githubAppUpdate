import info.git.versionHelper.getGitCommitCount
import info.git.versionHelper.getGitOriginRemote
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "info.hannes.github.sample"
    compileSdk = 36
    defaultConfig {
        versionCode = getGitCommitCount()
        versionName = "1.0"

        minSdk = 23

        buildConfigField("String", "GIT_REPOSITORY", "\"" + getGitOriginRemote() + "\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments.putAll(
            mapOf(
                "useTestStorageService" to "true",
            ),
        )
    }
    packaging {
        resources {
            pickFirsts += setOf("META-INF/atomicfu.kotlin_module")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":githubAppUpdate"))
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.3.10")

    androidTestImplementation("androidx.test.ext:junit-ktx:1.2.1")
    androidTestUtil("androidx.test.services:test-services:1.6.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
