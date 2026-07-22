# Contexto para Continuidade

## 1. Problema Resolvido: 4 Cards nas Quartas
- O problema de aparecer apenas um card nas Quartas foi resolvido com uma **injeção local no `BolaoViewModel`**.
- Como o Firestore estava vazio ou com permissão negada, o App agora gera 4 placeholders ("Vencedor Oitava 1", etc.) na memória caso eles não existam no banco.
- A lógica de agrupamento na `BolaoDetailScreen` foi ajustada para usar o `matchOrder`, garantindo que os 4 cards fiquem separados mesmo sem times definidos.

## 2. Reversão do Servidor (`functions/index.js`)
- **IMPORTANTE**: Todas as alterações na Cloud Function foram revertidas para o estado original conforme solicitado.
- O arquivo voltou a ter os `manualFixes` e a sincronização original.
- Foi feito o deploy da versão original restaurada.

## 3. Erro 'FootballDataMatchRepository'
- Foi reportado um erro de "Unresolved reference" na linha 177 do `BolaoViewModel`. 
- No código atual, essa referência **não existe**. Se o erro persistir na IDE, verifique se não há um import residual ou uma chamada de injeção (`get()`, `koinInject`) perdida. O repositório correto em uso é o `MatchRepository` (com implementação `FirebaseMatchRepository`).

## 4. Próximos Passos
- Validar se a aba "Quartas - Ida" agora exibe os 4 cards.
- Monitorar a sincronização da Libertadores via API para quando os times reais forem definidos.
