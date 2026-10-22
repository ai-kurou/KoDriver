package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.model.Simulator

internal fun selectTelemetryStream(
    simulator: Simulator,
    observeLmuWindows: ObserveLmuWindowsUseCase,
    observeGt7Ps5: ObserveGt7Ps5UseCase,
    observeAceWindowsStatus: ObserveAceWindowsStatusUseCase,
): Flow<*> =
    when (simulator) {
        is Simulator.LmuWindows -> observeLmuWindows()
        is Simulator.Gt7Ps5 -> observeGt7Ps5()
        is Simulator.AceWindows -> observeAceWindowsStatus()
    }
