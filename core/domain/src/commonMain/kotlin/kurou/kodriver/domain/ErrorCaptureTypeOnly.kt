package kurou.kodriver.domain

/**
 * 例外のメッセージやスタックを送らず、例外の種類だけを記録する。
 * 入力デバイス名などの端末情報が例外メッセージに混ざりうる箇所で使う。
 */
fun captureErrorTypeOnly(throwable: Throwable) {
    captureError(IllegalStateException("${throwable::class.simpleName ?: "UnknownThrowable"}"))
}
