package kurou.kodriver.feature.otheroverlaytextcolordetail

import io.sentry.Sentry

internal actual fun captureOtherOverlayTextColorDetailError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
