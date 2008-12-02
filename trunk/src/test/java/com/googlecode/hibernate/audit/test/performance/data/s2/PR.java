package com.googlecode.hibernate.audit.test.performance.data.s2;

import com.googlecode.hibernate.audit.test.performance.util.Util;
import com.googlecode.hibernate.audit.annotations.Audited;

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
@Table(name = "PR")
@Audited
public class PR
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static PR create(RRepository rRepository, P pone, P ptwo) throws Exception
    {
        PR pr = new PR();

        Util.fillPrimitives(pr);
        rRepository.fillReferences(pr);

        pr.setPone(pone);
        pr.setPtwo(ptwo);

        return pr;
    }

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private P pone;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private P ptwo;

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

    public P getPone()
    {
        return pone;
    }

    public void setPone(P pone)
    {
        this.pone = pone;
    }

    public P getPtwo()
    {
        return ptwo;
    }

    public void setPtwo(P ptwo)
    {
        this.ptwo = ptwo;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
