---
name: generate-tests
description: Blueprints the test vectors and uses the fake-verifier skill to audit test doubles against behavioral drift.
---
# Workflow: Forensic Test Automation

## Step 1: Test Vector Design
Read the newly committed implementation code and `./docs/spec/LAUNCH_CRITERIA.md`. Map out a complete testing matrix targeting the 0-5 point boundary constraints and potential race conditions.
Also use the new Koin compiler functionality that has shifted away from KSP, to use test modules and dynamic binding with Koin DSL to all test scopes via Koin through fetching from a TestScope the Dispatcher using an extension function on the test scope with CoroutineDispatcher key lookup to fetch the CoroutineContext element. 

## Step 2: Test Double Fidelity Audit
Invoke the `fake-verifier` skill to audit your proposed testing fakes if you have decided fakes are necessary.
Directive: Ensure custom fakes mimic real production behavior, thread-blocking latencies, and transaction boundaries perfectly without masking production edge-case bugs.

## Step 3: Asynchronous Clock Simulation
Apply the `concurrency` rule to construct active verification suites using `kotlinx-coroutines-test` and Turbine 1.2.1 flow tracking to safely evaluate the dice roll animation timings.

## Step 4: Mission Director Review [PAUSE]
Halt execution. Present the complete test suite to the user for manual validation. Do not write to disk until authorized.

## Step 5: Test Insertion & Anki Export
Write the verified tests to the workspace test directories. Conclude the workflow by generating the final structural Anki Protocol flashcard directly to disk.