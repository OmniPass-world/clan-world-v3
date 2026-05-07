plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

val homeUrl = providers.environmentVariable("CLAN_WORLD_HOME_URL")
  .orElse("https://clan-world.com")

android {
  namespace = "world.clan.app"
  compileSdk = 35

  defaultConfig {
    applicationId = "world.clan.app"
    minSdk = 26
    targetSdk = 35
    versionCode = 1
    versionName = "0.1.6"
    buildConfigField("String", "HOME_URL", "\"${homeUrl.get()}\"")
  }

  buildFeatures {
    buildConfig = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }
}
