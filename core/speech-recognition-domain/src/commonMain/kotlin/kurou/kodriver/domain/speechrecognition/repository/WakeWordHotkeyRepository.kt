package kurou.kodriver.domain.speechrecognition.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.HotkeyRegistrationState

/** 保存された割り当てを OS に登録する。登録は購読中だけ行う。 */
public interface WakeWordHotkeyRepository {
    public fun pressStream(): Flow<Unit>

    public fun registrationStateStream(): Flow<HotkeyRegistrationState>

    public suspend fun retryRegistration()
}
