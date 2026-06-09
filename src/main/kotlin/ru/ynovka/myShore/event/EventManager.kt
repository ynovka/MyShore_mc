package ru.ynovka.myShore.event

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import java.util.concurrent.atomic.AtomicReference
import ru.ynovka.myShore.game.pillars.PillarsGame
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import ru.ynovka.myShore.utils.Utils.asPlayers
import ru.ynovka.myShore.party.PartyManager
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.game.GameManager
import ru.ynovka.myShore.hub.Hub.toHub
import ru.ynovka.myShore.text.translate
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import java.util.UUID


enum class EventState { GATHERING, STARTED, FINISHED }

class GameEvent(
    var gameName: String?,
    val createdBy: UUID,
    val party: PartyManager.Party,
    @Volatile var state: EventState = EventState.GATHERING
) {
    val displayName: Component
        get() = gameName
            ?.replaceFirstChar { it.uppercase() }
            ?.let(Component::text)
            ?: Component.translatable("msg.myshore.event.name.default")

    val isActive: Boolean
        get() = state != EventState.FINISHED
}

private data class EventGameSpec(
    val canonicalName: String,
    val displayName: String,
    val startParty: (PartyManager.Party) -> Result<*>,
    val joinPlayer: (Player) -> Result<*>
)

object EventManager {
    private val currentEvent = AtomicReference<GameEvent?>(null)

    private val supportedGames = listOf(
        EventGameSpec(
            canonicalName = "pillars",
            displayName = "Pillars",
            startParty = { party ->
                GameManager.joinParty<PillarsGame>(
                    party = party,
                    partyFactory = { PillarsGame(it) }
                )
            },
            joinPlayer = { player ->
                GameManager.join<PillarsGame>(
                    player = player,
                    factory = { PillarsGame() },
                    partyFactory = { party -> PillarsGame(party) }
                )
            }
        )
    )

    val activeEvent: GameEvent?
        get() = currentEvent.get()?.takeIf { it.isActive }

    fun suggestGames(): Array<String> =
        supportedGames
            .flatMap { listOf(it.displayName) }
            .distinct()
            .toTypedArray()

    fun create(admin: Player): Boolean {
        if (!admin.isOp) {
            admin.sendMessage(Component.translatable("msg.myshore.event.error.admin.create").color(NamedTextColor.RED).translate(admin))
            return false
        }

        if (PartyManager.getParty(admin) != null) {
            admin.sendMessage(Component.translatable("msg.myshore.event.error.party_exists").color(NamedTextColor.RED).translate(admin))
            return false
        }

        val existing = activeEvent
        if (existing != null) {
            admin.sendMessage(
                Component.translatable(
                    "msg.myshore.event.error.already_active",
                    existing.displayName
                ).color(NamedTextColor.RED).translate(admin)
            )
            return false
        }

        val party = PartyManager.create(admin)
        val event = GameEvent(
            gameName = null,
            createdBy = admin.uniqueId,
            party = party
        )

        if (!currentEvent.compareAndSet(null, event)) {
            party.members.toList().forEach { PartyManager.unregisterMember(it) }
            party.members.clear()
            admin.sendMessage(Component.translatable("msg.myshore.event.error.concurrent_create").color(NamedTextColor.RED).translate(admin))
            return false
        }

        Bukkit.getOnlinePlayers().forEach { it.sendEventAnnouncement(event) }
        admin.sendMessage(Component.translatable("msg.myshore.event.created").color(NamedTextColor.GREEN).translate(admin))
        return true
    }

    fun start(admin: Player, gameName: String): Boolean {
        if (!admin.isOp) {
            admin.sendMessage(Component.translatable("msg.myshore.event.error.admin.start").color(NamedTextColor.RED).translate(admin))
            return false
        }

        val event = activeEvent ?: run {
            admin.sendMessage(Component.translatable("msg.myshore.event.error.no_active").color(NamedTextColor.RED).translate(admin))
            return false
        }

        if (event.state == EventState.STARTED) {
            admin.sendMessage(
                Component.translatable(
                    "msg.myshore.event.error.already_started",
                    event.displayName
                ).color(NamedTextColor.RED).translate(admin)
            )
            return false
        }

        val spec = resolveGame(gameName)
        if (spec == null) {
            admin.sendMessage(
                Component.translatable(
                    "msg.myshore.event.error.unknown_game",
                    Component.text(gameName),
                    Component.text(supportedGames.joinToString(", ") { it.displayName })
                ).color(NamedTextColor.RED).translate(admin)
            )
            return false
        }

        if (admin.uniqueId !in event.party.members) {
            event.party.members.add(admin.uniqueId)
            PartyManager.registerMember(admin.uniqueId, event.party)
        }

        event.gameName = spec.canonicalName
        event.state = EventState.STARTED

        val startResult = spec.startParty(event.party)
        if (startResult.isFailure) {
            event.state = EventState.GATHERING
            admin.sendGameJoinFailure(startResult)
            return false
        }

        event.party.members.asPlayers()
            .forEach {
                it.sendMessage(
                    Component.translatable(
                        "msg.myshore.event.started",
                        event.displayName
                    ).color(NamedTextColor.GREEN).translate(it)
                )
            }

        return true
    }

    fun join(player: Player): Boolean {
        val event = activeEvent ?: run {
            player.sendMessage(Component.translatable("msg.myshore.event.error.no_active").color(NamedTextColor.RED).translate(player))
            return false
        }

        val currentParty = PartyManager.getParty(player)
        if (currentParty != null && currentParty !== event.party) {
            player.sendMessage(Component.translatable("msg.myshore.event.error.other_party").color(NamedTextColor.RED).translate(player))
            return false
        }

        if (player.uniqueId in event.party.members) {
            player.sendMessage(
                Component.translatable(
                    "msg.myshore.event.join.already",
                    event.displayName
                ).color(NamedTextColor.YELLOW).translate(player)
            )
            if (event.state == EventState.STARTED && !GameManager.run { player.uniqueId.inGame() }) {
                startGameFor(event, player)
            }
            return true
        }

        event.party.members.add(player.uniqueId)
        PartyManager.registerMember(player.uniqueId, event.party)

        maybeTransferOwnershipToOp(event, player)

        player.sendMessage(
            Component.translatable(
                "msg.myshore.event.join.success",
                event.displayName
            ).color(NamedTextColor.GREEN).translate(player)
        )

        event.party.members.asPlayers()
            .filter { it.uniqueId != player.uniqueId }
            .forEach {
                it.sendMessage(
                    Component.translatable(
                        "msg.myshore.event.join.broadcast",
                        Component.text(player.name)
                    ).color(NamedTextColor.YELLOW).translate(it)
                )
            }

        if (event.state == EventState.STARTED) {
            startGameFor(event, player)
        }

        return true
    }

    fun leave(player: Player): Boolean {
        val event = currentEvent.get()?.takeIf { it.isActive } ?: run {
            player.sendMessage(Component.translatable("msg.myshore.event.error.no_active").color(NamedTextColor.RED).translate(player))
            return false
        }

        if (player.uniqueId !in event.party.members) {
            player.sendMessage(Component.translatable("msg.myshore.event.leave.not_member").color(NamedTextColor.RED).translate(player))
            return false
        }

        val wasOwner = event.party.owner == player.uniqueId
        val remainingMembers = event.party.members.filterNot { it == player.uniqueId }

        if (event.state == EventState.STARTED && GameManager.run { player.uniqueId.inGame() }) {
            GameManager.leave(player.uniqueId)
        }

        removeMember(event, player.uniqueId)

        player.sendMessage(
            Component.translatable(
                "msg.myshore.event.leave.success",
                event.displayName
            ).color(NamedTextColor.GREEN).translate(player)
        )

        event.party.members.asPlayers()
            .forEach {
                it.sendMessage(
                    Component.translatable(
                        "msg.myshore.event.leave.broadcast",
                        Component.text(player.name)
                    ).color(NamedTextColor.YELLOW).translate(it)
                )
            }

        if (remainingMembers.isEmpty()) {
            finishEvent(event, Component.translatable("msg.myshore.event.finish.reason.empty"))
            return true
        }

        if (wasOwner) {
            maybeTransferOwnership(event, remainingMembers)
        }

        return true
    }

    fun finish(admin: Player): Boolean {
        if (!admin.isOp) {
            admin.sendMessage(Component.translatable("msg.myshore.event.error.admin.finish").color(NamedTextColor.RED).translate(admin))
            return false
        }

        val event = activeEvent ?: run {
            admin.sendMessage(Component.translatable("msg.myshore.event.error.no_active_finish").color(NamedTextColor.RED).translate(admin))
            return false
        }

        finishEvent(
            event,
            Component.translatable("msg.myshore.event.finish.reason.admin", Component.text(admin.name))
        )
        return true
    }

    fun onGameRoundFinished(party: PartyManager.Party?) {
        val event = activeEvent?.takeIf { it.party === party } ?: return
        if (event.state == EventState.FINISHED) return

        event.state = EventState.GATHERING

        event.party.members.asPlayers()
            .forEach {
                it.sendMessage(Component.translatable("msg.myshore.event.round_finished").color(NamedTextColor.GOLD).translate(it))
            }
    }

    fun onPlayerQuit(player: Player) {
        val event = activeEvent ?: return
        if (player.uniqueId !in event.party.members) return

        if (event.party.owner == player.uniqueId) {
            val remaining = event.party.members.filter { it != player.uniqueId }
            if (remaining.isEmpty()) {
                finishEvent(event, Component.translatable("msg.myshore.event.finish.reason.creator_left_empty"))
                return
            }

            maybeTransferOwnership(event, remaining)
        }

        PartyManager.unregisterMember(player.uniqueId)
    }

    fun onPlayerJoin(player: Player) {
        val event = activeEvent ?: return

        player.sendEventAnnouncement(event)

        if (player.uniqueId in event.party.members) {
            PartyManager.registerMember(player.uniqueId, event.party)

            maybeTransferOwnershipToOp(event, player)

            if (event.state == EventState.STARTED && !GameManager.run { player.uniqueId.inGame() }) {
                startGameFor(event, player)
            }
        }
    }

    private fun startGameFor(event: GameEvent, player: Player): Boolean {
        val gameName = event.gameName
        if (gameName == null) {
            player.sendMessage(Component.translatable("msg.myshore.event.error.game_not_selected").color(NamedTextColor.RED).translate(player))
            return false
        }

        val spec = resolveGame(gameName)
        if (spec == null) {
            player.sendMessage(
                Component.translatable(
                    "msg.myshore.event.error.game_unsupported",
                    event.displayName
                ).color(NamedTextColor.RED).translate(player)
            )
            return false
        }

        val result = spec.joinPlayer(player)
        if (result.isFailure) {
            player.sendGameJoinFailure(result)
        }
        return result.isSuccess
    }

    private fun resolveGame(gameName: String): EventGameSpec? {
        val normalized = normalizeGameName(gameName)
        return supportedGames.firstOrNull { spec ->
            normalizeGameName(spec.canonicalName) == normalized ||
                normalizeGameName(spec.displayName) == normalized
        }
    }

    private fun normalizeGameName(name: String): String =
        name.trim().lowercase().replace(" ", "")

    private fun removeMember(event: GameEvent, id: UUID) {
        event.party.members.remove(id)
        PartyManager.unregisterMember(id)
    }

    private fun maybeTransferOwnership(event: GameEvent, candidates: List<UUID>) {
        val newOwner = candidates.asPlayers()
            .firstOrNull { it.isOp }
            ?.uniqueId ?: return

        transferOwnership(event, newOwner)
    }

    private fun maybeTransferOwnershipToOp(event: GameEvent, player: Player) {
        val ownerOnline = Bukkit.getPlayer(event.party.owner)
        if (event.party.owner == player.uniqueId) return
        if (ownerOnline != null && ownerOnline.isOp) return
        if (!player.isOp) return

        transferOwnership(event, player.uniqueId)
    }

    private fun transferOwnership(event: GameEvent, newOwner: UUID) {
        event.party.owner = newOwner
        Bukkit.getPlayer(newOwner)?.let { player ->
            player.sendMessage(
                Component.translatable(
                    "msg.myshore.event.owner.transferred",
                    event.displayName
                ).color(NamedTextColor.GOLD).translate(player)
            )
        }
    }

    private fun finishEvent(event: GameEvent, reason: Component) {
        event.state = EventState.FINISHED

        val onlineMembers = event.party.members.asPlayers()
        onlineMembers.forEach { it.toHub() }

        onlineMembers.forEach {
            it.sendMessage(
                Component.translatable(
                    "msg.myshore.event.finished",
                    event.displayName,
                    reason
                ).color(NamedTextColor.GOLD).translate(it)
            )
        }

        event.party.members.toList().forEach { PartyManager.unregisterMember(it) }
        event.party.members.clear()

        currentEvent.set(null)
    }

    private fun buildEventAnnouncement(event: GameEvent, player: Player): Component =
        Component.text()
            .append(
                Component.translatable("msg.myshore.event.announcement", event.displayName)
                    .color(NamedTextColor.YELLOW)
            )
            .append(Component.newline())
            .append(
                Component.translatable("msg.myshore.event.announcement.join")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD)
                    .clickEvent(ClickEvent.runCommand("/event join"))
                    .hoverEvent(
                        HoverEvent.showText(
                            Component.translatable("msg.myshore.event.announcement.hover")
                                .color(NamedTextColor.GREEN)
                                .translate(player)
                        )
                    )
            )
            .build()

    private fun Player.sendEventAnnouncement(event: GameEvent) {
        sendMessage(buildEventAnnouncement(event, this).translate(this))
    }

    private fun Player.sendGameJoinFailure(result: Result<*>) {
        sendMessage(
            Component.translatable(
                "msg.myshore.event.game.join_failed",
                result.exceptionOrNull()?.message
                    ?.let(Component::text)
                    ?: Component.translatable("msg.myshore.event.error.unknown_reason")
            ).color(NamedTextColor.RED).translate(this)
        )
    }
}
