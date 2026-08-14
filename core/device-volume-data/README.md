# device-volume-data

端末（OS）のマスター音量を取得・設定するRepositoryを実装するJVM / Androidマルチプラットフォームモジュールです。
KoDriver自体のアナウンス音量設定とは異なり、OS側の再生音量（読み上げが実際に聞こえるかどうかに関わる音量）を扱います。

- jvmMain: Windows Core Audio（WASAPI `IAudioEndpointVolume`）をJNA経由で直接呼び出す`WindowsDeviceVolumeRepository`。
  非Windowsでは何もしないNo-Op実装にフォールバックする。
- androidMain: `AudioManager`（`STREAM_MUSIC`）を使う`AndroidDeviceVolumeRepository`。

いずれも`core:domain`の`DeviceVolumeRepository`（値は0-100のパーセンテージ）を実装する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-device-volume-data.svg)
<!-- MODULE-GRAPH-END -->
