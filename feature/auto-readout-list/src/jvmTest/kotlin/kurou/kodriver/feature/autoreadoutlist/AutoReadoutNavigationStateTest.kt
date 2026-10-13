package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class AutoReadoutNavigationStateTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `rememberAutoReadoutNavigationStateはデフォルトでListを初期値とする`() {
        lateinit var navigationState: AutoReadoutNavigationState

        rule.setContent {
            navigationState = rememberAutoReadoutNavigationState()
        }

        rule.runOnIdle {
            assertEquals(AutoReadoutPaneDestination.List, navigationState.current)
        }
    }

    @Test
    fun `rememberAutoReadoutNavigationStateはinitialに渡した宛先を初期値とする`() {
        lateinit var navigationState: AutoReadoutNavigationState

        rule.setContent {
            navigationState = rememberAutoReadoutNavigationState(initial = AutoReadoutPaneDestination.Detail)
        }

        rule.runOnIdle {
            assertEquals(AutoReadoutPaneDestination.Detail, navigationState.current)
        }
    }

    @Test
    fun `初期状態はinitialで渡した宛先になる`() {
        val state = AutoReadoutNavigationState(NavBackStack<NavKey>(AutoReadoutPaneDestination.Detail))

        assertEquals(AutoReadoutPaneDestination.Detail, state.current)
    }

    @Test
    fun `backStackが空の場合はListにフォールバックする`() {
        val state = AutoReadoutNavigationState(NavBackStack())

        assertEquals(AutoReadoutPaneDestination.List, state.current)
    }

    @Test
    fun `navigateToを呼ぶとcurrentが切り替わる`() {
        val state = AutoReadoutNavigationState(NavBackStack<NavKey>(AutoReadoutPaneDestination.List))

        state.navigateTo(AutoReadoutPaneDestination.Detail)

        assertEquals(AutoReadoutPaneDestination.Detail, state.current)
    }

    @Test
    fun `現在の宛先と同じ宛先をnavigateToしてもcurrentは変わらない`() {
        val state = AutoReadoutNavigationState(NavBackStack<NavKey>(AutoReadoutPaneDestination.List))

        state.navigateTo(AutoReadoutPaneDestination.List)

        assertEquals(AutoReadoutPaneDestination.List, state.current)
    }
}
