package kurou.kodriver.core.narrator

import io.sentry.Sentry

internal actual fun captureNarratorError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
