# aop
-keep class ivy.data.analytics.aop.** { *; }

-keep class ivy.data.analytics.encrypt.TDSecreteKey { *; }

-keep public interface ivy.data.analytics.ScreenAutoTracker { *; }
-keep public interface ivy.data.analytics.crash.CrashLogListener { *; }
-keep public class ivy.data.analytics.TDConfig { *; }
-keep public class ivy.data.analytics.utils.TASensitiveInfo { *; }
-keep public class ivy.data.analytics.TDConfig$TDMode { *; }
-keep public class ivy.data.analytics.TDConfig$ModeEnum { *; }
-keep public class ivy.data.analytics.TDConfig$NetworkType { *; }
-keep public class ivy.data.analytics.TDConfig$TDDNSService { *; }

-keep public class ivy.data.analytics.TDFirstEvent { *; }
-keep public class ivy.data.analytics.TDOverWritableEvent { *; }
-keep public class ivy.data.analytics.TDUpdatableEvent { *; }
-keep public class ivy.data.analytics.TDEventModel { *; }
-keep public class ivy.data.analytics.ThinkingAnalyticsEvent { *; }
-keep public class ivy.data.analytics.BuildConfig { *; }
-keep class ivy.data.analytics.model.** { *; }

-keep public class ivy.data.analytics.TDWebAppInterface { *; }

-keep public class ivy.data.analytics.ThinkingAdapterViewItemTrackProperties { *; }
-keep public class ivy.data.analytics.ThinkingDataAutoTrackAppViewScreenUrl { *; }
-keep public class ivy.data.analytics.ThinkingDataFragmentTitle { *; }
-keep public class ivy.data.analytics.ThinkingDataIgnoreTrackAppClick { *; }
-keep public class ivy.data.analytics.ThinkingDataIgnoreTrackAppViewScreen { *; }
-keep public class ivy.data.analytics.ThinkingDataIgnoreTrackAppViewScreenAndAppClick { *; }
-keep public class ivy.data.analytics.ThinkingDataIgnoreTrackOnClick { *; }
-keep public class ivy.data.analytics.ThinkingDataTrackEvent { *; }
-keep public class ivy.data.analytics.ThinkingDataTrackViewOnClick { *; }
-keep public class ivy.data.analytics.ThinkingExpandableListViewItemTrackProperties { *; }

-keep public class ivy.data.analytics.ThinkingDataRuntimeBridge { *; }

-keep public class ivy.data.analytics.TDPresetProperties { *; }

-keep public class ivy.data.analytics.ThinkingAnalyticsSDK { *; }
-keep public class ivy.data.analytics.ThinkingAnalyticsSDK$ThinkingdataNetworkType { *; }
-keep public class ivy.data.analytics.ThinkingAnalyticsSDK$DynamicSuperPropertiesTracker{ *; }
-keep public class ivy.data.analytics.ThinkingAnalyticsSDK$TATrackStatus{ *; }
-keep public class ivy.data.analytics.ThinkingAnalyticsSDK$AutoTrackEventType{ *; }
-keep public class ivy.data.analytics.ThinkingAnalyticsSDK$AutoTrackEventListener{ *; }
-keep public class ivy.data.analytics.ThinkingAnalyticsPlugin { *; }
-dontwarn ivy.data.analytics.ThinkingAnalyticsPlugin.**
-keep class ivy.data.module.routes.** { *; }

-keep public class ivy.data.analytics.TDAnalytics { *; }
-keep public class ivy.data.analytics.TDAnalytics$TDAutoTrackEventType { *; }
-keep public class ivy.data.analytics.TDAnalytics$TDAutoTrackEventHandler { *; }
-keep public class ivy.data.analytics.TDAnalytics$TDNetworkType { *; }
-keep public class ivy.data.analytics.TDAnalytics$TDTrackStatus { *; }
-keep public class ivy.data.analytics.TDAnalytics$TDDynamicSuperPropertiesHandler { *; }
-keep public class ivy.data.analytics.TDAnalyticsAPI { *; }

-keep class ivy.data.analytics.R$* {
    <fields>;
}
-keep public class * extends android.content.ContentProvider
-keepnames class * extends android.view.View

-dontwarn org.json.**
-keep class org.json.**{*;}


# AlertDialog
-keep class android.app.AlertDialog {*;}
-keep class android.support.v7.app.AlertDialog {*;}
-keep class androidx.appcompat.app.AlertDialog {*;}
-keep class * extends android.support.v7.app.AlertDialog {*;}
-keep class * extends androidx.appcompat.app.AlertDialog {*;}
-keep class * extends android.app.AlertDialog {*;}

# Fragment
-keep class android.app.Fragment {*;}
-keep class android.support.v4.app.Fragment {*;}
-keep class androidx.fragment.app.Fragment {*;}
-keepclassmembers class * extends androidx.fragment.app.Fragment {
    public void setUserVisibleHint(boolean);
    public void onViewCreated(android.view.View, android.os.Bundle);
    public void onHiddenChanged(boolean);
    public void onResume();
}
-keepclassmembers class * extends android.app.Fragment {
    public void setUserVisibleHint(boolean);
    public void onViewCreated(android.view.View, android.os.Bundle);
    public void onHiddenChanged(boolean);
    public void onResume();
}
-keepclassmembers class * extends android.support.v4.app.Fragment {
    public void setUserVisibleHint(boolean);
    public void onViewCreated(android.view.View, android.os.Bundle);
    public void onHiddenChanged(boolean);
    public void onResume();
}


# TabLayout
-keep class android.support.design.widget.TabLayout$Tab {*;}
-keep class com.google.android.material.tabs.TabLayout$Tab {*;}
-keep class * extends android.support.design.widget.TabLayout$Tab {*;}
-keep class * extends com.google.android.material.tabs.TabLayout$Tab {*;}

# ViewPager
-keep class android.support.v4.view.ViewPager {*;}
-keep class android.support.v4.view.PagerAdapter {*;}
-keep class androidx.viewpager.widget.ViewPager {*;}
-keep class androidx.viewpager.widget.PagerAdapter {*;}
-keep class * extends android.support.v4.view.ViewPager {*;}
-keep class * extends android.support.v4.view.PagerAdapter {*;}
-keep class * extends androidx.viewpager.widget.ViewPager {*;}
-keep class * extends androidx.viewpager.widget.PagerAdapter {*;}

# SwitchCompat
-keep class android.support.v7.widget.SwitchCompat {*;}
-keep class androidx.appcompat.widget.SwitchCompat {*;}
-keep class * extends android.support.v7.widget.SwitchCompat {*;}
-keep class * extends androidx.appcompat.widget.SwitchCompat {*;}

# ContextCompat
-keep class android.support.v4.content.ContextCompat {*;}
-keep class androidx.core.content.ContextCompat {*;}
-keep class * extends android.support.v4.content.ContextCompat {*;}
-keep class * extends androidx.core.content.ContextCompat {*;}

# AppCompatActivity
-keep class android.support.v7.app.AppCompatActivity {
    public android.support.v7.app.ActionBar getSupportActionBar();
}
-keep class androidx.appcompat.app.AppCompatActivity {
    public androidx.appcompat.app.ActionBar getSupportActionBar();
}
-keep class * extends android.support.v7.app.AppCompatActivity {
    public android.support.v7.app.ActionBar getSupportActionBar();
}
-keep class * extends androidx.appcompat.app.AppCompatActivity {
    public androidx.appcompat.app.ActionBar getSupportActionBar();
}

#ActionBar
-keep class android.support.v7.app.ActionBar {*;}
-keep class androidx.appcompat.app.ActionBar {*;}
-keep class * extends android.support.v7.app.ActionBar {*;}
-keep class * extends androidx.appcompat.app.ActionBar {*;}

-keep class com.ivy.sdk.** { *; }
-keep interface com.ivy.sdk.** { *; }
-keep class ivy.data.analytics.** { *; }
-keep interface ivy.data.analytics.** { *; }
#-keep class ivy.data.analytics.TDAnalytics { *; }