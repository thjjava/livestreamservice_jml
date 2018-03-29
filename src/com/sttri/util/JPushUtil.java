package com.sttri.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;

/**
 * 极光推送的工具类
 * @author thj
 *
 */
public class JPushUtil {
	 protected static final Logger LOG = LoggerFactory.getLogger(JPushUtil.class);

	    // demo App defined in resources/jpush-api.conf 
	    protected static final String APP_KEY ="12525cb5fbc7bd3af4ce1ddb";
	    protected static final String MASTER_SECRET = "49c3396f9a1a523ca55d570f";
	    protected static final String GROUP_PUSH_KEY = "5301e06dc006c19568cd3e0a";
	    protected static final String GROUP_MASTER_SECRET = "a87c8d318150fc1b99b52c13";
		
	    public static void main(String[] args) {
	    	Collection<String> list = new ArrayList<String>();
	    	list.add("170976fa8a8497b864c");
//	    	list.add("1a0018970aa24586025");
	    	System.out.println(sendPush("测试","xtdx_pc邀请您参加会议：https://meetingnow.zhumu.me/j/1744852926?pwd=tWzAHHXJzXQ%3D&uname=测试用户",list));
//			System.out.println(sendAndroidMessageWithRegistrationID("测试","https://meetingnow.zhumu.me/j/1544210162?pwd=tWzAHHXJzXQ%3D", "1a0018970aa24586025"));
		}
	    
	    /**
	     * 向所有注册设备发送推送消息
	     * @param title
	     * @param content
	     * @return
	     */
	    public static boolean sendPush(String title,String content){
	    	ClientConfig clientConfig = ClientConfig.getInstance();
	        final JPushClient jpushClient = new JPushClient(MASTER_SECRET, APP_KEY, null, clientConfig);
	        final PushPayload payload = buildPushObject_android_and_ios(title, content);
	        try {
	            PushResult result = jpushClient.sendPush(payload);
	            LOG.info("Got result - " + result);
	            if (result.getResponseCode() == 200) {
					return true;
				}
	        } catch (APIConnectionException e) {
	            LOG.error("Connection error. Should retry later. ", e);
	            LOG.error("Sendno: " + payload.getSendno());
	        } catch (APIRequestException e) {
	            LOG.error("Error response from JPush server. Should review and fix it. ", e);
	            LOG.info("HTTP Status: " + e.getStatus());
	            LOG.info("Error Code: " + e.getErrorCode());
	            LOG.info("Error Message: " + e.getErrorMessage());
	            LOG.info("Msg ID: " + e.getMsgId());
	            LOG.error("Sendno: " + payload.getSendno());
	        }
	        return false;
	    }
	    
	    /**
	     * 向特定某些registrationId的Android设备推送消息
	     * @param title
	     * @param content
	     * @param registrationId
	     * @return
	     */
	    public static boolean sendPush(String title,String content,String... registrationId){
	    	ClientConfig clientConfig = ClientConfig.getInstance();
	        final JPushClient jpushClient = new JPushClient(MASTER_SECRET, APP_KEY, null, clientConfig);
	        final PushPayload payload = buildPushObject_android_registrationId(title, content, registrationId);
	        try {
	            PushResult result = jpushClient.sendPush(payload);
	            LOG.info("Got result - " + result);
	            if (result.getResponseCode() == 200) {
					return true;
				}
	        } catch (APIConnectionException e) {
	            LOG.error("Connection error. Should retry later. ", e);
	            LOG.error("Sendno: " + payload.getSendno());
	        } catch (APIRequestException e) {
	            LOG.error("Error response from JPush server. Should review and fix it. ", e);
	            LOG.info("HTTP Status: " + e.getStatus());
	            LOG.info("Error Code: " + e.getErrorCode());
	            LOG.info("Error Message: " + e.getErrorMessage());
	            LOG.info("Msg ID: " + e.getMsgId());
	            LOG.error("Sendno: " + payload.getSendno());
	        }
	        return false;
	    }
	    
	    /**
	     * 向registrationIds集合中的所有的Android设备推送消息
	     * @param title
	     * @param content
	     * @param registrationIds
	     * @return
	     */
	    public static boolean sendPush(String title,String content,Collection<String> registrationIds){
	    	ClientConfig clientConfig = ClientConfig.getInstance();
	        final JPushClient jpushClient = new JPushClient(MASTER_SECRET, APP_KEY, null, clientConfig);
	        final PushPayload payload = buildPushObject_android_registrationId(title, content, registrationIds);
	        try {
	            PushResult result = jpushClient.sendPush(payload);
	            LOG.info("Got result - " + result);
	            if (result.getResponseCode() == 200) {
					return true;
				}
	        } catch (APIConnectionException e) {
	            LOG.error("Connection error. Should retry later. ", e);
	            LOG.error("Sendno: " + payload.getSendno());
	        } catch (APIRequestException e) {
	            LOG.error("Error response from JPush server. Should review and fix it. ", e);
	            LOG.info("HTTP Status: " + e.getStatus());
	            LOG.info("Error Code: " + e.getErrorCode());
	            LOG.info("Error Message: " + e.getErrorMessage());
	            LOG.info("Msg ID: " + e.getMsgId());
	            LOG.error("Sendno: " + payload.getSendno());
	        }
	        return false;
	    }
	    
	    public static PushPayload buildPushObject_android_and_ios(String title,String content) {
	        Map<String, String> extras = new HashMap<String, String>();
	        extras.put("test", "https://community.jiguang.cn/push");
	        return PushPayload.newBuilder()
	                .setPlatform(Platform.android_ios())
	                .setAudience(Audience.all())
	                .setNotification(Notification.newBuilder()
	                		.setAlert(content)
	                		.addPlatformNotification(AndroidNotification.newBuilder()
	                				.setTitle(title)
	                                .addExtras(extras).build())
	                		.addPlatformNotification(IosNotification.newBuilder()
	                				.incrBadge(1)
	                				.addExtra("extra_key", "extra_value").build())
	                		.build())
	                .build();
	    }
	    
	    public static PushPayload buildPushObject_android_registrationId(String title,String content,String... registrationId) {
	        return PushPayload.newBuilder()
	                .setPlatform(Platform.android())
	                .setAudience(Audience.registrationId(registrationId))
	                .setNotification(Notification.android(content, title, null))
	                .build();
	    }
	   
	    public static PushPayload buildPushObject_android_registrationId(String title,String content,Collection<String> registrationIds) {
	        return PushPayload.newBuilder()
	                .setPlatform(Platform.android())
	                .setAudience(Audience.registrationId(registrationIds))
	                .setNotification(Notification.android(content, title, null))
	                .build();
	    }
	    
	    /**
	     * 给客户端发送消息
	     * @param title
	     * @param msgContent
	     * @param registrationID
	     * @return
	     * @throws APIConnectionException
	     * @throws APIRequestException
	     */
	    public static boolean sendAndroidMessageWithRegistrationID(String title, String msgContent, String... registrationID) {
	    	ClientConfig clientConfig = ClientConfig.getInstance();
	        final JPushClient jpushClient = new JPushClient(MASTER_SECRET, APP_KEY, null, clientConfig);
	    	final PushPayload payload = PushPayload.newBuilder()
	                .setPlatform(Platform.android())
	                .setAudience(Audience.registrationId(registrationID))
	                .setMessage(Message.newBuilder()
	                        .setTitle(title)
	                        .setMsgContent(msgContent)
	                        .build())
	                .build();
	    	try {
				PushResult result = jpushClient.sendPush(payload);
				 LOG.info("Got result - " + result);
		            if (result.getResponseCode() == 200) {
						return true;
					}
		        } catch (APIConnectionException e) {
		            LOG.error("Connection error. Should retry later. ", e);
		            LOG.error("Sendno: " + payload.getSendno());
		        } catch (APIRequestException e) {
		            LOG.error("Error response from JPush server. Should review and fix it. ", e);
		            LOG.info("HTTP Status: " + e.getStatus());
		            LOG.info("Error Code: " + e.getErrorCode());
		            LOG.info("Error Message: " + e.getErrorMessage());
		            LOG.info("Msg ID: " + e.getMsgId());
		            LOG.error("Sendno: " + payload.getSendno());
		        }
		        return false;
	    }
}
