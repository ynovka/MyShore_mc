package ru.ynovka.myShore.game.tag.states

import ru.ynovka.myShore.game.tag.TagPlayerSetup.setupForWaiting
import ru.ynovka.myShore.game.tag.TagPlayer
import ru.ynovka.myShore.game.tag.TagGame
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.game.GameWorld
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