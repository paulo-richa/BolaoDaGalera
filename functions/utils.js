function mapPhase(stage) {
    switch (stage) {
        case "GROUP_STAGE":
        // The Champions League's Swiss-format league phase (36 clubs, single
        // table) has no "groups" - treated as GROUP_STAGE internally since
        // Championship.isGroupsAndKnockout only distinguishes "has a knockout
        // stage or not", with no separate model for the Swiss format.
        case "LEAGUE_STAGE": return "GROUP_STAGE";
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
