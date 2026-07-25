/**
 * Calcula a pontuação de um palpite baseado no resultado real e nas regras do bolão.
 */
function calculatePoints(prediction, actual, pointsExact, pointsResult) {
    const { homeScore: ph, awayScore: pa } = prediction;
    const { homeScore: ah, awayScore: aa } = actual;

    // Se o jogo ainda não terminou ou não tem placar, 0 pontos
    if (ah === null || aa === null) return 0;

    // 1. Placar Exato (3 pontos padrão)
    if (ph === ah && pa === aa) {
        return pointsExact;
    }

    // 2. Resultado Certo (Vencedor ou Empate - 1 ponto padrão)
    const predictedWinner = ph > pa ? 'home' : (ph < pa ? 'away' : 'draw');
    const actualWinner = ah > aa ? 'home' : (ah < aa ? 'away' : 'draw');

    if (predictedWinner === actualWinner) {
        return pointsResult;
    }

    // 3. Erro Total
    return 0;
}

module.exports = { calculatePoints };
