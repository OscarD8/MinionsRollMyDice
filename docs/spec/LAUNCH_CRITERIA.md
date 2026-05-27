# Launch Criteria: Decoupled Android Dice Roller (with Character Attributes)

This document establishes the architecture, clean-data schemas, design patterns, and safety checklists for implementing the **D&D Character Attributes & Dice Roller** system in **MinionsRollMyDice**.

---

## 1. Core Features & Business Rules

### A. Character Attributes (The Character Sheet)
* **Metrics**: The user profile contains three distinct attributes:
  1. `Wisdom` (0 to 5)
  2. `Insight` (0 to 5)
  3. `Inspired` (0 to 5)
* **Point Allocation Rule**: The user has a maximum of **5 attribute points** to allocate across all three metrics (i.e., $\text{Wisdom} + \text{Insight} + \text{Inspired} \le 5$).
* **Character Sheet Screen**: A separate screen with its own lifecycle-aware `CharacterSheetViewModel` that manages the point allocation UI and enforces validation in real-time.

### B. Dice Roll Modifier Impact
* The persisted character attributes directly affect dice rolls.
* **Modifier Formula**: The total result is calculated as:
  $$\text{Final Roll} = \text{Base Roll} + \text{Wisdom} + \text{Insight} + \text{Inspired}$$
  *(Or custom combinations based on selection).*

---

## 2. Clean Architecture & UDF (Unidirectional Data Flow)

To enforce strict separation of concerns, the application is divided into three distinct layers:

```
[ UI Layer (Compose Screens) ]
      ▲                     │
      │ StateFlow           ▼ Event / Intent
[ ViewModels (UDF State) ]
      ▲                     │
      │ Flow / Repositories ▼ Save / Dispatch
[ Domain Layer (Models & Rules) ]
      ▲                     │
      │ Entity Mapper       ▼ Insert / Update
[ Data Layer (Room DB / Entities) ]
```

### A. Layer Division & Separation

#### 1. Data Layer (Room Entity)
The database representation uses a flat structural model with validation annotations:
* **Entity**: `CharacterAttributesEntity`
* Contains database-specific fields.

#### 2. Domain Layer (Domain Model)
A pure Kotlin data class representation that contains business validations and logic:
* **Domain Model**: `CharacterAttributes`
* Contains pure functions to validate point allocations (e.g., `isValid()`, `remainingPoints()`, `canIncrement()`).
* **Independence**: Strictly contains no references to Room annotations, Android libraries, or View components.

#### 3. UI Layer (State & ViewModels)
Uses strict UDF where state flows down and events flow up:
* **`CharacterSheetViewModel`**: Exposes `CharacterSheetUiState` (wisdom, insight, inspired, remaining points, validation state). Handles events like `IncrementAttribute(AttributeType)`, `DecrementAttribute(AttributeType)`, and `SaveAttributes`.
* **`DiceRollerViewModel`**: Observes the `CharacterAttributes` domain model and applies modifiers to `DiceRollerUiState` (base roll, applied modifiers, final total).

---

## 3. Database Schema

### Table: `character_attributes`

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `INTEGER` | `PRIMARY KEY` (Always `1` for single-user profile) | Ensures a single profile row is maintained. |
| `wisdom` | `INTEGER` | `DEFAULT 0`, `CHECK(wisdom BETWEEN 0 AND 5)` | Wisdom points allocated. |
| `insight` | `INTEGER` | `DEFAULT 0`, `CHECK(insight BETWEEN 0 AND 5)` | Insight points allocated. |
| `inspired` | `INTEGER` | `DEFAULT 0`, `CHECK(inspired BETWEEN 0 AND 5)` | Inspired points allocated. |

### Room DAO Interface Spec
```kotlin
@Dao
interface CharacterAttributesDao {
    @Query("SELECT * FROM character_attributes WHERE id = 1 LIMIT 1")
    fun observeAttributes(): Flow<CharacterAttributesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAttributes(entity: CharacterAttributesEntity)
}
```

---

## 4. Concurrency & Threading Strategy (Rules: `concurrency` & `data`)

1. **Non-Blocking Persistence**: All write operations in the repository (`saveAttributes`) must switch context to `Dispatchers.IO` using Kotlin coroutines.
2. **Main-Thread Protection**:
   * Reading of attributes is exposed as a cold asynchronous `Flow` observed inside ViewModels.
   * Direct database queries or disk transactions are strictly prohibited on `Dispatchers.Main`.
3. **UDF Lifecycle Safety**:
   * Flows are collected in the UI using Compose lifecycle-aware APIs such as `collectAsStateWithLifecycle()` to prevent background flow collection leaks.

---

## 5. Launch Criteria Safety Checklist

Before production release, the implementation must satisfy the following checks:

- [ ] **Architecture Check**: UI components and Android frameworks do not import or reference domain classes from the state machine directly.
- [ ] **Point Allocation Validation Check**: The UI strictly prevents allocating more than 5 total points, and boundaries [0, 5] are mathematically locked at the domain model level.
- [ ] **Thread Boundaries Check**: No raw threading or block-on-UI disk read/writes exist in the codebase.
- [ ] **Rotation Safety Check**: Rotating the screen during an active rolling animation or when editing points does not crash, leak memory, or reset state.
- [ ] **ANR Safe Check**: StrictMode or profile audits confirm zero main-thread disk footprint.
- [ ] **Schema Correctness Check**: Room migration tests or unit tests verify check constraints (`BETWEEN 0 AND 5`) and total sum constraint.
