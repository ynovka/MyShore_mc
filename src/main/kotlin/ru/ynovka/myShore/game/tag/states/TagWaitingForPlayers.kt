package ru.ynovka.myShore.game.tag.states

import ru.ynovka.myShore.game.tag.TagPlayerSetup.setupForWaitingOrVoting
import ru.ynovka.myShore.game.GamePlayer.Companion.asPlayers
import ru.ynovka.myShore.game.gameUtils.ActionbarWaitingFor
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.tag.TagPlayer
import ru.ynovka.myShore.game.tag.TagGame
import ru.ynovka.myShore.game.GameState
import ru.ynovka.myShore.game.GameWorldOld
import org.bukkit.Sound


// Ожидание игроков (нужно хотя бы 2)
class TagWaitingForPlayers(game: TagGame) : GameState<TagPlayer, GameWorldOld, TagGame>(game) {

    override fun onEnterState() {
        ActionbarWaitingFor.startRendering(
            game = game,
            state = this,
            componentKey = "bar.myshore.waiting_for_players"
        )

        game.gamePlayers.asPlayers().forEach { player ->
            scheduler.schedule {
                player.setupForWaitingOrVoting(game)
                player.playSound(player.location, Sound.BLOCK_COPPER_BULB_TURN_OFF, 0.5f, 2f)
            }.entity(player).once()
        }
    }

    override fun onPlayerJoin(gamePlayer: TagPlayer) {
        val player = gamePlayer.player
        scheduler.schedule {
            player.setupForWaitingOrVoting(game)
        }.entity(player).once()

        if (game.gamePlayers.size >= 2) {
            game.fsm.transitionTo(TagVoting(game))
        }
    }
}