package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository
import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository
import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository
import kurou.kodriver.domain.usecase.ObserveOverlayBackgroundOpacityUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayTextColorUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayTextSizeUseCase
import kurou.kodriver.domain.usecase.ObserveOverlayVisibleUseCase
import kurou.kodriver.domain.usecase.SaveOverlayVisibleUseCase
import kotlin.test.Test

class OtherListOverlaySettingsFlowTest {
    private val visibleRepository: OverlayVisiblePreferencesRepository = mockk()
    private val textSizeRepository: OverlayTextSizePreferencesRepository = mockk()
    private val textColorRepository: OverlayTextColorPreferencesRepository = mockk()
    private val backgroundOpacityRepository: OverlayBackgroundOpacityPreferencesRepository = mockk()

    @Test
    fun `初期状態と全設定の更新を状態に反映する`() =
        runTest {
            val visible = MutableStateFlow(true)
            val textSize = MutableStateFlow(OverlayTextSize.MEDIUM)
            val textColor = MutableStateFlow(0xFFFFC107L)
            val backgroundOpacity = MutableStateFlow(50)
            every { visibleRepository.observeOverlayVisible() } returns visible
            every { textSizeRepository.observeOverlayTextSize() } returns textSize
            every { textColorRepository.observeOverlayTextColor() } returns textColor
            every { backgroundOpacityRepository.observeOverlayBackgroundOpacity() } returns backgroundOpacity
            val flow = createFlow()

            assertSettingsStateUpdates(
                uiState = flow.uiState,
                initialState = OtherListOverlaySettingsUiState(),
                updatedState = OtherListOverlaySettingsUiState(false, OverlayTextSize.LARGE, 0xFF123456L, 75),
            ) {
                visible.update { false }
                textSize.update { OverlayTextSize.LARGE }
                textColor.update { 0xFF123456L }
                backgroundOpacity.update { 75 }
            }
            verify(exactly = 1) { visibleRepository.observeOverlayVisible() }
            verify(exactly = 1) { textSizeRepository.observeOverlayTextSize() }
            verify(exactly = 1) { textColorRepository.observeOverlayTextColor() }
            verify(exactly = 1) { backgroundOpacityRepository.observeOverlayBackgroundOpacity() }
            confirmVerified(visibleRepository, textSizeRepository, textColorRepository, backgroundOpacityRepository)
        }

    @Test
    fun `表示状態の保存をUseCaseに委譲する`() =
        runTest {
            every { visibleRepository.observeOverlayVisible() } returns MutableStateFlow(true)
            every { textSizeRepository.observeOverlayTextSize() } returns MutableStateFlow(OverlayTextSize.MEDIUM)
            every { textColorRepository.observeOverlayTextColor() } returns MutableStateFlow(0xFFFFC107L)
            every { backgroundOpacityRepository.observeOverlayBackgroundOpacity() } returns MutableStateFlow(50)
            coEvery { visibleRepository.saveOverlayVisible(false) } returns Unit
            val flow = createFlow()

            flow.uiState.first()
            flow.onOverlayVisibleChange(false)

            coVerify(exactly = 1) { visibleRepository.saveOverlayVisible(false) }
            verify(exactly = 1) { visibleRepository.observeOverlayVisible() }
            verify(exactly = 1) { textSizeRepository.observeOverlayTextSize() }
            verify(exactly = 1) { textColorRepository.observeOverlayTextColor() }
            verify(exactly = 1) { backgroundOpacityRepository.observeOverlayBackgroundOpacity() }
            confirmVerified(visibleRepository, textSizeRepository, textColorRepository, backgroundOpacityRepository)
        }

    private fun createFlow() =
        OtherListOverlaySettingsFlow(
            ObserveOverlayVisibleUseCase(visibleRepository),
            SaveOverlayVisibleUseCase(visibleRepository),
            ObserveOverlayTextSizeUseCase(textSizeRepository),
            ObserveOverlayTextColorUseCase(textColorRepository),
            ObserveOverlayBackgroundOpacityUseCase(backgroundOpacityRepository),
        )
}
