---
name: git-workflow
description: Use sempre que for commitar, dar push ou abrir um pull request no BolaoDaGalera. Aplica a política de branches do repo — a main é protegida e só aceita merge vindo da develop.
---

# Fluxo de Git do BolaoDaGalera

A `main` é uma branch protegida (branch protection do GitHub,
`enforce_admins` ativado — isso bloqueia push direto pra qualquer um,
incluindo o dono do repo). Ela só se move através de um pull request
vindo da `develop`, e só depois que todo check obrigatório passar. Isso
não é preferência de estilo — um `git push origin <branch>:main` direto
vai ser **rejeitado pelo GitHub**, não importa quem rode.

## A regra

1. **O trabalho do dia a dia acontece na `develop`.** Commite e dê push
   lá por padrão, do mesmo jeito que este projeto antes fazia push direto
   pra `main`.
2. **Nunca dê push direto na `main`.** Vai falhar. Não tente
   `git push` com `main` como destino, não force push nela, não tente
   contornar a proteção — não tem bypass.
3. **Promova a `develop` pra `main` só via pull request, e só quando o
   usuário pedir explicitamente pra fazer release/promover/publicar em
   produção.** Abrir esse PR é uma ação deliberada e visível — trate como
   qualquer outra ação com consequências reais (ver a orientação
   "Executing actions with care"): confirme com o usuário antes, a menos
   que ele já tenha pedido isso no turno atual.
   ```bash
   gh pr create --base main --head develop --title "..." --body "..."
   ```
   O GitHub garante automaticamente que a branch de origem do PR precisa
   ser `develop` (um check obrigatório, `require-develop-source`, falha
   qualquer PR pra `main` que venha de outro lugar) e que todo job de CI
   passe antes de poder mergear.
4. **Mergear também é uma ação real e visível** — faça só quando o usuário
   pedir, igual à regra já existente de "não commitar/dar push sem ser
   pedido" que se estende naturalmente pra "não mergear sem ser pedido".

## Por que isso existe

Duas pipelines dividem o custo de validação de acordo:

- `.github/workflows/develop.yml` — push na `develop` recebe feedback
  rápido: ktlint, detekt, Android Lint, testes unitários + cobertura,
  build debug.
- `.github/workflows/release.yml` — um PR pra `main` recebe a validação
  completa (os mesmos checks mais build de release assinado, testes
  instrumentados em emulador, CodeQL, Gitleaks) *antes* do merge; os
  passos exclusivos de release (o artefato de build assinado de verdade +
  deploy no Firebase App Distribution) só rodam *depois* que o merge cai
  na `main` (o gatilho `push`, controlado por
  `if: github.event_name == 'push'` no job de deploy).

Isso mantém a branch de trabalho rápida de iterar enquanto garante que
nada chega na `main` — e nada é distribuído pros testers — sem passar
pelo gate completo.
