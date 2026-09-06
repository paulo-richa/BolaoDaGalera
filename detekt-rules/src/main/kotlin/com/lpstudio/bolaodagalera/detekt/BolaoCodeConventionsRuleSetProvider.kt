package com.lpstudio.bolaodagalera.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

/**
 * General project-wide code conventions (as opposed to bolao-design-system,
 * which only applies to UI/screen code) - enabled in the main detekt.yml so it
 * runs across every module via the regular `detekt` task.
 */
class BolaoCodeConventionsRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "bolao-code-conventions"

    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            NoPortugueseIdentifiers(config)
        )
    )
}
