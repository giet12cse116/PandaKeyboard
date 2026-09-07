# ProGuard / R8 Rules for Panda Keyboards

# Keep kotlinx.serialization models (themes.json parsing)
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
    @kotlinx.serialization.Serializer <fields>;
}
-keepclassmembers class * {
    *** Companion;
}

# Keep theme and settings data models
-keep class com.panda.keyboards.theme.** { *; }
-keep class com.panda.keyboards.fonts.** { *; }

# Keep Hilt / Dagger generated components
-keep class * extends dagger.hilt.internal.UnstableApi
-keep class com.panda.keyboards.di.** { *; }

# Keep AppWidgetProvider receivers
-keep class com.panda.keyboards.widget.** { *; }
