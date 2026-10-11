package kurou.kodriver.domain.engine

import kurou.kodriver.domain.model.ReadoutItemKey

/**
 * 音声エンジンへ渡す読み上げイベント。
 *
 * 各イベントは、OS標準TTSによる読み上げの種類と、読み上げ可否を判定する
 * [ReadoutItemKey] を結び付ける。キューイング可否はイベント単位ではなく
 * [readoutItemKey] のトップレベル項目で判定する。
 */
sealed interface SpeechEvent {
    /** このイベントを有効化・キュー可否判定に関連付ける読み上げ項目。 */
    val readoutItemKey: ReadoutItemKey

    /**
     * テレメトリログに記録するイベントの既定文言。
     * LMUの自由文字列イベントでは既定文言を参照し、
     * 判定時の実際の本文は [ReadoutTextEvent.resolvedText] に保持する。
     * ACEフラッグの既定文言は定数を参照し、判定時に解決した実際の本文は [ReadoutTextEvent.resolvedText] に保持する。
     * ドメイン層はCompose Resourcesに依存しないため、表示文言の変更時はここも更新する。
     */
    val narratedText: String
}

/** 判定時の本文を保持し、キュー待機中の設定変更後も発話とログを一致させる読み上げ文言イベント。 */
sealed interface ReadoutTextEvent : SpeechEvent {
    val resolvedText: String?

    fun withResolvedText(text: String): ReadoutTextEvent
}
