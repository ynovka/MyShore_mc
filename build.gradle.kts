plugins {
    kotlin("jvm") version "2.3.20-RC"
    id("com.gradleup.shadow") version "9.3.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

group = "ru.ynovka"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.leafmc.one/snapshots/")
    maven("https://repo.plasmoverse.com/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.devBundle("cn.dreeam.leaf", "1.21.11-R0.1-SNAPSHOT")

    compileOnly("com.github.darksoulq:AbyssalLib:v2.0.0-mc1.21.11-dev.17")
    //compileOnly("com.github.darksoulq:AbyssalLib")
    compileOnly("su.plo.voice.server:paper:2.1.8")

    implementation("dev.jorel:commandapi-paper-shade:11.1.0")
    implementation("dev.jorel:commandapi-kotlin-paper:11.1.0")

    compileOnly(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(21)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.shadowJar {
    archiveFileName.set("${project.name}-${project.version}.jar")

    destinationDirectory.set(
        file("/var/lib/featherpanel/volumes/f77dd561-9f8b-4f19-9563-e9360906a3a1/plugins/")
    )
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}