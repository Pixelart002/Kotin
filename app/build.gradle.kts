plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.upi.announcer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.upi.announcer"
        minSdk = 24  // Android 7.0 aur uske upar chalega
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    
    // Java 17 use karna zaroori hai naye Android versions ke liye
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Sirf core cheezein, koi faltu UI library nahi kyunki app background me chalegi
    implementation("androidx.core:core-ktx:1.12.0")
}