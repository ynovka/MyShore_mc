package ru.ynovka.myShore.game.pillars.states

import ru.ynovka.myShore.game.pillars.Pillar.Companion.TELEPORT_Y
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.game.GamePlayer.Companion.asPlayers
import ru.ynovka.myShore.game.gameUtils.ActionbarTimer
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.gameUtils.BossbarTimer
import ru.ynovka.myShore.game.pillars.PillarsPlayer
import ru.ynovka.myShore.game.pillars.PillarsWorld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.utils.restrictToBlock
import ru.ynovka.myShore.game.SpectatorReason
import net.kyori.adventure.text.Component
import org.bukkit.potion.PotionEffectType
import ru.ynovka.myShore.game.GameState
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.Location
import org.bukkit.GameMode
import org.bukkit.Material


class PillarsInProgress(game: PillarsGame) : GameState<PillarsPlayer, PillarsWorld, PillarsGame>(game) {

    override fun onEnterState() {
        // удаляем колбы
        val world = game.gameWorld.getOrCreate().get()
        game.gameWorld.pillars.forEach { pillar ->
            val blockLoc = Location(world, pillar.x.toDouble(), TELEPORT_Y - 1, pillar.z.toDouble())

            scheduler.schedule {
                val centerX = blockLoc.blockX
                val centerZ = blockLoc.blockZ

                for (x in centerX - 2..centerX + 2) {
                    for (z in centerZ - 2..centerZ + 2) {
                        for (y in 101..116) {
                            world.getBlockAt(x, y, z).type = Material.AIR
                        }
                    }
                }
            }.region(blockLoc).once()
        }

        // даём эффект плавного падения и меняем режим на выживание
        game.gamePlayers.asPlayers().forEach { player ->
            scheduler.schedule {
                player.gameMode = GameMode.SURVIVAL
                player.inventory.clear()
                player.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.SLOW_FALLING,
                        40,
                        0,
                        false,
                        false,
                        false
                    )
                )
            }.entity(player).once()
        }

        // даём возможность двигаться по приземлению
        scheduler.schedule {
            game.gamePlayers.asPlayers().forEach { player ->
                scheduler.schedule {
                    player.restrictToBlock(false)
                }.entity(player).once()
            }
        }.after(40L, Clock.TICKS).once()

        // Выдача рандом предметов
        startGiveRandomItemsTimer()

        // Барьер
        scheduler.schedule {
            val time = (world.worldBorder.size / 0.25).toLong()
            world.worldBorder.changeSize(2.0, time * 20L)
            BossbarTimer.startCountdownTimer(
                time = time.toInt(),
                game = game,
                state = this,
                onCompletion = { game, _ ->
                    game.fsm.transitionTo(PillarsFinishing(game))
                }
            )
        }.global().once()
    }

    // todo короче если под игроком нету блоков (getHighest) даём левитацию + фейрверки
    override fun onPlayerBecomeSpectator(gamePlayer: PillarsPlayer, reason: SpectatorReason) {
        hasWinner(gamePlayer)
        val player = gamePlayer.playerOrNull
        val world = game.gameWorld.getOrCreate().get()
        player?.let {
            scheduler.schedule {
                player.gameMode = GameMode.SPECTATOR
                player.teleportAsync(world.spawnLocation)
            }.entity(player).once()
        }
    }

    override fun onPlayerLeave(gamePlayer: PillarsPlayer) = hasWinner(gamePlayer)

    private fun hasWinner(gamePlayer: PillarsPlayer) {
        if (game.gamePlayers.size != 1) return
        val player = gamePlayer.playerOrNull
        player?.let {
            scheduler.schedule {
                val pillar = game.gameWorld.pillars.firstOrNull { it.owner == player.uniqueId } ?: return@schedule
                val world = game.gameWorld.get() ?: return@schedule
                player.teleportAsync(Location(
                    world, pillar.x + 0.5, TELEPORT_Y, pillar.z + 0.5
                ))
            }.entity(player).once()
        }


        val winner = game.gamePlayers.firstOrNull() ?: return
        val winnerPlayer = winner.player

        val msg = Component.translatable(
            "msg.myshore.player.win",
            Component.text(winnerPlayer.name)
        )
        val toAnon = game.gamePlayers + game.spectatorPlayers
        toAnon.asPlayers().forEach {
            scheduler.schedule {
                it.sendMessage(msg)
            }.entity(it).once()
        }

        game.fsm.transitionTo(PillarsFinishing(game))
    }

    private fun startGiveRandomItemsTimer() {
        ActionbarTimer.startCountdownTimer(
            time = 7,
            game = game,
            state = this,
            componentKey = "bar.myshore.new_item_in",
            playSound = false,
            onCompletion = { game, _ ->
                if (game.fsm.current !is PillarsInProgress) return@startCountdownTimer
                game.gamePlayers.asPlayers().forEach { player ->
                    scheduler.schedule {
                        player.inventory.addItem(ItemStack.of(items.random()))
                    }.entity(player).once()
                }
                startGiveRandomItemsTimer()
            }
        )
    }

    override fun canPlayerJoin(gamePlayer: PillarsPlayer) = false

    companion object {
        val excludedItems = setOf(
            Material.AIR,
            Material.COMMAND_BLOCK,
            Material.COMMAND_BLOCK_MINECART,
            Material.CHAIN_COMMAND_BLOCK,
            Material.REPEATING_COMMAND_BLOCK,
            Material.NETHER_PORTAL,
            Material.END_PORTAL,
            Material.LIGHT,
            Material.WATER,
            Material.LAVA,
        )

        val items = Material.entries
            .asSequence()
            .filterNot { it.name.startsWith("LEGACY_") }
            .filter { it.isItem }
            .filterNot { it in excludedItems }
            .toList()
    }
}