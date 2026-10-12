package kurou.kodriver.domain.model

/** LMU の安定識別子だけを復元する読み上げ設定シリアライザー。 */
object LmuWindowsReadoutItemKeyMapSerializer : ReadoutItemKeyMapSerializer({ LmuWindowsReadoutItemKey.entries })
