plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.wake_up_bird"
    compileSdk = 34

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.wake_up_bird"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding=true
        dataBinding=true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
    }
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src\\main\\assets", "src\\main\\assets")
            }
        }
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
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.github.bumptech.glide:glide:4.11.0")
    implementation("com.google.firebase:firebase-bom:29.3.1")
    implementation("com.google.firebase:firebase-firestore-ktx:21.2.1")
    implementation("com.google.firebase:firebase-storage:19.0.0")
    implementation("com.google.firebase:firebase-messaging-ktx:23.2.1")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.4.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.kakao.sdk:v2-user:2.12.1")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.2")
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.identity.credential.android)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation("com.google.android.material:material:1.4.0")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.0-alpha04")
    implementation("androidx.camera:camera-camera2:1.3.0-alpha04")
    implementation("androidx.camera:camera-lifecycle:1.3.0-alpha04")
    implementation("androidx.camera:camera-view:1.3.0-alpha04")
    implementation("androidx.camera:camera-mlkit-vision:1.3.0-alpha04")
    implementation("androidx.camera:camera-extensions:1.3.0-alpha04")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}