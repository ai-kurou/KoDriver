package kurou.kodriver.feature.lmunarrator

import io.sentry.Sentry

internal actual fun captureNarratorError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
