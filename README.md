@[TOC](Android SDK 接入文档)

# SDK引入
```js
aar 文件说明:
// 核心模块，必须引入
com_rise_Core_10.0.0.28.aar 
//google 支付模块，可以根据需要引入
com_rise_GooglePay_10.0.0.28.aar
//广告聚合平台，根据需要选择引入
com_rise_Admob_10.0.0.28.aar
com_rise_Yandex_10.0.0.28.aar
com_rise_Max_10.0.0.28.aar
//游戏排行榜、成就等，根据需要选择引入
com_rise_PlayGames_10.0.0.28.aar
//事件统计平台，根据需要引入
com_rise_Appsflyer_10.0.0.28.aar
com_rise_Facebook_10.0.0.28.aar
com_rise_Firebase_10.0.0.28.aar
com_rise_thinkingsdk_10.0.0.28.aar
//云存档模块，根据需要引入
com_rise_Firestore_10.0.0.28.aar
//客服模块，根据需要引入
com_rise_AIHelp_10.0..0.28.aar
```

# 混淆
```js
-keep class com.ivy.sdk.** { *; }
-keep interface com.ivy.sdk.** { *; }
-keep class ivy.data.analytics.** { *; }
-keep interface ivy.data.analytics.** { *; }

#unity 2021及之后版本混淆需要添加
-keep class com.google.androidgamesdk.**{*;}
```
# 初始化
```js
void Awake()
{
    IvySdk.Instance.Init();
}
```

# 广告
> Banner
- 广告事件监听
```js
IvySdkListener.OnBannerAdEvent += IvySdkListener_OnBannerAdEvent;
IvySdkListener.OnBannerAdEvent -= IvySdkListener_OnBannerAdEvent;
private void IvySdkListener_OnBannerAdEvent(IvySdk.AdEvents adEvent, int placement)
{
}
```


- 加载状态
  ```bool isReady = IvySdk.Instance.HasBannerAd();```

- 展示banner
```js
/**
 *  @param tag          广告标签，默认为 default; 必传
 *  @param position     广告位置，参考IvySDK.BannerAdPosition； 必传
 *  @param placement    广告位； 必传
 *  @param clientInfo   客户端自定义信息，字典结构，注意 bool值会被转换位1/0； 可选
 */
IvySdk.Instance.ShowBannerAd(string tag, BannerAdPosition position, int placement);
IvySdk.Instance.ShowBannerAd(string tag, BannerAdPosition position, int placement, string clientInfo);
```

- 关闭Banner
  ```IvySdk.Instance.CloseBannerAd(int placement);```


> 插屏

- 广告事件监听
```js
IvySdkListener.OnInterstitialAdEvent += IvySdkListener_OnInterstitialAdEvent;
IvySdkListener.OnInterstitialAdEvent -= IvySdkListener_OnInterstitialAdEvent;
private void IvySdkListener_OnInterstitialAdEvent(IvySdk.AdEvents adEvent, int placement)
{
    
}
```

- 加载状态
  bool isReady = IvySdk.Instance.HasInterstitialAd();

- 展示插屏
```js
/**
 *  展示 插屏 广告
 *  @param tag          广告标签，默认为 default；必传
 *  @param placement    广告位；可选
 *  @param clientInfo   客户端自定义信息，字典结构，注意 bool值会被转换位1/0； 可选
 */
IvySdk.Instance.ShowInterstitialAd(string tag, int placement, string clientInfo);
```

> 激励视频
- 广告事件监听
```js
IvySdkListener.OnRewardedAdEvent += IvySdkListener_OnRewardedAdEvent;
IvySdkListener.OnRewardedAdEvent -= IvySdkListener_OnRewardedAdEvent;
private void IvySdkListener_OnRewardedAdEvent(IvySdk.AdEvents adEvent, int placement)
{
}
```
- 加载状态
  bool isReady = IvySdk.Instance.HasRewardedAd();


- 展示视频广告
```js
/**
 *  @param tag          广告标签，默认为 default；必传
 *  @param placement    广告位，可用于标记奖励点；必传
 *  @param clientInfo   客户端自定义信息，字典结构，注意 bool值会被转换位1/0； 可选
 */
IvySdk.Instance.ShowRewardedAd(string tag, int placement, string clientInfo));
```

# 计费
- 支付
```js
/**
 *  @param id           计费点位id;必传
 *  @param payload      可选
 *  @param clientInfo   客户端自定义信息，字典结构，注意 bool值会被转换位1/0； 可选
 */
IvySdk.Instance.Pay(int payId);
IvySdk.Instance.Pay(int payId, string payload);
IvySdk.Instance.Pay(int payId, string payload, string clientInfo);
```

- 发货
  ++*在使用在线支付校验流程时，必须调用此接口，否则会存在重复发货情况*++
```js
//merchantTransactionId  预下单Id
IvySdk.Instance.ShippingGoods(string merchantTransactionId);
```

- 计费系统是否可用
  ```bool isReady = IvySdk.Instance.IsPaymentValid();```
- 查询所有计费点位详情
  ```string data = IvySdk.Instance.GetPaymentDatas();```
- 查询指定计费点位详情
  ```string data = IvySdk.Instance.GetPaymentData(int payId);```
- 查询所有未处理支付记录
  ```string data = IvySdk.Instance.QueryPaymentOrders();```
- 查询指定计费点位是否存在未处理支付记录
  ```string data = IvySdk.Instance.QueryPaymentOrder(int payId);```
- 监听计费结果

```js
// 未携带Payload 计费
IvySdkListener.OnPaymentEvent += IvySdkListener_OnPaymentEvent;
IvySdkListener.OnPaymentEvent -= IvySdkListener_OnPaymentEvent;
private void IvySdkListener_OnPaymentEvent(IvySdk.PaymentResult result, int payId, string merchantTransactionId)
{
}

// 携带Payload 计费
IvySdkListener.OnPaymentWithPayloadEvent += IvySdkListener_OnPaymentWithPayloadEvent;
IvySdkListener.OnPaymentWithPayloadEvent -= IvySdkListener_OnPaymentWithPayloadEvent;
private void IvySdkListener_OnPaymentWithPayloadEvent(IvySdk.PaymentResult result, int payId, string payload, string merchantTransactionId)
{
}

```

# 事件统计
++*所有事件接口中data参数结构为 逗号分隔的字符串，
如:key,value,key,value,...*++
- 事件流向所有平台
  ```IvySdk.Instance.TrackEvent(string eventName, string data);```
  ```IvySdk.Instance.TrackEventToConversion(string eventName, string data);```

- 事件流向Firebase
  ```IvySdk.Instance.TrackEventToFirebase(string eventName, string data);```

- 事件流向Facebook
  ```IvySdk.Instance.TrackEventToFacebook(string eventName, string data);```

- 事件流向AppsFlyer
  ```IvySdk.Instance.TrackEventToAppsflyer(string eventName, string data);```

- 事件流向自有平台
  ```IvySdk.Instance.TrackEventToIvy(string eventName, string data);```

- 设置用户属性至所有平台
  ```IvySdk.Instance.SetUserProperty(string key, string 		value);```

- 设置用户属性至Firebase
  ```IvySdk.Instance.SetUserPropertyToFirebase(string key, string value);```

- 设置用户属性至自有平台
  ```IvySdk.Instance.SetUserPropertyToIvy(string key, string value);```

- 设置自定义用户 id
  ```IvySdk.Instance.SetCustomUserId(string value);```

# RemoteConfig
- 获取 Firebase Remote Config 配置值
```js
int value = IvySdk.Instance.GetRemoteConfigInt(string key);//默认值 0
long value = IvySdk.Instance.GetRemoteConfigLong(string key);//默认值 0
double value = IvySdk.Instance.GetRemoteConfigDouble(string key);//默认值 0.0
bool value = IvySdk.Instance.GetRemoteConfigBoolean(string key);//默认值 false
string value = IvySdk.Instance.GetRemoteConfigString(string key);//默认值 ""
```

- 获取 自有 Remote Config 配置值
```js
int value = IvySdk.Instance.GetIvyRemoteConfigInt(string key);//默认值 0
long value = IvySdk.Instance.GetIvyRemoteConfigLong(string key);//默认值 0
double value = IvySdk.Instance.GetIvyRemoteConfigDouble(string key);//默认值 0.0
bool value = IvySdk.Instance.GetIvyRemoteConfigBoolean(string key);//默认值 false
string value = IvySdk.Instance.GetIvyRemoteConfigString(string key);//默认值 ""
```

# 三方登录
> PlayGames
- 事件监听
```js
IvySdkListener.OnPlayGamesLoginEvent += IvySdkListener_OnPlayGamesLoginEvent;
IvySdkListener.OnPlayGamesLoginEvent -= IvySdkListener_OnPlayGamesLoginEvent;
private void IvySdkListener_OnPlayGamesLoginEvent(bool status)
{
// status 登陆状态
}
```

- 登陆状态
  ```bool isLogin = IvySDK.Instance.IsPlayGamesLoggedIn();```

- 登陆(++如果项目配置了PlayGames，sdk会在游戏开启时主动登录PlayGames，客户端可以在登录回调中选择调用此接口++)
  ```IvySDK.Instance.LoginPlayGames();```

- 登出
  ```IvySDK.Instance.LogoutPlayGames();```

- 获取用户信息
  ```string data = IvySDK.Instance.GetPlayGamesUserInfo();```

- 解锁成就
  ```IvySDK.Instance.UnlockAchievement(string achievementId);```

- 提升成就
  ```IvySDK.Instance.IncreaseAchievement(string achievementId, int step);```

- 展示成就页面
  ```IvySDK.Instance.ShowAchievement();```

- 展示排行榜
  ```IvySDK.Instance.ShowLeaderboards();```

- 展示指定排行榜
  ```IvySDK.Instance.ShowLeaderboard(string leaderboardId);```

- 更新排行榜
  ```IvySDK.Instance.UpdateLeaderboard(string leaderboardId, long score);```

> Facebook
- 事件监听
```js
IvySdkListener.OnFacebookLoginEvent += IvySdkListener_OnFacebookLoginEvent;
IvySdkListener.OnFacebookLoginEvent -= IvySdkListener_OnFacebookLoginEvent;
private void IvySdkListener_OnFacebookLoginEvent(bool status)
{
// status 登陆状态
}
```

- 登陆
  ```IvySDK.Instance.LogInFacebook();```

- 登出
  ```IvySDK.Instance.LogoutFacebook();```

- 登陆状态
  ```bool isLogin = IvySDK.Instance.IsFacebookLoggedIn();```

- 获取用户信息
  ```string data = IvySDK.Instance.GetFacebookUserInfo();```

- 获取朋友列表
  ```string data = IvySDK.Instance.GetFacebookFriends();```


> Firebase

- 事件监听
```js
IvySdkListener.OnFirebaseLoginEvent += IvySdkListener_OnFirebaseLoginEvent;
IvySdkListener.OnFirebaseLoginEvent -= IvySdkListener_OnFirebaseLoginEvent;
private void IvySdkListener_OnFirebaseLoginEvent(string platform, bool status)
{
// platform 登陆平台，值请参考 FirebaseLinkChannel
// status 登陆状态
}
```
- 匿名登陆
  ```IvySDK.Instance.LoginFBWithAnonymous()```

- PlayGames渠道登陆
  ```IvySDK.Instance.LoginFBWithPlayGames()```

- Facebook渠道登陆
  ```IvySDK.Instance.LoginFBWithFacebook()```

- Email渠道登陆
  ```IvySDK.Instance.LoginFBWithEmailAndPwd(string email, string password)```

- 重载Firebase的登陆状态
  ```IvySDK.Instance.ReloadFirebaseLogStatus()```

- 是否可登出指定渠道(++channel 参考FirebaseLinkChannel++)
  ```bool status = IvySDK.Instance.CanFirebaseUnlinkWithChannel(string channel)```

- 登出指定渠道
  ```IvySDK.Instance.UnlinkFirebaseWithChannel(string channel)```

- 是否已登陆指定渠道
  ```bool status = IvySDK.Instance.IsFirebaseLinkedWithChannel(string channel)```

- 是否为匿名登陆
  ```bool status = IvySDK.Instance.IsFirebaseAnonymousLoggedIn()```

- 获取指定渠道用户信息
  ```string data = IvySDK.Instance.GetFirebaseUserInfo(string channel)```

- 登出
  ```IvySDK.Instance.LogoutFirebase()```

# Firestore云存档
- 存储数据到指定数据集合
```js
/**
 * @param collection     数据集合
 * @param jsonData       
 */
IvySDK.Instance.SaveCloudData(string collection, string jsonData);
```

- 存储结果监听
```js
IvySdkListener.OnCloudDataSaveEvent += IvySdkListener_OnCloudDataSaveEvent;
IvySdkListener.OnCloudDataSaveEvent -= IvySdkListener_OnCloudDataSaveEvent;
private void IvySdkListener_OnCloudDataSaveEvent(string collection, bool status)
{
}
```


- 读取指定数据集合
```js
/**
 * @param collection     数据集合  
 */
IvySDK.Instance.ReadCloudData(string collection);
```

- 读取指定数据集合内文档
```js
/**
 * @param collection     数据集合
 * @param documentId     文档id   
 */
IvySDK.Instance.ReadCloudData(string collection, string documentId);
```

- 读取结果监听
```js
IvySdkListener.OnCloudDataReadEvent += IvySdkListener_OnCloudDataReadEvent;
IvySdkListener.OnCloudDataReadEvent -= IvySdkListener_OnCloudDataReadEvent;
private void IvySdkListener_OnCloudDataReadEvent(string collection, string doucumentId, string data, bool status)
{
}
```

- 合并数据
```js
/**
 * @param collection     数据集合
 * @param jsonData       
 */
IvySDK.Instance.MergeCloudData(string collection, string jsonData);
```

- 合并结果监听
```js
IvySdkListener.OnCloudDataMergeEvent += IvySdkListener_OnCloudDataMergeEvent;
IvySdkListener.OnCloudDataMergeEvent -= IvySdkListener_OnCloudDataMergeEvent;
private void IvySdkListener_OnCloudDataMergeEvent(string collection, bool status)
{
}
```

- 查询数据
```js
/**
 * @param collection     数据集合  
 */
IvySDK.Instance.QueryCloudData(string collection);
```

- 查询结果监听
```js
IvySdkListener.OnCloudDataQueryEvent += IvySdkListener_OnCloudDataQueryEvent;
IvySdkListener.OnCloudDataQueryEvent -= IvySdkListener_OnCloudDataQueryEvent;
private void IvySdkListener_OnCloudDataQueryEvent(string collection, string data, bool status)
{
}
```


- 删除数据
```js
/**
 * @param collection     数据集合  
 */
IvySDK.Instance.DeleteCloudData(string collection);
```
- 删除结果监听
```js
IvySdkListener.OnCloudDataDeleteEvent += IvySdkListener_OnCloudDataDeleteEvent;
IvySdkListener.OnCloudDataDeleteEvent -= IvySdkListener_OnCloudDataDeleteEvent;
private void IvySdkListener_OnCloudDataDeleteEvent(string collection, bool status)
{
}
```


- 更新数据
```js
/**
 * @param collection        数据集合
 * @param transactionId     事务Id
 * @param jsonData      
 */
IvySDK.Instance.UpdateCloudData(string collection, string transactionId, string jsonData);
```
- 更新结果监听
```js
IvySdkListener.OnCloudDataUpdateEvent += IvySdkListener_OnCloudDataUpdateEvent;
IvySdkListener.OnCloudDataUpdateEvent -= IvySdkListener_OnCloudDataUpdateEvent;
private void IvySdkListener_OnCloudDataDeleteEvent(string collection, string transactionId, bool status)
{
}
```

- 备份数据
```js
/**
 * @param collection     数据集合  
 */
IvySDK.Instance.SnapshotCloudData(string collection);
```

- 备份数据
```js
/**
 * @param collection     数据集合
 * @param documentId     文档id   
 */
IvySDK.Instance.SnapshotCloudData(string collection, string documentId);
```

- 备份结果监听
```js
IvySdkListener.OnCloudDataSnapshotEvent += IvySdkListener_OnCloudDataSnapshotEvent;
IvySdkListener.OnCloudDataSnapshotEvent -= IvySdkListener_OnCloudDataSnapshotEvent;
private void IvySdkListener_OnCloudDataSnapshotEvent(string collection, string documentId, bool status)
{
}
```

# 客服
- 客服准备状态
  ```bool isReady = IvySDK.Instance.IsHelperInitialized();```

- 是否有新的客服消息
  ```bool isReady = IvySDK.Instance.HasNewHelperMessage();```

- 跳转客服
```js
/**
 * @param entranceId            自定义入口 ID; 必传
 * @param meta                  自定义用户属性，字典格式；可选
 * @param tags                  用户标签，AIHelp需要预先在后台定义用户标签；可选
 * @param welcomeMessage        欢迎语；可选
 */
IvySDK.Instance.ShowHelper(string entranceId, string meta, string tags, string welcomeMessage);
```

- 跳转指定客服单页
```js
/**
 * @param faqId     指定单页id; 必传
 * @param monment   
 */
IvySDK.Instance.ShowHelperSingleFAQ(string faqId, int moment = 3);
```

- 监听未读消息
  ```IvySDK.Instance.ListenHelperUnreadMsgCount(bool onlyOnce);```

- 停止监听未读消息
  ```IvySDK.Instance.StopListenHelperUnreadMsgCount();```

- 未读消息监听
```js
IvySdkListener.HelperUnreadMsgCountEvent += IvySdkListener_HelperUnreadMsgCountEvent;
IvySdkListener.HelperUnreadMsgCountEvent -= IvySdkListener_HelperUnreadMsgCountEvent;
private void IvySdkListener_HelperUnreadMsgCountEvent(int count)
{
}
```

- 更新用户属性
```js
/**
 * @param data      用户属性，字典格式；可选
 * @param tags      用户标签，AIHelp需要预先在后台定义用户标签；可选
 */
IvySDK.Instance.UpdateHelperUserInfo(string data, string tags);
```

- 重置用户属性
  ```IvySDK.Instance.ResetHelperUserInfo();```

- 关闭客服
  ```IvySDK.Instance.CloseHelper();```

# 通知
- 权限状态
```js
/**
 *  @returns        0: 权限被彻底拒绝，需要跳转设置页面开启
 *                  1: 权限已开启
 *                  2: 权限状态待定，仍可通过系统接口请求
 */
int state = IvySDK.Instance.LoadNotificationPermissionState();
```

- 请求权限
  ```IvySDK.Instance.RequestNotificationPermission();```

- 跳转权限设置页
  ```IvySDK.Instance.OpenNotificationSettings();```

- 本地通知任务
```js
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
IvySDK.Instance.PushNotificationTask(string tag, string title, string subtitle, string bigText, string smallIcon, string largeIcon, string bigPicture, long delay, bool autoCancel, string action, bool repeat, bool requireNetwork, bool requireCharging);
```

- 通知栏点击监听
```js
IvySdkListener.OnReceivedNotificationEvent += IvySdkListener_OnReceivedNotificationEvent;
IvySdkListener.OnReceivedNotificationEvent -= IvySdkListener_OnReceivedNotificationEvent;
private void IvySdkListener_OnReceivedNotificationEvent(string action)
{
    
}
```

- 关闭本地通知任务
  ```IvySDK.Instance.CancelNotification(string tag);```

# Appsflyer 用户互邀
- 发送邀请
  ```IvySDK.Instance.AppsflyerInviteUser(string inviterId, string inviterAppId);```
- 获取邀请者id
```js
/**
 * @returns inviterId  格式为 inviterId|inviterAppId
 */
string inviterId = IvySDK.Instance.GetAppsflyerInviterId();
```


# 其它
- 发送邮件
  ```IvySDK.Instance.SendEmail(string email, string extra);```
  ```IvySDK.Instance.SendEmail(string email, string title, string extra);```

- 网络状态
  ```bool isConnected = IvySDK.Instance.IsNetworkConnected();```

- 评价
  ```IvySDK.Instance.Rate();```

- 分享文本
  ```IvySDK.Instance.SystemShareText(String txt);```

- 分享图片
  ```IvySDK.Instance.SystemShareImage(String title, String imagePath);```

- 是否刘海屏
  ```bool hasNotch = IvySDK.Instance.HasNotch();```

- 刘海高度
  ```int height = IvySDK.Instance.GetNotchHeight();```

- 跳转应用商店
```js
/**
 * @param url    1.null，指定本游戏；2.指定游戏包名；3.应用商店地址
 */
IvySDK.Instance.OpenAppStore(String url);
```

- toast
  ```IvySDK.Instance.toast(String message);```

- 复制文本
  ```IvySDK.Instance.copyTxt(String txt);```

- 设备总内存，单位MB
  ```int size = IvySDK.Instance.GetTotalMemory();```

- 设备可用内存，单位MB
  ```int size = IvySDK.Instance.GetFreeMemory();```

- 设备总磁盘存储，单位MB
  ```int size = IvySDK.Instance.GetDiskSize();```

- 设备可用磁盘存储，单位MB
  ```int size = IvySDK.Instance.GetFreeDiskSize();```


# 配置文件

## 配置说明
> 广告
```js
广告位配置:
provider  	： 广告平台
p  		： 广告点配置
placement  	： 广告位 id
adaptive  	： 是否为自适应尺寸（仅banner需要配置），默认false
collapsible 	:  是否为可折叠（仅banner需要配置），默认 null, 可选值：1. top：广告位于屏幕顶部，展开式广告的顶部与收起式广告的顶部对齐； 2. bottom：广告位于屏幕底部，展开后广告的底部与收起后广告的底部对齐。
priority 	： 广告位展示优先级
aps_placement	:  amazon 广告id，仅支持banner

广告加载配置(adConfig)： 
delayOnLoadFail 	： 广告加载失败时，下一次加载的延迟时间，随着失败次数的增加，磁时间会叠加增长，达到 timesDelayOnLoadFail 时重置; 单位：秒
timesDelayOnLoadFail 	： 广告加载失败时，下一次加载需要延迟的次数
adLoadTimeOut 		: 自定义的广告加载超时时间; 单位：秒
bannerAdRefreshDuration ： banner自动刷新时间; 单位：秒
bannerRefreshByPlatform : banner 由广告平台自动刷新(如果广告刷新交由平台管理，只能配置一个banner ad id)
```

> 统计
```js
enableAdPing		： 统计广告收入事件开关
enablePurchasePing	： 统计计费收入事件开关
inviter_template_id	： appsflyer用户邀请模板
```

> 事件

eventChannel: 事件流向控制，此设置优先级最高
配置方式：
0000：共4位，分别对应四个打点平台； 0： 关闭，1：开启
第1位： appsflyer
第2位： firebase
第3位： facebook
第4位： thing data
示例：如事件只 流向firebase，配置位 ”0100“
```js
     "event_channel": {
       "banner_displayed": "0000"
     }
```

combinedEvents： 组合事件
```js
"video_shown_2_in3day": {
        "d": 3,
        "e": [
          "sdk_ad_request"
        ],
        "p": {
          "ad_format": "interstitial"
        },
        "r": false,
        "v": 2,
        "op": ">="
      }

说明：
video_shown_2_in3day 	：组合事件名称
d			: 天数，从游戏安装日期开始
v			: 条件触发次数
op			: 判断方式，如： 触发次数 >= v
r			: 重复触发组合事件
e			: 可触发的事件列表， 多事件名，满足其一即可
p			: 可触发的事件属性，如e存在有效值，则必须满足e条件，多属性，满足其一即可
```

accumulateEvent：累计组合事件
```js
"vs": {
        "e": [
          "sdk_ad_request"
        ],
        "p": [],
        "count": [
          2,
          3,
          5,
          10,
          20,
          50,
          100
        ]
      }

说明：
vs	: 组合事件名称
e	: 可触发的事件列表， 多事件名，满足其一即可
p	: 可触发的事件属性，如e存在有效值，则必须满足e条件，多属性，满足其一即可
count	: 触发次数，累计
```

> 计费
```js
"payment": {
    "verify-url": "https://verify.ivymobile.com/api/external/v1/purchase",
    "key": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxqXkK5zMvvelr5uUpshbTQz8Sh54o9h7J3eY/Z4BVvvedQgsgq+bNx+rH9Guhy+VLvhYsuJAi7/LnOB1wXcTIEPEiKUYZlZBCXob5e2lepwlgz/nWptfJrse1Gzm6StJ1fUDIJn+eszuGL8lYksPGcvE/Z+qEInr5kfmzPApRta+sA0Etip0Rm0ye/LgxKdEpIjvzDSXzLS4pLL/n5XcWHHey8wUJS601E0ZbiQvEG9gWHoFPTmeuKSiaBywTE7ds6bQBCaYGNnSWRpwCkcVXeOQV/icMcq2bFWrKsR1KcuwLAoTgFxCa/A7TMOKAwq4cF2wzgaELCFNizc/OzgSTwIDAQAB",
    "checkout": {
      "1": {
        "usd": 1.99,
        "repeat": 1,
        "feename": "20coins"
      }
    }
  }
```
verify-url	: 在线校验地址

> 三方服务
如需要登陆指定平台，则必须增加对应配置
```js
"facebook": {
// facebook 用户权限
      "permissions": [
        "public_profile",
        "email",
        "user_friends"
      ]
    },
    "firestore": [],
    "playGames": {
//必须配置此id，可在google开发者后台获取
      "web_client_id": "976142984853-u08f01in8d1pdhq1d9r6mli2gh83nck0.apps.googleusercontent.com"
    }
```

## 配置文件示例：
```json
{
  "ads": {
    "full": [
      {
        "p": {
          "priority": 1,
          "placement": "10994997af1ae8fb"
        },
        "provider": "applovinmax"
      }
    ],
    "video": [
      {
        "p": {
          "priority": 1,
          "placement": "ff53266146feed2f"
        },
        "provider": "applovinmax"
      }
    ],
    "banner": [
      {
        "p": {
          "adaptive": true,
          "priority": 1,
          "placement": "f3bc47b99733d26b",
          "collapsible": "bottom",
          "aps_placement": ""
        },
        "provider": "applovinmax"
      }
    ],
    "adConfig": {
      "adLoadTimeOut": 60,
      "delayOnLoadFail": 2,
      "timesDelayOnLoadFail": 2,
      "bannerAdRefreshDuration": 20000,
      "bannerRefreshByPlatform": true
    }
  },
  "gts": 1737538304000,
  "push": [],
  "appid": "2732",
  "token": "5dbced9099e90ad5be87e4625f95448f",
  "track": {
    "platform": {
      "facebook": {
        "enableAdPing": true,
        "enablePurchasePing": true
      },
      "firebase": {
        "enableAdPing": true,
        "enablePurchasePing": true
      },
      "appsflyer": {
        "app_key": "J6ejjnUP9fMkv29PqBuYzR",
        "enableAdPing": true,
        "enablePurchasePing": true,
        "inviter_template_id": ""
      },
      "thinkingData": {
        "app_key": "2732",
        "server_url": "https://1331017298-90kgoi3kgz.na-siliconvalley.tencentscf.com",
        "enableAdPing": true,
        "enablePurchasePing": true
      }
    },
    "eventChannel": {
      "sdk_ad_closed": "0001",
      "sdk_ad_request": "0001"
    },
    "combinedEvents": {
      "video_shown_2_in3day": {
        "d": 3,
        "e": [
          "sdk_ad_request"
        ],
        "p": {
          "ad_format": "interstitial"
        },
        "r": false,
        "v": 2,
        "op": ">="
      }
    },
    "accumulateEvent": {
      "vs": {
        "e": [
          "sdk_ad_request"
        ],
        "p": [],
        "count": [
          2,
          3,
          5,
          10,
          20,
          50,
          100
        ]
      }
    }
  },
  "v_api": "16",
  "domain": "",
  "helper": [],
  "uacApi": "https://hda2k62cp0.execute-api.us-west-1.amazonaws.com/top_user_advalue",
  "payment": {
    "verify-url": "https://verify.ivymobile.com/api/external/v1/purchase",
    "key": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxqXkK5zMvvelr5uUpshbTQz8Sh54o9h7J3eY/Z4BVvvedQgsgq+bNx+rH9Guhy+VLvhYsuJAi7/LnOB1wXcTIEPEiKUYZlZBCXob5e2lepwlgz/nWptfJrse1Gzm6StJ1fUDIJn+eszuGL8lYksPGcvE/Z+qEInr5kfmzPApRta+sA0Etip0Rm0ye/LgxKdEpIjvzDSXzLS4pLL/n5XcWHHey8wUJS601E0ZbiQvEG9gWHoFPTmeuKSiaBywTE7ds6bQBCaYGNnSWRpwCkcVXeOQV/icMcq2bFWrKsR1KcuwLAoTgFxCa/A7TMOKAwq4cF2wzgaELCFNizc/OzgSTwIDAQAB",
    "checkout": {
      "1": {
        "usd": 1.99,
        "repeat": 1,
        "feename": "20coins"
      }
    }
  },
  "gameServices": {
    "facebook": {
      "permissions": [
        "public_profile",
        "email",
        "user_friends"
      ]
    },
    "firestore": [],
    "playGames": {
      "web_client_id": "976142984853-u08f01in8d1pdhq1d9r6mli2gh83nck0.apps.googleusercontent.com"
    }
  },
  "remoteConfig": {
    "test_key": true
  },
  "adPingThreshold": 0.01
}
```