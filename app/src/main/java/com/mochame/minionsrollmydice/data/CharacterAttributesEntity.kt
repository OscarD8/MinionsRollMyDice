package com.mochame.minionsrollmydice.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mochame.minionsrollmydice.domain.CharacterAttributes

/**
 * Room database Entity representing character sheet attributes.
 * The primary key [id] is hardcoded to 1 to ensure only a single profile row exists.
 */
@Entity(tableName = "character_attributes")
data class CharacterAttributesEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "wisdom") val wisdom: Int = 0,
    @ColumnInfo(name = "insight") val insight: Int = 0,
    @ColumnInfo(name = "inspired") val inspired: Int = 0,
    @ColumnInfo(name = "strength") val strength: Int = 0,
    @ColumnInfo(name = "wealth") val wealth: Int = 0,
    @ColumnInfo(name = "luck") val luck: Int = 0
) {
    fun toDomain(): CharacterAttributes {
        return CharacterAttributes(
            wisdom = wisdom,
            insight = insight,
            inspired = inspired,
            strength = strength,
            wealth = wealth,
            luck = luck
        )
    }

    companion object {
        fun fromDomain(domain: CharacterAttributes): CharacterAttributesEntity {
            return CharacterAttributesEntity(
                id = 1,
                wisdom = domain.wisdom,
                insight = domain.insight,
                inspired = domain.inspired,
                strength = domain.strength,
                wealth = domain.wealth,
                luck = domain.luck
            )
        }
    }
}
