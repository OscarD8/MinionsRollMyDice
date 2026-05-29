# CharacterSheetV2 Launch Criteria Specification

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

### [CCL] Concurrency Invariants - 2026-05-29 20:14:58

#### 1. Qualified Dispatcher Injection & Thread-Safe Operations
- **Scope Restriction**: All background work, including database reads and writes, must be offloaded to `ioDispatcher` qualified by `@IoDispatcher`.
- **Dispatcher Qualifier**: Enforce type-safe dependency injection of `CoroutineContext` using `@IoDispatcher` (meta-annotated with `@Qualifier`), resolving to `Dispatchers.IO` in production (defined in `AppModule.kt`).
- **No Hardcoding**: Absolutely no direct use of `Dispatchers.IO` or `Dispatchers.Default` within repositories or ViewModels. All dispatchers must be qualified and injected to ensure test double fidelity and virtual-time controllability.
- **Explicit Flow Context**: Ensure that `CharacterRepository.observeAttributes()` explicitly uses `.flowOn(ioDispatcher)` to enforce query thread isolation and prevent implicit Room thread leakage into UI scopes.

#### 2. Lifecycle-Aware State Flow Collection
- **StateFlow Collection**: To avoid memory leaks and wasteful background collection, replace `collectAsState()` with `collectAsStateWithLifecycle()` from `androidx.lifecycle:lifecycle-runtime-compose` on all state collections inside Jetpack Compose screens (e.g., `CharacterSheetScreen.kt`).
- **Target State**: This restricts subscription to the `Lifecycle.State.STARTED` boundary, pausing updates when the application goes to the background.

#### 3. Channel-Based One-Shot UI Events
- **Race Condition Prevention**: Replace mutable-state-driven timers (like `LaunchedEffect(state.saveSuccess)`) with an explicit one-shot event mechanism. Use `Channel<Unit>` or `Channel<CharacterSheetEvent>` in the ViewModel, exposed as a `Flow` using `receiveAsFlow()`.
- **Event Flow Collection**: Collect this flow in Compose using `LaunchedEffect(true)` to trigger transient UI events (like showing a save-success banner and launching a 3000ms dismiss timer) without maintaining complex state flags that suffer from rapid-save race conditions.

#### 4. Rigorous Concurrency Testing Invariants
- **Virtual Time Manipulation**: Validate timing-dependent logic (such as the 3000ms dismiss timer) using `kotlinx-coroutines-test` virtual time.
- **Fidelity of Test Doubles**: Ensure `FakeCharacterAttributesDao` maintains 100% behavioral fidelity with the production Room database, validating all 6 attributes (wisdom, insight, inspired, strength, wealth, luck) against a maximum budget of 10 points and individual attribute boundaries `[0, 5]`.
- **State Verification via Turbine**: Validate StateFlow transitions during saving (e.g., `isSaving`, `saveSuccess`) and event emissions utilizing `Turbine` to guarantee state correctness across concurrent tasks.
- **Cancellation Isolation**: Confirm that all launched coroutines within `viewModelScope` are bound to the lifecycle and successfully cancelled when `ViewModel.onCleared()` is invoked.


## [SIL] Safety & Forensic Requirements
- [ ]

## [DPL] Data Persistence Requirements
- [ ] Upgrade the database schema from Version 1 to Version 2 to support three new columns: `strength`, `wealth`, and `luck` as NOT NULL INTEGER columns with a DEFAULT value of 0.
- [ ] Enforce the single-row profile restriction in the database entity where `id` is a primary key hardcoded to `1`.
- [ ] Validate that all database persist operations (`saveAttributes`) enforce the `MAX_POINTS = 10` sum limit and individual attribute bounds `[0, 5]` on the written `CharacterAttributesEntity`.
- [ ] Ensure `FakeCharacterAttributesDao` has 100% behavioral fidelity with the real Room DAO, enforcing the same validation rules for all 6 columns and throwing `IllegalArgumentException` on invalid bounds or sum.
- [ ] Verify that database write operations are offloaded to `Dispatchers.IO` via the injected `@IoDispatcher` context.
- [ ] Add unit tests in `ForensicTest` checking database invariant violations for the three new attributes (`strength`, `wealth`, `luck`) to ensure zero-data-loss validation.

### [DPL] Data Persistence Invariants - 2026-05-29 19:56:09
#### 1. Database Keys & Table Layout Limits
- **Table Name**: `character_attributes`
- **Primary Key**: `id: Int = 1` (forces a single-row design to store one character profile in the local application state).
- **Columns & SQLite Type Mappings**:
  - `id`: INTEGER PRIMARY KEY NOT NULL
  - `wisdom`: INTEGER NOT NULL DEFAULT 0, value bound `[0, 5]`
  - `insight`: INTEGER NOT NULL DEFAULT 0, value bound `[0, 5]`
  - `inspired`: INTEGER NOT NULL DEFAULT 0, value bound `[0, 5]`
  - `strength`: INTEGER NOT NULL DEFAULT 0, value bound `[0, 5]`
  - `wealth`: INTEGER NOT NULL DEFAULT 0, value bound `[0, 5]`
  - `luck`: INTEGER NOT NULL DEFAULT 0, value bound `[0, 5]`
- **Global Table Constraints**: Sum of (`wisdom + insight + inspired + strength + wealth + luck`) $\le 10$.

#### 2. Schema Migration Path (V1 to V2)
- **Version 1 (Legacy)**: Table `character_attributes` had fields `id`, `wisdom`, `insight`, `inspired`.
- **Version 2 (Current)**: Adds `strength`, `wealth`, `luck`.
- **Migration SQL**:
  ```sql
  ALTER TABLE character_attributes ADD COLUMN strength INTEGER NOT NULL DEFAULT 0;
  ALTER TABLE character_attributes ADD COLUMN wealth INTEGER NOT NULL DEFAULT 0;
  ALTER TABLE character_attributes ADD COLUMN luck INTEGER NOT NULL DEFAULT 0;
  ```
- **Execution Strategy**: Currently handled via `.fallbackToDestructiveMigration()` in development. For production migration, a migration subclass `MIGRATION_1_2` will execute the above SQL sequentially to guarantee zero-data-loss upgrades.

#### 3. Concurrency, WAL & Transaction Boundaries
- **Query Threading**: Query execution is fully directed to `ioDispatcher` via `.setQueryCoroutineContext(ioDispatcher)` to avoid blocking the main UI thread.
- **Write-Ahead Logging (WAL)**: Single-Writer, Multi-Reader concurrency mode enabled. Room's internal SQLite bundle processes writes sequentially to protect the DB from locking and race conditions during quick-succession saves.
- **Transaction Atomicity**: The save mechanism `@Insert(onConflict = OnConflictStrategy.REPLACE)` provides an atomic UPSERT of the singleton row. The repository wraps saving inside `withContext(ioDispatcher)` to guarantee thread safety.

## [DSL] Data Synchronization Requirements
- [ ]
