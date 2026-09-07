---
name: git-workflow
description: Use sempre que for commitar, dar push, abrir uma branch de release ou promover pra main/Play Store no BolaoDaGalera. Aplica o GitFlow do repo — main é protegida e só aceita merge vindo de uma branch release/x.y.z.
---

# Fluxo de Git do BolaoDaGalera (GitFlow)

A `main` é uma branch protegida (branch protection do GitHub,
`enforce_admins` ativado — isso bloqueia push direto pra qualquer um,
incluindo o dono do repo). Ela só se move através de um pull request vindo
de uma branch `release/x.y.z`, e só depois que todo check obrigatório
passar. Isso não é preferência de estilo — um `git push origin
<branch>:main` direto vai ser **rejeitado pelo GitHub**, não importa quem
rode.

## As branches

- **`develop`** — trabalho do dia a dia. Commite e dê push aqui por
  padrão.
- **`release/x.y.z`** — cortada da `develop` quando o usuário decide
  estabilizar uma versão pra lançamento. Todo push nela já builda, valida
  tudo e distribui automaticamente pro Firebase App Distribution, pro
  usuário testar antes de promover.
- **`main`** — produção, protegida. Só recebe merge de uma
  `release/x.y.z` via PR.

## O fluxo completo

1. **Cortar a release**: só quando o usuário pedir explicitamente pra
   estabilizar/lançar uma versão.
   ```bash
   git checkout develop && git pull
   git checkout -b release/x.y.z
   git push -u origin release/x.y.z
   ```
   O nome da branch **é** a versão — vira o `versionName` do app
   automaticamente (extraído do nome da branch pelos workflows de
   promoção, não precisa digitar de novo em lugar nenhum).
2. **Push nessa branch** dispara `release-candidate.yml`: validação
   completa (ktlint, detekt, lint, testes, build assinado, testes
   instrumentados, CodeQL, Gitleaks) e, se tudo passar, deploy automático
   no Firebase App Distribution. O usuário instala e testa esse build.
3. **Publicar no Play Store**: só quando o usuário confirmar que validou o
   build de teste. Aciona manualmente o workflow `promote-play-store.yml`
   (rodando a partir da branch `release/x.y.z`, nunca automático):
   ```bash
   gh workflow run promote-play-store.yml --ref release/x.y.z
   ```
   Builda um AAB assinado e publica na track de teste fechado do Play
   Console. O `versionCode` é resolvido automaticamente (Gradle Play
   Publisher consulta o Play Console e incrementa sozinho) — não precisa
   bump manual nem commit de volta pra branch.
4. **Promover pra `main`**: só quando o usuário pedir, e só depois do
   passo 3. Aciona manualmente o workflow `promote-main.yml` (rodando a
   partir da mesma branch `release/x.y.z`):
   ```bash
   gh workflow run promote-main.yml --ref release/x.y.z
   ```
   Ele abre o PR `release/x.y.z → main` e liga auto-merge — o merge
   acontece sozinho assim que os checks obrigatórios do `release.yml`
   passarem (o mesmo `require-release-source` que bloqueia qualquer PR
   pra `main` que não venha de `release/*`). A branch é apagada
   automaticamente após o merge (config do repo).
5. **Sincronização automática**: assim que algo cai na `main` (esse merge
   ou qualquer outro), `sync-main-to-develop.yml` mergeia `main` de volta
   pra `develop` sozinho — não precisa fazer nada manualmente aqui.

## Regras

- **Nunca dê push direto na `main`.** Vai falhar, sem bypass.
- **Cortar uma branch `release/*`, acionar `promote-play-store.yml` e
  acionar `promote-main.yml` são três ações deliberadas e separadas** — só
  execute cada uma quando o usuário pedir explicitamente aquele passo
  específico no turno atual (ver "Executing actions with care"). Validar
  o build de teste é responsabilidade do usuário, não presuma que passou.
- Commitar/dar push segue a regra de sempre: só quando pedido, uma
  aprovação anterior não vale pra sempre.

## Por que isso existe

Três pipelines dividem o custo de validação de acordo com o estágio:

- `.github/workflows/develop.yml` — push na `develop`: feedback rápido
  (ktlint, detekt, Android Lint, testes unitários + cobertura, build
  debug).
- `.github/workflows/release-candidate.yml` — push numa `release/x.y.z`:
  validação completa + deploy automático no Firebase App Distribution
  pra teste manual.
- `.github/workflows/release.yml` — PR de `release/x.y.z` pra `main`:
  a mesma validação completa de novo (checagem final antes de produção)
  + (só depois do merge) build assinado real + deploy.

Mais dois workflows sob demanda (`workflow_dispatch`, nunca automáticos):
`promote-play-store.yml` (build AAB + publica no Play Console) e
`promote-main.yml` (abre e mergeia o PR pra `main`). E um reativo:
`sync-main-to-develop.yml`, disparado por push na `main`.
