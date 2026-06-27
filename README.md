# KoDriver

[![On Main Merge](https://github.com/ai-kurou/KoDriver/actions/workflows/on-main-merge.yml/badge.svg?branch=main)](https://github.com/ai-kurou/KoDriver/actions/workflows/on-main-merge.yml)
[![codecov](https://codecov.io/gh/ai-kurou/KoDriver/graph/badge.svg?token=DSR32EAS87)](https://codecov.io/gh/ai-kurou/KoDriver)
[![Maintainability](https://qlty.sh/gh/ai-kurou/projects/KoDriver/maintainability.svg)](https://qlty.sh/gh/ai-kurou/projects/KoDriver)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/a22a1b1a759e48b1a4551f277d34ea6d)](https://app.codacy.com/gh/ai-kurou/KoDriver/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ai-kurou_KoDriver&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ai-kurou_KoDriver)
![CodeRabbit Pull Request Reviews](https://img.shields.io/coderabbit/prs/github/ai-kurou/KoDriver?utm_source=oss&utm_medium=github&utm_campaign=ai-kurou%2FKoDriver&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)
[![License](https://img.shields.io/github/license/ai-kurou/KoDriver)](LICENSE)
[![GitHub Release](https://img.shields.io/github/v/release/ai-kurou/KoDriver)](https://github.com/ai-kurou/KoDriver/releases)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-7F52FF?logo=kotlin)

Le Mans Ultimate（LMU）および GranTurismo 7（GT7 PS5）の走行情報に応じて、WAV 音声ファイルをリアルタイムに再生する Compose Multiplatform アプリ。

https://github.com/user-attachments/assets/87a7efa0-51ff-41f2-8bc2-a045154fa264

<img width="1445" height="715" alt="スクリーンショット 2026-06-21 16 47 49" src="https://github.com/user-attachments/assets/20278c9f-eeda-424b-a693-17163d7ef0ff" />

## 機能

- アナウンスする項目の選択・有効/無効の切り替え
- アナウンス優先度のドラッグ＆ドロップによる並び替え
- WAV 音声ファイルによるリアルタイムアナウンス
- LMU 由来の走行情報を WebSocket で配信（Android アプリで受信・表示可能）
- GT7 PS5 のテレメトリを UDP で直接受信（Android / Windows 対応、SimHub 経由も可）
- クラッシュが発生した場合、改善のためにクラッシュレポートを Sentry に自動送信

## 動作要件

**デスクトップアプリ（Windows）**

- Windows 10 以降
- Le Mans Ultimate（LMU 使用時）または GranTurismo 7 PS5（GT7 使用時）

**Android アプリ**

- Android 9.0 以降
- LMU 情報を受信する場合: デスクトップアプリと同一 LAN への接続
- GT7 PS5 テレメトリを受信する場合: PS5 または SimHub を実行している PC と同一 LAN への接続

## インストール

**デスクトップアプリ（Windows）**

[Releases](https://github.com/ai-kurou/KoDriver/releases) から最新の MSI インストーラーをダウンロードして実行してください。

Windows 版 KoDriver をあらかじめ起動しておいてください。LMU が起動すると KoDriver が自動的に接続します。

インストール時に Windows SmartScreen の警告が表示される場合があります。詳しい手順、ファイアウォール設定、既知の制限は [Windows 版のインストール手順](docs/windows-install.md) を参照してください。

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

## アーキテクチャ

Kotlin Multiplatform + Clean Architecture のマルチモジュール構成。モジュール構成とモジュールグラフの詳細は [アーキテクチャドキュメント](docs/architecture.md) を参照してください。
