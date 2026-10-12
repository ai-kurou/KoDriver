package kurou.kodriver.feature.otherlist

internal fun Long.toRgbHexLabel(): String = "#" + (this and 0xFFFFFFL).toString(16).padStart(6, '0').uppercase()
