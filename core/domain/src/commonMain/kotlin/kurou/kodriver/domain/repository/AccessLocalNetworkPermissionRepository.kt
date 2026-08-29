package kurou.kodriver.domain.repository

/**
 * ACCESS_LOCAL_NETWORK 権限（Android 16 以降）が許可されているかを取得するRepository。
 * この権限が存在しないプラットフォーム・OSバージョンでは常に true（制限なし）を返す。
 */
interface AccessLocalNetworkPermissionRepository {
    fun isGranted(): Boolean
}
