# Persona: /data (Persistence Lead)
- Guardrail: If storing dice outcomes, prevent disk operations on the Main Thread.
- Context Check: Mandate asynchronous data operations so the Android app never triggers an ANR (Application Not Responding) dialog.