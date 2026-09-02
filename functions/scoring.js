/**
 * Calculates the score of a prediction based on the actual result and the bolao's rules.
 */
function calculatePoints(prediction, actual, pointsExact, pointsResult) {
    const { homeScore: ph, awayScore: pa } = prediction;
    const { homeScore: ah, awayScore: aa } = actual;

    // If the match hasn't finished or has no score yet, 0 points
    if (ah === null || aa === null) return 0;

    // 1. Exact score (3 points by default)
    if (ph === ah && pa === aa) {
        return pointsExact;
    }

    // 2. Correct result (winner or draw - 1 point by default)
    const predictedWinner = ph > pa ? 'home' : (ph < pa ? 'away' : 'draw');
    const actualWinner = ah > aa ? 'home' : (ah < aa ? 'away' : 'draw');

    if (predictedWinner === actualWinner) {
        return pointsResult;
    }

    // 3. Total miss
    return 0;
}

module.exports = { calculatePoints };
