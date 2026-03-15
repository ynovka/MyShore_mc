package ru.ynovka.myShore.games.tag.menus

import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import net.kyori.adventure.text.format.NamedTextColor
import com.github.darksoulq.abyssallib.world.gui.Gui
import com.github.darksoulq.abyssallib.world.gui.gui
import ru.ynovka.myShore.hub.HubItems.playTagItem
import ru.ynovka.myShore.texturepack.GuiTextures
import ru.ynovka.myShore.utils.Utils.toComponent
import net.kyori.adventure.text.Component
import org.bukkit.inventory.MenuType


@Suppress("UnstableApiUsage")
object TagChooseRoleMenu {
    fun get(): Gui = gui(
        MenuType.GENERIC_9X1,
        Component.text()
            .append(TextOffset.getOffsetMinimessage(-8).toComponent().color(NamedTextColor.WHITE))
            .append(GuiTextures.MENU_1x9_028!!.toComponent().color(NamedTextColor.WHITE))
            .append(TextOffset.getOffsetMinimessage(-170).toComponent().color(NamedTextColor.WHITE))
            .append(Component.translatable("menu.myshore.minigames"))
            .build(),
    ) {
        set(
            SlotPosition.top(0),
            GuiButton.of(playTagItem.getStack(null))
        )
        set(
            SlotPosition.top(3),
            GuiButton.of(playTagItem.getStack(null))
        )
        set(
            SlotPosition.top(5),
            GuiButton.of(playTagItem.getStack(null))
        )
    }
}