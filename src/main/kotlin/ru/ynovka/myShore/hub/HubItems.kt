package ru.ynovka.myShore.hub

import com.github.darksoulq.abyssallib.server.event.ActionResult
import com.github.darksoulq.abyssallib.extension.openGui
import com.github.darksoulq.abyssallib.world.item.item
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.hub.menus.PlayMenu
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.EquipmentSlot
import ru.ynovka.myShore.MyShore.Companion.ITEMS
import ru.ynovka.myShore.hub.Hub.toHub
import ru.ynovka.myShore.texturepack.TexturePack


object HubItems {
    fun regsiter() {
        TexturePack.createItemTexture(playMenuItem)
        for (i in 1..3) {
            TexturePack.createItemTexture("${playMenuItem.id.value()}_$i")
        }
        ITEMS.register("play_menu") { playMenuItem }

        // hubTeleportItem ...
    }

    private fun onPlaymenuInteraction(
        player: Player,
        hand: EquipmentSlot
    ) {
        val stack = player.inventory.getItem(hand)
        val meta = stack.itemMeta
        for (i in 1..3) {
            inst.server.scheduler.runTaskLater(inst,
                Runnable {
                    meta.itemModel = NamespacedKey(inst, "${playMenuItem.id.value()}_$i")
                    stack.itemMeta = meta
                }, i * 2L)
        }
        player.openGui(PlayMenu.get())
    }
    val playMenuItem = item(Key.key(inst, "play_menu"), Material.RABBIT_FOOT) {
        onUse { entity, hand, _ ->
            val player = entity as Player
            onPlaymenuInteraction(player, hand)
            ActionResult.CANCEL
        }
        onUseOn { ctx ->
            val player = ctx.source as Player
            onPlaymenuInteraction(player, ctx.hand)
            ActionResult.CANCEL
        }
        onClick { _, _, _, _ ->
            ActionResult.CANCEL
        }
        onSwapHand { _, _ ->
            ActionResult.CANCEL
        }
        onDrop { _ ->
            ActionResult.CANCEL
        }
    }

    val hubTeleportItem = item(Key.key(inst, "hub_teleport"), Material.RABBIT_FOOT) {
        onUse { entity, _, _ ->
            val player = entity as Player
            player.toHub()
            ActionResult.CANCEL
        }
        onUseOn { ctx ->
            val player = ctx.source as Player
            player.toHub()
            ActionResult.CANCEL
        }
        onClick { _, _, _, _ ->
            ActionResult.CANCEL
        }
        onSwapHand { _, _ ->
            ActionResult.CANCEL
        }
        onDrop { _ ->
            ActionResult.CANCEL
        }
    }
}