package ru.ynovka.myShore

import ru.ynovka.myShore.games.tag.statistics.TagPlayerStatistics

object Stats {
    fun register() {
        TagPlayerStatistics.register()
    }
}