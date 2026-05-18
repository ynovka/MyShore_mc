package ru.ynovka.myShore.hub

import com.github.darksoulq.abyssallib.server.event.ActionResult
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import com.github.darksoulq.abyssallib.extension.openGui
import com.github.darksoulq.abyssallib.world.item.item
import ru.ynovka.myShore.MyShore.Companion.ITEMS
import ru.ynovka.myShore.texturepack.TexturePack
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.hub.menus.PlayMenu
import org.bukkit.inventory.EquipmentSlot
import ru.ynovka.myShore.utils.cancelItem
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.hub.Hub.toHub
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.NamespacedKey
import org.bukkit.Material


object HubItems {
    fun register() {
        TexturePack.createItemTexture(playMenu)
        for (i in 1..3) {
            TexturePack.createItemTexture("${playMenu.id.value()}_$i")
        }
        ITEMS.register("play_menu") { playMenu }

        TexturePack.createItemTexture(playTagItem)
        TexturePack.createItemTexture(playWDItem)

        TexturePack.createItemTexture(hubTeleport)
        ITEMS.register("hub_teleport") { hubTeleport }
    }

    private fun onPlaymenuInteraction(
        player: Player,
        hand: EquipmentSlot
    ) {
        val stack = player.inventory.getItem(hand)
        val meta = stack.itemMeta
        for (i in 1..3) {
            scheduler.schedule {
                meta.itemModel = NamespacedKey(inst, "${playMenu.id.value()}_$i")
                stack.itemMeta = meta
            }
                .sync()
                .after(i * 2L, Clock.TICKS)
                .once()
        }
        player.openGui(PlayMenu.get())
    }
    val playMenu = item(Key.key(inst, "play_menu"), Material.RABBIT_FOOT) {
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

    val hubTeleport = item(Key.key(inst, "hub_teleport"), Material.RABBIT_FOOT) {
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

    val playTagItem = cancelItem(Key.key(inst, "play_tag")) {
        tooltip { p ->
            line(Component.translatable("desc.myshore.play_tag.1"))
            line(Component.translatable("desc.myshore.play_tag.2"))
            line(Component.translatable("desc.myshore.play_tag.3"))
        }
    }

    val playWDItem = cancelItem(Key.key(inst, "play_wd")) {
        tooltip { p ->
            line(Component.translatable("desc.myshore.play_wd.1"))
            line(Component.translatable("desc.myshore.play_wd.2"))
            line(Component.translatable("desc.myshore.play_wd.3"))
        }
    }

    val playPillarsItem = cancelItem(Key.key(inst, "play_pillars")) {
        tooltip { p ->
            line(Component.translatable("desc.myshore.play_pillars.1"))
        }
    }
}