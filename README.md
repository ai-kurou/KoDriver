# KoDriver

[![On Main Merge](https://github.com/ai-kurou/KoDriver/actions/workflows/on-main-merge.yml/badge.svg?branch=main)](https://github.com/ai-kurou/KoDriver/actions/workflows/on-main-merge.yml)
[![codecov](https://codecov.io/gh/ai-kurou/KoDriver/graph/badge.svg?token=DSR32EAS87)](https://codecov.io/gh/ai-kurou/KoDriver)
[![Maintainability](https://qlty.sh/gh/ai-kurou/projects/KoDriver/maintainability.svg)](https://qlty.sh/gh/ai-kurou/projects/KoDriver)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/a22a1b1a759e48b1a4551f277d34ea6d)](https://app.codacy.com/gh/ai-kurou/KoDriver/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ai-kurou_KoDriver&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ai-kurou_KoDriver)
![CodeRabbit Pull Request Reviews](https://img.shields.io/coderabbit/prs/github/ai-kurou/KoDriver?utm_source=oss&utm_medium=github&utm_campaign=ai-kurou%2FKoDriver&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)
[![License](https://img.shields.io/github/license/ai-kurou/KoDriver)](LICENSE)
[![GitHub Release](https://img.shields.io/github/v/release/ai-kurou/KoDriver)](https://github.com/ai-kurou/KoDriver/releases)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin)

Le Mans Ultimate（LMU）の走行情報に応じて、WAV 音声ファイルをリアルタイムに再生する Compose Multiplatform アプリ。

## 機能

- アナウンスする項目の選択・有効/無効の切り替え
- アナウンス優先度のドラッグ&リオーダー
- WAV 音声ファイルによるリアルタイムアナウンス
- Windows 版デスクトップアプリからフラッグ情報を WebSocket 配信

## 動作要件

- Windows 10 以降
- Le Mans Ultimate（インストール済み）

## インストール

[Releases](https://github.com/ai-kurou/KoDriver/releases) から最新の MSI インストーラーをダウンロードして実行してください。

LMU が起動していない状態でアプリを起動しても問題ありません。LMU の起動を検知すると自動的に接続します。

## アーキテクチャ

Kotlin Multiplatform + Clean Architecture のマルチモジュール構成。

| モジュール | 役割 |
|---|---|
| `:app:desktopApp` | デスクトップアプリ エントリーポイント |
| `:app:androidApp` | Android アプリ エントリーポイント |
| `:app:webApp` | Web アプリ（未実装） |
| `:app:shared` | Compose Multiplatform 共通 UI |
| `:core:domain` | リポジトリ抽象・ユースケース |
| `:core:data` | 共有メモリ読み取り・DataStore（JVM / Android） |
| `:core:designsystem` | 共通 Composable コンポーネント |
| `:feature:lmu-windows-connection` | LMU 接続状態の監視 |
| `:feature:server-connection` | KoDriver サーバーへの接続状態確認 |
| `:feature:readout-list` | アナウンス設定 UI |
| `:feature:lmu-readout-vehicle-approach-detail` | 車両接近アナウンス詳細 UI |
| `:feature:lmu-readout-vehicle-damage-detail` | 車両故障アナウンス詳細 UI |
| `:feature:lmu-readout-flag-detail` | フラグアナウンス詳細 UI |
| `:feature:lmu-narrator` | WAV 音声再生エンジン |
| `:feature:other-license-detail` | その他画面のライセンス詳細表示 |
| `:feature:other-list` | その他画面の一覧表示・選択状態管理 |
| `:feature:other-volume-detail` | その他画面の音量設定詳細 |
| `:server` | デスクトップアプリ内で起動し、共有メモリ由来のデータを配信する Ktor サーバー |

## Contributing

このプロジェクトはプルリクエストを受け付けていません。
[GPL-3.0 ライセンス](LICENSE) の範囲内で自由にフォーク・改変・再配布できます。

## クレジット

このアプリは音声合成ソフトウェア `VOICEVOX` を利用しています。

- VOICEVOX:剣崎雌雄
- VOICEVOX 公式サイト: <https://voicevox.hiroshiba.jp/>
- VOICEVOX ソフトウェア利用規約: <https://voicevox.hiroshiba.jp/term/>
- 剣崎雌雄 利用規約: <https://voicevox.hiroshiba.jp/product/kenzaki_mesuo/>

## ライセンス

[GPL-3.0](LICENSE)

<!-- MODULE-GRAPH-START -->
## Module Graph

![Module Graph](docs/graphs/full-graph.svg)
<!-- MODULE-GRAPH-END -->
