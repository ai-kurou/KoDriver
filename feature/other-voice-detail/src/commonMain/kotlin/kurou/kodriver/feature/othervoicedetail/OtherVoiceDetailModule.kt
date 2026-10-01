package kurou.kodriver.feature.othervoicedetail

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * 読み上げ音声設定詳細（other-voice-detail feature）の Koin モジュール。
 *
 * 提供: OtherVoiceDetailViewModel。
 */
val otherVoiceDetailModule =
    module {
        viewModelOf(::OtherVoiceDetailViewModel)
    }
