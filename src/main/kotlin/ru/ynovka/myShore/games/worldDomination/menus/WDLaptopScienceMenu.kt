package ru.ynovka.myShore.games.worldDomination.menus

import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.gui
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType
import ru.ynovka.myShore.games.worldDomination.entity.Country
import ru.ynovka.myShore.games.worldDomination.entity.Country.Companion.CRAFT_NUCLEAR_BOMB_COST
import ru.ynovka.myShore.games.worldDomination.entity.Country.Companion.ECOLOGY_COST
import ru.ynovka.myShore.games.worldDomination.entity.Country.Companion.LEARN_NUCLEAR_COST
import ru.ynovka.myShore.utils.Utils.fill


@Suppress("UnstableApiUsage")
object WDLaptopScienceMenu {
    fun get(country: Country) = gui(MenuType.GENERIC_9X4, laptopTitle) {
        laptopNavBar()

        val nuclearButton = if (country.isNuclearLearned) {
            GuiButton.of(
                invisibleItem(
                    Component.text("Создать бомбу"), // todo перевод
                    listOf(Component.text("Стоимость: $CRAFT_NUCLEAR_BOMB_COST")) // todo цена, сколько бомб доступно
                )
            ) { ctx ->
                val player = (ctx.source as Player)
                country.createNuclearBomb().send(player)
            }
        } else {
            GuiButton.of(
                invisibleItem(
                    Component.text("Изучить ядерную технологию"), // todo перевод
                    listOf(Component.text("Стоимость: $LEARN_NUCLEAR_COST")) // todo цена
                )
            ) { ctx ->
                val player = (ctx.source as Player)
                country.learnNuclear().send(player)
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
                    listOf(Component.text("Стоимость: $ECOLOGY_COST")) // todo стоимость
                )
            ) { ctx ->
                val player = (ctx.source as Player)
                country.investmentsEcology().send(player)
            }
        )
    }
}