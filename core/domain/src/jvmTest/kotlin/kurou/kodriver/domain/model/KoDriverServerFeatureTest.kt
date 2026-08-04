package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class KoDriverServerFeatureTest {
    @Test
    fun `webSocketPathはSimulator idとfeatureからパスを生成する`() {
        assertEquals("/ws/lmu_windows/flags", KoDriverServerFeature.FLAGS.webSocketPath(Simulator.LmuWindows))
        assertEquals(
            "/ws/lmu_windows/vehicle_approach",
            KoDriverServerFeature.VEHICLE_APPROACH.webSocketPath(Simulator.LmuWindows),
        )
        assertEquals("/ws/lmu_windows/damage", KoDriverServerFeature.DAMAGE.webSocketPath(Simulator.LmuWindows))
        assertEquals(
            "/ws/lmu_windows/tyre_carcass_temperature",
            KoDriverServerFeature.TYRE_CARCASS_TEMPERATURE.webSocketPath(Simulator.LmuWindows),
        )
        assertEquals(
            "/ws/lmu_windows/tyre_wear",
            KoDriverServerFeature.TYRE_WEAR.webSocketPath(Simulator.LmuWindows),
        )
        assertEquals(
            "/ws/lmu_windows/vehicle_class",
            KoDriverServerFeature.VEHICLE_CLASS.webSocketPath(Simulator.LmuWindows),
        )
        assertEquals("/ws/ace_windows/fuel", KoDriverServerFeature.FUEL.webSocketPath(Simulator.AceWindows))
        assertEquals("/ws/ace_windows/status", KoDriverServerFeature.STATUS.webSocketPath(Simulator.AceWindows))
        assertEquals(
            "/ws/lmu_windows/pit_status",
            KoDriverServerFeature.PIT_STATUS.webSocketPath(Simulator.LmuWindows),
        )
    }
}
