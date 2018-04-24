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
 * 用户晨会自评表
 */
@Entity
@Table(name = "user_question")
public class UserQuestion implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	private String id;
	private String comId;
	private TblDev dev;
	private Integer answer1;
	private Integer answer2;
	private Integer answer3;
	private Integer answer4;
	private Integer answer5;
	private Integer score;
	private Integer timeLen;
	private String addTime;
	
	public UserQuestion() {
	}
	
	@Id
	@Column(name = "ID", unique = true, nullable = false, length = 50)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "ComId", length = 50)
	public String getComId() {
		return this.comId;
	}

	public void setComId(String comId) {
		this.comId = comId;
	}
	
	@ManyToOne
	@JoinColumn(name="DevId")
	@NotFound(action=NotFoundAction.IGNORE)
	public TblDev getDev() {
		return dev;
	}

	public void setDev(TblDev dev) {
		this.dev = dev;
	}
	
	@Column(name = "Answer1")
	public Integer getAnswer1() {
		return answer1;
	}

	public void setAnswer1(Integer answer1) {
		this.answer1 = answer1;
	}

	@Column(name = "Answer2")
	public Integer getAnswer2() {
		return answer2;
	}

	public void setAnswer2(Integer answer2) {
		this.answer2 = answer2;
	}
	
	@Column(name = "Answer3")
	public Integer getAnswer3() {
		return answer3;
	}

	public void setAnswer3(Integer answer3) {
		this.answer3 = answer3;
	}
	
	@Column(name = "Answer4")
	public Integer getAnswer4() {
		return answer4;
	}

	public void setAnswer4(Integer answer4) {
		this.answer4 = answer4;
	}
	
	@Column(name = "Answer5")
	public Integer getAnswer5() {
		return answer5;
	}

	public void setAnswer5(Integer answer5) {
		this.answer5 = answer5;
	}
	
	@Column(name = "Score")
	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}
	
	@Column(name = "TimeLen")
	public Integer getTimeLen() {
		return timeLen;
	}

	public void setTimeLen(Integer timeLen) {
		this.timeLen = timeLen;
	}
	
	@Column(name = "AddTime", length = 20)
	public String getAddTime() {
		return addTime;
	}

	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}
}