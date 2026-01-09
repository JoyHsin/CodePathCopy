plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "hsin.tools"
version = "1.0.0"

repositories {
    mavenCentral()
}

intellij {
    version.set("2023.2")
    type.set("IC")
    plugins.set(listOf("terminal"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("241.*")
    }
    named("instrumentCode") {
        enabled = false
    }
    named("instrumentTestCode") {
        enabled = false
    }
    named("buildSearchableOptions") {
        enabled = false
    }
}
