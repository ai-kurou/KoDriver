# KoDriver

[日本語](README.md)

[![On Main Merge](https://github.com/ai-kurou/KoDriver/actions/workflows/on-main-merge.yml/badge.svg?branch=main)](https://github.com/ai-kurou/KoDriver/actions/workflows/on-main-merge.yml)
[![codecov](https://codecov.io/gh/ai-kurou/KoDriver/graph/badge.svg?token=DSR32EAS87)](https://codecov.io/gh/ai-kurou/KoDriver)
[![Maintainability](https://qlty.sh/gh/ai-kurou/projects/KoDriver/maintainability.svg)](https://qlty.sh/gh/ai-kurou/projects/KoDriver)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/a22a1b1a759e48b1a4551f277d34ea6d)](https://app.codacy.com/gh/ai-kurou/KoDriver/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ai-kurou_KoDriver&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ai-kurou_KoDriver)
![CodeRabbit Pull Request Reviews](https://img.shields.io/coderabbit/prs/github/ai-kurou/KoDriver?utm_source=oss&utm_medium=github&utm_campaign=ai-kurou%2FKoDriver&labelColor=171717&color=FF570A&link=https%3A%2F%2Fcoderabbit.ai&label=CodeRabbit+Reviews)
[![License](https://img.shields.io/github/license/ai-kurou/KoDriver)](LICENSE)
[![GitHub Release](https://img.shields.io/github/v/release/ai-kurou/KoDriver)](https://github.com/ai-kurou/KoDriver/releases)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin)

A Compose Multiplatform app that announces real-time driving information from Le Mans Ultimate (LMU) via Windows TTS.

## Features

- Select announcements and toggle each item on/off
- Drag & reorder announcement priority
- Real-time announcements via Windows TTS (not yet implemented)

## Requirements

- Windows 10 or later
- Le Mans Ultimate (installed)

## Installation

Download the latest MSI installer from [Releases](https://github.com/ai-kurou/KoDriver/releases) and run it.

The app can be launched without LMU running. It will automatically connect once LMU is detected.

## Architecture

Multi-module Clean Architecture with Kotlin Multiplatform.

```
:app:desktopApp ┬→ :app:shared → :feature:* → :core:domain
                └→ :core:data ────────────────────────↑
                         :feature:* → :core:designsystem
```

| Module | Role |
|---|---|
| `:app:desktopApp` | Desktop app entry point |
| `:app:androidApp` | Android app entry point |
| `:app:webApp` | Web app (not yet implemented) |
| `:app:shared` | Compose Multiplatform shared UI |
| `:core:domain` | Repository interfaces & use cases |
| `:core:data` | Shared memory reader & DataStore (JVM / Android) |
| `:core:designsystem` | Shared Composable components |
| `:feature:readout` | Announcement settings UI |
| `:feature:readout-vehicle-approach` | Vehicle approach announcement detail UI |
| `:feature:narrator` | Audio playback engine (WAV TTS) |
| `:feature:other` | Other settings and license information UI |
| `:server` | Ktor server (not yet implemented) |

## Contributing

This project does not accept pull requests.
You are free to fork, modify, and redistribute this project under the terms of the [GPL-3.0 license](LICENSE).

## Credits

This app uses the `VOICEVOX` speech synthesis software.

- `VOICEVOX:Kenzaki Mesuo` (`VOICEVOX:剣崎雌雄`)
- VOICEVOX official website: <https://voicevox.hiroshiba.jp/>
- VOICEVOX software terms: <https://voicevox.hiroshiba.jp/term/>
- Kenzaki Mesuo terms: <https://voicevox.hiroshiba.jp/product/kenzaki_mesuo/>

## License

[GPL-3.0](LICENSE)

<!-- MODULE-GRAPH-START -->
## Module Graph

![Module Graph](docs/graphs/full-graph.svg)
<!-- MODULE-GRAPH-END -->
