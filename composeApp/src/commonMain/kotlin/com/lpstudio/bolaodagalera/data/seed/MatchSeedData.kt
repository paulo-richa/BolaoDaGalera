package com.lpstudio.bolaodagalera.data.seed

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase

/**
 * Copa do Mundo FIFA 2026 — Seed Data
 * Sincronizado com OpenFootball API. Horários em Brasília (UTC-3).
 */

private data class Team(val name: String, val code: String, val flag: String)

private const val BASE = 1781136000000L // 11 de Junho de 2026

private fun day(offset: Int, hourBrt: Int, minutes: Int = 0) =
    BASE + (offset * 86_400_000L) + ((hourBrt + 3) * 3_600_000L) + (minutes * 60_000L)

private fun match(
    id: String, home: Team, away: Team,
    dateMillis: Long, phase: Phase, group: String? = null,
    championshipId: String = "COPA_2026"
) = Match(
    id = id,
    homeTeam = home.name, awayTeam = away.name,
    homeTeamCode = home.code, awayTeamCode = away.code,
    homeTeamFlag = home.flag, awayTeamFlag = away.flag,
    matchDateMillis = dateMillis,
    phase = phase, group = group,
    championshipId = championshipId
)

// ─────────────────────────────── SELEÇÕES ────────────────────────────────────

private val MEX = Team("México",         "MEX", "🇲🇽")
private val KOR = Team("Coreia do Sul",  "KOR", "🇰🇷")
private val RSA = Team("África do Sul",  "RSA", "🇿🇦")
private val CZE = Team("Rep. Tcheca",    "CZE", "🇨🇿")
private val CAN = Team("Canadá",         "CAN", "🇨🇦")
private val SUI = Team("Suíça",          "SUI", "🇨🇭")
private val QAT = Team("Catar",          "QAT", "🇶🇦")
private val BIH = Team("Bósnia",         "BIH", "🇧🇦")
private val BRA = Team("Brasil",         "BRA", "🇧🇷")
private val MAR = Team("Marrocos",       "MAR", "🇲🇦")
private val SCO = Team("Escócia",        "SCO", "🏴󠁧󠁢󠁳󠁣󠁴󠁿")
private val HAI = Team("Haiti",          "HAI", "🇭🇹")
private val USA = Team("EUA",            "USA", "🇺🇸")
private val AUS = Team("Austrália",      "AUS", "🇦🇺")
private val PAR = Team("Paraguai",       "PAR", "🇵🇾")
private val TUR = Team("Turquia",        "TUR", "🇹🇷")
private val GER = Team("Alemanha",       "GER", "🇩🇪")
private val ECU = Team("Equador",        "ECU", "🇪🇨")
private val CIV = Team("Costa do Marfim","CIV", "🇨🇮")
private val CUW = Team("Curaçao",        "CUW", "🇨🇼")
private val NED = Team("Holanda",        "NED", "🇳🇱")
private val JPN = Team("Japão",          "JPN", "🇯🇵")
private val TUN = Team("Tunísia",        "TUN", "🇹🇳")
private val SWE = Team("Suécia",         "SWE", "🇸🇪")
private val EGY = Team("Egito",          "EGY", "🇪🇬")
private val IRN = Team("Irã",            "IRN", "🇮🇷")
private val NZL = Team("Nova Zelândia",  "NZL", "🇳🇿")
private val ESP = Team("Espanha",        "ESP", "🇪🇸")
private val URU = Team("Uruguai",        "URU", "🇺🇾")
private val KSA = Team("Arábia Saudita", "KSA", "🇸🇦")
private val CPV = Team("Cabo Verde",     "CPV", "🇨🇻")
private val FRA = Team("França",         "FRA", "🇫🇷")
private val SEN = Team("Senegal",        "SEN", "🇸🇳")
private val NOR = Team("Noruega",        "NOR", "🇳🇴")
private val IRQ = Team("Iraque",         "IRQ", "🇮🇶")
private val ARG = Team("Argentina",      "ARG", "🇦🇷")
private val ALG = Team("Argélia",        "ALG", "🇩🇿")
private val JOR = Team("Jordânia",       "JOR", "🇯🇴")
private val POR = Team("Portugal",       "POR", "🇵🇹")
private val COL = Team("Colômbia",       "COL", "🇨🇴")
private val UZB = Team("Uzbequistão",    "UZB", "🇺🇿")
private val COD = Team("Rep. Congo",     "COD", "🇨🇩")
private val CHI = Team("Chile",           "CHI", "🇨🇱")
private val ISL = Team("Islândia",        "ISL", "🇮🇸")
private val ENG = Team("Inglaterra",     "ENG", "🏴󠁧󠁢󠁥󠁮󠁧󠁿")
private val CRO = Team("Croácia",        "CRO", "🇭🇷")
private val PAN = Team("Panamá",         "PAN", "🇵🇦")
private val GHA = Team("Gana",           "GHA", "🇬🇭")
private val BEL = Team("Bélgica",        "BEL", "🇧🇪")
private val AUT = Team("Áustria",        "AUT", "🇦🇹")

// ──────────────────────────── FASE DE GRUPOS ─────────────────────────────────

private fun g(group: String, n: Int, home: Team, away: Team, time: Long) =
    match("GS-$group-$n", home, away, time, Phase.GROUP_STAGE, group)

private val groupStageMatches: List<Match> = listOf(
    // RODADA 1
    g("A", 1, MEX, RSA, day(0, 16)), g("A", 2, KOR, CZE, day(0, 23)),
    g("B", 1, CAN, BIH, day(1, 16)), g("D", 1, USA, PAR, day(1, 22)),
    g("B", 2, QAT, SUI, day(2, 16)), g("C", 1, BRA, MAR, day(2, 19)),
    g("C", 2, HAI, SCO, day(2, 22)), g("D", 2, AUS, TUR, day(3, 1)),
    g("E", 1, GER, CUW, day(3, 14)), g("E", 2, CIV, ECU, day(3, 20)),
    g("F", 1, NED, JPN, day(3, 17)), g("F", 2, SWE, TUN, day(3, 23)),
    g("G", 1, BEL, EGY, day(4, 16)), g("G", 2, IRN, NZL, day(4, 22)),
    g("H", 1, ESP, CPV, day(4, 13)), g("H", 2, KSA, URU, day(4, 19)),
    g("I", 1, FRA, SEN, day(5, 16)), g("I", 2, IRQ, NOR, day(5, 19)),
    g("J", 1, ARG, ALG, day(5, 22)), g("J", 2, AUT, JOR, day(6, 1)),
    g("K", 1, POR, COD, day(6, 14)), g("K", 2, UZB, COL, day(6, 23)),
    g("L", 1, ENG, CRO, day(6, 17)), g("L", 2, GHA, PAN, day(6, 20)),

    // RODADA 2
    g("A", 3, CZE, RSA, day(7, 13)), g("A", 4, MEX, KOR, day(7, 22)),
    g("B", 3, SUI, BIH, day(7, 16)), g("B", 4, CAN, QAT, day(7, 19)),
    g("C", 3, SCO, MAR, day(8, 19)), g("C", 4, BRA, HAI, day(8, 21, 30)),
    g("D", 3, USA, AUS, day(8, 16)), g("D", 4, TUR, PAR, day(9, 0)),
    g("E", 3, GER, CIV, day(9, 17)), g("E", 4, ECU, CUW, day(9, 21)),
    g("F", 3, NED, SWE, day(9, 14)), g("F", 4, TUN, JPN, day(10, 1)),
    g("G", 3, BEL, IRN, day(10, 16)), g("G", 4, NZL, EGY, day(10, 22)),
    g("H", 3, ESP, KSA, day(10, 13)), g("H", 4, URU, CPV, day(10, 19)),
    g("I", 3, FRA, IRQ, day(11, 18)), g("I", 4, NOR, SEN, day(11, 21)),
    g("J", 3, ARG, AUT, day(11, 14)), g("J", 4, JOR, ALG, day(12, 0)),
    g("K", 3, POR, UZB, day(12, 14)), g("K", 4, COL, COD, day(12, 23)),
    g("L", 3, ENG, GHA, day(12, 17)), g("L", 4, PAN, CRO, day(12, 20)),

    // RODADA 3
    g("A", 5, RSA, KOR, day(13, 22)), g("A", 6, CZE, MEX, day(13, 22)),
    g("B", 5, SUI, CAN, day(13, 16)), g("B", 6, BIH, QAT, day(13, 16)),
    g("C", 5, SCO, BRA, day(13, 19)), g("C", 6, MAR, HAI, day(13, 19)),
    g("D", 5, TUR, USA, day(14, 23)), g("D", 6, PAR, AUS, day(14, 23)),
    g("E", 5, CUW, CIV, day(14, 17)), g("E", 6, ECU, GER, day(14, 17)),
    g("F", 5, JPN, SWE, day(14, 20)), g("F", 6, TUN, NED, day(14, 20)),
    g("G", 5, EGY, IRN, day(15, 23)), g("G", 6, NZL, BEL, day(15, 23)),
    g("H", 5, CPV, KSA, day(15, 21)), g("H", 6, URU, ESP, day(15, 21)),
    g("I", 5, NOR, FRA, day(15, 16)), g("I", 6, SEN, IRQ, day(15, 16)),
    g("J", 5, ALG, AUT, day(16, 23)), g("J", 6, JOR, ARG, day(16, 23)),
    g("K", 5, COL, POR, day(16, 20, 30)), g("K", 6, COD, UZB, day(16, 20, 30)),
    g("L", 5, PAN, ENG, day(16, 18)), g("L", 6, CRO, GHA, day(16, 18)),
)

// ──────────────────────────── MATA-MATA ──────────────────────────────────────

private fun k(id: String, home: Team, away: Team, phase: Phase, offset: Int, hourBrt: Int, min: Int = 0) =
    match("KO-$id", home, away, day(offset, hourBrt, min), phase, phase.label)

private fun ktbd(id: String, home: String, away: String, phase: Phase, offset: Int, hourBrt: Int, min: Int = 0) =
    match("KO-$id", Team(home, "TBD", "🏳️"), Team(away, "TBD", "🏳️"), day(offset, hourBrt, min), phase, phase.label)

val knockoutMatches = listOf(
    k("32-1", GER, PAR, Phase.ROUND_OF_32, 18, 17, 30),
    k("32-2", FRA, SWE, Phase.ROUND_OF_32, 19, 18, 0),
    k("32-3", RSA, CAN, Phase.ROUND_OF_32, 17, 16, 0),
    k("32-4", NED, MAR, Phase.ROUND_OF_32, 18, 22, 0),
    k("32-5", POR, CRO, Phase.ROUND_OF_32, 21, 20, 0),
    k("32-6", ESP, AUT, Phase.ROUND_OF_32, 21, 16, 0),
    k("32-7", USA, BIH, Phase.ROUND_OF_32, 20, 21, 0),
    k("32-8", BEL, SEN, Phase.ROUND_OF_32, 20, 17, 0),
    k("32-9", BRA, JPN, Phase.ROUND_OF_32, 18, 14, 0),
    k("32-10", CIV, NOR, Phase.ROUND_OF_32, 19, 14, 0),
    k("32-11", MEX, ECU, Phase.ROUND_OF_32, 19, 23, 0),
    k("32-12", ENG, COD, Phase.ROUND_OF_32, 20, 13, 0),
    k("32-13", ARG, CPV, Phase.ROUND_OF_32, 22, 19, 0),
    k("32-14", AUS, EGY, Phase.ROUND_OF_32, 22, 15, 0),
    k("32-15", SUI, ALG, Phase.ROUND_OF_32, 22, 0, 0),
    k("32-16", COL, GHA, Phase.ROUND_OF_32, 22, 22, 30),

    // Oitavas de Final
    k("16-1", CAN, MAR, Phase.ROUND_OF_16, 23, 14, 0),
    k("16-2", PAR, FRA, Phase.ROUND_OF_16, 23, 18, 0),
    k("16-3", POR, ESP, Phase.ROUND_OF_16, 25, 16, 0),
    k("16-4", USA, BEL, Phase.ROUND_OF_16, 25, 21, 0),
    k("16-5", BRA, NOR, Phase.ROUND_OF_16, 24, 17, 0),
    k("16-6", MEX, ENG, Phase.ROUND_OF_16, 24, 21, 0),
    k("16-7", ARG, EGY, Phase.ROUND_OF_16, 26, 13, 0), 
    k("16-8", SUI, COL, Phase.ROUND_OF_16, 26, 17, 0),

    // Quartas de Final
    k("QF-1", FRA, MAR, Phase.QUARTERFINALS, 28, 17, 0), 
    k("QF-2", ESP, BEL, Phase.QUARTERFINALS, 29, 16, 0),
    k("QF-3", NOR, ENG, Phase.QUARTERFINALS, 30, 18, 0), 
    k("QF-4", ARG, SUI, Phase.QUARTERFINALS, 30, 22, 0),

    // Semifinais
    ktbd("SF-1", "Vencedor QF1", "Vencedor QF2", Phase.SEMIFINALS, 33, 16, 0), 
    ktbd("SF-2", "Vencedor QF3", "Vencedor QF4", Phase.SEMIFINALS, 34, 16, 0),

    // 3º Lugar e Final
    match("KO-SF-3", Team("Perdedor SF1", "TBD", "🏳️"), Team("Perdedor SF2", "TBD", "🏳️"), day(37, 18), Phase.THIRD_PLACE, Phase.THIRD_PLACE.label),
    match("KO-FINAL", Team("Vencedor SF1", "TBD", "🏳️"), Team("Vencedor SF2", "TBD", "🏳️"), day(38, 16), Phase.FINAL, Phase.FINAL.label)
)

val allMatches: List<Match> = groupStageMatches + knockoutMatches
