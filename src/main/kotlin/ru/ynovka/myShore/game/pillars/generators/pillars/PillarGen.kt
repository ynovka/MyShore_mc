package ru.ynovka.myShore.game.pillars.generators.pillars

import ru.ynovka.myShore.game.pillars.Pillar
import org.bukkit.World
import java.util.concurrent.CompletableFuture


interface PillarGen {
    fun generate(world: World, pillar: Pillar): CompletableFuture<Void>
    fun remove(world: World, pillar: Pillar): CompletableFuture<Void>
}

enum class PillarGenerator(
    val gen: PillarGen
) {
    DEFAULT(DefaultPillarGen),
    VOID(VoidPillarGen)
}
