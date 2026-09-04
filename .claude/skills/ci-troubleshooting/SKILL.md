---
name: ci-troubleshooting
description: Use whenever debugging a failing or flaky GitHub Actions run in BolaoDaGalera (release.yml, develop.yml, dependency-review.yml), or when checking on a run's status. Captures known failure patterns and the efficient way to fetch logs without flooding the conversation.
---

# BolaoDaGalera CI Troubleshooting

## Checking run status without wasting context

`gh run watch` sometimes exits 0 mid-run after a transient network error
(e.g. "read: connection reset by peer" while fetching annotations) — this
looks like completion but isn't. Always confirm the real state before
reporting anything to the user:

```bash
gh run view <run-id> --repo paulo-richa/BolaoDaGalera --json status,conclusion,jobs \
  -q '.status, .conclusion, (.jobs[] | "\(.name): \(.status) \(.conclusion)")'
```

To pull just the failure reason from a job's log instead of the full
(often 100KB+) output:

```bash
gh run view <run-id> --repo paulo-richa/BolaoDaGalera --log --job=<job-id> \
  | grep -iE "error|failure|exception|BUILD FAILED" | grep -v "linkOnly"
```

Prefer this over reading the full log — the full log is mostly Gradle/AGP
configuration noise (deprecation warnings, `linkOnly` framework notices).

## Known failure patterns

- **`assembleRelease` fails with `KeytoolException: No key with alias`** —
  the `ANDROID_KEY_ALIAS` secret doesn't match an alias actually in the
  keystore. Verify with `keytool -list -v -keystore <path>` locally
  (whoever holds the keystore password runs this), then reset the secret
  with `printf '%s' 'alias' | gh secret set ANDROID_KEY_ALIAS` — `printf`
  avoids trailing-newline/whitespace corruption that an interactive
  `gh secret set` prompt can introduce.
- **`appDistributionUploadRelease` fails with "Could not find an APK file
  for this variant"** — each GitHub Actions job runs on its own VM; the
  `deploy-firebase` job doesn't inherit the APK that `build-release` built.
  It must `actions/download-artifact` the `app-release` artifact first.
- **CodeQL fails with "no source code seen during build" /
  "could not process any of it"** — Gradle marked the compile tasks
  `UP-TO-DATE` from cache (via `gradle/actions/setup-gradle`'s restored
  cache), so CodeQL's tracer never observed a real compilation. The build
  step in the CodeQL job needs `--rerun-tasks` to force actual compilation.
- **Kover (`koverXmlReport`/`koverHtmlReport`, no variant suffix) fails
  with dependency resolution errors, or silently runs the wrong variant's
  tests** — this project only exercises the `debug` Android variant with
  unit tests (`release` has never been run and isn't set up for it). Use
  the variant-suffixed tasks: `koverXmlReportDebug`, `koverHtmlReportDebug`.
- **A brand-new Kover/coverage plugin only applied at the root project
  fails to resolve module dependencies (`Could not resolve project :x`)**
  — the plugin needs to be applied in every module being aggregated, not
  just the root; the root only declares the `kover(project(":x"))`
  dependencies and (optionally) report config.

## Editing workflow files

After any `.github/workflows/*.yml` change, validate the YAML actually
runs before telling the user it's done — either wait for the next natural
trigger (a push) or explicitly trigger one:

```bash
gh run rerun <run-id> --repo paulo-richa/BolaoDaGalera --failed   # retry only failed jobs
gh run rerun <run-id> --repo paulo-richa/BolaoDaGalera            # retry the whole run
```

Since `main` is protected (see the `git-workflow` skill), release.yml's
`push: [main]` trigger only fires after a real PR merge — day-to-day
workflow edits get validated via develop.yml on push to `develop`, or via
the `pull_request` trigger when a `develop → main` PR is open.
