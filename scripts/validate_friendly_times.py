#!/usr/bin/env python3
"""
Validador de horários dos amistosos
"""

from datetime import datetime, timedelta, timezone

# BASE: 11 de junho de 2026 00:00 UTC
BASE_TIMESTAMP_MS = 1781136000000

def day_ms(offset: int, hour_utc: int = 16) -> int:
    """Calcular timestamp em ms para um dia com offset a partir da base"""
    return BASE_TIMESTAMP_MS + offset * 86_400_000 + hour_utc * 3_600_000

def ms_to_datetime(ms: int) -> datetime:
    """Converter timestamp em ms para datetime"""
    return datetime.fromtimestamp(ms / 1000, tz=timezone.utc)

print("=" * 70)
print("🔍 VALIDAÇÃO DE HORÁRIOS - AMISTOSOS 08/06/2026")
print("=" * 70)

# Amistosos com (UTC_hour, minute, BRT_hour_expected, dayOffset)
amistosos = {
    # dayOffset: -3 => 08/06/2026; -2 => 09/06/2026 UTC (23:00 BRT on 08/06)
    "Holanda × Uzbequistão": (18, 45, 15, -3),
    "França × Irlanda do Norte": (19, 10, 16, -3),
    "Peru × Espanha": (2, 0, 23, -2),  # 02:00 UTC on 09/06 => 23:00 BRT on 08/06
}

print("\n📊 ESPERADO VS ATUAL:\n")

for match, (hour, minute, brt_hour_expected, day_offset) in amistosos.items():
    timestamp_ms = day_ms(day_offset, hour) + (minute * 60_000)
    dt_utc = ms_to_datetime(timestamp_ms)

    # Converter para BRT (UTC-3 durante verão)
    tz_brt = timezone(timedelta(hours=-3))
    dt_brt = dt_utc.astimezone(tz_brt)

    print(f"{match}")
    print(f"  UTC: {hour:02d}:{minute:02d} UTC")
    print(f"  BRT (esperado): {brt_hour_expected:02d}:{minute:02d} BRT")
    print(f"  BRT (calculado): {dt_brt.strftime('%H:%M')} BRT")
    print(f"  Timestamp: {timestamp_ms} ms")
    print(f"  Data/Hora UTC: {dt_utc.strftime('%Y-%m-%d %H:%M:%S')} UTC")
    print(f"  Data/Hora BRT: {dt_brt.strftime('%Y-%m-%d %H:%M:%S')} BRT")

    # Validar
    if dt_brt.hour == brt_hour_expected and dt_brt.minute == minute:
        print("  ✅ CORRETO")
    else:
        print(f"  ❌ ERRADO - Esperado {brt_hour_expected:02d}:{minute:02d}, got {dt_brt.hour:02d}:{dt_brt.minute:02d}")

    print()

print("=" * 70)
print("✅ VALIDAÇÃO COMPLETA!")
print("=" * 70)


