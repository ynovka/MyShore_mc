package ru.ynovka.myShore.game.worldDomination.menus

import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.gui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.inventory.MenuType
import ru.ynovka.myShore.game.worldDomination.entity.Country
import ru.ynovka.myShore.game.worldDomination.entity.Country.Companion.getFormattedName
import ru.ynovka.myShore.texturepack.GuiTextures
import ru.ynovka.myShore.utils.Utils.fill


@Suppress("UnstableApiUsage")
object WDLaptopSelectCountryMenu {
    fun get(
        cc: Country,
        player: Player,
        clickction: ((Player, Country) -> Unit)
    ) = gui(
        MenuType.GENERIC_9X4,
        Component.text().color(NamedTextColor.WHITE)
            .append(TextOffset.getOffset(-8))
            .append(GuiTextures.MENU_WD_SELECT_COUNTRY)
            .append(TextOffset.getOffset(-170))
            .append(getLaptopTitle(player))
            .build()) {
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