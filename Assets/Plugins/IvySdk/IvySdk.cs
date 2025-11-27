using System;
using System.Collections.Generic;
using System.Collections;
#if UNITY_IOS
using System.Runtime.InteropServices;
#endif
using UnityEngine;
#if UNITY_EDITOR
using UnityEditor;
using System.Diagnostics;
#endif

namespace com.ivy.sdk
{
    public sealed class IvySdk
    {
        //#if UNITY_ANDROID
        private static IvySdk _instance = null;

        private AndroidJavaClass _class = null;
        
        #if UNITY_EDITOR
            public static bool hasRewardAd = true;
        #endif

        public enum PaymentResult : int
        {
            Success = 1,
            Failed,
            Cancel,
            PaymentSystemError,
            PaymentSystemValid
        }

        public enum AdEvents : int
        {
            AD_LOADED = 1,
            AD_LOAD_FAILED,
            AD_SHOW_SUCCEED,
            AD_SHOW_FAILED,
            AD_CLICKED,
            AD_CLOSED,
            AD_REWARD_USER,
        }

        public enum ADTypes : int
        {
            AD_TYPE_INTERSTITIAL = 1,
            AD_TYPE_REWARDED = 2,
            AD_TYPE_BANNER = 3,
        }

        public enum BannerAdPosition : int
        {
            POSITION_LEFT_TOP = 1,
            POSITION_LEFT_BOTTOM = 2,
            POSITION_CENTER_TOP = 3,
            POSITION_CENTER_BOTTOM = 4,
            POSITION_CENTER = 5,
            POSITION_RIGHT_TOP = 6,
            POSITION_RIGHT_BOTTOM = 7,
        }

        public class FirebaseLinkChannel
        {
            public static string ANONYMOUS = "anonymous";
            public static string PLAY_GAMES = "playgames";
            public static string GOOGLE = "google";
            public static string FACEBOOK = "facebook";
            public static string EMAIL = "email";
            public static string APPLE = "apple";
            public static string DEFAULT = "default";
        }

        public enum ConfigKeys
        {
            CONFIG_KEY_APP_ID = 1,              // app id
            CONFIG_KEY_LEADER_BOARD_URL = 2,
            CONFIG_KEY_API_VERSION = 3,
            CONFIG_KEY_SCREEN_WIDTH = 4,        // 屏幕宽度
            CONFIG_KEY_SCREEN_HEIGHT = 5,       // 屏幕高度
            CONFIG_KEY_LANGUAGE = 6,            // 设备语言
            CONFIG_KEY_COUNTRY = 7,             // 设备国家
            CONFIG_KEY_VERSION_CODE = 8,        //版本号
            CONFIG_KEY_VERSION_NAME = 9,        //版本名
            CONFIG_KEY_PACKAGE_NAME = 10,       // 包名
            CONFIG_KEY_UUID = 11,               // role id
            SDK_CONFIG_KEY_JSON_VERSION = 21,
            SDK_KEY_NETWORK_TYPE = 22,
        }

        public enum ABChannel : int
        {
            IVY = 0,
            HUOSHAN = 1,
        }

        public static IvySdk Instance
        {
            get
            {
                if (_instance == null)
                {
                    _instance = new IvySdk();
                }
                return _instance;
            }
        }
#if UNITY_ANDROID
        public void Init()
        {
            RiseEditorAd.hasInit = true;
            if (_class != null)
            {
                return;
            }
            try
            {
                IvySdkListener.Instance.enabled = true;
                _class = new AndroidJavaClass("com.android.client.Unity");
                if (_class != null)
                {
                    using (AndroidJavaClass unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
                    {
                        using (AndroidJavaObject context = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity"))
                        {
                            _class.CallStatic("onCreate", context);
                        }
                    }
                }
                else
                {
                    throw new Exception("unable to init sdk class");
                }
            }
            catch (Exception e)
            {
                Debug.LogException(e);
            }
        }

        public void EnableAbTest(ABChannel channel)
        {
            if (_class != null)
            {
                _class.CallStatic("enableAbTest", (int)channel);
            }
        }

        public void SetHeaderInfo(Dictionary<string, object> data)
        {
            try
            {
                string param = "{}";
                if (data != null)
                {
                    param = IvyJson.Serialize(data);
                }

                if (_class != null)
                {
                    _class.CallStatic("setHeaderInfo", param);
                }
            }
            catch (Exception)
            {
                Debug.LogError($"SetHeaderInfo failed!!!, param convert failed");
            }
        }

        public void SetUserInfo(Dictionary<string, object> data)
        {
            try
            {
                string param = "{}";
                if (data != null)
                {
                    param = IvyJson.Serialize(data);
                }

                if (_class != null)
                {
                    _class.CallStatic("setUserInfo", param);
                }
            }
            catch (Exception)
            {
                Debug.LogError($"SetUserInfo failed!!!, param convert failed");
            }
        }


        public void StartTrackEvent()
        {
            if (_class != null)
            {
                _class.CallStatic("startTrackEvent");
            }
        }

        public void PullAbTestConfigs()
        {
            if (_class != null)
            {
                _class.CallStatic("pullAbTestConfigs");
            }
        }

        public string GetHuoShanSSID()
        {
            if (_class != null)
            {
                return _class.CallStatic<string>("getAbtestSSID");
            }
            return null;
        }

        public bool GetAbConfigBoolean(string key, bool defaultValue)
        {
            if (_class != null)
            {
               return _class.CallStatic<bool>("getAbConfigBoolean", key, defaultValue);
            } else
            {
                return defaultValue;
            }
        }

        public int GetAbConfigInt(string key, int defaultValue)
        {
            if (_class != null)
            {
                return _class.CallStatic<int>("getAbConfigInt", key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }

        public long GetAbConfigLong(string key, long defaultValue)
        {
            if (_class != null)
            {
                return _class.CallStatic<long>("getAbConfigLong", key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }

        public float GetAbConfigFloat(string key, float defaultValue)
        {
            if (_class != null)
            {
                return _class.CallStatic<float>("getAbConfigFloat", key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }

        public double GetAbConfigDouble(string key, double defaultValue)
        {
            if (_class != null)
            {
                return _class.CallStatic<double>("getAbConfigDouble", key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }

        public string GetAbConfigString(string key, string defaultValue)
        {
            if (_class != null)
            {
                return _class.CallStatic<string>("getAbConfigString", key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }


        #region ads

        // bxid = old, 执行老sdk 广告逻辑； 其它值请和运营确定
        public void SetBxId(string bxid)
        {
            if (_class != null)
            {
                _class.CallStatic("setBXId", bxid);
            }
        }

        public bool HasBannerAd()
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("hasBannerAd");
            }
            return false;
        }

        public void TriggerBannerAd(string placement)
        {
            if (_class != null)
            {
                _class.CallStatic("triggerBannerAd", placement);
            }
        }

        public void ShowBannerAd(string tag, BannerAdPosition position, string placement)
        {
#if UNITY_EDITOR
        RiseEditorAd.EditorAdInstance.ShowBanner(tag, (int)position);
#endif
            if (_class != null)
            {
                _class.CallStatic("showBannerAd", tag, ((int)position), placement);
            }
        }

        /**
         *  展示banner 广告
         *  @param tag          广告标签，默认为 default
         *  @param position     广告位置，参考BannerAdPosition
         *  @param placement    广告位，
         *  @param clientInfo   客户端自定义信息，JSONObject结构，注意 bool值会被转换位1/0
         * 
         */
        public void ShowBannerAd(string tag, BannerAdPosition position, string placement, string clientInfo)
        {
#if UNITY_EDITOR
        RiseEditorAd.EditorAdInstance.ShowBanner(tag, (int)position);
#endif
            if (_class != null)
            {
                _class.CallStatic("showBannerAd", tag, ((int)position), placement, clientInfo);
            }
        }

        /**
         *  关闭banner 广告
         *  @param placement    广告位
         */
        public void CloseBannerAd(string placement)
        {
#if UNITY_EDITOR
        RiseEditorAd.EditorAdInstance.CloseBanner();
#endif
            if (_class != null)
            {
                _class.CallStatic("closeBannerAd", placement);
            }
        }

        public bool HasInterstitialAd()
        {
#if UNITY_EDITOR
            return true;
#endif
            if (_class != null)
            {
                return _class.CallStatic<bool>("hasInterstitialAd");
            }
            return false;
        }

        public void TriggerInterstitialAd(string placement)
        {
            if (_class != null)
            {
                _class.CallStatic("triggerInterstitialAd", placement);
            }
        }

        /**
         *  展示 插屏 广告
         *  @param tag          广告标签，默认为 default
         *  @param placement    广告位，
         *  @param clientInfo   客户端自定义信息，JSONObject结构，注意 bool值会被转换位1/0
         */
        public void ShowInterstitialAd(string tag, string placement, string clientInfo = null)
        {
#if UNITY_EDITOR
        RiseEditorAd.EditorAdInstance.ShowAd(tag);
#endif
            if (_class != null)
            {
                _class.CallStatic("showInterstitialAd", tag, placement, clientInfo);
            }
        }

        public bool HasRewardedAd()
        {
#if UNITY_EDITOR
        return hasRewardAd;
#endif
            if (_class != null)
            {
                return _class.CallStatic<bool>("hasRewardedAd");
            }
            return false;
        }

        public void TriggerRewardedAd(string placement)
        {
            if (_class != null)
            {
                _class.CallStatic("triggerRewardedAd", placement);
            }
        }

        /**
         *  展示 激励视频 广告
         *  @param tag          广告标签，默认为 default
         *  @param placement    广告位，可以用于标记奖励点
         *  @param clientInfo   客户端自定义信息，JSONObject结构，注意 bool值会被转换位1/0
         */
        public void ShowRewardedAd(string tag, string placement, string clientInfo = null)
        {
#if UNITY_EDITOR
        RiseEditorAd.EditorAdInstance.ShowRewardAd(tag, placement);
#endif
            if (_class != null)
            {
                _class.CallStatic("showRewardedAd", tag, placement, clientInfo);
            }
        }

        #endregion

        #region 计费

        public void Pay(int id)
        {
#if UNITY_EDITOR
        RiseEditorAd.EditorAdInstance.Pay (id);
#endif
            if (_class != null)
            {
                _class.CallStatic("pay", id);
            }
        }

        public void Pay(int id, string payload)
        {
#if UNITY_EDITOR
        RiseEditorAd.EditorAdInstance.Pay (id);
#endif
            if (_class != null)
            {
                _class.CallStatic("pay", id, payload);
            }
        }

        /**
         *  支付
         *  @param id           计费点位id
         *  @param payload      
         *  @param clientInfo   客户端自定义信息，JSONObject结构，注意 bool值会被转换位1/0
         */
        public void Pay(int id, string payload, string clientInfo)
        {
#if UNITY_EDITOR
        RiseEditorAd.EditorAdInstance.Pay (id);
#endif
            if (_class != null)
            {
                _class.CallStatic("pay", id, payload, clientInfo);
            }
        }

        /**
         *  如果在使用在线计费校验时，请在客户端发放奖励时调用此接口通知服务端发货
         *  @param merchantTransactionId    预下单id
         */
        public void ShippingGoods(string merchantTransactionId)
        {
            if (_class != null)
            {
                _class.CallStatic("shippingGoods", merchantTransactionId);
            }
        }

        /**
         * 查询指定计费点位是否存在未处理支付记录
         * @param id    计费点位 id 
         */
        public void QueryPaymentOrder(int id)
        {
            if (_class != null)
            {
                _class.CallStatic("queryPaymentOrder", id);
            }
        }

        /**
         * 查询所有未处理支付记录
         */
        public void QueryPaymentOrders()
        {
            if (_class != null)
            {
                _class.CallStatic("queryPaymentOrders");
            }
        }

        /**
         *  查询指定计费点位详情
         */
        public string GetPaymentData(int id)
        {
            if (_class != null)
            {
                return _class.CallStatic<string>("getPaymentData", id);
            }
            return "{}";
        }

        /**
         *  查询所有计费点位详情
         */
        public string GetPaymentDatas()
        {
            if (_class != null)
            {
                return _class.CallStatic<string>("getPaymentDatas");
            }
            return "{}";
        }

        /**
         * 计费系统是否可用
         */
        public bool IsPaymentValid()
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("isPaymentValid");
            }
            return false;
        }

        #endregion

        #region track
        /**
         * 统计事件至 所有平台
         * @params  eventName    事件名
         *          data         事件属性，字典结构
         */
        public void TrackEvent(string eventName, Dictionary<string, object> data)
        {
            try
            {
                string param = "{}";
                if(data != null)
                {
                    param = IvyJson.Serialize(data);
                }

                if (_class != null)
                {
                    _class.CallStatic("trackEvent", eventName, param);
                }
            }
            catch (Exception)
            {
                Debug.LogError($"track event:{eventName} failed!!!, param convert failed");
            }
        }

        /**
         * 统计事件至 所有平台
         * @params  eventName    事件名
         *          data         事件属性，字典结构
         */
        public void TrackEventToConversion(string eventName, Dictionary<string, object> data)
        {
            try
            {
                string param = "{}";
                if (data != null)
                {
                    param = IvyJson.Serialize(data);
                }

                if (_class != null)
                {
                    _class.CallStatic("trackEventToConversion", eventName, param);
                }
            }
            catch (Exception)
            {
                Debug.LogError($"track event to conversion:{eventName} failed!!!, param convert failed");
            }
        }

        /**
          * 统计事件至 Firebase
          * @params  eventName    事件名
          *          data         事件属性，字典结构
          */
        public void TrackEventToFirebase(string eventName, Dictionary<string, object> data)
        {
            try
            {
                string param = "{}";
                if (data != null)
                {
                    param = IvyJson.Serialize(data);
                }

                if (_class != null)
                {
                    _class.CallStatic("trackEventToFirebase", eventName, param);
                }
            }
            catch (Exception)
            {
                Debug.LogError($"track event to firebase:{eventName} failed!!!, param convert failed");
            }
        }

        /**
          * 统计事件至 Facebook
          * @params  eventName    事件名
          *          data         事件属性，字典结构
          */
        public void TrackEventToFacebook(string eventName, Dictionary<string, object> data)
        {
            try
            {
                string param = "{}";
                if (data != null)
                {
                    param = IvyJson.Serialize(data);
                }

                if (_class != null)
                {
                    _class.CallStatic("trackEventToFacebook", eventName, param);
                }
            }
            catch (Exception)
            {
                Debug.LogError($"track event to facebook:{eventName} failed!!!, param convert failed");
            }
        }

        /**
          * 统计事件至 Appsflyer
          * @params  eventName    事件名
          *          data         事件属性，字典结构
          */
        public void TrackEventToAppsflyer(string eventName, Dictionary<string, object> data)
        {
            try
            {
                string param = "{}";
                if (data != null)
                {
                    param = IvyJson.Serialize(data);
                }

                if (_class != null)
                {
                    _class.CallStatic("trackEventToAppsflyer", eventName, param);
                }
            }
            catch (Exception)
            {
                Debug.LogError($"track event to appsflyer:{eventName} failed!!!, param convert failed");
            }
        }

        /**
          * 统计事件至 自有平台
          * @params  eventName    事件名
          *          data         事件属性，字典结构
          */
        public void TrackEventToIvy(string eventName, Dictionary<string, object> data)
        {
            try
            {
                string param = "{}";
                if (data != null)
                {
                    param = IvyJson.Serialize(data);
                }

                if (_class != null)
                {
                    _class.CallStatic("trackEventToIvy", eventName, param);
                }
            }
            catch (Exception)
            {
                Debug.LogError($"track event to ivy:{eventName} failed!!!, param convert failed");
            }
        }

        /**
         *  设置用户属性 至 所有平台
         */
        public void SetUserProperty(string key, string value)
        {
            if (_class != null)
            {
                _class.CallStatic("setUserProperty", key, value);
            }
        }

        /**
         *  设置用户属性 至 Firebase
         */
        public void SetUserPropertyToFirebase(string key, string value)
        {
            if (_class != null)
            {
                _class.CallStatic("setUserPropertyToFirebase", key, value);
            }
        }

        /**
         *  设置用户属性 至 Appsflyer
         */
        //public void SetUserPropertyToAppsflyer(string key, string value)
        //{
        //    if (_class != null)
        //    {
        //        _class.CallStatic("setUserPropertyToAppsflyer", key, value);
        //    }
        //}

        /**
         *  设置用户属性 至 自有平台
         */
        public void SetUserPropertyToIvy(string key, string value)
        {
            if (_class != null)
            {
                _class.CallStatic("setUserPropertyToIvy", key, value);
            }
        }

        /**
         *  设置自定义用户 id
         */
        public void SetCustomUserId(string value)
        {
            if (_class != null)
            {
                _class.CallStatic("setCustomUserId", value);
            }
        }

        #endregion

        #region firebase cloud function
        public void FirebaseCloudFunction(string functionName)
        {
            if (_class != null)
            {
                _class.CallStatic("firebaseCloudFunction", functionName);
            }
        }

        /**
         * @param   functionName    方法名
         *          parameters      参数，要求JSONObject格式
         */
        public void FirebaseCloudFunction(string functionName, string parameters)
        {
            if (_class != null)
            {
                _class.CallStatic("firebaseCloudFunction", functionName, parameters);
            }
        }
        #endregion

        #region remote config
        /**
         * 获取 Firebase Remote Config 配置值
         */
        public int GetRemoteConfigInt(string key)
        {
            if (_class != null)
            {
                return _class.CallStatic<int>("getRemoteConfigInt", key);
            }
            return 0;
        }

        /**
         * 获取 Firebase Remote Config 配置值
         */
        public long GetRemoteConfigLong(string key)
        {
            if (_class != null)
            {
                return _class.CallStatic<long>("getRemoteConfigLong", key);
            }
            return 0;
        }

        /**
         * 获取 Firebase Remote Config 配置值
         */
        public double GetRemoteConfigDouble(string key)
        {
            if (_class != null)
            {
                return _class.CallStatic<double>("getRemoteConfigDouble", key);
            }
            return 0.0;
        }

        /**
         * 获取 Firebase Remote Config 配置值
         */
        public bool GetRemoteConfigBoolean(string key)
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("getRemoteConfigBoolean", key);
            }
            return false;
        }

        /**
         * 获取 Firebase Remote Config 配置值
         */
        public string GetRemoteConfigString(string key)
        {
            if (_class != null)
            {
                return _class.CallStatic<string>("getRemoteConfigString", key);
            }
            return "";
        }

        /**
         * 获取 自有 Remote Config 配置值
         */

        public bool HasIvyRemoteConfigKey(string key){
            if (_class != null)
            {
                return _class.CallStatic<bool>("hasIvyRemoteConfigKey", key);
            }
            return false;
        }

        public int GetIvyRemoteConfigInt(string key)
        {
            if (_class != null)
            {
                return _class.CallStatic<int>("getIvyRemoteConfigInt", key);
            }
            return 0;
        }

        /**
         * 获取 自有 Remote Config 配置值
         */
        public long GetIvyRemoteConfigLong(string key)
        {
            if (_class != null)
            {
                return _class.CallStatic<long>("getIvyRemoteConfigLong", key);
            }
            return 0;
        }

        /**
         * 获取 自有 Remote Config 配置值
         */
        public double GetIvyRemoteConfigDouble(string key)
        {
            if (_class != null)
            {
                return _class.CallStatic<double>("getIvyRemoteConfigDouble", key);
            }
            return 0.0;
        }

        /**
         * 获取 自有 Remote Config 配置值
         */
        public bool GetIvyRemoteConfigBoolean(string key)
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("getIvyRemoteConfigBoolean", key);
            }
            return false;
        }

        /**
         * 获取 自有 Remote Config 配置值
         */
        public string GetIvyRemoteConfigString(string key)
        {
            if (_class != null)
            {
                return _class.CallStatic<string>("getIvyRemoteConfigString", key);
            }
            return "";
        }
        #endregion

        public bool IsAuthPlatformReady()
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("isAuthPlatformReady");
            }
            return false;
        }

        #region play games&成就&排行榜

        /**
         * 查询PlayGames 登录状态
         */
        public bool IsPlayGamesLoggedIn()
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("isPlayGamesLoggedIn");
            }
            return false;
        }

        /**
         *  主动登录PlayGames
         *  如果项目配置了PlayGames，sdk会在游戏开启时主动登录PlayGames，客户端可以在登录回调中选择调用此接口
         */
        public void LoginPlayGames()
        {
            if (_class != null)
            {
                _class.CallStatic("loginPlayGames");
            }
        }

        /**
         * 登出PlayGames
         */
        //public void LogoutPlayGames()
        //{
        //    if (_class != null)
        //    {
        //        _class.CallStatic("logoutPlayGames");
        //    }
        //}

        /**
         * 获取已登录的PlayGames 用户信息
         */
        public string GetPlayGamesUserInfo()
        {
            if (_class != null)
            {
                return _class.CallStatic<string>("getPlayGamesUserInfo");
            }
            return "{}";
        }

        /**
         * 解锁成就
         * @param achievementId     成就id
         */
        public void UnlockAchievement(string achievementId)
        {
            if (_class != null)
            {
                _class.CallStatic("unlockAchievement", achievementId);
            }
        }

        /**
         * 提升成就
         * @param achievementId     成就id
         * @param step
         */
        public void IncreaseAchievement(string achievementId, int step)
        {
            if (_class != null)
            {
                _class.CallStatic("increaseAchievement", achievementId, step);
            }
        }

        /**
         * 展示成就页面
         */
        public void ShowAchievement()
        {
            if (_class != null)
            {
                _class.CallStatic("showAchievement");
            }
        }

        /**
         *  展示排行榜
         */
        public void ShowLeaderboards()
        {
            if (_class != null)
            {
                _class.CallStatic("showLeaderboards");
            }
        }

        /**
         * 展示指定排行榜
         * @param leaderboardId     排行榜id
         */
        public void ShowLeaderboard(string leaderboardId)
        {
            if (_class != null)
            {
                _class.CallStatic("showLeaderboard", leaderboardId);
            }
        }

        /**
         *  更新排行榜
         *  @param leaderboardId     排行榜id
         *  @param score
         */
        public void UpdateLeaderboard(string leaderboardId, long score)
        {
            if (_class != null)
            {
                _class.CallStatic("updateLeaderboard", leaderboardId, score);
            }
        }
        #endregion

        #region G+
        public void LoginGoogle()
        {
            if (_class != null)
            {
                _class.CallStatic("loginGoogle");
            }
        }

        public void LogoutGoogle()
        {
            if (_class != null)
            {
                _class.CallStatic("logoutGoogle");
            }
        }

        public bool IsGoogleLogged()
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("isGoogleLogged");
            }
            return false;
        }

        public String GetGoogleUserInfo()
        {
            if (_class != null)
            {
                return _class.CallStatic<string>("getGoogleUserInfo");
            }
            return "{}";
        }

        public String GetGoogleUserId()
        {
            if (_class != null)
            {
                return _class.CallStatic<string>("getGoogleUserId");
            }
            return "";
        }


        #endregion

        #region facebook

        /**
         * 登录Facebook
         */
        public void LogInFacebook()
        {
            if (_class != null)
            {
                _class.CallStatic("logInFacebook");
            }
        }

        /**
         * 登出Facebook
         */
        public void LogoutFacebook()
        {
            if (_class != null)
            {
                _class.CallStatic("logoutFacebook");
            }
        }

        /**
         * 查询Facebook 登录状态
         */
        public bool IsFacebookLoggedIn()
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("isFacebookLoggedIn");
            }
            return false;
        }

        /**
         * 查询Facebook用户 朋友列表
         */
        public string GetFacebookFriends()
        {
            if (_class != null)
            {
                return _class.CallStatic<string>("getFacebookFriends");
            }
            return "[]";
        }

        /**
         * 查询Facebook用户信息
         */
        public string GetFacebookUserInfo()
        {
            if (_class != null)
            {
                return _class.CallStatic<string>("getFacebookUserInfo");
            }
            return "{}";
        }
        #endregion

        #region firebase

        /**
         * 登出Firebase
         */
        public void LogoutFirebase()
        {
            if (_class != null)
            {
                _class.CallStatic("logoutFirebase");
            }
        }

        /**
         * 查询Firebase用户信息
         * @param channel   登陆渠道，参考 FirebaseLinkChannel
         */
        public string GetFirebaseUserInfo(string channel)
        {
            if (_class != null)
            {
                return _class.CallStatic<string>("getFirebaseUserInfo", channel);
            }
            return "{}";
        }

        /**
       * 查询Firebase用户 id
       * @param channel   登陆渠道，参考 FirebaseLinkChannel
       */
        public string GetFirebaseUserId()
        {
            if (_class != null)
            {
                return _class.CallStatic<string>("getFirebaseUserId");
            }
            return "";
        }

        /**
         * 查询Firebase是否为匿名登陆
         */
        public bool IsFirebaseAnonymousLoggedIn()
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("isFirebaseAnonymousLoggedIn");
            }
            return false;
        }

        /**
         * 查询Firebase是否登陆指定渠道
         * @param channel   登陆渠道，参考 FirebaseLinkChannel 
         */
        public bool IsFirebaseLinkedWithChannel(string channel)
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("isFirebaseLinkedWithChannel", channel);
            }
            return false;
        }

        /**
         * 查询Firebase是否可登出指定渠道
         * @param channel   登陆渠道，参考 FirebaseLinkChannel
         */
        public bool CanFirebaseUnlinkWithChannel(string channel)
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("canFirebaseUnlinkWithChannel", channel);
            }
            return false;
        }

        /**
         * Firebase登出指定渠道
         * @param channel   登陆渠道，参考 FirebaseLinkChannel
         */
        public void UnlinkFirebaseWithChannel(string channel)
        {
            if (_class != null)
            {
                _class.CallStatic("unlinkFirebaseWithChannel", channel);
            }
        }

        /**
         * 重载Firebase的登陆状态
         */
        public void ReloadFirebaseLogStatus()
        {
            if (_class != null)
            {
                _class.CallStatic("reloadFirebaseLogStatus");
            }
        }

        /**
         * 匿名登陆Firebase
         */
        public void LoginFBWithAnonymous()
        {
            if (_class != null)
            {
                _class.CallStatic("loginFBWithAnonymous");
            }
        }

        /**
         * 通过Google渠道登陆Firebase
         */
        public void LoginFBWithGoogle()
        {
            if (_class != null)
            {
                _class.CallStatic("loginFBWithGoogle");
            }
        }

        /**
         * 通过PlayGames渠道登陆Firebase
         */
        public void LoginFBWithPlayGames()
        {
            if (_class != null)
            {
                _class.CallStatic("loginFBWithPlayGames");
            }
        }

        /**
         * 通过Facebook渠道登陆Firebase
         */
        public void LoginFBWithFacebook()
        {
            if (_class != null)
            {
                _class.CallStatic("loginFBWithFacebook");
            }
        }

        /**
         * 通过Email渠道登陆Firebase
         */
        public void LoginFBWithEmailAndPwd(string email, string password)
        {
            if (_class != null)
            {
                _class.CallStatic("loginFBWithEmailAndPwd", email, password);
            }
        }

        #endregion

        #region 存档
        /**
         * 存储数据到指定数据集合
         * @param collection     数据集合
         * @param jsonData       
         */
        public void SaveCloudData(string collection, string documentId, string jsonData)
        {
            if (_class != null)
            {
                _class.CallStatic("saveCloudData", collection, documentId, jsonData);
            }
        }

        /**
         * 读取指定数据集合内文档
         * @param collection     数据集合
         * @param documentId     文档id
         */
        public void ReadCloudData(string collection, string documentId)
        {
            if (_class != null)
            {
                _class.CallStatic("readCloudData", collection, documentId);
            }
        }

        /**
         * 合并数据
         * @param collection     数据集合
         * @param jsonData
         */
        public void MergeCloudData(string collection, string documentId, string jsonData)
        {
            if (_class != null)
            {
                _class.CallStatic("mergeCloudData", collection, documentId, jsonData);
            }
        }

        /**
         * 查询数据
         * @param collection     数据集合
         */
        public void QueryCloudData(string collection, string documentId)
        {
            if (_class != null)
            {
                _class.CallStatic("queryCloudData", collection, documentId);
            }
        }

        /**
         * 删除数据
         * @param collection     数据集合
         */
        public void DeleteCloudData(string collection, string documentId)
        {
            if (_class != null)
            {
                _class.CallStatic("deleteCloudData", collection, documentId);
            }
        }

        /**
         * 更新数据 
         * @param collection        数据集合
         * @param transactionId     事务Id
         * @param jsonData      
         */
        public void UpdateCloudData(string collection, string documentId, string transactionId, string jsonData)
        {
            if (_class != null)
            {
                _class.CallStatic("updateCloudData", collection, documentId, transactionId, jsonData);
            }
        }

        /**
         * 备份数据
         * @param collection    数据集合
         * @param documentId    文档id
         */
        public void SnapshotCloudData(string collection, string documentId)
        {
            if (_class != null)
            {
                _class.CallStatic("snapshotCloudData", collection, documentId);
            }
        }

        #endregion

        #region 客服
        /**
         * 客服 准备状态
         */
        public bool IsHelperInitialized()
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("isHelperInitialized");
            }
            return false;
        }

        /**
         * 是否有新的客服消息
         */
        public bool HasNewHelperMessage()
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("hasNewHelperMessage");
            }
            return false;
        }

        /**
         * 跳转客服页面
         * @param entranceId            自定义入口 ID
         * @param meta                  自定义用户属性， JSONObject 格式
         * @param tags                  用户标签，AIHelp需要预先在后台定义用户标签; JSONArray 格式
         * @param welcomeMessage        欢迎语
         */
        public void ShowHelper(string entranceId, string meta, string tags, string welcomeMessage)
        {
            if (_class != null)
            {
                _class.CallStatic("showHelper", entranceId, meta, tags, welcomeMessage);
            }
        }

        /**
         * 跳转指定客服页面
         * @param faqId     指定页面id
         * @param monment   
         */
        public void ShowHelperSingleFAQ(string faqId, int moment = 3)
        {
            if (_class != null)
            {
                _class.CallStatic("showHelperSingleFAQ", faqId, moment);
            }
        }

        /**
         * 监听未读消息
         */
        public void ListenHelperUnreadMsgCount(bool onlyOnce)
        {
            if (_class != null)
            {
                _class.CallStatic("listenHelperUnreadMsgCount", onlyOnce);
            }
        }

        /**
         * 停止监听未读消息
         */
        public void StopListenHelperUnreadMsgCount()
        {
            if (_class != null)
            {
                _class.CallStatic("stopListenHelperUnreadMsgCount");
            }
        }

        /**
         * 更新用户属性
         * @param data      用户属性，JSONObject格式
         * @param tags      用户标签，AIHelp需要预先在后台定义用户标签,逗号分隔的字符串
         */
        public void UpdateHelperUserInfo(string data, string tags)
        {
            if (_class != null)
            {
                _class.CallStatic("updateHelperUserInfo", data, tags);
            }
        }

        /**
         * 重置用户属性
         */
        public void ResetHelperUserInfo()
        {
            if (_class != null)
            {
                _class.CallStatic("resetHelperUserInfo");
            }
        }

        /**
         * 关闭客服
         */
        public void CloseHelper()
        {
            if (_class != null)
            {
                _class.CallStatic("closeHelper");
            }
        }

        #endregion

        #region 通知
        /**
         *  通知权限
         *  @returns        0: 权限被彻底拒绝，需要跳转设置页面开启
         *                  1: 权限已开启
         *                  2: 权限状态待定，仍可通过系统接口请求
         */
        public int LoadNotificationPermissionState()
        {
            if (_class != null)
            {
                return _class.CallStatic<int>("loadNotificationPermissionState");
            }
            return 0;
        }

        /**
         * 请求通知权限
         */
        public void RequestNotificationPermission()
        {
            if (_class != null)
            {
                _class.CallStatic("requestNotificationPermission");
            }
        }

        /**
         * 跳转通知权限设置页
         */
        public void OpenNotificationSettings()
        {
            if (_class != null)
            {
                _class.CallStatic("openNotificationSettings");
            }
        }

        /**
         *
         * @param tag                   通知任务唯一标志
         * @param title                 通知栏标题
         * @param subtitle              通知栏副标题
         * @param bigText               长文本
         * @param smallIcon             状态栏角标，放在assets目录下，传入完整文件名称
         * @param largeIcon             通知栏ICON，放在assets目录下，传入完整文件名称
         * @param bigPicture            大图通知栏，放在assets目录下，传入完整文件名称
         * @param pushTime              推送时间，单位毫秒
         * @param interval              两次推送的间隔，单位毫秒，最短间隔15分钟
         * @param autoCancel            是否可自动关闭
         * @param action                通知栏事件数据
         * @param repeat                是否循环展示
         * 
         */
        public void PushNotificationTask(string tag, string title, string subtitle, string bigText, string smallIcon, string largeIcon, string bigPicture, long pushTime, long interval, bool autoCancel, string action, bool repeat)
        {
            if (_class != null)
            {
                _class.CallStatic("pushNotificationTask", tag, title, subtitle, bigText, smallIcon, largeIcon, bigPicture, pushTime, interval, autoCancel, action, repeat);
            }
        }

        public void CancelNotification(string tag)
        {
            if (_class != null)
            {
                _class.CallStatic("cancelNotification", tag);
            }
        }

        public void CancelAllNotification()
        {
            if (_class != null)
            {
                _class.CallStatic("cancelAllNotification");
            }
        }

        public void AddBootNotification(string tag, string title, string subtitle, string bigText, string smallIcon, string largeIcon, string bigPicture, string action)
        {
            if (_class != null)
            {
                _class.CallStatic("addBootNotification", tag, title, subtitle, bigText, smallIcon, largeIcon, bigPicture, action);
            }
        }

        public void CancelBootNotification(string tag)
        {
            if (_class != null)
            {
                _class.CallStatic("removeBootNotification", tag);
            }
        }

        public void ClearAllBootNotification()
        {
            if (_class != null)
            {
                _class.CallStatic("clearBootNotification");
            }
        }

        #endregion

        #region Appsflyer 用户互邀

        /**
         * 通过af邀请用户
         * @param inviterId         邀请者id
         * @param inviterAppId      邀请者 app id
         */
        public void AppsflyerInviteUser(string inviterId, string inviterAppId)
        {
            if (_class != null)
            {
                _class.CallStatic("appsflyerInviteUser", inviterId, inviterAppId);
            }
        }

        /**
         * @returns inviterId  格式为 inviterId|inviterAppId
         */
        public string GetAppsflyerInviterId()
        {
            if (_class != null)
            {
                return _class.CallStatic<string>("getAppsflyerInviterId");
            }
            return "";
        }

        #endregion

        public void SendEmail(string email, string extra)
        {
            if (_class != null)
            {
                _class.CallStatic<int>("sendEmail", email, extra);
            }
        }

        public void SendEmail(string email, string title, string extra)
        {
            if (_class != null)
            {
                _class.CallStatic<int>("sendEmail", email, title, extra);
            }
        }

        public string GetConfig(ConfigKeys key)
        {
            if (_class != null)
            {
                return _class.CallStatic<string>("getConfig", ((int)key));
            }
            return "";
        }

        public bool IsNetworkConnected()
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("isNetworkConnected");
            }
            return true;
        }

        public void Rate()
        {
            if (_class != null)
            {
                _class.CallStatic("rate");
            }
        }

        public void SystemShareText(string txt)
        {
            if (_class != null)
            {
                _class.CallStatic("systemShareText", txt);
            }
        }

        public void SystemShareImage(string title, string imagePath)
        {
            if (_class != null)
            {
                _class.CallStatic("systemShareImage", title, imagePath);
            }
        }

        public void OpenUrl(string url)
        {
            if (_class != null)
            {
                _class.CallStatic("openUrl", url);
            }
        }

        public void ShowWebView(string title, string url)
        {
            if (_class != null)
            {
                _class.CallStatic("showWebView", title, url);
            }
        }

        public bool HasNotch()
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("hasNotch");
            }
            return false;
        }

        public int GetNotchHeight()
        {
            if (_class != null)
            {
                return _class.CallStatic<int>("getNotchHeight");
            }
            return 0;
        }

        public bool HasGestureBar()
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("hasGestureBar");
            }
            return false;
        }

        public int GetGestureBarHeight()
        {
            if (_class != null)
            {
                return _class.CallStatic<int>("getGestureBarHeight");
            }
            return 0;
        }

        /**
         * 跳转应用商店
         * @param url           1.null，指定本游戏；2.指定游戏包名；3.应用商店地址
         */
        public void OpenAppStore(string url)
        {
            if (_class != null)
            {
                _class.CallStatic("openAppStore", url, null);
            }
        }

        public void Toast(string message)
        {
#if UNITY_EDITOR
        RiseEditorAd.EditorAdInstance.Toast(message);
#endif
            if (_class != null)
            {
                _class.CallStatic("toast", message);
            }
        }

        public void copyTxt(string message)
        {
            if (_class != null)
            {
                _class.CallStatic("copyTxt", message);
            }
        }

        /**
         * 总内存，单位MB
         */
        public int GetTotalMemory()
        {
            if (_class != null)
            {
                return _class.CallStatic<int>("getTotalMemory");
            }
            return 0;
        }

        /**
         * 可用内存，单位MB
         */
        public int GetFreeMemory()
        {
            if (_class != null)
            {
                return _class.CallStatic<int>("getFreeMemory");
            }
            return 0;
        }

        /**
         * 总磁盘存储，单位MB
         */
        public int GetDiskSize()
        {
            if (_class != null)
            {
                return _class.CallStatic<int>("getDiskSize");
            }
            return 0;
        }

        /**
         * 可用磁盘存储，单位MB
         */
        public int GetFreeDiskSize()
        {
            if (_class != null)
            {
                return _class.CallStatic<int>("getFreeDiskSize");
            }
            return 0;
        }

        public void GoLauncher()
        {
            if (_class != null)
            {
                _class.CallStatic("goLauncher");
            }
        }

        public void ForceQuit()
        {
            if (_class != null)
            {
                _class.CallStatic("forceQuit");
            }
        }

        /**
         * 跳转facebook公共主页
         * @param pageId 公共主页id
         */
        public void OpenFacebookPage(string pageId)
        {
            if (_class != null)
            {
                _class.CallStatic("openFacebookPage", pageId);
            }
        }

        public bool IsDebug()
        {
            if (_class != null)
            {
                return _class.CallStatic<bool>("isDebug");
            }
            return false;
        }

         /**
         * @param duration  震动时长，单位毫秒
         * @param amplitude 震动强度， 1 ~ 255
         */
        public void Vibrate(long duration, int amplitude) {
            if (_class != null)
            {
                return _class.CallStatic("vibrate", duration, amplitude);
            }
        }

        /**
         * @param stepDuration 每个震动点的震动时长， 逗号分隔的字符串； 例如 1，2，3
         * @param curve        每个震动点的震动强度，范围在 1~255； 逗号分隔的字符串，例如 1，2，3
         * @implNote stepDuration 和 curve 的长度必须一致
         */
        public void VibrateWithCure(string stepDuration, string curve) {
            if (_class != null)
            {
                return _class.CallStatic("vibrateWithCure", stepDuration, curve);
            }
        }


#elif UNITY_IOS
        [DllImport ("__Internal")]
        private static extern void onCreate();
        [DllImport ("__Internal")]
        private static extern void requestATT();
        [DllImport ("__Internal")]
        private static extern void requestATTForCustomUMP();
        [DllImport ("__Internal")]
        private static extern int loadATTStatus();
        [DllImport ("__Internal")]
        private static extern void triggerBannerAd(string placement);
        [DllImport("__Internal")]
        private static extern void setBxId(string bxid);
        [DllImport ("__Internal")]
        private static extern bool hasBannerAd();
        [DllImport ("__Internal")]
        private static extern void showBannerAd(string tag, int pos, string placemnet, string clientInfo);
        [DllImport ("__Internal")]
        private static extern void closeBannerAd(string placement);
        [DllImport ("__Internal")]
        private static extern void triggerInterstitialAd(string placement);
        [DllImport ("__Internal")]
        private static extern bool hasInterstitialAd();
        [DllImport ("__Internal")]
        private static extern void showInterstitialAd(string tag, string placement, string clientInfo);
        [DllImport ("__Internal")]
        private static extern void triggerRewardedAd(string placement);
        [DllImport ("__Internal")]
        private static extern bool hasRewardedAd();
        [DllImport ("__Internal")]
        private static extern void showRewardedAd(string tag, string placement, string clientInfo);
        [DllImport ("__Internal")]
        private static extern void pay(int payId, string payload, string clientInfo);
        [DllImport ("__Internal")]
        private static extern void shippingGoods(string merchantTransactionId);
        [DllImport ("__Internal")]
        private static extern void queryPaymentOrders();
        [DllImport ("__Internal")]
        private static extern string getPaymentData(int payId);
        [DllImport ("__Internal")]
        private static extern string getPaymentDatas();
        [DllImport ("__Internal")]
        private static extern bool isPaymentValid();
        [DllImport("__Internal")]
        private static extern void restorePayments();
        [DllImport ("__Internal")]
        private static extern void trackEvent(string eventName, string param);
        [DllImport ("__Internal")]
        private static extern void trackEventToConversion(string eventName, string param);
        [DllImport ("__Internal")]
        private static extern void trackEventToFacebook(string eventName, string param);
        [DllImport ("__Internal")]
        private static extern void trackEventToFirebase(string eventName, string param);
        [DllImport ("__Internal")]
        private static extern void trackEventToAppsflyer(string eventName, string param);
        [DllImport ("__Internal")]
        private static extern void trackEventToIvy(string eventName, string param);
        [DllImport ("__Internal")]
        private static extern int getRemoteConfigInt(string key);
        [DllImport ("__Internal")]
        private static extern long getRemoteConfigLong(string key);
        [DllImport ("__Internal")]
        private static extern double getRemoteConfigDouble(string key);
        [DllImport ("__Internal")]
        private static extern bool getRemoteConfigBoolean(string key);
        [DllImport ("__Internal")]
        private static extern string getRemoteConfigString(string key);
        [DllImport ("__Internal")]
        private static extern int getIvyRemoteConfigInt(string key);
        [DllImport ("__Internal")]
        private static extern long getIvyRemoteConfigLong(string key);
        [DllImport ("__Internal")]
        private static extern double getIvyRemoteConfigDouble(string key);
        [DllImport ("__Internal")]
        private static extern bool getIvyRemoteConfigBoolean(string key);
        [DllImport ("__Internal")]
        private static extern string getIvyRemoteConfigString(string key);
        [DllImport("__Internal")]
        private static extern bool isAuthPlatformReady();
        [DllImport ("__Internal")]
        private static extern void loginFacebook();
        [DllImport ("__Internal")]
        private static extern void isFacebookLoggedIn();
        [DllImport ("__Internal")]
        private static extern void logoutFacebook();
        [DllImport ("__Internal")]
        private static extern string getFacebookUserId();
        [DllImport ("__Internal")]
        private static extern string getFacebookUserInfo();
        [DllImport ("__Internal")]
        private static extern string getFacebookFriends();
        [DllImport ("__Internal")]
        private static extern void loginApple();
        [DllImport ("__Internal")]
        private static extern void logoutApple();
        [DllImport("__Internal")]
        private static extern void isAppleLoggedIn();
        [DllImport ("__Internal")]
        private static extern string getAppleUserId();
        [DllImport ("__Internal")]
        private static extern string getAppleUserInfo();
        [DllImport ("__Internal")]
        private static extern void logoutFirebase();
        [DllImport ("__Internal")]
        private static extern string getFirebaseUserInfo(string channel);
        [DllImport ("__Internal")]
        private static extern string getFirebaseUserId();
        [DllImport ("__Internal")]
        private static extern bool isFirebaseAnoymousLoggedIn();
        [DllImport ("__Internal")]
        private static extern bool isFirebaseLinkedWithChannel(string channel);
        [DllImport ("__Internal")]
        private static extern bool canFirebaseUnlinkWithChannel(string channel);
        [DllImport ("__Internal")]
        private static extern void unlinkFirebaseWithChannel(string channel);
        [DllImport ("__Internal")]
        private static extern void reloadFirebaseLogStatus();
        [DllImport ("__Internal")]
        private static extern void loginFBWithAnoymous();
        [DllImport ("__Internal")]
        private static extern void loginFBWithApple();
        [DllImport ("__Internal")]
        private static extern void loginFBWithFacebook();
        [DllImport ("__Internal")]
        private static extern void loginFBWithEmailAndPwd(string email, string password);
        [DllImport ("__Internal")]
        private static extern void firebaseCloudFunction(string method, string param);
        [DllImport ("__Internal")]
        private static extern void saveCloudData(string collection, string documentId, string jsonData);
        [DllImport ("__Internal")]
        private static extern void readCloudData(string collection, string documentId);
        [DllImport ("__Internal")]
        private static extern void mergeCloudData(string collection, string documentId, string jsonData);
        [DllImport ("__Internal")]
        private static extern void queryCloudData(string collection, string documentId);
        [DllImport ("__Internal")]
        private static extern void deleteCloudData(string collection, string documentId);
        [DllImport ("__Internal")]
        private static extern void updateCloudData(string collection, string documentId, string transactionId, string jsonData);
        [DllImport ("__Internal")]
        private static extern void snapshotCloudData(string collection, string documentId);
        [DllImport ("__Internal")]
        private static extern string getAppsflyerInviterId();
        [DllImport ("__Internal")]
        private static extern void appsflyerInviteUser(string inviterId, string inviterAppId);
        [DllImport ("__Internal")]
        private static extern string getConfig(int key);
        [DllImport("__Internal")]
        private static extern void loadNotificationPermissionState();
        [DllImport("__Internal")]
        private static extern void requestNotificationPermission();
        [DllImport("__Internal")]
        private static extern void openNotificationSettings();
        [DllImport("__Internal")]
        private static extern void cancelNotificationWithIdentifier(string key);
        [DllImport("__Internal")]
        private static extern void cancelAllNotification();
        [DllImport("__Internal")]
        private static extern string getLocalNotificationData();
        [DllImport("__Internal")]
        private static extern void sendNotification(string key, string title, string msg, long pushTime, long interval, bool repeat, bool useSound, string soundName, string userInfo);
        [DllImport("__Internal")]
        private static extern void sendNotificationWithDate(string key, string title, string msg, string dateStr, long interval, bool repeat, bool useSound, string soundName, string userInfo);
        [DllImport("__Internal")]
        private static extern bool isHelperInitialized();
        [DllImport("__Internal")]
        private static extern bool hasNewHelperMessage();
        [DllImport("__Internal")]
        private static extern void showHelper(string entraceId, string meta, string tags, string welcome);
        [DllImport("__Internal")]
        private static extern void showHelperSingleFAQ(string faqId, int monment);
        [DllImport("__Internal")]
        private static extern void listenHelperUnreadMsgCount(bool onlyOnce);
        [DllImport("__Internal")]
        private static extern void stopListenHelperUnreadMsgCount();
        [DllImport("__Internal")]
        private static extern void updateHelperUserInfo(string meta, string tags);
        [DllImport("__Internal")]
        private static extern void resetHelperUserInfo();
        [DllImport("__Internal")]
        private static extern void closeHelper();
        [DllImport("__Internal")]
        private static extern void openFacebookPage(string pageId);
        [DllImport("__Internal")]
        private static extern bool hasNotch();
        [DllImport("__Internal")]
        private static extern int getNotchHeight();
        [DllImport("__Internal")]
        private static extern bool hasGestureBar();
        [DllImport("__Internal")]
        private static extern int getGestureBarHeight();
        [DllImport("__Internal")]
        private static extern bool isNetworkConnected();
        [DllImport("__Internal")]
        private static extern void copyText(string text);
        [DllImport("__Internal")]
        private static extern void sendEmail(string email, string content);
        [DllImport("__Internal")]
        private static extern long getFreeMemory();
        [DllImport("__Internal")]
        private static extern long getTotalMemory();
        [DllImport("__Internal")]
        private static extern long getFreeDiskSize();
        [DllImport("__Internal")]
        private static extern long getTotalDiskSize();
        [DllImport("__Internal")]
        private static extern void openAppStore(string appStoreId);
        [DllImport ("__Internal")]
        private static extern void showToast(string message);
        [DllImport("__Internal")]
        private static extern void openUrl(string url);
        [DllImport("__Internal")]
        private static extern bool isIpad();
        [DllImport("__Internal")]
        private static extern bool isDebug();
        [DllImport("__Internal")]
        private static extern void vibrate(int degree);
        [DllImport("__Internal")]
        private static extern void playAHAP(string file_name, string folder);

        [DllImport("__Internal")]
        private static extern void addShortcut(string type, string title, string subtitle, string icon);
        [DllImport("__Internal")]
        private static extern void deleteShortcut(string type);
        [DllImport("__Internal")]
        private static extern string getShortcutAction();
        [DllImport("__Internal")]
        private static extern void showWebView(string url, int x, int y, int w, int h);
        [DllImport("__Internal")]
        private static extern bool isWebViewDisplayed();
        [DllImport("__Internal")]
        private static extern void closeWebView();
        [DllImport("__Internal")]
        private static extern void loginGameCenter();
        [DllImport("__Internal")]
        private static extern void showGameCenter();
        [DllImport("__Internal")]
        private static extern void showLeaderboards();
        [DllImport("__Internal")]
        private static extern void showLeaderboard(string leaderboardId);
        [DllImport("__Internal")]
        private static extern void submitLeaderboard(string leaderboardId, long score);
        [DllImport("__Internal")]
        private static extern long getLeaderboardScore(string leaderboardId);
        [DllImport("__Internal")]
        private static extern void showAchievements();
        [DllImport("__Internal")]
        private static extern void submitAchievement(string achievementId, double percent);
        [DllImport("__Internal")]
        private static extern double getAchievementProgress(string achievementId);
        [DllImport("__Internal")]
        private static extern void resetAchievements();
        [DllImport("__Internal")]
        private static extern void showChallenges();
        [DllImport("__Internal")]
        private static extern void enableAbTest(int abChannel);
        [DllImport("__Internal")]
        private static extern void setHeaderInfo(string data);
        [DllImport("__Internal")]
        private static extern void setUserInfo(string data);
        [DllImport("__Internal")]
        private static extern void startTrackEvent();
        [DllImport("__Internal")]
        private static extern void pullAbTestConfigs();
        [DllImport("__Internal")]
        private static extern string getAllAbConfigs();
        [DllImport("__Internal")]
        private static extern string getAbConfigString(string key, string defaultValue);
        [DllImport("__Internal")]
        private static extern double getAbConfigDouble(string key, double defaultValue);
        [DllImport("__Internal")]
        private static extern float getAbConfigFloat(string key, float defaultValue);
        [DllImport("__Internal")]
        private static extern long getAbConfigLong(string key, long defaultValue);
        [DllImport("__Internal")]
        private static extern int getAbConfigInt(string key, int defaultValue);
        [DllImport("__Internal")]
        private static extern bool getAbConfigBoolean(string key, bool defaultValue);
        [DllImport("__Internal")]
        private static extern string getAbTestSSID();
        [DllImport("__Internal")]
        private static extern void setCustomUserId(string userId);





        private bool hasCalledInit = false;

        public void Init()
        {
            RiseEditorAd.hasInit = true;
            if (hasCalledInit)
            {
                return;
            }
            hasCalledInit = true;
            IvySdkListener.Instance.enabled = true;
            onCreate();
        }

        public void EnableAbTest(ABChannel channel)
        {
            enableAbTest((int)channel);
        }

        public void SetHeaderInfo(Dictionary<string, object> data)
        {
            try
            {
                string param = "{}";
                if (data != null)
                {
                    param = IvyJson.Serialize(data);
                }
                setHeaderInfo(param);
            }
            catch (Exception)
            {
            }
        }

        public void SetUserInfo(Dictionary<string, object> data)
        {
            try
            {
                string param = "{}";
                if (data != null)
                {
                    param = IvyJson.Serialize(data);
                }
                setUserInfo(param);
            }
            catch (Exception)
            {
            }
        }

        public void StartTrackEvent()
        {
            startTrackEvent();
        }

        public void PullAbTestConfigs()
        {
            pullAbTestConfigs();
        }

        public string GetHuoShanSSID()
        {
            return getAbTestSSID();
        }

        public string GetAllAbConfigs()
        {

            return getAllAbConfigs();
        }

        public bool GetAbConfigBoolean(string key, bool defaultValue)
        {

            return getAbConfigBoolean(key, defaultValue);
        }

        public int GetAbConfigInt(string key, int defaultValue)
        {
            return getAbConfigInt(key, defaultValue);
        }

        public long GetAbConfigLong(string key, long defaultValue)
        {
            return getAbConfigLong(key, defaultValue);
        }

        public float GetAbConfigFloat(string key, float defaultValue)
        {
            return getAbConfigFloat(key, defaultValue);
        }

        public double GetAbConfigDouble(string key, double defaultValue)
        {
            return getAbConfigDouble(key, defaultValue);
        }

        public string GetAbConfigString(string key, string defaultValue)
        {
            return getAbConfigString(key, defaultValue);
        }

        public void RequestATT()
        {
            requestATT();
        }

        public void RequestATTForCustomUMP()
        {
            requestATTForCustomUMP();
        }

        public int LoadATTStatus()
        {
            return loadATTStatus();
        }

        #region ads

        // bxid = old, 执行老sdk 广告逻辑； 其它值请和运营确定
        public void SetBxId(string bxid)
        {
            setBxId(bxid);
        }

        public bool HasBannerAd()
        {
            return hasBannerAd();
        }

        public void TriggerBannerAd(string placement)
        {
            triggerBannerAd(placement);
        }

        public void ShowBannerAd(string tag, BannerAdPosition position, string placement)
        {
            showBannerAd(tag, (int)position, placement, null);
        }

        /**
         *  展示banner 广告
         *  @param tag          广告标签，默认为 default
         *  @param position     广告位置，参考BannerAdPosition
         *  @param placement    广告位，
         *  @param clientInfo   客户端自定义信息，JSONObject结构，注意 bool值会被转换位1/0
         * 
         */
        public void ShowBannerAd(string tag, BannerAdPosition position, string placement, string clientInfo)
        {
            showBannerAd(tag, (int)position, placement, clientInfo);
        }

        /**
         *  关闭banner 广告
         *  @param placement    广告位
         */
        public void CloseBannerAd(string placement)
        {
            closeBannerAd(placement);
        }

        public bool HasInterstitialAd()
        {
            return hasInterstitialAd();
        }

        public void TriggerInterstitialAd(string placement)
        {
            triggerInterstitialAd(placement);
        }

        /**
         *  展示 插屏 广告
         *  @param tag          广告标签，默认为 default
         *  @param placement    广告位，
         *  @param clientInfo   客户端自定义信息，JSONObject结构，注意 bool值会被转换位1/0
         */
        public void ShowInterstitialAd(string tag, string placement, string clientInfo = null)
        {
            showInterstitialAd(tag, placement, clientInfo);
        }

        public bool HasRewardedAd()
        {
            return hasRewardedAd();
        }

        public void TriggerRewardedAd(string placement)
        {
            triggerRewardedAd(placement);
        }

        /**
         *  展示 激励视频 广告
         *  @param tag          广告标签，默认为 default
         *  @param placement    广告位，可以用于标记奖励点
         *  @param clientInfo   客户端自定义信息，JSONObject结构，注意 bool值会被转换位1/0
         */
        public void ShowRewardedAd(string tag, string placement, string clientInfo = null)
        {
            showRewardedAd(tag, placement, clientInfo);
        }

        #endregion

        #region 计费

        public void Pay(int id)
        {
            pay(id, null, null);
        }

        public void Pay(int id, string payload)
        {
            pay(id, payload, null);
        }

        /**
         *  支付
         *  @param id           计费点位id
         *  @param payload      
         *  @param clientInfo   客户端自定义信息，JSONObject结构，注意 bool值会被转换位1/0
         */
        public void Pay(int id, string payload, string clientInfo)
        {
            pay(id, payload, clientInfo);
        }

        /**
         *  如果在使用在线计费校验时，请在客户端发放奖励时调用此接口通知服务端发货
         *  @param merchantTransactionId    预下单id
         */
        public void ShippingGoods(string merchantTransactionId)
        {
            shippingGoods(merchantTransactionId);
        }

        /**
         * 查询指定计费点位是否存在未处理支付记录
         * @param id    计费点位 id 
         */
        public void QueryPaymentOrder(int id)
        {
            queryPaymentOrders();
        }

        /**
         * 查询所有未处理支付记录
         */
        public void QueryPaymentOrders()
        {
            queryPaymentOrders();
        }

        /**
         *  查询指定计费点位详情
         */
        public string GetPaymentData(int id)
        {
            return getPaymentData(id);
        }

        /**
         *  查询所有计费点位详情
         */
        public string GetPaymentDatas()
        {
            return getPaymentDatas();
        }

        /**
         * 计费系统是否可用
         */
        public bool IsPaymentValid()
        {
            return isPaymentValid();
        }

        public void RestorePayments()
        {
            restorePayments();
        }

        #endregion

        #region track
        /**
         * 统计事件至 所有平台
         * @params  eventName    事件名
         *          data         事件属性，字典结构
         */
        public void TrackEvent(string eventName, Dictionary<string, object> data)
        {
            try
            {
                string param = "{}";
                if(data != null)
                {
                    param = IvyJson.Serialize(data);
                }
                trackEvent(eventName, param);
            }
            catch (Exception)
            {
                Debug.LogError($"track event:{eventName} failed!!!, param convert failed");
            }
        }

        /**
         * 统计事件至 所有平台
         * @params  eventName    事件名
         *          data         事件属性，字典结构
         */
        public void TrackEventToConversion(string eventName, Dictionary<string, object> data)
        {
            try
            {
                string param = "{}";
                if (data != null)
                {
                    param = IvyJson.Serialize(data);
                }
                trackEventToConversion(eventName, param);
            }
            catch (Exception)
            {
                Debug.LogError($"track event to conversion:{eventName} failed!!!, param convert failed");
            }
        }

        /**
          * 统计事件至 Firebase
          * @params  eventName    事件名
          *          data         事件属性，字典结构
          */
        public void TrackEventToFirebase(string eventName, Dictionary<string, object> data)
        {
            try
            {
                string param = "{}";
                if (data != null)
                {
                    param = IvyJson.Serialize(data);
                }
                trackEventToFirebase(eventName, param);
            }
            catch (Exception)
            {
                Debug.LogError($"track event to firebase:{eventName} failed!!!, param convert failed");
            }
        }

        /**
          * 统计事件至 Facebook
          * @params  eventName    事件名
          *          data         事件属性，字典结构
          */
        public void TrackEventToFacebook(string eventName, Dictionary<string, object> data)
        {
            try
            {
                string param = "{}";
                if (data != null)
                {
                    param = IvyJson.Serialize(data);
                }
                trackEventToFacebook(eventName, param);
            }
            catch (Exception)
            {
                Debug.LogError($"track event to facebook:{eventName} failed!!!, param convert failed");
            }
        }

        /**
          * 统计事件至 Appsflyer
          * @params  eventName    事件名
          *          data         事件属性，字典结构
          */
        public void TrackEventToAppsflyer(string eventName, Dictionary<string, object> data)
        {
            try
            {
                string param = "{}";
                if (data != null)
                {
                    param = IvyJson.Serialize(data);
                }
                trackEventToAppsflyer(eventName, param);
            }
            catch (Exception)
            {
                Debug.LogError($"track event to appsflyer:{eventName} failed!!!, param convert failed");
            }
        }

        /**
          * 统计事件至 自有平台
          * @params  eventName    事件名
          *          data         事件属性，字典结构
          */
        public void TrackEventToIvy(string eventName, Dictionary<string, object> data)
        {
            try
            {
                string param = "{}";
                if (data != null)
                {
                    param = IvyJson.Serialize(data);
                }

                trackEventToIvy(eventName, param);
            }
            catch (Exception)
            {
                Debug.LogError($"track event to ivy:{eventName} failed!!!, param convert failed");
            }
        }

        /**
         *  设置用户属性 至 所有平台
         */
        //public void SetUserProperty(string key, string value)
        //{
           
        //}

        /**
         *  设置用户属性 至 Firebase
         */
        //public void SetUserPropertyToFirebase(string key, string value)
        //{
           
        //}

        /**
         *  设置用户属性 至 Appsflyer
         */
        //public void SetUserPropertyToAppsflyer(string key, string value)
        //{
        //    if (_class != null)
        //    {
        //        _class.CallStatic("setUserPropertyToAppsflyer", key, value);
        //    }
        //}

        /**
         *  设置用户属性 至 自有平台
         */
        //public void SetUserPropertyToIvy(string key, string value)
        //{
            
        //}

        /**
         *  设置自定义用户 id
         */
        public void SetCustomUserId(string value)
        {
            setCustomUserId(value);
        }

        #endregion

        public bool IsAuthPlatformReady()
        {
            return isAuthPlatformReady();
        }

        #region Apple login
        public void LoginApple()
        {
            loginApple();
        }

        public void LogoutApple()
        {
            logoutApple();
        }

        public void IsAppleLoggedIn()
        {
            isAppleLoggedIn();
        }

        public string GetAppleUserId()
        {
            return getAppleUserId();
        }

        public string GetAppleUserInfo()
        {
            return getAppleUserInfo();
        }

        #endregion

        #region firebase cloud function
        public void FirebaseCloudFunction(string functionName)
        {
            firebaseCloudFunction(functionName, null);
        }

        /**
         * @param   functionName    方法名
         *          parameters      参数，要求JSONObject格式
         */
        public void FirebaseCloudFunction(string functionName, string parameters)
        {
            firebaseCloudFunction(functionName, parameters);
        }
        #endregion

        #region remote config
        /**
         * 获取 Firebase Remote Config 配置值
         */
        public int GetRemoteConfigInt(string key)
        {
            return getRemoteConfigInt(key);
        }

        /**
         * 获取 Firebase Remote Config 配置值
         */
        public long GetRemoteConfigLong(string key)
        {
            return getRemoteConfigLong(key);
        }

        /**
         * 获取 Firebase Remote Config 配置值
         */
        public double GetRemoteConfigDouble(string key)
        {
            return getIvyRemoteConfigDouble(key);
        }

        /**
         * 获取 Firebase Remote Config 配置值
         */
        public bool GetRemoteConfigBoolean(string key)
        {
            return getRemoteConfigBoolean(key);
        }

        /**
         * 获取 Firebase Remote Config 配置值
         */
        public string GetRemoteConfigString(string key)
        {
            return getRemoteConfigString(key);
        }

        /**
         * 获取 自有 Remote Config 配置值
         */
        public int GetIvyRemoteConfigInt(string key)
        {
            return getIvyRemoteConfigInt(key);
        }

        /**
         * 获取 自有 Remote Config 配置值
         */
        public long GetIvyRemoteConfigLong(string key)
        {
            return getIvyRemoteConfigLong(key);
        }

        /**
         * 获取 自有 Remote Config 配置值
         */
        public double GetIvyRemoteConfigDouble(string key)
        {
            return getIvyRemoteConfigDouble(key);
        }

        /**
         * 获取 自有 Remote Config 配置值
         */
        public bool GetIvyRemoteConfigBoolean(string key)
        {
            return getIvyRemoteConfigBoolean(key);
        }

        /**
         * 获取 自有 Remote Config 配置值
         */
        public string GetIvyRemoteConfigString(string key)
        {
            return getIvyRemoteConfigString(key);
        }
        #endregion

        #region facebook

        /**
         * 登录Facebook
         */
        public void LogInFacebook()
        {
            loginFacebook();
        }

        /**
         * 登出Facebook
         */
        public void LogoutFacebook()
        {
            logoutFacebook();
        }

        /**
         * 查询Facebook 登录状态
         */
        public void IsFacebookLoggedIn()
        {
            isFacebookLoggedIn();
        }

        /**
         * 查询Facebook用户 朋友列表
         */
        public string GetFacebookFriends()
        {
            return getFacebookFriends();
        }

        /**
         * 查询Facebook用户信息
         */
        public string GetFacebookUserInfo()
        {
            return getFacebookUserInfo();
        }
        #endregion

        #region firebase
        

        /**
         * 登出Firebase
         */
        public void LogoutFirebase()
        {
            logoutFirebase();
        }

        /**
         * 查询Firebase用户信息
         * @param channel   登陆渠道，参考 FirebaseLinkChannel
         */
        public string GetFirebaseUserInfo(string channel)
        {
            return getFirebaseUserInfo(channel);
        }

        /**
       * 查询Firebase用户 id
       * @param channel   登陆渠道，参考 FirebaseLinkChannel
       */
        public string GetFirebaseUserId()
        {
            return getFirebaseUserId();
        }

        /**
         * 查询Firebase是否为匿名登陆
         */
        public bool IsFirebaseAnonymousLoggedIn()
        {
            return isFirebaseAnoymousLoggedIn();
        }

        /**
         * 查询Firebase是否登陆指定渠道
         * @param channel   登陆渠道，参考 FirebaseLinkChannel 
         */
        public bool IsFirebaseLinkedWithChannel(string channel)
        {
            return isFirebaseLinkedWithChannel(channel);
        }

        /**
         * 查询Firebase是否可登出指定渠道
         * @param channel   登陆渠道，参考 FirebaseLinkChannel
         */
        public bool CanFirebaseUnlinkWithChannel(string channel)
        {
            return canFirebaseUnlinkWithChannel(channel);
        }

        /**
         * Firebase登出指定渠道
         * @param channel   登陆渠道，参考 FirebaseLinkChannel
         */
        public void UnlinkFirebaseWithChannel(string channel)
        {
            unlinkFirebaseWithChannel(channel);
        }

        /**
         * 重载Firebase的登陆状态
         */
        public void ReloadFirebaseLogStatus()
        {
            reloadFirebaseLogStatus();
        }

        /**
         * 匿名登陆Firebase
         */
        public void LoginFBWithAnonymous()
        {
            loginFBWithAnoymous();
        }

        public void LoginFBWithApple()
        {
            loginFBWithApple();
        }

        /**
         * 通过Facebook渠道登陆Firebase
         */
        public void LoginFBWithFacebook()
        {
            loginFBWithFacebook();
        }

        /**
         * 通过Email渠道登陆Firebase
         */
        public void LoginFBWithEmailAndPwd(string email, string password)
        {
            loginFBWithEmailAndPwd(email, password);
        }

        #endregion

        #region 存档
        /**
         * 存储数据到指定数据集合
         * @param collection     数据集合
         * @param jsonData       
         */
        public void SaveCloudData(string collection, string documentId, string jsonData)
        {
            saveCloudData(collection, documentId, jsonData);
        }

        /**
         * 读取指定数据集合内文档
         * @param collection     数据集合
         * @param documentId     文档id
         */
        public void ReadCloudData(string collection, string documentId)
        {
            readCloudData(collection, documentId);
        }

        /**
         * 合并数据
         * @param collection     数据集合
         * @param jsonData
         */
        public void MergeCloudData(string collection, string documentId, string jsonData)
        {
            mergeCloudData(collection, documentId, jsonData);
        }

        /**
         * 查询数据
         * @param collection     数据集合
         */
        public void QueryCloudData(string collection, string documentId)
        {
            queryCloudData(collection, documentId);
        }

        /**
         * 删除数据
         * @param collection     数据集合
         */
        public void DeleteCloudData(string collection, string documentId)
        {
            deleteCloudData(collection, documentId);
        }

        /**
         * 更新数据 
         * @param collection        数据集合
         * @param transactionId     事务Id
         * @param jsonData      
         */
        public void UpdateCloudData(string collection, string documentId, string transactionId, string jsonData)
        {
            updateCloudData(collection, documentId, transactionId, jsonData);
        }

        /**
         * 备份数据
         * @param collection    数据集合
         * @param documentId    文档id
         */
        public void SnapshotCloudData(string collection, string documentId)
        {
            snapshotCloudData(collection, documentId);
        }

        #endregion

        #region 客服
        /**
         * 客服 准备状态
         */
        public bool IsHelperInitialized()
        {
            return isHelperInitialized();
        }

        /**
         * 是否有新的客服消息
         */
        public bool HasNewHelperMessage()
        {
            return hasNewHelperMessage();
        }

        /**
         * 跳转客服页面
         * @param entranceId            自定义入口 ID
         * @param meta                  自定义用户属性， JSONObject 格式
         * @param tags                  用户标签，AIHelp需要预先在后台定义用户标签; JSONArray 格式
         * @param welcomeMessage        欢迎语
         */
        public void ShowHelper(string entranceId, string meta, string tags, string welcomeMessage)
        {
            showHelper(entranceId, meta, tags, welcomeMessage);
        }

        /**
         * 跳转指定客服页面
         * @param faqId     指定页面id
         * @param monment   
         */
        public void ShowHelperSingleFAQ(string faqId, int moment = 3)
        {
            showHelperSingleFAQ(faqId, moment);
        }

        /**
         * 监听未读消息
         */
        public void ListenHelperUnreadMsgCount(bool onlyOnce)
        {
            listenHelperUnreadMsgCount(onlyOnce);
        }

        /**
         * 停止监听未读消息
         */
        public void StopListenHelperUnreadMsgCount()
        {
            stopListenHelperUnreadMsgCount();
        }

        /**
         * 更新用户属性
         * @param data      用户属性，JSONObject格式
         * @param tags      用户标签，AIHelp需要预先在后台定义用户标签,JSONArray 格式
         */
        public void UpdateHelperUserInfo(string data, string tags)
        {
            updateHelperUserInfo(data, tags);
        }

        /**
         * 重置用户属性
         */
        public void ResetHelperUserInfo()
        {
            resetHelperUserInfo();
        }

        /**
         * 关闭客服
         */
        public void CloseHelper()
        {
            closeHelper();
        }

        #endregion

        #region 通知
        /**
         *  通知权限
         *  @returns        0: 权限被彻底拒绝，需要跳转设置页面开启
         *                  1: 权限已开启
         *                  2: 权限状态待定，仍可通过系统接口请求
         */
        public void LoadNotificationPermissionState()
        {
            loadNotificationPermissionState();
        }

        /**
         * 请求通知权限
         */
        public void RequestNotificationPermission()
        {
            requestNotificationPermission();
        }

        /**
         * 跳转通知权限设置页
         */
        public void OpenNotificationSettings()
        {
            openNotificationSettings();
        }

        public enum NotificationInterval
        {
                  INTERVAL_YEAR = 4,
                  INTERVAL_MONTH = 8,
                  INTERVAL_DAY = 16,
                  INTERVAL_HOUR = 32,
                  INTERVAL_MINUTE = 64,
                  INTERVAL_WEEKDAY = 512,
        }

        public void PushNotificationTask(string tag, string title, string subtitle, long pushTime, NotificationInterval interval, bool repeat, bool useSound, string soundName, string userInfo)
        {
            sendNotification(tag, title, subtitle, pushTime, (long)interval, repeat, useSound, soundName, userInfo);
        }

        public void PushNotificationTask(string tag, string title, string subtitle, string dateStr, NotificationInterval interval, bool repeat, bool useSound, string soundName, string userInfo)
        {
            sendNotificationWithDate(tag, title, subtitle, dateStr, (long)interval, repeat, useSound, soundName, userInfo);
        }

        public void CancelNotification(string tag)
        {
            cancelNotificationWithIdentifier(tag);
        }

        public void CancelAllNotification()
        {
            cancelAllNotification();
        }

        #endregion

        #region Appsflyer 用户互邀

        /**
         * 通过af邀请用户
         * @param inviterId         邀请者id
         * @param inviterAppId      邀请者 app id
         */
        public void AppsflyerInviteUser(string inviterId, string inviterAppId)
        {
            appsflyerInviteUser(inviterId, inviterAppId);
        }

        /**
         * @returns inviterId  格式为 inviterId|inviterAppId
         */
        public string GetAppsflyerInviterId()
        {
            return getAppsflyerInviterId();
        }

        #endregion

        public void SendEmail(string email, string content)
        {
            sendEmail(email, content);
        }

        public string GetConfig(ConfigKeys key)
        {
            return getConfig((int)key);
        }

        public bool IsNetworkConnected()
        {
            return isNetworkConnected();
        }

        public void Rate()
        {
            openAppStore(null);
        }

        public void SystemShareText(String txt)
        {
            
        }

        public void SystemShareImage(String title, String imagePath)
        {
          
        }

        public void OpenUrl(String url)
        {
            openUrl(url);
        }

        public bool HasNotch()
        {
            return hasNotch();
        }

        public int GetNotchHeight()
        {
            return getNotchHeight();   
        }

        public bool HasGestureBar()
        {

            return hasGestureBar();
        }

        public int GetGestureBarHeight()
        {
            return getGestureBarHeight();
        }

        /**
         * 跳转应用商店
         * @param url           1.null，默认本游戏；2.指定游戏 appStoreId；
         */
        public void OpenAppStore(string appStoreId)
        {
            openAppStore(appStoreId);
        }

        public void Toast(string message)
        {
            showToast(message);
        }

        public void copyTxt(string message)
        {
            copyText(message);
        }

        /**
         * 跳转facebook公共主页
         * @param pageId 公共主页id
         */
        public void OpenFacebookPage(string pageId)
        {
            openFacebookPage(pageId);
        }

        public bool IsIpad()
        {
            return isIpad();
        }

        public bool IsDebug()
        {
            return isDebug();
        }

        /**
         *  @param degree  0:轻震； 1：中震； 2：强震
         */
        public void Vibrate(int degree)
        {
            vibrate(degree);
        }

        public void PlayAHAP(string file_name, string folder)
        {
            playAHAP(file_name, folder);
        }

        public void AddShortcut(string type, string title, string subtitle, string icon)
        {
            addShortcut(type, title, subtitle, icon);
        }

        public void DeleteShortcut(string type)
        {
            deleteShortcut(type);
        }

        public string GetShortcutAction()
        {
            return getShortcutAction();
        }

        public void ShowWebView(string url, int x, int y, int w, int h)
        {
            showWebView(url, x, y, w, h);
        }

        public bool IsWebViewDisplayed()
        {
            return isWebViewDisplayed();
        }

        public void CloseWebView()
        {
            closeWebView();
        }

        public void LoginGameCenter()
        {
            loginGameCenter();
        }

        public void ShowGameCenter()
        {
            showGameCenter();
        }

        public void ShowLeaderboards()
        {
            showLeaderboards();
        }

        public void ShowLeaderboard(string leaderboardId)
        {
            showLeaderboard(leaderboardId);
        }

        public void SubmitLeaderboard(string leaderboardId, long score)
        {
            submitLeaderboard(leaderboardId, score);
        }

        public long GetLeaderboardScore(string leaderboardId)
        {
            return getLeaderboardScore(leaderboardId);
        }

        public void ShowAchievements()
        {
            showAchievements();
        }

        public void SubmitAchievement(string achievementId, double percent)
        {
            submitAchievement(achievementId, percent);
        }

        public double GetAchievementProgress(string achievementId)
        {
           return getAchievementProgress(achievementId);
        }

        public void ResetAchievements()
        {
            resetAchievements();
        }

        public void ShowChallenges()
        {
            showChallenges();
        }


#endif
        /// <summary>
        /// Editor模式下的广告测试类，不可以调用该类的方法。
        /// </summary>
        private class RiseEditorAd : MonoBehaviour {
        private static RiseEditorAd _editorAdInstance = null;
        public static bool hasInit = false;
        private Rect bannerPos;
        private bool bannerShow = false;
        private string bannerContent = "";
        private bool interstitialShow = false;
        private string interstitialContent = "";
        private bool rewardShow = false;
        private string rewardContent = "";
        private float scaleWidth = 1;
        private float scaleHeight = 1;
        private int originScreenWidth = 1;
        private int originScreenHeight = 1;
        private bool toastShow = false;
        private List<string> toastList = new List<string> ();
        private GUIStyle toastStyle = null;
        private int rewardAdId = NONE_REWARD_ID;
        private string rewardPlacement = "";
        private string rewardAdTag = DEFAULT_REWARD_TAG;
        private float iconAdWidth = 56;
        private float iconAdXPercent = .2f;
        private float iconAdYPercent = .2f;
        private bool iconAdShow = false;
        private string iconAdContent = "Icon Ad";
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
        private EventSystem curEvent = null;
#endif

        private const int NONE_REWARD_ID = -10;
        private const string DEFAULT_REWARD_TAG = "DEFAULT";
        private const string BANNER_DEFAULT_TXT = "Banner AD";
        private const string INTERSTITIAL_DEFAULT_TXT = "\nInterstitial AD Test";
        private const string REWARD_DEFAULT_TXT = "Free Coin AD Test: ";
        private const int SCREEN_WIDTH = 854;
        private const int SCREEN_HEIGHT = 480;
        private const int GUI_DEPTH = -99;
        private const int BANNER_WIDTH = 320;
        private const int BANNER_HEIGHT = 50;

        void Awake () {
            if (_editorAdInstance == null) {
                _editorAdInstance = this;
            }
            DontDestroyOnLoad (gameObject);
            if (Screen.width > Screen.height) {
                originScreenWidth = SCREEN_WIDTH;
                originScreenHeight = SCREEN_HEIGHT;
            } else {
                originScreenWidth = SCREEN_HEIGHT;
                originScreenHeight = SCREEN_WIDTH;
            }
            scaleWidth = Screen.width * 1f / originScreenWidth;
            scaleHeight = Screen.height * 1f / originScreenHeight;
            toastStyle = new GUIStyle ();
            toastStyle.fontStyle = FontStyle.Bold;
            toastStyle.alignment = TextAnchor.MiddleCenter;
            toastStyle.fontSize = 30;
        }

        public static RiseEditorAd EditorAdInstance {
            get {
                if (_editorAdInstance == null) {
                    _editorAdInstance = FindObjectOfType<RiseEditorAd> () == null ? new GameObject ("RiseEditorAd").AddComponent<RiseEditorAd> () : _editorAdInstance;
                }
                if (!hasInit) {
                    Debug.LogError ("Fatal Error: \nNeed Call RiseSdk.Instance.Init () First At Initialize Scene");
                }
                return _editorAdInstance;
            }
        }

#if UNITY_EDITOR
        void OnGUI () {
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
            if (curEvent == null) {
                curEvent = EventSystem.current;
            }
#endif
            GUI.depth = GUI_DEPTH;
            if (bannerShow) {
                GUI.backgroundColor = Color.green;
                GUI.color = Color.red;
                if (GUI.Button (bannerPos, bannerContent)) {
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
                GUI.backgroundColor = Color.green;
                if (GUI.Button (bannerPos, bannerContent)) {
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
                GUI.backgroundColor = Color.green;
                if (GUI.Button (bannerPos, bannerContent)) {
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
                GUI.backgroundColor = Color.green;
                if (GUI.Button (bannerPos, bannerContent)) {
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
            }
            if (interstitialShow) {
                GUI.backgroundColor = Color.black;
                GUI.color = Color.white;
                //GUI.backgroundColor = new Color (0, 0, 0, 1);
                //GUI.color = new Color (1, 0, 0, 1);
                if (GUI.Button (new Rect (Screen.width - 100 * scaleWidth, 0, 100 * scaleWidth, 50 * scaleHeight), "Close")) {
                    InterstitialAdCallBack ();
                    interstitialShow = false;
                    // Instance.OnResume ();
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
                if (GUI.Button (new Rect (0, 0, Screen.width, Screen.height), interstitialContent)) {
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
                if (GUI.Button (new Rect (0, 0, Screen.width, Screen.height), interstitialContent)) {
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
                if (GUI.Button (new Rect (0, 0, Screen.width, Screen.height), interstitialContent)) {
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
                if (GUI.Button (new Rect (0, 0, Screen.width, Screen.height), interstitialContent)) {
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
                GUI.backgroundColor = Color.red;
                if (GUI.Button (new Rect (Screen.width - 100 * scaleWidth, 0, 100 * scaleWidth, 50 * scaleHeight), "Close")) {
                    InterstitialAdCallBack ();
                    interstitialShow = false;
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
                if (GUI.Button (new Rect (Screen.width - 100 * scaleWidth, 0, 100 * scaleWidth, 50 * scaleHeight), "Close")) {
                    InterstitialAdCallBack ();
                    interstitialShow = false;
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
                if (GUI.Button (new Rect (Screen.width - 100 * scaleWidth, 0, 100 * scaleWidth, 50 * scaleHeight), "Close")) {
                    InterstitialAdCallBack ();
                    interstitialShow = false;
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
                if (GUI.Button (new Rect (Screen.width - 100 * scaleWidth, 0, 100 * scaleWidth, 50 * scaleHeight), "Close")) {
                    InterstitialAdCallBack ();
                    interstitialShow = false;
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
            }
            if (rewardShow) {
                GUI.backgroundColor = Color.black;
                GUI.color = Color.white;
                if (GUI.Button (new Rect (Screen.width - 100 * scaleWidth, 0, 100 * scaleWidth, 50 * scaleHeight), "Close")) {
                    rewardShow = false;
                    // Instance.OnResume ();
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                    RewardAdCallBack ();
                }
                if (GUI.Button (new Rect (0, 0, Screen.width, Screen.height), rewardContent)) {
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
                if (GUI.Button (new Rect (0, 0, Screen.width, Screen.height), rewardContent)) {
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
                if (GUI.Button (new Rect (0, 0, Screen.width, Screen.height), rewardContent)) {
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
                if (GUI.Button (new Rect (0, 0, Screen.width, Screen.height), rewardContent)) {
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                }
                GUI.backgroundColor = Color.red;
                if (GUI.Button (new Rect (Screen.width - 100 * scaleWidth, 0, 100 * scaleWidth, 50 * scaleHeight), "Close")) {
                    rewardShow = false;
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                    RewardAdCallBack ();
                }
                if (GUI.Button (new Rect (Screen.width - 100 * scaleWidth, 0, 100 * scaleWidth, 50 * scaleHeight), "Close")) {
                    rewardShow = false;
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                    RewardAdCallBack ();
                }
                if (GUI.Button (new Rect (Screen.width - 100 * scaleWidth, 0, 100 * scaleWidth, 50 * scaleHeight), "Close")) {
                    rewardShow = false;
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                    RewardAdCallBack ();
                }
                if (GUI.Button (new Rect (Screen.width - 100 * scaleWidth, 0, 100 * scaleWidth, 50 * scaleHeight), "Close")) {
                    rewardShow = false;
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                    if (EventSystem.current != null) {
                        EventSystem.current.enabled = false;
                    }
#endif
                    RewardAdCallBack ();
                }
            }
            if (iconAdShow) {
                GUI.backgroundColor = Color.black;
                GUI.color = Color.white;
                //GUI.backgroundColor = new Color (0, 0, 0, 1);
                //GUI.color = new Color (1, 0, 0, 1);
                if (GUI.Button (new Rect (Screen.width * iconAdXPercent, Screen.height * iconAdYPercent, iconAdWidth, iconAdWidth), iconAdContent)) {
                }
                if (GUI.Button (new Rect (Screen.width * iconAdXPercent, Screen.height * iconAdYPercent, iconAdWidth, iconAdWidth), iconAdContent)) {
                }
                if (GUI.Button (new Rect (Screen.width * iconAdXPercent, Screen.height * iconAdYPercent, iconAdWidth, iconAdWidth), iconAdContent)) {
                }
                if (GUI.Button (new Rect (Screen.width * iconAdXPercent, Screen.height * iconAdYPercent, iconAdWidth, iconAdWidth), iconAdContent)) {
                }
            }
            if (toastList.Count > 0) {
                GUI.backgroundColor = Color.black;
                GUI.color = Color.red;
                //GUI.contentColor = Color.red;
                GUI.Button (new Rect ((Screen.width - 400 * scaleWidth) * .5f, Screen.height - 100 * scaleHeight, 400 * scaleWidth, 50 * scaleHeight), toastList[0]);
                GUI.Button (new Rect ((Screen.width - 400 * scaleWidth) * .5f, Screen.height - 100 * scaleHeight, 400 * scaleWidth, 50 * scaleHeight), toastList[0]);
                //GUI.Label (new Rect ((Screen.width - 200 * scaleWidth) * .5f, Screen.height - 100 * scaleHeight, 200 * scaleWidth, 50 * scaleHeight), toastList [0], toastStyle);
            }
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
            if (EventSystem.current != null) {
                EventSystem.current.enabled = true;
            } else if (curEvent != null) {
                curEvent.enabled = true;
                EventSystem.current = curEvent;
            }
#endif
        }

        void Update () {
            if (Input.GetKeyDown (KeyCode.Escape)) {
                if (interstitialShow) {
                    interstitialShow = false;
                    InterstitialAdCallBack ();
                } else if (rewardShow) {
                    rewardShow = false;
                    RewardAdCallBack ();
                }
#if UNITY_5 || UNITY_4_6 || UNITY_4_7 || UNITY_4_8 || UNITY_4_9
                if (EventSystem.current != null) {
                    EventSystem.current.enabled = true;
                } else if (curEvent != null) {
                    curEvent.enabled = true;
                    EventSystem.current = curEvent;
                }
#endif
            }
        }

        private void InterstitialAdCallBack () {
            if (interstitialShow) {
#if UNITY_IOS
				// IvySdkListener.Instance.adDidClose ("custom|1");
#elif UNITY_ANDROID
                // IvySdkListener.Instance.onFullAdClosed ("EditorAd");
#endif
            }
        }

        private void RewardAdCallBack () {
            if (rewardAdId != NONE_REWARD_ID) {
                Toast ("Show Reward Ad Success");
                IvySdkListener.Instance.onAdRewardUser("1|" + rewardAdTag + "|" + rewardPlacement);
            }
            rewardAdId = NONE_REWARD_ID;
            rewardAdTag = DEFAULT_REWARD_TAG;
        }
#endif

        public void ShowBanner (int pos) {
#if UNITY_EDITOR
            bannerContent = BANNER_DEFAULT_TXT + ", tag: default, pos: " + pos;
            bannerShow = true;
            SetBannerPos (pos);
#endif
        }

        public void ShowBanner (string tag, int pos) {
#if UNITY_EDITOR
            bannerContent = BANNER_DEFAULT_TXT + ", tag: " + tag + ", pos: " + pos;
            bannerShow = true;
            SetBannerPos (pos);
#endif
        }

        public void ShowBanner (string tag, int pos, int animate) {
#if UNITY_EDITOR
            bannerContent = BANNER_DEFAULT_TXT + ", tag: " + tag + ", pos: " + pos + ", animate: " + animate;
            bannerShow = true;
            SetBannerPos (pos);
#endif
        }

        public void CloseBanner () {
#if UNITY_EDITOR
            bannerShow = false;
#endif
        }

        private void SetBannerPos (int pos) {
#if UNITY_EDITOR
            switch ((BannerAdPosition)pos) {
                case BannerAdPosition.POSITION_LEFT_BOTTOM:
                    bannerPos = new Rect (0, Screen.height - BANNER_HEIGHT * scaleHeight, BANNER_WIDTH * scaleWidth, BANNER_HEIGHT * scaleHeight);
                    break;
                case BannerAdPosition.POSITION_LEFT_TOP:
                    bannerPos = new Rect (0, 0, BANNER_WIDTH * scaleWidth, BANNER_HEIGHT * scaleHeight);
                    break;
                case BannerAdPosition.POSITION_CENTER_BOTTOM:
                    bannerPos = new Rect ((Screen.width - BANNER_WIDTH * scaleWidth) * .5f, Screen.height - BANNER_HEIGHT * scaleHeight, BANNER_WIDTH * scaleWidth, BANNER_HEIGHT * scaleHeight);
                    break;
                case BannerAdPosition.POSITION_CENTER:
                    bannerPos = new Rect ((Screen.width - BANNER_WIDTH * scaleWidth) * .5f, (Screen.height - BANNER_HEIGHT * scaleHeight) * .5f, BANNER_WIDTH * scaleWidth, BANNER_HEIGHT * scaleHeight);
                    break;
                case BannerAdPosition.POSITION_CENTER_TOP:
                    bannerPos = new Rect ((Screen.width - BANNER_WIDTH * scaleWidth) * .5f, 0, BANNER_WIDTH * scaleWidth, BANNER_HEIGHT * scaleHeight);
                    break;
                case BannerAdPosition.POSITION_RIGHT_BOTTOM:
                    bannerPos = new Rect (Screen.width - BANNER_WIDTH * scaleWidth, Screen.height - BANNER_HEIGHT * scaleHeight, BANNER_WIDTH * scaleWidth, BANNER_HEIGHT * scaleHeight);
                    break;
                case BannerAdPosition.POSITION_RIGHT_TOP:
                    bannerPos = new Rect (Screen.width - BANNER_WIDTH * scaleWidth, 0, BANNER_WIDTH * scaleWidth, BANNER_HEIGHT * scaleHeight);
                    break;
            }
#endif
        }

        public void ShowAd (string tag) {
#if UNITY_EDITOR
            interstitialShow = true;
            interstitialContent = tag + INTERSTITIAL_DEFAULT_TXT;
            // Instance.OnPause ();
#endif
        }

        public void ShowRewardAd (int id) {
#if UNITY_EDITOR
            rewardShow = true;
            rewardAdId = id;
            rewardAdTag = DEFAULT_REWARD_TAG;
            rewardContent = REWARD_DEFAULT_TXT + rewardAdTag;
            // Instance.OnPause ();
#endif
        }

        public void ShowRewardAd (string tag, string id) {
#if UNITY_EDITOR
            rewardShow = true;
            rewardAdId = 1;
            rewardPlacement = id;
            rewardAdTag = tag;
            rewardContent = REWARD_DEFAULT_TXT + tag;
            // Instance.OnPause ();
#endif
        }

        public void ShowIconAd (float width, float xPercent, float yPercent) {
            iconAdShow = true;
            iconAdWidth = width;
            iconAdXPercent = xPercent;
            iconAdYPercent = yPercent;
        }

        public void CloseIconAd () {
            iconAdShow = false;
        }

        public void Pay (int billingId) {
#if !Headline
#if UNITY_EDITOR
#if UNITY_ANDROID
            switch (EditorUtility.DisplayDialogComplex ("Pay", "Pay: " + billingId, "TRY FAILURE", "NO", "YES")) {
                case 0://TRY FAILURE
                    Toast ("pay " + billingId + " Failed");
                    IvySdkListener.Instance.onPaymentFail (billingId + "|" + "");
                    break;
                case 1://NO
                    Toast ("pay " + billingId + " Canceled");
                    IvySdkListener.Instance.onPaymentFail (billingId + "|" + "");
                    break;
                case 2://YES
                    Toast ("pay " + billingId + " Success");
                    IvySdkListener.Instance.onPaymentSuccess (billingId + "|" + "");
                    break;
            }
#elif UNITY_IOS
                switch (EditorUtility.DisplayDialogComplex ("Pay", "Pay: " + billingId, "TRY FAILURE", "NO", "YES")) {
                case 0://TRY FAILURE
                    Toast ("pay " + billingId + " Failed");
                    IvySdkListener.Instance.onPaymentFail(billingId + "|" + "");
                    break;
                case 1://NO
                    Toast ("pay " + billingId + " Canceled");
					IvySdkListener.Instance.onPaymentFail(billingId + "|" + "");
                    break;
                case 2://YES
                    Toast ("pay " + billingId + " Success");
					IvySdkListener.Instance.onPaymentSuccess (billingId + "|" + "");
                    break;
            }
#endif
#endif
#endif
        }

        private bool timeCounting = false;

        public void Toast (string msg) {
#if UNITY_EDITOR
            toastList.Add (msg);
            if (!timeCounting) {
                timeCounting = true;
                StartCoroutine (CheckToast ());
            }
#endif
        }

        private IEnumerator CheckToast (float time = 2) {
            yield return new WaitForSeconds (time);
            if (toastList.Count > 0) {
                toastList.RemoveAt (0);
            }
            if (toastList.Count > 0) {
                StartCoroutine (CheckToast ());
            } else {
                timeCounting = false;
            }
        }

        public void Alert (string title, string msg) {
#if UNITY_EDITOR
            EditorUtility.DisplayDialog (title, msg, "NO", "OK");
#endif
        }

        public void OnExit () {
#if UNITY_EDITOR
            if (EditorUtility.DisplayDialog ("Exit", "Are you sure to exit?", "YES", "NO")) {
                EditorApplication.isPlaying = false;
            }
#endif
        }
    }
    }
}
