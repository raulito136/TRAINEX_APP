plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.trainex"
    compileSdk = 36

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.trainex"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"FAKE_API_KEY\"")
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
}

dependencies {
    // AndroidX & Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Networking
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Ads
    implementation("com.google.android.gms:play-services-ads:23.0.0")

    // --- FIREBASE (Gestionado por el BOM) ---
    // El BOM controla que todas las versiones sean compatibles entre sí
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))

    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // Video
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")

    // Graficas
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")

    // Imagenes (Glide)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    // Para poder iniciar sesion con google
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Dependencias de Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Traduccion
    implementation("com.google.mlkit:translate:17.0.2")
    implementation("com.google.mlkit:language-id:17.0.5")

    //notificaciones
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
// Lifecycle - Proporciona 'lifecycleScope' para la Activity
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
// Si vas a usar LiveData en el futuro
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")

    implementation("androidx.fragment:fragment-ktx:1.8.5")
}