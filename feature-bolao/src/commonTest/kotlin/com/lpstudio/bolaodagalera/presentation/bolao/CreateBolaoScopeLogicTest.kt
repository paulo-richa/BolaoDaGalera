package com.lpstudio.bolaodagalera.presentation.bolao

import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Championship
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CreateBolaoScopeLogicTest {
    private val libertadores = Championship(id = "LIBERTADORES", isGroupsAndKnockout = true, isPointsBased = false)
    private val brasileirao = Championship(id = "BRASILEIRAO", isGroupsAndKnockout = false, isPointsBased = true)
    private val copaDoBrasil = Championship(id = "COPA_DO_BRASIL", isGroupsAndKnockout = false, isPointsBased = false)

    @Test
    fun `PONTOS_CORRIDOS nunca aparece para campeonato com grupos e mata-mata`() {
        assertFalse(
            isScopeVisible(BolaoScope.PONTOS_CORRIDOS, libertadores, isGroupStageAvailable = true, isKnockoutAvailable = true)
        )
    }

    @Test
    fun `PONTOS_CORRIDOS aparece para campeonato baseado em pontos`() {
        assertTrue(
            isScopeVisible(BolaoScope.PONTOS_CORRIDOS, brasileirao, isGroupStageAvailable = false, isKnockoutAvailable = false)
        )
    }

    @Test
    fun `adjustScopeForAvailability corrige PONTOS_CORRIDOS residual ao trocar pra campeonato so mata-mata`() {
        // Reproduz o bug real: currentScope ainda é PONTOS_CORRIDOS (do
        // campeonato anterior, baseado em pontos) no exato instante em que o
        // efeito de disponibilidade roda pro campeonato recém-selecionado.
        val corrected =
            adjustScopeForAvailability(
                currentScope = BolaoScope.PONTOS_CORRIDOS,
                championship = copaDoBrasil,
                isGroupStageAvailable = false,
                isKnockoutAvailable = true
            )
        assertTrue(corrected == BolaoScope.ONLY_KNOCKOUT)
    }
}
