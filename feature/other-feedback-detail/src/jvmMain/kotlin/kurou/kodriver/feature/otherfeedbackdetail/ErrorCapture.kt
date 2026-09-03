package kurou.kodriver.feature.otherfeedbackdetail

import io.sentry.Sentry

internal actual fun captureOtherFeedbackDetailError(throwable: Throwable) {
    Sentry.captureException(throwable)
}
