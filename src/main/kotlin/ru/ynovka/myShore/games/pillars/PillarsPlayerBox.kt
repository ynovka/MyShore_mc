package ru.ynovka.myShore.games.pillars

import com.github.darksoulq.abyssallib.world.structure.Structure
import com.github.darksoulq.abyssallib.world.structure.StructureLoader
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import ru.ynovka.myShore.MyShore.Companion.inst


enum class PillarsPlayerBox(
    val structure: Structure
) {
    DEFAULT(StructureLoader.loadResource(inst, "structures/pillars/box/default.json"));

    companion object {
        fun create(world: World, pPlayer: PillarsPlayer, pillarLoc: Pillar) {
            // todo сделать в PillarsPlayer функцию getPlayerBox(): PillarsPlayerBox
            val structure = DEFAULT.structure
            structure.place(
                Location(world, pillarLoc.x.toDouble(), 100.0, pillarLoc.z.toDouble()),
                StructureRotation.NONE,
                Mirror.NONE,
                1.0f
            )
        }
    }
}