package kurou.kodriver.domain

import io.sentry.Sentry

actual fun captureError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
