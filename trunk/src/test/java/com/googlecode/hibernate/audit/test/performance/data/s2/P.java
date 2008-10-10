package com.googlecode.hibernate.audit.test.performance.data.s2;

import com.googlecode.hibernate.audit.test.performance.Util;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Entity
@Table(name = "P")
public class P
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static P create(Scenario s, RRepository rRepository, DP dp) throws Exception
    {
        P p = new P();

        Util.fillPrimitives(dp);
        rRepository.fillReferences(dp);

        p.getDps().add(dp);

        return p;
    }

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @GeneratedValue
    private Long id;

    private Date d0;
    private Boolean b0;

    private String s0;
    private String s1;
    private String s2;
    private String s3;
    private String s4;
    private String s5;
    private String s6;
    private String s7;
    private String s8;
    private String s9;
    private String s10;
    private String s11;
    private String s12;
    private String s13;
    private String s14;
    private String s15;
    private String s16;
    private String s17;
    private String s18;
    private String s19;

    @ManyToOne(fetch = FetchType.LAZY)
    private PT pt;

    @ManyToOne(fetch = FetchType.LAZY)
    private GIT git;

    @ManyToOne(fetch = FetchType.LAZY)
    private SC sc;

    @ManyToOne(fetch = FetchType.LAZY)
    private CC cc;

    @ManyToOne(fetch = FetchType.LAZY)
    private PCC pcc;

    @ManyToOne(fetch = FetchType.LAZY)
    private NS ns;

    @OneToMany(mappedBy = "p", cascade = CascadeType.ALL)
    private List<DP> dps;

    // Constructors --------------------------------------------------------------------------------

    P()
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

    public Date getD0()
    {
        return d0;
    }

    public void setD0(Date d0)
    {
        this.d0 = d0;
    }

    public Boolean getB0()
    {
        return b0;
    }

    public void setB0(Boolean b0)
    {
        this.b0 = b0;
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

    public String getS3()
    {
        return s3;
    }

    public void setS3(String s3)
    {
        this.s3 = s3;
    }

    public String getS4()
    {
        return s4;
    }

    public void setS4(String s4)
    {
        this.s4 = s4;
    }

    public String getS5()
    {
        return s5;
    }

    public void setS5(String s5)
    {
        this.s5 = s5;
    }

    public String getS6()
    {
        return s6;
    }

    public void setS6(String s6)
    {
        this.s6 = s6;
    }

    public String getS7()
    {
        return s7;
    }

    public void setS7(String s7)
    {
        this.s7 = s7;
    }

    public String getS8()
    {
        return s8;
    }

    public void setS8(String s8)
    {
        this.s8 = s8;
    }

    public String getS9()
    {
        return s9;
    }

    public void setS9(String s9)
    {
        this.s9 = s9;
    }

    public String getS10()
    {
        return s10;
    }

    public void setS10(String s10)
    {
        this.s10 = s10;
    }

    public String getS11()
    {
        return s11;
    }

    public void setS11(String s11)
    {
        this.s11 = s11;
    }

    public String getS12()
    {
        return s12;
    }

    public void setS12(String s12)
    {
        this.s12 = s12;
    }

    public String getS13()
    {
        return s13;
    }

    public void setS13(String s13)
    {
        this.s13 = s13;
    }

    public String getS14()
    {
        return s14;
    }

    public void setS14(String s14)
    {
        this.s14 = s14;
    }

    public String getS15()
    {
        return s15;
    }

    public void setS15(String s15)
    {
        this.s15 = s15;
    }

    public String getS16()
    {
        return s16;
    }

    public void setS16(String s16)
    {
        this.s16 = s16;
    }

    public String getS17()
    {
        return s17;
    }

    public void setS17(String s17)
    {
        this.s17 = s17;
    }

    public String getS18()
    {
        return s18;
    }

    public void setS18(String s18)
    {
        this.s18 = s18;
    }

    public String getS19()
    {
        return s19;
    }

    public void setS19(String s19)
    {
        this.s19 = s19;
    }

    public List<DP> getDps()
    {
        return dps;
    }

    public void setDps(List<DP> dps)
    {
        this.dps = dps;
    }

    public PT getPt()
    {
        return pt;
    }

    public void setPt(PT pt)
    {
        this.pt = pt;
    }

    public GIT getGit()
    {
        return git;
    }

    public void setGit(GIT git)
    {
        this.git = git;
    }

    public SC getSc()
    {
        return sc;
    }

    public void setSc(SC sc)
    {
        this.sc = sc;
    }

    public CC getCc()
    {
        return cc;
    }

    public void setCc(CC cc)
    {
        this.cc = cc;
    }

    public PCC getPcc()
    {
        return pcc;
    }

    public void setPcc(PCC pcc)
    {
        this.pcc = pcc;
    }

    public NS getNs()
    {
        return ns;
    }

    public void setNs(NS ns)
    {
        this.ns = ns;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
