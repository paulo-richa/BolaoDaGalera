# BolaoDaGalera

App Kotlin Multiplatform (Compose Multiplatform) — Android é o alvo
ativo; os alvos iOS compilam mas o app hoje é distribuído só pra Android.
Pacote `com.lpstudio.bolaodagalera`, projeto Firebase `bolaodagalera-bb002`.

## Módulos

- `composeApp` — ponto de entrada do app Android, navegação, wiring de DI (Koin).
- `designsystem` — todo primitivo de UI que telas podem usar (ver regra abaixo).
- `core-common` — contratos multiplataforma: observabilidade
  (`PerformanceMonitor`, `AnalyticsTracker`, `CrashReporter`, catálogos
  `AnalyticsEvents`/`PerformanceTraces`), tratamento de erro (`ErrorReporter`).
- `core-data` — repositórios, acesso ao Firebase (Auth/Firestore/RemoteConfig).
- `core-testing` — fakes compartilhados (`FakeAuthRepository`, etc.) usados
  pelos testes de todo módulo — mantenha os dados de fixture fictícios,
  nunca dados reais de pessoas.
- `feature-auth`, `feature-bolao`, `feature-core` — módulos de feature
  (telas + ViewModels), cada um KMP com `commonMain`/`commonTest`.
- `detekt-rules` — regras de detekt customizadas deste projeto (ex: a
  checagem de string hardcoded em componentes Bolao*).
- `baselineprofile` — teste instrumentado que gera o Baseline Profile
  (`androidx.baselineprofile`), não é uma lib que outros módulos dependem.
- `functions` — Cloud Functions em Node.js (separado do app Kotlin; não
  coberto pela configuração de Gradle/CI abaixo).

## Comandos do dia a dia

```bash
./gradlew ktlintFormat                                   # auto-corrige formatação
./gradlew detekt detektDesignSystem                       # análise estática (repo inteiro)
./gradlew testDebugUnitTest                                # testes unitários, todos os módulos
./gradlew :composeApp:lintDebug                            # Android Lint
./gradlew :composeApp:assembleDebug                        # APK debug
```

Um pre-commit hook local (`.git/hooks/pre-commit`, não versionado — cada
clone precisa configurar manualmente) roda ktlint + detekt +
detektDesignSystem + lint antes de todo commit, silenciosamente (só mostra
output se algo falhar).

## Regras fixas (ver `.claude/skills/` pras versões completas)

- **Só inglês** em código, identificadores e comentários/KDoc — incluindo
  português remanescente em código antigo, conforme for tocado. Strings
  visíveis ao usuário (`strings.xml`) ficam em pt-BR — o público do app é
  brasileiro. Documentação meta (`CLAUDE.md`, `SKILL.md`) fica em
  português, já que é pra navegação/entendimento do projeto, não código
  que roda.
- **Nenhuma tela importa `androidx.compose.material3` diretamente**, e
  **nenhuma string hardcoded visível ao usuário** — ver o skill
  `design-system`.
- **A `main` é uma branch protegida** — o trabalho acontece na `develop`;
  releases são estabilizadas numa branch `release/x.y.z` (o nome vira o
  `versionName` automaticamente); a `main` só se move via PR
  `release/x.y.z → main`, aberto só quando pedido explicitamente. Ver o
  skill `git-workflow`.
- Não commite/dê push, e não mergeie/promova pra `main`, sem um pedido
  explícito no turno atual — uma aprovação anterior não vale pra sempre.
- Nenhuma imagem de snapshot do Roborazzi é versionada neste repo.

## CI (GitHub Actions) — GitFlow completo, ver skill `git-workflow`

- `.github/workflows/develop.yml` — push na `develop`: ktlint, detekt,
  Android Lint, testes unitários + cobertura (Kover), build debug.
  Feedback rápido.
- `.github/workflows/release-candidate.yml` — push numa `release/x.y.z`:
  validação completa + deploy automático no Firebase App Distribution pro
  usuário testar.
- `.github/workflows/release.yml` — PR de `release/x.y.z` pra `main`: a
  mesma validação completa, controlada por um check
  `require-release-source` que falha qualquer PR que não venha de
  `release/*`. Depois que o PR mergeia (`push` na `main`), também faz
  deploy do build assinado no Firebase App Distribution.
- `.github/workflows/promote-play-store.yml` — manual
  (`workflow_dispatch`, rodado a partir de uma `release/x.y.z`): builda
  AAB assinado, publica na track de teste fechado do Play Console.
  `versionName` vem do nome da branch; `versionCode` é auto-incrementado
  pelo Gradle Play Publisher a partir do que já está no Play Console.
- `.github/workflows/promote-main.yml` — manual (`workflow_dispatch`,
  rodado a partir de uma `release/x.y.z`): abre o PR pra `main` e liga
  auto-merge.
- `.github/workflows/sync-main-to-develop.yml` — reativo, push na `main`:
  mergeia `main` de volta pra `develop` automaticamente.
- `.github/workflows/dependency-review.yml` — só em PR, bloqueia
  dependência nova vulnerável.
- Credenciais de assinatura de release, Firebase App Distribution e Play
  Console vêm de secrets do GitHub Actions (`ANDROID_KEYSTORE_BASE64`,
  `PLAY_SERVICE_ACCOUNT_BASE64`, `PLAY_TRACK` etc.) — ver
  `composeApp/build.gradle.kts` pros nomes exatos das env vars. Builds
  locais ficam sem assinatura/assinados com debug quando essas env vars
  não estão setadas.

## Trabalhando de forma econômica neste repo

- O output do Gradle é verboso por padrão — prefira `--console=plain -q`
  e grep por `FAILURE`/`error` em vez de despejar o log inteiro na
  conversa.
- Pra investigação barulhenta (ler logs grandes, greps amplos pelo repo,
  acompanhar uma execução de CI até o fim), prefira um subagente forkado
  pra o output bruto não encher o contexto da conversa principal.
- `gh run watch` às vezes cai no meio da execução por instabilidade de
  rede (sai com código 0 sem a execução ter realmente terminado) —
  confirme com `gh run view <id> --json status,conclusion` antes de
  confiar no output dele.
