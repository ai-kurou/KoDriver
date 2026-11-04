package kurou.kodriver.domain.repository

/**
 * マイクの録音権限（RECORD_AUDIO）が許可されているかを取得するRepository。
 * 権限が存在しないプラットフォームでは常に true（制限なし）を返す。
 */
interface RecordAudioPermissionRepository {
    fun isGranted(): Boolean
}
