package ru.ynovka.myShore.games.worldDomination

import com.github.darksoulq.abyssallib.extension.openGui
import com.github.darksoulq.abyssallib.server.event.ActionResult
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.ynovka.myShore.MyShore.Companion.ITEMS
import ru.ynovka.myShore.MyShore.Companion.inst
import ru.ynovka.myShore.games.GameManager.currentGame
import ru.ynovka.myShore.games.worldDomination.menus.WDPhoneMenu
import ru.ynovka.myShore.texturepack.TexturePack
import ru.ynovka.myShore.utils.cancelItem

object WDItems {

    fun register() {
        TexturePack.createItemTexture(wdPhoneMenu)
        ITEMS.register("wd_phone_menu") { wdPhoneMenu }
    }

    val wdPhoneMenu = cancelItem(Key.key(inst, "wd_phone_menu")) {
        tooltip { player ->
            line(Component.translatable("desc.myshore.wd_phone_menu.1"))
            line(Component.translatable("desc.myshore.wd_phone_menu.2"))
            line(Component.translatable("desc.myshore.wd_phone_menu.3"))
        }
        onUse { source, _, _ ->
            val player = source as Player
            val game = player.currentGame() ?: return@onUse ActionResult.PASS
            val wdGame = game as? WDGame ?: return@onUse ActionResult.PASS
            val wdRole = wdGame.gamePlayers.firstOrNull { it.playerId == player.uniqueId }?.role
                ?: return@onUse ActionResult.PASS
            player.openGui(WDPhoneMenu.get(game, wdRole))
            ActionResult.CANCEL
        }
        onUseOn { ctx ->
            val player = ctx.source as Player
            val game = player.currentGame() ?: return@onUseOn ActionResult.PASS
            val wdGame = game as? WDGame ?: return@onUseOn ActionResult.PASS
            val wdRole = wdGame.gamePlayers.firstOrNull { it.playerId == player.uniqueId }?.role
                ?: return@onUseOn ActionResult.PASS
            player.openGui(WDPhoneMenu.get(game, wdRole))
            ActionResult.CANCEL
        }
    }
}