package com.lpstudio.bolaodagalera.data.seed

import com.lpstudio.bolaodagalera.domain.model.Phase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MatchSeedDataTest {
    
    @Test
    fun testFriendlyMatchesHaveCorrectedTimes() {
        // Verificar que os amistosos têm os horários corretos
        val nedUzbMatch = friendlyMatches.find { it.id == "FR-NED-UZB" }
        val fraMatch = friendlyMatches.find { it.id == "FR-FRA-NIR" }
        val perMatch = friendlyMatches.find { it.id == "FR-PER-ESP" }
        
        // Verificar que todos foram encontrados
        assertTrue(nedUzbMatch != null, "Holanda × Uzbequistão não encontrado")
        assertTrue(fraMatch != null, "França × Irlanda não encontrado")
        assertTrue(perMatch != null, "Peru × Espanha não encontrado")
        
        // Verificar times
        assertEquals("Holanda", nedUzbMatch?.homeTeam)
        assertEquals("Uzbequistão", nedUzbMatch?.awayTeam)
        
        assertEquals("França", fraMatch?.homeTeam)
        assertEquals("Irl. do Norte", fraMatch?.awayTeam)
        
        assertEquals("Peru", perMatch?.homeTeam)
        assertEquals("Espanha", perMatch?.awayTeam)
        
        // Verificar horários (dados brutos em ms)
        // BASE = 1781136000000L (11 de junho 2026 00:00 UTC)
        // day(-3, hour) = BASE - 3 dias + hour * 3600000ms
        
        val BASE = 1781136000000L
        val oneDayMs = 86_400_000L
        val eightJuneMidnightUTC = BASE - (3 * oneDayMs) // 8 de junho 00:00 UTC
        
        // Holanda × Uzbequistão: 18:45 UTC
        val nedUzbExpected = eightJuneMidnightUTC + (18 * 3_600_000L) + (45 * 60_000L)
        assertEquals(nedUzbExpected, nedUzbMatch?.matchDateMillis, "Holanda × Uzbequistão - horário incorreto")
        
        // França × Irlanda do Norte: 19:10 UTC (CORRIGIDO)
        val fraExpected = eightJuneMidnightUTC + (19 * 3_600_000L) + (10 * 60_000L)
        assertEquals(fraExpected, fraMatch?.matchDateMillis, "França × Irlanda - horário incorreto (deveria ser 19:10 UTC)")
        
        // Peru × Espanha: 02:00 UTC (próximo dia) (CORRIGIDO)
        val perExpected = eightJuneMidnightUTC + (2 * 3_600_000L) + (0 * 60_000L)
        assertEquals(perExpected, perMatch?.matchDateMillis, "Peru × Espanha - horário incorreto (deveria ser 02:00 UTC)")
    }
    
    @Test
    fun testFriendlyMatchesPhaseAndGroup() {
        // Verificar que todos são fase "FRIENDLIES" e grupo "Amistosos Pre-Copa"
        for (match in friendlyMatches) {
            assertEquals(Phase.FRIENDLIES, match.phase, "${match.id}: Fase incorreta")
            assertEquals("Amistosos Pre-Copa", match.group, "${match.id}: Grupo incorreto")
        }
    }
    
    @Test
    fun testFriendlyMatchesCount() {
        // Verificar que temos exatamente 3 amistosos
        assertEquals(3, friendlyMatches.size, "Deveria haver 3 amistosos para hoje")
    }
}
