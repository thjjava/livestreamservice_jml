package com.sttri.action;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.apache.cxf.common.util.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ResponseBody;










import com.sttri.bean.PageView;
import com.sttri.bean.QueryResult;
import com.sttri.pojo.CompanyGroup;
import com.sttri.pojo.DevGood;
import com.sttri.pojo.DevLog;
import com.sttri.pojo.DevRecord;
import com.sttri.pojo.DevRecordFile;
import com.sttri.pojo.DevRecordTime;
import com.sttri.pojo.DevView;
import com.sttri.pojo.MediaServer;
import com.sttri.pojo.MeetingRecord;
import com.sttri.pojo.MeetingResource;
import com.sttri.pojo.RoleMenus;
import com.sttri.pojo.TblControl;
import com.sttri.pojo.TblDev;
import com.sttri.pojo.TblMobile;
import com.sttri.pojo.TblProblem;
import com.sttri.pojo.TblQuestion;
import com.sttri.pojo.TblUser;
import com.sttri.pojo.UserGroup;
import com.sttri.pojo.UserPolling;
import com.sttri.pojo.UserQuestion;
import com.sttri.pojo.UserRole;
import com.sttri.service.ICompanyGroupService;
import com.sttri.service.IControlService;
import com.sttri.service.IDevGoodService;
import com.sttri.service.IDevLogService;
import com.sttri.service.IDevRecordFileService;
import com.sttri.service.IDevRecordService;
import com.sttri.service.IDevRecordTimeService;
import com.sttri.service.IDevService;
import com.sttri.service.IDevViewService;
import com.sttri.service.IMediaServerService;
import com.sttri.service.IMeetingRecordService;
import com.sttri.service.IMeetingResourceService;
import com.sttri.service.IMobileService;
import com.sttri.service.IProblemService;
import com.sttri.service.IQuestionService;
import com.sttri.service.IRoleMenusService;
import com.sttri.service.ITblIPService;
import com.sttri.service.IUserGroupService;
import com.sttri.service.IUserPollingService;
import com.sttri.service.IUserQuestionService;
import com.sttri.service.IUserRoleService;
import com.sttri.service.IUserService;
import com.sttri.thread.IPAddressThread;
import com.sttri.thread.PushThread;
import com.sttri.util.Base64Util;
import com.sttri.util.Constant;
import com.sttri.util.JsonUtil;
import com.sttri.util.MeetingApiUtil;
import com.sttri.util.Util;
import com.sttri.util.WorkUtil;

public class ServerAction extends BaseAction {
	private static final Logger LOG = Logger.getLogger(ServerAction.class.getName());
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
	@Autowired
	private IUserPollingService userPollingService;
	@Autowired
	private IMobileService mobileService;
	@Autowired
	private IDevLogService devLogService;
	@Autowired
	private IProblemService problemService;
	@Autowired
	private ITblIPService ipService;
	@Autowired
	private IUserRoleService userRoleService;
	@Autowired
	private IRoleMenusService roleMenusService;
	@Autowired
	private IDevGoodService devGoodService;
	@Autowired
	private IDevViewService devViewService;
	@Autowired
	private IMeetingResourceService meetingResourceService;
	@Autowired
	private IMeetingRecordService meetingRecordService;
	@Autowired
	private IQuestionService questionService;
	@Autowired
	private IUserQuestionService userQuestionService;
	@Autowired
	private IDevRecordTimeService devRecordTimeService;
	
	/**
	 * pc客户端
	 * @return
	 */
	@ResponseBody
	public void login(){
		LOG.info("Executing operation login");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String sourceType = Util.dealNull(request.getParameter("sourceType"));
		String conType = Util.dealNull(request.getParameter("conType"));
		String conVer = Util.dealNull(request.getParameter("conVer"));
		LOG.info(account+" login on "+ Util.dateToStr(new Date()));
		try {
			JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
			//获取该用户的权限
			TblUser user = (TblUser) JSONObject.toBean(obj.optJSONObject("user"), TblUser.class);
			obj.remove("user");
			if(obj.optInt("code", -1)!=0){
				obj.put("upgradeStatus", 0);
				obj.put("downUrl", "");
				JsonUtil.jsonString(response, obj.toString());
				return;
			}
			if(Util.notEmpty(sourceType)&&Util.notEmpty(conType)&&Util.notEmpty(conVer)){
				TblControl control = controlService.checkVer(Integer.valueOf(sourceType), Integer.valueOf(conType), conVer);
				if(control!=null){
					if (Util.compareVersion(conVer, control.getConVer()) < 0 ) {
						obj.put("upgradeStatus", control.getUpgradeStatus());
						obj.put("downUrl", Constant.readKey("appDownUrl")+control.getConPath());
					}
				}else{
					obj.put("upgradeStatus", 0);
					obj.put("downUrl", "");
				}
			}
			String userId = user.getId();
			List<UserRole> uRoles = this.userRoleService.getResultList(" o.user.id=?", null, new Object[]{userId});
			if (uRoles != null && uRoles.size()>0) {
				List<RoleMenus> roleMenus = this.roleMenusService.getResultList(" o.role.id=?", null, new Object[]{uRoles.get(0).getRole().getId()});
				for (RoleMenus rm : roleMenus) {
					if("下载".equals(rm.getMenus().getName())){
						obj.put("downPermission", "1");//有下载权限
					}else if ("删除".equals(rm.getMenus().getName())) {
						obj.put("delPermission", "1");//有删除权限
					}else if ("会议室".equals(rm.getMenus().getName())) {
						obj.put("meetingFlag", "1");//有会议室权限
					}
				}
			}
			System.out.println(obj.toString());
			JsonUtil.jsonBeanToString(response, obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 手机客户端登录
	 * @return
	 */
	public void devLogin(){
		LOG.info("Executing operation devLogin");
		try {
			JSONObject param = (JSONObject)new XMLSerializer().readFromStream(request.getInputStream());
			System.out.println("devLogin接收参数:"+param.toString());
			JSONObject obj = WorkUtil.checkDev(devService, param.optString("DevID", ""), param.optString("DevKey", ""));
			int upgradeStatus = 0;
			String downUrl = "", newVer = "", logDesc = "", devName = param.optString("DevID", "");
			TblDev dev = null;
			if(obj.optInt("code", -1)==0){
				dev = (TblDev) JSONObject.toBean(obj.optJSONObject("dev"), TblDev.class);
				devName = dev.getDevName();
				//dev.setOnLines(0);
//				dev.setLastLoginTime(Util.dateToStr(new Date()));
//				devService.update(dev);
				
				String sourceType = param.optString("SourceType","");
				String conType = param.optString("ConType", "");
				String conVer = param.optString("ConVer", "");
				if(Util.notEmpty(sourceType)&&Util.notEmpty(conType)&&Util.notEmpty(conVer)){
					TblControl control = controlService.checkVer(Integer.valueOf(sourceType), Integer.valueOf(conType), conVer);
					if(control!=null){
						newVer = control.getConVer();
						if (Util.compareVersion(conVer, control.getConVer()) < 0 ) {
							upgradeStatus = control.getUpgradeStatus();
							downUrl = Constant.readKey("appDownUrl")+control.getConPath();
						}
					}else{
						control = controlService.checkVer(Integer.valueOf(sourceType), Integer.valueOf(conType), "");
						if(control!=null){
							newVer = control.getConVer();
						}
					}
				}
				
				if(Util.isEmpty(downUrl)){
					logDesc = dev.getDevNo()+",登录成功!";
				}else {
					logDesc = dev.getDevNo()+",登录失败,版本过低,需要升级!";
				}
			}else {
				logDesc = param.optString("DevID", "")+",登录失败,"+obj.optString("desc", "");
			}
			obj.remove("dev");
			
			//保存登录日志
			String clientIP = getIpAddr(request);
			saveDevLog(dev, clientIP,2, logDesc);
			
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("LOGIN_RES");
			doc.setRootElement(root);
			root.addElement("Result").addText(obj.optInt("code", -1)+"");
			root.addElement("UpgradeStatus").addText(upgradeStatus+"");
			root.addElement("NewVer").addText(newVer);
			if(Util.notEmpty(downUrl))
				root.addElement("DownUrl").addText(downUrl);
			root.addElement("devName").addText(devName);//返回结果中新增账号名称
			System.out.println("devLogin返回结果:"+doc.asXML());
			JsonUtil.jsonString(response, doc.asXML());
			//JsonUtil.jsonBeanToString(response, obj);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 手机登出
	 * @return
	 */
	public void devLogout(){
		LOG.info("Executing operation devLogout");
		try {
			JSONObject param = (JSONObject)new XMLSerializer().readFromStream(request.getInputStream());
			JSONObject obj = WorkUtil.checkDev(devService, param.optString("DevID", ""), param.optString("DevKey", ""));
			if(obj.optInt("code", -1)==0){
				TblDev dev = (TblDev) JSONObject.toBean(obj.optJSONObject("dev"), TblDev.class);
				dev.setOnLines(1);
				devService.update(dev);
			}
			obj.remove("dev");
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("LOGOUT_RES");
			doc.setRootElement(root);
			root.addElement("Result").addText(obj.optInt("code", -1)+"");
			System.out.println("devLogout返回结果:"+doc.asXML());
			JsonUtil.jsonString(response, doc.asXML());
			//JsonUtil.jsonBeanToString(response, obj);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * pc获取设备列表
	 * @return
	 */
	public String getDevList(){
		LOG.info("Executing operation getDevList");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		LOG.info(account+" getDevList on "+ Util.dateToStr(new Date()));
		
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
	public void refreshOnline(){
		LOG.info("Executing operation refreshOnline");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String data = Util.dealNull(request.getParameter("data"));//设备在线状态
		String groups = Util.dealNull(request.getParameter("groups"));//组织在线数统计
		
		LOG.info(account+" refreshOnline on "+ Util.dateToStr(new Date()));
		try {
			JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
			TblUser user = (TblUser)JSONObject.toBean(obj.optJSONObject("user"), TblUser.class);
			obj.remove("user");
			obj.put("code", "0");
			obj.put("desc", "实时刷新");
			JSONArray array = new JSONArray();
			if(obj.optInt("code")!=0){
				JsonUtil.jsonString(response, obj.toString());
				return;
			}else{
				array = JSONArray.fromObject(data);
				JSONObject ob = null;
				TblDev dev = null;
				/**
				 * 更新时间：2017-9-20 
				 * 更新内容：判断组织ID参数是否为空，如果为空，就直接返回客户端设备的状态，如果不为空，则返回客户端设备的在线状态和组织下的设备统计
				 */
				if ("".equals(groups)) {
//					String d2 = Util.dateToStr(new Date());
					for(int i=0;i<array.size();i++){
						ob = array.getJSONObject(i);
						dev = devService.getById(ob.get("id"));
						/*if(WorkUtil.isTimeout(dev.getLastLoginTime(), d2, 3600)){
							dev.setOnLines(1);
							devService.update(dev);
						}*/
						int online = dev == null?1:dev.getOnLines();
						ob.put("onLine", online);
						array.set(i, ob);
					}
					JsonUtil.jsonString(response, array.toString());
				}else {
					//设备在线状态
//					String d2 = Util.dateToStr(new Date());
					for(int i=0;i<array.size();i++){
						ob = array.getJSONObject(i);
						dev = devService.getById(ob.get("id"));
						/*if(WorkUtil.isTimeout(dev.getLastLoginTime(), d2, 3600)){
							dev.setOnLines(1);
							devService.update(dev);
						}*/
						int online = dev == null?1:dev.getOnLines();
						ob.put("onLine", online);
						array.set(i, ob);
						obj.put("data", array.toString());
					}
					//组织在线数统计
					array = JSONArray.fromObject(groups);
					for(int i=0;i<array.size();i++){
						ob = array.getJSONObject(i);
						String groupId = ob.optString("groupid");
						JSONArray gArray = new JSONArray();
						gArray = getArray(groupId, gArray);
						String jpqlStr = gArray.toString().replace("[", "(").replace("]", ")").replaceAll("\"", "'");
						List<TblDev> total = this.devService.getResultList(" o.company.id=? and o.groupId in "+jpqlStr, null,new Object[]{ user.getCompany().getId()});
						List<TblDev> onLine = this.devService.getResultList(" o.company.id=? and o.onLines=? and o.groupId in "+jpqlStr, null,new Object[]{ user.getCompany().getId(),0});
						ob.put("total", total.size());
						ob.put("onLineCount", onLine.size());
						array.set(i, ob);
						obj.put("groups", array.toString());
					}
					JsonUtil.jsonString(response, obj.toString());
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据用户的组织id，查询该组织所有子节点的id
	 */
	public JSONArray getArray(String id,JSONArray array){
		array.add(id);
		//查询组织表中，该ID的根节点下的所有子节点
		List<CompanyGroup> gList = this.groupService.getResultList(" o.pid=?", null, id);
		if(gList != null && gList.size()>0){
			for (CompanyGroup companyGroup : gList) {
				String gid = companyGroup.getId();
				array.add(gid);
				getArray(gid,array);//递归查询gid该节点的子节点
			}
		}
		return array;
	}
	
	/**
	 * pc获取分组
	 * @return
	 */
	public String group(){
		LOG.info("Executing operation group");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		LOG.info(account+" group on "+ Util.dateToStr(new Date()));
		
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
		
		JSONArray gArray = null;
		if(groupId.equals("0")){
			//获取节点下所有的设备数和在线设备数
			gArray = new JSONArray();
			gArray = getArray(groupId, gArray);
			String jpqlStr = gArray.toString().replace("[", "(").replace("]", ")").replaceAll("\"", "'");
			List<TblDev> total = this.devService.getResultList(" o.company.id=? and o.groupId in "+jpqlStr, null,new Object[]{ user.getCompany().getId()});
			List<TblDev> onLine = this.devService.getResultList(" o.company.id=? and o.onLines=? and o.groupId in "+jpqlStr, null,new Object[]{ user.getCompany().getId(),0});
			
			List<UserGroup> uglist = userGroupService.getResultList("o.user.account=?", null, account);
			for(UserGroup ug:uglist){
				if(ug.getGroup()==null)
					continue;
				ob = new JSONObject();
				ob.put("id", ug.getGroup().getId());
				ob.put("name", ug.getGroup().getGroupName());
				ob.put("type", 1);
				ob.put("total", total.size());
				ob.put("onLineCount", onLine.size());
				array.add(ob);
			}
		}else{
			List<CompanyGroup> gplist = groupService.getResultList("o.pid=?", null, groupId);
			for(CompanyGroup g:gplist){
				ob = new JSONObject();
				ob.put("id", g.getId());
				ob.put("name", g.getGroupName());
				ob.put("type", 1);
				
				//获取节点下所有的设备数和在线设备数
				gArray = new JSONArray();
				gArray = getArray(g.getId(), gArray);
				String jpqlStr = gArray.toString().replace("[", "(").replace("]", ")").replaceAll("\"", "'");
				List<TblDev> total = this.devService.getResultList(" o.company.id=? and o.groupId in "+jpqlStr, null,new Object[]{ user.getCompany().getId()});
				List<TblDev> onLine = this.devService.getResultList(" o.company.id=? and o.onLines=? and o.groupId in "+jpqlStr, null,new Object[]{ user.getCompany().getId(),0});
				
				ob.put("total", total.size());
				ob.put("onLineCount", onLine.size());
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
		LOG.info("Executing operation getUrl");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		LOG.info(account+" getUrl on "+ Util.dateToStr(new Date()));
		
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
		obj.put("code", 0);
		obj.put("desc", "获取播放地址成功!");
		obj.put("url", Util.dealNull(dev.getPublishUrl()));
		obj.put("devName", dev.getDevName());
		obj.put("onLine", dev.getOnLines());
		JsonUtil.jsonString(response, obj.toString());
		return null;
	}
	
	/**
	 * 南京-获取实时播放地址
	 * @return
	 */
	public void getUrl1(){
		LOG.info("Executing operation getUrl1");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		LOG.info(account+" getUrl1 on "+ Util.dateToStr(new Date()));
		
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		obj.put("url", "");
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		obj.remove("user");
		String id = Util.dealNull(request.getParameter("devNo"));
		List<TblDev> devList = devService.getResultList("o.devNo=?", null, id);
		if(devList == null || devList.size() == 0){
			obj.put("code", 1);
			obj.put("desc", "没有找到当前设备");
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		TblDev dev = devList.get(0);
		obj.put("url", Util.dealNull(dev.getPublishUrl()));
		obj.put("onLine", dev.getOnLines());
		JsonUtil.jsonString(response, obj.toString());
	}
	
	/**
	 * pc获取录像列表
	 * @return
	 */
	public String getRecordList(){
		LOG.info("Executing operation getRecordList");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		LOG.info(account+" getRecordList on "+ Util.dateToStr(new Date()));
		
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
			endTime = endTime.substring(0,10)+" 23:59:59";
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
	public void getRecordList1(){
		LOG.info("Executing operation getRecordList1");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		LOG.info(account+" getRecordList1 on "+ Util.dateToStr(new Date()));
		
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return;
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
			return ;
		}
		List<TblDev> devList = devService.getResultList("o.devNo=?", null, devNo);
		if(devList==null || devList.size()==0){
			obj.put("code", -1);
			obj.put("desc", "没有找到当前设备!");
			JsonUtil.jsonString(response, obj.toString());
			return ;
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
			return ;
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
	}
	
	/**
	 * 根据录像任务编号recordTaskNo获取录像列表
	 * @return
	 */
	public void getReordListByTaskNo(){
		LOG.info("Executing operation getReordListByTaskNo");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		LOG.info(account+" getReordListByTaskNo on "+ Util.dateToStr(new Date()));
		
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return ;
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
			return ;
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
			return ;
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
	}
	
	/**
	 * pc删除录像
	 * @return
	 */
	public String removeRecord(){
		LOG.info("Executing operation removeRecord");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		LOG.info(account+" removeRecord on "+ Util.dateToStr(new Date()));
		
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		//获取该用户的权限
		TblUser user = (TblUser) JSONObject.toBean(obj.optJSONObject("user"), TblUser.class);
		obj.remove("user");
		obj.put("code", -1);
		String ids = Util.dealNull(request.getParameter("ids"));
		if(ids.isEmpty()){
			obj.put("desc", "请选择要删除的录像!");
			JsonUtil.jsonString(response, obj.toString());
			return null;
		}
		String userId = user.getId();
		List<UserRole> uRoles = this.userRoleService.getResultList(" o.user.id=?", null, new Object[]{userId});
		if (uRoles != null && uRoles.size()>0) {
			List<RoleMenus> roleMenus = this.roleMenusService.getResultList(" o.role.id=?", null, new Object[]{uRoles.get(0).getRole().getId()});
			for (RoleMenus menus : roleMenus) {
				if ("删除".equals(menus.getMenus().getName())) {
					devRecordFileService.deletebyids(ids.split(","));
					obj.put("code", 0);
					obj.put("desc", "删除成功!");
				}
			}
		}else {
			obj.put("code", 5);
			obj.put("desc", "该用户没有删除权限，删除失败!");
		}
		JsonUtil.jsonString(response, obj.toString());
		return null;
	}
	
	/**
	 * 手机开启直播
	 * @return
	 */
	public void recordStart(){
		LOG.info("Executing operation recordStart");
		response.setCharacterEncoding("UTF-8");
		try {
			JSONObject param = (JSONObject)new XMLSerializer().readFromStream(request.getInputStream());
			System.out.println("recordStart接收参数:"+param.toString());
			JSONObject obj = WorkUtil.checkDev(devService, param.optString("DevID", ""), param.optString("DevKey", ""));
			int result = -1;
			String url = "";
			String recordTaskNo = "";
			String protocol = param.optString("Protocol","");//请求的协议类型
			if(obj.optInt("code", -1)==0){
				TblDev dev = (TblDev) JSONObject.toBean(obj.optJSONObject("dev"), TblDev.class);
				dev.setImsi(obj.optString("Imsi", ""));
				JSONObject ob = this.devService.videoStart(dev,protocol);
				if(ob.optBoolean("flag", false)){
					result = 0;
					url = ob.optString("url", "");
				}
				recordTaskNo = ob.optString("recordTaskNo","");
				
				String clientIP = getIpAddr(request);
				saveDevLog(dev, clientIP,0, dev.getDevNo()+",开始直播!");//保存日志
			}
			
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("STARTUPLIVE_RES");
			doc.setRootElement(root);
			root.addElement("Result").addText(result+"");
			root.addElement("RecordTaskNo").addText(recordTaskNo);
			root.addElement("PublishUrl").addText(url);
			root.addElement("AudioRtpPort").addText("0");
			root.addElement("AudioRtcpPort").addText("0");
			root.addElement("VideoRtpPort").addText("0");
			root.addElement("VideoRtcpPort").addText("0");
			System.out.println("recordStart返回结果:"+doc.asXML());
			JsonUtil.jsonString(response, doc.asXML());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 手机结束直播
	 * @return
	 */
	public void recordEnd(){
		LOG.info("Executing operation recordEnd");
		response.setCharacterEncoding("UTF-8");
		try {
			JSONObject param = (JSONObject) new XMLSerializer().readFromStream(request.getInputStream());
			System.out.println("recordEnd接收参数:" + param.toString());
			JSONObject obj = WorkUtil.checkDev(devService, param.optString("DevID", ""),param.optString("DevKey", ""));
			String recordTaskNo = param.optString("RecordTaskNo", "");
			int result = -1;
			String logDesc = "";
			if (obj.optInt("code", -1) == 0) {
				TblDev dev = (TblDev) JSONObject.toBean(obj.optJSONObject("dev"), TblDev.class);
				if (dev.getOnLines() == 0) {
					boolean flag = this.devService.videoEnd(dev, recordTaskNo);
					if (flag) {
						result = 0;
						logDesc = dev.getDevNo() + ",停止直播!";
					}
				} else {
					result = 0;
					logDesc = dev.getDevNo() + ",设备已经离线,停止直播!";
				}

				//保存日志
				String clientIP = getIpAddr(this.request);
				saveDevLog(dev, clientIP, 1, logDesc);
			}

			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("STOPLIVE_RES");
			doc.setRootElement(root);
			root.addElement("Result").addText(result + "");
			System.out.println("recordEnd返回结果:" + doc.asXML());
			JsonUtil.jsonString(response, doc.asXML());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取HLS播放地址
	 * @return 0:成功 1：没有找到设备 2:该设备所属企业不支持HLS直播
	 */
	public String getHlsUrl(){
		LOG.info("Executing operation getHlsUrl");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String devNo = Util.dealNull(request.getParameter("devNo"));
		LOG.info(account+" getHlsUrl on "+ Util.dateToStr(new Date()));
		
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
	
	/**
	 * 今麦郎项目定制接口
	 * 获取该设备用户的所有领导的设备编号
	 * @return
	 */
	public void getLeaderDevList() {
		LOG.info("Executing operation getLeaderDevList");
		response.setCharacterEncoding("UTF-8");
		String devNo = Util.dealNull(request.getParameter("devNo"));
		String devKey = Util.dealNull(request.getParameter("pwd"));
		String sourceType = Util.dealNull(request.getParameter("sourceType"));
		String conType = Util.dealNull(request.getParameter("conType"));
		String conVer = Util.dealNull(request.getParameter("conVer"));
		
		LOG.info(devNo+" getLeaderDevList on "+ Util.dateToStr(new Date()));
		
		JSONArray array = new JSONArray();
		JSONObject object = new JSONObject();
		object.put("result", -1);
		object.put("msg", "");
		object.put("devList", null);
		if(Util.isEmpty(devNo)){
			object.put("result", 1);
			object.put("msg", "设备号为空!");
			JsonUtil.jsonString(response, object.toString());
			return;
		}
		if(Util.isEmpty(devKey)){
			object.put("result", 2);
			object.put("msg", "设备密码为空!");
			JsonUtil.jsonString(response, object.toString());
			return;
		}
		List<TblDev> list = devService.getResultList("o.devNo=?", null, new Object[]{devNo});
		if(Util.isNull(list)||list.size()==0){
			object.put("result", 3);
			object.put("msg", "没有找到当前设备!");
			JsonUtil.jsonString(response, object.toString());
			return;
		}
		TblDev dev = list.get(0);
		if(dev.getIsAble()==1){
			object.put("result", 5);
			object.put("msg", "设备未启用!");
			JsonUtil.jsonString(response, object.toString());
			return;
		}
		if(!dev.getDevKey().equals(Base64Util.encode(devKey))){
			object.put("result", 4);
			object.put("msg", "设备密码错误!");
			JsonUtil.jsonString(response, object.toString());
			return;
		}
		if(Util.notEmpty(sourceType)&&Util.notEmpty(conType)&&Util.notEmpty(conVer)){
			TblControl control = controlService.checkVer(Integer.valueOf(sourceType), Integer.valueOf(conType), conVer);
			if(control!=null){
				object.put("result", 5);
				object.put("msg", "有新版本需要升级!");
				object.put("upgradeStatus", control.getUpgradeStatus());
				object.put("downUrl", Constant.readKey("appDownUrl")+control.getConPath());
				JsonUtil.jsonString(response, object.toString());
				return;
			}
		}
		
		String comId = dev.getCompany().getId();
		String curGroupId = dev.getGroupId();
		String url = dev.getSubPublishUrl();
		if("".equals(url) || url == null){
			url = dev.getPublishUrl();
		}
		List<CompanyGroup> cgList = this.groupService.getResultList(" o.id=?", null, curGroupId);
		if(cgList != null && cgList.size()>0){
			CompanyGroup companyGroup = cgList.get(0);//设备所在组织
			String pId = companyGroup.getPid();//设备所在组织的父ID
			JSONObject ob = null;
			if("0".equals(pId)){
				ob = new JSONObject();
				ob.put("leaderDevNo", dev.getDevNo());
				ob.put("name", dev.getDevName());
				ob.put("mainFalg", "0");
				ob.put("onLine", dev.getOnLines());
				ob.put("url", url==null?"":url);//url==null JSONObject在调用put方法的时候会把该key参数删掉
				ob.put("fullFlag", dev.getFullFlag()==null?0:dev.getFullFlag());
				array.add(ob);
			}else {
				array = getArray(pId, comId,array);
				if (array == null || array.size() <= 0) {
					ob = new JSONObject();
					ob.put("leaderDevNo", dev.getDevNo());
					ob.put("name", dev.getDevName());
					ob.put("mainFalg", "0");
					ob.put("onLine", dev.getOnLines());
					ob.put("url", url==null?"":url);//url==null JSONObject在调用put方法的时候会把该key参数删掉
					ob.put("fullFlag", dev.getFullFlag()==null?0:dev.getFullFlag());
					array.add(ob);
				}
			}
			object.put("result", "0");
			object.put("msg", "获取设备列表成功");
			object.put("devList", array.toString());
			System.out.println(object.toString());
			JsonUtil.jsonString(response, object.toString());
		}else {
			object.put("result", "-1");
			object.put("msg", "获取设备列表失败");
			object.put("devList", array.toString());
			JsonUtil.jsonString(response, object.toString());
		}
		//保存日志
		String clientIP = getIpAddr(request);
		saveDevLog(dev, clientIP, 5, devNo+",登录远程客户端成功!");
	}
	
	//远程客户端登录后，每隔15秒刷新设备列表
	public void refreshLeaderDevList() {
		response.setCharacterEncoding("UTF-8");
		String devNo = Util.dealNull(request.getParameter("devNo"));
		String devKey = Util.dealNull(request.getParameter("pwd"));
		String sourceType = Util.dealNull(request.getParameter("sourceType"));
		String conType = Util.dealNull(request.getParameter("conType"));
		String conVer = Util.dealNull(request.getParameter("conVer"));
		LOG.info(devNo+" refreshLeaderDevList on "+ Util.dateToStr(new Date()));
		
		JSONArray array = new JSONArray();
		JSONObject object = new JSONObject();
		object.put("result", -1);
		object.put("msg", "");
		object.put("devList", null);
		if(Util.isEmpty(devNo)){
			object.put("result", 1);
			object.put("msg", "设备号为空!");
			JsonUtil.jsonString(response, object.toString());
			return;
		}
		if(Util.isEmpty(devKey)){
			object.put("result", 2);
			object.put("msg", "设备密码为空!");
			JsonUtil.jsonString(response, object.toString());
			return;
		}
		List<TblDev> list = devService.getResultList("o.devNo=?", null, new Object[]{devNo});
		if(Util.isNull(list)||list.size()==0){
			object.put("result", 3);
			object.put("msg", "没有找到当前设备!");
			JsonUtil.jsonString(response, object.toString());
			return;
		}
		TblDev dev = list.get(0);
		if(dev.getIsAble()==1){
			object.put("result", 5);
			object.put("msg", "设备未启用!");
			JsonUtil.jsonString(response, object.toString());
			return;
		}
		if(!dev.getDevKey().equals(Base64Util.encode(devKey))){
			object.put("result", 4);
			object.put("msg", "设备密码错误!");
			JsonUtil.jsonString(response, object.toString());
			return;
		}
		if(Util.notEmpty(sourceType)&&Util.notEmpty(conType)&&Util.notEmpty(conVer)){
			TblControl control = controlService.checkVer(Integer.valueOf(sourceType), Integer.valueOf(conType), conVer);
			if(control!=null){
				object.put("result", 5);
				object.put("msg", "有新版本需要升级!");
				object.put("upgradeStatus", control.getUpgradeStatus());
				object.put("downUrl", Constant.readKey("appDownUrl")+control.getConPath());
				JsonUtil.jsonString(response, object.toString());
				return;
			}
		}
		
		String comId = dev.getCompany().getId();
		String curGroupId = dev.getGroupId();
		String url = dev.getSubPublishUrl();
		if("".equals(url) || url == null){
			url = dev.getPublishUrl();
		}
		List<CompanyGroup> cgList = this.groupService.getResultList(" o.id=?", null, curGroupId);
		if(cgList != null && cgList.size()>0){
			CompanyGroup companyGroup = cgList.get(0);//设备所在组织
			String pId = companyGroup.getPid();//设备所在组织的父ID
			JSONObject ob = null;
			if("0".equals(pId)){
				ob = new JSONObject();
				ob.put("leaderDevNo", dev.getDevNo());
				ob.put("name", dev.getDevName());
				ob.put("mainFalg", "0");
				ob.put("onLine", dev.getOnLines());
				ob.put("url", url==null?"":url);//url==null JSONObject在调用put方法的时候会把该key参数删掉
				ob.put("fullFlag", dev.getFullFlag()==null?0:dev.getFullFlag());
				array.add(ob);
			}else {
				array = getArray(pId, comId,array);
				if (array == null || array.size() <= 0) {
					ob = new JSONObject();
					ob.put("leaderDevNo", dev.getDevNo());
					ob.put("name", dev.getDevName());
					ob.put("mainFalg", "0");
					ob.put("onLine", dev.getOnLines());
					ob.put("url", url==null?"":url);//url==null JSONObject在调用put方法的时候会把该key参数删掉
					ob.put("fullFlag", dev.getFullFlag()==null?0:dev.getFullFlag());
					array.add(ob);
				}
			}
			object.put("result", "0");
			object.put("msg", "获取设备列表成功");
			object.put("devList", array.toString());
			System.out.println(object.toString());
			JsonUtil.jsonString(response, object.toString());
		}else {
			object.put("result", "-1");
			object.put("msg", "获取设备列表失败");
			object.put("devList", array.toString());
			JsonUtil.jsonString(response, object.toString());
		}
	}
	
	public JSONArray getArray(String id,String comId,JSONArray array){
		JSONObject ob = null;
		List<CompanyGroup> gList = this.groupService.getResultList(" o.id=?", null, id);
		if(gList != null && gList.size()>0){
			CompanyGroup pGroup = gList.get(0);
			String pgId = pGroup.getPid();
			List<TblDev> list = this.devService.getResultList("o.company.id=? and o.groupId=?", null, new Object[]{comId,id});
			for(TblDev tblDev:list){
				String url = tblDev.getSubPublishUrl();
				if("".equals(url) || url == null){
					url = tblDev.getPublishUrl();
				}
				ob = new JSONObject();
				ob.put("leaderDevNo", tblDev.getDevNo());
				ob.put("name", tblDev.getDevName());
				ob.put("mainFalg", "0");
				ob.put("onLine", tblDev.getOnLines());
				ob.put("url", url==null?"":url);//url==null JSONObject在调用put方法的时候会把该key参数删掉
				ob.put("fullFlag", tblDev.getFullFlag()==null?0:tblDev.getFullFlag());
				array.add(ob);
			}
			if(!"0".equals(pgId)){//判断当前设备所在组织不是根组织
				getArray(pgId, comId,array);
			}
		}
		return array;
	}
	
	/**
	 * 今麦郎项目定制接口
	 * 设置正在直播的设备在PC端的窗口中是否全屏显示
	 */
	public void setFull(){
		LOG.info("Executing operation setFull");
		response.setCharacterEncoding("UTF-8");
		String devNo = Util.dealNull(request.getParameter("devNo"));
		String devKey = Util.dealNull(request.getParameter("pwd"));
		LOG.info(devNo+" setFull on "+ Util.dateToStr(new Date()));
		
		try {
			JSONObject ob = new JSONObject();
			ob.put("code", 1);
			ob.put("desc", "设置失败!");
			List<TblDev> list = this.devService.getResultList("o.devNo=? and o.devKey=?", null, new Object[]{devNo,devKey});
			if (list!=null && list.size()>0) {
				TblDev dev = list.get(0);
				dev.setFullFlag(1);//窗口全屏标志 0-不全屏 1-全屏
				this.devService.update(dev);
				ob.put("code", 0);
				ob.put("desc", "设置成功!");
			}
			JsonUtil.jsonString(response, ob.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 今麦郎项目定制接口
	 * 设置正在直播的设备在PC端的窗口中退出全屏显示
	 */
	public void closeFull(){
		LOG.info("Executing operation closeFull");
		response.setCharacterEncoding("UTF-8");
		String devNo = Util.dealNull(request.getParameter("devNo"));
		String devKey = Util.dealNull(request.getParameter("pwd"));
		LOG.info(devNo+" closeFull on "+ Util.dateToStr(new Date()));
		
		try {
			JSONObject ob = new JSONObject();
			ob.put("code", 1);
			ob.put("desc", "设置失败!");
			List<TblDev> list = this.devService.getResultList("o.devNo=? and o.devKey=?", null, new Object[]{devNo,devKey});
			if (list!=null && list.size()>0) {
				TblDev dev = list.get(0);
				dev.setFullFlag(0);//窗口全屏标志 0-不全屏 1-全屏
				this.devService.update(dev);
				ob.put("code", 0);
				ob.put("desc", "设置成功!");
			}
			JsonUtil.jsonString(response, ob.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 今麦郎项目定制接口
	 * 获取轮询策略
	 */
	public void getPollingList(){
		LOG.info("Executing operation getPollingList");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		LOG.info(account+" closeFull on "+ Util.dateToStr(new Date()));
		
		try {
			JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
			if(obj.optInt("code")==0){
				TblUser user = (TblUser)JSONObject.toBean(obj.optJSONObject("user"), TblUser.class);
				List<UserPolling> list = this.userPollingService.getResultList(" o.user.id=?", null, new Object[]{user.getId()});
				if(Util.isNull(list)||list.size()==0){
					JsonUtil.jsonString(response, "[]");
					return;
				}
				JSONArray array = new JSONArray();
				JSONObject ob = null;
				for(UserPolling userPolling:list){
					ob = new JSONObject();
					ob.put("id", userPolling.getId());
					ob.put("pollingName", userPolling.getPollingName());
					ob.put("timeLen", userPolling.getTimeLen());
					ob.put("addTime", userPolling.getAddTime());
					ob.put("devList", userPolling.getDevList());
					array.add(ob);
				}
				JsonUtil.jsonString(response, array.toString());
			}else{
				JsonUtil.jsonString(response, "[]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 今麦郎项目定制接口
	 * 添加轮询策略
	 */
	public void addPolling(){
		LOG.info("Executing operation addPolling");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String pollingName = Util.dealNull(request.getParameter("pollingName"));
		String timeLen = Util.dealNull(request.getParameter("timeLen"));
		String devList = Util.dealNull(request.getParameter("devList"));
		LOG.info(account+" addPolling on "+ Util.dateToStr(new Date()));
		
		JSONObject ob = new JSONObject();
		try {
			JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
			if(obj.optInt("code")==0){
				TblUser user = (TblUser)JSONObject.toBean(obj.optJSONObject("user"), TblUser.class);
				UserPolling userPolling = new UserPolling();
				String pollingId = Util.getUUID(6);
				userPolling.setId(pollingId);
				userPolling.setPollingName(pollingName);
				userPolling.setUser(user);
				userPolling.setTimeLen(Integer.parseInt(timeLen));
				userPolling.setDevList(devList);
				userPolling.setAddTime(Util.dateToStr(new Date()));
				this.userPollingService.save(userPolling);
				ob.put("code", 0);
				ob.put("desc", "添加成功!");
				ob.put("pollingId", pollingId);
			}else{
				ob.put("code", 1);
				ob.put("desc", "该用户不存在!");
				ob.put("pollingId", "");
			}
			JsonUtil.jsonString(response, ob.toString());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 今麦郎项目定制接口
	 * 删除轮询策略
	 */
	public void removePolling(){
		LOG.info("Executing operation removePolling");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String pollingId = Util.dealNull(request.getParameter("pollingId"));
		LOG.info(account+" removePolling on "+ Util.dateToStr(new Date()));
		
		JSONObject ob = new JSONObject();
		try {
			JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
			if(obj.optInt("code", -2)!=0){
				JsonUtil.jsonString(response, obj.toString());
				return;
			}
			if(pollingId.isEmpty()){
				ob.put("code", 1);
				ob.put("desc", "请选择要删除的策略!");
				JsonUtil.jsonString(response, ob.toString());
				return ;
			}
			userPollingService.deletebyid(pollingId);
			ob.put("code", 0);
			ob.put("desc", "删除成功!");
			JsonUtil.jsonString(response, ob.toString());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 修改设备密码
	 */
	public void modifyPwd(){
		LOG.info("Executing operation modifyPwd");
		response.setCharacterEncoding("UTF-8");
		String devNo = Util.dealNull(request.getParameter("devNo"));
		String oldPwd = Util.dealNull(request.getParameter("oldPwd"));
		String newPwd = Util.dealNull(request.getParameter("newPwd"));
		LOG.info(devNo+" modifyPwd on "+Util.dateToStr(new Date()));
		try {
			JSONObject ob = new JSONObject();
			ob.put("code", 1);
			ob.put("desc", "修改失败!");
			if ("".equals(newPwd)) {
				ob.put("code", 3);
				ob.put("desc", "密码不能为空!");
				JsonUtil.jsonString(response, ob.toString());
				return;
			}
			List<TblDev> dList = this.devService.getResultList(" o.devNo=? and o.devKey=?", null, new Object[]{devNo,WorkUtil.pwdEncrypt(oldPwd)});
			if(dList != null && dList.size() > 0){
				TblDev dev = dList.get(0);
				dev.setDevKey(WorkUtil.pwdEncrypt(newPwd));
				this.devService.update(dev);
				ob.put("code", 0);
				ob.put("desc", "修改成功!");
			}else {
				ob.put("code", 2);
				ob.put("desc", "用户名或原始密码错误!");
			}
			JsonUtil.jsonString(response, ob.toString());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存用户手机硬件信息
	 */
	public void saveMobileInfo(){
		LOG.info("Executing operation saveMobileInfo");
		response.setCharacterEncoding("UTF-8");
		String devNo = Util.dealNull(request.getParameter("devNo"));
		String devKey = Util.dealNull(request.getParameter("devKey"));
		String mobileName = Util.dealNull(request.getParameter("mobileName"));
		String version = Util.dealNull(request.getParameter("version"));
		String netType = Util.dealNull(request.getParameter("netType"));
		String operatorName = Util.dealNull(request.getParameter("operatorName"));
		String imsi = Util.dealNull(request.getParameter("imsi"));
		String jPushRegistrationID = Util.dealNull(request.getParameter("JPushRegistrationID"));
		LOG.info(devNo+" saveMobileInfo on "+ Util.dateToStr(new Date()));
		
		try {
			JSONObject obj = WorkUtil.checkDev(devService, devNo, devKey);
			TblDev dev = (TblDev) JSONObject.toBean(obj.optJSONObject("dev"), TblDev.class);
			obj.remove("dev");
			if(obj.optInt("code", -1)==0){
				dev.setLastLoginTime(Util.dateToStr(new Date()));
				dev.setEditTime(Util.dateToStr(new Date()));
				dev.setPush_Registration_Id(jPushRegistrationID);
				this.devService.update(dev);
				
				TblMobile mobile = new TblMobile();
				mobile.setId(Util.getUUID(6));
				mobile.setDev(dev);
				mobile.setMobileName(mobileName);
				mobile.setVersion(version);
				mobile.setNetType(netType);
				mobile.setOperatorName(operatorName);
				mobile.setImsi(imsi);
				mobile.setAddTime(Util.dateToStr(new Date()));
				this.mobileService.save(mobile);
				obj.put("code", 0);
				obj.put("desc", "保存成功!");
			}
			JsonUtil.jsonString(response, obj.toString());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存设备操作日志
	 */
	public void saveDevLog(TblDev dev,String clientIP,int logType,String logDesc){
		LOG.info("Executing operation saveDevLog");
		DevLog devLog = new DevLog();
		try {
			MediaServer server = null;
			if (dev != null) {
				String serverId = dev.getServerId();
				server = this.mediaServerService.getById(serverId);
			}
			String id = Util.getUUID(6);
			devLog.setId(id);
			devLog.setDev(dev);
			devLog.setMediaServer(server);
			devLog.setClientIP(clientIP);
			devLog.setOperatorName("");
			devLog.setOperatorCode("");
			devLog.setLogType(logType);
			devLog.setLogDesc(logDesc);
			devLog.setAddTime(Util.dateToStr(new Date()));
			this.devLogService.save(devLog);
			//IP地址解析线程
			IPAddressThread rsthread = new IPAddressThread(devLogService,id,clientIP,ipService);
			Thread thread = new Thread(rsthread);
			thread.start();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取远程IP地址
	 * @param request
	 * @return
	 */
	public String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
           ip = request.getRemoteAddr();
       }
       return ip;
   }
	
	/**
	 * 保存用户反馈问题
	 */
	public void saveProblem(){
		LOG.info("Executing operation saveProblem");
		response.setCharacterEncoding("UTF-8");
		String devNo = Util.dealNull(request.getParameter("devNo"));
		String mobileModel = Util.dealNull(request.getParameter("mobileModel"));
		String systemOS = Util.dealNull(request.getParameter("systemOS"));
		String netType = Util.dealNull(request.getParameter("netType"));
		String content = Util.dealNull(request.getParameter("content"));
		LOG.info(devNo+" saveProblem on "+ Util.dateToStr(new Date()));
		
		try {
			JSONObject ob = new JSONObject();
			ob.put("code", 1);
			ob.put("desc", "保存失败!");
			List<TblDev> dList = this.devService.getResultList(" o.devNo=?", null, new Object[]{devNo});
			if(dList != null && dList.size() > 0){
				TblProblem problem = new TblProblem();
				problem.setId(Util.getUUID(6));
				problem.setDevNo(dList.get(0).getDevNo());
				problem.setMobileModel(mobileModel);
				problem.setSystemOS(systemOS);
				problem.setNetType(Integer.parseInt(netType));
				problem.setContent(content);
				problem.setAddTime(Util.dateToStr(new Date()));
				this.problemService.save(problem);
				ob.put("code", 0);
				ob.put("desc", "保存成功!");
			}
			JsonUtil.jsonString(response, ob.toString());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 修改PC端用户密码
	 */
	public void modifyUserPwd(){
		LOG.info("Executing operation modifyUserPwd");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String oldPwd = Util.dealNull(request.getParameter("oldPwd"));
		String newPwd = Util.dealNull(request.getParameter("newPwd"));
		LOG.info(account+" modifyUserPwd on "+ Util.dateToStr(new Date()));
		
		try {
			JSONObject ob = new JSONObject();
			ob.put("code", 1);
			ob.put("desc", "修改失败!");
			if ("".equals(newPwd)) {
				ob.put("code", 2);
				ob.put("desc", "密码不能为空!");
				JsonUtil.jsonString(response, ob.toString());
				return;
			}
			List<TblUser> uList = this.userService.getResultList(" o.account=? and o.pwd=?", null, new Object[]{account,WorkUtil.pwdEncrypt(oldPwd)});
			if(uList != null && uList.size() > 0){
				TblUser user = uList.get(0);
				user.setPwd(WorkUtil.pwdEncrypt(newPwd));
				this.userService.update(user);
				ob.put("code", 0);
				ob.put("desc", "修改成功!");
			}else {
				ob.put("code", 2);
				ob.put("desc", "用户名或原始密码错误!");
			}
			JsonUtil.jsonString(response, ob.toString());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存用户反馈问题
	 */
	public void saveError(){
		LOG.info("Executing operation saveError");
		response.setCharacterEncoding("UTF-8");
		String devNo = Util.dealNull(request.getParameter("devNo"));
		String clientIP = Util.dealNull(request.getParameter("clientIP"));
		String logDesc = Util.dealNull(request.getParameter("logDesc"));
		LOG.info(devNo+" saveError on "+ Util.dateToStr(new Date()));
		
		try {
			JSONObject ob = new JSONObject();
			ob.put("code", 1);
			ob.put("desc", "保存失败!");
			List<TblDev> dList = this.devService.getResultList(" o.devNo=?", null, new Object[]{devNo});
			if (dList != null && dList.size() >0) {
				saveDevLog(dList.get(0), clientIP, 4, logDesc);
				ob.put("code", 0);
				ob.put("desc", "保存成功!");
			}
			JsonUtil.jsonString(response, ob.toString());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取实时播放地址
	 * @return
	 */
	public void getPlayUrl(){
		LOG.info("Executing operation getPlayUrl");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		String clientIP = Util.dealNull(request.getParameter("clientIP"));
		LOG.info(account+" getPlayUrl on "+ Util.dateToStr(new Date()));
		
		TblUser user = (TblUser) JSONObject.toBean(obj.optJSONObject("user"),TblUser.class);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		obj.remove("user");
		String id = Util.dealNull(request.getParameter("id"));
		TblDev dev = devService.getById(id);
		if(dev==null){
			obj.put("code", 1);
			obj.put("desc", "没有找到当前设备");
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		obj.put("code", 0);
		obj.put("desc", "获取播放地址成功!");
		obj.put("url", Util.dealNull(dev.getPublishUrl()));
		obj.put("devId", id);
		//查询是否已被点赞过
		List<DevGood> devGoods = this.devGoodService.getResultList(" o.dev.id=? and o.user.account=?", null, new Object[]{id,account});
		boolean goodFlag = false;
		if (devGoods == null || devGoods.size() ==0) {
			goodFlag = true;//可以点赞
		}
		obj.put("goodFlag",goodFlag);
		//查询被浏览数
		List<DevView> devViews = this.devViewService.getResultList(" o.dev.id=? and o.user.account=?", null, new Object[]{id,account});
		obj.put("views", devViews.size());
		//新增浏览记录
		DevView devView = new DevView();
		devView.setId(Util.getUUID(6));
		devView.setUser(user);
		devView.setDev(dev);
		devView.setViewTime(Util.dateToStr(new Date()));
		devView.setClientIP(clientIP);
		this.devViewService.save(devView);
		JsonUtil.jsonString(response, obj.toString());
	}
	
	/**
	 * 创建会议室
	 * meetingType 创建 会议类型， 1-即时会议， 2-计划会议， 3-长期会议，meetingType=1时，statTime可为空，默认当前时间
	 * flag:会议召集类型 0：部分 1：全部,flag=1时 data可以为空
	 * @throws Exception 
	 */
	public void createMeetingRoom() throws Exception{
		LOG.info("Executing operation createMeetingRoom");
		response.setCharacterEncoding("UTF-8");
 		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String meetingType = Util.dealNull(request.getParameter("meetingType"));
		String meetingPwd = Util.dealNull(request.getParameter("meetingPwd"));
		String devDatas = Util.dealNull(request.getParameter("data"));
		String flag = Util.dealNull(request.getParameter("flag"));
		String startTime = Util.dealNull(request.getParameter("startTime"));
		String topic = Util.dealNull(request.getParameter("topic"));
		LOG.info(account+" createMeetingRoom on "+ Util.dateToStr(new Date()));
		
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		TblUser user = (TblUser) JSONObject.toBean(obj.optJSONObject("user"),TblUser.class);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		obj.remove("user");
		JSONObject ob = new JSONObject();
		ob.put("code", 0);
		ob.put("desc", "会议创建成功!");
		
		if (user.getMeetingFlag()==0) {
			ob.put("code", 5);
			ob.put("desc", "会议创建失败，该用户未开通会易通功能!");
			JsonUtil.jsonString(response, ob.toString());
			return;
		}
		
		/*
		 * 注释日期：2018-1-10 
		 * 注释原因：创建会议室机制改为从会议室资源池中获取会易通账号
		 * String zcode = user.getZcode();
		 */
		String zcode = setUserMeetingAccount(user);
		System.out.println(account+",获取会易通账号："+zcode);
		if ("".equals(zcode) || zcode == null) {
			ob.put("code", 6);
			ob.put("desc", "会议创建失败，会议室资源已满!");
			JsonUtil.jsonString(response, ob.toString());
			return;
		}
		//会议类型为即时会议时，开始时间默认当前日期
		startTime = "1".equals(meetingType)?Util.dateToStr(new Date()):startTime;
		obj = MeetingApiUtil.createMeetingRoom(zcode, topic, Integer.parseInt(meetingType), meetingPwd, startTime);
		System.out.println("**创建会议结果是否为null**："+obj.get("code"));
		if (obj.get("code")!=null ) {
			ob.put("code", 7);
			ob.put("desc", "会易通服务出现异常，会议创建失败!");
			JsonUtil.jsonString(response, ob.toString());
			return;
		}
		String meetingUuid = obj.getString("uuid");
		String meetingId = obj.getString("id");
		String start_url = obj.getString("start_url");
		String join_url = obj.getString("join_url");
		//获取用户的信息
		JSONObject meetingUser = MeetingApiUtil.getUser(user.getEmail());
		if ("100".equals(meetingUser.getString("code"))) {
			String uid = meetingUser.getString("id");
			String token = meetingUser.getString("token");
			String uname = meetingUser.getString("username");
			uname = URLEncoder.encode(uname,"UTF-8");
			start_url = "meetingnow://meetingnow.zhumu.me/start?action=start&confno="+meetingId+"&stype=100&sid=123456&uid="+uid+"&uname="+uname+"&token="+token;
			join_url = "meetingnow://meetingnow.zhumu.me?action=join&confno="+meetingId+"&pwd="+meetingPwd;
		}
		//推送线程，将会议地址推送到Android客户端
		devDatas = devDatas.replace("[", "").replace("]", "");
		int participants =1;
		if (!"".equals(devDatas)) {
			List<String> list = Arrays.asList(devDatas.split(","));
			participants += list.size();
			PushThread pushThread = new PushThread(topic, topic+",邀请您参加，请点击链接加入："+join_url,list,devService,flag,user.getCompany().getId());
			Thread thread = new Thread(pushThread);
			thread.start();
		}
		//保存会议室信息
		saveMeetingRecord(user, meetingId, meetingUuid, topic, participants, startTime, start_url, join_url);
		//更新会议室资源状态
		List<MeetingResource> mList = this.meetingResourceService.getResultList(" o.zcode =?", null, new Object[]{zcode});
		MeetingResource meetingResource = mList.get(0);
		meetingResource.setIsMeeting(1);//是否正在开会  0-否 1-是
		this.meetingResourceService.update(meetingResource);
		
		ob.put("meetingId", meetingId);
		ob.put("topic", topic);
		ob.put("password", meetingPwd);
		ob.put("start_url", start_url);
		ob.put("join_url", join_url);
		ob.put("start_time", startTime);
		System.out.println("***createMeeting***:"+ob.toString());
		JsonUtil.jsonString(response, ob.toString());
	}
	
	/**
	 * 用户创建会议室时，从会议资源表中获取会易通账号
	 * 1.先查询当前用户是否已经分配会易通账号，如果已分配，则继续使用已分配的，如果没有，则从资源表中获取并分配
	 * 2.当前用户未分配会易通账号，则按照该用户的方数以被分配时间升序查询,首先查找status=0的是否存在，如果存在，则分配，
	 * 如果不存在，则查找status=1的最先被分配的一个，并且该账号未正进行会议
	 * @param user
	 * @return zcode
	 */
	public String setUserMeetingAccount(TblUser user){
		String zcode = "";
		try {
			//查询当前用户是否已被分配会易通账号
			List<MeetingResource> uList = this.meetingResourceService.getResultList(" o.comId=? and o.usedAccount=?", null, new Object[]{user.getCompany().getId(),user.getAccount()});
			if (uList != null && uList.size() >0) {
				//更新资源表
				MeetingResource meetingResource = uList.get(0);
				meetingResource.setUsedTime(Util.dateToStr(new Date()));
				this.meetingResourceService.update(meetingResource);
				return meetingResource.getZcode();
			}
			/*if (uList != null && uList.size() >0) {
				MeetingResource meetingResource = uList.get(0);
				String mzcode = meetingResource.getZcode();
				JSONObject obj = MeetingApiUtil.queryRegularMeetingList(mzcode, 10000000, 1);
				if (Integer.parseInt(obj.getString("total_records")) > 0) {
					JSONArray array = obj.getJSONArray("meetings");
					JSONObject ob = array.getJSONObject(0);
					int meetingStatus = ob.getInt("status");
					if (meetingStatus == 0) {//上次创建的会议已结束
						//更新资源表
						meetingResource.setUsedTime(Util.dateToStr(new Date()));
						this.meetingResourceService.update(meetingResource);
						return mzcode;
					}
				}
			}*/
			int participants = user.getParticipants();
			LinkedHashMap<String, String> orderBy = new LinkedHashMap<String, String>();
			orderBy.put("usedTime", "asc");
			//查询未被分配的会易通账号
			List<MeetingResource> unList = this.meetingResourceService.getResultList(" o.comId=? and o.participants=? and o.status =?", orderBy, new Object[]{user.getCompany().getId(),participants,0});
			if (unList !=null && unList.size() >0) {
				//更新资源表
				MeetingResource meetingResource = unList.get(0);
				meetingResource.setStatus(1);
				meetingResource.setUsedTime(Util.dateToStr(new Date()));
				meetingResource.setUsedAccount(user.getAccount());
				this.meetingResourceService.update(meetingResource);
				//更新用户表
				String code = meetingResource.getZcode();
				user.setZcode(code);
				user.setEmail(meetingResource.getAccount());
				this.userService.update(user);
				return code;
			}
			//查询已被分配的会易通账号,从中选择一个满足要求的账号分配给用户
			List<MeetingResource> mList = this.meetingResourceService.getResultList(" o.comId=? and o.participants=? and o.status =?", orderBy, new Object[]{user.getCompany().getId(), participants,1});
			if (mList !=null && mList.size() >0) {
				for (MeetingResource meetingResource : mList) {
					String mzcode = meetingResource.getZcode();
					JSONObject obj = MeetingApiUtil.queryRegularMeetingList(mzcode, 10000000, 1);
					if (Integer.parseInt(obj.getString("total_records")) > 0) {
						JSONArray array = obj.getJSONArray("meetings");
						JSONObject ob = array.getJSONObject(0);
						int meetingStatus = ob.getInt("status");
						if (meetingStatus == 0) {
							//更新资源表
							meetingResource.setUsedAccount(user.getAccount());
							meetingResource.setUsedTime(Util.dateToStr(new Date()));
							this.meetingResourceService.update(meetingResource);
							//更新用户表
							user.setZcode(mzcode);
							user.setEmail(meetingResource.getAccount());
							this.userService.update(user);
							zcode = mzcode;
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return zcode;
	}
	
	/**
	 * 结束会议
	 */
	public void endMeeting(){
		LOG.info("Executing operation endMeeting");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String meetingId = Util.dealNull(request.getParameter("meetingId"));
		LOG.info(account+" endMeeting on "+ Util.dateToStr(new Date()));
		
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		obj.remove("user");
		obj.put("code", 0);
		obj.put("desc", "结束会议成功!");
		MeetingRecord meetingRecord = this.meetingRecordService.getById(meetingId);
		if (meetingRecord == null) {
			obj.put("code", -1);
			obj.put("desc", "该会议不存在!");
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		String zcode = meetingRecord.getZcode();	
		obj = MeetingApiUtil.endMeeting(zcode, meetingRecord.getMeetingId());
		if (obj.get("code") != null) {
			obj.put("code", obj.getString("code"));
			obj.put("desc", obj.getString("msg"));
		}else {
			//更新会议室资源状态
			List<MeetingResource> mList = this.meetingResourceService.getResultList(" o.zcode =? and o.isMeeting=?", null, new Object[]{zcode,1});
			if (mList != null && mList.size() >0 ) {
				MeetingResource meetingResource = mList.get(0);
				meetingResource.setIsMeeting(0);//是否正在开会  0-否 1-是
				this.meetingResourceService.update(meetingResource);
			}
		}
		System.out.println("***endMeeting***:"+obj.toString());
		JsonUtil.jsonString(response, obj.toString());
	}
	
	/**
	 * 查询某用户下创建的预约会议记录
	 * 返回的会议状态：status：0-结束 1-正在进行
	 */
	/*
	 * 注释日期：2018-1-11 
	 * 注释原因：由接口查询改为从数据库查询
	 * public void queryRegularMeetingList(){
		LOG.info("Executing operation queryRegularMeetingList");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String pageNo = Util.dealNull(request.getParameter("pageNo"));
		String pageSize = Util.dealNull(request.getParameter("pageSize"));
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		TblUser user = (TblUser) JSONObject.toBean(obj.optJSONObject("user"),TblUser.class);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		obj.remove("user");
		obj.put("code", 0);
		obj.put("desc", "查询会议成功!");
		obj.put("total_records", 0);
		obj.put("meetings","");
		String zcode = user.getZcode();
		String groupId = Util.dealNull(request.getParameter("groupId"));
		if(!"".equals(groupId) && groupId != null){
			List<UserGroup> ugList = this.userGroupService.getResultList(" o.group.id=? and o.user.meetingFlag=? ", null, new Object[]{groupId,1});
			if (ugList != null && ugList.size() >0) {
				TblUser gUser = ugList.get(0).getUser();
				zcode = gUser.getZcode();
			}else {
				obj.put("code", 1);
				obj.put("desc", "查询会议失败,该组织下的用户未开通会易通功能!");
				obj.put("total_records", 0);
				obj.put("meetings","");
				JsonUtil.jsonString(response, obj.toString());
				return;
			}
		}
		if (!"".equals(zcode) && zcode != null) {
			int page = ("".equals(pageNo)|| pageNo == null)?1:Integer.parseInt(pageNo);
			int rows = ("".equals(pageSize)|| pageSize == null)?10:Integer.parseInt(pageSize);
			JSONObject ob = MeetingApiUtil.queryRegularMeetingList(zcode, page, rows);
			obj.put("total_records", ob.get("total_records"));
			obj.put("page_count", ob.get("page_count"));
			obj.put("meetings", ob.getJSONArray("meetings"));
		}
//		System.out.println("***queryRegularMeeting***:"+obj.toString());
		JsonUtil.jsonString(response, obj.toString());
	}*/
	
	public void queryRegularMeetingList() throws Exception{
		LOG.info("Executing operation queryRegularMeetingList");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String pageNo = Util.dealNull(request.getParameter("pageNo"));
		String pageSize = Util.dealNull(request.getParameter("pageSize"));
		LOG.info(account+" queryRegularMeetingList on "+ Util.dateToStr(new Date()));
		
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		TblUser user = (TblUser) JSONObject.toBean(obj.optJSONObject("user"),TblUser.class);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		obj.remove("user");
		obj.put("code", 0);
		obj.put("desc", "查询会议成功!");
		obj.put("total_records", 0);
		obj.put("meetings","");
		String groupId = Util.dealNull(request.getParameter("groupId"));
		if(!"".equals(groupId) && groupId != null){
			List<UserGroup> ugList = this.userGroupService.getResultList(" o.group.id=? and o.user.meetingFlag=? ", null, new Object[]{groupId,1});
			if (ugList != null && ugList.size() >0) {
				TblUser gUser = ugList.get(0).getUser();
				account = gUser.getAccount();
			}else {
				obj.put("code", 1);
				obj.put("desc", "查询会议失败,该组织下的用户未开通会易通功能!");
				obj.put("total_records", 0);
				obj.put("meetings","");
				JsonUtil.jsonString(response, obj.toString());
				return;
			}
		}
		int page = ("".equals(pageNo)|| pageNo == null)?1:Integer.parseInt(pageNo);
		int rows = ("".equals(pageSize)|| pageSize == null)?10:Integer.parseInt(pageSize);
		PageView<MeetingRecord> pageView = new PageView<MeetingRecord>(rows, page);
		int firstindex = (pageView.getCurrentPage() - 1)*pageView.getMaxResult();
		LinkedHashMap<String, String> orderby = new LinkedHashMap<String, String>();
		orderby.put("addTime", "desc");
		QueryResult<MeetingRecord> qr = this.meetingRecordService.getScrollData(firstindex, pageView.getMaxResult(), " o.comId=? and o.userAccount =?", new Object[]{user.getCompany().getId(),account}, orderby);
		if(qr!=null && qr.getResultList().size()>0){
			pageView.setQueryResult(qr);
			List<MeetingRecord> list = qr.getResultList();
			obj.put("total_records", pageView.getTotalRecord());
			obj.put("page_count", pageView.getTotalPage());
			obj.put("meetings", JSONArray.fromObject(list));
		}
		System.out.println("***queryRegularMeeting***:"+obj.toString());
		JsonUtil.jsonString(response, obj.toString());
	}
	
	/**
	 * 删除会议
	 */
	public void deleteMeeting(){
		LOG.info("Executing operation deleteMeeting");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String meetingId = Util.dealNull(request.getParameter("meetingId"));
		LOG.info(account+" deleteMeeting on "+ Util.dateToStr(new Date()));
		
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		obj.remove("user");
		obj.put("code", 0);
		obj.put("desc", "删除会议成功!");
		MeetingRecord meetingRecord = this.meetingRecordService.getById(meetingId);
		if (meetingRecord == null) {
			obj.put("code", -1);
			obj.put("desc", "该会议不存在!");
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		//调用接口进行删除
		String zcode = meetingRecord.getZcode();
		JSONObject ob = MeetingApiUtil.deleteMeeting(zcode, meetingRecord.getMeetingId());
		if (ob.get("code")!=null) {
			obj.put("code", ob.get("code"));
			obj.put("desc", ob.get("msg"));
		}else {
			//删除成功后，更新会议室资源状态
			List<MeetingResource> mList = this.meetingResourceService.getResultList(" o.zcode =? and o.isMeeting=?", null, new Object[]{zcode,1});
			if (mList != null && mList.size() >0 ) {
				MeetingResource meetingResource = mList.get(0);
				meetingResource.setIsMeeting(0);//是否正在开会  0-否 1-是
				this.meetingResourceService.update(meetingResource);
			}
		}
		//删除数据库中的记录
		this.meetingRecordService.deletebyid(meetingId);
		System.out.println("***deleteMeeting***:"+obj.toString());
		JsonUtil.jsonString(response, obj.toString());
	}
	
	/*
	 * 邀请加入会议
	 *注释日期：2018-1-24 10:13:46 
	 *注释原因：加入会议机制由接口查询改为数据库资源池查询
	 public void joinMeeting(){
		LOG.info("Executing operation joinMeeting");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String meetingId = Util.dealNull(request.getParameter("meetingId"));
		String devDatas = Util.dealNull(request.getParameter("data"));
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		TblUser user = (TblUser) JSONObject.toBean(obj.optJSONObject("user"),TblUser.class);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		obj.remove("user");
		JSONObject ob = new JSONObject();
		ob.put("code", 0);
		ob.put("desc", "加入会议成功!");
		String zcode = user.getZcode();
		obj = MeetingApiUtil.getMeeting(zcode, meetingId);
		if (obj.get("code")!=null ) {
			ob.put("code", obj.get("code"));
			ob.put("desc", obj.getString("msg"));
			JsonUtil.jsonString(response, ob.toString());
			return;
		}
		String join_url = obj.getString("join_url");
		String topic = obj.getString("topic");
		//获取用户的信息
		JSONObject meetingUser = MeetingApiUtil.getUser(user.getEmail());
		if ("100".equals(meetingUser.getString("code"))) {
			String meetingPwd = obj.getString("password");
			join_url = "meetingnow://meetingnow.zhumu.me?action=join&confno="+meetingId+"&pwd="+meetingPwd;
		}
		//推送线程，将会议地址推送到Android客户端
		devDatas = devDatas.replace("[", "").replace("]", "");
		if (!"".equals(devDatas)) {
			List<String> list = Arrays.asList(devDatas.split(","));
			PushThread pushThread = new PushThread(topic, topic+",邀请您参加，请点击链接加入："+join_url,list,devService,"0",user.getCompany().getId());
			Thread thread = new Thread(pushThread);
			thread.start();
		}
		JsonUtil.jsonString(response, ob.toString());
	}*/
	/**
	 * 邀请加入会议
	 * @param type 加入会议类型 0：PC端账号加入 1：手机端账号加入类型
	 */
	public void joinMeeting(){
		LOG.info("Executing operation joinMeeting");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String meetingId = Util.dealNull(request.getParameter("meetingId"));
		String devDatas = Util.dealNull(request.getParameter("data"));
		String type = Util.dealNull(request.getParameter("type"));
		LOG.info(account+" joinMeeting on "+ Util.dateToStr(new Date()));
		
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		TblUser user = (TblUser) JSONObject.toBean(obj.optJSONObject("user"),TblUser.class);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		obj.remove("user");
		JSONObject ob = new JSONObject();
		ob.put("code", 0);
		ob.put("desc", "加入会议成功!");
		MeetingRecord meetingRecord = this.meetingRecordService.getById(meetingId);
		if (meetingRecord == null ) {
			ob.put("code", obj.get("code"));
			ob.put("desc", obj.getString("msg"));
			JsonUtil.jsonString(response, ob.toString());
			return;
		}
		String join_url = meetingRecord.getJoinUrl();
		String topic = meetingRecord.getTopic();
		ob.put("join_url", join_url);
		//手机端账号加入会议
		if ("1".equals(type)) {
			//推送线程，将会议地址推送到Android客户端
			devDatas = devDatas.replace("[", "").replace("]", "");
			if (!"".equals(devDatas)) {
				List<String> list = Arrays.asList(devDatas.split(","));
				PushThread pushThread = new PushThread(topic, topic+",邀请您参加，请点击链接加入："+join_url,list,devService,"0",user.getCompany().getId());
				Thread thread = new Thread(pushThread);
				thread.start();
			}
		}
		JsonUtil.jsonString(response, ob.toString());
	}
	
	/**
	 * 获取用户下所有的会议中的某个会议的详细信息
	 * @param meetingId 会议的UUID
	 * @param status 会议状态 0-结束 1-正在进行
	 * @throws Exception
	 */
	/*
	 * 注释日期：2018-1-11 
	 * 注释原因：由接口查询机制改为资源池机制
	 * 
	 * public void getMeetingInfo() throws Exception{
		LOG.info("Executing operation getMeetingInfo");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String meetingUuid = Util.dealNull(request.getParameter("meetingId"));
		String status = Util.dealNull(request.getParameter("status"));
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
//		TblUser user = (TblUser) JSONObject.toBean(obj.optJSONObject("user"),TblUser.class);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		obj.remove("user");
		JSONObject ob = new JSONObject();
		ob.put("code", 0);
		ob.put("desc", "获取会议详细信息成功!");
		meetingUuid = URLEncoder.encode(meetingUuid,"UTF-8");
		int type = 1;
		if ("0".equals(status)) {
			type=2;
		}
		//查询会议详细信息，正在进行的会议 type=1，已结束的会议 type=2
		obj = MeetingApiUtil.queryMeetingDetail(meetingUuid, type);
		ob.put("data", obj.get("Data"));
		if (!"0".equals(obj.getString("Code"))) {
			ob.put("code", obj.getString("Code"));
			ob.put("desc", obj.getString("Message"));
			ob.put("data", null);
		}
		System.out.println("**getMeetingInfo**:"+ob.toString());
		JsonUtil.jsonString(response, ob.toString());
	}*/
	public void getMeetingInfo() throws Exception{
		LOG.info("Executing operation getMeetingInfo");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String meetingId = Util.dealNull(request.getParameter("meetingId"));
		String status = Util.dealNull(request.getParameter("status"));
		LOG.info(account+" getMeetingInfo on "+ Util.dateToStr(new Date()));
		
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		obj.remove("user");
		JSONObject ob = new JSONObject();
		ob.put("code", 0);
		ob.put("desc", "获取会议详细信息成功!");
		ob.put("meetingId", meetingId);
		List<MeetingRecord> mList = this.meetingRecordService.getResultList(" o.meetingId=?", null, new Object[]{meetingId});
		if (mList != null && mList.size() >0) {
			MeetingRecord record = mList.get(0);
			String zcode = record.getZcode();
			String meetingUuid = record.getMeetingUuid();
			JSONObject meetingDetail = MeetingApiUtil.getMeeting(zcode, meetingId);
			if (meetingDetail.get("code") == null && meetingDetail.getInt("status") == 0 && meetingDetail.getInt("status") != Integer.parseInt(status)) {
				status = "0";
				record.setStatus(0);
				/*ob.put("code", "1");
				ob.put("desc", "该会议已结束!");
				ob.put("data", null);
				JsonUtil.jsonString(response, ob.toString());
				return;*/
				//更新会议室资源状态
				List<MeetingResource> resources = this.meetingResourceService.getResultList(" o.zcode =? and o.isMeeting=?", null, new Object[]{zcode,1});
				if (resources != null && resources.size() >0 ) {
					MeetingResource meetingResource = resources.get(0);
					meetingResource.setIsMeeting(0);//是否正在开会  0-否 1-是
					this.meetingResourceService.update(meetingResource);
				}
			}
			meetingUuid = URLEncoder.encode(meetingUuid,"UTF-8");
			int type = 1;
			if ("0".equals(status)) {
				type=2;
			}
			//查询会议详细信息，正在进行的会议 type=1，已结束的会议 type=2
			obj = MeetingApiUtil.queryMeetingDetail(meetingUuid, type);
			if (!"0".equals(obj.getString("Code"))) {
				ob.put("code", obj.getString("Code"));
				ob.put("desc", obj.getString("Message"));
				ob.put("data", null);
				this.meetingRecordService.update(record);
				JsonUtil.jsonString(response, ob.toString());
				return;
			}
			JSONObject data =  obj.getJSONObject("Data");
			ob.put("data", data);
			//更新会议记录信息
			record.setParticipants(data.getInt("participants_count"));
			record.setEndTime(type==2?Util.utcDateFormatter(data.getString("end_time")):"");//会议状态结束后才有结束日期
			record.setDuration(data.getString("duration"));
			this.meetingRecordService.update(record);
			System.out.println("**getMeetingInfo**:"+ob.toString());
			JsonUtil.jsonString(response, ob.toString());
		}
	}
	
	//保存会议室记录
	public void saveMeetingRecord(TblUser user,String meetingId,String meetingUuid,String topic,int participants,String startTime,String startUrl,String joinUrl){
		MeetingRecord meetingRecord = new MeetingRecord();
		meetingRecord.setId(Util.getUUID(6));
		meetingRecord.setComId(user.getCompany().getId());
		meetingRecord.setUserAccount(user.getAccount());
		meetingRecord.setUserName(user.getUserName());
		meetingRecord.setZcode(user.getZcode());
		meetingRecord.setMeetingId(meetingId);
		meetingRecord.setMeetingUuid(meetingUuid);
		meetingRecord.setTopic(topic);
		meetingRecord.setParticipants(participants);
		meetingRecord.setStatus(1);
		meetingRecord.setStartTime(startTime);
		meetingRecord.setStartUrl(startUrl);
		meetingRecord.setJoinUrl(joinUrl);
		meetingRecord.setAddTime(Util.dateToStr(new Date()));
		this.meetingRecordService.save(meetingRecord);
	}
	
	/**
	 * 查询会议室资源
	 */
	public void queryMeetingResource(){
		LOG.info("Executing operation queryMeetingResource");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		try {
			JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
			if(obj.optInt("code", -2)!=0){
				JsonUtil.jsonString(response, obj.toString());
				return;
			}
			TblUser user = (TblUser) JSONObject.toBean(obj.optJSONObject("user"),TblUser.class);
			obj.remove("user");
			JSONObject ob = new JSONObject();
			ob.put("code", 0);
			ob.put("desc", "获取会议资源成功!");
			List<MeetingResource> mList = this.meetingResourceService.getResultList(" o.comId=?", null, new Object[]{user.getCompany().getId()});
			ob.put("meetingResource", JSONArray.fromObject(mList));
			System.out.println("**queryMeetingResource**:"+ob.toString());
			JsonUtil.jsonString(response, ob.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 查询自评问题
	 * type:请求的类型  0-固定查询几条记录 1-随机查询几条记录
	 */
	public void queryQuestion(){
		LOG.info("Executing operation queryQuestion");
		response.setCharacterEncoding("UTf-8");
		int type = Integer.parseInt(Util.dealNull(request.getParameter("type")));
		try {
			JSONObject ob = new JSONObject();
			ob.put("code", 0);
			ob.put("desc", "查询成功!");
			if (type == 1) {
				String hql = "select o from TblQuestion o order by rand()";
				List<TblQuestion> list = this.questionService.getRandResultList(hql, 0, 5);
				ob.put("questions", JSONArray.fromObject(list));
			}else {
				String hql = "select o from TblQuestion o ";
				List<TblQuestion> list = this.questionService.getRandResultList(hql, 0, 5);
				ob.put("questions", JSONArray.fromObject(list));
			}
			System.out.println(ob.toString());
			JsonUtil.jsonString(response, ob.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存自评结果
	 * 修改日期 ：2018-7-20 
	 * 修改原因：应今麦郎方要求问题2改成问题6显示
	 * --------------------------------
	 * 修改日期：2018-12-03
	 * 修改原因：应今麦郎要求显示新增问题7-重点项目追踪
	 */
	public void saveUserQuestion(){
		LOG.info("Executing operation saveUserQuestion");
		response.setCharacterEncoding("UTf-8");
		String devNo = Util.dealNull(request.getParameter("devNo"));
		String devKey = Util.dealNull(request.getParameter("devKey"));
		String answer1 = Util.dealNull(request.getParameter("answer1"));
		String answer2 = Util.dealNull(request.getParameter("answer2"));
		String answer3 = Util.dealNull(request.getParameter("answer3"));
		String answer4 = Util.dealNull(request.getParameter("answer4"));
		String answer5 = Util.dealNull(request.getParameter("answer5"));
		String timeLen = Util.dealNull(request.getParameter("timeLen"));
		String answer6 = Util.dealNull(request.getParameter("answer6"));
		String answer7 = Util.dealNull(request.getParameter("answer7"));
		System.out.println("**saveQuestione**:"+devNo+"="+timeLen+"&&"+Util.dateToStr(new Date()));
		JSONObject obj = WorkUtil.checkDev(devService, devNo, devKey);
		if(obj.optInt("code", -1)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		obj.put("code", -1);
		obj.put("desc", "添加失败");
		try {
			TblDev dev = (TblDev) JSONObject.toBean(obj.optJSONObject("dev"), TblDev.class);
			obj.remove("dev");
			if (Integer.parseInt(timeLen) > (24*60*60)) {//直播时长数值超过24小时，判断为非法数据，不保存数据库
				obj.put("code", 9);
				obj.put("desc", "直播时长数据有问题,添加失败!");
				JsonUtil.jsonString(response, obj.toString());
				return;
			}
			if (StringUtils.isEmpty(answer2)) {
				answer2="0";
			}
			if (StringUtils.isEmpty(answer6)) {
				answer6="0";
			}
			if (StringUtils.isEmpty(answer7)) {
				answer7="0";
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String date = sdf.format(new Date());
			List<UserQuestion> list = this.userQuestionService.getResultList(" o.dev.id=? and o.addTime like ?", null, new Object[]{dev.getId(),date+"%"});
			if (list != null && list.size() >0) {
				UserQuestion userQuestion = list.get(0);
				userQuestion.setAnswer1(Integer.parseInt(answer1));
				userQuestion.setAnswer2(Integer.parseInt(answer2));
				userQuestion.setAnswer3(Integer.parseInt(answer3));
				userQuestion.setAnswer4(Integer.parseInt(answer4));
				userQuestion.setAnswer5(Integer.parseInt(answer5));
				userQuestion.setAnswer6(Integer.parseInt(answer6));
				userQuestion.setAnswer7(Integer.parseInt(answer7));
				int score = 0;
				score += Integer.parseInt(answer1)+Integer.parseInt(answer2)+Integer.parseInt(answer3)+Integer.parseInt(answer4)+Integer.parseInt(answer6)+Integer.parseInt(answer7);
				//参会人数大于等于2算1分
				if (Integer.parseInt(answer5)>=2) {
					score += 1;
				}
				/*
				 * 修改日期：2018-5-8 
				 * 修改原因：时长改为直接从直播时长表中获取
				 * int liveTimeLen = userQuestion.getTimeLen();
				 * liveTimeLen += Integer.parseInt(timeLen);
				 */
				List<DevRecordTime> drt = this.devRecordTimeService.getResultList(" o.dev.id=? and o.addTime like ?", null, new Object[]{dev.getId(),date+"%"});
				int liveTimeLen = getTimeLen(drt);
				//开会时长大于等于15分钟算1分
				if (liveTimeLen >= (15*60)) {
					score += 1;
				}
				userQuestion.setTimeLen(liveTimeLen);
				userQuestion.setScore(score);
				userQuestion.setAddTime(Util.dateToStr(new Date()));
				this.userQuestionService.update(userQuestion);
			}else {
				UserQuestion userQuestion = new UserQuestion();
				userQuestion.setId(Util.getUUID(6));
				userQuestion.setComId(dev.getCompany().getId());
				userQuestion.setDev(dev);
				userQuestion.setAnswer1(Integer.parseInt(answer1));
				userQuestion.setAnswer2(Integer.parseInt(answer2));
				userQuestion.setAnswer3(Integer.parseInt(answer3));
				userQuestion.setAnswer4(Integer.parseInt(answer4));
				userQuestion.setAnswer5(Integer.parseInt(answer5));
				userQuestion.setAnswer6(Integer.parseInt(answer6));
				userQuestion.setAnswer7(Integer.parseInt(answer7));
				int score = 0;
				score += Integer.parseInt(answer1)+Integer.parseInt(answer2)+Integer.parseInt(answer3)+Integer.parseInt(answer4)+Integer.parseInt(answer6)+Integer.parseInt(answer7);
				//参会人数大于等于2算1分
				if (Integer.parseInt(answer5)>=2) {
					score += 1;
				}
				List<DevRecordTime> drt = this.devRecordTimeService.getResultList(" o.dev.id=? and o.addTime like ?", null, new Object[]{dev.getId(),date+"%"});
				int liveTimeLen = getTimeLen(drt);
				//开会时长大于等于15分钟算1分
				if (liveTimeLen >= (15*60)) {
					score += 1;
				}
				userQuestion.setTimeLen(liveTimeLen);
				userQuestion.setScore(score);
				userQuestion.setAddTime(Util.dateToStr(new Date()));
				this.userQuestionService.save(userQuestion);
			}
			obj.put("code", 0);
			obj.put("desc", "添加成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
		JsonUtil.jsonString(response, obj.toString());
	}
	
	/**
	 * 时间字符串转化为具体秒数，recordTime="0天0小时10分钟31秒"
	 * @param list
	 * @return 
	 */
	public int getTimeLen(List<DevRecordTime> list){
		int timeLen =0;
		if (list != null && list.size() > 0) {
			for (DevRecordTime devRecordTime : list) {
				String recordTime = devRecordTime.getTimeLen();
				String[] s1 = recordTime.split("天");
				timeLen += Integer.parseInt(s1[0])*24*60*60;//天->秒
				String[] s2 = s1[1].split("小时");
				timeLen += Integer.parseInt(s2[0])*60*60;//小时->秒
				String[] s3 = s2[1].split("分钟");
				timeLen += Integer.parseInt(s3[0])*60;//分钟->秒
				String[] s4 = s3[1].split("秒");
				timeLen += Integer.parseInt(s4[0]);
			}
		}
		return timeLen;
	}
	/**
	 * PC端根据会议室资源账号，加入正在进行的会议室中
	 */
	public void enterMeeting(){
		LOG.info("Executing operation enterMeeting");
		response.setCharacterEncoding("UTF-8");
		String account = Util.dealNull(request.getParameter("account"));
		String pwd = Util.dealNull(request.getParameter("pwd"));
		String zcode = Util.dealNull(request.getParameter("zcode"));
		LOG.info(account+" enterMeeting on "+ Util.dateToStr(new Date()));
		
		JSONObject obj = WorkUtil.checkUser(userService, account, pwd);
		if(obj.optInt("code", -2)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		obj.remove("user");
		JSONObject ob = new JSONObject();
		ob.put("code", 0);
		ob.put("desc", "加入会议成功!");
		try {
			LinkedHashMap<String, String> orderBy = new LinkedHashMap<String, String>();
			orderBy.put("addTime", "desc");
			List<MeetingRecord> records = this.meetingRecordService.getResultList(" o.zcode =? and o.status=?", orderBy, new Object[]{zcode,1});
			if (records != null && records.size() >0) {
				MeetingRecord meetingRecord = records.get(0);
				String join_url = meetingRecord.getJoinUrl();
				ob.put("join_url", join_url);
			}
			JsonUtil.jsonString(response, ob.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getDevRecordList(){
		LOG.info("Executing operation getDevRecordList");
		response.setCharacterEncoding("UTF-8");
		String devNo = Util.dealNull(request.getParameter("devNo"));
		String devKey = Util.dealNull(request.getParameter("devKey"));
		String startTime = Util.dealNull(request.getParameter("startTime"));
		String endTime = Util.dealNull(request.getParameter("endTime"));
		
		LOG.info(devNo+" getDevRecordList on "+ Util.dateToStr(new Date()));
		JSONObject obj = WorkUtil.checkDev(devService, devNo, devKey);
		if(obj.optInt("code", -1)!=0){
			JsonUtil.jsonString(response, obj.toString());
			return;
		}
		obj.remove("dev");
		obj.put("code", -1);
		obj.put("desc", "查询失败");
		obj.put("recordList", "[]");
		
		List<Object> param = new ArrayList<Object>();
		LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
		orderby.put("recordStartTime", "asc");
		String where = " o.dev.devNo=? ";
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
			obj.put("code", 0);
			obj.put("desc", "查询成功");
			JsonUtil.jsonString(response, obj.toString());
			return ;
		}
		
		JSONObject ob = null;
		List<DevRecordFile> drflist = null;
		MediaServer mediaServer = null;
		for(DevRecord dr:drlist){
			drflist = this.devRecordFileService.getResultList("o.drId=?", orderby, new Object[]{dr.getId()});
			if(drflist==null || drflist.size()==0)
				continue;
			mediaServer = mediaServerService.getById(dr.getMediaServerId());
			for(DevRecordFile drf:drflist){
//				if(drf.getRecordSize()<10000)
//					continue;
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
		obj.put("code", 0);
		obj.put("desc", "查询成功");
		obj.put("recordList", array.toString());
		JsonUtil.jsonString(response, obj.toString());
	}
}
