package com.googlecode.hibernate.audit.test.performance.data.s2;


import com.googlecode.hibernate.audit.test.performance.util.Util;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Entity
@Table(name = "CLC")
public class CLC
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static CLC create(Scenario s, RRepository rr, P p) throws Exception
    {
        ////////////
        ////////////
        //////////// INCOMPLETE
        ////////////
        ////////////
        
        CLC clc = new CLC();

        Util.fillPrimitives(clc);
        rr.fillReferences(clc);

        clc.setP(p);
        return clc;
    }

    // Attributes ----------------------------------------------------------------------------------

    ////////////
    ////////////
    //////////// INCOMPLETE
    ////////////
    ////////////

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private P p;

    // Constructors --------------------------------------------------------------------------------

    CLC()
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
