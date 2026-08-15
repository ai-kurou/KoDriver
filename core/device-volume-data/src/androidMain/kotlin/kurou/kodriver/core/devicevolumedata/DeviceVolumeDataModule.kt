package kurou.kodriver.core.devicevolumedata

import android.content.Context
import android.media.AudioManager
import kurou.kodriver.core.devicevolumedata.repository.AndroidDeviceVolumeRepository
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import org.koin.dsl.module

/**
 * 端末のマスター音量のRepositoryバインドを行うKoinモジュール（:core:device-volume-data / androidMain）。
 *
 * jvmMain版との違いは実装手段（`AudioManager` vs Windows Core Audio）のみ。
 */
val deviceVolumeDataModule =
    module {
        single<DeviceVolumeRepository> {
            val context = get<Context>()
            AndroidDeviceVolumeRepository(
                audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager,
            )
        }
    }
