<p align="center">
  <img src="composeApp/src/androidMain/res/mipmap-xxxhdpi/ic_launcher.png" width="96" alt="Bolão da Galera icon" />
</p>

<h1 align="center">Bolão da Galera</h1>

<p align="center">
  Crie e jogue bolões de futebol (Brasileirão, Libertadores) com seus amigos: previsões de placar, ranking automático, convites e notificações — tudo em tempo real.
</p>

<p align="center">
  <a href="https://github.com/paulo-richa/BolaoDaGalera/actions/workflows/develop.yml"><img src="https://github.com/paulo-richa/BolaoDaGalera/actions/workflows/develop.yml/badge.svg" alt="Develop CI"></a>
  <a href="https://github.com/paulo-richa/BolaoDaGalera/actions/workflows/release.yml"><img src="https://github.com/paulo-richa/BolaoDaGalera/actions/workflows/release.yml/badge.svg" alt="Release CI"></a>
</p>

## O que o app faz

- Criação e administração de bolões (grupos de apostas) por competição (Brasileirão, Libertadores).
- Convite e aprovação de participantes, por link ou identificador.
- Palpites de placar por partida, com pontuação automática calculada nas Cloud Functions.
- Ranking em tempo real por bolão, com desempate e histórico de acertos.
- Notificações push (lembrete de partida, resumo de rodada, digest diário) e modo de manutenção controlado por Remote Config.
- Anúncios (AdMob) e autenticação por e-mail/senha.

## Stack técnica

**App (Kotlin Multiplatform + Compose Multiplatform)**
- Kotlin, Jetpack Compose / Compose Multiplatform (Android ativo; alvos iOS compilam, mas o app hoje é distribuído só para Android)
- Koin (injeção de dependência), Coroutines + Flow
- Firebase: Auth, Firestore, Remote Config, Crashlytics, Performance Monitoring, Analytics, Cloud Messaging, App Distribution
- Observabilidade própria: `ErrorReporter` centralizado, catálogos de eventos/traces (`AnalyticsEvents`/`PerformanceTraces`), trace por tela
- Baseline Profiles para startup, Compose compiler metrics/stability config
- Qualidade: ktlint, detekt (incluindo regras customizadas em `:detekt-rules`, como a que barra strings hardcoded e imports diretos de `material3` nas telas), Android Lint, Kover (cobertura)

**Backend**
- Cloud Functions (Node.js) para cálculo de pontuação, lembretes, resumos de rodada e sincronização de dados de campeonato

## Arquitetura

Projeto modularizado por camada e por feature:

```
composeApp/       — entry point Android, navegação, DI
designsystem/      — todos os componentes de UI (nenhuma tela importa material3 diretamente)
core-common/       — contratos de observabilidade e tratamento de erro
core-data/         — repositórios e acesso ao Firebase
core-testing/      — fakes compartilhados para testes
feature-auth/      — autenticação e perfil
feature-bolao/     — bolões, palpites, ranking
feature-core/      — home, convites, notificações, ajuda
detekt-rules/       — regras de lint customizadas do projeto
baselineprofile/   — geração de Baseline Profile
functions/          — Cloud Functions (Node.js)
```

## CI/CD

GitFlow completo rodando no GitHub Actions, com `main` protegida (só aceita merge vindo de uma branch `release/x.y.z`, nunca push direto):

| Etapa | O que valida/faz |
|---|---|
| Push na `develop` | ktlint, detekt, Android Lint, testes unitários + cobertura, build debug |
| Push numa `release/x.y.z` | tudo acima + build assinado, testes instrumentados em emulador, CodeQL, Gitleaks, e deploy automático no Firebase App Distribution |
| PR `release/x.y.z → main` | reaproveita os checks já validados; bloqueia qualquer origem que não seja uma branch de release |
| Publicação manual | build de AAB assinado + publicação no Google Play (Gradle Play Publisher, versionCode auto-incrementado) |
| Merge na `main` | sincronização automática de volta para a `develop` |

Também rodam Dependency Review (bloqueia dependência vulnerável em PRs) e Gitleaks (varredura de segredos) em todo push.

## Rodando localmente

```bash
./gradlew :composeApp:assembleDebug   # build debug Android
./gradlew testDebugUnitTest             # testes unitários (todos os módulos)
./gradlew ktlintFormat detekt detektDesignSystem :composeApp:lintDebug   # qualidade de código
```
