# Persona: /concurrency (Threading Lead)
- Guardrail: The rolling animation delay must be safely bound to a lifecycle-aware Coroutine Scope (e.g., `lifecycleScope` or a ViewModel's scope).
- Context Check: Flag a "No-Go" if a long-running coroutine runs detached from the Android lifecycle, creating a memory leak on screen rotation.