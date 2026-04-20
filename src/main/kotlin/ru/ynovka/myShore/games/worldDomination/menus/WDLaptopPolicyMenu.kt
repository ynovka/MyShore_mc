package ru.ynovka.myShore.games.worldDomination.menus

import com.github.darksoulq.abyssallib.extension.openGui
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.gui
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.utils.Utils.fill


@Suppress("UnstableApiUsage")
object WDLaptopPolicyMenu {
    fun get(
        player: Player,
        wdPlayer: WDPlayer
    ) = gui(MenuType.GENERIC_9X4, laptopTitle) {
        laptopNavBar()

        fill(
            SlotPosition.top(9),
            SlotPosition.top(28),
            GuiButton.of(
                invisibleItem(
                    Component.text("Разведка"), // todo перевод
                    listOf(Component.empty()) // todo стоимость
                )
            ) {
                WDLaptopSelectCountryMenu.get(
                    player,
                    wdPlayer.country!!
                ) { player, country ->
                    println("разведка: TODO") // todo
                }
            }
        )

        fill(
            SlotPosition.top(12),
            SlotPosition.top(31),
            GuiButton.of(
                invisibleItem(
                    Component.text("Санкции"), // todo перевод
                    listOf(Component.empty()) // todo стоимость
                )
            ) {
                WDLaptopSelectCountryMenu.get(
                    player,
                    wdPlayer.country!!
                ) { player, country ->
                    println("санкции: TODO") // todo
                }
            }
        )

        fill(
            SlotPosition.top(15),
            SlotPosition.top(34),
            GuiButton.of(
                invisibleItem(
                    Component.text("Бомбардировка"), // todo перевод
                    listOf(Component.empty()) // todo стоимость
                )
            ) {
                WDLaptopSelectCountryMenu.get(
                    player,
                    wdPlayer.country!!,
                ) { player, country ->
                    player.openGui(
                        WDLaptopSelectCityMenu.get(country) { player, city ->
                            println("бомбардировка: ${city.bombardCity()}")
                        }
                    )
                }
            }
        )
    }
}