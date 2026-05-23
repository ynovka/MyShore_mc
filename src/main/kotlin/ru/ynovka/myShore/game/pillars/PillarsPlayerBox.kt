package ru.ynovka.myShore.game.pillars

import com.github.darksoulq.abyssallib.world.structure.StructureLoader
import com.github.darksoulq.abyssallib.world.structure.Structure
import org.bukkit.block.structure.StructureRotation
import ru.ynovka.myShore.MyShore.Companion.inst
import org.bukkit.block.structure.Mirror
import org.bukkit.Location
import org.bukkit.World
import ru.ynovka.myShore.game.pillars.Pillar.Companion.TELEPORT_Y


enum class PillarsPlayerBox(
    val structure: Structure
) {
    DEFAULT(
        StructureLoader.loadResource(inst, "structures/pillars/box/default.struct")
            ?: error("Failed to load pillars player box structure")
    );

    companion object {
        fun create(world: World, pillarLoc: Pillar) {
            // todo сделать в PillarsPlayer функцию getPlayerBox(): PillarsPlayerBox
            val structure = DEFAULT.structure
            structure.place(
                Location(world, pillarLoc.x.toDouble() - 1, TELEPORT_Y - 1, pillarLoc.z.toDouble() - 1),
                StructureRotation.NONE,
                Mirror.NONE,
                1.0f
            )
        }
    }
}