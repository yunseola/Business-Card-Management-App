import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("dagger.hilt.android.plugin") // ✅ Hilt 플러그인 추가
    kotlin("kapt")                   // ✅ kapt 추가 (Hilt 컴파일러 사용을 위해)
}

android {
    namespace = "com.example.businesscardapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.businesscardapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // local.properties의 값 읽기 (없으면 빈 문자열)
        val localPropsFile = rootProject.file("local.properties")
        val localProps = Properties().apply {
            if (localPropsFile.exists()) {
                load(localPropsFile.inputStream())
            }
        }
        val gmsKey = localProps.getProperty("GMS_KEY") ?: ""
// BuildConfig 주입
        buildConfigField("String", "GMS_KEY", "\"$gmsKey\"")
    }
    sourceSets["main"].jniLibs.srcDirs("src/main/jniLibs")

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.0" // 최신 버전 권장
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    // ————————————————————————————————
    // 1. Compose BOM (한 번만 선언)
    //    UI 라이브러리 버전을 BOM에 위임
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))

    // 2. Compose 핵심 UI
    implementation("androidx.compose.ui:ui")                        // Compose UI
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")         // Preview 지원
    debugImplementation("androidx.compose.ui:ui-tooling")            // Tooling (디버그)
    implementation("androidx.compose.ui:ui-text")

    implementation("androidx.compose.foundation:foundation:1.6.0")    // 레이아웃, 리스팅 등
//    implementation("androidx.compose.foundation:foundation-pager:1.6.0") // Pager 공식 API
//    implementation("android.compose.foundation:foundation-pager")
    implementation("androidx.compose.material:material")             // Material2
    implementation("androidx.compose.material3:material3")           // Material3

    implementation("androidx.activity:activity-compose:1.9.0")

    // 3. Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // 4. Accompanist
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.31.5-beta")
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // 5. CameraX
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // 6. ML Kit (OCR)
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:text-recognition-korean:16.0.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    implementation("com.google.android.gms:play-services-tasks:18.0.0")
    implementation("com.google.android.gms:play-services-safetynet:18.0.1")

    // 7. Coil (이미지 로딩)
    implementation("io.coil-kt:coil:2.5.0")
    implementation("io.coil-kt:coil-compose:2.5.0")

    // 8. Retrofit & Coroutines
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")

    // 9. Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation ("com.google.accompanist:accompanist-pager:0.35.0-alpha")
    implementation ("com.google.accompanist:accompanist-pager-indicators:0.35.0-alpha")

    // 10. Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // ————————————————————————————————
    // 11. Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // 12. login
    implementation ("com.google.android.gms:play-services-auth:20.7.0")

    // 13. share
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    kapt ("com.github.bumptech.glide:compiler:4.16.0")

    // ————————————————————————————————
    // 백엔드 연동 시 다시 추가할 예정
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // AI 프롬프트 기반 분류를 위한 의존성
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    // 14. 앨범
    implementation("org.opencv:opencv:4.9.0")

    // 15. 전화
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    kapt ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("androidx.appcompat:appcompat:1.7.0")


    // 16. 위젯
    implementation ("androidx.glance:glance-appwidget:1.1.0")
    implementation ("androidx.glance:glance-material3:1.1.0")

    // 디지털 명함 가로,세로 사진
    implementation("androidx.core:core-ktx:1.13.1")
    implementation ("com.google.zxing:core:3.5.3")
}