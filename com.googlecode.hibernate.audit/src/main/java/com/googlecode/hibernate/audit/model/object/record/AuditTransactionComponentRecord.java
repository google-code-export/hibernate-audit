package com.googlecode.hibernate.audit.model.transaction.record;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("COMPONENT")
public class AuditTransactionComponentRecord extends AuditTransactionRecord {

}
