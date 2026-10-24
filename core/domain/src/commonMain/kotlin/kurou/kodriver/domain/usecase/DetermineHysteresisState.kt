package kurou.kodriver.domain.usecase

/**
 * 高温警告のヒステリシス状態を判定する。
 *
 * いずれかの値が [highThresholdCelsius] 以上なら有効、すべての値が
 * [highThresholdCelsius] から [hysteresisCelsius] を引いた値以下なら無効にし、
 * その間は [currentState] を維持する。
 */
fun determineHysteresisState(
    valuesCelsius: Iterable<Float>,
    highThresholdCelsius: Float,
    hysteresisCelsius: Float,
    currentState: Boolean,
): Boolean {
    val coolThresholdCelsius = highThresholdCelsius - hysteresisCelsius
    return when {
        valuesCelsius.any { it >= highThresholdCelsius } -> true
        valuesCelsius.all { it <= coolThresholdCelsius } -> false
        else -> currentState
    }
}
