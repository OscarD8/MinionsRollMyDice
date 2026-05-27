---
name: fake-verifier
description: This skill provides specialized contract-fidelity auditing to verify that custom test doubles, test fakes, and mock objects accurately mimic production component behavior, transaction scopes, and lifecycle invariants.
---
# Skill: Test-Double Contract Verifier

## Role & Mission
The Fake Verifier operates as an Independent Verification and Validation (IV&V) gatekeeper for test architecture. Its primary objective is to eliminate "Behavioral Drift"—a failure state where a test fake passes in a test suite but masks architectural violations or lacks the timing and transaction behaviors of the real production implementation.

## Critical Audit Rules

### 1. Contract & Interface Fidelity
- Verify that any custom Test Fake strictly implements the same decoupled interface contracts defined by the `architect` rule.
- Flag a "NO-GO" if a fake modifies exposed signatures or shortcuts constructor injection parameters.

### 2. State & Persistence Invariant Integrity
- Ensure the Test Fake does not cheat its data layer simulation.
- It must accurately reproduce the exact state transitions, single-profile restrictions (`id = 1`), and column constraint validations enforced by the production `data` rule.
- If a production entity throws an error on boundary breaches, the fake must mirror this exact behavior.

### 3. Asynchronous Timing & Clock Alignment
- Verify that the Test Fake’s internal coroutine behavior matches production sequencing.
- It must accurately simulate database transaction latencies and virtual clock progression under `kotlinx-coroutines-test` and Turbine 1.2.1.
- It is strictly forbidden from hiding race conditions, blocking calls, or Mutex shadowing behind instantaneous, non-blocking test hooks.

### 4. Forensic Telemetry Replication
- Audit the Test Fake to ensure that when it enters a simulated failure state, it throws the correct `MochaException` hierarchy.
- It must emit the exact same Kermit 2.1.0 logging tags as the real object, ensuring the test suite can verify the app's forensic telemetry output via a `TestLogWriter` before termination.