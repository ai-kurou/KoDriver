package kurou.kodriver.feature.telemetryloglist

import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class TelemetryLogNavigationStateTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `rememberTelemetryLogNavigationStateはデフォルトでListを初期値とする`() {
        lateinit var navigationState: TelemetryLogNavigationState

        rule.setContent {
            navigationState = rememberTelemetryLogNavigationState()
        }

        rule.runOnIdle {
            assertEquals(TelemetryLogPaneDestination.List, navigationState.current)
        }
    }

    @Test
    fun `rememberTelemetryLogNavigationStateはinitialに渡した宛先を初期値とする`() {
        lateinit var navigationState: TelemetryLogNavigationState

        rule.setContent {
            navigationState = rememberTelemetryLogNavigationState(initial = TelemetryLogPaneDestination.Detail)
        }

        rule.runOnIdle {
            assertEquals(TelemetryLogPaneDestination.Detail, navigationState.current)
        }
    }

    @Test
    fun `初期状態はinitialで渡した宛先になる`() {
        val state = TelemetryLogNavigationState(NavBackStack<NavKey>(TelemetryLogPaneDestination.Detail))

        assertEquals(TelemetryLogPaneDestination.Detail, state.current)
    }

    @Test
    fun `backStackが空の場合はListにフォールバックする`() {
        val state = TelemetryLogNavigationState(NavBackStack())

        assertEquals(TelemetryLogPaneDestination.List, state.current)
    }

    @Test
    fun `navigateToを呼ぶとcurrentが切り替わる`() {
        val state = TelemetryLogNavigationState(NavBackStack<NavKey>(TelemetryLogPaneDestination.List))

        state.navigateTo(TelemetryLogPaneDestination.Detail)

        assertEquals(TelemetryLogPaneDestination.Detail, state.current)
    }

    @Test
    fun `現在の宛先と同じ宛先をnavigateToしてもcurrentは変わらない`() {
        val state = TelemetryLogNavigationState(NavBackStack<NavKey>(TelemetryLogPaneDestination.List))

        state.navigateTo(TelemetryLogPaneDestination.List)

        assertEquals(TelemetryLogPaneDestination.List, state.current)
    }

    @Test
    fun `TelemetryLogNavigationStateSaverはcurrentの名前を保存しNavBackStackを再構築して復元する`() {
        val saverScope = SaverScope { true }
        val state = TelemetryLogNavigationState(NavBackStack<NavKey>(TelemetryLogPaneDestination.Detail))

        val saved = with(TelemetryLogNavigationStateSaver) { saverScope.save(state) }
        val restored = TelemetryLogNavigationStateSaver.restore(checkNotNull(saved))

        assertEquals(TelemetryLogPaneDestination.Detail.name, saved)
        assertEquals(TelemetryLogPaneDestination.Detail, restored?.current)
    }

    @Test
    fun `TelemetryLogNavigationStateSaverは未知のキーからの復元でnullを返す`() {
        val restored = TelemetryLogNavigationStateSaver.restore("UnknownDestination")

        assertEquals(null, restored)
    }
}
