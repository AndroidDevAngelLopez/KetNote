plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("io.realm.kotlin")
    id("com.google.gms.google-services")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    //ROOM
    val roomVersion = "2.6.0"
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    //Mongo Realm
    implementation("io.realm.kotlin:library-sync:1.11.1")
    //SplashScreen
    implementation("androidx.core:core-splashscreen:1.0.1")
    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    //Navigation
    val navVersion = "2.7.4"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    //Fragments
    val fragmentVersion = "1.7.0-alpha06"
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")
    //Recyclerview
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    //Activity extensions
    val activityVersion = "1.8.0"
    implementation("androidx.activity:activity-ktx:$activityVersion")
    //ViewPager
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    //Hilt
    implementation("com.google.dagger:hilt-android:2.48.1")
    ksp("com.google.dagger:hilt-compiler:2.48.1")
    //DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    //GMS Auth
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    //Lifecycle - ViewModel
    val lifecycleVersion = "2.6.2"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    //Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    //Core extensions
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}