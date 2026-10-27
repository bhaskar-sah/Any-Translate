# Proguard rules for Any-Translate

# Keep Room generated code
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep ML Kit Translation models & native methods
-keep class com.google.mlkit.nl.translate.** { *; }
-dontwarn com.google.mlkit.nl.translate.**

# Google Play Services & AdMob
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.ads.** {
   public *;
}

# Gson models
-keepclassmembers class com.bhaskar.anytranslate.data.** { <fields>; }
-keep class com.bhaskar.anytranslate.data.model.** { *; }
-keep class com.bhaskar.anytranslate.data.local.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
