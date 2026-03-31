package ru.ynovka.myShore.games.tag.menus

import ru.ynovka.myShore.games.tag.TagItems.tagVoteMountainTrackMapMenuItem
import com.github.darksoulq.abyssallib.server.translation.ServerTranslator
import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import ru.ynovka.myShore.games.tag.TagItems.tagVoteJungleMapMenuItem
import ru.ynovka.myShore.games.tag.TagItems.tagVoteRandomMapMenuItem
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import ru.ynovka.myShore.games.tag.states.TagVoting.setupMap
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import net.kyori.adventure.text.format.NamedTextColor
import com.github.darksoulq.abyssallib.world.gui.Gui
import com.github.darksoulq.abyssallib.world.gui.gui
import ru.ynovka.myShore.games.tag.maps.TagMaps
import ru.ynovka.myShore.games.tag.maps.TagMap
import ru.ynovka.myShore.utils.Utils.toComponent
import ru.ynovka.myShore.texturepack.GuiTextures
import ru.ynovka.myShore.games.tag.states.TagWaitingForPlayers
import ru.ynovka.myShore.games.tag.currentTagGame
import net.kyori.adventure.text.Component
import org.bukkit.inventory.MenuType
import org.bukkit.entity.Player
import org.bukkit.Sound


@Suppress("UnstableApiUsage")
object TagVoteMapMenu {
    fun get(): Gui = gui(
        MenuType.GENERIC_9X1,
        Component.text()
            .append(TextOffset.getOffsetMinimessage(-8).toComponent().color(NamedTextColor.WHITE))
            .append(GuiTextures.MENU_1x9_028!!.toComponent().color(NamedTextColor.WHITE))
            .append(TextOffset.getOffsetMinimessage(-170).toComponent().color(NamedTextColor.WHITE))
            .append(Component.translatable("menu.myshore.minigames"))
            .build(),
    ) {
        set(
            SlotPosition.top(0),
            GuiButton.of(tagVoteJungleMapMenuItem.getStack(null)) { ctx ->
                val player = ctx.view.inventoryView.player as Player
                voteMap(player, TagMaps.JUNGLE.mapProvider())
            }
        )
        set(
            SlotPosition.top(2),
            GuiButton.of(tagVoteMountainTrackMapMenuItem.getStack(null)) { ctx ->
                val player = ctx.view.inventoryView.player as Player
                voteMap(player, TagMaps.MOUNTAIN_TRACK.mapProvider())
            }
        )
        set(
            SlotPosition.top(8),
            GuiButton.of(tagVoteRandomMapMenuItem.getStack(null)) { ctx ->
                val player = ctx.view.inventoryView.player as Player
                voteMap(player, TagMaps.RANDOM.mapProvider(), true)
            }
        )
    }

    private fun voteMap(player: Player, map: TagMap, isRandom: Boolean = false) {
        val mapNameTranslatable = if (isRandom) Component.translatable("name.myshore.tag.map.random") else map.mapName
        val mapNameComp = ServerTranslator.translate(mapNameTranslatable, player)

        val game = player.currentTagGame() ?: return

        if (game.fsm.current == TagWaitingForPlayers) {
            setupMap(game, map, true)
            return
        }

        game.mapVotes[player.uniqueId] = map

        player.playSound(player.location, Sound.BLOCK_COPPER_BULB_TURN_OFF, 0.5f, 2f)

        player.sendMessage(Component.translatable(
            "msg.myshore.tag.player.map_vote.self",
            mapNameComp
        ))

        game.gamePlayers
            .filter { it.player.uniqueId != player.uniqueId }
            .forEach {
                it.player.sendMessage(Component.translatable(
                    "msg.myshore.tag.player.map_vote.other",
                    Component.text(player.name),
                    mapNameComp
                ))
            }
    }
}