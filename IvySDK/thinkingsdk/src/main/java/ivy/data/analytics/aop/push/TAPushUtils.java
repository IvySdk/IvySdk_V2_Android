/*
 * Copyright (C) 2022 ThinkingData
 */
package ivy.data.analytics.aop.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ivy.data.analytics.ThinkingAnalyticsSDK;
import ivy.data.analytics.ThinkingDataRuntimeBridge;
import ivy.data.analytics.utils.TDConstants;

/**
 * push click event util
 *
 * @author liulongbing
 * @since 2022/5/31
 */
public class TAPushUtils {

    private static final String TA_PUSH_CLICK_EVENT = "te_ops_push_click";
    private static final List<PushEventItem> pushList = new ArrayList<>();
    public static List<String> gtMsgList = new ArrayList<>();

    public static void handleStartIntent(Intent intent) {
        // huawei oppo vivo fcm
        if (TAPushUtils.handleBundleExtraData(intent)) return;
        // meizu
        if (TAPushUtils.handleIntentExtraData(intent)) return;
        //jpush
        if (handleJPushIntentData(intent)) return;
        // xiaomi
        TAPushUtils.handleMiPushData(intent);
    }

    /**
     * handle mi push data
     *
     * @param intent intent
     */
    public static void handleMiPushData(Intent intent) {
        if (null == intent) return;
        try {
            Object miPushObj = intent.getSerializableExtra("key_message");
            if (null != miPushObj) {
                Class<?> miPushMessageClazz = miPushObj.getClass();
                Method getExtrasMethod = miPushMessageClazz.getMethod("getExtra");
                Object miObj = getExtrasMethod.invoke(miPushObj);
                if (miObj instanceof Map) {
                    Map<String, String> miMsg = ( Map<String, String> ) miObj;
                    String te_extras = ( String ) miMsg.get("te_extras");
                    trackPushClickEvent(te_extras);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * push data in extras
     *
     * @param intent intent
     * @return boolean track push click event success
     */
    public static boolean handleBundleExtraData(Intent intent) {
        if (null == intent) return false;
        try {
            Bundle bundle = intent.getExtras();
            if (null != bundle) {
                String te_extras = bundle.getString("te_extras");
                return trackPushClickEvent(te_extras);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * push data in intent
     *
     * @param intent intent
     * @return boolean track push click event success
     */
    public static boolean handleIntentExtraData(Intent intent) {
        if (null == intent) return false;
        try {
            String te_extras = intent.getStringExtra("te_extras");
            return trackPushClickEvent(te_extras);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean handleJPushIntentData(Intent intent) {
        if (null == intent) return false;
        try {
            String pushData = null;
            if (intent.getData() != null) {
                pushData = intent.getData().toString();
            }
            if (TextUtils.isEmpty(pushData) && intent.getExtras() != null) {
                pushData = intent.getExtras().getString("JMessageExtra");
            }
            if (null != pushData && !TextUtils.isEmpty(pushData)) {
                JSONObject jsonObject = new JSONObject(pushData);
                Object extras = jsonObject.opt("n_extras");
                JSONObject json = null;
                if (extras instanceof String) {
                    json = new JSONObject(( String ) extras);
                } else if (extras instanceof JSONObject) {
                    json = ( JSONObject ) extras;
                }
                if (null != json) {
                    return trackPushClickEvent(json.optString("te_extras"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void handleExtraReceiverData(String extras) {
        try {
            if (TextUtils.isEmpty(extras)) return;
            JSONObject json = new JSONObject(extras);
            trackPushClickEvent(json.optString("te_extras"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean trackPushClickEvent(String te_extras) {
        boolean trackSuccess = false;
        try {
            if (TextUtils.isEmpty(te_extras)) return false;
            JSONObject json = new JSONObject(te_extras);
            Object obj = json.opt("#ops_receipt_properties");
            JSONObject ops = null;
            if (obj instanceof String) {
                ops = new JSONObject(( String ) obj);
            } else if (obj instanceof JSONObject) {
                ops = ( JSONObject ) obj;
            }
            if (null != ops) {
                final JSONObject properties = new JSONObject();
                properties.put("#ops_receipt_properties", ops);
                trackSuccess = true;
                final boolean[] flags = new boolean[1];
                ThinkingAnalyticsSDK.allInstances(new ThinkingAnalyticsSDK.InstanceProcessor() {
                    @Override
                    public void process(ThinkingAnalyticsSDK instance) {
                        flags[0] = true;
                        if (instance.mConfig.mEnableAutoPush) {
                            ThinkingDataRuntimeBridge.onAppPushClickEvent(instance, TA_PUSH_CLICK_EVENT, properties);
                        }
                    }
                });
                if (!flags[0]) {
                    PushEventItem item = new PushEventItem();
                    item.type = TDConstants.DataType.TRACK;
                    item.properties = properties;
                    pushList.add(item);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return trackSuccess;
    }

    public static void handlePushToken(final JSONObject json) {
        final boolean[] flags = new boolean[1];
        ThinkingAnalyticsSDK.allInstances(new ThinkingAnalyticsSDK.InstanceProcessor() {
            @Override
            public void process(ThinkingAnalyticsSDK instance) {
                flags[0] = true;
                if (instance.mConfig.mEnableAutoPush) {
                    instance.user_set(json);
                }
            }
        });
        if (!flags[0]) {
            PushEventItem item = new PushEventItem();
            item.type = TDConstants.DataType.USER_SET;
            item.properties = json;
            pushList.add(item);
        }
    }

    /**
     * clear cache event
     *
     * @param instance ThinkingAnalyticsSDK
     */
    public static void clearPushEvent(ThinkingAnalyticsSDK instance) {
        if (null == instance) return;
        if(instance.mConfig.mEnableAutoPush) {
            for (PushEventItem item : pushList) {
                if (item.type == TDConstants.DataType.TRACK) {
                    instance.track(TA_PUSH_CLICK_EVENT, item.properties);
                } else if (item.type == TDConstants.DataType.USER_SET) {
                    instance.user_set(item.properties);
                }
            }
        }
//        pushList.clear();
    }

    public static void handlePushTokenAfterLogin(final ThinkingAnalyticsSDK instance) {
        if (!instance.mConfig.mEnableAutoPush) {
            return;
        }
        try {
            Class<?> jPushClazz = Class.forName("cn.jpush.android.api.JPushInterface");
            Method getJPushToken = jPushClazz.getDeclaredMethod("getRegistrationID", Context.class);
            String jPushToken = ( String ) getJPushToken.invoke(null, instance.mConfig.mContext);
            if (!TextUtils.isEmpty(jPushToken)) {
                JSONObject jPushJson = new JSONObject();
                jPushJson.put("jiguang_id", jPushToken);
                instance.user_set(jPushJson);
            }
        } catch (Exception e) {
        }
        try {
            Class<?> firebaseClazz = Class.forName("com.google.firebase.messaging.FirebaseMessaging");
            Method getInstanceMethod = firebaseClazz.getDeclaredMethod("getInstance");
            Object firebaseInstance = getInstanceMethod.invoke(null);
            Method getTokenMethod = firebaseClazz.getDeclaredMethod("getToken");
            Object taskInstance = getTokenMethod.invoke(firebaseInstance);
            if (taskInstance != null) {
                Class<?> onCompleteListenerClazz = Class.forName("com.google.android.gms.tasks.OnCompleteListener");
                Method completeListenerMethod = taskInstance.getClass().getMethod("addOnCompleteListener", onCompleteListenerClazz);
                Object mDataHandlerObj = Proxy.newProxyInstance(firebaseClazz.getClassLoader(), new Class[]{onCompleteListenerClazz}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        try {
                            if ("onComplete".equals(method.getName())) {
                                Object task = args[0];
                                Class<?> taskClazz = Class.forName("com.google.android.gms.tasks.Task");
                                Method getResultMethod = taskClazz.getDeclaredMethod("getResult");
                                String fcmToken = ( String ) getResultMethod.invoke(task);
                                if (!TextUtils.isEmpty(fcmToken)) {
                                    JSONObject fcmJson = new JSONObject();
                                    fcmJson.put("fcm_token", fcmToken);
                                    instance.user_set(fcmJson);
                                }
                            }
                        }catch (Exception e){}
                        return 0;
                    }
                });
                completeListenerMethod.invoke(taskInstance, mDataHandlerObj);
            }
        } catch (Throwable e) {
        }
    }

    public static void handleGtPushEvent(String payload, String msgId) {
        if (gtMsgList.remove(msgId)) {
            handleExtraReceiverData(payload);
        }
    }


}
