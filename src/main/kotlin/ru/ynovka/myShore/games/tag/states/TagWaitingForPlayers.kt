package ru.ynovka.myShore.games.tag.states

import ru.ynovka.myShore.games.tag.TagPlayerSetup.setupForWaiting
import ru.ynovka.myShore.games.tag.TagPlayer
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.GameState
import ru.ynovka.myShore.games.GameWorld
import org.bukkit.Sound


// Ожидание игроков (нужно хотя бы 2)
class TagWaitingForPlayers(game: TagGame) : GameState<TagPlayer, GameWorld, TagGame>(game) {

    override fun onEnterState() {
        game.gamePlayers.forEach { tagPlayer ->
            tagPlayer.player.setupForWaiting(game)
            tagPlayer.player.playSound(tagPlayer.player.location, Sound.BLOCK_COPPER_BULB_TURN_OFF, 0.5f, 2f)
        }
    }

    override fun onPlayerJoin(gamePlayer: TagPlayer) {
        gamePlayer.player.setupForWaiting(game)

        if (game.gamePlayers.size >= 2) {
            game.fsm.transitionTo(TagVoting(game))
        }
    }
}