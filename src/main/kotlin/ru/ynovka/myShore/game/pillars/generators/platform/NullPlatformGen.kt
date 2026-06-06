package ru.ynovka.myShore.game.pillars.generators.platform

import ru.ynovka.myShore.game.pillars.Pillar
import org.bukkit.World
import java.util.concurrent.CompletableFuture


object NullPlatformGen : PlatformGen {
    override fun generate(world: World, pillars: Collection<Pillar>): CompletableFuture<Void> {
        return CompletableFuture.completedFuture(null)
    }
}
