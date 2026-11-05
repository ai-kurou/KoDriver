package kurou.kodriver.feature.main

import kurou.kodriver.domain.usecase.GetAudioInputDevicesUseCase
import kurou.kodriver.domain.usecase.ObserveMicrophoneInputStatusUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedAudioInputDeviceNameUseCase
import kurou.kodriver.domain.usecase.SaveSelectedAudioInputDeviceNameUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * main feature のプラットフォーム別バインド（expect は MainModule.kt）。
 * LMU / ACE 接続バナー用の LmuBannerConnectionChecker / AceBannerConnectionChecker を
 * プラットフォーム別に提供する（JVM=共有メモリ経由、Android=KoDriver サーバーへの疎通確認）。
 * マイク入力の確認用 MicrophoneInputViewModel は MicrophoneRepository を持つ JVM のみで提供する。
 */
actual val mainPlatformModule: Module =
    module {
        factory<LmuBannerConnectionChecker> { LmuWindowsBannerConnectionChecker(get()) }
        factory<AceBannerConnectionChecker> { AceWindowsBannerConnectionChecker(get()) }
        viewModel {
            MicrophoneInputViewModel(
                observeMicrophoneInputStatus = ObserveMicrophoneInputStatusUseCase(get()),
                getAudioInputDevices = GetAudioInputDevicesUseCase(get()),
                observeSelectedAudioInputDeviceName = ObserveSelectedAudioInputDeviceNameUseCase(get()),
                saveSelectedAudioInputDeviceName = SaveSelectedAudioInputDeviceNameUseCase(get()),
            )
        }
    }
