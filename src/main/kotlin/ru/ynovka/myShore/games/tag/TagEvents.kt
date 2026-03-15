package ru.ynovka.myShore.games.tag

import org.bukkit.event.entity.EntityDamageByEntityEvent
import ru.ynovka.myShore.Database.tagCaughtsRepository
import ru.ynovka.myShore.games.tag.maps.TagGameMap
import org.bukkit.event.player.PlayerInteractEvent
import ru.ynovka.myShore.utils.effects.RiftEffect
import ru.ynovka.myShore.texturepack.SoundsPack
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.utils.Utils.asPlayers
import org.bukkit.event.block.BlockBreakEvent
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.lobby.getLobby
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.entity.Player
import org.bukkit.GameMode
import org.bukkit.Sound


object TagEvents : Listener {

    fun register() {
        inst.server.pluginManager.registerEvents(this, inst)
        TagGameMap.maps.forEach { it.registerEvents() }
    }

    @EventHandler
    fun onPlayerHit(event: EntityDamageByEntityEvent) {
        val victim  = event.entity  as? Player ?: return
        val hunter  = event.damager as? Player ?: return

        val game = hunter.getLobby()?.game as? TagGame ?: return

        // Только охотник может "пятнать"
        if (game.players[hunter.uniqueId] != TagPlayerRoles.HUNTER) {
            event.isCancelled = true
            return
        }

        catchVictim(victim, hunter, game)
        tagCaughtsRepository.save(victim, hunter, game)
        event.isCancelled = true
    }

    /** Вся логика момента поимки — в отдельном методе, легко тестировать. */
    private fun catchVictim(victim: Player, hunter: Player, game: TagGame) {
        victim.gameMode = GameMode.SPECTATOR
        victim.clearActivePotionEffects()
        game.players[victim.uniqueId] = TagPlayerRoles.SPECTATOR_VICTIM

        victim.world.playSound(victim.location, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2f, 1.2f)
        RiftEffect.play(game.lobby, victim)

        val msg = Component.translatable(
            "msg.myshore.tag.hunter.caught",
            Component.text(hunter.name),
            Component.text(victim.name)
        )

        game.lobby.members.asPlayers().forEach {
            it.sendMessage(msg)
            SoundsPack.RIFT_SOUND.play(it)
        }

        if (!game.hasVictims()) {
            game.transitionTo(TagGameStates.FINISHING)
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