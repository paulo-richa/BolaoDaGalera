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
 * BolaoDaGalera design-system rule: `.padding(...)` e `Arrangement.spacedBy(...)`
 * não podem receber valores `N.dp` literais - use `BolaoSpacing.xxx` (escala
 * de espaçamento do :designsystem).
 */
class NoRawSpacingDp(config: Config = Config.empty) : Rule(config) {
    override val issue =
        Issue(
            javaClass.simpleName,
            Severity.Defect,
            "padding/spacedBy com dp hardcoded - use BolaoSpacing.xxx.",
            Debt.FIVE_MINS
        )

    private val targetCalleeNames = setOf("padding", "spacedBy")

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val calleeName = expression.calleeExpression?.text ?: return
        if (calleeName !in targetCalleeNames) return
        if (isInsidePreview(expression)) return

        val hasLiteralDp =
            expression.valueArguments.filterIsInstance<KtValueArgument>().any { arg ->
                val dotExpression = arg.getArgumentExpression() as? KtDotQualifiedExpression ?: return@any false
                dotExpression.selectorExpression?.text == "dp" && dotExpression.receiverExpression.text.toDoubleOrNull() != null
            }
        if (!hasLiteralDp) return

        report(
            CodeSmell(
                issue,
                Entity.from(expression),
                "$calleeName(...) com dp hardcoded - use BolaoSpacing.xxx."
            )
        )
    }

    private fun isInsidePreview(expression: KtCallExpression): Boolean {
        val enclosingFunction = expression.getStrictParentOfType<KtNamedFunction>() ?: return false
        return enclosingFunction.annotationEntries.any { it.shortName?.asString() == "Preview" }
    }
}
