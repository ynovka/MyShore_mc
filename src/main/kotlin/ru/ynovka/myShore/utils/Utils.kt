package ru.ynovka.myShore.utils

import org.bukkit.scoreboard.Team
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import java.util.*


object Utils {
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
}