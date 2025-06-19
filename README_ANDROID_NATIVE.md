@[TOC](Android SDK 接入文档)

# SDK引入
```js
    def sdk_version = "xxx"
// 核心模块，必须引入
    implementation("io.github.ivysdk:Core:$sdk_version")
//google 支付模块，可以根据需要引入
    implementation("io.github.ivysdk:GooglePay:$sdk_version")
//广告聚合平台，根据需要选择引入
    implementation("io.github.ivysdk:Admob:$sdk_version")
    implementation("io.github.ivysdk:Max:$sdk_version")
    implementation("io.github.ivysdk:Yandex:$sdk_version")
//游戏排行榜、成就等，根据需要选择引入
    implementation("io.github.ivysdk:PlayGames:$sdk_version")
//事件统计平台，根据需要引入
    implementation("io.github.ivysdk:Appsflyer:$sdk_version")
    implementation("io.github.ivysdk:Facebook:$sdk_version")
    implementation("io.github.ivysdk:Firebase:$sdk_version")
    implementation("io.github.ivysdk:thinkingsdk:$sdk_version")
//云存档模块，根据需要引入
    implementation("io.github.ivysdk:Firestore:$sdk_version")
//客服模块，根据需要引入
    implementation("io.github.ivysdk:AIHelp:$sdk_version")
```

# 混淆
```js
-keep class com.ivy.sdk.** { *; }
-keep interface com.ivy.sdk.** { *; }
-keep class ivy.data.analytics.** { *; }
-keep interface ivy.data.analytics.** { *; }

#unity 2021及之后版本混淆需要添加
-keep class com.google.androidgamesdk.**{*;}

-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}
```

# 原生配置
```js
请在原生项目 build.gradle文件内增加以下配置:
android {
//...
    defaultConfig {
       //...
        manifestPlaceholders = [
                "ivy_debug"            : true,
                "ivy_app_id"           : "2732",//请联系运营人员获取 app id
                "facebook_app_id"      : "443246477338698",
                "facebook_client_token": "e27c126af9b1fc8b66b42111eabab047",
                "admob_application_id" : "ca-app-pub-3940256099942544~3347511713",
                "gms_play_services_id" : "16951538787", 
                "applovin_sdk_key"      : "E8pVhU9mykQd3y0TD0Ksoq4vpf_Muat6ifcP9m96UakTWk5klQaWEeQ2IPOA-GHgxu54eEA8pvgKcn2MBdtQGH",
                "ivy_notch"             : true, //是否适配刘海屏
                "aps_id"                : "" //amazion app id
        ]
    }
}
```

# 初始化
```js
        Builder builder = new Builder.Build().build();
        AndroidSdk.onCreate(activity, builder)
```
<br>

# 广告

- 广告事件监听
```js
    Builder builder = new Builder.Build().setAdListener(new IAdListener() {
            @Override
            public void onAdLoadSuccess(@NonNull AdType adType) {
                
            }

            @Override
            public void onAdLoadFailure(@NonNull AdType adType, @Nullable String reason) {

            }

            @Override
            public void onAdShowSuccess(@NonNull AdType adType, @NonNull String tag, int placement) {

            }

            @Override
            public void onAdShowFailed(@NonNull AdType adType, @Nullable String reason, @NonNull String tag, int placement) {

            }

            @Override
            public void onAdClicked(@NonNull AdType adType, @NonNull String tag, int placement) {

            }

            @Override
            public void onAdClosed(@NonNull AdType adType, boolean gotReward, @NonNull String tag, int placement) {

            }
        }).build();
```

> Banner
- 加载状态<br>
  ```boolean isReady = AndroidSdk.hasBannerAd();```
  <br>
- 展示banner
```js
/**
 *  @param tag          广告标签，默认为 default; 必传
 *  @param pos          广告位置，参考BannerPosition； 必传
 *  @param placement    广告位； 必传
 *  @param clientInfo   客户端自定义信息，JSONObject结构，注意 bool值会被转换位1/0； 可选
 */
AndroidSdk.showBannerAd(String tag, int pos, int placement, String clientInfo);
```
- 关闭Banner<br>
  ```AndroidSdk.CloseBannerAd(int placement);```
  <br>

> 插屏

- 加载状态<br>
  ```boolean isReady = AndroidSdk.hasInterstitialAd();```
  <br>
- 展示插屏
```js
/**
 *  展示 插屏 广告
 *  @param tag          广告标签，默认为 default；必传
 *  @param placement    广告位；可选
 *  @param clientInfo   客户端自定义信息，JSONObject结构，注意 bool值会被转换位1/0； 可选
 */
AndroidSdk.showInterstitialAd(String tag, int placement, String clientInfo);
```
<br>

> 激励视频

- 加载状态<br>
  ```boolean isReady = AndroidSdk.hasRewardedAd();```
  <br>
- 展示视频广告
```js
/**
 *  @param tag          广告标签，默认为 default；必传
 *  @param placement    广告位，可用于标记奖励点；必传
 *  @param clientInfo   客户端自定义信息，JSONObject结构，注意 bool值会被转换位1/0； 可选
 */
AndroidSdk.showRewardedAd(String tag, int placement, String clientInfo);
```
<br>

# 计费
- 支付
```js
/**
 *  @param id           计费点位id;必传
 *  @param payload      可选
 *  @param clientInfo   客户端自定义信息，JSONObject结构，注意 bool值会被转换位1/0； 可选
 */
AndroidSdk.Pay(int id, String payload, String clientInfo);
```
- 发货<br>
  ++*在使用在线支付校验流程时，必须调用此接口，否则会存在重复发货情况*++
```js
//merchantTransactionId  预下单Id
AndroidSdk.shippingGoods(string merchantTransactionId);
```
- 计费系统是否可用<br>
  ```boolean isReady = AndroidSdk.isPaymentValid();```
  <br>
- 查询所有计费点位详情<br>
  ```String data = AndroidSdk.getPurchaseInfo(-1);```
  <br>
- 查询指定计费点位详情<br>
  ```String data = AndroidSdk.getPurchaseInfo(int payId);```
  <br>
- 查询所有未处理支付记录<br>
  ```String data = AndroidSdk.query(-1);```
  <br>
- 查询指定计费点位是否存在未处理支付记录<br>
  ```String data = AndroidSdk.query(int payId);```
  <br>
- 监听计费结果
```js
      Builder builder = new Builder.Build().setPurchaseListener(new IPurchaseResult() {
            @Override
            public void payResult(int payId, int status, @Nullable String payload, @Nullable String merchantTransactionId) {
                
            }

            @Override
            public void onShippingResult(@NonNull String merchantTransactionId, boolean status) {

            }

            @Override
            public void onStoreInitialized(boolean initState) {

            }
        }).build();
```
<br>

# 事件统计 <br>
++*所有事件接口中data参数结构为 单层JSONObject
<br>

- 事件流向所有平台<br>
  ```AndroidSdk.trackEvent(string eventName, string data, 5);```
  <br>

- 事件流向Firebase<br>
  ```AndroidSdk.trackEvent(string eventName, string data, 1);```
  <br>

- 事件流向Facebook<br>
  ```AndroidSdk.trackEvent(string eventName, string data, 2);```
  <br>

- 事件流向AppsFlyer<br>
  ```AndroidSdk.trackEvent(string eventName, string data, 3);```
  <br>

- 事件流向自有平台<br>
  ```AndroidSdk.trackEvent(string eventName, string data, 4);```
  <br>

- 设置用户属性至所有平台<br>
  ```AndroidSdk.setUserProperty(string key, string value, 5);```
  <br>

- 设置用户属性至Firebase<br>
  ```AndroidSdk.setUserProperty(string key, string value, 1);```
  <br>

- 设置用户属性至自有平台<br>
  ```AndroidSdk.setUserProperty(string key, string value, 4);```
  <br>


# RemoteConfig
- 获取 Firebase Remote Config 配置值
```js
int value = AndroidSdk.getRemoteConfigInt(string key);//默认值 0
long value = AndroidSdk.getRemoteConfigLong(string key);//默认值 0
double value = AndroidSdk.getRemoteConfigDouble(string key);//默认值 0.0
boolean value = AndroidSdk.getRemoteConfigBoolean(string key);//默认值 false
String value = AndroidSdk.getRemoteConfigString(string key);//默认值 ""
```

- 获取 自有 Remote Config 配置值
```js
int value = AndroidSdk.getIvyRemoteConfigInt(string key);//默认值 0
long value = AndroidSdk.getIvyRemoteConfigLong(string key);//默认值 0
double value = AndroidSdk.getIvyRemoteConfigDouble(string key);//默认值 0.0
boolean value = AndroidSdk.getIvyRemoteConfigBoolean(string key);//默认值 false
String value = AndroidSdk.getIvyRemoteConfigString(string key);//默认值 ""
```

# 三方登录
> PlayGames
- 事件监听
```js
        Builder builder = new Builder.Build().setAuthListener(new IAuthResult() {
            @Override
            public void onLogout(@NonNull String platform) {

            }

            @Override
            public void onLoginResult(@NonNull String platform, boolean status, @Nullable String channel, @Nullable String reason) {
                // platform  登陆平台，参考 AuthPlatforms
                // status    登陆结果
                // channel   仅在Firebase登陆中有效
                // reason    失败原因
            }
        }).build();
```

- 登陆状态<br>
  ```boolean isLogin = AndroidSdk.isPlayGamesLoggedIn();```
  <br>

- 登陆 <br> (++如果项目配置了PlayGames，sdk会在游戏开启时主动登录PlayGames，客户端可以在登录回调中选择调用此接口++) <br>
  ```AndroidSdk.loginPlayGames();```
  <br>
- 登出<br>
  ```AndroidSdk.logoutPlayGames();```
  <br>
- 获取用户信息<br>
  ```js
    /**
     *  @returns data   结构示例  
     * {
     *     "id":"",          
     *     "name":"",
     *     "photo":"",
     *   }
     */
    String data = AndroidSdk.getPlayGamesUserInfo();
  ```

- 解锁成就<br>
  ```AndroidSdk.unlockAchievement(String achievementId);```
  <br>
- 提升成就<br>
  ```AndroidSdk.increaseAchievement(String achievementId, int step);```
  <br>
- 展示成就页面<br>
  ```AndroidSdk.showAchievement();```
  <br>
- 展示排行榜<br>
  ```AndroidSdk.showLeaderboards();```
  <br>
- 展示指定排行榜<br>
  ```AndroidSdk.showLeaderboard(String leaderboardId);```
  <br>
- 更新排行榜<br>
  ```AndroidSdk.updateLeaderboard(String leaderboardId, long score);```
  <br>

> G+

- 登陆<br>
  ```AndroidSdk.LoginGoogle();```
  <br>
- 登出<br>
  ```AndroidSdk.LogoutGoogle();```
  <br>
- 登陆状态<br>
  ```boolean isLogin = AndroidSdk.IsGoogleLogged();```
  <br>
- 获取用户信息<br>
  ```js
    /**
     *  @returns data   结构示例  
     * {
     *     "id":"",
     *     "name":"",
     *     "photo":"",
     *     "email":""
     *   }
     */
    String data = AndroidSdk.GetGoogleUserInfo();
  ```
- 用户id<br>
  ```String user_id = AndroidSdk.GetGoogleUserId();```


> Facebook
- 事件监听
```js
        Builder builder = new Builder.Build().setAuthListener(new IAuthResult() {
            @Override
            public void onLogout(@NonNull String platform) {

            }

            @Override
            public void onLoginResult(@NonNull String platform, boolean status, @Nullable String channel, @Nullable String reason) {
                // platform  登陆平台，参考 AuthPlatforms
                // status    登陆结果
                // channel   仅在Firebase登陆中有效
                // reason    失败原因
            }
        }).build();
```

- 登陆<br>
  ```AndroidSdk.logInFacebook();```
  <br>
- 登出<br>
  ```AndroidSdk.logoutFacebook();```
  <br>
- 登陆状态<br>
  ```boolean isLogin = AndroidSdk.isFacebookLoggedIn();```
  <br>
- 获取用户信息<br>
  ```js
    /**
     *  @returns data   结构示例  
     * {
     *     "id":"",
     *     "name":"",
     *     "photo":"",
     *   }
     */
    String data = AndroidSdk.getFacebookUserInfo();
  ```

- 获取朋友列表<br>
**必须预先在Facebook后台申请对应权限，然后再default.json中增加facebook登陆权限方可使用**
  ```String data = AndroidSdk.getFacebookFriends();```
  <br>

> Firebase

- 事件监听
```js
        Builder builder = new Builder.Build().setAuthListener(new IAuthResult() {
            @Override
            public void onLogout(@NonNull String platform) {

            }

            @Override
            public void onLoginResult(@NonNull String platform, boolean status, @Nullable String channel, @Nullable String reason) {
                // platform  登陆平台，参考 AuthPlatforms
                // status    登陆结果
                // channel   登陆渠道，仅在Firebase登陆中有效，参考 FirebaseLinkChannel
                // reason    失败原因
            }
        }).build();

```

- 匿名登陆<br>
```js
    AndroidSdk.signAnonymous(new IAuthResponse() {
        @Override
        public void onLoginResult(@NonNull String platform, boolean status, @Nullable String channel, @Nullable String reason) {
                // channel   登陆渠道，仅在Firebase登陆中有效，参考 FirebaseLinkChannel
                // reason    失败原因
        }
    });
```

- Google渠道登陆<br>
```js
        AndroidSdk.signWithGoogle(new IAuthResponse() {

            @Override
            public void onLoginResult(@NonNull String platform, boolean status, String channel, @Nullable String reason) {
                // channel   登陆渠道，仅在Firebase登陆中有效，参考 FirebaseLinkChannel
                // reason    失败原因
            }
        });
```

- PlayGames渠道登陆<br>
  ```js
        AndroidSdk.signWithPlayGames(new IAuthResponse() {

            @Override
            public void onLoginResult(@NonNull String platform, boolean status, String channel, @Nullable String reason) {
                // channel   登陆渠道，仅在Firebase登陆中有效，参考 FirebaseLinkChannel
                // reason    失败原因
            }
        });
  ```
  <br>
- Facebook渠道登陆<br>
  ```js
        AndroidSdk.signWithFacebook(new IAuthResponse() {

            @Override
            public void onLoginResult(@NonNull String platform, boolean status, String channel, @Nullable String reason) {
                // channel   登陆渠道，仅在Firebase登陆中有效，参考 FirebaseLinkChannel
                // reason    失败原因
            }
        });
  ```
  <br>
- Email渠道登陆<br>
  ```js
        AndroidSdk.signWithEmailAndPassword(String email, String password, new IAuthResponse() {

            @Override
            public void onLoginResult(@NonNull String platform, boolean status, String channel, @Nullable String reason) {
                // channel   登陆渠道，仅在Firebase登陆中有效，参考 FirebaseLinkChannel
                // reason    失败原因
            }
        });
  ```
  <br>
- 重载Firebase的登陆状态<br>
  ```js
          AndroidSdk.reloadFirebaseLastSign(new IFirebaseAuthReload() {
            @Override
            public void onReload(boolean status, @Nullable String reason) {
                // status    登陆结果
                // reason    失败原因  
            }
        });
  ```
  <br>
- 是否可登出指定渠道(++channel 参考FirebaseLinkChannel++)<br>
  ```boolean status = AndroidSdk.canFirebaseUnlinkWithChannel(string channel)```
  <br>
- 登出指定渠道<br>
  ```js
          AndroidSdk.unlinkFirebaseWithChannel(String channel, new IFirebaseUnlink() {
            @Override
            public void onUnlinked(@NonNull String unlinkChannel, boolean status, @Nullable String reason) {
                
            }
        });
  ```
  <br>
- 是否已登陆指定渠道<br>
  ```boolean status = AndroidSdk.isFirebaseLinkedWithChannel(String channel)```
  <br>
- 是否为匿名登陆<br>
  ```boolean status = AndroidSdk.isFirebaseAnonymousSign()```
  <br>
- 获取指定渠道用户信息<br>
  ```js
    /**
     *  @returns data   结构示例  
     * {
     *     "id":"",
     *     "name":"",
     *     "photo":"",
     *     "email":""
     *   }
     */
    String data = AndroidSdk.getFirebaseUserInfo(String channel);
  ```

- 登出<br>
  ```AndroidSdk.signOutFirebase()```
  <br>

# Firestore云存档

- 存储数据到指定数据集合
```js
/**
 * @param collection     数据集合
 * @param jsonData       
 */
      AndroidSdk.setArchive(collection, documentId, jsonData, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String documentId, @Nullable String data) {
        
            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String documentId, @Nullable String reason) {

            }
        });
```

- 读取指定数据集合内文档
```js
/**
 * @param collection     数据集合
 * @param documentId     文档id   
 */
      AndroidSdk.readArchive(String collection, String documentId, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {

            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {

            }
        });
```

- 合并数据
```js
/**
 * @param collection     数据集合
 * @param jsonData       
 */
        AndroidSdk.mergeArchive(collection, documentId, jsonData, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String documentId, @Nullable String data) {
       
            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String documentId, @Nullable String reason) {
            
            }
        });
```

- 查询数据
```js
/**
 * @param collection     数据集合  
 */
        AndroidSdk.queryArchive(collection, documentId, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String documentId, @Nullable String data) {
            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String documentId, @Nullable String reason) {

            }
        });
```


- 删除数据
```js
/**
 * @param collection     数据集合  
 */
        AndroidSdk.deleteArchive(collection, documentId, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String documentId, @Nullable String data) {

            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String documentId, @Nullable String reason) {

            }
        });
```

- 更新数据
```js
/**
 * @param collection        数据集合
 * @param transactionId     事务Id
 * @param jsonData      
 */
        AndroidSdk.updateArchive(collection, documentId, jsonData, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String documentId, @Nullable String data) {
            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String documentId, @Nullable String reason) {
            }
        });
```

`

- 备份数据
```js
/**
 * @param collection     数据集合
 * @param documentId     文档id，
 */
     AndroidSdk.snapshotArchive(String collection, String documentId, new IArchiveResult() {
            @Override
            public void onSuccess(@NonNull String collection, @Nullable String document, @Nullable String data) {
      
            }

            @Override
            public void onFailure(@NonNull String collection, @Nullable String document, @Nullable String reason) {
   
            }
        });
```
<br>

# 客服
- 客服准备状态<br>
  ```boolean isReady = AndroidSdk.isHelperInitialized();```
  <br>
- 是否有新的客服消息<br>
  ```boolean isReady = AndroidSdk.hasNewHelperMessage();```
  <br>
- 跳转客服
```js
/**
 * @param entranceId            自定义入口 ID; 必传
 * @param meta                  自定义用户属性，字典格式；可选
 * @param tags                  用户标签，AIHelp需要预先在后台定义用户标签；可选
 * @param welcomeMessage        欢迎语；可选
 */
AndroidSdk.showHelper(String entranceId, String meta, String tags, String welcomeMessage);
```

- 跳转指定客服单页
```js
/**
 * @param faqId     指定单页id; 必传
 * @param monment   
 */
AndroidSdk.showHelperSingleFAQ(String faqId, int moment = 3);
```

- 监听未读消息<br>
  ```AndroidSdk.listenHelperUnreadMsgCount(boolean onlyOnce);```
  <br>
- 停止监听未读消息<br>
  ```AndroidSdk.stopListenHelperUnreadMsgCount();```
  <br>
- 未读消息监听
```js
        Builder builder = new Builder.Build().setHelperListener(new IHelperCallback() {
            @Override
            public void onUnreadHelperMessageCount(int count) {
                
            }
        }).build();
```

- 更新用户属性
```js
/**
 * @param data      用户属性，字典格式；可选
 * @param tags      用户标签，AIHelp需要预先在后台定义用户标签；可选
 */
AndroidSdk.updateHelperUserInfo(String data, String tags);
```

- 重置用户属性<br>
  ```AndroidSdk.resetHelperUserInfo();```
  <br>
- 关闭客服<br>
  ```AndroidSdk.closeHelper();```
  <br>

# 通知
- 权限状态
```js
/**
 *  @returns        0: 权限被彻底拒绝，需要跳转设置页面开启
 *                  1: 权限已开启
 *                  2: 权限状态待定，仍可通过系统接口请求
 */
int state = AndroidSdk.loadNotificationPermissionState();
```

- 请求权限<br>
  ```AndroidSdk.requestNotificationPermission();```
  <br>
- 跳转权限设置页<br>
  ```AndroidSdk.openNotificationSettings();```
  <br>

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
AndroidSdk.pushNotificationTask(String tag, String title, String subtitle, String bigText, String smallIcon, String largeIcon, String bigPicture, long delay, boolean autoCancel, String action, boolean repeat, boolean requireNetwork, boolean requireCharging);
```
<br>

- 通知栏点击监听
```js
        Builder builder = new Builder.Build().setNotificationEventListener(new INotificationEvent() {
            @Override
            public void onReceivedNotificationAction(@NonNull String action) {
                
            }
        }).build();
```

- 关闭本地通知任务<br>
  ```AndroidSdk.cancelNotification(string tag);```
  <br>

# Appsflyer 用户互邀

- 发送邀请<br>
  ```AndroidSdk.appsflyerInviteUser(string inviterId, string inviterAppId);```
  <br>
- 获取邀请者id
```js
/**
 * @returns inviterId  格式为 inviterId|inviterAppId
 */
string inviterId = AndroidSdk.getAppsflyerInviterId();
```
<br>

# 其它
- 发送邮件<br>
  ```AndroidSdk.sendEmail(String email, String extra);```
  <br>
  ```AndroidSdk.sendEmail(String email, String title, String extra);```
  <br>
- 网络状态<br>
  ```boolean isConnected = AndroidSdk.isNetworkConnected();```
  <br>
- 评价<br>
  ```AndroidSdk.rate();```
  <br>
- 分享文本<br>
  ```AndroidSdk.systemShareText(String txt);```
  <br>
- 分享图片<br>
  ```AndroidSdk.systemShareImage(String title, String imagePath);```
  <br>
- 是否刘海屏<br>
  ```boolean hasNotch = AndroidSdk.hasNotch();```
  <br>
- 刘海高度<br>
  ```int height = AndroidSdk.getNotchHeight();```
  <br>
- 跳转应用商店
```js
/**
 * @param url    1.null，指定本游戏；2.指定游戏包名；3.应用商店地址
 */
AndroidSdk.OpenAppStore(String url);
```
- toast<br>
  ```AndroidSdk.toast(String message);```
  <br>
- 复制文本<br>
  ```AndroidSdk.copyTxt(String txt);```
  <br>
- 设备总内存，单位MB<br>
  ```int size = AndroidSdk.getTotalMemory();```
  <br>
- 设备可用内存，单位MB<br>
  ```int size = AndroidSdk.getFreeMemory();```
  <br>

- 设备总磁盘存储，单位MB<br>
  ```int size = AndroidSdk.getDiskSize();```
  <br>

- 设备可用磁盘存储，单位MB<br>
  ```int size = AndroidSdk.getFreeDiskSize();```
  <br>

- 获取配置信息
  ```js
  /**
   * @params key    1       app id
   *                2       
   *                3       数据版本
   *                4       屏幕宽度
   *                5       屏幕高度
   *                6       设备语言
   *                7       设备国家
   *                8       应用版本号，整型
   *                9       应用版本名，字符串
   *                10      包名
   *                11      uuid
   *                12      
   *
    */
  String result = AndroidSdk.getConfig(int key);
  ```
  <br>

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
<br>

> 统计
```js
enableAdPing		： 统计广告收入事件开关
enablePurchasePing	： 统计计费收入事件开关
inviter_template_id	： appsflyer用户邀请模板
```
<br>

> 事件

eventChannel: 事件流向控制，此设置优先级最高<br>
配置方式：<br>
0000：共4位，分别对应四个打点平台； 0： 关闭，1：开启<br>
第1位： appsflyer<br>
第2位： firebase<br>
第3位： facebook<br>
第4位： thing data<br>
示例：如事件只 流向firebase，配置位 ”0100“<br>
```js
     "event_channel": {
       "banner_displayed": "0000"
     }
```
<br>

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
<br>

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
<br>

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
verify-url	: 在线校验地址,使用在线校验、发货时必须配置
<br>

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
<br><br>

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