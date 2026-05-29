/e# CharacterSheetV2 Launch Criteria Specification

## Executive Summary
Add three new character traits (Strength, Wealth, Luck) to the existing character sheet, increase the total possible point allocations from the current maximum to 10, and introduce a themed animation on the point allocation buttons when a player increases or reduces a value.

## [ARL] Architectural Requirements
- [ ]

## [CCL] Concurrency & Timing Requirements
- [ ] Replace `collectAsState()` with `collectAsStateWithLifecycle()` (androidx.lifecycle:lifecycle-runtime-compose) on all StateFlow collectors in CharacterSheetScreen.kt to respect Lifecycle.State.STARTED boundary
- [ ] Replace `LaunchedEffect(state.saveSuccess)` dismiss-timer pattern with a `Channel<Unit>` or `SharedFlow` one-shot event emitted from the ViewModel to eliminate the race condition on rapid saves
- [ ] Add explicit `.flowOn(ioDispatcher)` to `CharacterRepository.observeAttributes()` at the repository boundary — do not rely on Room's implicit emission thread
- [ ] Replace `EmptyCoroutineContext` default for `@IoDispatcher` in AppModule with `Dispatchers.IO` or remove the default parameter entirely to prevent silent fallback failures
- [ ] Write `TestScope` + `advanceTimeBy(3000)` virtual-time test for the dismiss-timer coroutine in CharacterSheetViewModel
- [ ] Write Turbine-based test asserting `uiState` transitions on save success and reset
- [ ] Write cancellation test verifying `viewModelScope` collector is cancelled on `ViewModel.onCleared()`
- [ ] Update `FakeCharacterAttributesDao` constraint to validate all 6 attribute fields (wisdom, insight, inspired, strength, wealth, luck) against MAX_POINTS = 10 to restore fake-production fidelity

## [SIL] Safety & Forensic Requirements
- [ ]

## [DPL] Data Persistence Requirements
- [ ]

## [DSL] Data Synchronization Requirements
- [ ]
