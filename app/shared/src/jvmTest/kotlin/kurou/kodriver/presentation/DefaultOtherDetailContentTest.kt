package kurou.kodriver.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.feature.otherandroidaudiousagedetail.otherAndroidAudioUsageDetailModule
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.fakeOtherListModule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultOtherDetailContentTest {
    private val rule = createComposeRule()

    @get:Rule
    val rules: TestRule =
        RuleChain
            .outerRule(
                object : ExternalResource() {
                    override fun before() {
                        Dispatchers.setMain(UnconfinedTestDispatcher())
                        startKoin { modules(otherAndroidAudioUsageDetailModule, fakeOtherListModule) }
                    }

                    override fun after() {
                        stopKoin()
                        Dispatchers.resetMain()
                    }
                },
            ).around(rule)

    @Test
    fun `音声の用途を選択すると用途の選択肢を表示する`() {
        rule.setContent {
            MaterialTheme {
                DefaultOtherDetailContent(
                    itemType = OtherListItemType.AndroidAudioUsage,
                    selectedSimulatorId = "",
                    canNavigateBack = false,
                    onBack = {},
                    feedbackTelemetryLogId = null,
                    feedbackAttachRequestId = 0L,
                )
            }
        }

        rule.onNodeWithText("メディア").assertIsDisplayed()
        rule.onNodeWithText("音声の用途").assertIsDisplayed()
        rule.onNodeWithText("読み上げ音声を、メディアや通話などどの用途の音声として扱うか設定します。右の再生ボタンで、その用途で試聴できます。").assertIsDisplayed()
    }
}
