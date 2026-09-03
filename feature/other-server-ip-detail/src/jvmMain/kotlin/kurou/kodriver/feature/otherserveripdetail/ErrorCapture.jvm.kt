package kurou.kodriver.feature.otherserveripdetail

import io.sentry.Sentry

internal actual fun captureOtherServerIpDetailError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
