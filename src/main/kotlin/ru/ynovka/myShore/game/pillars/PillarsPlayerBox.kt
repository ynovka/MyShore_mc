package ru.ynovka.myShore.game.pillars

import com.github.darksoulq.abyssallib.world.structure.StructureLoader
import ru.ynovka.myShore.game.pillars.Pillar.Companion.TELEPORT_Y
import com.github.darksoulq.abyssallib.world.structure.Structure
import ru.ynovka.myShore.MyShore.Companion.scheduler
import org.bukkit.block.structure.StructureRotation
import ru.ynovka.myShore.MyShore.Companion.inst
import org.bukkit.block.structure.Mirror
import org.bukkit.Location
import org.bukkit.World
import java.util.concurrent.CompletableFuture


enum class PillarsPlayerBox(
    val structure: Structure
) {
    DEFAULT(
        StructureLoader.loadResource(inst, "structures/pillars/box/default.struct")
            ?: error("Failed to load pillars player box structure")
    );

    companion object {
        fun create(world: World, pillarLoc: Pillar): CompletableFuture<Void> {
            val future = CompletableFuture<Void>()
            val structure = DEFAULT.structure
            val location = Location(world, pillarLoc.x.toDouble() - 1, TELEPORT_Y - 1, pillarLoc.z.toDouble() - 1)

            // todo сделать в PillarsPlayer функцию getPlayerBox(): PillarsPlayerBox
            scheduler.schedule {
                try {
                    structure.place(
                        location,
                        StructureRotation.NONE,
                        Mirror.NONE,
                        1.0f
                    )
                    future.complete(null)
                } catch (throwable: Throwable) {
                    future.completeExceptionally(throwable)
                }
            }.region(location).once()

            return future
        }
    }
}
