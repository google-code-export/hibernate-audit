package com.googlecode.hibernate.audit.test.write_collision.data;

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

    // Constructors --------------------------------------------------------------------------------

    public Root()
    {
        as = new ArrayList<A>();
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

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
