package ru.ynovka.myShore.games.tag

import org.bukkit.event.entity.EntityDamageByEntityEvent
import ru.ynovka.myShore.Database.tagCaughtsRepository
import ru.ynovka.myShore.games.tag.maps.TagMap
import ru.ynovka.myShore.games.tag.states.TagFinishing
import org.bukkit.event.player.PlayerInteractEvent
import ru.ynovka.myShore.utils.effects.RiftEffect
import ru.ynovka.myShore.texturepack.SoundsPack
import ru.ynovka.myShore.MyShore.Companion.inst
import org.bukkit.event.block.BlockBreakEvent
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.entity.Player
import org.bukkit.GameMode
import ru.ynovka.myShore.games.GamePlayer.Companion.asPlayers


object TagEvents : Listener {

    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
        TagMap.maps.forEach { it.registerEvents() }
    }

    @EventHandler
    fun onPlayerHit(event: EntityDamageByEntityEvent) {
        val victim  = event.entity  as? Player ?: return
        val hunter  = event.damager as? Player ?: return

        val game = hunter.currentTagGame() ?: return
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

        victim.gameMode = GameMode.SPECTATOR
        victim.clearActivePotionEffects()
        victimTagPlayer.role = TagPlayerRoles.SPECTATOR_VICTIM

        val msg = Component.translatable(
            "msg.myshore.tag.hunter.caught",
            Component.text(hunter.name),
            Component.text(victim.name)
        )

        RiftEffect.play(game, victim)
        game.gamePlayers.asPlayers().forEach {
            it.sendMessage(msg)
            SoundsPack.RIFT_SOUND.play(it, 0.3f, 2f)
        }

        if (!game.hasVictims()) {
            game.fsm.transitionTo(TagFinishing(game))
        } else {
            game.totalTime += 20 // +20 сек за поимку жертвы
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

/** Возвращает TagGame, в которой сейчас находится игрок, или null. */
fun Player.currentTagGame(): TagGame? =
    ru.ynovka.myShore.games.GameManager.run { currentGame() } as? TagGame