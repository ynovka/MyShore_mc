package ru.ynovka.myShore.games.tag

import com.github.darksoulq.abyssallib.server.event.ActionResult
import com.github.darksoulq.abyssallib.extension.openGui
import ru.ynovka.myShore.games.tag.menus.TagVoteMapMenu
import ru.ynovka.myShore.texturepack.TexturePack
import ru.ynovka.myShore.MyShore.Companion.ITEMS
import ru.ynovka.myShore.MyShore.Companion.inst
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.utils.cancelItem
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import ru.ynovka.myShore.games.tag.TagStats.getPlayerTagStats


object TagItems {

    fun register() {
        TexturePack.createItemTexture(tagMapVoteMenu)
        ITEMS.register("tag_map_vote_menu") { tagMapVoteMenu }
        TexturePack.createItemTexture(tagPlayerStats)
        ITEMS.register("tag_player_stats") { tagPlayerStats }
        TexturePack.createItemTexture(tagChooseRoleMenu)
        TexturePack.createItemTexture(tagChooseLobbyMenu)
        TexturePack.createItemTexture(tagVoteRandomMapMenuItem)
        TexturePack.createItemTexture(tagVoteJungleMapMenuItem)
        TexturePack.createItemTexture(tagVoteMountainTrackMapMenuItem)
    }

    val tagMapVoteMenu = cancelItem(Key.key(inst, "tag_map_vote_menu")) {
        tooltip { player ->
            // todo перевод
            line(Component.translatable("desc.myshore.tag_map_vote_menu.1"))
            line(Component.translatable("desc.myshore.tag_map_vote_menu.2"))
            line(Component.translatable("desc.myshore.tag_map_vote_menu.3"))
        }
        onUse { source, _, _ ->
            (source as Player).openGui(TagVoteMapMenu.get())
            ActionResult.CANCEL
        }
        onUseOn { ctx ->
            (ctx.source as Player).openGui(TagVoteMapMenu.get())
            ActionResult.CANCEL
        }
    }

    val tagPlayerStats = cancelItem(Key.key(inst, "tag_player_stats")) {
        tooltip { player ->
            line(Component.translatable("desc.myshore.tag_player_stats.1"))
            if (player != null) {
                val stats = getPlayerTagStats(player)
                line(Component.translatable(
                    "desc.myshore.tag_player_stats.2",
                    Component.text(stats.victimWin),
                    Component.text(stats.hunterWin)
                ))
                line(Component.translatable(
                    "desc.myshore.tag_player_stats.3",
                    Component.text(stats.victimLose),
                    Component.text(stats.hunterLose)
                ))
                line(Component.translatable(
                    "desc.myshore.tag_player_stats.4",
                    Component.text(stats.totalPlayed()),
                    Component.text(stats.winstike)
                ))
            } else {
                line(Component.translatable("desc.myshore.no_data"))
            }
        }
        onUse { source, _, _ ->
            (source as Player).sendMessage(
                Component.translatable("msg.myshore.tag.see_player_stats")
            )
            ActionResult.CANCEL
        }
        onUseOn { ctx ->
            (ctx.source as Player).sendMessage(
                Component.translatable("msg.myshore.tag.see_player_stats")
            )
            ActionResult.CANCEL
        }
    }


    val tagChooseRoleMenu = cancelItem(Key.key(inst, "tag_choose_role_menu")) {
        tooltip { player ->
            line(Component.translatable("desc.myshore.tag_choose_role_menu.1"))
            line(Component.translatable("desc.myshore.tag_choose_role_menu.2"))
            line(Component.translatable("desc.myshore.tag_choose_role_menu.3"))
        }
    }

    val tagChooseLobbyMenu = cancelItem(Key.key(inst, "tag_choose_lobby_menu")) {
        tooltip { player ->
            line(Component.translatable("desc.myshore.tag_choose_lobby_menu.1"))
        }
    }

    val tagVoteRandomMapMenuItem = cancelItem(Key.key(inst, "tag_vote_random_map"))

    val tagVoteJungleMapMenuItem = cancelItem(Key.key(inst, "tag_vote_jungle_map"))

    val tagVoteMountainTrackMapMenuItem = cancelItem(Key.key(inst, "tag_vote_mountain_track_map"))
}