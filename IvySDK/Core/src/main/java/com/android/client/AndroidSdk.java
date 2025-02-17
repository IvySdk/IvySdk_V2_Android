package com.android.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.ivy.sdk.base.game.archive.IArchiveResult;
import com.ivy.sdk.base.game.auth.IAuthResponse;
import com.ivy.sdk.base.game.auth.IFirebaseAuthReload;
import com.ivy.sdk.base.game.auth.IFirebaseUnlink;
import com.ivy.sdk.base.track.EventSrc;
import com.ivy.sdk.base.track.EventType;
import com.ivy.sdk.base.track.TrackPlatform;
import com.ivy.sdk.base.utils.ILog;
import com.ivy.sdk.base.utils.IToast;
import com.ivy.sdk.base.utils.Util;
import com.ivy.sdk.core.Builder;
import com.ivy.sdk.core.IvySdk;
import com.ivy.sdk.core.net.NetStateUtil;
import com.ivy.sdk.core.utils.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AndroidSdk {
    private static final String TAG = "";
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void onCreate(Activity activity, Builder builder) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().onCreate(activity, builder);
        });
    }

    public static void onStart() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().onStart();
        });
    }

    public static void onResume(Activity activity) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().onResume(activity);
        });
    }

    public static void onPause() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().onPause();
        });
    }

    public static void onStop() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().onStop();
        });
    }

    public static void onDestroy() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().onDestroy();
        });
    }

    public static void onNewIntent(Intent intent) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().onNewIntent(intent);
        });
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().onActivityResult(requestCode, resultCode, data);
        });
    }

    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
        });
    }

    // 广告
    public static boolean hasBannerAd() {
        return IvySdk.Companion.getInstance().hasBannerAd();
    }

    public static void showBannerAd(String tag, int pos, int placement, String clientInfo) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().showBannerAd(pos, tag, placement, clientInfo);
        });
    }

    public static void closeBannerAd(int placement) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().closeBannerAd(placement);
        });
    }

    public static boolean hasInterstitialAd() {
        return IvySdk.Companion.getInstance().hasInterstitialAd();
    }

    public static void showInterstitialAd(String tag, int placement, String clientInfo) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().showInterstitialAd(tag, placement, clientInfo);
        });
    }

    public static boolean hasRewardedAd() {
        return IvySdk.Companion.getInstance().hasRewardedAd();
    }

    public static void showRewardedAd(String tag, int placement, String clientInfo) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().showRewardedAd(tag, placement, clientInfo);
        });
    }
    // 广告

    //计费
    public static void pay(int id, String payload, String clientInfo) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().pay(id, payload, clientInfo);
        });
    }

    public static void shippingGoods(String merchantTransactionId) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().shippingGoods(merchantTransactionId);
        });
    }

    public static void query(int id) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().queryPurchase(id);
        });
    }

    public static String getPurchaseInfo(int id) {
        return IvySdk.Companion.getInstance().getGoodsInfo(id);
    }

    public static boolean isPaymentValid() {
        return IvySdk.Companion.getInstance().isPaymentInitialized();
    }
    //计费

    //统计
    public static void trackEvent(String event, String data, int platform) {
        runUiThread(() -> {
            Map<String, Object> params = new HashMap<>();
            if (TextUtils.isEmpty(data)) {
                params = null;
            } else {
                try {
                    params = new HashMap<>();
                    String[] args = data.split(",");
                    if (args.length > 0 && args.length % 2 == 0) {
                        for (int i = 0; i < args.length; i += 2) {
                            String key = args[i];
                            Object value = args[i + 1];
                            params.put(key, value);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    params = null;
                }
            }
            List<TrackPlatform> platforms = new ArrayList<>();
            if (platform == 5) {
                platforms.add(TrackPlatform.FIREBASE);
                platforms.add(TrackPlatform.APPSFLYER);
                platforms.add(TrackPlatform.FACEBOOK);
                platforms.add(TrackPlatform.THINkING_DATA);
            } else if (platform == 4) {
                platforms.add(TrackPlatform.THINkING_DATA);
            } else if (platform == 3) {
                platforms.add(TrackPlatform.APPSFLYER);
            } else if (platform == 2) {
                platforms.add(TrackPlatform.FACEBOOK);
            } else if (platform == 1) {
                platforms.add(TrackPlatform.FIREBASE);
            }
            IvySdk.Companion.getInstance().logEvent(event, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_CLIENT, params, platforms);
        });
    }

    public static void setUserProperty(String key, String value, int platform) {
        runUiThread(() -> {
            if (platform == 5) {
                IvySdk.Companion.getInstance().setUserProperty(key, value, null);
            } else if (platform == 4) {
                IvySdk.Companion.getInstance().setUserProperty(key, value, TrackPlatform.THINkING_DATA);
            } else if (platform == 3) {
                IvySdk.Companion.getInstance().setUserProperty(key, value, TrackPlatform.APPSFLYER);
            } else if (platform == 2) {
                IvySdk.Companion.getInstance().setUserProperty(key, value, TrackPlatform.FACEBOOK);
            } else if (platform == 1) {
                IvySdk.Companion.getInstance().setUserProperty(key, value, TrackPlatform.FIREBASE);
            }
        });
    }

    //统计


    //remote config
    public static int getRemoteConfigInt(String key) {
        return (int) IvySdk.Companion.getInstance().getRemoteConfigLong(key);
    }

    public static long getRemoteConfigLong(String key) {
        return IvySdk.Companion.getInstance().getRemoteConfigLong(key);
    }

    public static double getRemoteConfigDouble(String key) {
        return IvySdk.Companion.getInstance().getRemoteConfigDouble(key);
    }

    public static boolean getRemoteConfigBoolean(String key) {
        return IvySdk.Companion.getInstance().getRemoteConfigBoolean(key);
    }

    public static String getRemoteConfigString(String key) {
        return IvySdk.Companion.getInstance().getRemoteConfigString(key);
    }

    public static int getIvyRemoteConfigInt(String key) {
        return IvySdk.Companion.getInstance().getIvyRemoteConfigInt(key);
    }

    public static String getIvyRemoteConfigString(String key) {
        return IvySdk.Companion.getInstance().getIvyRemoteConfigString(key);
    }

    public static double getIvyRemoteConfigDouble(String key) {
        return IvySdk.Companion.getInstance().getIvyRemoteConfigDouble(key);
    }

    public static boolean getIvyRemoteConfigBoolean(String key) {
        return IvySdk.Companion.getInstance().getIvyRemoteConfigBoolean(key);
    }

    public static long getIvyRemoteConfigLong(String key) {
        return IvySdk.Companion.getInstance().getIvyRemoteConfigLong(key);
    }

    //remote config

    //game services
    public static boolean hasPlayGamesSigned() {
        return IvySdk.Companion.getInstance().hasPlayGamesSigned();
    }

    public static void signPlayGames() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().loginPlayGames();
        });
    }

    public static void signOutPlayGames() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().logoutPlayGames();
        });
    }

    public static String getPlayGamesUserInfo() {
        return IvySdk.Companion.getInstance().getPlayGamesUserInfo();
    }

    public static void unlockAchievement(String achievementId) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().unlockAchievement(achievementId);
        });
    }

    public static void increaseAchievement(String achievementId, int step) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().increaseAchievement(achievementId, step);
        });
    }

    public static void showAchievement() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().showAchievement();
        });
    }

    public static void showLeaderboards() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().showLeaderboards();
        });
    }

    public static void showLeaderboard(String leaderboardId) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().showLeaderboard(leaderboardId);
        });
    }

    public static void updateLeaderboard(String leaderboardId, long score) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().updateLeaderboard(leaderboardId, score);
        });
    }

    public static void signFacebook() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().loginFacebook();
        });
    }

    public static void signOutFacebook() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().logoutFacebook();
        });
    }

    public static boolean hasFacebookSigned() {
        return IvySdk.Companion.getInstance().isFacebookLogged();
    }

    public static String getFacebookFriends() {
        return IvySdk.Companion.getInstance().getFacebookFriends();
    }

    public static String getFacebookUserInfo() {
        return IvySdk.Companion.getInstance().getFacebookUserInfo();
    }

    public static void signOutFirebase() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().logoutFirebase();
        });
    }

    public static String getFirebaseUserInfo(String channel) {
        return IvySdk.Companion.getInstance().getFirebaseUserInfo(channel);
    }

    public static String getFirebaseUserId(){
        return IvySdk.Companion.getInstance().getFirebaseUserId();
    }

    public static boolean isFirebaseAnonymousSign() {
        return IvySdk.Companion.getInstance().isFirebaseAnonymousLogged();
    }

    public static boolean isFirebaseLinkedWithChannel(String channel) {
        return IvySdk.Companion.getInstance().isFirebaseLinkedWithChannel(channel);
    }

    public static boolean canFirebaseUnlinkWithChannel(String channel) {
        return IvySdk.Companion.getInstance().canFirebaseUnlinkWithChannel(channel);
    }

    public static void unlinkFirebaseWithChannel(String channel, IFirebaseUnlink callback) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().unlinkFirebaseWithChannel(channel, callback);
        });
    }

    public static void reloadFirebaseLastSign(IFirebaseAuthReload authReload) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().reloadFirebaseLastSign(authReload);
        });
    }

    public static void signAnonymous(IAuthResponse authResult) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().loginAnonymous(authResult);
        });
    }

    public static void signWithPlayGames(IAuthResponse authResult) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().loginWithPlayGames(authResult);
        });
    }

    public static void signWithFacebook(IAuthResponse authResult) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().loginWithFacebook(authResult);
        });
    }

    public static void signWithEmailAndPassword(String email, String password, IAuthResponse authResult) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().loginWithEmailAndPassword(email, password, authResult);
        });
    }

    //存档
    public static void setArchive(String collection, String jsonData, IArchiveResult callback) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().setArchive(collection, jsonData, callback);
        });
    }

    public static void readArchive(String collection, String documentId, IArchiveResult callback) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().readArchive(collection, documentId, callback);
        });
    }

    public static void mergeArchive(String collection, String jsonData, IArchiveResult callback) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().mergeArchive(collection, jsonData, callback);
        });
    }

    public static void queryArchive(String collection, IArchiveResult callback) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().queryArchive(collection, callback);
        });
    }

    public static void deleteArchive(String collection, IArchiveResult callback) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().deleteArchive(collection, callback);
        });
    }

    public static void updateArchive(String collection, String jsonData, IArchiveResult callback) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().updateArchive(collection, jsonData, callback);
        });
    }

    public static void snapshotArchive(String collection, String documentId, IArchiveResult callback) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().snapshotArchive(collection, documentId, callback);
        });
    }
    //存档
    //game services

    //客服
    public static boolean isCustomerInitialized() {
        return IvySdk.Companion.getInstance().isHelperInitialized();
    }

    public static boolean hasNewMessage() {
        return IvySdk.Companion.getInstance().hasNewHelperMessage();
    }

    public static void showCustomer(String entranceId, String meta, String tags, String welcomeMessag) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().showHelper(entranceId, meta, tags, welcomeMessag);
        });
    }

    public static void showSingleFAQ(String faqId, int moment) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().showHelperSingleFAQ(faqId, moment);
        });
    }

    public static void listenUnreadMessageCount(boolean onlyOnce) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().listenHelperUnreadMessageCount(onlyOnce);
        });
    }

    public static void stopListenUnreadMessageCount() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().stopListenHelperUnreadMessageCount();
        });
    }

    public static void updateCustomerUserInfo(String data, String tags) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().updateHelperUserInfo(data, tags);
        });
    }

    public static void resetCustomerUserInfo() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().resetHelperUserInfo();
        });
    }

    public static void closeCustomer() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().closeHelper();
        });
    }

    //客服

    //其它
    public static String getConfig(int configKey) {
        String result = Helper.Companion.getConfig(configKey);
        if (result == null) return "";
        return result;
    }

    public static boolean isNetworkConnected() {
        return NetStateUtil.Companion.isNetworkConnected();
    }

    public static void rateUs() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().rateUs();
        });
    }

    public static void openAppStore(String url, String referrer) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().openAppStore(url, referrer);
        });
    }

    //通知
    public static int loadNotificationPermissionState() {
        return IvySdk.Companion.getInstance().loadNotificationPermissionState();
    }

    public static void requestNotificationPermission() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().requestNotificationPermission();
        });
    }

    public static void openNotificationSettings() {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().openNotificationSettings();
        });
    }

    public static void pushNotificationTask(String tag, String title, String subtitle, String bigText, String smallIcon, String largeIcon,
                                            String bigPicture, long delay, boolean autoCancel, String action, boolean repeat,
                                            boolean requireNetwork, boolean requireCharging) {
        if (TextUtils.isEmpty(tag)) {
            ILog.Companion.e(TAG, "invalid notification tag! cannot be null");
            return;
        }
        if (TextUtils.isEmpty(title)) {
            ILog.Companion.e(TAG, "invalid notification title! cannot be null");
            return;
        }
        runUiThread(() -> {
            IvySdk.Companion.getInstance().pushNotificationTask(tag, title, subtitle, bigText, smallIcon, largeIcon, bigPicture, delay, autoCancel, action, repeat, requireNetwork, requireCharging);
        });
    }


    public static void cancelNotification(String tag) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().cancelNotification(tag);
        });
    }
    //通知
    public static void appsflyerInviteUser(String inviterId, String inviterAppId) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().appsflyerInviteUser("android_user_invite", "user_invite", inviterId, inviterAppId);
        });
    }

    public static String getAppsflyerInviterId() {
        return IvySdk.Companion.getInstance().getAppsflyerInviterId();
    }

    //其它
    public static void copyTxt(String txt){
        runUiThread(() -> {
            IvySdk.Companion.getInstance().copyTxt(txt);
        });
    }

    public static int getTotalMemory() {
        return Util.INSTANCE.byte2MB(Util.INSTANCE.getTotalMemory());
    }

    public static int getFreeMemory() {
        return Util.INSTANCE.byte2MB(Util.INSTANCE.getFreeMemory());
    }

    public static int getDiskSize() {
        return Util.INSTANCE.byte2MB(Util.INSTANCE.getTotalDiskSize());
    }

    public static int getFreeDiskSize() {
        return Util.INSTANCE.byte2MB(Util.INSTANCE.getAvailableDiskSize());
    }

    public static void sendEmail(String email, String title, String extra) {
        runUiThread(() -> {
            IvySdk.Companion.getInstance().sendEmail(email, title, extra);
        });
    }

    public static void systemShareText(String txt) {
        runUiThread(() -> {
            AndroidSdk.systemShareText(txt);
        });
    }

    public static void systemShareImage(String title, String imagePath) {
        runUiThread(() -> {
            AndroidSdk.systemShareImage(title, imagePath);
        });
    }

    public static void openUrl(String url) {
        runUiThread(() -> {
            AndroidSdk.openUrl(url);
        });
    }

    public static boolean hasNotch() {
        return IvySdk.Companion.getInstance().hasNotch();
    }

    public static void displayInNotch(Activity activity) {
        IvySdk.Companion.getInstance().displayInNotch(activity);
    }

    public static int getNotchHeight() {
        return IvySdk.Companion.getInstance().getNotchHeight();
    }

    public static void toast(String msg) {
        if (msg != null) {
            runUiThread(() -> {
                IToast.Companion.toastAnyway(msg);
            });
        }
    }

    private static void runUiThread(Runnable runnable) {
        handler.post(runnable);
    }


}
