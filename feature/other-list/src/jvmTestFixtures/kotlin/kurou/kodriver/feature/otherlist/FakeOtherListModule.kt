package kurou.kodriver.feature.otherlist

import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import kurou.kodriver.domain.repository.AccessLocalNetworkPermissionRepository
import kurou.kodriver.domain.repository.HapticFeedbackAvailabilityRepository
import kurou.kodriver.domain.repository.StartupEnabledRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import org.koin.dsl.module

/**
 * テスト用の Fake Koin モジュール（testFixtures）。:core:windows-startup-data の代わりに
 * StartupEnabledRepository の Fake 実装をバインドし、実OSのレジストリ操作を避ける。また、
 * HapticFeedbackAvailabilityRepository を常に振動機能ありとするFake実装に、
 * AccessLocalNetworkPermissionRepository を常に許可済みとするFake実装に差し替え、
 * テスト実行環境（実機・エミュレータ）のハードウェア・権限状態にテスト結果が左右されないようにする。
 */
val fakeTextToSpeechRepository = FakeTextToSpeechRepository()

val fakeOtherListModule =
    module {
        single<StartupEnabledRepository> { FakeStartupEnabledRepository() }
        single<HapticFeedbackAvailabilityRepository> { FakeHapticFeedbackAvailabilityRepository() }
        single<AccessLocalNetworkPermissionRepository> { FakeAccessLocalNetworkPermissionRepository() }
        single<TextToSpeechRepository> { fakeTextToSpeechRepository }
    }

class FakeStartupEnabledRepository : StartupEnabledRepository {
    private var enabled = false

    override suspend fun isEnabled(): Boolean = enabled

    override suspend fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}

class FakeTextToSpeechRepository : TextToSpeechRepository {
    var unavailableReason: TextToSpeechUnavailableReason = TextToSpeechUnavailableReason.WindowsSpeechUnavailable

    override suspend fun isAvailable(): Boolean = false

    override suspend fun unavailableReason(): TextToSpeechUnavailableReason = unavailableReason

    override suspend fun speak(
        text: String,
        queue: Boolean,
        volume: Int,
    ) = Unit

    override suspend fun stop() = Unit
}

class FakeHapticFeedbackAvailabilityRepository : HapticFeedbackAvailabilityRepository {
    var available = true

    override fun isHapticFeedbackAvailable(): Boolean = available
}

class FakeAccessLocalNetworkPermissionRepository : AccessLocalNetworkPermissionRepository {
    var granted = true

    override fun isGranted(): Boolean = granted
}
