package ru.ynovka.myShore.game.tag

import ru.ynovka.myShore.game.tag.TagGame.Companion.currentTagGame
import ru.ynovka.myShore.game.GamePlayer.Companion.asPlayers
import org.bukkit.event.entity.EntityDamageByEntityEvent
import ru.ynovka.myShore.Database.tagCaughtsRepository
import ru.ynovka.myShore.game.tag.states.TagFinishing
import ru.ynovka.myShore.game.tag.utils.RiftEffect
import org.bukkit.event.player.PlayerInteractEvent
import ru.ynovka.myShore.texturepack.SoundsPack
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.game.tag.maps.TagMap
import org.bukkit.event.block.BlockBreakEvent
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.entity.Player
import org.bukkit.GameMode
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.SpectatorReason
import ru.ynovka.myShore.game.tag.states.TagInProgressState


object TagEvents : Listener {

    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
        TagMap.maps.forEach { it.registerEvents() }
    }

    @EventHandler
    fun onPlayerHit(event: EntityDamageByEntityEvent) {
        val victim  = event.entity  as? Player ?: return
        val hunter  = event.damager as? Player ?: return

        val game = hunter.uniqueId.currentTagGame() ?: return
        val hunterTagPlayer = game.findPlayer(hunter) ?: return

        // Только охотник может "пятнать"
        if (hunterTagPlayer.role != TagPlayerRoles.HUNTER) {
            event.isCancelled = true
            return
        }

        catchVictim(victim, hunter, game)
        tagCaughtsRepository.save(victim, hunter, game)
        event.isCancelled = true
    }

    /** Вся логика момента поимки — в отдельном методе, легко тестировать. */
    private fun catchVictim(victim: Player, hunter: Player, game: TagGame) {
        val victimTagPlayer = game.findPlayer(victim) ?: return

        game.movePlayerToSpectator(
            victim,
            SpectatorReason.ELIMINATED
        )
        victimTagPlayer.role = TagPlayerRoles.SPECTATOR_VICTIM

        val msg = Component.translatable(
            "msg.myshore.tag.hunter.caught",
            Component.text(hunter.name),
            Component.text(victim.name)
        )

        RiftEffect.play(game, victim)
        game.gamePlayers.asPlayers().forEach { player ->
            scheduler.schedule {
                player.sendMessage(msg)
                SoundsPack.RIFT_SOUND.play(player, 0.3f, 2f)
            }
                .entity(player)
                .once()
        }

        if (!game.hasVictims()) {
            game.fsm.transitionTo(TagFinishing(game))
        } else {
            val state = game.fsm.current
            if (state is TagInProgressState) {
                state.timer.addTime(20)
            }
        }
    }

    // Запрещаем взаимодействие с миром в игровых мирах (не для CREATIVE)
    @EventHandler
    fun onPlayerInteraction(event: PlayerInteractEvent) {
        if (!event.player.isInTagWorld() || event.player.gameMode == GameMode.CREATIVE) return
        event.isCancelled = true
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (!event.player.isInTagWorld() || event.player.gameMode == GameMode.CREATIVE) return
        event.isCancelled = true
    }

    private fun Player.isInTagWorld() = world.name.startsWith("tag_")
}