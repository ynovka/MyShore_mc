package ru.ynovka.myShore.party

import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import ru.ynovka.myShore.utils.Utils.asPlayers
import java.util.UUID


enum class LeftReason { COMMAND, QUIT }

object PartyManager {
    // Храним ссылку на объект Party для каждого UUID участника для быстрого доступа
    private val parties: MutableMap<UUID, Party> = mutableMapOf()

    /**
     * Получить пати игрока.
     */
    fun getParty(player: Player): Party? = parties[player.uniqueId]

    /**
     * Создаёт новое пати (если игрок уже в пати — вернёт текущую).
     */
    fun create(owner: Player): Party {
        val ownerId = owner.uniqueId
        parties[ownerId]?.let { return it }

        val party = Party(ownerId)
        parties[ownerId] = party
        return party
    }

    /**
     * Распускает пати.
     */
    fun disband(owner: Player): Boolean {
        val party = getParty(owner)
        if (party == null) {
            // todo перевод
            owner.sendMessage("У вас нет пати.")
            return false
        }
        if (party.owner != owner.uniqueId) {
            // todo перевод
            owner.sendMessage("Только лидер может удалить пати.")
            return false
        }

        val membersOnline = party.members.toList().asPlayers()
        party.members.forEach(parties::remove)

        // todo перевод
        membersOnline.forEach { it.sendMessage("Пати было распущено.") }
        return true
    }

    /**
     * Пригласить игрока в пати (создаст пати, если у отправителя его нет).
     */
    fun invite(sender: Player, invitedPlayer: Player): Boolean {
        if (sender.uniqueId == invitedPlayer.uniqueId) {
            // todo перевод
            sender.sendMessage("Вы не можете пригласить самого себя.")
            return false
        }

        val party = getParty(sender) ?: create(sender)

        if (party.owner != sender.uniqueId) {
            // todo перевод
            sender.sendMessage("Только лидер пати может приглашать игроков.")
            return false
        }

        val invitedId = invitedPlayer.uniqueId
        if (invitedId in parties) {
            // todo перевод
            sender.sendMessage("Игрок ${invitedPlayer.name} уже состоит в пати.")
            return false
        }
        if (invitedId in party.invited) {
            // todo перевод
            sender.sendMessage("Игрок ${invitedPlayer.name} уже приглашён.")
            return false
        }
        if (invitedId in party.members) {
            // todo перевод
            sender.sendMessage("Игрок ${invitedPlayer.name} уже в вашей пати.")
            return false
        }

        party.invited.add(invitedId)

        // todo перевод
        sender.sendMessage("Вы пригласили игрока ${invitedPlayer.name} в пати.")
        invitedPlayer.sendMessage(buildInviteAcceptMessage(sender))
        return true
    }

    /**
     * Принять приглашение в пати указанного владельца.
     */
    fun acceptInvite(invited: Player, partyOwner: Player): Boolean {
        val party = getParty(partyOwner)
        if (party == null) {
            // todo перевод
            invited.sendMessage("Пати не существует.")
            return false
        }

        val invitedId = invited.uniqueId
        if (invitedId in parties) {
            // todo перевод
            invited.sendMessage("Вы уже состоите в пати.")
            return false
        }

        if (invitedId !in party.invited) {
            // todo перевод
            invited.sendMessage("Вас не приглашали в это пати.")
            return false
        }

        party.invited.remove(invitedId)
        party.members.add(invitedId)
        parties[invitedId] = party

        // todo перевод
        invited.sendMessage("Вы присоединились к пати игрока ${partyOwner.name}.")

        party.members.asPlayers()
            .filter { it.uniqueId != invitedId }
            // todo перевод
            .forEach { it.sendMessage("Игрок ${invited.name} присоединился к пати.") }

        return true
    }

    /**
     * Выход игрока из пати.
     */
    fun leave(player: Player, reason: LeftReason = LeftReason.COMMAND): Boolean {
        val playerId = player.uniqueId
        val party = parties[playerId]
        if (party == null) {
            // todo перевод
            if (reason == LeftReason.COMMAND) player.sendMessage("Вы не состоите в пати.")
            return false
        }

        val oldOwner = party.owner
        val wasOwner = (oldOwner == playerId)

        party.members.remove(playerId)
        parties.remove(playerId)

        // Если пати пустое — оно исчезает (ссылок на него больше нет в map)
        if (party.members.isEmpty()) {
            // todo перевод
            if (reason == LeftReason.COMMAND) player.sendMessage("Вы покинули пати.")
            return true
        }

        // Если ушел владелец — назначаем нового (следующего)
        if (wasOwner) {
            party.owner = party.members.first()
        }

        val ownerNameBefore = Bukkit.getPlayer(oldOwner)?.name
        if (reason == LeftReason.COMMAND) {
            // todo перевод
            player.sendMessage("Вы покинули пати" + (ownerNameBefore?.let { " игрока $it" } ?: ""))
        }

        // todo перевод
        val leaveText = when (reason) {
            LeftReason.QUIT -> "Игрок ${player.name} вышел из игры и покинул пати."
            LeftReason.COMMAND -> "Игрок ${player.name} покинул пати."
        }

        party.members.asPlayers()
            .forEach { it.sendMessage(leaveText) }

        // Сообщение о смене лидера (если было)
        if (wasOwner) {
            val newOwnerPlayer = Bukkit.getPlayer(party.owner)
            // todo перевод
            newOwnerPlayer?.sendMessage("Вы стали лидером пати.")

            party.members.asPlayers()
                .filter { it.uniqueId != party.owner }
                // todo перевод
                .forEach { it.sendMessage("Новый лидер пати: ${newOwnerPlayer?.name ?: "???"}") }
        }

        return true
    }

    /**
     * Выгнать игрока из пати.
     */
    fun kick(actor: Player, target: Player): Boolean {
        val party = getParty(actor)
        if (party == null) {
            // todo перевод
            actor.sendMessage("У вас нет пати.")
            return false
        }

        if (party.owner != actor.uniqueId) {
            // todo перевод
            actor.sendMessage("Только лидер пати может кикать участников.")
            return false
        }

        if (target.uniqueId == actor.uniqueId) {
            // todo перевод
            actor.sendMessage("Вы не можете кикнуть самого себя.")
            return false
        }

        if (target.getParty() !== party) {
            // todo перевод
            actor.sendMessage("Этот игрок не в вашей пати.")
            return false
        }

        if (target.uniqueId == party.owner) {
            // todo перевод
            actor.sendMessage("Нельзя кикнуть лидера пати.")
            return false
        }

        val membersBefore = party.members.toList()

        // выгоняем
        leave(target, LeftReason.COMMAND)

        // todo перевод
        actor.sendMessage("Игрок ${target.name} был изгнан из пати.")
        target.sendMessage("Вас выгнали из пати игрока ${actor.name}.")

        membersBefore.asPlayers()
            .filter { it.uniqueId != target.uniqueId }
            // todo перевод
            .forEach { it.sendMessage("Игрок ${target.name} был изгнан из пати.") }

        return true
    }

    /**
     * Передать лидерство участнику (/p setOwner).
     */
    fun setOwner(currentOwner: Player, newOwner: Player): Boolean {
        val party = getParty(currentOwner)
        if (party == null) {
            // todo перевод
            currentOwner.sendMessage("У вас нет пати.")
            return false
        }
        if (party.owner != currentOwner.uniqueId) {
            // todo перевод
            currentOwner.sendMessage("Только лидер может передавать лидерство.")
            return false
        }
        if (newOwner.getParty() !== party) {
            // todo перевод
            currentOwner.sendMessage("Этот игрок не в вашей пати.")
            return false
        }
        if (newOwner.uniqueId == party.owner) {
            // todo перевод
            currentOwner.sendMessage("Этот игрок уже является лидером.")
            return false
        }

        party.owner = newOwner.uniqueId

        // todo перевод
        currentOwner.sendMessage("Вы передали лидерство игроку ${newOwner.name}.")
        newOwner.sendMessage("Вы стали лидером пати.")

        party.members.asPlayers()
            .filter { it.uniqueId != currentOwner.uniqueId && it.uniqueId != newOwner.uniqueId }
            // todo перевод
            .forEach { it.sendMessage("Новый лидер пати: ${newOwner.name}.") }

        return true
    }

    /**
     * Показать список участников пати.
     */
    fun showMembers(player: Player): Boolean {
        val party = getParty(player)
        if (party == null) {
            // todo перевод
            player.sendMessage("Вы не состоите в пати.")
            return false
        }

        val ownerName = Bukkit.getPlayer(party.owner)?.name ?: "???"
        val names = party.members.asPlayers().joinToString(", ") { p ->
            val mark = if (p.uniqueId == party.owner) " (лидер)" else ""
            p.name + mark
        }

        // todo перевод
        player.sendMessage(
            "Лидер пати: $ownerName",
            "Участников: ${party.members.size}",
            names
        )
        return true
    }

    /**
     * Подсказки для /p kick.
     */
    fun suggestKickTargets(sender: Player): Array<String> {
        val party = getParty(sender) ?: return emptyArray()
        return party.members.asPlayers()
            .filter { it.uniqueId != sender.uniqueId }
            .filter { it.uniqueId != party.owner }
            .map { it.name }
            .distinct()
            .toTypedArray()
    }

    /**
     * Подсказки для /p setOwner.
     */
    fun suggestOwnerTargets(sender: Player): Array<String> {
        val party = getParty(sender) ?: return emptyArray()
        return party.members.asPlayers()
            .filter { it.uniqueId != party.owner } // нельзя выбрать текущего лидера
            .map { it.name }
            .distinct()
            .toTypedArray()
    }

    /**
     * Сообщение с кликабельным принятием приглашения.
     */
    private fun buildInviteAcceptMessage(inviter: Player): Component {
        // todo перевод
        return Component.text()
            .append(Component.text("▍ ").color(NamedTextColor.BLUE))
            .append(Component.text("Игрок ${inviter.name} пригласил вас в пати ").color(NamedTextColor.YELLOW))
            .append(
                Component.text("ПРИНЯТЬ")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD)
                    .clickEvent(ClickEvent.runCommand("/p accept ${inviter.name}"))
                    .hoverEvent(
                        HoverEvent.showText(
                            Component.text("Нажмите, чтобы принять приглашение").color(NamedTextColor.BLUE)
                        )
                    )
            )
            .build()
    }

    /**
     * Класс Party.
     * Содержит владельца, список участников и приглашённых.
     */
    data class Party(
        var owner: UUID,
        val members: MutableSet<UUID> = mutableSetOf(owner),
        val invited: MutableSet<UUID> = mutableSetOf()
    )
}

fun Player.getParty(): PartyManager.Party? = PartyManager.getParty(this)
