package kurou.kodriver.feature.othervolumedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import org.koin.dsl.module

/**
 * テスト用の Fake Koin モジュール（testFixtures）。:core:device-volume-data の代わりに
 * DeviceVolumeRepository の Fake 実装をバインドし、実OSのオーディオAPI（AudioManager/WASAPI）
 * への呼び出しを避ける。
 */
val fakeOtherVolumeDetailModule =
    module {
        single<DeviceVolumeRepository> { FakeDeviceVolumeRepository() }
    }

class FakeDeviceVolumeRepository : DeviceVolumeRepository {
    private val volume = MutableStateFlow(50)

    override fun observeVolume(): Flow<Int> = volume.asStateFlow()

    override suspend fun getVolume(): Int = volume.value

    override suspend fun setVolume(volume: Int) {
        this.volume.update { volume }
    }
}
