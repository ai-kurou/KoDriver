# GT7 UDP テレメトリー（SimulatorInterface）フィールドリファレンス

Gran Turismo 7 は PS4/PS5 から **UDP** でテレメトリーを送信する（Polyphony Digital の SimulatorInterface）。本ドキュメントは [Nenkai/PDTools](https://github.com/Nenkai/PDTools) の `SimulatorPacket.cs` / `SimulatorInterfaceClient.cs` と [MacManley/gt7-udp](https://github.com/MacManley/gt7-udp) の `GT7UDPParser.h` を基に照合済み。

## パケット概要

パケット種別はハートビートとして送る 1 文字で選択する。Update 1.42 で B / ~ が、Update 1.68 前後で C が追加された。

| パケット種別 | ハートビート文字 | サイズ | 概要 |
|---|---|---|---|
| A | `'A'` | 0x128 (296 bytes) | 基本パケット |
| B | `'B'` | 0x13C (316 bytes) | 拡張（ステアリング・車体揺動を追加。Sport モードでは利用不可） |
| ~ | `'~'` | 0x158 (344 bytes) | さらに拡張（フィルタ済みペダル・トルクベクトル・エネルギー回生など。リプレイでは利用不可） |
| C | `'C'` | 0x170 (368 bytes) | さらに拡張（路面種別・現在ラップタイム・前輪舵角・ホイールベース・車両カテゴリ。Update 1.68 前後で追加） |

**ポート構成:**
- ハートビート送信先（PC→PS5）: **33739**（PS5 側の待ち受けポート）
- テレメトリ受信（PS5→PC）: **33740**（PC 側でバインドするポート。PS5 はハートビートの送信元 IP へ送り返す）

参考: GT6 / GT Sport は 33339（送信先）/ 33340（受信）を使う。

**ハートビート:** 希望するパケット種別の 1 バイト（`'A'` / `'B'` / `'~'` / `'C'`）を PS5 の 33739 番へ定期的に送信する。一定時間送らないとテレメトリ送信が止まるため、10 秒以内の間隔で送り続ける必要がある（PDTools は 10 秒ごと、gt7-udp は約 1,000 パケットごと、KoDriver は 100 パケットごとに送信）。

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
2. `iv2 = iv1 XOR <パケット種別ごとの定数>`
3. 最終IV = `iv2（4バイト LE）` + `iv1（4バイト LE）`

**パケット種別ごとのXOR定数:**

| パケット種別 | XOR定数 |
|---|---|
| A | `0xDEADBEAF` |
| B | `0xDEADBEEF` |
| ~ | `0x55FABB4F` |
| C | `0xDEADBEEF` |

**Python実装例:**
```python
from salsa20 import Salsa20_xor
import struct

KEY = b"Simulator Interface Packet GT7 ver 0.0"

def decrypt(data: bytes) -> bytes:
    iv1 = struct.unpack_from('<I', data, 0x40)[0]
    iv2 = iv1 ^ 0xDEADBEAF  # パケットAの場合
    iv = struct.pack('<II', iv2, iv1)
    decrypted = Salsa20_xor(data, iv, KEY[:32])
    assert struct.unpack_from('<I', decrypted, 0)[0] == 0x47375330
    return decrypted
```

**マジックナンバー:** 復号後、先頭4バイトが `0x47375330`（リトルエンディアン。ワイヤ上のバイト列は ASCII で `"0S7G"`）であることを検証する。GT Sport / GT7 共通の値。GT6（PS3）は `0x30533647`（`"G6S0"`）でビッグエンディアンのパケットを送る。

---

## サンプリングレート

- **送信レート:** 60Hz（60パケット/秒。ゲームの物理ティックごとに1パケット）

---

## パケット A のフィールド一覧（296バイト）

| オフセット | フィールド名 | 型 | サイズ | 内容・備考 |
|---|---|---|---|---|
| 0x00 | magic | int32 | 4 | `0x47375330` = GT Sport / GT7 識別子 |
| 0x04 | position.X | float | 4 | トラック上のX座標（メートル） |
| 0x08 | position.Y | float | 4 | トラック上のY座標（メートル） |
| 0x0C | position.Z | float | 4 | トラック上のZ座標（メートル） |
| 0x10 | velocity.X | float | 4 | X軸方向速度（m/s） |
| 0x14 | velocity.Y | float | 4 | Y軸方向速度（m/s） |
| 0x18 | velocity.Z | float | 4 | Z軸方向速度（m/s） |
| 0x1C | rotation.X | float | 4 | 姿勢（下記「rotation の解釈」参照） |
| 0x20 | rotation.Y | float | 4 | 〃 |
| 0x24 | rotation.Z | float | 4 | 〃 |
| 0x28 | rotation.W / orientationNorth | float | 4 | 〃（PDTools はクォータニオンの W、別解釈では北方向 1.0=北/0.0=南） |
| 0x2C | angularVelocity.X | float | 4 | X軸角速度（rad/s、-1〜1） |
| 0x30 | angularVelocity.Y | float | 4 | Y軸角速度（rad/s、-1〜1） |
| 0x34 | angularVelocity.Z | float | 4 | Z軸角速度（rad/s、-1〜1） |
| 0x38 | bodyHeight | float | 4 | 車高（メートル） |
| 0x3C | engineRPM | float | 4 | エンジン回転数（RPM） |
| 0x40 | iv | uint8[4] | 4 | Salsa20ノンス用シード（復号後もこの位置に残る） |
| 0x44 | gasLevel | float | 4 | 現在燃料残量（リットル）、EVでは回生ブレーキで0から変動しうる |
| 0x48 | gasCapacity | float | 4 | タンク容量（通常100、カート5、EV=0） |
| 0x4C | speed | float | 4 | 車速（m/s）、×3.6でkm/h |
| 0x50 | turboBoost | float | 4 | ターボ圧（+1オフセット。1.0=0、2.0=1×100kPa。実値は-1する） |
| 0x54 | oilPressure | float | 4 | 油圧（Bar） |
| 0x58 | waterTemperature | float | 4 | 水温（°C）、ゲームは常に85を送る |
| 0x5C | oilTemperature | float | 4 | 油温（°C）、ゲームは常に110を送る |
| 0x60 | tireTemp_FL | float | 4 | 前左タイヤ表面温度（°C） |
| 0x64 | tireTemp_FR | float | 4 | 前右タイヤ表面温度（°C） |
| 0x68 | tireTemp_RL | float | 4 | 後左タイヤ表面温度（°C） |
| 0x6C | tireTemp_RR | float | 4 | 後右タイヤ表面温度（°C） |
| 0x70 | packetId | int32 | 4 | パケット通し番号（シーケンスID） |
| 0x74 | lapCount | int16 | 2 | 現在周回数 |
| 0x76 | lapsInRace | int16 | 2 | レース総周回数（0=フリー走行・タイムトライアル等） |
| 0x78 | bestLapTime | int32 | 4 | ベストラップ（ms、未設定=-1） |
| 0x7C | lastLapTime | int32 | 4 | 前周回タイム（ms、未設定=-1） |
| 0x80 | dayProgression | int32 | 4 | ゲーム内時刻（ms） |
| 0x84 | preRaceStartPosition | int16 | 2 | スターティンググリッド位置または予選順位（レース開始後=-1） |
| 0x86 | numCarsAtPreRace | int16 | 2 | レース前の出場台数（開始後=-1） |
| 0x88 | minAlertRPM | int16 | 2 | 回転警告の下限RPM |
| 0x8A | maxAlertRPM | int16 | 2 | 回転警告の上限RPM（レッドライン） |
| 0x8C | calcMaxSpeed | int16 | 2 | 現在のトランスミッション設定での最高速度（km/h） |
| 0x8E | flags | int16 | 2 | ビットフラグ（SimulatorFlags参照） |
| 0x90 | gears | uint8 | 1 | 下位4ビット=現在ギア、上位4ビット=推奨ギア（15=推奨なし） |
| 0x91 | throttle | uint8 | 1 | アクセル開度（0〜255、÷2.55で%） |
| 0x92 | brake | uint8 | 1 | ブレーキ踏力（0〜255、÷2.55で%） |
| 0x93 | (padding) | uint8 | 1 | パディング（常に0） |
| 0x94 | roadPlane.X | float | 4 | 路面法線ベクトルX |
| 0x98 | roadPlane.Y | float | 4 | 路面法線ベクトルY |
| 0x9C | roadPlane.Z | float | 4 | 路面法線ベクトルZ |
| 0xA0 | roadPlaneDistance | float | 4 | 路面平面からの距離（窪みで負、丘で正） |
| 0xA4 | wheelRPS_FL | float | 4 | 前左タイヤ回転速度（rad/s） |
| 0xA8 | wheelRPS_FR | float | 4 | 前右タイヤ回転速度（rad/s） |
| 0xAC | wheelRPS_RL | float | 4 | 後左タイヤ回転速度（rad/s） |
| 0xB0 | wheelRPS_RR | float | 4 | 後右タイヤ回転速度（rad/s） |
| 0xB4 | tireRadius_FL | float | 4 | 前左タイヤ半径（m） |
| 0xB8 | tireRadius_FR | float | 4 | 前右タイヤ半径（m） |
| 0xBC | tireRadius_RL | float | 4 | 後左タイヤ半径（m） |
| 0xC0 | tireRadius_RR | float | 4 | 後右タイヤ半径（m） |
| 0xC4 | suspHeight_FL | float | 4 | 前左サスペンション高さ |
| 0xC8 | suspHeight_FR | float | 4 | 前右サスペンション高さ |
| 0xCC | suspHeight_RL | float | 4 | 後左サスペンション高さ |
| 0xD0 | suspHeight_RR | float | 4 | 後右サスペンション高さ |
| 0xD4〜0xF0 | (reserved) | float[8] | 32 | 未使用（ゲームがセットしない） |
| 0xF4 | clutchPedal | float | 4 | クラッチペダル位置（0.0〜1.0） |
| 0xF8 | clutchEngagement | float | 4 | クラッチ係合度（0.0〜1.0） |
| 0xFC | rpmFromClutchToGearbox | float | 4 | クラッチ〜ギアボックス間のRPM（ギアが入りクラッチ非踏時はエンジンRPMとほぼ同じ、クラッチ踏込時は0） |
| 0x100 | transmissionTopSpeed | float | 4 | トランスミッション設定の最高速度（ギア比値として） |
| 0x104 | gearRatio[0] | float | 4 | 1速ギア比 |
| 0x108 | gearRatio[1] | float | 4 | 2速ギア比 |
| 0x10C | gearRatio[2] | float | 4 | 3速ギア比 |
| 0x110 | gearRatio[3] | float | 4 | 4速ギア比 |
| 0x114 | gearRatio[4] | float | 4 | 5速ギア比 |
| 0x118 | gearRatio[5] | float | 4 | 6速ギア比 |
| 0x11C | gearRatio[6] | float | 4 | 7速ギア比 |
| 0x120 | gearRatio[7] | float | 4 | 8速ギア比（通常はゲームがセットしない。下記の既知バグ参照） |
| 0x124 | carCode | int32 | 4 | 車両ID |

### rotation の解釈（0x1C〜0x28）

コミュニティで解釈が分かれている。

- **PDTools（Nenkai）**: 0x1C〜0x28 の 4 float を**クォータニオン**（x, y, z, w）として読む
- **その他の実装（gt7-udp, gt7dashboard 等）**: 0x1C〜0x24 を Pitch/Yaw/Roll（-1〜1）、0x28 を北方向への向き（1.0=北、0.0=南）として読む

---

## パケット B の追加フィールド（+20バイト、合計316バイト）

GT7 Update 1.42 で追加。ハートビート `'B'` / `'~'` / `'C'` で取得できる。

| オフセット | フィールド名 | 型 | サイズ | 内容 |
|---|---|---|---|---|
| 0x128 | wheelRotation | float | 4 | ステアリングホイール回転角（ラジアン） |
| 0x12C | steeringAngularVelocity | float | 4 | ステアリングホイール角速度（rad/s。PDTools では `FillerFloatFB` として未解明扱い） |
| 0x130 | sway | float | 4 | 横揺れ加速度（X軸、m/s²） |
| 0x134 | heave | float | 4 | 上下揺れ加速度（Y軸、m/s²） |
| 0x138 | surge | float | 4 | 前後揺れ加速度（Z軸、m/s²） |

---

## パケット ~ の追加フィールド（+28バイト、合計344バイト）

GT7 Update 1.42 で追加。ハートビート `'~'` / `'C'` で取得できる。

| オフセット | フィールド名 | 型 | サイズ | 内容 |
|---|---|---|---|---|
| 0x13C | throttleFiltered | uint8 | 1 | フィルタ済みアクセル開度（0〜255） |
| 0x13D | brakeFiltered | uint8 | 1 | フィルタ済みブレーキ踏力（0〜255） |
| 0x13E | (unknown) | uint8 | 1 | 未解明（PDTools は carType? と推測、4=EV） |
| 0x13F | noGasConsumption | uint8 | 1 | 燃料消費なしフラグ（PDTools の推測） |
| 0x140〜0x14C | torqueVectors | float[4] | 16 | 各輪トルクベクトル（FL/FR/RL/RR。正=駆動力、負=制動・回生） |
| 0x150 | energyRecovery | float | 4 | バッテリーへのエネルギー回生量 |
| 0x154 | (unknown) | float | 4 | 未解明 |

---

## パケット C の追加フィールド（+24バイト、合計368バイト）

GT7 Update 1.68 前後で追加。ハートビート `'C'` で取得できる。XOR 定数は `0xDEADBEEF`。

| オフセット | フィールド名 | 型 | サイズ | 内容 |
|---|---|---|---|---|
| 0x158 | surfaceType[4] | char[4] | 4 | 各輪の接地路面種別（FL/FR/RL/RR。`T`=舗装路, `C`=縁石, `D`=土・芝） |
| 0x15C | currentLapTime | int32 | 4 | 現在周回の経過タイム（ms） |
| 0x160 | wheelSteeringAngle[0] | float | 4 | 前左輪の舵角（ラジアン） |
| 0x164 | wheelSteeringAngle[1] | float | 4 | 前右輪の舵角（ラジアン） |
| 0x168 | wheelBase | float | 4 | ホイールベース（前後車軸間距離、m） |
| 0x16C | carCategory[4] | char[4] | 4 | 車両カテゴリ（NULL終端文字列。GR3, GRX 等） |

---

## SimulatorFlags ビットフィールド（0x8E〜0x8F）

| ビット | フラグ名 | 内容 |
|---|---|---|
| bit 0 | CarOnTrack | 車がコース上またはパドックにいる（データ有効） |
| bit 1 | Paused | シミュレーションが一時停止中（オンラインモードのポーズメニューでは停止しない） |
| bit 2 | LoadingOrProcessing | トラック・車両のロード中 |
| bit 3 | InGear | ギアが入っている（要調査とされている） |
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

- ゲームはギア比を境界チェックなしで memcpy するため、**9速以上**の車ではギア比が `gearRatio[7]`（0x120）以降にあふれる。10速の LC500 では `carCode`（0x124）まで上書きされる既知のバグがある
- `bestLapTime` / `lastLapTime` は未設定時に **-1**。有効判定してから使うこと
- `waterTemperature` / `oilTemperature` は現状ゲームが固定値（85 / 110）しか送らない
- パワートレイン種別は `gasCapacity` から推定できる（100前後=ICE、5=カート、0=EV）

---

## KoDriver での実装状況

`core:gt7-ps5-data` の `Gt7Ps5UdpSource` / `Gt7Ps5Mapper` の実装:

- ハートビートは **`'C'`** を使用し、PS5 の 33739 番へ送信、PC 側は 33740 番で受信する
- ハートビートは 100 パケット受信ごと、およびソケットタイムアウト（3秒）時に再送する
- 復号は Salsa20、XOR 定数 `0xDEADBEEF`、復号後にマジック `0x47375330` を検証する
- パケットサイズは 0x170（368バイト、C パケット）以上を期待する

`Gt7Ps5Mapper` が読み取っているフィールド:

| 機能 | フィールド | オフセット |
|---|---|---|
| 燃料残量 | gasLevel | 0x44 |
| 燃料タンク容量 | gasCapacity | 0x48 |
| 現在周回数 | lapCount | 0x74 |
| レース総周回数 | lapsInRace | 0x76 |
| ベストラップタイム | bestLapTime | 0x78 |

---

## 参考リポジトリ

| リポジトリ | 言語 | 概要 |
|---|---|---|
| [Nenkai/PDTools](https://github.com/Nenkai/PDTools) | C# (.NET) | 最も権威ある実装。フィールド定義・復号ロジック完全実装（GT6/GT Sport/GT7 対応） |
| [MacManley/gt7-udp](https://github.com/MacManley/gt7-udp) | C++ | ESP32/ESP8266向け。全パケット型（A/B/~/C）対応。C パケットのフィールド定義が明確。[README](https://github.com/MacManley/gt7-udp/blob/main/README.md) に `surfaceType` の ID 一覧と `carCode` → 車両名の対応表がある |
| [Bornhall/gt7telemetry](https://github.com/Bornhall/gt7telemetry) | Python | シンプルなPython実装・Salsa20復号付き |
| [GeekyDeaks/raw-sim-telemetry](https://github.com/GeekyDeaks/raw-sim-telemetry) | Python | ロガー実装、復号ロジックが明快 |
| [snipem/gt7dashboard](https://github.com/snipem/gt7dashboard) | Python | ダッシュボードUI付きの実装 |
| [granturismo (PyPI)](https://pypi.org/project/granturismo/) | Python | パッケージ化されたライブラリ |
| [carlos-menezes/gran-turismo-query](https://github.com/carlos-menezes/gran-turismo-query) | TypeScript | Node.js向け実装 |
| [zetetos/gt-telemetry](https://github.com/zetetos/gt-telemetry) | Go | Go言語実装。パケット全体を Kaitai Struct で宣言的に定義（[`internal/kaitai/gran_turismo_telemetry.ksy`](https://github.com/zetetos/gt-telemetry/blob/main/internal/kaitai/gran_turismo_telemetry.ksy)）。旧 `vwhitteron/gt-telemetry` からの移転先で、旧リポジトリは2025年5月に更新終了 |
