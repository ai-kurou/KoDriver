package kurou.kodriver.feature.otheroverlaybackgroundopacitydetail

import io.sentry.Sentry

internal actual fun captureOtherOverlayBackgroundOpacityDetailError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
