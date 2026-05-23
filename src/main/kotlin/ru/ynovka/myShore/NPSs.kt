package ru.ynovka.myShore

import com.github.darksoulq.abyssallib.server.scheduler.Clock
import de.oliver.fancynpcs.api.FancyNpcsPlugin
import de.oliver.fancynpcs.api.actions.ActionTrigger
import de.oliver.fancynpcs.api.actions.NpcAction
import de.oliver.fancynpcs.api.actions.executor.ActionExecutionContext
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.GameManager
import ru.ynovka.myShore.game.pillars.PillarsGame

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