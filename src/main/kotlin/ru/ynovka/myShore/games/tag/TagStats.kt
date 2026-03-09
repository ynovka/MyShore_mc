package ru.ynovka.myShore.games.tag

import com.github.darksoulq.abyssallib.world.data.statistic.PlayerStatistics
import com.github.darksoulq.abyssallib.world.data.statistic.Statistic
import ru.ynovka.myShore.utils.StatsUtils.incrementStat
import ru.ynovka.myShore.MyShore.Companion.PLUGIN_ID
import ru.ynovka.myShore.utils.StatsUtils.resetStat
import ru.ynovka.myShore.MyShore.Companion.STATS
import ru.ynovka.myShore.utils.Utils.asPlayer
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player


object TagStats {
    val STAT_TAG_VICTIM_WIN_ID = Key.key(PLUGIN_ID, "tag_victim_win")
    val STAT_TAG_HUNTER_WIN_ID = Key.key(PLUGIN_ID, "tag_hunter_win")
    val STAT_TAG_WINSTRIKE_ID = Key.key(PLUGIN_ID, "tag_winstrike")

    val STAT_TAG_VICTIM_LOSE_DIED_ID = Key.key(PLUGIN_ID, "tag_victim_lose_died")
    val STAT_TAG_HUNTER_LOSE_ID = Key.key(PLUGIN_ID, "tag_hunter_lose")

    fun register() {
        println("1111111")
        STATS.register(STAT_TAG_VICTIM_WIN_ID.value() ) { id -> Statistic.of(id, 0) }
        STATS.register(STAT_TAG_HUNTER_WIN_ID.value()) { id -> Statistic.of(id, 0) }
        STATS.register(STAT_TAG_WINSTRIKE_ID.value()) { id -> Statistic.of(id, 0) }
        STATS.register(STAT_TAG_VICTIM_LOSE_DIED_ID.value()) { id -> Statistic.of(id, 0) }
        STATS.register(STAT_TAG_HUNTER_LOSE_ID.value()) { id -> Statistic.of(id, 0) }
    }


    data class PlayerTagStats(
        val victimWin: Int = 0,
        val hunterWin: Int = 0,
        val winstike: Int = 0,
        val victimLose: Int = 0,
        val hunterLose: Int = 0
    ) {
        fun totalPlayed(): Int = victimWin + hunterWin + winstike + victimLose + hunterLose
    }

    private fun PlayerStatistics.getIntValue(id: Key): Int {
        val stat: Statistic.IntStatistic? = this.get(id) as Statistic.IntStatistic?
        return stat?.value ?: 0
    }

    fun getPlayerTagStats(player: Player): PlayerTagStats {
        val stats = PlayerStatistics.of(player)
        return PlayerTagStats(
            victimWin = stats.getIntValue(STAT_TAG_VICTIM_WIN_ID),
            hunterWin = stats.getIntValue(STAT_TAG_HUNTER_WIN_ID),
            winstike = stats.getIntValue(STAT_TAG_WINSTRIKE_ID),
            victimLose = stats.getIntValue(STAT_TAG_VICTIM_LOSE_DIED_ID),
            hunterLose = stats.getIntValue(STAT_TAG_HUNTER_LOSE_ID)
        )
    }

    fun saveStats(game: TagGame, winnerRole: TagPlayerRoles) {
        game.players.forEach { (uuid, role) ->
            val player = uuid.asPlayer() ?: return@forEach

            val isWinner = when (winnerRole) {
                TagPlayerRoles.HUNTER if role == TagPlayerRoles.HUNTER -> true
                TagPlayerRoles.VICTIM if (role ==TagPlayerRoles.VICTIM || role == TagPlayerRoles.SPECTATOR_VICTIM) -> true
                else -> false
            }

            val specificStatId = when (winnerRole) {
                TagPlayerRoles.HUNTER -> when (role) {
                    TagPlayerRoles.HUNTER -> STAT_TAG_HUNTER_WIN_ID
                    else -> STAT_TAG_VICTIM_LOSE_DIED_ID
                }
                TagPlayerRoles.VICTIM -> when (role) {
                    TagPlayerRoles.HUNTER -> STAT_TAG_HUNTER_LOSE_ID
                    TagPlayerRoles.VICTIM, TagPlayerRoles.SPECTATOR_VICTIM -> STAT_TAG_VICTIM_WIN_ID
                    else -> null
                }
                else -> null
            }

            specificStatId?.let { player.incrementStat(it) }

            if (isWinner) {
                player.incrementStat(STAT_TAG_WINSTRIKE_ID)
            } else {
                player.resetStat(STAT_TAG_WINSTRIKE_ID)
            }
        }
    }
}