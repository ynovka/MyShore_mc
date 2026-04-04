package ru.ynovka.myShore.plasmo

import su.plo.voice.api.event.EventPriority
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.audio.source.ServerDirectSource
import su.plo.voice.api.server.audio.source.ServerPlayerSource
import su.plo.voice.api.server.event.audio.capture.PlayerServerActivationEvent
import su.plo.voice.api.server.player.VoicePlayer
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID


object PhoneCallVoice {

    private lateinit var privateLine: ServerSourceLine
    private lateinit var nearbyLine: ServerSourceLine
    private lateinit var nearbyHeardByCallerLine: ServerSourceLine

    private val privateSources = ConcurrentHashMap<UUID, ServerDirectSource>()
    private val nearbySources = ConcurrentHashMap<UUID, ServerPlayerSource>()
    private val nearbyHeardByCallerSources = ConcurrentHashMap<UUID, ServerPlayerSource>()

    fun init(addon: PlasmoAddon) {
        val vs = addon.voiceServer

        privateLine = vs.sourceLineManager
            .createBuilder(addon, "phone_private", "myshore.phone_private", "speaker", 100)
            .setDefaultVolume(1.0)
            .build()

        nearbyLine = vs.sourceLineManager
            .createBuilder(addon, "phone_nearby", "myshore.phone_nearby", "speaker", 50)
            .setDefaultVolume(1.0)
            .build()

        nearbyHeardByCallerLine = vs.sourceLineManager
            .createBuilder(addon, "phone_nearby_caller", "myshore.phone_nearby_caller", "speaker", 50)
            .setDefaultVolume(0.25)
            .build()
    }

    fun startCallAudio(addon: PlasmoAddon, call: Call) {
        val vs = addon.voiceServer
        val fromVoice = vs.playerManager.getPlayerById(call.from).orElse(null) ?: return
        val toVoice = vs.playerManager.getPlayerById(call.to).orElse(null) ?: return

        // Приватный звонок
        val privateForFrom = privateLine.createDirectSource(toVoice, false)
        privateForFrom.sender = fromVoice
        privateForFrom.isIconVisible = false
        privateSources[call.from] = privateForFrom

        val privateForTo = privateLine.createDirectSource(fromVoice, false)
        privateForTo.sender = toVoice
        privateForTo.isIconVisible = false
        privateSources[call.to] = privateForTo

        // Nearby для окружающих
        val nearbyFrom = nearbyLine.createPlayerSource(fromVoice, false)
        nearbyFrom.isIconVisible = false
        nearbyFrom.addFilter { player: VoicePlayer -> player.instance.uuid != call.to } // окружающие, кроме собеседника
        nearbySources[call.from] = nearbyFrom

        val nearbyTo = nearbyLine.createPlayerSource(toVoice, false)
        nearbyTo.isIconVisible = false
        nearbyTo.addFilter { player: VoicePlayer -> player.instance.uuid != call.from }
        nearbySources[call.to] = nearbyTo

        // Nearby для звонящего (тише слышит окружающих)
        val callerNearbyFrom = nearbyHeardByCallerLine.createPlayerSource(fromVoice, false)
        callerNearbyFrom.isIconVisible = false
        callerNearbyFrom.addFilter { player: VoicePlayer -> player.instance.uuid != fromVoice.instance.uuid && player.instance.uuid != call.to }
        nearbyHeardByCallerSources[call.from] = callerNearbyFrom

        val callerNearbyTo = nearbyHeardByCallerLine.createPlayerSource(toVoice, false)
        callerNearbyTo.isIconVisible = false
        callerNearbyTo.addFilter { player: VoicePlayer -> player.instance.uuid != toVoice.instance.uuid && player.instance.uuid != call.from }
        nearbyHeardByCallerSources[call.to] = callerNearbyTo
    }

    fun stopCallAudio(call: Call) {
        privateSources.remove(call.from)?.remove()
        privateSources.remove(call.to)?.remove()
        nearbySources.remove(call.from)?.remove()
        nearbySources.remove(call.to)?.remove()
        nearbyHeardByCallerSources.remove(call.from)?.remove()
        nearbyHeardByCallerSources.remove(call.to)?.remove()
    }

    @EventSubscribe(ignoreCancelled = false, priority = EventPriority.LOWEST)
    fun onPlayerActivation(event: PlayerServerActivationEvent) {
        val uuid = event.player.instance.uuid

        val frame = event.packet.data
        val sequenceNumber = event.packet.sequenceNumber
        val distance = event.packet.distance

        // Приватный звонок
        privateSources[uuid]?.sendAudioFrame(frame, sequenceNumber)

        // Nearby для окружающих
        nearbySources[uuid]?.sendAudioFrame(frame, sequenceNumber, distance)

        // Nearby для звонящего (тише)
        nearbyHeardByCallerSources.values.forEach { source ->
            if (source.player.instance.uuid != uuid) {
                source.sendAudioFrame(frame, sequenceNumber, distance)
            }
        }
    }
}