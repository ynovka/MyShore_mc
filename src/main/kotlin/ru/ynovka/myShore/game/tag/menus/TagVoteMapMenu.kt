package ru.ynovka.myShore.game.tag.menus

import ru.ynovka.myShore.game.tag.TagItems.tagVoteMountainTrackMapMenuItem
import com.github.darksoulq.abyssallib.server.translation.ServerTranslator
import ru.ynovka.myShore.game.tag.states.TagVoting.Companion.setupMap
import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import ru.ynovka.myShore.game.tag.TagItems.tagVoteJungleMapMenuItem
import ru.ynovka.myShore.game.tag.TagItems.tagVoteRandomMapMenuItem
import ru.ynovka.myShore.game.tag.TagGame.Companion.currentTagGame
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import ru.ynovka.myShore.game.tag.states.TagWaitingForPlayers
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import net.kyori.adventure.text.format.NamedTextColor
import com.github.darksoulq.abyssallib.world.gui.Gui
import com.github.darksoulq.abyssallib.world.gui.gui
import ru.ynovka.myShore.texturepack.GuiTextures
import ru.ynovka.myShore.game.tag.maps.TagMaps
import ru.ynovka.myShore.game.tag.maps.TagMap
import net.kyori.adventure.text.Component
import org.bukkit.inventory.MenuType
import org.bukkit.entity.Player
import org.bukkit.Sound


@Suppress("UnstableApiUsage")
object TagVoteMapMenu {
    fun get(): Gui = gui(
        MenuType.GENERIC_9X1,
        Component.text().color(NamedTextColor.WHITE)
            .append(TextOffset.getOffset(-8))
            .append(GuiTextures.MENU_1x9_028)
            .append(TextOffset.getOffset(-170))
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
        if (game.fsm.current is TagWaitingForPlayers) {
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