buildscript {
    repositories {
        google()  // Google repository
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0") // Gradle 버전
        classpath("com.google.gms:google-services:4.3.15") // Firebase 플러그인
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
