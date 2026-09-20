package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.TelemetryLogRepository

/**
 * 実際に読み上げられた最新のテレメトリログを観測する。
 *
 * 読み上げ条件は整ったが読み上げされなかったログ（`NarrationOutcome.SKIPPED`）は音が鳴っていないため、
 * ナレーターオーバーレイのような「今読み上げている内容」を示す用途では対象外にする。
 */
class ObserveLatestNarratedTelemetryLogUseCase(
    private val repository: TelemetryLogRepository,
) {
    operator fun invoke(): Flow<TelemetryLog?> = repository.observeLatestNarratedTelemetryLog()
}
