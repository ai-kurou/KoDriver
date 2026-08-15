package kurou.kodriver.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TARGET_PACKAGE_NAME = "kurou.kodriver"
private const val UI_TIMEOUT_MS = 5_000L

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() {
        baselineProfileRule.collect(packageName = TARGET_PACKAGE_NAME) {
            pressHome()
            startActivityAndWait()

            // 起動〜シミュレータ選択〜読み上げ一覧表示までの
            // クリティカルユーザージャーニーをプロファイル対象にする
            device.wait(Until.hasObject(By.desc("シミュレータを選択")), UI_TIMEOUT_MS)
            device.findObject(By.desc("シミュレータを選択")).click()
            device.wait(Until.hasObject(By.text("Le Mans Ultimate（Windows版）")), UI_TIMEOUT_MS)
            device.findObjects(By.text("Le Mans Ultimate（Windows版）")).last().click()

            device.wait(Until.hasObject(By.text("フラッグ")), UI_TIMEOUT_MS)
        }
    }
}
