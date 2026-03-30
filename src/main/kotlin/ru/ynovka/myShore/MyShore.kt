package ru.ynovka.myShore

import com.github.darksoulq.abyssallib.server.registry.DeferredRegistry
import com.github.darksoulq.abyssallib.server.registry.Registries
import com.github.darksoulq.abyssallib.world.data.statistic.Statistic
import com.github.darksoulq.abyssallib.world.item.Item
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import org.bukkit.plugin.java.JavaPlugin
import ru.ynovka.myShore.antiCheat.AntiCheat
import ru.ynovka.myShore.texturepack.TexturePack
import su.plo.voice.api.server.PlasmoVoiceServer


class MyShore : JavaPlugin() {
    companion object {
        lateinit var inst: MyShore
            private set
        const val PLUGIN_ID = "myshore"
        val ITEMS: DeferredRegistry<Item> = DeferredRegistry.create(Registries.ITEMS, PLUGIN_ID)
        val STATS: DeferredRegistry<Statistic> = DeferredRegistry.create(Registries.STATISTICS, PLUGIN_ID)
        val plasmo = PlasmoAddon()
    }

    override fun onLoad() {
        CommandAPI.onLoad(CommandAPIPaperConfig(this))
        PlasmoVoiceServer.getAddonsLoader().load(plasmo)
    }

    override fun onEnable() {
        inst = this
        dataFolder.mkdirs()
        CommandAPI.onEnable()

        AntiCheat.register()
        Database.register(inst)
        Commands.register()
        Events.register()
        Items.register()
        Stats.register()

        ITEMS.apply()
        STATS.apply()
        TexturePack.register()
    }

    override fun onDisable() {
        CommandAPI.onDisable()
        Database.db.disconnect()
    }
}
