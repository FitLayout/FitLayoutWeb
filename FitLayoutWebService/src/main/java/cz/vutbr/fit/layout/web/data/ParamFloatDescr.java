/**
 * ParamFloatDescr.java
 *
 * Created on 28. 2. 2021, 18:45:09 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import cz.vutbr.fit.layout.impl.ParameterFloat;

/**
 * 
 * @author burgetr
 */
public class ParamFloatDescr extends ParamDescr
{
    public float minValue;
    public float maxValue;

    public ParamFloatDescr(ParameterFloat param)
    {
        super(param, "float");
        minValue = param.getMinValue();
        maxValue = param.getMaxValue();
    }
    
    public float getMinValue()
    {
        return minValue;
    }

    public float getMaxValue()
    {
        return maxValue;
    }


}
