# کالانو — R8/ProGuard
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Application & Activities (Manifest)
-keep public class com.example.anbar.AnbarApp { *; }
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends androidx.work.Worker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# ViewBinding / databinding generated
-keep class com.example.anbar.databinding.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn androidx.room.paging.**

# Models used by Room / reflection-free but keep for safety
-keep class com.example.anbar.db.** { *; }

# Kotlin
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }

# Material / AndroidX
-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }

# CameraX + ML Kit
-keep class androidx.camera.** { *; }
-keep class com.google.mlkit.** { *; }
-dontwarn androidx.camera.**
-dontwarn com.google.mlkit.**

# Play services auth
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.auth.** { *; }

# Security crypto
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# ZXing
-keep class com.google.zxing.** { *; }

# FileProvider
-keep class androidx.core.content.FileProvider { *; }

# Enums
-keepclassmembers enum * { *; }

# Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Don't strip license integrity helpers
-keep class com.example.anbar.LicenseManager { *; }
