const { logger } = require("firebase-functions");

/**
 * Cache + retry system to guarantee verified data.
 * Single source of truth: football-data.org (free and reliable).
 *
 * Strategy:
 * 1. Try football-data.org with retry
 * 2. On failure, fall back to the previous cache (if any)
 * 3. If no cache exists, wait for the next sync
 * 4. NEVER fabricate provisional data
 */

let lastSuccessfulData = null;
let lastSuccessfulChampionsLeagueData = null;

async function getLibertadoresData(axios) {
    const API_KEY = process.env.FOOTBALL_DATA_KEY || require("./config").API_KEY;

    try {
        // Always fetch fresh data from the API - the cache below exists only
        // as a fallback for when the request fails (network/API down).
        // Using it on the happy path froze the live score for up to 1h,
        // since a "warm" Cloud Run instance would reuse the cache even
        // with the scheduler running every 3 minutes.
        logger.info("📡 Sincronizando com football-data.org...");

        const response = await axios.get(
            "https://api.football-data.org/v4/competitions/CLI/matches",
            {
                headers: { 'X-Auth-Token': API_KEY },
                timeout: 15000
            }
        );

        if (response?.data?.matches && response.data.matches.length > 0) {
            lastSuccessfulData = response.data;

            const qf = response.data.matches.filter(m => m.stage === "QUARTER_FINALS").length;
            const sf = response.data.matches.filter(m => m.stage === "SEMI_FINALS").length;
            const f = response.data.matches.filter(m => m.stage === "FINAL").length;

            logger.info(`✅ Libertadores sincronizada: ${qf} Quartas, ${sf} Semis, ${f} Final`);
            return response.data;
        }

        throw new Error("Resposta vazia da API");

    } catch (error) {
        logger.warn(`⚠️  Erro ao sincronizar (${error.message})`);

        // Fall back to the previous cache, if any
        if (lastSuccessfulData) {
            logger.info("📦 Usando cache anterior como fallback");
            return lastSuccessfulData;
        }

        logger.error("❌ football-data.org indisponível e sem cache disponível");
        logger.error("   ⏳ Próxima tentativa em 5 minutos (Cloud Scheduler)");
        return null;
    }
}

async function getChampionsLeagueData(axios) {
    const API_KEY = process.env.FOOTBALL_DATA_KEY || require("./config").API_KEY;

    try {
        // Same reasoning as getLibertadoresData: always fetch fresh, cache is
        // fallback-only.
        logger.info("📡 Sincronizando com football-data.org (Champions League)...");

        const response = await axios.get(
            "https://api.football-data.org/v4/competitions/CL/matches",
            {
                headers: { 'X-Auth-Token': API_KEY },
                timeout: 15000
            }
        );

        if (response?.data?.matches && response.data.matches.length > 0) {
            lastSuccessfulChampionsLeagueData = response.data;
            logger.info(`✅ Champions League sincronizada: ${response.data.matches.length} jogos`);
            return response.data;
        }

        throw new Error("Resposta vazia da API");

    } catch (error) {
        logger.warn(`⚠️  Erro ao sincronizar Champions League (${error.message})`);

        if (lastSuccessfulChampionsLeagueData) {
            logger.info("📦 Usando cache anterior como fallback");
            return lastSuccessfulChampionsLeagueData;
        }

        logger.error("❌ football-data.org indisponível e sem cache disponível (Champions League)");
        return null;
    }
}

module.exports = { getLibertadoresData, getChampionsLeagueData };
