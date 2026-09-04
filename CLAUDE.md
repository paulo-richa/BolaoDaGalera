# BolaoDaGalera

Kotlin Multiplatform (Compose Multiplatform) app — Android is the active
target; iOS targets compile but the app currently ships Android-only.
Package `com.lpstudio.bolaodagalera`, Firebase project `bolaodagalera-bb002`.

## Modules

- `composeApp` — the Android app entry point, navigation, DI wiring (Koin).
- `designsystem` — every UI primitive screens are allowed to use (see rule below).
- `core-common` — cross-platform contracts: observability (`PerformanceMonitor`,
  `AnalyticsTracker`, `CrashReporter`, `AnalyticsEvents`/`PerformanceTraces`
  catalogs), error handling (`ErrorReporter`).
- `core-data` — repositories, Firebase (Auth/Firestore/RemoteConfig) access.
- `core-testing` — shared fakes (`FakeAuthRepository`, etc.) used by every
  module's tests — keep fixture data fictional, never real people's info.
- `feature-auth`, `feature-bolao`, `feature-core` — feature modules
  (screens + ViewModels), each KMP with `commonMain`/`commonTest`.
- `detekt-rules` — this project's custom detekt rules (e.g. the
  no-hardcoded-strings-in-Bolao*-components check).
- `baselineprofile` — instrumented test that generates the Baseline Profile
  (`androidx.baselineprofile`), not a library other modules depend on.
- `functions` — Node.js Cloud Functions (separate from the Kotlin app; not
  covered by the Gradle/CI setup below).

## Everyday commands

```bash
./gradlew ktlintFormat                                   # auto-fix formatting
./gradlew detekt detektDesignSystem                       # static analysis (full repo)
./gradlew testDebugUnitTest                                # unit tests, all modules
./gradlew :composeApp:lintDebug                            # Android Lint
./gradlew :composeApp:assembleDebug                        # debug APK
```

A local pre-commit hook (`.git/hooks/pre-commit`, not version-controlled —
each clone needs it set up manually) runs ktlint + detekt + detektDesignSystem
+ lint before every commit, quietly (only prints output on failure).

## Hard rules (see `.claude/skills/` for the full versions)

- **English only** in code, identifiers, and comments/KDoc — including
  existing Portuguese left over in older code, as you touch it. User-facing
  strings (`strings.xml`) stay in pt-BR — the app's users are Brazilian.
- **No screen imports `androidx.compose.material3` directly**, and **no
  hardcoded user-facing strings** — see the `design-system` skill.
- **`main` is a protected branch** — work happens on `develop`; `main` only
  moves via a `develop → main` pull request, opened only when explicitly
  asked. See the `git-workflow` skill.
- Don't commit/push, and don't merge/promote to `main`, without an explicit
  ask in the current turn — a prior approval doesn't carry forward.
- No Roborazzi/screenshot snapshot images get versioned in this repo.

## CI (GitHub Actions)

- `.github/workflows/develop.yml` — push to `develop`: ktlint, detekt,
  Android Lint, unit tests + coverage (Kover), debug build. Fast feedback.
- `.github/workflows/release.yml` — PR into `main`: the same checks plus
  signed release build, instrumented tests (emulator), CodeQL, Gitleaks,
  gated by a `require-develop-source` check that fails any PR not coming
  from `develop`. After the PR merges (`push` to `main`), it also deploys
  the signed build to Firebase App Distribution.
- `.github/workflows/dependency-review.yml` — PR-only, blocks new
  vulnerable dependencies.
- Release signing and Firebase App Distribution credentials come from
  GitHub Actions secrets (`ANDROID_KEYSTORE_BASE64` etc.) — see
  `composeApp/build.gradle.kts` for the exact env var names. Local builds
  stay unsigned/debug-signed when those env vars aren't set.

## Working efficiently in this repo

- Gradle output is verbose by default — prefer `--console=plain -q` and
  grep for `FAILURE`/`error` over dumping full logs into the conversation.
- For noisy investigation (reading through large logs, broad greps across
  the repo, watching a CI run to completion) prefer a forked subagent so
  the raw output doesn't fill the main conversation's context.
- `gh run watch` occasionally drops mid-run on network hiccups (exits 0
  without the run actually finishing) — confirm with
  `gh run view <id> --json status,conclusion` before trusting its output.
