package kurou.kodriver.domain.lmuwindows.model

/** LMU 共有メモリが報告するセッション種別コードのうち、テストデイのコード。 */
const val LMU_WINDOWS_SESSION_TEST_DAY = 0

/** LMU 共有メモリが報告するセッション種別コードのうち、練習走行のコード範囲。 */
val LMU_WINDOWS_SESSION_PRACTICE_RANGE = 1..4

/** LMU 共有メモリが報告するセッション種別コードのうち、予選のコード範囲。 */
val LMU_WINDOWS_SESSION_QUALIFYING_RANGE = 5..8

/** LMU 共有メモリが報告するセッション種別コードのうち、ウォームアップのコード。 */
const val LMU_WINDOWS_SESSION_WARMUP = 9

/** LMU 共有メモリが報告するセッション種別コードのうち、決勝のコード範囲。 */
val LMU_WINDOWS_SESSION_RACE_RANGE = 10..13
