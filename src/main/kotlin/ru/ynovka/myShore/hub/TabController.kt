package ru.ynovka.myShore.hub

import com.github.darksoulq.abyssallib.server.translation.ServerTranslator
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit


object TabController {

    private val mm = MiniMessage.miniMessage()

    fun updateAll() {
        val online = Bukkit.getOnlinePlayers().size

        val header: Component = mm.deserialize(
            "<newline><bold><gradient:#5653c2:#7B79CF>\u2007\u2007\u2007\u2007MyShore\u2007\u2007\u2007\u2007</gradient></bold><newline>"
        )

        for (player in Bukkit.getOnlinePlayers()) {
            val onlineText = ServerTranslator.translate(
                Component.translatable(
                    "tab.myshore.hub.online",
                    Component.text(online)
                ),
                player.locale()
            )

            val footer: Component = Component.text()
                .append(Component.newline())
                .append(onlineText)
                .append(Component.newline())
                .build()
            player.sendPlayerListHeaderAndFooter(header, footer)
        }
    }
}