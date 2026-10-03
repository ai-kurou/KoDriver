package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutPitTimingDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文とカードタイトルを表示する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent()
            }
        }

        rule
            .onNodeWithText(
                "ピットインの最適なタイミングが近づいたときに音声でお知らせします。\n" +
                    "毎周ベストラップの30秒前に、燃料残量・タイヤ摩耗の予想残り周回数を判定し、" +
                    "いずれかが閾値以下であれば、より緊急性の高い（予想残り周回数が少ない）方を1回だけ読み上げます。\n" +
                    "バーチャルエナジーは入力した文言を音声合成で、タイヤ摩耗は収録音声で読み上げます。",
            ).assertIsDisplayed()
        rule.onNodeWithText("バーチャルエナジー").assertIsDisplayed()
        rule.onNodeWithText("タイヤ摩耗").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("バーチャルエナジー予想残り周回数").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("タイヤ摩耗予想残り周回数").performScrollTo().assertIsDisplayed()
        rule.onAllNodesWithText("N周以内にピットイン・必ずピットイン").assertCountEquals(1)
        rule.onAllNodesWithText("残り約: 3 周").assertCountEquals(2)
    }

    @Test
    fun `チップをタップするとコールバックが呼ばれる`() {
        var previewClicked = false
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    onPreviewClicked = { previewClicked = true },
                )
            }
        }

        rule.onAllNodesWithText("N周以内にピットイン・必ずピットイン")[0].performScrollTo().performClick()

        assert(previewClicked)
    }

    @Test
    fun `バーチャルエナジーのスイッチをタップするとコールバックが呼ばれる`() {
        var virtualEnergyEnabled = true
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState = LmuWindowsReadoutPitTimingDetailUiState(virtualEnergyEnabled = virtualEnergyEnabled),
                    onVirtualEnergyEnabledChanged = { virtualEnergyEnabled = it },
                )
            }
        }

        rule.onNodeWithText("バーチャルエナジー").performClick()

        assert(!virtualEnergyEnabled)
    }

    @Test
    fun `タイヤ摩耗のスイッチをタップするとコールバックが呼ばれる`() {
        var tyreWearEnabled = true
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState = LmuWindowsReadoutPitTimingDetailUiState(tyreWearEnabled = tyreWearEnabled),
                    onTyreWearEnabledChanged = { tyreWearEnabled = it },
                )
            }
        }

        rule.onNodeWithText("タイヤ摩耗").performScrollTo().performClick()

        assert(!tyreWearEnabled)
    }

    @Test
    fun `バーチャルエナジーのスライダーを動かすとコールバックが呼ばれる`() {
        var virtualEnergyLaps = 3
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState = LmuWindowsReadoutPitTimingDetailUiState(virtualEnergyLaps = virtualEnergyLaps),
                    onVirtualEnergyLapsChanged = { virtualEnergyLaps = it },
                )
            }
        }

        rule
            .onAllNodes(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 3f, range = 1f..5f, steps = 3)),
            )[0]
            .performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) {
                it(5f)
            }

        assert(virtualEnergyLaps == 5)
    }

    @Test
    fun `タイヤ摩耗のスライダーを動かすとコールバックが呼ばれる`() {
        var tyreWearLaps = 3
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState = LmuWindowsReadoutPitTimingDetailUiState(tyreWearLaps = tyreWearLaps),
                    onTyreWearLapsChanged = { tyreWearLaps = it },
                )
            }
        }

        rule
            .onAllNodes(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 3f, range = 1f..5f, steps = 3)),
            )[1]
            .performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) {
                it(1f)
            }

        assert(tyreWearLaps == 1)
    }

    @Test
    fun `ヘルプアイコンをタップするとヘルプシートが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent()
            }
        }

        rule.onNodeWithContentDescription("バーチャルエナジー予想残り周回数の計算方法の説明を表示").performScrollTo().performClick()

        rule.onNodeWithText("直近1周分の消費量", substring = true).assertIsDisplayed()
    }

    @Test
    fun `スイッチOFFでも通常と切迫時の文言を編集し入力中の文言を試聴できる`() {
        val changed = mutableListOf<String>()
        val imminentChanged = mutableListOf<String>()
        val previews = mutableListOf<String>()
        val imminentPreviews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutPitTimingDetailUiState(
                            virtualEnergyEnabled = false,
                            isTextToSpeechAvailable = true,
                        ),
                    onVirtualEnergyTextChanged = { changed += it },
                    onVirtualEnergyImminentTextChanged = { imminentChanged += it },
                    onVirtualEnergyTextPreviewClicked = { previews += it },
                    onVirtualEnergyImminentTextPreviewClicked = { imminentPreviews += it },
                )
            }
        }
        rule
            .onAllNodes(hasSetTextAction())[0]
            .performScrollTo()
            .assertIsEnabled()
            .performTextReplacement("あ".repeat(31))
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performScrollTo().performClick()
        rule.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextReplacement("必ず{laps}")
        rule.onAllNodesWithContentDescription("入力した文言を再生")[1].performScrollTo().performClick()
        assertEquals(listOf("あ".repeat(30)), changed)
        assertEquals(changed, previews)
        assertEquals(listOf("必ず{laps}"), imminentChanged)
        assertEquals(imminentChanged, imminentPreviews)
    }

    @Test
    fun `入力直後の文言にlapsを挿入し上限ちょうどで無効になる`() {
        val changed = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState = LmuWindowsReadoutPitTimingDetailUiState(isTextToSpeechAvailable = true),
                    onVirtualEnergyTextChanged = { changed += it },
                )
            }
        }
        val text = "あ".repeat(24)
        rule.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextReplacement(text)
        rule
            .onNodeWithText("{laps}を挿入")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf(text, text + "{laps}"), changed)
        rule.onNodeWithText("{laps}を挿入").assertIsNotEnabled()
    }

    @Test
    fun `挿入で上限を超える場合は無効にする`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutPitTimingDetailUiState(
                            virtualEnergyText = "あ".repeat(25),
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("{laps}を挿入").assertIsNotEnabled()
    }

    @Test
    fun `未知トークン警告は通常文言だけに表示し空欄なら読み上げない案内を表示する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutPitTimingDetailUiState(
                            virtualEnergyText = "{lap}{x}{lap}",
                            virtualEnergyImminentText = "",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("{lap}、{x} は置き換えられません。{laps} を使用してください").assertExists()
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
    }

    @Test
    fun `切迫時の未知トークンは警告しない`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutPitTimingDetailUiState(
                            virtualEnergyText = "",
                            virtualEnergyImminentText = "{lap}{laps}",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("{lap} は置き換えられません。{laps} を使用してください").assertDoesNotExist()
        rule.onNodeWithText("空欄のままなら読み上げません").assertExists()
    }

    @Test
    fun `TTS不可では警告より利用不可案内を優先し入力と試聴と挿入を無効にする`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState = LmuWindowsReadoutPitTimingDetailUiState(virtualEnergyText = "{lap}"),
                )
            }
        }
        rule.onNodeWithText("{laps}を挿入").assertIsNotEnabled()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].assertIsNotEnabled()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[1].assertIsNotEnabled()
        rule.onNodeWithText("{lap}").assertIsNotEnabled()
        rule
            .onAllNodesWithText("この端末では音声合成を利用できないため、バーチャルエナジーは読み上げません")
            .assertCountEquals(2)
    }
}
