package ru.ynovka.myShore.game.pillars.states

import ru.ynovka.myShore.game.GamePlayer.Companion.forEachOnlinePlayer
import ru.ynovka.myShore.game.pillars.Pillar.Companion.TELEPORT_Y
import com.github.darksoulq.abyssallib.server.scheduler.Clock
import ru.ynovka.myShore.game.gameUtils.ActionbarTimer
import ru.ynovka.myShore.MyShore.Companion.scheduler
import ru.ynovka.myShore.game.gameUtils.BossbarTimer
import ru.ynovka.myShore.game.pillars.PillarsPlayer
import ru.ynovka.myShore.game.pillars.PillarsWorld
import ru.ynovka.myShore.game.pillars.PillarsGame
import ru.ynovka.myShore.utils.restrictToBlock
import ru.ynovka.myShore.game.SpectatorReason
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

        game.roundGameMode.gm.roundStart(game)

        game.gamePlayers.forEach {
            it.resetKills()
            it.resetEliminated()
        }

        // даём эффект плавного падения и меняем режим на выживание
        game.activePlayers.forEachOnlinePlayer { player ->
            scheduler.schedule {
                player.gameMode = GameMode.SURVIVAL
                player.allowFlight = false
                player.isFlying = false
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
            game.activePlayers.forEachOnlinePlayer { player ->
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
            world.worldBorder.changeSize(4.0, time * 20L)
            BossbarTimer.startCountdownTimer(
                time = time.toInt(),
                game = game,
                state = this,
                onCompletion = { game, state ->
                    scheduler.schedule {
                        game.activePlayers.forEachOnlinePlayer { p ->
                            println(p.name)
                            //p.damage(1.0)
                        }
                    }
                        .global()
                        .repeatWhile { game.fsm.current === state }
                        .repeatEvery(20L, Clock.TICKS)

                    BossbarTimer.startCountdownTimer(
                        time = 60,
                        game = game,
                        state = state,
                        onCompletion = { game, _ ->
                            game.fsm.transitionTo(PillarsFinishing(game))
                        }
                    )
                }
            )
        }.global().once()
    }

    override fun onPlayerBecomeSpectator(gamePlayer: PillarsPlayer, reason: SpectatorReason) {
        tryFinishRound()

        game.gameWorld.get()?.let { world ->
            gamePlayer.withOnlinePlayer { player ->
                scheduler.schedule {
                    player.teleportAsync(Location(world, 0.0, 110.0, 0.0))
                }.entity(player).once()
            }
        }
    }

    override fun onSpectatorJoin(gameSpectator: PillarsPlayer) {
        gameSpectator.resetKills()
    }

    override fun onPlayerLeave(gamePlayer: PillarsPlayer) = tryFinishRound()

    private fun tryFinishRound() {
        if (game.activePlayers.size <= 1) {
            game.fsm.transitionTo(PillarsFinishing(game))
        }
    }

    private fun startGiveRandomItemsTimer() {
        val gm = game.roundGameMode.gm
        ActionbarTimer.startCountdownTimer(
            time = gm.giveItemsDelaySec,
            game = game,
            state = this,
            componentKey = "bar.myshore.new_item_in",
            playSound = false,
            onCompletion = { game, _ ->
                if (game.fsm.current !is PillarsInProgress) return@startCountdownTimer
                game.activePlayers.forEachOnlinePlayer { player ->
                    scheduler.schedule {
                        if (gm.shouldClearInventory) player.inventory.clear()

                        repeat(gm.giveItemsAmount) {
                            player.inventory.addItem(ItemStack.of(items.random()))
                        }
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
            Material.DEBUG_STICK,
            Material.TEST_BLOCK,
            Material.TEST_INSTANCE_BLOCK,
            Material.STRUCTURE_VOID,
            Material.STRUCTURE_BLOCK,
            Material.COMMAND_BLOCK_MINECART,
            Material.JIGSAW,
            Material.LIGHT,
            Material.BARRIER,
            Material.ENCHANTED_BOOK,
        )

        val items = Material.entries
            .asSequence()
            .filterNot { it.name.startsWith("LEGACY_") }
            .filter { it.isItem }
            .filterNot { it in excludedItems }
            .toList()
    }
}
