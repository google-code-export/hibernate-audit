package com.googlecode.hibernate.audit.listener;

import org.apache.log4j.Logger;
import org.hibernate.StatelessSession;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.engine.SessionImplementor;
import com.googlecode.hibernate.audit.model.transaction.AuditTransaction;

/**
 * @author <a href="mailto:chobantonov@gmail.com">Petko Chobantonov</a>
 * @author <a href="mailto:jchobantonov@gmail.com">Zhelyazko Chobantonov</a>
 * @author <a href="mailto:kchobantonov@gmail.com">Krasimir Chobantonov</a>
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 */
abstract class AbstractAuditEventListener implements AuditEventListener
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(AbstractAuditEventListener.class);

    // TODO this should be a member of HibernateAudit, not AbstractAuditEventListener
    private static final ThreadLocal<AuditTransaction> auditTransaction;

    static
    {
       auditTransaction = new ThreadLocal<AuditTransaction>();
    }

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // the "dedicated" audit session factory, if available. May be null if HBA was configured to use
    // the session factory of the persistence unit being audited
    private SessionFactory auditSessionFactory;

    // Constructors --------------------------------------------------------------------------------

    // AuditEventListener implementation -----------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // TODO maybe we don't need to expose this to subclasses, evaluate later
    protected StatelessSession getAuditSession(SessionImplementor auditedSession)
    {
        if (auditSessionFactory != null)
        {
            // TODO explore the possiblity (and add tests) of stateless session being cached and reused
            return (StatelessSession)auditSessionFactory.getCurrentSession();
        }

        SessionFactory auditedSessionFactory = auditedSession.getFactory();
        //event.getSession().connection()
        StatelessSession ss = auditedSessionFactory.openStatelessSession();

        return ss;
    }

    /**
     * Make sure we log the transaction the current event occured in scope of.
     */
    protected void logTransaction(SessionImplementor auditedSession)
    {
        AuditTransaction at = auditTransaction.get();

        if (at != null)
        {
            // already logged

            // TODO assert it's the active one

            return;
        }

        // todo this cast is not ok
        Transaction t = ((Session)auditedSession).getTransaction();

        at = new AuditTransaction(System.identityHashCode(t));

        // persist in the context of the audited session, if not dedicated session is available,
        // or in the context of the dedicated session

        auditTransaction.set(at);

        throw new RuntimeException("CONTINUE HERE");

        

    }

//    protected synchronized AuditTransaction getOrCreateAuditTransaction(
//            StatelessSession session, String actorId, Session originalSession) {
//        Object transactionKey = getTransactionKey(originalSession);
//        AuditTransaction auditTransaction = null;
//
//        // create HashMap if necessary
//        HashMap<Object, AuditTransaction> auditTransactions = transactionKeyToAuditTransaction
//                .get();
//        if (auditTransactions == null) {
//            auditTransactions = new HashMap<Object, AuditTransaction>();
//            transactionKeyToAuditTransaction.set(auditTransactions);
//        } else {
//            auditTransaction = auditTransactions.get(transactionKey);
//        }
//
//        if (auditTransaction == null) {
//            auditTransaction = new AuditTransaction();
//            auditTransaction.setTransactionTime(new Date());
//            auditTransaction.setUser(actorId);
//            session.insert(auditTransaction);
//            /*
//             * if (LOG.isDebugEnabled()) { LOG.debug("Add audit transaction with
//             * id " + auditTransaction.getId() + " for user " + actorId); }
//             */
//            // clear any previous states
//            auditTransactions.clear();
//            auditTransactions.put(transactionKey, auditTransaction);
//        }
//        return auditTransaction;
//    }

//    protected Object getTransactionKey(Session originalSession) {
//
//        Object transactionKey = null;
//
//        Transaction transaction = originalSession.getTransaction();
//
//        if (transaction instanceof JTATransaction) {
//
//            // try to get transaction ID from TransactionSynchronizationRegistry
//            try {
//                // we are going to use reflection so the audit don't depend on
//                // JNDI or JTA interfaces
//                Class transactionSynchronizationRegistryClass = Class
//                        .forName(
//                                "javax.transaction.TransactionSynchronizationRegistry",
//                                true, AuditAbstractEventListener.class.getClassLoader());
//
//                // If we came here, we might be on Java EE 5, since the JTA 1.1
//                // API is present.
//                Class initialContextClass = Class.forName(
//                        "javax.naming.InitialContext", true,
//                        AuditAbstractEventListener.class.getClassLoader());
//                Object initialContext = initialContextClass.newInstance();
//
//                Method lookupMethod = initialContextClass.getMethod("lookup", String.class);
//
//                Object transactionSynchronizationRegistry = lookupMethod
//                        .invoke(initialContext,
//                                "java:comp/TransactionSynchronizationRegistry");
//
//                Method method = transactionSynchronizationRegistryClass
//                    .getMethod("getTransactionKey");
//
//                transactionKey = method.invoke(transactionSynchronizationRegistry);
//
//            } catch (Exception e) {
//
//                log.debug("JTA transaction lookup failed", e);
//            }
//        }
//
//        if (transactionKey == null) {
//            transactionKey = originalSession.getTransaction();
//        }
//
//        return transactionKey;
//    }


    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
