package kurou.kodriver.feature.telemetrylogdetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.TelemetryLogDetail
import kurou.kodriver.domain.usecase.ObserveTelemetryLogDetailUseCase

/** 選択したログIDから詳細の表示状態を組み立てる冷たい Flow。 */
internal class TelemetryLogDetailFlow(
    selectedLogId: Flow<Long?>,
    observeTelemetryLogDetail: ObserveTelemetryLogDetailUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: Flow<TelemetryLogDetailUiState> =
        selectedLogId.flatMapLatest { id ->
            if (id == null) {
                flowOf(TelemetryLogDetailUiState())
            } else {
                observeTelemetryLogDetail(id).map { detail ->
                    TelemetryLogDetailUiState(
                        logId = id,
                        items = detail.toItems(),
                    )
                }
            }
        }
}

private fun TelemetryLogDetail?.toItems(): List<TelemetryLogDetailItemUiState> {
    if (this == null) return emptyList()
    return buildList {
        add(
            TelemetryLogDetailItemUiState(
                kind = TelemetryLogDetailItemKind.SELECTED,
                telemetryJson = current.telemetryJson,
            ),
        )
        previous?.let { log ->
            add(
                TelemetryLogDetailItemUiState(
                    kind = TelemetryLogDetailItemKind.PREVIOUS,
                    telemetryJson = log.telemetryJson,
                ),
            )
        }
    }
}
