package ru.ynovka.myShore.games.worldDomination

import com.github.darksoulq.abyssallib.extension.openGui
import com.github.darksoulq.abyssallib.server.event.ActionResult
import com.github.darksoulq.abyssallib.world.item.component.builtin.ItemModel
import com.github.darksoulq.abyssallib.world.item.item
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import ru.ynovka.myShore.MyShore.Companion.ITEMS
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.GameManager.currentGame
import ru.ynovka.myShore.games.worldDomination.menus.WDPhoneMenu
import ru.ynovka.myShore.plasmo.PhoneCall
import ru.ynovka.myShore.texturepack.TexturePack
import ru.ynovka.myShore.utils.cancelItem

object WDItems {

    fun register() {
        TexturePack.createItemTexture(wdPhoneMenu)
        ITEMS.register("wd_phone_menu") { wdPhoneMenu }
        ITEMS.register("wd_notebook") { wdNotebook }
        ITEMS.register("wd_invisible_item") { wdInvisibleItem }
    }

    val wdInvisibleItem = cancelItem(Key.key(inst, "wd_invisible_item")) {
        component(ItemModel(NamespacedKey.minecraft(Material.AIR.toString().lowercase())))
    }

    val wdNotebook = item(Key.key(inst, "wd_notebook"), Material.WRITABLE_BOOK) {
        component(ItemModel(NamespacedKey.minecraft(Material.WRITABLE_BOOK.toString().lowercase())))
        onClick { _, _, _, _ -> ActionResult.CANCEL }
        onSwapHand { _, _ -> ActionResult.CANCEL }
        tooltip { player ->
            line(Component.translatable("desc.myshore.wd_notebook.1"))
        }
    }

    val wdPhoneMenu = cancelItem(Key.key(inst, "wd_phone_menu")) {
        tooltip { player ->
            line(Component.translatable("desc.myshore.wd_phone_menu.1"))
            line(Component.translatable("desc.myshore.wd_phone_menu.2"))
            line(Component.translatable("desc.myshore.wd_phone_menu.3"))
        }
        onUse { source, _, _ -> openPhoneMenu(source as Player) }
        onUseOn { ctx -> openPhoneMenu(ctx.source as Player) }
        onDrop { _ -> ActionResult.CANCEL }
        onSwapHand { player, _ ->
            PhoneCall.acceptCall(player)
            ActionResult.CANCEL
        }
    }
    private fun openPhoneMenu(
        player: Player
    ): ActionResult {
        val game = player.currentGame() ?: return ActionResult.PASS
        val wdGame = game as? WDGame ?: return ActionResult.PASS
        val wdRole = wdGame.gamePlayers.firstOrNull { it.playerId == player.uniqueId }?.role
            ?: return ActionResult.PASS
        player.openGui(WDPhoneMenu.get(game, wdRole))
        return ActionResult.CANCEL
    }
}