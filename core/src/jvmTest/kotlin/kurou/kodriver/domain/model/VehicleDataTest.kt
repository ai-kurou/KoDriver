package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class VehicleDataTest {

    private fun vehicleData(vx: Double = 0.0, vy: Double = 0.0, vz: Double = 0.0) = VehicleData(
        localVelocityX = vx,
        localVelocityY = vy,
        localVelocityZ = vz,
        positionX = 0.0,
        positionY = 0.0,
        positionZ = 0.0,
    )

    @Test
    fun `全速度ゼロのとき speedMs と speedKmh はゼロになる`() {
        val data = vehicleData()

        assertEquals(0.0, data.speedMs)
        assertEquals(0.0, data.speedKmh)
    }

    @Test
    fun `3次元ベクトルの速度が正しく計算される`() {
        // 1² + 2² + 2² = 9 → sqrt(9) = 3 m/s → 10.8 km/h
        val data = vehicleData(vx = 1.0, vy = 2.0, vz = 2.0)

        assertEquals(3.0, data.speedMs, 1e-9)
        assertEquals(10.8, data.speedKmh, 1e-9)
    }

    @Test
    fun `負の速度成分でも正の speedMs になる`() {
        // (-3)² + (-4)² + 0² = 25 → sqrt(25) = 5 m/s
        val data = vehicleData(vx = -3.0, vy = -4.0, vz = 0.0)

        assertEquals(5.0, data.speedMs, 1e-9)
    }
}
