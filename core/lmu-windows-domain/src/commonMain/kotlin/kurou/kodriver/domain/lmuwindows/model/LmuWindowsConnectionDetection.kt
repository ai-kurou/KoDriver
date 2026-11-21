package kurou.kodriver.domain.lmuwindows.model

/**
 * LMU の共有メモリで mCurrentET が変化しなくなってから、LMU が起動していないとみなすまでの時間（ミリ秒）。
 * OpenFileMapping が成功しても、LMU 終了後に別プロセスがセクションを保持している場合を検出するために使う。
 */
public const val LMU_WINDOWS_ET_STALE_THRESHOLD_MILLIS: Long = 3_000L

/**
 * LMU の共有メモリ上の scoring 車両数の上限（車両スロット数）。
 * 読み取った車両数をこの値で丸め、範囲外のオフセット参照を防ぐ。
 */
public const val LMU_WINDOWS_MAX_SCORING_VEHICLES: Int = 104
