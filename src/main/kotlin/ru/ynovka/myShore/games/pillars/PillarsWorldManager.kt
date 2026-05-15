package ru.ynovka.myShore.games.pillars

import ru.ynovka.myShore.games.pillars.generators.allocators.AllocatorGenerator
import ru.ynovka.myShore.games.pillars.generators.pillars.PillarGenerator
import ru.ynovka.myShore.utils.VoidWorldGenerator
import org.bukkit.WorldCreator
import java.util.UUID


object PillarsWorldManager {

    fun createWorld(aGen: AllocatorGenerator, pGen: PillarGenerator): PillarsWorld {
        val world = PillarsWorld(
            WorldCreator.name("pillars_${UUID.randomUUID()}")
                .generateStructures(false)
                .generator(VoidWorldGenerator())
                .createWorld()?.uid
                ?: error("Failed to create pillars world"),
            aGen, pGen
        )

        return world
    }

    fun updateWorld() {}

}