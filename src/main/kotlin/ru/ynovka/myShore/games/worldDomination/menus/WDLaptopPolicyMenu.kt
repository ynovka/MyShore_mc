package ru.ynovka.myShore.games.worldDomination.menus

import com.github.darksoulq.abyssallib.extension.openGui
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.gui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.worldDomination.entity.WDAction
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
            ) { ctx ->
                (ctx.source as Player).openGui(
                    WDLaptopSelectCountryMenu.get(
                        player,
                        wdPlayer.country!!
                    ) { targetPlayer, country ->
                        val myCountry = wdPlayer.country ?: return@get
                        val stats = myCountry.spy(country)

                        if (stats == null) {
                            targetPlayer.sendMessage(
                                Component.text("Недостаточно средств для разведки.").color(NamedTextColor.RED)
                            ) // todo перевод
                            return@get
                        }

                        if (stats.isEmpty()) {
                            targetPlayer.sendMessage(
                                Component.text("Нет данных о действиях этой страны.").color(NamedTextColor.GRAY)
                            ) // todo перевод
                            return@get
                        }

                        val msg = Component.text().append(Component.text("Разведданные:").color(NamedTextColor.GOLD)) // todo перевод
                        stats.forEach { (action, count) ->
                            msg.appendNewline().append(
                                Component.text("  ${action.name}: ×$count").color(when (action) { // todo перевод action.name
                                    WDAction.SPY -> NamedTextColor.AQUA
                                    WDAction.BOMBARDMENT -> NamedTextColor.RED
                                    WDAction.SANCTION -> NamedTextColor.YELLOW
                                    WDAction.NUCLEAR_LEARNED, WDAction.NUCLEAR_BOMB_CREATED -> NamedTextColor.LIGHT_PURPLE
                                    else -> NamedTextColor.WHITE
                                })
                            )
                        }
                        targetPlayer.sendMessage(msg.build())
                    }
                )
            }
        )

        fill(
            SlotPosition.top(12),
            SlotPosition.top(31),
            GuiButton.of(
                invisibleItem(
                    Component.text("Санкции"), // todo перевод
                    listOf(
                        Component.text("Наложите санкции на страну на 1 раунд"),
                        Component.text("Длится 1 раунд, уменьшает доход страны")
                    ) // todo стоимость
                )
            ) { ctx ->
                (ctx.source as Player).openGui(
                    WDLaptopSelectCountryMenu.get(
                        player,
                        wdPlayer.country!!
                    ) { _, country ->
                        wdPlayer.country?.sanctionCountry(country)
                    }
                )
            }
        )

        fill(
            SlotPosition.top(15),
            SlotPosition.top(34),
            GuiButton.of(
                invisibleItem(
                    Component.text("Бомбардировка"), // todo перевод
                    listOf(
                        Component.text("Отправьте 1 ядерную бомбу своему врагу").color(NamedTextColor.GRAY) // todo перевод
                    )
                )
            ) { ctx ->
                (ctx.source as Player).openGui(
                    WDLaptopSelectCountryMenu.get(
                        player,
                        wdPlayer.country!!,
                    ) { targetPlayer, country ->
                        targetPlayer.openGui(
                            WDLaptopSelectCityMenu.get(country) { _, city ->
                                wdPlayer.country?.scheduleBombardment(city)
                            }
                        )
                    }
                )
            }
        )
    }
}