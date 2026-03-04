package ru.ynovka.myShore.lobby

import java.util.TreeSet


object LobbyIdAllocator {
    private val freeIds: TreeSet<Int> = TreeSet()
    private var nextId: Int = 1

    fun acquire(): Int {
        return if (freeIds.isNotEmpty()) {
            freeIds.pollFirst()
        } else {
            nextId++
            nextId - 1
        }
    }

    fun release(id: Int) {
        if (id in 1..<nextId) {
            freeIds.add(id)
        }
    }
}
