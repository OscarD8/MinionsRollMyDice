package com.mochame.minionsrollmydice.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object (DAO) for [CharacterAttributesEntity].
 */
@Dao
interface CharacterAttributesDao {

    @Query("SELECT * FROM character_attributes WHERE id = 1 LIMIT 1")
    fun observeAttributes(): Flow<CharacterAttributesEntity?>

    @Query("SELECT * FROM character_attributes WHERE id = 1 LIMIT 1")
    suspend fun getAttributes(): CharacterAttributesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAttributes(entity: CharacterAttributesEntity)
}
