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
import com.unity3d.player.UnityPlayer;

public class Unity {

    public static final int AD_FULL = 1;
    public static final int AD_VIDEO = 2;
    public static final int AD_BANNER = 3;
    public static final int AD_NATIVE = 5;
    public static final int AD_GIF_ICON = 6;
    public static final int AD_APPOPEN = 7;

    public static final String TRUE = "1";
    public static final String FALSE = "0";

    private static int getAdType(@Nullable AdType adType) throws Exception {
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

    public static void onCreate(Activity activity) {
        Builder builder = new Builder.Build().setAdListener(new IAdListener() {

            @Override
            public void onAdLoadSuccess(@Nullable AdType adType) {
                try {
                    int newAdType = getAdType(adType);
                    sendMessage("onAdLoaded", String.valueOf(newAdType));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            @Override
            public void onAdLoadFailure(@Nullable AdType adType, @Nullable String reason) {
                try {
                    int newAdType = getAdType(adType);
                    sendMessage("onAdLoadFailed", String.valueOf(newAdType));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            @Override
            public void onAdShowSuccess(@NonNull AdType adType, @NonNull String tag, int placement) {
                try {
                    int newAdType = getAdType(adType);
                    String params = newAdType + "|" + tag + "|" + placement;
                    sendMessage("onAdShowSuccess", params);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            @Override
            public void onAdClosed(@NonNull AdType adType, boolean gotReward, @NonNull String tag, int placement) {
                try {
                    int newAdType = getAdType(adType);
                    String params = newAdType + "|" + tag + "|" + placement;
                    sendMessage("onAdClosed", params);
                    if (adType == AdType.REWARDED) {
                        String rewardParams = (gotReward ? TRUE : FALSE) + "|" + tag + "|" + placement;
                        sendMessage("onAdRewardUser", rewardParams);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            @Override
            public void onAdClicked(@NonNull AdType adType, @NonNull String tag, int placement) {
                try {
                    int newAdType = getAdType(adType);
                    String params = newAdType + "|" + tag + "|" + placement;
                    sendMessage("onAdClicked", params);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            @Override
            public void onAdShowFailed(@NonNull AdType adType, @Nullable String reason, @NonNull String tag, int placement) {
                try {
                    int newAdType = getAdType(adType);
                    String params = newAdType + "|" + tag + "|" + placement;
                    sendMessage("onAdShowFailed", params);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }).setPurchaseListener(new IPurchaseResult() {
            @Override
            public void onShippingResult(@NonNull String merchantTransactionId, boolean status) {
                sendMessage("onShippingResult", merchantTransactionId + "|" + (status ? TRUE : FALSE));
            }

            @Override
            public void payResult(int payId, int status, @Nullable String payload, @Nullable String merchantTransactionId) {
                if (status == IPurchaseResult.PAY_SUCCEED) {
                    if (payload == null) {
                        String params = payId + "|" + (merchantTransactionId == null ? "" : merchantTransactionId);
                        sendMessage("onPaymentSuccess", params);
                    } else {
                        String params = payId + "|" + payload + "|" + (merchantTransactionId == null ? "" : merchantTransactionId);
                        sendMessage("onPaymentSuccessWithPayload", params);
                    }
                } else {
                    String params = payId + "|" + (merchantTransactionId == null ? "" : merchantTransactionId);
                    sendMessage("onPaymentFail", params);
                }
            }

            @Override
            public void onStoreInitialized(boolean initState) {
                if (initState) {
                    sendMessage("onPaymentSystemValid", "");
                } else {
                    sendMessage("onPaymentSystemError", "");
                }
            }
        }).setCustomerListener(new IHelperCallback() {
            @Override
            public void onUnreadHelperMessageCount(int count) {
                sendMessage("unreadHelperMsgCount", String.valueOf(count));
            }
        }).setAuthListener(new IAuthResult() {
            @Override
            public void onLoginResult(@NonNull String platform, boolean status, String channel, @Nullable String reason) {
                if (status) {
                    sendMessage("onLoginSuccess", platform);
                } else {
                    sendMessage("onLoginFailed", platform + "|" + (reason == null ? "" : reason));
                }
            }

            @Override
            public void onLogout(@NonNull String platform) {
                sendMessage("onLogout", platform);
            }
        }).setNotificationEventListener(new INotificationEvent() {
            @Override
            public void onReceivedNotificationAction(@NonNull String action) {
                sendMessage("onReceivedNotifyAction", action);
            }
        }).build();
        AndroidSdk.onCreate(activity, builder);
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
        return AndroidSdk.getIvyRemoteConfigString(key);
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

    public static boolean isPlayGamesLoggedIn() {
        return AndroidSdk.hasPlayGamesSigned();
    }

    public static void loginPlayGames() {
        AndroidSdk.signPlayGames();
    }

    public static void logoutPlayGames() {
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

    public static void logInFacebook() {
        AndroidSdk.signFacebook();
    }

    public static void logoutFacebook() {
        AndroidSdk.signOutFacebook();
    }

    public static boolean isFacebookLoggedIn() {
        return AndroidSdk.hasFacebookSigned();
    }

    public static String getFacebookFriends() {
        return AndroidSdk.getFacebookFriends();
    }

    public static String getFacebookUserInfo() {
        return AndroidSdk.getFacebookUserInfo();
    }

    public static void logoutFirebase() {
        AndroidSdk.signOutFirebase();
    }

    public static String getFirebaseUserInfo(String channel) {
        return AndroidSdk.getFirebaseUserInfo(channel);
    }

    public static String getFirebaseUserId() {
        return AndroidSdk.getFirebaseUserId();
    }

    public static boolean isFirebaseAnonymousLoggedIn() {
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
                if (status) {
                    sendMessage("onFirebaseUnlinkSuccess", unlinkChannel);
                } else {
                    sendMessage("onFirebaseUnlinkFailed", unlinkChannel + "|" + (reason == null ? "" : reason));
                }
            }
        });
    }

    public static void reloadFirebaseLogStatus() {
        AndroidSdk.reloadFirebaseLastSign(new IFirebaseAuthReload() {
            @Override
            public void onReload(boolean status, @Nullable String reason) {
                sendMessage("onReloadFBLoginStatus", (status ? TRUE : FALSE) + "|" + (reason == null ? "" : reason));
            }
        });
    }

    public static void loginFBWithAnonymous() {
        AndroidSdk.signAnonymous(new IAuthResponse() {

            @Override
            public void onLoginResult(@NonNull String platform, boolean status, String channel, @Nullable String reason) {
                sendMessage("onFirebaseLoginResult", (channel == null ? "" : channel) + "|" + (status ? TRUE : FALSE) + "|" + (reason == null ? "" : reason));
            }
        });
    }

    public static void loginFBWithPlayGames() {
        AndroidSdk.signWithPlayGames(new IAuthResponse() {

            @Override
            public void onLoginResult(@NonNull String platform, boolean status, String channel, @Nullable String reason) {
                sendMessage("onFirebaseLoginResult", (channel == null ? "" : channel) + "|" + (status ? TRUE : FALSE) + "|" + (reason == null ? "" : reason));
            }
        });
    }

    public static void loginFBWithFacebook() {
        AndroidSdk.signWithFacebook(new IAuthResponse() {

            @Override
            public void onLoginResult(@NonNull String platform, boolean status, String channel, @Nullable String reason) {
                sendMessage("onFirebaseLoginResult", (channel == null ? "" : channel) + "|" + (status ? TRUE : FALSE) + "|" + (reason == null ? "" : reason));
            }
        });
    }

    public static void loginFBWithEmailAndPwd(String email, String password) {
        AndroidSdk.signWithEmailAndPassword(email, password, new IAuthResponse() {

            @Override
            public void onLoginResult(@NonNull String platform, boolean status, String channel, @Nullable String reason) {
                sendMessage("onFirebaseLoginResult", (channel == null ? "" : channel) + "|" + (status ? TRUE : FALSE) + "|" + (reason == null ? "" : reason));
            }
        });
    }

    //存档
    public static void saveCloudData(String collection, String jsonData) {
        AndroidSdk.setArchive(collection, jsonData, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {
                sendMessage("onCDSaveSuccess", collection);
            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {
                sendMessage("onCDSaveFailed", collection);
            }
        });
    }

    public static void readCloudData(String collection) {
        AndroidSdk.readArchive(collection, null, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {
                sendMessage("onCDReadSuccess", collection + "|" + (document == null ? "" : document) + "|" + (data == null ? "" : data));
            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {
                sendMessage("onCDReadFailed", collection + "|" + (document == null ? "" : document));
            }
        });
    }

    public static void readCloudData(String collection, String documentId) {
        AndroidSdk.readArchive(collection, documentId, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {
                sendMessage("onCDReadSuccess", collection + "|" + (document == null ? "" : document) + "|" + (data == null ? "" : data));
            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {
                sendMessage("onCDReadFailed", collection + "|" + (document == null ? "" : document));
            }
        });
    }

    public static void mergeCloudData(String collection, String jsonData) {
        AndroidSdk.mergeArchive(collection, jsonData, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {
                sendMessage("onCDMergeSuccess", collection);
            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {
                sendMessage("onCDMergeFailed", collection);
            }
        });
    }

    public static void queryCloudData(String collection) {
        AndroidSdk.queryArchive(collection, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {
                sendMessage("onCDQuerySuccess", collection + "|" + (data == null ? "" : data));
            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {
                sendMessage("onCDQueryFailed", collection);
            }
        });
    }

    public static void deleteCloudData(String collection) {
        AndroidSdk.deleteArchive(collection, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {
                sendMessage("onCDDeleteSuccess", collection);
            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {
                sendMessage("onCDDeleteFailed", collection);
            }
        });
    }

    public static void updateCloudData(String collection, String transactionId, String jsonData) {
        AndroidSdk.updateArchive(collection, jsonData, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {
                sendMessage("onCDUpdateSuccess", collection + "|" + transactionId);
            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {
                sendMessage("onCDUpdateFailed", collection + "|" + transactionId);
            }
        });
    }

    public static void snapshotCloudData(String collection) {
        AndroidSdk.snapshotArchive(collection, null, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {
                sendMessage("onCDSnapshotSuccess", collection + "|" + (document == null ? "" : document));
            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {
                sendMessage("onCDSnapshotFailed", collection + "|" + (document == null ? "" : document));
            }
        });
    }

    public static void snapshotCloudData(String collection, String documentId) {
        AndroidSdk.snapshotArchive(collection, documentId, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {
                sendMessage("onCDSnapshotSuccess", collection + "|" + (document == null ? "" : document));
            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {
                sendMessage("onCDSnapshotFailed", collection + "|" + (document == null ? "" : document));
            }
        });
    }
    //存档
    //game services

    //客服 helper
    public static boolean isHelperInitialized() {
        return AndroidSdk.isCustomerInitialized();
    }

    public static boolean hasNewHelperMessage() {
        return AndroidSdk.hasNewMessage();
    }

    public static void showHelper(String entranceId, String meta, String tags, String welcomeMessage) {
        AndroidSdk.showCustomer(entranceId, meta, tags, welcomeMessage);
    }

    public static void showHelperSingleFAQ(String faqId, int moment) {
        AndroidSdk.showSingleFAQ(faqId, moment);
    }

    public static void listenHelperUnreadMsgCount(boolean onlyOnce) {
        AndroidSdk.listenUnreadMessageCount(onlyOnce);
    }

    public static void stopListenHelperUnreadMsgCount() {
        AndroidSdk.stopListenUnreadMessageCount();
    }

    public static void updateHelperUserInfo(String data, String tags) {
        AndroidSdk.updateCustomerUserInfo(data, tags);
    }

    public static void resetHelperUserInfo() {
        AndroidSdk.resetCustomerUserInfo();
    }

    public static void closeHelper() {
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

    /**
     * @param tag             任务 id
     * @param title           通知栏标题
     * @param subtitle        通知栏副标题
     * @param bigText         长文本
     * @param smallIcon       小图标
     * @param largeIcon       大图标
     * @param bigPicture      大图
     * @param delay           延迟时间
     * @param autoCancel      可关闭
     * @param action          通知栏点击事件行为
     * @param repeat          重复触发通知栏
     * @param requireNetwork  要求联网状态展示通知栏
     * @param requireCharging 要求充电状态展示通知栏
     */
    public static void pushNotificationTask(String tag, String title, String subtitle, String bigText, String smallIcon, String largeIcon, String bigPicture, long delay, boolean autoCancel, String action, boolean repeat, boolean requireNetwork, boolean requireCharging) {
        AndroidSdk.pushNotificationTask(tag, title, subtitle, bigText, smallIcon, largeIcon, bigPicture, delay, autoCancel, action, repeat, requireNetwork, requireCharging);
    }

    public static void cancelNotification(String tag) {
        AndroidSdk.cancelNotification(tag);
    }
    //通知

    public static void appsflyerInviteUser(String inviterId, String inviterAppId) {
        AndroidSdk.appsflyerInviteUser(inviterId, inviterAppId);
    }

    public static String getAppsflyerInviterId() {
        return AndroidSdk.getAppsflyerInviterId();
    }


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

    private static void sendMessage(String method, String data) {
        try {
            ILog.Companion.e("Unity", "send message:");
            ILog.Companion.e("Unity", "method:" + method);
            ILog.Companion.e("Unity", "data:" + data);
            UnityPlayer.UnitySendMessage("RiseSdkListener", method, data);
        } catch (Exception e) {
            ILog.Companion.e("Unity", "send message err:" + e.getMessage());
            e.printStackTrace();
        }
    }


}
