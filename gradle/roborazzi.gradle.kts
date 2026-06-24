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

tasks.register("recordRoborazziAndroidHostTest") {
    group = "roborazzi"
    description = "Android スクリーンショットのゴールデン画像を更新する"
    dependsOn("testAndroidHostTest")
}

tasks.register("verifyRoborazziAndroidHostTest") {
    group = "roborazzi"
    description = "Android スクリーンショットをゴールデン画像と比較する"
    dependsOn("testAndroidHostTest")
}
