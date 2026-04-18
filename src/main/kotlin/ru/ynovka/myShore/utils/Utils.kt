package ru.ynovka.myShore.utils

import com.github.darksoulq.abyssallib.world.gui.GuiBuilder
import com.github.darksoulq.abyssallib.world.gui.GuiElement
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team
import ru.ynovka.myShore.MyShore.Companion.mm
import java.util.*


object Utils {
    fun String.toComponent(): Component = mm.deserialize(this)


    fun UUID.asPlayer(): Player? = Bukkit.getPlayer(this)
    fun Iterable<UUID>.asPlayers(): List<Player> = mapNotNull(Bukkit::getPlayer)


    fun Player.clearTeams() {
        val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        val entry = this.name
        for (team: Team in scoreboard.teams) {
            if (team.hasEntry(entry)) {
                team.removeEntry(entry)
            }
        }
    }

    fun <T> Iterable<T>.distribute(groupsCount: Int): List<MutableList<T>> {
        require(groupsCount > 0)

        val groups = List(groupsCount) { mutableListOf<T>() }

        forEachIndexed { index, element ->
            val groupIndex = index % groupsCount
            groups[groupIndex].add(element)
        }

        return groups
    }

    fun GuiBuilder.fill(
        from: SlotPosition,
        to: SlotPosition,
        element: GuiElement
    ) {
        require(from.segment() == to.segment()) { "Both positions must be in the same segment" }
        val width = 9
        val fromRow = from.index() / width
        val fromCol = from.index() % width
        val toRow = to.index() / width
        val toCol = to.index() % width

        for (row in minOf(fromRow, toRow)..maxOf(fromRow, toRow)) {
            for (col in minOf(fromCol, toCol)..maxOf(fromCol, toCol)) {
                set(SlotPosition(from.segment(), row * width + col), element)
            }
        }
    }

    val Boolean.intValue
        get() = if (this) 1 else 0
}