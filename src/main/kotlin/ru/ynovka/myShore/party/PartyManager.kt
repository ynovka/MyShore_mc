package ru.ynovka.myShore.party

import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import ru.ynovka.myShore.utils.Utils.asPlayers
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.Bukkit
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
            owner.sendMessage(Component.translatable("msg.myshore.party.error.no_party"))
            return false
        }
        if (party.owner != owner.uniqueId) {
            owner.sendMessage(Component.translatable("msg.myshore.party.error.disband.not_owner"))
            return false
        }

        val membersOnline = party.members.toList().asPlayers()
        party.members.forEach(parties::remove)

        membersOnline.forEach { it.sendMessage(Component.translatable("msg.myshore.party.disbanded")) }
        return true
    }

    /**
     * Пригласить игрока в пати (создаст пати, если у отправителя его нет).
     */
    fun invite(sender: Player, invitedPlayer: Player): Boolean {
        if (sender.uniqueId == invitedPlayer.uniqueId) {
            sender.sendMessage(Component.translatable("msg.myshore.party.invite.error.self"))
            return false
        }

        val party = getParty(sender) ?: create(sender)

        if (party.owner != sender.uniqueId) {
            sender.sendMessage(Component.translatable("msg.myshore.party.invite.error.not_owner"))
            return false
        }

        val invitedId = invitedPlayer.uniqueId
        if (invitedId in parties) {
            sender.sendMessage(
                Component.translatable("msg.myshore.party.invite.error.target_in_party", Component.text(invitedPlayer.name))
            )
            return false
        }
        if (invitedId in party.invited) {
            sender.sendMessage(
                Component.translatable("msg.myshore.party.invite.error.already_invited", Component.text(invitedPlayer.name))
            )
            return false
        }
        if (invitedId in party.members) {
            sender.sendMessage(
                Component.translatable("msg.myshore.party.invite.error.already_member", Component.text(invitedPlayer.name))
            )
            return false
        }

        party.invited.add(invitedId)

        sender.sendMessage(Component.translatable("msg.myshore.party.invite.sent", Component.text(invitedPlayer.name)))
        invitedPlayer.sendMessage(buildInviteAcceptMessage(sender))
        return true
    }

    /**
     * Принять приглашение в пати указанного владельца.
     */
    fun acceptInvite(invited: Player, partyOwner: Player): Boolean {
        val party = getParty(partyOwner)
        if (party == null) {
            invited.sendMessage(Component.translatable("msg.myshore.party.accept.error.no_party"))
            return false
        }

        val invitedId = invited.uniqueId
        if (invitedId in parties) {
            invited.sendMessage(Component.translatable("msg.myshore.party.error.already_in_party"))
            return false
        }

        if (invitedId !in party.invited) {
            invited.sendMessage(Component.translatable("msg.myshore.party.accept.error.not_invited"))
            return false
        }

        party.invited.remove(invitedId)
        party.members.add(invitedId)
        parties[invitedId] = party

        invited.sendMessage(Component.translatable("msg.myshore.party.accept.success", Component.text(partyOwner.name)))

        party.members.asPlayers()
            .filter { it.uniqueId != invitedId }
            .forEach {
                it.sendMessage(Component.translatable("msg.myshore.party.member.joined", Component.text(invited.name)))
            }

        return true
    }

    /**
     * Выход игрока из пати.
     */
    fun leave(player: Player, reason: LeftReason = LeftReason.COMMAND): Boolean {
        val playerId = player.uniqueId
        val party = parties[playerId]
        if (party == null) {
            if (reason == LeftReason.COMMAND) {
                player.sendMessage(Component.translatable("msg.myshore.party.error.not_in_party"))
            }
            return false
        }

        val oldOwner = party.owner
        val wasOwner = (oldOwner == playerId)

        party.members.remove(playerId)
        parties.remove(playerId)

        // Если пати пустое — оно исчезает (ссылок на него больше нет в map)
        if (party.members.isEmpty()) {
            if (reason == LeftReason.COMMAND) {
                player.sendMessage(Component.translatable("msg.myshore.party.leave.success"))
            }
            return true
        }

        // Если ушел владелец — назначаем нового (следующего)
        if (wasOwner) {
            party.owner = party.members.first()
        }

        val ownerNameBefore = Bukkit.getPlayer(oldOwner)?.name
        if (reason == LeftReason.COMMAND) {
            if (ownerNameBefore != null) {
                player.sendMessage(Component.translatable("msg.myshore.party.leave.success_owner", Component.text(ownerNameBefore)))
            } else {
                player.sendMessage(Component.translatable("msg.myshore.party.leave.success"))
            }
        }

        val leaveKey = when (reason) {
            LeftReason.QUIT -> "msg.myshore.party.member.left.quit"
            LeftReason.COMMAND -> "msg.myshore.party.member.left"
        }

        party.members.asPlayers()
            .forEach { it.sendMessage(Component.translatable(leaveKey, Component.text(player.name))) }

        // Сообщение о смене лидера (если было)
        if (wasOwner) {
            val newOwnerPlayer = Bukkit.getPlayer(party.owner)
            newOwnerPlayer?.sendMessage(Component.translatable("msg.myshore.party.owner.you"))

            party.members.asPlayers()
                .filter { it.uniqueId != party.owner }
                .forEach {
                    it.sendMessage(
                        Component.translatable(
                            "msg.myshore.party.owner.changed",
                            Component.text(newOwnerPlayer?.name ?: "???")
                        )
                    )
                }
        }

        return true
    }

    /**
     * Выгнать игрока из пати.
     */
    fun kick(actor: Player, target: Player): Boolean {
        val party = getParty(actor)
        if (party == null) {
            actor.sendMessage(Component.translatable("msg.myshore.party.error.no_party"))
            return false
        }

        if (party.owner != actor.uniqueId) {
            actor.sendMessage(Component.translatable("msg.myshore.party.kick.error.not_owner"))
            return false
        }

        if (target.uniqueId == actor.uniqueId) {
            actor.sendMessage(Component.translatable("msg.myshore.party.kick.error.self"))
            return false
        }

        if (target.getParty() !== party) {
            actor.sendMessage(Component.translatable("msg.myshore.party.error.target_not_member"))
            return false
        }

        if (target.uniqueId == party.owner) {
            actor.sendMessage(Component.translatable("msg.myshore.party.kick.error.owner"))
            return false
        }

        val membersBefore = party.members.toList()

        // выгоняем
        leave(target, LeftReason.COMMAND)

        actor.sendMessage(Component.translatable("msg.myshore.party.kick.success", Component.text(target.name)))
        target.sendMessage(Component.translatable("msg.myshore.party.kick.target", Component.text(actor.name)))

        membersBefore.asPlayers()
            .filter { it.uniqueId != target.uniqueId }
            .forEach {
                it.sendMessage(Component.translatable("msg.myshore.party.kick.broadcast", Component.text(target.name)))
            }

        return true
    }

    /**
     * Передать лидерство участнику (/p setOwner).
     */
    fun setOwner(currentOwner: Player, newOwner: Player): Boolean {
        val party = getParty(currentOwner)
        if (party == null) {
            currentOwner.sendMessage(Component.translatable("msg.myshore.party.error.no_party"))
            return false
        }
        if (party.owner != currentOwner.uniqueId) {
            currentOwner.sendMessage(Component.translatable("msg.myshore.party.set_owner.error.not_owner"))
            return false
        }
        if (newOwner.getParty() !== party) {
            currentOwner.sendMessage(Component.translatable("msg.myshore.party.error.target_not_member"))
            return false
        }
        if (newOwner.uniqueId == party.owner) {
            currentOwner.sendMessage(Component.translatable("msg.myshore.party.set_owner.error.already_owner"))
            return false
        }

        party.owner = newOwner.uniqueId

        currentOwner.sendMessage(Component.translatable("msg.myshore.party.set_owner.success", Component.text(newOwner.name)))
        newOwner.sendMessage(Component.translatable("msg.myshore.party.owner.you"))

        party.members.asPlayers()
            .filter { it.uniqueId != currentOwner.uniqueId && it.uniqueId != newOwner.uniqueId }
            .forEach {
                it.sendMessage(Component.translatable("msg.myshore.party.owner.changed", Component.text(newOwner.name)))
            }

        return true
    }

    /**
     * Зарегистрировать участника в mapping (используется EventManager для прямого добавления).
     */
    fun registerMember(playerId: UUID, party: Party) {
        parties[playerId] = party
    }

    /**
     * Удалить участника из mapping без изменения members set (используется EventManager).
     */
    fun unregisterMember(playerId: UUID) {
        parties.remove(playerId)
    }

    /**
     * Показать список участников пати.
     */
    fun showMembers(player: Player): Boolean {
        val party = getParty(player)
        if (party == null) {
            player.sendMessage(Component.translatable("msg.myshore.party.error.not_in_party"))
            return false
        }

        val ownerName = Bukkit.getPlayer(party.owner)?.name ?: "???"
        val names = party.members.asPlayers()
            .foldIndexed(Component.text()) { index, builder, p ->
                if (index > 0) {
                    builder.append(Component.text(", "))
                }
                builder.append(Component.text(p.name))
                if (p.uniqueId == party.owner) {
                    builder
                        .append(Component.space())
                        .append(Component.text("("))
                        .append(Component.translatable("msg.myshore.party.members.owner_mark"))
                        .append(Component.text(")"))
                }
                builder
            }
            .build()

        player.sendMessage(
            Component.translatable(
                "msg.myshore.party.members.info",
                Component.text(ownerName),
                Component.text(party.members.size),
                names
            )
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
        return Component.text()
            .append(
                Component.translatable("msg.myshore.party.invite.accept.message", Component.text(inviter.name))
                    .color(NamedTextColor.YELLOW)
            )
            .append(Component.space())
            .append(
                Component.translatable("msg.myshore.party.invite.accept.button")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD)
                    .clickEvent(ClickEvent.runCommand("/p accept ${inviter.name}"))
                    .hoverEvent(
                        HoverEvent.showText(
                            Component.translatable("msg.myshore.party.invite.accept.hover")
                                .color(NamedTextColor.BLUE)
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
