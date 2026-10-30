package kurou.kodriver.domain.repository

/**
 * ACCESS_LOCAL_NETWORK 権限が許可されているかを取得するRepository。権限のチェックが必要なのは Android 17（API 37）以降で、それ未満は常に許可済みとして扱う。
 * この権限が存在しないプラットフォーム・OSバージョンでは常に true（制限なし）を返す。
 */
interface AccessLocalNetworkPermissionRepository {
    fun isGranted(): Boolean
}
