package kurou.kodriver

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.core.gt7ps5data.gt7Ps5DataModule
import kurou.kodriver.core.lmuwindowsdata.lmuWindowsDataModule
import kurou.kodriver.data.desktopDataModule
import kurou.kodriver.feature.lmuwindowsnarrator.fakeLmuWindowsNarratorModule
import kurou.kodriver.feature.readoutlist.fakeReadoutListModule
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.appModules
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class AppTest {

    companion object {
        @BeforeClass @JvmStatic
        fun setUpKoin() {
            startKoin {
                modules(
                    listOf(
                        desktopDataModule,
                        lmuWindowsDataModule,
                        gt7Ps5DataModule,
                        fakeLmuWindowsNarratorModule,
                        fakeReadoutListModule,
                    ) + appModules,
                )
            }
        }

        @AfterClass @JvmStatic
        fun tearDownKoin() {
            stopKoin()
        }
    }

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `LMU選択時に読み上げ項目を順にタップする`() {
        rule.setContent { AppScreen() }

        selectSimulator("Le Mans Ultimate（Windows版）")

        waitUntilDisplayed("フラッグ")
        clickItem("フラッグ")
        clickItem("車両接近")
        clickItem("車両故障")
    }

    @Test
    fun `GT7選択時に読み上げ項目を順にタップする`() {
        rule.setContent { AppScreen() }

        selectSimulator("GranTurismo 7（PS5）")

        waitUntilDisplayed("燃料残り周回数")
        clickItem("燃料残り周回数")
        clickItem("自己ベストラップ")
    }

    @Test
    fun `その他タブの項目を順にタップする`() {
        rule.setContent { AppScreen() }

        clickItem("その他")
        clickItem("音量")
        clickItem("読み上げ開始音")
        clickItem("キャンセル")
        clickItem("ライセンス")
    }

    private fun selectSimulator(simulatorName: String) {
        rule.onNode(hasContentDescription("シミュレータを選択")).performClick()
        rule.waitForIdle()
        clickItem(simulatorName)
    }

    private fun waitUntilDisplayed(text: String) {
        rule.waitUntil(timeoutMillis = 5_000L) {
            rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun clickItem(text: String) {
        rule.onNodeWithText(text).performClick()
        rule.waitForIdle()
    }
}
