# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep smart-exception classes needed by ffmpeg-kit at runtime.
# Without these, FFmpegKitConfig crashes with NoClassDefFoundError
# when auto-stop triggers getRecordingInformation() → getBatchesForFFmpeg().
-keep class com.arthenica.smartexception.** { *; }

# ── kotlinx.serialization ──
# 防止 R8 移除 @Serializable 类的序列化器，导致 DataStore 读写静默失败。
# Without these, saveLastRecording() writes to DataStore fail silently,
# and the "Save & Exit" flow appears to do nothing.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class app.leo.alibi_cam.db.**$$serializer { *; }
-keepclassmembers class app.leo.alibi_cam.db.** {
    *** Companion;
}
-keep,includedescriptorclasses class app.leo.alibi_cam.db.RecordingInformation { *; }
-keep,includedescriptorclasses class app.leo.alibi_cam.db.AppSettings { *; }