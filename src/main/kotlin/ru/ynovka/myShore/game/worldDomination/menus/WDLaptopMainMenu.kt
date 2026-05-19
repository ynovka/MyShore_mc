package ru.ynovka.myShore.game.worldDomination.menus

import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import ru.ynovka.myShore.game.worldDomination.WDItems.wdInvisibleItem
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import com.github.darksoulq.abyssallib.world.gui.GuiBuilder
import com.github.darksoulq.abyssallib.extension.openGui
import net.kyori.adventure.text.format.NamedTextColor
import com.github.darksoulq.abyssallib.world.gui.Gui
import com.github.darksoulq.abyssallib.world.gui.gui
import ru.ynovka.myShore.texturepack.GuiTextures
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.utils.Utils.fill
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.entity.Player
import ru.ynovka.myShore.game.worldDomination.WDPlayer.Companion.asWDPlayer


@Suppress("UnstableApiUsage")
object WDLaptopMainMenu {
    fun get(player: Player): Gui = gui(
        MenuType.GENERIC_9X4,
        Component.text().color(NamedTextColor.WHITE)
            .append(TextOffset.getOffset(-8))
            .append(GuiTextures.MENU_WD_MAIN)
            .append(TextOffset.getOffset(-170))
            .append(getLaptopTitle(player))
            .build()
    ) {
        laptopNavBar(29)
    }
}

fun getLaptopTitle(player: Player): Component {
    val balance = player.uniqueId.asWDPlayer()?.country?.balance ?: 0
    return Component.translatable(
        "menu.myshore.wd.laptop",
        Component.text(balance)
    )
}

internal fun invisibleItem(
    name: Component,
    lore: List<Component> = emptyList()
): ItemStack = wdInvisibleItem.getStack(null).apply {
    editMeta { meta ->
        meta.displayName(name)
        if (lore.isNotEmpty()) meta.lore(lore)
    }
}

internal fun GuiBuilder.laptopNavBar(
    fromSlot: Int = 2
) {
    val citiesItem  = invisibleItem(Component.translatable("menu.myshore.wd.section.cities"))
    val scienceItem = invisibleItem(Component.translatable("menu.myshore.wd.section.science"))
    val policyItem  = invisibleItem(Component.translatable("menu.myshore.wd.section.policy"))

    fill(
        SlotPosition.top(0),
        SlotPosition.top(fromSlot),
        GuiButton.of(citiesItem) { ctx ->
            val player = ctx.view.inventoryView.player as Player
            val wdPlayer = player.uniqueId.asWDPlayer() ?: return@of

            wdPlayer.country?.let { country ->
                player.openGui(
                    WDLaptopSelectCityMenu.get(country, player) { player, city ->
                        player.openGui(WDLaptopCityMenu.get(city, player))
                    }
                )
            }
        }
    )

    fill(
        SlotPosition.top(3),
        SlotPosition.top(fromSlot + 3),
        GuiButton.of(scienceItem) { ctx ->
            val player = ctx.view.inventoryView.player as Player
            val wdPlayer = player.uniqueId.asWDPlayer() ?: return@of
            val country = wdPlayer.country ?: return@of

            player.openGui(WDLaptopScienceMenu.get(country, player))
        }
    )

    fill(
        SlotPosition.top(6),
        SlotPosition.top(fromSlot + 6),
        GuiButton.of(policyItem) { ctx ->
            val player = ctx.view.inventoryView.player as Player
            val wdPlayer = player.uniqueId.asWDPlayer() ?: return@of

            player.openGui(WDLaptopPolicyMenu.get(player, wdPlayer))
        }
    )
}