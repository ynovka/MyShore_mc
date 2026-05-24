package ru.ynovka.myShore.text.chat

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import ru.ynovka.myShore.MyShore
import ru.ynovka.myShore.party.getParty
import ru.ynovka.myShore.utils.Utils.asPlayers
import ru.ynovka.myShore.game.gameUtils.VisibilityGroup.Companion.getVisiblePlayers

object ChatEvents : Listener {

    fun register() {
        MyShore.Companion.inst.server.pluginManager.registerEvents(this, MyShore.Companion.inst)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        e.joinMessage(Component.empty())
        e.player.getVisiblePlayers(true).asPlayers().forEach { t ->
            t.sendMessage(
                MyShore.mm.deserialize("<#7bed47>⏵ <white>${e.player.name}")
            )
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        e.quitMessage(Component.empty())
        e.player.getVisiblePlayers(true).asPlayers().forEach { t ->
            t.sendMessage(
                MyShore.Companion.mm.deserialize("<#dc424e>⏴ <white>${e.player.name}")
            )
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(e: AsyncChatEvent) {
        val plain = PlainTextComponentSerializer.plainText().serialize(e.message())
        val party = e.player.getParty()
        val hasPrefix = plain.startsWith("!")

        val messageText = if (hasPrefix) plain.drop(1) else plain
        val allowed = when {
            party != null && !hasPrefix -> party.members
            else -> e.player.getVisiblePlayers(true)
        }
        val messageColor = when {
            party != null && !hasPrefix -> "<#cccccc>"
            else -> "<white>"
        }
        val preffixColor = when {
            party != null && !hasPrefix -> "<#d0ecfd>"
            else -> "<#56baf8>"
        }

        val safeMessage = MyShore.Companion.mm.escapeTags(messageText)

        e.viewers().clear()
        e.viewers().addAll(allowed.asPlayers())
        e.renderer { source, _, _, _ ->
            MyShore.Companion.mm.deserialize("$preffixColor| <#87CEFA>${source.name} <#e0e0e0>» $messageColor$safeMessage")
        }
    }
}