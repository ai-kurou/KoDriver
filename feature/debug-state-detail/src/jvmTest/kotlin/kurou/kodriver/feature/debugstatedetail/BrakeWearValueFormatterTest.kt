package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.model.BrakeThicknessMeters
import kotlin.test.Test
import kotlin.test.assertEquals

class BrakeWearValueFormatterTest {
    @Test
    fun `厚さはミリメートルの小数3桁で整形する`() {
        assertEquals("36.000", formatBrakeThicknessMillimeters(BrakeThicknessMeters(0.036f)))
        assertEquals("30.500", formatBrakeThicknessMillimeters(BrakeThicknessMeters(0.0305f)))
        assertEquals("0.000", formatBrakeThicknessMillimeters(BrakeThicknessMeters(0f)))
    }

    @Test
    fun `小数4桁目以降は四捨五入する`() {
        assertEquals("30.560", formatBrakeThicknessMillimeters(BrakeThicknessMeters(0.03056f)))
        assertEquals("30.440", formatBrakeThicknessMillimeters(BrakeThicknessMeters(0.03044f)))
        assertEquals("30.561", formatBrakeThicknessMillimeters(BrakeThicknessMeters(0.0305606f)))
        assertEquals("30.560", formatBrakeThicknessMillimeters(BrakeThicknessMeters(0.0305604f)))
    }

    @Test
    fun `負の厚さは0として整形する`() {
        assertEquals("0.000", formatBrakeThicknessMillimeters(BrakeThicknessMeters(-0.001f)))
    }

    @Test
    fun `残量は百分率の小数1桁で整形する`() {
        assertEquals("87.0%", formatBrakeWearPercent(87f))
        assertEquals("87.1%", formatBrakeWearPercent(87.06f))
        assertEquals("0.0%", formatBrakeWearPercent(-1f))
        assertEquals("0.0%", formatBrakeWearPercent(0f))
    }
}
