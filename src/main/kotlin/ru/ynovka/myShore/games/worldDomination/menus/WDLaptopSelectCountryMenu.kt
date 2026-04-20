package ru.ynovka.myShore.games.worldDomination.menus

import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.gui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType
import ru.ynovka.myShore.games.worldDomination.entity.Country
import ru.ynovka.myShore.games.worldDomination.entity.Country.Companion.getFormattedName
import ru.ynovka.myShore.utils.Utils.fill


@Suppress("UnstableApiUsage")
object WDLaptopSelectCountryMenu {
    fun get(
        player: Player,
        cc: Country,
        clickction: ((Player, Country) -> Unit)
    ) = gui(MenuType.GENERIC_9X4, laptopTitle) {
        laptopNavBar()

        val slotMap = listOf(9, 11, 13, 15, 18, 20, 22, 24, 27, 29)

        cc.game.countries.forEach { country ->
            val baseSlot = slotMap[country.type.ordinal]

            if (country == cc) {
                fill(
                    SlotPosition.top(baseSlot),
                    SlotPosition.top(baseSlot + 1),
                    GuiButton.of(
                        invisibleItem(
                            country.getFormattedName(player),
                            listOf(Component.text("вы находитесь здесь").color(NamedTextColor.GRAY))
                        )
                    ) { ctx -> clickction(ctx.view.inventoryView.player as Player, country) }
                )
            } else {
                fill(
                    SlotPosition.top(baseSlot),
                    SlotPosition.top(baseSlot + 1),
                    GuiButton.of(
                        invisibleItem(
                            country.getFormattedName(player),
                            listOf(Component.empty())
                        )
                    ) { ctx -> clickction(ctx.view.inventoryView.player as Player, country) }
                )
            }
        }
    }
}