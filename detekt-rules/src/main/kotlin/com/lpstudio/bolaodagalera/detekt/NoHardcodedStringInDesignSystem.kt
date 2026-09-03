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
 * BolaoDaGalera design-system rule: no user-visible text may be a string
 * literal - it must come from stringResource(Res.string.xxx).
 * Flags string literals passed to the text parameters of `Bolao*`
 * :designsystem components.
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
            "BolaoEmptyState",
            "BolaoIcon"
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
     * Catches the other place a hardcoded literal can hide: a default parameter
     * value on a `Bolao*` component's own declaration (e.g. `dismissText: String
     * = "Cancelar"`). [visitCallExpression] only sees call-site arguments, so a
     * caller that simply omits the parameter and relies on this default would
     * otherwise never be flagged.
     */
    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        val functionName = function.name ?: return
        if (functionName !in targetFunctionNames) return
        if (function.annotationEntries.any { it.shortName?.asString() == "Preview" }) return

        function.valueParameters.forEach { parameter ->
            if (parameter.name !in textParamNames) return@forEach
            val template = parameter.defaultValue as? KtStringTemplateExpression ?: return@forEach
            if (hasHardcodedText(template)) {
                report(
                    CodeSmell(
                        issue,
                        Entity.from(parameter),
                        "Valor padrão hardcoded em $functionName(${parameter.name}) - extraia pra stringResource()."
                    )
                )
            }
        }
    }

    /**
     * @Preview functions intentionally use sample data - it is not real text
     * shown to users in production, so they are exempt from this rule.
     */
    private fun isInsidePreview(expression: KtCallExpression): Boolean {
        val enclosingFunction = expression.getStrictParentOfType<KtNamedFunction>() ?: return false
        return enclosingFunction.annotationEntries.any { it.shortName?.asString() == "Preview" }
    }

    private fun hasHardcodedText(template: KtStringTemplateExpression): Boolean = template.entries.any { entry ->
        entry is KtLiteralStringTemplateEntry && entry.text.any { it.isLetter() }
    }
}
