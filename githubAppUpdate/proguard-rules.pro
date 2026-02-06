# Keep Kotlin metadata for coroutines
-keep class kotlin.Metadata { *; }
-keepattributes RuntimeVisibleAnnotations
-keepattributes *Annotation*

# Keep coroutine related classes
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Preserve Kotlin coroutine internal structures
-keep class kotlin.coroutines.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Don't obfuscate Kotlin metadata
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep synthetic methods for Kotlin coroutines
-keepclassmembers class ** {
    synthetic <methods>;
}

# Preserve function types
-keep class kotlin.jvm.functions.** { *; }

# Don't warn about R8 issues with Kotlin metadata
-dontwarn kotlin.Metadata
-dontwarn kotlinx.coroutines.**

# Keep lifecycle coroutine scope
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class androidx.lifecycle.** { *; }
