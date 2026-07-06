plugins {
    id("feature-compose")
}

private val libs = versionCatalogs.named("libs")
private val generatedRoborazziUtilDir = layout.buildDirectory.dir("generated/source/roborazzi/jvmTest/kotlin")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("compose-foundation").get())
            implementation(libs.findLibrary("compose-material3").get())
            implementation(libs.findLibrary("compose-uiToolingPreview").get())
        }
        jvmTest.dependencies {
            implementation(libs.findLibrary("compose-uiTest").get())
            implementation(libs.findLibrary("compose-uiTestJunit4").get())
            implementation(libs.findLibrary("kotlin-testJunit").get())
            implementation(compose.desktop.currentOs)
            implementation(libs.findLibrary("compose-material3-adaptive-layout").get())
            implementation(libs.findLibrary("roborazzi-composeDesktop").get())
        }
        jvmTest {
            kotlin.srcDir(generatedRoborazziUtilDir)
        }
    }
}

afterEvaluate {
    val namespace =
        extensions.getByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>().android.namespace
            ?: error("Android namespace is required")
    val outputFile =
        generatedRoborazziUtilDir.get().file("${namespace.replace('.', '/')}/RoborazziUtil.kt").asFile
    outputFile.parentFile.mkdirs()
    outputFile.writeText(
        """
        package $namespace

        import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
        import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
        import androidx.compose.ui.test.SemanticsNodeInteraction
        import androidx.compose.ui.unit.dp
        import com.github.takahirom.roborazzi.RoborazziOptions
        import io.github.takahirom.roborazzi.captureRoboImage

        private val defaultOptions = RoborazziOptions(
            compareOptions = RoborazziOptions.CompareOptions(
                changeThreshold = 0.02f,
            ),
        )

        internal fun SemanticsNodeInteraction.captureRoboImage() =
            captureRoboImage(roborazziOptions = defaultOptions)

        @OptIn(ExperimentalMaterial3AdaptiveApi::class)
        internal val twoPaneDirective = PaneScaffoldDirective(
            maxHorizontalPartitions = 2,
            horizontalPartitionSpacerSize = 16.dp,
            maxVerticalPartitions = 1,
            verticalPartitionSpacerSize = 0.dp,
            defaultPanePreferredWidth = 360.dp,
            excludedBounds = emptyList(),
        )
        """.trimIndent() + "\n",
    )
}

apply(from = rootProject.file("gradle/roborazzi.gradle.kts"))
