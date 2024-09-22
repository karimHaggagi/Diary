plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin)
    alias(libs.plugins.compose.compiler)

    id("io.realm.kotlin")
}

android {
    namespace = "com.example.write"
    compileSdk = ProjectConfig.compileSdk

    defaultConfig {
        minSdk = ProjectConfig.minSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)



    // Compose Navigation
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)


    // Runtime Compose
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Message Bar Compose
    implementation(libs.messagebarcompose)



    // Mongo DB Realm
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.library.sync)

    // CALENDAR
    implementation(libs.calendar)
    // Date-Time Picker
    implementation(libs.core)

    // CLOCK
    implementation(libs.clock)

    // Coil
    implementation(libs.coil.compose)

    // Firebase
    implementation(libs.firebase.auth.ktx)

    implementation(libs.firebase.storage.ktx)
    // Desugar JDK
    coreLibraryDesugaring(libs.desugar.jdk.libs)


    implementation(project(":core:util"))
    implementation(project(":core:ui"))
    implementation(project(":data:mongo"))
}