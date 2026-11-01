package kurou.kodriver.feature.otherthemedetail

import io.sentry.Sentry

internal actual fun captureOtherThemeDetailError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
