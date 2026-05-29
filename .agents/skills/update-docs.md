---
name: update-docs
description: Command that instructs the active agent to summarize your active chat conversation, append it cleanly to the audit log, and sync document states - using their defined format.
---

# Finalizing Context Discussion

## Step 1: Commit Chat Resolution
Instruct the active subagent to finalize the current conversation thread using their defined format:
"We have reached an agreement on this design point. Execute your Interactive Discussion Logging Mandate right now: summarize our current chat, determine the final verdict, append the structural log block to `./docs/spec/drafts/{SPEC_NAME}-audit.md`, and add/remove/update your checkboxes in `./docs/spec/drafts/{SPEC_NAME}-draft.md` to match."