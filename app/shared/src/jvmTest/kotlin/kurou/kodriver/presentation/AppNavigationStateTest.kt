package kurou.kodriver.presentation

import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals

class AppNavigationStateTest {
    @Test
    fun `初期状態はinitialで渡した宛先になる`() {
        val state = AppNavigationState(mutableStateOf(AppDestination.Log))

        assertEquals(AppDestination.Log, state.current)
    }

    @Test
    fun `navigateToを呼ぶとcurrentが切り替わる`() {
        val state = AppNavigationState(mutableStateOf(AppDestination.Readout))

        state.navigateTo(AppDestination.More)

        assertEquals(AppDestination.More, state.current)
    }

    @Test
    fun `現在の宛先と同じタブをhandleTabClickするとonReselectedが呼ばれcurrentは変わらない`() {
        val state = AppNavigationState(mutableStateOf(AppDestination.Readout))
        var reselected: AppDestination? = null

        state.handleTabClick(AppDestination.Readout) { reselected = it }

        assertEquals(AppDestination.Readout, reselected)
        assertEquals(AppDestination.Readout, state.current)
    }

    @Test
    fun `別のタブをhandleTabClickするとonReselectedは呼ばれずcurrentが切り替わる`() {
        val state = AppNavigationState(mutableStateOf(AppDestination.Readout))
        var reselected: AppDestination? = null

        state.handleTabClick(AppDestination.Log) { reselected = it }

        assertEquals(null, reselected)
        assertEquals(AppDestination.Log, state.current)
    }
}
