package kurou.kodriver.domain

import io.sentry.Sentry

internal actual fun captureDomainError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
