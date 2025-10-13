# ==============================================================================
# SoundArch ProGuard/R8 Configuration
# ==============================================================================
# R8 is enabled in release builds (see build.gradle.kts)
# CRITICAL: Must keep JNI native methods to prevent runtime crashes
#
# Without these rules, R8 will:
# - Strip JNI method names (UnsatisfiedLinkError at runtime)
# - Obfuscate classes referenced from C++ (ClassNotFoundException)
# - Remove seemingly "unused" native methods
#
# ==============================================================================

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# 🔒 JNI KEEP RULES - CRITICAL for native code interaction
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

# Keep MainActivity and all native methods
# R8 must NOT rename/remove methods called from native-lib.cpp
-keep class com.soundarch.MainActivity {
    # Keep all native methods (called from C++ via JNI)
    native <methods>;

    # Keep static callback methods (called from C++ threads)
    public static void updateLatencyText(double);

    # Keep all public methods (may be called from native code)
    public *;
}

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# 🎵 OBOE AUDIO LIBRARY - Keep classes referenced from C++
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

# Oboe may use reflection for AAudio/OpenSL ES backends
-keep class com.google.oboe.** { *; }
-dontwarn com.google.oboe.**

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# 📱 ANDROID COMPOSE - Prevent stripping UI components
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

# Keep Compose runtime classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Compose compiler annotations
-keepattributes *Annotation*

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# 🐛 DEBUGGING - Preserve stack traces for crash reports
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

# Keep source file names and line numbers for readable stack traces
-keepattributes SourceFile,LineNumberTable

# Keep exception messages
-keepattributes Exceptions

# Rename source file to hide original file names (security)
-renamesourcefileattribute SourceFile

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# ⚡ OPTIMIZATION - Aggressive bytecode optimization
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

# Allow R8 to optimize method calls
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Inline short methods for performance
-optimizationpasses 5

# Repackage classes into single package (smaller APK)
-repackageclasses ''

# Allow aggressive class/member removal
-allowaccessmodification