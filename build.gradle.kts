// Top-level build.gradle

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // The Android Gradle plugin (adjust the version as needed)
        classpath (libs.gradle)
        // The Google Services plugin
        classpath (libs.google.services)
    }
}

// Optionally, if you want to use older style plugin application, you can do:
// apply plugin: "com.google.gms.google-services"

// If you’re using the new plugins DSL in module-level build.gradle, you might not need the below:
plugins {
    // If using version catalogs, you might have lines like:
     alias(libs.plugins.android.application) apply false
     alias(libs.plugins.kotlin.android) apply false
     alias(libs.plugins.kotlin.compose) apply false
     //alias(libs.plugins.google.services) version "4.3.15" apply false
}

// If you have a “dependencyResolutionManagement” block, it might be in settings.gradle:
// This is an example for settings.gradle or settings.gradle.kts
/*
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
*/

