package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import kurou.kodriver.feature.otherlist.OtherListItemType
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppScreenEffectsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `起動と復帰でTTS利用可否を再判定し再描画では再判定しない`() {
        var checkCount = 0
        val owner =
            object : LifecycleOwner {
                val registry = LifecycleRegistry.createUnsafe(this)
                override val lifecycle: Lifecycle = registry
            }
        val redraw = mutableStateOf(false)
        owner.registry.currentState = Lifecycle.State.RESUMED
        rule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                Text(redraw.value.toString())
                AppTtsAvailabilityEffect { checkCount++ }
            }
        }
        rule.waitForIdle()
        rule.runOnIdle { redraw.value = true }
        rule.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        rule.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        rule.waitForIdle()
        assertEquals(2, checkCount)
    }

    @Test
    fun `タブ再選択時に選択中の項目がある場合は選択を解除する`() {
        var clearSelectedItemCount = 0
        var requestScrollToTopCount = 0

        handleTabReselected(
            selectedItem = "selected",
            clearSelectedItem = { clearSelectedItemCount++ },
            requestScrollToTop = { requestScrollToTopCount++ },
        )

        assertEquals(1, clearSelectedItemCount)
        assertEquals(0, requestScrollToTopCount)
    }

    @Test
    fun `タブ再選択時に選択中の項目がない場合は先頭スクロールを要求する`() {
        var clearSelectedItemCount = 0
        var requestScrollToTopCount = 0

        handleTabReselected(
            selectedItem = null,
            clearSelectedItem = { clearSelectedItemCount++ },
            requestScrollToTop = { requestScrollToTopCount++ },
        )

        assertEquals(0, clearSelectedItemCount)
        assertEquals(1, requestScrollToTopCount)
    }

    @Test
    fun `接続バナーがタップ可能な場合は対応するその他項目を選択する`() {
        val bannerUiState =
            mutableStateOf(
                ConnectionBannerUiState(
                    isTappable = true,
                    tapNavigationTarget = ConnectionBannerNavigationTarget.ServerIp,
                ),
            )
        var selectedItemType: OtherListItemType? = null
        var onBannerTap: (() -> Unit)? = null

        rule.setContent {
            onBannerTap =
                rememberConnectionBannerTap(
                    bannerUiState = bannerUiState.value,
                    onSelectOtherItem = { selectedItemType = it },
                )
        }

        onBannerTap?.invoke()
        rule.waitForIdle()

        assertEquals(OtherListItemType.ServerIp, selectedItemType)
    }

    @Test
    fun `接続バナーがタップ不可の場合はタップ処理を作らない`() {
        var onBannerTap: (() -> Unit)? = {}

        rule.setContent {
            onBannerTap =
                rememberConnectionBannerTap(
                    bannerUiState =
                        ConnectionBannerUiState(
                            isTappable = false,
                            tapNavigationTarget = ConnectionBannerNavigationTarget.ConsoleIp,
                        ),
                    onSelectOtherItem = {},
                )
        }

        rule.waitForIdle()

        assertNull(onBannerTap)
    }
}
