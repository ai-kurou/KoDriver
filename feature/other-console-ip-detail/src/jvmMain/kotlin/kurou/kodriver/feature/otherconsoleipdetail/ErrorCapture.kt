package kurou.kodriver.feature.otherconsoleipdetail

import io.sentry.Sentry

internal actual fun captureOtherConsoleIpDetailError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
