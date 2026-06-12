# 🔧 Correções de Horários - Amistosos 08/06/2026

## ✅ O que foi feito

### 1. **Scraper Dinâmico Implementado**
   - Arquivo: `scripts/scrape_ge_friendly_matches.py`
   - Usa **Playwright + Chromium** para renderizar JavaScript
   - Busca dados **em tempo real** do Globo Esportes
   - Executa: `python3 scripts/scrape_ge_friendly_matches.py`

### 2. **Horários Corrigidos em `MatchSeedData.kt`**

| Jogo | Horário Anterior | Horário Correto | Diferença |
|------|------------------|-----------------|-----------|
| Holanda × Uzbequistão | 18:45 UTC (15:45 BRT) | 18:45 UTC (15:45 BRT) | ✅ Correto |
| **França × Irl. Norte** | 19:00 UTC (16:00 BRT) | **19:10 UTC (16:10 BRT)** | +10 min |
| **Peru × Espanha** | 22:00 UTC (19:00 BRT) | **02:00 UTC (23:00 BRT)*** | +5h |

*Peru × Espanha: 02:00 UTC do dia 9 de junho = 23:00 BRT do dia 8 (horário de verão)

### 3. **Integração no App**
   - Nova classe: `GEFriendlyMatchSynchronizer.kt`
   - Permite sincronizar dados com GE em tempo real
   - Estrutura pronta para HTTP requests

---

## 🚀 Como Usar

### Atualizar dados em tempo real via script Python:

```bash
cd /Users/paulogeorgemoreirarocha/AndroidStudioProjects/BolaoDaGalera
python3 scripts/scrape_ge_friendly_matches.py
```

Resultado salvo em: `/tmp/ge_friendly_matches.json`

### Integrar no app Android (Firebase):

Para sincronizar automaticamente os horários, você pode:

1. Executar o scraper periodicamente (via Cloud Function ou Worker)
2. Salvar resultado em Firestore
3. O app lê dados do Firestore primeiro, depois `MatchSeedData.kt` como fallback

---

## 📋 Dados Extraídos do Globo (08/06/2026)

```json
{
  "date": "08/06/2026",
  "source": "ge.globo.com",
  "friendly_matches": [
    {
      "home_team": "Holanda",
      "away_team": "Uzbequistão",
      "time": "15:45",
      "competition": "Amistosos Pré-Copa"
    },
    {
      "home_team": "França",
      "away_team": "Irlanda do Norte",
      "time": "16:10",
      "competition": "Amistosos Pré-Copa"
    },
    {
      "home_team": "Peru",
      "away_team": "Espanha",
      "time": "23:00",
      "competition": "Amistosos Pré-Copa"
    }
  ]
}
```

---

## 🔄 Sincronização em Tempo Real

Para manter os horários sempre atualizados:

1. **Backend (Node.js/Python)**: Roda scraper diariamente
2. **Firestore**: Armazena dados do Globo
3. **App**: Consulta Firestore primeiro

Exemplo de função Cloud:

```javascript
// cloud_functions/syncGeMatches.js
const { onSchedule } = require("firebase-functions/v2/scheduler");
const axios = require("axios");

exports.syncGeMatches = onSchedule("every 6 hours", async (context) => {
  // Executar scraper Python ou usar Playwright no Node
  const matches = await scrapeGeMatches();
  
  // Salvar em Firestore
  await db.collection("matches")
    .doc("friendly_today")
    .set(matches);
});
```

---

## ✨ Próximos Passos

1. [ ] Integrar HTTP request no `FirebaseMatchRepository.kt` para buscar do scraper
2. [ ] Criar Cloud Function que sincroniza horários
3. [ ] Adicionar cache local para offline
4. [ ] Notificar usuários quando horários mudam

---

**Status**: ✅ Horários corrigidos e scraper dinâmico implementado!

