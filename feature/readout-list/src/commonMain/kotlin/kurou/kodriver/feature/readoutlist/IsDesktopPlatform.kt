package kurou.kodriver.feature.readoutlist

/**
 * デスクトップ（JVM）版で実行されているかどうかを返す。Android版では false を返す。
 */
internal expect fun isDesktopPlatform(): Boolean
