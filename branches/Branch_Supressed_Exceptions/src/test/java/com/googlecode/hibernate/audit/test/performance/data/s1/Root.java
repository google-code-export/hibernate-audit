package com.googlecode.hibernate.audit.test.performance.data.s1;

import com.googlecode.hibernate.audit.test.performance.util.Util;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;

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

    public static Root random(int stringLength, int rootCollectionSize, int levelOneCollectionSize)
        throws Exception
    {
        Root result = new Root();

        for(int i = 0; i < 10; i ++)
        {
            Method m = Root.class.getMethod("setS" + i, String.class);
            m.invoke(result, Util.randomString(stringLength));

            m = Root.class.getMethod("setLevelTwo" + i, LevelTwo.class);
            LevelTwo lt = LevelTwo.random(stringLength);
            m.invoke(result, lt);

            m = Root.class.getMethod("getLevelOnes" + i);
            List<LevelOne> levelOnes = (List<LevelOne>)m.invoke(result);

            for(int j = 0; j < rootCollectionSize; j ++)
            {
                LevelOne levelOne = LevelOne.random(stringLength, levelOneCollectionSize);
                levelOnes.add(levelOne);
                levelOne.setRoot(result);
            }
        }

        return result;
    }

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @GeneratedValue
    private Long id;

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

    @ManyToOne(cascade = CascadeType.ALL)
    private LevelTwo levelTwo0;
    @ManyToOne(cascade = CascadeType.ALL)
    private LevelTwo levelTwo1;
    @ManyToOne(cascade = CascadeType.ALL)
    private LevelTwo levelTwo2;
    @ManyToOne(cascade = CascadeType.ALL)
    private LevelTwo levelTwo3;
    @ManyToOne(cascade = CascadeType.ALL)
    private LevelTwo levelTwo4;
    @ManyToOne(cascade = CascadeType.ALL)
    private LevelTwo levelTwo5;
    @ManyToOne(cascade = CascadeType.ALL)
    private LevelTwo levelTwo6;
    @ManyToOne(cascade = CascadeType.ALL)
    private LevelTwo levelTwo7;
    @ManyToOne(cascade = CascadeType.ALL)
    private LevelTwo levelTwo8;
    @ManyToOne(cascade = CascadeType.ALL)
    private LevelTwo levelTwo9;

    @OneToMany(mappedBy = "root", cascade = CascadeType.ALL)
    private List<LevelOne> levelOnes0;
    @OneToMany(mappedBy = "root", cascade = CascadeType.ALL)
    private List<LevelOne> levelOnes1;
    @OneToMany(mappedBy = "root", cascade = CascadeType.ALL)
    private List<LevelOne> levelOnes2;
    @OneToMany(mappedBy = "root", cascade = CascadeType.ALL)
    private List<LevelOne> levelOnes3;
    @OneToMany(mappedBy = "root", cascade = CascadeType.ALL)
    private List<LevelOne> levelOnes4;
    @OneToMany(mappedBy = "root", cascade = CascadeType.ALL)
    private List<LevelOne> levelOnes5;
    @OneToMany(mappedBy = "root", cascade = CascadeType.ALL)
    private List<LevelOne> levelOnes6;
    @OneToMany(mappedBy = "root", cascade = CascadeType.ALL)
    private List<LevelOne> levelOnes7;
    @OneToMany(mappedBy = "root", cascade = CascadeType.ALL)
    private List<LevelOne> levelOnes8;
    @OneToMany(mappedBy = "root", cascade = CascadeType.ALL)
    private List<LevelOne> levelOnes9;

    // Constructors --------------------------------------------------------------------------------

    public Root()
    {
        levelOnes0 = new ArrayList<LevelOne>();
        levelOnes1 = new ArrayList<LevelOne>();
        levelOnes2 = new ArrayList<LevelOne>();
        levelOnes3 = new ArrayList<LevelOne>();
        levelOnes4 = new ArrayList<LevelOne>();
        levelOnes5 = new ArrayList<LevelOne>();
        levelOnes6 = new ArrayList<LevelOne>();
        levelOnes7 = new ArrayList<LevelOne>();
        levelOnes8 = new ArrayList<LevelOne>();
        levelOnes9 = new ArrayList<LevelOne>();
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

    public LevelTwo getLevelTwo0()
    {
        return levelTwo0;
    }

    public void setLevelTwo0(LevelTwo levelTwo0)
    {
        this.levelTwo0 = levelTwo0;
    }

    public LevelTwo getLevelTwo1()
    {
        return levelTwo1;
    }

    public void setLevelTwo1(LevelTwo levelTwo1)
    {
        this.levelTwo1 = levelTwo1;
    }

    public LevelTwo getLevelTwo2()
    {
        return levelTwo2;
    }

    public void setLevelTwo2(LevelTwo levelTwo2)
    {
        this.levelTwo2 = levelTwo2;
    }

    public LevelTwo getLevelTwo3()
    {
        return levelTwo3;
    }

    public void setLevelTwo3(LevelTwo levelTwo3)
    {
        this.levelTwo3 = levelTwo3;
    }

    public LevelTwo getLevelTwo4()
    {
        return levelTwo4;
    }

    public void setLevelTwo4(LevelTwo levelTwo4)
    {
        this.levelTwo4 = levelTwo4;
    }

    public LevelTwo getLevelTwo5()
    {
        return levelTwo5;
    }

    public void setLevelTwo5(LevelTwo levelTwo5)
    {
        this.levelTwo5 = levelTwo5;
    }

    public LevelTwo getLevelTwo6()
    {
        return levelTwo6;
    }

    public void setLevelTwo6(LevelTwo levelTwo6)
    {
        this.levelTwo6 = levelTwo6;
    }

    public LevelTwo getLevelTwo7()
    {
        return levelTwo7;
    }

    public void setLevelTwo7(LevelTwo levelTwo7)
    {
        this.levelTwo7 = levelTwo7;
    }

    public LevelTwo getLevelTwo8()
    {
        return levelTwo8;
    }

    public void setLevelTwo8(LevelTwo levelTwo8)
    {
        this.levelTwo8 = levelTwo8;
    }

    public LevelTwo getLevelTwo9()
    {
        return levelTwo9;
    }

    public void setLevelTwo9(LevelTwo levelTwo9)
    {
        this.levelTwo9 = levelTwo9;
    }

    public List<LevelOne> getLevelOnes0()
    {
        return levelOnes0;
    }

    public void setLevelOnes0(List<LevelOne> levelOnes0)
    {
        this.levelOnes0 = levelOnes0;
    }

    public List<LevelOne> getLevelOnes1()
    {
        return levelOnes1;
    }

    public void setLevelOnes1(List<LevelOne> levelOnes1)
    {
        this.levelOnes1 = levelOnes1;
    }

    public List<LevelOne> getLevelOnes2()
    {
        return levelOnes2;
    }

    public void setLevelOnes2(List<LevelOne> levelOnes2)
    {
        this.levelOnes2 = levelOnes2;
    }

    public List<LevelOne> getLevelOnes3()
    {
        return levelOnes3;
    }

    public void setLevelOnes3(List<LevelOne> levelOnes3)
    {
        this.levelOnes3 = levelOnes3;
    }

    public List<LevelOne> getLevelOnes4()
    {
        return levelOnes4;
    }

    public void setLevelOnes4(List<LevelOne> levelOnes4)
    {
        this.levelOnes4 = levelOnes4;
    }

    public List<LevelOne> getLevelOnes5()
    {
        return levelOnes5;
    }

    public void setLevelOnes5(List<LevelOne> levelOnes5)
    {
        this.levelOnes5 = levelOnes5;
    }

    public List<LevelOne> getLevelOnes6()
    {
        return levelOnes6;
    }

    public void setLevelOnes6(List<LevelOne> levelOnes6)
    {
        this.levelOnes6 = levelOnes6;
    }

    public List<LevelOne> getLevelOnes7()
    {
        return levelOnes7;
    }

    public void setLevelOnes7(List<LevelOne> levelOnes7)
    {
        this.levelOnes7 = levelOnes7;
    }

    public List<LevelOne> getLevelOnes8()
    {
        return levelOnes8;
    }

    public void setLevelOnes8(List<LevelOne> levelOnes8)
    {
        this.levelOnes8 = levelOnes8;
    }

    public List<LevelOne> getLevelOnes9()
    {
        return levelOnes9;
    }

    public void setLevelOnes9(List<LevelOne> levelOnes9)
    {
        this.levelOnes9 = levelOnes9;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
