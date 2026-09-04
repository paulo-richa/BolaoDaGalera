---
name: git-workflow
description: Use whenever committing, pushing, or opening a pull request in BolaoDaGalera. Enforces the repo's branch policy — main is protected and only accepts merges from develop.
---

# BolaoDaGalera Git Workflow

`main` is a protected branch (GitHub branch protection, `enforce_admins`
enabled — this blocks direct pushes for everyone, including the repo
owner). It only moves through a pull request from `develop`, and only
after every required check passes. This is not a style preference — a
direct `git push origin <branch>:main` will be **rejected by GitHub**
regardless of who runs it.

## The rule

1. **Day-to-day work happens on `develop`.** Commit and push there by
   default, the same way this project used to push straight to `main`.
2. **Never push directly to `main`.** It will fail. Don't attempt
   `git push` with `main` as the destination, don't force-push to it,
   don't try to bypass protection — there is no bypass.
3. **Promote `develop` to `main` only via pull request, and only when the
   user explicitly asks to release/promote/publish to production.**
   Opening that PR is a deliberate, visible action — treat it like any
   other action with real consequences (see the "Executing actions with
   care" guidance): confirm with the user first unless they've already
   asked for it in this turn.
   ```bash
   gh pr create --base main --head develop --title "..." --body "..."
   ```
   GitHub enforces automatically that the PR's source branch must be
   `develop` (a required check, `require-develop-source`, fails any PR to
   `main` from anywhere else) and that every CI job passes before it can
   merge.
4. **Merging is also a real, visible action** — do it only when the user
   asks, same as the existing "don't commit/push without being asked"
   rule extends naturally to "don't merge without being asked."

## Why this exists

Two pipelines split the validation cost accordingly:

- `.github/workflows/develop.yml` — push to `develop` gets fast feedback:
  ktlint, detekt, Android Lint, unit tests + coverage, debug build.
- `.github/workflows/release.yml` — a PR into `main` gets the full
  validation (same checks plus signed release build, instrumented tests
  on an emulator, CodeQL, Gitleaks) *before* merge; the release-only steps
  (the actual signed build artifact + Firebase App Distribution deploy)
  only run *after* the merge lands on `main` (the `push` trigger, gated
  by `if: github.event_name == 'push'` on the deploy job).

This keeps the working branch fast to iterate on while making sure
nothing reaches `main` — and nothing gets distributed to testers — without
passing the complete gate.
