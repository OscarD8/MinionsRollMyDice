package com.mochame.minionsrollmydice.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database instance for MinionsRollMyDice application.
 * Instantiation is fully delegated to the Koin DI container in AppModule.
 */
@Database(entities = [CharacterAttributesEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterAttributesDao(): CharacterAttributesDao
}
