package ru.ynovka.myShore

import com.github.darksoulq.abyssallib.world.data.statistic.StatisticType
import com.github.darksoulq.abyssallib.server.registry.DeferredRegistry
import com.github.darksoulq.abyssallib.server.registry.Registries
import com.github.darksoulq.abyssallib.server.scheduler.Scheduler
import net.kyori.adventure.text.minimessage.MiniMessage
import com.github.darksoulq.abyssallib.world.item.Item
import dev.jorel.commandapi.CommandAPIPaperConfig
import ru.ynovka.myShore.texturepack.TexturePack
import su.plo.voice.api.server.PlasmoVoiceServer
import ru.ynovka.myShore.plasmo.PlasmoAddon
import org.bukkit.plugin.java.JavaPlugin
import dev.jorel.commandapi.CommandAPI


class MyShore : JavaPlugin() {
    companion object {
        lateinit var inst: MyShore
            private set
        lateinit var scheduler: Scheduler
            private set
        const val PLUGIN_ID = "myshore"
        val mm = MiniMessage.miniMessage()
        val ITEMS: DeferredRegistry<Item> = DeferredRegistry.create(Registries.ITEMS, PLUGIN_ID)
        val STATISTIC_TYPES: DeferredRegistry<StatisticType> = DeferredRegistry.create(Registries.STATISTIC_TYPES, PLUGIN_ID)
        val plasmo = PlasmoAddon()
    }

    override fun onLoad() {
        CommandAPI.onLoad(CommandAPIPaperConfig(this))
        PlasmoVoiceServer.getAddonsLoader().load(plasmo)
    }

    override fun onEnable() {
        inst = this
        scheduler = Scheduler(this)
        dataFolder.mkdirs()
        CommandAPI.onEnable()

        NPCs.register()
        Commands.register()
        Events.register()
        Items.register()

        ITEMS.apply()
        STATISTIC_TYPES.apply()
        TexturePack.register()
    }

    override fun onDisable() {
        CommandAPI.onDisable()
    }
}
