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

#-keep class com.ivy.sdk.max.**{*;}
#-keep class com.ivy.sdk.max.MaxAdProvider {
#    public <init>(...);
#}
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-ignorewarnings
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-printmapping proguardMapping.txt
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-keepattributes *Annotation*,InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService

-keep class com.android.client.** {
    <methods>;
}
-keep class com.ivy.sdk.core.IvySdk.*{
    <methods>;
}
-keep class * extends com.ivy.sdk.base.ads.IAdProvider{
    public <init>(...);
}
-keep class * extends com.ivy.sdk.base.track.AbsTrack{
   public <init>(...);
}
-keep class * extends com.ivy.sdk.base.game.auth.IBaseAuth{
   public <init>(...);
}
-keep class * extends com.ivy.sdk.base.helper.ICustomer{
   public <init>(...);
}
-keep class * extends com.ivy.sdk.base.billing.AbsPurchase{
   public <init>(...);
}

-keep class com.unity3d.player.** { *; }

-keep class org.cocos2dx.** { *; }
-dontwarn org.cocos2dx.**
-keepclassmembers class org.cocos2dx.** { *;}

-keepclasseswithmembernames class * {
    native <methods>;
}


-keep class okhttp3.** { *; }
-keep class okio.** { *; }
#gogole billing
-keep class com.google.android.gms.** {*; }
-keep class com.android.billingclient.** {*; }
#appsflyer
-keep class com.appsflyer.** { *; }
-keep class kotlin.jvm.internal.** { *; }
-keep public class com.android.installreferrer.** { *; }
-keep public class com.miui.referrer.** {*;}
-keep class kotlin.jvm.internal.Intrinsics{ *; }
-keep class kotlin.collections.**{ *; }
#admob
-keep class com.google.android.gms.ads.identifier.** { *; }
#applovin
-dontwarn com.applovin.**
-keep class com.applovin.** { *; }
#AIHelp
-keep class net.aihelp.** {*;}
#yandex
-keep class com.yandex.div.** { *; }
-keep class com.yandex.div2.** { *; }
-keep class com.yandex.** { *; }
#mbridge
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mbridge.** {*; }
-keep interface com.mbridge.** {*; }
-dontwarn com.mbridge.**
-keepclassmembers class **.R$* { public static final int mbridge*; }
-keep class com.mbridge.msdk.foundation.tools.FastKV{*;}
-keep class com.mbridge.msdk.foundation.tools.FastKV$Builder{*;}
-keep public class com.mbridge.* extends androidx.** { *; }
#mmkv
-keepattributes *Annotation*
-keep class com.tencent.** { *; }
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}
-keep public class * {
    public protected *;
}
#chartboost
-keep class com.chartboost.** { *; }
-dontwarn com.chartboost.**
#inmobi
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**
#tapjoy
-dontwarn com.tapjoy.**
#ironsource
-keep class com.ironsource.** { *; }
-dontwarn com.ironsource.**
#vungle
-keep class com.vungle.** { *; }
-dontwarn com.vungle.**
#smaato
-keep public class com.smaato.sdk.** { *; }
-keep public interface com.smaato.sdk.** { *; }
#bytedance
-keep class com.bytedance.sdk.** { *; }
-keep class com.pgl.sys.ces.* {*;}

#
## 保持所有的接口不被混淆
#-keep public interface com.ivy.sdk.base.** {
#    public *;
#}
#
## 保持所有抽象类不被混淆
#-keep public abstract class com.ivy.sdk.base.** {
#    public *;
#}
#
## 保持所有枚举类不被混淆
#-keep public enum com.ivy.sdk.base.** {
#    public *;
#}

-keep class com.ivy.sdk.** { *; }
-keep interface com.ivy.sdk.** { *; }
-keep class ivy.data.analytics.** { *; }
-keep interface ivy.data.analytics.** { *; }