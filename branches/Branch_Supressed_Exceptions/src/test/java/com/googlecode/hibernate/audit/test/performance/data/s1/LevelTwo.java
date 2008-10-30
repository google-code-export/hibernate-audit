package com.googlecode.hibernate.audit.test.performance.data.s1;

import com.googlecode.hibernate.audit.test.performance.util.Util;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Entity
@Table(name = "LEVEL_TWO")
public class LevelTwo
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static LevelTwo random(int stringLength) throws Exception
    {
        LevelTwo result = new LevelTwo();

        for(int i = 0; i < 10; i ++)
        {
            Method m = LevelTwo.class.getMethod("setS" + i, String.class);
            m.invoke(result, Util.randomString(stringLength));
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
    private LevelOne levelOne;

    // Constructors --------------------------------------------------------------------------------

    public LevelTwo()
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

    public LevelOne getLevelOne()
    {
        return levelOne;
    }

    public void setLevelOne(LevelOne levelOne)
    {
        this.levelOne = levelOne;
    }

    @Override
    public String toString()
    {
        return
            "LevelTwo [" + id + "]\n" +
            "         " + s0 + "\n" +
            "         " + s1 + "\n" +
            "         " + s2 + "\n" +
            "         " + s3 + "\n" +
            "         " + s4 + "\n" +
            "         " + s5 + "\n" +
            "         " + s6 + "\n" +
            "         " + s7 + "\n" +
            "         " + s8+ "\n" +
            "         " + s9;

    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
