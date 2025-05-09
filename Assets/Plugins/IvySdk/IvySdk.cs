using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SocialPlatforms.Impl;

namespace com.ivy.sdk
{
    public sealed class IvySdk
    {
        //#if UNITY_ANDROID
        private static IvySdk _instance = null;

        private AndroidJavaClass _class = null;

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

        public void Init()
        {
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

        #region ads
        public enum BannerAdPosition
        {
            POSITION_LEFT_TOP = 1,
            POSITION_LEFT_BOTTOM = 2,
            POSITION_CENTER_TOP = 3,
            POSITION_CENTER_BOTTOM = 4,
            POSITION_CENTER = 5,
            POSITION_RIGHT_TOP = 6,
            POSITION_RIGHT_BOTTOM = 7,
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
            if (_class != null)
            {
                _class.CallStatic("closeBannerAd", placement);
            }
        }

        public bool HasInterstitialAd()
        {
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
            if (_class != null)
            {
                _class.CallStatic("showInterstitialAd", tag, placement, clientInfo);
            }
        }

        public bool HasRewardedAd()
        {
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
            if (_class != null)
            {
                _class.CallStatic("showRewardedAd", tag, placement, clientInfo);
            }
        }

        #endregion

        #region 计费

        public void Pay(int id)
        {
            if (_class != null)
            {
                _class.CallStatic("pay", id);
            }
        }

        public void Pay(int id, string payload)
        {
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
            return "[]";
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
        public class FirebaseLinkChannel
        {
            public static string ANONYMOUS = "anonymous";
            public static string PLAY_GAMES = "playgames";
            public static string GOOGLE = "google";
            public static string FACEBOOK = "facebook";
            public static string EMAIL = "email";
            public static string DEFAULT = "default";
        }

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
         * @param meta                  自定义用户属性，字典格式
         * @param tags                  用户标签，AIHelp需要预先在后台定义用户标签
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
                _class.CallStatic<int>("openNotificationSettings");
            }
        }

        /**
         *
         * @param tag                   任务 id
         * @param title                 通知栏标题
         * @param subtitle              通知栏副标题
         * @param bigText               长文本
         * @param smallIcon             小图标
         * @param largeIcon             大图标
         * @param bigPicture            大图
         * @param delay                 延迟时间
         * @param autoCancel            可关闭
         * @param action                通知栏点击事件行为
         * @param repeat                重复触发通知栏
         * @param requireNetwork        要求联网状态展示通知栏
         * @param requireCharging       要求充电状态展示通知栏
         */
        public void PushNotificationTask(string tag, string title, string subtitle, string bigText, string smallIcon, string largeIcon, string bigPicture, long delay, bool autoCancel, string action, bool repeat, bool requireNetwork, bool requireCharging)
        {
            if (_class != null)
            {
                _class.CallStatic<int>("pushNotificationTask", tag, title, subtitle, bigText, smallIcon, largeIcon, bigPicture, delay, autoCancel, action, repeat, requireNetwork, requireCharging);
            }
        }

        public void CancelNotification(string tag)
        {
            if (_class != null)
            {
                _class.CallStatic<int>("cancelNotification", tag);
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

        public void SystemShareText(String txt)
        {
            if (_class != null)
            {
                _class.CallStatic("systemShareText", txt);
            }
        }

        public void SystemShareImage(String title, String imagePath)
        {
            if (_class != null)
            {
                _class.CallStatic("systemShareImage", title, imagePath);
            }
        }

        public void OpenUrl(String url)
        {
            if (_class != null)
            {
                _class.CallStatic("openUrl", url);
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

        public void toast(string message)
        {
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

        //#endif
    }
}