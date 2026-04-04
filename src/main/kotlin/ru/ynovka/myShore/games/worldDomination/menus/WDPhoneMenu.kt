package ru.ynovka.myShore.games.worldDomination.menus

import com.github.darksoulq.abyssallib.extension.closeGui
import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import net.kyori.adventure.text.format.NamedTextColor
import com.github.darksoulq.abyssallib.world.gui.Gui
import com.github.darksoulq.abyssallib.world.gui.GuiLayer
import com.github.darksoulq.abyssallib.world.gui.GuiView
import com.github.darksoulq.abyssallib.world.gui.gui
import ru.ynovka.myShore.utils.Utils.toComponent
import ru.ynovka.myShore.texturepack.GuiTextures
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.MenuType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.worldDomination.WDItems
import ru.ynovka.myShore.games.worldDomination.WDPlayerRole
import ru.ynovka.myShore.games.worldDomination.WDPlayer
import ru.ynovka.myShore.games.worldDomination.WDPlayer.Companion.asWDPlayer
import ru.ynovka.myShore.games.worldDomination.entity.Country.Companion.getFormattedName
import ru.ynovka.myShore.plasmo.PhoneCall


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
    ) { layer(WDPhoneLayer(game, targetRole)) }

    class WDPhoneLayer(
        private val game: Game<WDPlayer>,
        private val targetRole: WDPlayerRole
    ) : GuiLayer {
        override fun renderTo(view: GuiView) {
            val players = game.gamePlayers
                .filter { it.role == targetRole }
                .sortedBy { it.country?.president?.playerId }

            players.forEachIndexed { index, target ->
                val head = try { target.player.skull
                } catch (_: Exception) { ItemStack(Material.PLAYER_HEAD) }

                view.inventoryView.setItem(SlotPosition.top(index).index, head)
                view.gui.elements[SlotPosition.top(index)] = GuiButton.of(head) { ctx ->
                    val player = ctx.view.inventoryView.player as Player
                    val playerFormattedName = player.asWDPlayer()?.getFormattedName() ?: return@of

                    val head = ctx.currentItem ?: return@of
                    val targetPlayerUUID = (head.itemMeta as SkullMeta).owningPlayer?.uniqueId ?: return@of
                    val targetPlayer = Bukkit.getPlayer(targetPlayerUUID) ?: return@of
                    val targetFormattedName = targetPlayer.asWDPlayer()?.getFormattedName() ?: return@of

                    // звоним выбранному игроку
                    PhoneCall.call(
                        player,
                        playerFormattedName,
                        targetPlayer,
                        targetFormattedName
                    )

                    val phone = WDItems.wdPhoneMenu.getStack(null)

                    // Перемещаем телефон в offhand
                    player.inventory.clear(7)
                    player.inventory.setItemInOffHand(phone)
                    targetPlayer.inventory.clear(7)
                    targetPlayer.inventory.setItemInOffHand(phone)

                    player.closeGui()
                }
            }
        }

        override fun cleanup(view: GuiView) {
            val size = view.top.size
            for (i in 0 until size) {
                view.gui.elements.remove(SlotPosition.top(i))
                view.top.setItem(i, null)
            }
        }
    }
}