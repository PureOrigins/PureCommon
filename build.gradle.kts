plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("io.papermc.paperweight.userdev") version "1.3.4"
}

group = "it.pureorigins"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    api("org.freemarker:freemarker:2.3.31")
    api("org.jetbrains.exposed:exposed-core:0.37.3")
    api("org.jetbrains.exposed:exposed-jdbc:0.37.3")
    api("org.postgresql:postgresql:42.3.2")
    api("org.xerial:sqlite-jdbc:3.36.0.2")
}

tasks {
    jar {
        archiveFileName.set("${project.name}-${project.version}.jar")
    }
    
    shadowJar {
        archiveFileName.set("${project.name}-${project.version}-fat.jar")
        mergeServiceFiles()
    }
    
    reobfJar {
        outputJar.set(shadowJar.get().archiveFile)
    }
    
    build {
        dependsOn(reobfJar)
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
    
            from(components["kotlin"])
            artifact(tasks["kotlinSourcesJar"])
        }
    }
}