package kurou.kodriver.feature.lmuwindowsautoreadout.vehicledamagedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsAutoReadoutVehicleDamageDetailFlowTest {
    @Test
    fun `既定の入力から既定のUiStateを合成する`() =
        runTest {
            val inputs = Inputs()
            assertEquals(LmuWindowsAutoReadoutVehicleDamageDetailUiState(), inputs.flow().first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    @Test
    fun `各キーと文言とTTS可否の更新を個別に反映する`() =
        runTest {
            val inputs = Inputs()
            val states = mutableListOf<LmuWindowsAutoReadoutVehicleDamageDetailUiState>()
            val collector = backgroundScope.launch { inputs.flow().collect { states.add(it) } }
            testScheduler.runCurrent()
            var expected = LmuWindowsAutoReadoutVehicleDamageDetailUiState()
            assertEquals(expected, states.last())

            inputs.enabledStates.update { it + (LmuWindowsReadoutItemKey.VehicleDamage.Overheat to false) }
            testScheduler.runCurrent()
            expected = expected.copy(overheatEnabled = false)
            assertEquals(expected, states.last())

            inputs.enabledStates.update { it + (LmuWindowsReadoutItemKey.VehicleDamage.PartDetached to false) }
            testScheduler.runCurrent()
            expected = expected.copy(partDetachedEnabled = false)
            assertEquals(expected, states.last())

            inputs.enabledStates.update { it + (LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached to false) }
            testScheduler.runCurrent()
            expected = expected.copy(tyreDetachedEnabled = false)
            assertEquals(expected, states.last())

            inputs.overheatReadoutText.update { " エンジン " }
            testScheduler.runCurrent()
            expected = expected.copy(overheatReadoutText = " エンジン ")
            assertEquals(expected, states.last())

            inputs.partDetachedReadoutText.update { "" }
            testScheduler.runCurrent()
            expected = expected.copy(partDetachedReadoutText = "")
            assertEquals(expected, states.last())

            inputs.tyreDetachedReadoutText.update { "ホイール" }
            testScheduler.runCurrent()
            expected = expected.copy(tyreDetachedReadoutText = "ホイール")
            assertEquals(expected, states.last())

            for (available in listOf(true, false)) {
                inputs.textToSpeechAvailable.update { available }
                testScheduler.runCurrent()
                expected = expected.copy(isTextToSpeechAvailable = available)
                assertEquals(expected, states.last())
            }
            collector.cancel()
        }

    @Test
    fun `購読時だけ五入力を購読し解除後は最新入力で再購読できる`() =
        runTest {
            val inputs = Inputs()
            val flow = inputs.flow()
            assertEquals(0, inputs.activeSubscriptions)
            val firstCollector = backgroundScope.launch { flow.collect {} }
            testScheduler.runCurrent()
            assertEquals(5, inputs.activeSubscriptions)
            val secondCollector = backgroundScope.launch { flow.collect {} }
            testScheduler.runCurrent()
            assertEquals(10, inputs.activeSubscriptions)
            firstCollector.cancel()
            testScheduler.runCurrent()
            assertEquals(5, inputs.activeSubscriptions)
            secondCollector.cancel()
            testScheduler.runCurrent()
            assertEquals(0, inputs.activeSubscriptions)
            inputs.overheatReadoutText.update { "再購読" }
            assertEquals(LmuWindowsAutoReadoutVehicleDamageDetailUiState(overheatReadoutText = "再購読"), flow.first())
            assertEquals(0, inputs.activeSubscriptions)
        }

    @Test
    fun `必須キーが欠ける場合はgetValueの例外を保持する`() =
        runTest {
            for (key in listOf(
                LmuWindowsReadoutItemKey.VehicleDamage.Overheat,
                LmuWindowsReadoutItemKey.VehicleDamage.PartDetached,
                LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached,
            )) {
                val inputs = Inputs()
                inputs.enabledStates.update { it - key }
                assertFailsWith<NoSuchElementException> { inputs.flow().first() }
                assertEquals(0, inputs.activeSubscriptions)
            }
        }

    private class Inputs {
        private val defaults = LmuWindowsAutoReadoutVehicleDamageDetailUiState()
        val enabledStates =
            MutableStateFlow<Map<ReadoutItemKey, Boolean>>(
                mapOf(
                    LmuWindowsReadoutItemKey.VehicleDamage.Overheat to defaults.overheatEnabled,
                    LmuWindowsReadoutItemKey.VehicleDamage.PartDetached to defaults.partDetachedEnabled,
                    LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached to defaults.tyreDetachedEnabled,
                ),
            )
        val overheatReadoutText = MutableStateFlow(defaults.overheatReadoutText)
        val partDetachedReadoutText = MutableStateFlow(defaults.partDetachedReadoutText)
        val tyreDetachedReadoutText = MutableStateFlow(defaults.tyreDetachedReadoutText)
        val textToSpeechAvailable = MutableStateFlow(defaults.isTextToSpeechAvailable)
        var activeSubscriptions = 0
            private set

        fun flow(): Flow<LmuWindowsAutoReadoutVehicleDamageDetailUiState> =
            LmuWindowsAutoReadoutVehicleDamageDetailFlow(
                enabledStates = enabledStates.tracked(),
                overheatReadoutText = overheatReadoutText.tracked(),
                partDetachedReadoutText = partDetachedReadoutText.tracked(),
                tyreDetachedReadoutText = tyreDetachedReadoutText.tracked(),
                textToSpeechAvailable = textToSpeechAvailable.tracked(),
            ).uiState

        private fun <T> Flow<T>.tracked(): Flow<T> =
            onStart { activeSubscriptions++ }.onCompletion { activeSubscriptions-- }
    }
}
