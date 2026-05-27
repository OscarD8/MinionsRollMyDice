package com.mochame.minionsrollmydice.data

import com.mochame.minionsrollmydice.di.IoDispatcher
import com.mochame.minionsrollmydice.domain.CharacterAttributes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Thread-safe repository implementing the clean data-access contracts.
 * Handles database operations off the main thread using injected context.
 */
@Single
class CharacterRepository(
    private val dao: CharacterAttributesDao,
    @IoDispatcher private val ioDispatcher: CoroutineContext = EmptyCoroutineContext
) {
    /**
     * Cold stream of the user's character attributes.
     * Maps database entities to pure Kotlin domain models.
     */
    fun observeAttributes(): Flow<CharacterAttributes> {
        return dao.observeAttributes().map { entity ->
            entity?.toDomain() ?: CharacterAttributes()
        }
    }

    /**
     * Persists character sheet attributes to disk.
     * Enforces the `data` rule by executing on the injected qualified coroutine context.
     * Hardcoded Dispatchers.IO is completely avoided here.
     */
    suspend fun saveAttributes(attributes: CharacterAttributes) = withContext(ioDispatcher) {
        dao.saveAttributes(CharacterAttributesEntity.fromDomain(attributes))
    }
}
