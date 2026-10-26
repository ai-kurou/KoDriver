package kurou.kodriver.feature.main

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AppScreenFlowTest {
    @Test
    fun `六つの状態をAppScreenUiStateへ反映し更新を流す`() =
        runTest {
            val hasAppUpdate = MutableStateFlow(false)
            val permissionGranted = MutableStateFlow(true)
            val keepScreenOn = MutableStateFlow(false)
            val dynamicColorEnabled = MutableStateFlow(false)
            val hapticFeedbackEnabled = MutableStateFlow(true)
            val selectedSimulator = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val flow =
                AppScreenFlow(
                    hasAppUpdate = hasAppUpdate,
                    accessLocalNetworkPermissionGranted = permissionGranted,
                    keepScreenOn = keepScreenOn,
                    dynamicColorEnabled = dynamicColorEnabled,
                    hapticFeedbackEnabled = hapticFeedbackEnabled,
                    selectedSimulator = selectedSimulator,
                )
            val states = mutableListOf<AppScreenUiState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                flow.uiState.collect { states += it }
            }

            assertEquals(
                AppScreenUiState(
                    hasAppUpdate = false,
                    accessLocalNetworkPermissionGranted = true,
                    keepScreenOn = false,
                    dynamicColorEnabled = false,
                    hapticFeedbackEnabled = true,
                    selectedSimulator = Simulator.LmuWindows,
                ),
                states.last(),
            )

            hasAppUpdate.update { true }
            permissionGranted.update { false }
            keepScreenOn.update { true }
            dynamicColorEnabled.update { true }
            hapticFeedbackEnabled.update { false }
            selectedSimulator.update { Simulator.Gt7Ps5 }
            runCurrent()

            assertEquals(
                AppScreenUiState(
                    hasAppUpdate = true,
                    accessLocalNetworkPermissionGranted = false,
                    keepScreenOn = true,
                    dynamicColorEnabled = true,
                    hapticFeedbackEnabled = false,
                    selectedSimulator = Simulator.Gt7Ps5,
                ),
                states.last(),
            )
        }
}
