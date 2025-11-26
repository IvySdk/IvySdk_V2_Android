using System;
using UnityEngine;

namespace com.ivy.sdk
{
    public class IvySdkListener : MonoBehaviour
    {

        private static IvySdkListener _instance;

        public static event Action<IvySdk.PaymentResult, int, string> OnPaymentEvent;
        public static event Action<IvySdk.PaymentResult, int, string, string> OnPaymentWithPayloadEvent;
        public static event Action<string, bool> OnPaymentShippingEvent;
#if UNITY_IOS
        public static event Action<int, bool> OnPaymentRestoreEvent;
#endif

        public static event Action<int> HelperUnreadMsgCountEvent;

        public static event Action OnAuthPlatformInitializeEvent; // 三方登录 平台初始化回调
        public static event Action<bool> OnPlayGamesLoginEvent;// 
        public static event Action<bool> OnGoogleLoginEvent;
        public static event Action<bool> OnGoogleLogoutEvent;
        public static event Action<bool> OnFacebookLoginEvent;
        public static event Action<bool> OnFacebookLogoutEvent;
        public static event Action<string, bool> OnFirebaseLoginEvent;


#if UNITY_IOS
        public static event Action<bool> OnAppleLoginEvent; // apple 登入
        public static event Action<bool> OnAppleLogoutEvent;//apple 登出
        public static event Action<bool> OnAppleLogStatusEvent;//apple 登录状态

        public static event Action<bool> OnFacebookLogStatusEvent;// facebook 登录状态
#endif

        public static event Action<string> OnReceivedNotificationEvent;

        /**
         * int 为广告位, 其中AD_LOADED、AD_LOAD_FAILED事件中无效
         */
        public static event Action<IvySdk.AdEvents, string> OnInterstitialAdEvent;
        public static event Action<IvySdk.AdEvents, string> OnRewardedAdEvent;
        public static event Action<IvySdk.AdEvents, string> OnBannerAdEvent;

        public static event Action<string, bool> OnFirebaseUnlinkEvent;

        public static event Action<string, string, bool> OnCloudDataSaveEvent;
        public static event Action<string, string, string, bool> OnCloudDataReadEvent;
        public static event Action<string, string, bool> OnCloudDataMergeEvent;
        public static event Action<string, string, string, bool> OnCloudDataQueryEvent;
        public static event Action<string, string, bool> OnCloudDataDeleteEvent;
        public static event Action<string, string, string, bool> OnCloudDataUpdateEvent;
        public static event Action<string, string, bool> OnCloudDataSnapshotEvent;

        //firebase cloud function
        // 方法名、返回值\失败原因、状态（成功\失败）
        public static event Action<string, string, bool> OnFirebaseCloudFunctionEvent;

        //Ab test 配置同步完成回调
        public static event Action OnAbTestConfigsSyncEvent;

        //remote data 同步完成回调
        public static event Action OnRemoteDataSyncEvent;

#if UNITY_IOS
        public static event Action<int> OnNotificationPermissionEvent; // 通知权限状态，0：已拒绝； 1：已允许；2：待申请
        public static event Action OnCustomATTRequestEvent;  //自定义UMP引导
        public static event Action OnCustomATTRequestEndEvent; //自定义UMP引导结束
        public static event Action<bool> OnATTRequestEvent; //自定义ATT引导结束
        public static event Action<bool> OnGameCenterAuthEvent; //GameCenter 登录回调
#endif

        public static IvySdkListener Instance
        {
            get
            {
                if (_instance == null)
                {
                    _instance = FindObjectOfType(typeof(IvySdkListener)) as IvySdkListener;
                    if (_instance == null)
                    {
                        var obj = new GameObject("IvySdkListener");
                        _instance = obj.AddComponent<IvySdkListener>();
                        DontDestroyOnLoad(obj);
                    }
                }
                return _instance;
            }
        }

        public void onRemoteConfigSynced()
        {
            if (OnRemoteDataSyncEvent != null && OnRemoteDataSyncEvent.GetInvocationList().Length > 0)
            {
                OnRemoteDataSyncEvent.Invoke();
            }
        }

        public void onAbTestConfigsSynced()
        {
            if (OnAbTestConfigsSyncEvent != null && OnAbTestConfigsSyncEvent.GetInvocationList().Length > 0)
            {
                OnAbTestConfigsSyncEvent.Invoke();
            }
        }

        #region 支付
        public void onPaymentSuccess(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    int payId = int.Parse(args[0]);
                    string merchantTransactionId = args[1];
                    if (OnPaymentEvent != null && OnPaymentEvent.GetInvocationList().Length > 0)
                    {
                        OnPaymentEvent.Invoke(IvySdk.PaymentResult.Success, payId, merchantTransactionId);
                    }
                }
            }
        }

        public void onPaymentSuccessWithPayload(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 3)
                {
                    int payId = int.Parse(args[0]);
                    string payload = args[1];
                    string merchantTransactionId = args[2];
                    if (OnPaymentWithPayloadEvent != null && OnPaymentWithPayloadEvent.GetInvocationList().Length > 0)
                    {
                        OnPaymentWithPayloadEvent.Invoke(IvySdk.PaymentResult.Success, payId, payload, merchantTransactionId);
                    }
                }
            }
        }

        public void onPaymentFail(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    int payId = int.Parse(args[0]);
                    string merchantTransactionId = args[1];
                    if (OnPaymentEvent != null && OnPaymentEvent.GetInvocationList().Length > 0)
                    {
                        OnPaymentEvent.Invoke(IvySdk.PaymentResult.Failed, payId, merchantTransactionId);
                    }
                }
            }
        }

        public void onShippingResult(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    int status = int.Parse(args[1]);
                    string merchantTransactionId = args[0];
                    if (OnPaymentShippingEvent != null && OnPaymentShippingEvent.GetInvocationList().Length > 0)
                    {
                        OnPaymentShippingEvent.Invoke(merchantTransactionId, status == 1);
                    }
                }
            }
        }

        public void onPaymentSystemValid(string data)
        {
            if (OnPaymentEvent != null && OnPaymentEvent.GetInvocationList().Length > 0)
            {
                OnPaymentEvent.Invoke(IvySdk.PaymentResult.PaymentSystemValid, 0, "");
            }
        }

        public void onPaymentSystemError(string data)
        {
            if (OnPaymentEvent != null && OnPaymentEvent.GetInvocationList().Length > 0)
            {
                OnPaymentEvent.Invoke(IvySdk.PaymentResult.PaymentSystemError, 0, "");
            }
        }

#if UNITY_IOS

        public void onRestoreResult(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    int payId = int.Parse(args[0]);
                    int status = int.Parse(args[1]);
                    if (OnPaymentRestoreEvent != null && OnPaymentRestoreEvent.GetInvocationList().Length > 0)
                    {
                        OnPaymentRestoreEvent.Invoke(payId, status == 1);
                    }
                }
            }
        }

#endif

    #endregion

        #region 客服
        public void unreadHelperMsgCount(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                try
                {
                    int msgCount = int.Parse(data);
                    if (HelperUnreadMsgCountEvent != null && HelperUnreadMsgCountEvent.GetInvocationList().Length > 0)
                    {
                        HelperUnreadMsgCountEvent.Invoke(msgCount);
                    }
                }
                catch { }
            }
        }
        #endregion

        #region 登陆
        public void onAuthPlatformsInitialized(string data)
        {
            if (OnAuthPlatformInitializeEvent != null && OnAuthPlatformInitializeEvent.GetInvocationList().Length > 0)
            {
                OnAuthPlatformInitializeEvent.Invoke();
            }
        }

        public void onLoginSuccess(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                try
                {
                    if (data.Equals("play_games"))
                    {
                        if (OnPlayGamesLoginEvent != null && OnPlayGamesLoginEvent.GetInvocationList().Length > 0)
                        {
                            OnPlayGamesLoginEvent.Invoke(true);
                        }
                    }
                    else if (data.Equals("facebook"))
                    {
                        if (OnFacebookLoginEvent != null && OnFacebookLoginEvent.GetInvocationList().Length > 0)
                        {
                            OnFacebookLoginEvent.Invoke(true);
                        }
                    } 
                    else if (data.Equals("google"))
                    {
                        if (OnGoogleLoginEvent != null && OnGoogleLoginEvent.GetInvocationList().Length > 0)
                        {
                            OnGoogleLoginEvent.Invoke(true);
                        }
                    } 
                    else if (data.Equals("apple"))
                    {
#if UNITY_IOS
                        if (OnAppleLoginEvent != null && OnAppleLoginEvent.GetInvocationList().Length > 0)
                        {
                            OnAppleLoginEvent.Invoke(true);
                        }
#endif
                    }
                }
                catch { }
            }
        }

        public void onLoginFailed(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                try
                {
                    string[] args = data.Split('|');
                    if (args != null && args.Length == 2)
                    {
                        string platform = args[0];
                        //string reason = args[1];
                        if (platform.Equals("play_games"))
                        {
                            if (OnPlayGamesLoginEvent != null && OnPlayGamesLoginEvent.GetInvocationList().Length > 0)
                            {
                                OnPlayGamesLoginEvent.Invoke(false);
                            }
                        }
                        else if (platform.Equals("facebook"))
                        {
                            if (OnFacebookLoginEvent != null && OnFacebookLoginEvent.GetInvocationList().Length > 0)
                            {
                                OnFacebookLoginEvent.Invoke(false);
                            }
                        }
                        else if (platform.Equals("google"))
                        {
                            if (OnGoogleLoginEvent != null && OnGoogleLoginEvent.GetInvocationList().Length > 0)
                            {
                                OnGoogleLoginEvent.Invoke(false);
                            }
                        }
                        else if (data.Equals("apple"))
                        {
#if UNITY_IOS
                            if (OnAppleLoginEvent != null && OnAppleLoginEvent.GetInvocationList().Length > 0)
                            {
                                OnAppleLoginEvent.Invoke(false);
                            }
#endif
                        }
                    }
                }
                catch { }
            }
        }

        public void onLogout(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                try
                {
                    if (data.Equals("facebook"))
                    {
                        if (OnFacebookLogoutEvent != null && OnFacebookLogoutEvent.GetInvocationList().Length > 0)
                        {
                            OnFacebookLogoutEvent.Invoke(true);
                        }
                    }
                    else if (data.Equals("google"))
                    {
                        if (OnGoogleLogoutEvent != null && OnGoogleLogoutEvent.GetInvocationList().Length > 0)
                        {
                            OnGoogleLogoutEvent.Invoke(true);
                        }
                    }
                    else if (data.Equals("apple"))
                    {
#if UNITY_IOS
                        if (OnAppleLogoutEvent != null && OnAppleLogoutEvent.GetInvocationList().Length > 0)
                        {
                            OnAppleLogoutEvent.Invoke(true);
                        }
#endif
                    }
                }
                catch { }
            }
        }

#if UNITY_IOS
        public void onLogStatusSuccess(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                try
                {
                    if (data.Equals("facebook"))
                    {
                        if (OnFacebookLogStatusEvent != null && OnFacebookLogStatusEvent.GetInvocationList().Length > 0)
                        {
                            OnFacebookLogStatusEvent.Invoke(true);
                        }
                    }
                    else if (data.Equals("apple"))
                    {
                        if (OnAppleLogStatusEvent != null && OnAppleLogStatusEvent.GetInvocationList().Length > 0)
                        {
                            OnAppleLogStatusEvent.Invoke(true);
                        }
                    }
                }
                catch { }
            }
        }

        public void onLogStatusFailed(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                try
                {
                    if (data.Equals("facebook"))
                    {
                        if (OnFacebookLogStatusEvent != null && OnFacebookLogStatusEvent.GetInvocationList().Length > 0)
                        {
                            OnFacebookLogStatusEvent.Invoke(false);
                        }
                    }
                    else if (data.Equals("apple"))
                    {
                        if (OnAppleLogStatusEvent != null && OnAppleLogStatusEvent.GetInvocationList().Length > 0)
                        {
                            OnAppleLogStatusEvent.Invoke(false);
                        }
                    }
                }
                catch { }
            }
        }
#endif

        #endregion

        #region 通知
#if UNITY_IOS
        public void onNotificationPermissionState(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                try
                {
                    int state = int.Parse(data);
                    if (OnNotificationPermissionEvent != null && OnNotificationPermissionEvent.GetInvocationList().Length > 0)
                    {
                        OnNotificationPermissionEvent.Invoke(state);
                    }
                }
                catch { }
            }
        }
#endif
        public void onReceivedNotifyAction(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                if (OnReceivedNotificationEvent != null && OnReceivedNotificationEvent.GetInvocationList().Length > 0)
                {
                    OnReceivedNotificationEvent.Invoke(data);
                }
            }
        }
        #endregion


#if UNITY_IOS
    #region ATT
       public void onCustomATTRequest(string data)
       {
            if (OnCustomATTRequestEvent != null && OnCustomATTRequestEvent.GetInvocationList().Length > 0)
            {
                OnCustomATTRequestEvent.Invoke();
            }
        }

        public void onCustomATTRequestEnd(string data)
        {
            if (OnCustomATTRequestEndEvent != null && OnCustomATTRequestEndEvent.GetInvocationList().Length > 0)
            {
                OnCustomATTRequestEndEvent.Invoke();
            }
        }

        public void onATTRequestResult(string data)
        {
            try
            {
                if (!string.IsNullOrEmpty(data))
                {
                    int arg = int.Parse(data);
                    if (OnATTRequestEvent != null && OnATTRequestEvent.GetInvocationList().Length > 0)
                    {
                        OnATTRequestEvent.Invoke(arg == 1);
                    }
                }
            }
            catch (Exception e) {
            }
        }

        public void onGameCenterAuthResult(string data)
        {
            try
            {
                if (!string.IsNullOrEmpty(data))
                {
                    int arg = int.Parse(data);
                    if (OnGameCenterAuthEvent != null && OnGameCenterAuthEvent.GetInvocationList().Length > 0)
                    {
                        OnGameCenterAuthEvent.Invoke(arg == 1);
                    }
                }
            }
            catch (Exception e)
            {
            }
        }

        #endregion
#endif

        #region ads
        public void onAdLoaded(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                int adType = int.Parse(data);
                if (adType == (int)IvySdk.ADTypes.AD_TYPE_INTERSTITIAL)
                {
                    if (OnInterstitialAdEvent != null && OnInterstitialAdEvent.GetInvocationList().Length > 0)
                    {
                        OnInterstitialAdEvent.Invoke(IvySdk.AdEvents.AD_LOADED, "0");
                    }
                }
                else if (adType == (int)IvySdk.ADTypes.AD_TYPE_REWARDED)
                {
                    if (OnRewardedAdEvent != null && OnRewardedAdEvent.GetInvocationList().Length > 0)
                    {
                        OnRewardedAdEvent.Invoke(IvySdk.AdEvents.AD_LOADED, "0");
                    }
                }
                else if (adType == (int)IvySdk.ADTypes.AD_TYPE_BANNER)
                {
                    if (OnBannerAdEvent != null && OnBannerAdEvent.GetInvocationList().Length > 0)
                    {
                        OnBannerAdEvent.Invoke(IvySdk.AdEvents.AD_LOADED, "0");
                    }
                }
            }
        }

        public void onAdLoadFailed(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                int adType = int.Parse(data);
                if (adType == (int)IvySdk.ADTypes.AD_TYPE_INTERSTITIAL)
                {
                    if (OnInterstitialAdEvent != null && OnInterstitialAdEvent.GetInvocationList().Length > 0)
                    {
                        OnInterstitialAdEvent.Invoke(IvySdk.AdEvents.AD_LOAD_FAILED, "0");
                    }
                }
                else if (adType == (int)IvySdk.ADTypes.AD_TYPE_REWARDED)
                {
                    if (OnRewardedAdEvent != null && OnRewardedAdEvent.GetInvocationList().Length > 0)
                    {
                        OnRewardedAdEvent.Invoke(IvySdk.AdEvents.AD_LOAD_FAILED, "0");
                    }
                }
                else if (adType == (int)IvySdk.ADTypes.AD_TYPE_BANNER)
                {
                    if (OnBannerAdEvent != null && OnBannerAdEvent.GetInvocationList().Length > 0)
                    {
                        OnBannerAdEvent.Invoke(IvySdk.AdEvents.AD_LOAD_FAILED, "0");
                    }
                }
            }
        }

        public void onAdShowSuccess(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 3)
                {
                    int adType = int.Parse(args[0]);
                    string placement = args[2];
                    if (adType == (int)IvySdk.ADTypes.AD_TYPE_INTERSTITIAL)
                    {
                        if (OnInterstitialAdEvent != null && OnInterstitialAdEvent.GetInvocationList().Length > 0)
                        {
                            OnInterstitialAdEvent.Invoke(IvySdk.AdEvents.AD_SHOW_SUCCEED, placement);
                        }
                    }
                    else if (adType == (int)IvySdk.ADTypes.AD_TYPE_REWARDED)
                    {
                        if (OnRewardedAdEvent != null && OnRewardedAdEvent.GetInvocationList().Length > 0)
                        {
                            OnRewardedAdEvent.Invoke(IvySdk.AdEvents.AD_SHOW_SUCCEED, placement);
                        }
                    }
                    else if (adType == (int)IvySdk.ADTypes.AD_TYPE_BANNER)
                    {
                        if (OnBannerAdEvent != null && OnBannerAdEvent.GetInvocationList().Length > 0)
                        {
                            OnBannerAdEvent.Invoke(IvySdk.AdEvents.AD_SHOW_SUCCEED, placement);
                        }
                    }
                }
            }
        }

        public void onAdShowFailed(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 3)
                {
                    int adType = int.Parse(args[0]);
                    string placement = args[2];
                    if (adType == (int)IvySdk.ADTypes.AD_TYPE_INTERSTITIAL)
                    {
                        if (OnInterstitialAdEvent != null && OnInterstitialAdEvent.GetInvocationList().Length > 0)
                        {
                            OnInterstitialAdEvent.Invoke(IvySdk.AdEvents.AD_SHOW_FAILED, placement);
                        }
                    }
                    else if (adType == (int)IvySdk.ADTypes.AD_TYPE_REWARDED)
                    {
                        if (OnRewardedAdEvent != null && OnRewardedAdEvent.GetInvocationList().Length > 0)
                        {
                            OnRewardedAdEvent.Invoke(IvySdk.AdEvents.AD_SHOW_FAILED, placement);
                        }
                    }
                    else if (adType == (int)IvySdk.ADTypes.AD_TYPE_BANNER)
                    {
                        if (OnBannerAdEvent != null && OnBannerAdEvent.GetInvocationList().Length > 0)
                        {
                            OnBannerAdEvent.Invoke(IvySdk.AdEvents.AD_SHOW_FAILED, placement);
                        }
                    }
                }
            }
        }

        public void onAdClicked(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 3)
                {
                    int adType = int.Parse(args[0]);
                    string placement = args[2];
                    if (adType == (int)IvySdk.ADTypes.AD_TYPE_INTERSTITIAL)
                    {
                        if (OnInterstitialAdEvent != null && OnInterstitialAdEvent.GetInvocationList().Length > 0)
                        {
                            OnInterstitialAdEvent.Invoke(IvySdk.AdEvents.AD_CLICKED, placement);
                        }
                    }
                    else if (adType == (int)IvySdk.ADTypes.AD_TYPE_REWARDED)
                    {
                        if (OnRewardedAdEvent != null && OnRewardedAdEvent.GetInvocationList().Length > 0)
                        {
                            OnRewardedAdEvent.Invoke(IvySdk.AdEvents.AD_CLICKED, placement);
                        }
                    }
                    else if (adType == (int)IvySdk.ADTypes.AD_TYPE_BANNER)
                    {
                        if (OnBannerAdEvent != null && OnBannerAdEvent.GetInvocationList().Length > 0)
                        {
                            OnBannerAdEvent.Invoke(IvySdk.AdEvents.AD_CLICKED, placement);
                        }
                    }
                }
            }
        }

        public void onAdClosed(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 3)
                {
                    int adType = int.Parse(args[0]);
                    string placement = args[2];
                    if (adType == (int)IvySdk.ADTypes.AD_TYPE_INTERSTITIAL)
                    {
                        if (OnInterstitialAdEvent != null && OnInterstitialAdEvent.GetInvocationList().Length > 0)
                        {
                            OnInterstitialAdEvent.Invoke(IvySdk.AdEvents.AD_CLOSED, placement);
                        }
                    }
                    else if (adType == (int)IvySdk.ADTypes.AD_TYPE_REWARDED)
                    {
                        if (OnRewardedAdEvent != null && OnRewardedAdEvent.GetInvocationList().Length > 0)
                        {
                            OnRewardedAdEvent.Invoke(IvySdk.AdEvents.AD_CLOSED, placement);
                        }
                    }
                    else if (adType == (int)IvySdk.ADTypes.AD_TYPE_BANNER)
                    {
                        if (OnBannerAdEvent != null && OnBannerAdEvent.GetInvocationList().Length > 0)
                        {
                            OnBannerAdEvent.Invoke(IvySdk.AdEvents.AD_CLOSED, placement);
                        }
                    }
                }
            }
        }

        public void onAdRewardUser(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 3)
                {
                    int gotReward = int.Parse(args[0]);
                    if (gotReward == 1)
                    {
                        string placement = args[2];
                        if (OnRewardedAdEvent != null && OnRewardedAdEvent.GetInvocationList().Length > 0)
                        {
                            OnRewardedAdEvent.Invoke(IvySdk.AdEvents.AD_REWARD_USER, placement);
                        }
                    }
                }
            }
        }

        #endregion

        #region Firebase
        public void onFirebaseUnlinkSuccess(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                if (OnFirebaseUnlinkEvent != null && OnFirebaseUnlinkEvent.GetInvocationList().Length > 0)
                {
                    OnFirebaseUnlinkEvent.Invoke(data, true);
                }
            }
        }

        public void onFirebaseUnlinkFailed(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    string channel = args[1];
                    if (OnFirebaseUnlinkEvent != null && OnFirebaseUnlinkEvent.GetInvocationList().Length > 0)
                    {
                        OnFirebaseUnlinkEvent.Invoke(channel, false);
                    }
                    //if (((string)IvySdk.FirebaseLinkChannel.PLAY_GAMES).Equals(channel))
                    //{

                    //}
                    //else if (((string)IvySdk.FirebaseLinkChannel.FACEBOOK).Equals(channel))
                    //{

                    //}
                    //else if (((string)IvySdk.FirebaseLinkChannel.EMAIL).Equals(channel))
                    //{

                    //}
                }
            }
        }

        public void onReloadFBLoginStatus(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    int status = int.Parse(args[1]);
                    if (OnFirebaseLoginEvent != null && OnFirebaseLoginEvent.GetInvocationList().Length > 0)
                    {
                        OnFirebaseLoginEvent.Invoke("", status == 1);
                    }
                }
            }
        }

        public void onFirebaseLoginResult(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 3)
                {
                    string channel = args[0];
                    int status = int.Parse(args[1]);
                    if (OnFirebaseLoginEvent != null && OnFirebaseLoginEvent.GetInvocationList().Length > 0)
                    {
                        OnFirebaseLoginEvent.Invoke(channel, status == 1);
                    }
                }
            }
        }
        #endregion

        #region 存档
        /**
         * @params data:  collection
         */
        public void onCDSaveSuccess(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    if (OnCloudDataSaveEvent != null && OnCloudDataSaveEvent.GetInvocationList().Length > 0)
                    {
                        OnCloudDataSaveEvent.Invoke(args[0], args[1],  true);
                    }
                }
            }
        }
        /**
         * @params data:  collection
         */
        public void onCDSaveFailed(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    if (OnCloudDataSaveEvent != null && OnCloudDataSaveEvent.GetInvocationList().Length > 0)
                    {
                        OnCloudDataSaveEvent.Invoke(args[0], args[1], false);
                    }
                }
            }
        }

        public void onCDReadSuccess(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 3)
                {
                    string collection = args[0];
                    string doucumentId = args[1];
                    string cData = args[2];
                    if (OnCloudDataReadEvent != null && OnCloudDataReadEvent.GetInvocationList().Length > 0)
                    {
                        OnCloudDataReadEvent.Invoke(collection, doucumentId, cData, true);
                    }
                }
            }
        }

        public void onCDReadFailed(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    string collection = args[0];
                    string doucumentId = args[1];
                    if (OnCloudDataReadEvent != null && OnCloudDataReadEvent.GetInvocationList().Length > 0)
                    {
                        OnCloudDataReadEvent.Invoke(collection, doucumentId, null, false);
                    }
                }
            }
        }

        /**
         * @params data:  collection
         */
        public void onCDMergeSuccess(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    if (OnCloudDataMergeEvent != null && OnCloudDataMergeEvent.GetInvocationList().Length > 0)
                    {
                        OnCloudDataMergeEvent.Invoke(args[0], args[1], true);
                    }
                }
            }
        }

        /**
           * @params data:  collection
           */
        public void onCDMergeFailed(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    if (OnCloudDataMergeEvent != null && OnCloudDataMergeEvent.GetInvocationList().Length > 0)
                    {
                        OnCloudDataMergeEvent.Invoke(args[0], args[1], false);
                    }
                }
            }
        }

        public void onCDQuerySuccess(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 3)
                {

                    string collection = args[0];
                    string documentId = args[1];
                    string cData = args[2];
                    if (OnCloudDataQueryEvent != null && OnCloudDataQueryEvent.GetInvocationList().Length > 0)
                    {
                        OnCloudDataQueryEvent.Invoke(collection, documentId, cData, true);
                    }
                }
            }
        }

        public void onCDQueryFailed(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    string collection = args[0];
                    string documentId = args[1];
                    if (OnCloudDataQueryEvent != null && OnCloudDataQueryEvent.GetInvocationList().Length > 0)
                    {
                        OnCloudDataQueryEvent.Invoke(data, documentId, null, false);
                    }
                }
            }
        }

        public void onCDDeleteSuccess(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    if (OnCloudDataDeleteEvent != null && OnCloudDataDeleteEvent.GetInvocationList().Length > 0)
                    {
                        OnCloudDataDeleteEvent.Invoke(args[0], args[1], true);
                    }
                }
            }
        }

        public void onCDDeleteFailed(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    if (OnCloudDataDeleteEvent != null && OnCloudDataDeleteEvent.GetInvocationList().Length > 0)
                    {
                        OnCloudDataDeleteEvent.Invoke(args[0], args[1], false);
                    }
                }
            }
        }

        public void onCDUpdateSuccess(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 3)
                {
                    string collection = args[0];
                    string documentId = args[1];
                    string transactionId = args[2];
                    if (OnCloudDataUpdateEvent != null && OnCloudDataUpdateEvent.GetInvocationList().Length > 0)
                    {
                        OnCloudDataUpdateEvent.Invoke(collection, documentId, transactionId, true);
                    }
                }
            }
        }

        public void onCDUpdateFailed(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 3)
                {
                    string collection = args[0];
                    string documentId = args[1];
                    string transactionId = args[2];
                    if (OnCloudDataUpdateEvent != null && OnCloudDataUpdateEvent.GetInvocationList().Length > 0)
                    {
                        OnCloudDataUpdateEvent.Invoke(collection, documentId, transactionId, false);
                    }
                }
            }
        }

        public void onCDSnapshotSuccess(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    string collection = args[0];
                    string documentId = args[1];
                    if (OnCloudDataSnapshotEvent != null && OnCloudDataSnapshotEvent.GetInvocationList().Length > 0)
                    {
                        OnCloudDataSnapshotEvent.Invoke(collection, documentId, true);
                    }
                }
            }
        }

        public void onCDSnapshotFailed(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    string collection = args[0];
                    string documentId = args[1];
                    if (OnCloudDataSnapshotEvent != null && OnCloudDataSnapshotEvent.GetInvocationList().Length > 0)
                    {
                        OnCloudDataSnapshotEvent.Invoke(collection, documentId, false);
                    }
                }
            }
        }

        #endregion

        #region firebase cloud function
        public void onCloudFunctionSuccess(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    string functionName = args[0];
                    string response = args[1];
                    if (OnFirebaseCloudFunctionEvent != null && OnFirebaseCloudFunctionEvent.GetInvocationList().Length > 0)
                    {
                        OnFirebaseCloudFunctionEvent.Invoke(functionName, response, true);
                    }
                }
            }
        }

        public void onCloudFunctionFailure(string data)
        {
            if (!string.IsNullOrEmpty(data))
            {
                string[] args = data.Split('|');
                if (args != null && args.Length == 2)
                {
                    string functionName = args[0];
                    string reason = args[1];
                    if (OnFirebaseCloudFunctionEvent != null && OnFirebaseCloudFunctionEvent.GetInvocationList().Length > 0)
                    {
                        OnFirebaseCloudFunctionEvent.Invoke(functionName, reason, false);
                    }
                }
            }
        }
        #endregion


    }

}
