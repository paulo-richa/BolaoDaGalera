# 🚀 Bolão da Galera - Contexto de Integração Brasileirão 2026

Este arquivo serve para transferir o contexto atual do desenvolvimento para uma nova sessão.

## 📌 Status Atual
- **Brasileirão 2026:** Integrado com sucesso.
- **Cloud Functions:** Função `syncScores` em `functions/index.js` agora possui um "limpador" que força status `FINISHED` para jogos com mais de 48h de atraso na API.
- **App (Filtro de Corte):** Lógica de `firstValidRound` no `BolaoViewModel.kt` foi robustecida. Agora ela ignora jogos que aconteceram antes da criação do bolão ou há mais de 24h (mesmo se o status não for `FINISHED`).
- **Match Model:** `groupRound()` agora suporta o formato de ID `BSA-2026-R{round}`.

## ✅ Problema Resolvido (Rodada de Corte)
Os bolões criados recentemente não devem mais mostrar a Rodada 4 (jogos de Fevereiro) se estivermos em Julho, pois o filtro agora é baseado no tempo absoluto (`matchDateMillis`) em relação à data de criação do bolão e ao tempo atual.

## 🛠️ Próximos Passos
1. **Monitoramento:** Observar se o "limpador" na Cloud Function está atualizando corretamente os jogos antigos no Firestore.
2. **UI/UX:** Verificar se o seletor de rodadas na `BolaoDetailScreen` está se comportando bem com a lista filtrada de matches.

## 📄 Arquivos Importantes
- `composeApp/src/commonMain/kotlin/com/lpstudio/bolaodagalera/presentation/bolao/BolaoViewModel.kt` (Lógica de filtro)
- `functions/index.js` (Sincronização do servidor)
- `composeApp/src/commonMain/kotlin/com/lpstudio/bolaodagalera/domain/model/Match.kt` (Helper de rodadas)

---
**Instrução para o próximo Gemini:**
"Leia o arquivo `GEMINI_CONTEXT.md` e o `BolaoViewModel.kt`. Precisamos ajustar a lógica de `firstValidRound` para que o seletor de rodadas ignore de vez as rodadas passadas (1 a 18) em bolões criados recentemente, já que o status `isFinished` parece não estar vindo correto para alguns jogos antigos da API."
