package kurou.kodriver.feature.otherlist

import kurou.kodriver.domain.repository.HapticFeedbackAvailabilityRepository
import kurou.kodriver.domain.repository.StartupEnabledRepository
import org.koin.dsl.module

/**
 * テスト用の Fake Koin モジュール（testFixtures）。:core:windows-startup-data の代わりに
 * StartupEnabledRepository の Fake 実装をバインドし、実OSのレジストリ操作を避ける。また、
 * HapticFeedbackAvailabilityRepository を常に振動機能ありとするFake実装に差し替え、
 * テスト実行環境（実機・エミュレータ）の振動ハードウェア有無にテスト結果が左右されないようにする。
 */
val fakeOtherListModule =
    module {
        single<StartupEnabledRepository> { FakeStartupEnabledRepository() }
        single<HapticFeedbackAvailabilityRepository> { FakeHapticFeedbackAvailabilityRepository() }
    }

class FakeStartupEnabledRepository : StartupEnabledRepository {
    private var enabled = false

    override suspend fun isEnabled(): Boolean = enabled

    override suspend fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}

class FakeHapticFeedbackAvailabilityRepository : HapticFeedbackAvailabilityRepository {
    var available = true

    override fun isHapticFeedbackAvailable(): Boolean = available
}
