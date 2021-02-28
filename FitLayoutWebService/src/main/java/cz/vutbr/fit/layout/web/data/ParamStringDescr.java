/**
 * ParamStringDescr.java
 *
 * Created on 28. 2. 2021, 18:43:04 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import cz.vutbr.fit.layout.impl.ParameterString;

/**
 * 
 * @author burgetr
 */
public class ParamStringDescr extends ParamDescr
{
    public int minLength;
    public int maxLength;

    public ParamStringDescr(ParameterString param)
    {
        super(param, "string");
        minLength = param.getMinLength();
        maxLength = param.getMaxLength();
    }
    
    public int getMinLength()
    {
        return minLength;
    }

    public int getMaxLength()
    {
        return maxLength;
    }

}
