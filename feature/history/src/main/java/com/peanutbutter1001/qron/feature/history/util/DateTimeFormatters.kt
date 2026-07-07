package com.peanutbutter1001.qron.feature.history.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val displayFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

fun LocalDateTime.toDisplayString(): String = this.format(displayFormatter)
