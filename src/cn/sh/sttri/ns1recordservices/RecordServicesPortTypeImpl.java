
/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package cn.sh.sttri.ns1recordservices;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import com.sttri.pojo.DevLog;
import com.sttri.pojo.DevRecord;
import com.sttri.pojo.DevRecordFile;
import com.sttri.pojo.DevRecordTime;
import com.sttri.pojo.MediaServer;
import com.sttri.pojo.TblDev;
import com.sttri.pojo.UserQuestion;
import com.sttri.service.IDevLogService;
import com.sttri.service.IDevRecordFileService;
import com.sttri.service.IDevRecordService;
import com.sttri.service.IDevRecordTimeService;
import com.sttri.service.IDevService;
import com.sttri.service.IMediaServerService;
import com.sttri.service.IUserQuestionService;
import com.sttri.util.Util;

/**
 * This class was generated by Apache CXF 2.5.2
 * 2015-05-17T14:15:07.054+08:00
 * Generated source version: 2.5.2
 * 
 */

@javax.jws.WebService(
                      serviceName = "RecordServices",
                      portName = "RecordServicesHttpPort",
                      targetNamespace = "http://www.sttri.sh.cn/ns1RecordServices/",
                      endpointInterface = "cn.sh.sttri.ns1recordservices.RecordServicesPortType")
                      
public class RecordServicesPortTypeImpl implements RecordServicesPortType {

    private static final Logger LOG = Logger.getLogger(RecordServicesPortTypeImpl.class.getName());
    
    @Autowired
    private IDevRecordService devRecordService;
    @Autowired
    private IDevRecordFileService devRecordFileService;
    @Autowired
    private IDevService devService;
    @Autowired
    private IMediaServerService serverService;
    @Autowired
    private IDevLogService devLogService;
    @Autowired
    private IDevRecordTimeService devRecordTimeService;
    @Autowired
    private IUserQuestionService userQuestionService;

    /* (non-Javadoc)
     * @see cn.sh.sttri.ns1recordservices.RecordServicesPortType#devEndRecord(cn.sh.sttri.ns1recordservices.DevEndRecordReq  devEndRecordReq )*
     */
    public cn.sh.sttri.ns1recordservices.DevEndRecordRes devEndRecord(DevEndRecordReq devEndRecordReq) { 
        LOG.info("Executing operation devEndRecord");
        System.out.println(devEndRecordReq);
        try {
            cn.sh.sttri.ns1recordservices.DevEndRecordRes _return = null;
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
            
        }
    }

    /* (non-Javadoc)
     * 十分钟一个录像文件
     * @see cn.sh.sttri.ns1recordservices.RecordServicesPortType#devRecordUp(cn.sh.sttri.ns1recordservices.DevRecordUpReq  devRecordUpReq )*
     */
    public cn.sh.sttri.ns1recordservices.DevRecordUpRes devRecordUp(DevRecordUpReq devRecordUpReq) { 
        LOG.info("Executing operation devRecordUp=："+devRecordUpReq.getRecordId()+"-录像任务上传文件@:"+Util.dateToStr(new Date()));
        try {
        	DevRecordFile drf = new DevRecordFile();
        	drf.setId(Util.getUUID(8));
        	drf.setDrId(devRecordUpReq.getRecordId());
        	DevRecord dr = devRecordService.getById(devRecordUpReq.getRecordId());
        	drf.setComId(dr.getComId());
        	drf.setMediaServerId(dr.getMediaServerId());
        	drf.setRecordName(devRecordUpReq.getRecordName());
        	drf.setRecordStartTime(devRecordUpReq.getRecordStartTime());
        	drf.setRecordEndTime(devRecordUpReq.getRecordEndTime());
        	drf.setRecordSize(devRecordUpReq.getRecordSize());
        	devRecordFileService.save(drf);
            cn.sh.sttri.ns1recordservices.DevRecordUpRes _return = new DevRecordUpRes();
            _return.setResult(0);
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    /* (non-Javadoc)
     * @see cn.sh.sttri.ns1recordservices.RecordServicesPortType#devStartRecord(cn.sh.sttri.ns1recordservices.DevStartRecordReq  devStartRecordReq )*
     */
    public cn.sh.sttri.ns1recordservices.DevStartRecordRes devStartRecord(DevStartRecordReq devStartRecordReq) { 
        LOG.info("Executing operation devStartRecord");
        System.out.println(devStartRecordReq);
        try {
            cn.sh.sttri.ns1recordservices.DevStartRecordRes _return = null;
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

	@Override
	public DevStreamErrorRes devStreamError(DevStreamErrorReq devStreamErrorReq) {
		// TODO Auto-generated method stub
		 LOG.info("Executing operation devStreamError："+devStreamErrorReq.getRecordId());
	     DevStreamErrorRes res = new DevStreamErrorRes();
	     try {
			String recordId = devStreamErrorReq.getRecordId();
			/*
			 * 考虑到用户在遇到开启直播失败会再次点击开启直播时，
			 * 由于通过录像对象获取设备对象会造成粗暴更新设备的离线状态，
			 * 所以改成通过该录像编号直接获取拥有该录像编号的设备对象，
			 * 这样就避免了，更新设备离线状态时，该设备是在正常直播，
			 * 更新成离线状态后，就立即更新该设备的录像编号为空
			 */
//			DevRecord devRecord = this.devRecordService.getById(recordId);
			List<TblDev> dList = this.devService.getResultList(" o.drId=?", null, new Object[]{recordId});
			if(dList !=null && dList.size() > 0){
				TblDev dev = dList.get(0);
				//设备设置离线状态后，同时更新流媒体服务器上的在线设备数
				String serverId = dev.getServerId();
				if (serverId != null && !"".equals(serverId)) {
					MediaServer server = this.serverService.getById(serverId);
					int devNum = server.getDevNum();
					devNum = devNum-1<0?0:devNum-1;
					server.setDevNum(devNum);
					this.serverService.update(server);
					LOG.info("***更新流媒体服务器的在线设备数***:"+dev.getDevNo()+"&&&"+server.getServerName());
				}
				//设置该设备录像任务结束
				DevRecord devRecord = this.devRecordService.getById(recordId);
				devRecord.setRecordEndTime(Util.dateToStr(new Date()));
				devRecord.setRecordStatus(2);
				this.devRecordService.update(devRecord);
				//更新设备的在线状态
				dev.setOnLines(1);
				dev.setDrId("");
				dev.setServerId("");
				dev.setEditTime(Util.dateToStr(new Date()));
				this.devService.update(dev);
				//更新设备直播时长记录
				updateDevRecordTime(dev,recordId);
				//记录日志
				saveDevLog(dev, 3, dev.getDevNo()+",直播过程中出现网络异常导致中断!");
				res.setResult(0);
			}else {
				//记录日志
				System.out.println("录像任务编号："+recordId+",直播过程中出现网络异常导致中断,告知服务器失败!");
				res.setResult(1);
			}
		} catch (Exception e) {
			// TODO: handle exception
			res.setResult(-1);
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 保存设备操作日志
	 */
	public void saveDevLog(TblDev dev,int logType,String logDesc){
		DevLog devLog = new DevLog();
		try {
			LinkedHashMap<String, String> orderby = new LinkedHashMap<String, String>();
			orderby.put("addTime", "desc");
			String clientIP ="", operatorName="";
			MediaServer server = null;
			if (dev != null) {
				String serverId = dev.getServerId();
				server = this.serverService.getById(serverId);
				List<DevLog> dLogs = this.devLogService.getResultList(" o.dev.id=? and o.logType=? ", orderby, new Object[]{dev.getId(),0});
				if (dLogs !=null && dLogs.size() >0) {
					clientIP = dLogs.get(0).getClientIP();
					operatorName = dLogs.get(0).getOperatorName();
				}
			}
			String id = Util.getUUID(6);
			devLog.setId(id);
			devLog.setDev(dev);
			devLog.setMediaServer(server);
			devLog.setClientIP(clientIP);
			devLog.setOperatorName(operatorName);
			devLog.setOperatorCode("");
			devLog.setLogType(logType);
			devLog.setLogDesc(logDesc);
			devLog.setAddTime(Util.dateToStr(new Date()));
			this.devLogService.save(devLog);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 更新某个设备的直播时长
	 * @param timeLen求取的是停止时间和开始时间之间的差值，先获取的是两个时间之间相差的毫秒数，然后除以1000得到秒数
	 * @param status 0-直播结束 1-直播开始
	 * @throws ParseException 
	 */
	public void updateDevRecordTime(TblDev dev,String recordId) throws ParseException{
		List<DevRecordTime> list = this.devRecordTimeService.getResultList(" o.dev.id=? and o.status=? and o.recordTaskNo=? ", null, new Object[]{dev.getId(),1,recordId});
		if (list != null && list.size() >0) {
			DevRecordTime devRecordTime = list.get(0);
			String recordStartTime = devRecordTime.getRecordStartTime();
			String recordEndTime = Util.dateToStr(new Date());
//			int timeLen = (new Long(Util.datediff(recordStartTime, recordEndTime, "yyyy-MM-dd HH:mm:ss")).intValue())/1000;
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date startDate=sdf.parse(recordStartTime);
		    Date endDate=sdf.parse(recordEndTime);
		    //当前时间减去90秒，减去录像服务等待的时间
		    Calendar c = Calendar.getInstance();
		    c.setTime(endDate);
		    c.add(Calendar.SECOND, -90);
		    endDate = c.getTime();
			String timeLen = Util.getDatePoor(endDate, startDate);
			devRecordTime.setRecordEndTime(recordEndTime);
			devRecordTime.setTimeLen(timeLen);
			devRecordTime.setStatus(0);
			this.devRecordTimeService.update(devRecordTime);
			//重新更新晨会质量表里该设备的直播时长
			String date = recordStartTime.substring(0,10);
			//查询date日期提交的问卷记录
			List<UserQuestion> uList = this.userQuestionService.getResultList(" o.dev.id=? and o.addTime like ?", null, new Object[]{dev.getId(),date+"%"});
			if (uList != null && uList.size() >0) {
				UserQuestion userQuestion = uList.get(0);
				int score = userQuestion.getScore();
				int hasdTimeLen = userQuestion.getTimeLen();
				//查询date日期所有录像任务数据
				List<DevRecordTime> drt = this.devRecordTimeService.getResultList(" o.dev.id=? and o.addTime like ?", null, new Object[]{dev.getId(),date+"%"});
				int liveTimeLen = getTimeLen(drt);
				//开会时长大于等于15分钟算1分,当该设备的以前统计的时长小于15分钟，并且总时长大于等于15分钟了，则分数+1
				if (hasdTimeLen < (15*60) && liveTimeLen >= (15*60)) {
					score += 1;
				}
				userQuestion.setTimeLen(liveTimeLen);
				userQuestion.setScore(score);
				this.userQuestionService.update(userQuestion);
			}
		}
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
}
