package kurou.kodriver.feature.speechrecognitionlist

import kurou.kodriver.domain.model.SpeechCommandGuideGroup

/** 音声タブの一覧の表示状態。選択中のシミュレーターの案内を、カテゴリごとにまとめて持つ。 */
internal data class SpeechRecognitionListUiState(
    val groups: List<SpeechCommandGuideGroup> = emptyList(),
)
