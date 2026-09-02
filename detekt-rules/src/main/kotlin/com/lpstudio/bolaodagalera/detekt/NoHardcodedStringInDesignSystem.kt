package com.lpstudio.bolaodagalera.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

/**
 * BolaoDaGalera design-system rule: nenhum texto visível ao usuário pode ser
 * um literal de string - tudo precisa vir de stringResource(Res.string.xxx).
 * Flags string literais passadas pros parâmetros de texto de componentes
 * `Bolao*` do :designsystem.
 */
class NoHardcodedStringInDesignSystem(config: Config = Config.empty) : Rule(config) {
    override val issue =
        Issue(
            javaClass.simpleName,
            Severity.Defect,
            "Componentes do :designsystem (Bolao*) não podem receber texto hardcoded - use stringResource(Res.string.xxx).",
            Debt.FIVE_MINS
        )

    private val targetFunctionNames =
        setOf(
            "BolaoText",
            "BolaoButton",
            "BolaoOutlinedButton",
            "BolaoTextButton",
            "BolaoTextField",
            "BolaoConfirmDialog",
            "BolaoTopBar",
            "BolaoChip",
            "BolaoEmptyState"
        )

    private val textParamNames =
        setOf("text", "title", "message", "confirmText", "dismissText", "label", "contentDescription")

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val calleeName = expression.calleeExpression?.text ?: return
        if (calleeName !in targetFunctionNames) return
        if (isInsidePreview(expression)) return

        expression.valueArguments.filterIsInstance<KtValueArgument>().forEachIndexed { index, arg ->
            val argumentName = arg.getArgumentName()?.asName?.asString()
            val isTextPositional = index == 0 && argumentName == null
            val isTextNamed = argumentName in textParamNames
            if (!isTextPositional && !isTextNamed) return@forEachIndexed

            val template = arg.getArgumentExpression() as? KtStringTemplateExpression ?: return@forEachIndexed
            if (hasHardcodedText(template)) {
                report(
                    CodeSmell(
                        issue,
                        Entity.from(arg),
                        "String hardcoded em $calleeName(...) - extraia pra strings.xml e use stringResource()."
                    )
                )
            }
        }
    }

    /**
     * Funções @Preview usam dados de demonstração de propósito - não são texto
     * real exibido ao usuário em produção, então ficam fora da regra.
     */
    private fun isInsidePreview(expression: KtCallExpression): Boolean {
        val enclosingFunction = expression.getStrictParentOfType<KtNamedFunction>() ?: return false
        return enclosingFunction.annotationEntries.any { it.shortName?.asString() == "Preview" }
    }

    private fun hasHardcodedText(template: KtStringTemplateExpression): Boolean =
        template.entries.any { entry ->
            entry is KtLiteralStringTemplateEntry && entry.text.any { it.isLetter() }
        }
}
