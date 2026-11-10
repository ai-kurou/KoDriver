package kurou.kodriver.feature.speechrecognitionlist

import kurou.kodriver.domain.model.SpeechCommandGuideGroup

/**
 * 音声タブの一覧の表示状態。選択中のシミュレーターの案内を、カテゴリごとにまとめて持つ。
 * [loaded] は案内の最初の取得が済んだかを表し、取得前に「項目がない」案内を出さないために使う。
 */
internal data class SpeechRecognitionListUiState(
    val groups: List<SpeechCommandGuideGroup> = emptyList(),
    val loaded: Boolean = false,
)
