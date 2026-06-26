package kurou.kodriver

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.core.gt7ps5data.createGt7UdpPortPreferencesRepository
import kurou.kodriver.core.gt7ps5data.gt7Ps5DataModule
import kurou.kodriver.domain.repository.Gt7UdpPortPreferencesRepository
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
                        module {
                            single<Gt7UdpPortPreferencesRepository> {
                                createGt7UdpPortPreferencesRepository(
                                    "${System.getProperty("user.home")}/.kodriver",
                                )
                            }
                        },
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
    fun `シミュレータ選択後に最上位の読み上げ項目をタップしその他タブへ移動しライセンスを開く`() {
        rule.setContent { AppScreen() }

        // シミュレータ選択ドロップダウンを開く
        rule.onNode(hasContentDescription("シミュレータを選択")).performClick()
        rule.waitForIdle()

        // LMU Windowsシミュレータを選択
        rule.onNode(hasText("Le Mans Ultimate（Windows版）")).performClick()
        rule.waitForIdle()

        // 読み上げリストが表示されるまで待機
        rule.waitUntil(timeoutMillis = 5_000L) {
            rule.onAllNodes(hasText("フラッグ")).fetchSemanticsNodes().isNotEmpty()
        }
        // 読み上げ項目「フラッグ」をタップ
        rule.onNode(hasText("フラッグ")).performClick()
        rule.waitForIdle()

        // 読み上げ項目「車両接近」をタップ
        rule.onNode(hasText("車両接近")).performClick()
        rule.waitForIdle()

        // 読み上げ項目「車両故障」をタップ
        rule.onNode(hasText("車両故障")).performClick()
        rule.waitForIdle()

        // その他タブへ移動
        rule.onNode(hasText("その他")).performClick()
        rule.waitForIdle()

        // 音量をタップ
        rule.onNode(hasText("音量")).performClick()
        rule.waitForIdle()

        // 読み上げ開始音をタップ
        rule.onNode(hasText("読み上げ開始音")).performClick()
        rule.waitForIdle()

        // ダイアログをキャンセル
        rule.onNodeWithText("キャンセル").performClick()
        rule.waitForIdle()

        // ライセンスをタップ
        rule.onNode(hasText("ライセンス")).performClick()
        rule.waitForIdle()
    }
}
