package com.sttri.thread;

import java.util.List;

import com.sttri.pojo.TblDev;
import com.sttri.service.IDevService;
import com.sttri.util.JPushUtil;


public class PushThread implements Runnable{
	private String title;
	private String content;
	private List<String> array;
	private IDevService devService;
	private String flag;
	private String comId;
	
	public PushThread(String title, String content,List<String> array,IDevService devService,String flag,String comId){
		this.title = title;
		this.content = content;
		this.array = array;
		this.devService = devService;
		this.flag = flag;
		this.comId = comId;
	}

	@Override
	public void run() {
		System.out.println("进入推送消息线程!");
		try {
			Thread.sleep(1000);
			if ("1".equals(flag)) {//表示推送该comid下的所有设备
				List<TblDev> dList = devService.getResultList(" o.company.id=? ", null, new Object[]{comId});
				for (int i = 0; i < dList.size(); i++) {
					TblDev dev = dList.get(i);
					if (!"".equals(dev.getPush_Registration_Id()) && dev.getPush_Registration_Id() != null) {
						JPushUtil.sendPush(title, content+"&uname="+dev.getDevName(),dev.getPush_Registration_Id());
					}
				}
			}else {
				for (int i = 0; i < array.size(); i++) {
					List<TblDev> list = devService.getResultList("o.devNo=?", null, array.get(i));
					TblDev dev = list.get(0);
					if (!"".equals(dev.getPush_Registration_Id()) && dev.getPush_Registration_Id() != null) {
						JPushUtil.sendPush(title, content+"&uname="+dev.getDevName(),dev.getPush_Registration_Id());
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
