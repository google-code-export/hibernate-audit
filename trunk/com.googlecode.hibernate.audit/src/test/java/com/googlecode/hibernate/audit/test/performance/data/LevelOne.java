package com.googlecode.hibernate.audit.test.performance.data;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.persistence.ManyToOne;
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
@Table(name = "LEVEL_ONE")
public class LevelOne
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static LevelOne random(int stringLength, int collectionSize) throws Exception
    {
        LevelOne result = new LevelOne();

        for(int i = 0; i < 10; i ++)
        {
            Method m = LevelOne.class.getMethod("setS" + i, String.class);
            m.invoke(result, LevelTwo.randomString(stringLength));

            m = LevelOne.class.getMethod("getLevelTwos" + i);
            List<LevelTwo> levelTwos =  (List<LevelTwo>)m.invoke(result);

            for(int j = 0; j < collectionSize; j ++)
            {
                LevelTwo levelTwo = LevelTwo.random(stringLength);
                levelTwos.add(levelTwo);
                levelTwo.setLevelOne(result);                
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

    @ManyToOne
    private Root root;

    @OneToMany(mappedBy = "levelOne", cascade = CascadeType.ALL)
    private List<LevelTwo> levelTwos0;
    @OneToMany(mappedBy = "levelOne", cascade = CascadeType.ALL)
    private List<LevelTwo> levelTwos1;
    @OneToMany(mappedBy = "levelOne", cascade = CascadeType.ALL)
    private List<LevelTwo> levelTwos2;
    @OneToMany(mappedBy = "levelOne", cascade = CascadeType.ALL)
    private List<LevelTwo> levelTwos3;
    @OneToMany(mappedBy = "levelOne", cascade = CascadeType.ALL)
    private List<LevelTwo> levelTwos4;
    @OneToMany(mappedBy = "levelOne", cascade = CascadeType.ALL)
    private List<LevelTwo> levelTwos5;
    @OneToMany(mappedBy = "levelOne", cascade = CascadeType.ALL)
    private List<LevelTwo> levelTwos6;
    @OneToMany(mappedBy = "levelOne", cascade = CascadeType.ALL)
    private List<LevelTwo> levelTwos7;
    @OneToMany(mappedBy = "levelOne", cascade = CascadeType.ALL)
    private List<LevelTwo> levelTwos8;
    @OneToMany(mappedBy = "levelOne", cascade = CascadeType.ALL)
    private List<LevelTwo> levelTwos9;

    // Constructors --------------------------------------------------------------------------------

    public LevelOne()
    {
        levelTwos0 = new ArrayList<LevelTwo>();
        levelTwos1 = new ArrayList<LevelTwo>();
        levelTwos2 = new ArrayList<LevelTwo>();
        levelTwos3 = new ArrayList<LevelTwo>();
        levelTwos4 = new ArrayList<LevelTwo>();
        levelTwos5 = new ArrayList<LevelTwo>();
        levelTwos6 = new ArrayList<LevelTwo>();
        levelTwos7 = new ArrayList<LevelTwo>();
        levelTwos8 = new ArrayList<LevelTwo>();
        levelTwos9 = new ArrayList<LevelTwo>();
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

    public Root getRoot()
    {
        return root;
    }

    public void setRoot(Root root)
    {
        this.root = root;
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

    public List<LevelTwo> getLevelTwos0()
    {
        return levelTwos0;
    }

    public void setLevelTwos0(List<LevelTwo> levelTwos0)
    {
        this.levelTwos0 = levelTwos0;
    }

    public List<LevelTwo> getLevelTwos1()
    {
        return levelTwos1;
    }

    public void setLevelTwos1(List<LevelTwo> levelTwos1)
    {
        this.levelTwos1 = levelTwos1;
    }

    public List<LevelTwo> getLevelTwos2()
    {
        return levelTwos2;
    }

    public void setLevelTwos2(List<LevelTwo> levelTwos2)
    {
        this.levelTwos2 = levelTwos2;
    }

    public List<LevelTwo> getLevelTwos3()
    {
        return levelTwos3;
    }

    public void setLevelTwos3(List<LevelTwo> levelTwos3)
    {
        this.levelTwos3 = levelTwos3;
    }

    public List<LevelTwo> getLevelTwos4()
    {
        return levelTwos4;
    }

    public void setLevelTwos4(List<LevelTwo> levelTwos4)
    {
        this.levelTwos4 = levelTwos4;
    }

    public List<LevelTwo> getLevelTwos5()
    {
        return levelTwos5;
    }

    public void setLevelTwos5(List<LevelTwo> levelTwos5)
    {
        this.levelTwos5 = levelTwos5;
    }

    public List<LevelTwo> getLevelTwos6()
    {
        return levelTwos6;
    }

    public void setLevelTwos6(List<LevelTwo> levelTwos6)
    {
        this.levelTwos6 = levelTwos6;
    }

    public List<LevelTwo> getLevelTwos7()
    {
        return levelTwos7;
    }

    public void setLevelTwos7(List<LevelTwo> levelTwos7)
    {
        this.levelTwos7 = levelTwos7;
    }

    public List<LevelTwo> getLevelTwos8()
    {
        return levelTwos8;
    }

    public void setLevelTwos8(List<LevelTwo> levelTwos8)
    {
        this.levelTwos8 = levelTwos8;
    }

    public List<LevelTwo> getLevelTwos9()
    {
        return levelTwos9;
    }

    public void setLevelTwos9(List<LevelTwo> levelTwos9)
    {
        this.levelTwos9 = levelTwos9;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
