package kurou.kodriver.domain

/** 非致命的な例外を記録する共通入口。Android/JVM では Sentry に送信する。 */
expect fun captureError(throwable: Throwable)
