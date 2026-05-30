# KoDriver — CLAUDE.md

## プロジェクト概要

Le Mans Ultimate（LMU）から Windows 共有メモリ経由でテレメトリデータを取得し、Compose Multiplatform デスクトップアプリで表示し、Windows TTS でアナウンスする。

---

## モジュール構成

```
KoDriver/
├── core/           ドメインモデル・リポジトリ抽象・JVM データ層（JNA 共有メモリ）
├── app/
│   ├── shared/     Compose Multiplatform 共通 UI
│   ├── desktopApp/ JVM デスクトップアプリ（ViewModel, Screen, TTS）
│   ├── androidApp/ Android アプリ（未実装）
│   └── webApp/     Web アプリ（未実装）
└── server/         Ktor サーバー（未実装）
```

### `core` モジュールのレイヤー構造

```
commonMain/domain/
  model/          LmuTelemetryData, EngineData, FuelData, TimingData, TyreData,
                  VehicleData, InputsData, WheelIndex
  repository/     LmuRepository（interface）
  usecase/        ObserveLmuUseCase, CheckLmuConnectionUseCase,
                  DisconnectLmuUseCase

jvmMain/data/
  datasource/     SharedMemoryReader（JNA で Windows File Mapping を open/read/close）
                  Kernel32Ext（JNA インターフェース）
  mapper/         LmuMapper（LMU バイナリ → LmuTelemetryData）
  repository/     LmuRepositoryImpl
```

---

## 重要な制約・注意事項

### 共有メモリ読み取りは Windows 専用
`SharedMemoryReader` は `OpenFileMappingA` / `MapViewOfFile` を使用するため **Windows のみ**動作する。macOS / Linux ではシミュレーターが起動しないため `open()` が `false` を返し続ける（クラッシュはしない）。

### TimingData のラップタイムは未実装
`LmuMapper` のラップタイム系フィールド（`currentLapTimeMs`, `lastLapTimeMs`, `bestLapTimeMs`, `sector1Ms`, `sector2Ms`）は `0L` で固定。Scoring セグメントの実装が必要。

### オフセット情報
`LmuMapper.kt` のコメントに pyLMUSharedMemory の ctypes レイアウト（`_pack_=4`）を記載済み。

---

## ビルド・実行コマンド

```bash
# デスクトップアプリ起動（通常）
./gradlew :app:desktopApp:run

# デスクトップアプリ起動（ホットリロード）
./gradlew :app:desktopApp:hotRun --auto

# Windows MSI パッケージビルド（CI: GitHub Actions / ローカル Windows 環境）
./gradlew :app:desktopApp:packageMsi

# テスト
./gradlew :app:shared:jvmTest
./gradlew :server:test
```

GitHub Actions ワークフロー `build-windows.yml` は `workflow_dispatch` でのみ起動し、MSI を Artifact として 30 日間保存する。

---

## 主要ライブラリバージョン（libs.versions.toml）

| ライブラリ | バージョン |
|---|---|
| Kotlin | 2.3.21 |
| Compose Multiplatform | 1.11.0 |
| JNA | 5.17.0 |
| kotlinx-coroutines | 1.11.0 |
| androidx-lifecycle | 2.11.0-beta01 |
| Ktor | 3.4.3 |

---

## Git 操作ルール

- `git commit` および `git push` は、ユーザーから明示的に指示された場合のみ実行する。
- 作業完了後に自動でコミット・プッシュしない。

---

## コーディング規約

- Compose の状態管理は `StateFlow` + `ViewModel`（`LmuViewModel` を参照）。
- `LmuRepository` は `Flow<LmuTelemetryData>` を emit する cold flow として実装する。ポーリング間隔デフォルトは 16ms（≈60fps）。
- 共有メモリのパースロジックは `internal object XxxMapper` に隔離し、ドメイン層には持ち込まない。
- `jvmMain` のコードは `commonMain` の `expect/actual` なしに直接 `LmuRepository` を実装してよい（KMP の `jvmMain` sourceSet として宣言するだけでよい）。
