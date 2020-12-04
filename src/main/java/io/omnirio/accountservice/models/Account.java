package io.omnirio.accountservice.models;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class Account {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	private String name;
	private String type;
	private Date openDate;
	private String branch;
	private boolean isMinor;
	
	public Account() {
	}
	
	@OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Date getOpenDate() {
		return openDate;
	}
	
	public void setOpenDate(Date openDate) {
		this.openDate = openDate;
	}
	
	public String getBranch() {
		return branch;
	}
	
	public void setBranch(String branch) {
		this.branch = branch;
	}
	
	public boolean isMinor() {
		return isMinor;
	}
	
	public void setMinor(boolean isMinor) {
		this.isMinor = isMinor;
	}

	@Override
	public String toString() {
		return "Account [id=" + id + ", name=" + name + ", type=" + type + ", openDate=" + openDate + ", branch="
				+ branch + ", isMinor=" + isMinor + "]";
	}
	
}
