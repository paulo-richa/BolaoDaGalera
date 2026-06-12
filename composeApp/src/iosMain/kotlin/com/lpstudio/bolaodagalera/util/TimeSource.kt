package com.lpstudio.bolaodagalera.util

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual object TimeSource {
    actual fun nowMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }
}
