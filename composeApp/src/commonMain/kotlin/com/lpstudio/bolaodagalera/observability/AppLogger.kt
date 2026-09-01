package com.lpstudio.bolaodagalera.observability

import co.touchlab.kermit.Logger

fun appLogger(tag: String): Logger = Logger.withTag(tag)
