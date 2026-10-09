package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import kurou.kodriver.domain.model.BrakeThicknessMeters
import kotlin.test.Test
import kotlin.test.assertEquals

class BrakeWearValueFormatterTest {
    @Test
    fun `厚さはミリメートルの小数1桁で整形する`() {
        assertEquals("36.0", formatBrakeThicknessMillimeters(BrakeThicknessMeters(0.036f)))
        assertEquals("30.5", formatBrakeThicknessMillimeters(BrakeThicknessMeters(0.0305f)))
        assertEquals("0.0", formatBrakeThicknessMillimeters(BrakeThicknessMeters(0f)))
    }

    @Test
    fun `小数2桁目以降は四捨五入する`() {
        assertEquals("30.6", formatBrakeThicknessMillimeters(BrakeThicknessMeters(0.03056f)))
        assertEquals("30.4", formatBrakeThicknessMillimeters(BrakeThicknessMeters(0.03044f)))
    }

    @Test
    fun `負の厚さは0として整形する`() {
        assertEquals("0.0", formatBrakeThicknessMillimeters(BrakeThicknessMeters(-0.001f)))
    }

    @Test
    fun `残量は百分率で整形する`() {
        assertEquals("87%", formatBrakeWearPercent(87))
        assertEquals("0%", formatBrakeWearPercent(0))
    }
}
