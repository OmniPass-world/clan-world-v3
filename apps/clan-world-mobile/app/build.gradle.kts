plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

val homeUrl = providers.environmentVariable("CLAN_WORLD_HOME_URL")
  .orElse("https://clan-world.com")

// Optional stable signing key. See apps/kickstart-mobile/app/build.gradle.kts
// for details — both apps share the same secret names so one CI step can
// provision both.
val releaseKeystorePath: String? = System.getenv("RELEASE_KEYSTORE_PATH")?.takeIf { it.isNotBlank() }
val releaseKeystorePassword: String? = System.getenv("RELEASE_KEYSTORE_PASSWORD")
val releaseKeyAlias: String? = System.getenv("RELEASE_KEY_ALIAS")
val releaseKeyPassword: String? = System.getenv("RELEASE_KEY_PASSWORD")
val hasStableSigning = releaseKeystorePath != null &&
  !releaseKeystorePassword.isNullOrBlank() &&
  !releaseKeyAlias.isNullOrBlank() &&
  !releaseKeyPassword.isNullOrBlank()

android {
  namespace = "world.clan.app"
  compileSdk = 35

  defaultConfig {
    applicationId = "world.clan.app"
    minSdk = 26
    targetSdk = 35
    versionCode = 3
    versionName = "0.1.10"
    buildConfigField("String", "HOME_URL", "\"${homeUrl.get()}\"")
  }

  if (hasStableSigning) {
    signingConfigs {
      create("stable") {
        storeFile = file(releaseKeystorePath!!)
        storePassword = releaseKeystorePassword
        keyAlias = releaseKeyAlias
        keyPassword = releaseKeyPassword
      }
    }
  }

  buildTypes {
    debug {
      if (hasStableSigning) {
        signingConfig = signingConfigs.getByName("stable")
      }
    }
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
