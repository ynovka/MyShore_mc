package ru.ynovka.myShore.game.pillars

import ru.ynovka.myShore.game.pillars.gameMode.PillarsGameMode
import ru.ynovka.myShore.game.pillars.generators.allocators.AllocatorGenerator
import ru.ynovka.myShore.game.pillars.generators.pillars.PillarGenerator
import ru.ynovka.myShore.game.pillars.generators.platform.PlatformGenerator


data class PillarsRoundSettings(
    val gameMode: PillarsGameMode = PillarsGameMode.NULL,
    val allocator: AllocatorGenerator = AllocatorGenerator.HONEY,
    val platform: PlatformGenerator = PlatformGenerator.NULL,
    val pillar: PillarGenerator = PillarGenerator.DEFAULT
)

class PillarsRoundConfig(
    initial: PillarsRoundSettings = PillarsRoundSettings()
) {
    var next: PillarsRoundSettings = initial

    var current: PillarsRoundSettings = initial
        private set

    fun applyNextTo(world: PillarsWorld) {
        current = next
        world.pillarGen = current.pillar
        world.allocatorGen = current.allocator
        world.platformGen = current.platform
    }
}
