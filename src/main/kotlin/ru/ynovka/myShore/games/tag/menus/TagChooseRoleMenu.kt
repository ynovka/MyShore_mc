package ru.ynovka.myShore.games.tag.menus

import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import com.github.darksoulq.abyssallib.world.gui.Gui
import com.github.darksoulq.abyssallib.world.gui.gui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.inventory.MenuType
import ru.ynovka.myShore.texturepack.GuiTextures


@Suppress("UnstableApiUsage")
object TagChooseRoleMenu {
    fun get(): Gui = gui(
        MenuType.GENERIC_9X1,
        Component.text().color(NamedTextColor.WHITE)
            .append(TextOffset.getOffset(-8))
            .append(GuiTextures.MENU_1x9_028)
            .append(TextOffset.getOffset(-170))
            .append(Component.translatable("menu.myshore.minigames"))
            .build(),
    ) {
    }
}