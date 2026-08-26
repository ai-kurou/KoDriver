package kurou.kodriver.domain.repository

/**
 * 端末がハプティックフィードバック（振動）用のハードウェアを備えているかを取得するRepository。
 * ユーザー設定である [HapticFeedbackEnabledRepository] とは異なり、こちらは端末のハードウェア
 * 構成を表す読み取り専用の情報を扱う。
 */
interface HapticFeedbackAvailabilityRepository {
    fun isHapticFeedbackAvailable(): Boolean
}
