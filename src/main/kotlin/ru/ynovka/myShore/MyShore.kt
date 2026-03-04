package ru.ynovka.myShore

import com.github.darksoulq.abyssallib.server.registry.DeferredRegistry
import com.github.darksoulq.abyssallib.server.registry.Registries
import com.github.darksoulq.abyssallib.world.item.Item
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import org.bukkit.plugin.java.JavaPlugin
import ru.ynovka.myShore.hub.Hub
import ru.ynovka.myShore.texturepack.TexturePack


class MyShore : JavaPlugin() {
    companion object {
        lateinit var inst: MyShore
            private set
        const val pluginId = "myshore"
        val ITEMS: DeferredRegistry<Item> = DeferredRegistry.create(Registries.ITEMS, pluginId)
    }

    override fun onLoad() {
        CommandAPI.onLoad(CommandAPIPaperConfig(this))
    }

    override fun onEnable() {
        inst = this
        CommandAPI.onEnable()

        Translator.register()
        Commands.register()
        Events.register()

        Hub

        ITEMS.apply()
        TexturePack.register()
    }

    override fun onDisable() {
        CommandAPI.onDisable()
    }
}
