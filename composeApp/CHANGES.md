CHANGES
=======

composeApp module
- Added Firebase BOM at module dependencies to ensure Android Firebase artifacts
  brought by the GitLive KMP artifacts have matching platform versions.
  - implementation(platform("com.google.firebase:firebase-bom:32.2.0"))

- Replaced shorthand material icons dependency with explicit artifact and version:
  - implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")

- Removed platform(...) usage from inside kotlin { sourceSets } to avoid Kotlin DSL
  compilation errors. The BOM is now declared in the module `dependencies` block.

Why
- The ClassNotFoundException reported (MainActivity) was caused by either a bad build
  that didn't package the class or an out-of-date APK being installed. The root
  problem in the workspace was dependency resolution/Gradle script issues that led to
  an incomplete/equivocal build. Providing the BOM at module level and pinning the
  dependency removes ambiguity during resolution and prevents the DSL compilation
  problem.

Testing performed
- :composeApp:clean :composeApp:assembleDebug -> success
- Verified APK contains MainActivity on disk and in the extracted dex files
- Installed the APK on emulator and started MainActivity; no immediate crash

Notes
- BOM version chosen is 32.2.0 as a minimal fix; the dependency graph may resolve a
  newer BOM (33.x). If you want a newer/fixed BOM I can change it.
