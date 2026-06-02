# KoDriver

[![On Main Merge](https://github.com/ai-kurou/KoDriver/actions/workflows/on-main-merge.yml/badge.svg?branch=main)](https://github.com/ai-kurou/KoDriver/actions/workflows/on-main-merge.yml)
[![codecov](https://codecov.io/gh/ai-kurou/KoDriver/graph/badge.svg?token=DSR32EAS87)](https://codecov.io/gh/ai-kurou/KoDriver)
[![Maintainability](https://qlty.sh/gh/ai-kurou/projects/KoDriver/maintainability.svg)](https://qlty.sh/gh/ai-kurou/projects/KoDriver)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/a22a1b1a759e48b1a4551f277d34ea6d)](https://app.codacy.com/gh/ai-kurou/KoDriver/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ai-kurou_KoDriver&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ai-kurou_KoDriver)
![CodeRabbit Pull Request Reviews](https://img.shields.io/coderabbit/prs/github/ai-kurou/KoDriver?utm_source=oss&utm_medium=github&utm_campaign=ai-kurou%2FKoDriver&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)
[![License](https://img.shields.io/github/license/ai-kurou/KoDriver)](LICENSE)
[![GitHub Release](https://img.shields.io/github/v/release/ai-kurou/KoDriver)](https://github.com/ai-kurou/KoDriver/releases)
![Android](https://img.shields.io/badge/Android-API%2028%2B-green?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin)

Le Mans Ultimate（LMU）のテレメトリデータをリアルタイムで表示する Compose Multiplatform デスクトップアプリ。

> **注意**: テレメトリ読み取りは Windows 専用です。共有メモリ（`LMU_Data`）へのアクセスに JNA / `OpenFileMappingA` を使用しています。

## 機能

- 走行中のデータをリアルタイム表示
- シミュレーターが未起動の場合は自動リトライ接続（1秒間隔）
- Windows TTS でアナウンス（未実装）

## プロジェクト構成

```
KoDriver/
├── core/
│   ├── domain/     ドメインモデル・リポジトリ抽象（KMP: JVM / JS / wasmJS / Android）
│   └── data/       JVM データ層・JNA 共有メモリ実装（JVM 専用）
├── app/
│   ├── shared/     Compose Multiplatform 共通 UI
│   ├── desktopApp/ デスクトップアプリ本体
│   ├── androidApp/ Android アプリ（基本実装済み）
│   └── webApp/     Web アプリ（未実装）
└── server/         Ktor サーバー（未実装）
```

## テレメトリデータモデル

`LmuTelemetryData` は以下のサブモデルで構成されています。

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
./gradlew :core:data:test
./gradlew :app:shared:jvmTest
./gradlew :server:test
```

## 技術スタック

- **Kotlin** 2.3.21 / Kotlin Multiplatform
- **Compose Multiplatform** 1.11.0
- **JNA** 5.17.0（Windows 共有メモリアクセス）
- **kotlinx-coroutines** 1.11.0
- **Ktor** 3.4.3
- **detekt** 1.23.8（静的解析）
- **Kover** 0.9.8（コードカバレッジ）

---

詳細なアーキテクチャ・開発ガイドは [CLAUDE.md](./CLAUDE.md) を参照してください。

<!-- MODULE-GRAPH-START -->
## Module Graph

![Module Graph](docs/graphs/full-graph.svg)
<!-- MODULE-GRAPH-END -->
