package com.catoocraft.demo.dbunit.model;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "items")
public class Item {

	@Id
	//@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;

	public String title;

	public Integer price;

	@Column(updatable = false)
	public Timestamp created;

	public Timestamp updated;

	@Version()
	public Integer version;

	@PrePersist
	public void prePersist() {
		Timestamp ts = new Timestamp((new Date()).getTime());
		this.created = ts;
		this.updated = ts;
	}

	@PreUpdate
	public void preUpdate() {
		this.updated = new Timestamp((new Date()).getTime());
	}

}
