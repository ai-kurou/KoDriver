package kurou.kodriver.domain.model

/**
 * 有限値の温度を摂氏の数値で返す。
 *
 * NaN・無限大は取得できなかった値として判定や最大値の対象から外す（NaN は roundToInt で例外になる）。
 */
public fun Iterable<CelsiusReading>.finiteCelsius(): List<Float> = map { it.value }.filter { it.isFinite() }
