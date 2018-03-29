package com.sttri.action;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;

import com.sttri.pojo.CompanyGroup;
import com.sttri.pojo.DevRecord;
import com.sttri.pojo.DevRecordFile;
import com.sttri.pojo.MediaServer;
import com.sttri.pojo.TblControl;
import com.sttri.pojo.TblDev;
import com.sttri.pojo.TblUser;
import com.sttri.pojo.UserGroup;
import com.sttri.service.ICompanyGroupService;
import com.sttri.service.IControlService;
import com.sttri.service.IDevRecordFileService;
import com.sttri.service.IDevRecordService;
import com.sttri.service.IDevService;
import com.sttri.service.IMediaServerService;
import com.sttri.service.IUserGroupService;
import com.sttri.service.IUserService;
import com.sttri.util.Constant;
import com.sttri.util.JsonUtil;
import com.sttri.util.Util;
import com.sttri.util.WorkUtil;

public class MobileServerAction extends BaseAction {
	private static final long serialVersionUID = 1L;
	
	@Autowired
	private IDevService devService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IDevRecordService devRecordService;
	@Autowired
	private IDevRecordFileService devRecordFileService;
	@Autowired
	private IControlService controlService;
	@Autowired
	private IMediaServerService mediaServerService;
	@Autowired
	private IUserGroupService userGroupService;
	@Autowired
	private ICompanyGroupService groupService;
	
	
	/**
	 * pc客户端
	 * @return
	 */
	public String login(){
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String sourceType = Util.dealNull(request.getParameter("sourceType"));
		String conType = Util.dealNull(request.getParameter("conType"));
		String conVer = Util.dealNull(request.getParameter("conVer"));
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		obj.remove("user");
		if(obj.optInt("code", -1)!=0){
			obj.put("upgradeStatus", 0);
			obj.put("downUrl", "");
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		if(Util.notEmpty(sourceType)&&Util.notEmpty(conType)&&Util.notEmpty(conVer)){
			TblControl control = controlService.checkVer(Integer.valueOf(sourceType), Integer.valueOf(conType), conVer);
			if(control!=null){
				obj.put("upgradeStatus", control.getUpgradeStatus());
				obj.put("downUrl", Constant.readKey("appDownUrl")+control.getConPath());
			}else{
				obj.put("upgradeStatus", 0);
				obj.put("downUrl", "");
			}
		}
		JsonUtil.jsonBeanToString(response, obj);
		
		return null;
	}
	
	/**
	 * 手机客户端登录
	 * @return
	 */
	public void devLogin(){
		try {
//			JSONObject param = parseReq(request);
//			System.out.println("devLogin接收参数:"+param.toString());
			String devID = Util.dealNull(request.getParameter("DevID"));
			String devKey = Util.dealNull(request.getParameter("DevKey"));
			JSONObject obj = WorkUtil.checkDev(devService, devID, devKey);
			int upgradeStatus = 0;
			String downUrl = "", newVer = "";
			if(obj.optInt("code", -1)==0){
				TblDev dev = (TblDev) JSONObject.toBean(obj.optJSONObject("dev"), TblDev.class);
				//dev.setOnLines(0);
				dev.setLastLoginTime(Util.dateToStr(new Date()));
				devService.update(dev);
				
				String sourceType = Util.dealNull(request.getParameter("SourceType"));
				String conType = Util.dealNull(request.getParameter("ConType"));//控件类型(1-android/2-ios/3-pc/4-专业/5-桌面采集终端)
				String conVer = Util.dealNull(request.getParameter("ConVer"));
				if(Util.notEmpty(sourceType)&&Util.notEmpty(conType)&&Util.notEmpty(conVer)){
					TblControl control = controlService.checkVer(Integer.valueOf(sourceType), Integer.valueOf(conType), conVer);
					if(control!=null){
						upgradeStatus = control.getUpgradeStatus();
						downUrl = Constant.readKey("appDownUrl")+control.getConPath();
						newVer = control.getConVer();
					}else{
						control = controlService.checkVer(Integer.valueOf(sourceType), Integer.valueOf(conType), "");
						if(control!=null){
							newVer = control.getConVer();
						}
					}
				}
			}
			obj.remove("dev");
			System.out.println("设备登录结果:"+obj.toString());
			
			JSONObject object = new JSONObject();
			object.put("Result", obj.optInt("code", -1)+"");
			object.put("UpgradeStatus", upgradeStatus+"");
			object.put("NewVer", newVer);
			if(Util.notEmpty(downUrl)){
				object.put("DownUrl",downUrl);
			}
			System.out.println("devLogin返回结果:"+object.toString());
			JsonUtil.jsonBeanToString(response, object);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 手机登出
	 * @return
	 */
	public String devLogout(){
		try {
//			JSONObject param = parseReq(request);
			String devID = Util.dealNull(request.getParameter("DevID"));
			String devKey = Util.dealNull(request.getParameter("DevKey"));
			JSONObject obj = WorkUtil.checkDev(devService, devID, devKey);
			/*
			if(obj.optInt("code", -1)==0){
				TblDev dev = (TblDev) JSONObject.toBean(obj.optJSONObject("dev"), TblDev.class);
				dev.setOnLines(1);
				devService.update(dev);
			}
			*/
			obj.remove("dev");
			System.out.println("设备登出结果:"+obj.toString());
			JSONObject object = new JSONObject();
			object.put("Result", obj.optInt("code", -1)+"");
			System.out.println("devLogout返回结果:"+object.toString());
			JsonUtil.jsonBeanToString(response, object);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * pc获取设备列表
	 * @return
	 */
	public String getDevList(){
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		if(obj.optInt("code")==0){
			TblUser user = (TblUser)JSONObject.toBean(obj.optJSONObject("user"), TblUser.class);
			List<TblDev> list = devService.getResultList("o.company.id=?", null, new Object[]{user.getCompany().getId()});
			if(Util.isNull(list)||list.size()==0){
				JsonUtil.jsonString(response, "[]");
				return null;
			}
			JSONArray array = new JSONArray();
			JSONObject ob = null;
//			String d2 = Util.dateToStr(new Date());
			for(TblDev dev:list){
				ob = new JSONObject();
				/*if(WorkUtil.isTimeout(dev.getLastLoginTime(), d2, 3600)){
					dev.setOnLines(1);
					devService.update(dev);
				}*/
				ob.put("id", dev.getId());
				ob.put("devNo", dev.getDevNo());
				ob.put("name", dev.getDevName());
				ob.put("onLine", dev.getOnLines());
				array.add(ob);
			}
			JsonUtil.jsonString(response, array.toString());
		}else{
			JsonUtil.jsonString(response, "[]");
		}
		return null;
	}
	
	/**
	 * 实时刷新状态
	 * @return
	 */
	public String refreshOnline(){
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		JSONArray array = new JSONArray();
		if(obj.optInt("code")!=0){
			JsonUtil.jsonString(response, array.toString());
		}else{
			String data = Util.dealNull(request.getParameter("data"));
			System.out.println(data);
			array = JSONArray.fromObject(data);
			JSONObject ob = null;
			TblDev dev = null;
//			String d2 = Util.dateToStr(new Date());
			for(int i=0;i<array.size();i++){
				ob = array.getJSONObject(i);
				dev = devService.getById(ob.get("id"));
				/*if(WorkUtil.isTimeout(dev.getLastLoginTime(), d2, 3600)){
					dev.setOnLines(1);
					devService.update(dev);
				}*/
				int online = dev == null?1:dev.getOnLines();
				ob.put("onLine", online);
				/*if(dev==null)
					ob.put("onLine", 1);
				else
					ob.put("onLine", dev.getOnLines());*/
				array.set(i, ob);
			}
			JsonUtil.jsonString(response, array.toString());
		}
		return null;
	}
	
	/**
	 * pc获取分组
	 * @return
	 */
	public String group(){
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		if(obj.optInt("code",-1)!=0){
			JsonUtil.jsonString(response, "[]");
			return null;
		}
		
		TblUser user = (TblUser)JSONObject.toBean(obj.optJSONObject("user"), TblUser.class);
		String groupId = Util.dealNull(request.getParameter("groupId"));
		List<Object> param = new ArrayList<Object>();
		String where = "o.company.id=? ";
		param.add(user.getCompany().getId());
		if(groupId.equals("")){
			groupId = "0";
		}
		if(groupId.equals("0")){
			where += "and (o.groupId is null or o.groupId='')";
		}else{
			where += "and o.groupId=?";
			param.add(groupId);
		}
		
		JSONArray array = new JSONArray();
		JSONObject ob = null;
//		String d2 = Util.dateToStr(new Date());
		List<TblDev> devlist = devService.getResultList(where, null, param.toArray());
		for(TblDev dev:devlist){
			ob = new JSONObject();
			/*if(WorkUtil.isTimeout(dev.getLastLoginTime(), d2, 3600)){
				dev.setOnLines(1);
				devService.update(dev);
			}*/
			ob.put("id", dev.getId());
			ob.put("name", dev.getDevName());
			ob.put("devNo", dev.getDevNo());
			ob.put("type", 0);
			ob.put("onLine", dev.getOnLines());
			array.add(ob);
		}
		
		if(groupId.equals("0")){
			List<UserGroup> uglist = userGroupService.getResultList("o.user.account=?", null, account);
			for(UserGroup ug:uglist){
				if(ug.getGroup()==null)
					continue;
				ob = new JSONObject();
				ob.put("id", ug.getGroup().getId());
				ob.put("name", ug.getGroup().getGroupName());
				ob.put("type", 1);
				array.add(ob);
			}
		}else{
			List<CompanyGroup> gplist = groupService.getResultList("o.pid=?", null, groupId);
			for(CompanyGroup g:gplist){
				ob = new JSONObject();
				ob.put("id", g.getId());
				ob.put("name", g.getGroupName());
				ob.put("type", 1);
				array.add(ob);
			}
		}
		System.out.println(array.toString());
		JsonUtil.jsonString(response, array.toString());
		return null;
	}
	
	/**
	 * pc获取实时播放地址
	 * @return
	 */
	public String getUrl(){
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		obj.put("url", "");
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		obj.remove("user");
		String id = Util.dealNull(request.getParameter("id"));
		TblDev dev = devService.getById(id);
		if(dev==null){
			obj.put("code", 1);
			obj.put("desc", "没有找到当前设备");
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		obj.put("url", Util.dealNull(dev.getPublishUrl()));
		JsonUtil.jsonString(response, obj.toString());
		return null;
	}
	
	/**
	 * 南京-获取实时播放地址
	 * @return
	 */
	public String getUrl1(){
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		obj.put("url", "");
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		obj.remove("user");
		String id = Util.dealNull(request.getParameter("devNo"));
		List<TblDev> devList = devService.getResultList("o.devNo=?", null, id);
		if(devList == null || devList.size() == 0){
			obj.put("code", 1);
			obj.put("desc", "没有找到当前设备");
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		TblDev dev = devList.get(0);
		obj.put("url", Util.dealNull(dev.getPublishUrl()));
		obj.put("onLine", dev.getOnLines());
		JsonUtil.jsonString(response, obj.toString());
		return null;
	}
	
	/**
	 * pc获取录像列表
	 * @return
	 */
	public String getRecordList(){
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		obj.remove("user");
		String devId = Util.dealNull(request.getParameter("devId"));
		String startTime = Util.dealNull(request.getParameter("startTime"));
		String endTime = Util.dealNull(request.getParameter("endTime"));
		List<Object> param = new ArrayList<Object>();
		LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
		orderby.put("recordStartTime", "asc");
		String where = " 1=1 ";
		if(Util.isEmpty(devId)){
			obj.put("code", -1);
			obj.put("desc", "请选择设备!");
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		where += "and o.dev.id=? ";
		param.add(devId);
		if(Util.notEmpty(startTime)){
			where += "and o.recordStartTime>=? ";
			param.add(startTime);
		}
		if(Util.notEmpty(endTime)){
			where += "and o.recordStartTime<=? ";
			param.add(endTime);
		}
		List<DevRecord> drlist = devRecordService.getResultList(where, orderby, param.toArray());
		JSONArray array = new JSONArray();
		if(drlist==null||drlist.size()==0){
			JsonUtil.jsonString(response, array.toString());
			return null;
		}
		
		JSONObject ob = null;
		List<DevRecordFile> drflist = null;
		MediaServer mediaServer = null;
		for(DevRecord dr:drlist){
			drflist = devRecordFileService.getResultList("o.drId=?", orderby, new Object[]{dr.getId()});
			if(drflist==null || drflist.size()==0)
				continue;
			mediaServer = mediaServerService.getById(dr.getMediaServerId());
			if(mediaServer == null)
				continue;
			for(DevRecordFile drf:drflist){
				if(drf.getRecordSize()<10000)
					continue;
				ob = new JSONObject();
				ob.put("id", drf.getId());
				ob.put("name", drf.getRecordName());
				ob.put("startTime", drf.getRecordStartTime());
				ob.put("endTime", drf.getRecordEndTime());
				ob.put("recordSize", drf.getRecordSize());
				ob.put("playUrl", mediaServer.getRecordPlayUrl()+drf.getRecordName());
				ob.put("downUrl", mediaServer.getRecordDownUrl()+drf.getRecordName());
				array.add(ob);
			}
		}
		
		JsonUtil.jsonString(response, array.toString());
		return null;
	}
	
	/**
	 * 南京
	 * @return
	 */
	public String getRecordList1(){
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		obj.remove("user");
		String devNo = Util.dealNull(request.getParameter("devNo"));
		String startTime = Util.dealNull(request.getParameter("startTime"));
		String endTime = Util.dealNull(request.getParameter("endTime"));
		List<Object> param = new ArrayList<Object>();
		LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
		orderby.put("recordStartTime", "asc");
		String where = " 1=1 ";
		if(Util.isEmpty(devNo)){
			obj.put("code", -1);
			obj.put("desc", "请选择设备!");
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		List<TblDev> devList = devService.getResultList("o.devNo=?", null, devNo);
		if(devList==null || devList.size()==0){
			obj.put("code", -1);
			obj.put("desc", "没有找到当前设备!");
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		where += "and o.dev.devNo=? ";
		param.add(devNo);
		if(Util.notEmpty(startTime)){
			where += "and o.recordStartTime>=? ";
			param.add(startTime);
		}
		if(Util.notEmpty(endTime)){
			where += "and o.recordStartTime<=? ";
			param.add(endTime);
		}
		List<DevRecord> drlist = devRecordService.getResultList(where, orderby, param.toArray());
		JSONArray array = new JSONArray();
		if(drlist==null||drlist.size()==0){
			JsonUtil.jsonString(response, array.toString());
			return null;
		}
		
		JSONObject ob = null;
		List<DevRecordFile> drflist = null;
		MediaServer mediaServer = null;
		for(DevRecord dr:drlist){
			drflist = devRecordFileService.getResultList("o.drId=?", orderby, new Object[]{dr.getId()});
			if(drflist==null || drflist.size()==0)
				continue;
			mediaServer = mediaServerService.getById(dr.getMediaServerId());
			for(DevRecordFile drf:drflist){
				if(drf.getRecordSize()<10000)
					continue;
				ob = new JSONObject();
				ob.put("id", drf.getId());
				ob.put("name", drf.getRecordName());
				ob.put("startTime", drf.getRecordStartTime());
				ob.put("endTime", drf.getRecordEndTime());
				ob.put("recordSize", drf.getRecordSize());
				ob.put("playUrl", mediaServer.getRecordPlayUrl()+drf.getRecordName());
				ob.put("downUrl", mediaServer.getRecordDownUrl()+drf.getRecordName());
				array.add(ob);
			}
		}
		
		JsonUtil.jsonString(response, array.toString());
		return null;
	}
	
	/**
	 * 根据录像任务编号recordTaskNo获取录像列表
	 * @return
	 */
	public String getReordListByTaskNo(){
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		obj.remove("user");
		String devNo = Util.dealNull(request.getParameter("devNo"));
		String recordTaskNo = Util.dealNull(request.getParameter("recordTaskNo"));
		List<Object> param = new ArrayList<Object>();
		LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
		orderby.put("recordStartTime", "asc");
		String where = " 1=1 ";
		if(Util.isEmpty(devNo)){
			obj.put("code", -1);
			obj.put("desc", "请选择设备!");
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		where += "and o.dev.devNo=? ";
		param.add(devNo);
		if(Util.notEmpty(recordTaskNo)){
			where += "and o.id = ? ";
			param.add(recordTaskNo);
		}
		List<DevRecord> drlist = devRecordService.getResultList(where, orderby, param.toArray());
		JSONArray array = new JSONArray();
		if(drlist==null||drlist.size()==0){
			JsonUtil.jsonString(response, array.toString());
			return null;
		}
		
		JSONObject ob = null;
		List<DevRecordFile> drflist = null;
		MediaServer mediaServer = null;
		for(DevRecord dr:drlist){
			drflist = devRecordFileService.getResultList("o.drId=?", orderby, new Object[]{dr.getId()});
			if(drflist==null || drflist.size()==0)
				continue;
			mediaServer = mediaServerService.getById(dr.getMediaServerId());
			if(mediaServer == null)
				continue;
			for(DevRecordFile drf:drflist){
				if(drf.getRecordSize()<10000)
					continue;
				ob = new JSONObject();
				ob.put("id", drf.getId());
				ob.put("name", drf.getRecordName());
				ob.put("startTime", drf.getRecordStartTime());
				ob.put("endTime", drf.getRecordEndTime());
				ob.put("recordSize", drf.getRecordSize());
				ob.put("playUrl", mediaServer.getRecordPlayUrl()+drf.getRecordName());
				ob.put("downUrl", mediaServer.getRecordDownUrl()+drf.getRecordName());
				array.add(ob);
			}
		}
		
		JsonUtil.jsonString(response, array.toString());
		return null;
	}
	
	/**
	 * pc删除录像
	 * @return
	 */
	public String removeRecord(){
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		obj.remove("user");
		obj.put("code", -1);
		String ids = Util.dealNull(request.getParameter("ids"));
		if(ids.isEmpty()){
			obj.put("desc", "请选择要删除的录像!");
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		devRecordFileService.deletebyids(ids.split(","));
		obj.put("code", 0);
		obj.put("desc", "删除成功!");
		JsonUtil.jsonString(response, obj.toString());
		return null;
	}
	
	/**
	 * 手机开启直播
	 * @return
	 */
	public void recordStart(){
		response.setCharacterEncoding("UTF-8");
		try {
			System.out.println("method:"+request.getMethod());
			String devID = Util.dealNull(request.getParameter("DevID"));
			String devKey = Util.dealNull(request.getParameter("DevKey"));
			JSONObject obj = WorkUtil.checkDev(devService, devID, devKey);
			//System.out.println("recordStart验证结果:"+obj.toString());
			int result = -1;
			String url = "";
			String recordTaskNo = "";
			String protocol = Util.dealNull(request.getParameter("Protocol"));//请求的协议类型
			System.out.println("***请求协议类型:***"+protocol);
			if(obj.optInt("code", -1)==0){
				TblDev dev = (TblDev) JSONObject.toBean(obj.optJSONObject("dev"), TblDev.class);
				dev.setImsi(obj.optString("Imsi", ""));
				JSONObject ob = devService.videoStart(dev,protocol);
				if(ob.optBoolean("flag", false)){
					result = 0;
					url = ob.optString("url", "");
				}
				recordTaskNo = ob.optString("recordTaskNo","");
			}
			
			JSONObject object = new JSONObject();
			object.put("Result", result+"");
			System.out.println("***rtmp-url:***"+url);
			object.put("PublishUrl", url);
			object.put("RecordTaskNo", recordTaskNo);
			System.out.println("recordStart返回结果:"+object.toString());
			JsonUtil.jsonBeanToString(response, object);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 手机结束直播
	 * @return
	 */
	public void recordEnd(){
		response.setCharacterEncoding("UTF-8");
		try {
			String devID = Util.dealNull(request.getParameter("DevID"));
			String devKey = Util.dealNull(request.getParameter("DevKey"));
			JSONObject obj = WorkUtil.checkDev(devService, devID, devKey);
			System.out.println("recordEnd验证结果:"+obj.toString());
			String recordTaskNo = Util.dealNull(request.getParameter("RecordTaskNo"));
			int result = -1;
			if(obj.optInt("code", -1)==0){
				TblDev dev = (TblDev) JSONObject.toBean(obj.optJSONObject("dev"), TblDev.class);
				boolean flag = devService.videoEnd(dev,recordTaskNo);
				if(flag){
					result = 0;
				}
			}
			
			JSONObject object = new JSONObject();
			object.put("Result", result+"");
			System.out.println("recordEnd返回结果:"+object.toString());
			JsonUtil.jsonBeanToString(response, object);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public JSONObject parseReq(HttpServletRequest request){
		JSONObject param = null;
		try {
			int iContentLen = request.getContentLength();
			byte sContent[] = new byte[iContentLen];
			BufferedInputStream buf = new BufferedInputStream(request.getInputStream());
			buf.read(sContent, 0, iContentLen);
			String xmlfile = new String(sContent,0,iContentLen,"UTF-8");
			param = JsonUtil.stringToJson(xmlfile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return param;
	}
	
	/**
	 * 获取HLS播放地址
	 * @return 0:成功 1：没有找到设备 2:该设备所属企业不支持HLS直播
	 */
	public String getHlsUrl(){
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String devNo = Util.dealNull(request.getParameter("devNo"));
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		obj.put("hlsurl", "");
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		obj.remove("user");
		List<TblDev> dev = this.devService.getResultList(" o.devNo =? ", null, devNo);
		if(dev==null || dev.size() ==0){
			obj.put("code", 1);
			obj.put("desc", "没有找到当前设备");
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		if(dev.get(0).getCompany().getHlsLiveFlag()==0){//判断该设备所属的企业是否支持HLS直播
			obj.put("code", 2);
			obj.put("desc", "该设备不支持HLS直播!");
			obj.put("onLine", Util.dealNull(dev.get(0).getOnLines()));//是否在线(0-在线/1-下线)
			obj.put("hlsurl", "");
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		obj.put("code", 0);
		obj.put("desc", "获取HLS播放地址成功!");
		obj.put("onLine", Util.dealNull(dev.get(0).getOnLines()));//是否在线(0-在线/1-下线)
		obj.put("hlsurl", Util.dealNull(dev.get(0).getHlsUrl()));
		JsonUtil.jsonString(response, obj.toString());
		return null;
	}

}


