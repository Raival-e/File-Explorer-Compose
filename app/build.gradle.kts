plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.raival.compose.file.explorer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.raival.compose.file.explorer"
        minSdk = 26
        targetSdk = 36
        versionCode = 8
        versionName = "1.3.0"
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        apiVersion = "1.9"
    }

    baselineProfile {
        dexLayoutOptimization = true
    }
}

dependencies {
    "baselineProfile"(project(":baselineprofile"))
    implementation(libs.androidx.profileinstaller)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Local/File-based dependencies
    implementation(files("libs/APKEditor.jar"))

    // AndroidX - Core & Lifecycle
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.material)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.ui.tooling.preview.android)

    // Other Jetpack & Android Libraries
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.palette.ktx)

    // Sora Code Editor
    implementation(libs.sora.editor)
    implementation(libs.sora.editor.language.java)
    implementation(libs.sora.editor.language.textmate)

    // Image Loading - Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)
    implementation(libs.coil.video)
    implementation(libs.okio)

    // Third-Party UI/Compose Utilities
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.cascade.compose)
    implementation(libs.compose.swipebox)
    implementation(libs.grid)
    implementation(libs.lazycolumnscrollbar)
    implementation(libs.reorderable)
    implementation(libs.zoomable)

    // Third-Party General Utilities
    implementation(libs.apksig)
    implementation(libs.commons.net)
    implementation(libs.gson)
    implementation(libs.storage)
    implementation(libs.zip4j)
}