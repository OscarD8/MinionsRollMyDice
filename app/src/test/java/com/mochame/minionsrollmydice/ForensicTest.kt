package com.mochame.minionsrollmydice

import app.cash.turbine.test
import com.mochame.minionsrollmydice.data.CharacterAttributesDao
import com.mochame.minionsrollmydice.data.CharacterAttributesEntity
import com.mochame.minionsrollmydice.data.CharacterRepository
import com.mochame.minionsrollmydice.di.IoDispatcher
import com.mochame.minionsrollmydice.domain.AttributeType
import com.mochame.minionsrollmydice.domain.CharacterAttributes
import com.mochame.minionsrollmydice.domain.DiceRollerStateMachine
import com.mochame.minionsrollmydice.ui.CharacterSheetViewModel
import com.mochame.minionsrollmydice.ui.DiceRollerViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

// 1. Extension function to retrieve CoroutineDispatcher from TestScope context
fun TestScope.getTestDispatcher(): CoroutineDispatcher {
    return this.coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
}

// 2. High-fidelity Test Double for Room DAO adhering to Skill rules
class FakeCharacterAttributesDao : CharacterAttributesDao {
    private val _attributesFlow = MutableStateFlow<CharacterAttributesEntity?>(null)

    override fun observeAttributes(): Flow<CharacterAttributesEntity?> {
        return _attributesFlow.asStateFlow()
    }

    override suspend fun getAttributes(): CharacterAttributesEntity? {
        return _attributesFlow.value
    }

    override suspend fun saveAttributes(entity: CharacterAttributesEntity) {
        // Enforce database-level invariants
        require(entity.id == 1) { "Single-profile restriction breached: id must be 1" }
        require(entity.wisdom in 0..5) { "Wisdom must be between 0 and 5" }
        require(entity.insight in 0..5) { "Insight must be between 0 and 5" }
        require(entity.inspired in 0..5) { "Inspired must be between 0 and 5" }
        require(entity.wisdom + entity.insight + entity.inspired <= 5) { "Total sum must be <= 5" }

        _attributesFlow.value = entity
    }
}

class ForensicTest : KoinTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Enforce the concurrency rule: set the main dispatcher for testing viewmodels on JVM
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun test_database_fidelity_and_violations() = runTest {
        val fakeDao = FakeCharacterAttributesDao()

        // Verify default save works
        val entity = CharacterAttributesEntity(id = 1, wisdom = 2, insight = 2, inspired = 1)
        fakeDao.saveAttributes(entity)
        assertEquals(2, fakeDao.getAttributes()?.wisdom)

        // Violate single profile restriction - verify exception is thrown safely
        try {
            fakeDao.saveAttributes(entity.copy(id = 2))
            fail("Should throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Single-profile restriction breached: id must be 1", e.message)
        }

        // Violate boundary points check
        try {
            fakeDao.saveAttributes(entity.copy(wisdom = 6))
            fail("Should throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Wisdom must be between 0 and 5", e.message)
        }

        // Violate total points constraint
        try {
            fakeDao.saveAttributes(entity.copy(wisdom = 3, insight = 3))
            fail("Should throw IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Total sum must be <= 5", e.message)
        }
    }

    @Test
    fun test_koin_dynamic_binding_and_viewModel_udf() = runTest {
        // Retrieve the test dispatcher using the required extension function
        val testDispatcher = getTestDispatcher()

        // Setup test module using compiler-plugin compatible Koin DSL with type-safe qualifiers
        val testModule = module {
            single<CoroutineContext>(qualifier = named<IoDispatcher>()) { testDispatcher }
            single<CharacterAttributesDao> { FakeCharacterAttributesDao() }
            single { CharacterRepository(get(), get(named<IoDispatcher>())) }
            single { CharacterSheetViewModel(get()) }
            single { DiceRollerViewModel(get()) }
        }

        startKoin {
            modules(testModule)
        }

        val repository: CharacterRepository = get()
        val sheetViewModel: CharacterSheetViewModel = get()

        // 1. Verify repository saves successfully on injected test dispatcher
        repository.saveAttributes(CharacterAttributes(wisdom = 2, insight = 2, inspired = 1))

        // Allow init coroutine in Viewmodel to collect the repository's initial attributes
        testScheduler.advanceUntilIdle()

        // 2. Verify CharacterSheetViewModel receives update and manages UDF states
        sheetViewModel.uiState.test {
            // First item is the current settled state (wisdom = 2)
            var state = awaitItem()
            assertEquals(2, state.attributes.wisdom)

            // Try to increment (fails budget check, so no flow emission occurs)
            sheetViewModel.incrementAttribute(AttributeType.WISDOM)
            // Assert directly on the current value that nothing changed (StateFlow is conflated)
            assertEquals(2, sheetViewModel.uiState.value.attributes.wisdom)

            // Decrement inspired from 1 to 0 (succeeds, so emits new state)
            sheetViewModel.decrementAttribute(AttributeType.INSPIRED)
            state = awaitItem()
            assertEquals(0, state.attributes.inspired)
            assertEquals(1, state.attributes.remainingPoints())

            // Increment wisdom from 2 to 3 (succeeds, so emits new state)
            sheetViewModel.incrementAttribute(AttributeType.WISDOM)
            state = awaitItem()
            assertEquals(3, state.attributes.wisdom)

            // Save attributes (succeeds, emits isSaving=true, then saveSuccess=true)
            sheetViewModel.saveAttributes()
            
            state = awaitItem()
            assertTrue(state.isSaving)

            state = awaitItem()
            assertFalse(state.isSaving)
            assertTrue(state.saveSuccess)
        }
    }

    @Test
    fun test_dice_roller_animation_timing_turbine() = runTest {
        val testDispatcher = getTestDispatcher()
        val fakeDao = FakeCharacterAttributesDao()
        val repository = CharacterRepository(fakeDao, testDispatcher)
        val rollerViewModel = DiceRollerViewModel(repository)

        // Seed some points
        repository.saveAttributes(CharacterAttributes(wisdom = 2, insight = 1, inspired = 0)) // +3 modifier

        // Allow init coroutine in Viewmodel to collect initial attributes
        testScheduler.advanceUntilIdle()

        rollerViewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(3, initial.attributes.calculateTotalModifier())
            assertTrue(initial.diceState is DiceRollerStateMachine.State.Idle)

            // Trigger roll
            rollerViewModel.rollDice(20)

            // Collect all animation tick frames
            var state = awaitItem()
            assertTrue(state.diceState is DiceRollerStateMachine.State.Rolling)

            // Wait for completed state (Turbine handles virtual time delays automatically)
            while (state.diceState !is DiceRollerStateMachine.State.Rolled) {
                state = awaitItem()
            }

            val rolled = state.diceState as DiceRollerStateMachine.State.Rolled
            assertEquals(3, rolled.modifier)
            assertEquals(rolled.baseRoll + 3, rolled.finalTotal)
        }
    }
}
