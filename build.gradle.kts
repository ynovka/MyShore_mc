plugins {
    kotlin("jvm") version "2.3.20-RC"
    id("com.gradleup.shadow") version "9.3.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

group = "ru.ynovka"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.leafmc.one/snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.plasmoverse.com/releases")
}

dependencies {
    paperweight.devBundle("cn.dreeam.leaf", "1.21.11-R0.1-SNAPSHOT")

    compileOnly("com.github.darksoulq:AbyssalLib:v2.0.0-mc1.21.11-dev.12")
    //compileOnly("com.github.darksoulq:AbyssalLib")
    compileOnly("su.plo.voice.server:paper:2.1.8")

    implementation("dev.jorel:commandapi-paper-shade:11.1.0")
    implementation("dev.jorel:commandapi-kotlin-paper:11.1.0")

    compileOnly(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(21)
}

tasks {
    runServer {
        serverJar(File("run/server.jar"))
        minecraftVersion("1.21.11")
    }
}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
    jvmArgs("-XX:+AllowEnhancedClassRedefinition")
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}