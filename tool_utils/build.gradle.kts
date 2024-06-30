plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")

    implementation("com.aallam.openai:openai-client:3.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    runtimeOnly("io.ktor:ktor-client-java:2.3.12")
}

kotlin {
    jvmToolchain(17)
}
