package com.googlecode.hibernate.audit.model_obsolete.transaction.record;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("COMPONENT")
public class AuditTransactionComponentRecord extends AuditTransactionRecord {

}
