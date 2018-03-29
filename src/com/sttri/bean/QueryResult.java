package com.sttri.bean;

import java.util.List;

public class QueryResult<T> {
	private List<T> resultList;
	private long totalRecord;
	
	public List<T> getResultList() {
		return resultList;
	}
	public void setResultlist(List<T> resultList) {
		this.resultList = resultList;
	}
	public long getTotalRecord() {
		return totalRecord;
	}
	public void setTotalRecord(long totalRecord) {
		this.totalRecord = totalRecord;
	}
}
