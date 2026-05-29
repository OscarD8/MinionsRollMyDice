package com.mochame.minionsrollmydice.di

import android.content.Context
import androidx.room.Room
import com.mochame.minionsrollmydice.data.AppDatabase
import com.mochame.minionsrollmydice.data.CharacterAttributesDao
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// Type-safe qualifier annotation to remove magic strings
@Qualifier
annotation class IoDispatcher

@Module
@ComponentScan("com.mochame.minionsrollmydice")
class AppModule {

    @Single
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineContext {
        return Dispatchers.IO
    }

    @Single
    fun provideDatabase(
        context: Context,
        @IoDispatcher ioDispatcher: CoroutineContext = EmptyCoroutineContext
    ): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "minions_roll_my_dice_db"
        )
        .fallbackToDestructiveMigration()
        // Room 2.8.4+ feature: configure the coroutine context used for queries
        .setQueryCoroutineContext(ioDispatcher)
        .build()
    }

    @Single
    fun provideCharacterAttributesDao(database: AppDatabase): CharacterAttributesDao {
        return database.characterAttributesDao()
    }
}
