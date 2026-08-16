package kurou.kodriver.feature.otherlist

import kurou.kodriver.domain.repository.StartupEnabledRepository
import org.koin.dsl.module

/**
 * テスト用の Fake Koin モジュール（testFixtures）。:core:windows-startup-data の代わりに
 * StartupEnabledRepository の Fake 実装をバインドし、実OSのレジストリ操作を避ける。
 */
val fakeOtherListModule =
    module {
        single<StartupEnabledRepository> { FakeStartupEnabledRepository() }
    }

class FakeStartupEnabledRepository : StartupEnabledRepository {
    private var enabled = false

    override suspend fun isEnabled(): Boolean = enabled

    override suspend fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}
