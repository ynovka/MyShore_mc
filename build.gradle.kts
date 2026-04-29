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

fun readDotEnvValue(key: String): String? {
    val envFile = rootProject.file(".env")
    if (!envFile.exists()) return null

    return envFile.readLines()
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.startsWith("#") }
        .mapNotNull {
            val parts = it.split("=", limit = 2)
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }
        .firstOrNull { it.first == key }
        ?.second
}

tasks.register("updateServer") {
    notCompatibleWithConfigurationCache("This task starts/restarts an external Minecraft server and polls RCON.")

    dependsOn("shadowJar")

    doLast {
        val rconPassword =
            System.getenv("RCON_PASSWORD")
                ?: readDotEnvValue("RCON_PASSWORD")
                ?: error("RCON_PASSWORD not set")

        fun runRconCommand(command: String): Pair<Int, String> {
            val process = ProcessBuilder(
                "mcrcon-nsg",
                "-H", "127.0.0.1",
                "-P", "25568",
                "-p", rconPassword,
                command
            )
                .redirectErrorStream(true)
                .start()

            val output = StringBuilder()

            val readerThread = Thread {
                process.inputStream.bufferedReader(Charsets.UTF_8).useLines { lines ->
                    lines.forEach { line ->
                        output.appendLine(line)
                    }
                }
            }

            readerThread.start()

            val finished = process.waitFor(15, TimeUnit.SECONDS)

            if (!finished) {
                process.destroyForcibly()
                readerThread.join(1_000)
                return -1 to "RCON command timed out: $command\n$output"
            }

            readerThread.join(1_000)

            return process.exitValue() to output.toString()
        }

        println("Sending server restart command...")

        val restartResult = runRconCommand("restart")

        if (restartResult.first != 0) {
            println("Restart command returned exit code ${restartResult.first}:")
            println(restartResult.second.trim())
            println("Continuing because the server may be restarting...")
        }

        println("Waiting for server shutdown...")
        Thread.sleep(10_000)

        val timeoutMs = 180_000L
        val pollIntervalMs = 1_500L
        val startedAt = System.currentTimeMillis()

        println("Waiting for server to become fully loaded...")

        while (true) {
            val elapsed = System.currentTimeMillis() - startedAt

            if (elapsed > timeoutMs) {
                error("Server did not become ready within ${timeoutMs / 1000} seconds")
            }

            val (exitCode, response) = runRconCommand("list")

            if (exitCode == 0 && response.contains("There are", ignoreCase = true)) {
                println("Server is ready.")
                println(response.trim())
                break
            }

            println("Server is not ready yet...")
            Thread.sleep(pollIntervalMs)
        }
    }
}