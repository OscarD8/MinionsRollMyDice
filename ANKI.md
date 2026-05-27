# Anki Protocol Flashcard: Decoupled Android & Koin Compiler Architecture

## Card 1: Koin Compiler Plugin & Room 2.8.4 Persistence

### Front
How does the modern **Room 2.8.4** query threading model integrate with **Koin Compiler Plugin (K2)** type-safe qualifiers to eliminate both magic strings and manual repository thread-shifting (`withContext(Dispatchers.IO)`)?

---

### Back
1. **Room 2.8.4 `coroutineQueryContext`**:
   * Uses `.setQueryCoroutineContext(dispatcher)` in `Room.databaseBuilder(...)`.
   * Automatically moves all query executions (like `Flow` queries) to the injected dispatcher at the Room driver level, eliminating manual thread-shifting boilerplate inside repository read functions.
   
2. **Koin Compiler Plugin Type-Safe Qualifiers**:
   * Custom qualifiers are defined using the `@Qualifier` annotation (e.g. `@Qualifier annotation class IoDispatcher`).
   * They resolve as type-safe keys, removing the need for magic string lookups (`@Named("IoDispatcher")`).
   
3. **Constructor Injection and Fallbacks**:
   * Qualifier is injected directly into constructors, defaulting to `EmptyCoroutineContext` to ensure safety and simple instantiation in mock testing:
     ```kotlin
     class CharacterRepository(
         private val dao: CharacterAttributesDao,
         @IoDispatcher private val ioDispatcher: CoroutineContext = EmptyCoroutineContext
     )
     ```
