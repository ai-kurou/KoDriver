# GT7 UDP テレメトリー 完全フィールドリファレンス

## パケット概要

GT7 は PS4/PS5 から **UDP** でテレメトリーを送信する。Update 1.42 以降、パケット種別が A / B / ~ の3種類に拡張された。

| パケット種別 | ハートビート文字 | サイズ | 概要 |
|---|---|---|---|
| A | `'A'` | 0x128 (296 bytes) | 基本パケット |
| B | `'B'` | 0x13C (316 bytes) | 拡張（車体揺動など追加） |
| ~ | `'~'` | 0x158 (344 bytes) | さらに拡張（フィルタ済みペダル・エネルギー回生など） |

**ポート構成:**
- 受信ポート（PS5→PC）: **33739**
- 送信ポート（PC→PS5、ハートビート用）: **33740**

**ハートビート:** 接続維持のため約10秒ごとに `'A'`/`'B'`/`'~'` のいずれか1バイトをPS5に送信する必要がある。

---

## 暗号化・復号方法

### 暗号方式: Salsa20

**共通鍵（32バイト）:**
```
"Simulator Interface Packet GT7 ver 0.0"
 ↓ 先頭32バイトのみ使用
b"Simulator Interface Packet GT7 "
```

**ノンス（IV）生成（8バイト）:**
1. 暗号化済みパケットのオフセット `0x40` から4バイトをリトルエンディアンで読み取り → `iv1`
2. `iv2 = iv1 XOR 0xDEADBEAF`（パケットAの場合）
3. 最終IV = `iv2（4バイト LE）` + `iv1（4バイト LE）`

**パケット種別ごとのXOR定数:**

| パケット種別 | XOR定数 |
|---|---|
| A | `0xDEADBEAF` |
| B | `0xDEADBEEF` |
| ~ | `0x55FABB4F` |

**Python実装例:**
```python
from salsa20 import Salsa20_xor
import struct

KEY = b"Simulator Interface Packet GT7 ver 0.0"

def decrypt(data: bytes) -> bytes:
    iv1 = struct.unpack_from('<I', data, 0x40)[0]
    iv2 = iv1 ^ 0xDEADBEAF
    iv = struct.pack('<II', iv2, iv1)
    decrypted = Salsa20_xor(data, iv, KEY[:32])
    assert struct.unpack_from('<I', decrypted, 0)[0] == 0x47375330  # "0S7G"
    return decrypted
```

**マジックナンバー:** 復号後、先頭4バイトが `0x47375330`（"G7S0" のリトルエンディアン）であることを検証する。

---

## サンプリングレート

- **送信レート:** 60Hz（60パケット/秒）

---

## パケット A のフィールド一覧（296バイト）

| オフセット | フィールド名 | 型 | サイズ | 内容・備考 |
|---|---|---|---|---|
| 0x00 | magic | int32 | 4 | `0x47375330` = GT7/GTSport識別子 |
| 0x04 | position.X | float | 4 | トラック上のX座標（メートル） |
| 0x08 | position.Y | float | 4 | トラック上のY座標（メートル） |
| 0x0C | position.Z | float | 4 | トラック上のZ座標（メートル） |
| 0x10 | velocity.X | float | 4 | X軸方向速度（m/s） |
| 0x14 | velocity.Y | float | 4 | Y軸方向速度（m/s） |
| 0x18 | velocity.Z | float | 4 | Z軸方向速度（m/s） |
| 0x1C | rotation.X（Pitch） | float | 4 | ピッチ（-1〜1） |
| 0x20 | rotation.Y（Yaw） | float | 4 | ヨー（-1〜1） |
| 0x24 | rotation.Z（Roll） | float | 4 | ロール（-1〜1） |
| 0x28 | orientationNorth | float | 4 | 北方向への向き（1.0=北、0.0=南） |
| 0x2C | angularVelocity.X | float | 4 | X軸角速度（rad/s） |
| 0x30 | angularVelocity.Y | float | 4 | Y軸角速度（rad/s） |
| 0x34 | angularVelocity.Z | float | 4 | Z軸角速度（rad/s） |
| 0x38 | bodyHeight | float | 4 | 車高（メートル、×1000でmm） |
| 0x3C | engineRPM | float | 4 | エンジン回転数（RPM） |
| 0x40 | iv | uint8[4] | 4 | Salsa20ノンス用シード（暗号化時） |
| 0x44 | gasLevel | float | 4 | 現在燃料残量（リットル）、EVは電力量 |
| 0x48 | gasCapacity | float | 4 | タンク容量（通常100L、カート5L、EV=0） |
| 0x4C | speed | float | 4 | 車速（m/s）、×3.6でkm/h |
| 0x50 | turboBoost | float | 4 | ターボ圧（-1すると実値、2.0=100kPa） |
| 0x54 | oilPressure | float | 4 | 油圧（Bar） |
| 0x58 | waterTemperature | float | 4 | 水温（°C）、常に85前後 |
| 0x5C | oilTemperature | float | 4 | 油温（°C）、常に110前後 |
| 0x60 | tireTemp_FL | float | 4 | 前左タイヤ表面温度（°C） |
| 0x64 | tireTemp_FR | float | 4 | 前右タイヤ表面温度（°C） |
| 0x68 | tireTemp_RL | float | 4 | 後左タイヤ表面温度（°C） |
| 0x6C | tireTemp_RR | float | 4 | 後右タイヤ表面温度（°C） |
| 0x70 | packetId | int32 | 4 | パケット通し番号（シーケンスID） |
| 0x74 | lapCount | int16 | 2 | 現在周回数 |
| 0x76 | lapsInRace | int16 | 2 | レース総周回数 |
| 0x78 | bestLapTime | int32 | 4 | ベストラップ（ms、未設定=-1） |
| 0x7C | lastLapTime | int32 | 4 | 前周回タイム（ms、未設定=-1） |
| 0x80 | dayProgression | int32 | 4 | ゲーム内時刻（ms） |
| 0x84 | startPosition | int16 | 2 | スターティンググリッド位置（レース開始後=-1） |
| 0x86 | numCarsAtPreRace | int16 | 2 | レース前の出場台数（開始後=-1） |
| 0x88 | minAlertRPM | int16 | 2 | 回転警告の下限RPM |
| 0x8A | maxAlertRPM | int16 | 2 | 回転警告の上限RPM（レッドライン） |
| 0x8C | calcMaxSpeed | int16 | 2 | 現在のトランスミッション設定での最高速度（km/h） |
| 0x8E | flags | int16 | 2 | ビットフラグ（SimulatorFlags参照） |
| 0x90 | gears | uint8 | 1 | 下位4ビット=現在ギア、上位4ビット=推奨ギア（15=なし） |
| 0x91 | throttle | uint8 | 1 | アクセル開度（0〜255、÷2.55で%） |
| 0x92 | brake | uint8 | 1 | ブレーキ踏力（0〜255、÷2.55で%） |
| 0x93 | (unknown) | uint8 | 1 | 未解明のパディング |
| 0x94 | roadPlane.X | float | 4 | 路面法線ベクトルX |
| 0x98 | roadPlane.Y | float | 4 | 路面法線ベクトルY |
| 0x9C | roadPlane.Z | float | 4 | 路面法線ベクトルZ |
| 0xA0 | roadPlaneDistance | float | 4 | 路面からの距離（実質車高） |
| 0xA4 | wheelRPS_FL | float | 4 | 前左タイヤ回転速度（rad/s） |
| 0xA8 | wheelRPS_FR | float | 4 | 前右タイヤ回転速度（rad/s） |
| 0xAC | wheelRPS_RL | float | 4 | 後左タイヤ回転速度（rad/s） |
| 0xB0 | wheelRPS_RR | float | 4 | 後右タイヤ回転速度（rad/s） |
| 0xB4 | tireRadius_FL | float | 4 | 前左タイヤ半径（m） |
| 0xB8 | tireRadius_FR | float | 4 | 前右タイヤ半径（m） |
| 0xBC | tireRadius_RL | float | 4 | 後左タイヤ半径（m） |
| 0xC0 | tireRadius_RR | float | 4 | 後右タイヤ半径（m） |
| 0xC4 | suspHeight_FL | float | 4 | 前左サスペンション圧縮量（m） |
| 0xC8 | suspHeight_FR | float | 4 | 前右サスペンション圧縮量（m） |
| 0xCC | suspHeight_RL | float | 4 | 後左サスペンション圧縮量（m） |
| 0xD0 | suspHeight_RR | float | 4 | 後右サスペンション圧縮量（m） |
| 0xD4〜0xF0 | (reserved) | float[8] | 32 | 未使用（ゲームがセットしない） |
| 0xF4 | clutchPedal | float | 4 | クラッチペダル位置（0.0〜1.0） |
| 0xF8 | clutchEngagement | float | 4 | クラッチ係合度（0.0〜1.0） |
| 0xFC | rpmFromClutchToGearbox | float | 4 | クラッチ〜ギアボックス間のRPM |
| 0x100 | transmissionTopSpeed | float | 4 | トランスミッション設定の最高速度 |
| 0x104 | gearRatio[0] | float | 4 | 1速ギア比 |
| 0x108 | gearRatio[1] | float | 4 | 2速ギア比 |
| 0x10C | gearRatio[2] | float | 4 | 3速ギア比 |
| 0x110 | gearRatio[3] | float | 4 | 4速ギア比 |
| 0x114 | gearRatio[4] | float | 4 | 5速ギア比 |
| 0x118 | gearRatio[5] | float | 4 | 6速ギア比 |
| 0x11C | gearRatio[6] | float | 4 | 7速ギア比 |
| 0x120 | gearRatio[7] | float | 4 | 8速ギア比（未使用またはオーバーフロー） |
| 0x124 | carCode | int32 | 4 | 車両ID |

---

## パケット B の追加フィールド（+20バイト、合計316バイト）

| オフセット | フィールド名 | 型 | サイズ | 内容 |
|---|---|---|---|---|
| 0x128 | wheelRotation | float | 4 | ステアリングホイール回転角（ラジアン） |
| 0x12C | fillerFloatFB | float | 4 | 未解明（前後揺動関連か） |
| 0x130 | sway | float | 4 | 横揺れ加速度（m/s²） |
| 0x134 | heave | float | 4 | 上下揺れ加速度（m/s²） |
| 0x138 | surge | float | 4 | 前後揺れ加速度（m/s²） |

---

## パケット ~ の追加フィールド（+28バイト、合計344バイト）

| オフセット | フィールド名 | 型 | サイズ | 内容 |
|---|---|---|---|---|
| 0x13C | throttleFiltered | uint8 | 1 | フィルタ済みアクセル開度 |
| 0x13D | brakeFiltered | uint8 | 1 | フィルタ済みブレーキ踏力 |
| 0x13E | (unknown / carType?) | uint8 | 1 | 未解明 |
| 0x13F | noGasConsumption | uint8 | 1 | 燃料消費なしフラグ？ |
| 0x140〜0x14C | torqueVectors | float[4] | 16 | 各輪トルクベクトル（FL/FR/RL/RR） |
| 0x150 | energyRecovery | float | 4 | エネルギー回生量 |
| 0x154 | (unknown) | float | 4 | 未解明 |

---

## SimulatorFlags ビットフィールド（0x8E〜0x8F）

| ビット | フラグ名 | 内容 |
|---|---|---|
| bit 0 | CarOnTrack | 車がコース上にある（データ有効） |
| bit 1 | Paused | ゲームが一時停止中 |
| bit 2 | LoadingOrProcessing | ロード中 |
| bit 3 | InGear | ギアが入っている |
| bit 4 | HasTurbo | ターボ搭載車 |
| bit 5 | RevLimiterBlinkAlertActive | レブリミッター警告点滅中 |
| bit 6 | HandBrakeActive | ハンドブレーキ作動中 |
| bit 7 | LightsActive | ライト点灯中 |
| bit 8 | HighBeamActive | ハイビーム点灯中 |
| bit 9 | LowBeamActive | ロービーム点灯中 |
| bit 10 | ASMActive | ASM（安定制御）作動中 |
| bit 11 | TCSActive | トラクションコントロール作動中 |

---

## 既知のバグ・注意事項

- `gearRatio[7]`（0x120）はゲームが memcpy 時の境界チェックを行わないため、10速以上の車（LC500等）では `carCode`（0x124）が上書きされる既知のバグがある
- `rotation`（0x1C〜0x24）は Nenkai/PDTools では Quaternion として解釈しており、コミュニティによって解釈が分かれている

---

## 参考リポジトリ

| リポジトリ | 言語 | 概要 |
|---|---|---|
| [Nenkai/PDTools](https://github.com/Nenkai/PDTools) | C# (.NET) | 最も権威ある実装。フィールド定義・復号ロジック完全実装 |
| [MacManley/gt7-udp](https://github.com/MacManley/gt7-udp) | C++ | ESP32/ESP8266向け。全パケット型（A/B/~）対応 |
| [Bornhall/gt7telemetry](https://github.com/Bornhall/gt7telemetry) | Python | シンプルなPython実装・Salsa20復号付き |
| [GeekyDeaks/raw-sim-telemetry](https://github.com/GeekyDeaks/raw-sim-telemetry) | Python | ロガー実装、復号ロジックが明快 |
| [snipem/gt7dashboard](https://github.com/snipem/gt7dashboard) | Python | ダッシュボードUI付きの実装 |
| [granturismo (PyPI)](https://pypi.org/project/granturismo/) | Python | パッケージ化されたライブラリ |
| [carlos-menezes/gran-turismo-query](https://github.com/carlos-menezes/gran-turismo-query) | TypeScript | Node.js向け実装 |
| [vwhitteron/gt-telemetry](https://pkg.go.dev/github.com/vwhitteron/gt-telemetry) | Go | Go言語実装 |
