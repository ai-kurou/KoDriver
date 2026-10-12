package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.HotkeyRegistrationState
import org.junit.Rule
import org.junit.Test

class ReadoutHotkeyCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `非対応状態のカード単体は登録文言を表示せず動作確認を無効にする`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutHotkeyCard(ReadoutHotkeyUiState(), {}, {})
            }
        }
        rule.onNodeWithText("動作確認").assertIsNotEnabled()
        rule.onNodeWithText("登録済み").assertDoesNotExist()
    }

    @Test
    fun `登録状態と動作確認結果を表示し非対応環境ではカードを隠す`() {
        val state = MutableStateFlow(ReadoutHotkeyUiState())
        rule.setContent {
            val hotkey by state.collectAsState()
            KoDriverTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState = LmuWindowsDebugStateDetailUiState(readoutHotkey = hotkey),
                    canNavigateBack = false,
                    onBack = {},
                    modifier = Modifier.requiredSize(1560.dp, 1080.dp),
                )
            }
        }
        rule.onNodeWithTag("readout_hotkey_card").assertDoesNotExist()
        state.update { it.copy(registrationState = HotkeyRegistrationState.Registering) }
        rule.onNodeWithText("登録中").assertIsDisplayed()
        rule.onNodeWithText("動作確認").assertIsNotEnabled()
        state.update { it.copy(registrationState = HotkeyRegistrationState.Conflict) }
        rule.onNodeWithText("他のアプリが使用中のため登録できません").assertIsDisplayed()
        state.update { it.copy(registrationState = HotkeyRegistrationState.Failed(5)) }
        rule.onNodeWithText("登録に失敗しました（エラーコード: 5）").assertIsDisplayed()
        state.update {
            it.copy(registrationState = HotkeyRegistrationState.Registered, checkState = HotkeyCheckState.Waiting)
        }
        rule.onNodeWithText("登録済み").assertIsDisplayed()
        rule.onNodeWithText("ホットキーを押してください（最大10秒）").assertIsDisplayed()
        rule.onNodeWithText("動作確認").assertIsNotEnabled()
        state.update { it.copy(checkState = HotkeyCheckState.Detected) }
        rule.onNodeWithText("検出しました").assertIsDisplayed()
        state.update { it.copy(checkState = HotkeyCheckState.TimedOut) }
        rule.onNodeWithText("検出されませんでした").assertIsDisplayed()
    }
}
