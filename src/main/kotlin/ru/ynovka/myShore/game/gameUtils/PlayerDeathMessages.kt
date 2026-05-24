package ru.ynovka.myShore.game.gameUtils

import org.bukkit.event.entity.PlayerDeathEvent
import net.kyori.adventure.text.Component
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player


object PlayerDeathMessages {
    private val resolver = DeathMessageResolver(
        listOf(
            PlayerKillMessageRule,
            MobKillMessageRule,
            CauseMessageRule("msg.myshore.player.death.fall", "FALL"),
            CauseMessageRule("msg.myshore.player.death.fire", "FIRE", "FIRE_TICK", "HOT_FLOOR"),
            CauseMessageRule("msg.myshore.player.death.lava", "LAVA"),
            CauseMessageRule("msg.myshore.player.death.drowning", "DROWNING"),
            CauseMessageRule("msg.myshore.player.death.suffocation", "SUFFOCATION", "CRAMMING"),
            CauseMessageRule("msg.myshore.player.death.starvation", "STARVATION"),
            CauseMessageRule("msg.myshore.player.death.poison", "POISON"),
            CauseMessageRule("msg.myshore.player.death.wither", "WITHER"),
            CauseMessageRule("msg.myshore.player.death.magic", "MAGIC"),
            CauseMessageRule("msg.myshore.player.death.lightning", "LIGHTNING"),
            CauseMessageRule("msg.myshore.player.death.explosion", "BLOCK_EXPLOSION", "ENTITY_EXPLOSION"),
            CauseMessageRule("msg.myshore.player.death.projectile", "PROJECTILE"),
            CauseMessageRule("msg.myshore.player.death.falling_block", "FALLING_BLOCK"),
            CauseMessageRule("msg.myshore.player.death.fly_into_wall", "FLY_INTO_WALL"),
            CauseMessageRule("msg.myshore.player.death.freeze", "FREEZE"),
            CauseMessageRule("msg.myshore.player.death.sonic_boom", "SONIC_BOOM"),
            CauseMessageRule("msg.myshore.player.death.world_border", "WORLD_BORDER"),
            CauseMessageRule("msg.myshore.player.death.contact", "CONTACT"),
            CauseMessageRule("msg.myshore.player.death.thorns", "THORNS"),
            CauseMessageRule("msg.myshore.player.death.dragon_breath", "DRAGON_BREATH"),
            CauseMessageRule("msg.myshore.player.death.dryout", "DRYOUT"),
            GenericDeathMessageRule
        )
    )

    fun from(event: PlayerDeathEvent): Component = resolver.resolve(event)

    fun voidFall(player: Player): Component =
        Component.translatable("msg.myshore.player.fall_death", Component.text(player.name))
}

private class DeathMessageResolver(
    private val rules: List<DeathMessageRule>
) {
    fun resolve(event: PlayerDeathEvent): Component =
        rules.firstNotNullOf { it.create(event) }
}

private interface DeathMessageRule {
    fun create(event: PlayerDeathEvent): Component?
}

private object PlayerKillMessageRule : DeathMessageRule {
    override fun create(event: PlayerDeathEvent): Component? {
        val killer = event.player.killer
            ?: event.damageSource.causingEntity as? Player
            ?: return null

        if (killer.uniqueId == event.player.uniqueId) return null

        return Component.translatable(
            "msg.myshore.player.kill",
            Component.text(killer.name),
            Component.text(event.player.name)
        )
    }
}

private object MobKillMessageRule : DeathMessageRule {
    override fun create(event: PlayerDeathEvent): Component? {
        val entity = event.damageSource.causingEntity as? LivingEntity ?: return null
        if (entity is Player) return null
        if (entity.uniqueId == event.player.uniqueId) return null

        return Component.translatable(
            "msg.myshore.player.death.mob",
            Component.text(event.player.name),
            Component.text(entity.name)
        )
    }
}

private class CauseMessageRule(
    private val key: String,
    vararg causes: String
) : DeathMessageRule {
    private val causes = causes.toSet()

    override fun create(event: PlayerDeathEvent): Component? {
        val cause = event.player.lastDamageCause?.cause?.name ?: return null
        if (cause !in causes) return null

        return Component.translatable(
            key,
            Component.text(event.player.name)
        )
    }
}

private object GenericDeathMessageRule : DeathMessageRule {
    override fun create(event: PlayerDeathEvent): Component =
        Component.translatable(
            "msg.myshore.player.death.generic",
            Component.text(event.player.name)
        )
}