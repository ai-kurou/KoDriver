package kurou.kodriver.core.narrator

/** ナレーター機能の非致命的な例外を記録するための共通入口。Android/JVM では Sentry に送信する。 */
expect fun captureNarratorError(throwable: Throwable)
