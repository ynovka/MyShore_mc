package ru.ynovka.myShore.plasmo

import org.bukkit.entity.Player
import su.plo.voice.api.addon.AddonInitializer
import su.plo.voice.api.addon.InjectPlasmoVoice
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.event.connection.UdpClientConnectEvent
import su.plo.voice.api.server.event.connection.UdpClientDisconnectedEvent
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

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
        PhoneCallVoice.init(this)
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