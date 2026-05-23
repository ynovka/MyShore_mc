package ru.ynovka.myShore

import ru.ynovka.myShore.game.tag.statistics.TagPlayerStatistics

object Stats {
    fun register() {
        TagPlayerStatistics.register()
    }
}