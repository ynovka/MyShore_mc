package ru.ynovka.myShore.lobby

import ru.ynovka.myShore.party.PartyManager
import ru.ynovka.myShore.party.getParty
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import ru.ynovka.myShore.games.Game
import ru.ynovka.myShore.games.GameId
import ru.ynovka.myShore.games.pillars.PillarsGame
import ru.ynovka.myShore.games.tag.TagGame
import ru.ynovka.myShore.games.worldDomination.WDGame
import ru.ynovka.myShore.utils.PlayerVisibilityController
import ru.ynovka.myShore.utils.Utils.asPlayers


object LobbyManager {
    val lobbies: MutableList<Lobby> = mutableListOf()

    fun join(player: Player, gameType: GameId, isPublic: Boolean = true): Boolean {
        if (player.hasLobby()) {
            // todo перевод
            player.sendMessage("Вы уже находитесь в лобби.")
            return false
        }

        val party = player.getParty()
        return if (party == null) {
            joinSolo(player, gameType)
        } else {
            // Только лидер может запускать поиск/создание лобби для пати
            if (party.owner != player.uniqueId) {
                // todo перевод
                player.sendMessage("Только лидер пати может выбирать игру!")
                return false
            }
            join(party, gameType, isPublic)
        }
    }

    private fun ensureGame(lobby: Lobby): Game {
        val existing = lobby.game
        if (existing != null) return existing

        val created: Game = when (lobby.gameType) {
            GameId.TAG -> TagGame(lobby)
            GameId.PILLARS -> PillarsGame(lobby)
            GameId.WORLD_DOMINATION -> WDGame(lobby)
        }

        lobby.game = created

        return created
    }

    private fun join(party: PartyManager.Party, gameType: GameId, isPublic: Boolean): Boolean {
        val owner = Bukkit.getPlayer(party.owner) ?: return false

        if (owner.hasLobby()) {
            // todo перевод
            owner.sendMessage("Вы уже находитесь в лобби.")
            return false
        }

        if (party.members.size > gameType.maxPlayers) {
            // todo перевод
            owner.sendMessage("Ваше пати слишком большое для этого режима!")
            return false
        }

        // Оптимизированная проверка: занят ли кто-то из пати (лучше проверить это до создания лобби)
        val busyMember = party.members.firstOrNull { uuid -> lobbies.any { it.hasMember(uuid) } }
        if (busyMember != null) {
            val name = Bukkit.getPlayer(busyMember)?.name ?: "???"
            // todo перевод
            owner.sendMessage("Участник пати $name уже в игре!")
            return false
        }

        val membersOnline = party.members.asPlayers()

        // --- PRIVATE: только пати ---
        if (!isPublic) {
            val lobby = PartyLobby(gameType, party).apply {
                members.addAll(party.members)
            }
            lobbies.add(lobby)

            val game = ensureGame(lobby)
            membersOnline.forEach { game.join(it) }

            // todo перевод
            membersOnline.forEach {
                it.sendMessage("Вы вошли в приватное лобби (Party)!")
            }
            return true
        }


        // --- PUBLIC: пати вместе, но с другими игроками ---
        val partySize = party.members.size

        // Ищем public lobby с достаточным числом свободных слотов ИЛИ создаем новое
        val lobby = lobbies.firstOrNull { l ->
            l is PublicLobby &&
                    l.gameType == gameType &&
                    (l.membersLimit - l.members.size) >= partySize
        } ?: PublicLobby(gameType).also { lobbies.add(it) }

        lobby.members.addAll(party.members)

        PlayerVisibilityController.refreshAll()

        val game = ensureGame(lobby)
        membersOnline.forEach { game.join(it) }

        // todo перевод
        membersOnline.forEach {
            it.sendMessage("Вы вошли в публичное лобби (Party)!")
        }
        return true
    }

    private fun joinSolo(player: Player, gameType: GameId): Boolean {
        // Ищем подходящее лобби ИЛИ создаем новое и сразу добавляем в список
        val lobby = lobbies.firstOrNull {
            it is PublicLobby && it.gameType == gameType && !it.isFull()
        } ?: PublicLobby(gameType).also { lobbies.add(it) }

        lobby.addMember(player.uniqueId)

        PlayerVisibilityController.refreshVisibility(player)

        // уведомили игру
        val game = ensureGame(lobby)
        game.join(player)

        // todo перевод
        player.sendMessage("Вы вошли в лобби!")
        return true
    }


    fun leave(player: Player): Boolean {
        val lobby = player.getLobby() ?: run { return false }

        val playerId = player.uniqueId

        // Удаляем игрока
        lobby.removeMember(playerId)

        PlayerVisibilityController.refreshAll()

        // todo перевод
        player.sendMessage("Вы покинули лобби")

        val game = ensureGame(lobby)
        game.leave(player)

        // Логика роспуска пати-лобби
        if (lobby is PartyLobby && lobby.party.owner == playerId) {
            // todo перевод
            disbandLobby(lobby, "Лобби распущено, так как лидер вышел")
            return true
        }

        // Удаляем лобби, если оно пустое
        if (lobby.isEmpty()) {
            lobby.game = null
            lobbies.remove(lobby)
            LobbyIdAllocator.release(lobby.id)
        }

        return true
    }

    /**
     * Эффективный роспуск лобби без итераторов.
     */
    private fun disbandLobby(lobby: Lobby, reason: String) {
        // 1. Оповещаем всех оставшихся
        val players = lobby.members.asPlayers()
        players.forEach {
            it.sendMessage(reason)
        }

        // 2. Очищаем список участников (быстрое удаление ссылок)
        lobby.members.clear()

        // 3. Удаляем само лобби из менеджера
        lobby.game = null
        lobbies.remove(lobby)
        LobbyIdAllocator.release(lobby.id)
    }
}