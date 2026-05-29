# CharacterSheetV2 Historical Audit Logs

Chronological record of evaluations, debates, and overrides.

## Timeline

================================================================================

### [CCL] Concurrency Control Lead — Discussion Pass - 2026-05-29 17:00:29

**Verdict:** NO-GO FOR UPLINK

**Lifecycle Phase:** Specification Design

**Core Issue Discussed:**

- Full lifecycle and concurrency review of CharacterSheetV2 scoped files. User expressed concern
  about lifecycle syntax correctness, janky UI, and memory leaks.

#### Resolution Details

8 findings identified across 8 files. 3 HIGH severity issues: (1) `collectAsState()` is not
lifecycle-aware and should be replaced with `collectAsStateWithLifecycle()` — currently keeps a live
Flow subscriber while the screen is backgrounded; (2) `LaunchedEffect(state.saveSuccess)` is keyed
on a mutable state boolean, creating a dismiss-timer race condition on rapid saves — recommend a
`Channel`/`SharedFlow` one-shot event bus instead; (7) zero coroutine-lifecycle tests exist for
CharacterSheetViewModel — no cancellation coverage, no virtual-time manipulation, no Turbine
assertions. 2 MEDIUM severity issues: (3) `observeAttributes()` in CharacterRepository has no
explicit `.flowOn(ioDispatcher)` — implicit Room threading is fragile; (6)
`FakeCharacterAttributesDao` constraint checks 3 fields against cap of 5, but domain now requires 6
fields against MAX_POINTS = 10 — fake diverges from production fidelity. 1 LOW severity issue: (8)
`EmptyCoroutineContext` default for `@IoDispatcher` is a silent fallback failure mode. 2 CLEAN
findings: `viewModelScope` ownership is correct (no leak surface); `saveAttributes()` dispatcher
qualification is correct. Overall verdict is NO-GO FOR UPLINK — the LaunchedEffect race and absent
lifecycle tests must be resolved before implementation is considered safe.

================================================================================

================================================================================
### [Data Persistence Lead] Discussion Pass - 2026-05-29 19:56:09

**Verdict:** GO

**Lifecycle Phase:** Specification Design

**Core Issue Discussed:**
- Sweep of the persistence/data package and all Room database components under `app/src/main` and `app/src/test` for the CharacterSheetV2 expansion. Focus on the schema upgrade to version 2, point limit increase to 10, single-profile row constraint validation, and coroutine execution context qualification.

#### Resolution Details
We swept 4 main-source files (`AppDatabase.kt`, `CharacterAttributesEntity.kt`, `CharacterAttributesDao.kt`, `CharacterRepository.kt`, `AppModule.kt`) and 1 test-source file (`ForensicTest.kt`).
Our findings show:
1. `CharacterAttributesEntity` and the domain class already define all 6 fields (wisdom, insight, inspired, strength, wealth, luck) with `MAX_POINTS = 10` enabled in domain logic.
2. The test double `FakeCharacterAttributesDao` in `ForensicTest.kt` diverges significantly from production rules by enforcing a hard legacy constraint on only 3 fields with sum <= 5. This behavioral drift will cause test failures or mask violations in testing.
3. `AppModule` is utilizing fallbackToDestructiveMigration() in development, and we established the migration requirements/SQL commands to upgrade production DBs safely from V1 to V2 without data loss.
4. Formulated the complete set of persistence requirements and invariants (including table limits, migration paths, WAL strategy, and transactional atomicity) and inserted them into the draft specification as empty checkboxes.

The specification design phase for persistence invariants is successfully completed, and we declare a verdict of GO.

================================================================================

================================================================================
### [Concurrency Control Lead] Discussion Pass - 2026-05-29 20:14:58

**Verdict:** GO

**Lifecycle Phase:** Specification Design

**Core Issue Discussed:**
- Technical sweep of the codebase for KSP-to-Koin compiler migration compliance, including annotation placement, dependency injection structures, type-safe qualifiers (`@IoDispatcher`), custom type qualifier behavior matching the Koin 4.2.0-RC1 plugin requirements, and review of concurrency and lifecycle handling for the `CharacterSheetV2` expansion.

#### Resolution Details
Swept the codebase (`AppModule.kt`, `MainActivity.kt`, `CharacterRepository.kt`, `CharacterSheetViewModel.kt`, `DiceRollerViewModel.kt`, `ForensicTest.kt`, `app/build.gradle.kts`, `gradle/libs.versions.toml`) to verify DI, Koin annotations, and concurrency setup:
1. **KSP-to-Koin Compiler Migration Compliance**:
   - Verified that the legacy `koin-ksp-compiler` dependency has been completely removed from `app/build.gradle.kts` and replaced by the Koin Compiler Plugin (`io.insert-koin.compiler.plugin` version `1.0.0`) in the plugins block, integrated natively into the K2 compilation pipeline (Kotlin 2.3.0).
   - Verified that dependencies in `libs.versions.toml` use Koin 4.2.0-RC1, which supports this compiler.
   - Confirmed best-practice imports: `org.koin.core.annotation.KoinViewModel` is used in both ViewModels rather than the deprecated `org.koin.android.annotation.KoinViewModel` package, ensuring proper Multiplatform compatibility.
   - Confirmed `@KoinApplication(modules = [AppModule::class])` in `MainActivity.kt` and subsequent startup using `startKoin<MinionsApp> { ... }` correctly leverage the compiler's reflectionless, classless FIR generation.
   - Inspected `@IoDispatcher` (a custom qualifier annotation with no value argument) and confirmed that it matches the Koin compiler plugin's type-qualifier behavior. Under the new compiler guidelines, custom annotations without value arguments register as type qualifiers (keyed on the class type) and resolve via `named<IoDispatcher>()` rather than magic string keys, which matches the test usage in `ForensicTest.kt` (`named<IoDispatcher>()`).
2. **CharacterSheetV2 Concurrency and Lifecycle Integration**:
   - Completed formulation of the `CharacterSheetV2` concurrency, timing, and testing requirements, including lifecycle-aware flow collection (`collectAsStateWithLifecycle`), channel-based event handling to resolve rapid-save race conditions, and virtual-time Turbine-based state flow testing.
   - Appended the comprehensive `### [CCL] Concurrency Invariants - 2026-05-29 20:14:58` section to `LC-CharacterSheetV2-draft.md` with empty checkboxes as all implementation remains pending.

All guidelines have been fully complied with. We issue a verdict of GO to proceed to the code implementation phase.

================================================================================

