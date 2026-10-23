import java.util.Properties
import java.io.File

plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose") }
val credentials = Properties().apply {
    file("../../../../keystore.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
android {
    namespace = "com.music.streamx.skins.meridian"
    compileSdk = 37
    lint { abortOnError = true }
    defaultConfig {
        applicationId = "com.music.streamx.skins.meridian"
        minSdk = 26
        targetSdk = 36
        versionCode = 5 * 10000 + (providers.gradleProperty("extensionBuildNumber").orNull?.toInt() ?: 0)
        versionName = "1.0.1.2"
    }
    signingConfigs {
        val store = credentials.getProperty("storeFile")?.let { if (File(it).isAbsolute) File(it) else file("../../../../$it") }
        if (store != null && store.exists()) create("release") {
            storeFile = store; storePassword = credentials.getProperty("storePassword")
            keyAlias = credentials.getProperty("keyAlias"); keyPassword = credentials.getProperty("keyPassword")
        }
    }
    buildTypes { release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "skin-rules.pro"); signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug") } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
kotlin { compilerOptions { jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17 } }

// Host supplies the SDK, Compose, models and common services. None are packaged in the skin.
dependencies {
    compileOnly(project(":sharedUi"))
    compileOnly(kotlin("stdlib"))
    testImplementation(project(":sharedUi"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}
