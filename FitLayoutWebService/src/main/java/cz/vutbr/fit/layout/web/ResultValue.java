/**
 * ResultValue.java
 *
 * Created on 26. 9. 2020, 13:25:07 by burgetr
 */
package cz.vutbr.fit.layout.web;

/**
 * 
 * @author burgetr
 */
public class ResultValue
{
    Object result;

    public ResultValue(Object result)
    {
        this.result = result;
    }

    public Object getResult()
    {
        return result;
    }

    public void setResult(Object result)
    {
        this.result = result;
    }

}
