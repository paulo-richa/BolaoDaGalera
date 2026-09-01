---
name: design-system
description: Use whenever touching UI/Compose code in BolaoDaGalera — creating, editing, or reviewing any screen, component, theme token, or string. Enforces the app's design-system rules (:designsystem module encapsulation, zero material3 imports in screens, zero hardcoded strings).
---

# BolaoDaGalera Design System

This app is being modularized into a dedicated `:designsystem` Gradle module
(KMP: `commonMain`/`androidMain`/`iosMain`). The rules below are not
optional style preferences — they are the actual architecture contract for
this codebase. Apply them any time you write or touch a `.kt` file under
`presentation/` (or the design-system module itself), whether adding a new
screen, editing an existing one, extracting a component, or reviewing a
diff.

## The two hard rules

1. **No screen imports `androidx.compose.material3` directly. Not even
   `Text`.** Every visual element — text, icons, buttons, dialogs, app
   bars, scaffolds, snackbars, dividers, dropdowns, progress indicators,
   text fields, switches, radio buttons, surfaces — goes through a
   `:designsystem` wrapper composable (`BolaoText`, `BolaoIcon`,
   `BolaoButton`, `BolaoScaffold`, etc). If a screen needs a Material 3
   primitive that has no wrapper yet, the fix is to **add the wrapper to
   `:designsystem`**, never to import material3 into the screen.
2. **No hardcoded user-facing string literals anywhere.** Every string a
   user reads (labels, button text, dialog titles/messages, placeholders,
   error messages, snackbar text, content descriptions) comes from
   `stringResource(Res.string.xxx)`, backed by
   `composeApp/src/commonMain/composeResources/values/strings.xml`.

Both rules apply retroactively — when you touch a screen for an unrelated
reason and notice it still imports material3 directly or has literal
strings, that is in scope to fix as part of the change, not a separate
task to defer.

## Module layout

```
designsystem/src/commonMain/kotlin/com/lpstudio/bolaodagalera/designsystem/
  theme/
    Color.kt       — all named Color vals + gradients (single source of truth)
    Typography.kt  — BolaoTypography (Material 3 type scale)
    Shape.kt       — BolaoShapes (Material 3 shape scale)
    Theme.kt       — BolaoTheme(content) = MaterialTheme(colorScheme, typography, shapes)
  components/
    Text.kt, Icon.kt, Button.kt, TextField.kt, Card.kt, Chip.kt,
    Dialog.kt, AppBar.kt, Scaffold.kt, Snackbar.kt, Surface.kt,
    RadioButton.kt, Switch.kt, Divider.kt, DropdownMenu.kt,
    LoadingIndicator.kt, EmptyState.kt, Avatar.kt
    — one file per component family, each composable prefixed `Bolao`
```

`composeApp`'s own `presentation/theme/AppTheme.kt` is a thin wrapper: it
re-exports the design-system's colors/gradients (so legacy imports don't
break during migration) and combines `BolaoTheme` with app-specific
concerns that don't belong in a design system (e.g. `SystemAppearance` /
status-bar behavior).

## What "component, not primitive" looks like

Naming convention: `Bolao<Material3Name>`, e.g. `Text` → `BolaoText`,
`IconButton` → `BolaoIconButton`, `AlertDialog` → `BolaoConfirmDialog` (for
the confirm/cancel shape) or `BolaoDialog` (generic, for custom content
that doesn't fit confirm/cancel). Wrapper signatures mirror the underlying
Material 3 composable's parameters as closely as possible — the point is
encapsulation of the *import*, not restricting the API surface.

Structural/layout primitives from `androidx.compose.foundation.layout`
(`Box`, `Column`, `Row`, `Spacer`, `Modifier.padding`, etc.) are **not**
part of this rule — they carry no Material 3 theming and don't need
wrapping. Only `androidx.compose.material3.*` imports are banned from
screens.

`SnackbarHostState` is a state holder, not a themed visual — screens use
`rememberBolaoSnackbarHostState()` (returns a
`typealias BolaoSnackbarHostState = SnackbarHostState`) instead of
`remember { SnackbarHostState() }`, so the `androidx.compose.material3`
import still never appears in the screen.

`@OptIn(ExperimentalMaterial3Api::class)` annotations should disappear
from screens once they no longer call material3 composables directly —
opt-in requirements don't propagate through a stable wrapper that isn't
itself marked experimental. If a screen still needs the annotation after
migration, that's a sign something wasn't fully wrapped.

When a genuinely new visual pattern shows up in 2+ places (not just a
one-off), that's a signal to extract it as a new `:designsystem`
component rather than duplicate it — this already happened with
`BolaoGlassCard` (found identical in Login, Register, and Join screens)
and `BolaoCard` (the 16dp/`NavyCard` pattern repeated across many
screens). Don't force-fit a component onto a visually different instance
just to reduce file count — e.g. a card with a different corner radius or
color is a legitimate variant, not automatically "the same" component;
check for exact parameter parity before reusing vs. extending vs. leaving
alone.

## String resources

Add entries to `composeApp/src/commonMain/composeResources/values/strings.xml`
(Android string-resource XML format). Compose Multiplatform's resource
plugin generates `Res.string.xxx` accessors automatically — never hand-edit
generated code.

- Key convention: `<screen>_<description>`, e.g. `login_title`,
  `login_field_email_label`, `profile_sign_out_dialog_title`.
- Interpolated strings use positional format args:
  `<string name="key">Remover %1$s deste bolão?</string>` called as
  `stringResource(Res.string.key, user.name)`.
- **Do extract**: anything rendered on screen, including emoji used as
  standalone UI content (e.g. "🔑", "🎉") and content descriptions.
- **Do not extract**: internal-only strings that a user never sees —
  analytics event names/keys (`analyticsTracker.logEvent("bolao_created",
  ...)`), crash-reporter context messages
  (`crashReporter.recordException(e, "Erro ao...")`), logger messages
  (`logger.d { "..." }`), Firestore field names, enum-like internal
  identifiers. These are plumbing, not UI text — leave them as plain
  Kotlin string literals.

## Workflow when migrating or building a screen

1. Read the target file(s) fully before editing — note every
   `androidx.compose.material3.*` import and every string literal.
2. Swap each material3 import for its `:designsystem` equivalent from the
   table of existing components (check `designsystem/.../components/`
   first — most primitives already have a wrapper). Extract each
   user-facing string into `strings.xml` and call site to
   `stringResource(...)`.
3. If a needed wrapper doesn't exist yet, add it to `:designsystem` first
   (with a `@Preview`, matching the existing component style), then use it
   — don't improvise a local workaround in the screen.
4. Validate before considering the change done:
   ```
   ./gradlew :designsystem:ktlintFormat :composeApp:ktlintFormat -q
   ./gradlew :designsystem:compileDebugKotlinAndroid :designsystem:compileKotlinIosSimulatorArm64 \
             :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosSimulatorArm64 \
             :composeApp:testDebugUnitTest \
             :designsystem:ktlintCheck :composeApp:ktlintCheck \
             :designsystem:detekt :composeApp:detekt -q
   ```
   Then confirm zero material3 imports remain in the touched files:
   `grep -n "androidx.compose.material3" <files>` should return nothing.
5. Watch for window-inset regressions when swapping `TopAppBar` →
   `BolaoTopBar`: `BolaoTopBar` always zeroes its own top inset (assumes
   the screen's outer container already applies `.systemBarsPadding()`).
   If the original `TopAppBar` had no `windowInsets` override (relying on
   the default), add `.systemBarsPadding()` to the screen's outer `Box`/
   `Scaffold` when migrating, or the content will render under the status
   bar.

## Precedent: components built so far

`BolaoText`, `BolaoIcon`/`BolaoIconButton`, `BolaoButton`/
`BolaoOutlinedButton`/`BolaoTextButton`, `BolaoTextField`, `BolaoCard`/
`BolaoGlassCard`, `BolaoChip`, `BolaoConfirmDialog`/`BolaoDialog`,
`BolaoTopBar`, `BolaoScaffold`, `BolaoSnackbar`/`BolaoSnackbarHost`/
`rememberBolaoSnackbarHostState`, `BolaoSurface`, `BolaoRadioButton`,
`BolaoSwitch`, `BolaoLoadingIndicator`/`BolaoFullScreenLoading`/
`BolaoLinearProgressIndicator`, `BolaoHorizontalDivider`/
`BolaoVerticalDivider`, `BolaoDropdownMenu`/`BolaoDropdownMenuItem`,
`BolaoEmptyState`, `UserAvatar`. Check this list (and the actual files in
`designsystem/.../components/`) before assuming a wrapper doesn't exist.
