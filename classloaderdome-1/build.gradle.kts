plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // ❗作为 Xposed 模块使用务必添加，其它情况可选
    alias(libs.plugins.yukihookapi.devtools)
}

android {
    namespace = "io.github.dreammooncai.classloaderdome_1"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.github.dreammooncai.classloaderdome_1"
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
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    compileOnlyApi(project.files("libs/test.jar"))
    // 基础依赖
    api(libs.yukihookapi.api.kotlin)

    // ❗作为 Xposed 模块使用务必添加，其它情况可选
    compileOnlyApi(libs.android.xposed)
    // ❗作为 Xposed 模块使用务必添加，其它情况可选
    ksp(libs.yukihookapi.ksp.xposed)
}