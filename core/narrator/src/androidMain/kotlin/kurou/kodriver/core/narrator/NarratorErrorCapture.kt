package kurou.kodriver.core.narrator

import io.sentry.Sentry

actual fun captureNarratorError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
