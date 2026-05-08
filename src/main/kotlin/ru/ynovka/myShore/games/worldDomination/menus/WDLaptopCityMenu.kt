package ru.ynovka.myShore.games.worldDomination.menus

import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.gui
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType
import ru.ynovka.myShore.games.worldDomination.entity.City
import ru.ynovka.myShore.games.worldDomination.entity.City.Companion.SHIELD_COST
import ru.ynovka.myShore.games.worldDomination.entity.City.Companion.UPGRADE_COST
import ru.ynovka.myShore.utils.Utils.fill


@Suppress("UnstableApiUsage")
object WDLaptopCityMenu {
    fun get(city: City, player: Player) = gui(MenuType.GENERIC_9X4, getLaptopTitle(player)) {
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
                    listOf(
                        Component.text("Стоимость улучшения: $UPGRADE_COST"),
                        Component.text("${city.lvl} уровень -> ${city.lvl+1} уровень")
                    ) // todo перевод
                )
            ) { ctx ->
                val player = (ctx.source as Player)
                city.buyUpgrade().send(player)
            }
        )

        fill(
            SlotPosition.top(15),
            SlotPosition.top(34),
            GuiButton.of(
                invisibleItem(
                    Component.text("Установить щит"), // todo перевод
                    listOf(
                        Component.text("Стоимость установки: $SHIELD_COST"),
                        Component.text("Наличие щита: ${if (city.hasShield) "ЕСТЬ" else "НЕТУ"}")
                    ) // todo стоимость
                )
            ) { ctx ->
                val player = (ctx.source as Player)
                city.buyShield().send(player)
            }
        )
    }
}