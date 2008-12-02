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
import java.util.Date;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Entity
@Table(name = "DP")
@Audited
public class DP
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static DP create(Scenario s, RRepository rRepository, D d) throws Exception
    {
        DP dp = new DP();

        Util.fillPrimitives(dp);
        rRepository.fillReferences(dp);

        dp.setD(d);

        P p = P.create(s, rRepository, dp);
        dp.setP(p);

        return dp;
    }

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @GeneratedValue
    private Long id;

    private Integer i0;

    private Date d0;
    private Date d1;

    @ManyToOne(fetch = FetchType.LAZY)
    private DPR dpr;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private D d;

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

    public Integer getI0()
    {
        return i0;
    }

    public void setI0(Integer i0)
    {
        this.i0 = i0;
    }

    public Date getD0()
    {
        return d0;
    }

    public void setD0(Date d0)
    {
        this.d0 = d0;
    }

    public Date getD1()
    {
        return d1;
    }

    public void setD1(Date d1)
    {
        this.d1 = d1;
    }

    public D getD()
    {
        return d;
    }

    public void setD(D d)
    {
        this.d = d;
    }

    public P getP()
    {
        return p;
    }

    public void setP(P p)
    {
        this.p = p;
    }

    public DPR getDpr()
    {
        return dpr;
    }

    public void setDpr(DPR dpr)
    {
        this.dpr = dpr;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
