package com.lpstudio.bolaodagalera.presentation.bolao

import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Championship

/**
 * Pure scope resolution used when a new championship is selected: point-based
 * championships always use the league scope, and groups+knockout availability
 * determines whether the group stage can still be included.
 */
internal fun initialScopeForChampionship(championship: Championship, isGroupStageAvailable: Boolean): BolaoScope = when {
    championship.isPointsBased -> BolaoScope.PONTOS_CORRIDOS
    !championship.isGroupsAndKnockout -> BolaoScope.ONLY_KNOCKOUT
    !isGroupStageAvailable -> BolaoScope.ONLY_KNOCKOUT
    else -> BolaoScope.FULL
}

/**
 * Pure scope resolution reacting to phase availability changes (e.g. matches loading in),
 * falling back to the closest still-valid scope instead of an unavailable one.
 */
internal fun adjustScopeForAvailability(
    currentScope: BolaoScope,
    championship: Championship,
    isGroupStageAvailable: Boolean,
    isKnockoutAvailable: Boolean
): BolaoScope {
    if (championship.isPointsBased) return BolaoScope.PONTOS_CORRIDOS

    val isFullValid = isGroupStageAvailable && isKnockoutAvailable
    val isOnlyGroupsValid = isGroupStageAvailable
    val isOnlyKnockoutValid = isKnockoutAvailable

    return when (currentScope) {
        BolaoScope.FULL ->
            if (isFullValid) currentScope else resolveFullScopeFallback(isOnlyGroupsValid, isOnlyKnockoutValid)
        BolaoScope.ONLY_GROUPS ->
            if (isOnlyGroupsValid) {
                currentScope
            } else {
                resolveScopeFallback(isOnlyKnockoutValid, BolaoScope.ONLY_KNOCKOUT, BolaoScope.ONLY_GROUPS)
            }
        BolaoScope.ONLY_KNOCKOUT ->
            if (isOnlyKnockoutValid) {
                currentScope
            } else {
                resolveScopeFallback(isOnlyGroupsValid, BolaoScope.ONLY_GROUPS, BolaoScope.ONLY_KNOCKOUT)
            }
        else -> currentScope
    }
}

private fun resolveFullScopeFallback(isOnlyGroupsValid: Boolean, isOnlyKnockoutValid: Boolean): BolaoScope = when {
    isOnlyGroupsValid -> BolaoScope.ONLY_GROUPS
    isOnlyKnockoutValid -> BolaoScope.ONLY_KNOCKOUT
    else -> BolaoScope.FULL
}

private fun resolveScopeFallback(condition: Boolean, ifTrue: BolaoScope, ifFalse: BolaoScope): BolaoScope =
    if (condition) ifTrue else ifFalse

// Scope visibility filters based on championship and match dates
internal fun isScopeVisible(
    scope: BolaoScope,
    championship: Championship,
    isGroupStageAvailable: Boolean,
    isKnockoutAvailable: Boolean
): Boolean = when (scope) {
    BolaoScope.ONLY_GROUPS -> championship.isGroupsAndKnockout && isGroupStageAvailable
    BolaoScope.ONLY_KNOCKOUT ->
        (championship.isGroupsAndKnockout || !championship.isPointsBased) && isKnockoutAvailable
    BolaoScope.FULL -> championship.isGroupsAndKnockout && isGroupStageAvailable && isKnockoutAvailable
    // Only a points-based championship (e.g. Brasileirão) offers this scope - a
    // groups-and-knockout championship (e.g. Libertadores) only ever has the two
    // phase-specific scopes above (plus FULL), never a flat "league table".
    BolaoScope.PONTOS_CORRIDOS -> championship.isPointsBased
}

internal fun isScopeEnabled(scope: BolaoScope, isGroupStageAvailable: Boolean, isKnockoutAvailable: Boolean): Boolean = when (scope) {
    BolaoScope.FULL -> isGroupStageAvailable && isKnockoutAvailable
    BolaoScope.ONLY_GROUPS -> isGroupStageAvailable
    BolaoScope.ONLY_KNOCKOUT -> isKnockoutAvailable
    BolaoScope.PONTOS_CORRIDOS -> true
}

internal fun scopeErrorMessage(
    scope: BolaoScope,
    isEnabled: Boolean,
    isGroupStageAvailable: Boolean,
    groupsClosedText: String,
    knockoutClosedText: String
): String? = when {
    (scope == BolaoScope.FULL || scope == BolaoScope.ONLY_GROUPS) && !isGroupStageAvailable -> groupsClosedText
    scope == BolaoScope.ONLY_KNOCKOUT && !isEnabled -> knockoutClosedText
    else -> null
}
