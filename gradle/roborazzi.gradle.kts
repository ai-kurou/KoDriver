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

// record/verify の各タスク（recordRoborazziJvmTest 等）は
// io.github.takahirom.roborazzi Gradle Plugin がテストタスク（jvmTest / testAndroidHostTest）ごとに
// 自動生成する（同名のため、以前ここで手動登録していたタスクは廃止）。
// プラグイン適用により、record/verify 実行後に HTML レポート（build/reports/roborazzi/index.html）も生成される。
