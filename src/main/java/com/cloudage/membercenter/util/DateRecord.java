package com.cloudage.membercenter.util;



import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.MappedSuperclass;
import javax.persistence.PreUpdate;

@MappedSuperclass
public class DateRecord{
	Date createDate;
	Date editDate;
	
	@Column(updatable=false)
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	public Date getEditDate() {
		return editDate;
	}
	public void setEditDate(Date editDate) {
		this.editDate = editDate;
	}
	
	@PreUpdate
	void onPreUpdate(){
		editDate = new Date();
	}
	
	

}
