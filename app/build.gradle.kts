plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// The app version is read from VERSION at the repository root so there is one
// place to change it. versionCode is derived from the same string: 1.2.3 -> 10203.
val appVersionName: String = rootProject.file("VERSION").readText().trim()
val appVersionCode: Int = appVersionName
    .substringBefore('-')
    .split('.')
    .map { it.toInt() }
    .let { (major, minor, patch) -> major * 10000 + minor * 100 + patch }

android {
    namespace = "io.github.nikolareljin.pharos"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.github.nikolareljin.pharos"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // The protocol version is deliberately independent of the app version:
        // most app releases do not change the wire contract, and a controller
        // needs to know which contract it is talking to, not which build.
        buildConfigField("String", "PROTOCOL_VERSION", "\"1.0\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Release signing is supplied by CI from secrets and is absent locally and
    // in a fork. When it is absent the release build still produces an unsigned
    // APK rather than failing, so anyone can build this repository.
    val keystorePath: String? = System.getenv("KEYSTORE_PATH")
    signingConfigs {
        if (keystorePath != null && file(keystorePath).exists()) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        sarifReport = true
        // Path-scoped suppressions live in app/lint.xml.
        lintConfig = file("lint.xml")

        disable += setOf(
            // "A newer version exists" is true of every dependency eventually.
            // Failing the build the day upstream publishes turns an unrelated
            // release into our outage; dependency updates are a deliberate
            // change with their own pull request.
            "GradleDependency",
            "AndroidGradlePluginVersion",
            // The TV banner is xhdpi-only by convention — televisions run at
            // xhdpi and above, and generating four more copies of one asset
            // would add weight to satisfy a check about drawables in general.
            "IconMissingDensityFolder",
        )
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
