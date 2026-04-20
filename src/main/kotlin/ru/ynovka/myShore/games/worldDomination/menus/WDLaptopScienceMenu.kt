package ru.ynovka.myShore.games.worldDomination.menus

import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.gui
import net.kyori.adventure.text.Component
import org.bukkit.inventory.MenuType
import ru.ynovka.myShore.games.worldDomination.entity.Country
import ru.ynovka.myShore.utils.Utils.fill


@Suppress("UnstableApiUsage")
object WDLaptopScienceMenu {
    fun get(country: Country) = gui(MenuType.GENERIC_9X4, laptopTitle) {
        laptopNavBar()

        val nuclearButton = if (country.isNuclearLearned) {
            GuiButton.of(
                invisibleItem(
                    Component.text("Создать бомбу"), // todo перевод
                    listOf(Component.empty()) // todo цена, сколько бомб доступно
                )
            ) {
                println("создать бомбу: ${country.createNuclearBomb()}")
            }
        } else {
            GuiButton.of(
                invisibleItem(
                    Component.text("Изучить ядерную технологию"), // todo перевод
                    listOf(Component.empty()) // todo цена
                )
            ) {
                println("изучить ядерную технологию: ${country.learnNuclear()}")
            }
        }

        fill(
            SlotPosition.top(18),
            SlotPosition.top(30),
            nuclearButton
        )

        fill(
            SlotPosition.top(23),
            SlotPosition.top(35),
            GuiButton.of(
                invisibleItem(
                    Component.text("Вклад в экологию"), // todo перевод
                    listOf(Component.empty()) // todo стоимость
                )
            ) {
                println("вклад в экологию: ${country.investmentsEcology()}")
            }
        )
    }
}