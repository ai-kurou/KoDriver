@file:Suppress("FunctionNaming")

package kurou.kodriver.core.devicevolumedata.repository

import android.media.AudioManager
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidDeviceVolumeRepositoryTest {
    @MockK(relaxUnitFun = true)
    private lateinit var audioManager: AudioManager

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `getVolumeは現在値と最大値の比率を0から100のパーセンテージへ変換する`() =
        runTest {
            every { audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) } returns 5
            every { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) } returns 10
            val repository = AndroidDeviceVolumeRepository(audioManager)

            val result = repository.getVolume()

            assertEquals(50, result)
            verify(exactly = 1) { audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) }
            verify(exactly = 1) { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
            confirmVerified(audioManager)
        }

    @Test
    fun `最大値が0の場合はgetVolumeは0を返す`() =
        runTest {
            every { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) } returns 0
            val repository = AndroidDeviceVolumeRepository(audioManager)

            val result = repository.getVolume()

            assertEquals(0, result)
            verify(exactly = 1) { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
            confirmVerified(audioManager)
        }

    @Test
    fun `setVolumeは0から100のパーセンテージを最大値に応じたストリーム音量へ変換して設定する`() =
        runTest {
            every { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) } returns 10
            val repository = AndroidDeviceVolumeRepository(audioManager)

            repository.setVolume(50)

            verify(exactly = 1) { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
            verify(exactly = 1) { audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0) }
            confirmVerified(audioManager)
        }
}
