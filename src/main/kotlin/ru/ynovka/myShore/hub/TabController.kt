package ru.ynovka.myShore.hub

import com.github.darksoulq.abyssallib.extension.mmString
import com.github.darksoulq.abyssallib.server.translation.ServerTranslator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit


object TabController {

    private val mm = MiniMessage.miniMessage()

    fun updateAll() {
        val online = Bukkit.getOnlinePlayers().size

        val header: Component = mm.deserialize(
            "<newline>\u2007\u2007\u2007\u2007<bold><gradient:#5653c2:#7B79CF>MyShore</gradient></bold>\u2007\u2007\u2007\u2007<newline>"
        )

        for (player in Bukkit.getOnlinePlayers()) {
            val onlineText = ServerTranslator.translate(
                Component.translatable(
                    "desc.myshore.hub.online",
                    online.toString()
                ),
                player.locale()
            )

            println("onlineText: ${onlineText.mmString}")

            val footer: Component = Component.text()
                .append(Component.newline())
                .append(onlineText)
                .append(Component.newline())
                .build()
            player.sendPlayerListHeaderAndFooter(header, footer)
        }
    }
}