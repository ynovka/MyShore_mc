plugins {
    idea
    kotlin("jvm") version "2.3.20-RC"
    id("com.gradleup.shadow") version "9.3.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "ru.ynovka"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.canvasmc.io/releases")
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.plasmoverse.com/releases")
}

dependencies {
    paperweight.devBundle("io.canvasmc.canvas", "26.1.2.build.+")


    compileOnly("com.github.darksoulq:AbyssalLib:v2.2.2-mc.26.1.2")
    //compileOnly("com.github.darksoulq:AbyssalLib")
    compileOnly("su.plo.voice.server:paper:2.1.8")
    compileOnly("net.thenextlvl:worlds:4.2.2")

    implementation("dev.jorel:commandapi-paper-shade:11.2.0")
    implementation("dev.jorel:commandapi-kotlin-paper:11.2.0")

    compileOnly(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(25)
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
