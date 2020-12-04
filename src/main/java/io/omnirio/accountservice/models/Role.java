package io.omnirio.accountservice.models;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "code"}))
@Entity
public class Role {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	Integer id;
	String name;
	String code;
	
	public Role() {
	}
	
	@ManyToMany(mappedBy = "userRole", cascade = CascadeType.MERGE)
    Set<User> assignedUser;

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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "Role [id=" + id + ", name=" + name + ", code=" + code + "]";
	}
	
}
