package kurou.kodriver.feature.manualreadoutlist

/**
 * デスクトップ（JVM）版で実行されているかどうかを返す。Android版では false を返す。
 */
internal expect fun isDesktopPlatform(): Boolean
