plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    `maven-publish`
}

group = "it.pureorigins"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    mavenLocal()
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.18.1-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.freemarker:freemarker:2.3.31")
    implementation("org.jetbrains.exposed:exposed-core:0.34.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.34.1")
    implementation("org.postgresql:postgresql:42.2.16")
    implementation("org.xerial:sqlite-jdbc:3.34.0")
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.WARN
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.PureOrigins"
            artifactId = project.name
            version = project.version.toString()
    
            artifact(tasks.named("jar", Jar::class).get().archiveFile) {
                builtBy(tasks["jar"])
            }
            artifact(tasks["kotlinSourcesJar"]) {
                builtBy(tasks["kotlinSourcesJar"])
            }
        }
    }
}