#!/usr/bin/env kotlin

// Validar horários dos amistosos - Execute com: kotlinc -cp . -script validate_friendly_times.kts

// BASE: 11 de junho de 2026 00:00 UTC
const val BASE = 1781136000000L

fun day(offset: Int, hourUtc: Int = 16) =
    BASE + offset * 86_400_000L + hourUtc * 3_600_000L

fun prettyDate(millis: Long): String {
    val instant = java.time.Instant.ofEpochMilli(millis)
    val zoneId = java.time.ZoneId.of("Brazil/East") // BRT
    val dateTime = java.time.LocalDateTime.ofInstant(instant, zoneId)
    return dateTime.toString()
}

println("=" * 70)
println("🔍 VALIDAÇÃO DE HORÁRIOS - AMISTOSOS 08/06/2026")
println("=" * 70)

val amistosos = mapOf(
    "Holanda × Uzbequistão" to Triple(18, 45, 15),  // UTC hour, minute, BRT hour
    "França × Irlanda do Norte" to Triple(19, 10, 16),
    "Peru × Espanha" to Triple(2, 0, 23)  // próximo dia, 23:00 BRT anterior
)

println("\n📊 ESPERADO VS ATUAL:\n")

amistosos.forEach { (match, times) ->
    val (hour, minute, brtHour) = times
    val timestampMs = day(-3, hour) + (minute * 60_000L)
    val dateStr = prettyDate(timestampMs)

    println("$match")
    println("  UTC: ${String.format("%02d:%02d", hour, minute)} UTC")
    println("  BRT: ${String.format("%02d:%02d", brtHour, minute)} BRT")
    println("  Timestamp: $timestampMs ms")
    println("  Data/Hora: $dateStr")
    println()
}

println("=" * 70)
println("✅ VALIDAÇÃO COMPLETA!")
println("=" * 70)

