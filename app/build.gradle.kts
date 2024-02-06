plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("io.realm.kotlin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.complexsoft.ketnote"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.complexsoft.ketnote"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
    androidResources {
        generateLocaleConfig = true
    }
}

dependencies {

    //WINDOW MANAGER
    implementation("androidx.window:window:1.2.0")
    implementation("androidx.window:window-core:1.2.0")
    implementation("androidx.window.extensions.core:core:1.0.0")
    //GEMINI
    implementation("com.google.ai.client.generativeai:generativeai:0.1.2")
    //PHOTO VIEW
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    //ROOM
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    //Mongo Realm
    implementation("io.realm.kotlin:library-sync:1.13.0")
    //SplashScreen
    implementation("androidx.core:core-splashscreen:1.0.1")
    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    //Navigation
    val navVersion = "2.7.6"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    //Fragments
    val fragmentVersion = "1.7.0-alpha09"
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")
    //Recyclerview
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    //Activity extensions
    val activityVersion = "1.8.2"
    implementation("androidx.activity:activity-ktx:$activityVersion")
    //ViewPager
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    //Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    //DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    //GMS Auth
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    //Lifecycle - ViewModel
    val lifecycleVersion = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    //Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    //Material Design
    implementation("com.google.android.material:material:1.12.0-alpha03")
    //Core extensions
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}