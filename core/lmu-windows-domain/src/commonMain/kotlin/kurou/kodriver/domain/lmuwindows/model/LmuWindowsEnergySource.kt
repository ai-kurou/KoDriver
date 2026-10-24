package kurou.kodriver.domain.lmuwindows.model

/** LMU 車両クラスのエネルギー源。 */
enum class LmuWindowsEnergySource {
    /** 共有メモリの mVirtualEnergy を使うクラス。 */
    VirtualEnergy,

    /** 燃料で走るクラス。 */
    Fuel,
}
