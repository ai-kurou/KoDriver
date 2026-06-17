package kurou.kodriver.feature.lmuwindowsnarrator

import io.sentry.Sentry

internal actual fun captureNarratorError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
