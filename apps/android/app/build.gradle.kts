plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

val convexUrl = providers.environmentVariable("CLANWORLD_CONVEX_URL")
  .orElse("https://valuable-kudu-985.convex.cloud")
val tokenUrl = providers.environmentVariable("CLANWORLD_GOLD_TOKEN_URL")
  .orElse("https://kickstart.easya.io/token/4kWysUHVqtFmxrvwPUxA66exm2iJBMkvD4EBRrNmcieL")

android {
  namespace = "world.clan.gold"
  compileSdk = 35

  defaultConfig {
    applicationId = "world.clan.gold"
    minSdk = 26
    targetSdk = 35
    versionCode = 1
    versionName = "0.1.0"
    buildConfigField("String", "CONVEX_URL", "\"${convexUrl.get()}\"")
    buildConfigField("String", "TOKEN_URL", "\"${tokenUrl.get()}\"")
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
