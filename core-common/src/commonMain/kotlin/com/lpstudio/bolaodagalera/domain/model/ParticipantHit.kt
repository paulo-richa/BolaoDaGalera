package com.lpstudio.bolaodagalera.domain.model

/** A single scored prediction of a participant, used to render their point-by-point history. */
data class ParticipantHit(val match: Match, val prediction: Prediction, val points: Int)
