package kurou.kodriver.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.NarrationSource
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.feature.otherandroidaudiousagedetail.otherAndroidAudioUsageDetailModule
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.fakeOtherListModule
import kurou.kodriver.feature.telemetryloglist.fakeTelemetryLogListModule
import kurou.kodriver.feature.telemetryloglist.fakeTelemetryLogRepository
import kurou.kodriver.feature.telemetryloglist.telemetryLogListModule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertEquals

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
                        startKoin {
                            modules(
                                otherAndroidAudioUsageDetailModule,
                                fakeOtherListModule,
                                telemetryLogListModule,
                                fakeTelemetryLogListModule,
                                module {
                                    single<SimulatorPreferencesRepository> {
                                        object : SimulatorPreferencesRepository {
                                            override fun selectedSimulator() = flowOf<Simulator>(Simulator.LmuWindows)

                                            override suspend fun saveSelectedSimulator(simulator: Simulator) = Unit
                                        }
                                    }
                                },
                            )
                        }
                    }

                    override fun after() {
                        fakeTelemetryLogRepository.clear()
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

    @Test
    fun `ログを表示でフィードバック送信を選ぶとonFeedbackSelectedにログIDを渡す`() {
        fakeTelemetryLogRepository.emit(
            listOf(
                TelemetryLog(
                    id = 7L,
                    createdAt = 100L,
                    simulator = Simulator.LmuWindows,
                    readoutItemKey =
                        object : ReadoutItemKey {
                            override val value = "lmu_windows_auto_sector_yellow_flag"
                        },
                    narratedText = "イエローフラッグ",
                    narrationOutcome = NarrationOutcome.INTERRUPTED,
                    narrationSource = NarrationSource.AUTO,
                    telemetryJson = "{}",
                ),
            ),
        )
        var selectedLogId: Long? = null
        rule.setContent {
            MaterialTheme {
                DefaultOtherDetailContent(
                    itemType = OtherListItemType.ShowLogs,
                    selectedSimulatorId = "",
                    canNavigateBack = false,
                    onBack = {},
                    feedbackTelemetryLogId = null,
                    feedbackAttachRequestId = 0L,
                    onFeedbackSelected = { selectedLogId = it },
                )
            }
        }

        rule.onNodeWithContentDescription("その他の操作").performClick()
        rule.onNodeWithText("フィードバック送信").performClick()

        assertEquals(7L, selectedLogId)
    }
}
