package ru.ynovka.myShore.games.tag.menus

import ru.ynovka.myShore.games.tag.TagItems.tagVoteMountainTrackMapMenuItem
import com.github.darksoulq.abyssallib.server.resource.util.TextOffset
import ru.ynovka.myShore.games.tag.TagItems.tagVoteJungleMapMenuItem
import ru.ynovka.myShore.games.tag.TagItems.tagVoteRandomMapMenuItem
import com.github.darksoulq.abyssallib.world.gui.element.GuiButton
import com.github.darksoulq.abyssallib.world.gui.SlotPosition
import net.kyori.adventure.text.format.NamedTextColor
import com.github.darksoulq.abyssallib.world.gui.Gui
import com.github.darksoulq.abyssallib.world.gui.gui
import ru.ynovka.myShore.games.tag.maps.TagGameMap
import ru.ynovka.myShore.utils.Utils.toComponent
import ru.ynovka.myShore.texturepack.GuiTextures
import ru.ynovka.myShore.utils.Utils.asPlayers
import ru.ynovka.myShore.games.tag.TagGame
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.lobby.getLobby
import org.bukkit.inventory.MenuType
import org.bukkit.entity.Player
import org.bukkit.Sound


@Suppress("UnstableApiUsage")
object TagVoteMapMenu {
    fun get(): Gui = gui(
        MenuType.GENERIC_9X1,
        Component.text()
            .append(TextOffset.getOffsetMinimessage(-8).toComponent().color(NamedTextColor.WHITE))
            .append(GuiTextures.TAG_CHOOSE_ROLE_MENU!!.toComponent().color(NamedTextColor.WHITE))
            .append(TextOffset.getOffsetMinimessage(-170).toComponent().color(NamedTextColor.WHITE))
            .append(Component.translatable("menu.myshore.minigames"))
            .build(),
    ) {
        set(
            SlotPosition.top(0),
            GuiButton.of(tagVoteRandomMapMenuItem.getStack(null)) {
                println("pressed tagVoteMap random")
            }
        )
        set(
            SlotPosition.top(3),
            GuiButton.of(tagVoteJungleMapMenuItem.getStack(null)) {
                println("pressed tagVoteMap jungle")
            }
        )
        set(
            SlotPosition.top(5),
            GuiButton.of(tagVoteMountainTrackMapMenuItem.getStack(null)) {
                println("pressed tagVoteMap mountain track")
            }
        )
    }

    private fun voteMap(player: Player, map: TagGameMap, isRandom: Boolean = false) {
        val mapName = if (isRandom) "Случайная" else map.mapName
        val lobby = player.getLobby() ?: return
        val game = lobby.game as? TagGame ?: return

        game.mapVotes[player.uniqueId] = map

        player.playSound(
            player.location,
            Sound.BLOCK_NOTE_BLOCK_PLING,
            0.5f,
            2f
        )

        player.sendMessage(
            Component.text("Вы проголосовали за карту: ")
                .append(Component.text(mapName))
        )

        game.players.keys.filter { it != player.uniqueId }.asPlayers().forEach {
            it.sendMessage(
                Component.text("${player.name} проголосовал за карту: ")
                    .append(Component.text(mapName))
            )
        }
    }
}