package kurou.kodriver.presentation

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class AppNavigationStateTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `rememberAppNavigationStateはデフォルトでReadoutを初期値とする`() {
        lateinit var navigationState: AppNavigationState

        rule.setContent {
            navigationState = rememberAppNavigationState()
        }

        rule.runOnIdle {
            assertEquals(AppDestination.Readout, navigationState.current)
        }
    }

    @Test
    fun `rememberAppNavigationStateはinitialに渡した宛先を初期値とする`() {
        lateinit var navigationState: AppNavigationState

        rule.setContent {
            navigationState = rememberAppNavigationState(initial = AppDestination.Log)
        }

        rule.runOnIdle {
            assertEquals(AppDestination.Log, navigationState.current)
        }
    }

    @Test
    fun `rememberAppNavigationStateが返すインスタンスでnavigateToするとcurrentが切り替わる`() {
        lateinit var navigationState: AppNavigationState

        rule.setContent {
            navigationState = rememberAppNavigationState()
        }
        rule.runOnIdle {
            navigationState.navigateTo(AppDestination.More)
        }

        rule.runOnIdle {
            assertEquals(AppDestination.More, navigationState.current)
        }
    }

    @Test
    fun `初期状態はinitialで渡した宛先になる`() {
        val state = AppNavigationState(NavBackStack<NavKey>(AppDestination.Log))

        assertEquals(AppDestination.Log, state.current)
    }

    @Test
    fun `navigateToを呼ぶとcurrentが切り替わる`() {
        val state = AppNavigationState(NavBackStack<NavKey>(AppDestination.Readout))

        state.navigateTo(AppDestination.More)

        assertEquals(AppDestination.More, state.current)
    }

    @Test
    fun `現在の宛先と同じタブをhandleTabClickするとonReselectedが呼ばれcurrentは変わらない`() {
        val state = AppNavigationState(NavBackStack<NavKey>(AppDestination.Readout))
        var reselected: AppDestination? = null

        state.handleTabClick(AppDestination.Readout) { reselected = it }

        assertEquals(AppDestination.Readout, reselected)
        assertEquals(AppDestination.Readout, state.current)
    }

    @Test
    fun `別のタブをhandleTabClickするとonReselectedは呼ばれずcurrentが切り替わる`() {
        val state = AppNavigationState(NavBackStack<NavKey>(AppDestination.Readout))
        var reselected: AppDestination? = null

        state.handleTabClick(AppDestination.Log) { reselected = it }

        assertEquals(null, reselected)
        assertEquals(AppDestination.Log, state.current)
    }
}
