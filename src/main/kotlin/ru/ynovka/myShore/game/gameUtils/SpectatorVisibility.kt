package ru.ynovka.myShore.game.gameUtils

import ru.ynovka.myShore.game.GamePlayer


internal class SpectatorVisibility(
    private val visibilityGroup: VisibilityGroup
) {
    fun refresh(
        spectatorPlayers: Iterable<GamePlayer>
    ) {
        val spectatorIds = spectatorPlayers.mapTo(mutableSetOf()) { it.playerId }
        val viewerIds = visibilityGroup.getViewers()

        viewerIds.forEach { viewerId ->
            viewerIds.forEach { targetId ->
                if (viewerId == targetId) return@forEach

                if (targetId in spectatorIds) {
                    visibilityGroup.hideFor(viewerId, targetId)
                } else {
                    visibilityGroup.showFor(viewerId, targetId)
                }
            }
        }
    }
}
