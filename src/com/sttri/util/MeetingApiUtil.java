package com.sttri.util;

import net.sf.json.JSONObject;

import com.sttri.enums.MeetingApiInfoEnum;
import com.sttri.enums.MeetingApiUrlEnum;

/**
 * 
 * @author thj
 * 调用会易通API接口
 */
public class MeetingApiUtil {
	private final static String API = "api_key="+MeetingApiInfoEnum.ApiKey.getValue()
			+"&api_secret="+MeetingApiInfoEnum.ApiSecret.getValue();
	/**
	 * 调用会易通创建用户接口
	 * @param userName 用户名，可以为空
	 * @param email 用户邮箱，不能为空
	 * @param userType 用户类型，0：免费用户，1：普通用户 ，2：高级用户  默认为0
	 * @return
	 */
	public static JSONObject createUser(String userName,String email,int userType){
		String param = API+"&username="+userName+"&email="+email+"&usertype="+userType;
		String result = HttpUtil.sendPost(MeetingApiUrlEnum.createUserUrl.getValue(), param);
		return JSONObject.fromObject(result);
	}
	
	/**
	 * 获取用户信息
	 * @param email 
	 * @param logintype 登陆账户类型 1-ZCode，2-Mobile，3-Email 默认3
	 * @param loginname 根据所选 根据所选 logintype，相应的 ，相应的 ，相应的 ，相应的 Zcode、Mobile或 Email
	 * @return
	 */
	public static JSONObject getUser(String email){
		String param = API+"&logintype=3&loginname="+email;
		String result = HttpUtil.sendPost(MeetingApiUrlEnum.getUserUrl.getValue(), param);
		return JSONObject.fromObject(result);
	}
	/**
	 * 调用会易通删除用户接口
	 * @param zcode  会易通中用户的唯一标识
	 * @return
	 */
	public static JSONObject deleteUser(String zcode){
		String param = API+"&zcode="+zcode;
		String result = HttpUtil.sendPost(MeetingApiUrlEnum.deleteUserUrl.getValue(), param);
		return JSONObject.fromObject(result);
	}
	
	/**
	 * 
	 * @param zcode 会议发起者的唯一标识
	 * @param topic 会议主题,标题
	 * @param type 创建 会议类型， 1-即时会议， 2-计划会议， 3-长期会议
	 * @param password 会议密码
	 * @param startTime 会议开始时间
	 * @return
	 */
	public static JSONObject createMeetingRoom(String zcode,String topic,int type,String password,String startTime){
		String param = API+"&zcode="+zcode+"&password="+password+"&topic="+topic+"&type="+type+"&start_time="+startTime+"&option_jbh=true";
		String result = HttpUtil.sendPost(MeetingApiUrlEnum.createMeetingUrl.getValue(), param);
		if ("".equals(result)) {
			result="{\"code\":500}";
		}
		return JSONObject.fromObject(result);
	}
	
	/**
	 * 终止会议
	 * @param zcode 会议发起者的唯一标识
	 * @param meetingId 会议ID
	 * @return
	 */
	public static JSONObject endMeeting(String zcode,String meetingId){
		String param = API+"&zcode="+zcode+"&meeting_id="+meetingId;
		String result = HttpUtil.sendPost(MeetingApiUrlEnum.endMeetingUrl.getValue(), param);
		return JSONObject.fromObject(result);
	}
	
	/**
	 * 查询会议
	 * @param zcode 会议发起者的唯一标识
	 * @param meetingId 会议ID
	 * @return
	 */
	public static JSONObject getMeeting(String zcode,String meetingId){
		String param = API+"&zcode="+zcode+"&meeting_id="+meetingId;
		String result = HttpUtil.sendPost(MeetingApiUrlEnum.getMeetingUrl.getValue(), param);
		return JSONObject.fromObject(result);
	}
	
	/**
	 * 删除会议
	 * @param zcode 会议发起者的唯一标识
	 * @param meetingId 会议ID
	 * @return
	 */
	public static JSONObject deleteMeeting(String zcode,String meetingId){
		String param = API+"&zcode="+zcode+"&meeting_id="+meetingId;
		String result = HttpUtil.sendPost(MeetingApiUrlEnum.deleteMeetingUrl.getValue(), param);
		return JSONObject.fromObject(result);
	}
	
	/**
	 * 列示某个用户下的预约会议记录
	 * @param zcode
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public static JSONObject queryRegularMeetingList(String zcode,int pageNo,int pageSize){
		String param = API+"&zcode="+zcode+"&page_number="+pageNo+"&page_size="+pageSize;
		String result = HttpUtil.sendPost(MeetingApiUrlEnum.queryRegularMeetingList.getValue(), param);
		return JSONObject.fromObject(result);
	}
	
	/**
	 * 查询某个用户下所有的会议记录
	 * @param type 1-实时会议，无需输入 from, to 2-历史会议，需输入  from, to
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static JSONObject queryMeetingList(int type,String startTime,String endTime){
		String param = API+"&type="+type+"&from="+startTime+"&to="+endTime;
		String result = HttpUtil.sendPost(MeetingApiUrlEnum.queryMeetingListUrl.getValue(), param);
		return JSONObject.fromObject(result);
	}
	
	/**
	 * 获取某个用户下所有的会议中的一个会议的详细信息
	 * @param meeting_uid ：会议UUID
	 * @param type 1-实时会议，无需输入 from, to 2-历史会议，需输入  from, to
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static JSONObject queryMeetingDetail(String meeting_uid,int type){
		String param = API+"&meeting_id="+meeting_uid+"&type="+type;
		String result = HttpUtil.sendPost(MeetingApiUrlEnum.queryMeetingDetailUrl.getValue(), param);
		return JSONObject.fromObject(result);
	}
	
	/**
	 * 获取会议报表
	 * @param meeting_uid
	 * @param zcode
	 * @return
	 */
	public static JSONObject queryMeetingReport(String meeting_uid,String zcode){
		String param = API+"&meeting_uuid="+meeting_uid+"&zcode="+zcode;
		String result = HttpUtil.sendPost(MeetingApiUrlEnum.queryMeetingReportUrl.getValue(), param);
		return JSONObject.fromObject(result);
	}
	
	public static void main(String[] args) {
		System.out.println(getUser("jmlr1@jml.com"));
		JSONObject object = getUser("xtdx_pc@jml.com");
		System.out.println(object.toString());
		System.out.println(queryRegularMeetingList("7064270569", 10000000, 10));
		System.out.println(createMeetingRoom("4531530112", "测试3", 2, "123456", "2017-11-07 14:11:13"));//type=1,开始时间可以为空
		JSONObject ob = createMeetingRoom("4531530112", "测试3", 2, "123456", "2017-11-07 14:11:13");
		System.out.println(ob.toString());
//		System.out.println(endMeeting("4531530112", "1586920676"));
//		System.out.println(getMeeting("4531530112", "1321724782"));
		/*System.out.println("--------1-------");
		String param = API+"&zcode=4531530112&pageNo=1000&pageSize=10";
		System.out.println(HttpUtil.sendPost(MeetingApiUrlEnum.queryRegularMeetingList.getValue(), param));
		System.out.println("--------2-------");
		System.out.println(HttpUtil.sendPost(MeetingApiUrlEnum.queryMeetingListUrl.getValue(), API+"&type=1"));
		System.out.println("-------list--------");
		System.out.println(HttpUtil.sendPost(MeetingApiUrlEnum.queryMeetingListUrl.getValue(), API+"&type=2&from=2018-01-01&to=2018-01-30"));
		System.out.println("-------report--------");
		System.out.println(HttpUtil.sendPost(MeetingApiUrlEnum.queryMeetingReportUrl.getValue(), API+"&zcode=4531530112&meeting_uuid=LRHWIH5NQt+N18sS4TDISQ=="));
		System.out.println("---------------");
		System.out.println(HttpUtil.sendPost(MeetingApiUrlEnum.getMeetingUrl.getValue(), API+"&zcode=4531530112&meeting_id=1516012774"));
		System.out.println("-------detail--------");
		System.out.println(HttpUtil.sendPost("https://api.zhumu.me/v3/meeting/metricsdetail", API+"&type=2&meeting_id=XdZBVS48QtqBViKSYd0gjg%3D%3D"));
		System.out.println("---------------");*/
	}
}
