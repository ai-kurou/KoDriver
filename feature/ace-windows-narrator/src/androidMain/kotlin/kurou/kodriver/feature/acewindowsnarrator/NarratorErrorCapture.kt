package kurou.kodriver.feature.acewindowsnarrator

import io.sentry.Sentry

internal actual fun captureNarratorError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
