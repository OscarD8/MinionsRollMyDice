---
name: implement-feature
description: Generates local Android implementation components and runs a sequential cross-domain Go/No-Go audit against the spec.
---
# Workflow: Feature Implementation & Cross-Domain Audit

## Step 1: Core Implementation Draft
Read the immutable contract at `./docs/spec/LAUNCH_CRITERIA.md`.
Apply the structural guidelines from the `architect` rule and generate the Jetpack Compose UI packages, ViewModels, and Room Database entities. Do not write them to disk yet; stage them in active memory.

## Step 2: Sequential Cross-Domain Verification Poll
Pass the staged implementation through your automated ruleset gates. Each domain must evaluate *only* its specific constraints and explicitly output either a "GO" or "NO-GO" signal:
- **Architect Audit:** Verify strict internal/api module isolation and Koin 4.1.1 constructor injection boundaries using annotations where possible. (Must output: `[architect] GO/NO-GO`)
- **Data Audit:** Verify Room schema column checks, single-profile restrictions, and `@Transaction` encapsulation. (Must output: `[data] GO/NO-GO`)
- **Concurrency Audit:** Verify all data hooks execute via `Dispatchers.IO` and animation routines handle configuration shifts safely. (Must output: `[concurrency] GO/NO-GO`)

## Step 3: Flight Director Intervention Gate [PAUSE]
- Consolidate the domain poll outputs into a unified terminal overview.
- If any domain lead issues a "NO-GO", halt execution, print the exact failure mode, and await human instructions.
- If all domains successfully issue a "GO", pause and await the user's explicit verification check.

## Step 4: Local Source Tree Commit
Write the completely verified and audited Kotlin classes directly to your project compilation tree paths.