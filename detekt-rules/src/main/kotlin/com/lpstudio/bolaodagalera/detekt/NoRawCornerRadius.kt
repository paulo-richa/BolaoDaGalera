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
 * BolaoDaGalera design-system rule: nenhum raio de canto pode ser um
 * `RoundedCornerShape(N.dp)` com valor literal - use `BolaoRadiusShape.xxx`
 * ou `MaterialTheme.shapes.xxx` (escala calibrada do :designsystem).
 */
class NoRawCornerRadius(config: Config = Config.empty) : Rule(config) {
    override val issue =
        Issue(
            javaClass.simpleName,
            Severity.Defect,
            "RoundedCornerShape com valor literal - use BolaoRadiusShape.xxx ou MaterialTheme.shapes.xxx.",
            Debt.FIVE_MINS
        )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text != "RoundedCornerShape") return
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
                "RoundedCornerShape com dp hardcoded - use BolaoRadiusShape.xxx ou MaterialTheme.shapes.xxx."
            )
        )
    }

    private fun isInsidePreview(expression: KtCallExpression): Boolean {
        val enclosingFunction = expression.getStrictParentOfType<KtNamedFunction>() ?: return false
        return enclosingFunction.annotationEntries.any { it.shortName?.asString() == "Preview" }
    }
}
