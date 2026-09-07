---
name: ci-troubleshooting
description: Use sempre que for depurar uma execução do GitHub Actions falhando ou instável no BolaoDaGalera (release.yml, develop.yml, dependency-review.yml), ou pra checar o status de uma execução. Reúne padrões de falha conhecidos e a forma econômica de puxar logs sem lotar a conversa.
---

# Troubleshooting de CI do BolaoDaGalera

## Checando status de execução sem gastar contexto à toa

`gh run watch` às vezes sai com código 0 no meio da execução depois de um
erro de rede transitório (ex: "read: connection reset by peer" ao buscar
annotations) — parece que terminou, mas não terminou. Sempre confirme o
estado real antes de reportar qualquer coisa ao usuário:

```bash
gh run view <run-id> --repo paulo-richa/BolaoDaGalera --json status,conclusion,jobs \
  -q '.status, .conclusion, (.jobs[] | "\(.name): \(.status) \(.conclusion)")'
```

Pra puxar só o motivo da falha do log de um job em vez do log completo
(muitas vezes 100KB+):

```bash
gh run view <run-id> --repo paulo-richa/BolaoDaGalera --log --job=<job-id> \
  | grep -iE "error|failure|exception|BUILD FAILED" | grep -v "linkOnly"
```

Prefira isso a ler o log completo — o log completo é majoritariamente
ruído de configuração do Gradle/AGP (avisos de depreciação, notas de
`linkOnly` de framework).

## Padrões de falha conhecidos

- **`assembleRelease` falha com `KeytoolException: No key with alias`** —
  o secret `ANDROID_KEY_ALIAS` não bate com nenhum alias que exista de
  verdade no keystore. Confirme com `keytool -list -v -keystore <path>`
  localmente (quem tiver a senha do keystore roda isso), depois atualize
  o secret com
  `printf '%s' 'alias' | gh secret set ANDROID_KEY_ALIAS` — `printf`
  evita corrupção por espaço/quebra de linha no final, que um prompt
  interativo do `gh secret set` pode introduzir.
- **`appDistributionUploadRelease` falha com "Could not find an APK file
  for this variant"** — cada job do GitHub Actions roda na sua própria
  VM; o job `deploy-firebase` não herda o APK que o `build-release`
  gerou. Precisa fazer `actions/download-artifact` do artifact
  `app-release` primeiro.
- **CodeQL falha com "no source code seen during build" / "could not
  process any of it"** — o Gradle marcou as tasks de compilação como
  `UP-TO-DATE` a partir do cache (via cache restaurado pelo
  `gradle/actions/setup-gradle`), então o tracer do CodeQL nunca observou
  uma compilação de verdade. O passo de build no job do CodeQL precisa de
  `--rerun-tasks` pra forçar a compilação de verdade.
- **Kover (`koverXmlReport`/`koverHtmlReport`, sem sufixo de variante)
  falha com erro de resolução de dependência, ou roda silenciosamente os
  testes da variante errada** — este projeto só exercita a variante
  Android `debug` com testes unitários (a `release` nunca rodou e não
  está preparada pra isso). Use as tasks com sufixo de variante:
  `koverXmlReportDebug`, `koverHtmlReportDebug`.
- **Um plugin de Kover/cobertura recém-aplicado só no projeto raiz falha
  ao resolver dependências de módulo (`Could not resolve project :x`)** —
  o plugin precisa ser aplicado em cada módulo sendo agregado, não só na
  raiz; a raiz só declara as dependências `kover(project(":x"))` e
  (opcionalmente) a config do relatório.

## Editando arquivos de workflow

Depois de qualquer mudança em `.github/workflows/*.yml`, valide que o
YAML realmente roda antes de dizer ao usuário que terminou — ou espere o
próximo gatilho natural (um push) ou dispare um explicitamente:

```bash
gh run rerun <run-id> --repo paulo-richa/BolaoDaGalera --failed   # tenta de novo só os jobs que falharam
gh run rerun <run-id> --repo paulo-richa/BolaoDaGalera            # tenta de novo a execução inteira
```

Como a `main` é protegida (ver o skill `git-workflow`), o gatilho
`push: [main]` do release.yml só dispara depois de um merge de PR de
verdade — validação de mudanças no dia a dia acontece via develop.yml no
push pra `develop`, ou via o gatilho `pull_request` quando um PR
`develop → main` está aberto.
