# Project specific ProGuard rules for ColorWorlds

# Preserve Room components and entities
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}

# Preserve Moshi / JSON models if needed
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# Google Play Services & AdMob
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# Keep line numbers for debugging crash stack traces
-keepattributes SourceFile,LineNumberTable

