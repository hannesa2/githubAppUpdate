import org.gradle.internal.jvm.Jvm

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:9.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0")
    }
}

println("Gradle uses Java ${Jvm.current()}")

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
