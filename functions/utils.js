function mapPhase(stage) {
    switch (stage) {
        case "GROUP_STAGE": return "GROUP_STAGE";
        case "ROUND_OF_16": return "ROUND_OF_16";
        case "QUARTER_FINALS": return "QUARTERFINALS";
        case "SEMI_FINALS": return "SEMIFINALS";
        case "FINAL": return "FINAL";
        default: return "GROUP_STAGE";
    }
}

module.exports = { mapPhase };
