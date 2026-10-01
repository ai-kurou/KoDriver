package kurou.kodriver.domain

/** ドメイン処理の非致命的な例外を記録する。Android/JVM では Sentry に送信する。 */
internal expect fun captureDomainError(throwable: Throwable)
