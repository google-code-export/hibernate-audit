package com.googlecode.hibernate.audit.test.performance.data.s2;

import com.googlecode.hibernate.audit.test.performance.util.Util;
import com.googlecode.hibernate.audit.annotations.Audited;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import javax.persistence.CascadeType;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Entity
@Table(name = "AP")
@Audited
public class AP
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static AP create(Scenario s, RRepository rRepository, P p) throws Exception
    {
        AP ap = new AP();

        Util.fillPrimitives(ap);
        rRepository.fillReferences(ap);

        ap.setP(p);

        ////////////
        ////////////
        //////////// INCOMPLETE
        ////////////
        ////////////
        
//        A a = A.create(s, rRepository, ap);
//        ap.setA(a);

        return ap;
    }

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private P p;

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public P getP()
    {
        return p;
    }

    public void setP(P p)
    {
        this.p = p;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
