package kurou.kodriver.domain.acewindows.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsBrakeTemperatureDataTest {
    @Test
    fun `シリアライズした値をデシリアライズすると元の値になる`() {
        val data =
            AceWindowsBrakeTemperatureData(
                wheels =
                    mapOf(
                        WheelIndex.FRONT_LEFT to CelsiusReading(300.0f),
                        WheelIndex.REAR_RIGHT to CelsiusReading(260.0f),
                    ),
            )

        assertEquals(data, Json.decodeFromString<AceWindowsBrakeTemperatureData>(Json.encodeToString(data)))
    }
}
