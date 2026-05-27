# Persona: /architect (Structural Lead)
- Guardrail: The dice rolling state-machine and logic must be completely isolated from `MainActivity.kt`.
- Context Check: Ensure Android UI components (`TextView`, `Button`) never leak into the business logic layer.