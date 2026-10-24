package kurou.kodriver.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals

class DetermineHysteresisStateTest {
    @Test
    fun `いずれかの値が高温閾値に達すると有効になる`() {
        assertEquals(
            true,
            determineHysteresisState(
                valuesCelsius = listOf(89f, 90f),
                highThresholdCelsius = 90f,
                hysteresisCelsius = 5f,
                currentState = false,
            ),
        )
    }

    @Test
    fun `すべての値が冷却閾値以下になると無効になる`() {
        assertEquals(
            false,
            determineHysteresisState(
                valuesCelsius = listOf(84f, 85f),
                highThresholdCelsius = 90f,
                hysteresisCelsius = 5f,
                currentState = true,
            ),
        )
    }

    @Test
    fun `ヒステリシス範囲内では現在の有効状態を保つ`() {
        assertEquals(
            true,
            determineHysteresisState(
                valuesCelsius = listOf(87f),
                highThresholdCelsius = 90f,
                hysteresisCelsius = 5f,
                currentState = true,
            ),
        )
    }

    @Test
    fun `ヒステリシス範囲内では現在の無効状態を保つ`() {
        assertEquals(
            false,
            determineHysteresisState(
                valuesCelsius = listOf(87f),
                highThresholdCelsius = 90f,
                hysteresisCelsius = 5f,
                currentState = false,
            ),
        )
    }

    @Test
    fun `値が空の場合はallの判定により無効になる`() {
        assertEquals(
            false,
            determineHysteresisState(
                valuesCelsius = emptyList(),
                highThresholdCelsius = 90f,
                hysteresisCelsius = 5f,
                currentState = true,
            ),
        )
    }
}
