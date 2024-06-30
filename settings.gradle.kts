pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    plugins {
        id("com.github.ben-manes.versions") version "0.51.0"
    }
}

rootProject.name = "GptTools"

include("tool_utils")
include("run_ai_tasks_tool")
