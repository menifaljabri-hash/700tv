# 700TV ProGuard Rules

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.google.gson.** { *; }
-keep class com.tv700.player.data.model.** { *; }
-keep class com.tv700.player.domain.model.** { *; }
-keep class com.tv700.player.data.download.DownloadItem { *; }

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# ExoPlayer / Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }

# Kotlin Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Compose
-keep class androidx.compose.** { *; }
