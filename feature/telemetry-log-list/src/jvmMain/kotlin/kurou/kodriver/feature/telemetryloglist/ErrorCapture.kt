package kurou.kodriver.feature.telemetryloglist

import io.sentry.Sentry

internal actual fun captureTelemetryLogListError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
