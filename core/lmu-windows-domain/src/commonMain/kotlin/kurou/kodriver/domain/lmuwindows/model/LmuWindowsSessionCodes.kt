package kurou.kodriver.domain.lmuwindows.model

/** LMU 共有メモリが報告するセッション種別コードのうち、テストデイのコード。 */
public const val LMU_WINDOWS_SESSION_TEST_DAY: Int = 0

/** LMU 共有メモリが報告するセッション種別コードのうち、練習走行のコード範囲。 */
public val LMU_WINDOWS_SESSION_PRACTICE_RANGE: IntRange = 1..4

/** LMU 共有メモリが報告するセッション種別コードのうち、予選のコード範囲。 */
public val LMU_WINDOWS_SESSION_QUALIFYING_RANGE: IntRange = 5..8

/** LMU 共有メモリが報告するセッション種別コードのうち、ウォームアップのコード。 */
public const val LMU_WINDOWS_SESSION_WARMUP: Int = 9

/** LMU 共有メモリが報告するセッション種別コードのうち、決勝のコード範囲。 */
public val LMU_WINDOWS_SESSION_RACE_RANGE: IntRange = 10..13
