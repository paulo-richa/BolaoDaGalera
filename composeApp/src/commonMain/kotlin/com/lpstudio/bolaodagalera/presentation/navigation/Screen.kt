package com.lpstudio.bolaodagalera.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
object Register

@Serializable
object Home

@Serializable
object CreateBolao

@Serializable
object JoinBolao

@Serializable
data class BolaoDetail(val bolaoId: String)

@Serializable
data class Prediction(val bolaoId: String, val matchId: String)

@Serializable
data class AddParticipants(val bolaoId: String)

@Serializable
data class EditBolao(val bolaoId: String)

@Serializable
data class MatchPredictions(val bolaoId: String, val matchId: String)

@Serializable
object Profile
