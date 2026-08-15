package kurou.kodriver.core.devicevolumedata.repository

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WindowsDeviceVolumeRepositoryTest {
    @Test
    fun `getVolumeはスカラー値を0から100のパーセンテージへ変換する`() =
        runTest {
            val source = FakeWindowsMasterVolumeSource(scalarVolume = 0.5f)
            val repository = WindowsDeviceVolumeRepository(source)

            assertEquals(50, repository.getVolume())
        }

    @Test
    fun `setVolumeは0から100のパーセンテージをスカラー値へ変換して設定する`() =
        runTest {
            val source = FakeWindowsMasterVolumeSource()
            val repository = WindowsDeviceVolumeRepository(source)

            repository.setVolume(25)

            assertEquals(0.25f, source.getScalarVolume())
        }

    @Test
    fun `getVolumeは0と100の境界値を扱える`() =
        runTest {
            val minSource = FakeWindowsMasterVolumeSource(scalarVolume = 0f)
            val maxSource = FakeWindowsMasterVolumeSource(scalarVolume = 1f)

            assertEquals(0, WindowsDeviceVolumeRepository(minSource).getVolume())
            assertEquals(100, WindowsDeviceVolumeRepository(maxSource).getVolume())
        }
}
