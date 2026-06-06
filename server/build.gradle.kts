import com.google.cloud.tools.gradle.appengine.appyaml.AppEngineAppYamlExtension

configure<AppEngineAppYamlExtension> {
    stage {
        setArtifact("build/libs/${project.name}-all.jar")
    }
    println("Project name: ${project.name}")
    deploy {
        version = "GCLOUD_CONFIG"
        projectId = "GCLOUD_CONFIG"
    }
}

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application

    id("com.google.cloud.tools.appengine") version "2.8.0"
}

group = "com.openfda.funwitopenfda"
version = "1.0.0"
application {
    mainClass.set("com.openfda.funwitopenfda.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    runtimeOnly(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.server.call.logging)
    testImplementation(libs.ktor.serverTestHost)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.auth)


    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)

    implementation (libs.ktor.server.contentnegotiation.json)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.content)

    testImplementation(libs.kotlin.testJunit)
}