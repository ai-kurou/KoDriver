// Gradle はコンフィギュレーション時にタスク名を解決するため、実行時ではなくここで判定する
val startTaskNames = gradle.startParameter.taskNames
val isRecordMode = startTaskNames.any { it.contains("recordRoborazziJvmTest") }
val isVerifyMode = startTaskNames.any { it.contains("verifyRoborazziJvmTest") }
val isRecordModeAndroid = startTaskNames.any { it.contains("recordRoborazziAndroidHostTest") }
val isVerifyModeAndroid = startTaskNames.any { it.contains("verifyRoborazziAndroidHostTest") }

tasks.withType<Test>().configureEach {
    val isAndroidHostTest = name.contains("AndroidHostTest", ignoreCase = true)
    systemProperty("skiko.renderApi", "SOFTWARE_FAST")
    systemProperty(
        "roborazzi.output.dir",
        if (isAndroidHostTest) "$projectDir/src/androidHostTest/snapshots"
        else "$projectDir/src/jvmTest/snapshots",
    )
    if (isAndroidHostTest) {
        if (isRecordModeAndroid) systemProperty("roborazzi.test.record", "true")
        if (isVerifyModeAndroid) systemProperty("roborazzi.test.verify", "true")
    } else {
        if (isRecordMode) systemProperty("roborazzi.test.record", "true")
        if (isVerifyMode) systemProperty("roborazzi.test.verify", "true")
    }
}

fun registerRoborazziTaskIfAbsent(
    taskName: String,
    description: String,
    dependsOnTaskName: String,
) {
    if (tasks.findByName(taskName) == null) {
        tasks.register(taskName) {
            group = "roborazzi"
            this.description = description
            dependsOn(dependsOnTaskName)
        }
    }
}

registerRoborazziTaskIfAbsent(
    taskName = "recordRoborazziJvmTest",
    description = "スクリーンショットのゴールデン画像を更新する",
    dependsOnTaskName = "jvmTest",
)

registerRoborazziTaskIfAbsent(
    taskName = "verifyRoborazziJvmTest",
    description = "スクリーンショットをゴールデン画像と比較する",
    dependsOnTaskName = "jvmTest",
)

// io.github.takahirom.roborazzi Gradle プラグイン(Preview自動生成の試験導入対象モジュールが適用)は、
// AGP の afterEvaluate 処理の中で Android 向けに同名の recordRoborazziAndroidHostTest /
// verifyRoborazziAndroidHostTest タスクを自身登録する。ここでの登録が先に走ると二重登録で
// ビルドが失敗するため、AGP の afterEvaluate より後に確実に評価されるよう afterEvaluate に
// 包み、その時点で未登録の場合のみ登録する(afterEvaluate は登録順に実行されるため、AGP の
// afterEvaluate(通常 feature-kmp 適用時に登録される)より後にこの行が評価される限り安全)。
afterEvaluate {
    registerRoborazziTaskIfAbsent(
        taskName = "recordRoborazziAndroidHostTest",
        description = "Android スクリーンショットのゴールデン画像を更新する",
        dependsOnTaskName = "testAndroidHostTest",
    )

    registerRoborazziTaskIfAbsent(
        taskName = "verifyRoborazziAndroidHostTest",
        description = "Android スクリーンショットをゴールデン画像と比較する",
        dependsOnTaskName = "testAndroidHostTest",
    )
}
