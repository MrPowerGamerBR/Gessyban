package net.perfectdreams.gessyban

import kotlin.js.Date

actual fun currentTimeMillis(): Long = Date.now().toLong()
