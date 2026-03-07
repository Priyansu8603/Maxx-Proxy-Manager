plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.maxx"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.maxx"
        minSdk = 24
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
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
    implementation (libs.androidx.material.icons.extended)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.protolite.well.known.types)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation (libs.ui)
    implementation (libs.material3)
    implementation (libs.androidx.navigation.compose)
    implementation(libs.okhttp)

    // Additional OkHttp dependencies for proxy testing
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // WebView support for proxy browser
    implementation("androidx.webkit:webkit:1.11.0")

    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    implementation (libs.androidx.room.runtime)
    kapt (libs.androidx.room.compiler)
    implementation (libs.androidx.room.ktx)
    implementation (libs.androidx.room.paging)

    implementation ("androidx.datastore:datastore-preferences:1.0.0")

    implementation("com.google.dagger:hilt-android:2.56.2")
    kapt("com.google.dagger:hilt-android-compiler:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    implementation (libs.androidx.datastore.datastore.preferences)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation ("androidx.compose.material:material:1.8.3")

    implementation("org.jsoup:jsoup:1.17.2") // For HTTP testing
}