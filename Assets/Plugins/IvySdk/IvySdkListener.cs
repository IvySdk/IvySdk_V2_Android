using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace com.ivy.sdk { 
public class IvySdkListener : MonoBehaviour
{

    private static IvySdkListener _instance;

    public static event Action<IvySdk.PaymentResult, int, string> OnPaymentEvent;
    public static event Action<IvySdk.PaymentResult, int, string, string> OnPaymentWithPayloadEvent;

    public static event Action<int> HelperUnreadMsgCountEvent;

    public static event Action<bool> OnPlayGamesLoginEvent;
    public static event Action<bool> OnFacebookLoginEvent;
    public static event Action<string, bool> OnFirebaseLoginEvent;

    public static event Action<string> OnReceivedNotificationEvent;

    /**
     * int 为广告位, 其中AD_LOADED、AD_LOAD_FAILED事件中无效
     */
    public static event Action<IvySdk.AdEvents, int> OnInterstitialAdEvent;
    public static event Action<IvySdk.AdEvents, int> OnRewardedAdEvent;
    public static event Action<IvySdk.AdEvents, int> OnBannerAdEvent;

    public static event Action<string, bool> OnFirebaseUnlinkEvent;

    public static event Action<string, bool> OnCloudDataSaveEvent;
    public static event Action<string, string, string, bool> OnCloudDataReadEvent;
    public static event Action<string, bool> OnCloudDataMergeEvent;
    public static event Action<string, string, bool> OnCloudDataQueryEvent;
    public static event Action<string, bool> OnCloudDataDeleteEvent;
    public static event Action<string, string, bool> OnCloudDataUpdateEvent;
    public static event Action<string, string, bool> OnCloudDataSnapshotEvent;


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
                OnPaymentEvent.Invoke(IvySdk.PaymentResult.Success, payId, merchantTransactionId);
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
                OnPaymentWithPayloadEvent.Invoke(IvySdk.PaymentResult.Success, payId, payload, merchantTransactionId);
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
                OnPaymentEvent.Invoke(IvySdk.PaymentResult.Failed, payId, merchantTransactionId);
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
                //TODO: 
            }
        }
    }

    public void onPaymentSystemValid(string data)
    {
        OnPaymentEvent.Invoke(IvySdk.PaymentResult.PaymentSystemValid, 0, "");
    }

    public void onPaymentSystemError(string data)
    {
        OnPaymentEvent.Invoke(IvySdk.PaymentResult.PaymentSystemError, 0, "");
    }

    #endregion


    #region 客服
    public void unreadHelperMsgCount(string data)
    {
        if (!string.IsNullOrEmpty(data))
        {
            try
            {
                int msgCount = int.Parse(data);
                HelperUnreadMsgCountEvent.Invoke(msgCount);
            }
            catch { }
        }
    }
    #endregion

    #region 登陆
    public void onLoginSuccess(string data)
    {
        if (!string.IsNullOrEmpty(data))
        {
            try
            {
                if (data.Equals("play_games"))
                {
                    OnPlayGamesLoginEvent.Invoke(true);
                }
                else if (data.Equals("facebook"))
                {
                    OnFacebookLoginEvent.Invoke(true);
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
                        OnPlayGamesLoginEvent.Invoke(false);
                    }
                    else if (platform.Equals("facebook"))
                    {
                        OnFacebookLoginEvent.Invoke(false);
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
                if (data.Equals("play_games"))
                {
                    OnPlayGamesLoginEvent.Invoke(false);
                }
                else if (data.Equals("facebook"))
                {
                    OnFacebookLoginEvent.Invoke(false);
                }
            }
            catch { }
        }
    }

    #endregion

    #region 通知
    public void onReceivedNotifyAction(string data)
    {
        if (!string.IsNullOrEmpty(data))
        {
            OnReceivedNotificationEvent.Invoke(data);
        }
    }
    #endregion

    #region ads
    public void onAdLoaded(string data)
    {
        if (!string.IsNullOrEmpty(data))
        {
            int adType = int.Parse(data);
            if (adType == (int)IvySdk.ADTypes.AD_TYPE_INTERSTITIAL)
            {
                OnInterstitialAdEvent.Invoke(IvySdk.AdEvents.AD_LOADED, 0);
            }
            else if (adType == (int)IvySdk.ADTypes.AD_TYPE_REWARDED)
            {
                OnRewardedAdEvent.Invoke(IvySdk.AdEvents.AD_LOADED, 0);
            }
            else if (adType == (int)IvySdk.ADTypes.AD_TYPE_BANNER)
            {
                OnBannerAdEvent.Invoke(IvySdk.AdEvents.AD_LOADED, 0);
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
                OnInterstitialAdEvent.Invoke(IvySdk.AdEvents.AD_LOAD_FAILED, 0);
            }
            else if (adType == (int)IvySdk.ADTypes.AD_TYPE_REWARDED)
            {
                OnRewardedAdEvent.Invoke(IvySdk.AdEvents.AD_LOAD_FAILED, 0);
            }
            else if (adType == (int)IvySdk.ADTypes.AD_TYPE_BANNER)
            {
                OnBannerAdEvent.Invoke(IvySdk.AdEvents.AD_LOAD_FAILED, 0);
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
                int placement = int.Parse(args[2]);
                if (adType == (int)IvySdk.ADTypes.AD_TYPE_INTERSTITIAL)
                {
                    OnInterstitialAdEvent.Invoke(IvySdk.AdEvents.AD_SHOW_SUCCEED, placement);
                }
                else if (adType == (int)IvySdk.ADTypes.AD_TYPE_REWARDED)
                {
                    OnRewardedAdEvent.Invoke(IvySdk.AdEvents.AD_SHOW_SUCCEED, placement);
                }
                else if (adType == (int)IvySdk.ADTypes.AD_TYPE_BANNER)
                {
                    OnBannerAdEvent.Invoke(IvySdk.AdEvents.AD_SHOW_SUCCEED, placement);
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
                int placement = int.Parse(args[2]);
                if (adType == (int)IvySdk.ADTypes.AD_TYPE_INTERSTITIAL)
                {
                    OnInterstitialAdEvent.Invoke(IvySdk.AdEvents.AD_SHOW_FAILED, placement);
                }
                else if (adType == (int)IvySdk.ADTypes.AD_TYPE_REWARDED)
                {
                    OnRewardedAdEvent.Invoke(IvySdk.AdEvents.AD_SHOW_FAILED, placement);
                }
                else if (adType == (int)IvySdk.ADTypes.AD_TYPE_BANNER)
                {
                    OnBannerAdEvent.Invoke(IvySdk.AdEvents.AD_SHOW_FAILED, placement);
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
                int placement = int.Parse(args[2]);
                if (adType == (int)IvySdk.ADTypes.AD_TYPE_INTERSTITIAL)
                {
                    OnInterstitialAdEvent.Invoke(IvySdk.AdEvents.AD_CLICKED, placement);
                }
                else if (adType == (int)IvySdk.ADTypes.AD_TYPE_REWARDED)
                {
                    OnRewardedAdEvent.Invoke(IvySdk.AdEvents.AD_CLICKED, placement);
                }
                else if (adType == (int)IvySdk.ADTypes.AD_TYPE_BANNER)
                {
                    OnBannerAdEvent.Invoke(IvySdk.AdEvents.AD_CLICKED, placement);
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
                int placement = int.Parse(args[2]);
                if (adType == (int)IvySdk.ADTypes.AD_TYPE_INTERSTITIAL)
                {
                    OnInterstitialAdEvent.Invoke(IvySdk.AdEvents.AD_CLOSED, placement);
                }
                else if (adType == (int)IvySdk.ADTypes.AD_TYPE_REWARDED)
                {
                    OnRewardedAdEvent.Invoke(IvySdk.AdEvents.AD_CLOSED, placement);
                }
                else if (adType == (int)IvySdk.ADTypes.AD_TYPE_BANNER)
                {
                    OnBannerAdEvent.Invoke(IvySdk.AdEvents.AD_CLOSED, placement);
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
                    int placement = int.Parse(args[2]);
                    OnRewardedAdEvent.Invoke(IvySdk.AdEvents.AD_REWARD_USER, placement);
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
            OnFirebaseUnlinkEvent.Invoke(data, true);
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
                OnFirebaseUnlinkEvent.Invoke(channel, false);
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
                OnFirebaseUnlinkEvent.Invoke("", status == 1);
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
                OnFirebaseUnlinkEvent.Invoke(channel, status == 1);
            }
        }
    }
    #endregion

    #region
    /**
     * @params data:  collection
     */
    public void onCDSaveSuccess(string data)
    {
        if (!string.IsNullOrEmpty(data))
        {
            OnCloudDataSaveEvent.Invoke(data, true);
        }
    }
    /**
     * @params data:  collection
     */
    public void onCDSaveFailed(string data)
    {
        if (!string.IsNullOrEmpty(data))
        {
            OnCloudDataSaveEvent.Invoke(data, false);
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
                OnCloudDataReadEvent.Invoke(collection, doucumentId, cData, true);
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
                OnCloudDataReadEvent.Invoke(collection, doucumentId, null, false);
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
            OnCloudDataMergeEvent.Invoke(data, true);
        }
    }

    /**
       * @params data:  collection
       */
    public void onCDMergeFailed(string data)
    {
        if (!string.IsNullOrEmpty(data))
        {
            OnCloudDataMergeEvent.Invoke(data, false);
        }
    }

    public void onCDQuerySuccess(string data)
    {
        if (!string.IsNullOrEmpty(data))
        {
            string[] args = data.Split('|');
            if (args != null && args.Length == 2)
            {

                string collection = args[0];
                string cData = args[1];
                OnCloudDataQueryEvent.Invoke(collection, cData, true);
            }
        }
    }

    public void onCDQueryFailed(string data)
    {
        if (!string.IsNullOrEmpty(data))
        {
            OnCloudDataQueryEvent.Invoke(data, null, false);
        }
    }

    public void onCDDeleteSuccess(string data)
    {
        if (!string.IsNullOrEmpty(data))
        {
            OnCloudDataDeleteEvent.Invoke(data, true);
        }
    }

    public void onCDDeleteFailed(string data)
    {
        if (!string.IsNullOrEmpty(data))
        {
            OnCloudDataDeleteEvent.Invoke(data, false);
        }
    }

    public void onCDUpdateSuccess(string data)
    {
        if (!string.IsNullOrEmpty(data))
        {
            string[] args = data.Split('|');
            if (args != null && args.Length == 2)
            {
                string collection = args[0];
                string transactionId = args[1];
                OnCloudDataUpdateEvent.Invoke(collection, transactionId, true);
            }
        }
    }

    public void onCDUpdateFailed(string data)
    {
        if (!string.IsNullOrEmpty(data))
        {
            string[] args = data.Split('|');
            if (args != null && args.Length == 2)
            {
                string collection = args[0];
                string transactionId = args[1];
                OnCloudDataUpdateEvent.Invoke(collection, transactionId, false);
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
                OnCloudDataSnapshotEvent.Invoke(collection, documentId, true);
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
                OnCloudDataSnapshotEvent.Invoke(collection, documentId, false);
            }
        }
    }

    #endregion

}

}
