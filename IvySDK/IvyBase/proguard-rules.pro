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
#
#-dontwarn java.lang.invoke.StringConcatFactory

## 保持所有的接口不被混淆
-keep public interface * {
    public *;
}

# 保持所有抽象类不被混淆
-keep public abstract class * {
    public *;
}

# 保持所有枚举类不被混淆
-keep public enum * {
    public *;
}

-keep class com.ivy.sdk.** { *; }
-keep interface com.ivy.sdk.** { *; }
-keep class ivy.data.analytics.** { *; }
-keep interface ivy.data.analytics.** { *; }
