package ru.ynovka.myShore

import com.github.darksoulq.abyssallib.server.registry.DeferredRegistry
import com.github.darksoulq.abyssallib.server.registry.Registries
import com.github.darksoulq.abyssallib.server.scheduler.Scheduler
import com.github.darksoulq.abyssallib.world.data.statistic.StatisticType
import com.github.darksoulq.abyssallib.world.item.Item
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.java.JavaPlugin
import ru.ynovka.myShore.antiCheat.AntiCheat
import ru.ynovka.myShore.plasmo.PlasmoAddon
import ru.ynovka.myShore.texturepack.TexturePack
import su.plo.voice.api.server.PlasmoVoiceServer


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

        AntiCheat.register()
        Database.register(inst)
        Commands.register()
        Events.register()
        Items.register()
        Stats.register()

        ITEMS.apply()
        STATISTIC_TYPES.apply()
        TexturePack.register()
    }

    override fun onDisable() {
        CommandAPI.onDisable()
        Database.db.disconnect()
    }
}
