//import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    /*js {
        browser()
        binaries.executable()
    }*/

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.jetbrainsCompose.runtime)
            implementation(libs.jetbrainsCompose.foundation)
            implementation(libs.jetbrainsCompose.material3)
            implementation(libs.jetbrainsCompose.ui)
            implementation(libs.jetbrainsCompose.resources)
            implementation(libs.jetbrainsCompose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)

            //implementation("androidx.compose.material:material-icons-extended:1.7.8")
            implementation(libs.jetbrainsCompose.materialIconsCore)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}


