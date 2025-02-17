package com.android.client;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ivy.sdk.base.ads.AdType;
import com.ivy.sdk.base.ads.IAdListener;
import com.ivy.sdk.base.billing.IPurchaseResult;
import com.ivy.sdk.base.game.archive.IArchiveResult;
import com.ivy.sdk.base.game.auth.IAuthResponse;
import com.ivy.sdk.base.game.auth.IAuthResult;
import com.ivy.sdk.base.game.auth.IFirebaseAuthReload;
import com.ivy.sdk.base.game.auth.IFirebaseUnlink;
import com.ivy.sdk.base.helper.IHelperCallback;
import com.ivy.sdk.base.notification.INotificationEvent;
import com.ivy.sdk.base.utils.ILog;
import com.ivy.sdk.core.Builder;

public class Cocos {

    //ad was clicked
    public static native void awc(int adType, String tag);

    //ad was closed(dismiss)
    public static native void awd(int adType, String tag);

    //ad shows success
    public static native void aws(int adType, String tag);

    // receive reward
    public static native void rr(boolean success, int id, String tag, boolean skip);

    // user center message
    public static native void sns(int msg, boolean success, int extra);

    public static native void lar(String tag, boolean success);

    // google result listener
    public static native void gr(int tag, boolean success);

    // home ad loading
    public static native void hal(boolean yes);

    //消费查询
    public static native void ph(String purchaseHistory);

    //订阅是否有效
    public static native void isa(boolean status);

    /**
     * @param bill
     * @param success 0: success , 1: cancel ,2:failure
     */
    public static native void pr(int bill, int success);

    // payment system is valid
    public static native void pv();

    // payment system error
    public static native void pe(String message);

    // bill prices and currency type
    // {"currency":"USD", "1":"0.99", "2":"1.99"}
    public static native void ps(String prices);

    // server result
    public static native void sr(int msg, boolean success, String extra);

    // cache url
    public static native void url(int tag, boolean success, String extra);

    public static native void nd(String data);

    public static native void networkstatus(boolean online);

    public static native void adloaded(int adtype);

    public static native void installreward(String extra);

    public static native void eventoccurred(String eventName);

    public static native void deeplink(String uri);

    // leader board
    public static native void lb(boolean isSubmit, boolean success, String leaderBoardId, String data);

    public static native void deliciouseiconclicked(String bannerLocalUrl, String appstoreUrl);

    public static native void wvsuccess();

    public static native void wvfail();

    public static native void wvclose();

    public static native void wvcall(String params);

    public static final int AD_FULL = 1;
    public static final int AD_VIDEO = 2;
    public static final int AD_BANNER = 3;
    public static final int AD_NATIVE = 5;
    public static final int AD_GIF_ICON = 6;
    public static final int AD_APPOPEN = 7;

    private static final String TAG = "cocos";

    public static void onCreate(Activity activity) {
        Builder builder = new Builder.Build().setAdListener(new IAdListener() {
                    @Override
                    public void onAdClosed(@NonNull AdType adType, boolean gotReward, @NonNull String tag, int placement) {
                        try {
                            int newAdType = getAdType(adType);
                            ILog.Companion.i(TAG, "ad closed callback from sdk:" + adType.getValue());
                            awd(newAdType, "default");
                            if (adType == AdType.REWARDED) {
                                rr(gotReward, placement, tag, !gotReward);
                                ILog.Companion.i(TAG, "ad reward user callback from sdk:" + adType.getValue());
                            }
                        } catch (Throwable t) {
                            ILog.Companion.i(TAG, "ad closed,but type not defined");
                        }
                    }

                    @Override
                    public void onAdClicked(@NonNull AdType adType, @NonNull String tag, int placement) {
                        try {
                            int newAdType = getAdType(adType);
                            ILog.Companion.i(TAG, "ad click success callback from sdk:" + adType.getValue());
                            awc(newAdType, "default");
                        } catch (Throwable t) {
                            ILog.Companion.i(TAG, "ad click success,but type not defined");
                        }
                    }

                    @Override
                    public void onAdShowFailed(@NonNull AdType adType, @Nullable String reason, @NonNull String tag, int placement) {
                        try {
                            if (adType == AdType.REWARDED) {
                                rr(false, placement, tag, false);
                                ILog.Companion.i(TAG, "ad reward user callback from sdk:" + adType.getValue());
                            }
                        } catch (Throwable t) {
                            ILog.Companion.i(TAG, "ad closed,but type not defined");
                        }
                    }

                    @Override
                    public void onAdShowSuccess(@NonNull AdType adType, @NonNull String tag, int placement) {
                        try {
                            int newAdType = getAdType(adType);
                            ILog.Companion.i(TAG, "ad show success callback from sdk:" + adType.getValue());
                            aws(newAdType, "default");
                        } catch (Throwable t) {
                            ILog.Companion.i(TAG, "ad show success,but type not defined");
                        }
                    }

                    @Override
                    public void onAdLoadSuccess(@Nullable AdType adType) {
                        try {
                            int newAdType = getAdType(adType);
                            ILog.Companion.i(TAG, "ad load success callback from sdk:" + (adType == null ? "" : adType.getValue()));
                            adloaded(newAdType);
                        } catch (Throwable t) {
                            ILog.Companion.i(TAG, "ad loaded,but type not defined");
                        }
                    }

                    @Override
                    public void onAdLoadFailure(@Nullable AdType adType, @Nullable String reason) {

                    }
                }).setPurchaseListener(new IPurchaseResult() {
                    @Override
                    public void onShippingResult(@NonNull String merchantTransactionId, boolean status) {

                    }

                    @Override
                    public void payResult(int payId, int status, @Nullable String payload, @Nullable String merchantTransactionId) {
                        if (status == IPurchaseResult.PAY_SUCCEED) {
                            pr(payId, 0);
                        } else {
                            pr(payId, 2);
                        }
                    }

                    @Override
                    public void onStoreInitialized(boolean initState) {
                        pv();
                    }
                }).setCustomerListener(new IHelperCallback() {
                    @Override
                    public void onUnreadHelperMessageCount(int count) {
                        //sendMessage("unreadMessageCount", String.valueOf(msgCount));
                    }
                }).setAuthListener(new IAuthResult() {

                    @Override
                    public void onLoginResult(@NonNull String platform, boolean status, @Nullable String channel, @Nullable String reason) {

                    }

                    @Override
                    public void onLogout(@NonNull String platform) {

                    }
                }).setNotificationEventListener(new INotificationEvent() {
                    @Override
                    public void onReceivedNotificationAction(@NonNull String action) {

                    }
                })
                .build();
        AndroidSdk.onCreate(activity, builder);
    }

    private static int getAdType(AdType adType) {
        int newAdType = 0;
        if (adType == AdType.INTERSTITIAL) {
            newAdType = AD_FULL;
        } else if (adType == AdType.REWARDED) {
            newAdType = AD_VIDEO;
        } else if (adType == AdType.BANNER) {
            newAdType = AD_BANNER;
        }
        return newAdType;
    }

    public static void onStart() {
        AndroidSdk.onStart();
    }

    public static void onResume(Activity activity) {
        AndroidSdk.onResume(activity);
    }

    public static void onPause() {
        AndroidSdk.onPause();
    }

    public static void onStop() {
        AndroidSdk.onStop();
    }

    public static void onDestroy() {
        AndroidSdk.onDestroy();
    }

    public static void onNewIntent(Intent intent) {
        AndroidSdk.onNewIntent(intent);
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        AndroidSdk.onActivityResult(requestCode, resultCode, data);
    }

    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        AndroidSdk.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // 广告
    public static boolean hasBannerAd() {
        return AndroidSdk.hasBannerAd();
    }

    /**
     *  banner 必须传递广告位，否则多banner展示无法操作指定广告位banner
     * @param tag
     * @param pos
     * @param placement
     */
    public static void showBannerAd(String tag, int pos, int placement) {
        showBannerAd(tag, pos, placement, null);
    }

    public static void showBannerAd(String tag, int pos, int placement, String clientInfo) {
        if (tag == null){
            tag = "default";
        }
        AndroidSdk.showBannerAd(tag, pos, placement, clientInfo);
    }

    /**
     * 根据广告位关闭banner
     * @param placement
     */
    public static void closeBannerAd(int placement) {
        AndroidSdk.closeBannerAd(placement);
    }

    public static boolean hasInterstitialAd() {
        return AndroidSdk.hasInterstitialAd();
    }

    public static void showInterstitialAd(String tag) {
        showInterstitialAd(tag, 0);
    }

    public static void showInterstitialAd(String tag, int placement) {
        showInterstitialAd(tag, placement, null);
    }

    /**
     * @param tag        暂无作用
     * @param placement  广告位 id
     * @param clientInfo 客户端携带信息，要求格式为JSONObject
     */
    public static void showInterstitialAd(String tag, int placement, String clientInfo) {
        if (tag == null){
            tag = "default";
        }
        AndroidSdk.showInterstitialAd(tag, placement, clientInfo);
    }

    public static boolean hasRewardedAd() {
        return AndroidSdk.hasRewardedAd();
    }

    public static void showRewardedAd(String tag) {
        showRewardedAd(tag, 0);
    }

    public static void showRewardedAd(String tag, int placement) {
        showRewardedAd(tag, placement, null);
    }

    /**
     *
     * @param tag           暂无作用
     * @param placement     广告位 id
     * @param clientInfo    客户端携带信息，要求格式为JSONObject
     */
    public static void showRewardedAd(String tag, int placement, String clientInfo) {
        if (tag == null){
            tag = "default";
        }
        AndroidSdk.showRewardedAd(tag, placement, clientInfo);
    }
    // 广告

    //计费
    public static void pay(int id) {
        pay(id, null);
    }

    public static void pay(int id, String payload) {
        pay(id, payload, null);
    }

    public static void pay(int id, String payload, String clientInfo) {
        AndroidSdk.pay(id, payload, clientInfo);
    }

    public static void shippingGoods(String merchantTransactionId){
        AndroidSdk.shippingGoods(merchantTransactionId);
    }

    public static void queryPaymentOrders() {
        queryPaymentOrder(-1);
    }

    public static void queryPaymentOrder(int id) {
        AndroidSdk.query(id);
    }

    public static String getPaymentData(int id) {
        String result = AndroidSdk.getPurchaseInfo(id);
        return result;
    }

    public static String getPaymentDatas() {
        return AndroidSdk.getPurchaseInfo(-1);
    }

    public static boolean isPaymentValid() {
        return AndroidSdk.isPaymentValid();
    }
    //计费

    //统计
    public static void trackEvent(String event, String data) {
        AndroidSdk.trackEvent(event, data, 5);
    }

    public static void trackEventToConversion(String event, String data) {
        AndroidSdk.trackEvent(event, data, 5);
    }

    public static void trackEventToFirebase(String event, String data) {
        AndroidSdk.trackEvent(event, data, 1);
    }

    public static void trackEventToFacebook(String event, String data) {
        AndroidSdk.trackEvent(event, data, 2);
    }

    public static void trackEventToAppsflyer(String event, String data) {
        AndroidSdk.trackEvent(event, data, 3);
    }

    public static void trackEventToIvy(String event, String data) {
        AndroidSdk.trackEvent(event, data, 4);
    }

    public static void setUserProperty(String key, String value) {
        AndroidSdk.setUserProperty(key, value, 5);
    }

    public static void setUserPropertyToFirebase(String key, String value) {
        AndroidSdk.setUserProperty(key, value, 1);
    }

    public static void setUserPropertyToAppsflyer(String key, String value) {
        AndroidSdk.setUserProperty(key, value, 3);
    }

    public static void setUserPropertyToIvy(String key, String value) {
        AndroidSdk.setUserProperty(key, value, 4);
    }

    public static void setCustomUserId(String value) {
        setUserProperty("customer_user_id", value);
    }
    //统计

    //remote config
    public static int getRemoteConfigInt(String key) {
        return AndroidSdk.getRemoteConfigInt(key);
    }

    public static long getRemoteConfigLong(String key) {
        return AndroidSdk.getRemoteConfigLong(key);
    }

    public static double getRemoteConfigDouble(String key) {
        return AndroidSdk.getRemoteConfigDouble(key);
    }

    public static boolean getRemoteConfigBoolean(String key) {
        return AndroidSdk.getRemoteConfigBoolean(key);
    }

    public static String getRemoteConfigString(String key) {
        return AndroidSdk.getRemoteConfigString(key);
    }

    public static int getIvyRemoteConfigInt(String key) {
        return AndroidSdk.getIvyRemoteConfigInt(key);
    }

    public static String getIvyRemoteConfigString(String key) {
        String value = AndroidSdk.getIvyRemoteConfigString(key);
        ILog.Companion.i(TAG, "rc key=" + key + " value=" + value);
        return value;
    }

    public static double getIvyRemoteConfigDouble(String key) {
        return AndroidSdk.getIvyRemoteConfigDouble(key);
    }

    public static boolean getIvyRemoteConfigBoolean(String key) {
        return AndroidSdk.getIvyRemoteConfigBoolean(key);
    }

    public static long getIvyRemoteConfigLong(String key) {
        return AndroidSdk.getIvyRemoteConfigLong(key);
    }
    //remote config

    public static boolean hasPlayGamesSigned() {
        return AndroidSdk.hasPlayGamesSigned();
    }

    public static void signPlayGames() {
        AndroidSdk.signPlayGames();
    }

    public static void signOutPlayGames() {
        AndroidSdk.signOutPlayGames();
    }

    public static String getPlayGamesUserInfo() {
        return AndroidSdk.getPlayGamesUserInfo();
    }

    public static void unlockAchievement(String achievementId) {
        AndroidSdk.unlockAchievement(achievementId);
    }

    public static void increaseAchievement(String achievementId, int step) {
        AndroidSdk.increaseAchievement(achievementId, step);
    }

    public static void showAchievement() {
        AndroidSdk.showAchievement();
    }

    public static void showLeaderboards() {
        AndroidSdk.showLeaderboards();
    }

    public static void showLeaderboard(String leaderboardId) {
        AndroidSdk.showLeaderboard(leaderboardId);
    }

    public static void updateLeaderboard(String leaderboardId, long score) {
        AndroidSdk.updateLeaderboard(leaderboardId, score);
    }

    public static void signFacebook() {
        AndroidSdk.signFacebook();
    }

    public static void signOutFacebook() {
        AndroidSdk.signOutFacebook();
    }

    public static boolean hasFacebookSigned() {
        return AndroidSdk.hasFacebookSigned();
    }

    public static String getFacebookFriends() {
        return AndroidSdk.getFacebookFriends();
    }

    public static String getFacebookUserInfo() {
        return AndroidSdk.getFacebookUserInfo();
    }

    public static void signOutFirebase() {
        AndroidSdk.signOutFirebase();
    }

    public static String getFirebaseUserInfo(String channel) {
        return AndroidSdk.getFirebaseUserInfo(channel);
    }

    public static boolean isFirebaseAnonymousSign() {
        return AndroidSdk.isFirebaseAnonymousSign();
    }

    public static boolean isFirebaseLinkedWithChannel(String channel) {
        return AndroidSdk.isFirebaseLinkedWithChannel(channel);
    }

    public static boolean canFirebaseUnlinkWithChannel(String channel) {
        return AndroidSdk.canFirebaseUnlinkWithChannel(channel);
    }

    public static void unlinkFirebaseWithChannel(String channel) {
        AndroidSdk.unlinkFirebaseWithChannel(channel, new IFirebaseUnlink() {
            @Override
            public void onUnlinked(@NonNull String unlinkChannel, boolean status, @Nullable String reason) {
                ILog.Companion.i(TAG, "unlink result: " + status + ";channel:" + channel + ";reason:" + (reason == null ? "" : reason));
            }
        });
    }

    public static void reloadFirebaseLastSign() {
        AndroidSdk.reloadFirebaseLastSign(new IFirebaseAuthReload() {
            @Override
            public void onReload(boolean status, @Nullable String reason) {
                ILog.Companion.i(TAG, "reload firebase sign status: " + status + " ;reason:" + (reason == null ? "" : reason));
            }
        });
    }

    public static void signAnonymous() {
        AndroidSdk.signAnonymous(new IAuthResponse() {

            @Override
            public void onLoginResult(@NonNull String platform, boolean status, @Nullable String channel, @Nullable String reason) {
                ILog.Companion.i(TAG, "signAnonymous status: " + status + " ;reason:" + (reason == null ? "" : reason));
            }

        });
    }

    public static void signWithPlayGames() {
        AndroidSdk.signWithPlayGames(new IAuthResponse() {

            @Override
            public void onLoginResult(@NonNull String platform, boolean status, String channel, @Nullable String reason) {
                ILog.Companion.i(TAG, "signWithPlayGames status: " + status + " ;reason:" + (reason == null ? "" : reason));
            }
        });
    }

    public static void signWithFacebook() {
        AndroidSdk.signWithFacebook(new IAuthResponse() {

            @Override
            public void onLoginResult(@NonNull String platform, boolean status, String channel, @Nullable String reason) {
                ILog.Companion.i(TAG, "signWithFacebook status: " + status + " ;reason:" + (reason == null ? "" : reason));
            }
        });
    }

    public static void signWithEmailAndPassword(String email, String password) {
        AndroidSdk.signWithEmailAndPassword(email, password, new IAuthResponse() {

            @Override
            public void onLoginResult(@NonNull String platform, boolean status, String channel, @Nullable String reason) {
                ILog.Companion.i(TAG, "signWithEmailAndPassword status: " + status + " ;reason:" + (reason == null ? "" : reason));
            }
        });
    }

    //存档
    public static void setArchive(String collection, String jsonData) {
        AndroidSdk.setArchive(collection, jsonData, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {

            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {

            }
        });
    }

    public static void readArchive(String collection, String documentId) {
        AndroidSdk.readArchive(collection, documentId, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {

            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {

            }
        });
    }

    public static void mergeArchive(String collection, String jsonData) {
        AndroidSdk.mergeArchive(collection, jsonData, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {

            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {

            }
        });
    }

    public static void queryArchive(String collection) {
        AndroidSdk.queryArchive(collection, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {

            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {

            }
        });
    }

    public static void deleteArchive(String collection) {
        AndroidSdk.deleteArchive(collection, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {

            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {

            }
        });
    }

    public static void updateArchive(String collection, String jsonData) {
        AndroidSdk.updateArchive(collection, jsonData, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {

            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {

            }
        });
    }

    public static void snapshotArchive(String collection, String documentId) {
        AndroidSdk.snapshotArchive(collection, documentId, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {

            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {

            }
        });
    }
    //存档
    //game services

    //客服
    public static boolean isCustomerInitialized() {
        return AndroidSdk.isCustomerInitialized();
    }

    public static boolean hasNewMessage() {
        return AndroidSdk.hasNewMessage();
    }

    public static void showCustomer(String entranceId, String meta, String tags, String welcomeMessag) {
        AndroidSdk.showCustomer(entranceId, meta, tags, welcomeMessag);
    }

    public static void showSingleFAQ(String faqId, int moment) {
        AndroidSdk.showSingleFAQ(faqId, moment);
    }

    public static void listenUnreadMessageCount(boolean onlyOnce) {
        AndroidSdk.listenUnreadMessageCount(onlyOnce);
    }

    public static void stopListenUnreadMessageCount() {
        AndroidSdk.stopListenUnreadMessageCount();
    }

    public static void updateCustomerUserInfo(String data, String tags) {
        AndroidSdk.updateCustomerUserInfo(data, tags);
    }

    public static void resetCustomerUserInfo() {
        AndroidSdk.resetCustomerUserInfo();
    }

    public static void closeCustomer() {
        AndroidSdk.closeCustomer();
    }

    //客服

    //通知
    public static int loadNotificationPermissionState() {
        return AndroidSdk.loadNotificationPermissionState();
    }

    public static void requestNotificationPermission() {
        AndroidSdk.requestNotificationPermission();
    }

    public static void openNotificationSettings() {
        AndroidSdk.openNotificationSettings();
    }

    public static void pushNotificationTask(String tag, String title, String subtitle, String bigText, String smallIcon, String largeIcon,
                                            String bigPicture, long delay, boolean autoCancel, String action, boolean repeat,
                                            boolean requireNetwork, boolean requireCharging) {
        AndroidSdk.pushNotificationTask(tag, title, subtitle, bigText, smallIcon, largeIcon, bigPicture, delay, autoCancel, action, repeat, requireNetwork, requireCharging);
    }

    public static void cancelNotification(String tag) {
        AndroidSdk.cancelNotification(tag);
    }
    //通知

    //其它
    public static String getConfig(int configKey) {
        return AndroidSdk.getConfig(configKey);
    }

    public static boolean isNetworkConnected() {
        return AndroidSdk.isNetworkConnected();
    }

    public static void rate() {
        AndroidSdk.rateUs();
    }

    public static void openAppStore(String url, String referrer) {
        AndroidSdk.openAppStore(url, referrer);
    }

    public static void openAppStore() {
        AndroidSdk.openAppStore(null, null);
    }

    public static void copyTxt(String txt) {
        AndroidSdk.copyTxt(txt);
    }

    public static int getTotalMemory() {
        return AndroidSdk.getTotalMemory();
    }

    public static int getFreeMemory() {
        return AndroidSdk.getFreeMemory();
    }

    public static int getDiskSize() {
        return AndroidSdk.getDiskSize();
    }

    public static int getFreeDiskSize() {
        return AndroidSdk.getFreeDiskSize();
    }

    public static void sendEmail(String email, String extra) {
        sendEmail(email, null, extra);
    }

    public static void sendEmail(String email, String title, String extra) {
        AndroidSdk.sendEmail(email, title, extra);
    }

    public static void systemShareText(String txt) {
        AndroidSdk.systemShareText(txt);
    }

    public static void systemShareImage(String title, String imagePath) {
        AndroidSdk.systemShareImage(title, imagePath);
    }

    public static void openUrl(String url) {
        AndroidSdk.openUrl(url);
    }

    public static boolean hasNotch() {
        return AndroidSdk.hasNotch();
    }

    public static int getNotchHeight() {
        return AndroidSdk.getNotchHeight();
    }

    public static void toast(String msg) {
        AndroidSdk.toast(msg);
    }


}
