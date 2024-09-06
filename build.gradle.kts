import org.gradle.kotlin.dsl.bukkit

plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `maven-publish`
}

group = "it.pureorigins"
version = "0.4.1"

bukkit {
    name = project.name
    version = project.version.toString()
    main = "it.pureorigins.common.${project.name}"
    apiVersion = "1.21.1"
}

repositories {
    mavenCentral()
    maven("https://repo.purpurmc.org/snapshots")
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.21.1-R0.1-SNAPSHOT")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")
    api("org.freemarker:freemarker:2.3.31")
    api("org.jetbrains.exposed:exposed-core:0.54.0")
    api("org.jetbrains.exposed:exposed-jdbc:0.54.0")
    api("org.postgresql:postgresql:42.7.2")
    api("org.xerial:sqlite-jdbc:3.41.2.2")
}

afterEvaluate {
    tasks {
        shadowJar {
            mergeServiceFiles()
        }
        
        jar {
            archiveClassifier.set("")
        }
        
        shadowJar {
            archiveClassifier.set("fat")
        }

        /*reobfJar {
            outputJar.set(shadowJar.get().archiveFile)
        }*/
        
        build {
            //dependsOn(reobfJar)
            dependsOn(shadowJar)
        }
    }
}

kotlin {
    jvmToolchain {
        this.languageVersion.set(JavaLanguageVersion.of(21))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.PureOrigins"
            artifactId = project.name
            version = project.version.toString()
            
            afterEvaluate {
                from(components["kotlin"])
                artifact(tasks["kotlinSourcesJar"])
            }
        }
    }
}