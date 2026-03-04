package ru.ynovka.myShore.hub.menus

import com.github.darksoulq.abyssallib.server.event.ActionResult
import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import com.github.darksoulq.abyssallib.world.gui.Gui
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.gui
import com.github.darksoulq.abyssallib.world.item.item
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.GameId
import ru.ynovka.myShore.hub.menus.PlayMenu.Items.playTagItem
import ru.ynovka.myShore.utils.Utils.toComponent
import ru.ynovka.myShore.texturepack.GuiTextures
import ru.ynovka.myShore.texturepack.TexturePack
import ru.ynovka.myShore.lobby.LobbyManager


object PlayMenu {
    object Items {
        val playTagItem = item(Key.key(inst, "play_tag"), Material.RABBIT_FOOT) {
            tooltip { p ->
                line(Component.translatable("desc.myshore.minigames.tag.1"))
                line(Component.translatable("desc.myshore.minigames.tag.2"))
            }
            onUse { entity, _, _ ->
                val player = entity as Player
                LobbyManager.join(player, GameId.TAG)
                ActionResult.CANCEL
            }
            onUseOn { ctx ->
                val player = ctx.source as Player
                LobbyManager.join(player, GameId.TAG)
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

    init {
        println("22222222222222222")
        TexturePack.createItemTexture(playTagItem)
    }

    fun get(): Gui = gui(
        MenuType.GENERIC_9X2,
        Component.text()
            .append(TextOffset.getOffsetMinimessage(-8).toComponent().color(NamedTextColor.WHITE))
            .append(GuiTextures.GENERIC_9X2_PAGE_MENU!!.toComponent().color(NamedTextColor.WHITE))
            .append(TextOffset.getOffsetMinimessage(-170).toComponent().color(NamedTextColor.WHITE))
            .append(Component.translatable("menu.myshore.minigames"))
            .build(),
    ) {
        set(
            SlotPosition.top(0),
            GuiButton.of(playTagItem.getStack(null)) {
                println("pressed playTag item")
            }
        )
    }
}
