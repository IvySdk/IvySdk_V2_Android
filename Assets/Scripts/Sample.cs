using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Transactions;
using TMPro;
using UnityEngine;
using UnityEngine.UI;
using com.ivy.sdk;

public class Sample : MonoBehaviour
{

    public TMP_Dropdown adDropDown;
    public TMP_Dropdown purchaseDropDown;
    public TMP_Dropdown eventsDropDown;
    public TMP_Dropdown remoteConfigDropDown;
    public TMP_Dropdown playGamesDropDown;
    public TMP_Dropdown facebookDropDown;
    public TMP_Dropdown firebasekDropDown;
    public TMP_Dropdown firestoreDropDown;
    public TMP_Dropdown helperDropDown;
    public TMP_Dropdown notificationDropDown;
    public TMP_Dropdown appsflyerinviteDropDown;
    public TMP_Dropdown othersDropDown;
    //#if UNITY_ANDROID
    void Awake()
    {

        // 初始化sdk
        IvySdk.Instance.Init();

    }

    // Start is called before the first frame update
    void Start()
    {

        Dictionary<string, object> dict = new Dictionary<string, object>();
        dict.Add("key1", 1);
        dict.Add("key2", 2.0);
        dict.Add("key3", true);
        dict.Add("key4", "str");
        dict.Add("key5", DateTime.Now);
        dict.Add("key6", DateTime.Now.Date);
        string result = IvyJson.Serialize(dict);
        Debug.Log($"serialize result:{result}");


        //广告
        //banner ad 监听
        IvySdkListener.OnBannerAdEvent += IvySdkListener_OnBannerAdEvent;
        //IvySdkListener.OnBannerAdEvent -= IvySdkListener_OnBannerAdEvent;

        //rewarded ad 监听
        IvySdkListener.OnRewardedAdEvent += IvySdkListener_OnRewardedAdEvent;
        //IvySdkListener.OnRewardedAdEvent -= IvySdkListener_OnRewardedAdEvent;

        //interstitial ad 监听
        IvySdkListener.OnInterstitialAdEvent += IvySdkListener_OnInterstitialAdEvent;
        //IvySdkListener.OnInterstitialAdEvent += IvySdkListener_OnInterstitialAdEvent;

        adDropDown.options = new List<TMP_Dropdown.OptionData>
        {
            new TMP_Dropdown.OptionData("is_banner_ready"),
            new TMP_Dropdown.OptionData("show_banner_ad"),
            new TMP_Dropdown.OptionData("close_banner"),
            new TMP_Dropdown.OptionData("is_interstitial_ready"),
            new TMP_Dropdown.OptionData("show_interstital_ad"),
            new TMP_Dropdown.OptionData("is_rewarded_ready"),
            new TMP_Dropdown.OptionData("show_rewarded_ad"),
        };
        adDropDown.onValueChanged.AddListener(OnAdDropDownValueChanged);

        //计费
        IvySdkListener.OnPaymentEvent += IvySdkListener_OnPaymentEvent;
        //IvySdkListener.OnPaymentEvent -= IvySdkListener_OnPaymentEvent;

        IvySdkListener.OnPaymentWithPayloadEvent += IvySdkListener_OnPaymentWithPayloadEvent;
        //IvySdkListener.OnPaymentWithPayloadEvent -= IvySdkListener_OnPaymentWithPayloadEvent;

        purchaseDropDown.options = new List<TMP_Dropdown.OptionData>
        {
            new TMP_Dropdown.OptionData("pay"),
            new TMP_Dropdown.OptionData("pay_with_payload"),
            new TMP_Dropdown.OptionData("shipping_goods"),//发货;使用在线计费时，务必调用此接口通知后台发货
            new TMP_Dropdown.OptionData("query_payment_order"),//查询指定计费点位是否存在未处理支付记录
            new TMP_Dropdown.OptionData("query_payment_orders"),//查询所有未处理支付记录
            new TMP_Dropdown.OptionData("query_payment_data"),//查询指定计费点位详情
            new TMP_Dropdown.OptionData("query_payment_datas"),//查询所有计费点位详情
            new TMP_Dropdown.OptionData("is_payment_ready"),
        };
        purchaseDropDown.onValueChanged.AddListener(OnPurchaseDropDownValueChanged);

        //事件
        eventsDropDown.options = new List<TMP_Dropdown.OptionData>
        {
            new TMP_Dropdown.OptionData("log_to_all_platform"),
            new TMP_Dropdown.OptionData("log_to_firebase"),
            new TMP_Dropdown.OptionData("log_to_facebook"),
            new TMP_Dropdown.OptionData("log_to_appsflyer"),
            new TMP_Dropdown.OptionData("log_to_ivy"),
            new TMP_Dropdown.OptionData("set_user_property_to_all_platform"),
            new TMP_Dropdown.OptionData("set_user_property_to_firebase"),
            new TMP_Dropdown.OptionData("set_user_property_to_ivy"),
            new TMP_Dropdown.OptionData("set_user_custom_user_id"),
        };
        eventsDropDown.onValueChanged.AddListener(OnEventsDropDownValueChanged);

        // remote config
        remoteConfigDropDown.options = new List<TMP_Dropdown.OptionData>
        {
            new TMP_Dropdown.OptionData("get_int_from_firebase"),
            new TMP_Dropdown.OptionData("get_long_from_firebase"),
            new TMP_Dropdown.OptionData("get_double_from_firebase"),
            new TMP_Dropdown.OptionData("get_bool_from_firebase"),
            new TMP_Dropdown.OptionData("get_string_from_firebase"),
            new TMP_Dropdown.OptionData("get_int_from_ivy"),
            new TMP_Dropdown.OptionData("get_long_from_ivy"),
            new TMP_Dropdown.OptionData("get_double_from_ivy"),
            new TMP_Dropdown.OptionData("get_bool_from_ivy"),
            new TMP_Dropdown.OptionData("get_string_from_ivy"),
        };
        remoteConfigDropDown.onValueChanged.AddListener(OnRemoteConfigDropDownValueChanged);

        // play games
        IvySdkListener.OnPlayGamesLoginEvent += IvySdkListener_OnPlayGamesLoginEvent;
        IvySdkListener.OnPlayGamesLoginEvent -= IvySdkListener_OnPlayGamesLoginEvent;


        playGamesDropDown.options = new List<TMP_Dropdown.OptionData>
        {
            new TMP_Dropdown.OptionData("is_play_games_logged_in"),
            new TMP_Dropdown.OptionData("login_play_games"),
            new TMP_Dropdown.OptionData("play_games_user_info"),
            new TMP_Dropdown.OptionData("unlock_achievement"),
            new TMP_Dropdown.OptionData("increase_achievement"),
            new TMP_Dropdown.OptionData("show_achievement"),
            new TMP_Dropdown.OptionData("show_leaderboard"),
            new TMP_Dropdown.OptionData("show_leaderboards"),
            new TMP_Dropdown.OptionData("update_leaderboard"),
        };
        playGamesDropDown.onValueChanged.AddListener(OnPlayGamesDropDownValueChanged);

        // facebook
        IvySdkListener.OnFacebookLoginEvent += IvySdkListener_OnFacebookLoginEvent;
        IvySdkListener.OnFacebookLoginEvent -= IvySdkListener_OnFacebookLoginEvent;


        facebookDropDown.options = new List<TMP_Dropdown.OptionData>
        {
            new TMP_Dropdown.OptionData("is_facebook_logged_in"),
            new TMP_Dropdown.OptionData("login_facebook"),
            new TMP_Dropdown.OptionData("logout_facebook"),
            new TMP_Dropdown.OptionData("get_facebook_friends"),
            new TMP_Dropdown.OptionData("get_facebook_user_info")
        };
        facebookDropDown.onValueChanged.AddListener(OnFacebookDropDownValueChanged);

        // firebase
        IvySdkListener.OnFirebaseLoginEvent += IvySdkListener_OnFirebaseLoginEvent;
        IvySdkListener.OnFirebaseLoginEvent -= IvySdkListener_OnFirebaseLoginEvent;

        IvySdkListener.OnFirebaseUnlinkEvent += IvySdkListener_OnFirebaseUnlinkEvent;
        IvySdkListener.OnFirebaseUnlinkEvent -= IvySdkListener_OnFirebaseUnlinkEvent;

        firebasekDropDown.options = new List<TMP_Dropdown.OptionData>
        {
            new TMP_Dropdown.OptionData("logout_firebase"),
            new TMP_Dropdown.OptionData("get_firebase_user_info"),
            new TMP_Dropdown.OptionData("is_anonymous_logged_in"),
            new TMP_Dropdown.OptionData("is_linked_with_channel"),
            new TMP_Dropdown.OptionData("can_unlink_with_channel"),
            new TMP_Dropdown.OptionData("unlink_with_channel"),
            new TMP_Dropdown.OptionData("reload_log_state"),
            new TMP_Dropdown.OptionData("log_with_anonymous"),
            new TMP_Dropdown.OptionData("log_with_play_games"),
            new TMP_Dropdown.OptionData("log_with_facebook"),
            new TMP_Dropdown.OptionData("log_with_email"),
            new TMP_Dropdown.OptionData("firebase_user_id"),
        };
        firebasekDropDown.onValueChanged.AddListener(OnFirebaseDropDownValueChanged);

        // firestore
        // 默认文档id为用户的 firebase user id
        //IvySdkListener.OnCloudDataSaveEvent += IvySdkListener_OnCloudDataSaveEvent;
        //IvySdkListener.OnCloudDataSaveEvent -= IvySdkListener_OnCloudDataSaveEvent;

        //IvySdkListener.OnCloudDataReadEvent += IvySdkListener_OnCloudDataReadEvent;
        //IvySdkListener.OnCloudDataReadEvent -= IvySdkListener_OnCloudDataReadEvent;

        //IvySdkListener.OnCloudDataMergeEvent += IvySdkListener_OnCloudDataMergeEvent;
        //IvySdkListener.OnCloudDataMergeEvent -= IvySdkListener_OnCloudDataMergeEvent;

        //IvySdkListener.OnCloudDataQueryEvent += IvySdkListener_OnCloudDataQueryEvent;
        //IvySdkListener.OnCloudDataQueryEvent -= IvySdkListener_OnCloudDataQueryEvent;

        //IvySdkListener.OnCloudDataDeleteEvent += IvySdkListener_OnCloudDataDeleteEvent;
        //IvySdkListener.OnCloudDataDeleteEvent -= IvySdkListener_OnCloudDataDeleteEvent;

        //IvySdkListener.OnCloudDataUpdateEvent += IvySdkListener_OnCloudDataUpdateEvent;
        //IvySdkListener.OnCloudDataUpdateEvent -= IvySdkListener_OnCloudDataUpdateEvent;

        //IvySdkListener.OnCloudDataSnapshotEvent += IvySdkListener_OnCloudDataSnapshotEvent;
        //IvySdkListener.OnCloudDataSnapshotEvent -= IvySdkListener_OnCloudDataSnapshotEvent;

        firestoreDropDown.options = new List<TMP_Dropdown.OptionData>
        {
            new TMP_Dropdown.OptionData("save"),
            new TMP_Dropdown.OptionData("read"),
            new TMP_Dropdown.OptionData("merge"),
            new TMP_Dropdown.OptionData("query"),
            new TMP_Dropdown.OptionData("delete"),
            new TMP_Dropdown.OptionData("update"),
            new TMP_Dropdown.OptionData("snapshot")
        };
        firestoreDropDown.onValueChanged.AddListener(OnFirestoreDropDownValueChanged);

        // 客服
        IvySdkListener.HelperUnreadMsgCountEvent += IvySdkListener_HelperUnreadMsgCountEvent;
        IvySdkListener.HelperUnreadMsgCountEvent -= IvySdkListener_HelperUnreadMsgCountEvent;

        helperDropDown.options = new List<TMP_Dropdown.OptionData>
        {
            new TMP_Dropdown.OptionData("is_initialized"),
            new TMP_Dropdown.OptionData("has_new_message"),
            new TMP_Dropdown.OptionData("show_helper"),
            new TMP_Dropdown.OptionData("show_helper_single_faq"),
            new TMP_Dropdown.OptionData("listen_unread_message"),
            new TMP_Dropdown.OptionData("stop_listen_unread_message"),
            new TMP_Dropdown.OptionData("update_user_info"),
            new TMP_Dropdown.OptionData("reset_user_info"),
            new TMP_Dropdown.OptionData("close_helper"),
        };
        helperDropDown.onValueChanged.AddListener(OnHelperDropDownValueChanged);

        // 本地通知
        IvySdkListener.OnReceivedNotificationEvent += IvySdkListener_OnReceivedNotificationEvent;
        IvySdkListener.OnReceivedNotificationEvent -= IvySdkListener_OnReceivedNotificationEvent;

        notificationDropDown.options = new List<TMP_Dropdown.OptionData>
        {
            new TMP_Dropdown.OptionData("permission_state"),
            new TMP_Dropdown.OptionData("request_permission"),
            new TMP_Dropdown.OptionData("open_permission_setting"),
            new TMP_Dropdown.OptionData("push_notification"),
            new TMP_Dropdown.OptionData("cancel_notification"),
        };
        notificationDropDown.onValueChanged.AddListener(OnNotificationDropDownValueChanged);

        // appsflyer 用户互邀

        appsflyerinviteDropDown.options = new List<TMP_Dropdown.OptionData>
        {
            new TMP_Dropdown.OptionData("invite"),
            new TMP_Dropdown.OptionData("get_inviter_id"),
        };
        appsflyerinviteDropDown.onValueChanged.AddListener(OnAppsflyerinviteDropDown);

        // 其它
        othersDropDown.options = new List<TMP_Dropdown.OptionData>
        {
            new TMP_Dropdown.OptionData("send_email"),
            new TMP_Dropdown.OptionData("get_config"),
            new TMP_Dropdown.OptionData("is_network_connected"),
            new TMP_Dropdown.OptionData("rate"),
            new TMP_Dropdown.OptionData("share_text"),
            new TMP_Dropdown.OptionData("share_image"),
            new TMP_Dropdown.OptionData("open_url"),
            new TMP_Dropdown.OptionData("is_notch_screen"),
            new TMP_Dropdown.OptionData("notch_height"),
            new TMP_Dropdown.OptionData("open_app_store"),
            new TMP_Dropdown.OptionData("toast"),
            new TMP_Dropdown.OptionData("copy_text"),
            new TMP_Dropdown.OptionData("device_total_memory"),
            new TMP_Dropdown.OptionData("device_free_memory"),
            new TMP_Dropdown.OptionData("device_total_disk_size"),
            new TMP_Dropdown.OptionData("device_free_disk_size"),
        };
        othersDropDown.onValueChanged.AddListener(OnOthersinviteDropDown);

    }

    public void OnAdDropDownValueChanged(int value)
    {
        switch (value)
        {
            case 0:
                bool isBannerReady = IvySdk.Instance.HasBannerAd();
                Debug.Log($"is_banner_ready --> {isBannerReady}");
                break;
            case 1:
                IvySdk.Instance.ShowBannerAd("default", IvySdk.BannerAdPosition.POSITION_CENTER_BOTTOM, "1");
                Debug.Log("show_banner_ad called");
                break;
            case 2:
                IvySdk.Instance.CloseBannerAd("1");
                Debug.Log("close_banner called");
                break;
            case 3:
                bool isInterstitialReady = IvySdk.Instance.HasInterstitialAd();
                Debug.Log($"is_interstitial_ready --> {isInterstitialReady}");
                break;
            case 4:
                IvySdk.Instance.ShowInterstitialAd("default", "2", null);
                Debug.Log("show_interstital_ad called");
                break;
            case 5:
                bool isRewardedReady = IvySdk.Instance.HasRewardedAd();
                Debug.Log($"is_rewarded_ready --> {isRewardedReady}");
                break;
            case 6:
                IvySdk.Instance.ShowRewardedAd("default", "3", null);
                Debug.Log("show_rewarded_ad called");
                break;
        }
    }

    public void OnPurchaseDropDownValueChanged(int value)
    {
        switch (value)
        {
            case 0:
                IvySdk.Instance.Pay(1);
                Debug.Log("pay called");
                break;
            case 1:
                IvySdk.Instance.Pay(1, "test_payload_data");
                Debug.Log("pay_with_payload called");
                break;
            case 2:
                //发货;使用在线计费时，务必调用此接口通知后台发货
                IvySdk.Instance.ShippingGoods("test_merchantTransaction_id");
                Debug.Log("shipping_goods called");
                break;
            case 3:
                IvySdk.Instance.QueryPaymentOrder(1);
                Debug.Log("query_payment_order called");
                break;
            case 4:
                IvySdk.Instance.QueryPaymentOrders();
                Debug.Log("query_payment_orders called");
                break;
            case 5:
                IvySdk.Instance.GetPaymentData(1);
                Debug.Log("query_payment_data called");
                break;
            case 6:
                IvySdk.Instance.GetPaymentDatas();
                Debug.Log("query_payment_datas called");
                break;
            case 7:
                bool isReady = IvySdk.Instance.IsPaymentValid();
                Debug.Log($"is_payment_ready --> {isReady}");
                break;
        }
    }

    public void OnEventsDropDownValueChanged(int value)
    {
        switch (value)
        {
            case 0:
                IvySdk.Instance.TrackEventToFacebook("event_name", null);
                Debug.Log("log_to_all_platform called");
                break;
            case 1:
                IvySdk.Instance.TrackEventToFirebase("event_name", null);
                Debug.Log("log_to_firebase called");
                break;
            case 2:
                IvySdk.Instance.TrackEventToFacebook("event_name", null);
                Debug.Log("log_to_facebook called");
                break;
            case 3:
                IvySdk.Instance.TrackEventToAppsflyer("event_name", null);
                Debug.Log("log_to_appsflyer called");
                break;
            case 4:
                IvySdk.Instance.TrackEventToIvy("event_name", null);
                Debug.Log("log_to_ivy called");
                break;
            case 5:
                IvySdk.Instance.SetUserProperty("key", "value");
                Debug.Log("set_user_property_to_all_platform called");
                break;
            case 6:
                IvySdk.Instance.SetUserPropertyToFirebase("key", "value");
                Debug.Log("set_user_property_to_firebase called");
                break;
            case 7:
                IvySdk.Instance.SetUserPropertyToIvy("key", "value");
                Debug.Log("set_user_property_to_ivy called");
                break;
            case 8:
                IvySdk.Instance.SetCustomUserId("custom_user_id");
                Debug.Log("set_user_custom_user_id called");
                break;
        }
    }

    public void OnRemoteConfigDropDownValueChanged(int value)
    {
        switch (value)
        {
            case 0:
                int intValue = IvySdk.Instance.GetRemoteConfigInt("key");
                Debug.Log($"get_int_from_firebase --> {intValue}");
                break;
            case 1:
                long longValue = IvySdk.Instance.GetRemoteConfigLong("key");
                Debug.Log($"get_long_from_firebase --> {longValue}");
                break;
            case 2:
                double doubleValue = IvySdk.Instance.GetRemoteConfigDouble("key");
                Debug.Log($"get_double_from_firebase --> {doubleValue}");
                break;
            case 3:
                bool boolValue = IvySdk.Instance.GetRemoteConfigBoolean("key");
                Debug.Log($"get_bool_from_firebase --> {boolValue}");
                break;
            case 4:
                string stringValue = IvySdk.Instance.GetRemoteConfigString("key");
                Debug.Log($"get_string_from_firebase --> {stringValue}");
                break;
            case 5:
                int ivyIntValue = IvySdk.Instance.GetIvyRemoteConfigInt("key");
                Debug.Log($"get_int_from_ivy --> {ivyIntValue}");
                break;
            case 6:
                long ivyLongValue = IvySdk.Instance.GetIvyRemoteConfigLong("key");
                Debug.Log($"get_long_from_ivy --> {ivyLongValue}");
                break;
            case 7:
                double ivyDoublwValue = IvySdk.Instance.GetIvyRemoteConfigDouble("key");
                Debug.Log($"get_double_from_ivy --> {ivyDoublwValue}");
                break;
            case 8:
                bool ivyBoolValue = IvySdk.Instance.GetIvyRemoteConfigBoolean("key");
                Debug.Log($"get_bool_from_ivy --> {ivyBoolValue}");
                break;
            case 9:
                string ivyStringValue = IvySdk.Instance.GetIvyRemoteConfigString("key");
                Debug.Log($"get_string_from_ivy --> {ivyStringValue}");
                break;
        }
    }

    public void OnPlayGamesDropDownValueChanged(int value)
    {
        //switch (value)
        //{
        //    case 0:
        //        bool isLoggedIn = IvySdk.Instance.IsPlayGamesLoggedIn();
        //        Debug.Log($"is_play_games_logged_in --> {isLoggedIn}");
        //        break;
        //    case 1:
        //        IvySdk.Instance.LoginPlayGames();
        //        Debug.Log($"login_play_games called");
        //        break;
        //    case 2:
        //        string user_info = IvySdk.Instance.GetPlayGamesUserInfo();
        //        Debug.Log($"play_games_user_info --> {user_info}");
        //        break;
        //    case 3:
        //        IvySdk.Instance.UnlockAchievement("achievemnt_id");
        //        Debug.Log($"unlock_achievement called");
        //        break;
        //    case 4:
        //        IvySdk.Instance.IncreaseAchievement("achievemnt_id", 1);
        //        Debug.Log($"increase_achievement called");
        //        break;
        //    case 5:
        //        IvySdk.Instance.ShowAchievement();
        //        Debug.Log($"show_achievement called");
        //        break;
        //    case 6:
        //        IvySdk.Instance.ShowLeaderboard("leaderboard_id");
        //        Debug.Log($"show_leaderboard called");
        //        break;
        //    case 7:
        //        IvySdk.Instance.ShowLeaderboards();
        //        Debug.Log($"show_leaderboards called");
        //        break;
        //    case 8:
        //        IvySdk.Instance.UpdateLeaderboard("leaderboard_id", 100);
        //        Debug.Log($"update_leaderboard called");
        //        break;
        //}
    }

    public void OnFacebookDropDownValueChanged(int value)
    {
        //switch (value)
        //{
        //    case 0:
        //        bool isLogged = IvySdk.Instance.IsFacebookLoggedIn();
        //        Debug.Log($"is_facebook_logged_in --> {isLogged}");
        //        break;
        //    case 1:
        //        IvySdk.Instance.LogInFacebook();
        //        Debug.Log("login_facebook called");
        //        break;
        //    case 2:
        //        IvySdk.Instance.LogoutFacebook();
        //        Debug.Log("logout_facebook called");
        //        break;
        //    case 3:
        //        string friends = IvySdk.Instance.GetFacebookFriends();
        //        Debug.Log($"get_facebook_friends --> {friends}");
        //        break;
        //    case 4:
        //        string user_info = IvySdk.Instance.GetFacebookUserInfo();
        //        Debug.Log($"get_facebook_user_info --> {user_info}");
        //        break;
        //}
    }

    public void OnFirebaseDropDownValueChanged(int value)
    {
        switch (value)
        {
            case 0:
                IvySdk.Instance.LogoutFirebase();
                Debug.Log("logout_firebase called");
                break;
            case 1:
                string user_info = IvySdk.Instance.GetFirebaseUserInfo(IvySdk.FirebaseLinkChannel.PLAY_GAMES);
                Debug.Log($"get_firebase_user_info called --> {user_info}");
                break;
            case 2:
                bool isAnonymous = IvySdk.Instance.IsFirebaseAnonymousLoggedIn();
                Debug.Log($"is_anonymous_logged_in called --> {isAnonymous}");
                break;
            case 3:
                bool isLinked = IvySdk.Instance.IsFirebaseLinkedWithChannel(IvySdk.FirebaseLinkChannel.PLAY_GAMES);
                Debug.Log($"is_linked_with_channel called --> {isLinked}");
                break;
            case 4:
                bool canUnlink = IvySdk.Instance.CanFirebaseUnlinkWithChannel(IvySdk.FirebaseLinkChannel.PLAY_GAMES);
                Debug.Log($"can_unlink_with_channel called --> {canUnlink}");
                break;
            case 5:
                IvySdk.Instance.UnlinkFirebaseWithChannel(IvySdk.FirebaseLinkChannel.PLAY_GAMES);
                Debug.Log("unlink_with_channel called");
                break;
            case 6:
                IvySdk.Instance.ReloadFirebaseLogStatus();
                Debug.Log("reload_log_state called");
                break;
            case 7:
                IvySdk.Instance.LoginFBWithAnonymous();
                Debug.Log("log_with_anonymous called");
                break;
            case 8:
                //IvySdk.Instance.LoginFBWithPlayGames();
                //Debug.Log("log_with_play_games called");
                break;
            case 9:
                IvySdk.Instance.LoginFBWithFacebook();
                Debug.Log("log_with_facebook called");
                break;
            case 10:
                IvySdk.Instance.LoginFBWithEmailAndPwd("email", "password");
                Debug.Log("log_with_email called");
                break;
            case 11:
                string user_id = IvySdk.Instance.GetFirebaseUserId();
                Debug.Log($"firebase_user_id called --> {user_id}");
                break;
        }
    }

    public void OnFirestoreDropDownValueChanged(int value)
    {
        switch (value)
        {
            case 0:
                IvySdk.Instance.SaveCloudData("collection", "documentId", "{}");
                Debug.Log("save called");
                break;
            case 1:
                IvySdk.Instance.ReadCloudData("collection", "documentId");
                Debug.Log("read called");
                break;
            case 2:
                IvySdk.Instance.MergeCloudData("collection", "documentId", "{}");
                Debug.Log("merge called");
                break;
            case 3:
                IvySdk.Instance.QueryCloudData("collection", "documentId");
                Debug.Log("query called");
                break;
            case 4:
                IvySdk.Instance.DeleteCloudData("collection", "documentId");
                Debug.Log("delete called");
                break;
            case 5:
                IvySdk.Instance.UpdateCloudData("collection", "documentId", "transactionId", "{}");
                Debug.Log("update called");
                break;
            case 6:
                IvySdk.Instance.SnapshotCloudData("collection", "documentId");
                Debug.Log("snapshot called");
                break;
        }
    }

    public void OnHelperDropDownValueChanged(int value)
    {
        switch (value)
        {
            case 0:
                bool isInit = IvySdk.Instance.IsHelperInitialized();
                Debug.Log($"is_initialized --> {isInit}");
                break;
            case 1:
                bool hasNew = IvySdk.Instance.HasNewHelperMessage();
                Debug.Log($"has_new_message --> {hasNew}");
                break;
            case 2:
                IvySdk.Instance.ShowHelper("entranceId", null, null, null);
                Debug.Log("show_helper called");
                break;
            case 3:
                IvySdk.Instance.ShowHelperSingleFAQ("faqId");
                Debug.Log("show_helper_single_faq called");
                break;
            case 4:
                IvySdk.Instance.ListenHelperUnreadMsgCount(false);
                Debug.Log("listen_unread_message called");
                break;
            case 5:
                IvySdk.Instance.StopListenHelperUnreadMsgCount();
                Debug.Log("stop_listen_unread_message called");
                break;
            case 6:
                IvySdk.Instance.UpdateHelperUserInfo("{}", "");
                Debug.Log("update_user_info called");
                break;
            case 7:
                IvySdk.Instance.ResetHelperUserInfo();
                Debug.Log("reset_user_info called");
                break;
            case 8:
                IvySdk.Instance.CloseHelper();
                Debug.Log("close_helper called");
                break;
        }
    }

    public void OnNotificationDropDownValueChanged(int value)
    {
        //switch (value)
        //{
        //    case 0:
        //        int isInit = IvySdk.Instance.LoadNotificationPermissionState();
        //        Debug.Log($"permission_state --> {isInit}");
        //        break;
        //    case 1:
        //        IvySdk.Instance.RequestNotificationPermission();
        //        Debug.Log("request_permission called");
        //        break;
        //    case 2:
        //        IvySdk.Instance.OpenNotificationSettings();
        //        Debug.Log("open_permission_setting called");
        //        break;
        //    case 3:
        //        IvySdk.Instance.PushNotificationTask("tag", "title", "subtitle", null, null, null, null, 60, true, "test_action", true, false, false);
        //        Debug.Log("push_notification called");
        //        break;
        //    case 4:
        //        IvySdk.Instance.CancelNotification("tag");
        //        Debug.Log("cancel_notification called");
        //        break;
        //}
    }

    public void OnAppsflyerinviteDropDown(int value)
    {
        switch (value)
        {
            case 0:
                IvySdk.Instance.AppsflyerInviteUser("inviterId", "inviter_app_id");
                Debug.Log("invite called");
                break;
            case 1:
                string data = IvySdk.Instance.GetAppsflyerInviterId();
                Debug.Log($"get_inviter_id --> {data}");
                break;
        }
    }

    public void OnOthersinviteDropDown(int value)
    {
        //switch (value)
        //{
        //    case 0:
        //        IvySdk.Instance.SendEmail("email", "data");
        //        Debug.Log("send_email called");
        //        break;
        //    case 1:
        //        string data = IvySdk.Instance.GetConfig(IvySdk.ConfigKeys.CONFIG_KEY_APP_ID);
        //        Debug.Log($"get_config --> {data}");
        //        break;
        //    case 2:
        //        bool isConnected = IvySdk.Instance.IsNetworkConnected();
        //        Debug.Log($"is_network_connected --> {isConnected}");
        //        break;
        //    case 3:
        //        IvySdk.Instance.Rate();
        //        Debug.Log($"rate called");
        //        break;
        //    case 4:
        //        IvySdk.Instance.SystemShareText("text");
        //        Debug.Log("share_text called");
        //        break;
        //    case 5:
        //        IvySdk.Instance.SystemShareImage("title", "pic_patch");
        //        Debug.Log("share_image called");
        //        break;
        //    case 6:
        //        IvySdk.Instance.OpenUrl("https://www.baidu.com");
        //        Debug.Log("open_url called");
        //        break;
        //    case 7:
        //        bool hasNotch = IvySdk.Instance.HasNotch();
        //        Debug.Log($"is_notch_screen --> {hasNotch}");
        //        break;
        //    case 8:
        //        int height = IvySdk.Instance.GetNotchHeight();
        //        Debug.Log($"notch_height --> {height}");
        //        break;
        //    case 9:
        //        IvySdk.Instance.OpenAppStore("market://details?id=package_name");
        //        Debug.Log("open_app_store called");
        //        break;
        //    case 10:
        //        IvySdk.Instance.toast("message");
        //        Debug.Log("toast called");
        //        break;
        //    case 11:
        //        IvySdk.Instance.copyTxt("text");
        //        Debug.Log("copy_text called");
        //        break;
        //    case 12:
        //        int tmMB = IvySdk.Instance.GetTotalMemory();
        //        Debug.Log($"device_total_memory --> {tmMB}");
        //        break;
        //    case 13:
        //        int fmMB = IvySdk.Instance.GetFreeMemory();
        //        Debug.Log($"device_free_memory --> {fmMB}");
        //        break;
        //    case 14:
        //        int tdMB = IvySdk.Instance.GetDiskSize();
        //        Debug.Log($"device_total_disk_size --> {tdMB}");
        //        break;
        //    case 15:
        //        int fdMB = IvySdk.Instance.GetFreeDiskSize();
        //        Debug.Log($"device_free_disk_size --> {fdMB}");
        //        break;
        //}
    }

    private void IvySdkListener_OnReceivedNotificationEvent(string obj)
    {
        Debug.Log($"received notification action --> {obj}");
    }

    private void IvySdkListener_HelperUnreadMsgCountEvent(int obj)
    {
        Debug.Log($"received unread message count --> {obj}");
    }

    //private void IvySdkListener_OnCloudDataSnapshotEvent(string collection, string documentId, bool status)
    //{
    //    Debug.Log($"snapshot result --> {collection};{documentId};{status}");
    //}

    //private void IvySdkListener_OnCloudDataUpdateEvent(string collection, string transactionId, bool status)
    //{
    //    Debug.Log($"update result --> {collection};{transactionId};{status}");
    //}

    //private void IvySdkListener_OnCloudDataDeleteEvent(string data, bool status)
    //{
    //    Debug.Log($"delete result --> {data};{status}");
    //}

    //private void IvySdkListener_OnCloudDataQueryEvent(string collection, string data, bool status)
    //{
    //    Debug.Log($"query result --> {collection};{data};{status}");
    //}

    //private void IvySdkListener_OnCloudDataMergeEvent(string data, bool status)
    //{
    //    Debug.Log($"merge result --> {data};{status}");
    //}

    //private void IvySdkListener_OnCloudDataReadEvent(string collection, string doucumentId, string data, bool status)
    //{
    //    Debug.Log($"read result --> {collection};{doucumentId};{data};{status}");
    //}

    //private void IvySdkListener_OnCloudDataSaveEvent(string data, bool status)
    //{
    //    Debug.Log($"save result --> {data};{status}");
    //}

    private void IvySdkListener_OnFirebaseUnlinkEvent(string platform, bool status)
    {
        Debug.Log($"firestore unlink result --> {platform};{status}");
    }

    private void IvySdkListener_OnFirebaseLoginEvent(string platform, bool status)
    {
        Debug.Log($"firestore login result --> {platform};{status}");
    }

    private void IvySdkListener_OnFacebookLoginEvent(bool status)
    {
        Debug.Log($"facebook login result --> {status}");
    }

    private void IvySdkListener_OnPlayGamesLoginEvent(bool status)
    {
        Debug.Log($"playGames login result --> {status}");
    }

    private void IvySdkListener_OnPaymentWithPayloadEvent(IvySdk.PaymentResult result, int payId, string payload, string merchantTransactionId)
    {
        Debug.Log($"pay result --> {result};{payId};{payload};{merchantTransactionId}");
    }

    private void IvySdkListener_OnPaymentEvent(IvySdk.PaymentResult result, int payId, string merchantTransactionId)
    {
        Debug.Log($"pay result --> {result};{payId};{merchantTransactionId}");
    }

    private void IvySdkListener_OnBannerAdEvent(IvySdk.AdEvents adEvent, string placement)
    {
        switch (adEvent)
        {
            case IvySdk.AdEvents.AD_LOADED:
                Debug.Log($"banner ad loaded; ad placement:{placement}");
                break;
            case IvySdk.AdEvents.AD_LOAD_FAILED:
                Debug.Log($"banner ad load failed; ad placement:{placement}");
                break;
            case IvySdk.AdEvents.AD_SHOW_SUCCEED:
                Debug.Log($"banner ad show success; ad placement:{placement}");
                break;
            case IvySdk.AdEvents.AD_SHOW_FAILED:
                Debug.Log($"banner ad show failed; ad placement:{placement}");
                break;
            case IvySdk.AdEvents.AD_CLICKED:
                Debug.Log($"banner ad clicked; ad placement:{placement}");
                break;
            case IvySdk.AdEvents.AD_CLOSED:
                Debug.Log($"banner ad closed; ad placement:{placement}");
                break;
        }
    }

    private void IvySdkListener_OnRewardedAdEvent(IvySdk.AdEvents adEvent, string placement)
    {
        switch (adEvent)
        {
            case IvySdk.AdEvents.AD_LOADED:
                Debug.Log($"rewarded ad loaded; ad placement:{placement}");
                break;
            case IvySdk.AdEvents.AD_LOAD_FAILED:
                Debug.Log($"rewarded ad load failed; ad placement:{placement}");
                break;
            case IvySdk.AdEvents.AD_SHOW_SUCCEED:
                Debug.Log($"rewarded ad show success; ad placement:{placement}");
                break;
            case IvySdk.AdEvents.AD_SHOW_FAILED:
                Debug.Log($"rewarded ad show failed; ad placement:{placement}");
                break;
            case IvySdk.AdEvents.AD_CLICKED:
                Debug.Log($"rewarded ad clicked; ad placement:{placement}");
                break;
            case IvySdk.AdEvents.AD_CLOSED:
                Debug.Log($"rewarded ad closed; ad placement:{placement}");
                break;
            case IvySdk.AdEvents.AD_REWARD_USER:
                Debug.Log($"rewarded ad reward user; ad placement:{placement}");
                break;
        }
    }

    private void IvySdkListener_OnInterstitialAdEvent(IvySdk.AdEvents adEvent, string placement)
    {
        switch (adEvent)
        {
            case IvySdk.AdEvents.AD_LOADED:
                Debug.Log($"interstitial ad loaded; ad placement:{placement}");
                break;
            case IvySdk.AdEvents.AD_LOAD_FAILED:
                Debug.Log($"interstitial ad load failed; ad placement:{placement}");
                break;
            case IvySdk.AdEvents.AD_SHOW_SUCCEED:
                Debug.Log($"interstitial ad show success; ad placement:{placement}");
                break;
            case IvySdk.AdEvents.AD_SHOW_FAILED:
                Debug.Log($"interstitial ad show failed; ad placement:{placement}");
                break;
            case IvySdk.AdEvents.AD_CLICKED:
                Debug.Log($"interstitial ad clicked; ad placement:{placement}");
                break;
            case IvySdk.AdEvents.AD_CLOSED:
                Debug.Log($"interstitial ad closed; ad placement:{placement}");
                break;
        }
    }

    // Update is called once per frame
    void Update()
    {

    }
    //#endif
}
