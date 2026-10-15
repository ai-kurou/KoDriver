package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutNavigationStateTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `rememberManualReadoutNavigationStateはデフォルトでListを初期値とする`() {
        lateinit var navigationState: ManualReadoutNavigationState

        rule.setContent {
            navigationState = rememberManualReadoutNavigationState()
        }

        rule.runOnIdle {
            assertEquals(ManualReadoutPaneDestination.List, navigationState.current)
        }
    }

    @Test
    fun `rememberManualReadoutNavigationStateはinitialに渡した宛先を初期値とする`() {
        lateinit var navigationState: ManualReadoutNavigationState

        rule.setContent {
            navigationState = rememberManualReadoutNavigationState(initial = ManualReadoutPaneDestination.Detail)
        }

        rule.runOnIdle {
            assertEquals(ManualReadoutPaneDestination.Detail, navigationState.current)
        }
    }

    @Test
    fun `初期状態はinitialで渡した宛先になる`() {
        val state = ManualReadoutNavigationState(NavBackStack<NavKey>(ManualReadoutPaneDestination.Detail))

        assertEquals(ManualReadoutPaneDestination.Detail, state.current)
    }

    @Test
    fun `backStackが空の場合はListにフォールバックする`() {
        val state = ManualReadoutNavigationState(NavBackStack())

        assertEquals(ManualReadoutPaneDestination.List, state.current)
    }

    @Test
    fun `navigateToを呼ぶとcurrentが切り替わる`() {
        val state = ManualReadoutNavigationState(NavBackStack<NavKey>(ManualReadoutPaneDestination.List))

        state.navigateTo(ManualReadoutPaneDestination.Detail)

        assertEquals(ManualReadoutPaneDestination.Detail, state.current)
    }

    @Test
    fun `現在の宛先と同じ宛先をnavigateToしてもcurrentは変わらない`() {
        val state = ManualReadoutNavigationState(NavBackStack<NavKey>(ManualReadoutPaneDestination.List))

        state.navigateTo(ManualReadoutPaneDestination.List)

        assertEquals(ManualReadoutPaneDestination.List, state.current)
    }
}
