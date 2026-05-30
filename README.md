# KoDriver

Le Mans Ultimate（LMU）のテレメトリデータをリアルタイムで表示する Compose Multiplatform デスクトップアプリ。

> **注意**: テレメトリ読み取りは Windows 専用です。共有メモリ（`LMU_Data`）へのアクセスに JNA / `OpenFileMappingA` を使用しています。

## 機能

- 走行中のデータをリアルタイム表示
- Windows TTS でアナウンス
- シミュレーターが未起動の場合は自動リトライ接続（1秒間隔）

## プロジェクト構成

```
KoDriver/
├── core/           ドメイン層 + JVM データ層（KMP: JVM / JS / wasmJS / Android）
├── app/
│   ├── shared/     Compose Multiplatform 共通 UI
│   ├── desktopApp/ デスクトップアプリ本体
│   ├── androidApp/ Android アプリ
│   └── webApp/     Web アプリ
└── server/         Ktor サーバー
```

## テレメトリデータモデル

`TelemetryData` は以下のサブモデルで構成されています。

| モデル | 主なフィールド |
|---|---|
| `EngineData` | rpm, maxRpm, gear |
| `InputsData` | throttle, brake, clutch, steering |
| `TyreData` | 各輪の温度(K), ブレーキ温度(°C), 空気圧(kPa), 摩耗 |
| `FuelData` | 現在量(L), 容量(L) |
| `TimingData` | ラップ番号（ラップタイムは未実装） |
| `VehicleData` | 位置, ローカル速度 → `speedKmh` プロパティ |

## 実行

```bash
# 標準起動
./gradlew :app:desktopApp:run

# ホットリロード
./gradlew :app:desktopApp:hotRun --auto
```

## ビルド（Windows MSI）

```bash
./gradlew :app:desktopApp:packageMsi
```

GitHub Actions の `build-windows.yml` ワークフロー（`workflow_dispatch`）を使用して Windows 環境でビルドすることもできます。成果物は 30 日間 Artifact として保存されます。

## テスト

```bash
./gradlew :app:shared:jvmTest
./gradlew :server:test
```

## 技術スタック

- **Kotlin** 2.3.21 / Kotlin Multiplatform
- **Compose Multiplatform** 1.11.0
- **JNA** 5.17.0（Windows 共有メモリアクセス）
- **kotlinx-coroutines** 1.11.0
- **Ktor** 3.4.3

---

詳細なアーキテクチャ・開発ガイドは [CLAUDE.md](./CLAUDE.md) を参照してください。
