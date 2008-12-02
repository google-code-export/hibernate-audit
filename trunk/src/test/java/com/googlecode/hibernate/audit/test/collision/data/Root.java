package com.googlecode.hibernate.audit.test.collision.data;

import com.googlecode.hibernate.audit.annotations.Audited;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
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
@Table(name = "ROOT")
@Audited
public class Root
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @GeneratedValue
    private Long id;

    private String s;

    @ManyToOne(cascade = CascadeType.ALL)
    private Shared shared;

    @OneToMany(mappedBy = "root", cascade = CascadeType.ALL)
    private List<A> as;

    @OneToMany(mappedBy = "root", cascade = CascadeType.ALL)
    private List<B> bs;

    // Constructors --------------------------------------------------------------------------------

    public Root()
    {
        as = new ArrayList<A>();
        bs = new ArrayList<B>();
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

    public String getS()
    {
        return s;
    }

    public void setS(String s)
    {
        this.s = s;
    }

    public Shared getShared()
    {
        return shared;
    }

    public void setShared(Shared shared)
    {
        this.shared = shared;
    }

    public List<A> getAs()
    {
        return as;
    }

    public void setAs(List<A> as)
    {
        this.as = as;
    }

    public List<B> getBs()
    {
        return bs;
    }

    public void setBs(List<B> bs)
    {
        this.bs = bs;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
