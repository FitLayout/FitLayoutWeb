/**
 * QuadrupleData.java
 *
 * Created on 13. 4. 2021, 8:30:12 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

/**
 * Data about a quadruple to be added.
 * 
 * @author burgetr
 */
public class QuadrupleData
{
    public String s;
    public String p;
    public String o;
    public Object value;
    public String artifact;

    public String getS()
    {
        return s;
    }

    public void setS(String s)
    {
        this.s = s;
    }

    public String getP()
    {
        return p;
    }

    public void setP(String p)
    {
        this.p = p;
    }

    public String getO()
    {
        return o;
    }

    public void setO(String o)
    {
        this.o = o;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public String getArtifact()
    {
        return artifact;
    }

    public void setArtifact(String artifact)
    {
        this.artifact = artifact;
    }
    
    public boolean isOk()
    {
        return (s != null && p != null && artifact != null && (o != null || value != null));
    }

}
