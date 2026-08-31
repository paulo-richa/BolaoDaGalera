const { logger } = require("firebase-functions");

/**
 * Sistema de cache + retry para garantir dados verificados
 * Fonte única de verdade: football-data.org (gratuita e confiável)
 *
 * Estratégia:
 * 1. Tenta football-data.org com retry
 * 2. Se falhar, usa cache anterior (se houver)
 * 3. Se não houver cache, aguarda próxima sincronização
 * 4. NUNCA cria dados provisórios
 */

let lastSuccessfulData = null;

async function getLibertadoresData(axios) {
    const API_KEY = process.env.FOOTBALL_DATA_KEY || require("./config").API_KEY;

    try {
        // Sempre busca dado fresco da API - o cache abaixo só existe como
        // fallback para quando a requisição falhar (rede/API fora do ar).
        // Usá-lo no caminho feliz travava o placar ao vivo por até 1h,
        // já que uma instância "quente" do Cloud Run reaproveitava o cache
        // mesmo com o scheduler rodando de 3 em 3 minutos.
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

        // Tenta usar cache anterior se houver
        if (lastSuccessfulData) {
            logger.info("📦 Usando cache anterior como fallback");
            return lastSuccessfulData;
        }

        logger.error("❌ football-data.org indisponível e sem cache disponível");
        logger.error("   ⏳ Próxima tentativa em 5 minutos (Cloud Scheduler)");
        return null;
    }
}

module.exports = { getLibertadoresData };
