package kurou.kodriver.feature.main

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.LowVolumeWarning
import kurou.kodriver.domain.usecase.ObserveLowVolumeWarningUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LowVolumeWarningViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val observeWarning: ObserveLowVolumeWarningUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `警告なしを非表示状態にする`() =
        runTest(dispatcher) {
            every { observeWarning() } returns MutableStateFlow(null)
            val viewModel = LowVolumeWarningViewModel(observeWarning)
            val job = launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            assertFalse(viewModel.uiState.first().isVisible)
            job.cancelAndJoin()
            verify(exactly = 1) { observeWarning() }
            confirmVerified(observeWarning)
        }

    @Test
    fun `端末音量だけの警告をマッピングする`() =
        assertMapped(
            warning = LowVolumeWarning(deviceVolume = 20, soundVolume = null),
            expected = LowVolumeWarningUiState(deviceVolume = 20),
        )

    @Test
    fun `KoDriver音量だけの警告をマッピングする`() =
        assertMapped(
            warning = LowVolumeWarning(deviceVolume = null, soundVolume = 20),
            expected = LowVolumeWarningUiState(soundVolume = 20),
        )

    @Test
    fun `両方の警告をマッピングする`() =
        assertMapped(
            warning = LowVolumeWarning(deviceVolume = 20, soundVolume = 30),
            expected = LowVolumeWarningUiState(deviceVolume = 20, soundVolume = 30),
        )

    @Test
    fun `dismissすると警告が続く間は非表示のままにする`() =
        runTest(dispatcher) {
            val warnings = MutableStateFlow<LowVolumeWarning?>(LowVolumeWarning(deviceVolume = 20, soundVolume = null))
            every { observeWarning() } returns warnings
            val viewModel = LowVolumeWarningViewModel(observeWarning)
            val job = launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            viewModel.dismiss()
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.isVisible)
            warnings.update { LowVolumeWarning(deviceVolume = 10, soundVolume = null) }
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.isVisible)

            job.cancelAndJoin()
            verify(exactly = 1) { observeWarning() }
            confirmVerified(observeWarning)
        }

    @Test
    fun `警告が解消した後の再低下では再表示する`() =
        runTest(dispatcher) {
            val warnings = MutableStateFlow<LowVolumeWarning?>(LowVolumeWarning(deviceVolume = 20, soundVolume = null))
            every { observeWarning() } returns warnings
            val viewModel = LowVolumeWarningViewModel(observeWarning)
            val job = launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            viewModel.dismiss()
            warnings.update { null }
            advanceUntilIdle()
            warnings.update { LowVolumeWarning(deviceVolume = 25, soundVolume = null) }
            advanceUntilIdle()

            assertEquals(LowVolumeWarningUiState(deviceVolume = 25), viewModel.uiState.value)
            job.cancelAndJoin()
            verify(exactly = 1) { observeWarning() }
            confirmVerified(observeWarning)
        }

    @Test
    fun `警告なしのdismissは以後の警告を抑制しない`() =
        runTest(dispatcher) {
            val warnings = MutableStateFlow<LowVolumeWarning?>(null)
            every { observeWarning() } returns warnings
            val viewModel = LowVolumeWarningViewModel(observeWarning)
            val job = launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            viewModel.dismiss()
            warnings.update { LowVolumeWarning(deviceVolume = 20, soundVolume = null) }
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isVisible)
            job.cancelAndJoin()
            verify(exactly = 1) { observeWarning() }
            confirmVerified(observeWarning)
        }

    private fun assertMapped(
        warning: LowVolumeWarning,
        expected: LowVolumeWarningUiState,
    ) = runTest(dispatcher) {
        val warnings = MutableStateFlow<LowVolumeWarning?>(warning)
        every { observeWarning() } returns warnings
        val viewModel = LowVolumeWarningViewModel(observeWarning)
        val job = launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        assertEquals(expected, viewModel.uiState.first())
        assertTrue(viewModel.uiState.first().isVisible)
        job.cancelAndJoin()
        verify(exactly = 1) { observeWarning() }
        confirmVerified(observeWarning)
    }
}
