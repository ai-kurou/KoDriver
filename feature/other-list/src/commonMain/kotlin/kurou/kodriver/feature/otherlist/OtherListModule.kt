package kurou.kodriver.feature.otherlist

import kurou.kodriver.domain.usecase.CheckTextToSpeechUnavailableReasonUseCase
import kurou.kodriver.domain.usecase.GetDeviceVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UdpPortUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayVisibleUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveThemeModeUseCase
import kurou.kodriver.domain.usecase.ObserveVoicePitchUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kurou.kodriver.domain.usecase.OpenWindowsSpeechSettingsUseCase
import kurou.kodriver.domain.usecase.SaveOverlayVisibleUseCase
import kurou.kodriver.domain.usecase.StartupRegistrationUseCases
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * その他一覧画面（other-list feature）の Koin モジュール。
 *
 * 提供: OtherListViewModel と、それが使う設定・TTS関連 UseCase。
 * 消費（get で解決）: OtherListViewModel が使う UseCase 群（:core:domain。実体の Repository は
 *   :core:data / :core:device-volume-data / :core:windows-startup-data / :core:text-to-speech-data で登録）。アプリバージョンはビルド生成値を直接渡す。
 */
val otherListModule =
    module {
        // ViewModel
        viewModel {
            OtherListViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                OtherListAppVersionInfo(
                    currentVersion = currentAppVersion(),
                    appVersionLabel = currentAppVersionLabel(),
                ),
                get(),
            )
        }

        factory {
            OtherListSettingsUseCases(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                // 接続先IPの設定はAndroidのみ。項目がないプラットフォームでは購読しない。
                if (OtherListItemType.ServerIp in buildOtherListItems()) get() else null,
                get(),
                get(),
            )
        }

        // ドメイン UseCase（:core:domain。get() は :core:windows-startup-data の Repository を解決）
        factory { StartupRegistrationUseCases(get()) }

        // オーバーレイ表示ON/OFF（:core:domain。get() は :core:data の Repository を解決）
        factory { ObserveOverlayVisibleUseCase(get()) }
        factory { ObserveVoiceUseCase(get()) }
        factory { ObserveVoiceSpeedUseCase(get()) }
        factory { ObserveVoicePitchUseCase(get()) }
        factory { ObserveReadoutStartSoundTypeUseCase(get()) }
        factory { ObserveSoundVolumeUseCase(get()) }
        factory { SaveOverlayVisibleUseCase(get()) }

        // other-theme-detail と同じfactory定義なので、どちらが後から登録されても同じ依存を解決する。
        factory { ObserveThemeModeUseCase(get()) }
        factory { ObserveServerIpUseCase(get()) }
        factory { ObserveConsoleAddressUseCase(get()) }
        factory { ObserveGt7Ps5UdpPortUseCase(get()) }

        // 端末のマスター音量（:core:device-volume-data の Repository を解決）。
        // other-volume-detail と同じfactory定義なので、どちらが後から登録されても同じ依存を解決する。
        factory { GetDeviceVolumeUseCase(get()) }

        // TTS利用不可理由の判定と音声設定の起動（:core:domain。get() は :core:text-to-speech-data の Repository を解決）
        factory { CheckTextToSpeechUnavailableReasonUseCase(get()) }
        factory { OpenWindowsSpeechSettingsUseCase(get()) }
    }
