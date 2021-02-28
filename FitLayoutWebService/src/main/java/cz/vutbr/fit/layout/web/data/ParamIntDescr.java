/**
 * PrarmIntDescr.java
 *
 * Created on 28. 2. 2021, 18:40:56 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import cz.vutbr.fit.layout.impl.ParameterInt;

/**
 * 
 * @author burgetr
 */
public class ParamIntDescr extends ParamDescr
{
    public int minValue;
    public int maxValue;

    public ParamIntDescr(ParameterInt param)
    {
        super(param, "int");
        minValue = param.getMinValue();
        maxValue = param.getMaxValue();
    }
    
    public int getMinValue()
    {
        return minValue;
    }

    public int getMaxValue()
    {
        return maxValue;
    }

}
