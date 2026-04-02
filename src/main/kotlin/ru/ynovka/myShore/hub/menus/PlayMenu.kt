package ru.ynovka.myShore.hub.menus

import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import net.kyori.adventure.text.format.NamedTextColor
import com.github.darksoulq.abyssallib.world.gui.Gui
import com.github.darksoulq.abyssallib.world.gui.gui
import ru.ynovka.myShore.utils.Utils.toComponent
import ru.ynovka.myShore.texturepack.GuiTextures
import net.kyori.adventure.text.Component
import org.bukkit.inventory.MenuType
import org.bukkit.entity.Player
import ru.ynovka.myShore.games.GameManager
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.hub.HubItems.playTagItem
import ru.ynovka.myShore.hub.HubItems.playWDItem


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
                val player = ctx.view.inventoryView.player as Player
                GameManager.join(player, ::TagGame)
            }
        )
        set(
            SlotPosition.top(11),
            GuiButton.of(playWDItem.getStack(null)) { ctx ->
                val player = ctx.view.inventoryView.player as Player
                GameManager.join(player, ::WDGame)
            }
        )
    }
}
