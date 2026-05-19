package ru.ynovka.myShore

import com.github.darksoulq.abyssallib.common.database.relational.sql.Database
import ru.ynovka.myShore.game.tag.statistics.TagCaughtsRepository
import java.io.File

object Database {
    lateinit var db: Database
    lateinit var tagCaughtsRepository: TagCaughtsRepository

    fun register(plugin: MyShore) {
        db = Database(
            File(plugin.dataFolder, "data.db")
        )
        db.connect()
        tagCaughtsRepository = TagCaughtsRepository(db)
        tagCaughtsRepository.init()
    }
}