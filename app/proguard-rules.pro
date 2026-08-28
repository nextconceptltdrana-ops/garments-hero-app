# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep all App Classes and Data Models for Firestore & JSON serialization
-keep class com.example.** { *; }
-keepclassmembers class com.example.** { *; }

# Firebase & Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Google AdMob
-keepattributes Doxygen,NoInline
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.ads.** {
   public *;
}


