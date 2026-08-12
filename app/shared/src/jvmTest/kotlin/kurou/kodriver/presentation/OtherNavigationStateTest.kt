package kurou.kodriver.presentation

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class OtherNavigationStateTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `rememberOtherNavigationStateはデフォルトでListを初期値とする`() {
        lateinit var navigationState: OtherNavigationState

        rule.setContent {
            navigationState = rememberOtherNavigationState()
        }

        rule.runOnIdle {
            assertEquals(OtherPaneDestination.List, navigationState.current)
        }
    }

    @Test
    fun `rememberOtherNavigationStateはinitialに渡した宛先を初期値とする`() {
        lateinit var navigationState: OtherNavigationState

        rule.setContent {
            navigationState = rememberOtherNavigationState(initial = OtherPaneDestination.Detail)
        }

        rule.runOnIdle {
            assertEquals(OtherPaneDestination.Detail, navigationState.current)
        }
    }

    @Test
    fun `初期状態はinitialで渡した宛先になる`() {
        val state = OtherNavigationState(NavBackStack<NavKey>(OtherPaneDestination.Detail))

        assertEquals(OtherPaneDestination.Detail, state.current)
    }

    @Test
    fun `backStackが空の場合はListにフォールバックする`() {
        val state = OtherNavigationState(NavBackStack())

        assertEquals(OtherPaneDestination.List, state.current)
    }

    @Test
    fun `navigateToを呼ぶとcurrentが切り替わる`() {
        val state = OtherNavigationState(NavBackStack<NavKey>(OtherPaneDestination.List))

        state.navigateTo(OtherPaneDestination.Detail)

        assertEquals(OtherPaneDestination.Detail, state.current)
    }

    @Test
    fun `現在の宛先と同じ宛先をnavigateToしてもcurrentは変わらない`() {
        val state = OtherNavigationState(NavBackStack<NavKey>(OtherPaneDestination.List))

        state.navigateTo(OtherPaneDestination.List)

        assertEquals(OtherPaneDestination.List, state.current)
    }
}
