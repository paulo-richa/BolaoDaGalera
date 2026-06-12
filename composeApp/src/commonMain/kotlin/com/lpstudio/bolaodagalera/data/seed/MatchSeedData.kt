package com.lpstudio.bolaodagalera.data.seed

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase

// ──────────────────────────────────────────────────────────────────────────────
// Copa do Mundo FIFA 2026 — EUA · Canadá · México
// Calendário Oficial FIFA (Seed Data) - Sincronizado com OpenFootball API
// Horários convertidos para Brasília (UTC-3)
// ──────────────────────────────────────────────────────────────────────────────

private data class Team(val name: String, val code: String, val flag: String)

// Base: 11 de junho de 2026 00:00 UTC
private const val BASE = 1781136000000L

/**
 * Converte data da API para Millis.
 * @param offset Dias após o início (0 = 11/06)
 * @param hourBrt Hora no fuso de Brasília (UTC-3)
 */
private fun day(offset: Int, hourBrt: Int, minutes: Int = 0) =
    BASE + (offset * 86_400_000L) + ((hourBrt + 3) * 3_600_000L) + (minutes * 60_000L)

private fun match(
    id: String, home: Team, away: Team,
    dateMillis: Long, phase: Phase, group: String? = null
) = Match(
    id = id,
    homeTeam = home.name, awayTeam = away.name,
    homeTeamCode = home.code, awayTeamCode = away.code,
    homeTeamFlag = home.flag, awayTeamFlag = away.flag,
    matchDateMillis = dateMillis,
    phase = phase, group = group
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

private fun k(id: String, home: String, away: String, phase: Phase, offset: Int, hourBrt: Int, min: Int = 0) =
    match("KO-$id", Team(home, "TBD", "🏳️"), Team(away, "TBD", "🏳️"), day(offset, hourBrt, min), phase, phase.label)

val knockoutMatches = listOf(
    // 16-avos de Final
    k("32-1",  "1º Grupo A", "3º Gr. C/E/F", Phase.ROUND_OF_32, 17, 16), 
    k("32-2",  "2º Grupo A", "2º Grupo B",   Phase.ROUND_OF_32, 18, 19, 30),
    k("32-3",  "1º Grupo B", "3º Gr. A/C/D", Phase.ROUND_OF_32, 18, 22), 
    k("32-4",  "1º Grupo C", "3º Gr. A/B/F", Phase.ROUND_OF_32, 18, 14),
    k("32-5",  "1º Grupo E", "3º Gr. A/B/C", Phase.ROUND_OF_32, 19, 20), 
    k("32-6",  "1º Grupo F", "2º Grupo C",   Phase.ROUND_OF_32, 19, 14),
    k("32-7",  "2º Grupo E", "2º Grupo I",   Phase.ROUND_OF_32, 19, 22), 
    k("32-8",  "1º Grupo D", "3º Gr. B/E/F", Phase.ROUND_OF_32, 20, 13),
    k("32-9",  "1º Grupo G", "3º Gr. A/E/H", Phase.ROUND_OF_32, 20, 21), 
    k("32-10", "2º Grupo G", "2º Grupo H",   Phase.ROUND_OF_32, 20, 17),
    k("32-11", "1º Grupo H", "2º Grupo J",   Phase.ROUND_OF_32, 21, 20), 
    k("32-12", "1º Grupo I", "3º Gr. D/G/J", Phase.ROUND_OF_32, 21, 16),
    k("32-13", "2º Grupo I", "2º Grupo K",   Phase.ROUND_OF_32, 22, 0), 
    k("32-14", "1º Grupo J", "2º Grupo L",   Phase.ROUND_OF_32, 22, 19),
    k("32-15", "1º Grupo K", "3º Gr. I/L",   Phase.ROUND_OF_32, 22, 22, 30), 
    k("32-16", "1º Grupo L", "3º Gr. G/H/K", Phase.ROUND_OF_32, 22, 15),

    // Oitavas de Final
    k("16-1", "Venc. J32-1",  "Venc. J32-2",  Phase.ROUND_OF_16, 23, 20), 
    k("16-2", "Venc. J32-3",  "Venc. J32-4",  Phase.ROUND_OF_16, 23, 15),
    k("16-3", "Venc. J32-5",  "Venc. J32-6",  Phase.ROUND_OF_16, 24, 19), 
    k("16-4", "Venc. J32-7",  "Venc. J32-8",  Phase.ROUND_OF_16, 24, 21),
    k("16-5", "Venc. J32-9",  "Venc. J32-10", Phase.ROUND_OF_16, 25, 17), 
    k("16-6", "Venc. J32-11", "Venc. J32-12", Phase.ROUND_OF_16, 25, 20),
    k("16-7", "Venc. J32-13", "Venc. J32-14", Phase.ROUND_OF_16, 26, 15), 
    k("16-8", "Venc. J32-15", "Venc. J32-16", Phase.ROUND_OF_16, 26, 16),

    // Quartas de Final
    k("QF-1", "Venc. Oit. 1", "Venc. Oit. 2", Phase.QUARTERFINALS, 28, 19), 
    k("QF-2", "Venc. Oit. 3", "Venc. Oit. 4", Phase.QUARTERFINALS, 29, 15),
    k("QF-3", "Venc. Oit. 5", "Venc. Oit. 6", Phase.QUARTERFINALS, 30, 20), 
    k("QF-4", "Venc. Oit. 7", "Venc. Oit. 8", Phase.QUARTERFINALS, 30, 23),

    // Semifinais
    k("SF-1", "Venc. QF 1", "Venc. QF 2", Phase.SEMIFINALS, 33, 17), 
    k("SF-2", "Venc. QF 3", "Venc. QF 4", Phase.SEMIFINALS, 34, 18),

    // 3º Lugar e Final
    k("3RD",   "Perd. Semi 1", "Perd. Semi 2", Phase.THIRD_PLACE, 37, 20),
    k("FINAL", "Venc. Semi 1", "Venc. Semi 2", Phase.FINAL, 38, 18)
)

val allMatches: List<Match> = groupStageMatches + knockoutMatches
