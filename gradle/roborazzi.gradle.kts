// Gradle はコンフィギュレーション時にタスク名を解決するため、実行時ではなくここで判定する
val startTaskNames = gradle.startParameter.taskNames
val isRecordMode = startTaskNames.any { it.contains("recordRoborazziJvmTest") }
val isVerifyMode = startTaskNames.any { it.contains("verifyRoborazziJvmTest") }

tasks.withType<Test>().configureEach {
    systemProperty("skiko.renderApi", "SOFTWARE_FAST")
    systemProperty("roborazzi.output.dir", "$projectDir/src/jvmTest/snapshots")
    if (isRecordMode) systemProperty("roborazzi.test.record", "true")
    if (isVerifyMode) systemProperty("roborazzi.test.verify", "true")
}

tasks.register("recordRoborazziJvmTest") {
    group = "roborazzi"
    description = "スクリーンショットのゴールデン画像を更新する"
    dependsOn("jvmTest")
}

tasks.register("verifyRoborazziJvmTest") {
    group = "roborazzi"
    description = "スクリーンショットをゴールデン画像と比較する"
    dependsOn("jvmTest")
}
