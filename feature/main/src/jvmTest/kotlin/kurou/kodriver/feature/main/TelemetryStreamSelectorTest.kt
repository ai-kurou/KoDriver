package kurou.kodriver.feature.main

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.acewindows.model.AceWindowsStatusData
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertSame

class TelemetryStreamSelectorTest {
    private val observeLmuWindows: ObserveLmuWindowsUseCase = mockk()
    private val observeGt7Ps5: ObserveGt7Ps5UseCase = mockk()
    private val observeAceWindowsStatus: ObserveAceWindowsStatusUseCase = mockk()

    @Test
    fun `選択中シミュレーターのUseCaseだけを購読する`() {
        val lmu = emptyFlow<LmuWindowsTelemetryData>()
        every { observeLmuWindows() } returns lmu
        val gt7 = emptyFlow<Gt7Ps5TelemetryData>()
        val ace = emptyFlow<AceWindowsStatusData>()
        every { observeGt7Ps5() } returns gt7
        every { observeAceWindowsStatus() } returns ace

        assertSame(
            lmu,
            selectTelemetryStream(Simulator.LmuWindows, observeLmuWindows, observeGt7Ps5, observeAceWindowsStatus),
        )
        assertSame(
            gt7,
            selectTelemetryStream(Simulator.Gt7Ps5, observeLmuWindows, observeGt7Ps5, observeAceWindowsStatus),
        )
        assertSame(
            ace,
            selectTelemetryStream(Simulator.AceWindows, observeLmuWindows, observeGt7Ps5, observeAceWindowsStatus),
        )
        verify(exactly = 1) { observeLmuWindows() }
        verify(exactly = 1) { observeGt7Ps5() }
        verify(exactly = 1) { observeAceWindowsStatus() }
        confirmVerified(observeLmuWindows, observeGt7Ps5, observeAceWindowsStatus)
    }
}
