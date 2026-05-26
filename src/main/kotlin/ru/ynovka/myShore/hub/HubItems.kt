package ru.ynovka.myShore.hub

import com.github.darksoulq.abyssallib.server.event.ActionResult
import com.github.darksoulq.abyssallib.world.item.item
import ru.ynovka.myShore.MyShore.Companion.ITEMS
import ru.ynovka.myShore.texturepack.TexturePack
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.hub.Hub.toHub
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.Material


object HubItems {
    fun register() {
        TexturePack.createItemTexture(hubTeleport)
        ITEMS.register("hub_teleport") { hubTeleport }
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
}