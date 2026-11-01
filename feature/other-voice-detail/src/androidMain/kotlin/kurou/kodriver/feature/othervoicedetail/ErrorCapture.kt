package kurou.kodriver.feature.othervoicedetail

import io.sentry.Sentry

internal actual fun captureOtherVoiceDetailError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
