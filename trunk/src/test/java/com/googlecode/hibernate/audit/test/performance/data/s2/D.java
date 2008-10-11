package com.googlecode.hibernate.audit.test.performance.data.s2;

import com.googlecode.hibernate.audit.test.performance.util.Util;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToMany;
import javax.persistence.ManyToOne;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Entity
@Table(name = "D")
public class D
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    /**
     * Reference data repository size.
     */
    public static D create(Scenario s, RRepository rRepository) throws Exception
    {
        D d = new D();

        Util.fillPrimitives(d);
        rRepository.fillReferences(d);

        for(int i = 0; i < s.getPPerDCount(); i ++)
        {
            DP dp = DP.create(s, rRepository, d);
            d.getDps().add(dp);
        }

        return d;
    }

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @GeneratedValue
    private Long id;

    private Long l0;
    private Long l1;

    private Date d0;

    private String s0;

    private Boolean b0;
    private Boolean b1;
    private Boolean b2;
    private Boolean b3;
    private Boolean b4;

    @ManyToOne(fetch = FetchType.LAZY)
    private MT mt;

    @ManyToOne(fetch = FetchType.LAZY)
    private CS cs;

    @ManyToOne(fetch = FetchType.LAZY)
    private MDL mdl;

    @ManyToOne(fetch = FetchType.LAZY)
    private MD md;

    @ManyToOne(fetch = FetchType.LAZY)
    private CRD crd;

    @OneToMany(mappedBy = "d", cascade = CascadeType.ALL)
    private List<DP> dps;

    // Constructors --------------------------------------------------------------------------------

    D()
    {
        dps = new ArrayList<DP>();
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

    public Long getL0()
    {
        return l0;
    }

    public void setL0(Long l0)
    {
        this.l0 = l0;
    }

    public Long getL1()
    {
        return l1;
    }

    public void setL1(Long l1)
    {
        this.l1 = l1;
    }

    public Date getD0()
    {
        return d0;
    }

    public void setD0(Date d0)
    {
        this.d0 = d0;
    }

    public String getS0()
    {
        return s0;
    }

    public void setS0(String s0)
    {
        this.s0 = s0;
    }

    public Boolean getB0()
    {
        return b0;
    }

    public void setB0(Boolean b0)
    {
        this.b0 = b0;
    }

    public Boolean getB1()
    {
        return b1;
    }

    public void setB1(Boolean b1)
    {
        this.b1 = b1;
    }

    public Boolean getB2()
    {
        return b2;
    }

    public void setB2(Boolean b2)
    {
        this.b2 = b2;
    }

    public Boolean getB3()
    {
        return b3;
    }

    public void setB3(Boolean b3)
    {
        this.b3 = b3;
    }

    public Boolean getB4()
    {
        return b4;
    }

    public void setB4(Boolean b4)
    {
        this.b4 = b4;
    }

    public MT getMt()
    {
        return mt;
    }

    public void setMt(MT mt)
    {
        this.mt = mt;
    }

    public CS getCs()
    {
        return cs;
    }

    public void setCs(CS cs)
    {
        this.cs = cs;
    }

    public MDL getMdl()
    {
        return mdl;
    }

    public void setMdl(MDL mdl)
    {
        this.mdl = mdl;
    }

    public MD getMd()
    {
        return md;
    }

    public void setMd(MD md)
    {
        this.md = md;
    }

    public CRD getCrd()
    {
        return crd;
    }

    public void setCrd(CRD crd)
    {
        this.crd = crd;
    }

    public List<DP> getDps()
    {
        return dps;
    }

    public void setDps(List<DP> dps)
    {
        this.dps = dps;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
