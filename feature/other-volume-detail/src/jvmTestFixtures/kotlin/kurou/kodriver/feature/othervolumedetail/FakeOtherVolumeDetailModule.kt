package kurou.kodriver.feature.othervolumedetail

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
    private var volume = 50

    override suspend fun getVolume(): Int = volume

    override suspend fun setVolume(volume: Int) {
        this.volume = volume
    }
}
