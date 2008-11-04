package com.googlecode.hibernate.audit.test.write_collision_to_phase_out.data;

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

    public static void applyChanges(Root base, Root modified)
    {
        base.setS(modified.getS());

        Shared msh = modified.getShared();

        if (msh == null)
        {
            base.setShared(null);
        }
        else
        {
            Shared bsh = base.getShared();
            bsh.setS(msh.getS());
        }

        List<A> mas = modified.getAs();

        if (mas == null)
        {
            base.setAs(null);
        }
        else
        {
            List<A> bas = base.getAs();

            if (bas == null)
            {
                base.setAs(mas);
            }
            else
            {
                applyAListChanges(bas, mas);
            }
        }


        List<B> mbs = modified.getBs();

        if (mbs == null)
        {
            base.setBs(null);
        }
        else
        {
            List<B> bbs = base.getBs();

            if (bbs == null)
            {
                base.setBs(mbs);
            }
            else
            {
                applyBListChanges(bbs, mbs);
            }
        }
    }

    public static void applyAListChanges(List<A> base, List<A> modified)
    {
        if (base.size() != modified.size())
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        for(int i = 0; i < base.size(); i ++)
        {
            A ab = base.get(i);
            A am = modified.get(i);

            if (!ab.getId().equals(am.getId()))
            {
                throw new RuntimeException("NOT YET IMPLEMENTED");
            }

            ab.setS(am.getS());
        }
    }

    public static void applyBListChanges(List<B> base, List<B> modified)
    {
        if (base.size() != modified.size())
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        for(int i = 0; i < base.size(); i ++)
        {
            B bb = base.get(i);
            B bm = modified.get(i);

            if (!bb.getId().equals(bm.getId()))
            {
                throw new RuntimeException("NOT YET IMPLEMENTED");
            }

            bb.setS(bm.getS());
        }
    }

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
