# Add project specific ProGuard rules here
-keep class com.luviio.upialert.** { *; }
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepattributes *Annotation*
-keepclassmembers class **.R$* {
    public static <fields>;
}
-dontwarn kotlin.**
-keep class kotlin.** { *; }
