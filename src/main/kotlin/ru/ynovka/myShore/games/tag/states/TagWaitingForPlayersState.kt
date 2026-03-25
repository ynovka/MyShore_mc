package ru.ynovka.myShore.games.tag.states

import ru.ynovka.myShore.games.tag.TagPlayerSetup.setupForWaiting
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.tag.TagPlayer
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GameState
import org.bukkit.Sound


// Ожидание игроков (нужно хотя бы 2)
object TagWaitingForPlayersState : GameState<TagPlayer> {

    override fun onEnter(game: Game<TagPlayer>) {
        val tagGame = game as TagGame
        tagGame.gamePlayers.forEach { tagPlayer ->
            tagPlayer.player.setupForWaiting(tagGame)
            tagPlayer.player.playSound(tagPlayer.player.location, Sound.BLOCK_COPPER_BULB_TURN_OFF, 0.5f, 2f)
        }
    }

    override fun onPlayerJoin(game: Game<TagPlayer>, player: TagPlayer) {
        val tagGame = game as TagGame
        player.player.setupForWaiting(tagGame)

        if (tagGame.gamePlayers.size >= 2) {
            tagGame.fsm.transitionTo(TagVotingState)
        }
    }
}