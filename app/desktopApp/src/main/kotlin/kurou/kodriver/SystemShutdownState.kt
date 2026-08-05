package kurou.kodriver

import java.util.concurrent.atomic.AtomicBoolean

/**
 * OS のシャットダウン・ログオフによる終了要求を検知したかどうかを保持する。
 *
 * Windows はシャットダウン時に各ウィンドウへ終了要求を送るが、そこで終了確認ダイアログを表示して
 * ユーザー操作を待つと「このアプリがシャットダウンを妨げています」と表示されてシャットダウンが
 * ブロックされる。JVM のシャットダウンフックでこのフラグを立て、確認ダイアログを省略して
 * 即座に終了できるようにする。
 */
internal object SystemShutdownState {
    private val shuttingDown = AtomicBoolean(false)

    /** OS 由来の終了要求を検知済みかどうか。 */
    val isShuttingDown: Boolean get() = shuttingDown.get()

    /** OS 由来の終了要求を検知したことを記録する。 */
    fun markShuttingDown() {
        shuttingDown.set(true)
    }

    /** テスト用に状態を初期化する。 */
    fun reset() {
        shuttingDown.set(false)
    }
}

/**
 * ウィンドウの閉じる要求に対して終了確認ダイアログを表示してよいかを判定する。
 *
 * 初期化が完了していない間（[ready] が false）はダイアログを表示する画面がまだ無く、
 * OS のシャットダウン中（[shuttingDown] が true）はユーザー操作を待てないため、
 * どちらの場合も確認せずに即座に終了する。
 */
internal fun shouldConfirmExit(
    ready: Boolean,
    shuttingDown: Boolean,
): Boolean = ready && !shuttingDown
