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
import net.thenextlvl.worlds.WorldsAccess
import org.bukkit.plugin.java.JavaPlugin
import dev.jorel.commandapi.CommandAPI
import org.bukkit.Bukkit


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

        Commands.register()
        Events.register()
        Items.register()

        ITEMS.apply()
        STATISTIC_TYPES.apply()
        TexturePack.register()

        // todo сделать что бы миры удалялись ещё и по завершению игры
        val access = WorldsAccess.access()
        scheduler.schedule {
            Bukkit.getWorlds().forEach { world ->
                if (!Regex(
                        "^myshore_pillars_[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
                    ).matches(world.name)
                ) return@forEach
                access.delete(world)
            }
        }.global().once()
    }

    override fun onDisable() {
        CommandAPI.onDisable()
    }
}
