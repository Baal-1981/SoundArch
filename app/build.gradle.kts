plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"  // KSP for Hilt (Kotlin 2.0+ compatible)
    id("com.google.dagger.hilt.android") version "2.48" apply true
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"  // Kotlinx Serialization
    jacoco  // Code coverage (core Gradle plugin, no version needed)
}

android {
    namespace = "com.soundarch"
    compileSdk = 34

    // ==============================================================================
    // 🔒 NDK VERSION LOCK - Prevents fragmentation, ensures reproducible builds
    // ==============================================================================
    // NDK 26.1.10909125 (r26b) - Latest stable as of 2024
    // - Compatible with minSdk 29 (Android 10+)
    // - Full AAudio support via Oboe
    // - Stable Clang 17 toolchain
    ndkVersion = "26.1.10909125"

    defaultConfig {
        applicationId = "com.soundarch"

        // minSdk 29 = Android 10 (API 29)
        // - Full AAudio low-latency support
        // - AAUDIO_PERFORMANCE_MODE_LOW_LATENCY available
        // - Covers 85%+ of active devices (2024)
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ==============================================================================
        // 📦 ABI SPLIT - Build only for ARM devices (drop x86 emulators)
        // ==============================================================================
        // armeabi-v7a: 32-bit ARM (older phones, ~15% of market)
        // arm64-v8a:   64-bit ARM (modern phones, ~85% of market)
        // x86/x86_64:  Emulators only, skip for production builds
        //
        // Benefits:
        // - Smaller APK size (50% reduction vs building all ABIs)
        // - Faster build times
        // - Play Store auto-delivers correct ABI per device
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                // Pass ABI to CMake for architecture-specific flags
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DANDROID_PLATFORM=android-${minSdk}"
                )
            }
        }
    }

    buildTypes {
        release {
            // ==============================================================================
            // 🗜️ R8 OPTIMIZATION - Code shrinking + obfuscation
            // ==============================================================================
            // R8 (replacing ProGuard):
            // - Removes unused code (dead code elimination)
            // - Obfuscates class/method names (security + smaller APK)
            // - Optimizes bytecode (method inlining, constant folding)
            //
            // CRITICAL: Must keep JNI native methods (see proguard-rules.pro)
            isMinifyEnabled = true
            isShrinkResources = true  // Remove unused resources (layouts, drawables)

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            // Debug builds: No minification for faster builds + readable stack traces
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
        prefab = true  // Enable prefab for TensorFlow Lite native libraries
        buildConfig = true  // Enable BuildConfig generation
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // 🧩 toujours requis
    }

    // ==============================================================================
    // 📦 ASSETS CONFIGURATION - TFLite models stored in assets/models/
    // ==============================================================================
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src/main/assets")
            }
        }
    }

    // Prevent compression of .tflite files (they're already compressed)
    androidResources {
        noCompress += "tflite"
    }

    // ==============================================================================
    // 🔍 LINT CONFIGURATION
    // ==============================================================================
    lint {
        // Disable problematic Compose lint detectors (known bugs with Kotlin 2.0+)
        // These crash due to null UAST service in current AGP/Kotlin combination
        disable += listOf(
            "AutoboxingStateCreation",
            "MutableCollectionMutableState"
        )

        // Abort build on error for CI/CD (disabled to allow warnings-only mode)
        abortOnError = false

        // Check all warnings
        checkAllWarnings = true

        // Generate HTML and XML reports
        htmlReport = true
        xmlReport = true
    }

    // ==============================================================================
    // 🧪 TEST CONFIGURATION - Professional-grade test infrastructure
    // ==============================================================================
    testOptions {
        // Enable Test Orchestrator for isolated test execution
        // Each test runs in its own process to prevent shared state issues
        execution = "ANDROIDX_TEST_ORCHESTRATOR"

        // Unit test configuration
        unitTests {
            isIncludeAndroidResources = true  // Enable Robolectric support
            isReturnDefaultValues = true       // Mock Android APIs
        }

        // Animation control (disable for faster, more reliable tests)
        animationsDisabled = true
    }

    // ==============================================================================
    // 📦 PACKAGING OPTIONS - Fix duplicate resource conflicts
    // ==============================================================================
    packaging {
        resources {
            excludes += listOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/NOTICE.md"
            )
        }
    }
}

// ==============================================================================
// 💉 HILT KSP CONFIGURATION
// ==============================================================================
ksp {
    arg("correctErrorTypes", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.compose.material:material-icons-extended:1.5.3")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 💉 HILT DEPENDENCY INJECTION (using KSP for Kotlin 2.0+ compatibility)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🎵 AUDIO ENGINE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    implementation("com.google.oboe:oboe:1.8.0")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🤖 MACHINE LEARNING (TensorFlow Lite)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TFLite GPU delegate is optional and causes build issues
    // Using CPU-only version with XNNPACK delegate for optimal performance
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // Optional: TFLite GPU delegate (comment out if build fails)
    // implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🎨 JETPACK COMPOSE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.ui:ui:1.5.3")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.3")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🧭 NAVIGATION
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    implementation("androidx.navigation:navigation-compose:2.7.4")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🏛️ ARCHITECTURE COMPONENTS (ViewModel, LiveData, SavedState)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.6.2")
    implementation("androidx.lifecycle:lifecycle-service:2.6.2")  // For foreground service

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ⚡ COROUTINES (Async/Background Processing)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 💾 DATASTORE (Preferences)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 📝 SERIALIZATION (for logging)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🔐 PERMISSIONS (Accompanist for Compose)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🐛 DEBUGGING TOOLS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.3")

    // LeakCanary - Memory leak detection (P0 - Critical)
    // Automatically detects memory leaks in debug builds
    // Reports appear in Logcat and as notifications
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 🧪 TESTS - Professional-grade testing infrastructure
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    // JVM Unit Tests
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.0.21")

    // Professional test libraries
    testImplementation("io.mockk:mockk:1.13.8")  // Kotlin mocking framework
    testImplementation("com.google.truth:truth:1.1.5")  // Better assertions
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")  // Coroutine testing
    testImplementation("app.cash.turbine:turbine:1.0.0")  // Flow testing

    // Instrumented Tests (Android)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Professional test libraries for instrumented tests
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
    androidTestImplementation("com.google.truth:truth:1.1.5")

    // Coroutines testing support (with full runtime dependencies)
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Test Orchestrator (runs each test in isolation)
    androidTestUtil("androidx.test:orchestrator:1.4.2")

    // Compose UI Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.3")

    // Kotlin reflection for UiIds coverage scanner
    androidTestImplementation("org.jetbrains.kotlin:kotlin-reflect:2.0.21")
}

// ==============================================================================
// 📊 UIIDS COVERAGE CUSTOM GRADLE TASKS
// ==============================================================================

/**
 * Run all UiIds coverage tests and generate reports
 *
 * Usage:
 *   ./gradlew runUiIdsCoverage
 *   ./gradlew runUiIdsCoverage -PdeviceId=DEVICE_SERIAL
 */
tasks.register("runUiIdsCoverage") {
    group = "verification"
    description = "Run UiIds coverage tests and generate comprehensive reports"

    doFirst {
        println("")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Running UiIds Coverage Test Suite")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("")
    }

    dependsOn("connectedDebugAndroidTest")

    // Configure test runner arguments via android extension
    android.testOptions.execution = "ANDROIDX_TEST_ORCHESTRATOR"

    // Support custom device selection
    if (project.hasProperty("deviceId")) {
        val deviceId = project.property("deviceId").toString()
        println("🔌 Using device: $deviceId")
        // Device selection handled by adb/gradle automatically
    }

    doLast {
        println("")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("✅ UiIds Coverage Tests Complete")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📄 Reports available at:")
        println("   • debug/log_ui/coverage/SUMMARY.md")
        println("   • debug/log_ui/coverage/coverage_summary.json")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("")
    }
}

/**
 * Check UiIds coverage gate (fail if incomplete)
 *
 * Usage:
 *   ./gradlew checkUiIdsCoverage
 */
tasks.register("checkUiIdsCoverage") {
    group = "verification"
    description = "Check UiIds coverage gate (fails if incomplete)"

    dependsOn("runUiIdsCoverage")

    doLast {
        val summaryFile = file("debug/log_ui/coverage/coverage_summary.json")

        if (!summaryFile.exists()) {
            throw GradleException(
                """
                ❌ Coverage summary not found!
                Expected: ${summaryFile.absolutePath}

                Run './gradlew runUiIdsCoverage' first.
                """.trimIndent()
            )
        }

        // Parse JSON (using kotlinx.serialization would be overkill here)
        val summaryText = summaryFile.readText()
        val allComplete = summaryText.contains("\"allComplete\":true") ||
                         summaryText.contains("\"allComplete\": true")
        val coverageMatch = Regex(""""overallCoverage"\s*:\s*"([^"]+)"""").find(summaryText)
        val coverage = coverageMatch?.groupValues?.get(1) ?: "0.0"

        println("")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🎯 UiIds Coverage Gate Result")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Coverage: $coverage%")
        println("Status: ${if (allComplete) "✅ COMPLETE" else "❌ INCOMPLETE"}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("")

        if (!allComplete) {
            throw GradleException(
                """
                ❌ UiIds coverage is incomplete!

                Review detailed report:
                  cat debug/log_ui/coverage/SUMMARY.md

                Or open:
                  debug/log_ui/coverage/SUMMARY.md
                """.trimIndent()
            )
        }

        println("✅ UiIds coverage gate: PASS")
    }
}

/**
 * Pull coverage reports from device
 *
 * Usage:
 *   ./gradlew pullCoverageReports
 *   ./gradlew pullCoverageReports -PdeviceId=DEVICE_SERIAL
 */
tasks.register<Exec>("pullCoverageReports") {
    group = "verification"
    description = "Pull UiIds coverage reports from device to local debug/ directory"

    val deviceId = if (project.hasProperty("deviceId")) {
        project.property("deviceId").toString()
    } else {
        null
    }

    val adbCommand = if (deviceId != null) {
        listOf("adb", "-s", deviceId, "pull")
    } else {
        listOf("adb", "pull")
    }

    // Try multiple paths (depends on Android version and app permissions)
    val remotePaths = listOf(
        "/sdcard/Android/data/com.soundarch/files/debug/log_ui/coverage/",
        "/storage/emulated/0/Android/data/com.soundarch/files/debug/log_ui/coverage/"
    )

    val localPath = "debug/log_ui/coverage/"

    doFirst {
        println("📥 Pulling coverage reports from device...")
        if (deviceId != null) {
            println("   Device: $deviceId")
        }
    }

    // Create local directory
    file(localPath).mkdirs()

    // Set default command line (will try first path)
    commandLine(adbCommand + listOf(remotePaths.first(), localPath))

    doLast {
        println("✅ Reports pulled from device")
        println("   Check: $localPath")
    }
}

// ==============================================================================
// 📊 JACOCO CODE COVERAGE CONFIGURATION
// ==============================================================================

/**
 * JaCoCo Code Coverage Configuration
 * Generates code coverage reports for JVM unit tests
 *
 * Usage:
 *   ./gradlew testDebugUnitTestCoverage
 *   ./gradlew jacocoTestReport
 *
 * Reports:
 *   - HTML: app/build/reports/jacoco/html/index.html
 *   - XML: app/build/reports/jacoco/jacocoTestReport.xml
 */

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Generate JaCoCo code coverage report for unit tests"

    dependsOn("testDebugUnitTest")

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)

        html.outputLocation.set(file("${buildDir}/reports/jacoco/html"))
        xml.outputLocation.set(file("${buildDir}/reports/jacoco/jacocoTestReport.xml"))
    }

    // Source files
    sourceDirectories.setFrom(files("${project.projectDir}/src/main/java"))
    classDirectories.setFrom(files("${buildDir}/intermediates/javac/debug/classes"))

    // Execution data from tests
    executionData.setFrom(fileTree(buildDir).include("jacoco/testDebugUnitTest.exec"))

    doLast {
        println("")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 JaCoCo Code Coverage Report Generated")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📄 HTML Report: ${buildDir}/reports/jacoco/html/index.html")
        println("📄 XML Report: ${buildDir}/reports/jacoco/jacocoTestReport.xml")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("")
    }
}

/**
 * Enable JaCoCo for debug unit tests
 */
tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}
