package com.lpstudio.bolaodagalera.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class BolaoRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "bolao-design-system"

    override fun instance(config: Config): RuleSet =
        RuleSet(
            ruleSetId,
            listOf(
                NoHardcodedStringInDesignSystem(config)
            )
        )
}
