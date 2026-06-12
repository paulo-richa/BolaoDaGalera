#!/usr/bin/env python3
"""
Scraper dinâmico para pegar jogos amistosos do GE em tempo real
Usa Playwright para renderizar JavaScript
"""

import asyncio
import json
import re
from datetime import datetime
from playwright.async_api import async_playwright


async def scrape_ge_friendly_matches_today():
    """
    Scrapa jogos amistosos de hoje (08/06/2026) do Globo Esportes
    """
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context()
        page = await context.new_page()

        url = "https://ge.globo.com/agenda/#/futebol/08-06-2026"
        print(f"🔄 Acessando {url}...")

        try:
            await page.goto(url, wait_until="networkidle", timeout=30000)
            print("✅ Página carregada com sucesso!")

            # Aguardar 5 segundos para conteúdo renderizar
            await page.wait_for_timeout(5000)
            print("✅ Conteúdo renderizado!")

        except Exception as e:
            print(f"❌ Erro ao carregar página: {e}")
            await browser.close()
            return None

        # Extrair dados dos jogos
        try:
            # Executar JavaScript para extrair matches de forma genérica
            script = """
            () => {
                const matches = [];
                const allText = document.body.innerText;
                const lines = allText.split('\\n');

                // Buscar padrões de amistosos
                let i = 0;
                while (i < lines.length) {
                    const line = lines[i].trim();

                    // Procura por "Amistoso"
                    if (line.includes('Amistoso') || line.includes('amistoso')) {
                        // Próximas linhas contêm os times e horários
                        const context = lines.slice(Math.max(0, i-5), Math.min(lines.length, i+5)).join('|');
                        if (context) {
                            matches.push(context);
                        }
                    }
                    i++;
                }

                // Também tenta encontrar elementos com horários
                const timeElements = document.querySelectorAll('*');
                const timeMatches = [];

                timeElements.forEach(el => {
                    const text = el.textContent;
                    // Padrão: HH:MM
                    if (/\\d{1,2}:\\d{2}/.test(text) && text.length < 200) {
                        if (text.includes('×') || text.includes('x') || text.includes('vs')) {
                            timeMatches.push(text.trim());
                        }
                    }
                });

                return {
                    friendlyMatches: matches,
                    timeMatches: timeMatches.slice(0, 20)
                };
            }
            """

            data = await page.evaluate(script)
            print("\n📊 Dados extraídos via JavaScript:")
            print(json.dumps(data, indent=2, ensure_ascii=False)[:1000])

            # Parse dos dados
            friendly_matches = []

            for match_text in data.get('timeMatches', []):
                # Tentar extrair times e horário
                # Padrão: "Time1 x Time2" ou "Time1 × Time2"
                if '×' in match_text or 'x' in match_text.lower() or ' vs ' in match_text:
                    parts = re.split(r'×|x\s|\svs\s', match_text, flags=re.IGNORECASE)

                    # Extrair horário
                    time_match = re.search(r'(\d{1,2}):(\d{2})', match_text)

                    if len(parts) >= 2 and time_match:
                        friendly_matches.append({
                            "home_team": parts[0].strip(),
                            "away_team": parts[1].strip().split()[0] if parts[1].strip() else "N/A",
                            "time": time_match.group(0),
                            "competition": "Amistoso Pre-Copa"
                        })
                        print(f"  ✅ {parts[0].strip()} x {parts[1].strip().split()[0] if parts[1].strip() else 'N/A'} - {time_match.group(0)}")

            result = {
                "date": "08/06/2026",
                "source": "ge.globo.com",
                "timestamp": datetime.now().isoformat(),
                "friendly_matches": friendly_matches,
                "raw_data": data
            }

            print(f"\n✅ Total de amistosos encontrados: {len(friendly_matches)}")

            await browser.close()
            return result

        except Exception as e:
            print(f"❌ Erro ao extrair dados: {e}")
            await browser.close()
            return None


async def extract_from_page_content(page):
    """
    Extrai dados usando JavaScript ou HTML parsing alternativo
    """
    try:
        # Executar JavaScript para extrair dados do DOM
        script = """
        () => {
            const matches = [];
            const containers = document.querySelectorAll('[class*="match"], [class*="game"], [class*="Card"]');

            containers.forEach(container => {
                const text = container.textContent;
                if (text.includes('Amistoso') || text.includes('Friendly')) {
                    // Tentar extrair times e horários
                    const timeMatch = text.match(/(\\d{1,2}):(\\d{2})/);
                    matches.push({
                        text: text.substring(0, 200),
                        time: timeMatch ? timeMatch[0] : 'N/A'
                    });
                }
            });

            return matches;
        }
        """

        result = await page.evaluate(script)
        return result

    except Exception as e:
        print(f"Script evaluation error: {e}")
        return []


async def main():
    print("=" * 60)
    print("🌐 Scraper Dinâmico de Jogos Amistosos - GE")
    print("=" * 60)

    result = await scrape_ge_friendly_matches_today()

    if result:
        print("\n" + "=" * 60)
        print("📊 RESULTADO:")
        print("=" * 60)
        print(json.dumps(result, indent=2, ensure_ascii=False))

        # Salvar em arquivo JSON
        output_file = "/tmp/ge_friendly_matches.json"
        with open(output_file, "w", encoding="utf-8") as f:
            json.dump(result, f, indent=2, ensure_ascii=False)
        print(f"\n💾 Resultado salvo em: {output_file}")
    else:
        print("❌ Falha ao buscar dados")


if __name__ == "__main__":
    asyncio.run(main())



