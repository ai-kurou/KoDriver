package kurou.kodriver.domain.usecase

/**
 * 高温警告のヒステリシス状態を判定する。
 *
 * いずれかの値が [highThresholdCelsius] 以上なら有効、すべての値が
 * [highThresholdCelsius] から [hysteresisCelsius] を引いた値以下なら無効にし、
 * その間は [currentState] を維持する。[valuesCelsius] が空（有効な値がない）の場合も [currentState] を維持する。
 */
public fun determineHysteresisState(
    valuesCelsius: Iterable<Float>,
    highThresholdCelsius: Float,
    hysteresisCelsius: Float,
    currentState: Boolean,
): Boolean {
    val coolThresholdCelsius = highThresholdCelsius - hysteresisCelsius
    return when {
        valuesCelsius.none() -> currentState
        valuesCelsius.any { it >= highThresholdCelsius } -> true
        valuesCelsius.all { it <= coolThresholdCelsius } -> false
        else -> currentState
    }
}
