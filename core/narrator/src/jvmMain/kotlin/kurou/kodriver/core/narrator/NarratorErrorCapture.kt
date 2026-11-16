package kurou.kodriver.core.narrator

import io.sentry.Sentry

public actual fun captureNarratorError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
