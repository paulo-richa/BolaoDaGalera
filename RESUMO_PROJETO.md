# 🚀 Snapshot do Estado do Projeto: Bolão Da Galera

## 🎯 Objetivo Geral
Sincronizar e automatizar o mata-mata da Libertadores, garantindo que os escudos, nomes e o chaveamento (Oitavas -> Final) funcionem sozinhos via Cloud Functions, sem códigos fixos (hardcoded) no App Android.

## 🛠️ O que foi feito (Últimas Atividades)

1.  **Mapeamento Oficial de Times (`functions/teams.js`)**:
    *   Sincronizamos todos os escudos da Libertadores com os IDs reais da API.
    *   Corrigimos especificamente: **Independiente del Valle (6989), LDU (4528), Cerro Porteño (9373), Platense (7580), Coquimbo (7912)** e **Ind. Rivadavia (2052)**.
    *   Os nomes agora seguem o padrão curto (ex: "Ind. del Valle", "Cerro Porteño").

2.  **Automação de Mata-Mata (`functions/knockout.js`)**:
    *   Criada a função `advanceTeams` que calcula o vencedor do agregado (Ida + Volta).
    *   Implementada a lógica de "Candidatos": Enquanto o jogo não termina, o banco grava no nome do time: `"Time A ou Time B"`.
    *   O App foi ajustado para detectar esse "ou" e centralizar o texto no card.

3.  **Blindagem da Sincronização (`functions/libertadores.js`)**:
    *   O robô de sincronização agora **ignora** jogos de Quartas, Semis e Final vindos da API para evitar que ela crie entradas duplicadas ou lixo no seu banco. Nós controlamos essas fases pelos nossos IDs padronizados (`QF1`, `SF1`, etc.).

4.  **Ajustes na UI (`BolaoDetailScreen.kt` & `MatchUtils.kt`)**:
    *   Corrigida a aba "Final" que exibia outros jogos.
    *   Componente `TeamIcon` agora garante que, se não houver escudo, o nome/emoji apareça de forma legível.
    *   Removida toda a lógica hardcoded de nomes de times do Kotlin; agora o App apenas exibe o que o Firestore envia.

## 📂 Arquivos Modificados (Localmente)

*   `functions/teams.js`: Base de dados de escudos e nomes.
*   `functions/knockout.js`: Inteligência de avanço de fase.
*   `functions/libertadores.js`: Filtro de entrada de dados da API.
*   `composeApp/src/commonMain/kotlin/com/lpstudio/bolaodagalera/util/MatchUtils.kt`: Resolução visual de nomes.
*   `composeApp/src/commonMain/kotlin/com/lpstudio/bolaodagalera/presentation/bolao/BolaoDetailScreen.kt`: Exibição de ícones e abas.

## 📋 Próximos Passos (TODO)

1.  **Deploy das Functions**: Como as últimas correções de `teams.js` foram locais, você precisará rodar `firebase deploy --only functions` na nova conta.
2.  **Sincronização de Teste**: Rodar o script de limpeza mestre para garantir que o Firestore reflita os novos escudos de Independiente del Valle, LDU, etc.
3.  **Validação Visual**: Abrir o App e confirmar se o jogo **Palmeiras vs Cerro Porteño** (Oitava 6) e **Cruzeiro vs Flamengo** (Oitava 3) estão com os logos corretos e se as Quartas mostram os nomes limpos.
4.  **Finalização**: Assim que as Oitavas de Volta terminarem, verificar se o vencedor subiu automaticamente para a aba de Quartas.

---
**Nota**: Não foi realizado `git push` das últimas alterações de escudos. As mudanças estão apenas nos arquivos locais. ⚽🏆✨
