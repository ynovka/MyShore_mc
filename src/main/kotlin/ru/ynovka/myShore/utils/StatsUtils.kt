package ru.ynovka.myShore.utils

import com.github.darksoulq.abyssallib.world.data.statistic.PlayerStatistics
import com.github.darksoulq.abyssallib.world.data.statistic.Statistic
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player

object StatsUtils {
    fun Player.decrementStat(id: Key, amount: Int = 1) = this.incrementStat(id, -1)
    fun Player.incrementStat(id: Key, amount: Int = 1) {
        val statistics = PlayerStatistics.of(this)
        val stat: Statistic.IntStatistic? = statistics.get(id) as Statistic.IntStatistic?
        if (stat != null) {
            val newValue = stat.value + amount
            stat.setValue(newValue)
            statistics.set(stat)
        }
    }

    fun Player.resetStat(id: Key) {
        val statistics = PlayerStatistics.of(this)
        val stat: Statistic.IntStatistic? = statistics.get(id) as Statistic.IntStatistic?
        if (stat != null) {
            stat.setValue(0)
            statistics.set(stat)
        }
    }
}