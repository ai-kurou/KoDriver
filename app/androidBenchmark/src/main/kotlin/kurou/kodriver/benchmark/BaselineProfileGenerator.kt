package kurou.kodriver.benchmark

import android.os.Build
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

private const val TARGET_PACKAGE_NAME = "kurou.kodriver"
private const val UI_TIMEOUT_MS = 30_000L
private const val LOCAL_NETWORK_PERMISSION = "android.permission.ACCESS_LOCAL_NETWORK"
private const val LOCAL_NETWORK_MIN_SDK = 37

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() {
        baselineProfileRule.collect(packageName = TARGET_PACKAGE_NAME) {
            // Android 17以降はACCESS_LOCAL_NETWORKの実行時権限ダイアログが起動直後のUIを覆うため、事前に許可する。
            if (Build.VERSION.SDK_INT >= LOCAL_NETWORK_MIN_SDK) {
                device.executeShellCommand("pm grant $TARGET_PACKAGE_NAME $LOCAL_NETWORK_PERMISSION")
            }
            pressHome()
            startActivityAndWait()

            // 起動〜シミュレーター選択〜読み上げ一覧表示までの
            // クリティカルユーザージャーニーをプロファイル対象にする。
            // アイコンのcontentDescriptionはCompose Resourcesの非同期解決に依存し
            // 出現直後はUiAutomatorから見つからないことがあるため、
            // 同期的に確定する短縮名テキスト（デフォルト選択中のLMU）をクリック対象にする。
            device.requireText("LMU")
            device.findObject(By.text("LMU")).click()
            device.requireText("Le Mans Ultimate（Windows版）")
            device.findObjects(By.text("Le Mans Ultimate（Windows版）")).last().click()

            device.requireText("フラッグ")
        }
    }
}

/** テキストが現れるまで待ち、現れなければ原因調査用に画面の階層ダンプを添えて失敗させる。 */
private fun UiDevice.requireText(text: String) {
    if (wait(Until.hasObject(By.text(text)), UI_TIMEOUT_MS)) return
    val hierarchy = ByteArrayOutputStream().also { dumpWindowHierarchy(it) }.toString(Charsets.UTF_8.name())
    error("「$text」が${UI_TIMEOUT_MS}ms以内に表示されませんでした。現在の画面:\n$hierarchy")
}
