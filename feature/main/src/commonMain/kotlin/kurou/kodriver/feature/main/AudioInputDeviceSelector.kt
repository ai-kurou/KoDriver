package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.model.SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED
import kurou.kodriver.domain.usecase.GetAudioInputDevicesUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedAudioInputDeviceNameUseCase
import kurou.kodriver.domain.usecase.SaveSelectedAudioInputDeviceNameUseCase

/** 入力デバイスの一覧と、その中で選択中のデバイス。 */
internal data class AudioInputDeviceList(
    /** 入力デバイス名の一覧。デバイスが無ければ空。 */
    val names: List<String>,
    /** 選択中のデバイス名。保存済みの名前が一覧にない場合や未選択の場合は先頭。一覧が空なら null。 */
    val selected: String?,
)

/**
 * 入力デバイスの一覧取得と選択の保存を扱う。デスクトップだけが提供し、Android では使わない。
 * 保存済みの名前は初回の取得時に読み込み、一覧にない場合も保存値は書き換えない（そのデバイスが接続されれば次の取得で選ばれる）。
 * 音声認識は同じ規則（保存済みの名前が一覧にあればそれ、なければ先頭）で開くデバイスを決める。
 */
internal class AudioInputDeviceSelector(
    private val getAudioInputDevices: GetAudioInputDevicesUseCase,
    private val observeSelectedAudioInputDeviceName: ObserveSelectedAudioInputDeviceNameUseCase,
    private val saveSelectedAudioInputDeviceName: SaveSelectedAudioInputDeviceNameUseCase,
) {
    /** 保存済みまたはユーザーが選んだデバイス名。未選択は null。 */
    private var selectedDeviceName: String? = null

    /** 保存済みの名前を [selectedDeviceName] へ読み込み済み、またはユーザーが先に選んだため読み込み不要になったか。 */
    private var savedSelectionResolved = false

    /** 入力デバイスの一覧を取得し直し、選択中のデバイスを返す。保存値を読めなかった場合は例外を投げる。 */
    suspend fun refresh(): AudioInputDeviceList {
        val names = getAudioInputDevices().map { it.name }
        loadSavedSelection()
        return AudioInputDeviceList(names, names.firstOrNull { it == selectedDeviceName } ?: names.firstOrNull())
    }

    /** デバイスを選んで保存する。保存に失敗しても今回の選択は覚えておき、例外を投げる。 */
    suspend fun select(name: String) {
        selectedDeviceName = name
        savedSelectionResolved = true
        saveSelectedAudioInputDeviceName(name)
    }

    /** 初回だけ、保存済みのデバイス名を読み込む。読み込み中にユーザーが選んだ場合は選択を優先する。 */
    private suspend fun loadSavedSelection() {
        if (savedSelectionResolved) return
        val saved = observeSelectedAudioInputDeviceName().first()
        if (savedSelectionResolved) return
        savedSelectionResolved = true
        selectedDeviceName = saved.takeIf { it != SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED }
    }
}
