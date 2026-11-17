package kurou.kodriver.feature.otherlist

/**
 * currentAppVersion のプラットフォーム別実装を要求する expect 宣言。
 */
public expect fun currentAppVersion(): String

/**
 * currentAppVersionLabel のプラットフォーム別実装を要求する expect 宣言。
 */
internal expect fun currentAppVersionLabel(): String
