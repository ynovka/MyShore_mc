package ru.ynovka.myShore

import de.oliver.fancynpcs.api.actions.executor.ActionExecutionContext
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.MyShore.Companion.scheduler
import de.oliver.fancynpcs.api.actions.ActionTrigger
import ru.ynovka.myShore.game.pillars.PillarsGame
import de.oliver.fancynpcs.api.actions.NpcAction
import de.oliver.fancynpcs.api.FancyNpcsPlugin
import ru.ynovka.myShore.game.GameManager

object NPCs {
    fun register() {
        scheduler.schedule {
            val fancyNpcs = FancyNpcsPlugin.get()

            val minigames = fancyNpcs.npcManager.getNpc("minigames") ?: return@schedule

            val minigamesAction = object : NpcAction("open_minigames_menu", false) {
                override fun execute(context: ActionExecutionContext, value: String?) {
                    val player = context.getPlayer() ?: return
                    // todo заменить на openGui
                    GameManager.join(player, ::PillarsGame)
                }
            }

            fancyNpcs.actionManager.registerAction(minigamesAction)

            minigames.data.addAction(
                ActionTrigger.ANY_CLICK,
                0,
                minigamesAction,
                null
            )

            minigames.updateForAll()
        }.global().after(10 * 20L, Clock.TICKS)
    }
}