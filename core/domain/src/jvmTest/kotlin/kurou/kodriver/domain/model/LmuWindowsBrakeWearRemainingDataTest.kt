package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsBrakeWearRemainingDataTest {
    private val max = BrakeThicknessMeters(0.036f)
    private val failure = BrakeThicknessMeters(0.025f)

    @Test
    fun `新品時の厚さなら100%`() {
        assertEquals(100f, calculateBrakeWearRemainingPercent(max, max, failure))
    }

    @Test
    fun `新品時の厚さと破損厚さの中間なら50%`() {
        assertEquals(50f, calculateBrakeWearRemainingPercent(BrakeThicknessMeters(0.0305f), max, failure), 0.001f)
    }

    @Test
    fun `破損厚さ以下なら0%`() {
        assertEquals(0f, calculateBrakeWearRemainingPercent(failure, max, failure))
        assertEquals(0f, calculateBrakeWearRemainingPercent(BrakeThicknessMeters(0.010f), max, failure))
    }

    @Test
    fun `新品時の厚さを超えても100%を上限にする`() {
        assertEquals(100f, calculateBrakeWearRemainingPercent(BrakeThicknessMeters(0.040f), max, failure))
    }

    @Test
    fun `使える厚さの幅が0以下なら100%`() {
        assertEquals(100f, calculateBrakeWearRemainingPercent(failure, failure, failure))
        assertEquals(
            100f,
            calculateBrakeWearRemainingPercent(BrakeThicknessMeters(0.020f), BrakeThicknessMeters(0.020f), failure),
        )
    }
}
