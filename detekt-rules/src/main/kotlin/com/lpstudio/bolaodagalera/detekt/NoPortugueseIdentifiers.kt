package com.lpstudio.bolaodagalera.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtNamedDeclaration

/**
 * BolaoDaGalera project convention: identifiers (classes, functions, properties,
 * parameters) must be in English. This only catches Portuguese-specific
 * accented/cedilla characters (ã, õ, ç, é, ê, í, ó, ú, à, â, ô) - no legitimate
 * English identifier contains them, so this has zero false positives, but it
 * doesn't catch unaccented Portuguese words (e.g. "usuario", "convidar").
 */
class NoPortugueseIdentifiers(config: Config = Config.empty) : Rule(config) {
    override val issue =
        Issue(
            javaClass.simpleName,
            Severity.Defect,
            "Identificador com caractere de português (acento/cedilha) - nomes de classe, função, propriedade " +
                "e parâmetro devem ser em inglês.",
            Debt.FIVE_MINS
        )

    override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
        super.visitNamedDeclaration(declaration)
        val name = declaration.name ?: return
        if (name.any { it in PORTUGUESE_CHARS }) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(declaration),
                    "Identificador '$name' contém caractere de português - use um nome em inglês."
                )
            )
        }
    }

    private companion object {
        private val PORTUGUESE_CHARS =
            "ãõçéêíóúàâôÃÕÇÉÊÍÓÚÀÂÔ".toSet()
    }
}
