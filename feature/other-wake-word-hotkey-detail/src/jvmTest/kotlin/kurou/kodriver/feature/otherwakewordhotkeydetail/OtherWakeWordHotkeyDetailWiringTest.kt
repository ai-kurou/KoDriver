package kurou.kodriver.feature.otherwakewordhotkeydetail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.pressKey
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.HotkeyRegistrationState
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherWakeWordHotkeyDetailWiringTest {
    private val rule = createComposeRule()

    // Compose の後片付けが終わってから Main ディスパッチャーを戻す。
    @get:Rule
    val rules: TestRule =
        RuleChain
            .outerRule(
                object : ExternalResource() {
                    override fun before() {
                        Dispatchers.setMain(UnconfinedTestDispatcher())
                    }

                    override fun after() {
                        Dispatchers.resetMain()
                    }
                },
            ).around(rule)

    private val uriHandler: UriHandler = mockk()

    @Test
    fun `画面の操作はホットキーの保存と解除とガイドの表示に配線される`() {
        val url = "https://github.com/ai-kurou/KoDriver/blob/main/docs/simhub-hotkey-setup.md"
        every { uriHandler.openUri(url) } returns Unit
        val owner =
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        startKoin { modules(otherWakeWordHotkeyDetailModule, fakeOtherWakeWordHotkeyDetailModule) }
        try {
            rule.setContent {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides owner,
                    LocalUriHandler provides uriHandler,
                ) {
                    KoDriverTheme {
                        OtherWakeWordHotkeyDetailPane(canNavigateBack = false, onBack = {}, showHotkey = true)
                    }
                }
            }
            rule.onNodeWithTag("wake_word_hotkey_modifiers").performClick()
            rule.onNodeWithTag("wake_word_hotkey_modifiers_CTRL_SHIFT").performClick()
            rule.onNodeWithText("Ctrl + Shift").assertIsDisplayed()
            rule.onNodeWithTag("wake_word_hotkey_key").performClick().performKeyInput { pressKey(Key.A) }
            rule.onNodeWithText("A").assertIsDisplayed()
            rule.onNodeWithText("音声認識がOFFのため、押しても反応しません").performScrollTo().assertIsDisplayed()
            rule.onNodeWithText("割り当てを解除").performClick()
            rule.onNodeWithText("割り当てなし").assertIsDisplayed()
            rule.onNodeWithText("音声認識がOFFのため、押しても反応しません").assertDoesNotExist()
            rule.onNodeWithText("ステアリングのボタンから実行する方法を見る").performScrollTo().performClick()
            rule.waitForIdle()
            verify(exactly = 1) { uriHandler.openUri(url) }
            confirmVerified(uriHandler)
        } finally {
            owner.viewModelStore.clear()
            stopKoin()
        }
    }

    @Test
    fun `登録の競合と失敗と重複の文言を表示し再試行を通知する`() {
        var retried = 0
        val states =
            mutableStateOf(
                OtherWakeWordHotkeyDetailUiState(
                    hotkeyVirtualKeyCode = 0x41,
                    hotkeyRegistrationState = HotkeyRegistrationState.Conflict,
                ),
            )
        rule.setContent {
            KoDriverTheme {
                OtherWakeWordHotkeyDetailContent(
                    uiState = states.value,
                    canNavigateBack = false,
                    onBack = {},
                    onRetryHotkeyRegistration = { retried++ },
                )
            }
        }
        rule.onNodeWithText("他のアプリが使用中のため登録できません").assertIsDisplayed()
        rule.onNodeWithText("再試行").performClick()
        rule.runOnIdle { assertEquals(1, retried) }

        states.value =
            states.value.copy(
                hotkeyRegistrationState =
                    kurou.kodriver.domain.model.HotkeyRegistrationState
                        .Failed(5),
                hotkeyConflict = OtherWakeWordHotkeyConflict("lmu_windows", 2, "レース用"),
            )
        rule.onNodeWithText("登録に失敗しました（エラーコード: 5）").assertIsDisplayed()
        rule.onNodeWithText("の「レース用」で使用中のため割り当てできません", substring = true).assertIsDisplayed()

        states.value =
            states.value.copy(
                hotkeyRegistrationState = HotkeyRegistrationState.Registering,
                hotkeyConflict = OtherWakeWordHotkeyConflict("lmu_windows", 2),
            )
        rule.onNodeWithText("登録中").assertIsDisplayed()
        rule.onNodeWithText("のセット2で使用中のため割り当てできません", substring = true).assertIsDisplayed()
    }
}
