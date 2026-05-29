```markdown
---
name: order-66-optimized
description: Token-minimized structural setup and lazy subagent environment instantiation.
---

## Step 1: Parse TASK.md Scope

Read `TASK.md` to extract `{SPEC_NAME}` and target packages.

---

## Step 2: Establish Staging Workspace Templates

1. Write `./docs/spec/drafts/LC-{SPEC_NAME}-draft.md` with the standard 6-section Launch Criteria layout.
2. Write `./docs/spec/drafts/{SPEC_NAME}-audit.md` with the Historical Audit Log timeline.

---

## Step 2.5: Seed Draft Files

Update `./docs/spec/drafts/LC-{SPEC_NAME}-draft.md`:

```markdown
# {SPEC_NAME} Launch Criteria Specification

## Executive Summary
[Concise plain-English summary parsed from TASK.md]

## [ARL] Architectural Requirements
- [ ]

## [CCL] Concurrency & Timing Requirements
- [ ]

## [SIL] Safety & Forensic Requirements
- [ ]

## [DPL] Data Persistence Requirements
- [ ]

## [DSL] Data Synchronization Requirements
- [ ]
```

Update ./docs/spec/drafts/{SPEC_NAME}-audit.md:

```markdown
# {SPEC_NAME} Historical Audit Logs

Chronological record of evaluations, debates, and overrides.

## Timeline
```

---

## Step 3: Register Self-Configuring Subagents

Define the 5 subagent types (`arl`, `ccl`, `sil`, `dpl`, `dsl`).

Instruct each to read its configuration dynamically:

- `arl`  
  Prompt="You are the Architectural Lead. Read and adhere to system prompt configurations at `./.agents/agents/arl/agent.json`."

- `ccl`  
  Prompt="You are the Concurrency Control Lead. Read and adhere to system prompt configurations at `./.agents/agents/ccl/agent.json`."

- `sil`  
  Prompt="You are the System Integrity Lead. Read and adhere to system prompt configurations at `./.agents/agents/sil/agent.json`."

- `dpl`  
  Prompt="You are the Data Persistence Lead. Read and adhere to system prompt configurations at `./.agents/agents/dpl/agent.json`."

- `dsl`  
  Prompt="You are the Data Synchronization Lead. Read and adhere to system prompt configurations at `./.agents/agents/dsl/agent.json`."

---

## Step 4: Lazy Session Instantiation

Invoke all 5 subagents in a single batch.

Command them to:
- lock their scope to the parsed packages
- remain absolutely lazy
- perform no analysis or directory crawling
- suspend immediately
- respond with a 1-sentence confirmation

---

## Step 5: Yield Control and Finalize output



When all 5 subagents confirm they are suspended and ready, output ONLY the following then suspend and await further instruction:


```markdown

---

# I pass you control of my Minions.
# We are ready for your plans...
# Use `/ping [name]` to commune.

---

```
