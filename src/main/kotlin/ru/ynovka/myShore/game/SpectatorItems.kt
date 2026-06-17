package ru.ynovka.myShore.game

import com.github.darksoulq.abyssallib.server.event.ActionResult
import com.github.darksoulq.abyssallib.world.item.component.builtin.ItemModel
import com.github.darksoulq.abyssallib.world.item.component.builtin.ItemName
import com.github.darksoulq.abyssallib.world.item.component.builtin.MaxStackSize
import com.github.darksoulq.abyssallib.world.item.item
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import ru.ynovka.myShore.MyShore.Companion.ITEMS
import ru.ynovka.myShore.MyShore.Companion.PLUGIN_ID
import ru.ynovka.myShore.game.gameUtils.SpectatorTeleportMenu


object SpectatorItems {
    fun register() {
        ITEMS.register("spectator_teleport") { teleportCompass }
    }

    val teleportCompass = item(Key.key(PLUGIN_ID, "spectator_teleport"), Material.COMPASS) {
        component(ItemName(Component.text("Teleport to player", NamedTextColor.AQUA)))
        component(ItemModel(Key.key("minecraft", "compass")))
        component(MaxStackSize(1))
        tooltip { _: Player? ->
            line(Component.text("Right click to teleport to an active player.", NamedTextColor.GRAY))
        }
        onClick { _, _, _, _ -> ActionResult.CANCEL }
        onUse { entity, _, _ ->
            openTeleportMenu(entity as Player)
            ActionResult.CANCEL
        }
        onUseOn { ctx ->
            openTeleportMenu(ctx.source as Player)
            ActionResult.CANCEL
        }
        onSwapHand { _, _ -> ActionResult.CANCEL }
        onDrop { _ -> ActionResult.CANCEL }
    }

    private fun openTeleportMenu(player: Player) {
        val game = GameManager.run { player.uniqueId.currentGame<Game<*, *>>() }

        if (game == null || !game.hasSpectator(player.uniqueId)) {
            player.sendMessage(Component.text("This item is only available to spectators.", NamedTextColor.RED))
            return
        }

        SpectatorTeleportMenu.open(player, game)
    }
}
