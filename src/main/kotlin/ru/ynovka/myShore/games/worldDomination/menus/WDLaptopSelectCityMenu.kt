package ru.ynovka.myShore.games.worldDomination.menus

import com.github.darksoulq.abyssallib.world.gui.Gui
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.gui
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType
import ru.ynovka.myShore.games.worldDomination.entity.City
import ru.ynovka.myShore.games.worldDomination.entity.Country
import ru.ynovka.myShore.utils.Utils.fill


@Suppress("UnstableApiUsage")
object WDLaptopSelectCityMenu {
    fun get(
        country: Country,
        clickction: ((Player, City) -> Unit)
    ): Gui = gui(MenuType.GENERIC_9X4, laptopTitle) {
        laptopNavBar()

        country.cities.values.forEachIndexed { idx, city ->
            val base = idx * 2 + (if (idx >= 2) 1 else 0) + 9
            fill(
                SlotPosition.top(base),
                SlotPosition.top(base + 10),
                GuiButton.of(
                    invisibleItem(city.name, listOf(Component.empty()))
                ) { ctx ->
                    clickction(ctx.view.inventoryView.player as Player, city)
                }
            )
        }
    }
}