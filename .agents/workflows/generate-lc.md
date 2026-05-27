---
name: generate-lc
description: Generates an Android Launch Criteria checklist for the Dice Roller Activity.
---
# Workflow: Android Dice Roller Spec

## Step 1: Architectural Base Draft
Apply the structural guidelines from the `architect` rule. Analyze the intent to build a decoupled dice roller inside an empty Android Activity, outputting to `LC_DRAFT.md`.

## Step 2: Android Domain Cross-Audit
Evaluate `LC_DRAFT.md` against our active project rules:
- Audit thread boundaries and screen rotation safety using the `concurrency` rule.
- Audit history serialization safety using the `data` rule.

## Step 3: Flight Director Approval Gate [PAUSE]
Halt execution and present the mobile-safety checklist. Wait for the user to type `/approve`.