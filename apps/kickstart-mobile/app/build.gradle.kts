plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

// Build-time URL config.
//
// We deliberately fall back from environment → Gradle project property → null.
// If neither is set the build fails loudly: previously the env-var-only path
// silently shipped the dev Convex URL into release APKs.
//
// Local devs can keep the dev URL in `~/.gradle/gradle.properties`:
//   clanworldConvexUrl=https://valuable-kudu-985.convex.cloud
//   kickstartHomeUrl=https://kickstart.easya.io
// CI must pass the env var explicitly.
fun resolveBuildConfigUrl(envName: String, propName: String): String {
  val envValue = System.getenv(envName)?.takeIf { it.isNotBlank() }
  if (envValue != null) return envValue
  val propValue = (project.findProperty(propName) as? String)?.takeIf { it.isNotBlank() }
  if (propValue != null) return propValue
  error(
    "$envName must be set at build time (env var or Gradle property `$propName`). " +
      "Refusing to fall back to a hardcoded URL — that path silently shipped the dev " +
      "backend in past releases.",
  )
}

val convexUrl = resolveBuildConfigUrl("CLANWORLD_CONVEX_URL", "clanworldConvexUrl")
val homeUrl = resolveBuildConfigUrl("KICKSTART_HOME_URL", "kickstartHomeUrl")

android {
  namespace = "io.easya.kickstart"
  compileSdk = 35

  defaultConfig {
    applicationId = "io.easya.kickstart"
    minSdk = 26
    targetSdk = 35
    versionCode = 9
    versionName = "0.1.9"
    buildConfigField("String", "CONVEX_URL", "\"$convexUrl\"")
    buildConfigField("String", "HOME_URL", "\"$homeUrl\"")
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
