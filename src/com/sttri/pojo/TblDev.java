package com.sttri.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

/**
 * �豸
 * @author xiaobai
 *
 */
@Entity
@Table(name = "tbl_dev")
public class TblDev implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	private String id;
	private String devName;
	private String devNo;
	private transient String devKey;
	private String imsi;
	private String phone;
	private String publishUrl;
	private Integer audioRtpPort;
	private Integer audioRtcpPort;
	private Integer videoRtpPort;
	private Integer videoRtcpPort;
	//private String comId;
	private Company company;
	private String groupId;
	private Integer onLines;
	private Integer isAble;
	private String serverId;
	private String drId;
	private String lastLoginTime;
	private String addTime;
	private String editTime;
	private String hlsUrl;
	private Integer isTransCode;
	private String subPublishUrl;//ת���ĵ�ַ
	private Integer fullFlag;//��������Ŀ������С������ĳ�������Ƿ�ȫ��
	private String push_Registration_Id;//��������Ŀ�����������豸ע��ID
	
	public TblDev() {
	}
	
	@Id
	@Column(name = "ID", unique = true, nullable = false, length = 50)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "DevName", length = 100)
	public String getDevName() {
		return this.devName;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

	@Column(name = "DevNo", length = 50)
	public String getDevNo() {
		return this.devNo;
	}

	public void setDevNo(String devNo) {
		this.devNo = devNo;
	}

	@Column(name = "DevKey", length = 50)
	public String getDevKey() {
		return this.devKey;
	}

	public void setDevKey(String devKey) {
		this.devKey = devKey;
	}

	@Column(name = "IMSI", length = 50)
	public String getImsi() {
		return this.imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	@Column(name = "Phone", length = 11)
	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Column(name = "PublishUrl", length = 300)
	public String getPublishUrl() {
		return this.publishUrl;
	}

	public void setPublishUrl(String publishUrl) {
		this.publishUrl = publishUrl;
	}

	@Column(name = "AudioRtpPort")
	public Integer getAudioRtpPort() {
		return this.audioRtpPort;
	}

	public void setAudioRtpPort(Integer audioRtpPort) {
		this.audioRtpPort = audioRtpPort;
	}

	@Column(name = "AudioRtcpPort")
	public Integer getAudioRtcpPort() {
		return this.audioRtcpPort;
	}

	public void setAudioRtcpPort(Integer audioRtcpPort) {
		this.audioRtcpPort = audioRtcpPort;
	}

	@Column(name = "VideoRtpPort")
	public Integer getVideoRtpPort() {
		return this.videoRtpPort;
	}

	public void setVideoRtpPort(Integer videoRtpPort) {
		this.videoRtpPort = videoRtpPort;
	}

	@Column(name = "VideoRtcpPort")
	public Integer getVideoRtcpPort() {
		return this.videoRtcpPort;
	}

	public void setVideoRtcpPort(Integer videoRtcpPort) {
		this.videoRtcpPort = videoRtcpPort;
	}

	@ManyToOne
	@JoinColumn(name="comId")
	@NotFound(action=NotFoundAction.IGNORE)
	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	@Column(name = "GroupId", length = 50)
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	@Column(name = "OnLines")
	public Integer getOnLines() {
		return this.onLines;
	}

	public void setOnLines(Integer onLines) {
		this.onLines = onLines;
	}

	@Column(name = "IsAble")
	public Integer getIsAble() {
		return isAble;
	}

	public void setIsAble(Integer isAble) {
		this.isAble = isAble;
	}

	@Column(name = "ServerId", length = 50)
	public String getServerId() {
		return this.serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	@Column(name = "DrId", length = 50)
	public String getDrId() {
		return drId;
	}

	public void setDrId(String drId) {
		this.drId = drId;
	}

	@Column(name = "lastLoginTime", length = 50)
	public String getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(String lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	@Column(name = "AddTime", length = 30)
	public String getAddTime() {
		return this.addTime;
	}

	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}

	@Column(name = "EditTime", length = 30)
	public String getEditTime() {
		return this.editTime;
	}

	public void setEditTime(String editTime) {
		this.editTime = editTime;
	}
	
	@Column(name = "HlsUrl", length = 300)
	public String getHlsUrl() {
		return this.hlsUrl;
	}

	public void setHlsUrl(String hlsUrl) {
		this.hlsUrl = hlsUrl;
	}
	
	@Column(name = "IsTransCode")
	public Integer getIsTransCode() {
		return isTransCode;
	}

	public void setIsTransCode(Integer isTransCode) {
		this.isTransCode = isTransCode;
	}
	
	@Column(name = "SubPublishUrl", length = 300)
	public String getSubPublishUrl() {
		return this.subPublishUrl;
	}

	public void setSubPublishUrl(String subPublishUrl) {
		this.subPublishUrl = subPublishUrl;
	}
	
	@Column(name = "FullFlag")
	public Integer getFullFlag() {
		return fullFlag;
	}

	public void setFullFlag(Integer fullFlag) {
		this.fullFlag = fullFlag;
	}
	
	@Column(name = "Push_Registration_Id", length = 50)
	public String getPush_Registration_Id() {
		return push_Registration_Id;
	}

	public void setPush_Registration_Id(String push_Registration_Id) {
		this.push_Registration_Id = push_Registration_Id;
	}
}