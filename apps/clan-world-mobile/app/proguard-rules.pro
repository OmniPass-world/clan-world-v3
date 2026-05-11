# Keep app-specific release rules here if R8 exposes reflection/runtime needs.

# Tink references Error Prone annotations used only at compile time.
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi
