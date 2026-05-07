plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
}

val homeUrl = providers.environmentVariable("CLAN_WORLD_HOME_URL")
  .orElse("https://app.clan-world.com")

val terminalBaseUrl = providers.environmentVariable("CLAN_WORLD_TERMINAL_BASE_URL")
  .orElse("https://cockpit.clan-world.com")

android {
  namespace = "world.clan.app"
  compileSdk = 35

  defaultConfig {
    applicationId = "world.clan.app"
    minSdk = 26
    targetSdk = 35
    versionCode = 1
    versionName = "0.1.0"
    buildConfigField("String", "MAP_URL", "\"${homeUrl.get()}\"")
    buildConfigField("String", "TERMINAL_BASE_URL", "\"${terminalBaseUrl.get()}\"")
  }

  buildFeatures {
    buildConfig = true
    compose = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {
  // Compose BOM — pins all Compose lib versions to a tested set
  val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Compose core
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.foundation:foundation")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.runtime:runtime")
  debugImplementation("androidx.compose.ui:ui-tooling")

  // Activity / lifecycle
  implementation("androidx.activity:activity-compose:1.9.3")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
  implementation("androidx.core:core-ktx:1.15.0")

  // Navigation
  implementation("androidx.navigation:navigation-compose:2.8.5")

  // WebView (modern APIs + safe browsing)
  implementation("androidx.webkit:webkit:1.12.1")

  // Solana Mobile Wallet Adapter — real signing
  implementation("com.solanamobile:mobile-wallet-adapter-clientlib-ktx:2.0.7")
}
