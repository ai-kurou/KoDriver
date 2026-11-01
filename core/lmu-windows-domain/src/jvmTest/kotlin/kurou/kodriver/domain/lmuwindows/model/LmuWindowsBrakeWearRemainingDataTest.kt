package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.BrakeThicknessMeters
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsBrakeWearRemainingDataTest {
    private val max = BrakeThicknessMeters(0.036f)
    private val failure = BrakeThicknessMeters(0.025f)

    @Test
    fun `新品時の厚さなら100パーセント`() {
        assertEquals(100f, calculateBrakeWearRemainingPercent(max, max, failure))
    }

    @Test
    fun `新品時の厚さと破損厚さの中間なら50パーセント`() {
        assertEquals(50f, calculateBrakeWearRemainingPercent(BrakeThicknessMeters(0.0305f), max, failure), 0.001f)
    }

    @Test
    fun `破損厚さ以下なら0パーセント`() {
        assertEquals(0f, calculateBrakeWearRemainingPercent(failure, max, failure))
        assertEquals(0f, calculateBrakeWearRemainingPercent(BrakeThicknessMeters(0.010f), max, failure))
    }

    @Test
    fun `新品時の厚さを超えても100パーセントを上限にする`() {
        assertEquals(100f, calculateBrakeWearRemainingPercent(BrakeThicknessMeters(0.040f), max, failure))
    }

    @Test
    fun `使える厚さの幅が0以下なら100パーセント`() {
        assertEquals(100f, calculateBrakeWearRemainingPercent(failure, failure, failure))
        assertEquals(
            100f,
            calculateBrakeWearRemainingPercent(BrakeThicknessMeters(0.020f), BrakeThicknessMeters(0.020f), failure),
        )
    }
}
