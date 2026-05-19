package ru.ynovka.myShore.game.pillars

import com.github.darksoulq.abyssallib.world.structure.StructureLoader
import com.github.darksoulq.abyssallib.world.structure.Structure
import org.bukkit.block.structure.StructureRotation
import ru.ynovka.myShore.MyShore.Companion.inst
import org.bukkit.block.structure.Mirror
import org.bukkit.Location
import org.bukkit.World


enum class PillarsPlayerBox(
    val structure: Structure
) {
    DEFAULT(StructureLoader.loadResource(inst, "structures/pillars/box/default.json"));

    companion object {
        fun create(world: World, pillarLoc: Pillar) {
            // todo сделать в PillarsPlayer функцию getPlayerBox(): PillarsPlayerBox
            val structure = DEFAULT.structure
            structure.placeAsync(
                inst,
                Location(world, pillarLoc.x.toDouble(), 100.0, pillarLoc.z.toDouble()),
                StructureRotation.NONE,
                Mirror.NONE,
                1.0f,
                64
            )
        }
    }
}