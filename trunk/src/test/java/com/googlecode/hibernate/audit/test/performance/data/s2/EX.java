package com.googlecode.hibernate.audit.test.performance.data.s2;

import com.googlecode.hibernate.audit.test.performance.util.Util;
import com.googlecode.hibernate.audit.annotations.Audited;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Entity
@Table(name = "EX")
@Audited
public class EX
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static EX create(Scenario s, RRepository rRepository, P p) throws Exception
    {
        EX ex = new EX();

        Util.fillPrimitives(ex);
        rRepository.fillReferences(ex);

        ex.setP(p);

        ////////////
        ////////////
        //////////// INCOMPLETE
        ////////////
        ////////////

        return ex;

    }

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private P p;

    // Constructors --------------------------------------------------------------------------------

    EX()
    {
    }

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
