-keep class com.example.htmlquickview.database.** { *; }
-keepclassmembers class com.example.htmlquickview.database.** { *; }

-keep class androidx.room.** { *; }
-keepclassmembers class androidx.room.** { *; }

-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver {
    <methods>;
}

-keep class com.example.htmlquickview.model.** { *; }
-keepclassmembers class com.example.htmlquickview.model.** { *; }

-dontwarn javax.annotation.**
-dontwarn javax.inject.**

-keepattributes Signature
-keepattributes *Annotation*

-if interface * { @androidx.room.Dao public *** *(...); }
-keepclassmembers class * implements androidx.room.Dao {
    public <methods>;
}

-keepclasseswithmembers class * {
    @androidx.room.Entity <fields>;
}

-keep class * extends androidx.room.RoomDatabase {
    <methods>;
}