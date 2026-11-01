package kurou.kodriver.feature.otheroverlaytextsizedetail

import io.sentry.Sentry

internal actual fun captureOtherOverlayTextSizeDetailError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
