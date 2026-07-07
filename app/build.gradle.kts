plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.checkin.watermark"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.checkin.watermark"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    flavorDimensions += "locationMode"
    productFlavors {
        create("manual") {
            dimension = "locationMode"
            applicationIdSuffix = ".manual"
            resValue("string", "app_name", "水印打卡-手动版")
            buildConfigField("boolean", "SMART_LOCATION", "false")
        }
        create("smart") {
            dimension = "locationMode"
            applicationIdSuffix = ".smart"
            resValue("string", "app_name", "水印打卡-智能版")
            buildConfigField("boolean", "SMART_LOCATION", "true")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:record"))
    implementation(project(":location:common"))
    implementation(project(":location:manual"))
    implementation(project(":location:smart"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
