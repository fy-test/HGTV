-dontwarn org.bouncycastle.**
-dontwarn javax.naming.**
-dontwarn javax.mail.**
-dontwarn javax.activation.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn androidx.media3.**
-dontwarn com.google.android.exoplayer2.**

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

-keep class androidx.multidex.** { *; }
-keep class com.hgtv.star.MainActivity { *; }
-keep class com.hgtv.star.core.** { *; }
-keep class com.hgtv.star.data.** { *; }
