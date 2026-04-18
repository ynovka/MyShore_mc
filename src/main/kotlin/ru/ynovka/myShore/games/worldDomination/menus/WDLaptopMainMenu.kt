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
import ru.ynovka.myShore.games.worldDomination.WDGame.Companion.currentWDGame
import ru.ynovka.myShore.games.worldDomination.WDItems
import ru.ynovka.myShore.games.worldDomination.WDItems.wdInvisibleItem
import ru.ynovka.myShore.games.worldDomination.WDPlayer.Companion.asWDPlayer
import ru.ynovka.myShore.utils.Utils.fill


@Suppress("UnstableApiUsage")
object WDLaptopMainMenu {
    fun get(): Gui = gui(
        MenuType.GENERIC_9X4,
        Component.text()
            .append(Component.translatable("menu.myshore.wd.laptop"))
            .build(),
    ) {
        // Раздел "города"
        val cityItem = wdInvisibleItem.getStack(null)
        cityItem.editMeta { meta ->
            // todo перевод
            meta.displayName(Component.text("города"))
        }
        fill(
            SlotPosition.top(0),
            SlotPosition.top(29),
            GuiButton.of(cityItem) { ctx ->
                val player = ctx.view.inventoryView.player as Player
                val wdPlayer = player.asWDPlayer() ?: return@of
                player.openGui(WDLaptopCitiesMenu.get(wdPlayer))
            }
        )

        // Раздел "наука"
        val scienceItem = wdInvisibleItem.getStack(null)
        scienceItem.editMeta { meta ->
            // todo перевод
            meta.displayName(Component.text("наука"))
        }
        fill(
            SlotPosition.top(3),
            SlotPosition.top(32),
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
            SlotPosition.top(35),
            GuiButton.of(policyItem) // todo openGui
        )
    }
}