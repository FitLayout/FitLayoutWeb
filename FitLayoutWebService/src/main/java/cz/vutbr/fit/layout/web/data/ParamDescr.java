/**
 * ParamDescr.java
 *
 * Created on 28. 2. 2021, 18:35:11 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import cz.vutbr.fit.layout.api.Parameter;

/**
 * 
 * @author burgetr
 */
public class ParamDescr
{
    public String name;
    public String type;

    public ParamDescr(Parameter param, String type)
    {
        name = param.getName();
        this.type = type; 
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }
}
