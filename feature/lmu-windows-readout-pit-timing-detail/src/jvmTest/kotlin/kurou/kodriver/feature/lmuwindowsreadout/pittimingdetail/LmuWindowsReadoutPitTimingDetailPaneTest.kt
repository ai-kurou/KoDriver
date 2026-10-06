package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LmuWindowsReadoutPitTimingDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと入力欄の間に4dpを確保する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState = LmuWindowsReadoutPitTimingDetailUiState(isTextToSpeechAvailable = true),
                    modifier = Modifier.requiredSize(360.dp, 4000.dp),
                )
            }
        }

        val expected = with(rule.density) { KoDriverSpacing.extraSmall.toPx() }
        val fields = rule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().map { it.boundsInRoot }
        listOf(
            "通常（残り1周以上）",
            "ピットイン必須（残り1周未満）",
        ).forEach { label ->
            rule.onAllNodesWithText(label).assertCountEquals(2)
            rule.onAllNodesWithText(label).fetchSemanticsNodes().forEach { node ->
                val labelBounds = node.boundsInRoot
                val fieldBounds = fields.first { it.top >= labelBounds.bottom }
                assertEquals(expected, fieldBounds.top - labelBounds.bottom, absoluteTolerance = 1f)
            }
        }
    }

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
                    "読み上げる文言は、バーチャルエナジー・タイヤ摩耗それぞれ下の欄で設定できます。",
            ).assertIsDisplayed()
        rule.onNodeWithText("バーチャルエナジー").assertIsDisplayed()
        rule.onNodeWithText("タイヤ摩耗").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("バーチャルエナジー予想残り周回数").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("タイヤ摩耗予想残り周回数").performScrollTo().assertIsDisplayed()
        rule.onAllNodesWithText("残り約: 3 周").assertCountEquals(2)
        rule.onAllNodesWithText("通常（残り1周以上）").assertCountEquals(2)
        rule.onAllNodesWithText("ピットイン必須（残り1周未満）").assertCountEquals(2)
    }

    @Test
    fun `狭い幅でも挿入チップの右側にヒントを表示する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(modifier = Modifier.width(360.dp))
            }
        }

        val chip = rule.onAllNodesWithText("{laps}を挿入")[0]
        chip.performScrollTo().assertIsDisplayed()
        val hint = rule.onAllNodesWithText("{laps} は予想残り周回数に置き換わります")[0]
        hint.assertIsDisplayed()
        val chipBounds = chip.fetchSemanticsNode().boundsInRoot
        val hintBounds = hint.fetchSemanticsNode().boundsInRoot
        assertTrue(hintBounds.left > chipBounds.right)
        assertEquals(chipBounds.center.y, hintBounds.center.y, absoluteTolerance = 1f)
    }

    @Test
    fun `両カードでチップ行と必須文言の間にグループ余白を確保する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutPitTimingDetailUiState(
                            virtualEnergyText = "通常エナジー",
                            virtualEnergyImminentText = "必須エナジー",
                            tyreWearText = "通常タイヤ",
                            tyreWearImminentText = "必須タイヤ",
                            isTextToSpeechAvailable = true,
                        ),
                    modifier = Modifier.requiredSize(360.dp, 4000.dp),
                )
            }
        }

        val groupSpacing = with(rule.density) { (KoDriverSpacing.large + KoDriverSpacing.small).toPx() }
        val labelSpacing = with(rule.density) { KoDriverSpacing.extraSmall.toPx() }
        val fields = rule.onAllNodes(hasSetTextAction())
        repeat(2) { index ->
            val imminentLabel = rule.onAllNodesWithText("ピットイン必須（残り1周未満）")[index]
            val imminentBounds = imminentLabel.fetchSemanticsNode().boundsInRoot
            val chipBounds = rule.onAllNodesWithText("{laps}を挿入")[index].fetchSemanticsNode().boundsInRoot
            val hintBounds =
                rule
                    .onAllNodesWithText("{laps} は予想残り周回数に置き換わります")[index]
                    .fetchSemanticsNode()
                    .boundsInRoot
            assertTrue(imminentBounds.top - maxOf(chipBounds.bottom, hintBounds.bottom) >= groupSpacing)
            val normalBounds = fields[index * 2].fetchSemanticsNode().boundsInRoot
            val imminentFieldBounds = fields[index * 2 + 1].fetchSemanticsNode().boundsInRoot
            assertTrue(imminentFieldBounds.top - normalBounds.bottom >= groupSpacing)
            assertEquals(labelSpacing, imminentFieldBounds.top - imminentBounds.bottom, absoluteTolerance = 1f)
        }
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
            .onAllNodesWithText("{laps}を挿入")[0]
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf(text, text + "{laps}"), changed)
        rule.onAllNodesWithText("{laps}を挿入")[0].assertIsNotEnabled()
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
        rule.onAllNodesWithText("{laps}を挿入")[0].assertIsNotEnabled()
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
    fun `タイヤ摩耗のスイッチOFFでも通常と切迫時の文言を編集し入力中の文言を試聴できる`() {
        val changed = mutableListOf<String>()
        val imminentChanged = mutableListOf<String>()
        val previews = mutableListOf<String>()
        val imminentPreviews = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutPitTimingDetailUiState(
                            tyreWearEnabled = false,
                            isTextToSpeechAvailable = true,
                        ),
                    onTyreWearTextChanged = { changed += it },
                    onTyreWearImminentTextChanged = { imminentChanged += it },
                    onTyreWearTextPreviewClicked = { previews += it },
                    onTyreWearImminentTextPreviewClicked = { imminentPreviews += it },
                )
            }
        }
        rule
            .onAllNodes(hasSetTextAction())[2]
            .performScrollTo()
            .assertIsEnabled()
            .performTextReplacement("あ".repeat(31))
        rule.onAllNodesWithContentDescription("入力した文言を再生")[2].performScrollTo().performClick()
        rule.onAllNodes(hasSetTextAction())[3].performScrollTo().performTextReplacement("必ず{laps}")
        rule.onAllNodesWithContentDescription("入力した文言を再生")[3].performScrollTo().performClick()
        assertEquals(listOf("あ".repeat(30)), changed)
        assertEquals(changed, previews)
        assertEquals(listOf("必ず{laps}"), imminentChanged)
        assertEquals(imminentChanged, imminentPreviews)
    }

    @Test
    fun `タイヤ摩耗の入力直後の文言にlapsを挿入し上限ちょうどで無効になる`() {
        val changed = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState = LmuWindowsReadoutPitTimingDetailUiState(isTextToSpeechAvailable = true),
                    onTyreWearTextChanged = { changed += it },
                )
            }
        }
        val text = "あ".repeat(24)
        rule.onAllNodes(hasSetTextAction())[2].performScrollTo().performTextReplacement(text)
        rule
            .onAllNodesWithText("{laps}を挿入")[1]
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        assertEquals(listOf(text, text + "{laps}"), changed)
        rule.onAllNodesWithText("{laps}を挿入")[1].assertIsNotEnabled()
    }

    @Test
    fun `タイヤ摩耗の挿入で上限を超える場合は無効にする`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutPitTimingDetailUiState(
                            tyreWearText = "あ".repeat(25),
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onAllNodesWithText("{laps}を挿入")[1].assertIsNotEnabled()
    }

    @Test
    fun `タイヤ摩耗の未知トークン警告は通常文言だけに表示し空欄なら読み上げない案内を表示する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutPitTimingDetailUiState(
                            tyreWearText = "{lap}{x}{lap}",
                            tyreWearImminentText = "",
                            isTextToSpeechAvailable = true,
                        ),
                )
            }
        }
        rule.onNodeWithText("{lap}、{x} は置き換えられません。{laps} を使用してください").assertExists()
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
        rule.onAllNodesWithText("{laps}を挿入")[0].assertIsNotEnabled()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[0].assertIsNotEnabled()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[1].assertIsNotEnabled()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[2].assertIsNotEnabled()
        rule.onAllNodesWithContentDescription("入力した文言を再生")[3].assertIsNotEnabled()
        rule.onAllNodesWithText("{laps}を挿入")[1].assertIsNotEnabled()
        rule.onNodeWithText("{lap}").assertIsNotEnabled()
        rule
            .onAllNodesWithText("この端末では音声合成を利用できないため、読み上げません")
            .assertCountEquals(4)
    }
}
