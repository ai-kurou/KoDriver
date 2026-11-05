package kurou.kodriver.core.speechrecognitiondata.speech

/** `RecognitionListener.onRmsChanged` が通知する音量（dB）のおおよその下限。この値以下は無音として扱う。 */
internal const val RECOGNITION_RMS_DB_MIN = -2f

/** `RecognitionListener.onRmsChanged` が通知する音量（dB）のおおよその上限。この値以上は最大として扱う。 */
internal const val RECOGNITION_RMS_DB_MAX = 10f

/** 認識エンジンが通知する音量（dB）を 0.0（無音）〜1.0（大音量）に正規化する。範囲外の値は端に丸め、NaN は無音とする。 */
internal fun normalizeRecognitionRmsDb(rmsdB: Float): Float =
    if (rmsdB.isNaN()) {
        0f
    } else {
        ((rmsdB - RECOGNITION_RMS_DB_MIN) / (RECOGNITION_RMS_DB_MAX - RECOGNITION_RMS_DB_MIN)).coerceIn(0f, 1f)
    }
