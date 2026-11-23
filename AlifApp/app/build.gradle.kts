plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.alif.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.alif.app"
        minSdk = 26          // Android 8.0
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        // Поддерживаемые языки интерфейса. Чтобы добавить таджикский:
        // 1) создайте app/src/main/res/values-tg/strings.xml
        // 2) добавьте "tg" сюда, в res/xml/locales_config.xml и в AppLanguage.kt
        resourceConfigurations += listOf("ru")
    }

    androidResources {
        // Аудио не сжимаем — так оно быстрее открывается из assets
        noCompress += listOf("mp3", "ogg", "wav")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Для учебной сборки подписываем debug-ключом, чтобы APK сразу ставился на телефон.
            // Для публикации в Google Play создайте свой ключ (Build > Generate Signed Bundle / APK).
            signingConfig = signingConfigs.getByName("debug")
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
        compose = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.datasource)
    implementation(libs.androidx.media3.database)

    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}
