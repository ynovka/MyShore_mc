package ru.ynovka.myShore.games.worldDomination.menus

import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.gui
import net.kyori.adventure.text.Component
import org.bukkit.inventory.MenuType
import ru.ynovka.myShore.games.worldDomination.entity.City
import ru.ynovka.myShore.utils.Utils.fill


@Suppress("UnstableApiUsage")
object WDLaptopCityMenu {
    fun get(city: City) = gui(MenuType.GENERIC_9X4, laptopTitle) {
        laptopNavBar()

        fill(
            SlotPosition.top(9),
            SlotPosition.top(28),
            GuiButton.of(invisibleItem(city.name, listOf(Component.empty())))
        )

        fill(
            SlotPosition.top(12),
            SlotPosition.top(31),
            GuiButton.of(
                invisibleItem(
                    Component.text("Улучшить город"), // todo перевод
                    listOf(Component.empty()) // todo стоимость и "1 уровень -> 2 уровень"
                )
            ) {
                println("улучшить город: ${city.buyUpgrade()}")
            }
        )

        fill(
            SlotPosition.top(15),
            SlotPosition.top(34),
            GuiButton.of(
                invisibleItem(
                    Component.text("Построить щит"), // todo перевод
                    listOf(Component.empty()) // todo стоимость
                )
            ) {
                println("построить щит: ${city.buyShield()}")
            }
        )
    }
}