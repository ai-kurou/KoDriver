package kurou.kodriver.domain

import io.sentry.Sentry

public actual fun captureError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
