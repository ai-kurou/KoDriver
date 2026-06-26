package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class KoDriverServerFeatureTest {

    @Test
    fun `webSocketPathはSimulator idとfeatureからパスを生成する`() {
        assertEquals("/ws/lmu_windows/flags", KoDriverServerFeature.FLAGS.webSocketPath(Simulator.LmuWindows))
        assertEquals("/ws/lmu_windows/proximity", KoDriverServerFeature.PROXIMITY.webSocketPath(Simulator.LmuWindows))
        assertEquals("/ws/lmu_windows/damage", KoDriverServerFeature.DAMAGE.webSocketPath(Simulator.LmuWindows))
    }
}
