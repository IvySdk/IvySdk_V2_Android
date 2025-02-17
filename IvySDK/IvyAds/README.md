"ads": {
"banner": [
{
"provider": "admob",
"p": {
"placement": "ca-app-pub-3940256099942544/9214589741",
"adaptive": true,
"collapsible", "top",
"priority": 1
}
}
],
"full": [
{
"provider": "admob",
"p": {
"placement": "ca-app-pub-3940256099942544/1033173712",
"priority": 1
}
}
],
"video": [
{
"provider": "admob",
"p": {
"placement": "ca-app-pub-3940256099942544/5224354917",
"priority": 1
}
}
],
"adConfig": {
"delayOnLoadFail": 2,
"timesDelayOnLoadFail": 2,
"adLoadTimeOut": 60,
"bannerAdRefreshDuration": 20,
"bannerRefreshByPlatform": false
}
}

字段说明
provider  ： 广告平台
p  ： 广告点配置
placement  ： 广告位 id
adaptive  ： 是否为自适应尺寸（仅banner需要配置），默认false
collapsible : 是否为可折叠（仅banner需要配置），默认 null, 可选值：1. top：广告位于屏幕顶部，展开式广告的顶部与收起式广告的顶部对齐； 2. bottom：广告位于屏幕底部，展开后广告的底部与收起后广告的底部对齐。
priority ： 广告位展示优先级

adConfig ： 广告加载配置 
delayOnLoadFail ： 广告加载失败时，下一次加载的延迟时间，随着失败次数的增加，磁时间会叠加增长，达到 timesDelayOnLoadFail 时重置; 单位：秒
timesDelayOnLoadFail ： 广告加载失败时，下一次加载需要延迟的次数
adLoadTimeOut : 自定义的广告加载超时时间; 单位：秒
bannerAdRefreshDuration ： banner自动刷新时间; 单位：秒
bannerRefreshByPlatform : banner 由广告平台自动刷新(如果广告刷新交由平台管理，只能配置一个banner ad id)






