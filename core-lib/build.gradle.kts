plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    `maven-publish`
}

group = "com.abyxcz.weatherconditions.core"
version = "1.0.0"

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // Add watchos targets if needed to match main app
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("stdlib"))
            implementation(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
            implementation(libs.kotest.assertions)
            implementation(libs.kotest.property)
            implementation(libs.kotest.framework)
            implementation(libs.kotest.datatest)
        }
    }
}

android {
    namespace = "com.abyxcz.weatherconditions.core"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

androidComponents {
    beforeVariants { variantBuilder ->
        variantBuilder.androidTest.enable = false
    }
}

publishing {
    publications {
        // Kotlin Multiplatform plugin automatically creates publications for targets.
    }
    
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/abyxcz/WeatherConditions-CoreLib")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: "USER_NOT_SET"
                password = System.getenv("GITHUB_TOKEN") ?: "TOKEN_NOT_SET"
            }
        }
    }
}
