package kurou.kodriver

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import kurou.kodriver.core.devicevolumedata.deviceVolumeDataModule
import kurou.kodriver.core.gt7ps5data.gt7Ps5DataModule
import kurou.kodriver.core.windowsstartupdata.windowsStartupDataModule
import kurou.kodriver.data.androidDataModule
import kurou.kodriver.presentation.featureModules
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.check.checkKoinModules
import org.robolectric.RobolectricTestRunner

/**
 * KoDriverApplication.kt の composition root（androidDataModule + gt7Ps5DataModule +
 * deviceVolumeDataModule + windowsStartupDataModule + featureModules + appVersion 定数）と
 * 同一のモジュール構成を、Koin の `checkKoinModules()` で実際に解決してみることで検証する。
 *
 * 登録漏れ・型不一致・依存解決不能な構成があっても、これまでは実際にその画面/機能を操作するまで
 * 実行時エラーとして顕在化しなかった（#1445）。このテストは KoDriverApplication.kt が束ねる
 * モジュール一覧が変わったときに追随して更新すること。
 *
 * Android 版は LMU/ACE 系の実データを Windows 共有メモリではなく KoDriver サーバーへの
 * WebSocket 経由で取得するため（`lmuWindowsDataModule`/`aceWindowsDataModule` を含まない）、
 * `DesktopKoinModuleGraphTest` と異なりダミー実装での置き換えは不要。
 */
@RunWith(RobolectricTestRunner::class)
class AndroidKoinModuleGraphTest {
    @Test
    fun `Android構成のKoinモジュールグラフが解決可能である`() {
        val context = ApplicationProvider.getApplicationContext<Application>()

        checkKoinModules(
            listOf(
                androidDataModule(context),
                gt7Ps5DataModule,
                deviceVolumeDataModule,
                windowsStartupDataModule,
            ) +
                featureModules +
                listOf(module { single(named("appVersion")) { "test" } }),
        )
    }
}
