// Same format as teams_lib.js: API's official team name -> display data.
// Populated from the real Champions League 2026 league-phase fixtures
// (football-data.org, matchday 1) - not a guess.
const CL_TEAMS = {
    // ENGLAND
    "Arsenal FC": { name: "Arsenal", flag: "", code: "ARS", crest: "https://crests.football-data.org/57.png" },
    "Aston Villa FC": { name: "Aston Villa", flag: "", code: "AVL", crest: "https://crests.football-data.org/58.png" },
    "Liverpool FC": { name: "Liverpool", flag: "", code: "LIV", crest: "https://crests.football-data.org/64.png" },
    "Manchester City FC": { name: "Man City", flag: "", code: "MCI", crest: "https://crests.football-data.org/65.png" },
    "Manchester United FC": { name: "Man United", flag: "", code: "MUN", crest: "https://crests.football-data.org/66.png" },

    // SPAIN
    "Club Atlético de Madrid": { name: "Atlético Madrid", flag: "", code: "ATM", crest: "https://crests.football-data.org/78.png" },
    "FC Barcelona": { name: "Barcelona", flag: "", code: "BAR", crest: "https://crests.football-data.org/81.png" },
    "Real Betis Balompié": { name: "Real Betis", flag: "", code: "BET", crest: "https://crests.football-data.org/90.png" },
    "Real Madrid CF": { name: "Real Madrid", flag: "", code: "RMA", crest: "https://crests.football-data.org/86.png" },
    "Villarreal CF": { name: "Villarreal", flag: "", code: "VIL", crest: "https://crests.football-data.org/94.png" },

    // GERMANY
    "Borussia Dortmund": { name: "Dortmund", flag: "", code: "BVB", crest: "https://crests.football-data.org/4.png" },
    "FC Bayern München": { name: "Bayern München", flag: "", code: "BAY", crest: "https://crests.football-data.org/5.png" },
    "RB Leipzig": { name: "RB Leipzig", flag: "", code: "RBL", crest: "https://crests.football-data.org/721.png" },
    "VfB Stuttgart": { name: "Stuttgart", flag: "", code: "STU", crest: "https://crests.football-data.org/10.png" },

    // ITALY
    "AS Roma": { name: "Roma", flag: "", code: "ROM", crest: "https://crests.football-data.org/100.png" },
    "Como 1907": { name: "Como", flag: "", code: "COM", crest: "https://crests.football-data.org/7397.png" },
    "FC Internazionale Milano": { name: "Inter", flag: "", code: "INT", crest: "https://crests.football-data.org/108.png" },
    "SSC Napoli": { name: "Napoli", flag: "", code: "NAP", crest: "https://crests.football-data.org/113.png" },

    // FRANCE
    "Lille OSC": { name: "Lille", flag: "", code: "LIL", crest: "https://crests.football-data.org/521.png" },
    "Paris Saint-Germain FC": { name: "PSG", flag: "", code: "PSG", crest: "https://crests.football-data.org/524.png" },
    "Racing Club de Lens": { name: "Lens", flag: "", code: "LEN", crest: "https://crests.football-data.org/546.png" },

    // PORTUGAL
    "FC Porto": { name: "Porto", flag: "", code: "POR", crest: "https://crests.football-data.org/503.png" },
    "Sporting Clube de Portugal": { name: "Sporting CP", flag: "", code: "SPO", crest: "https://crests.football-data.org/498.png" },

    // NETHERLANDS
    "Feyenoord Rotterdam": { name: "Feyenoord", flag: "", code: "FEY", crest: "https://crests.football-data.org/675.png" },
    "PSV": { name: "PSV", flag: "", code: "PSV", crest: "https://crests.football-data.org/674.png" },

    // BELGIUM
    "Club Brugge KV": { name: "Club Brugge", flag: "", code: "BRU", crest: "https://crests.football-data.org/851.png" },

    // TURKEY
    "Fenerbahçe SK": { name: "Fenerbahçe", flag: "", code: "FEN", crest: "https://crests.football-data.org/613.png" },
    "Galatasaray SK": { name: "Galatasaray", flag: "", code: "GAL", crest: "https://crests.football-data.org/610.png" },

    // NORWAY
    "FK Bodø/Glimt": { name: "Bodø/Glimt", flag: "", code: "BOD", crest: "https://crests.football-data.org/5721.png" },
    "Viking FK": { name: "Viking", flag: "", code: "VIK", crest: "https://crests.football-data.org/5720.png" },

    // UKRAINE
    "FK Shakhtar Donetsk": { name: "Shakhtar", flag: "", code: "SHA", crest: "https://crests.football-data.org/1887.png" },

    // AUSTRIA
    "LASK Linz": { name: "LASK", flag: "", code: "LAS", crest: "https://crests.football-data.org/2016.png" },

    // CZECH REPUBLIC
    "SK Slavia Praha": { name: "Slavia Praha", flag: "", code: "SLA", crest: "https://crests.football-data.org/930.png" },

    // GREECE
    "PAE AEK": { name: "AEK Athens", flag: "", code: "AEK", crest: "https://crests.football-data.org/1899.png" },

    // AZERBAIJAN
    "Sabah FK": { name: "Sabah", flag: "", code: "SAB", crest: "https://crests.football-data.org/10233.png" },

    // SLOVAKIA
    "ŠK Slovan Bratislava": { name: "Slovan Bratislava", flag: "", code: "SLO", crest: "https://crests.football-data.org/7509.png" }
};

module.exports = { CL_TEAMS };
