package kurou.kodriver.feature.otherlist

/**
 * currentAppVersion のプラットフォーム別実装を要求する expect 宣言。
 */
expect fun currentAppVersion(): String

/**
 * currentAppVersionLabel のプラットフォーム別実装を要求する expect 宣言。
 */
expect fun currentAppVersionLabel(): String
