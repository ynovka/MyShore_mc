package ru.ynovka.myShore.game.tag.statistics

import com.github.darksoulq.abyssallib.world.data.statistic.PlayerStatistics
import com.github.darksoulq.abyssallib.world.data.statistic.StatisticType
import ru.ynovka.myShore.MyShore.Companion.STATISTIC_TYPES
import ru.ynovka.myShore.game.tag.TagPlayerRoles
import ru.ynovka.myShore.game.tag.TagGame
import net.kyori.adventure.key.Key
import ru.ynovka.myShore.MyShore
import org.bukkit.entity.Player


object TagPlayerStatistics {
    val STAT_TAG_VICTIM_WIN_ID       = Key.key(MyShore.Companion.PLUGIN_ID, "tag_victim_win")
    val STAT_TAG_HUNTER_WIN_ID       = Key.key(MyShore.Companion.PLUGIN_ID, "tag_hunter_win")
    val STAT_TAG_WINSTRIKE_ID        = Key.key(MyShore.Companion.PLUGIN_ID, "tag_winstrike")
    val STAT_TAG_VICTIM_LOSE_DIED_ID = Key.key(MyShore.Companion.PLUGIN_ID, "tag_victim_lose_died")
    val STAT_TAG_HUNTER_LOSE_ID      = Key.key(MyShore.Companion.PLUGIN_ID, "tag_hunter_lose")

    lateinit var STAT_TAG_VICTIM_WIN: StatisticType
    lateinit var STAT_TAG_HUNTER_WIN: StatisticType
    lateinit var STAT_TAG_WINSTRIKE: StatisticType
    lateinit var STAT_TAG_VICTIM_LOSE_DIED: StatisticType
    lateinit var STAT_TAG_HUNTER_LOSE: StatisticType

    fun register() {
        STAT_TAG_VICTIM_WIN = STATISTIC_TYPES.register(STAT_TAG_VICTIM_WIN_ID.value(), ::StatisticType)
        STAT_TAG_HUNTER_WIN = STATISTIC_TYPES.register(STAT_TAG_HUNTER_WIN_ID.value(), ::StatisticType)
        STAT_TAG_WINSTRIKE = STATISTIC_TYPES.register(STAT_TAG_WINSTRIKE_ID.value(), ::StatisticType)
        STAT_TAG_VICTIM_LOSE_DIED = STATISTIC_TYPES.register(STAT_TAG_VICTIM_LOSE_DIED_ID.value(), ::StatisticType)
        STAT_TAG_HUNTER_LOSE = STATISTIC_TYPES.register(STAT_TAG_HUNTER_LOSE_ID.value(), ::StatisticType)
    }

    data class PlayerTagStats(
        val victimWin: Int = 0,
        val hunterWin: Int = 0,
        val winstike:  Int = 0,
        val victimLose: Int = 0,
        val hunterLose: Int = 0,
    )

    fun getPlayerTagStats(player: Player): PlayerTagStats {
        val stats = PlayerStatistics.of(player)
        return PlayerTagStats(
            victimWin  = stats.get(STAT_TAG_VICTIM_WIN.get(STAT_TAG_VICTIM_WIN_ID)),
            hunterWin  = stats.get(STAT_TAG_HUNTER_WIN.get(STAT_TAG_HUNTER_WIN_ID)),
            winstike   = stats.get(STAT_TAG_WINSTRIKE.get(STAT_TAG_WINSTRIKE_ID)),
            victimLose = stats.get(STAT_TAG_VICTIM_LOSE_DIED.get(STAT_TAG_VICTIM_LOSE_DIED_ID)),
            hunterLose = stats.get(STAT_TAG_HUNTER_LOSE.get(STAT_TAG_HUNTER_LOSE_ID)),
        )
    }

    fun saveStats(game: TagGame, winnerRole: TagPlayerRoles) {
        game.gamePlayers.forEach { tagPlayer ->
            val player = tagPlayer.player
            val role = tagPlayer.role
            val stats = PlayerStatistics.of(player)

            val isWinner = when (winnerRole) {
                TagPlayerRoles.HUNTER if role == TagPlayerRoles.HUNTER -> true
                TagPlayerRoles.VICTIM if (role == TagPlayerRoles.VICTIM || role == TagPlayerRoles.SPECTATOR_VICTIM) -> true
                else -> false
            }

            val specificStatId = when (winnerRole) {
                TagPlayerRoles.HUNTER -> when (role) {
                    TagPlayerRoles.HUNTER -> STAT_TAG_HUNTER_WIN to STAT_TAG_HUNTER_WIN_ID
                    else -> STAT_TAG_VICTIM_LOSE_DIED to STAT_TAG_VICTIM_LOSE_DIED_ID
                }
                TagPlayerRoles.VICTIM -> when (role) {
                    TagPlayerRoles.HUNTER -> STAT_TAG_HUNTER_LOSE to STAT_TAG_HUNTER_LOSE_ID
                    TagPlayerRoles.VICTIM, TagPlayerRoles.SPECTATOR_VICTIM -> STAT_TAG_VICTIM_WIN to STAT_TAG_VICTIM_WIN_ID
                    else -> null
                }
                else -> null
            }

            specificStatId?.let { stats.increment(it.first.get(it.second), 1) }

            if (isWinner) stats.increment(STAT_TAG_WINSTRIKE.get(STAT_TAG_WINSTRIKE_ID), 1)
            else stats.set(STAT_TAG_WINSTRIKE.get(STAT_TAG_WINSTRIKE_ID), 0)
        }
    }
}