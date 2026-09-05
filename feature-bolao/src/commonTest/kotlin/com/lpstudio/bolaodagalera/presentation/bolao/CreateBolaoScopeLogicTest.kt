package com.lpstudio.bolaodagalera.presentation.bolao

import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Championship
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CreateBolaoScopeLogicTest {
    private val libertadores = Championship(id = "LIBERTADORES", isGroupsAndKnockout = true, isPointsBased = false)
    private val brasileirao = Championship(id = "BRASILEIRAO", isGroupsAndKnockout = false, isPointsBased = true)

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
}
