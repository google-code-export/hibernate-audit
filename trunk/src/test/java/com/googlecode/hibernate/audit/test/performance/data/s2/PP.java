package com.googlecode.hibernate.audit.test.performance.data.s2;

import com.googlecode.hibernate.audit.test.performance.util.Util;

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
@Table(name = "PP")
public class PP
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static PP create(RRepository rRepository, P p) throws Exception
    {
        PP pp = new PP();

        Util.fillPrimitives(pp);
        rRepository.fillReferences(pp);

        pp.setP(p);

        return pp;
    }

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private P p;

    private String s0;
    private String s1;
    private String s2;

    @ManyToOne(fetch = FetchType.LAZY)
    private PHT pht;

    @ManyToOne(fetch = FetchType.LAZY)
    private PCC pcc;

    // Constructors --------------------------------------------------------------------------------

    PP()
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

    public String getS0()
    {
        return s0;
    }

    public void setS0(String s0)
    {
        this.s0 = s0;
    }

    public String getS1()
    {
        return s1;
    }

    public void setS1(String s1)
    {
        this.s1 = s1;
    }

    public String getS2()
    {
        return s2;
    }

    public void setS2(String s2)
    {
        this.s2 = s2;
    }

    public PHT getPht()
    {
        return pht;
    }

    public void setPht(PHT pht)
    {
        this.pht = pht;
    }

    public PCC getPcc()
    {
        return pcc;
    }

    public void setPcc(PCC pcc)
    {
        this.pcc = pcc;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
