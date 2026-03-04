package ru.ynovka.myShore.games.tag.states

import ru.ynovka.myShore.games.tag.TagGame
import org.bukkit.entity.Player


interface TagState {
    fun onStateStart(game: TagGame) {}
    fun onPlayerJoin(game: TagGame, player: Player) {}
}