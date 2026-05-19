package ru.ynovka.myShore.game.worldDomination.menus

import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import com.github.darksoulq.abyssallib.world.gui.Gui
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.gui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType
import ru.ynovka.myShore.game.worldDomination.entity.City
import ru.ynovka.myShore.game.worldDomination.entity.Country
import ru.ynovka.myShore.texturepack.GuiTextures
import ru.ynovka.myShore.utils.Utils.fill


@Suppress("UnstableApiUsage")
object WDLaptopSelectCityMenu {
    fun get(
        country: Country,
        player: Player,
        clickction: ((Player, City) -> Unit)
    ): Gui = gui(
        MenuType.GENERIC_9X4,
        Component.text().color(NamedTextColor.WHITE)
            .append(TextOffset.getOffset(-8))
            .append(GuiTextures.MENU_WD_SELECT_CITY)
            .append(TextOffset.getOffset(-170))
            .append(getLaptopTitle(player))
            .build()) {
        laptopNavBar()

        country.cities.values.forEachIndexed { idx, city ->
            val base = idx * 2 + (if (idx >= 2) 1 else 0) + 9
            fill(
                SlotPosition.top(base),
                SlotPosition.top(base + 19),
                GuiButton.of(
                    invisibleItem(city.name, listOf(Component.empty()))
                ) { ctx ->
                    clickction(ctx.view.inventoryView.player as Player, city)
                }
            )
        }
    }
}