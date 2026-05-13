package ru.ynovka.myShore.plasmo

import su.plo.voice.api.server.event.connection.UdpClientDisconnectedEvent
import su.plo.voice.api.server.event.connection.UdpClientConnectEvent
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.addon.InjectPlasmoVoice
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.addon.AddonInitializer
import java.util.concurrent.ConcurrentHashMap
import su.plo.voice.api.event.EventSubscribe
import org.bukkit.entity.Player
import java.util.Collections
import java.util.UUID


@Addon(
    id = "myshore-plasmo-addon",
    name = "MyShore Plasmo Voice Addon",
    version = "1.0.0",
    authors = [PlasmoAddon.AUTHOR]
)
class PlasmoAddon : AddonInitializer {
    @InjectPlasmoVoice
    lateinit var voiceServer: PlasmoVoiceServer
    val connected: MutableSet<UUID> = Collections.newSetFromMap(ConcurrentHashMap())

    /** Нужно перед подключением к играм, требующим plasmo voice */
    fun isPlayerConnected(player: Player): Boolean = connected.contains(player.uniqueId)

    override fun onAddonInitialize() {
        StageVoice.init(this)
        PhoneCallVoice.init(this)
        voiceServer.eventBus.register(this, StageVoice)
        voiceServer.eventBus.register(this, PhoneCallVoice)
    }

    override fun onAddonShutdown() {
        voiceServer.eventBus.unregister(this, PhoneCallVoice)
    }

    @EventSubscribe
    fun onClientConnected(event: UdpClientConnectEvent) {
        if (event.isCancelled) return
        val uuid = event.connection.player.instance.uuid
        connected.add(uuid)
    }

    @EventSubscribe
    fun onClientDisconnected(event: UdpClientDisconnectedEvent) {
        val uuid = event.connection.player.instance.uuid
        connected.remove(uuid)
    }


    companion object {
        const val AUTHOR = "Ynovka"
    }
}