function mapPhase(stage) {
    switch (stage) {
        case "GROUP_STAGE": return "GROUP_STAGE";
        case "ROUND_OF_16":
        case "PLAY_OFFS": return "ROUND_OF_16";
        case "QUARTER_FINALS":
        case "QUARTERFINALS": return "QUARTERFINALS";
        case "SEMI_FINALS":
        case "SEMIFINALS": return "SEMIFINALS";
        case "FINAL": return "FINAL";
        default: return "GROUP_STAGE";
    }
}

module.exports = { mapPhase };
