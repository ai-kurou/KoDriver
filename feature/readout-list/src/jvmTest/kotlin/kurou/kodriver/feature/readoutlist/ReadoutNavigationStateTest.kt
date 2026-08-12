package kurou.kodriver.feature.readoutlist

import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutNavigationStateTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `rememberReadoutNavigationStateはデフォルトでListを初期値とする`() {
        lateinit var navigationState: ReadoutNavigationState

        rule.setContent {
            navigationState = rememberReadoutNavigationState()
        }

        rule.runOnIdle {
            assertEquals(ReadoutPaneDestination.List, navigationState.current)
        }
    }

    @Test
    fun `rememberReadoutNavigationStateはinitialに渡した宛先を初期値とする`() {
        lateinit var navigationState: ReadoutNavigationState

        rule.setContent {
            navigationState = rememberReadoutNavigationState(initial = ReadoutPaneDestination.Detail)
        }

        rule.runOnIdle {
            assertEquals(ReadoutPaneDestination.Detail, navigationState.current)
        }
    }

    @Test
    fun `初期状態はinitialで渡した宛先になる`() {
        val state = ReadoutNavigationState(NavBackStack<NavKey>(ReadoutPaneDestination.Detail))

        assertEquals(ReadoutPaneDestination.Detail, state.current)
    }

    @Test
    fun `backStackが空の場合はListにフォールバックする`() {
        val state = ReadoutNavigationState(NavBackStack())

        assertEquals(ReadoutPaneDestination.List, state.current)
    }

    @Test
    fun `navigateToを呼ぶとcurrentが切り替わる`() {
        val state = ReadoutNavigationState(NavBackStack<NavKey>(ReadoutPaneDestination.List))

        state.navigateTo(ReadoutPaneDestination.Detail)

        assertEquals(ReadoutPaneDestination.Detail, state.current)
    }

    @Test
    fun `現在の宛先と同じ宛先をnavigateToしてもcurrentは変わらない`() {
        val state = ReadoutNavigationState(NavBackStack<NavKey>(ReadoutPaneDestination.List))

        state.navigateTo(ReadoutPaneDestination.List)

        assertEquals(ReadoutPaneDestination.List, state.current)
    }

    @Test
    fun `ReadoutNavigationStateSaverはcurrentの名前を保存しNavBackStackを再構築して復元する`() {
        val saverScope = SaverScope { true }
        val state = ReadoutNavigationState(NavBackStack<NavKey>(ReadoutPaneDestination.Detail))

        val saved = with(ReadoutNavigationStateSaver) { saverScope.save(state) }
        val restored = ReadoutNavigationStateSaver.restore(checkNotNull(saved))

        assertEquals(ReadoutPaneDestination.Detail.name, saved)
        assertEquals(ReadoutPaneDestination.Detail, restored?.current)
    }

    @Test
    fun `ReadoutNavigationStateSaverは未知のキーからの復元でnullを返す`() {
        val restored = ReadoutNavigationStateSaver.restore("UnknownDestination")

        assertEquals(null, restored)
    }
}
