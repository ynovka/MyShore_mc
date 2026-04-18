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
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.utils.Utils.fill


@Suppress("UnstableApiUsage")
object WDLaptopCitiesMenu {
    fun get(
        wdPlayer: WDPlayer
    ): Gui = gui(
        MenuType.GENERIC_9X4,
        Component.text()
            .append(Component.translatable("menu.myshore.wd.laptop"))
            .build(),
    ) {
        // Раздел "города"
        val cities = wdPlayer.country!!.cities.values
        cities.mapIndexed { idx, city ->
            val stack = wdInvisibleItem.getStack(null)
            stack.editMeta { meta ->
                meta.displayName(city.name)
                // todo добавить в lore данные о городе
                meta.lore(listOf(Component.empty()))
            }
            val add = if (idx > 1) 1 else 0
            fill(
                SlotPosition.top(idx * 2 + add + 9),
                SlotPosition.top(idx * 2 + add + 28),
                GuiButton.of(stack) { ctx ->
                    val player = ctx.view.inventoryView.player as Player
                    player.openGui(WDLaptopCityMenu.get(city))
                }
            )
        }


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