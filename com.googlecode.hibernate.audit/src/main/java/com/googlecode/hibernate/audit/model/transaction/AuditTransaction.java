package com.googlecode.hibernate.audit.model.transaction;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "AUDIT_TRANSACTION")
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_TRANSACTION_ID_SEQ")
public class AuditTransaction {
	
	@Id
	@Column(name = "AUDIT_TRANSACTION_ID")
	@GeneratedValue(generator = "sequence", strategy = GenerationType.AUTO)
	private Long id;

	/** the transaction time. */
	@Column(name = "TRANSACTION_TIME")
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date transactionTime;

	/** the transaction change description. */
	@Column(name = "DESCRIPTION", length = 4000)
	private String description;

	/** the transaction change user. */
	@Column(name = "USER_NAME")
	private String user;

/*	@OneToMany(mappedBy = "auditTransaction")
	private Set<AuditTransactionRecord> auditObjects = new HashSet<AuditTransactionRecord>();
*/	
	public AuditTransaction() {
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the transactionTime
	 */
	public Date getTransactionTime() {
		return transactionTime;
	}

	/**
	 * @param transactionTime
	 *            the transactionTime to set
	 */
	public void setTransactionTime(Date transactionTime) {
		this.transactionTime = transactionTime;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

/*	public Set<AuditTransactionRecord> getAuditObjects() {
		return Collections.unmodifiableSet(auditObjects);
	}
	
	public void addAuditObject(AuditTransactionRecord record) {
		record.setAuditTransaction(this);
		auditObjects.add(record);
	}
*/}
