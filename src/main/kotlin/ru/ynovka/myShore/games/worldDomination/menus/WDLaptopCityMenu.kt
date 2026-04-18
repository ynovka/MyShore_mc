package ru.ynovka.myShore.games.worldDomination.menus

import com.github.darksoulq.abyssallib.extension.openGui
import com.github.darksoulq.abyssallib.world.gui.Gui
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.gui
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import ru.ynovka.myShore.games.worldDomination.WDItems.wdInvisibleItem
import ru.ynovka.myShore.games.worldDomination.WDPlayer.Companion.asWDPlayer
import ru.ynovka.myShore.games.worldDomination.entity.City
import ru.ynovka.myShore.utils.Utils.fill


@Suppress("UnstableApiUsage")
object WDLaptopCityMenu {
    fun get(
        city: City
    ): Gui = gui(
        MenuType.GENERIC_9X4,
        Component.text()
            .append(Component.translatable("menu.myshore.wd.laptop"))
            .build(),
    ) {
        val cityItem = wdInvisibleItem.getStack(null)
        cityItem.editMeta { meta ->
            meta.displayName(city.name)
            // todo добавить в lore данные о городе
            meta.lore(listOf(Component.empty()))
        }
        fill(
            SlotPosition.top(9),
            SlotPosition.top(19),
            GuiButton.of(cityItem) { ctx ->
                val player = ctx.view.inventoryView.player as Player
            }
        )

        // Кнопка улучшить город
        val upgradeCityItem = wdInvisibleItem.getStack(null)
        upgradeCityItem.editMeta { meta ->
            meta.displayName(Component.text("Улучшить город"))
            // todo добавить в lore стоимость улучшения и "1 уровень -> 2 уровень"
            meta.lore(listOf(Component.empty()))
        }
        fill(
            SlotPosition.top(12),
            SlotPosition.top(22),
            GuiButton.of(upgradeCityItem) { ctx ->
                val player = ctx.view.inventoryView.player as Player
                println("улучшить город: ${city.buyUpgrade()}")
                // city.buyUpgrade()
            }
        )

        // Кнопка построить щит
        val buyShieldCityItem = wdInvisibleItem.getStack(null)
        buyShieldCityItem.editMeta { meta ->
            meta.displayName(Component.text("Построить щит"))
            // todo добавить в lore стоимость щита
            meta.lore(listOf(Component.empty()))
        }
        fill(
            SlotPosition.top(15),
            SlotPosition.top(25),
            GuiButton.of(buyShieldCityItem) { ctx ->
                val player = ctx.view.inventoryView.player as Player
                println("построить щит: ${city.buyShield()}")
                // city.buyShield()
            }
        )

        // Раздел "города"
        val cityItem2 = wdInvisibleItem.getStack(null)
        cityItem2.editMeta { meta ->
            // todo перевод
            meta.displayName(Component.text("города"))
        }
        fill(
            SlotPosition.top(0),
            SlotPosition.top(2),
            GuiButton.of(cityItem2)
        )

        // Раздел "наука"
        val scienceItem = wdInvisibleItem.getStack(null)
        scienceItem.editMeta { meta ->
            // todo перевод
            meta.displayName(Component.text("наука"))
        }
        fill(
            SlotPosition.top(3),
            SlotPosition.top(5),
            GuiButton.of(scienceItem) // todo openGui
        )

        // Раздел "политика"
        val policyItem = wdInvisibleItem.getStack(null)
        policyItem.editMeta { meta ->
            // todo перевод
            meta.displayName(Component.text("политика"))
        }
        fill(
            SlotPosition.top(6),
            SlotPosition.top(8),
            GuiButton.of(policyItem) // todo openGui
        )
    }
}