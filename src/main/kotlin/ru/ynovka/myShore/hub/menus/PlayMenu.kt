package ru.ynovka.myShore.hub.menus

import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import net.kyori.adventure.text.format.NamedTextColor
import com.github.darksoulq.abyssallib.world.gui.Gui
import com.github.darksoulq.abyssallib.world.gui.gui
import ru.ynovka.myShore.utils.Utils.toComponent
import ru.ynovka.myShore.texturepack.GuiTextures
import ru.ynovka.myShore.lobby.LobbyManager
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.games.GameId
import org.bukkit.inventory.MenuType
import org.bukkit.entity.Player
import ru.ynovka.myShore.hub.HubItems.playTagItem


object PlayMenu {
    fun get(): Gui = gui(
        MenuType.GENERIC_9X2,
        Component.text()
            .append(TextOffset.getOffsetMinimessage(-8).toComponent().color(NamedTextColor.WHITE))
            .append(GuiTextures.MENU_2x9_048_26!!.toComponent().color(NamedTextColor.WHITE))
            .append(TextOffset.getOffsetMinimessage(-170).toComponent().color(NamedTextColor.WHITE))
            .append(Component.translatable("menu.myshore.minigames"))
            .build(),
    ) {
        set(
            SlotPosition.top(0),
            GuiButton.of(playTagItem.getStack(null)) { ctx ->
                println("pressed playTag item")
                val player = ctx.view.inventoryView.player as Player
                LobbyManager.join(player, GameId.TAG)
            }
        )
    }
}
