// Voskの日本語モデルを取得するタスク `prepareVoskModel` を登録する。展開先（outputDir・modelPath）は適用先で設定する
tasks.register<PrepareVoskModelTask>("prepareVoskModel") {
    modelUrl.set("https://alphacephei.com/vosk/models/vosk-model-small-ja-0.22.zip")
    // Gradleユーザーホーム配下に保存し、クリーンビルドやワークツリーをまたいで再取得を避ける
    cacheDir.set(gradle.gradleUserHomeDir.resolve("kodriver-vosk-model"))
    sha256.set("efa092d280153a77615e9e0c7d7283e93e600de3d19d3bec686c57ef19d52eac")
}
