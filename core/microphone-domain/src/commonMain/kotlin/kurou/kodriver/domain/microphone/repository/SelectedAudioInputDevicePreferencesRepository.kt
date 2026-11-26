package kurou.kodriver.domain.microphone.repository

import kotlinx.coroutines.flow.Flow

/**
 * ユーザーが選んだ音声入力（マイク）デバイス名を保存するRepository。
 */
public interface SelectedAudioInputDevicePreferencesRepository {
    /** 保存済みのデバイス名。未保存なら `SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED`。 */
    public fun selectedDeviceName(): Flow<String>

    public suspend fun saveSelectedDeviceName(name: String)
}
