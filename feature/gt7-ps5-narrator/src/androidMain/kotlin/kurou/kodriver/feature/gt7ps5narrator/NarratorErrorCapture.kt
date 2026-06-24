package kurou.kodriver.feature.gt7ps5narrator

import io.sentry.Sentry

internal actual fun captureNarratorError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
