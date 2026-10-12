package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase

/**
 * 「スリープさせない」設定を実際に適用すべきかどうかを監視する UseCase。
 *
 * 設定が有効、かつ選択中シミュレータのテレメトリを実際に受信中の場合のみ true になる。
 */
class ObserveEffectiveKeepScreenOnUseCase(
    private val observeKeepScreenOnEnabled: ObserveKeepScreenOnEnabledUseCase,
    private val observeTelemetryReceiving: ObserveTelemetryReceivingUseCase,
) {
    operator fun invoke(): Flow<Boolean> =
        combine(
            observeKeepScreenOnEnabled(),
            observeTelemetryReceiving(),
        ) { keepScreenOnEnabled, isTelemetryReceiving ->
            keepScreenOnEnabled && isTelemetryReceiving
        }
}
