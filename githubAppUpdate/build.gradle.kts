import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
}

android {
    namespace = "info.hannes.github"
    compileSdk = 36
    defaultConfig {
        minSdk = 23
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
    publishing {
        singleVariant("release") {}
    }
}

base {
    archivesName.set("githubAppUpdate")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.3.10")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("androidx.work:work-runtime-ktx:2.11.1")

    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    api("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["release"])
                pom {
                    licenses {
                        license {
                            name = "Apache License Version 2.0"
                            url = "https://github.com/hannesa2/githubAppUpdate/blob/master/LICENSE"
                        }
                    }
                }
            }
        }
    }
}
