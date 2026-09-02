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
 * BolaoDaGalera design-system rule: no font size may be a literal `N.sp` -
 * use `style = BolaoTypography.xxx` (the calibrated :designsystem type
 * scale) instead of `fontSize = N.sp`.
 */
class NoRawFontSize(config: Config = Config.empty) : Rule(config) {
    override val issue =
        Issue(
            javaClass.simpleName,
            Severity.Defect,
            "fontSize com valor literal - use style = BolaoTypography.xxx em vez de fontSize/fontWeight soltos.",
            Debt.FIVE_MINS
        )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (isInsidePreview(expression)) return

        expression.valueArguments.filterIsInstance<KtValueArgument>().forEach { arg ->
            val argumentName = arg.getArgumentName()?.asName?.asString() ?: return@forEach
            if (argumentName != "fontSize") return@forEach

            val dotExpression = arg.getArgumentExpression() as? KtDotQualifiedExpression ?: return@forEach
            if (dotExpression.selectorExpression?.text != "sp") return@forEach
            if (dotExpression.receiverExpression.text.toDoubleOrNull() == null) return@forEach

            report(
                CodeSmell(
                    issue,
                    Entity.from(arg),
                    "fontSize hardcoded (${dotExpression.text}) - use style = BolaoTypography.xxx."
                )
            )
        }
    }

    private fun isInsidePreview(expression: KtCallExpression): Boolean {
        val enclosingFunction = expression.getStrictParentOfType<KtNamedFunction>() ?: return false
        return enclosingFunction.annotationEntries.any { it.shortName?.asString() == "Preview" }
    }
}
