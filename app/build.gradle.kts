plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.luanasilva.aulafirebase"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.luanasilva.aulafirebase"
        minSdk = 28
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //Dependências Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    //Firebase Analytics
    implementation("com.google.firebase:firebase-analytics")


    //Autenticação
    implementation("com.google.firebase:firebase-auth-ktx")
    //Banco de Dados
    implementation("com.google.firebase:firebase-firestore-ktx")
    //Armazenamento
    implementation("com.google.firebase:firebase-storage-ktx")

}