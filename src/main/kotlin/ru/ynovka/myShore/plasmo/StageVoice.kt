package ru.ynovka.myShore.plasmo

import su.plo.voice.api.server.event.audio.capture.PlayerServerActivationEvent
import su.plo.voice.api.server.audio.source.ServerDirectSource
import su.plo.voice.api.server.audio.capture.ServerActivation
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.api.server.PlasmoVoiceServer
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ConcurrentHashMap
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.event.EventPriority
import java.util.UUID


object StageVoice {

    private lateinit var voiceServer: PlasmoVoiceServer
    private lateinit var stageLine: ServerSourceLine

    private val stages = CopyOnWriteArrayList<Stage>()

    private val speakerStages = ConcurrentHashMap<UUID, Stage>()
    private val listenerStages = ConcurrentHashMap<UUID, Stage>()

    fun init(addon: PlasmoAddon) {
        voiceServer = addon.voiceServer

        stageLine = voiceServer.sourceLineManager
            .createBuilder(addon, "stage", "myshore.stage", "speaker", 100)
            .setDefaultVolume(1.0)
            .build()
    }

    @EventSubscribe(ignoreCancelled = false, priority = EventPriority.LOWEST)
    fun onPlayerActivation(event: PlayerServerActivationEvent) {
        val uuid = event.player.instance.uuid

        if (listenerStages.containsKey(uuid)) {
            event.result = ServerActivation.Result.HANDLED
            return
        }

        val stage = speakerStages[uuid] ?: return

        val frame = event.packet.data
        val sequenceNumber = event.packet.sequenceNumber

        stage.sendToAll(frame, sequenceNumber)

        event.result = ServerActivation.Result.HANDLED
    }

    fun createStage(players: Collection<UUID>): Stage {
        val stage = Stage()

        players.distinct().forEach { uuid ->
            val source = createSource(uuid) ?: return@forEach

            stage.sources[uuid] = source
            stage.listeners[uuid] = source
            listenerStages[uuid] = stage
        }

        stages += stage

        return stage
    }

    fun setSpeakers(
        stage: Stage,
        speakerUuids: Collection<UUID>
    ) {
        val speakerSet = speakerUuids.toHashSet()

        synchronized(stage) {
            val currentPlayers = stage.sources.keys.toList()

            for (uuid in currentPlayers) {
                val source = stage.sources[uuid] ?: continue

                if (uuid in speakerSet) {
                    stage.listeners.remove(uuid)
                    stage.speakers[uuid] = source

                    listenerStages.remove(uuid, stage)
                    speakerStages[uuid] = stage
                } else {
                    stage.speakers.remove(uuid)
                    stage.listeners[uuid] = source

                    speakerStages.remove(uuid, stage)
                    listenerStages[uuid] = stage
                }
            }
        }
    }

    fun addSpeaker(
        stage: Stage,
        uuid: UUID
    ): Boolean {
        val source = createSource(uuid) ?: return false

        synchronized(stage) {
            removePlayerInternal(uuid, removeSource = true)

            stage.sources[uuid] = source
            stage.speakers[uuid] = source
            stage.listeners.remove(uuid)

            speakerStages[uuid] = stage
            listenerStages.remove(uuid)
        }

        return true
    }

    fun addListener(
        stage: Stage,
        uuid: UUID
    ): Boolean {
        val source = createSource(uuid) ?: return false

        synchronized(stage) {
            removePlayerInternal(uuid, removeSource = true)

            stage.sources[uuid] = source
            stage.listeners[uuid] = source
            stage.speakers.remove(uuid)

            listenerStages[uuid] = stage
            speakerStages.remove(uuid)
        }

        return true
    }

    fun removePlayer(uuid: UUID) {
        removePlayerInternal(uuid, removeSource = true)
    }

    fun removeStage(stage: Stage) {
        stages -= stage

        synchronized(stage) {
            stage.sources.keys.toList().forEach { uuid ->
                speakerStages.remove(uuid, stage)
                listenerStages.remove(uuid, stage)
            }

            stage.removeAllSources()
            stage.clear()
        }
    }

    private fun removePlayerInternal(
        uuid: UUID,
        removeSource: Boolean
    ) {
        val stage = speakerStages.remove(uuid)
            ?: listenerStages.remove(uuid)
            ?: return

        synchronized(stage) {
            speakerStages.remove(uuid, stage)
            listenerStages.remove(uuid, stage)

            stage.speakers.remove(uuid)
            stage.listeners.remove(uuid)

            val source = stage.sources.remove(uuid)

            if (removeSource) {
                source?.remove()
            }
        }
    }

    private fun createSource(uuid: UUID): ServerDirectSource? {
        val player = findPlayer(uuid) ?: return null

        return stageLine.createDirectSource(player, false)
    }

    private fun findPlayer(uuid: UUID): VoiceServerPlayer? {
        return voiceServer.playerManager
            .getPlayerById(uuid)
            .orElse(null)
    }
}

data class Stage(
    val sources: ConcurrentHashMap<UUID, ServerDirectSource> = ConcurrentHashMap(),
    val speakers: ConcurrentHashMap<UUID, ServerDirectSource> = ConcurrentHashMap(),
    val listeners: ConcurrentHashMap<UUID, ServerDirectSource> = ConcurrentHashMap()
) {

    fun sendToAll(
        frame: ByteArray,
        sequenceNumber: Long
    ) {
        sources.values.forEach { source ->
            source.sendAudioFrame(frame, sequenceNumber)
        }
    }

    fun removeAllSources() {
        sources.values.forEach { source ->
            source.remove()
        }
    }

    fun clear() {
        sources.clear()
        speakers.clear()
        listeners.clear()
    }
}