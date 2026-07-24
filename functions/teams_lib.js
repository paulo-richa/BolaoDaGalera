const LIB_TEAMS = {
    // ARGENTINA
    "River Plate": { name: "River Plate", flag: "", code: "RIV", crest: "https://crests.football-data.org/1106.png" },
    "Club Atlético River Plate": { name: "River Plate", flag: "", code: "RIV", crest: "https://crests.football-data.org/1106.png" },
    "Boca Juniors": { name: "Boca Juniors", flag: "", code: "BOC", crest: "https://crests.football-data.org/1105.png" },
    "Club Atlético Boca Juniors": { name: "Boca Juniors", flag: "", code: "BOC", crest: "https://crests.football-data.org/1105.png" },
    "CA Talleres": { name: "Talleres", flag: "", code: "TAL", crest: "https://crests.football-data.org/1104.png" },
    "Club Atlético Talleres": { name: "Talleres", flag: "", code: "TAL", crest: "https://crests.football-data.org/1104.png" },
    "Estudiantes": { name: "Estudiantes", flag: "", code: "EST", crest: "https://crests.football-data.org/1108.png" },
    "Club Estudiantes de La Plata": { name: "Estudiantes", flag: "", code: "EST", crest: "https://crests.football-data.org/1108.png" },
    "Platense": { name: "Platense", flag: "", code: "PLA", crest: "https://crests.football-data.org/7580.png" },
    "CA Platense": { name: "Platense", flag: "", code: "PLA", crest: "https://crests.football-data.org/7580.png" },
    "Club Atlético Platense": { name: "Platense", flag: "", code: "PLA", crest: "https://crests.football-data.org/7580.png" },
    "Ind. Rivadavia": { name: "Ind. Rivadavia", flag: "", code: "IRV", crest: "https://crests.football-data.org/2052.png" },
    "Independiente Rivadavia": { name: "Ind. Rivadavia", flag: "", code: "IRV", crest: "https://crests.football-data.org/2052.png" },

    // URUGUAI
    "Peñarol": { name: "Peñarol", flag: "", code: "PEN", crest: "https://crests.football-data.org/1805.png" },
    "Club Atlético Peñarol": { name: "Peñarol", flag: "", code: "PEN", crest: "https://crests.football-data.org/1805.png" },
    "Nacional": { name: "Nacional", flag: "", code: "NAC", crest: "https://crests.football-data.org/1802.png" },
    "Club Nacional de Football": { name: "Nacional", flag: "", code: "NAC", crest: "https://crests.football-data.org/1802.png" },

    // CHILE / PARAGUAI / EQUADOR
    "Colo Colo": { name: "Colo-Colo", flag: "", code: "COL", crest: "https://crests.football-data.org/1792.png" },
    "Club Social y Deportivo Colo-Colo": { name: "Colo-Colo", flag: "", code: "COL", crest: "https://crests.football-data.org/1792.png" },
    "Cerro Porteño": { name: "Cerro Porteño", flag: "", code: "CCP", crest: "https://crests.football-data.org/9373.png" },
    "Club Cerro Porteño": { name: "Cerro Porteño", flag: "", code: "CCP", crest: "https://crests.football-data.org/9373.png" },
    "LDU": { name: "LDU", flag: "", code: "LDU", crest: "https://crests.football-data.org/4528.png" },
    "LDU de Quito": { name: "LDU", flag: "", code: "LDU", crest: "https://crests.football-data.org/4528.png" },
    "Liga Deportiva Universitaria": { name: "LDU", flag: "", code: "LDU", crest: "https://crests.football-data.org/4528.png" },
    "Independiente del Valle": { name: "Ind. del Valle", flag: "", code: "IDV", crest: "https://crests.football-data.org/6989.png" },
    "Club de Alto Rendimiento Especializado Independiente del Valle": { name: "Ind. del Valle", flag: "", code: "IDV", crest: "https://crests.football-data.org/6989.png" },
    "Coquimbo": { name: "Coquimbo", flag: "", code: "COQ", crest: "https://crests.football-data.org/7912.png" },
    "Coquimbo Unido": { name: "Coquimbo", flag: "", code: "COQ", crest: "https://crests.football-data.org/7912.png" },

    // COLÔMBIA
    "Junior FC": { name: "Junior", flag: "", code: "JUN", crest: "https://crests.football-data.org/1799.png" },
    "Club Deportivo Popular Junior FC": { name: "Junior", flag: "", code: "JUN", crest: "https://crests.football-data.org/1799.png" },

    // BRASILEIROS (SINCRONIZADOS COM TEAMS_BR.JS)
    "Palmeiras": { name: "Palmeiras", flag: "", code: "PAL", crest: "https://crests.football-data.org/1769.png" },
    "SE Palmeiras": { name: "Palmeiras", flag: "", code: "PAL", crest: "https://crests.football-data.org/1769.png" },
    "Flamengo": { name: "Flamengo", flag: "", code: "FLA", crest: "https://crests.football-data.org/1783.png" },
    "CR Flamengo": { name: "Flamengo", flag: "", code: "FLA", crest: "https://crests.football-data.org/1783.png" },
    "Atlético-MG": { name: "Atlético-MG", flag: "", code: "CAM", crest: "https://crests.football-data.org/1766.png" },
    "CA Mineiro": { name: "Atlético-MG", flag: "", code: "CAM", crest: "https://crests.football-data.org/1766.png" },
    "Botafogo": { name: "Botafogo", flag: "", code: "BOT", crest: "https://crests.football-data.org/1770.png" },
    "Botafogo FR": { name: "Botafogo", flag: "", code: "BOT", crest: "https://crests.football-data.org/1770.png" },
    "Fluminense": { name: "Fluminense", flag: "", code: "FLU", crest: "https://crests.football-data.org/1765.png" },
    "Fluminense FC": { name: "Fluminense", flag: "", code: "FLU", crest: "https://crests.football-data.org/1765.png" },
    "Grêmio": { name: "Grêmio", flag: "", code: "GRE", crest: "https://crests.football-data.org/1767.png" },
    "Grêmio FBPA": { name: "Grêmio", flag: "", code: "GRE", crest: "https://crests.football-data.org/1767.png" },
    "São Paulo": { name: "São Paulo", flag: "", code: "SAO", crest: "https://crests.football-data.org/1776.png" },
    "São Paulo FC": { name: "São Paulo", flag: "", code: "SAO", crest: "https://crests.football-data.org/1776.png" }
};

module.exports = { LIB_TEAMS };
