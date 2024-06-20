plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":tool_utils"))
}

kotlin {
    jvmToolchain(17)
}
