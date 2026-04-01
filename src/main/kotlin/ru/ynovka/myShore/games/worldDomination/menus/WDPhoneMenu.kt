package ru.ynovka.myShore.games.worldDomination.menus

import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import ru.ynovka.myShore.games.tag.TagItems.tagVoteJungleMapMenuItem
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import net.kyori.adventure.text.format.NamedTextColor
import com.github.darksoulq.abyssallib.world.gui.Gui
import com.github.darksoulq.abyssallib.world.gui.gui
import ru.ynovka.myShore.utils.Utils.toComponent
import ru.ynovka.myShore.texturepack.GuiTextures
import net.kyori.adventure.text.Component
import org.bukkit.inventory.MenuType
import org.bukkit.entity.Player
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.worldDomination.WDPlayerRole
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.worldDomination.entity.Country.Companion.getFlag


@Suppress("UnstableApiUsage")
object WDPhoneMenu {
    fun get(
        game: Game<WDPlayer>,
        targetRole: WDPlayerRole
    ): Gui = gui(
        MenuType.GENERIC_9X6,
        Component.text()
            .append(TextOffset.getOffsetMinimessage(-8).toComponent().color(NamedTextColor.WHITE))
            .append(GuiTextures.MENU_6x9!!.toComponent().color(NamedTextColor.WHITE))
            .append(TextOffset.getOffsetMinimessage(-170).toComponent().color(NamedTextColor.WHITE))
            .append(Component.translatable("menu.myshore.wd.phone"))
            .build(),
    ) {
        onOpen { e ->
            game.gamePlayers
                .asSequence()
                .filter { it.role == targetRole && it.playerId != e.player.uniqueId }
                .sortedBy { it.country?.president?.playerId }
                .forEachIndexed { index, target ->
                    val targetPlayer = target.player
                    val icon = targetPlayer.skull
                    icon.editMeta { meta ->
                        meta.displayName(
                            Component.text("${target.country.getFlag()} ${targetPlayer.name}").color(NamedTextColor.WHITE)
                        )
                    }
                    set(
                        SlotPosition.top(index),
                        GuiButton.of(icon) { ctx ->
                            val player = ctx.view.inventoryView.player as Player
                            // todo звоним выбранному игроку

                            // todo отложенная задача через 10 секунд
                            //  если трубку не взяли воспроизводим аудио "Абонент временно недоступен, перезвоните позже"
                        }
                    )
                }
        }
    }
}