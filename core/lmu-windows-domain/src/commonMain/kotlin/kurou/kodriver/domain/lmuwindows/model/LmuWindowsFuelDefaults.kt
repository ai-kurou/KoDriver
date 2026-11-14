package kurou.kodriver.domain.lmuwindows.model

/**
 * LMU の燃料値の鮮度上限（ミリ秒）。WebSocket で燃料を受信してからこの時間が経過すると、
 * 燃料値を空（未取得相当）に戻す。
 */
const val LMU_WINDOWS_FUEL_MAX_AGE_MS = 5_000L
