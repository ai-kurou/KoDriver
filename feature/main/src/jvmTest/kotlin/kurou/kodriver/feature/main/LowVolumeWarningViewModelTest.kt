package kurou.kodriver.feature.main

import io.mockk.coEvery
import io.mockk.coVerify
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.LowVolumeWarning
import kurou.kodriver.domain.usecase.DismissLowVolumeWarningUseCase
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
    private val dismissWarning: DismissLowVolumeWarningUseCase = mockk()

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
            val viewModel = LowVolumeWarningViewModel(observeWarning, dismissWarning)
            val job = launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            assertFalse(viewModel.uiState.first().isVisible)
            job.cancelAndJoin()
            verify(exactly = 1) { observeWarning() }
            confirmVerified(observeWarning, dismissWarning)
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
    fun `dismissは現在の警告を一度渡す`() =
        runTest(dispatcher) {
            val warning = LowVolumeWarning(deviceVolume = 20, soundVolume = null)
            every { observeWarning() } returns MutableStateFlow(warning)
            coEvery { dismissWarning(warning) } returns Unit
            val viewModel = LowVolumeWarningViewModel(observeWarning, dismissWarning)
            val job = launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            viewModel.dismiss()
            advanceUntilIdle()

            coVerify(exactly = 1) { dismissWarning(warning) }
            job.cancelAndJoin()
            verify(exactly = 1) { observeWarning() }
            confirmVerified(observeWarning, dismissWarning)
        }

    @Test
    fun `警告なしのdismissはユースケースを呼ばない`() =
        runTest(dispatcher) {
            val warnings = MutableStateFlow<LowVolumeWarning?>(null)
            every { observeWarning() } returns warnings
            val viewModel = LowVolumeWarningViewModel(observeWarning, dismissWarning)
            val job = launch { viewModel.uiState.collect() }
            advanceUntilIdle()

            viewModel.dismiss()
            advanceUntilIdle()
            job.cancelAndJoin()
            verify(exactly = 1) { observeWarning() }
            confirmVerified(observeWarning, dismissWarning)
        }

    private fun assertMapped(
        warning: LowVolumeWarning,
        expected: LowVolumeWarningUiState,
    ) = runTest(dispatcher) {
        val warnings = MutableStateFlow<LowVolumeWarning?>(warning)
        every { observeWarning() } returns warnings
        val viewModel = LowVolumeWarningViewModel(observeWarning, dismissWarning)
        val job = launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        assertEquals(expected, viewModel.uiState.first())
        assertTrue(viewModel.uiState.first().isVisible)
        job.cancelAndJoin()
        verify(exactly = 1) { observeWarning() }
        confirmVerified(observeWarning, dismissWarning)
    }
}
