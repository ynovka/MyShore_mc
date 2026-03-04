package ru.ynovka.myShore.games.tag

import com.github.darksoulq.abyssallib.server.event.ActionResult
import com.github.darksoulq.abyssallib.extension.openGui
import ru.ynovka.myShore.games.tag.menus.TagVoteMapMenu
import net.kyori.adventure.text.format.NamedTextColor
import ru.ynovka.myShore.texturepack.TexturePack
import ru.ynovka.myShore.MyShore.Companion.ITEMS
import ru.ynovka.myShore.MyShore.Companion.inst
import net.kyori.adventure.text.Component
import ru.ynovka.myShore.utils.cancelItem
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player


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
            line(Component.text("Проголосуйте за смену карты."))
            line(Component.text("Для смены необходимо 50% и более голосов,"))
            line(Component.text("Если голосов поровну — карта выбирается случайно."))
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
            line(Component.translatable("Статистика «Салочки»"))
            if (player != null) {
                // todo stats
                // val stats = getPlayerTagStats(player)
                // todo перевод
                // line(Component.text("✔ Победы: раннер ${stats.victimWin}, охотник ${stats.hunterWin}"))
                // line(Component.text("✘ Поражения: раннер ${stats.victimLose}, охотник ${stats.hunterLose}"))
                // line(Component.text("▶ Всего игр: ${stats.totalPlayed()} | Серия побед: ${stats.winstike}"))
            } else {
                // todo перевод
                line(Component.text("Данные не загружены").color(NamedTextColor.DARK_GRAY))
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
            // todo перевод
            line(Component.text("Вы можете выбрать желаемую роль для игры."))
            line(Component.text("Этот выбор не гарантирует, что вам достанется именно эта роль!"))
            line(Component.text("Выбор сохраняется, пока вы его не измените, или не смените лобби."))
        }
    }

    val tagChooseLobbyMenu = cancelItem(Key.key(inst, "tag_choose_lobby_menu")) {
        tooltip { player ->
            // todo перевод
            line(Component.text("Вы можете выбрать любое публичное лобби."))
        }
    }

    val tagVoteRandomMapMenuItem = cancelItem(Key.key(inst, "tag_vote_random_map")) {
        tooltip { player ->
            // todo перевод
            line(Component.text("Случайная карта").color(NamedTextColor.WHITE))
        }
    }

    val tagVoteJungleMapMenuItem = cancelItem(Key.key(inst, "tag_vote_jungle_map")) {
        tooltip { player ->
            // todo перевод
            line(Component.text("Карта \"джунгли\"; средняя сложность; маленького размера.").color(NamedTextColor.WHITE))
        }
    }

    val tagVoteMountainTrackMapMenuItem = cancelItem(Key.key(inst, "tag_vote_mountain_track_map")) {
        tooltip { player ->
            // todo перевод
            line(Component.text("Карта \"зимняя деревня\"; средняя сложность; среднего размера.").color(NamedTextColor.WHITE))
        }
    }
}