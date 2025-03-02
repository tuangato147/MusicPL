plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.musicplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.musicplayer"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // ExoPlayer
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.exoplayer:exoplayer-core:2.18.7")
    implementation ("com.google.android.exoplayer:exoplayer-ui:2.18.7")

    // RecyclerView
    implementation ("androidx.recyclerview:recyclerview:1.3.0")

    // Material Design (cho các components UI)
    implementation ("com.google.android.material:material:1.9.0")

    // Thư viện ExoPlayer để phát nhạc
//    implementation ("com.google.android.exoplayer:exoplayer:2.18.0")
//    implementation ("androidx.recyclerview:recyclerview:1.2.1")
}