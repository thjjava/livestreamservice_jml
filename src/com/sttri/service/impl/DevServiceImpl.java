package com.sttri.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sttri.bean.QueryResult;
import com.sttri.dao.CommonDao;
import com.sttri.pojo.DevRecord;
import com.sttri.pojo.DevRecordTime;
import com.sttri.pojo.MediaServer;
import com.sttri.pojo.TblDev;
import com.sttri.service.IDevRecordService;
import com.sttri.service.IDevRecordTimeService;
import com.sttri.service.IDevService;
import com.sttri.service.IMediaServerService;
import com.sttri.service.IUserQuestionService;
import com.sttri.thread.HlsLiveEndThread;
import com.sttri.thread.HlsLiveStartThread;
import com.sttri.thread.RecordEndThread;
import com.sttri.thread.RecordStartThread;
import com.sttri.thread.TransCodeEndThread;
import com.sttri.thread.TransCodeStartThread;
import com.sttri.util.Base64Util;
import com.sttri.util.MD5Util;
import com.sttri.util.Util;
import com.sttri.util.WorkUtil;

@Service
public class DevServiceImpl implements IDevService {
	@Autowired
	private CommonDao dao;
	@Autowired
	private IMediaServerService mediaServerService;
	@Autowired
	private IDevRecordService devRecordService;
	@Autowired
	private IDevRecordTimeService devRecordTimeService;
	@Autowired
	private IUserQuestionService userQuestionService;
	
	@Override
	public void deletebyid(Object id) {
		dao.delete(TblDev.class, id);
	}

	@Override
	public void deletebyids(Object[] array) {
		dao.delete(TblDev.class, array);
	}

	@Override
	public TblDev getById(Object id) {
		return dao.find(TblDev.class, id);
	}

	@Override
	public List<TblDev> getResultList(String wherejpql,
			LinkedHashMap<String, String> orderby, Object... queryParams) {
		return dao.getResultList(TblDev.class, wherejpql, orderby, queryParams);
	}

	@Override
	public QueryResult<TblDev> getScrollData(int firstindex, int maxresult,
			String wherejpql, Object[] queryParams,
			LinkedHashMap<String, String> orderby) {
		return dao.getScrollData(TblDev.class, firstindex, maxresult, wherejpql, queryParams, orderby);
	}

	@Override
	public void save(TblDev dev) {
		dao.save(dev);
	}

	@Override
	public void update(TblDev dev) {
		dao.update(dev);
	}

	/**
	 * 直播开始
	 */
	@Override
	public JSONObject videoStart(TblDev dev,String protocol) {
		if(dev.getOnLines()==0){
			videoEnd(dev,"");
			try {
				updateDevRecordTime(dev);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		JSONObject obj = new JSONObject();
		boolean flag = false;
		obj.put("flag", flag);
		obj.put("url", "");
		String imsi = dev.getImsi();
		MediaServer mediaServer = WorkUtil.currDevMediaServer(mediaServerService, dev,protocol);
		if (mediaServer == null) {
			obj.put("flag", flag);
			obj.put("url", "");
			obj.put("recordTaskNo", "");
			return obj;
		}
		/*
		 * 注释日期：2017-5-22 
		 * 原因：流媒体服务器自己检验是否正常，业务接口直接获取正常的服务器对象
		 * 
		boolean serverFlag = false;
		while(!serverFlag){
			mediaServer = WorkUtil.currDevMediaServer(mediaServerService, dev,protocol);
			if (mediaServer == null) {
				obj.put("flag", flag);
				obj.put("url", "");
				obj.put("recordTaskNo", "");
				return obj;
			}
			String playUrl = mediaServer.getRecordPlayUrl();
			try {
				URL url = new URL(playUrl.replace("rtmp", "http").replace("rtsp", "http"));
				String ip = url.getHost();
				int port = url.getPort();
				serverFlag= SocketUtils.getSocket(ip, port);
				//当服务器连接不通时，更新服务器在线状态为离线
				if (serverFlag == false) {
					System.out.println("***流媒体服务器出现异常***："+mediaServer.getServerName());
					mediaServer.setOnLine(0);
					this.mediaServerService.update(mediaServer);
				}
			} catch (Exception e) {
				serverFlag = true;
				e.printStackTrace();
			}
		}*/
		
		/**
		 * 修改时间：2017-5-15 
		 * 修改说明：新增录像地址 recordUrl,用内网地址生成的URL，告知给录像服务
		 * --------------
		 * 修改时间：2017-07-17 
		 * 修改说明：修改直播第url和录像服务地址recordUrl的拼凑方式
		 */
		String sdpUrl = Base64Util.encode((dev.getDevNo()+dev.getDevKey()+System.currentTimeMillis()))+".sdp";
		String url = mediaServer.getRealPlayUrl()+sdpUrl;//外网直播地址，告知手机
		String recordUrl = mediaServer.getRecordPlayUrl()+sdpUrl;//内网直播地址，告知录像服务
		if("rtmp".equals(protocol)){
//			String urlStr = Base64Util.encode((dev.getDevNo()+dev.getDevKey()+System.currentTimeMillis()));
			String rtmpUrl = MD5Util.MD5Code(dev.getDevNo()+dev.getDevKey()+System.currentTimeMillis());
			rtmpUrl = rtmpUrl.substring(0,16);
			url = mediaServer.getRealPlayUrl()+rtmpUrl;
			recordUrl = mediaServer.getRecordPlayUrl()+rtmpUrl;
		}
		System.out.println(dev.getDevNo()+"的外网直播地址："+url+",内网直播地址："+recordUrl);
		if(dev.getCompany().getComStoreDays()>0){
			DevRecord dr = new DevRecord();
			String id = Util.getUUID(8);
			dr.setId(id);
			dr.setDev(dev);
			dr.setComId(dev.getCompany().getId());
			dr.setMediaServerId(mediaServer.getId());
			dr.setRecordStatus(1);
			dr.setRecordStartTime(Util.dateToStr(new Date()));
			dr.setRecordSource(2);
			devRecordService.save(dr);
			dev.setDrId(id);
			String serviceUrl = mediaServer.getRecordWebServiceUrl();
			RecordStartThread rsthread = new RecordStartThread(id, recordUrl, serviceUrl);
			Thread thread = new Thread(rsthread);
			thread.start();
		}
		//1：表示该company支持HLS直播
		if(dev.getCompany().getHlsLiveFlag()==1){
			String serviceUrl = mediaServer.getHlsServiceUrl();
			HlsLiveStartThread hlsthread = new HlsLiveStartThread(dao,dev.getDevNo(), recordUrl, serviceUrl);
			Thread thread = new Thread(hlsthread);
			thread.start();
		}
		//1:表示该设备支持转码服务
		if (dev.getIsTransCode() == 1) {
			String transCodeServiceUrl = mediaServer.getTransCodeServiceUrl();
			TransCodeStartThread transCodeThread = new TransCodeStartThread(dao, dev.getDevNo(), recordUrl, transCodeServiceUrl,mediaServer.getRealPlayUrl());
			Thread thread = new Thread(transCodeThread);
			thread.start();
		}
		dev.setImsi(imsi);
		dev.setPublishUrl(url);
		dev.setOnLines(0);
		dev.setServerId(mediaServer.getId());
		update(dev);
		//更新服务器表使用该编号服务器在线设备数
		int devNum = mediaServer.getDevNum();
		devNum +=1;
		mediaServer.setDevNum(devNum);
		this.mediaServerService.update(mediaServer);
		//记录设备直播时长
		saveDevRecordTime(dev);
		
		flag = true;
		obj.put("flag", flag);
		obj.put("url", url);
		obj.put("recordTaskNo", dev.getDrId());
		return obj;
	}

	/**
	 * 手机直播结束
	 * @param recordTaskNo 录像任务编号，如果recordTaskNo为空，则以TblDev获取不为空情况下的drId作为参数请求停止服务，
	 * 如果recordTaskNo不为空，则直接以传入的recordTaskNo作为参数请求停止服务
	 */
	@Override
	public boolean videoEnd(TblDev dev,String recordTaskNo) {
		boolean flag = false;
		MediaServer mediaServer = mediaServerService.getById(dev.getServerId());
		if(mediaServer == null){
			return true;
		}
		//更新服务器表使用该编号服务器在线设备数
		int devNum = mediaServer.getDevNum();
		devNum = devNum-1<0?0:devNum-1;
		mediaServer.setDevNum(devNum);
		this.mediaServerService.update(mediaServer);
		
		if("".equals(recordTaskNo) || recordTaskNo == null || recordTaskNo == ""){
			if(!dev.getDrId().equals("")){
				recordTaskNo = dev.getDrId();
			}else {
				dev.setServerId("");
				dev.setDrId("");
				dev.setOnLines(1);
				dev.setFullFlag(0);
				dev.setEditTime(Util.dateToStr(new Date()));
				update(dev);
				flag = true;
				return flag;
			}
		}
		
		RecordEndThread rethread = new RecordEndThread(devRecordService, recordTaskNo, mediaServer.getRecordWebServiceUrl());
		Thread thread = new Thread(rethread);
		thread.start();
		
		//1：表示该company支持HLS直播
		if(dev.getCompany().getHlsLiveFlag()==1){
			HlsLiveEndThread hlsthread = new HlsLiveEndThread(mediaServer.getHlsServiceUrl(),dev.getDevNo(),dao);
			Thread thread1 = new Thread(hlsthread);
			thread1.start();
		}
		
		//1:表示该设备支持转码服务
		if (dev.getIsTransCode() == 1) {
			String transCodeServiceUrl = mediaServer.getTransCodeServiceUrl();
			TransCodeEndThread transCodeThread = new TransCodeEndThread(transCodeServiceUrl,dev.getDevNo(),dao);
			Thread thread2 = new Thread(transCodeThread);
			thread2.start();
		}
		dev.setServerId("");
		dev.setDrId("");
		dev.setOnLines(1);
		dev.setFullFlag(0);
		dev.setEditTime(Util.dateToStr(new Date()));
		update(dev);
		//更新设备直播时长记录
		try {
			updateDevRecordTime(dev);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		flag = true;
		return flag;
	}

	/**
	 * 记录某个设备的直播时长
	 */
	public void saveDevRecordTime(TblDev dev){
		DevRecordTime devRecordTime = new DevRecordTime();
		devRecordTime.setId(Util.getUUID(6));
		devRecordTime.setDev(dev);
		devRecordTime.setRecordStartTime(Util.dateToStr(new Date()));
		devRecordTime.setRecordEndTime("");
		devRecordTime.setTimeLen("0天0小时0分钟0秒");
		devRecordTime.setStatus(1);
		devRecordTime.setAddTime(Util.dateToStr(new Date()));
		devRecordTime.setRecordTaskNo(dev.getDrId());
		this.devRecordTimeService.save(devRecordTime);
	}

	/**
	 * 更新某个设备的直播时长,以秒计算
	 * @param timeLen求取的是停止时间和开始时间之间的差值，先获取的是两个时间之间相差的毫秒数，然后除以1000得到秒数
	 * @param status 0-直播结束 1-直播开始
	 * @throws ParseException 
	 */
	public void updateDevRecordTime(TblDev dev) throws ParseException{
		LinkedHashMap<String, String> orderBy = new LinkedHashMap<String, String>();
		orderBy.put("addTime", "desc");
		List<DevRecordTime> list = this.devRecordTimeService.getResultList(" o.dev.id=? and o.status=?", orderBy, new Object[]{dev.getId(),1});
		if (list != null && list.size() > 0) {
			DevRecordTime devRecordTime = list.get(0);
			String recordStartTime = devRecordTime.getRecordStartTime();
			String recordEndTime = Util.dateToStr(new Date());
//			int timeLen = (new Long(Util.datediff(recordStartTime, recordEndTime, "yyyy-MM-dd HH:mm:ss")).intValue())/1000;
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date startDate=sdf.parse(recordStartTime);
		    Date endDate=sdf.parse(recordEndTime);
			String timeLen = Util.getDatePoor(endDate, startDate);
			devRecordTime.setRecordEndTime(recordEndTime);
			devRecordTime.setTimeLen(timeLen);
			devRecordTime.setStatus(0);
			this.devRecordTimeService.update(devRecordTime);
			
		}
	}
	
}
