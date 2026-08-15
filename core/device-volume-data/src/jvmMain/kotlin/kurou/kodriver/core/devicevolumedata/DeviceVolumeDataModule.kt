package kurou.kodriver.core.devicevolumedata

import kurou.kodriver.core.devicevolumedata.repository.WindowsDeviceVolumeRepository
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import org.koin.dsl.module

private val isWindows = System.getProperty("os.name").lowercase().startsWith("windows")

/**
 * 端末のマスター音量のRepositoryバインドを行うKoinモジュール（:core:device-volume-data / jvmMain）。
 *
 * Windows Core Audio（WASAPI）へのアクセスはWindows専用のため、非Windowsでは何もしない
 * No-Op実装（下部のprivate class）にフォールバックする。Android版はandroidMainの同名モジュールを参照。
 */
val deviceVolumeDataModule =
    module {
        single<DeviceVolumeRepository> {
            if (isWindows) WindowsDeviceVolumeRepository() else NoOpDeviceVolumeRepository()
        }
    }

private class NoOpDeviceVolumeRepository : DeviceVolumeRepository {
    override suspend fun getVolume(): Int = 0

    override suspend fun setVolume(volume: Int) = Unit
}
