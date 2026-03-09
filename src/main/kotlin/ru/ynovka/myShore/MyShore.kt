package ru.ynovka.myShore

import com.github.darksoulq.abyssallib.server.registry.DeferredRegistry
import com.github.darksoulq.abyssallib.world.data.statistic.Statistic
import com.github.darksoulq.abyssallib.server.registry.Registries
import com.github.darksoulq.abyssallib.world.item.Item
import dev.jorel.commandapi.CommandAPIPaperConfig
import ru.ynovka.myShore.texturepack.TexturePack
import org.bukkit.plugin.java.JavaPlugin
import dev.jorel.commandapi.CommandAPI


class MyShore : JavaPlugin() {
    companion object {
        lateinit var inst: MyShore
            private set
        const val PLUGIN_ID = "myshore"
        val ITEMS: DeferredRegistry<Item> = DeferredRegistry.create(Registries.ITEMS, PLUGIN_ID)
        val STATS: DeferredRegistry<Statistic> = DeferredRegistry.create(Registries.STATISTICS, PLUGIN_ID)
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
        Items.register()
        println("000000000")
        Stats.register()

        ITEMS.apply()
        println("222222222")
        STATS.apply()
        TexturePack.register()
    }

    override fun onDisable() {
        CommandAPI.onDisable()
    }
}
