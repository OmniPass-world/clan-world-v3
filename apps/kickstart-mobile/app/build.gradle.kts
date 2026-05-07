plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

val convexUrl = providers.environmentVariable("CLANWORLD_CONVEX_URL")
  .orElse("https://valuable-kudu-985.convex.cloud")
val homeUrl = providers.environmentVariable("KICKSTART_HOME_URL")
  .orElse("https://kickstart.easya.io")

android {
  namespace = "io.easya.kickstart"
  compileSdk = 35

  defaultConfig {
    applicationId = "io.easya.kickstart"
    minSdk = 26
    targetSdk = 35
    versionCode = 4
    versionName = "0.1.3"
    buildConfigField("String", "CONVEX_URL", "\"${convexUrl.get()}\"")
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
