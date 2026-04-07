# Keep Kotlin metadata for coroutines
-keep class kotlin.Metadata { *; }
-keepattributes RuntimeVisibleAnnotations
-keepattributes *Annotation*,InnerClasses,AnnotationDefault

# Keep coroutine related classes
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Preserve Kotlin coroutine internal structures
-keep class kotlin.coroutines.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Don't obfuscate Kotlin metadata
-keepattributes SourceFile,LineNumberTable

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

##------------------ kotlinx.serialization ------------------

# Keep @Serializable companion objects and their serializer() methods
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep serializer() on named companion objects of serializable classes
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep INSTANCE.serializer() for serializable objects
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serialization descriptor fields
-keepclassmembers class kotlinx.serialization.internal.** { *; }

# Suppress notes about kotlinx.serialization internals
-dontnote kotlinx.serialization.**
-dontwarn kotlinx.serialization.**
