plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

// The upload key, from gradle properties or the environment; never in the repository.
fun releaseSetting(name: String) = providers.gradleProperty(name).orElse(providers.environmentVariable(name)).orNull
val uploadStore = releaseSetting("ANTENIO_RELEASE_STORE_FILE")

android {
    namespace = "dev.iptvplayer.tv"
    compileSdk = 36
    defaultConfig {
        applicationId = "app.antenio.tv"
        minSdk = 26
        targetSdk = 36
        versionCode = 106
        versionName = "0.15.0-design-preview"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true; buildConfig = true }
    // English (values/, en-US in res/resources.properties) and Turkish (values-tr/): Android 13 and later list them for
    // the app's own language (LocaleController).
    androidResources { generateLocaleConfig = true }
    // Settings picks a language the TV may not speak, so a Play bundle keeps every language in the base APK.
    bundle { language { enableSplit = false } }
    signingConfigs {
        if (!uploadStore.isNullOrBlank()) {
            create("release") {
                storeFile = file(uploadStore)
                storePassword = releaseSetting("ANTENIO_RELEASE_STORE_PASSWORD")
                keyAlias = releaseSetting("ANTENIO_RELEASE_KEY_ALIAS")
                keyPassword = releaseSetting("ANTENIO_RELEASE_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        // en-XA (longer, accented text) and ar-XB (right to left) for layout checks on the emulator; never in release.
        debug { isPseudoLocalesEnabled = true }
        // TV deliveries use this type: debuggable builds run Compose without ART optimisations or the
        // libraries' baseline profiles and stutter on TV SoCs. Instrumentation tests keep using debug.
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Design previews update earlier debug-signed installs in place; the same key keeps user data. Only they may
            // fall back to the debug key: a Play bundle or a store version stops without the upload key (below).
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions { unitTests.isReturnDefaultValues = true }
    sourceSets.getByName("test").resources.srcDir("src/sharedTest/resources")
    sourceSets.getByName("androidTest").assets.srcDir("src/sharedTest/resources")
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

// LAUNCH_AUDIT E1: never a debug-signed artifact for the store. bundleRelease (the Play upload, E2) always needs the
// upload key; assembleRelease needs it too unless the version is a design preview for the test TV.
val hasUploadKey = !uploadStore.isNullOrBlank()
val designPreview = android.defaultConfig.versionName.orEmpty().endsWith("-design-preview")
tasks.matching { it.name == "bundleRelease" || (it.name == "assembleRelease" && !designPreview) }.configureEach {
    val signed = hasUploadKey
    doFirst {
        if (!signed) throw GradleException("$name needs the upload key: set ANTENIO_RELEASE_STORE_FILE, " +
            "ANTENIO_RELEASE_STORE_PASSWORD, ANTENIO_RELEASE_KEY_ALIAS and ANTENIO_RELEASE_KEY_PASSWORD (WORKFLOW).")
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.05.01"))
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    // QR code for adding a source from a phone (SETUP_FLOW, D117); MIT, no transitive dependencies.
    implementation("io.nayuki:qrcodegen:1.8.0")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.tv:tv-material:1.1.0")
    implementation("androidx.media3:media3-exoplayer:1.11.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.11.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.11.1")
    implementation("androidx.media3:media3-ui:1.11.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20250517")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.05.01"))
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
