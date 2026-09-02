package com.lpstudio.bolaodagalera.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

/**
 * BolaoDaGalera design-system rule: `tonalElevation`/`shadowElevation` must
 * not receive a literal `N.dp` value - use `BolaoElevation.levelX` (the 6
 * official Material 3 tonal elevation levels) instead.
 */
class NoRawElevation(config: Config = Config.empty) : Rule(config) {
    override val issue =
        Issue(
            javaClass.simpleName,
            Severity.Defect,
            "tonalElevation/shadowElevation com valor literal - use BolaoElevation.levelX.",
            Debt.FIVE_MINS
        )

    private val targetParamNames = setOf("tonalElevation", "shadowElevation")

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (isInsidePreview(expression)) return

        expression.valueArguments.filterIsInstance<KtValueArgument>().forEach { arg ->
            val argumentName = arg.getArgumentName()?.asName?.asString() ?: return@forEach
            if (argumentName !in targetParamNames) return@forEach

            val dotExpression = arg.getArgumentExpression() as? KtDotQualifiedExpression ?: return@forEach
            if (dotExpression.selectorExpression?.text != "dp") return@forEach
            if (dotExpression.receiverExpression.text.toDoubleOrNull() == null) return@forEach

            report(
                CodeSmell(
                    issue,
                    Entity.from(arg),
                    "$argumentName hardcoded (${dotExpression.text}) - use BolaoElevation.levelX."
                )
            )
        }
    }

    private fun isInsidePreview(expression: KtCallExpression): Boolean {
        val enclosingFunction = expression.getStrictParentOfType<KtNamedFunction>() ?: return false
        return enclosingFunction.annotationEntries.any { it.shortName?.asString() == "Preview" }
    }
}
