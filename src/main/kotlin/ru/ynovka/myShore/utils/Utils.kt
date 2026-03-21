package ru.ynovka.myShore.utils

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.Component
import org.bukkit.scoreboard.Team
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import java.util.UUID


object Utils {
    fun String.toComponent(): Component = MiniMessage.miniMessage().deserialize(this)


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

    val Boolean.intValue
        get() = if (this) 1 else 0
}