# Contexto de Continuidade - Bolão da Galera

## 1. Objetivos Atuais
- Refinar a fase de mata-mata da Libertadores 2026.
- Garantir a ordem do chaveamento conforme o GE (1-Estudiantes, 2-Rosario, 3-Cruzeiro, 4-Tolima, 5-Mirassol, 6-Palmeiras, 7-Platense, 8-Fluminense).
- Separar corretamente as abas de Ida e Volta no `BolaoDetailScreen`.
- Mostrar 4 cards de confronto nas Quartas de Final, mesmo sem times definidos (placeholders).

## 2. O que foi feito
- **Cloud Function (`index.js`)**: 
    - Corrigido e feito Deploy com sucesso.
    - Implementada a criação automática de placeholders para Quartas, Semifinais e Final.
    - Padronização de IDs: `CLI-2026-R16-X-L1/L2` para Oitavas e `CLI-2026-QFX-L1/L2` para Quartas.
    - Mapeamento de `matchOrder` para garantir a ordem visual no App.
- **App (`BolaoDetailScreen.kt`)**:
    - Lógica de agrupamento refinada para usar o `matchOrder` ou o prefixo do ID, garantindo que os cards não se colapsem quando os times são "TBD".
    - Ordenação "blindada" para as Oitavas baseada em IDs e nomes de times.
    - Adicionada limpeza de nomes para exibir "A Definir" em vez de strings vazias.
    - Fixado o seletor de fase para não dar scroll automático ao trocar de aba.
- **ViewModel (`BolaoViewModel.kt`)**:
    - Removidas injeções locais de dados para manter a fonte da verdade no Firestore.
    - Refinado o filtro de partidas.

## 3. Estado Atual
- **Deploy do Servidor**: OK. A função `syncScores` roda a cada 1 minuto.
- **Interface**: As abas de Oitavas (Ida/Volta) e Quartas devem estar exibindo a quantidade correta de cards e na ordem certa.
- **Permissões**: O App ainda sofre de `PERMISSION_DENIED` ao tentar gravar partidas diretamente. A recomendação é deixar o servidor gerenciar os dados ou ajustar as Rules do Firestore.

## 4. Próximos Passos Sugeridos
- Validar se os 4 cards de Quartas apareceram na produção após a rodada da Cloud Function.
- Verificar o erro mencionado: `BolaoViewModel.kt:177:73 Unresolved reference 'FootballDataMatchRepository'`. (Não encontrado nos arquivos atuais, pode ser resquício de código não salvo ou em outra branch).
- Testar a navegação entre Ida/Volta para garantir que os palpites estão sendo salvos nos documentos corretos.

---
*Gerado via Assistente AI em 17/05/2026*
